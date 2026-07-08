// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'kana_link.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

KanaLinkResult _$KanaLinkResultFromJson(Map<String, dynamic> json) =>
    KanaLinkResult(
      score: (json['score'] as num).toInt(),
      wordsFound: (json['wordsFound'] as num).toInt(),
      timeSeconds: (json['timeSeconds'] as num).toInt(),
      levelId: json['levelId'] as String,
      timestamp: (json['timestamp'] as num).toInt(),
    );

Map<String, dynamic> _$KanaLinkResultToJson(KanaLinkResult instance) =>
    <String, dynamic>{
      'score': instance.score,
      'wordsFound': instance.wordsFound,
      'timeSeconds': instance.timeSeconds,
      'levelId': instance.levelId,
      'timestamp': instance.timestamp,
    };

KanaLinkCell _$KanaLinkCellFromJson(Map<String, dynamic> json) => KanaLinkCell(
  id: json['id'] as String,
  char: json['char'] as String,
  row: (json['row'] as num).toInt(),
  col: (json['col'] as num).toInt(),
  isSelected: json['isSelected'] as bool? ?? false,
  isMatched: json['isMatched'] as bool? ?? false,
);

Map<String, dynamic> _$KanaLinkCellToJson(KanaLinkCell instance) =>
    <String, dynamic>{
      'id': instance.id,
      'char': instance.char,
      'row': instance.row,
      'col': instance.col,
      'isSelected': instance.isSelected,
      'isMatched': instance.isMatched,
    };

KanaLinkConfig _$KanaLinkConfigFromJson(Map<String, dynamic> json) =>
    KanaLinkConfig(
      rows: (json['rows'] as num?)?.toInt() ?? 10,
      cols: (json['cols'] as num?)?.toInt() ?? 7,
      levelId: json['levelId'] as String? ?? "",
      mode:
          $enumDecodeNullable(_$KanaLinkModeEnumMap, json['mode']) ??
          KanaLinkMode.timeAttack,
      initialTime: (json['initialTime'] as num?)?.toInt() ?? 60,
    );

Map<String, dynamic> _$KanaLinkConfigToJson(KanaLinkConfig instance) =>
    <String, dynamic>{
      'rows': instance.rows,
      'cols': instance.cols,
      'levelId': instance.levelId,
      'mode': _$KanaLinkModeEnumMap[instance.mode]!,
      'initialTime': instance.initialTime,
    };

const _$KanaLinkModeEnumMap = {
  KanaLinkMode.timeAttack: 'timeAttack',
  KanaLinkMode.survival: 'survival',
};
