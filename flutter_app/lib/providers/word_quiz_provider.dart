import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_tts/flutter_tts.dart';
import '../models/quiz_models.dart';
import '../models/dictionary.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../services/word_quiz_engine.dart';

class WordQuizProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final WordMeaningRepository _meaningRepo;
  final FlutterTts _tts = FlutterTts();
  late final WordQuizEngine _engine;

  GameState _state = GameState.loading;
  List<AnswerButtonState> _buttonStates = List.filled(4, AnswerButtonState.defaultState);
  List<String> _currentAnswers = [];
  WordQuizItem? _currentWord;
  int _errorCount = 0;
  bool _isInitialized = false;

  // Getters
  GameState get state => _state;
  List<AnswerButtonState> get buttonStates => _buttonStates;
  List<String> get currentAnswers => _currentAnswers;
  WordQuizItem? get currentWord => _currentWord;
  int get errorCount => _errorCount;
  bool get isInitialized => _isInitialized;
  WordQuizEngine get engine => _engine;

  WordQuizProvider(this._wordRepo, this._scoreRepo, this._meaningRepo) {
    _engine = WordQuizEngine();
  }

  Future<void> initializeGame(String levelId, String locale) async {
    _isInitialized = false;
    _state = GameState.loading;
    notifyListeners();

    final filtered = await _wordRepo.getWordsForLevel(levelId);
    final meanings = await _meaningRepo.getWordMeanings(locale);

    if (filtered.isEmpty) {
      _state = GameState.finished;
      _isInitialized = true;
      notifyListeners();
      return;
    }

    _engine.allWords = filtered.map((e) => WordQuizItem(
      text: e.text,
      phonetics: e.phonetics,
      meaning: meanings[e.id],
    )).toList()..shuffle();

    _engine.wordListPosition = 0;
    _engine.isGameInitialized = true;

    _startNewSet();
    _isInitialized = true;
    notifyListeners();
  }

  void _startNewSet() {
    if (!_engine.startNewSet()) {
      _state = GameState.finished;
    } else {
      _displayQuestion();
    }
    notifyListeners();
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
    notifyListeners();
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
      _engine.wordStatus[_currentWord!.text] = GameStatus.correct;
      _engine.revisionList.remove(_currentWord);
      _buttonStates[index] = AnswerButtonState.correct;
    } else {
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
    _displayQuestion();
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
