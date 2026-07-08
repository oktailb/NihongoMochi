import 'dart:math';
import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/quiz_models.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';

enum RecognitionMode { meaning, reading }

class RecognitionQuizProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;
  final Random _random = Random();

  RecognitionQuizProvider(this._dictionaryRepo, this._scoreRepo, this._audioService);

  List<DictionaryItem> _allKanji = [];
  List<DictionaryItem> _currentSet = [];
  List<DictionaryItem> _revisionList = [];
  Map<String, GameStatus> _kanjiStatus = {};

  DictionaryItem? _currentKanji;
  RecognitionMode _mode = RecognitionMode.meaning;
  KanaQuestionDirection _direction = KanaQuestionDirection.normal;
  GameState _state = GameState.loading;
  List<String> _currentAnswers = [];
  List<AnswerButtonState> _buttonStates = List.filled(4, () => AnswerButtonState.defaultState);

  int _errorCount = 0;
  int _listPosition = 0;

  // Getters
  DictionaryItem? get currentKanji => _currentKanji;
  RecognitionMode get mode => _mode;
  KanaQuestionDirection get direction => _direction;
  GameState get state => _state;
  List<String> get currentAnswers => _currentAnswers;
  List<AnswerButtonState> get buttonStates => _buttonStates;
  int get errorCount => _errorCount;
  List<GameStatus> get progressHistory => _currentSet.map((e) => _kanjiStatus[e.id] ?? GameStatus.notAnswered).toList();

  Future<void> startQuiz(String levelId, RecognitionMode mode, String locale) async {
    _state = GameState.loading;
    _mode = mode;
    notifyListeners();

    final all = await _dictionaryRepo.getFullDictionary(locale);
    _allKanji = all.where((k) => k.levelIds.contains(levelId)).toList()..shuffle();

    if (_allKanji.isEmpty) {
      _state = GameState.finished;
    } else {
      _listPosition = 0;
      _errorCount = 0;
      _startNextSet();
    }
    notifyListeners();
  }

  bool _startNextSet() {
    _revisionList.clear();
    _kanjiStatus.clear();

    if (_listPosition >= _allKanji.length) return false;

    _currentSet = _allKanji.skip(_listPosition).take(10).toList();
    _listPosition += _currentSet.length;
    _revisionList = List.from(_currentSet);

    for (var k in _currentSet) {
      _kanjiStatus[k.id] = GameStatus.notAnswered;
    }
    _nextQuestion();
    return true;
  }

  void _nextQuestion() {
    if (_revisionList.isEmpty) {
      if (!_startNextSet()) {
        _state = GameState.finished;
        return;
      }
    }

    _currentKanji = _revisionList[_random.nextInt(_revisionList.length)];
    // On alterne aléatoirement la direction comme en Kotlin
    _direction = _random.nextBool() ? KanaQuestionDirection.normal : KanaQuestionDirection.reverse;

    _generateAnswers();
    _buttonStates = List.filled(4, AnswerButtonState.defaultState);
    _state = GameState.waitingForAnswer;
  }

  void _generateAnswers() {
    if (_currentKanji == null) return;

    final bool isNormal = _direction == KanaQuestionDirection.normal;
    String correctAnswer;

    if (isNormal) {
      correctAnswer = _mode == RecognitionMode.meaning
          ? (_currentKanji!.meanings.isNotEmpty ? _currentKanji!.meanings.first : "")
          : (_currentKanji!.readings.isNotEmpty ? _currentKanji!.readings.first.text : "");
    } else {
      correctAnswer = _currentKanji!.character;
    }

    final pool = _allKanji
        .where((k) => k.id != _currentKanji!.id)
        .map((k) {
          if (isNormal) {
            return _mode == RecognitionMode.meaning
                ? (k.meanings.isNotEmpty ? k.meanings.first : "")
                : (k.readings.isNotEmpty ? k.readings.first.text : "");
          } else {
            return k.character;
          }
        })
        .where((s) => s.isNotEmpty)
        .toSet()
        .toList();

    pool.shuffle();
    final answers = pool.take(3).toList();
    answers.add(correctAnswer);
    answers.shuffle();

    _currentAnswers = answers;
  }

  Future<void> submitAnswer(int index) async {
    if (_state != GameState.waitingForAnswer) return;

    final selected = _currentAnswers[index];
    String correctAnswer;
    if (_direction == KanaQuestionDirection.normal) {
      correctAnswer = _mode == RecognitionMode.meaning
          ? (_currentKanji!.meanings.first)
          : (_currentKanji!.readings.first.text);
    } else {
      correctAnswer = _currentKanji!.character;
    }

    final isCorrect = selected == correctAnswer;

    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _kanjiStatus[_currentKanji!.id] = GameStatus.correct;
      _revisionList.remove(_currentKanji);
      _buttonStates[index] = AnswerButtonState.correct;
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _errorCount++;
      _kanjiStatus[_currentKanji!.id] = GameStatus.incorrect;
      _buttonStates[index] = AnswerButtonState.incorrect;

      final correctIdx = _currentAnswers.indexOf(correctAnswer);
      if (correctIdx != -1) _buttonStates[correctIdx] = AnswerButtonState.correct;
    }

    await _scoreRepo.saveScore(
      key: _currentKanji!.character,
      wasCorrect: isCorrect,
      type: ScoreType.recognition,
    );

    _state = GameState.showingResult;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 1500));
    _nextQuestion();
    notifyListeners();
  }
}
