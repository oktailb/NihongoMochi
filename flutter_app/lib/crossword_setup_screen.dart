import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/crossword.dart';
import 'providers/crossword_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'crossword_game_screen.dart';
import 'widgets/game_restore_card.dart';

class CrosswordSetupScreen extends StatefulWidget {
  const CrosswordSetupScreen({super.key});

  @override
  State<CrosswordSetupScreen> createState() => _CrosswordSetupScreenState();
}

class _CrosswordSetupScreenState extends State<CrosswordSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<CrosswordProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const CrosswordGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<CrosswordProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_crosswords_title");
    final subVal = settings.getString("game_crosswords_subtitle");

    final title = (titleVal.isNotEmpty && titleVal != "game_crosswords_title") ? titleVal : "Mots Croisés";
    final subtitle = (subVal.isNotEmpty && subVal != "game_crosswords_subtitle") ? subVal : "Mochi-Cross";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const CrosswordGameScreen()),
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
                    MaterialPageRoute(builder: (context) => const CrosswordGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              await provider.startGame(locale);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const CrosswordGameScreen()),
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
                  settings.getString("game_crossword_setup_mode").isNotEmpty && settings.getString("game_crossword_setup_mode") != "game_crossword_setup_mode"
                      ? settings.getString("game_crossword_setup_mode").toUpperCase()
                      : "MODE DE JEU",
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Row(
                  children: CrosswordMode.values.map((mode) {
                    final isSelected = provider.selectedMode == mode;
                    return Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 4.0),
                        child: FilterChip(
                          label: Center(
                            child: Text(mode == CrosswordMode.kanas ? "Kanas" : "Kanjis"),
                          ),
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

        // Word Count Selection (Slider)
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withValues(alpha: 0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  _formatString(
                    settings,
                    "game_crossword_setup_word_count",
                    "NOMBRE DE MOTS : %d",
                    "${provider.wordCount}",
                  ),
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                Slider(
                  value: provider.wordCount.toDouble(),
                  min: 5,
                  max: 42,
                  divisions: 37,
                  activeColor: theme.colorScheme.primary,
                  inactiveColor: theme.colorScheme.primaryContainer,
                  onChanged: (val) => provider.onWordCountSelected(val.toInt()),
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
            final modeLabel = result.mode == CrosswordMode.kanas ? "Kanas" : "Kanjis";
            return GameHistoryRow(
              label: _formatString(
                settings,
                "game_crossword_history_item",
                "%d mots ($modeLabel)",
                "${result.wordCount}",
              ),
              score: "${result.completionPercentage}%",
              time: _formatString(
                settings,
                "game_memorize_time_format",
                "%ds",
                "${result.timeSeconds}",
              ),
            );
          },
        ),

        if (provider.isGenerating) ...[
          const SizedBox(height: 16),
          const Center(
            child: Column(
              children: [
                CircularProgressIndicator(),
                SizedBox(height: 8),
                Text("Génération de la grille..."),
              ],
            ),
          ),
        ],
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
