import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:provider/provider.dart';
import '../providers/grammar_provider.dart';
import '../providers/settings_provider.dart';
import '../models/quiz_models.dart';
import '../utils/score_presentation_utils.dart';

const double scale = kIsWeb ? 1.5 : 1.0;

class GrammarNodeItem extends StatelessWidget {
  final GrammarNode node;
  final VoidCallback onNodeClick;
  final VoidCallback onLessonClick;
  final bool isLeft;

  const GrammarNodeItem({
    super.key,
    required this.node,
    required this.onNodeClick,
    required this.onLessonClick,
    required this.isLeft,
  });

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();
    final isDark = settings.isDarkMode;

    // Calcul de la couleur de fond en fonction du score
    final scoreObj = LearningScore(
      successes: node.successes,
      failures: node.failures,
    );
    final Color baseColor = isDark ? Colors.grey.shade800 : Colors.white;
    final Color backgroundColor = ScorePresentationUtils.getScoreColor(scoreObj, baseColor);

    final String cardBg = isDark ? 'assets/drawable/card_bg_dark.webp' : 'assets/drawable/card_bg_light.webp';
    final String description = settings.getString(node.rule.description);

    return SizedBox(
      width: 164 * scale,
      height: 134 * scale,
      child: Stack(
        clipBehavior: Clip.none,
        children: [
          // 1. Zone cliquable pour le quiz (la carte centrée)
          Positioned(
            left: 27 * scale,
            width: 110 * scale,
            top: 0,
            bottom: 64 * scale,
            child: GestureDetector(
              onTap: onNodeClick,
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(8 * scale),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 8 * scale,
                      offset: Offset(0, 4 * scale),
                    ),
                  ],
                  image: DecorationImage(
                    image: AssetImage(cardBg),
                    fit: BoxFit.fill,
                  ),
                ),
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(8 * scale),
                    color: backgroundColor.withOpacity(isDark ? 0.2 : 0.3),
                  ),
                  padding: EdgeInsets.all(8 * scale),
                  alignment: Alignment.center,
                  child: Text(
                    description,
                    style: TextStyle(
                      fontSize: 10 * scale,
                      fontWeight: FontWeight.w500,
                      color: isDark ? Colors.white : Colors.black87,
                    ),
                    textAlign: TextAlign.center,
                    maxLines: 3,
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
              ),
            ),
          ),
          // 2. Zone cliquable pour la leçon (icône suspendue)
          if (node.hasLesson)
            Positioned(
              left: (isLeft ? 2.0 : 132.0) * scale,
              top: 35 * scale,
              child: GestureDetector(
                onTap: onLessonClick,
                child: Container(
                  decoration: BoxDecoration(
                    boxShadow: [
                      BoxShadow(
                        color: Colors.black.withOpacity(0.3),
                        blurRadius: 8 * scale,
                        offset: Offset(0, 4 * scale),
                      ),
                    ],
                  ),
                  child: SizedBox(
                    width: 32 * scale,
                    child: Image.asset(
                      'assets/drawable/have_lesson.webp',
                      fit: BoxFit.fitWidth,
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}
