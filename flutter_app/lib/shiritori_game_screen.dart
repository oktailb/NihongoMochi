import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/shiritori.dart';
import 'providers/shiritori_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'widgets/game_components.dart';
import 'providers/settings_provider.dart';

class ShiritoriGameScreen extends StatefulWidget {
  const ShiritoriGameScreen({super.key});

  @override
  State<ShiritoriGameScreen> createState() => _ShiritoriGameScreenState();
}

class _ShiritoriGameScreenState extends State<ShiritoriGameScreen> {
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _textController = TextEditingController();
  int _lastPlayedWordsCount = 0;

  @override
  void dispose() {
    _scrollController.dispose();
    _textController.dispose();
    super.dispose();
  }

  void _scrollToBottom() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scrollController.hasClients) {
        _scrollController.animateTo(
          _scrollController.position.maxScrollExtent,
          duration: const Duration(milliseconds: 300),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ShiritoriProvider>();
    final settings = context.watch<SettingsProvider>();
    final locale = settings.currentLocaleCode;

    if (provider.playedWords.length != _lastPlayedWordsCount) {
      _lastPlayedWordsCount = provider.playedWords.length;
      _scrollToBottom();
    }

    final titleVal = settings.getString("game_shiritori_title");
    final primaryHUDLabel = (titleVal.isNotEmpty && titleVal != "game_shiritori_title") ? titleVal.toUpperCase() : "SHIRITORI";

    return PopScope(
      canPop: provider.gameState == ShiritoriGameState.gameOver,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _showExitDialog(context, provider);
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(titleVal.isNotEmpty && titleVal != "game_shiritori_title" ? titleVal : "Shiritori"),
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
                      primaryLabel: primaryHUDLabel,
                      primaryValue: "",
                      secondaryLabel: "SCORE",
                      secondaryValue: provider.score.toString(),
                      timeSeconds: provider.gameTimeSeconds,
                    ),
                    const SizedBox(height: 8),
                    if (provider.gameState != ShiritoriGameState.gameOver &&
                        provider.gameState != ShiritoriGameState.idle &&
                        provider.gameState != ShiritoriGameState.paused)
                      _buildTargetKana(context, provider.lastKana, settings),
                    Expanded(
                      child: ListView.builder(
                        controller: _scrollController,
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        itemCount: provider.playedWords.length +
                            (provider.gameState == ShiritoriGameState.aiTurn ? 1 : 0) +
                            (provider.gameState == ShiritoriGameState.loading ? 1 : 0),
                        itemBuilder: (context, index) {
                          if (index < provider.playedWords.length) {
                            return _buildWordBubble(context, provider.playedWords[index]);
                          } else if (provider.gameState == ShiritoriGameState.aiTurn &&
                              index == provider.playedWords.length) {
                            return _buildTypingIndicator(context, settings);
                          } else {
                            return const Center(
                              child: Padding(
                                padding: EdgeInsets.all(16.0),
                                child: CircularProgressIndicator(),
                              ),
                            );
                          }
                        },
                      ),
                    ),
                    if (provider.error != ShiritoriError.none)
                      _buildErrorBadge(context, provider.error, provider.lastKana, settings),
                    _buildInputArea(context, provider, settings),
                  ],
                ),
              ),
              if (provider.gameState == ShiritoriGameState.gameOver)
                GameResultOverlay(
                  isVictory: provider.isVictory,
                  title: provider.isVictory
                      ? settings.getString("game_victory_title")
                      : settings.getString("game_defeat_title"),
                  score: provider.score.toString(),
                  bestScore: "${settings.getString("game_best_score_label")} : ${provider.bestScore}",
                  stats: [
                    MapEntry(settings.getString("shiritori_words_found"), provider.score.toString()),
                    MapEntry(settings.getString("shiritori_time"), _formatGameTimeHUD(provider.gameTimeSeconds)),
                  ],
                  onReplayClick: () {
                    provider.startGame(locale);
                  },
                  onMenuClick: () {
                    provider.resetToIdle();
                    Navigator.pop(context);
                  },
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTargetKana(BuildContext context, String lastKana, SettingsProvider settings) {
    final theme = Theme.of(context);
    return Center(
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 8),
        padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
        decoration: BoxDecoration(
          color: theme.colorScheme.secondaryContainer.withValues(alpha: 0.8),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              settings.getString("shiritori_next"),
              style: TextStyle(color: theme.colorScheme.onSecondaryContainer, fontSize: 14),
            ),
            Text(
              lastKana.isEmpty ? "?" : lastKana,
              style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildWordBubble(BuildContext context, ShiritoriWord word) {
    final theme = Theme.of(context);
    final bool isPlayer = word.isPlayer;
    final bubbleColor = isPlayer ? theme.colorScheme.primaryContainer : theme.colorScheme.secondaryContainer;
    final textColor = isPlayer ? theme.colorScheme.onPrimaryContainer : theme.colorScheme.onSecondaryContainer;

    return Align(
      alignment: isPlayer ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: bubbleColor,
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(16),
            topRight: const Radius.circular(16),
            bottomLeft: Radius.circular(isPlayer ? 16 : 0),
            bottomRight: Radius.circular(isPlayer ? 0 : 16),
          ),
        ),
        child: Column(
          crossAxisAlignment: isPlayer ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              word.phonetics,
              style: TextStyle(fontSize: 11, color: textColor.withValues(alpha: 0.7)),
            ),
            Text(
              word.meaning.isNotEmpty ? "${word.word} (${word.meaning})" : word.word,
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: textColor),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTypingIndicator(BuildContext context, SettingsProvider settings) {
    final theme = Theme.of(context);
    return Align(
      alignment: Alignment.centerLeft,
      child: Padding(
        padding: const EdgeInsets.all(8.0),
        child: Text(
          settings.getString("shiritori_ia_sink"),
          style: TextStyle(fontStyle: FontStyle.italic, color: theme.colorScheme.onSurface.withValues(alpha: 0.5)),
        ),
      ),
    );
  }

  Widget _buildErrorBadge(BuildContext context, ShiritoriError error, String lastKana, SettingsProvider settings) {
    final theme = Theme.of(context);
    String message = "";
    switch (error) {
      case ShiritoriError.invalidStart:
        message = "${settings.getString("shiritori_starts_by")} $lastKana";
        break;
      case ShiritoriError.endsInN:
        message = settings.getString("shiritori_finishes_by");
        break;
      case ShiritoriError.alreadyUsed:
        message = settings.getString("shiritori_taken");
        break;
      case ShiritoriError.wordNotFound:
        message = settings.getString("shiritori_unknown_word");
        break;
      default:
        return const SizedBox.shrink();
    }
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(
        color: theme.colorScheme.errorContainer,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(
        message,
        style: TextStyle(color: theme.colorScheme.onErrorContainer, fontSize: 12, fontWeight: FontWeight.bold),
      ),
    );
  }

  Widget _buildInputArea(BuildContext context, ShiritoriProvider provider, SettingsProvider settings) {
    final theme = Theme.of(context);
    return Container(
      padding: EdgeInsets.only(
        left: 16,
        right: 16,
        top: 16,
        bottom: 16 + MediaQuery.of(context).viewInsets.bottom,
      ),
      color: theme.colorScheme.surface.withValues(alpha: 0.95),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _textController,
              enabled: provider.gameState == ShiritoriGameState.playerTurn,
              decoration: InputDecoration(
                hintText: settings.getString("shiritori_typing"),
                hintStyle: TextStyle(color: theme.colorScheme.onSurface.withValues(alpha: 0.5)),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(24),
                  borderSide: BorderSide.none,
                ),
                filled: true,
                fillColor: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.5),
                contentPadding: const EdgeInsets.symmetric(horizontal: 20),
              ),
              onChanged: (val) {
                provider.onInputChanged(val);
                if (_textController.text != provider.inputText) {
                  _textController.text = provider.inputText;
                  _textController.selection = TextSelection.fromPosition(
                    TextPosition(offset: _textController.text.length),
                  );
                }
              },
              onSubmitted: (_) {
                if (provider.inputText.trim().isNotEmpty) {
                  provider.onPlayerSubmit();
                  _textController.clear();
                }
              },
            ),
          ),
          const SizedBox(width: 8),
          FloatingActionButton(
            onPressed: provider.gameState == ShiritoriGameState.playerTurn &&
                    provider.inputText.trim().isNotEmpty
                ? () {
                    provider.onPlayerSubmit();
                    _textController.clear();
                  }
                : null,
            mini: true,
            elevation: 0,
            highlightElevation: 0,
            backgroundColor: provider.gameState == ShiritoriGameState.playerTurn &&
                    provider.inputText.trim().isNotEmpty
                ? theme.colorScheme.primary
                : theme.colorScheme.primary.withValues(alpha: 0.3),
            foregroundColor: theme.colorScheme.onPrimary,
            shape: const CircleBorder(),
            child: const Icon(Icons.send),
          ),
        ],
      ),
    );
  }

  String _formatGameTimeHUD(int seconds) {
    final m = seconds ~/ 60;
    final s = seconds % 60;
    return m > 0 ? "${m}m ${s}s" : "${s}s";
  }

  void _showExitDialog(BuildContext context, ShiritoriProvider provider) {
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
