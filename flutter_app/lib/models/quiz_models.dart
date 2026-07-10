enum GameStatus { notAnswered, correct, incorrect, partial }

enum GameState { loading, waitingForAnswer, showingResult, finished }

enum AnswerButtonState { defaultState, correct, incorrect, neutral }

enum KanaQuestionDirection { normal, reverse }

class KanaCharacter {
  final String kana;
  final String romaji;
  final String category;

  KanaCharacter({required this.kana, required this.romaji, required this.category});
}

class LearningScore {
  final int successes;
  final int failures;

  LearningScore({required this.successes, required this.failures});
}

class KanaProgress {
  bool normalSolved = false;
  bool reverseSolved = false;
}

class WordQuizItem {
  final String text;
  final String phonetics;
  final String? meaning;

  WordQuizItem({required this.text, required this.phonetics, this.meaning});
}
