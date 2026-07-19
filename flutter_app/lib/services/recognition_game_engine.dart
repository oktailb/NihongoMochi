import 'dart:math';
import '../models/quiz_models.dart';
import '../models/dictionary.dart';
import '../repositories/score_repository.dart';
import '../utils/kana_utils.dart';

enum QuestionDirection { normal, reverse }

class KanjiProgress {
  bool normalSolved = false;
  bool reverseSolved = false;
}

class RecognitionGameEngine {
  final ScoreRepository _scoreRepo;
  final Random _random = Random();

  bool isGameInitialized = false;
  final List<DictionaryItem> allKanjiDetails = [];
  final List<DictionaryItem> currentKanjiSet = [];
  final List<DictionaryItem> revisionList = [];
  final Map<String, GameStatus> kanjiStatus = {};
  final Map<String, KanjiProgress> kanjiProgress = {};
  
  int kanjiListPosition = 0;
  DictionaryItem? currentKanji;
  
  int correctAnswersInSession = 0;
  int totalAnswersInSession = 0;

  late String correctAnswer;
  late String gameMode;
  late String readingMode;
  QuestionDirection currentDirection = QuestionDirection.normal;
  
  List<String> currentAnswers = [];
  GameState state = GameState.loading;
  List<AnswerButtonState> buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
  int errorCount = 0;
  double animationSpeed = 1.0;

  String pronunciationMode = "Hiragana"; // "Hiragana" or "Roman"
  bool isProcessingAnswer = false;

  RecognitionGameEngine(this._scoreRepo);

  void resetState() {
    isGameInitialized = false;
    allKanjiDetails.clear();
    currentKanjiSet.clear();
    revisionList.clear();
    kanjiStatus.clear();
    kanjiProgress.clear();
    kanjiListPosition = 0;
    currentAnswers = [];
    currentKanji = null;
    isProcessingAnswer = false;
    errorCount = 0;
    state = GameState.loading;
    buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
    correctAnswersInSession = 0;
    totalAnswersInSession = 0;
  }

  List<GameStatus> get currentSetStatus => 
    currentKanjiSet.map((k) => kanjiStatus[k.character] ?? GameStatus.notAnswered).toList();

  void startGame() {
    if (allKanjiDetails.isEmpty) {
      state = GameState.finished;
      return;
    }

    kanjiStatus.clear();
    kanjiProgress.clear();
    for (var item in allKanjiDetails) {
      kanjiStatus[item.character] = GameStatus.notAnswered;
      kanjiProgress[item.character] = KanjiProgress();
    }

    if (startNewSet()) {
      nextQuestion();
      state = GameState.waitingForAnswer;
    } else {
      state = GameState.finished;
    }
  }

  bool startNewSet() {
    revisionList.clear();
    if (kanjiListPosition >= allKanjiDetails.length) return false;

    final nextSet = allKanjiDetails.skip(kanjiListPosition).take(10).toList();
    kanjiListPosition += nextSet.length;

    currentKanjiSet.clear();
    currentKanjiSet.addAll(nextSet);
    revisionList.addAll(nextSet);
    return true;
  }

  void nextQuestion() {
    if (revisionList.isEmpty) {
      if (!startNewSet()) {
        state = GameState.finished;
        return;
      }
    }

    currentKanji = revisionList[_random.nextInt(revisionList.length)];
    final progress = kanjiProgress[currentKanji!.character]!;

    if (!progress.normalSolved && !progress.reverseSolved) {
      currentDirection = _random.nextBool() ? QuestionDirection.normal : QuestionDirection.reverse;
    } else if (!progress.normalSolved) {
      currentDirection = QuestionDirection.normal;
    } else {
      currentDirection = QuestionDirection.reverse;
    }

    currentAnswers = _generateAnswers(currentKanji!);
    buttonStates = List.generate(4, (_) => AnswerButtonState.defaultState);
  }

