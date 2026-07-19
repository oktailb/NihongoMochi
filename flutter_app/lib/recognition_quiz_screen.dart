import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'providers/recognition_quiz_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'services/level_content_provider.dart';
import 'services/recognition_game_engine.dart';
import 'services/audio_service.dart';
import 'services/statistics_service.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_components.dart';
import 'providers/settings_provider.dart';

class RecognitionQuizScreen extends StatelessWidget {
  final String levelId;
  final String gameMode;
  final String readingMode;
  final int quizSize;
  final List<String>? customKanjiList;

  const RecognitionQuizScreen({
    super.key,
    required this.levelId,
    required this.gameMode,
    required this.readingMode,
    required this.quizSize,
    this.customKanjiList,
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
          context.read<StatisticsService>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.initializeGame(
          levelId: levelId,
          gameMode: gameMode,
          readingMode: readingMode,
          quizSize: quizSize,
          locale: locale,
          customKanjiList: customKanjiList,
        );
        return provider;
      },
      child: RecognitionQuizView(
        levelId: levelId,
        gameMode: gameMode,
        readingMode: readingMode,
        quizSize: quizSize,
        customKanjiList: customKanjiList,
      ),
    );
  }
}

class RecognitionQuizView extends StatefulWidget {
  final String levelId;
  final String gameMode;
  final String readingMode;
  final int quizSize;
  final List<String>? customKanjiList;

  const RecognitionQuizView({
    super.key,
    required this.levelId,
    required this.gameMode,
    required this.readingMode,
    required this.quizSize,
    this.customKanjiList,
  });

  @override
  State<RecognitionQuizView> createState() => _RecognitionQuizViewState();
}

class _RecognitionQuizViewState extends State<RecognitionQuizView> {
  bool _canPop = false;

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<RecognitionQuizProvider>();
    final engine = provider.engine;
    final settings = context.watch<SettingsProvider>();

