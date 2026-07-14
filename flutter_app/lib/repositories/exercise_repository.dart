import 'dart:convert';
import '../models/grammar_quiz.dart';
import '../services/resource_loader.dart';
import 'package:flutter/foundation.dart';

class ExerciseRepository {
  final ResourceLoader _loader;
  List<Exercise>? _allExercises;

  ExerciseRepository(this._loader);

  Future<void> _ensureLoaded() async {
    if (_allExercises != null) return;

    try {
      final jsonString = await _loader.loadString(
        'exercices.json',
        assetPath: 'assets/files/common/exercices.json',
      );
      
      final Map<String, dynamic> data = json.decode(jsonString);
      var rawExercises = data['exercises'] ?? [];
      final List<dynamic> entries = rawExercises is List ? rawExercises : [rawExercises];
      _allExercises = entries.map((e) => Exercise.fromJson(e)).toList();
    } catch (e) {
      debugPrint("Erreur chargement base exercices: $e");
      _allExercises = [];
    }
  }

  Future<List<Exercise>> getExercisesForTag(String tag, {int limit = 10}) async {
    // 1. Essayer de charger le fichier d'exercice spécifique par règle (nouveau paradigme)
    try {
      final jsonString = await _loader.loadString(
        'grammar/$tag.json', // Vérifie le pack de langue local
        assetPath: 'assets/files/exercices/$tag.json', // Repli sur l'asset local
      );
      final Map<String, dynamic> data = json.decode(jsonString);
      var rawExercises = data['exercises'] ?? [];
      final List<dynamic> entries = rawExercises is List ? rawExercises : [rawExercises];
      final exercises = entries.map((e) => Exercise.fromJson(e)).toList();
      if (exercises.isNotEmpty) {
        return (List<Exercise>.from(exercises)..shuffle()).take(limit).toList();
      }
    } catch (e) {
      // Ignorer l'erreur et passer au fallback
      debugPrint("Infos: Pas de fichier individuel pour le tag $tag, repli sur exercices.json ($e)");
    }

    // 2. Repli sur le fichier global exercices.json hérité
    await _ensureLoaded();
    final filtered = _allExercises!.where((e) => e.tags.contains(tag)).toList();
    final list = List<Exercise>.from(filtered)..shuffle();
    return list.take(limit).toList();
  }

  ExercisePayload? parsePayload(Exercise exercise) {
    try {
      final p = exercise.payload;
      switch (exercise.type) {
        case ExerciseType.fillBlank:
          return FillBlankPayload(
            sentence: p['sentence'] ?? '',
            correct: p['correct'] ?? '',
            distractors: List<String>.from(p['distractors'] ?? []),
          );
        case ExerciseType.sentenceOrder:
          return SentenceOrderPayload(
            prefix: p['prefix'] ?? '',
            suffix: p['suffix'] ?? '',
            blocks: List<String>.from(p['blocks'] ?? []),
          );
        case ExerciseType.underlineReading:
        case ExerciseType.underlineWriting:
          return UnderlinePayload(
            sentence: p['sentence'] ?? '',
            correct: p['correct'] ?? '',
            distractors: List<String>.from(p['distractors'] ?? []),
          );
        case ExerciseType.wordUsage:
          final options = (p['options'] as List).map((o) => UsageOption(
            text: o['text'] ?? '',
            isCorrect: o['is_correct'] ?? false,
          )).toList();
          return WordUsagePayload(
            word: p['word'] ?? '',
            options: options,
          );
        default:
          return null;
      }
    } catch (e) {
      debugPrint("Erreur parsing payload: $e");
      return null;
    }
  }
}
