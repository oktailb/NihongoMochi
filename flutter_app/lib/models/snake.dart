import 'package:json_annotation/json_annotation.dart';

part 'snake.g.dart';

enum SnakeMode { hiragana, katakana, numbers, words }

@JsonSerializable()
class SnakePoint {
  final int x;
  final int y;

  SnakePoint({required this.x, required this.y});

  factory SnakePoint.fromJson(Map<String, dynamic> json) => _$SnakePointFromJson(json);
  Map<String, dynamic> toJson() => _$SnakePointToJson(this);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SnakePoint && runtimeType == other.runtimeType && x == other.x && y == other.y;

  @override
  int get hashCode => x.hashCode ^ y.hashCode;
}

enum SnakeDirection { up, down, left, right }

@JsonSerializable()
class SnakeItem {
  final String character;
  final SnakePoint position;
  final bool isTarget;

  SnakeItem({
    required this.character,
    required this.position,
    required this.isTarget,
  });

  factory SnakeItem.fromJson(Map<String, dynamic> json) => _$SnakeItemFromJson(json);
  Map<String, dynamic> toJson() => _$SnakeItemToJson(this);
}

@JsonSerializable()
class SnakeGameState {
  final List<SnakePoint> snake;
  final SnakeDirection direction;
  final SnakeItem? targetItem;
  final List<SnakeItem> distractions;
  final int score;
  final int wordsCompleted;
  final bool isGameOver;
  final bool isPaused;
  final int timeSeconds;
  final String currentTargetLabel;
  final int gridWidth;
  final int gridHeight;
  final SnakeMode mode;
  final int sequenceIndex;
  final int currentNumber;
  final int tickDelay;

  SnakeGameState({
    this.snake = const [SnakePoint(x: 10, y: 10), SnakePoint(x: 10, y: 11), SnakePoint(x: 10, y: 12)],
    this.direction = SnakeDirection.up,
    this.targetItem,
    this.distractions = const [],
    this.score = 0,
    this.wordsCompleted = 0,
    this.isGameOver = false,
    this.isPaused = false,
    this.timeSeconds = 0,
    this.currentTargetLabel = "",
    this.gridWidth = 20,
    this.gridHeight = 30,
    this.mode = SnakeMode.hiragana,
    this.sequenceIndex = 0,
    this.currentNumber = 1,
    this.tickDelay = 220,
  });

  SnakeGameState copyWith({
    List<SnakePoint>? snake,
    SnakeDirection? direction,
    SnakeItem? targetItem,
    List<SnakeItem>? distractions,
    int? score,
    int? wordsCompleted,
    bool? isGameOver,
    bool? isPaused,
    int? timeSeconds,
    String? currentTargetLabel,
    int? gridWidth,
    int? gridHeight,
    SnakeMode? mode,
    int? sequenceIndex,
    int? currentNumber,
    int? tickDelay,
  }) {
    return SnakeGameState(
      snake: snake ?? this.snake,
      direction: direction ?? this.direction,
      targetItem: targetItem ?? this.targetItem,
      distractions: distractions ?? this.distractions,
      score: score ?? this.score,
      wordsCompleted: wordsCompleted ?? this.wordsCompleted,
      isGameOver: isGameOver ?? this.isGameOver,
      isPaused: isPaused ?? this.isPaused,
      timeSeconds: timeSeconds ?? this.timeSeconds,
      currentTargetLabel: currentTargetLabel ?? this.currentTargetLabel,
      gridWidth: gridWidth ?? this.gridWidth,
      gridHeight: gridHeight ?? this.gridHeight,
      mode: mode ?? this.mode,
      sequenceIndex: sequenceIndex ?? this.sequenceIndex,
      currentNumber: currentNumber ?? this.currentNumber,
      tickDelay: tickDelay ?? this.tickDelay,
    );
  }

  factory SnakeGameState.fromJson(Map<String, dynamic> json) => _$SnakeGameStateFromJson(json);
  Map<String, dynamic> toJson() => _$SnakeGameStateToJson(this);
}

@JsonSerializable()
class SnakeGameResult {
  final SnakeMode mode;
  final int score;
  final int wordsCompleted;
  final int timeSeconds;
  final int timestamp;

  SnakeGameResult({
    required this.mode,
    required this.score,
    required this.wordsCompleted,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory SnakeGameResult.fromJson(Map<String, dynamic> json) => _$SnakeGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$SnakeGameResultToJson(this);
}
