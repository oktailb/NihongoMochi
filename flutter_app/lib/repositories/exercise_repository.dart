import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/grammar_quiz.dart';

class ExerciseRepository {
  final Map<String, List<Exercise>> _cache = {};

  Future<List<Exercise>> getExercisesForTag(String tag, {int limit = 10}) async {
    if (_cache.containsKey(tag)) {
      final list = List<Exercise>.from(_cache[tag]!)..shuffle();
      return list.take(limit).toList();
    }

    try {
      final String jsonString = await rootBundle.loadString('assets/files/grammar/$tag.json');
      final Map<String, dynamic> data = json.decode(jsonString);
      final List<dynamic> exercisesJson = data['exercises'] ?? [];

      final exercises = exercisesJson.map((e) => Exercise.fromJson(e)).toList();
      _cache[tag] = exercises;

      final list = List<Exercise>.from(exercises)..shuffle();
      return list.take(limit).toList();
    } catch (e) {
      print("Erreur chargement exercices pour $tag: $e");
      return [];
    }
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
      print("Erreur parsing payload: $e");
      return null;
    }
  }
}
