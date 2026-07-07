import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/level.dart';

class LevelRepository {
  LevelDefinitions? _definitions;

  Future<LevelDefinitions> loadLevels() async {
    if (_definitions != null) return _definitions!;

    try {
      final String jsonString = await rootBundle.loadString('assets/files/levels.json');
      final Map<String, dynamic> jsonData = json.decode(jsonString);
      _definitions = LevelDefinitions.fromJson(jsonData);
      return _definitions!;
    } catch (e) {
      print("Error loading levels: $e");
      return LevelDefinitions(version: "0", sections: {}, activityTypes: {});
    }
  }

  Future<List<LevelDefinition>> getFlattenedLevels() async {
    final defs = await loadLevels();
    final List<LevelDefinition> allLevels = [];

    // Order matters: fundamentals, then jlpt, then school, then challenge
    final sectionKeys = ['fundamentals', 'jlpt', 'school', 'challenge'];

    for (var key in sectionKeys) {
      if (defs.sections.containsKey(key)) {
        allLevels.addAll(defs.sections[key]!.levels);
      }
    }

    return allLevels;
  }
}
