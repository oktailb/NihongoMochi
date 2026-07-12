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
}
