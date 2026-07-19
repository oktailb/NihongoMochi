import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_tts/flutter_tts.dart';
import '../models/quiz_models.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../services/word_quiz_engine.dart';
import '../services/audio_service.dart';
import '../services/statistics_service.dart';

class WordQuizProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final WordMeaningRepository _meaningRepo;
  final AudioService _audioService;
  final StatisticsService _statisticsService;
  final FlutterTts _tts = FlutterTts();
  late final WordQuizEngine _engine;

  GameState _state = GameState.loading;
  List<AnswerButtonState> _buttonStates = List.filled(4, AnswerButtonState.defaultState);
  List<String> _currentAnswers = [];
  WordQuizItem? _currentWord;
  int _errorCount = 0;
  bool _isInitialized = false;

  String? _levelId;
  String? _locale;

  int _sessionMastery = 0;
  int _globalMastery = 0;

  // Getters
  GameState get state => _state;
  List<AnswerButtonState> get buttonStates => _buttonStates;
  List<String> get currentAnswers => _currentAnswers;
  WordQuizItem? get currentWord => _currentWord;
  int get errorCount => _errorCount;
  bool get isInitialized => _isInitialized;
  WordQuizEngine get engine => _engine;
  int get sessionMastery => _sessionMastery;
  int get globalMastery => _globalMastery;

  List<GameStatus> get currentSetStatus => _engine.currentWordSet
      .map((w) => _engine.wordStatus[w.text] ?? GameStatus.notAnswered)
      .toList();

  WordQuizProvider(
    this._wordRepo,
    this._scoreRepo,
    this._meaningRepo,
    this._audioService,
    this._statisticsService,
  ) {
    _engine = WordQuizEngine();
  }

  Future<void> initializeGame(String levelId, String locale, {List<String>? customWordList}) async {
    _isInitialized = false;
    _state = GameState.loading;
    _errorCount = 0;
    _levelId = levelId;
    _locale = locale;
    _engine.reset();

    notifyListeners();

    List<WordEntry> filtered;
    if (customWordList != null) {
      final all = await _wordRepo.getAllWords();
      final customSet = customWordList.toSet();
      filtered = all.where((w) => customSet.contains(w.text)).toList();
    } else {
      filtered = await _wordRepo.getWordsForLevel(levelId);
    }

    final meanings = await _meaningRepo.getWordMeanings(locale);

    // Filter out mastered words, unless they are in the manual revision list
    final manualRevisionList = await _scoreRepo.getListItems("Reading_List");
    final manualRevisionSet = manualRevisionList.toSet();

    if (customWordList == null && levelId != "user_custom_list" && levelId.isNotEmpty) {
      final List<WordEntry> tmp = [];
      for (var w in filtered) {
        final score = await _scoreRepo.getScore(w.text, ScoreType.reading);
        final mastery = (score?.successes ?? 0) - (score?.failures ?? 0);
        final isManualRevision = manualRevisionSet.contains(w.id) || manualRevisionSet.contains(w.text);
        if (mastery < 10 || isManualRevision) {
          tmp.add(w);
        }
      }
      filtered = tmp;
    }

    if (filtered.isEmpty) {
      await _calculateMastery();
      _state = GameState.finished;
      _isInitialized = true;
      notifyListeners();
      return;
    }

    _engine.allWords = filtered.map((e) => WordQuizItem(
      text: e.text,
      phonetics: e.phonetics,
      meaning: meanings[e.id],
    )).toList();

    // Shuffle only if it is not a custom list
    if (customWordList == null) {
      _engine.allWords.shuffle();
    }

    _engine.wordListPosition = 0;
    _engine.isGameInitialized = true;

    await _startNewSet();
    _isInitialized = true;
    notifyListeners();
  }

  Future<void> _calculateMastery() async {
    if (_levelId != null && _locale != null) {
      _globalMastery = await _statisticsService.getPercentageForLevel(_levelId!, ScoreType.reading, _locale!);
      final chars = _engine.allWords.map((w) => w.text).toList();
      _sessionMastery = await _statisticsService.calculateSessionScore(chars, ScoreType.reading);
    }
  }

  Future<void> _startNewSet() async {
    if (!_engine.startNewSet()) {
      await _calculateMastery();
      _state = GameState.finished;
    } else {
      _displayQuestion();
    }
  }

  void _displayQuestion() {
    if (_engine.revisionList.isEmpty) {
      _startNewSet();
      return;
    }

    final random = Random();
    _currentWord = _engine.revisionList[random.nextInt(_engine.revisionList.length)];
    _engine.currentWord = _currentWord;

    _generateAnswers();
    _buttonStates = List.filled(4, AnswerButtonState.defaultState);
    _state = GameState.waitingForAnswer;
  }

  void _generateAnswers() {
    if (_currentWord == null) return;

    final correctAnswer = _currentWord!.phonetics;
    final pool = _engine.allWords
        .where((w) => w.text != _currentWord!.text && w.phonetics.isNotEmpty)
        .map((w) => w.phonetics)
        .toSet()
        .toList();

    pool.shuffle();
    final answers = pool.take(3).toList();
    answers.add(correctAnswer);
    answers.shuffle();

    _currentAnswers = answers;
    _engine.currentAnswers = answers;
  }

  Future<void> submitAnswer(int index) async {
    if (_state != GameState.waitingForAnswer) return;

    final selected = _currentAnswers[index];
    final isCorrect = selected == _currentWord!.phonetics;

    await _scoreRepo.saveScore(
      key: _currentWord!.text,
      wasCorrect: isCorrect,
      type: ScoreType.reading,
    );

    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _engine.wordStatus[_currentWord!.text] = GameStatus.correct;
      _engine.revisionList.remove(_currentWord);
      _buttonStates[index] = AnswerButtonState.correct;
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _errorCount++;
      _engine.wordStatus[_currentWord!.text] = GameStatus.incorrect;
      _buttonStates[index] = AnswerButtonState.incorrect;

      final correctIdx = _currentAnswers.indexOf(_currentWord!.phonetics);
      if (correctIdx != -1) _buttonStates[correctIdx] = AnswerButtonState.correct;
    }

    _state = GameState.showingResult;
    notifyListeners();

    // Speak on correct answer
    if (isCorrect) {
      speak();
    }

    await Future.delayed(const Duration(milliseconds: 1500));
    await _nextQuestion();
    notifyListeners();
  }

  Future<void> _nextQuestion() async {
    if (_engine.revisionList.isEmpty) {
      await _startNewSet();
    } else {
      _displayQuestion();
    }
  }

  Future<void> speak() async {
    if (_currentWord == null) return;
    await _tts.setLanguage("ja-JP");
    await _tts.speak(_currentWord!.phonetics);
  }

  @override
  void dispose() {
    _tts.stop();
    super.dispose();
  }
}
