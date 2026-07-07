// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kana.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KanaEntry _$KanaEntryFromJson(Map<String, dynamic> json) => KanaEntry(
  character: json['character'] as String,
  romaji: json['romaji'] as String,
  type: $enumDecode(_$KanaTypeEnumMap, json['type']),
  line: (json['line'] as num?)?.toInt() ?? 0,
  column: (json['column'] as num?)?.toInt() ?? 0,
);

Map<String, dynamic> _$KanaEntryToJson(KanaEntry instance) => <String, dynamic>{
  'character': instance.character,
  'romaji': instance.romaji,
  'type': _$KanaTypeEnumMap[instance.type]!,
  'line': instance.line,
  'column': instance.column,
};

const _$KanaTypeEnumMap = {
  KanaType.hiragana: 'HIRAGANA',
  KanaType.katakana: 'KATAKANA',
};

KanaData _$KanaDataFromJson(Map<String, dynamic> json) => KanaData(
  characters: (json['characters'] as List<dynamic>)
      .map((e) => KanaEntry.fromJson(e as Map<String, dynamic>))
      .toList(),
);

Map<String, dynamic> _$KanaDataToJson(KanaData instance) => <String, dynamic>{
  'characters': instance.characters,
};

NumberEntry _$NumberEntryFromJson(Map<String, dynamic> json) => NumberEntry(
  character: json['character'] as String,
  romaji: json['romaji'] as String,
  value: (json['value'] as num).toInt(),
);

Map<String, dynamic> _$NumberEntryToJson(NumberEntry instance) =>
    <String, dynamic>{
      'character': instance.character,
      'romaji': instance.romaji,
      'value': instance.value,
    };

NumberData _$NumberDataFromJson(Map<String, dynamic> json) => NumberData(
  numbers: (json['numbers'] as List<dynamic>)
      .map((e) => NumberEntry.fromJson(e as Map<String, dynamic>))
      .toList(),
);

Map<String, dynamic> _$NumberDataToJson(NumberData instance) =>
    <String, dynamic>{'numbers': instance.numbers};