    if (engine.state == GameState.loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final isFinished = engine.state == GameState.finished;
    final isNormal = engine.currentDirection == QuestionDirection.normal;
    final questionText = !isFinished && engine.currentKanji != null
        ? (isNormal 
            ? engine.currentKanji?.character ?? "" 
            : (engine.gameMode == "meaning" 
                ? engine.currentKanji?.meanings.first ?? "" 
                : engine.getFormattedReadings(engine.currentKanji!)))
        : "";
    
    final secondaryInfo = !isFinished && engine.currentKanji != null
        ? (engine.gameMode == "meaning"
            ? engine.getFormattedReadings(engine.currentKanji!)
            : engine.currentKanji?.meanings.join(", ") ?? "")
        : "";

    return PopScope(
      canPop: isFinished || _canPop,
      onPopInvokedWithResult: (didPop, result) async {
        if (didPop) return;
        final shouldExit = await showDialog<bool>(
          context: context,
          builder: (dialogContext) => ExitConfirmationDialog(
            onConfirm: () => Navigator.of(dialogContext).pop(true),
            onDismiss: () => Navigator.of(dialogContext).pop(false),
          ),
        );
        if (shouldExit == true) {
          if (context.mounted) {
            setState(() => _canPop = true);
            Navigator.of(context).pop();
          }
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(
            settings.getString(engine.gameMode == "meaning" ? "game_recap_meaning" : "game_recap_reading"),
            style: TextStyle(color: Theme.of(context).colorScheme.onSurface),
          ),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: Icon(Icons.arrow_back, color: Theme.of(context).colorScheme.onSurface),
            onPressed: () async {
              if (isFinished) {
                Navigator.of(context).pop();
                return;
              }
              final shouldExit = await showDialog<bool>(
                context: context,
                builder: (dialogContext) => ExitConfirmationDialog(
                  onConfirm: () => Navigator.of(dialogContext).pop(true),
                  onDismiss: () => Navigator.of(dialogContext).pop(false),
                ),
              );
              if (shouldExit == true) {
                if (context.mounted) {
                  setState(() => _canPop = true);
                  Navigator.of(context).pop();
                }
              }
            },
          ),
        ),
        extendBodyBehindAppBar: true,
        body: Stack(
          children: [
            MochiBackground(
              child: SafeArea(
                child: Column(
                  children: [
                    GameProgressBar(statuses: engine.currentSetStatus),
                    const Spacer(),
                    if (!isFinished && engine.currentKanji != null) ...[
                      FlippableQuestionCard(
                        frontText: questionText,
                        backText: secondaryInfo,
                        frontFontSize: _calculateQuestionFontSize(questionText, engine.currentDirection),
                        backFontSize: _calculateQuestionFontSize(secondaryInfo, isNormal ? QuestionDirection.reverse : QuestionDirection.normal),
                      ),
                      const SizedBox(height: 16),
                      // Correct/Incorrect score capsule (shows learning score of current Kanji)
                      if (provider.currentKanjiScore != null)
                        Container(
                          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                          decoration: BoxDecoration(
                            color: Theme.of(context).colorScheme.onSurface.withValues(alpha: 0.06),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(Icons.check, size: 16, color: Colors.green),
                              const SizedBox(width: 4),
                              Text(
                                "${provider.currentKanjiScore!.successes}",
                                style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Theme.of(context).colorScheme.onSurface),
                              ),
                              const SizedBox(width: 16),
                              const Icon(Icons.close, size: 16, color: Colors.red),
                              const SizedBox(width: 4),
                              Text(
                                "${provider.currentKanjiScore!.failures}",
                                style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Theme.of(context).colorScheme.onSurface),
                              ),
                            ],
                          ),
                        ),
                    ],
                    const Spacer(),
                    if (!isFinished)
                      _buildAnswerArea(context, provider),
                    const SizedBox(height: 40),
                  ],
                ),
              ),
            ),
            if (isFinished)
              GameResultOverlay(
                isVictory: true,
                title: settings.getString("game_result_lot_mastery_recognition"),
                score: "${provider.sessionMastery}%",
                stats: [
                  MapEntry(settings.getString("game_result_title_session"), "${provider.sessionMastery}%"),
                  MapEntry(settings.getString("game_result_title_global"), "${provider.globalMastery}%"),
                  MapEntry(settings.getString("game_result_errors").replaceAll(":", ""), "${engine.errorCount}"),
                ],
                onReplayClick: () {
                  provider.initializeGame(
                    levelId: widget.levelId,
                    gameMode: widget.gameMode,
                    readingMode: widget.readingMode,
                    quizSize: widget.quizSize,
                    locale: settings.currentLocaleCode,
                    customKanjiList: widget.customKanjiList,
                  );
                },
                onMenuClick: () {
                  if (context.mounted) {
                    Navigator.of(context).pop();
                  }
                },
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildAnswerArea(BuildContext context, RecognitionQuizProvider provider) {
    return Padding(

      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        children: [
          Row(
            children: [
              _buildButton(0, provider),
              const SizedBox(width: 12),
              _buildButton(1, provider),
            ],
          ),
          const SizedBox(height: 12),
          Row(
            children: [
              _buildButton(2, provider),
              const SizedBox(width: 12),
              _buildButton(3, provider),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildButton(int index, RecognitionQuizProvider provider) {
    final engine = provider.engine;
    if (index >= engine.currentAnswers.length) return const Expanded(child: SizedBox.shrink());

    final answer = engine.currentAnswers[index];
    final state = engine.buttonStates[index];

    return Expanded(
      child: Padding(
        padding: const EdgeInsets.all(4.0),
        child: GameAnswerButton(
          text: answer,
          state: state,
          enabled: engine.state == GameState.waitingForAnswer,
          fontSize: _calculateButtonFontSize(answer, engine.currentDirection),
          onClick: () => provider.submitAnswer(index),
        ),
      ),
    );
  }
}

// Helpers for responsive font sizing matching Kotlin rules
double _calculateQuestionFontSize(String text, QuestionDirection direction) {
  if (direction == QuestionDirection.normal) {
    return 140.0;
  }
  final lineCount = '\n'.allMatches(text).length + 1;
  final textLength = text.length;
  if (lineCount > 7 || textLength > 100) return 14.0;
  if (lineCount > 5 || textLength > 70) return 18.0;
  if (lineCount > 3 || textLength > 40) return 24.0;
  if (lineCount > 1 || textLength > 15) return 32.0;
  return 48.0;
}

double _calculateButtonFontSize(String text, QuestionDirection direction) {
  if (direction == QuestionDirection.reverse) {
    return 40.0;
  }
  if (text.length > 40) return 10.0;
  if (text.length > 20) return 12.0;
  return 15.0;
}

