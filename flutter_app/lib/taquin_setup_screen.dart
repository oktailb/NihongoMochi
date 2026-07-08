import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/taquin.dart';
import 'providers/taquin_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'taquin_game_screen.dart';

class TaquinSetupScreen extends StatelessWidget {
  const TaquinSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TaquinProvider>();

    return GameSetupTemplate(
      title: "Taquin",
      subtitle: "パズル",
      onPlayClick: () async {
        await provider.startGame();
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const TaquinGameScreen()),
          );
        }
      },
      children: [
        // Mode de jeu
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
                  children: TaquinMode.values.map((mode) {
                    return ChoiceChip(
                      label: Text(_getModeName(mode)),
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

        // Nombre de lignes (Difficulté)
        if (provider.selectedMode != TaquinMode.numbers)
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            color: Colors.white.withOpacity(0.9),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    "NOMBRE DE LIGNES : ${provider.selectedRows}",
                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                  ),
                  Slider(
                    value: provider.selectedRows.toDouble(),
                    min: 2,
                    max: 10,
                    divisions: 8,
                    activeColor: Colors.pink,
                    onChanged: (val) => provider.onRowsSelected(val.toInt()),
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
              label: "${_getModeName(result.mode)} (${result.rows} lig.)",
              score: "${result.moves} coups",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }

  String _getModeName(TaquinMode mode) {
    switch (mode) {
      case TaquinMode.hiragana: return "Hiragana";
      case TaquinMode.katakana: return "Katakana";
      case TaquinMode.numbers: return "Chiffres";
    }
  }
}
