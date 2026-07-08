// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'simon.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SimonPlayable _$SimonPlayableFromJson(Map<String, dynamic> json) =>
    SimonPlayable(
      id: json['id'] as String,
      character: json['character'] as String,
      meanings: (json['meanings'] as List<dynamic>)
          .map((e) => e as String)
          .toList(),
      readings: (json['readings'] as List<dynamic>)
          .map((e) => e as String)
          .toList(),
      type: $enumDecode(_$PlayableTypeEnumMap, json['type']),
    );

Map<String, dynamic> _$SimonPlayableToJson(SimonPlayable instance) =>
    <String, dynamic>{
      'id': instance.id,
      'character': instance.character,
      'meanings': instance.meanings,
      'readings': instance.readings,
      'type': _$PlayableTypeEnumMap[instance.type]!,
    };

const _$PlayableTypeEnumMap = {
  PlayableType.kanji: 'kanji',
  PlayableType.hiragana: 'hiragana',
  PlayableType.katakana: 'katakana',
};

SimonGameResult _$SimonGameResultFromJson(Map<String, dynamic> json) =>
    SimonGameResult(
      levelId: json['levelId'] as String,
      mode: $enumDecode(_$SimonModeEnumMap, json['mode']),
      maxSequence: (json['maxSequence'] as num).toInt(),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$SimonGameResultToJson(SimonGameResult instance) =>
    <String, dynamic>{
      'levelId': instance.levelId,
      'mode': _$SimonModeEnumMap[instance.mode]!,
      'maxSequence': instance.maxSequence,
      'timeSeconds': instance.timeSeconds,
      'timestamp': instance.timestamp,
    };

const _$SimonModeEnumMap = {
  SimonMode.kanji: 'kanji',
  SimonMode.meaning: 'meaning',
  SimonMode.readingCommon: 'readingCommon',
  SimonMode.readingRandom: 'readingRandom',
  SimonMode.kanaSame: 'kanaSame',
  SimonMode.kanaCross: 'kanaCross',
};
