import 'package:flutter/material.dart';
import '../models/kana.dart';
import '../models/quiz_models.dart';
import '../repositories/kana_repository.dart';
import '../repositories/score_repository.dart';
import '../services/quiz_engine.dart';
import '../services/audio_service.dart';

class KanaQuizProvider extends ChangeNotifier {
  final KanaRepository _kanaRepo;
  final ScoreRepository _scoreRepo;
  final AudioService _audioService;
  late final QuizEngine _engine;

  bool _isInitialized = false;
  String _title = "";

  QuizEngine get engine => _engine;
  bool get isInitialized => _isInitialized;
  String get title => _title;

  KanaQuizProvider(this._kanaRepo, this._scoreRepo, this._audioService) {
    _engine = QuizEngine(_scoreRepo);
  }

  Future<void> startQuiz(KanaType type) async {
    _isInitialized = false;
    notifyListeners();

    final allKanas = await _kanaRepo.getKanaEntries(type);
    final kanaCharacters = allKanas.map((e) => KanaCharacter(
      kana: e.character,
      romaji: e.romaji,
      category: e.category,
    )).toList();

    _engine.reset(kanaCharacters);
    _engine.startNextSet();
    _engine.nextQuestion();

    _title = type == KanaType.hiragana ? "Quiz Hiragana" : "Quiz Katakana";
    _isInitialized = true;
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
    notifyListeners();
  }
}
