// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'grammar.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GrammarRule _$GrammarRuleFromJson(Map<String, dynamic> json) => GrammarRule(
  id: json['id'] as String,
  description: json['description'] as String,
  level: json['level'] as String,
  dependencies: (json['dependencies'] as List<dynamic>)
      .map((e) => e as String)
      .toList(),
  category: json['category'] as String?,
  tags:
      (json['tags'] as List<dynamic>?)?.map((e) => e as String).toList() ??
      const [],
);

Map<String, dynamic> _$GrammarRuleToJson(GrammarRule instance) =>
    <String, dynamic>{
      'id': instance.id,
      'description': instance.description,
      'level': instance.level,
      'dependencies': instance.dependencies,
      'category': instance.category,
      'tags': instance.tags,
    };

GrammarMetadata _$GrammarMetadataFromJson(Map<String, dynamic> json) =>
    GrammarMetadata(
      levels: (json['levels'] as List<dynamic>)
          .map((e) => e as String)
          .toList(),
      categories: (json['categories'] as List<dynamic>)
          .map((e) => e as String)
          .toList(),
    );

Map<String, dynamic> _$GrammarMetadataToJson(GrammarMetadata instance) =>
    <String, dynamic>{
      'levels': instance.levels,
      'categories': instance.categories,
    };

GrammarDefinition _$GrammarDefinitionFromJson(Map<String, dynamic> json) =>
    GrammarDefinition(
      version: json['version'] as String,
      metadata: GrammarMetadata.fromJson(
        json['metadata'] as Map<String, dynamic>,
      ),
      dependenciesBasics: (json['dependencies_basics'] as List<dynamic>)
          .map((e) => GrammarRule.fromJson(e as Map<String, dynamic>))
          .toList(),
      conjugaison: (json['conjugaison'] as List<dynamic>)
          .map((e) => GrammarRule.fromJson(e as Map<String, dynamic>))
          .toList(),
      rules: (json['rules'] as List<dynamic>)
          .map((e) => GrammarRule.fromJson(e as Map<String, dynamic>))
          .toList(),
    );

Map<String, dynamic> _$GrammarDefinitionToJson(GrammarDefinition instance) =>
    <String, dynamic>{
      'version': instance.version,
      'metadata': instance.metadata,
      'dependencies_basics': instance.dependenciesBasics,
      'conjugaison': instance.conjugaison,
      'rules': instance.rules,
    };
