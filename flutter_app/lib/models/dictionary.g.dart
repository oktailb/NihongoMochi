// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'dictionary.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ReadingInfo _$ReadingInfoFromJson(Map<String, dynamic> json) =>
    ReadingInfo(text: json['text'] as String, type: json['type'] as String);

Map<String, dynamic> _$ReadingInfoToJson(ReadingInfo instance) =>
    <String, dynamic>{'text': instance.text, 'type': instance.type};

ComponentEntry _$ComponentEntryFromJson(Map<String, dynamic> json) =>
    ComponentEntry(
      kanjiRef: json['kanjiRef'] as String?,
      text: json['text'] as String?,
    );

Map<String, dynamic> _$ComponentEntryToJson(ComponentEntry instance) =>
    <String, dynamic>{'kanjiRef': instance.kanjiRef, 'text': instance.text};

DictionaryItem _$DictionaryItemFromJson(Map<String, dynamic> json) =>
    DictionaryItem(
      id: json['id'] as String,
      character: json['character'] as String,
      readings: (json['readings'] as List<dynamic>)
          .map((e) => ReadingInfo.fromJson(e as Map<String, dynamic>))
          .toList(),
      strokeCount: (json['strokeCount'] as num).toInt(),
      meanings: (json['meanings'] as List<dynamic>)
          .map((e) => e as String)
          .toList(),
      categories:
          (json['categories'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      levelIds:
          (json['levelIds'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      displayLabelKeys:
          (json['displayLabelKeys'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          const [],
      structure: json['structure'] as String?,
      components:
          (json['components'] as List<dynamic>?)
              ?.map((e) => ComponentEntry.fromJson(e as Map<String, dynamic>))
              .toList() ??
          const [],
    );

Map<String, dynamic> _$DictionaryItemToJson(DictionaryItem instance) =>
    <String, dynamic>{
      'id': instance.id,
      'character': instance.character,
      'readings': instance.readings,
      'strokeCount': instance.strokeCount,
      'meanings': instance.meanings,
      'categories': instance.categories,
      'levelIds': instance.levelIds,
      'displayLabelKeys': instance.displayLabelKeys,
      'structure': instance.structure,
      'components': instance.components,
    };

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
