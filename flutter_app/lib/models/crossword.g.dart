// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'crossword.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CrosswordCell _$CrosswordCellFromJson(Map<String, dynamic> json) =>
    CrosswordCell(
      r: (json['r'] as num).toInt(),
      c: (json['c'] as num).toInt(),
      solution: json['solution'] as String? ?? "",
      userInput: json['userInput'] as String? ?? "",
      isBlack: json['isBlack'] as bool? ?? true,
      number: (json['number'] as num?)?.toInt(),
      isCorrect: json['isCorrect'] as bool? ?? false,
    );

Map<String, dynamic> _$CrosswordCellToJson(CrosswordCell instance) =>
    <String, dynamic>{
      'r': instance.r,
      'c': instance.c,
      'solution': instance.solution,
      'userInput': instance.userInput,
      'isBlack': instance.isBlack,
      'number': instance.number,
      'isCorrect': instance.isCorrect,
    };

CrosswordWord _$CrosswordWordFromJson(Map<String, dynamic> json) =>
    CrosswordWord(
      number: (json['number'] as num).toInt(),
      word: json['word'] as String,
      kanji: json['kanji'] as String,
      meaning: json['meaning'] as String,
      phonetics: json['phonetics'] as String? ?? "",
      row: (json['row'] as num).toInt(),
      col: (json['col'] as num).toInt(),
      isHorizontal: json['isHorizontal'] as bool,
      isSolved: json['isSolved'] as bool? ?? false,
    );

Map<String, dynamic> _$CrosswordWordToJson(CrosswordWord instance) =>
    <String, dynamic>{
      'number': instance.number,
      'word': instance.word,
      'kanji': instance.kanji,
      'meaning': instance.meaning,
      'phonetics': instance.phonetics,
      'row': instance.row,
      'col': instance.col,
      'isHorizontal': instance.isHorizontal,
      'isSolved': instance.isSolved,
    };

CrosswordGameResult _$CrosswordGameResultFromJson(Map<String, dynamic> json) =>
    CrosswordGameResult(
      wordCount: (json['wordCount'] as num).toInt(),
      mode: $enumDecode(_$CrosswordModeEnumMap, json['mode']),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      completionPercentage: (json['completionPercentage'] as num).toInt(),
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$CrosswordGameResultToJson(
  CrosswordGameResult instance,
) => <String, dynamic>{
  'wordCount': instance.wordCount,
  'mode': _$CrosswordModeEnumMap[instance.mode]!,
  'timeSeconds': instance.timeSeconds,
  'completionPercentage': instance.completionPercentage,
  'timestamp': instance.timestamp,
};

const _$CrosswordModeEnumMap = {
  CrosswordMode.kanas: 'KANAS',
  CrosswordMode.kanjis: 'KANJIS',
};
