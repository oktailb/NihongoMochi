import 'package:flutter/material.dart';
import '../models/saga.dart';

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
    // Calcul de la moyenne de progression pour l'affichage principal
    final scores = [
      if (node.recognitionId != null) progress.recognitionIndex,
      if (node.readingId != null) progress.readingIndex,
      if (node.grammarId != null) progress.grammarIndex,
    ];

    final avgProgress = scores.isEmpty
        ? 0
        : (scores.reduce((a, b) => a + b) / scores.length).round();

    final bool isDark = Theme.of(context).brightness == Brightness.dark;
    final Color contentColor = avgProgress >= 100
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.onSurface;

    return GestureDetector(
      onTap: () => onNodeClick(node.id, node.mainType),
      child: Column(
        children: [
          Container(
            width: 100,
            height: 100,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.white.withOpacity(0.9),
              border: Border.all(
                color: avgProgress >= 100 ? Colors.orange : Colors.blue.withOpacity(0.5),
                width: 4,
              ),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.1),
                  blurRadius: 8,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            alignment: Alignment.center,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text(
                  node.title.toUpperCase(),
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
                    fontWeight: FontWeight.w900, // Fixed: FontWeight.black -> FontWeight.w900
                    color: contentColor,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}
