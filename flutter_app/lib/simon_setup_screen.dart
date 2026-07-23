import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/simon.dart';
import 'providers/simon_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'simon_game_screen.dart';
import 'widgets/game_restore_card.dart';

class SimonSetupScreen extends StatefulWidget {
  const SimonSetupScreen({super.key});

  @override
  State<SimonSetupScreen> createState() => _SimonSetupScreenState();
}

class _SimonSetupScreenState extends State<SimonSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SimonProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const SimonGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SimonProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_simon_title");
    final subVal = settings.getString("game_simon_japanese_title");

    final title = (titleVal.isNotEmpty && titleVal != "game_simon_title") ? titleVal : "Simon";
    final subtitle = (subVal.isNotEmpty && subVal != "game_simon_japanese_title") ? subVal : "記憶";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
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
        // Partie en cours (Restauration)
        if (provider.hasSavedGame) ...[
          GameRestoreCard(
            onResumeClick: () {
              provider.restoreGame(() {
                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const SimonGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              await provider.startGame(locale);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SimonGameScreen()),
                );
              }
            },
          ),
          const SizedBox(height: 16),
        ],

        // Configuration du mode de jeu
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
                Column(
                  children: SimonMode.values.where((mode) {
                    if (provider.isKanaLevel) {
                      return mode == SimonMode.kanaSame || mode == SimonMode.kanaCross;
                    } else {
                      return mode != SimonMode.kanaSame && mode != SimonMode.kanaCross;
                    }
                  }).map((mode) {
                    final isSelected = provider.selectedMode == mode;
                    return Padding(
                      padding: const EdgeInsets.only(bottom: 4.0),
                      child: FilterChip(
                        label: Container(
                          width: double.infinity,
                          alignment: Alignment.center,
                          child: Text(_getModeLabel(settings, mode)),
                        ),
                        selected: isSelected,
                        onSelected: (selected) {
                          if (selected) {
                            provider.onModeSelected(mode);
                          }
                        },
                        selectedColor: theme.colorScheme.primaryContainer,
                        checkmarkColor: theme.colorScheme.onPrimaryContainer,
                        labelStyle: TextStyle(
                          fontWeight: FontWeight.bold,
                          color: isSelected ? theme.colorScheme.onPrimaryContainer : Colors.black87,
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
          history: provider.scoresHistory,
          emptyMessage: settings.getString("game_memorize_no_scores").isNotEmpty
              ? settings.getString("game_memorize_no_scores")
              : "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: _getModeLabel(settings, result.mode),
              score: _formatString(
                settings,
                "game_simon_score_label",
                "Séquence : %d",
                "${result.maxSequence}",
              ),
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

  String _getModeLabel(SettingsProvider settings, SimonMode mode) {
    switch (mode) {
      case SimonMode.kanji:
        return settings.getString("game_simon_mode_kanji").isNotEmpty
            ? settings.getString("game_simon_mode_kanji")
            : "Caractère Kanji";
      case SimonMode.meaning:
        return settings.getString("game_simon_mode_meaning").isNotEmpty
            ? settings.getString("game_simon_mode_meaning")
            : "Sens du Kanji";
      case SimonMode.readingCommon:
        return settings.getString("game_simon_mode_reading_std").isNotEmpty
            ? settings.getString("game_simon_mode_reading_std")
            : "Lecture standard";
      case SimonMode.readingRandom:
        return settings.getString("game_simon_mode_reading_rnd").isNotEmpty
            ? settings.getString("game_simon_mode_reading_rnd")
            : "Lecture aléatoire";
      case SimonMode.kanaSame:
        return settings.getString("game_simon_mode_kana_same").isNotEmpty
            ? settings.getString("game_simon_mode_kana_same")
            : "Même alphabet";
      case SimonMode.kanaCross:
        return settings.getString("game_simon_mode_kana_cross").isNotEmpty
            ? settings.getString("game_simon_mode_kana_cross")
            : "Hiragana ↔ Katakana";
    }
  }

  String _formatString(SettingsProvider settings, String key, String fallback, String value) {
    final result = settings.getString(key, [value]);
    if (result != key && !result.contains('{param1}')) {
      return result;
    }
    if (fallback.contains('%d')) {
      return fallback.replaceAll('%d', value);
    }
    return "$fallback $value";
  }
}
