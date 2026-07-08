import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/simon.dart';
import 'providers/simon_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'dart:ui' as ui;

class SimonGameScreen extends StatelessWidget {
  const SimonGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SimonProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text("Simon"),
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
          child: Column(
            children: [
              GameHUD(
                primaryLabel: "SCORE",
                primaryValue: provider.score.toString(),
                secondaryLabel: "MEILLEUR",
                secondaryValue: "0", // TODO: Fetch from history
                timeSeconds: provider.gameTimeSeconds,
              ),
              Expanded(
                child: Center(
                  child: AnimatedOpacity(
                    opacity: provider.isKanjiVisible ? 1.0 : 0.0,
                    duration: const Duration(milliseconds: 300),
                    child: _buildQuestionCard(provider.currentPlayable?.character ?? ""),
                  ),
                ),
              ),
              _buildAnswerButtons(context, provider),
              if (provider.gameState == SimonGameState.gameOver)
                _buildGameOverOverlay(context, provider),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildQuestionCard(String text) {
    return Card(
      elevation: 8,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: 250,
        height: 250,
        alignment: Alignment.center,
        child: Text(
          text,
          style: const TextStyle(fontSize: 120, fontWeight: FontWeight.bold, color: Colors.pink),
        ),
      ),
    );
  }

  Widget _buildAnswerButtons(BuildContext context, SimonProvider provider) {
    if (!provider.isButtonsVisible) return const SizedBox(height: 180);

    return Container(
      padding: const EdgeInsets.all(16),
      child: GridView.builder(
        shrinkWrap: true,
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
              backgroundColor: Colors.white,
              foregroundColor: Colors.black87,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
              elevation: 4,
            ),
            child: Text(
              entry.value,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              textAlign: TextAlign.center,
            ),
          );
        },
      ),
    );
  }

  Widget _buildGameOverOverlay(BuildContext context, SimonProvider provider) {
    return Container(
      color: Colors.black54,
      child: Center(
        child: Card(
          margin: const EdgeInsets.all(32),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          child: Padding(
            padding: const EdgeInsets.all(32.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.sentiment_very_dissatisfied, size: 80, color: Colors.red),
                const SizedBox(height: 16),
                const Text("Game Over", style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold)),
                const SizedBox(height: 16),
                Text("Séquence maximale : ${provider.score}", style: const TextStyle(fontSize: 20)),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text("QUITTER"),
                    ),
                    ElevatedButton(
                      onPressed: () {
                        final locale = ui.PlatformDispatcher.instance.locale.toString();
                        provider.startGame(locale);
                      },
                      style: ElevatedButton.styleFrom(backgroundColor: Colors.pink, foregroundColor: Colors.white),
                      child: const Text("RÉESSAYER"),
                    ),
                  ],
                )
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showExitDialog(BuildContext context, SimonProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Quitter ?"),
        content: const Text("Voulez-vous vraiment abandonner la partie ?"),
        actions: [
          TextButton(
            onPressed: () {
              provider.resumeGame();
              Navigator.pop(context);
            },
            child: const Text("NON"),
          ),
          TextButton(
            onPressed: () {
              provider.abandonGame();
              Navigator.pop(context);
              Navigator.pop(context);
            },
            child: const Text("OUI"),
          ),
        ],
      ),
    );
  }
}
