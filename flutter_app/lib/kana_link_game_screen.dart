import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana_link.dart';
import 'providers/kana_link_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';

class KanaLinkGameScreen extends StatelessWidget {
  const KanaLinkGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaLinkProvider>();
    final settings = context.watch<SettingsProvider>();

    final String title = settings.getString("game_kana_link_title").isNotEmpty
        ? settings.getString("game_kana_link_title")
        : "Kana Link";

    return PopScope(
      canPop: provider.isGameOver,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _showExitDialog(context, provider);
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(title),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => _showExitDialog(context, provider),
          ),
        ),
        extendBodyBehindAppBar: true,
        body: MochiBackground(
          child: SafeArea(
            child: provider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : Stack(
                    children: [
                      Column(
                        children: [
                          GameHUD(
                            primaryLabel: settings.getString("game_kana_link_title").toUpperCase(),
                            primaryValue: "",
                            secondaryLabel: settings.getString("game_kana_link_score_label").toUpperCase(),
                            secondaryValue: provider.score.toString(),
                            timeSeconds: provider.timeRemaining,
                          ),
                          _buildCurrentWordArea(context, provider),
                          Expanded(child: _buildGrid(context, provider)),
                        ],
                      ),

                      // Game result overlay
                      if (provider.isGameOver)
                        GameResultOverlay(
                          isVictory: false,
                          title: settings.getString("game_kana_link_game_over").isNotEmpty
                              ? settings.getString("game_kana_link_game_over")
                              : "Game Over",
                          score: provider.score.toString(),
                          bestScore: null,
                          stats: [
                            MapEntry(
                              settings.getString("game_kana_link_words_found").isNotEmpty
                                  ? settings.getString("game_kana_link_words_found")
                                  : "Mots trouvés",
                              provider.wordsFound.toString(),
                            ),
                            MapEntry(
                              settings.getString("game_taquin_time_label").isNotEmpty
                                  ? settings.getString("game_taquin_time_label")
                                  : "Temps",
                              _formatGameTimeHUD(provider.timeElapsed),
                            ),
                          ],
                          onReplayClick: () {
                            final levelId = settings.selectedLevel;
                            provider.initGame(levelId.isEmpty ? "n5" : levelId);
                          },
                          onMenuClick: () => Navigator.pop(context),
                        ),
                    ],
                  ),
          ),
        ),
      ),
    );
  }

  Widget _buildCurrentWordArea(BuildContext context, KanaLinkProvider provider) {
    final theme = Theme.of(context);
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 16),
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
      decoration: BoxDecoration(
        color: theme.colorScheme.primary.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        provider.currentWord.isEmpty ? " " : provider.currentWord,
        style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
      ),
    );
  }

  Widget _buildGrid(BuildContext context, KanaLinkProvider provider) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final int cols = provider.grid[0].length;
        final int rows = provider.grid.length;

        final double maxCellWidth = constraints.maxWidth / cols;
        final double maxCellHeight = constraints.maxHeight / rows;
        final double cellSize = min(maxCellWidth, maxCellHeight);

        final double totalGridWidth = cols * cellSize;
        final double totalGridHeight = rows * cellSize;

        final double offsetX = (constraints.maxWidth - totalGridWidth) / 2;
        final double offsetY = (constraints.maxHeight - totalGridHeight) / 2;

        return GestureDetector(
          onPanStart: (details) => _handleTouch(details.localPosition, cellSize, offsetX, offsetY, provider),
          onPanUpdate: (details) => _handleTouch(details.localPosition, cellSize, offsetX, offsetY, provider),
          onPanEnd: (_) => provider.onReleaseSelection(),
          child: Container(
            color: Colors.transparent,
            child: Stack(
              children: provider.grid.expand((row) => row).map((cell) {
                return AnimatedPositioned(
                  duration: const Duration(milliseconds: 300),
                  left: offsetX + cell.col * cellSize,
                  top: offsetY + cell.row * cellSize,
                  width: cellSize,
                  height: cellSize,
                  child: _buildCellItem(context, cell, cellSize, provider),
                );
              }).toList(),
            ),
          ),
        );
      },
    );
  }

  void _handleTouch(Offset localPos, double cellSize, double offsetX, double offsetY, KanaLinkProvider provider) {
    final int col = ((localPos.dx - offsetX) / cellSize).floor();
    final int row = ((localPos.dy - offsetY) / cellSize).floor();

    if (row >= 0 && row < provider.grid.length && col >= 0 && col < provider.grid[0].length) {
      provider.onCellTouched(row, col);
    }
  }

  Widget _buildCellItem(BuildContext context, KanaLinkCell cell, double size, KanaLinkProvider provider) {
    final isSelected = provider.selectedCells.any((c) => c.id == cell.id);
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(2),
      child: Opacity(
        opacity: cell.isMatched ? 0.0 : 1.0,
        child: Container(
          decoration: BoxDecoration(
            color: isSelected ? theme.colorScheme.primary : Colors.white.withValues(alpha: 0.9),
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.1),
                blurRadius: 2,
              ),
            ],
          ),
          alignment: Alignment.center,
          child: Text(
            cell.char,
            style: TextStyle(
              fontSize: size * 0.5,
              fontWeight: FontWeight.bold,
              color: isSelected ? theme.colorScheme.onPrimary : Colors.black87,
            ),
          ),
        ),
      ),
    );
  }

  String _formatGameTimeHUD(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  void _showExitDialog(BuildContext context, KanaLinkProvider provider) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogContext) {
        return ExitConfirmationDialog(
          onConfirm: () {
            provider.abandonGame();
            Navigator.pop(dialogContext); // close dialog
            Navigator.pop(context); // exit screen
          },
          onDismiss: () {
            Navigator.pop(dialogContext); // close dialog
          },
          onPause: () {
            provider.pauseGame();
          },
          onResume: () {
            provider.resumeGame();
          },
          onSaveAndExit: () {
            provider.saveAndExit();
            Navigator.pop(dialogContext); // close dialog
            Navigator.pop(context); // exit screen
          },
        );
      },
    );
  }
}
