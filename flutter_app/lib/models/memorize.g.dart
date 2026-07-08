// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'memorize.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MemorizePlayable _$MemorizePlayableFromJson(Map<String, dynamic> json) =>
    MemorizePlayable(
      id: json['id'] as String,
      character: json['character'] as String,
    );

Map<String, dynamic> _$MemorizePlayableToJson(MemorizePlayable instance) =>
    <String, dynamic>{'id': instance.id, 'character': instance.character};

MemorizeCardState _$MemorizeCardStateFromJson(Map<String, dynamic> json) =>
    MemorizeCardState(
      id: (json['id'] as num).toInt(),
      item: MemorizePlayable.fromJson(json['item'] as Map<String, dynamic>),
      isFaceUp: json['isFaceUp'] as bool? ?? false,
      isMatched: json['isMatched'] as bool? ?? false,
    );

Map<String, dynamic> _$MemorizeCardStateToJson(MemorizeCardState instance) =>
    <String, dynamic>{
      'id': instance.id,
      'item': instance.item,
      'isFaceUp': instance.isFaceUp,
      'isMatched': instance.isMatched,
    };

MemorizeGridSize _$MemorizeGridSizeFromJson(Map<String, dynamic> json) =>
    MemorizeGridSize(
      rows: (json['rows'] as num).toInt(),
      cols: (json['cols'] as num).toInt(),
    );

Map<String, dynamic> _$MemorizeGridSizeToJson(MemorizeGridSize instance) =>
    <String, dynamic>{'rows': instance.rows, 'cols': instance.cols};

MemorizeGameResult _$MemorizeGameResultFromJson(Map<String, dynamic> json) =>
    MemorizeGameResult(
      moves: (json['moves'] as num).toInt(),
      totalPairs: (json['totalPairs'] as num).toInt(),
      gridSizeLabel: json['gridSizeLabel'] as String,
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$MemorizeGameResultToJson(MemorizeGameResult instance) =>
    <String, dynamic>{
      'moves': instance.moves,
      'totalPairs': instance.totalPairs,
      'gridSizeLabel': instance.gridSizeLabel,
      'timeSeconds': instance.timeSeconds,
      'timestamp': instance.timestamp,
    };
