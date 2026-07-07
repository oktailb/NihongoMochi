import 'package:flutter/material.dart';
import '../providers/grammar_provider.dart';

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
    // Calcul de la couleur en fonction du score (similaire à ScorePresentationUtils.getScoreColor)
    final Color backgroundColor = _getScoreColor(node.score);
    final isDark = Theme.of(context).brightness == Brightness.dark;

    return GestureDetector(
      onTap: onNodeClick,
      child: SizedBox(
        width: 110,
        height: 134,
        child: Stack(
          clipBehavior: Clip.none,
          children: [
            // Card Background Image
            Positioned.fill(
              bottom: 64,
              child: Container(
                decoration: BoxDecoration(
                  borderRadius: BorderRadius.circular(8),
                  boxShadow: [
                    BoxShadow(
                      color: Colors.black.withOpacity(0.2),
                      blurRadius: 8,
                      offset: const Offset(0, 4),
                    ),
                  ],
                  image: DecorationImage(
                    image: AssetImage(
                      isDark ? 'assets/drawable/card_bg_dark.png' : 'assets/drawable/card_bg_light.png',
                    ),
                    fit: BoxFit.fill,
                  ),
                ),
                child: Container(
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(8),
                    color: backgroundColor.withOpacity(isDark ? 0.2 : 0.3),
                  ),
                  padding: const EdgeInsets.all(8),
                  alignment: Alignment.center,
                  child: Text(
                    node.rule.description,
                    style: TextStyle(
                      fontSize: 10,
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
            // Lesson Icon
            if (node.hasLesson)
              Positioned(
                left: isLeft ? -25 : 105,
                top: 35,
                child: GestureDetector(
                  onTap: onLessonClick,
                  child: Container(
                    width: 32,
                    height: 32,
                    decoration: BoxDecoration(
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.3),
                          blurRadius: 8,
                          offset: const Offset(0, 4),
                        ),
                      ],
                    ),
                    child: Image.asset('assets/drawable/have_lesson.png'),
                  ),
                ),
              ),
          ],
        ),
      ),
    );
  }

  Color _getScoreColor(int score) {
    if (score <= -5) return Colors.red;
    if (score < 0) return Colors.orange;
    if (score == 0) return Colors.transparent;
    if (score < 5) return Colors.lightGreen;
    return Colors.green;
  }
}
