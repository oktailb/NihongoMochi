import 'package:flutter/material.dart';
import '../models/kana.dart';
import '../models/quiz_models.dart';
import '../repositories/kana_repository.dart';
import '../repositories/score_repository.dart';
import '../services/quiz_engine.dart';
import '../services/audio_service.dart';
import '../services/statistics_service.dart';

class KanaQuizProvider extends ChangeNotifier {
  final KanaRepository _kanaRepo;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;
  final StatisticsService _statisticsService;
  late final QuizEngine _engine;

  bool _isInitialized = false;
  String _title = "";
  late KanaType _currentType;
  int _globalMastery = 0;
  int _sessionMastery = 0;

  QuizEngine get engine => _engine;
  bool get isInitialized => _isInitialized;
  String get title => _title;
  int get globalMastery => _globalMastery;
  int get sessionMastery => _sessionMastery;

  KanaQuizProvider(this._kanaRepo, this._scoreRepo, this._audioService, this._statisticsService) {
    _engine = QuizEngine(_scoreRepo);
  }

  Future<void> startQuiz(KanaType type) async {
    _currentType = type;
    _isInitialized = false;
    notifyListeners();

    final allKanas = await _kanaRepo.getKanaEntries(type);
    final kanaCharacters = allKanas.map((e) => KanaCharacter(
      kana: e.character,
      romaji: e.romaji,
      category: e.category,
    )).toList();

    // Progression pédagogique KMP
    final gojuon = kanaCharacters.where((c) => c.category == "gojuon").toList();
    final dakuon = kanaCharacters.where((c) => c.category == "dakuon" || c.category == "handakuon").toList();
    final yoon = kanaCharacters.where((c) => c.category == "yoon").toList();

    // 2. Calcule la maîtrise du Gojuon
    final gojuonMastery = await _calculateMastery(gojuon);
    
    final List<KanaCharacter> finalPool = [];
    finalPool.addAll(gojuon);

    // 3. Si Gojuon > 20% de maîtrise
    if (gojuonMastery >= 0.20) {
      finalPool.addAll(dakuon);
      
      // 4. Calcule la maîtrise globale (Gojuon + Dakuon)
      final dakuonMastery = await _calculateMastery(finalPool);
      
      // 5. Si global > 45%, ajoute les Yoons
      if (dakuonMastery >= 0.45) {
        finalPool.addAll(yoon);
      }
    }

    finalPool.shuffle();

    _engine.reset(finalPool);
    _engine.startNextSet();
    _engine.nextQuestion();

    _title = type == KanaType.hiragana ? "Quiz Hiragana" : "Quiz Katakana";
    _isInitialized = true;
    notifyListeners();
  }

  Future<double> _calculateMastery(List<KanaCharacter> chars) async {
    if (chars.isEmpty) return 0.0;
    final list = chars.map((c) => c.kana).toList();
    final pct = await _statisticsService.calculateSessionScore(list, ScoreType.recognition);
    return pct / 100.0;
  }

  Future<void> _updateMastery(KanaType type) async {
    final levelKey = type == KanaType.hiragana ? "hiragana" : "katakana";
    _globalMastery = await _statisticsService.getPercentageForLevel(levelKey, ScoreType.recognition, "en_GB");
    
    final sessionList = _engine.currentKanaSet.map((c) => c.kana).toList();
    _sessionMastery = await _statisticsService.calculateSessionScore(sessionList, ScoreType.recognition);
    notifyListeners();
  }

  Future<void> submitAnswer(int index) async {
    final bool isCorrect = await _engine.submitAnswer(index);

    if (isCorrect) {
      _audioService.playSound("assets/files/sounds/correct.mp3");
    } else {
      _audioService.playSound("assets/files/sounds/incorrect.mp3");
    }

    notifyListeners();

    await Future.delayed(const Duration(milliseconds: 1000));
    _engine.nextQuestion();

    if (_engine.state == GameState.finished) {
      await _updateMastery(_currentType);
    }

    notifyListeners();
  }
}
