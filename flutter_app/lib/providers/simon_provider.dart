import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/simon.dart';
import '../models/kana.dart';
import '../models/quiz_models.dart';
import '../repositories/kanji_repository.dart';
import '../repositories/kana_repository.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/settings_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';

class SimonProvider extends ChangeNotifier {
  final KanjiRepository _kanjiRepo;
  final KanaRepository _kanaRepo;
  final DictionaryRepository _dictionaryRepo;
  final SettingsRepository _settingsRepo;
  final LevelContentProvider _contentProvider;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;

  SimonProvider(
    this._kanjiRepo,
    this._kanaRepo,
    this._dictionaryRepo,
    this._settingsRepo,
    this._contentProvider,
    this._scoreRepo,
    this._audioService,
  ) {
    _checkLevelType();
  }

  SimonGameState _gameState = SimonGameState.idle;
  SimonMode _selectedMode = SimonMode.kanji;
  bool _isKanaLevel = false;
  List<SimonPlayable> _targetSequence = [];
  SimonPlayable? _currentPlayable;
  List<MapEntry<SimonPlayable, String>> _answers = [];
  bool _isKanjiVisible = false;
  bool _isButtonsVisible = false;
  int _gameTimeSeconds = 0;
  int _score = 0;
  List<SimonGameResult> _scoresHistory = [];

  int _inputIndex = 0;
  Timer? _timer;
  List<SimonPlayable> _allPlayablesInLevel = [];
  SimonGameState? _previousGameState;

  // Getters
  SimonGameState get gameState => _gameState;
  SimonMode get selectedMode => _selectedMode;
  bool get isKanaLevel => _isKanaLevel;
  List<SimonPlayable> get targetSequence => _targetSequence;
  SimonPlayable? get currentPlayable => _currentPlayable;
  List<MapEntry<SimonPlayable, String>> get answers => _answers;
  bool get isKanjiVisible => _isKanjiVisible;
  bool get isButtonsVisible => _isButtonsVisible;
  int get gameTimeSeconds => _gameTimeSeconds;
  int get score => _score;
  List<SimonGameResult> get scoresHistory => _scoresHistory;

  void _checkLevelType() {
    final levelId = _settingsRepo.getMode();
    _isKanaLevel = levelId.toLowerCase().contains("hiragana") || levelId.toLowerCase().contains("katakana");
    _selectedMode = _isKanaLevel ? SimonMode.kanaSame : SimonMode.kanji;
    notifyListeners();
  }

  Future<void> _loadLevelContent(String locale) async {
    final levelId = _settingsRepo.getMode();
    final lowerLevel = levelId.toLowerCase();

    if (_isKanaLevel) {
      final type = lowerLevel.contains("hiragana") ? KanaType.hiragana : KanaType.katakana;
      final playableType = type == KanaType.hiragana ? PlayableType.hiragana : PlayableType.katakana;
      final entries = await _kanaRepo.getKanaEntries(type);
      _allPlayablesInLevel = entries.map((e) => SimonPlayable(
        id: e.character,
        character: e.character,
        meanings: [e.romaji],
        readings: [e.romaji],
        type: playableType,
      )).toList();
    } else {
      final items = await _contentProvider.getItemsForLevel(levelId, ScoreType.recognition, locale);
      final allKanji = await _dictionaryRepo.getFullDictionary(locale);
      final kanjiMap = {for (var k in allKanji) k.character: k};

      _allPlayablesInLevel = items.map((char) {
        final k = kanjiMap[char];
        return SimonPlayable(
          id: k?.id ?? char,
          character: char,
          meanings: k?.meanings ?? [],
          readings: k?.readings.map((r) => r.text).toList() ?? [],
          type: PlayableType.kanji,
        );
      }).toList();
    }
  }

  void onModeSelected(SimonMode mode) {
    _selectedMode = mode;
    notifyListeners();
  }

