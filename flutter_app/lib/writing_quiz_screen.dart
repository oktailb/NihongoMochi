import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'providers/writing_quiz_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'utils/romaji_to_kana.dart';
import 'widgets/mochi_background.dart';
import 'providers/settings_provider.dart';

import 'services/level_content_provider.dart';

class WritingQuizScreen extends StatelessWidget {
  final String levelId;

  const WritingQuizScreen({super.key, required this.levelId});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WritingQuizProvider(
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
          context.read<LevelContentProvider>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.startQuiz(levelId, locale);
        return provider;
      },
      child: const WritingQuizView(),
    );
  }
}

class WritingQuizView extends StatefulWidget {
  const WritingQuizView({super.key});

  @override
  State<WritingQuizView> createState() => _WritingQuizViewState();
}

class _WritingQuizViewState extends State<WritingQuizView> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();

  @override
  void dispose() {
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WritingQuizProvider>();

    if (provider.state == GameState.loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (provider.state == GameState.finished) {
      return _buildFinishedScreen(context, provider);
    }

    // Request focus on new question
    if (provider.state == GameState.waitingForAnswer && !_focusNode.hasFocus) {
      _focusNode.requestFocus();
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text("Quiz Écriture"),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Stack(
            children: [
              Column(
                children: [
                  _buildProgressBar(provider),
                  Expanded(
                    child: Center(
                      child: _buildKanjiCard(provider.currentKanji?.character ?? ""),
                    ),
                  ),
                  _buildInputArea(context, provider),
                ],
              ),
              if (provider.showCorrection)
                Center(
                  child: _buildCorrectionCard(provider),
                ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProgressBar(WritingQuizProvider provider) {
    double progress = provider.currentSet.isEmpty
        ? 0
        : 1.0 - (provider.errorCount / 10); // Simplified for UI
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: LinearProgressIndicator(
        value: progress.clamp(0.0, 1.0),
        minHeight: 8,
        borderRadius: BorderRadius.circular(10),
        backgroundColor: Colors.white.withOpacity(0.3),
        valueColor: const AlwaysStoppedAnimation<Color>(Colors.purple),
      ),
    );
  }

  Widget _buildKanjiCard(String character) {
    return Card(
      elevation: 12,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Container(
        width: 250,
        height: 250,
        alignment: Alignment.center,
        child: Text(
          character,
          style: const TextStyle(
            fontSize: 140,
            fontFamily: 'KanjiStrokeOrders',
            color: Colors.purple,
          ),
        ),
      ),
    );
  }

  Widget _buildInputArea(BuildContext context, WritingQuizProvider provider) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.9),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Text(
            provider.currentQuestionType == QuestionType.meaning
                ? "Quel est le SENS de ce kanji ?"
                : "Quelle est la LECTURE de ce kanji ?",
            style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.purple),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: _controller,
            focusNode: _focusNode,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 24),
            decoration: InputDecoration(
              hintText: provider.currentQuestionType == QuestionType.reading ? "romaji -> kana" : "votre réponse",
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              filled: true,
              fillColor: Colors.grey.shade100,
            ),
            enabled: !provider.isProcessing,
            onChanged: (val) {
              if (provider.currentQuestionType == QuestionType.reading) {
                final replacement = RomajiToKana.checkReplacement(val);
                if (replacement != null) {
                  final entry = replacement.entries.first;
                  final prefix = val.substring(0, val.length - entry.key);
                  _controller.text = prefix + entry.value;
                  _controller.selection = TextSelection.fromPosition(TextPosition(offset: _controller.text.length));
                }
              }
            },
            onSubmitted: (val) {
              if (val.isNotEmpty) {
                provider.submitAnswer(val);
                _controller.clear();
              }
            },
          ),
          const SizedBox(height: 12),
          ElevatedButton(
            onPressed: provider.isProcessing ? null : () {
              if (_controller.text.isNotEmpty) {
                provider.submitAnswer(_controller.text);
                _controller.clear();
              }
            },
            style: ElevatedButton.styleFrom(
              minimumSize: const Size(double.infinity, 50),
              backgroundColor: Colors.purple,
              foregroundColor: Colors.white,
            ),
            child: const Text("VALIDER"),
          ),
        ],
      ),
    );
  }

  Widget _buildCorrectionCard(WritingQuizProvider provider) {
    final kanji = provider.currentKanji!;
    return Card(
      color: Colors.red.shade50,
      margin: const EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12), side: BorderSide(color: Colors.red.shade200)),
      child: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text("CORRECTION", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.red)),
            const SizedBox(height: 12),
            Text(
              "Sens : ${kanji.meanings.join(", ")}",
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 18),
            ),
            const SizedBox(height: 8),
            Text(
              "Lectures : ${kanji.readings.map((r) => r.text).join(", ")}",
              textAlign: TextAlign.center,
              style: const TextStyle(fontSize: 16, color: Colors.black54),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildFinishedScreen(BuildContext context, WritingQuizProvider provider) {
    return Scaffold(
      body: MochiBackground(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.edit_note, size: 100, color: Colors.purple),
              const SizedBox(height: 24),
              const Text("Quiz Terminé !", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
              const SizedBox(height: 40),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.purple, foregroundColor: Colors.white),
                child: const Text("RETOUR"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
