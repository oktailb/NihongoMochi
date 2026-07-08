import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'providers/word_quiz_provider.dart';
import 'repositories/word_repository.dart';
import 'repositories/score_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'widgets/mochi_background.dart';
import 'dart:ui' as ui;

class WordQuizScreen extends StatelessWidget {
  final String levelId;

  const WordQuizScreen({super.key, required this.levelId});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WordQuizProvider(
          context.read<WordRepository>(),
          context.read<ScoreRepository>(),
          context.read<WordMeaningRepository>(),
        );
        final locale = ui.PlatformDispatcher.instance.locale.toString();
        provider.initializeGame(levelId, locale);
        return provider;
      },
      child: const WordQuizView(),
    );
  }
}

class WordQuizView extends StatelessWidget {
  const WordQuizView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WordQuizProvider>();
    final engine = provider.engine;

    if (!provider.isInitialized) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (provider.state == GameState.finished) {
      return _buildFinishedScreen(context, provider);
    }

    final currentWord = provider.currentWord;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Quiz Lecture"),
        backgroundColor: Colors.transparent,
        elevation: 0,
        actions: [
          IconButton(
            icon: const Icon(Icons.volume_up),
            onPressed: () => provider.speak(),
          ),
        ],
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              _buildProgressBar(provider),
              const Spacer(),
              if (currentWord != null) _buildQuestionArea(currentWord),
              const Spacer(),
              _buildAnswerArea(context, provider),
              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProgressBar(WordQuizProvider provider) {
    final engine = provider.engine;
    double progress = engine.currentWordSet.isEmpty
        ? 0
        : 1.0 - (engine.revisionList.length / engine.currentWordSet.length);

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: Column(
        children: [
          ClipRRect(
            borderRadius: BorderRadius.circular(10),
            child: LinearProgressIndicator(
              value: progress,
              minHeight: 8,
              backgroundColor: Colors.white.withOpacity(0.3),
              valueColor: const AlwaysStoppedAnimation<Color>(Colors.blue),
            ),
          ),
          const SizedBox(height: 4),
          Text(
            "Mots restants : ${engine.revisionList.length}",
            style: const TextStyle(fontSize: 10, color: Colors.black45),
          ),
        ],
      ),
    );
  }

  Widget _buildQuestionArea(WordQuizItem word) {
    return Column(
      children: [
        const Text(
          "Comment se lit ce mot ?",
          style: TextStyle(fontSize: 18, color: Colors.black54),
        ),
        const SizedBox(height: 24),
        Card(
          elevation: 8,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 30),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text(
              word.text,
              style: const TextStyle(
                fontSize: 56,
                fontWeight: FontWeight.bold,
                color: Colors.blue,
              ),
            ),
          ),
        ),
        if (word.meaning != null) ...[
          const SizedBox(height: 16),
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 32),
            child: Text(
              word.meaning!,
              textAlign: TextAlign.center,
              style: TextStyle(fontSize: 16, color: Colors.grey.shade700, fontStyle: FontStyle.italic),
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildAnswerArea(BuildContext context, WordQuizProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 1,
          mainAxisSpacing: 12,
          childAspectRatio: 5,
        ),
        itemCount: provider.currentAnswers.length,
        itemBuilder: (context, index) {
          final answer = provider.currentAnswers[index];
          final state = provider.buttonStates[index];

          Color bgColor = Colors.white;
          Color textColor = Colors.black87;

          if (state == AnswerButtonState.correct) {
            bgColor = Colors.green;
            textColor = Colors.white;
          } else if (state == AnswerButtonState.incorrect) {
            bgColor = Colors.red;
            textColor = Colors.white;
          }

          return ElevatedButton(
            onPressed: provider.state == GameState.waitingForAnswer
                ? () => provider.submitAnswer(index)
                : null,
            style: ElevatedButton.styleFrom(
              backgroundColor: bgColor,
              foregroundColor: textColor,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(15)),
              elevation: 2,
            ),
            child: Text(
              answer,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w500),
            ),
          );
        },
      ),
    );
  }

  Widget _buildFinishedScreen(BuildContext context, WordQuizProvider provider) {
    return Scaffold(
      body: MochiBackground(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.check_circle_outline, size: 100, color: Colors.green),
              const SizedBox(height: 24),
              const Text(
                "Bravo !",
                style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              Text(
                "Session terminée avec ${provider.errorCount} erreurs.",
                style: const TextStyle(fontSize: 18, color: Colors.black54),
              ),
              const SizedBox(height: 40),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 50, vertical: 16),
                  backgroundColor: Colors.blue,
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(30)),
                ),
                child: const Text("RETOUR", style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
