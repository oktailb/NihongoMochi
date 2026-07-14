import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/saga.dart';
import '../providers/settings_provider.dart';

class SagaNodeItem extends StatelessWidget {
  final SagaNode node;
  final UserSagaProgress progress;
  final Function(String, StatisticsType) onNodeClick;

  const SagaNodeItem({
    super.key,
    required this.node,
    required this.progress,
    required this.onNodeClick,
  });

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();
    // Calcul de la moyenne de progression pour l'affichage principal
    final scores = [
      if (node.recognitionId != null) progress.recognitionIndex,
      if (node.readingId != null) progress.readingIndex,
      if (node.grammarId != null) progress.grammarIndex,
    ];

    final avgProgress = scores.isEmpty
        ? 0
        : (scores.reduce((a, b) => a + b) / scores.length).round();

    final Color contentColor = avgProgress >= 100
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.onSurface;

    final String levelBgRes = Theme.of(context).brightness == Brightness.dark
        ? 'assets/drawable/level_dark.webp'
        : 'assets/drawable/level_light.webp';

    return GestureDetector(
      onTap: () => onNodeClick(node.id, node.mainType),
      child: SizedBox(
        width: 110,
        height: 110,
        child: Stack(
          alignment: Alignment.center,
          children: [
            Image.asset(
              levelBgRes,
              width: 110,
              height: 110,
              fit: BoxFit.contain,
            ),
            Padding(
              padding: const EdgeInsets.only(bottom: 8.0),
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Text(
                    settings.getString(node.title).toUpperCase(),
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: contentColor,
                    ),
                  ),
                  Text(
                    "$avgProgress%",
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w900,
                      color: contentColor,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
