import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'providers/word_quiz_provider.dart';
import 'repositories/word_repository.dart';
import 'repositories/score_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'widgets/mochi_background.dart';
import 'providers/settings_provider.dart';
import 'services/audio_service.dart';
import 'services/statistics_service.dart';
import 'widgets/game_components.dart';

class WordQuizScreen extends StatelessWidget {
  final String levelId;
  final List<String>? customWordList;

  const WordQuizScreen({
    super.key,
    required this.levelId,
    this.customWordList,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WordQuizProvider(
          context.read<WordRepository>(),
          context.read<ScoreRepository>(),
          context.read<WordMeaningRepository>(),
          context.read<AudioService>(),
          context.read<StatisticsService>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.initializeGame(levelId, locale, customWordList: customWordList);
        return provider;
      },
      child: WordQuizView(
        levelId: levelId,
        customWordList: customWordList,
      ),
    );
  }
}

class WordQuizView extends StatefulWidget {
  final String levelId;
  final List<String>? customWordList;

  const WordQuizView({
    super.key,
    required this.levelId,
    this.customWordList,
  });

  @override
  State<WordQuizView> createState() => _WordQuizViewState();
}

class _WordQuizViewState extends State<WordQuizView> {
  bool _canPop = false;

  @override
  void initState() {
    super.initState();
  }


  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WordQuizProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    if (!provider.isInitialized) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final isFinished = provider.state == GameState.finished;
    final currentWord = provider.currentWord;

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
          title: Text(settings.getString("game_recap_reading")),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
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
          actions: [
            if (!isFinished && currentWord != null)
              IconButton(
                icon: const Icon(Icons.volume_up),
                onPressed: () => provider.speak(),
              ),
          ],
        ),
        extendBodyBehindAppBar: true,
        body: Stack(
          children: [
            MochiBackground(
              child: SafeArea(
                child: Column(
                  children: [
                    GameProgressBar(statuses: provider.currentSetStatus),
                    if (!isFinished && currentWord != null) ...[
                      const Spacer(),
                      _buildQuestionArea(context, theme, settings, currentWord),
                      const Spacer(),
                      _buildAnswerArea(context, theme, provider),
                      const SizedBox(height: 40),
                    ] else
                      const Spacer(),
                  ],
                ),
              ),
            ),
            if (isFinished)
              GameResultOverlay(
                isVictory: true,
                title: settings.getString("game_result_lot_mastery_reading").isNotEmpty
                    ? settings.getString("game_result_lot_mastery_reading")
                    : "Maîtrise de la lecture",
                score: "${provider.sessionMastery}%",
                stats: [
                  MapEntry(settings.getString("game_result_title_session"), "${provider.sessionMastery}%"),
                  MapEntry(settings.getString("game_result_title_global"), "${provider.globalMastery}%"),
                  MapEntry(settings.getString("game_result_errors").replaceAll(":", ""), "${provider.errorCount}"),
                ],
                onReplayClick: () {
                  provider.initializeGame(
                    widget.levelId,
                    settings.currentLocaleCode,
                    customWordList: widget.customWordList,
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

  Widget _buildQuestionArea(BuildContext context, ThemeData theme, SettingsProvider settings, WordQuizItem word) {
    final double cardSize = (MediaQuery.of(context).size.shortestSide * 0.65).clamp(200.0, 320.0);
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          settings.getString("game_writing_label_reading"),
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onSurfaceVariant,
            letterSpacing: 1.2,
          ),
        ),
        const SizedBox(height: 16),
        Card(
          elevation: 24,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          child: Container(
            width: cardSize,
            height: cardSize * 0.75,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(24),
            ),
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.all(12),
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Text(
                      word.text,
                      style: TextStyle(
                        fontSize: word.text.length > 4 ? 36 : 48,
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    if (word.meaning != null && word.meaning!.isNotEmpty) ...[
                      const SizedBox(height: 12),
                      Text(
                        word.meaning!,
                        style: TextStyle(
                          fontSize: 14,
                          fontStyle: FontStyle.italic,
                          color: theme.colorScheme.onSurface.withValues(alpha: 0.65),
                        ),
                        textAlign: TextAlign.center,
                      ),
                    ],
                  ],
                ),
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAnswerArea(BuildContext context, ThemeData theme, WordQuizProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: List.generate(provider.currentAnswers.length, (index) {
          final answer = provider.currentAnswers[index];
          final state = provider.buttonStates[index];

          Color bgColor = theme.colorScheme.surface.withValues(alpha: 0.85);
          Color textColor = theme.colorScheme.onSurface;

          if (state == AnswerButtonState.correct) {
            bgColor = Colors.green.shade400;
            textColor = Colors.white;
          } else if (state == AnswerButtonState.incorrect) {
            bgColor = Colors.red.shade400;
            textColor = Colors.white;
          }

          return Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: ElevatedButton(
              onPressed: provider.state == GameState.waitingForAnswer
                  ? () => provider.submitAnswer(index)
                  : null,
              style: ElevatedButton.styleFrom(
                minimumSize: const Size(double.infinity, 56),
                backgroundColor: bgColor,
                disabledBackgroundColor: bgColor,
                foregroundColor: textColor,
                disabledForegroundColor: textColor,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                side: BorderSide(color: theme.colorScheme.onSurface.withValues(alpha: 0.08)),
                elevation: 4,
              ),
              child: Text(
                answer,
                style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
              ),
            ),
          );
        }),
      ),
    );
  }
}
