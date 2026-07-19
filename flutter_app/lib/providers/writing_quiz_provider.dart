import 'dart:math';
import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/quiz_models.dart';
import '../models/kanji_sort_order.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../services/statistics_service.dart';
import '../utils/kana_utils.dart';

enum QuestionType { meaning, reading }

class KanjiProgress {
  bool meaningSolved = false;
  bool readingSolved = false;
}

class WritingQuizProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;
  final LevelContentProvider _contentProvider;
  final AudioService _audioService;
  final StatisticsService _statisticsService;
  final Random _random = Random();

  List<DictionaryItem> _allKanji = [];
  List<DictionaryItem> _currentSet = [];
  List<DictionaryItem> _revisionList = [];
  final Map<String, KanjiProgress> _progressMap = {};
  final Map<String, GameStatus> _kanjiStatus = {};

  DictionaryItem? _currentKanji;
  QuestionType _currentQuestionType = QuestionType.meaning;
  GameState _state = GameState.loading;
  bool _showCorrection = false;
  bool _isProcessing = false;
  int _errorCount = 0;
  int _listPosition = 0;
  bool? _isCorrect;

  String? _levelId;
  String? _locale;

  int _sessionMastery = 0;
  int _globalMastery = 0;

  // Getters
  DictionaryItem? get currentKanji => _currentKanji;
  QuestionType get currentQuestionType => _currentQuestionType;
  GameState get state => _state;
  bool get showCorrection => _showCorrection;
  bool get isProcessing => _isProcessing;
  int get errorCount => _errorCount;
  List<DictionaryItem> get currentSet => _currentSet;
  int get sessionMastery => _sessionMastery;
  int get globalMastery => _globalMastery;
  bool? get isCorrect => _isCorrect;

  List<GameStatus> get currentSetStatus =>
      _currentSet.map((k) => _kanjiStatus[k.id] ?? GameStatus.notAnswered).toList();

  WritingQuizProvider(
    this._dictionaryRepo,
    this._scoreRepo,
    this._contentProvider,
    this._audioService,
    this._statisticsService,
  );

  Future<void> startQuiz(
    String levelId,
    String locale, {
    List<String>? customKanjiList,
    KanjiSortOrder sortOrder = KanjiSortOrder.defaultOrder,
    int quizSize = 80,
  }) async {
    _locale = locale;

    _state = GameState.loading;

    notifyListeners();

    // 1. Get kanjis for level
    final kanjiCharsForLevel = customKanjiList ?? await _contentProvider.getItemsForLevel(
      levelId,
      levelId == "user_custom_list" ? ScoreType.writing : ScoreType.recognition,
      locale,
    );

    final all = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in all) k.character: k};

    final entriesForLevel = kanjiCharsForLevel.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList();

    // 2. Sort
    switch (sortOrder) {
      case KanjiSortOrder.frequency:
        entriesForLevel.sort((a, b) {
          final fa = a.frequency ?? 99999;
          final fb = b.frequency ?? 99999;
          return fa.compareTo(fb);
        });
        break;
      case KanjiSortOrder.strokes:
        entriesForLevel.sort((a, b) => a.strokeCount.compareTo(b.strokeCount));
        break;
      case KanjiSortOrder.defaultOrder:
        break;
    }

    // 3. Filter items that are NOT mastered, and take the quiz size
    List<DictionaryItem> filtered;
    if (customKanjiList == null && levelId != "user_custom_list") {
      filtered = [];
      for (var k in entriesForLevel) {
        final score = await _scoreRepo.getScore(k.character, ScoreType.writing);
        final successes = score?.successes ?? 0;
        final failures = score?.failures ?? 0;
        if ((successes - failures) < 10) {
          filtered.add(k);
        }
      }
      filtered = filtered.take(quizSize).toList();
    } else {
      filtered = entriesForLevel;
    }

    _allKanji = filtered;

    // 4. Shuffle if it's not a custom/revision list
    if (customKanjiList == null) {
      _allKanji.shuffle();
    }

    if (_allKanji.isEmpty) {
      await _calculateMastery();
      _state = GameState.finished;
    } else {
      _listPosition = 0;
      _errorCount = 0;
      await _startNextSet();
    }
    notifyListeners();
  }

  Future<void> _calculateMastery() async {
    if (_levelId != null && _locale != null) {
      _globalMastery = await _statisticsService.getPercentageForLevel(_levelId!, ScoreType.writing, _locale!);
      final chars = _allKanji.map((k) => k.character).toList();
      _sessionMastery = await _statisticsService.calculateSessionScore(chars, ScoreType.writing);
    }
  }

  Future<void> _startNextSet() async {
    _revisionList.clear();
    _progressMap.clear();
    _kanjiStatus.clear();

    if (_listPosition >= _allKanji.length) {
      await _calculateMastery();
      _state = GameState.finished;
      return;
    }

    _currentSet = _allKanji.skip(_listPosition).take(10).toList();
    _listPosition += _currentSet.length;
    _revisionList = List.from(_currentSet);

    for (var k in _currentSet) {
      _progressMap[k.id] = KanjiProgress();
      _kanjiStatus[k.id] = GameStatus.notAnswered;
    }
    await _nextQuestion();
  }

  Future<void> _nextQuestion() async {
    if (_revisionList.isEmpty) {
      await _startNextSet();
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
    _isCorrect = null;
    _state = GameState.waitingForAnswer;
  }

  Future<void> submitAnswer(String answer) async {
    if (_isProcessing || _currentKanji == null) return;
    _isProcessing = true;
    notifyListeners();

    bool isCorrect = false;
    final normalizedAnswer = _normalizeForComparison(answer, _currentQuestionType == QuestionType.reading);

    if (_currentQuestionType == QuestionType.meaning) {
      isCorrect = _currentKanji!.meanings.any(
        (m) => _normalizeForComparison(m, false) == normalizedAnswer
      );
    } else {
      isCorrect = _currentKanji!.readings.any(
        (r) => _normalizeForComparison(r.text, true) == normalizedAnswer
      );
    }

    _isCorrect = isCorrect;

    await _scoreRepo.saveScore(
      key: _currentKanji!.character,
      wasCorrect: isCorrect,
      type: ScoreType.writing,
    );

    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      final p = _progressMap[_currentKanji!.id]!;
      if (_currentQuestionType == QuestionType.meaning) {
        p.meaningSolved = true;
      } else {
        p.readingSolved = true;
      }

      if (p.meaningSolved && p.readingSolved) {
        _kanjiStatus[_currentKanji!.id] = GameStatus.correct;
        _revisionList.remove(_currentKanji);
      } else {
        _kanjiStatus[_currentKanji!.id] = GameStatus.partial;
      }

      _state = GameState.showingResult;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 800));
      await _nextQuestion();
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _kanjiStatus[_currentKanji!.id] = GameStatus.incorrect;
      _errorCount++;
      _showCorrection = true;
      _state = GameState.showingResult;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 3000));
      await _nextQuestion();
    }
    notifyListeners();
  }

  String _normalizeForComparison(String input, bool isReading) {
    var normalized = input.toLowerCase();

    if (!isReading) {
      normalized = _removeAccents(normalized);
    } else {
      normalized = KanaUtils.katakanaToHiragana(normalized);
    }

    // Supprime les points, les espaces, les tirets (ex: "kawa.i" -> "kawai")
    return normalized.replaceAll(RegExp(r'[.\s-]'), '');
  }

  String _removeAccents(String text) {
    const withDia = 'àáâãäåāăąæçćĉċčďđèéêëēĕėęěĝğġģĥħìíîïĩīĭįıĵķĺļľŀłñńņňŉŋòóôõöøōŏőœŕŗřśŝşšţťŧùúûüũūŭůűųŵýÿŷźżž';
    const withoutDia = 'aaaaaaaaaacccccccdeeeeeeeeeegggghhiiiiiiiiijklllllnnnnnnoooooooooeerrrsssstttuuuuuuuuuuwyyyzzz';
    for (int i = 0; i < withDia.length; i++) {
      text = text.replaceAll(withDia[i], withoutDia[i]);
    }
    return text;
  }
}
