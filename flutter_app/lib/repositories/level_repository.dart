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

  List<LevelDefinition> getLevelsForModeCached(String mode) {
    final defs = _definitions;
    if (defs == null) return [];

    String? sectionKey;
    for (var entry in defs.sections.entries) {
      if (entry.value.name.toLowerCase() == 'section_${mode.toLowerCase()}' ||
          entry.key.toLowerCase() == mode.toLowerCase()) {
        sectionKey = entry.key;
        break;
      }
    }
    sectionKey ??= 'jlpt';

    final targetSection = defs.sections[sectionKey];
    final List<LevelDefinition> levels = [];
    
    if (targetSection != null) {
      // Find sections that have prerequisiteFor containing targetSection key
      final prerequisites = defs.sections.entries
          .where((e) => e.value.prerequisiteFor.contains(sectionKey))
          .expand((e) => e.value.levels)
          .toList();
      levels.addAll(prerequisites);
      levels.addAll(targetSection.levels);
    }

    // Add revision level
    levels.add(LevelDefinition(
      id: 'user_custom_list',
      name: 'Revisions',
      description: 'Vos révisions personnalisées',
      activities: {
        'RECOGNITION': ActivityConfig(dataFile: 'user_custom_list', enabled: true),
        'READING': ActivityConfig(dataFile: 'user_custom_list', enabled: true),
        'WRITING': ActivityConfig(dataFile: 'user_custom_list', enabled: true),
        'GRAMMAR': ActivityConfig(dataFile: 'user_custom_list', enabled: true),
      },
    ));

    return levels;
  }
}

