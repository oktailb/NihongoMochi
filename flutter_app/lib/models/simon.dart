import 'package:json_annotation/json_annotation.dart';

part 'simon.g.dart';

enum SimonGameState {
  idle,
  showingSequence,
  awaitingInput,
  gameOver,
  paused
}

enum SimonMode {
  kanji,
  meaning,
  readingCommon,
  readingRandom,
  kanaSame,
  kanaCross
}

enum PlayableType { kanji, hiragana, katakana }

@JsonSerializable()
class SimonPlayable {
  final String id;
  final String character;
  final List<String> meanings;
  final List<String> readings;
  final PlayableType type;

  SimonPlayable({
    required this.id,
    required this.character,
    required this.meanings,
    required this.readings,
    required this.type,
  });

  factory SimonPlayable.fromJson(Map<String, dynamic> json) => _$SimonPlayableFromJson(json);
  Map<String, dynamic> toJson() => _$SimonPlayableToJson(this);
}

@JsonSerializable()
class SimonGameResult {
  final String levelId;
  final SimonMode mode;
  final int maxSequence;
  final int timeSeconds;
  final int timestamp;

  SimonGameResult({
    required this.levelId,
    required this.mode,
    required this.maxSequence,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory SimonGameResult.fromJson(Map<String, dynamic> json) => _$SimonGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$SimonGameResultToJson(this);
}
