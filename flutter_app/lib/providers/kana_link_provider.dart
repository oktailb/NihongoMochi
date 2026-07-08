import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/kana_link.dart';
import '../models/quiz_models.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';

class KanaLinkProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  final LevelContentProvider _contentProvider;
  final AudioService _audioService;

  KanaLinkProvider(
    this._wordRepo,
    this._scoreRepo,
    this._settingsRepo,
    this._contentProvider,
    this._audioService,
  ) {
    _loadHistory();
  }

  bool _isLoading = false;
  bool _isGameOver = false;
  bool _isPaused = false;
  bool _errorFlash = false;
  int _score = 0;
  int _wordsFound = 0;
  int _timeRemaining = 60;
  int _timeElapsed = 0;
  String _currentWord = "";
  List<List<KanaLinkCell>> _grid = [];
  List<KanaLinkCell> _selectedCells = [];
  List<KanaLinkResult> _history = [];
  late KanaLinkConfig _config;

  final Map<String, String> _kanaNormalizationMap = {
    'ぁ': 'あ', 'ぃ': 'い', 'ぅ': 'う', 'ぇ': 'え', 'ぉ': 'お',
    'っ': 'つ', 'ゃ': 'や', 'ゅ': 'ゆ', 'ょ': 'よ', 'ゎ': 'わ',
    'ァ': 'あ', 'ィ': 'い', 'ゥ': 'う', 'ェ': 'え', 'ォ': 'お',
    'ッ': 'つ', 'ャ': 'や', 'ュ': 'ゆ', 'ョ': 'よ', 'ヮ': 'わ',
  };

  Timer? _timer;
  List<WordEntry> _availableWords = [];
  List<String> _kanaPool = [];

  // Getters
  bool get isLoading => _isLoading;
  bool get isGameOver => _isGameOver;
  bool get isPaused => _isPaused;
  bool get errorFlash => _errorFlash;
  int get score => _score;
  int get wordsFound => _wordsFound;
  int get timeRemaining => _timeRemaining;
  int get timeElapsed => _timeElapsed;
  String get currentWord => _currentWord;
  List<List<KanaLinkCell>> get grid => _grid;
  List<KanaLinkCell> get selectedCells => _selectedCells;
  List<KanaLinkResult> get history => _history;

  void _loadHistory() {
    // Fetch from score repo in real implementation
    notifyListeners();
  }

  String _normalizeKana(String text) {
    String res = KanaUtils.katakanaToHiragana(text);
    return res.split('').map((char) => _kanaNormalizationMap[char] ?? char).join('');
  }

  Future<void> initGame(String levelId, {KanaLinkMode mode = KanaLinkMode.timeAttack}) async {
    _isLoading = true;
    _isGameOver = false;
    _isPaused = false;
    _score = mode == KanaLinkMode.survival ? 100 : 0;
    _wordsFound = 0;
    _timeElapsed = 0;
    _timeRemaining = mode == KanaLinkMode.timeAttack ? 60 : 0;
    _selectedCells = [];
    _currentWord = "";
    notifyListeners();

    _config = KanaLinkConfig(
      levelId: levelId,
      mode: mode,
      initialTime: _timeRemaining,
    );

    // Load resources
    _availableWords = await _wordRepo.getWordsForLevel(levelId);

    // Simple kana pool for now
    _kanaPool = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまmiむめもやゆよらりるれろわをん".split('');

    _generateInitialGrid();
    _isLoading = false;
    _startTimer();
    notifyListeners();
  }

  void _generateInitialGrid() {
    final rows = _config.rows;
    final cols = _config.cols;
    final random = Random();

    _grid = List.generate(rows, (r) => List.generate(cols, (c) => KanaLinkCell(
      id: "${r}_${c}_${random.nextInt(99999)}",
      char: _kanaPool[random.nextInt(_kanaPool.length)],
      row: r,
      col: c,
    )));

    // In a full implementation, we would inject some valid words into the grid here
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_isPaused || _isGameOver) return;

      _timeElapsed++;
      if (_config.mode == KanaLinkMode.timeAttack) {
        _timeRemaining--;
        if (_timeRemaining <= 0) {
          _timeRemaining = 0;
          _isGameOver = true;
          _timer?.cancel();
          _audioService.playSound("assets/files/sounds/game_over.mp3");
          _saveResult();
        }
      }
      notifyListeners();
    });
  }

  void onCellTouched(int row, int col) {
    if (_isGameOver || _isLoading || _isPaused) return;

    final cell = _grid[row][col];
    if (cell.isMatched) return;

    if (_selectedCells.isEmpty) {
      _selectedCells = [cell];
      _currentWord = cell.char;
      notifyListeners();
      return;
    }

    if (_selectedCells.last.id == cell.id) return;

    // Undo selection if touching the previous cell
    if (_selectedCells.length >= 2 && _selectedCells[_selectedCells.length - 2].id == cell.id) {
      _selectedCells.removeLast();
      _currentWord = _selectedCells.map((e) => e.char).join();
      notifyListeners();
      return;
    }

    // Check adjacency
    final last = _selectedCells.last;
    if ((last.row - row).abs() <= 1 && (last.col - col).abs() <= 1) {
      if (!_selectedCells.any((c) => c.id == cell.id)) {
        _selectedCells.add(cell);
        _currentWord += cell.char;
        notifyListeners();
      }
    }
  }

  Future<void> onReleaseSelection() async {
    if (_currentWord.isEmpty || _isGameOver || _isPaused) return;

    final normalizedSelection = _normalizeKana(_currentWord);
    final isValid = _availableWords.any((w) => _normalizeKana(w.phonetics) == normalizedSelection);

    if (isValid) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _handleValidWord(normalizedSelection.length);
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
      _score = max(0, _score - (_currentWord.length * 5));
      _selectedCells = [];
      _currentWord = "";
      _errorFlash = true;
      notifyListeners();
      await Future.delayed(const Duration(milliseconds: 300));
      _errorFlash = false;

      if (_config.mode == KanaLinkMode.survival && _score <= 0) {
        _isGameOver = true;
        _timer?.cancel();
        _audioService.playSound("assets/files/sounds/game_over.mp3");
        _saveResult();
      }
      notifyListeners();
    }
  }

  void _handleValidWord(int length) {
    final selectedIds = _selectedCells.map((e) => e.id).toSet();

    // Mark as matched
    for (var row in _grid) {
      for (int i = 0; i < row.length; i++) {
        if (selectedIds.contains(row[i].id)) {
          row[i] = row[i].copyWith(isMatched: true);
        }
      }
    }

    _score += length * 10;
    _wordsFound++;
    if (_config.mode == KanaLinkMode.timeAttack) {
      _timeRemaining += length;
    }

    notifyListeners();
    Future.delayed(const Duration(milliseconds: 300), () => _applyGravity());
  }

  void _applyGravity() {
    final rows = _config.rows;
    final cols = _config.cols;
    final random = Random();

    for (int c = 0; c < cols; c++) {
      List<KanaLinkCell> remaining = [];
      for (int r = rows - 1; r >= 0; r--) {
        if (!_grid[r][c].isMatched) {
          remaining.add(_grid[r][c]);
        }
      }

      for (int r = rows - 1; r >= 0; r--) {
        int index = rows - 1 - r;
        if (index < remaining.length) {
          _grid[r][c] = remaining[index].copyWith(row: r);
        } else {
          _grid[r][c] = KanaLinkCell(
            id: "n_${r}_${c}_${random.nextInt(999999)}",
            char: _kanaPool[random.nextInt(_kanaPool.length)],
            row: r,
            col: c,
          );
        }
      }
    }

    _selectedCells = [];
    _currentWord = "";
    notifyListeners();
  }

  void _saveResult() {
    // Save to repo logic
  }

  void pauseGame() { _isPaused = true; notifyListeners(); }
  void resumeGame() { _isPaused = false; notifyListeners(); }

  void abandonGame() {
    _timer?.cancel();
    _isGameOver = true;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
