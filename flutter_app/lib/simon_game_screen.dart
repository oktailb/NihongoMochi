import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/simon.dart';
import 'providers/simon_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';

class SimonGameScreen extends StatelessWidget {
  const SimonGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SimonProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;

    final bestScore = provider.scoresHistory.isEmpty
        ? 0
        : provider.scoresHistory.map((h) => h.maxSequence).reduce(max);

    final String title = settings.getString("game_simon_title").isNotEmpty
        ? settings.getString("game_simon_title")
        : "Simon";

    return PopScope(
      canPop: provider.gameState == SimonGameState.gameOver,
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
                      primaryLabel: settings.getString("game_simon_score_label").isNotEmpty
                          ? settings.getString("game_simon_score_label").toUpperCase().replaceAll(" %D", "").replaceAll(" %1\$D", "")
                          : "SEQUENCE",
                      primaryValue: provider.score.toString(),
                      secondaryLabel: settings.getString("game_simon_record_label").isNotEmpty
                          ? settings.getString("game_simon_record_label").toUpperCase().replaceAll(" %D", "").replaceAll(" %1\$D", "")
                          : "RECORD",
                      secondaryValue: bestScore.toString(),
                      timeSeconds: provider.gameTimeSeconds,
                    ),
                    Expanded(
                      child: Center(
                        child: AnimatedOpacity(
                          opacity: provider.isKanjiVisible ? 1.0 : 0.0,
                          duration: const Duration(milliseconds: 300),
                          child: _buildQuestionCard(context, provider.currentPlayable?.character ?? ""),
                        ),
                      ),
                    ),
                    _buildAnswerButtons(context, provider),
                  ],
                ),
              ),

              // Game result overlay
              if (provider.gameState == SimonGameState.gameOver)
                GameResultOverlay(
                  isVictory: false,
                  title: settings.getString("game_over_title").isNotEmpty
                      ? settings.getString("game_over_title")
                      : "Game Over",
                  score: provider.score.toString(),
                  bestScore: bestScore.toString(),
                  stats: [
                    MapEntry(
                      settings.getString("game_taquin_time_label").isNotEmpty
                          ? settings.getString("game_taquin_time_label")
                          : "Temps",
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

  Widget _buildQuestionCard(BuildContext context, String text) {
    final theme = Theme.of(context);
    return Card(
      elevation: 8,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      color: Colors.white.withValues(alpha: 0.95),
      child: Container(
        width: 240,
        height: 240,
        alignment: Alignment.center,
        child: Text(
          text,
          style: TextStyle(
            fontSize: 90,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.primary,
          ),
        ),
      ),
    );
  }

  Widget _buildAnswerButtons(BuildContext context, SimonProvider provider) {
    // Keep space when buttons are not visible to avoid layout jump
    if (!provider.isButtonsVisible) {
      return const SizedBox(height: 180);
    }

    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: GridView.builder(
        shrinkWrap: true,
        padding: EdgeInsets.zero,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          crossAxisSpacing: 12,
          mainAxisSpacing: 12,
          childAspectRatio: 2.2,
        ),
        itemCount: provider.answers.length,
        itemBuilder: (context, index) {
          final entry = provider.answers[index];
          return ElevatedButton(
            onPressed: () => provider.onAnswerClick(entry.key),
            style: ElevatedButton.styleFrom(
              backgroundColor: Colors.white.withValues(alpha: 0.95),
              foregroundColor: theme.colorScheme.onSurface,
              elevation: 4,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
                side: BorderSide(color: theme.colorScheme.primary.withValues(alpha: 0.2)),
              ),
            ),
            child: Text(
              entry.value,
              style: TextStyle(
                fontSize: _calculateFontSize(entry.value),
                fontWeight: FontWeight.bold,
              ),
              textAlign: TextAlign.center,
            ),
          );
        },
      ),
    );
  }

  double _calculateFontSize(String text) {
    if (text.length > 8) return 14;
    if (text.length > 5) return 18;
    return 22;
  }

  String _formatGameTimeHUD(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  void _showExitDialog(BuildContext context, SimonProvider provider) {
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
