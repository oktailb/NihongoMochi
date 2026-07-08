import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/crossword.dart';
import 'models/quiz_models.dart';
import 'providers/crossword_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';

class CrosswordGameScreen extends StatelessWidget {
  const CrosswordGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<CrosswordProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text("Mots Croisés"),
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
                primaryLabel: "MOCHI CROSS",
                primaryValue: "",
                secondaryLabel: "TEMPS",
                secondaryValue: "",
                timeSeconds: provider.gameTimeSeconds,
              ),
              Expanded(
                child: InteractiveViewer(
                  boundaryMargin: const EdgeInsets.all(100),
                  minScale: 0.5,
                  maxScale: 3.0,
                  child: Center(
                    child: _buildGrid(provider),
                  ),
                ),
              ),
              _buildClueArea(provider),
              _buildKeyboard(provider),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildGrid(CrosswordProvider provider) {
    const double cellSize = 40.0;
    const int gridSize = 16;

    return Container(
      width: gridSize * cellSize,
      height: gridSize * cellSize,
      color: Colors.grey.shade300,
      child: Stack(
        children: provider.cells.map((cell) {
          if (cell.isBlack) return const SizedBox.shrink();

          final isSelected = provider.selectedRow == cell.r && provider.selectedCol == cell.c;

          return Positioned(
            left: cell.c * cellSize,
            top: cell.r * cellSize,
            width: cellSize,
            height: cellSize,
            child: GestureDetector(
              onTap: () => provider.onCellSelected(cell.r, cell.c),
              child: Container(
                margin: const EdgeInsets.all(0.5),
                decoration: BoxDecoration(
                  color: cell.isCorrect
                      ? Colors.green.shade100
                      : (isSelected ? Colors.blue.shade100 : Colors.white),
                  border: Border.all(
                    color: isSelected ? Colors.blue : Colors.grey.shade400,
                    width: isSelected ? 2 : 0.5,
                  ),
                ),
                child: Stack(
                  children: [
                    if (cell.number != null)
                      Positioned(
                        top: 2,
                        left: 2,
                        child: Text(
                          cell.number.toString(),
                          style: const TextStyle(fontSize: 8, fontWeight: FontWeight.bold),
                        ),
                      ),
                    Center(
                      child: Text(
                        cell.userInput,
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                          color: cell.isCorrect ? Colors.green : Colors.black,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildClueArea(CrosswordProvider provider) {
    if (provider.selectedRow == null) return const SizedBox.shrink();

    // Trouver le mot actif pour afficher l'indice
    final words = provider.placedWords.where((w) {
      if (w.isHorizontal) {
        return w.row == provider.selectedRow && provider.selectedCol! >= w.col && provider.selectedCol! < w.col + w.word.length;
      } else {
        return w.col == provider.selectedCol && provider.selectedRow! >= w.row && provider.selectedRow! < w.row + w.word.length;
      }
    }).toList();

    if (words.isEmpty) return const SizedBox.shrink();
    final activeWord = provider.isVerticalInput
        ? (words.firstWhere((w) => !w.isHorizontal, orElse: () => words.first))
        : (words.firstWhere((w) => w.isHorizontal, orElse: () => words.first));

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.9),
        borderRadius: BorderRadius.circular(12),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4)],
      ),
      child: Column(
        children: [
          Text(
            provider.selectedMode == CrosswordMode.kanjis ? "Lecture & Sens" : "Kanji & Sens",
            style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold, color: Colors.grey),
          ),
          const SizedBox(height: 4),
          Text(
            provider.selectedMode == CrosswordMode.kanjis
                ? "${activeWord.phonetics} (${activeWord.meaning})"
                : "${activeWord.kanji} (${activeWord.meaning})",
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.blue),
          ),
        ],
      ),
    );
  }

  Widget _buildKeyboard(CrosswordProvider provider) {
    if (provider.selectedRow == null || provider.isFinished) return const SizedBox.shrink();

    return Container(
      padding: const EdgeInsets.all(8),
      color: Colors.white.withOpacity(0.95),
      child: Row(
        children: [
          Expanded(
            child: Wrap(
              spacing: 4,
              runSpacing: 4,
              alignment: WrapAlignment.center,
              children: provider.keyboardKeys.map((key) {
                return SizedBox(
                  width: 50,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: () => provider.onKeyTyped(key),
                    style: ElevatedButton.styleFrom(
                      padding: EdgeInsets.zero,
                      backgroundColor: Colors.blue.shade50,
                      foregroundColor: Colors.black87,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    ),
                    child: Text(key, style: const TextStyle(fontSize: 20)),
                  ),
                );
              }).toList(),
            ),
          ),
          Container(
            width: 60,
            height: 110,
            padding: const EdgeInsets.only(left: 8),
            child: ElevatedButton(
              onPressed: () => provider.onKeyTyped(""), // Delete
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.red.shade100,
                foregroundColor: Colors.red,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: const Icon(Icons.backspace_outlined),
            ),
          ),
        ],
      ),
    );
  }

  void _showExitDialog(BuildContext context, CrosswordProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Quitter ?"),
        content: const Text("Voulez-vous enregistrer et quitter ?"),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: const Text("ANNULER")),
          TextButton(
            onPressed: () {
              Navigator.pop(context); // close dialog
              Navigator.pop(context); // exit screen
            },
            child: const Text("QUITTER SANS SAUVER"),
          ),
          ElevatedButton(
            onPressed: () {
              // TODO: Save logic
              Navigator.pop(context);
              Navigator.pop(context);
            },
            child: const Text("SAUVER ET QUITTER"),
          ),
        ],
      ),
    );
  }
}
