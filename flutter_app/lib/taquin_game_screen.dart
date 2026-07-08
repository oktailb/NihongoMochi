import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/taquin.dart';
import 'providers/taquin_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';

class TaquinGameScreen extends StatelessWidget {
  const TaquinGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<TaquinProvider>();
    final state = provider.gameState;

    if (state == null) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text("Taquin"),
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
                primaryLabel: "MODE",
                primaryValue: _getModeName(state.mode),
                secondaryLabel: "COUPS",
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
              if (state.isSolved)
                _buildSolvedOverlay(context, provider),
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

  Widget _buildSolvedOverlay(BuildContext context, TaquinProvider provider) {
    final state = provider.gameState!;
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
                  "Félicitations !",
                  style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 24),
                _buildStatRow("Coups", state.moves.toString()),
                _buildStatRow("Temps", "${state.timeSeconds}s"),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text("MENU"),
                    ),
                    ElevatedButton(
                      onPressed: () => provider.startGame(),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.pink,
                        foregroundColor: Colors.white,
                      ),
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

  void _showExitDialog(BuildContext context, TaquinProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Abandonner ?"),
        content: const Text("Voulez-vous vraiment quitter la partie ?"),
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

class TaquinPieceItem extends StatelessWidget {
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
  Widget build(BuildContext context) {
    if (piece.isBlank) {
      return const SizedBox.shrink();
    }

    return GestureDetector(
      onTap: onClick,
      child: Container(
        margin: const EdgeInsets.all(2),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primary,
          borderRadius: BorderRadius.circular(8),
          boxShadow: [
            BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 2, offset: const Offset(0, 1)),
          ],
        ),
        alignment: Alignment.center,
        child: Text(
          piece.character,
          style: TextStyle(
            color: Theme.of(context).colorScheme.onPrimary,
            fontSize: pieceSize * 0.5,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}
