import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/shiritori.dart';
import '../repositories/word_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/settings_repository.dart';
import '../repositories/kana_repository.dart';
import '../services/audio_service.dart';
import '../utils/kana_utils.dart';
import '../utils/romaji_to_kana.dart';

enum ShiritoriError {
  none,
  invalidStart,
  endsInN,
  alreadyUsed,
  wordNotFound
}

class ShiritoriProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final WordMeaningRepository _meaningRepo;
  final ScoreRepository _scoreRepo;
  final SettingsRepository _settingsRepo;
  final KanaRepository _kanaRepo;
  final AudioService _audioService;

  ShiritoriProvider(
    this._wordRepo,
    this._meaningRepo,
    this._scoreRepo,
    this._settingsRepo,
    this._kanaRepo,
    this._audioService,
  ) {
    _loadHistory();
  }

  ShiritoriGameState _gameState = ShiritoriGameState.idle;
  List<ShiritoriWord> _playedWords = [];
  String _lastKana = "";
  ShiritoriError _error = ShiritoriError.none;
  int _score = 0;
  int _bestScore = 0;
  List<ShiritoriGameResult> _scoresHistory = [];
  int _gameTimeSeconds = 0;
  bool _isVictory = false;
  String _inputText = "";

  Timer? _timer;
  List<WordEntry> _allWords = [];
  List<WordEntry> _aiAvailableWords = [];
  Set<String> _usedPhonetics = {};
  Map<String, String> _currentMeanings = {};

  // Getters
  ShiritoriGameState get gameState => _gameState;
  List<ShiritoriWord> get playedWords => _playedWords;
  String get lastKana => _lastKana;
  ShiritoriError get error => _error;
  int get score => _score;
  int get bestScore => _bestScore;
  List<ShiritoriGameResult> get scoresHistory => _scoresHistory;
  int get gameTimeSeconds => _gameTimeSeconds;
  bool get isVictory => _isVictory;
  String get inputText => _inputText;

  void _loadHistory() {
    // _scoresHistory = await _scoreRepo.getShiritoriHistory();
    // _bestScore = ...
    notifyListeners();
  }

  void onInputChanged(String text) {
    String finalText = text;
    final replacement = RomajiToKana.checkReplacement(text);
    if (replacement != null) {
      final entry = replacement.entries.first;
      finalText = text.substring(0, text.length - entry.key) + entry.value;
    }
    _inputText = finalText;
    notifyListeners();
  }

  Future<void> startGame(String locale) async {
    _gameState = ShiritoriGameState.loading;
    notifyListeners();

    _currentMeanings = await _meaningRepo.getWordMeanings(locale);
    _allWords = await _wordRepo.getAllWords();

    // Simple logic for AI bank based on level
    final levelId = _settingsRepo.getMode();
    _aiAvailableWords = await _wordRepo.getWordsForLevel(levelId);

    _usedPhonetics.clear();
    _playedWords = [];
    _score = 0;
    _inputText = "";
    _isVictory = false;
    _error = ShiritoriError.none;
    _gameTimeSeconds = 0;

    // AI starts with a random word
    final firstWordCandidates = _aiAvailableWords.where((w) {
      final hira = KanaUtils.katakanaToHiragana(w.phonetics);
      return !hira.endsWith("ん");
    }).toList();

    if (firstWordCandidates.isNotEmpty) {
      final firstWord = firstWordCandidates[Random().nextInt(firstWordCandidates.length)];
      _addWord(firstWord, false);
      _lastKana = _getNextTargetKana(firstWord.phonetics);
    } else {
      _lastKana = "";
    }

    _gameState = ShiritoriGameState.playerTurn;
    _startTimer();
    notifyListeners();
  }

  void _startTimer() {
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_gameState != ShiritoriGameState.paused && _gameState != ShiritoriGameState.gameOver) {
        _gameTimeSeconds++;
        notifyListeners();
      }
    });
  }

  void onPlayerSubmit() {
    if (_gameState != ShiritoriGameState.playerTurn) return;
    _error = ShiritoriError.none;

    final input = _inputText.trim();
    if (input.isEmpty) return;

    final normalizedInput = KanaUtils.katakanaToHiragana(input);

    final wordEntry = _allWords.cast<WordEntry?>().firstWhere(
      (w) => w?.text == input || KanaUtils.katakanaToHiragana(w?.phonetics ?? "") == normalizedInput,
      orElse: () => null,
    );

    if (wordEntry == null) {
      _error = ShiritoriError.wordNotFound;
      notifyListeners();
      return;
    }

    final phonetics = wordEntry.phonetics;
    final normalizedPhonetics = KanaUtils.katakanaToHiragana(phonetics);

    if (normalizedPhonetics.endsWith("ん")) {
      _error = ShiritoriError.endsInN;
      _gameOver(false);
      return;
    }

    if (_lastKana.isNotEmpty && !_isValidStart(phonetics, _lastKana)) {
      _error = ShiritoriError.invalidStart;
      notifyListeners();
      return;
    }

    if (_usedPhonetics.contains(normalizedPhonetics)) {
      _error = ShiritoriError.alreadyUsed;
      notifyListeners();
      return;
    }

    _audioService.playSound("assets/files/sounds/correct.mp3");
    _addWord(wordEntry, true);
    _score++;
    _inputText = "";
    _aiTurn(phonetics);
    notifyListeners();
  }

  void _aiTurn(String lastPlayerWordPhonetics) async {
    if (_gameState == ShiritoriGameState.gameOver) return;

    _gameState = ShiritoriGameState.aiTurn;
    notifyListeners();

    final targetKana = _getNextTargetKana(lastPlayerWordPhonetics);
    await Future.delayed(const Duration(milliseconds: 1500));

    final possibleWords = _aiAvailableWords.where((w) =>
      _isValidStart(w.phonetics, targetKana) &&
      !w.phonetics.endsWith("ん") &&
      !_usedPhonetics.contains(KanaUtils.katakanaToHiragana(w.phonetics))
    ).toList();

    if (possibleWords.isEmpty) {
      _gameOver(true);
    } else {
      final aiWord = possibleWords[Random().nextInt(possibleWords.length)];
      _addWord(aiWord, false);
      _lastKana = _getNextTargetKana(aiWord.phonetics);
      _gameState = ShiritoriGameState.playerTurn;
      notifyListeners();
    }
  }

  void _addWord(WordEntry entry, bool isPlayer) {
    final meaning = _currentMeanings[entry.id] ?? "";
    _playedWords = [..._playedWords, ShiritoriWord(
      word: entry.text,
      phonetics: entry.phonetics,
      meaning: meaning,
      isPlayer: isPlayer,
      timestamp: DateTime.now().millisecondsSinceEpoch,
    )];
    _usedPhonetics.add(KanaUtils.katakanaToHiragana(entry.phonetics));
  }

  bool _isValidStart(String phonetics, String targetKana) {
    if (phonetics.isEmpty) return false;
    final firstChar = KanaUtils.katakanaToHiragana(phonetics.substring(0, 1));
    final normalizedTarget = KanaUtils.katakanaToHiragana(targetKana);
    return firstChar == normalizedTarget;
  }

  String _getNextTargetKana(String phonetics) {
    if (phonetics.isEmpty) return "";
    final hiraPhonetics = KanaUtils.katakanaToHiragana(phonetics);
    String lastChar = hiraPhonetics.substring(hiraPhonetics.length - 1);

    if (lastChar == "ー" && hiraPhonetics.length > 1) {
      lastChar = hiraPhonetics.substring(hiraPhonetics.length - 2, hiraPhonetics.length - 1);
    }

    const normalization = {
      "ゃ": "や", "ゅ": "ゆ", "ょ": "よ",
      "ぁ": "あ", "ぃ": "い", "ぅ": "う", "ぇ": "え", "ぉ": "お",
      "っ": "つ"
    };

    return normalization[lastChar] ?? lastChar;
  }

  void _gameOver(bool victory) {
    _gameState = ShiritoriGameState.gameOver;
    _isVictory = victory;
    _timer?.cancel();
    if (victory) {
       _audioService.playSound("assets/files/sounds/correct.mp3"); // success sound
    } else {
       _audioService.playSound("assets/files/sounds/game_over.mp3");
    }
    notifyListeners();
  }

  void pauseGame() { _gameState = ShiritoriGameState.paused; notifyListeners(); }
  void resumeGame() { _gameState = ShiritoriGameState.playerTurn; notifyListeners(); }
  void abandonGame() { _gameOver(false); }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
