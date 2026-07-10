import 'package:flutter/material.dart';
import '../models/quiz_models.dart';
import '../repositories/score_repository.dart';

class ScorePresentationUtils {
  static const Color colorGreen = Color(0xFF4CAF50);
  static const Color colorRed = Color(0xFFF44336);

  /// Calculates the interpolated color for a given score.
  static Color getScoreColor(LearningScore score, Color baseColor) {
    final balance = score.successes - score.failures;
    // Successes have more weight for positive color, but let's stick to the balance logic from Kotlin
    double percentage = (balance / 10.0).clamp(-1.0, 1.0);

    if (percentage > 0) {
      return Color.lerp(baseColor, colorGreen, percentage)!;
    } else if (percentage < 0) {
      return Color.lerp(baseColor, colorRed, -percentage)!;
    } else {
      return baseColor;
    }
  }
}
