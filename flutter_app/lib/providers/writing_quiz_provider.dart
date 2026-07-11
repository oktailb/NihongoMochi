import 'dart:math';
import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/quiz_models.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../utils/romaji_to_kana.dart';

enum QuestionType { meaning, reading }

class KanjiProgress {
  bool meaningSolved = false;
  bool readingSolved = false;
}

class WritingQuizProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;
  final LevelContentProvider _contentProvider;
  final Random _random = Random();

  List<DictionaryItem> _allKanji = [];
  List<DictionaryItem> _currentSet = [];
  List<DictionaryItem> _revisionList = [];
  Map<String, KanjiProgress> _progressMap = {};

  DictionaryItem? _currentKanji;
  QuestionType _currentQuestionType = QuestionType.meaning;
  GameState _state = GameState.loading;
  bool _showCorrection = false;
  bool _isProcessing = false;
  int _errorCount = 0;
  int _listPosition = 0;

  // Getters
  DictionaryItem? get currentKanji => _currentKanji;
  QuestionType get currentQuestionType => _currentQuestionType;
  GameState get state => _state;
  bool get showCorrection => _showCorrection;
  bool get isProcessing => _isProcessing;
  int get errorCount => _errorCount;
  List<DictionaryItem> get currentSet => _currentSet;

  WritingQuizProvider(this._dictionaryRepo, this._scoreRepo, this._contentProvider);

  Future<void> startQuiz(String levelId, String locale) async {
    _state = GameState.loading;
    notifyListeners();

    final characters = await _contentProvider.getItemsForLevel(levelId, ScoreType.writing, locale);
    final all = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in all) k.character: k};

    _allKanji = characters.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList()..shuffle();

    if (_allKanji.isEmpty) {
      _state = GameState.finished;
    } else {
      _listPosition = 0;
      _errorCount = 0;
      _startNextSet();
    }
    notifyListeners();
  }

  void _startNextSet() {
    _revisionList.clear();
    _progressMap.clear();

    if (_listPosition >= _allKanji.length) {
      _state = GameState.finished;
      return;
    }

    _currentSet = _allKanji.skip(_listPosition).take(10).toList();
    _listPosition += _currentSet.length;
    _revisionList = List.from(_currentSet);

    for (var k in _currentSet) {
      _progressMap[k.id] = KanjiProgress();
    }
    _nextQuestion();
  }

  void _nextQuestion() {
    if (_revisionList.isEmpty) {
      _startNextSet();
      return;
    }

    _currentKanji = _revisionList[_random.nextInt(_revisionList.length)];
    final progress = _progressMap[_currentKanji!.id]!;

    if (!progress.meaningSolved && !progress.readingSolved) {
      _currentQuestionType = _random.nextBool() ? QuestionType.meaning : QuestionType.reading;
    } else {
      _currentQuestionType = progress.meaningSolved ? QuestionType.reading : QuestionType.meaning;
    }

    _showCorrection = false;
    _isProcessing = false;
    _state = GameState.waitingForAnswer;
  }

  Future<void> submitAnswer(String answer) async {
    if (_isProcessing || _currentKanji == null) return;
    _isProcessing = true;
    notifyListeners();

    bool isCorrect = false;
    final normalized = answer.trim().toLowerCase();

    if (_currentQuestionType == QuestionType.meaning) {
      isCorrect = _currentKanji!.meanings.any((m) => m.toLowerCase() == normalized);
    } else {
      // Pour la lecture, on compare aux lectures On/Kun
      isCorrect = _currentKanji!.readings.any((r) => r.text == normalized);
    }

    await _scoreRepo.saveScore(
      key: _currentKanji!.character,
      wasCorrect: isCorrect,
      type: ScoreType.writing,
    );

    if (isCorrect) {
      final p = _progressMap[_currentKanji!.id]!;
      if (_currentQuestionType == QuestionType.meaning) p.meaningSolved = true; else p.readingSolved = true;

      if (p.meaningSolved && p.readingSolved) {
        _revisionList.remove(_currentKanji);
      }

      _state = GameState.showingResult;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 800));
      _nextQuestion();
    } else {
      _errorCount++;
      _showCorrection = true;
      _state = GameState.showingResult;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 3000));
      _nextQuestion();
    }
    notifyListeners();
  }
}