  Future<void> startGame(String locale) async {
    _gameState = SimonGameState.idle;
    notifyListeners();

    await _loadLevelContent(locale);
    if (_allPlayablesInLevel.isEmpty) return;

    _targetSequence = [];
    _score = 0;
    _gameTimeSeconds = 0;
    _gameState = SimonGameState.showingSequence;

    _startTimer();
    _nextRound();
    notifyListeners();
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_gameState != SimonGameState.paused && _gameState != SimonGameState.gameOver) {
        _gameTimeSeconds++;
        notifyListeners();
      }
    });
  }

  Future<void> _nextRound() async {
    _gameState = SimonGameState.showingSequence;
    _isButtonsVisible = false;
    notifyListeners();

    final nextItem = (_allPlayablesInLevel..shuffle()).first;
    _targetSequence.add(nextItem);

    for (int i = 0; i < _targetSequence.length; i++) {
      while (_gameState == SimonGameState.paused) {
        await Future.delayed(const Duration(milliseconds: 500));
      }
      if (_gameState == SimonGameState.gameOver) return;

      _currentPlayable = _targetSequence[i];
      final showDuration = i == _targetSequence.length - 1 ? 2000 : 1000;

      _isKanjiVisible = true;
      notifyListeners();
      await Future.delayed(Duration(milliseconds: showDuration));
      _isKanjiVisible = false;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 300));
    }

    _inputIndex = 0;
    _refreshAnswersForIndex(0);
    _gameState = SimonGameState.awaitingInput;
    _isButtonsVisible = true;
    notifyListeners();
  }

  void _refreshAnswersForIndex(int index) {
    if (_targetSequence.isEmpty) return;

    final correctAnswer = _targetSequence[index];
    final distractors = (_allPlayablesInLevel.where((item) => item.id != correctAnswer.id).toList()..shuffle()).take(3).toList();

    final allOptions = (distractors..add(correctAnswer))..shuffle();
    _answers = allOptions.map((item) => MapEntry(item, _getLabelForPlayable(item))).toList();
  }

  String _getLabelForPlayable(SimonPlayable item) {
    switch (_selectedMode) {
      case SimonMode.kanji: return item.character;
      case SimonMode.meaning: return item.meanings.isNotEmpty ? item.meanings.first : item.character;
      case SimonMode.readingCommon: return item.readings.isNotEmpty ? item.readings.first : item.character;
      case SimonMode.readingRandom: return item.readings.isNotEmpty ? (List.from(item.readings)..shuffle()).first : item.character;
      case SimonMode.kanaSame: return item.character;
      case SimonMode.kanaCross:
        if (item.type == PlayableType.hiragana) return KanaUtils.hiraganaToKatakana(item.character);
        if (item.type == PlayableType.katakana) return KanaUtils.katakanaToHiragana(item.character);
        return item.character;
    }
  }

  void onAnswerClick(SimonPlayable item) {
    if (_gameState != SimonGameState.awaitingInput) return;

    if (item.id == _targetSequence[_inputIndex].id) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _inputIndex++;
      if (_inputIndex >= _targetSequence.length) {
        _score = _targetSequence.length;
        _nextRound();
      } else {
        _refreshAnswersForIndex(_inputIndex);
        notifyListeners();
      }
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _gameState = SimonGameState.gameOver;
      _timer?.cancel();
      _saveFinalScore();
      notifyListeners();
    }
  }

  void _saveFinalScore() {
    // Logic to save score to database
  }

  void pauseGame() {
    if (_gameState != SimonGameState.gameOver && _gameState != SimonGameState.idle) {
      _previousGameState = _gameState;
      _gameState = SimonGameState.paused;
      notifyListeners();
    }
  }

  void resumeGame() {
    if (_gameState == SimonGameState.paused) {
      _gameState = _previousGameState ?? SimonGameState.awaitingInput;
      notifyListeners();
    }
  }

  void abandonGame() {
    _timer?.cancel();
    _gameState = SimonGameState.idle;
    _checkLevelType();
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
