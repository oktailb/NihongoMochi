import 'dart:async';
import 'dart:convert';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/taquin.dart';
import '../models/kana.dart';
import '../repositories/kana_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../services/audio_service.dart';
import '../db/database.dart';

class TaquinProvider extends ChangeNotifier {
  final KanaRepository _kanaRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  final AudioService _audioService;

  TaquinProvider(
    this._kanaRepo,
    this._scoreRepo,
    this._settingsRepo,
    this._audioService,
  ) {
    _loadScoresHistory();
    _checkSavedGame();
  }

  // --- Setup State ---
  TaquinMode _selectedMode = TaquinMode.hiragana;
  int _selectedRows = 3;
  List<TaquinGameResult> _scoresHistory = [];
  bool _hasSavedGame = false;

  // --- Game State ---
  TaquinGameState? _gameState;
  bool _isPaused = false;
  bool _autoRestoreDone = false;
  Timer? _timer;

  // Getters
  TaquinMode get selectedMode => _selectedMode;
  int get selectedRows => _selectedRows;
  List<TaquinGameResult> get scoresHistory => _scoresHistory;
  bool get hasSavedGame => _hasSavedGame;
  TaquinGameState? get gameState => _gameState;
  bool get isPaused => _isPaused;

  Future<void> _loadScoresHistory() async {
    try {
      final List<GameHistory> rows = await _scoreRepo.getGameHistory("TAQUIN");
      _scoresHistory = rows.map((r) => TaquinGameResult(
        mode: TaquinMode.values.firstWhere(
          (m) => m.toString().split('.').last.toUpperCase() == r.metadata?.toUpperCase(),
          orElse: () => TaquinMode.hiragana,
        ),
        rows: r.rows ?? 3,
        moves: r.moves ?? 0,
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
    final savedJson = _settingsRepo.getStringGeneric("game_state_taquin");
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
    final savedJson = _settingsRepo.getStringGeneric("game_state_taquin");
    if (savedJson == null) return;
    try {
      final restoredMap = jsonDecode(savedJson) as Map<String, dynamic>;
      final restored = TaquinGameState.fromJson(restoredMap);
      if (!restored.isSolved) {
        _gameState = restored;
        _selectedMode = restored.mode;
        _selectedRows = restored.rows;
        _isPaused = true;
        _startTimer();
        onRestored();
        notifyListeners();
      }
    } catch (e) {
      _settingsRepo.removeGeneric("game_state_taquin");
      _hasSavedGame = false;
      notifyListeners();
    }
  }

  void onModeSelected(TaquinMode mode) {
    _selectedMode = mode;
    if (mode == TaquinMode.numbers) {
      _selectedRows = 4;
    }
    notifyListeners();
  }

  void onRowsSelected(int rows) {
    _selectedRows = rows;
    notifyListeners();
  }

  Future<void> startGame() async {
    _settingsRepo.removeGeneric("game_state_taquin");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    final rows = _selectedRows;
    final cols = _selectedMode == TaquinMode.numbers ? 4 : 5;

    List<TaquinPiece> solvedPieces = [];

    if (_selectedMode != TaquinMode.numbers) {
      final entries = await _kanaRepo.getKanaEntries(
        _selectedMode == TaquinMode.hiragana ? KanaType.hiragana : KanaType.katakana,
      );
      final filteredEntries = entries.where((e) => e.line <= rows).toList();

      for (int r = 1; r <= rows; r++) {
        for (int c = 1; c <= cols; c++) {
          final entry = filteredEntries.cast<KanaEntry?>().firstWhere(
            (e) => e?.line == r && e?.column == c,
            orElse: () => null,
          );
          if (r == rows && c == cols) {
            solvedPieces.add(TaquinPiece(character: "", targetLine: r, targetColumn: c, isBlank: true));
          } else if (entry != null) {
            solvedPieces.add(TaquinPiece(character: entry.character, targetLine: r, targetColumn: c));
          } else {
            solvedPieces.add(TaquinPiece(character: "", targetLine: 0, targetColumn: 0));
          }
        }
      }
    } else {
      final numberEntries = await _kanaRepo.getNumberEntries();
      for (int i = 0; i < 15; i++) {
        final entry = numberEntries[i];
        solvedPieces.add(TaquinPiece(character: entry.character, targetLine: (i ~/ 4) + 1, targetColumn: (i % 4) + 1));
      }
      solvedPieces.add(TaquinPiece(character: "", targetLine: 4, targetColumn: 4, isBlank: true));
    }

    // Shuffle by making random valid moves to ensure solvability
    List<TaquinPiece> shuffledPieces = List.from(solvedPieces);
    final random = Random();
    int shuffleMoves = rows * cols * 20;

    int blankIndex = shuffledPieces.indexWhere((p) => p.isBlank);
    for (int i = 0; i < shuffleMoves; i++) {
      int bRow = blankIndex ~/ cols;
      int bCol = blankIndex % cols;

      List<int> neighbors = [];
      if (bRow > 0) neighbors.add(blankIndex - cols);
      if (bRow < rows - 1) neighbors.add(blankIndex + cols);
      if (bCol > 0) neighbors.add(blankIndex - 1);
      if (bCol < cols - 1) neighbors.add(blankIndex + 1);

      int moveIndex = neighbors[random.nextInt(neighbors.length)];
      // Swap
      final temp = shuffledPieces[blankIndex];
      shuffledPieces[blankIndex] = shuffledPieces[moveIndex];
      shuffledPieces[moveIndex] = temp;
      blankIndex = moveIndex;
    }

    _gameState = TaquinGameState(
      pieces: shuffledPieces,
      rows: rows,
      cols: cols,
      mode: _selectedMode,
    );
    _isPaused = false;
    _startTimer();
    notifyListeners();
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (!_isPaused && _gameState != null && !_gameState!.isSolved) {
        _gameState = _gameState!.copyWith(timeSeconds: _gameState!.timeSeconds + 1);
        notifyListeners();
      }
    });
  }

  void onPieceClicked(int clickedIndex) {
    final state = _gameState;
    if (state == null || state.isSolved || _isPaused) return;

    int blankIndex = state.pieces.indexWhere((p) => p.isBlank);
    if (blankIndex == -1) return;

    int clickedRow = clickedIndex ~/ state.cols;
    int clickedCol = clickedIndex % state.cols;
    int blankRow = blankIndex ~/ state.cols;
    int blankCol = blankIndex % state.cols;

    if (clickedRow == blankRow || clickedCol == blankCol) {
      List<TaquinPiece> newPieces = List.from(state.pieces);

      if (clickedRow == blankRow) {
        int step = clickedCol < blankCol ? 1 : -1;
        int curr = blankIndex;
        while (curr != clickedIndex) {
          int next = curr - step;
          newPieces[curr] = newPieces[next];
          curr = next;
        }
        newPieces[clickedIndex] = state.pieces[blankIndex];
      } else {
        int step = clickedRow < blankRow ? 1 : -1;
        int curr = blankIndex;
        while (curr != clickedIndex) {
          int next = curr - (step * state.cols);
          newPieces[curr] = newPieces[next];
          curr = next;
        }
        newPieces[clickedIndex] = state.pieces[blankIndex];
      }

      _gameState = state.copyWith(
        pieces: newPieces,
        moves: state.moves + 1,
      );
      _checkSolved();
      notifyListeners();
    }
  }

  void _checkSolved() {
    if (_gameState == null) return;

    bool isSolved = true;
    for (int i = 0; i < _gameState!.pieces.length; i++) {
      final piece = _gameState!.pieces[i];
      if (piece.character.isEmpty && !piece.isBlank) continue;

      int currentRow = (i ~/ _gameState!.cols) + 1;
      int currentCol = (i % _gameState!.cols) + 1;

      if (piece.isBlank) {
        if (currentRow != _gameState!.rows || currentCol != _gameState!.cols) {
           isSolved = false;
           break;
        }
      } else if (piece.targetLine != currentRow || piece.targetColumn != currentCol) {
        isSolved = false;
        break;
      }
    }

    if (isSolved) {
      _gameState = _gameState!.copyWith(isSolved: true);
      _timer?.cancel();
      _settingsRepo.removeGeneric("game_state_taquin");
      _hasSavedGame = false;
      _autoRestoreDone = true;
      _audioService.playSound("assets/files/sounds/correct.mp3");
      _saveResult();
    }
  }

  Future<void> _saveResult() async {
    final state = _gameState;
    if (state == null) return;

    final result = TaquinGameResult(
      mode: _selectedMode,
      rows: state.rows,
      moves: state.moves,
      timeSeconds: state.timeSeconds,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    );

    _scoresHistory = [result, ..._scoresHistory].take(10).toList();
    notifyListeners();

    try {
      await _scoreRepo.saveGameHistory(
        gameType: "TAQUIN",
        score: state.moves,
        moves: state.moves,
        timeSeconds: state.timeSeconds,
        rows: state.rows,
        metadata: _selectedMode.toString().split('.').last,
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
    if (_gameState != null) {
      final jsonStr = jsonEncode(_gameState!.toJson());
      _settingsRepo.setStringGeneric("game_state_taquin", jsonStr);
      _hasSavedGame = true;
      _autoRestoreDone = true;
      notifyListeners();
    }
  }

  void abandonGame() {
    _timer?.cancel();
    _settingsRepo.removeGeneric("game_state_taquin");
    _hasSavedGame = false;
    _autoRestoreDone = true;
    if (_gameState != null && !_gameState!.isSolved) {
      _audioService.playSound("assets/files/sounds/game_over.mp3");
    }
    _gameState = null;
    notifyListeners();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
