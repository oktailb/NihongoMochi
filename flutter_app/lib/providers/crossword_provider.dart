import 'dart:async';
import 'package:flutter/material.dart';
import '../models/crossword.dart';
import '../repositories/word_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/audio_service.dart';
import '../services/crossword_generator.dart';

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

  void _loadHistory() {
    // Fetch from repository in real app
    notifyListeners();
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

    // Fill meanings
    _placedWords = genWords.map((cw) {
      final originalEntry = words.cast<WordEntry?>().firstWhere(
        (entry) => _selectedMode == CrosswordMode.kanjis ? entry?.text == cw.word : entry?.phonetics == cw.word,
        orElse: () => null,
      );
      return cw.copyWith(isSolved: false); // Meaning would be here if we updated model
    }).toList();

    // Actually we need to make sure meanings are available
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

    final allKanas = "あいうえおかきくけこさしすせそたちつteとなにぬねのはひふへほまみむめもやゆよらりるれろわをん".split('');
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
        // Mark as correct
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
    // Save to history and db
  }

  void pauseGame() { _isPaused = true; notifyListeners(); }
  void resumeGame() { _isPaused = false; notifyListeners(); }
  void abandonGame() { _timer?.cancel(); _isFinished = true; notifyListeners(); }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
