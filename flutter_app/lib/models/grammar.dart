import 'package:json_annotation/json_annotation.dart';

part 'grammar.g.dart';

@JsonSerializable()
class GrammarRule {
  final String id;
  final String description;
  final String level;
  final List<String> dependencies;
  final String? category;
  final List<String> tags;

  GrammarRule({
    required this.id,
    required this.description,
    required this.level,
    required this.dependencies,
    this.category,
    this.tags = const [],
  });

  factory GrammarRule.fromJson(Map<String, dynamic> json) => _$GrammarRuleFromJson(json);
  Map<String, dynamic> toJson() => _$GrammarRuleToJson(this);
}

@JsonSerializable()
class GrammarMetadata {
  final List<String> levels;
  final List<String> categories;

  GrammarMetadata({required this.levels, required this.categories});

  factory GrammarMetadata.fromJson(Map<String, dynamic> json) => _$GrammarMetadataFromJson(json);
  Map<String, dynamic> toJson() => _$GrammarMetadataToJson(this);
}

@JsonSerializable()
class GrammarDefinition {
  final String version;
  final GrammarMetadata metadata;
  @JsonKey(name: 'dependencies_basics')
  final List<GrammarRule> dependenciesBasics;
  final List<GrammarRule> conjugaison;
  final List<GrammarRule> rules;

  GrammarDefinition({
    required this.version,
    required this.metadata,
    required this.dependenciesBasics,
    required this.conjugaison,
    required this.rules,
  });

  factory GrammarDefinition.fromJson(Map<String, dynamic> json) => _$GrammarDefinitionFromJson(json);
  Map<String, dynamic> toJson() => _$GrammarDefinitionToJson(this);
}
