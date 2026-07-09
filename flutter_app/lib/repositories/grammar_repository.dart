import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/grammar.dart';
import '../services/language_pack_manager.dart';

class GrammarRepository {
  final LanguagePackManager _lpManager;
  GrammarDefinition? _definition;
  List<GrammarRule>? _cachedAllRules;

  GrammarRepository(this._lpManager);

  Future<String> _loadResource(String path, String localPath) async {
    try {
      final downloaded = await _lpManager.loadLocalResource(localPath);
      if (downloaded != null) return downloaded;
    } catch (e) {
      print("LanguagePackManager error for $localPath: $e");
    }
    return await rootBundle.loadString(path);
  }

  Future<GrammarDefinition> loadGrammarDefinition() async {
    if (_definition != null) return _definition!;

    try {
      final String jsonString = await _loadResource(
        'assets/files/grammar/grammar.json',
        'grammar/grammar.json',
      );
      final Map<String, dynamic> jsonData = json.decode(jsonString);

      _definition = GrammarDefinition.fromJson(jsonData);
      _cachedAllRules = [
        ..._definition!.dependenciesBasics,
        ..._definition!.conjugaison,
        ..._definition!.rules,
      ];

      return _definition!;
    } catch (e) {
      print("Error loading grammar: $e");
      return GrammarDefinition(
        version: "0",
        metadata: GrammarMetadata(levels: [], categories: []),
        dependenciesBasics: [],
        conjugaison: [],
        rules: [],
      );
    }
  }

  Future<List<String>> getCategories() async {
    final def = await loadGrammarDefinition();
    return def.metadata.categories;
  }

  Future<List<GrammarRule>> getAllRules() async {
    if (_cachedAllRules == null) await loadGrammarDefinition();
    return _cachedAllRules ?? [];
  }

  Future<GrammarRule?> getRuleById(String id) async {
    final all = await getAllRules();
    try {
      return all.firstWhere((r) => r.id == id);
    } catch (e) {
      return null;
    }
  }

  Future<List<GrammarRule>> getRulesByBlock(String block, String maxLevelId) async {
    final def = await loadGrammarDefinition();
    List<GrammarRule> rulesInBlock;

    switch (block) {
      case 'dependencies_basics':
        rulesInBlock = def.dependenciesBasics;
        break;
      case 'conjugaison':
        rulesInBlock = def.conjugaison;
        break;
      case 'rules':
        rulesInBlock = def.rules;
        break;
      default:
        rulesInBlock = await getAllRules();
    }

    final levelsOrder = def.metadata.levels;
    final maxLevelIndex = levelsOrder.indexOf(maxLevelId.toLowerCase());

    if (maxLevelIndex == -1) return rulesInBlock;

    return rulesInBlock.where((rule) {
      final ruleLevelIndex = levelsOrder.indexOf(rule.level.toLowerCase());
      return ruleLevelIndex != -1 && ruleLevelIndex <= maxLevelIndex;
    }).toList();
  }

  Future<bool> hasLesson(String ruleId) async {
    final all = await getAllRules();
    return all.any((r) => r.id == ruleId);
  }

  Future<String> loadCss(bool isDark) async {
    final fileName = isDark ? "styles_dark.css" : "styles_light.css";
    try {
      return await _loadResource(
        'assets/files/grammar/lessons/$fileName',
        'grammar/lessons/$fileName',
      );
    } catch (e) {
      return isDark
          ? "body { color: #E0E0E0; background-color: #121212; }"
          : "body { color: #333; background-color: #FFFFFF; }";
    }
  }

  Future<String> loadLessonHtml(String ruleId, String languageCode) async {
    try {
      final local = await _lpManager.loadLocalResource('grammar/lessons/$languageCode/$ruleId.html', locale: languageCode);
      if (local != null) return local;

      try {
        return await rootBundle.loadString('assets/files/grammar/lessons/$languageCode/$ruleId.html');
      } catch (e) {
        return await rootBundle.loadString('assets/files/grammar/lessons/$ruleId.html');
      }
    } catch (e) {
      return "<h3>Lesson non trouvée</h3><p>Impossible de charger le contenu pour : $ruleId</p>";
    }
  }
}
