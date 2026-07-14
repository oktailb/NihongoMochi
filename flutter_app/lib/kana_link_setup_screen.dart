import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana_link.dart';
import 'providers/kana_link_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'kana_link_game_screen.dart';
import 'widgets/game_restore_card.dart';

class KanaLinkSetupScreen extends StatefulWidget {
  const KanaLinkSetupScreen({super.key});

  @override
  State<KanaLinkSetupScreen> createState() => _KanaLinkSetupScreenState();
}

class _KanaLinkSetupScreenState extends State<KanaLinkSetupScreen> {
  KanaLinkMode _selectedMode = KanaLinkMode.timeAttack;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<KanaLinkProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const KanaLinkGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaLinkProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_kana_link_title");
    final subVal = settings.getString("game_kana_link_subtitle");

    final title = (titleVal.isNotEmpty && titleVal != "game_kana_link_title") ? titleVal : "Kana Link";
    final subtitle = (subVal.isNotEmpty && subVal != "game_kana_link_subtitle") ? subVal : "カナリンク";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
      onPlayClick: () async {
        final levelId = settings.selectedLevel;
        await provider.initGame(levelId.isEmpty ? "n5" : levelId, mode: _selectedMode);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const KanaLinkGameScreen()),
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
                    MaterialPageRoute(builder: (context) => const KanaLinkGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              final levelId = settings.selectedLevel;
              await provider.initGame(levelId.isEmpty ? "n5" : levelId, mode: _selectedMode);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const KanaLinkGameScreen()),
                );
              }
            },
          ),
          const SizedBox(height: 16),
        ],

        // Choix du Mode de Jeu
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withValues(alpha: 0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settings.getString("game_config_title").isNotEmpty
                      ? settings.getString("game_config_title").toUpperCase()
                      : "MODE DE JEU",
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Row(
                  children: KanaLinkMode.values.map((mode) {
                    final isSelected = _selectedMode == mode;
                    return Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4.0),
                        child: FilterChip(
                          label: Center(
                            child: Text(mode == KanaLinkMode.timeAttack ? "Time Attack" : "Survival"),
                          ),
                          selected: isSelected,
                          onSelected: (selected) {
                            if (selected) {
                              setState(() {
                                _selectedMode = mode;
                              });
                            }
                          },
                          selectedColor: theme.colorScheme.primaryContainer,
                          checkmarkColor: theme.colorScheme.onPrimaryContainer,
                          labelStyle: TextStyle(
                            fontWeight: FontWeight.bold,
                            color: isSelected ? theme.colorScheme.onPrimaryContainer : Colors.black87,
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),

        // Historique
        GameHistoryCard(
          history: provider.history,
          emptyMessage: settings.getString("game_memorize_no_scores").isNotEmpty
              ? settings.getString("game_memorize_no_scores")
              : "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: result.levelId.toUpperCase(),
              score: "${result.score} pts",
              time: _formatString(
                settings,
                "game_memorize_time_format",
                "%ds",
                "${result.timeSeconds}",
              ),
            );
          },
        ),
      ],
    );
  }

  String _formatString(SettingsProvider settings, String key, String fallback, String value) {
    final raw = settings.getString(key);
    final text = raw.isNotEmpty && raw != key ? raw : fallback;
    if (text.contains("%")) {
      return text
          .replaceAll("%1\$d", value)
          .replaceAll("%1%d", value)
          .replaceAll("%d", value);
    }
    return "$text $value";
  }
}
