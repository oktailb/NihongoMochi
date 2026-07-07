import 'package:json_annotation/json_annotation.dart';

part 'level.g.dart';

@JsonSerializable()
class ActivityConfig {
  final String dataFile;
  final bool enabled;
  final String? jlpt;
  final int? minRank;
  final int? maxRank;

  ActivityConfig({
    required this.dataFile,
    this.enabled = false,
    this.jlpt,
    this.minRank,
    this.maxRank,
  });

  factory ActivityConfig.fromJson(Map<String, dynamic> json) => _$ActivityConfigFromJson(json);
  Map<String, dynamic> toJson() => _$ActivityConfigToJson(this);
}

@JsonSerializable()
class LevelDefinition {
  final String id;
  final String name;
  final String description;
  @JsonKey(defaultValue: [])
  final List<String> dependencies;
  @JsonKey(defaultValue: {})
  final Map<String, ActivityConfig> activities;

  LevelDefinition({
    required this.id,
    required this.name,
    this.description = "",
    this.dependencies = const [],
    this.activities = const {},
  });

  factory LevelDefinition.fromJson(Map<String, dynamic> json) => _$LevelDefinitionFromJson(json);
  Map<String, dynamic> toJson() => _$LevelDefinitionToJson(this);
}

@JsonSerializable()
class SectionDefinition {
  final String name;
  final String description;
  @JsonKey(defaultValue: [])
  final List<String> prerequisiteFor;
  @JsonKey(defaultValue: [])
  final List<LevelDefinition> levels;

  SectionDefinition({
    required this.name,
    this.description = "",
    this.prerequisiteFor = const [],
    this.levels = const [],
  });

  factory SectionDefinition.fromJson(Map<String, dynamic> json) => _$SectionDefinitionFromJson(json);
  Map<String, dynamic> toJson() => _$SectionDefinitionToJson(this);
}

@JsonSerializable()
class LevelDefinitions {
  final String version;
  final Map<String, SectionDefinition> sections;
  final Map<String, String> activityTypes;

  LevelDefinitions({
    required this.version,
    required this.sections,
    required this.activityTypes,
  });

  factory LevelDefinitions.fromJson(Map<String, dynamic> json) => _$LevelDefinitionsFromJson(json);
  Map<String, dynamic> toJson() => _$LevelDefinitionsToJson(this);
}
