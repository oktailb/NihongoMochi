import 'package:json_annotation/json_annotation.dart';

part 'kana.g.dart';

enum KanaType {
  @JsonValue('HIRAGANA')
  hiragana,
  @JsonValue('KATAKANA')
  katakana,
}

@JsonSerializable()
class KanaEntry {
  final String character;
  final String romaji;
  final KanaType type;
  final int line;
  final int column;

  KanaEntry({
    required this.character,
    required this.romaji,
    required this.type,
    this.line = 0,
    this.column = 0,
  });

  String get category {
    if (line <= 11) return "gojuon";
    if (line <= 16) {
      return romaji.startsWith("p") ? "handakuon" : "dakuon";
    }
    return "yoon";
  }

  factory KanaEntry.fromJson(Map<String, dynamic> json) => _$KanaEntryFromJson(json);
  Map<String, dynamic> toJson() => _$KanaEntryToJson(this);
}

@JsonSerializable()
class KanaData {
  final List<KanaEntry> characters;

  KanaData({required this.characters});

  factory KanaData.fromJson(Map<String, dynamic> json) => _$KanaDataFromJson(json);
  Map<String, dynamic> toJson() => _$KanaDataToJson(this);
}

@JsonSerializable()
class NumberEntry {
  final String character;
  final String romaji;
  final int value;

  NumberEntry({
    required this.character,
    required this.romaji,
    required this.value,
  });

  factory NumberEntry.fromJson(Map<String, dynamic> json) => _$NumberEntryFromJson(json);
  Map<String, dynamic> toJson() => _$NumberEntryToJson(this);
}

@JsonSerializable()
class NumberData {
  final List<NumberEntry> numbers;

  NumberData({required this.numbers});

  factory NumberData.fromJson(Map<String, dynamic> json) => _$NumberDataFromJson(json);
  Map<String, dynamic> toJson() => _$NumberDataToJson(this);
}
