import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/snake.dart';
import 'providers/snake_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'providers/settings_provider.dart';

class SnakeGameScreen extends StatelessWidget {
  const SnakeGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SnakeProvider>();
    final state = provider.gameState;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Snake"),
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
                primaryLabel: "SNAKE",
                primaryValue: "",
                secondaryLabel: "SCORE",
                secondaryValue: state.score.toString(),
                timeSeconds: state.timeSeconds,
              ),
              _buildTargetInfo(state),
              Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: GestureDetector(
                    onVerticalDragUpdate: (details) {
                      if (details.delta.dy < -5) provider.onDirectionChanged(SnakeDirection.up);
                      if (details.delta.dy > 5) provider.onDirectionChanged(SnakeDirection.down);
                    },
                    onHorizontalDragUpdate: (details) {
                      if (details.delta.dx < -5) provider.onDirectionChanged(SnakeDirection.left);
                      if (details.delta.dx > 5) provider.onDirectionChanged(SnakeDirection.right);
                    },
                    child: LayoutBuilder(
                      builder: (context, constraints) {
                        final cellSize = constraints.maxWidth / state.gridWidth;
                        return Stack(
                          children: [
                            Container(
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.2),
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                            // Snake segments
                            ...state.snake.asMap().entries.map((entry) {
                              final index = entry.key;
                              final point = entry.value;
                              return Positioned(
                                left: point.x * cellSize,
                                top: point.y * cellSize,
                                width: cellSize,
                                height: cellSize,
                                child: Container(
                                  margin: const EdgeInsets.all(1),
                                  decoration: BoxDecoration(
                                    color: index == 0 ? Colors.green.shade700 : Colors.green.shade400,
                                    shape: BoxShape.circle,
                                  ),
                                ),
                              );
                            }),
                            // Target item
                            if (state.targetItem != null)
                              Positioned(
                                left: state.targetItem!.position.x * cellSize,
                                top: state.targetItem!.position.y * cellSize,
                                width: cellSize,
                                height: cellSize,
                                child: _buildGameItem(state.targetItem!, cellSize, true),
                              ),
                            // Distractions
                            ...state.distractions.map((item) {
                              return Positioned(
                                left: item.position.x * cellSize,
                                top: item.position.y * cellSize,
                                width: cellSize,
                                height: cellSize,
                                child: _buildGameItem(item, cellSize, false),
                              );
                            }),
                            if (state.isGameOver)
                              _buildGameOverOverlay(context, provider),
                          ],
                        );
                      },
                    ),
                  ),
                ),
              ),
              _buildControls(provider),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTargetInfo(SnakeGameState state) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.blue.shade100.withOpacity(0.8),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Center(
        child: Text(
          state.currentTargetLabel,
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.blue),
        ),
      ),
    );
  }

  Widget _buildGameItem(SnakeItem item, double size, bool isTarget) {
    return Container(
      alignment: Alignment.center,
      decoration: BoxDecoration(
        color: isTarget ? Colors.orange.shade100 : Colors.grey.shade200,
        shape: BoxShape.circle,
      ),
      child: Text(
        item.character,
        style: TextStyle(
          fontSize: size * 0.7,
          fontWeight: FontWeight.bold,
          color: isTarget ? Colors.orange.shade900 : Colors.black54,
        ),
      ),
    );
  }

  Widget _buildControls(SnakeProvider provider) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _buildDirectionBtn(Icons.arrow_upward, () => provider.onDirectionChanged(SnakeDirection.up)),
          const SizedBox(width: 20),
          Column(
            children: [
              _buildDirectionBtn(Icons.arrow_back, () => provider.onDirectionChanged(SnakeDirection.left)),
              const SizedBox(height: 20),
              _buildDirectionBtn(Icons.arrow_forward, () => provider.onDirectionChanged(SnakeDirection.right)),
            ],
          ),
          const SizedBox(width: 20),
          _buildDirectionBtn(Icons.arrow_downward, () => provider.onDirectionChanged(SnakeDirection.down)),
        ],
      ),
    );
  }

  Widget _buildDirectionBtn(IconData icon, VoidCallback onTap) {
    return ElevatedButton(
      onPressed: onTap,
      style: ElevatedButton.styleFrom(
        shape: const CircleBorder(),
        padding: const EdgeInsets.all(20),
        backgroundColor: Colors.white.withOpacity(0.8),
      ),
      child: Icon(icon, color: Colors.pink),
    );
  }

  Widget _buildGameOverOverlay(BuildContext context, SnakeProvider provider) {
    return Container(
      color: Colors.black54,
      child: Center(
        child: Card(
          margin: const EdgeInsets.all(32),
          child: Padding(
            padding: const EdgeInsets.all(32.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.sentiment_very_dissatisfied, size: 80, color: Colors.red),
                const SizedBox(height: 16),
                const Text("Game Over", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
                const SizedBox(height: 24),
                Text("Score final : ${provider.gameState.score}"),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(onPressed: () => Navigator.pop(context), child: const Text("MENU")),
                    ElevatedButton(
                      onPressed: () => provider.startGame(context.read<SettingsProvider>().currentLocaleCode),
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

  void _showExitDialog(BuildContext context, SnakeProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Quitter ?"),
        content: const Text("Voulez-vous vraiment abandonner la partie ?"),
        actions: [
          TextButton(onPressed: () { provider.resumeGame(); Navigator.pop(context); }, child: const Text("NON")),
          TextButton(onPressed: () { Navigator.pop(context); Navigator.pop(context); }, child: const Text("OUI")),
        ],
      ),
    );
  }
}
