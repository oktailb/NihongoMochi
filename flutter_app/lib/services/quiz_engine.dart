import 'dart:math';
import '../models/quiz_models.dart';
import '../repositories/score_repository.dart';

class QuizEngine {
  final ScoreRepository _scoreRepo;
  final Random _random = Random();

  bool isGameInitialized = false;
  List<KanaCharacter> allKana = [];
  int kanaListPosition = 0;
  List<KanaCharacter> currentKanaSet = [];
  List<KanaCharacter> revisionList = [];
  Map<String, GameStatus> kanaStatus = {};
  Map<String, KanaProgress> kanaProgress = {};

  late KanaCharacter currentQuestion;
  KanaQuestionDirection currentDirection = KanaQuestionDirection.normal;
  List<String> currentAnswers = [];

  GameState state = GameState.loading;
  List<AnswerButtonState> buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
  int errorCount = 0;
  double animationSpeed = 1.0;

  QuizEngine(this._scoreRepo);

  void reset(List<KanaCharacter> kanaList) {
    isGameInitialized = false;
    allKana = kanaList;
    kanaListPosition = 0;
    currentKanaSet.clear();
    revisionList.clear();
    kanaStatus.clear();
    kanaProgress.clear();
    currentAnswers = [];
    errorCount = 0;
    state = GameState.loading;
    buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
  }

  bool startNextSet() {
    revisionList.clear();
    kanaStatus.clear();
    kanaProgress.clear();

    if (kanaListPosition >= allKana.length) {
      return false;
    }

    final nextSet = allKana.skip(kanaListPosition).take(10).toList();
    kanaListPosition += nextSet.length;

    currentKanaSet.clear();
    currentKanaSet.addAll(nextSet);
    revisionList.addAll(nextSet);
    for (var k in nextSet) {
      kanaStatus[k.kana] = GameStatus.notAnswered;
      kanaProgress[k.kana] = KanaProgress();
    }
    return true;
  }

  void nextQuestion() {
    if (revisionList.isEmpty) {
      if (!startNextSet()) {
        state = GameState.finished;
        return;
      }
    }

    currentQuestion = revisionList[_random.nextInt(revisionList.length)];
    final progress = kanaProgress[currentQuestion.kana]!;

    if (!progress.normalSolved && !progress.reverseSolved) {
      currentDirection = _random.nextBool() ? KanaQuestionDirection.normal : KanaQuestionDirection.reverse;
    } else if (!progress.normalSolved) {
      currentDirection = KanaQuestionDirection.normal;
    } else {
      currentDirection = KanaQuestionDirection.reverse;
    }

    currentAnswers = _generateAnswers(currentQuestion);
    buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
    state = GameState.waitingForAnswer;
  }

  List<String> _generateAnswers(KanaCharacter correctChar) {
    final isNormal = currentDirection == KanaQuestionDirection.normal;
    final correctAnswer = isNormal ? correctChar.romaji : correctChar.kana;

    final incorrectAnswers = allKana
        .map((k) => isNormal ? k.romaji : k.kana)
        .toSet()
        .where((a) => a != correctAnswer)
        .toList()
      ..shuffle(_random);

    final result = incorrectAnswers.take(3).toList()..add(correctAnswer);
    return result..shuffle(_random);
  }

  Future<bool> submitAnswer(int selectedIndex) async {
    final selectedAnswer = currentAnswers[selectedIndex];
    final isNormal = currentDirection == KanaQuestionDirection.normal;
    final correctAnswerText = isNormal ? currentQuestion.romaji : currentQuestion.kana;
    final isCorrect = selectedAnswer == correctAnswerText;

    await _scoreRepo.saveScore(
      key: currentQuestion.kana,
      wasCorrect: isCorrect,
      type: ScoreType.recognition,
    );

    if (isCorrect) {
      final progress = kanaProgress[currentQuestion.kana]!;
      if (isNormal) {
        progress.normalSolved = true;
      } else {
        progress.reverseSolved = true;
      }

      if (progress.normalSolved && progress.reverseSolved) {
        kanaStatus[currentQuestion.kana] = GameStatus.correct;
        revisionList.remove(currentQuestion);
        buttonStates[selectedIndex] = AnswerButtonState.correct;
      } else {
        kanaStatus[currentQuestion.kana] = GameStatus.partial;
        buttonStates[selectedIndex] = AnswerButtonState.neutral;
      }
    } else {
      errorCount += 1;
      kanaStatus[currentQuestion.kana] = GameStatus.incorrect;
      buttonStates[selectedIndex] = AnswerButtonState.incorrect;

      final correctIndex = currentAnswers.indexOf(correctAnswerText);
      if (correctIndex != -1) {
        buttonStates[correctIndex] = AnswerButtonState.correct;
      }
    }

    state = GameState.showingResult;
    return isCorrect;
  }
}
