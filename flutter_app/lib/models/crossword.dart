import 'package:json_annotation/json_annotation.dart';

part 'crossword.g.dart';

enum CrosswordMode {
  @JsonValue('KANAS')
  kanas,
  @JsonValue('KANJIS')
  kanjis
}

enum CrosswordHintType {
  @JsonValue('KANJI')
  kanji,
  @JsonValue('MEANING')
  meaning
}

@JsonSerializable()
class CrosswordCell {
  final int r;
  final int c;
  final String solution;
  final String userInput;
  final bool isBlack;
  final int? number;
  final bool isCorrect;

  CrosswordCell({
    required this.r,
    required this.c,
    this.solution = "",
    this.userInput = "",
    this.isBlack = true,
    this.number,
    this.isCorrect = false,
  });

  CrosswordCell copyWith({
    String? userInput,
    bool? isCorrect,
  }) {
    return CrosswordCell(
      r: r,
      c: c,
      solution: solution,
      userInput: userInput ?? this.userInput,
      isBlack: isBlack,
      number: number,
      isCorrect: isCorrect ?? this.isCorrect,
    );
  }

  factory CrosswordCell.fromJson(Map<String, dynamic> json) => _$CrosswordCellFromJson(json);
  Map<String, dynamic> toJson() => _$CrosswordCellToJson(this);
}

@JsonSerializable()
class CrosswordWord {
  final int number;
  final String word; // Grid solution string (Kana or Kanji)
  final String kanji;
  final String meaning;
  final String phonetics;
  final int row;
  final int col;
  final bool isHorizontal;
  final bool isSolved;

  CrosswordWord({
    required this.number,
    required this.word,
    required this.kanji,
    required this.meaning,
    this.phonetics = "",
    required this.row,
    required this.col,
    required this.isHorizontal,
    this.isSolved = false,
  });

  CrosswordWord copyWith({bool? isSolved}) {
    return CrosswordWord(
      number: number,
      word: word,
      kanji: kanji,
      meaning: meaning,
      phonetics: phonetics,
      row: row,
      col: col,
      isHorizontal: isHorizontal,
      isSolved: isSolved ?? this.isSolved,
    );
  }

  factory CrosswordWord.fromJson(Map<String, dynamic> json) => _$CrosswordWordFromJson(json);
  Map<String, dynamic> toJson() => _$CrosswordWordToJson(this);
}

@JsonSerializable()
class CrosswordGameResult {
  final int wordCount;
  final CrosswordMode mode;
  final int timeSeconds;
  final int completionPercentage;
  final int timestamp;

  CrosswordGameResult({
    required this.wordCount,
    required this.mode,
    required this.timeSeconds,
    required this.completionPercentage,
    required this.timestamp,
  });

  factory CrosswordGameResult.fromJson(Map<String, dynamic> json) => _$CrosswordGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$CrosswordGameResultToJson(this);
}
