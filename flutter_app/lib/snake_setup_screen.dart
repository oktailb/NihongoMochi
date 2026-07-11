import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/snake.dart';
import 'providers/snake_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'snake_game_screen.dart';
import 'providers/settings_provider.dart';

class SnakeSetupScreen extends StatelessWidget {
  const SnakeSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SnakeProvider>();
    final locale = context.watch<SettingsProvider>().currentLocaleCode;

    return GameSetupTemplate(
      title: "Snake",
      subtitle: "ヘビ",
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const SnakeGameScreen()),
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
                Wrap(
                  spacing: 8,
                  children: SnakeMode.values.map((mode) {
                    return ChoiceChip(
                      label: Text(_getModeLabel(mode)),
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

        // Recent Scores
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: _getModeLabel(result.mode),
              score: "${result.score} pts",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }

  String _getModeLabel(SnakeMode mode) {
    switch (mode) {
      case SnakeMode.hiragana: return "Hiragana";
      case SnakeMode.katakana: return "Katakana";
      case SnakeMode.numbers: return "Chiffres";
      case SnakeMode.words: return "Mots";
    }
  }
}
