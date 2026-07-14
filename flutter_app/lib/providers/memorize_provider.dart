import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/memorize.dart';
import '../models/kana.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/kana_repository.dart';
import '../repositories/settings_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../db/database.dart';

class MemorizeProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final KanaRepository _kanaRepo;
  final SettingsRepository _settingsRepo;
  final LevelContentProvider _contentProvider;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;

  MemorizeProvider(
    this._dictionaryRepo,
    this._kanaRepo,
    this._settingsRepo,
    this._contentProvider,
    this._scoreRepo,
    this._audioService,
  ) {
    _init();
  }

  static final List<MemorizeGridSize> allPossibleGridSizes = [
    MemorizeGridSize(rows: 4, cols: 3),
    MemorizeGridSize(rows: 4, cols: 4),
    MemorizeGridSize(rows: 5, cols: 4),
    MemorizeGridSize(rows: 6, cols: 4),
    MemorizeGridSize(rows: 6, cols: 5),
  ];

  List<MemorizeGridSize> _availableGridSizes = allPossibleGridSizes;
  MemorizeGridSize _selectedGridSize = allPossibleGridSizes[1];
  int _maxStrokes = 20;
  int _selectedMaxStrokes = 20;
  bool _isKanaLevel = false;
  List<MemorizeGameResult> _scoresHistory = [];
  List<MemorizeCardState> _cards = [];
  int _moves = 0;
  int _gameTimeSeconds = 0;
  bool _isGameFinished = false;
  bool _isProcessing = false;
  bool _isPaused = false;
  bool _hasSavedGame = false;
  bool _autoRestoreDone = false;

  int? _firstSelectedCardIndex;
  Timer? _timer;

  // Getters
  List<MemorizeGridSize> get availableGridSizes => _availableGridSizes;
  MemorizeGridSize get selectedGridSize => _selectedGridSize;
  int get maxStrokes => _maxStrokes;
  int get selectedMaxStrokes => _selectedMaxStrokes;
  bool get isKanaLevel => _isKanaLevel;
  List<MemorizeGameResult> get scoresHistory => _scoresHistory;
  List<MemorizeCardState> get cards => _cards;
  int get moves => _moves;
  int get gameTimeSeconds => _gameTimeSeconds;
  bool get isGameFinished => _isGameFinished;
  bool get isProcessing => _isProcessing;
  bool get isPaused => _isPaused;
  bool get hasSavedGame => _hasSavedGame;

  Future<void> _init() async {
    final locale = _settingsRepo.getAppLocale();
    final allKanji = await _dictionaryRepo.getFullDictionary(locale);
    if (allKanji.isNotEmpty) {
      _maxStrokes = allKanji.map((k) => k.strokeCount).reduce(max);
      _selectedMaxStrokes = _maxStrokes;
    }
    updateLevelInfo(locale);
    _loadScoresHistory();
    _checkSavedGame();
  }

  Future<void> _loadScoresHistory() async {
    try {
      final List<GameHistory> rows = await _scoreRepo.getGameHistory("MEMORIZE");
      _scoresHistory = rows.map((r) => MemorizeGameResult(
        moves: r.moves ?? r.score,
        totalPairs: r.totalPairs ?? 0,
        gridSizeLabel: r.metadata ?? "4x4",
        timeSeconds: r.timeSeconds ?? 0,
        timestamp: r.timestamp,
      )).toList();
      notifyListeners();
    } catch (e) {
      _scoresHistory = [];
      notifyListeners();
    }
  }

  void _checkSavedGame() {
    final savedJson = _settingsRepo.getStringGeneric("game_state_memory");
    _hasSavedGame = savedJson != null;
    notifyListeners();
  }

  void tryAutoRestore(VoidCallback onRestored) {
    if (!_autoRestoreDone && _hasSavedGame) {
      _autoRestoreDone = true;
      restoreGame(onRestored);
    }
  }

  void restoreGame(VoidCallback onRestored) {
    final savedJson = _settingsRepo.getStringGeneric("game_state_memory");
    if (savedJson == null) return;
    try {
      final map = jsonDecode(savedJson) as Map<String, dynamic>;
      final List<dynamic> cardsList = map['cards'];
      _cards = cardsList.map((e) => MemorizeCardState.fromJson(e as Map<String, dynamic>)).toList();
      _moves = map['moves'] as int;
      _gameTimeSeconds = map['gameTimeSeconds'] as int;
      _selectedGridSize = MemorizeGridSize.fromJson(map['selectedGridSize'] as Map<String, dynamic>);
      _isKanaLevel = map['isKanaLevel'] as bool;
      _isGameFinished = map['isGameFinished'] as bool;
      _firstSelectedCardIndex = map['firstSelectedCardIndex'] as int?;
      _isProcessing = map['isProcessing'] as bool;

      _isPaused = true;
      _hasSavedGame = false;
      _autoRestoreDone = true;

      _startTimer();
      onRestored();
      notifyListeners();
    } catch (e) {
      _settingsRepo.removeGeneric("game_state_memory");
      _hasSavedGame = false;
      notifyListeners();
    }
  }

  void updateLevelInfo(String locale) async {
    final levelId = _settingsRepo.getSelectedLevel();
    final lowerLevel = levelId.toLowerCase();
    _isKanaLevel = lowerLevel.contains("hiragana") || lowerLevel.contains("katakana");

    int count = 0;
    if (_isKanaLevel) {
      final type = lowerLevel.contains("hiragana") ? KanaType.hiragana : KanaType.katakana;
      count = (await _kanaRepo.getKanaEntries(type)).length;
    } else {
      final items = await _contentProvider.getItemsForLevel(levelId.isEmpty ? "n5" : levelId, ScoreType.recognition, locale);
      final allKanji = await _dictionaryRepo.getFullDictionary(locale);
      final kanjiMap = {for (var k in allKanji) k.character: k};
      count = items.where((char) {
        final k = kanjiMap[char];
        return k != null && k.strokeCount <= _selectedMaxStrokes;
      }).length;
    }

    _availableGridSizes = allPossibleGridSizes.where((size) => size.pairsCount <= count).toList();
    if (_availableGridSizes.isEmpty) _availableGridSizes = [allPossibleGridSizes.first];

    if (!_availableGridSizes.contains(_selectedGridSize)) {
      _selectedGridSize = _availableGridSizes.last;
    }
    notifyListeners();
  }

  void onGridSizeSelected(MemorizeGridSize size) {
    _selectedGridSize = size;
    notifyListeners();
  }

  void onMaxStrokesChanged(int strokes, String locale) {
    _selectedMaxStrokes = strokes;
    updateLevelInfo(locale);
  }

  Future<void> startGame(String locale) async {
    _settingsRepo.removeGeneric("game_state_memory");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    _isProcessing = true;
    notifyListeners();

    final levelId = _settingsRepo.getSelectedLevel();
    final lowerLevel = levelId.toLowerCase();

    List<MemorizePlayable> allPlayables = [];

    if (_isKanaLevel) {
      final type = lowerLevel.contains("hiragana") ? KanaType.hiragana : KanaType.katakana;
      final entries = await _kanaRepo.getKanaEntries(type);
      allPlayables = entries.map((e) => MemorizePlayable(id: e.character, character: e.character)).toList();
    } else {
      final items = await _contentProvider.getItemsForLevel(levelId.isEmpty ? "n5" : levelId, ScoreType.recognition, locale);
      final allKanji = await _dictionaryRepo.getFullDictionary(locale);
      final kanjiMap = {for (var k in allKanji) k.character: k};

      allPlayables = items
          .map((char) => kanjiMap[char])
          .where((k) => k != null && k.strokeCount <= _selectedMaxStrokes)
          .map((k) => MemorizePlayable(id: k!.id, character: k.character))
          .toList();
    }

    if (allPlayables.isEmpty) {
      _isProcessing = false;
      notifyListeners();
      return;
    }

    final pairsToSelect = _selectedGridSize.pairsCount;
    final random = Random();
    final selectedItems = (List<MemorizePlayable>.from(allPlayables)..shuffle(random)).take(pairsToSelect).toList();

    final gameCards = (selectedItems + selectedItems)..shuffle(random);

    _cards = gameCards.asMap().entries.map((entry) => MemorizeCardState(
      id: entry.key,
      item: entry.value,
    )).toList();

    _moves = 0;
    _gameTimeSeconds = 0;
    _isGameFinished = false;
    _isProcessing = false;
    _isPaused = false;
    _firstSelectedCardIndex = null;

    _startTimer();
    notifyListeners();
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!_isPaused && !_isGameFinished) {
        _gameTimeSeconds++;
        notifyListeners();
      }
    });
  }

  void onCardClicked(int index) async {
    if (index < 0 || index >= _cards.length || _isProcessing || _cards[index].isFaceUp || _cards[index].isMatched || _isGameFinished || _isPaused) {
      return;
    }

    _cards[index] = _cards[index].copyWith(isFaceUp: true);
    notifyListeners();

    if (_firstSelectedCardIndex == null) {
      _firstSelectedCardIndex = index;
    } else {
      final firstIndex = _firstSelectedCardIndex!;
      _firstSelectedCardIndex = null;
      _moves++;
      _isProcessing = true;
      notifyListeners();

      await Future.delayed(const Duration(milliseconds: 500));

      if (_cards[firstIndex].item.id == _cards[index].item.id) {
        _audioService.playSound("assets/files/sounds/correct.mp3");
        _cards[firstIndex] = _cards[firstIndex].copyWith(isMatched: true);
        _cards[index] = _cards[index].copyWith(isMatched: true);
        _isProcessing = false;
        _checkGameFinished();
      } else {
        _audioService.playSound("assets/files/sounds/incorrect.mp3");
        await Future.delayed(const Duration(milliseconds: 500));
        _cards[firstIndex] = _cards[firstIndex].copyWith(isFaceUp: false);
        _cards[index] = _cards[index].copyWith(isFaceUp: false);
        _isProcessing = false;
      }
      notifyListeners();
    }
  }

  void _checkGameFinished() {
    if (_cards.every((card) => card.isMatched)) {
      _isGameFinished = true;
      _timer?.cancel();
      _settingsRepo.removeGeneric("game_state_memory");
      _hasSavedGame = false;
      _autoRestoreDone = true;
      _saveFinalScore();
    }
  }

  Future<void> _saveFinalScore() async {
    final result = MemorizeGameResult(
      moves: _moves,
      totalPairs: _selectedGridSize.pairsCount,
      gridSizeLabel: _selectedGridSize.toString(),
      timeSeconds: _gameTimeSeconds,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );

    _scoresHistory = [result, ..._scoresHistory].take(10).toList();
    notifyListeners();

    try {
      await _scoreRepo.saveGameHistory(
        gameType: "MEMORIZE",
        score: _moves,
        moves: _moves,
        timeSeconds: _gameTimeSeconds,
        totalPairs: _selectedGridSize.pairsCount,
        metadata: _selectedGridSize.toString(),
      );
    } catch (e) {
      // Silent catch
    }
  }

  void pauseGame() {
    _isPaused = true;
    notifyListeners();
  }

  void resumeGame() {
    _isPaused = false;
    notifyListeners();
  }

  void saveAndExit() {
    final stateMap = {
      'cards': _cards.map((e) => e.toJson()).toList(),
      'moves': _moves,
      'gameTimeSeconds': _gameTimeSeconds,
      'selectedGridSize': _selectedGridSize.toJson(),
      'isKanaLevel': _isKanaLevel,
      'isGameFinished': _isGameFinished,
      'firstSelectedCardIndex': _firstSelectedCardIndex,
      'isProcessing': _isProcessing,
      'isPaused': _isPaused,
    };
    _settingsRepo.setStringGeneric("game_state_memory", jsonEncode(stateMap));
    _hasSavedGame = true;
    _autoRestoreDone = true;
    _timer?.cancel();
    notifyListeners();
  }

  void abandonGame() {
    _timer?.cancel();
    _settingsRepo.removeGeneric("game_state_memory");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    if (!_isGameFinished && _cards.isNotEmpty) {
      _audioService.playSound("assets/files/sounds/game_over.mp3");
    }
    _cards = [];
    _isGameFinished = false;
    _isProcessing = false;
    _isPaused = false;
    _firstSelectedCardIndex = null;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
