// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'level.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ActivityConfig _$ActivityConfigFromJson(Map<String, dynamic> json) =>
    ActivityConfig(
      dataFile: json['dataFile'] as String,
      enabled: json['enabled'] as bool? ?? false,
      jlpt: json['jlpt'] as String?,
      minRank: (json['minRank'] as num?)?.toInt(),
      maxRank: (json['maxRank'] as num?)?.toInt(),
    );

Map<String, dynamic> _$ActivityConfigToJson(ActivityConfig instance) =>
    <String, dynamic>{
      'dataFile': instance.dataFile,
      'enabled': instance.enabled,
      'jlpt': instance.jlpt,
      'minRank': instance.minRank,
      'maxRank': instance.maxRank,
    };

LevelDefinition _$LevelDefinitionFromJson(Map<String, dynamic> json) =>
    LevelDefinition(
      id: json['id'] as String,
      name: json['name'] as String,
      description: json['description'] as String? ?? "",
      dependencies:
          (json['dependencies'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          [],
      activities:
          (json['activities'] as Map<String, dynamic>?)?.map(
            (k, e) =>
                MapEntry(k, ActivityConfig.fromJson(e as Map<String, dynamic>)),
          ) ??
          {},
    );

Map<String, dynamic> _$LevelDefinitionToJson(LevelDefinition instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'description': instance.description,
      'dependencies': instance.dependencies,
      'activities': instance.activities,
    };

SectionDefinition _$SectionDefinitionFromJson(Map<String, dynamic> json) =>
    SectionDefinition(
      name: json['name'] as String,
      description: json['description'] as String? ?? "",
      prerequisiteFor:
          (json['prerequisiteFor'] as List<dynamic>?)
              ?.map((e) => e as String)
              .toList() ??
          [],
      levels:
          (json['levels'] as List<dynamic>?)
              ?.map((e) => LevelDefinition.fromJson(e as Map<String, dynamic>))
              .toList() ??
          [],
    );

Map<String, dynamic> _$SectionDefinitionToJson(SectionDefinition instance) =>
    <String, dynamic>{
      'name': instance.name,
      'description': instance.description,
      'prerequisiteFor': instance.prerequisiteFor,
      'levels': instance.levels,
    };

LevelDefinitions _$LevelDefinitionsFromJson(Map<String, dynamic> json) =>
    LevelDefinitions(
      version: json['version'] as String,
      sections: (json['sections'] as Map<String, dynamic>).map(
        (k, e) =>
            MapEntry(k, SectionDefinition.fromJson(e as Map<String, dynamic>)),
      ),
      activityTypes: Map<String, String>.from(json['activityTypes'] as Map),
    );

Map<String, dynamic> _$LevelDefinitionsToJson(LevelDefinitions instance) =>
    <String, dynamic>{
      'version': instance.version,
      'sections': instance.sections,
      'activityTypes': instance.activityTypes,
    };
