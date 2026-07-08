import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/snake.dart';
import '../repositories/score_repository.dart';
import '../repositories/word_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';

class SnakeProvider extends ChangeNotifier {
  final LevelContentProvider _levelContentProvider;
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  final AudioService _audioService;

  SnakeProvider(
    this._levelContentProvider,
    this._wordRepo,
    this._scoreRepo,
    this._settingsRepo,
    this._audioService,
  ) {
    _loadHistory();
  }

  SnakeGameState _gameState = SnakeGameState();
  SnakeGameState get gameState => _gameState;

  SnakeMode _selectedMode = SnakeMode.hiragana;
  SnakeMode get selectedMode => _selectedMode;

  List<SnakeGameResult> _scoresHistory = [];
  List<SnakeGameResult> get scoresHistory => _scoresHistory;

  Timer? _gameTimer;
  Timer? _tickTimer;
  List<String> _itemSequence = [];
  dynamic _currentWord; // WordEntry but dynamic for now

  void _loadHistory() {
    // In a real app, fetch from repository
    notifyListeners();
  }

  void onModeSelected(SnakeMode mode) {
    _selectedMode = mode;
    notifyListeners();
  }

  void onDirectionChanged(SnakeDirection newDirection) {
    final currentDir = _gameState.direction;
    final isOpposite = (newDirection == SnakeDirection.up && currentDir == SnakeDirection.down) ||
                       (newDirection == SnakeDirection.down && currentDir == SnakeDirection.up) ||
                       (newDirection == SnakeDirection.left && currentDir == SnakeDirection.right) ||
                       (newDirection == SnakeDirection.right && currentDir == SnakeDirection.left);

    if (!isOpposite && !_gameState.isPaused) {
      _gameState = _gameState.copyWith(direction: newDirection);
      notifyListeners();
    }
  }

  Future<void> startGame(String locale) async {
    _stopTimers();
    await _prepareSequence(locale);

    _gameState = SnakeGameState(
      gridWidth: 15,
      gridHeight: 22,
      snake: [const SnakePoint(x: 7, y: 10), const SnakePoint(x: 7, y: 11), const SnakePoint(x: 7, y: 12)],
      direction: SnakeDirection.up,
      mode: _selectedMode,
      sequenceIndex: 0,
      currentNumber: 1,
      tickDelay: 220,
    );

    _spawnItems();
    _startTimers(locale);
    notifyListeners();
  }

