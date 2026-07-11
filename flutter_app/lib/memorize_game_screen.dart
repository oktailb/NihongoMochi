import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/memorize_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/memory_card.dart';
import 'providers/settings_provider.dart';

class MemorizeGameScreen extends StatelessWidget {
  const MemorizeGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<MemorizeProvider>();
    final gridSize = provider.selectedGridSize;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Memorize"),
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () {
            // Show confirmation dialog before exiting
            _showExitDialog(context, provider);
          },
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              GameHUD(
                primaryLabel: "COUPS",
                primaryValue: provider.moves.toString(),
                secondaryLabel: "PAIRES",
                secondaryValue: "${provider.cards.where((c) => c.isMatched).length ~/ 2} / ${gridSize.pairsCount}",
                timeSeconds: provider.gameTimeSeconds,
              ),
              const SizedBox(height: 16),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  child: GridView.builder(
                    gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                      crossAxisCount: gridSize.cols,
                      crossAxisSpacing: 8,
                      mainAxisSpacing: 8,
                      childAspectRatio: 0.8,
                    ),
                    itemCount: provider.cards.length,
                    itemBuilder: (context, index) {
                      return MemoryCard(
                        state: provider.cards[index],
                        onClick: () => provider.onCardClicked(index),
                      );
                    },
                  ),
                ),
              ),
              if (provider.isGameFinished)
                _buildFinishedOverlay(context, provider),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildFinishedOverlay(BuildContext context, MemorizeProvider provider) {
    return Container(
      color: Colors.black54,
      child: Center(
        child: Card(
          margin: const EdgeInsets.all(32),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          child: Padding(
            padding: const EdgeInsets.all(32.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.emoji_events, size: 80, color: Colors.orange),
                const SizedBox(height: 16),
                const Text(
                  "Terminé !",
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 24),
                _buildStatRow("Coups", provider.moves.toString()),
                _buildStatRow("Temps", "${provider.gameTimeSeconds}s"),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text("MENU"),
                    ),
                    ElevatedButton(
                      onPressed: () {
                        final locale = context.read<SettingsProvider>().currentLocaleCode;
                        provider.startGame(locale);
                      },
                      style: ElevatedButton.styleFrom(backgroundColor: Colors.pink, foregroundColor: Colors.white),
                      child: const Text("REJOUER"),
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

  Widget _buildStatRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: const TextStyle(fontSize: 18, color: Colors.black54)),
          Text(value, style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
        ],
      ),
    );
  }

  void _showExitDialog(BuildContext context, MemorizeProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Abandonner ?"),
        content: const Text("Voulez-vous vraiment quitter la partie en cours ?"),
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
              Navigator.pop(context); // close dialog
              Navigator.pop(context); // exit screen
            },
            child: const Text("OUI"),
          ),
        ],
      ),
    );
  }
}
