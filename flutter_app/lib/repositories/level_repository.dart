import 'dart:convert';
import '../models/level.dart';
import '../services/resource_loader.dart';

class LevelRepository {
  final ResourceLoader _loader;
  LevelDefinitions? _definitions;

  LevelRepository(this._loader);

  Future<LevelDefinitions> loadLevels() async {
    if (_definitions != null) return _definitions!;

    try {
      final jsonString = await _loader.loadString('levels.json');
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