  void _startTimers(String locale) {
    _gameTimer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!_gameState.isPaused && !_gameState.isGameOver) {
        _gameState = _gameState.copyWith(timeSeconds: _gameState.timeSeconds + 1);
        notifyListeners();
      }
    });

    _scheduleTick(locale);
  }

  void _scheduleTick(String locale) {
    _tickTimer?.cancel();
    _tickTimer = Timer(Duration(milliseconds: _gameState.tickDelay), () {
      if (!_gameState.isGameOver) {
        if (!_gameState.isPaused) {
          _moveSnake(locale);
        }
        _scheduleTick(locale);
      }
    });
  }

  void _stopTimers() {
    _gameTimer?.cancel();
    _tickTimer?.cancel();
  }

  Future<void> _prepareSequence(String locale) async {
    switch (_selectedMode) {
      case SnakeMode.hiragana:
        _itemSequence = await _levelContentProvider.getItemsForLevel("hiragana", ScoreType.recognition, locale);
        break;
      case SnakeMode.katakana:
        _itemSequence = await _levelContentProvider.getItemsForLevel("katakana", ScoreType.recognition, locale);
        break;
      case SnakeMode.numbers:
        _itemSequence = [_convertToKanji(1)];
        break;
      case SnakeMode.words:
        await _pickNextWord();
        break;
    }
    _updateLabel();
  }

  Future<void> _pickNextWord() async {
    final words = await _wordRepo.getWordsForLevel("n5");
    final random = Random();
    if (words.isNotEmpty) {
      _currentWord = words[random.nextInt(words.length)];
      _itemSequence = _currentWord.phonetics.split('').where((s) => s.trim().isNotEmpty).toList();
      _gameState = _gameState.copyWith(sequenceIndex: 0);
    }
  }

  void _updateLabel() {
    String label = "";
    final target = _itemSequence.asMap().containsKey(_gameState.sequenceIndex) ? _itemSequence[_gameState.sequenceIndex] : "";

    switch (_selectedMode) {
      case SnakeMode.words:
        label = "Mot : ${_currentWord?.text ?? ""}";
        break;
      case SnakeMode.numbers:
        label = "Cible : ${_itemSequence[0]}";
        break;
      default:
        label = "Cible : $target";
    }
    _gameState = _gameState.copyWith(currentTargetLabel: label);
  }

  void _spawnItems() {
    final occupied = _gameState.snake.toSet();
    final random = Random();

    SnakePoint randomPoint() {
      SnakePoint p;
      do {
        p = SnakePoint(x: random.nextInt(_gameState.gridWidth), y: random.nextInt(_gameState.gridHeight));
      } while (occupied.contains(p));
      return p;
    }

    if (_gameState.sequenceIndex >= _itemSequence.length) return;

    final targetChar = _itemSequence[_gameState.sequenceIndex];
    final targetItem = SnakeItem(character: targetChar, position: randomPoint(), isTarget: true);

    List<SnakeItem> distractions = [];
    // Simple distractions for now
    for (int i = 0; i < 2; i++) {
      distractions.add(SnakeItem(character: "？", position: randomPoint(), isTarget: false));
    }

    _gameState = _gameState.copyWith(targetItem: targetItem, distractions: distractions);
  }

  void _moveSnake(String locale) {
    final head = _gameState.snake.first;
    SnakePoint nextHead;
    switch (_gameState.direction) {
      case SnakeDirection.up: nextHead = SnakePoint(x: head.x, y: head.y - 1); break;
      case SnakeDirection.down: nextHead = SnakePoint(x: head.x, y: head.y + 1); break;
      case SnakeDirection.left: nextHead = SnakePoint(x: head.x - 1, y: head.y); break;
      case SnakeDirection.right: nextHead = SnakePoint(x: head.x + 1, y: head.y); break;
    }

    if (nextHead.x < 0 || nextHead.x >= _gameState.gridWidth || nextHead.y < 0 || nextHead.y >= _gameState.gridHeight || _gameState.snake.contains(nextHead)) {
      _gameOver();
      return;
    }

    bool grew = false;
    if (nextHead == _gameState.targetItem?.position) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      grew = true;
      int nextSeqIdx = _gameState.sequenceIndex + 1;
      int nextScore = _gameState.score + 10;

      _gameState = _gameState.copyWith(score: nextScore, sequenceIndex: nextSeqIdx);

      if (nextSeqIdx >= _itemSequence.length) {
        if (_selectedMode == SnakeMode.words) {
          _gameState = _gameState.copyWith(wordsCompleted: _gameState.wordsCompleted + 1);
          _pickNextWord();
        } else if (_selectedMode == SnakeMode.numbers) {
          int nextNum = _gameState.currentNumber + 1;
          _itemSequence = [_convertToKanji(nextNum)];
          _gameState = _gameState.copyWith(currentNumber: nextNum, sequenceIndex: 0, tickDelay: max(100, (_gameState.tickDelay * 0.98).toInt()));
        } else {
          _gameState = _gameState.copyWith(sequenceIndex: 0, tickDelay: max(100, (_gameState.tickDelay * 0.95).toInt()));
        }
      }
      _updateLabel();
      _spawnItems();
    } else if (_gameState.distractions.any((d) => d.position == nextHead)) {
      _gameOver();
      return;
    }

    final newSnake = [nextHead, ... (grew ? _gameState.snake : _gameState.snake.sublist(0, _gameState.snake.length - 1))];
    _gameState = _gameState.copyWith(snake: newSnake);
    notifyListeners();
  }

  void _gameOver() {
    if (_gameState.isGameOver) return;
    _stopTimers();
    _audioService.playSound("assets/files/sounds/game_over.mp3");
    _gameState = _gameState.copyWith(isGameOver: true);

    final result = SnakeGameResult(
      mode: _selectedMode,
      score: _gameState.score,
      wordsCompleted: _gameState.wordsCompleted,
      timeSeconds: _gameState.timeSeconds,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );
    // _scoreRepo.saveSnakeResult(result);
    _scoresHistory.insert(0, result);
    notifyListeners();
  }

  String _convertToKanji(int n) {
    if (n == 0) return "零";
    final units = ["", "一", "二", "三", "四", "五", "六", "七", "八", "九"];
    final positions = ["", "十", "百", "千"];
    var num = n;
    var res = "";
    var pos = 0;
    while (num > 0) {
      int digit = num % 10;
      if (digit > 0) {
        String p = positions[pos];
        String u = (digit == 1 && pos > 0) ? "" : units[digit];
        res = u + p + res;
      }
      num ~/= 10;
      pos++;
    }
    return res;
  }

  void pauseGame() { _gameState = _gameState.copyWith(isPaused: true); notifyListeners(); }
  void resumeGame() { _gameState = _gameState.copyWith(isPaused: false); notifyListeners(); }
  void abandonGame() { _gameOver(); }

  @override
  void dispose() {
    _stopTimers();
    super.dispose();
  }
}