  List<String> _generateAnswers(DictionaryItem correctKanji) {
    if (currentDirection == QuestionDirection.normal) {
      String correctButtonText;
      if (gameMode == "meaning") {
        correctAnswer = correctKanji.meanings.isNotEmpty ? correctKanji.meanings.first : "";
        correctButtonText = correctKanji.meanings.take(3).join("\n");
      } else {
        correctButtonText = getFormattedReadings(correctKanji);
        correctAnswer = correctButtonText.split("\n").first;
      }

      if (correctAnswer.isEmpty) return ["", "", "", ""];

      final incorrectPool = allKanjiDetails
          .where((k) => k.id != correctKanji.id)
          .map((detail) {
            if (gameMode == "meaning") {
              return detail.meanings.take(3).join("\n");
            } else {
              return getFormattedReadings(detail);
            }
          })
          .where((s) => s.isNotEmpty)
          .toSet()
          .toList()
        ..shuffle(_random);

      final result = incorrectPool.take(3).toList()..add(correctButtonText);
      return result..shuffle(_random);
    } else {
      correctAnswer = correctKanji.character;
      final correctButtonText = correctKanji.character;

      final incorrectPool = allKanjiDetails
          .where((k) => k.id != correctKanji.id)
          .map((k) => k.character)
          .toSet()
          .toList()
        ..shuffle(_random);

      final result = incorrectPool.take(3).toList()..add(correctButtonText);
      return result..shuffle(_random);
    }
  }

  String getFormattedReadings(DictionaryItem kanji) {
    final onReadings = kanji.readings.where((r) => r.type == 'on').toList();
    final kunReadings = kanji.readings.where((r) => r.type == 'kun').toList();

    if (readingMode == "common") {
      // In a real app, we might have frequency data for readings too.
    } else {
      onReadings.shuffle(_random);
      kunReadings.shuffle(_random);
    }

    final onStrings = onReadings.take(2).map((r) => KanaUtils.hiraganaToKatakana(r.text)).toList();
    final kunStrings = kunReadings.take(2).map((r) => r.text).toList();

    return (onStrings + kunStrings).join("\n");
  }

  Future<bool> submitAnswer(int selectedIndex) async {
    final kanji = currentKanji;
    if (kanji == null || isProcessingAnswer || state == GameState.finished) return false;
    isProcessingAnswer = true;

    final selectedAnswer = currentAnswers[selectedIndex];
    final isCorrect = currentDirection == QuestionDirection.normal
        ? selectedAnswer.split("\n").any((line) => line.trim().toLowerCase() == correctAnswer.trim().toLowerCase())
        : selectedAnswer == correctAnswer;

    totalAnswersInSession++;
    if (isCorrect) {
      correctAnswersInSession++;
    } else {
      errorCount++;
    }

    await _scoreRepo.saveScore(
      key: kanji.character,
      wasCorrect: isCorrect,
      type: gameMode == "meaning" ? ScoreType.recognition : ScoreType.reading,
    );

    final progress = kanjiProgress[kanji.character]!;
    if (isCorrect) {
      if (currentDirection == QuestionDirection.normal) {
        progress.normalSolved = true;
      } else {
        progress.reverseSolved = true;
      }

      if (progress.normalSolved && progress.reverseSolved) {
        kanjiStatus[kanji.character] = GameStatus.correct;
        revisionList.remove(kanji);
        buttonStates[selectedIndex] = AnswerButtonState.correct;
      } else {
        kanjiStatus[kanji.character] = GameStatus.partial;
        buttonStates[selectedIndex] = AnswerButtonState.neutral;
      }
    } else {
      kanjiStatus[kanji.character] = GameStatus.incorrect;
      buttonStates[selectedIndex] = AnswerButtonState.incorrect;
      
      final correctIdx = currentAnswers.indexWhere((ans) => currentDirection == QuestionDirection.normal 
        ? ans.split("\n").any((line) => line.trim().toLowerCase() == correctAnswer.trim().toLowerCase())
        : ans == correctAnswer
      );
      if (correctIdx != -1) buttonStates[correctIdx] = AnswerButtonState.correct;
    }

    state = GameState.showingResult;
    return isCorrect;
  }

  void acknowledgeResult() {
    isProcessingAnswer = false;
    if (revisionList.isEmpty) {
      if (!startNewSet()) {
        state = GameState.finished;
        return;
      }
    }
    nextQuestion();
    state = GameState.waitingForAnswer;
  }
}
