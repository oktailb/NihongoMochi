import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana_link.dart';
import 'providers/kana_link_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';

class KanaLinkGameScreen extends StatelessWidget {
  const KanaLinkGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaLinkProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text("Kana Link"),
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
              : Column(
                  children: [
                    GameHUD(
                      primaryLabel: "KANA LINK",
                      primaryValue: "",
                      secondaryLabel: "SCORE",
                      secondaryValue: provider.score.toString(),
                      timeSeconds: provider.timeRemaining,
                    ),
                    _buildCurrentWordArea(provider),
                    Expanded(child: _buildGrid(context, provider)),
                    if (provider.isGameOver)
                      _buildGameOverOverlay(context, provider),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildCurrentWordArea(KanaLinkProvider provider) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 16),
      padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.blue.shade100.withOpacity(0.8),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        provider.currentWord.isEmpty ? " " : provider.currentWord,
        style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold, color: Colors.blue),
      ),
    );
  }

  Widget _buildGrid(BuildContext context, KanaLinkProvider provider) {
    return LayoutBuilder(
      builder: (context, constraints) {
        final double cellSize = constraints.maxWidth / provider.grid[0].length;

        return GestureDetector(
          onPanStart: (details) => _handleTouch(details.localPosition, cellSize, provider),
          onPanUpdate: (details) => _handleTouch(details.localPosition, cellSize, provider),
          onPanEnd: (_) => provider.onReleaseSelection(),
          child: Container(
            color: Colors.transparent,
            child: Stack(
              children: provider.grid.expand((row) => row).map((cell) {
                return AnimatedPositioned(
                  duration: const Duration(milliseconds: 300),
                  left: cell.col * cellSize,
                  top: cell.row * cellSize,
                  width: cellSize,
                  height: cellSize,
                  child: _buildCellItem(cell, cellSize, provider),
                );
              }).toList(),
            ),
          ),
        );
      },
    );
  }

  void _handleTouch(Offset localPos, double cellSize, KanaLinkProvider provider) {
    final int col = (localPos.dx / cellSize).floor();
    final int row = (localPos.dy / cellSize).floor();

    if (row >= 0 && row < provider.grid.length && col >= 0 && col < provider.grid[0].length) {
      provider.onCellTouched(row, col);
    }
  }

  Widget _buildCellItem(KanaLinkCell cell, double size, KanaLinkProvider provider) {
    final isSelected = provider.selectedCells.any((c) => c.id == cell.id);

    return Container(
      padding: const EdgeInsets.all(2),
      child: Opacity(
        opacity: cell.isMatched ? 0.0 : 1.0,
        child: Container(
          decoration: BoxDecoration(
            color: isSelected ? Colors.blue : Colors.white.withOpacity(0.9),
            borderRadius: BorderRadius.circular(8),
            boxShadow: [
              BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 2),
            ],
          ),
          alignment: Alignment.center,
          child: Text(
            cell.char,
            style: TextStyle(
              fontSize: size * 0.5,
              fontWeight: FontWeight.bold,
              color: isSelected ? Colors.white : Colors.black87,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildGameOverOverlay(BuildContext context, KanaLinkProvider provider) {
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
                const Icon(Icons.timer_off, size: 80, color: Colors.orange),
                const SizedBox(height: 16),
                const Text("Temps écoulé !", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
                const SizedBox(height: 24),
                _buildStatRow("Score", provider.score.toString()),
                _buildStatRow("Mots trouvés", provider.wordsFound.toString()),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(
                      onPressed: () => Navigator.pop(context),
                      child: const Text("MENU"),
                    ),
                    ElevatedButton(
                      onPressed: () => provider.initGame("n5"),
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

  void _showExitDialog(BuildContext context, KanaLinkProvider provider) {
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
