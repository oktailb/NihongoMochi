import 'package:json_annotation/json_annotation.dart';

part 'shiritori.g.dart';

@JsonSerializable()
class ShiritoriWord {
  final String word;
  final String phonetics;
  final String meaning;
  final bool isPlayer;
  final int timestamp;

  ShiritoriWord({
    required this.word,
    required this.phonetics,
    required this.meaning,
    required this.isPlayer,
    required this.timestamp,
  });

  factory ShiritoriWord.fromJson(Map<String, dynamic> json) => _$ShiritoriWordFromJson(json);
  Map<String, dynamic> toJson() => _$ShiritoriWordToJson(this);
}

enum ShiritoriGameState {
  idle,
  loading,
  playerTurn,
  aiTurn,
  gameOver,
  paused
}

@JsonSerializable()
class ShiritoriGameResult {
  final String levelId;
  final int score;
  final int timeSeconds;
  final int timestamp;

  ShiritoriGameResult({
    required this.levelId,
    required this.score,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory ShiritoriGameResult.fromJson(Map<String, dynamic> json) => _$ShiritoriGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$ShiritoriGameResultToJson(this);
}
