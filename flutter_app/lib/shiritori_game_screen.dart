import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/shiritori.dart';
import 'providers/shiritori_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';
import 'providers/settings_provider.dart';

class ShiritoriGameScreen extends StatefulWidget {
  const ShiritoriGameScreen({super.key});

  @override
  State<ShiritoriGameScreen> createState() => _ShiritoriGameScreenState();
}

class _ShiritoriGameScreenState extends State<ShiritoriGameScreen> {
  final ScrollController _scrollController = ScrollController();
  final TextEditingController _textController = TextEditingController();

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

    if (provider.playedWords.isNotEmpty) {
      _scrollToBottom();
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text("Shiritori"),
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
                primaryLabel: "SHIRITORI",
                primaryValue: "",
                secondaryLabel: "SCORE",
                secondaryValue: provider.score.toString(),
                timeSeconds: provider.gameTimeSeconds,
              ),
              const SizedBox(height: 8),
              if (provider.gameState != ShiritoriGameState.gameOver &&
                  provider.gameState != ShiritoriGameState.idle)
                _buildTargetKana(provider.lastKana),
              Expanded(
                child: ListView.builder(
                  controller: _scrollController,
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  itemCount: provider.playedWords.length + (provider.gameState == ShiritoriGameState.aiTurn ? 1 : 0),
                  itemBuilder: (context, index) {
                    if (index < provider.playedWords.length) {
                      return _buildWordBubble(provider.playedWords[index]);
                    } else {
                      return _buildTypingIndicator();
                    }
                  },
                ),
              ),
              if (provider.error != ShiritoriError.none)
                _buildErrorBadge(provider.error, provider.lastKana),
              _buildInputArea(context, provider),
              if (provider.gameState == ShiritoriGameState.gameOver)
                _buildGameOverOverlay(context, provider),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildTargetKana(String lastKana) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      decoration: BoxDecoration(
        color: Colors.blue.shade100.withOpacity(0.8),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          const Text("Suivant : ", style: TextStyle(color: Colors.black54)),
          Text(
            lastKana.isEmpty ? "?" : lastKana,
            style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold, color: Colors.blue),
          ),
        ],
      ),
    );
  }

  Widget _buildWordBubble(ShiritoriWord word) {
    final bool isPlayer = word.isPlayer;
    return Align(
      alignment: isPlayer ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 4),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isPlayer ? Colors.blue.shade100 : Colors.white.withOpacity(0.9),
          borderRadius: BorderRadius.only(
            topLeft: const Radius.circular(16),
            topRight: const Radius.circular(16),
            bottomLeft: Radius.circular(isPlayer ? 16 : 0),
            bottomRight: Radius.circular(isPlayer ? 0 : 16),
          ),
        ),
        child: Column(
          crossAxisAlignment: isPlayer ? CrossAxisAlignment.end : CrossAxisAlignment.start,
          children: [
            Text(word.phonetics, style: const TextStyle(fontSize: 10, color: Colors.black54)),
            Text(
              word.meaning.isNotEmpty ? "${word.word} (${word.meaning})" : word.word,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTypingIndicator() {
    return const Align(
      alignment: Alignment.centerLeft,
      child: Padding(
        padding: EdgeInsets.all(8.0),
        child: Text("L'IA réfléchit...", style: TextStyle(fontStyle: FontStyle.italic, color: Colors.black45)),
      ),
    );
  }

  Widget _buildErrorBadge(ShiritoriError error, String lastKana) {
    String message = "";
    switch (error) {
      case ShiritoriError.invalidStart: message = "Doit commencer par $lastKana"; break;
      case ShiritoriError.endsInN: message = "Ne doit pas finir par 'ん'"; break;
      case ShiritoriError.alreadyUsed: message = "Déjà utilisé"; break;
      case ShiritoriError.wordNotFound: message = "Mot inconnu"; break;
      default: return const SizedBox.shrink();
    }
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(8)),
      child: Text(message, style: const TextStyle(color: Colors.red, fontSize: 12, fontWeight: FontWeight.bold)),
    );
  }

  Widget _buildInputArea(BuildContext context, ShiritoriProvider provider) {
    return Container(
      padding: const EdgeInsets.all(16),
      color: Colors.white.withOpacity(0.95),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              controller: _textController,
              enabled: provider.gameState == ShiritoriGameState.playerTurn,
              decoration: InputDecoration(
                hintText: "Entrez un mot...",
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(24)),
                contentPadding: const EdgeInsets.symmetric(horizontal: 20),
              ),
              onChanged: (val) {
                provider.onInputChanged(val);
                if (_textController.text != provider.inputText) {
                  _textController.text = provider.inputText;
                  _textController.selection = TextSelection.fromPosition(TextPosition(offset: _textController.text.length));
                }
              },
              onSubmitted: (_) {
                provider.onPlayerSubmit();
                _textController.clear();
              },
            ),
          ),
          const SizedBox(width: 8),
          IconButton(
            icon: const Icon(Icons.send, color: Colors.blue),
            onPressed: provider.gameState == ShiritoriGameState.playerTurn ? () {
              provider.onPlayerSubmit();
              _textController.clear();
            } : null,
          ),
        ],
      ),
    );
  }

  Widget _buildGameOverOverlay(BuildContext context, ShiritoriProvider provider) {
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
                Icon(
                  provider.isVictory ? Icons.emoji_events : Icons.sentiment_very_dissatisfied,
                  size: 80,
                  color: provider.isVictory ? Colors.orange : Colors.red,
                ),
                const SizedBox(height: 16),
                Text(
                  provider.isVictory ? "Victoire !" : "Défaite !",
                  style: const TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 24),
                _buildStatRow("Mots trouvés", provider.score.toString()),
                _buildStatRow("Temps", "${provider.gameTimeSeconds}s"),
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

  void _showExitDialog(BuildContext context, ShiritoriProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Abandonner ?"),
        content: const Text("Voulez-vous vraiment quitter la partie ?"),
        actions: [
          TextButton(onPressed: () { provider.resumeGame(); Navigator.pop(context); }, child: const Text("NON")),
          TextButton(onPressed: () { Navigator.pop(context); Navigator.pop(context); }, child: const Text("OUI")),
        ],
      ),
    );
  }
}
