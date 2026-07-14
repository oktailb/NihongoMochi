import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/shiritori_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'widgets/game_restore_card.dart';
import 'shiritori_game_screen.dart';
import 'providers/settings_provider.dart';

class ShiritoriSetupScreen extends StatefulWidget {
  const ShiritoriSetupScreen({super.key});

  @override
  State<ShiritoriSetupScreen> createState() => _ShiritoriSetupScreenState();
}

class _ShiritoriSetupScreenState extends State<ShiritoriSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<ShiritoriProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const ShiritoriGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ShiritoriProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;

    // Title and Subtitle conform to KMP
    final titleVal = settings.getString("game_shiritori_title");
    final title = (titleVal.isNotEmpty && titleVal != "game_shiritori_title") ? titleVal : "Shiritori Zen";
    const subtitle = "しりとり";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
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
        // Partie en cours (Restauration)
        if (provider.hasSavedGame) ...[
          GameRestoreCard(
            onResumeClick: () {
              provider.restoreGame(() {
                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const ShiritoriGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              await provider.startGame(locale);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const ShiritoriGameScreen()),
                );
              }
            },
          ),
          const SizedBox(height: 16),
        ],

        // Règles du jeu
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withValues(alpha: 0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settings.getString("shiritori_rules").toUpperCase(),
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 8),
                Text(
                  settings.getString("shiritori_rules_desc"),
                  style: const TextStyle(fontSize: 14),
                ),
              ],
            ),
          ),
        ),

        // Historique
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: settings.getString("game_kana_link_no_history"),
          itemBuilder: (result) {
            return GameHistoryRow(
              label: result.levelId.toUpperCase(),
              score: settings.getString("game_kana_link_history_item_format", [result.score]),
              time: settings.getString("game_memorize_time_format", [result.timeSeconds]),
            );
          },
        ),
      ],
    );
  }
}
