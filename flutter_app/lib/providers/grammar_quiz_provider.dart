import 'dart:math';
import 'package:flutter/material.dart';
import '../models/grammar_quiz.dart';
import '../models/quiz_models.dart';
import '../repositories/exercise_repository.dart';
import '../repositories/score_repository.dart';
import '../services/audio_service.dart';
import '../services/statistics_service.dart';

class GrammarQuizProvider extends ChangeNotifier {
  final ExerciseRepository _exerciseRepo;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;
  final List<String> grammarTags;

  GrammarQuizProvider({
    required ExerciseRepository exerciseRepo,
    required ScoreRepository scoreRepo,
    required AudioService audioService,
    required this.grammarTags,
  })  : _exerciseRepo = exerciseRepo,
        _scoreRepo = scoreRepo,
        _audioService = audioService;

  List<Exercise> _allExercises = [];
  List<Exercise> _currentSet = [];
  List<Exercise> _revisionList = [];
  Map<String, GameStatus> _exercisesStatus = {};
  int _listPosition = 0;

  Exercise? _currentExercise;
  ExercisePayload? _currentPayload;
  List<String> _currentOptions = [];
  String? _selectedOption;
  bool? _isAnswerCorrect;
  GameState _gameState = GameState.loading;
  int _score = 0;
  int _errorCount = 0;

  // Getters
  Exercise? get currentExercise => _currentExercise;
  ExercisePayload? get currentPayload => _currentPayload;
  List<String> get currentOptions => _currentOptions;
  String? get selectedOption => _selectedOption;
  bool? get isAnswerCorrect => _isAnswerCorrect;
  GameState get gameState => _gameState;
  int get quizScore => _score;
  int get errorCount => _errorCount;
  List<GameStatus> get progressHistory => _currentSet.map((e) => _exercisesStatus[e.id] ?? GameStatus.notAnswered).toList();

  Future<void> startQuiz() async {
    _gameState = GameState.loading;
    notifyListeners();

    final List<Exercise> allPossibleExercises = [];
    for (var tag in grammarTags) {
      final exercises = await _exerciseRepo.getExercisesForTag(tag, limit: 10);
      allPossibleExercises.addAll(exercises);
    }

    _allExercises = allPossibleExercises..shuffle();

    if (_allExercises.isNotEmpty) {
      if (_startNewSet()) {
        _setupQuestion();
      } else {
        _gameState = GameState.finished;
      }
    } else {
      _gameState = GameState.finished;
    }
    notifyListeners();
  }

  bool _startNewSet() {
    _revisionList.clear();
    _exercisesStatus.clear();

    if (_listPosition >= _allExercises.length) return false;

    _currentSet = _allExercises.skip(_listPosition).take(10).toList();
    _listPosition += _currentSet.length;

    _revisionList = List.from(_currentSet);
    for (var e in _currentSet) {
      _exercisesStatus[e.id] = GameStatus.notAnswered;
    }
    return true;
  }

  void _setupQuestion() {
    if (_revisionList.isEmpty) {
      if (_startNewSet()) {
        _setupQuestion();
      } else {
        _gameState = GameState.finished;
      }
      return;
    }

    _currentExercise = _revisionList[Random().nextInt(_revisionList.length)];
    _currentPayload = _exerciseRepo.parsePayload(_currentExercise!);
    _currentOptions = _generateOptions(_currentPayload);

    _selectedOption = null;
    _isAnswerCorrect = null;
    _gameState = GameState.waitingForAnswer;
  }

  List<String> _generateOptions(ExercisePayload? payload) {
    if (payload is FillBlankPayload) {
      final options = List<String>.from(payload.distractors)..shuffle();
      return (options.take(3).toList()..add(payload.correct))..shuffle();
    } else if (payload is SentenceOrderPayload) {
      return List<String>.from(payload.blocks)..shuffle();
    } else if (payload is WordUsagePayload) {
      final corrects = payload.options.where((o) => o.isCorrect).toList()..shuffle();
      final incorrects = payload.options.where((o) => !o.isCorrect).toList()..shuffle();
      final selected = (corrects.take(1).map((e) => e.text).toList() +
                        incorrects.take(3).map((e) => e.text).toList())..shuffle();
      return selected;
    }
    return [];
  }

  Future<void> submitAnswer(String option) async {
    if (_selectedOption != null || _currentExercise == null) return;

    _selectedOption = option;
    final bool isCorrect = _checkAnswer(option);
    _isAnswerCorrect = isCorrect;

    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _score++;
      _exercisesStatus[_currentExercise!.id] = GameStatus.correct;
      _revisionList.remove(_currentExercise);
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _errorCount++;
      _exercisesStatus[_currentExercise!.id] = GameStatus.incorrect;
    }

    // Sauvegarde du score
    for (var tag in _currentExercise!.tags) {
      await _scoreRepo.saveScore(
        key: tag,
        wasCorrect: isCorrect,
        type: ScoreType.grammar,
      );
    }

    _gameState = GameState.showingResult;
    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 1500));
    _setupQuestion();
    notifyListeners();
  }

  bool _checkAnswer(String option) {
    final p = _currentPayload;
    if (p is FillBlankPayload) return option == p.correct;
    if (p is WordUsagePayload) return p.options.firstWhere((o) => o.text == option).isCorrect;
    // Sentence order simplified for multiple choice in this port
    if (p is SentenceOrderPayload) return option == p.blocks.first;
    return false;
  }
}
