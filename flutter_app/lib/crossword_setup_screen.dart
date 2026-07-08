import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/crossword.dart';
import 'providers/crossword_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'crossword_game_screen.dart';
import 'dart:ui' as ui;

class CrosswordSetupScreen extends StatelessWidget {
  const CrosswordSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<CrosswordProvider>();
    final locale = ui.PlatformDispatcher.instance.locale.toString();

    return GameSetupTemplate(
      title: "Mots Croisés",
      subtitle: "Mochi-Cross",
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const CrosswordGameScreen()),
          );
        }
      },
      children: [
        // Mode Selection
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withOpacity(0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text(
                  "MODE DE JEU",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: CrosswordMode.values.map((mode) {
                    return ChoiceChip(
                      label: Text(mode == CrosswordMode.kanas ? "Kanas" : "Kanjis"),
                      selected: provider.selectedMode == mode,
                      onSelected: (selected) {
                        if (selected) provider.onModeSelected(mode);
                      },
                      selectedColor: Colors.pink.shade100,
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),

        // Word Count Selection
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withOpacity(0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "NOMBRE DE MOTS : ${provider.wordCount}",
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                Slider(
                  value: provider.wordCount.toDouble(),
                  min: 5,
                  max: 42,
                  divisions: 37,
                  activeColor: Colors.pink,
                  onChanged: (val) => provider.onWordCountSelected(val.toInt()),
                ),
              ],
            ),
          ),
        ),

        // Recent Scores
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: "${result.wordCount} mots (${result.mode == CrosswordMode.kanas ? 'Kanas' : 'Kanjis'})",
              score: "${result.completionPercentage}%",
              time: "${result.timeSeconds}s",
            );
          },
        ),

        if (provider.isGenerating)
          const Center(
            child: Column(
              children: [
                CircularProgressIndicator(),
                SizedBox(height: 8),
                Text("Génération de la grille..."),
              ],
            ),
          ),
      ],
    );
  }
}
