import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/memorize_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/memory_card.dart';
import 'widgets/game_components.dart';

class MemorizeGameScreen extends StatelessWidget {
  const MemorizeGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MemorizeProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final gridSize = provider.selectedGridSize;

    final String title = settings.getString("game_memorize_title").isNotEmpty
        ? settings.getString("game_memorize_title")
        : "Memorize";

    return PopScope(
      canPop: provider.isGameFinished,
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
          child: Stack(
            children: [
              SafeArea(
                child: Column(
                  children: [
                    GameHUD(
                      primaryLabel: settings.getString("game_taquin_moves_label").toUpperCase(),
                      primaryValue: provider.moves.toString(),
                      secondaryLabel: "",
                      secondaryValue: settings.getString("game_memorize_pairs_count", [
                        provider.cards.where((c) => c.isMatched).length ~/ 2,
                        gridSize.pairsCount,
                      ]),
                      timeSeconds: provider.gameTimeSeconds,
                    ),
                    const SizedBox(height: 16),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        child: LayoutBuilder(
                          builder: (context, constraints) {
                            final double totalHorizontalSpacing = (gridSize.cols - 1) * 8.0;
                            final double totalVerticalSpacing = (gridSize.rows - 1) * 8.0;
                            final double itemWidth = (constraints.maxWidth - totalHorizontalSpacing) / gridSize.cols;
                            final double itemHeight = (constraints.maxHeight - totalVerticalSpacing) / gridSize.rows;
                            final double childAspectRatio = (itemWidth / itemHeight).clamp(0.2, 5.0);

                            return GridView.builder(
                              padding: EdgeInsets.zero,
                              physics: const NeverScrollableScrollPhysics(),
                              gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                                crossAxisCount: gridSize.cols,
                                crossAxisSpacing: 8,
                                mainAxisSpacing: 8,
                                childAspectRatio: childAspectRatio,
                              ),
                              itemCount: provider.cards.length,
                              itemBuilder: (context, index) {
                                return MemoryCard(
                                  state: provider.cards[index],
                                  onClick: () => provider.onCardClicked(index),
                                );
                              },
                            );
                          },
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              // Game result overlay
              if (provider.isGameFinished)
                GameResultOverlay(
                  isVictory: true,
                  title: settings.getString("game_memorize_success_title"),
                  score: provider.moves.toString(),
                  bestScore: null,
                  stats: [
                    MapEntry(
                      settings.getString("game_taquin_time_label"),
                      _formatGameTimeHUD(provider.gameTimeSeconds),
                    ),
                  ],
                  onReplayClick: () => provider.startGame(locale),
                  onMenuClick: () => Navigator.pop(context),
                ),
            ],
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

  void _showExitDialog(BuildContext context, MemorizeProvider provider) {
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
