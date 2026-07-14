import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/memorize_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'memorize_game_screen.dart';

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
                            MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
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
                          MaterialPageRoute(builder: (context) => const MemorizeGameScreen()),
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
                  settings.getString("game_memorize_grid_size_label").isNotEmpty
                      ? settings.getString("game_memorize_grid_size_label").toUpperCase()
                      : "TAILLE DE LA GRILLE",
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
                    _formatString(
                      settings,
                      "game_memorize_strokes_max_label",
                      "MAXIMUM DE TRAITS : %d",
                      "${provider.selectedMaxStrokes}",
                    ),
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
          emptyMessage: settings.getString("game_memorize_no_scores").isNotEmpty
              ? settings.getString("game_memorize_no_scores")
              : "Aucun score pour le moment",
          itemBuilder: (result) {
            return GameHistoryRow(
              label: settings.getString("game_memorize_grid_size_format").isNotEmpty
                  ? settings.getString("game_memorize_grid_size_format").replaceFirst("%s", result.gridSizeLabel)
                  : "Grille ${result.gridSizeLabel}",
              score: _formatString(
                settings,
                "game_memorize_score_format",
                "%d coups",
                "${result.moves}",
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
