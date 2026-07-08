import 'dart:math';
import 'package:flutter/material.dart';
import '../models/grammar.dart';
import '../repositories/grammar_repository.dart';
import '../repositories/score_repository.dart';

class GrammarNode {
  final GrammarRule rule;
  final double x; // Relative X (0.0 - 1.0)
  final double y; // Relative Y (0.0 - 1.0)
  final int successes;
  final int failures;
  final bool hasLesson;

  GrammarNode({
    required this.rule,
    required this.x,
    required this.y,
    this.successes = 0,
    this.failures = 0,
    this.hasLesson = false,
  });

  int get score => successes - failures;
}

class GrammarLevelSeparator {
  final String levelId;
  final double y;
  final int completionPercentage;
  final List<String> ruleIds;

  GrammarLevelSeparator({
    required this.levelId,
    required this.y,
    this.completionPercentage = 0,
    this.ruleIds = const [],
  });
}

class GrammarProvider extends ChangeNotifier {
  final GrammarRepository _repository;
  final ScoreRepository _scoreRepo;

  List<GrammarNode> _nodes = [];
  List<GrammarLevelSeparator> _separators = [];
  double _totalLayoutSlots = 1.0;
  bool _isLoading = true;
  String _currentLevelId = "N5";

  List<GrammarNode> get nodes => _nodes;
  List<GrammarLevelSeparator> get separators => _separators;
  double get totalLayoutSlots => _totalLayoutSlots;
  bool get isLoading => _isLoading;
  String get currentLevelId => _currentLevelId;

  GrammarProvider(this._repository, this._scoreRepo);

  Future<void> loadGraph(String maxLevelId) async {
    _isLoading = true;
    _currentLevelId = maxLevelId;
    notifyListeners();

    await _refreshGraph();

    _isLoading = false;
    notifyListeners();
  }

  Future<void> _refreshGraph() async {
    final def = await _repository.loadGrammarDefinition();
    final allLevels = def.metadata.levels;
    final targetIndex = allLevels.indexOf(_currentLevelId.toLowerCase());
    final levelsToShow = allLevels.take(targetIndex != -1 ? targetIndex + 1 : allLevels.length).toList();

    final rules = await _repository.getRulesByBlock('rules', _currentLevelId);

    // Algorithme de layout (porté depuis Kotlin)
    const slotHeightPerNode = 1.0;
    const paddingSlotsPerLevel = 3.0;
    const initialTopPadding = 2.0;

    final rulesMap = {for (var r in rules) r.id: r};
    final depthCache = <String, int>{};

    int getDepth(String ruleId) {
      if (depthCache.containsKey(ruleId)) return depthCache[ruleId]!;
      final rule = rulesMap[ruleId];
      if (rule == null) return 0;

      int maxDepDepth = -1;
      for (var depId in rule.dependencies) {
        final d = getDepth(depId);
        if (d > maxDepDepth) maxDepDepth = d;
      }
      final depth = maxDepDepth + 1;
      depthCache[ruleId] = depth;
      return depth;
    }

    for (var r in rules) {
      getDepth(r.id);
    }

    final rulesByLevel = <String, List<GrammarRule>>{};
    for (var r in rules) {
      final level = r.level.toLowerCase();
      rulesByLevel.putIfAbsent(level, () => []).add(r);
    }

    double currentSlot = initialTopPadding;
    final rawSeparators = <Map<String, dynamic>>[];
    final finalNodes = <GrammarNode>[];

    for (var levelId in levelsToShow) {
      currentSlot += 0.5;
      final levelRules = rulesByLevel[levelId.toLowerCase()] ?? [];

      if (levelRules.isNotEmpty) {
        final assignedSides = <String, double>{};
        int leftCount = 0;
        int rightCount = 0;

        final sortedRules = List<GrammarRule>.from(levelRules)
          ..sort((a, b) {
            final depthA = depthCache[a.id] ?? 0;
            final depthB = depthCache[b.id] ?? 0;
            return depthA != depthB ? depthA.compareTo(depthB) : a.id.compareTo(b.id);
          });

        for (var rule in sortedRules) {
          double? preferredSide;
          final intraLevelParent = rule.dependencies.firstWhere(
            (depId) => rulesMap[depId]?.level.toLowerCase() == levelId.toLowerCase() && assignedSides.containsKey(depId),
            orElse: () => "",
          );
          if (intraLevelParent.isNotEmpty) preferredSide = assignedSides[intraLevelParent];

          final side = preferredSide ?? (leftCount <= rightCount ? 0.3 : 0.7);
          assignedSides[rule.id] = side;
          if (side < 0.5) leftCount++; else rightCount++;

          // Score & HasLesson
          final scoreEntity = await _scoreRepo.getScore(rule.id, ScoreType.grammar);
          final hasLesson = await _repository.hasLesson(rule.id);

          finalNodes.add(GrammarNode(
            rule: rule,
            x: side,
            y: currentSlot,
            successes: scoreEntity?.successes ?? 0,
            failures: scoreEntity?.failures ?? 0,
            hasLesson: hasLesson,
          ));
          currentSlot += slotHeightPerNode;
        }
      } else {
        currentSlot += slotHeightPerNode;
      }

      currentSlot += paddingSlotsPerLevel / 2;
      rawSeparators.add({
        'id': levelId,
        'y': currentSlot,
        'rules': levelRules.map((r) => r.id).toList(),
      });
      currentSlot += paddingSlotsPerLevel / 2;
    }

    _totalLayoutSlots = currentSlot == 0 ? 1.0 : currentSlot;

    _nodes = finalNodes.map((n) => GrammarNode(
      rule: n.rule,
      x: n.x,
      y: n.y / _totalLayoutSlots,
      successes: n.successes,
      failures: n.failures,
      hasLesson: n.hasLesson,
    )).toList();

    _separators = rawSeparators.map((s) {
      final levelRules = rulesByLevel[s['id'].toString().toLowerCase()] ?? [];
      int completion = 0;
      if (levelRules.isNotEmpty) {
        int successCount = 0;
        for (var r in levelRules) {
          // Note: On pourrait optimiser en pré-chargeant tous les scores
          final nodeIndex = _nodes.indexWhere((n) => n.rule.id == r.id);
          if (nodeIndex != -1 && _nodes[nodeIndex].score >= 1) successCount++;
        }
        completion = (successCount * 100) ~/ levelRules.length;
      }

      return GrammarLevelSeparator(
        levelId: s['id'],
        y: s['y'] / _totalLayoutSlots,
        completionPercentage: completion,
        ruleIds: List<String>.from(s['rules']),
      );
    }).toList();
  }
}
