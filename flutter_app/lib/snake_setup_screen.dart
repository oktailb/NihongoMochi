import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/snake.dart';
import 'providers/snake_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'snake_game_screen.dart';
import 'widgets/game_restore_card.dart';

class SnakeSetupScreen extends StatefulWidget {
  const SnakeSetupScreen({super.key});

  @override
  State<SnakeSetupScreen> createState() => _SnakeSetupScreenState();
}

class _SnakeSetupScreenState extends State<SnakeSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<SnakeProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const SnakeGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SnakeProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_snake_title");
    final subVal = settings.getString("game_snake_subtitle");

    final title = (titleVal.isNotEmpty && titleVal != "game_snake_title") ? titleVal : "Snake";
    final subtitle = (subVal.isNotEmpty && subVal != "game_snake_subtitle") ? subVal : "ヘビ";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
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
        // Partie en cours (Restauration)
        if (provider.hasSavedGame) ...[
          GameRestoreCard(
            onResumeClick: () {
              provider.restoreGame(() {
                if (context.mounted) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(builder: (context) => const SnakeGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              await provider.startGame(locale);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const SnakeGameScreen()),
                );
              }
            },
          ),
          const SizedBox(height: 16),
        ],

        // Mode Selection
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
                Wrap(
                  spacing: 8,
                  children: SnakeMode.values.map((mode) {
                    final isSelected = provider.selectedMode == mode;
                    return FilterChip(
                      label: Text(_getModeLabel(mode, settings)),
                      selected: isSelected,
                      onSelected: (selected) {
                        if (selected) provider.onModeSelected(mode);
                      },
                      selectedColor: theme.colorScheme.primaryContainer,
                      checkmarkColor: theme.colorScheme.onPrimaryContainer,
                      labelStyle: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: isSelected ? theme.colorScheme.onPrimaryContainer : Colors.black87,
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),

        // Recent Scores
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: settings.getString("game_memorize_no_scores").isNotEmpty
              ? settings.getString("game_memorize_no_scores")
              : "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: _getModeLabel(result.mode, settings),
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

  String _getModeLabel(SnakeMode mode, SettingsProvider settings) {
    switch (mode) {
      case SnakeMode.hiragana:
        final str = settings.getString("game_taquin_mode_hiragana");
        return (str.isNotEmpty && str != "game_taquin_mode_hiragana") ? str : "Hiragana";
      case SnakeMode.katakana:
        final str = settings.getString("game_taquin_mode_katakana");
        return (str.isNotEmpty && str != "game_taquin_mode_katakana") ? str : "Katakana";
      case SnakeMode.numbers:
        final str = settings.getString("game_taquin_mode_numbers");
        return (str.isNotEmpty && str != "game_taquin_mode_numbers") ? str : "Chiffres";
      case SnakeMode.words:
        final str = settings.getString("game_writing_label_meaning");
        return (str.isNotEmpty && str != "game_writing_label_meaning") ? str : "Mots";
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
