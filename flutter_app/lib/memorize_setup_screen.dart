import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/memorize_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'memorize_game_screen.dart';
import 'dart:ui' as ui;

class MemorizeSetupScreen extends StatelessWidget {
  const MemorizeSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MemorizeProvider>();
    final locale = ui.PlatformDispatcher.instance.locale.toString();

    return GameSetupTemplate(
      title: "Memorize",
      subtitle: "神経衰弱",
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
          );
        }
      },
      children: [
        // Choix de la taille de grille
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withOpacity(0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "TAILLE DE LA GRILLE",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  children: provider.availableGridSizes.map((size) {
                    final isSelected = provider.selectedGridSize == size;
                    return ChoiceChip(
                      label: Text(size.toString()),
                      selected: isSelected,
                      onSelected: (selected) {
                        if (selected) provider.onGridSizeSelected(size);
                      },
                      selectedColor: Colors.pink.shade100,
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),

        // Filtre par traits (si pas de Kanas)
        if (!provider.isKanaLevel)
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            color: Colors.white.withOpacity(0.9),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "MAXIMUM DE TRAITS : ${provider.selectedMaxStrokes}",
                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                  ),
                  Slider(
                    value: provider.selectedMaxStrokes.toDouble(),
                    min: 1,
                    max: provider.maxStrokes.toDouble().clamp(1.0, 100.0),
                    divisions: (provider.maxStrokes - 1).clamp(1, 100),
                    activeColor: Colors.pink,
                    onChanged: (val) => provider.onMaxStrokesChanged(val.toInt(), locale),
                  ),
                ],
              ),
            ),
          ),

        // Historique
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: "Grille ${result.gridSizeLabel}",
              score: "${result.moves} coups",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }
}
