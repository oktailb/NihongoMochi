// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dictionary.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KanjiDetail _$KanjiDetailFromJson(Map<String, dynamic> json) => KanjiDetail(
  kanji: json['kanji'] as String,
  meanings: (json['meanings'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  onyomi:
      (json['onyomi'] as List<dynamic>?)?.map((e) => e as String).toList() ??
      const [],
  kunyomi:
      (json['kunyomi'] as List<dynamic>?)?.map((e) => e as String).toList() ??
      const [],
  strokes: (json['strokes'] as num?)?.toInt(),
  jlpt: (json['jlpt'] as num?)?.toInt(),
);

Map<String, dynamic> _$KanjiDetailToJson(KanjiDetail instance) =>
    <String, dynamic>{
      'kanji': instance.kanji,
      'meanings': instance.meanings,
      'onyomi': instance.onyomi,
      'kunyomi': instance.kunyomi,
      'strokes': instance.strokes,
      'jlpt': instance.jlpt,
    };

WordDetail _$WordDetailFromJson(Map<String, dynamic> json) => WordDetail(
  word: json['word'] as String,
  reading: json['reading'] as String,
  meanings: (json['meanings'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  jlpt: json['jlpt'] as String?,
);

Map<String, dynamic> _$WordDetailToJson(WordDetail instance) =>
    <String, dynamic>{
      'word': instance.word,
      'reading': instance.reading,
      'meanings': instance.meanings,
      'jlpt': instance.jlpt,
    };
