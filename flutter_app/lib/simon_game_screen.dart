import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
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
          title: Text(title, style: TextStyle(color: Theme.of(context).colorScheme.onSurface)),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: Icon(Icons.arrow_back, color: Theme.of(context).colorScheme.onSurface),
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
                    const Spacer(),
                    AnimatedOpacity(
                      opacity: provider.isKanjiVisible ? 1.0 : 0.0,
                      duration: const Duration(milliseconds: 300),
                      child: _buildQuestionCard(context, provider.currentPlayable?.character ?? ""),
                    ),
                    const Spacer(),
                    _buildAnswerButtons(context, provider),
                    const SizedBox(height: 16),
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
    final double cardSize = (MediaQuery.of(context).size.shortestSide * 0.55).clamp(160.0, 240.0);
    final double fontSize = text.length <= 2 ? 80.0 : (text.length <= 6 ? 40.0 : 24.0);

    return Card(
      elevation: 12,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: cardSize,
        height: cardSize,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Center(
          child: Text(
            text,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: fontSize,
              fontWeight: FontWeight.bold,
              color: theme.colorScheme.onSurface,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildAnswerButtons(BuildContext context, SimonProvider provider) {
    if (!provider.isButtonsVisible) {
      return const SizedBox(height: 140);
    }

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        children: [
          Row(
            children: [
              _buildButton(0, provider),
              const SizedBox(width: 12),
              _buildButton(1, provider),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _buildButton(2, provider),
              const SizedBox(width: 12),
              _buildButton(3, provider),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildButton(int index, SimonProvider provider) {
    if (index >= provider.answers.length) return const Expanded(child: SizedBox.shrink());

    final entry = provider.answers[index];

    return Expanded(
      child: GameAnswerButton(
        text: entry.value,
        state: AnswerButtonState.defaultState,
        enabled: provider.gameState == SimonGameState.awaitingInput,
        fontSize: _calculateButtonFontSize(entry.value),
        onClick: () => provider.onAnswerClick(entry.key),
      ),
    );
  }

  double _calculateButtonFontSize(String text) {
    if (text.length <= 2) return 28.0;
    if (text.length <= 6) return 20.0;
    if (text.length <= 15) return 15.0;
    if (text.length <= 30) return 12.0;
    return 10.0;
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

