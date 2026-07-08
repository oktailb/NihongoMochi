// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'taquin.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TaquinPiece _$TaquinPieceFromJson(Map<String, dynamic> json) => TaquinPiece(
  character: json['character'] as String,
  targetLine: (json['targetLine'] as num).toInt(),
  targetColumn: (json['targetColumn'] as num).toInt(),
  isBlank: json['isBlank'] as bool? ?? false,
);

Map<String, dynamic> _$TaquinPieceToJson(TaquinPiece instance) =>
    <String, dynamic>{
      'character': instance.character,
      'targetLine': instance.targetLine,
      'targetColumn': instance.targetColumn,
      'isBlank': instance.isBlank,
    };

TaquinGameState _$TaquinGameStateFromJson(Map<String, dynamic> json) =>
    TaquinGameState(
      pieces: (json['pieces'] as List<dynamic>)
          .map((e) => TaquinPiece.fromJson(e as Map<String, dynamic>))
          .toList(),
      rows: (json['rows'] as num).toInt(),
      cols: (json['cols'] as num).toInt(),
      moves: (json['moves'] as num?)?.toInt() ?? 0,
      timeSeconds: (json['timeSeconds'] as num?)?.toInt() ?? 0,
      isSolved: json['isSolved'] as bool? ?? false,
      mode:
          $enumDecodeNullable(_$TaquinModeEnumMap, json['mode']) ??
          TaquinMode.hiragana,
    );

Map<String, dynamic> _$TaquinGameStateToJson(TaquinGameState instance) =>
    <String, dynamic>{
      'pieces': instance.pieces,
      'rows': instance.rows,
      'cols': instance.cols,
      'moves': instance.moves,
      'timeSeconds': instance.timeSeconds,
      'isSolved': instance.isSolved,
      'mode': _$TaquinModeEnumMap[instance.mode]!,
    };

const _$TaquinModeEnumMap = {
  TaquinMode.hiragana: 'hiragana',
  TaquinMode.katakana: 'katakana',
  TaquinMode.numbers: 'numbers',
};

TaquinGameResult _$TaquinGameResultFromJson(Map<String, dynamic> json) =>
    TaquinGameResult(
      mode: $enumDecode(_$TaquinModeEnumMap, json['mode']),
      rows: (json['rows'] as num).toInt(),
      moves: (json['moves'] as num).toInt(),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$TaquinGameResultToJson(TaquinGameResult instance) =>
    <String, dynamic>{
      'mode': _$TaquinModeEnumMap[instance.mode]!,
      'rows': instance.rows,
      'moves': instance.moves,
      'timeSeconds': instance.timeSeconds,
      'timestamp': instance.timestamp,
    };
