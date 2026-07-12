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
        debugPrint("[QUIZ_SCREEN] onPopInvokedWithResult called: didPop=$didPop, result=$result, isFinished=$isFinished, _canPop=$_canPop");
        if (didPop) return;
        final shouldExit = await showDialog<bool>(
          context: context,
          builder: (dialogContext) => ExitConfirmationDialog(
            onConfirm: () => Navigator.of(dialogContext).pop(true),
            onDismiss: () => Navigator.of(dialogContext).pop(false),
          ),
        );
        debugPrint("[QUIZ_SCREEN] ExitConfirmationDialog returned shouldExit: $shouldExit");
        if (shouldExit == true) {
          if (context.mounted) {
            debugPrint("[QUIZ_SCREEN] Pop confirmed. Setting _canPop = true and calling Navigator.pop");
            setState(() => _canPop = true);
            Navigator.of(context).pop();
          }
        }
      },
      child: Scaffold(
        appBar: AppBar(
          title: Text(settings.getString(engine.gameMode == "meaning" ? "game_recap_meaning" : "game_recap_reading")),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () async {
              debugPrint("[QUIZ_SCREEN] AppBar back button pressed. isFinished: $isFinished");
              if (isFinished) {
                debugPrint("[QUIZ_SCREEN] Quiz finished. Calling Navigator.pop directly.");
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
              debugPrint("[QUIZ_SCREEN] AppBar exit dialog returned shouldExit: $shouldExit");
              if (shouldExit == true) {
                if (context.mounted) {
                  debugPrint("[QUIZ_SCREEN] AppBar exit confirmed. Setting _canPop = true and popping.");
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
                            color: Colors.black.withOpacity(0.06),
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Row(
                            mainAxisSize: MainAxisSize.min,
                            children: [
                              const Icon(Icons.check, size: 16, color: Colors.green),
                              const SizedBox(width: 4),
                              Text(
                                "${provider.currentKanjiScore!.successes}",
                                style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.black87),
                              ),
                              const SizedBox(width: 16),
                              const Icon(Icons.close, size: 16, color: Colors.red),
                              const SizedBox(width: 4),
                              Text(
                                "${provider.currentKanjiScore!.failures}",
                                style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.black87),
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
                  debugPrint("[QUIZ_SCREEN] GameResultOverlay onMenuClick clicked. calling Navigator.pop");
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
    final theme = Theme.of(context);

    Color bgColor = theme.colorScheme.primary;
    Color textColor = theme.colorScheme.onPrimary;

    if (state == AnswerButtonState.correct) {
      bgColor = const Color(0xFF00E676); // Fluo Green A400
      textColor = Colors.white;
    } else if (state == AnswerButtonState.incorrect) {
      bgColor = const Color(0xFFEF5350); // Red 400
      textColor = Colors.white;
    } else if (state == AnswerButtonState.neutral) {
      bgColor = const Color(0xFF4FC3F7); // Light Blue 300
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
            elevation: 4,
          ),
          child: Text(
            answer,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: _calculateButtonFontSize(answer, engine.currentDirection),
              fontWeight: FontWeight.bold,
            ),
          ),
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

