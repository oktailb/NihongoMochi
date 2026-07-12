import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/crossword.dart';
import 'models/quiz_models.dart';
import 'providers/crossword_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';
import 'providers/settings_provider.dart';

class CrosswordGameScreen extends StatelessWidget {
  const CrosswordGameScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<CrosswordProvider>();
    final settings = context.watch<SettingsProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text(settings.getString("game_crosswords_title")),
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
                    primaryLabel: settings.getString("game_crosswords_title").toUpperCase(),
                    primaryValue: "",
                    secondaryLabel: settings.getString("game_kana_link_time_label").toUpperCase(),
                    secondaryValue: "",
                    timeSeconds: provider.gameTimeSeconds,
                  ),
                  Expanded(
                    child: InteractiveViewer(
                      boundaryMargin: const EdgeInsets.all(100),
                      minScale: 0.5,
                      maxScale: 3.0,
                      child: Center(
                        child: _buildGrid(context, provider),
                      ),
                    ),
                  ),
                  _buildClueArea(context, provider),
                  _buildKeyboard(context, provider),
                ],
              ),
            ),
            if (provider.isFinished)
              GameResultOverlay(
                isVictory: true,
                title: settings.getString("game_crossword_congrats"),
                stats: [
                  MapEntry(
                    settings.getString("game_crossword_history_item", [
                      provider.placedWords.length,
                      provider.selectedMode == CrosswordMode.kanjis ? "KANJI" : "KANA/MEANING"
                    ]),
                    _formatGameTimeHUD(provider.gameTimeSeconds),
                  ),
                ],
                onReplayClick: () => provider.startGame("n5"),
                onMenuClick: () => Navigator.pop(context),
              ),
          ],
        ),
      ),
    );
  }

  String _formatGameTimeHUD(int seconds) {
    final int m = seconds ~/ 60;
    final int s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  Widget _buildGrid(BuildContext context, CrosswordProvider provider) {
    const double cellSize = 40.0;
    const int gridSize = 16;
    final theme = Theme.of(context);

    return Container(
      width: gridSize * cellSize,
      height: gridSize * cellSize,
      color: theme.colorScheme.onSurface.withOpacity(0.1),
      child: Stack(
        children: provider.cells.map((cell) {
          if (cell.isBlack) return const SizedBox.shrink();

          final isSelected = provider.selectedRow == cell.r && provider.selectedCol == cell.c;
          final isInputCorrect = cell.userInput.isNotEmpty && cell.userInput == cell.solution;
          final isInputWrong = cell.userInput.isNotEmpty && cell.userInput != cell.solution;

          final Color backgroundColor = isInputCorrect || cell.isCorrect
              ? const Color(0xFFC8E6C9)
              : (isInputWrong
                  ? const Color(0xFFFFCDD2)
                  : (isSelected ? theme.colorScheme.primaryContainer : Colors.white));

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
                  color: backgroundColor,
                  border: Border.all(
                    color: isSelected ? theme.colorScheme.primary : Colors.grey.withOpacity(0.5),
                    width: isSelected ? 1.5 : 0.5,
                  ),
                  borderRadius: BorderRadius.circular(2),
                ),
                child: Stack(
                  children: [
                    if (cell.number != null)
                      Positioned(
                        top: 2,
                        left: 2,
                        child: Text(
                          cell.number.toString(),
                          style: const TextStyle(fontSize: 8, fontWeight: FontWeight.bold, color: Colors.black54),
                        ),
                      ),
                    Center(
                      child: Text(
                        cell.userInput,
                        style: TextStyle(
                          fontSize: 18,
                          fontWeight: FontWeight.bold,
                          color: isInputCorrect || cell.isCorrect
                              ? const Color(0xFF2E7D32)
                              : (isInputWrong ? const Color(0xFFD32F2F) : Colors.black),
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

  Widget _buildClueArea(BuildContext context, CrosswordProvider provider) {
    if (provider.selectedRow == null) return const SizedBox.shrink();
    final theme = Theme.of(context);

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

    final String clueText = provider.selectedMode == CrosswordMode.kanjis
        ? "${activeWord.phonetics} (${activeWord.meaning})"
        : "${activeWord.kanji} (${activeWord.meaning})";

    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: theme.colorScheme.secondaryContainer,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4)],
      ),
      child: Text(
        clueText,
        textAlign: TextAlign.center,
        style: TextStyle(
          fontSize: provider.selectedMode == CrosswordMode.kanjis ? 22 : 18,
          fontWeight: FontWeight.bold,
          color: provider.selectedMode == CrosswordMode.kanjis ? theme.colorScheme.primary : theme.colorScheme.onSecondaryContainer,
        ),
      ),
    );
  }

  Widget _buildKeyboard(BuildContext context, CrosswordProvider provider) {
    if (provider.selectedRow == null || provider.isFinished) return const SizedBox.shrink();
    final theme = Theme.of(context);

    return Container(
      padding: const EdgeInsets.all(8),
      color: theme.colorScheme.surface.withOpacity(0.95),
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
                      backgroundColor: theme.colorScheme.primaryContainer,
                      foregroundColor: theme.colorScheme.onPrimaryContainer,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                    ),
                    child: Text(key, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w500)),
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
                backgroundColor: theme.colorScheme.errorContainer,
                foregroundColor: theme.colorScheme.onErrorContainer,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
              ),
              child: const Text("⌫", style: TextStyle(fontSize: 24)),
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
      builder: (context) => ExitConfirmationDialog(
        onConfirm: () {
          Navigator.pop(context); // close dialog
          provider.abandonGame();
          Navigator.pop(context); // exit screen
        },
        onDismiss: () {
          provider.resumeGame();
          Navigator.pop(context); // close dialog
        },
        onPause: provider.pauseGame,
        onResume: provider.resumeGame,
        onSaveAndExit: () {
          Navigator.pop(context); // close dialog
          provider.saveAndExit();
          Navigator.pop(context); // exit screen
        },
      ),
    );
  }
}
