import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/taquin.dart';
import 'providers/taquin_provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';

class TaquinGameScreen extends StatelessWidget {
  const TaquinGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TaquinProvider>();
    final settings = context.watch<SettingsProvider>();
    final state = provider.gameState;

    if (state == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return PopScope(
      canPop: state.isSolved,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _showExitDialog(context, provider);
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            settings.getString("game_taquin_title").isNotEmpty
                ? settings.getString("game_taquin_title")
                : "Taquin",
          ),
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
                      primaryLabel: settings.getString("game_taquin_mode_label").isNotEmpty
                          ? settings.getString("game_taquin_mode_label").toUpperCase()
                          : "MODE",
                      primaryValue: _getModeName(state.mode),
                      secondaryLabel: settings.getString("game_taquin_moves_label").isNotEmpty
                          ? settings.getString("game_taquin_moves_label").toUpperCase()
                          : "COUPS",
                      secondaryValue: state.moves.toString(),
                      timeSeconds: state.timeSeconds,
                    ),
                    const SizedBox(height: 16),
                    Expanded(
                      child: Center(
                        child: Padding(
                          padding: const EdgeInsets.all(16.0),
                          child: LayoutBuilder(
                            builder: (context, constraints) {
                              final boardSize = constraints.biggest.shortestSide;
                              final pieceSize = boardSize / max(state.cols, state.rows);

                              return SizedBox(
                                width: pieceSize * state.cols,
                                height: pieceSize * state.rows,
                                child: GridView.builder(
                                  padding: EdgeInsets.zero,
                                  physics: const NeverScrollableScrollPhysics(),
                                  gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
                                    crossAxisCount: state.cols,
                                    childAspectRatio: 1.0,
                                  ),
                                  itemCount: state.pieces.length,
                                  itemBuilder: (context, index) {
                                    final piece = state.pieces[index];
                                    return TaquinPieceItem(
                                      piece: piece,
                                      pieceSize: pieceSize,
                                      onClick: () => provider.onPieceClicked(index),
                                    );
                                  },
                                ),
                              );
                            },
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              // Game result overlay
              if (state.isSolved)
                GameResultOverlay(
                  isVictory: true,
                  title: settings.getString("game_taquin_title").isNotEmpty
                      ? settings.getString("game_taquin_title")
                      : "Taquin",
                  stats: [
                    MapEntry(
                      settings.getString("game_taquin_moves_label").isNotEmpty
                          ? settings.getString("game_taquin_moves_label")
                          : "Coups",
                      state.moves.toString(),
                    ),
                    MapEntry(
                      settings.getString("game_taquin_time_label").isNotEmpty
                          ? settings.getString("game_taquin_time_label")
                          : "Temps",
                      _formatGameTimeHUD(state.timeSeconds),
                    ),
                  ],
                  onReplayClick: () => provider.startGame(),
                  onMenuClick: () => Navigator.pop(context),
                ),
            ],
          ),
        ),
      ),
    );
  }

  String _getModeName(TaquinMode mode) {
    switch (mode) {
      case TaquinMode.hiragana: return "Hiragana";
      case TaquinMode.katakana: return "Katakana";
      case TaquinMode.numbers: return "Chiffres";
    }
  }

  String _formatGameTimeHUD(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  void _showExitDialog(BuildContext context, TaquinProvider provider) {
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

class TaquinPieceItem extends StatefulWidget {
  final TaquinPiece piece;
  final double pieceSize;
  final VoidCallback onClick;

  const TaquinPieceItem({
    super.key,
    required this.piece,
    required this.pieceSize,
    required this.onClick,
  });

  @override
  State<TaquinPieceItem> createState() => _TaquinPieceItemState();
}

class _TaquinPieceItemState extends State<TaquinPieceItem> {
  double _dragAccumulatedX = 0.0;
  double _dragAccumulatedY = 0.0;
  static const double _dragThreshold = 24.0;

  @override
  Widget build(BuildContext context) {
    if (widget.piece.isBlank) {
      return const SizedBox.shrink();
    }

    final theme = Theme.of(context);

    return GestureDetector(
      onTap: widget.onClick,
      onPanStart: (_) {
        _dragAccumulatedX = 0.0;
        _dragAccumulatedY = 0.0;
      },
      onPanUpdate: (details) {
        _dragAccumulatedX += details.delta.dx;
        _dragAccumulatedY += details.delta.dy;
      },
      onPanEnd: (_) {
        if (_dragAccumulatedX.abs() > _dragThreshold || _dragAccumulatedY.abs() > _dragThreshold) {
          widget.onClick();
        }
      },
      child: Container(
        margin: const EdgeInsets.all(2),
        decoration: BoxDecoration(
          color: theme.colorScheme.primary,
          borderRadius: BorderRadius.circular(8),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.2),
              blurRadius: 2,
              offset: const Offset(0, 1),
            ),
          ],
        ),
        alignment: Alignment.center,
        child: Text(
          widget.piece.character,
          style: TextStyle(
            color: theme.colorScheme.onPrimary,
            fontSize: (widget.pieceSize * 0.45).clamp(16.0, 36.0),
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
