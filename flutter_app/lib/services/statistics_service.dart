import '../db/database.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';

class StatisticsService {
  final LevelContentProvider levelContentProvider;
  final ScoreRepository scoreRepo;

  StatisticsService({
    required this.levelContentProvider,
    required this.scoreRepo,
  });

  /// Calcule le pourcentage de maîtrise (0-100) pour un niveau donné.
  /// Basé sur la règle métier Kotlin : chaque item maîtrisé vaut 10 points.
  Future<int> getPercentageForLevel(
    String levelId, 
    ScoreType type, 
    String locale, 
    [Map<String, LearningScoreEntity>? preloadedScores]
  ) async {
    final items = await levelContentProvider.getItemsForLevel(levelId, type, locale);
    if (items.isEmpty) return 0;

    double totalPoints = 0;
    for (var item in items) {
      final score = preloadedScores != null 
          ? preloadedScores[item] 
          : await scoreRepo.getScore(item, type);
      if (score != null) {
        // Balance succès - échecs, plafonnée entre 0 et 10
        final balance = score.successes - score.failures;
        totalPoints += balance.clamp(0, 10);
      }
    }

    final maxPossiblePoints = items.length * 10.0;
    return ((totalPoints / maxPossiblePoints) * 100).toInt();
  }

  /// Calcule la progression globale d'une session de quiz.
  Future<int> calculateSessionScore(List<String> quizItems, ScoreType type) async {
    if (quizItems.isEmpty) return 0;

    double totalPoints = 0;
    for (var item in quizItems) {
      final score = await scoreRepo.getScore(item, type);
      if (score != null) {
        totalPoints += (score.successes - score.failures).clamp(0, 10);
      }
    }

    return ((totalPoints / (quizItems.length * 10.0)) * 100).toInt();
  }
}
