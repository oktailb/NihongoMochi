import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/grammar.dart';

class GrammarRepository {
  GrammarDefinition? _definition;
  List<GrammarRule>? _cachedAllRules;

  Future<GrammarDefinition> loadGrammarDefinition() async {
    if (_definition != null) return _definition!;

    try {
      final String jsonString = await rootBundle.loadString('assets/files/grammar/grammar.json');
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
      return await rootBundle.loadString('assets/files/grammar/lessons/$fileName');
    } catch (e) {
      return isDark
          ? "body { color: #E0E0E0; background-color: #121212; }"
          : "body { color: #333; background-color: #FFFFFF; }";
    }
  }

  Future<String> loadLessonHtml(String ruleId, String languageCode) async {
    // Tente de charger la version localisée, sinon fallback anglais
    try {
      // Structure suggérée: assets/files/grammar/lessons/fr/lesson_id.html
      return await rootBundle.loadString('assets/files/grammar/lessons/$languageCode/$ruleId.html');
    } catch (e) {
      try {
        return await rootBundle.loadString('assets/files/grammar/lessons/$ruleId.html');
      } catch (e2) {
        return "<h3>Lesson non trouvée</h3><p>Impossible de charger le contenu pour : $ruleId</p>";
      }
    }
  }
}
