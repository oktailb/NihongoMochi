import '../models/quiz_models.dart';

class WordQuizEngine {


  bool isGameInitialized = false;
  List<WordQuizItem> allWords = [];
  List<WordQuizItem> currentWordSet = [];
  List<WordQuizItem> revisionList = [];
  final Map<String, GameStatus> wordStatus = {};
  int wordListPosition = 0;
  WordQuizItem? currentWord;
  List<String> currentAnswers = [];

  void reset() {
    isGameInitialized = false;
    allWords = [];
    currentWordSet = [];
    revisionList = [];
    wordStatus.clear();
    wordListPosition = 0;
    currentWord = null;
    currentAnswers = [];
  }

  bool startNewSet() {
    revisionList.clear();
    wordStatus.clear();

    if (wordListPosition >= allWords.length) {
      return false;
    }

    final nextSet = allWords.skip(wordListPosition).take(10).toList();
    wordListPosition += nextSet.length;

    currentWordSet = nextSet;
    revisionList = List.from(nextSet);
    for (var word in nextSet) {
      wordStatus[word.text] = GameStatus.notAnswered;
    }
    return true;
  }
}
