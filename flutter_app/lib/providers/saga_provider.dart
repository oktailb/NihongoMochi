import 'dart:math';
import 'package:flutter/material.dart';
import '../models/saga.dart';
import '../models/level.dart';
import '../repositories/level_repository.dart';
import '../services/statistics_service.dart';
import '../repositories/score_repository.dart';

class SagaProvider extends ChangeNotifier {
  final LevelRepository _levelRepo;
  final StatisticsService _statsService;

  SagaProvider(this._levelRepo, this._statsService);

  List<SagaStep> _steps = [];
  Map<String, UserSagaProgress> _nodeProgress = {};
  bool _isLoading = true;
  SagaTab _currentTab = SagaTab.jlpt;

  List<SagaStep> get steps => _steps;
  Map<String, UserSagaProgress> get nodeProgress => _nodeProgress;
  bool get isLoading => _isLoading;
  SagaTab get currentTab => _currentTab;

  Future<void> loadSaga(SagaTab tab, String locale) async {
    _isLoading = true;
    _currentTab = tab;
    notifyListeners();

    final defs = await _levelRepo.loadLevels();

    // 1. Sélectionner les sections selon l'onglet
    List<String> sectionKeys;
    switch (tab) {
      case SagaTab.jlpt:
        sectionKeys = ["fundamentals", "jlpt"];
        break;
      case SagaTab.school:
        sectionKeys = ["fundamentals", "school"];
        break;
      case SagaTab.challenges:
        sectionKeys = ["challenge"];
        break;
    }

    final relevantLevels = sectionKeys
        .where((k) => defs.sections.containsKey(k))
        .expand((k) => defs.sections[k]!.levels)
        .toList();

    if (relevantLevels.isEmpty) {
      _steps = [];
      _isLoading = false;
      notifyListeners();
      return;
    }

    // 2. Calculer la profondeur par dépendances (Logique Kotlin)
    final levelMap = {for (var l in relevantLevels) l.id: l};
    final depthCache = <String, int>{};

    int getDepth(String levelId, Set<String> stack) {
      if (stack.contains(levelId)) return 0;
      if (depthCache.containsKey(levelId)) return depthCache[levelId]!;

      final level = levelMap[levelId];
      if (level == null) return 0;

      final validDeps = level.dependencies.where((d) => levelMap.containsKey(d)).toList();

      int depth = 0;
      if (validDeps.isNotEmpty) {
        int maxDepDepth = 0;
        for (var d in validDeps) {
          maxDepDepth = max(maxDepDepth, getDepth(d, {...stack, levelId}));
        }
        depth = maxDepDepth + 1;
      }

      depthCache[levelId] = depth;
      return depth;
    }

    // Groupement par profondeur
    final Map<int, List<LevelDefinition>> levelsByDepth = {};
    for (var level in relevantLevels) {
      final d = getDepth(level.id, {});
      levelsByDepth.putIfAbsent(d, () => []).add(level);
    }

    final sortedDepths = levelsByDepth.keys.toList()..sort();

    // 3. Construire les SagaNodes et calculer la progression
    _steps = [];
    _nodeProgress = {};

    for (var depth in sortedDepths) {
      final nodes = <SagaNode>[];
      for (var level in levelsByDepth[depth]!) {
        // Déterminer le type principal
        StatisticsType mainType = StatisticsType.recognition;
        if (level.activities.containsKey("READING")) mainType = StatisticsType.reading;
        else if (level.activities.containsKey("GRAMMAR")) mainType = StatisticsType.grammar;

        final sagaNode = SagaNode(
          id: level.id,
          title: level.name,
          recognitionId: level.activities["RECOGNITION"]?.dataFile == "kanji_details" ? level.id : level.activities["RECOGNITION"]?.dataFile,
          readingId: level.activities["READING"]?.dataFile == "kanji_details" ? level.id : level.activities["READING"]?.dataFile,
          grammarId: level.activities["GRAMMAR"]?.dataFile,
          mainType: mainType,
        );
        nodes.add(sagaNode);

        // Calculer la progression réelle pour ce nœud
        final recogP = sagaNode.recognitionId != null ? await _statsService.getPercentageForLevel(sagaNode.recognitionId!, ScoreType.recognition, locale) : 0;
        final readP = sagaNode.readingId != null ? await _statsService.getPercentageForLevel(sagaNode.readingId!, ScoreType.reading, locale) : 0;
        final grammarP = sagaNode.grammarId != null ? await _statsService.getPercentageForLevel(sagaNode.grammarId!, ScoreType.grammar, locale) : 0;

        _nodeProgress[level.id] = UserSagaProgress(
          recognitionIndex: recogP,
          readingIndex: readP,
          grammarIndex: grammarP,
          nodeProgress: {
            if (sagaNode.recognitionId != null) sagaNode.recognitionId!: recogP,
            if (sagaNode.readingId != null) sagaNode.readingId!: readP,
            if (sagaNode.grammarId != null) sagaNode.grammarId!: grammarP,
          }
        );
      }
      _steps.add(SagaStep(id: "step_$depth", nodes: nodes));
    }

    _isLoading = false;
    notifyListeners();
  }
}
