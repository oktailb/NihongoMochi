import 'package:flutter/material.dart';
import '../models/quiz_models.dart';
import '../models/dictionary.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../services/recognition_game_engine.dart';
import '../services/audio_service.dart';

class RecognitionQuizProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;
  final LevelContentProvider _contentProvider;
  final AudioService _audioService;
  late final RecognitionGameEngine _engine;

  RecognitionQuizProvider(
    this._dictionaryRepo,
    this._scoreRepo,
    this._contentProvider,
    this._audioService,
  ) {
    _engine = RecognitionGameEngine(_scoreRepo);
  }

  RecognitionGameEngine get engine => _engine;

  Future<void> initializeGame({
    required String levelId,
    required String gameMode,
    required String readingMode,
    required int quizSize,
    required String locale,
  }) async {
    _engine.resetState();
    _engine.gameMode = gameMode;
    _engine.readingMode = readingMode;

    final characters = await _contentProvider.getItemsForLevel(levelId, ScoreType.recognition, locale);
    final allDictionary = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in allDictionary) k.character: k};

    final allForLevel = characters.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList();

    // Filter by mastery like in Kotlin
    final List<DictionaryItem> filtered = [];
    for (var k in allForLevel) {
      final entity = await _scoreRepo.getScore(k.character, gameMode == "meaning" ? ScoreType.recognition : ScoreType.reading);
      final score = LearningScore(
        successes: entity?.successes ?? 0,
        failures: entity?.failures ?? 0,
      );
      final listName = gameMode == "meaning" ? "Recognition_List" : "Reading_List";
      final revisionList = await _scoreRepo.getListItems(listName);
      
      if (score.successes - score.failures < 10 || revisionList.contains(k.character)) {
        filtered.add(k);
      }
      if (filtered.length >= quizSize) break;
    }

    _engine.allKanjiDetails.addAll(filtered..shuffle());
    _engine.startGame();
    notifyListeners();
  }

  Future<void> submitAnswer(int index) async {
    final isCorrect = await _engine.submitAnswer(index);
    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
    }
    notifyListeners();

    await Future.delayed(Duration(milliseconds: (1000 * _engine.animationSpeed).toInt()));
    _engine.acknowledgeResult();
    notifyListeners();
  }
}
