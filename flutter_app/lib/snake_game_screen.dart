import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'models/snake.dart';
import 'providers/snake_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';
import 'providers/settings_provider.dart';

class SnakeGameScreen extends StatefulWidget {
  const SnakeGameScreen({super.key});

  @override
  State<SnakeGameScreen> createState() => _SnakeGameScreenState();
}

class _SnakeGameScreenState extends State<SnakeGameScreen> {
  final FocusNode _focusNode = FocusNode();

  @override
  void dispose() {
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SnakeProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;
    final state = provider.gameState;


    final String title = settings.getString("game_snake_title").isNotEmpty
        ? settings.getString("game_snake_title")
        : "Snake";

    return KeyboardListener(
      focusNode: _focusNode,
      autofocus: true,
      onKeyEvent: (KeyEvent event) {
        if (event is KeyDownEvent) {
          final key = event.logicalKey;
          if (key == LogicalKeyboardKey.arrowUp || key == LogicalKeyboardKey.keyW) {
            provider.onDirectionChanged(SnakeDirection.up);
          } else if (key == LogicalKeyboardKey.arrowDown || key == LogicalKeyboardKey.keyS) {
            provider.onDirectionChanged(SnakeDirection.down);
          } else if (key == LogicalKeyboardKey.arrowLeft || key == LogicalKeyboardKey.keyA) {
            provider.onDirectionChanged(SnakeDirection.left);
          } else if (key == LogicalKeyboardKey.arrowRight || key == LogicalKeyboardKey.keyD) {
            provider.onDirectionChanged(SnakeDirection.right);
          }
        }
      },
      child: PopScope(
        canPop: state.isGameOver,
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
              child: Stack(
                children: [
                  Column(
                    children: [
                      GameHUD(
                        primaryLabel: settings.getString("game_snake_title").isNotEmpty
                            ? settings.getString("game_snake_title").toUpperCase()
                            : "SNAKE",
                        primaryValue: "",
                        secondaryLabel: settings.getString("game_snake_score").isNotEmpty
                            ? settings.getString("game_snake_score").toUpperCase()
                            : "SCORE",
                        secondaryValue: state.score.toString(),
                        timeSeconds: state.timeSeconds,
                      ),
                      _buildTargetInfo(context, state),
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
                                final double cellWidth = constraints.maxWidth / state.gridWidth;
                                final double cellHeight = constraints.maxHeight / state.gridHeight;
                                final double cellSize = min(cellWidth, cellHeight);

                                final double totalGridWidth = state.gridWidth * cellSize;
                                final double totalGridHeight = state.gridHeight * cellSize;

                                final double offsetX = (constraints.maxWidth - totalGridWidth) / 2;
                                final double offsetY = (constraints.maxHeight - totalGridHeight) / 2;

                                return Stack(
                                  children: [
                                    Positioned(
                                      left: offsetX,
                                      top: offsetY,
                                      width: totalGridWidth,
                                      height: totalGridHeight,
                                      child: Container(
                                        decoration: BoxDecoration(
                                          color: Colors.white.withValues(alpha: 0.2),
                                          borderRadius: BorderRadius.circular(12),
                                        ),
                                      ),
                                    ),
                                    // Snake segments
                                    ...state.snake.asMap().entries.map((entry) {
                                      final index = entry.key;
                                      final point = entry.value;
                                      return Positioned(
                                        left: offsetX + point.x * cellSize,
                                        top: offsetY + point.y * cellSize,
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
                                        left: offsetX + state.targetItem!.position.x * cellSize,
                                        top: offsetY + state.targetItem!.position.y * cellSize,
                                        width: cellSize,
                                        height: cellSize,
                                        child: _buildGameItem(state.targetItem!, cellSize, true),
                                      ),
                                    // Distractions
                                    ...state.distractions.map((item) {
                                      return Positioned(
                                        left: offsetX + item.position.x * cellSize,
                                        top: offsetY + item.position.y * cellSize,
                                        width: cellSize,
                                        height: cellSize,
                                        child: _buildGameItem(item, cellSize, false),
                                      );
                                    }),
                                  ],
                                );
                              },
                            ),
                          ),
                        ),
                      ),
                      _buildControls(context, provider),
                    ],
                  ),

                  // Game result overlay
                  if (state.isGameOver)
                    GameResultOverlay(
                      isVictory: false,
                      title: settings.getString("game_snake_game_over").isNotEmpty
                          ? settings.getString("game_snake_game_over")
                          : "Game Over",
                      score: state.score.toString(),
                      bestScore: null,
                      stats: [
                        MapEntry(
                          settings.getString("game_snake_words").isNotEmpty
                              ? settings.getString("game_snake_words")
                              : "Mots complétés",
                          state.wordsCompleted.toString(),
                        ),
                        MapEntry(
                          settings.getString("game_taquin_time_label").isNotEmpty
                              ? settings.getString("game_taquin_time_label")
                              : "Temps",
                          _formatGameTimeHUD(state.timeSeconds),
                        ),
                      ],
                      onReplayClick: () => provider.startGame(locale),
                      onMenuClick: () => Navigator.pop(context),
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  String _formatGameTimeHUD(int seconds) {
    final int m = seconds ~/ 60;
    final int s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  Widget _buildTargetInfo(BuildContext context, SnakeGameState state) {
    final theme = Theme.of(context);
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: theme.colorScheme.primaryContainer.withValues(alpha: 0.8),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Center(
        child: Text(
          state.currentTargetLabel,
          style: TextStyle(
            fontSize: 24,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onPrimaryContainer,
          ),
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

  Widget _buildControls(BuildContext context, SnakeProvider provider) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          _buildDirectionBtn(context, Icons.arrow_upward, () => provider.onDirectionChanged(SnakeDirection.up)),
          const SizedBox(width: 20),
          Column(
            children: [
              _buildDirectionBtn(context, Icons.arrow_back, () => provider.onDirectionChanged(SnakeDirection.left)),
              const SizedBox(height: 20),
              _buildDirectionBtn(context, Icons.arrow_forward, () => provider.onDirectionChanged(SnakeDirection.right)),
            ],
          ),
          const SizedBox(width: 20),
          _buildDirectionBtn(context, Icons.arrow_downward, () => provider.onDirectionChanged(SnakeDirection.down)),
        ],
      ),
    );
  }

  Widget _buildDirectionBtn(BuildContext context, IconData icon, VoidCallback onTap) {
    final theme = Theme.of(context);
    return ElevatedButton(
      onPressed: onTap,
      style: ElevatedButton.styleFrom(
        shape: const CircleBorder(),
        padding: const EdgeInsets.all(20),
        backgroundColor: theme.colorScheme.primaryContainer.withValues(alpha: 0.8),
      ),
      child: Icon(icon, color: theme.colorScheme.onPrimaryContainer),
    );
  }

  void _showExitDialog(BuildContext context, SnakeProvider provider) {
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
