import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'models/quiz_models.dart';
import 'providers/kana_quiz_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';
import 'services/audio_service.dart';
import 'widgets/mochi_background.dart';

class KanaQuizScreen extends StatelessWidget {
  final KanaType type;

  const KanaQuizScreen({super.key, required this.type});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanaQuizProvider(
          context.read<KanaRepository>(),
          context.read<ScoreRepository>(),
          context.read<AudioService>(),
        );
        provider.startQuiz(type);
        return provider;
      },
      child: const KanaQuizView(),
    );
  }
}

class KanaQuizView extends StatelessWidget {
  const KanaQuizView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaQuizProvider>();
    final engine = provider.engine;

    if (!provider.isInitialized) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (engine.state == GameState.finished) {
      return _buildFinishedScreen(context, engine);
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(provider.title),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              _buildProgressBar(engine),
              const Spacer(),
              _buildQuestionArea(engine),
              const Spacer(),
              _buildAnswerArea(context, provider, engine),
              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildProgressBar(dynamic engine) {
    double total = (engine.currentKanaSet.length > 0) ? engine.currentKanaSet.length.toDouble() : 1.0;
    double progress = 1.0 - (engine.revisionList.length / total);
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(10),
        child: LinearProgressIndicator(
          value: progress.clamp(0.0, 1.0),
          minHeight: 10,
          backgroundColor: Colors.white.withOpacity(0.3),
          valueColor: const AlwaysStoppedAnimation<Color>(Colors.pink),
        ),
      ),
    );
  }

  Widget _buildQuestionArea(dynamic engine) {
    final isNormal = engine.currentDirection == KanaQuestionDirection.normal;
    final questionText = isNormal ? engine.currentQuestion.kana : engine.currentQuestion.romaji;

    return Column(
      children: [
        Text(
          isNormal ? "Quel est ce kana ?" : "Trouvez le kana pour :",
          style: const TextStyle(fontSize: 18, color: Colors.black54),
        ),
        const SizedBox(height: 20),
        Container(
          padding: const EdgeInsets.all(40),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.9),
            shape: BoxShape.circle,
            boxShadow: [
              BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 20),
            ],
          ),
          child: Text(
            questionText,
            style: TextStyle(
              fontSize: isNormal ? 80 : 40,
              fontWeight: FontWeight.bold,
              color: Colors.pink,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAnswerArea(BuildContext context, KanaQuizProvider provider, dynamic engine) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
          crossAxisCount: 2,
          crossAxisSpacing: 16,
          mainAxisSpacing: 16,
          childAspectRatio: 2.5,
        ),
        itemCount: 4,
        itemBuilder: (context, index) {
          if (index >= engine.currentAnswers.length) return const SizedBox.shrink();

          final answer = engine.currentAnswers[index];
          final state = engine.buttonStates[index];

          Color bgColor = Colors.white;
          Color textColor = Colors.black87;

          if (state == AnswerButtonState.correct) {
            bgColor = Colors.green;
            textColor = Colors.white;
          } else if (state == AnswerButtonState.incorrect) {
            bgColor = Colors.red;
            textColor = Colors.white;
          } else if (state == AnswerButtonState.neutral) {
            bgColor = Colors.orange;
            textColor = Colors.white;
          }

          return ElevatedButton(
            onPressed: engine.state == GameState.waitingForAnswer
                ? () => provider.submitAnswer(index)
                : null,
            style: ElevatedButton.styleFrom(
              backgroundColor: bgColor,
              foregroundColor: textColor,
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              elevation: 4,
            ),
            child: Text(
              answer,
              style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            ),
          );
        },
      ),
    );
  }

  Widget _buildFinishedScreen(BuildContext context, dynamic engine) {
    return Scaffold(
      body: MochiBackground(
        child: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(Icons.emoji_events, size: 100, color: Colors.orange),
              const SizedBox(height: 24),
              const Text(
                "Session terminée !",
                style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold),
              ),
              const SizedBox(height: 16),
              Text(
                "Erreurs : ${engine.errorCount}",
                style: const TextStyle(fontSize: 20, color: Colors.black54),
              ),
              const SizedBox(height: 40),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 40, vertical: 16),
                  backgroundColor: Colors.pink,
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
