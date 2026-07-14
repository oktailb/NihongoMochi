import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/kana_link.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/level_content_provider.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';
import '../db/database.dart';

class KanaLinkProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  // ignore: unused_field
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
    _checkSavedGame();
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

  bool _hasSavedGame = false;
  bool _autoRestoreDone = false;

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
  bool get hasSavedGame => _hasSavedGame;

  Future<void> _loadHistory() async {
    try {
      final List<GameHistory> rows = await _scoreRepo.getGameHistory("KANALINK");
      _history = rows.map((r) {
        final parts = r.metadata?.split(',') ?? [];
        final levelId = parts.length > 1 ? parts[1] : "unknown";
        
        return KanaLinkResult(
          score: r.score,
          wordsFound: r.wordsFound ?? 0,
          timeSeconds: r.timeSeconds ?? 0,
          levelId: levelId,
          timestamp: r.timestamp,
        );
      }).toList();
      notifyListeners();
    } catch (e) {
      _history = [];
      notifyListeners();
    }
  }

  void _checkSavedGame() {
    final savedJson = _settingsRepo.getStringGeneric("game_state_kanalink");
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
    final savedJson = _settingsRepo.getStringGeneric("game_state_kanalink");
    if (savedJson == null) return;
    try {
      final map = jsonDecode(savedJson) as Map<String, dynamic>;
      
      final List<dynamic> gridList = map['grid'];
      _grid = gridList.map((row) => (row as List<dynamic>).map((e) => KanaLinkCell.fromJson(e as Map<String, dynamic>)).toList()).toList();
      
      final List<dynamic> selectedList = map['selectedCells'];
      _selectedCells = selectedList.map((e) => KanaLinkCell.fromJson(e as Map<String, dynamic>)).toList();
      
      _currentWord = map['currentWord'] as String;
      _score = map['score'] as int;
      _wordsFound = map['wordsFound'] as int;
      _timeRemaining = map['timeRemaining'] as int;
      _timeElapsed = map['timeElapsed'] as int;
      _isGameOver = map['isGameOver'] as bool;
      _config = KanaLinkConfig.fromJson(map['config'] as Map<String, dynamic>);
      
      _isPaused = true;
      _hasSavedGame = false;
      _autoRestoreDone = true;

      _startTimer();
      onRestored();
      notifyListeners();
    } catch (e) {
      _settingsRepo.removeGeneric("game_state_kanalink");
      _hasSavedGame = false;
      notifyListeners();
    }
  }

  String _normalizeKana(String text) {
    String res = KanaUtils.katakanaToHiragana(text);
    return res.split('').map((char) => _kanaNormalizationMap[char] ?? char).join('');
  }

  Future<void> initGame(String levelId, {KanaLinkMode mode = KanaLinkMode.timeAttack}) async {
    _settingsRepo.removeGeneric("game_state_kanalink");
    _hasSavedGame = false;
    _autoRestoreDone = true;
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

    // Load resources: include all levels up to the selected level (same as KMP)
    final allLevels = ["n5", "n4", "n3", "n2", "n1"];
    final selectedLevelIndex = allLevels.indexOf(levelId.toLowerCase());
    final List<WordEntry> entries = [];
    if (selectedLevelIndex != -1) {
      for (int i = 0; i <= selectedLevelIndex; i++) {
        final words = await _wordRepo.getWordsForLevel(allLevels[i]);
        entries.addAll(words);
      }
    } else {
      final words = await _wordRepo.getWordsForLevel(levelId.isEmpty ? "n5" : levelId);
      entries.addAll(words);
    }
    _availableWords = entries.toSet().toList(); // distinct
    if (_availableWords.isEmpty) {
      _availableWords = await _wordRepo.getWordsForLevel("n5");
    }

    // Simple kana pool for now
    _kanaPool = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよらりるれろわをん".split('');

    _generateInitialGrid();
    _isLoading = false;
    _startTimer();
    notifyListeners();
  }

  void _generateInitialGrid() {
    final rows = _config.rows;
    final cols = _config.cols;
    final random = Random();

    // Initialize temporary grid with empty chars
    final tempGrid = List.generate(rows, (r) => List.generate(cols, (c) => KanaLinkCell(
      id: "${r}_${c}_init",
      char: "",
      row: r,
      col: c,
    )));

    // Shuffle and take up to 5 words to inject (same as KMP)
    final shuffledWords = List<WordEntry>.from(_availableWords)..shuffle(random);
    final wordsToInject = shuffledWords.take(5).map((w) => _normalizeKana(w.phonetics)).toList();

    for (final word in wordsToInject) {
      bool placed = false;
      int attempts = 0;
      while (!placed && attempts < 10) {
        final startRow = random.nextInt(rows);
        final startCol = random.nextInt(cols);
        final direction = random.nextInt(2); // 0 = Horizontal, 1 = Vertical

        if (direction == 0 && startCol + word.length <= cols) {
          bool canPlace = true;
          for (int i = 0; i < word.length; i++) {
            if (tempGrid[startRow][startCol + i].char.isNotEmpty) {
              canPlace = false;
              break;
            }
          }
          if (canPlace) {
            for (int i = 0; i < word.length; i++) {
              tempGrid[startRow][startCol + i] = tempGrid[startRow][startCol + i].copyWith(char: word[i]);
            }
            placed = true;
          }
        } else if (direction == 1 && startRow + word.length <= rows) {
          bool canPlace = true;
          for (int i = 0; i < word.length; i++) {
            if (tempGrid[startRow + i][startCol].char.isNotEmpty) {
              canPlace = false;
              break;
            }
          }
          if (canPlace) {
            for (int i = 0; i < word.length; i++) {
              tempGrid[startRow + i][startCol] = tempGrid[startRow + i][startCol].copyWith(char: word[i]);
            }
            placed = true;
          }
        }
        attempts++;
      }
    }

    // Fill remaining cells with random kana
    _grid = List.generate(rows, (r) => List.generate(cols, (c) {
      final cell = tempGrid[r][c];
      if (cell.char.isEmpty) {
        return cell.copyWith(
          char: _kanaPool[random.nextInt(_kanaPool.length)],
          id: "${r}_${c}_${random.nextInt(999999)}",
        );
      } else {
        return cell.copyWith(
          id: "${r}_${c}_${random.nextInt(999999)}",
        );
      }
    }));
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

  Future<void> _saveResult() async {
    final modeName = _config.mode.toString().split('.').last;
    final result = KanaLinkResult(
      score: _score,
      wordsFound: _wordsFound,
      timeSeconds: _timeElapsed,
      levelId: _config.levelId,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );

    _history = [result, ..._history].take(10).toList();
    notifyListeners();

    try {
      await _scoreRepo.saveGameHistory(
        gameType: "KANALINK",
        score: _score,
        timeSeconds: _timeElapsed,
        wordsFound: _wordsFound,
        metadata: "$modeName,${_config.levelId}",
      );
    } catch (e) {
      // Silent catch
    }
  }

  void pauseGame() { _isPaused = true; notifyListeners(); }
  void resumeGame() { _isPaused = false; notifyListeners(); }

  void saveAndExit() {
    final stateMap = {
      'grid': _grid.map((row) => row.map((e) => e.toJson()).toList()).toList(),
      'selectedCells': _selectedCells.map((e) => e.toJson()).toList(),
      'currentWord': _currentWord,
      'score': _score,
      'wordsFound': _wordsFound,
      'timeRemaining': _timeRemaining,
      'timeElapsed': _timeElapsed,
      'isGameOver': _isGameOver,
      'config': _config.toJson(),
    };
    _settingsRepo.setStringGeneric("game_state_kanalink", jsonEncode(stateMap));
    _hasSavedGame = true;
    _autoRestoreDone = true;
    _timer?.cancel();
    notifyListeners();
  }

  void abandonGame() {
    _timer?.cancel();
    _settingsRepo.removeGeneric("game_state_kanalink");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    _grid = [];
    _selectedCells = [];
    _currentWord = "";
    _score = 0;
    _wordsFound = 0;
    _isGameOver = false;
    _isPaused = false;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
