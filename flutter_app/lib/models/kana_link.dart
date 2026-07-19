import 'package:json_annotation/json_annotation.dart';

part 'kana_link.g.dart';

enum KanaLinkMode { timeAttack, survival }

@JsonSerializable()
class KanaLinkResult {
  final int score;
  final int wordsFound;
  final int timeSeconds;
  final String levelId;
  final int timestamp;

  KanaLinkResult({
    required this.score,
    required this.wordsFound,
    required this.timeSeconds,
    required this.levelId,
    required this.timestamp,
  });

  factory KanaLinkResult.fromJson(Map<String, dynamic> json) => _$KanaLinkResultFromJson(json);
  Map<String, dynamic> toJson() => _$KanaLinkResultToJson(this);
}

@JsonSerializable()
class KanaLinkCell {
  final String id;
  final String char;
  final int row;
  final int col;
  final bool isSelected;
  final bool isMatched;

  KanaLinkCell({
    required this.id,
    required this.char,
    required this.row,
    required this.col,
    this.isSelected = false,
    this.isMatched = false,
  });

  KanaLinkCell copyWith({
    bool? isSelected,
    bool? isMatched,
    String? char,
    String? id,
    int? row,
    int? col,
  }) {
    return KanaLinkCell(
      id: id ?? this.id,
      char: char ?? this.char,
      row: row ?? this.row,
      col: col ?? this.col,
      isSelected: isSelected ?? this.isSelected,
      isMatched: isMatched ?? this.isMatched,
    );
  }


  factory KanaLinkCell.fromJson(Map<String, dynamic> json) => _$KanaLinkCellFromJson(json);
  Map<String, dynamic> toJson() => _$KanaLinkCellToJson(this);
}

@JsonSerializable()
class KanaLinkConfig {
  final int rows;
  final int cols;
  final String levelId;
  final KanaLinkMode mode;
  final int initialTime;

  KanaLinkConfig({
    this.rows = 10,
    this.cols = 7,
    this.levelId = "",
    this.mode = KanaLinkMode.timeAttack,
    this.initialTime = 60,
  });

  factory KanaLinkConfig.fromJson(Map<String, dynamic> json) => _$KanaLinkConfigFromJson(json);
  Map<String, dynamic> toJson() => _$KanaLinkConfigToJson(this);
}
