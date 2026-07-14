import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/taquin.dart';
import 'providers/taquin_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/game_setup_template.dart';
import 'widgets/game_history_card.dart';
import 'taquin_game_screen.dart';

class TaquinSetupScreen extends StatefulWidget {
  const TaquinSetupScreen({super.key});

  @override
  State<TaquinSetupScreen> createState() => _TaquinSetupScreenState();
}

class _TaquinSetupScreenState extends State<TaquinSetupScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<TaquinProvider>().tryAutoRestore(() {
        if (mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const TaquinGameScreen()),
          );
        }
      });
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TaquinProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    final titleVal = settings.getString("game_taquin_title");
    final subVal = settings.getString("game_taquin_japanese_title");

    return GameSetupTemplate(
      title: (titleVal.isNotEmpty && titleVal != "game_taquin_title") ? titleVal : "Taquin",
      subtitle: (subVal.isNotEmpty && subVal != "game_taquin_japanese_title") ? subVal : "パズル",
      onPlayClick: () async {
        await provider.startGame();
        if (context.mounted) {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const TaquinGameScreen()),
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
                            MaterialPageRoute(builder: (context) => const TaquinGameScreen()),
                          );
                        }
                      });
                    },
                  ),
                  const SizedBox(height: 8),
                  TextButton(
                    onPressed: () async {
                      await provider.startGame();
                      if (context.mounted) {
                        Navigator.push(
                          context,
                          MaterialPageRoute(builder: (context) => const TaquinGameScreen()),
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
                  settings.getString("game_taquin_mode_label").isNotEmpty
                      ? settings.getString("game_taquin_mode_label")
                      : "MODE DE JEU",
                  style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                ),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: TaquinMode.values.map((mode) {
                    final isSelected = provider.selectedMode == mode;
                    return FilterChip(
                      label: Text(mode.toString().split('.').last.toUpperCase()),
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
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 12),

        // Nombre de lignes (Difficulté) - Masqué en mode Chiffres
        if (provider.selectedMode != TaquinMode.numbers) ...[
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
                      "game_taquin_rows_label",
                      "NOMBRE DE LIGNES : %d",
                      "${provider.selectedRows}",
                    ),
                    style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.black54),
                  ),
                  Slider(
                    value: provider.selectedRows.toDouble(),
                    min: 2,
                    max: 10,
                    divisions: 8,
                    activeColor: theme.colorScheme.primary,
                    inactiveColor: theme.colorScheme.primaryContainer,
                    onChanged: (val) => provider.onRowsSelected(val.toInt()),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 12),
        ],

        // Historique des scores
        GameHistoryCard(
          history: provider.scoresHistory,
          emptyMessage: settings.getString("game_memorize_no_scores").isNotEmpty
              ? settings.getString("game_memorize_no_scores")
              : "Aucun score pour le moment",
          itemBuilder: (result) {
            final String modeLabel = result.mode.toString().split('.').last.toUpperCase();
            final String sizeLabel = result.mode == TaquinMode.numbers ? "4x4" : "${result.rows}x5";
            return GameHistoryRow(
              label: "TAQUIN - $modeLabel ($sizeLabel)",
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
    return text
        .replaceAll("%1\$d", value)
        .replaceAll("%1%d", value)
        .replaceAll("%d", value);
  }
}
