import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/shiritori_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'shiritori_game_screen.dart';
import 'providers/settings_provider.dart';

class ShiritoriSetupScreen extends StatelessWidget {
  const ShiritoriSetupScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ShiritoriProvider>();
    final locale = context.watch<SettingsProvider>().currentLocaleCode;

    return GameSetupTemplate(
      title: "Shiritori",
      subtitle: "しりとり",
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const ShiritoriGameScreen()),
          );
        }
      },
      children: [
        // Règles du jeu
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withOpacity(0.9),
          child: const Padding(
            padding: EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  "RÈGLES DU JEU",
                  style: TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                SizedBox(height: 8),
                Text(
                  "Trouvez un mot commençant par le dernier kana du mot précédent. Si vous finissez par 'ん', vous avez perdu !",
                  style: TextStyle(fontSize: 14),
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
              label: result.levelId.toUpperCase(),
              score: "${result.score} mots",
              time: "${result.timeSeconds}s",
            );
          },
        ),
      ],
    );
  }
}
