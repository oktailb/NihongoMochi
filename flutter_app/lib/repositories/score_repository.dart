import 'dart:convert';
import 'package:drift/drift.dart';
import '../db/database.dart';
import 'settings_repository.dart';

enum ScoreType { recognition, reading, writing, grammar }

class ScoreRepository {
  final MochiDatabase db;
  final SettingsRepository settingsRepo;

  ScoreRepository(this.db, this.settingsRepo);

  // Conversion helper: enum to String (matching Kotlin names)
  String _typeToString(ScoreType type) {
    return type.name.toUpperCase();
  }

  /// Exports all user scores and list items to a JSON string for backup.
  Future<String> exportDataJson() async {
    final allScores = await db.select(db.learningScoreEntities).get();
    final allLists = await db.select(db.userLists).get();

    final scoresData = allScores
        .map((s) => {
              'key': s.key,
              'type': s.type,
              'successes': s.successes,
              'failures': s.failures,
              'lastReviewDate': s.lastReviewDate,
            })
        .toList();

    final listsData = allLists
        .map((l) => {
              'listName': l.listName,
              'itemKey': l.itemKey,
            })
        .toList();

    return jsonEncode({
      'version': 1,
      'exportedAt': DateTime.now().millisecondsSinceEpoch,
      'scores': scoresData,
      'userLists': listsData,
    });
  }

  /// Restores user scores and list items from a JSON backup string.
  Future<bool> importDataJson(String jsonStr) async {
    try {
      final data = jsonDecode(jsonStr) as Map<String, dynamic>;
      final scores = data['scores'] as List<dynamic>?;
      if (scores != null) {
        for (final item in scores) {
          final m = item as Map<String, dynamic>;
          await db.into(db.learningScoreEntities).insertOnConflictUpdate(
                LearningScoreEntity(
                  key: m['key'] as String,
                  type: m['type'] as String,
                  successes: (m['successes'] as num).toInt(),
                  failures: (m['failures'] as num).toInt(),
                  lastReviewDate: (m['lastReviewDate'] as num).toInt(),
                ),
              );
        }
      }

      final lists = data['userLists'] as List<dynamic>?;
      if (lists != null) {
        for (final item in lists) {
          final m = item as Map<String, dynamic>;
          await db.into(db.userLists).insertOnConflictUpdate(
                UserList(
                  listName: m['listName'] as String,
                  itemKey: m['itemKey'] as String,
                ),
              );
        }
      }
      return true;
    } catch (e) {
      return false;
    }
  }


  Future<void> saveScore({
    required String key,
    required bool wasCorrect,
    required ScoreType type,
  }) async {
    final typeStr = _typeToString(type);

    // Get current score
    final current = await (db.select(db.learningScoreEntities)
          ..where((t) => t.key.equals(key) & t.type.equals(typeStr)))
        .getSingleOrNull();

    final successes = (current?.successes ?? 0) + (wasCorrect ? 1 : 0);
    final failures = (current?.failures ?? 0) + (wasCorrect ? 0 : 1);
    final now = DateTime.now().millisecondsSinceEpoch;

    await db.into(db.learningScoreEntities).insertOnConflictUpdate(
      LearningScoreEntity(
        key: key,
        type: typeStr,
        successes: successes,
        failures: failures,
        lastReviewDate: now,
      ),
    );

    // Automatique add/remove from revision lists
    final shouldAddWrongAnswers = settingsRepo.shouldAddWrongAnswers();
    final shouldRemoveGoodAnswers = settingsRepo.shouldRemoveGoodAnswers();

    final targetList = _getListNameForType(type);

    if (!wasCorrect && shouldAddWrongAnswers) {
      await addItemToList(targetList, key);
    } else if (wasCorrect && shouldRemoveGoodAnswers) {
      final balance = successes - failures;
      if (balance >= 10) {
        await removeItemFromList(targetList, key);
      }
    }
  }

