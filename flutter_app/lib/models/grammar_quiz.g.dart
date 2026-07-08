// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grammar_quiz.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Exercise _$ExerciseFromJson(Map<String, dynamic> json) => Exercise(
  id: json['id'] as String,
  type: $enumDecode(_$ExerciseTypeEnumMap, json['type']),
  tags: (json['tags'] as List<dynamic>).map((e) => e as String).toList(),
  payload: json['payload'] as Map<String, dynamic>,
);

Map<String, dynamic> _$ExerciseToJson(Exercise instance) => <String, dynamic>{
  'id': instance.id,
  'type': _$ExerciseTypeEnumMap[instance.type]!,
  'tags': instance.tags,
  'payload': instance.payload,
};

const _$ExerciseTypeEnumMap = {
  ExerciseType.fillBlank: 'FILL_BLANK',
  ExerciseType.sentenceOrder: 'SENTENCE_ORDER',
  ExerciseType.underlineReading: 'UNDERLINE_READING',
  ExerciseType.underlineWriting: 'UNDERLINE_WRITING',
  ExerciseType.paraphrase: 'PARAPHRASE',
  ExerciseType.wordUsage: 'WORD_USAGE',
};
