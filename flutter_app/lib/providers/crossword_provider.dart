import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/crossword.dart';
import '../repositories/word_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/audio_service.dart';
import '../services/crossword_generator.dart';
import '../db/database.dart';

class CrosswordProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final WordMeaningRepository _wordMeaningRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  final AudioService _audioService;

  CrosswordProvider(
    this._wordRepo,
    this._wordMeaningRepo,
    this._scoreRepo,
    this._settingsRepo,
    this._audioService,
  ) {
    _loadHistory();
    _checkSavedGame();
  }

  CrosswordMode _selectedMode = CrosswordMode.kanas;
  CrosswordHintType _selectedHintType = CrosswordHintType.kanji;
  int _wordCount = 10;
  List<CrosswordGameResult> _scoresHistory = [];

  List<CrosswordCell> _cells = [];
  List<CrosswordWord> _placedWords = [];
  bool _isGenerating = false;
  int _gameTimeSeconds = 0;
  int? _selectedRow;
  int? _selectedCol;
  bool _isVerticalInput = false;
  List<String> _keyboardKeys = [];
  bool _isFinished = false;
  bool _isPaused = false;

  bool _hasSavedGame = false;
  bool _autoRestoreDone = false;

  Timer? _timer;

  // Getters
  CrosswordMode get selectedMode => _selectedMode;
  CrosswordHintType get selectedHintType => _selectedHintType;
  int get wordCount => _wordCount;
  List<CrosswordGameResult> get scoresHistory => _scoresHistory;
  List<CrosswordCell> get cells => _cells;
  List<CrosswordWord> get placedWords => _placedWords;
  bool get isGenerating => _isGenerating;
  int get gameTimeSeconds => _gameTimeSeconds;
  int? get selectedRow => _selectedRow;
  int? get selectedCol => _selectedCol;
  bool get isVerticalInput => _isVerticalInput;
  List<String> get keyboardKeys => _keyboardKeys;
  bool get isFinished => _isFinished;
  bool get isPaused => _isPaused;
  bool get hasSavedGame => _hasSavedGame;

  Future<void> _loadHistory() async {
    try {
      final List<GameHistory> rows = await _scoreRepo.getGameHistory("CROSSWORD");
      _scoresHistory = rows.map((r) {
        final parts = r.metadata?.split(',') ?? [];
        final modeStr = parts.isNotEmpty ? parts[0] : "kanas";
        final mode = modeStr == "kanjis" ? CrosswordMode.kanjis : CrosswordMode.kanas;
        final wordCount = parts.length > 1 ? (int.tryParse(parts[1]) ?? 10) : 10;

        return CrosswordGameResult(
          wordCount: wordCount,
          mode: mode,
          timeSeconds: r.timeSeconds ?? 0,
          completionPercentage: r.score,
          timestamp: r.timestamp,
        );
      }).toList();
      notifyListeners();
    } catch (e) {
      _scoresHistory = [];
      notifyListeners();
    }
  }

  void _checkSavedGame() {
    final savedJson = _settingsRepo.getStringGeneric("game_state_crossword");
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
    final savedJson = _settingsRepo.getStringGeneric("game_state_crossword");
    if (savedJson == null) return;
    try {
      final map = jsonDecode(savedJson) as Map<String, dynamic>;

      final List<dynamic> cellsList = map['cells'];
      _cells = cellsList.map((e) => CrosswordCell.fromJson(e as Map<String, dynamic>)).toList();

      final List<dynamic> wordsList = map['placedWords'];
      _placedWords = wordsList.map((e) => CrosswordWord.fromJson(e as Map<String, dynamic>)).toList();

      _gameTimeSeconds = map['gameTimeSeconds'] as int;

      final modeStr = map['selectedMode'] as String;
      _selectedMode = modeStr == 'KANJIS' || modeStr == 'kanjis' ? CrosswordMode.kanjis : CrosswordMode.kanas;

      final hintStr = map['selectedHintType'] as String;
      _selectedHintType = hintStr == 'MEANING' || hintStr == 'meaning' ? CrosswordHintType.meaning : CrosswordHintType.kanji;

      _isFinished = map['isFinished'] as bool;
      _selectedRow = map['selectedRow'] as int?;
      _selectedCol = map['selectedCol'] as int?;
      _isVerticalInput = map['isVerticalInput'] as bool;

      final List<dynamic> keysList = map['keyboardKeys'] ?? [];
      _keyboardKeys = keysList.map((e) => e.toString()).toList();

      _wordCount = map['wordCount'] as int? ?? 10;

      _isPaused = true;
      _hasSavedGame = false;
      _autoRestoreDone = true;

      _startTimer();
      onRestored();
      notifyListeners();
    } catch (e) {
      _settingsRepo.removeGeneric("game_state_crossword");
      _hasSavedGame = false;
      notifyListeners();
    }
  }

  void onModeSelected(CrosswordMode mode) {
    _selectedMode = mode;
    notifyListeners();
  }

  void onHintTypeSelected(CrosswordHintType hintType) {
    _selectedHintType = hintType;
    notifyListeners();
  }

  void onWordCountSelected(int count) {
    _wordCount = count;
    notifyListeners();
  }

  Future<void> startGame(String locale) async {
    _settingsRepo.removeGeneric("game_state_crossword");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    _isGenerating = true;
    _isFinished = false;
    _isPaused = false;
    _selectedRow = null;
    _selectedCol = null;
    notifyListeners();

    final levelId = _settingsRepo.getMode();
    final words = await _wordRepo.getWordsForLevel(levelId);
    final meanings = await _wordMeaningRepo.getWordMeanings(locale);

    final generator = CrosswordGenerator(
      availableWords: words,
      targetWordCount: _wordCount,
      mode: _selectedMode,
    );

    final result = generator.generate();
    _cells = result['cells'];
    final List<CrosswordWord> genWords = result['words'];

    _placedWords = [];
    for (var cw in genWords) {
      final originalEntry = words.cast<WordEntry?>().firstWhere(
        (entry) => _selectedMode == CrosswordMode.kanjis ? entry?.text == cw.word : entry?.phonetics == cw.word,
        orElse: () => null,
      );
      _placedWords.add(CrosswordWord(
        number: cw.number,
        word: cw.word,
        kanji: cw.kanji,
        meaning: originalEntry != null ? (meanings[originalEntry.id] ?? "???") : "???",
        phonetics: cw.phonetics,
        row: cw.row,
        col: cw.col,
        isHorizontal: cw.isHorizontal,
      ));
    }

    _isGenerating = false;
    _gameTimeSeconds = 0;
    _startTimer();
    notifyListeners();
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!_isPaused && !_isFinished) {
        _gameTimeSeconds++;
        notifyListeners();
      }
    });
  }

  void onCellSelected(int r, int c, {bool preserveOrientation = false}) {
    if (_isFinished || _isPaused) return;

    final words = _getWordsAt(r, c);
    if (words.isEmpty) return;

    if (_selectedRow == r && _selectedCol == c && !preserveOrientation) {
      if (words.length > 1) {
        _isVerticalInput = !_isVerticalInput;
      }
    } else if (!preserveOrientation) {
      final hasHorizontal = words.any((w) => w.isHorizontal);
      final hasVertical = words.any((w) => !w.isHorizontal);

      if (_isVerticalInput && hasVertical) {
        _isVerticalInput = true;
      } else if (!_isVerticalInput && hasHorizontal) {
        _isVerticalInput = false;
      } else {
        _isVerticalInput = hasVertical;
      }
    }

    _selectedRow = r;
    _selectedCol = c;
    _updateKeyboardKeys();
    notifyListeners();
  }

  List<CrosswordWord> _getWordsAt(int r, int c) {
    return _placedWords.where((word) {
      if (word.isHorizontal) {
        return word.row == r && c >= word.col && c < word.col + word.word.length;
      } else {
        return word.col == c && r >= word.row && r < word.row + word.word.length;
      }
    }).toList();
  }

  void _updateKeyboardKeys() {
    final activeWord = _getActiveWordAt(_selectedRow!, _selectedCol!);
    if (activeWord == null) return;

    final wordChars = activeWord.word.split('').toSet();
    final random = Random();

    final allKanas = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまmiむめもやゆよらりるれろわをん".replaceAll("mi", "み").split('');
    final List<String> keys = wordChars.toList();

    while (keys.length < 12) {
      final char = allKanas[random.nextInt(allKanas.length)];
      if (!keys.contains(char)) keys.add(char);
    }

    keys.shuffle();
    _keyboardKeys = keys;
  }

  CrosswordWord? _getActiveWordAt(int r, int c) {
    final words = _getWordsAt(r, c);
    if (_isVerticalInput) {
      return words.cast<CrosswordWord?>().firstWhere((w) => !w!.isHorizontal, orElse: () => words.firstOrNull);
    } else {
      return words.cast<CrosswordWord?>().firstWhere((w) => w!.isHorizontal, orElse: () => words.firstOrNull);
    }
  }

  void onKeyTyped(String char) {
    if (_isFinished || _isPaused || _selectedRow == null) return;

    final cellIndex = _cells.indexWhere((c) => c.r == _selectedRow && c.c == _selectedCol);
    if (cellIndex == -1 || _cells[cellIndex].isCorrect) return;

    final isCorrect = char == _cells[cellIndex].solution;
    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
    }

    _cells[cellIndex] = _cells[cellIndex].copyWith(userInput: char);
    _checkWordCompletion(_selectedRow!, _selectedCol!);
    _moveToNextCell();
    notifyListeners();
  }

  void _moveToNextCell() {
    final word = _getActiveWordAt(_selectedRow!, _selectedCol!);
    if (word == null) return;

    if (word.isHorizontal) {
      if (_selectedCol! + 1 < word.col + word.word.length) {
        onCellSelected(_selectedRow!, _selectedCol! + 1, preserveOrientation: true);
      }
    } else {
      if (_selectedRow! + 1 < word.row + word.word.length) {
        onCellSelected(_selectedRow! + 1, _selectedCol!, preserveOrientation: true);
      }
    }
  }

  void _checkWordCompletion(int r, int c) {
    final words = _getWordsAt(r, c);
    for (var word in words) {
      final wordCells = _cells.where((cell) {
        if (word.isHorizontal) {
          return cell.r == word.row && cell.c >= word.col && cell.c < word.col + word.word.length;
        } else {
          return cell.c == word.col && cell.r >= word.row && cell.r < word.row + word.word.length;
        }
      }).toList();

      if (wordCells.every((cell) => cell.userInput == cell.solution)) {
        for (var wc in wordCells) {
          final idx = _cells.indexWhere((cell) => cell.r == wc.r && cell.c == wc.c);
          _cells[idx] = _cells[idx].copyWith(isCorrect: true);
        }
        final wordIdx = _placedWords.indexWhere((w) => w.number == word.number);
        _placedWords[wordIdx] = _placedWords[wordIdx].copyWith(isSolved: true);
      }
    }

    if (_placedWords.every((w) => w.isSolved)) {
      _isFinished = true;
      _timer?.cancel();
      _saveResult();
    }
  }

  void _saveResult() {
    final modeName = _selectedMode.toString().split('.').last;
    final solvedCount = _placedWords.where((w) => w.isSolved).length;
    final percentage = _placedWords.isEmpty ? 0 : (solvedCount * 100) ~/ _placedWords.length;

    final result = CrosswordGameResult(
      wordCount: _wordCount,
      mode: _selectedMode,
      timeSeconds: _gameTimeSeconds,
      completionPercentage: percentage,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );

    _scoresHistory = [result, ..._scoresHistory].take(10).toList();
    notifyListeners();

    try {
      _scoreRepo.saveGameHistory(
        gameType: "CROSSWORD",
        score: percentage,
        timeSeconds: _gameTimeSeconds,
        wordsFound: solvedCount,
        metadata: "$modeName,$_wordCount",
      );
    } catch (e) {
      // Silent catch
    }
  }

  void pauseGame() { _isPaused = true; notifyListeners(); }
  void resumeGame() { _isPaused = false; notifyListeners(); }

  void saveAndExit() {
    final stateMap = {
      'cells': _cells.map((e) => e.toJson()).toList(),
      'placedWords': _placedWords.map((e) => e.toJson()).toList(),
      'gameTimeSeconds': _gameTimeSeconds,
      'selectedMode': _selectedMode.toString().split('.').last.toUpperCase(),
      'selectedHintType': _selectedHintType.toString().split('.').last.toUpperCase(),
      'isFinished': _isFinished,
      'selectedRow': _selectedRow,
      'selectedCol': _selectedCol,
      'isVerticalInput': _isVerticalInput,
      'keyboardKeys': _keyboardKeys,
      'wordCount': _wordCount,
    };
    _settingsRepo.setStringGeneric("game_state_crossword", jsonEncode(stateMap));
    _hasSavedGame = true;
    _autoRestoreDone = true;
    _timer?.cancel();
    notifyListeners();
  }

  void abandonGame() {
    _timer?.cancel();
    _settingsRepo.removeGeneric("game_state_crossword");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    _cells = [];
    _placedWords = [];
    _isFinished = false;
    _isPaused = false;
    _selectedRow = null;
    _selectedCol = null;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
