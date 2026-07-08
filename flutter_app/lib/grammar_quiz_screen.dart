import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/grammar_quiz.dart';
import 'models/quiz_models.dart';
import 'providers/grammar_quiz_provider.dart';
import 'repositories/exercise_repository.dart';
import 'repositories/score_repository.dart';
import 'services/audio_service.dart';
import 'widgets/mochi_background.dart';

class GrammarQuizScreen extends StatelessWidget {
  final List<String> grammarTags;

  const GrammarQuizScreen({super.key, required this.grammarTags});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = GrammarQuizProvider(
          exerciseRepo: context.read<ExerciseRepository>(),
          scoreRepo: context.read<ScoreRepository>(),
          audioService: context.read<AudioService>(),
          grammarTags: grammarTags,
        );
        provider.startQuiz();
        return provider;
      },
      child: const GrammarQuizView(),
    );
  }
}

class GrammarQuizView extends StatelessWidget {
  const GrammarQuizView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<GrammarQuizProvider>();

    if (provider.gameState == GameState.loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (provider.gameState == GameState.finished) {
      return _buildFinishedScreen(context, provider);
    }

    final payload = provider.currentPayload;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Quiz Grammaire"),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              _buildProgressBar(provider),
              Expanded(
                child: SingleChildScrollView(
                  padding: const EdgeInsets.all(24),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      if (payload != null) _buildQuestionCard(payload),
                      const SizedBox(height: 32),
                      _buildOptionsArea(provider),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProgressBar(GrammarQuizProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: Row(
        children: provider.progressHistory.map((status) {
          Color color = Colors.white.withOpacity(0.3);
          if (status == GameStatus.correct) color = Colors.green;
          if (status == GameStatus.incorrect) color = Colors.red;

          return Expanded(
            child: Container(
              height: 6,
              margin: const EdgeInsets.symmetric(horizontal: 2),
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(3),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildQuestionCard(ExercisePayload payload) {
    String text = "";
    if (payload is FillBlankPayload) {
      text = payload.sentence.replaceAll("___", " ____ ");
    } else if (payload is WordUsagePayload) {
      text = "Utilisation correcte de : ${payload.word}";
    } else if (payload is SentenceOrderPayload) {
      text = "${payload.prefix} ... ${payload.suffix}";
    }

    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(24.0),
        child: Text(
          text,
          textAlign: TextAlign.center,
          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.w500),
        ),
      ),
    );
  }

  Widget _buildOptionsArea(GrammarQuizProvider provider) {
    return Column(
      children: provider.currentOptions.map((option) {
        final isSelected = provider.selectedOption == option;
        final isCorrect = provider.isAnswerCorrect;

        Color color = Colors.white;
        if (isSelected) {
          color = (isCorrect ?? false) ? Colors.green.shade100 : Colors.red.shade100;
        }

        return Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: ElevatedButton(
            onPressed: provider.selectedOption == null
                ? () => provider.submitAnswer(option)
                : null,
            style: ElevatedButton.styleFrom(
              backgroundColor: color,
              minimumSize: const Size(double.infinity, 60),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            ),
            child: Text(
              option,
              style: const TextStyle(fontSize: 18, color: Colors.black87),
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildFinishedScreen(BuildContext context, GrammarQuizProvider provider) {
    return Scaffold(
      body: MochiBackground(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.auto_awesome, size: 80, color: Colors.orange),
              const SizedBox(height: 24),
              const Text("Examen terminé", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              Text("Score : ${provider.quizScore} / 10", style: const TextStyle(fontSize: 20)),
              const SizedBox(height: 40),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(backgroundColor: Colors.pink, foregroundColor: Colors.white),
                child: const Text("CONTINUER"),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
