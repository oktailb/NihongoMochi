// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'snake.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SnakePoint _$SnakePointFromJson(Map<String, dynamic> json) =>
    SnakePoint(x: (json['x'] as num).toInt(), y: (json['y'] as num).toInt());

Map<String, dynamic> _$SnakePointToJson(SnakePoint instance) =>
    <String, dynamic>{'x': instance.x, 'y': instance.y};

SnakeItem _$SnakeItemFromJson(Map<String, dynamic> json) => SnakeItem(
  character: json['character'] as String,
  position: SnakePoint.fromJson(json['position'] as Map<String, dynamic>),
  isTarget: json['isTarget'] as bool,
);

Map<String, dynamic> _$SnakeItemToJson(SnakeItem instance) => <String, dynamic>{
  'character': instance.character,
  'position': instance.position,
  'isTarget': instance.isTarget,
};

SnakeGameState _$SnakeGameStateFromJson(Map<String, dynamic> json) =>
    SnakeGameState(
      snake:
          (json['snake'] as List<dynamic>?)
              ?.map((e) => SnakePoint.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [
            SnakePoint(x: 10, y: 10),
            SnakePoint(x: 10, y: 11),
            SnakePoint(x: 10, y: 12),
          ],
      direction:
          $enumDecodeNullable(_$SnakeDirectionEnumMap, json['direction']) ??
          SnakeDirection.up,
      targetItem: json['targetItem'] == null
          ? null
          : SnakeItem.fromJson(json['targetItem'] as Map<String, dynamic>),
      distractions:
          (json['distractions'] as List<dynamic>?)
              ?.map((e) => SnakeItem.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
      score: (json['score'] as num?)?.toInt() ?? 0,
      wordsCompleted: (json['wordsCompleted'] as num?)?.toInt() ?? 0,
      isGameOver: json['isGameOver'] as bool? ?? false,
      isPaused: json['isPaused'] as bool? ?? false,
      timeSeconds: (json['timeSeconds'] as num?)?.toInt() ?? 0,
      currentTargetLabel: json['currentTargetLabel'] as String? ?? "",
      gridWidth: (json['gridWidth'] as num?)?.toInt() ?? 20,
      gridHeight: (json['gridHeight'] as num?)?.toInt() ?? 30,
      mode:
          $enumDecodeNullable(_$SnakeModeEnumMap, json['mode']) ??
          SnakeMode.hiragana,
      sequenceIndex: (json['sequenceIndex'] as num?)?.toInt() ?? 0,
      currentNumber: (json['currentNumber'] as num?)?.toInt() ?? 1,
      tickDelay: (json['tickDelay'] as num?)?.toInt() ?? 220,
    );

Map<String, dynamic> _$SnakeGameStateToJson(SnakeGameState instance) =>
    <String, dynamic>{
      'snake': instance.snake,
      'direction': _$SnakeDirectionEnumMap[instance.direction]!,
      'targetItem': instance.targetItem,
      'distractions': instance.distractions,
      'score': instance.score,
      'wordsCompleted': instance.wordsCompleted,
      'isGameOver': instance.isGameOver,
      'isPaused': instance.isPaused,
      'timeSeconds': instance.timeSeconds,
      'currentTargetLabel': instance.currentTargetLabel,
      'gridWidth': instance.gridWidth,
      'gridHeight': instance.gridHeight,
      'mode': _$SnakeModeEnumMap[instance.mode]!,
      'sequenceIndex': instance.sequenceIndex,
      'currentNumber': instance.currentNumber,
      'tickDelay': instance.tickDelay,
    };

const _$SnakeDirectionEnumMap = {
  SnakeDirection.up: 'up',
  SnakeDirection.down: 'down',
  SnakeDirection.left: 'left',
  SnakeDirection.right: 'right',
};

const _$SnakeModeEnumMap = {
  SnakeMode.hiragana: 'hiragana',
  SnakeMode.katakana: 'katakana',
  SnakeMode.numbers: 'numbers',
  SnakeMode.words: 'words',
};

SnakeGameResult _$SnakeGameResultFromJson(Map<String, dynamic> json) =>
    SnakeGameResult(
      mode: $enumDecode(_$SnakeModeEnumMap, json['mode']),
      score: (json['score'] as num).toInt(),
      wordsCompleted: (json['wordsCompleted'] as num).toInt(),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$SnakeGameResultToJson(SnakeGameResult instance) =>
    <String, dynamic>{
      'mode': _$SnakeModeEnumMap[instance.mode]!,
      'score': instance.score,
      'wordsCompleted': instance.wordsCompleted,
      'timeSeconds': instance.timeSeconds,
      'timestamp': instance.timestamp,
    };