  String _getListNameForType(ScoreType type) {
    switch (type) {
      case ScoreType.recognition: return "Recognition_List";
      case ScoreType.reading: return "Reading_List";
      case ScoreType.writing: return "Writing_List";
      case ScoreType.grammar: return "Grammar_List";
    }
  }

  Future<LearningScoreEntity?> getScore(String key, ScoreType type) async {
    return (db.select(db.learningScoreEntities)
          ..where((t) => t.key.equals(key) & t.type.equals(_typeToString(type))))
        .getSingleOrNull();
  }

  Future<List<LearningScoreEntity>> getAllScores(ScoreType type) async {
    return (db.select(db.learningScoreEntities)
          ..where((t) => t.type.equals(_typeToString(type))))
        .get();
  }

  // --- Gestion des listes (Revision Lists) ---

  Future<void> addItemToList(String listName, String itemKey) async {
    await db.into(db.userLists).insertOnConflictUpdate(
      UserList(listName: listName, itemKey: itemKey),
    );
  }

  Future<void> removeItemFromList(String listName, String itemKey) async {
    await (db.delete(db.userLists)
          ..where((t) => t.listName.equals(listName) & t.itemKey.equals(itemKey)))
        .go();
  }

  Future<List<String>> getListItems(String listName) async {
    final rows = await (db.select(db.userLists)
          ..where((t) => t.listName.equals(listName)))
        .get();
    return rows.map((r) => r.itemKey).toList();
  }

  Future<bool> isInList(String listName, String itemKey) async {
    final row = await (db.select(db.userLists)
          ..where((t) => t.listName.equals(listName) & t.itemKey.equals(itemKey)))
        .getSingleOrNull();
    return row != null;
  }

  /// Applies decay to scores that haven't been reviewed for a while (1+ weeks).
  /// Reduces successes by 10% per unreviewed week (max 50% reduction).
  /// Returns true if any score was decayed/updated, false otherwise.
  Future<bool> decayScores() async {
    bool anyDecayed = false;
    final now = DateTime.now().millisecondsSinceEpoch;
    const oneWeekMs = 7 * 24 * 60 * 60 * 1000;

    final allScores = await db.select(db.learningScoreEntities).get();
    for (final entity in allScores) {
      if (entity.lastReviewDate <= 0) continue;
      final weeksPassed = (now - entity.lastReviewDate) ~/ oneWeekMs;
      if (weeksPassed >= 1 && entity.successes > 0) {
        final decayPercent = (weeksPassed * 0.10).clamp(0.0, 0.50);
        final newSuccesses = (entity.successes * (1.0 - decayPercent)).round();

        await db.into(db.learningScoreEntities).insertOnConflictUpdate(
          LearningScoreEntity(
            key: entity.key,
            type: entity.type,
            successes: newSuccesses,
            failures: entity.failures,
            lastReviewDate: now,
          ),
        );
        anyDecayed = true;
      }
    }
    return anyDecayed;
  }

  // --- Gestion de l'historique des jeux (Game Histories) ---


  Future<void> saveGameHistory({
    required String gameType,
    required int score,
    int? moves,
    int? timeSeconds,
    int? maxSequence,
    int? totalPairs,
    int? rows,
    int? wordsFound,
    String? metadata,
  }) async {
    final now = DateTime.now().millisecondsSinceEpoch;
    await db.into(db.gameHistories).insert(
          GameHistoriesCompanion.insert(
            gameType: gameType,
            score: score,
            moves: Value(moves),
            timeSeconds: Value(timeSeconds),
            maxSequence: Value(maxSequence),
            totalPairs: Value(totalPairs),
            rows: Value(rows),
            wordsFound: Value(wordsFound),
            timestamp: now,
            metadata: Value(metadata),
          ),
        );
  }

  Future<List<GameHistory>> getGameHistory(String gameType) async {
    return (db.select(db.gameHistories)
          ..where((t) => t.gameType.equals(gameType))
          ..orderBy([(t) => OrderingTerm(expression: t.timestamp, mode: OrderingMode.desc)])
          ..limit(10))
        .get();
  }
}
