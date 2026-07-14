import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/grammar_quiz.dart';
import 'models/quiz_models.dart';
import 'providers/grammar_quiz_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/exercise_repository.dart';
import 'repositories/score_repository.dart';
import 'services/audio_service.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_components.dart';

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

    return PopScope(
      canPop: false,
      onPopInvokedWithResult: (didPop, result) {
        if (didPop) return;
        _showExitDialog(context, provider);
      },
      child: Scaffold(
        appBar: AppBar(
          title: GameProgressBar(
            statuses: provider.progressHistory,
            maxItems: 10,
          ),
          backgroundColor: Colors.transparent,
          elevation: 0,
          automaticallyImplyLeading: false,
          leading: IconButton(
            icon: const Icon(Icons.arrow_back),
            onPressed: () => _showExitDialog(context, provider),
          ),
        ),
        backgroundColor: Colors.transparent,
        extendBodyBehindAppBar: true,
        body: MochiBackground(
          child: SafeArea(
            child: Column(
              children: [
                Expanded(
                  child: Center(
                    child: SingleChildScrollView(
                      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 8),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          if (payload != null)
                            GameQuestionCard(
                              text: _getQuestionText(payload, provider.currentStarIndex),
                              fontSize: (payload is WordUsagePayload) ? 18 : 22,
                            ),
                          const SizedBox(height: 32),
                          if (payload != null)
                            _buildOptionsArea(context, provider, payload),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showExitDialog(BuildContext context, GrammarQuizProvider provider) {
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogContext) => ExitConfirmationDialog(
        onConfirm: () {
          Navigator.pop(dialogContext); // close dialog
          Navigator.pop(context); // exit quiz
        },
        onDismiss: () => Navigator.pop(dialogContext), // resume
      ),
    );
  }

  Widget _buildFinishedScreen(BuildContext context, GrammarQuizProvider provider) {
    final settings = context.read<SettingsProvider>();
    return GameResultOverlay(
      isVictory: provider.quizScore >= 8,
      title: settings.getString("game_result_lot_mastery_grammar"),
      score: "${(provider.globalMasteryPercent * 100).toInt()}%",
      stats: [
        MapEntry(settings.getString("game_result_title_session"), "${(provider.sessionMasteryPercent * 100).toInt()}%"),
        MapEntry(settings.getString("game_result_title_global"), "${(provider.globalMasteryPercent * 100).toInt()}%"),
      ],
      onReplayClick: () => provider.replay(),
      onMenuClick: () => Navigator.pop(context),
    );
  }

  String _getQuestionText(ExercisePayload payload, int starIndex) {
    if (payload is FillBlankPayload) {
      return payload.sentence.replaceAll("__", " ___★___ ");
    } else if (payload is UnderlinePayload) {
      return payload.sentence.replaceAll("[", "【").replaceAll("]", "】");
    } else if (payload is WordUsagePayload) {
      return "「${payload.word}」の使い方が正しいものを選んでください。";
    } else if (payload is SentenceOrderPayload) {
      final holes = List.generate(payload.blocks.length, (i) => i == starIndex ? " ★ " : " ___ ").join("");
      return "${payload.prefix} $holes ${payload.suffix}";
    }
    return "";
  }

  Widget _buildOptionsArea(BuildContext context, GrammarQuizProvider provider, ExercisePayload payload) {
    final options = provider.currentOptions;
    final List<List<String>> chunkedOptions = [];
    for (int i = 0; i < options.length; i += 2) {
      chunkedOptions.add(options.sublist(i, i + 2 > options.length ? options.length : i + 2));
    }

    return Column(
      children: chunkedOptions.map((row) {
        return Padding(
          padding: const EdgeInsets.only(bottom: 8.0),
          child: Row(
            children: row.map((option) {
              final isSelected = provider.selectedOption == option;
              final isActuallyCorrect = _isOptionCorrect(option, payload);
              
              AnswerButtonState buttonState = AnswerButtonState.defaultState;
              if (isSelected && (provider.isAnswerCorrect ?? false)) {
                buttonState = AnswerButtonState.correct;
              } else if (isSelected && !(provider.isAnswerCorrect ?? false)) {
                buttonState = AnswerButtonState.incorrect;
              } else if (provider.selectedOption != null && isActuallyCorrect) {
                buttonState = AnswerButtonState.correct;
              }

              return Expanded(
                child: Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 4.0),
                  child: GameAnswerButton(
                    text: option,
                    state: buttonState,
                    enabled: provider.selectedOption == null,
                    fontSize: (payload is WordUsagePayload) ? 14 : 18,
                    onClick: () => provider.submitAnswer(option),
                  ),
                ),
              );
            }).toList(),
          ),
        );
      }).toList(),
    );
  }

  bool _isOptionCorrect(String option, ExercisePayload p) {
    if (p is FillBlankPayload) return option == p.correct;
    if (p is UnderlinePayload) return option == p.correct;
    if (p is WordUsagePayload) {
      final opt = p.options.firstWhere(
        (o) => o.text == option,
        orElse: () => UsageOption(text: "", isCorrect: false),
      );
      return opt.isCorrect;
    }
    if (p is SentenceOrderPayload) return option == p.blocks.first;
    return false;
  }
}

class GameQuestionCard extends StatelessWidget {
  final String text;
  final double fontSize;

  const GameQuestionCard({
    super.key,
    required this.text,
    required this.fontSize,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Card(
      elevation: 12,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: 340,
        height: 240,
        padding: const EdgeInsets.all(24),
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(24),
        ),
        child: SingleChildScrollView(
          child: Text(
            text,
            textAlign: TextAlign.center,
            style: TextStyle(
              fontSize: fontSize,
              fontWeight: FontWeight.bold,
              color: theme.colorScheme.onSurface,
            ),
          ),
        ),
      ),
    );
  }
}
