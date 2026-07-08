import 'package:json_annotation/json_annotation.dart';

part 'taquin.g.dart';

enum TaquinMode { hiragana, katakana, numbers }

@JsonSerializable()
class TaquinPiece {
  final String character;
  final int targetLine;
  final int targetColumn;
  final bool isBlank;

  TaquinPiece({
    required this.character,
    required this.targetLine,
    required this.targetColumn,
    this.isBlank = false,
  });

  factory TaquinPiece.fromJson(Map<String, dynamic> json) => _$TaquinPieceFromJson(json);
  Map<String, dynamic> toJson() => _$TaquinPieceToJson(this);
}

@JsonSerializable()
class TaquinGameState {
  final List<TaquinPiece> pieces;
  final int rows;
  final int cols;
  final int moves;
  final int timeSeconds;
  final bool isSolved;
  final TaquinMode mode;

  TaquinGameState({
    required this.pieces,
    required this.rows,
    required this.cols,
    this.moves = 0,
    this.timeSeconds = 0,
    this.isSolved = false,
    this.mode = TaquinMode.hiragana,
  });

  TaquinGameState copyWith({
    List<TaquinPiece>? pieces,
    int? moves,
    int? timeSeconds,
    bool? isSolved,
  }) {
    return TaquinGameState(
      pieces: pieces ?? this.pieces,
      rows: rows,
      cols: cols,
      moves: moves ?? this.moves,
      timeSeconds: timeSeconds ?? this.timeSeconds,
      isSolved: isSolved ?? this.isSolved,
      mode: mode,
    );
  }

  factory TaquinGameState.fromJson(Map<String, dynamic> json) => _$TaquinGameStateFromJson(json);
  Map<String, dynamic> toJson() => _$TaquinGameStateToJson(this);
}

@JsonSerializable()
class TaquinGameResult {
  final TaquinMode mode;
  final int rows;
  final int moves;
  final int timeSeconds;
  final int timestamp;

  TaquinGameResult({
    required this.mode,
    required this.rows,
    required this.moves,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory TaquinGameResult.fromJson(Map<String, dynamic> json) => _$TaquinGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$TaquinGameResultToJson(this);
}
