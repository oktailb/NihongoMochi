import 'package:json_annotation/json_annotation.dart';

part 'dictionary.g.dart';

@JsonSerializable()
class ReadingInfo {
  final String text;
  final String type; // 'on' or 'kun'

  ReadingInfo({required this.text, required this.type});

  factory ReadingInfo.fromJson(Map<String, dynamic> json) => _$ReadingInfoFromJson(json);
  Map<String, dynamic> toJson() => _$ReadingInfoToJson(this);
}

@JsonSerializable()
class ComponentEntry {
  final String? kanjiRef;
  final String? text;

  ComponentEntry({this.kanjiRef, this.text});

  factory ComponentEntry.fromJson(Map<String, dynamic> json) => _$ComponentEntryFromJson(json);
  Map<String, dynamic> toJson() => _$ComponentEntryToJson(this);
}

@JsonSerializable()
class DictionaryItem {
  final String id;
  final String character;
  final List<ReadingInfo> readings;
  final int strokeCount;
  final List<String> meanings;
  final List<String> categories;
  final List<String> levelIds;
  final List<String> displayLabelKeys;
  final String? structure; // Structure du Kanji (ex: ⿰, ⿳)
  final List<ComponentEntry> components;
  final int? frequency;

  DictionaryItem({
    required this.id,
    required this.character,
    required this.readings,
    required this.strokeCount,
    required this.meanings,
    this.categories = const [],
    this.levelIds = const [],
    this.displayLabelKeys = const [],
    this.structure,
    this.components = const [],
    this.frequency,
  });

  factory DictionaryItem.fromJson(Map<String, dynamic> json) => _$DictionaryItemFromJson(json);
  Map<String, dynamic> toJson() => _$DictionaryItemToJson(this);
}

// ... KanjiDetail et WordDetail pour compatibilité repository ...
@JsonSerializable()
class KanjiDetail {
  final String kanji;
  final List<String> meanings;
  final List<String> onyomi;
  final List<String> kunyomi;
  final int? strokes;
  final int? jlpt;

  KanjiDetail({
    required this.kanji,
    required this.meanings,
    this.onyomi = const [],
    this.kunyomi = const [],
    this.strokes,
    this.jlpt,
  });

  factory KanjiDetail.fromJson(Map<String, dynamic> json) => _$KanjiDetailFromJson(json);
}

@JsonSerializable()
class WordDetail {
  final String word;
  final String reading;
  final List<String> meanings;
  final String? jlpt;

  WordDetail({
    required this.word,
    required this.reading,
    required this.meanings,
    this.jlpt,
  });

  factory WordDetail.fromJson(Map<String, dynamic> json) => _$WordDetailFromJson(json);
}
