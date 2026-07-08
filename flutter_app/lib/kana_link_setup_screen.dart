import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana_link.dart';
import 'providers/kana_link_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'kana_link_game_screen.dart';

class KanaLinkSetupScreen extends StatelessWidget {
  const KanaLinkSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaLinkProvider>();

    return GameSetupTemplate(
      title: "Kana Link",
      subtitle: "リンク",
      onPlayClick: () async {
        // Le mode est géré par l'état local du setup ou passé ici
        await provider.initGame("n5", mode: provider.history.isEmpty ? KanaLinkMode.timeAttack : KanaLinkMode.timeAttack);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const KanaLinkGameScreen()),
          );
        }
      },
      children: [
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
                  children: [
                    Expanded(
                      child: ChoiceChip(
                        label: const Center(child: Text("Time Attack")),
                        selected: true, // Simplifié pour le portage
                        onSelected: (val) {},
                        selectedColor: Colors.pink.shade100,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
        GameHistoryCard(
          history: provider.history,
          emptyMessage: "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: result.levelId.toUpperCase(),
              score: "${result.score} pts",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }
}
