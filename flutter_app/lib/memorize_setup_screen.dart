import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/memorize_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'memorize_game_screen.dart';
import 'widgets/game_restore_card.dart';

class MemorizeSetupScreen extends StatefulWidget {
  const MemorizeSetupScreen({super.key});

  @override
  State<MemorizeSetupScreen> createState() => _MemorizeSetupScreenState();
}

class _MemorizeSetupScreenState extends State<MemorizeSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<MemorizeProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MemorizeProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_memorize_title");
    final subVal = settings.getString("game_memorize_japanese_title");

    final title = (titleVal.isNotEmpty && titleVal != "game_memorize_title") ? titleVal : "Memorize";
    final subtitle = (subVal.isNotEmpty && subVal != "game_memorize_japanese_title") ? subVal : "神経衰弱";

    return GameSetupTemplate(
      title: title,
      subtitle: subtitle,
      onPlayClick: () async {
        await provider.startGame(locale);
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
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
                    MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
                  );
                }
              });
            },
            onNewGameClick: () async {
              await provider.startGame(locale);
              if (context.mounted) {
                Navigator.push(
                  context,
                  MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
                );
              }
            },
          ),
          const SizedBox(height: 16),
        ],

        // Choix de la taille de grille
        Card(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          color: Colors.white.withValues(alpha: 0.9),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  settings.getString("game_memorize_grid_size").toUpperCase(),
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Wrap(
                  spacing: 8,
                  children: provider.availableGridSizes.map((size) {
                    final isSelected = provider.selectedGridSize == size;
                    return FilterChip(
                      label: Text(size.toString()),
                      selected: isSelected,
                      onSelected: (selected) {
                        if (selected) {
                          provider.onGridSizeSelected(size);
                        }
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

        // Filtre par traits (si pas de Kanas)
        if (!provider.isKanaLevel) ...[
          Card(
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
            color: Colors.white.withValues(alpha: 0.9),
            child: Padding(
              padding: const EdgeInsets.all(16.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    settings.getString("game_memorize_max_strokes", [provider.selectedMaxStrokes]).toUpperCase(),
                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                  ),
                  Slider(
                    value: provider.selectedMaxStrokes.toDouble(),
                    min: 1,
                    max: provider.maxStrokes.toDouble().clamp(1.0, 100.0),
                    divisions: (provider.maxStrokes - 1).clamp(1, 100),
                    activeColor: theme.colorScheme.primary,
                    inactiveColor: theme.colorScheme.primaryContainer,
                    onChanged: (val) => provider.onMaxStrokesChanged(val.toInt(), locale),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
        ],

        // Historique
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: settings.getString("game_memorize_no_scores"),
          itemBuilder: (result) {
            return GameHistoryRow(
              label: settings.getString("game_memorize_grid_label", [result.gridSizeLabel]),
              score: settings.getString("game_memorize_score_format", [result.moves]),
              time: settings.getString("game_memorize_time_format", [result.timeSeconds]),
            );
          },
        ),
      ],
    );
  }
}
