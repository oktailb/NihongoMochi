import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/simon.dart';
import 'providers/simon_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'simon_game_screen.dart';
import 'providers/settings_provider.dart';

class SimonSetupScreen extends StatelessWidget {
  const SimonSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SimonProvider>();
    final locale = context.watch<SettingsProvider>().currentLocaleCode;

    return GameSetupTemplate(
      title: "Simon",
      subtitle: "記憶",
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const SimonGameScreen()),
          );
        }
      },
      children: [
        // Configuration du mode de jeu
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
                Column(
                  children: SimonMode.values.where((mode) {
                    // Filtrer les modes selon si c'est un niveau Kana ou Kanji
                    if (provider.isKanaLevel) {
                      return mode == SimonMode.kanaSame || mode == SimonMode.kanaCross;
                    } else {
                      return mode != SimonMode.kanaSame && mode != SimonMode.kanaCross;
                    }
                  }).map((mode) {
                    final isSelected = provider.selectedMode == mode;
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 4.0),
                      child: ChoiceChip(
                        label: Container(
                          width: double.infinity,
                          alignment: Alignment.center,
                          child: Text(_getModeLabel(mode)),
                        ),
                        selected: isSelected,
                        onSelected: (selected) {
                          if (selected) provider.onModeSelected(mode);
                        },
                        selectedColor: Colors.pink.shade100,
                      ),
                    );
                  }).toList(),
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
              label: _getModeLabel(result.mode),
              score: "${result.maxSequence} étapes",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }

  String _getModeLabel(SimonMode mode) {
    switch (mode) {
      case SimonMode.kanji: return "Caractère Kanji";
      case SimonMode.meaning: return "Sens du Kanji";
      case SimonMode.readingCommon: return "Lecture standard";
      case SimonMode.readingRandom: return "Lecture aléatoire";
      case SimonMode.kanaSame: return "Même alphabet";
      case SimonMode.kanaCross: return "Hiragana ↔ Katakana";
    }
  }
}
