import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'providers/recognition_quiz_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'services/level_content_provider.dart';
import 'services/recognition_game_engine.dart';
import 'services/audio_service.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_components.dart';
import 'dart:ui' as ui;

class RecognitionQuizScreen extends StatelessWidget {
  final String levelId;
  final String gameMode;
  final String readingMode;
  final int quizSize;

  const RecognitionQuizScreen({
    super.key,
    required this.levelId,
    required this.gameMode,
    required this.readingMode,
    required this.quizSize,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = RecognitionQuizProvider(
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
          context.read<LevelContentProvider>(),
          context.read<AudioService>(),
        );
        final locale = ui.PlatformDispatcher.instance.locale.toString();
        provider.initializeGame(
          levelId: levelId,
          gameMode: gameMode,
          readingMode: readingMode,
          quizSize: quizSize,
          locale: locale,
        );
        return provider;
      },
      child: const RecognitionQuizView(),
    );
  }
}

class RecognitionQuizView extends StatelessWidget {
  const RecognitionQuizView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<RecognitionQuizProvider>();
    final engine = provider.engine;

    if (engine.state == GameState.loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    if (engine.state == GameState.finished) {
      return _buildFinishedScreen(context, engine);
    }

    final isNormal = engine.currentDirection == QuestionDirection.normal;
    final questionText = isNormal 
        ? engine.currentKanji?.character ?? "" 
        : (engine.gameMode == "meaning" 
            ? engine.currentKanji?.meanings.first ?? "" 
            : engine.getFormattedReadings(engine.currentKanji!));
    
    final secondaryInfo = engine.gameMode == "meaning"
        ? engine.getFormattedReadings(engine.currentKanji!)
        : engine.currentKanji?.meanings.join(", ") ?? "";

    return Scaffold(
      appBar: AppBar(
        title: Text(engine.gameMode == "meaning" ? "Sens des Kanji" : "Lecture des Kanji"),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              GameProgressBar(statuses: engine.currentSetStatus),
              const Spacer(),
              if (engine.currentKanji != null) 
                FlippableQuestionCard(
                  frontText: questionText,
                  backText: secondaryInfo,
                  frontFontSize: isNormal ? 100 : 32,
                  backFontSize: isNormal ? 32 : 24,
                ),
              const Spacer(),
              _buildAnswerArea(context, provider),
              const SizedBox(height: 40),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildAnswerArea(BuildContext context, RecognitionQuizProvider provider) {
    final engine = provider.engine;
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        children: [
          Row(
            children: [
              _AnswerButton(index: 0, provider: provider),
              const SizedBox(width: 12),
              _AnswerButton(index: 1, provider: provider),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _AnswerButton(index: 2, provider: provider),
              const SizedBox(width: 12),
              _AnswerButton(index: 3, provider: provider),
            ],
          ),
        ],
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
              const Icon(Icons.check_circle_outline, size: 100, color: Colors.green),
              const SizedBox(height: 24),
              const Text("Bravo !", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
              const SizedBox(height: 16),
              Text("Erreurs : ${engine.errorCount}", style: const TextStyle(fontSize: 20, color: Colors.black54)),
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

class _AnswerButton extends StatelessWidget {
  final int index;
  final RecognitionQuizProvider provider;

  const _AnswerButton({required this.index, required this.provider});

  @override
  Widget build(BuildContext context) {
    final engine = provider.engine;
    if (index >= engine.currentAnswers.length) return const Expanded(child: SizedBox.shrink());

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

    return Expanded(
      child: Padding(
        padding: const EdgeInsets.all(4.0),
        child: ElevatedButton(
          onPressed: engine.state == GameState.waitingForAnswer
              ? () => provider.submitAnswer(index)
              : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: bgColor,
            disabledBackgroundColor: bgColor,
            foregroundColor: textColor,
            disabledForegroundColor: textColor,
            minimumSize: const Size(0, 120),
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
            elevation: 6,
          ),
          child: Text(
            answer,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
        ),
      ),
    );
  }
}
