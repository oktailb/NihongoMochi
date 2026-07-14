import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/simon.dart';
import 'providers/simon_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'simon_game_screen.dart';

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

    final title = settings.getString("game_simon_title").isNotEmpty
        ? settings.getString("game_simon_title")
        : "Simon";
    final subtitle = settings.getString("game_simon_japanese_title").isNotEmpty
        ? settings.getString("game_simon_japanese_title")
        : "記憶";

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
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            color: theme.colorScheme.primaryContainer,
            elevation: 8,
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                children: [
                  Text(
                    "Une partie est en pause",
                    style: TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                      color: theme.colorScheme.onPrimaryContainer,
                    ),
                  ),
                  const SizedBox(height: 12),
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      minimumSize: const Size.fromHeight(48),
                      backgroundColor: theme.colorScheme.primary,
                      foregroundColor: theme.colorScheme.onPrimary,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                    ),
                    icon: const Icon(Icons.history),
                    label: const Text("Reprendre la partie"),
                    onPressed: () {
                      provider.restoreGame(() {
                        if (context.mounted) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(builder: (context) => const SimonGameScreen()),
                          );
                        }
                      });
                    },
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: () async {
                      await provider.startGame(locale);
                      if (context.mounted) {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const SimonGameScreen()),
                        );
                      }
                    },
                    child: Text(
                      "Nouvelle partie (effacer la précédente)",
                      style: TextStyle(color: theme.colorScheme.onPrimaryContainer.withValues(alpha: 0.8)),
                    ),
                  ),
                ],
              ),
            ),
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
    final raw = settings.getString(key);
    final text = raw.isNotEmpty ? raw : fallback;
    if (text.contains("%")) {
      return text
          .replaceAll("%1\$d", value)
          .replaceAll("%1%d", value)
          .replaceAll("%d", value);
    }
    return "$text $value";
  }
}
