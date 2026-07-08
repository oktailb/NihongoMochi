// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'shiritori.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShiritoriWord _$ShiritoriWordFromJson(Map<String, dynamic> json) =>
    ShiritoriWord(
      word: json['word'] as String,
      phonetics: json['phonetics'] as String,
      meaning: json['meaning'] as String,
      isPlayer: json['isPlayer'] as bool,
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$ShiritoriWordToJson(ShiritoriWord instance) =>
    <String, dynamic>{
      'word': instance.word,
      'phonetics': instance.phonetics,
      'meaning': instance.meaning,
      'isPlayer': instance.isPlayer,
      'timestamp': instance.timestamp,
    };

ShiritoriGameResult _$ShiritoriGameResultFromJson(Map<String, dynamic> json) =>
    ShiritoriGameResult(
      levelId: json['levelId'] as String,
      score: (json['score'] as num).toInt(),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$ShiritoriGameResultToJson(
  ShiritoriGameResult instance,
) => <String, dynamic>{
  'levelId': instance.levelId,
  'score': instance.score,
  'timeSeconds': instance.timeSeconds,
  'timestamp': instance.timestamp,
};
