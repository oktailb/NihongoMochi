import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'models/quiz_models.dart';
import 'providers/kana_quiz_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';
import 'services/audio_service.dart';
import 'services/statistics_service.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_components.dart';
import 'providers/settings_provider.dart';

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
          context.read<StatisticsService>(),
        );
        provider.startQuiz(type);
        return provider;
      },
      child: KanaQuizView(type: type),
    );
  }
}

class KanaQuizView extends StatefulWidget {
  final KanaType type;

  const KanaQuizView({super.key, required this.type});

  @override
  State<KanaQuizView> createState() => _KanaQuizViewState();
}

class _KanaQuizViewState extends State<KanaQuizView> {
  bool _canPop = false;

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaQuizProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);
    final engine = provider.engine;

    if (!provider.isInitialized) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final isFinished = engine.state == GameState.finished;

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
            provider.title,
            style: TextStyle(color: theme.colorScheme.onSurface),
          ),
          backgroundColor: Colors.transparent,
          elevation: 0,
          leading: IconButton(
            icon: Icon(Icons.arrow_back, color: theme.colorScheme.onSurface),
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
                    _buildProgressBar(engine),
                    const Spacer(),
                    if (!isFinished) _buildQuestionArea(engine, settings, theme),
                    const Spacer(),
                    if (!isFinished) _buildAnswerArea(context, provider, engine, theme),
                    const SizedBox(height: 40),
                  ],
                ),
              ),
            ),
            if (isFinished)
              GameResultOverlay(
                isVictory: true,
                title: settings.getString("game_result_lot_mastery_kana"),
                score: "${provider.sessionMastery}%",
                stats: [
                  MapEntry(settings.getString("game_result_title_session"), "${provider.sessionMastery}%"),
                  MapEntry(settings.getString("game_result_title_global"), "${provider.globalMastery}%"),
                  MapEntry(settings.getString("game_result_errors").replaceAll(":", ""), "${engine.errorCount}"),
                ],
                onReplayClick: () {
                  provider.startQuiz(widget.type);
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

  Widget _buildProgressBar(dynamic engine) {
    final List<GameStatus> progressStatuses = [];
    for (var item in engine.currentKanaSet) {
      if (item is KanaCharacter) {
        progressStatuses.add(engine.kanaStatus[item.kana] ?? GameStatus.notAnswered);
      }
    }

    return GameProgressBar(
      statuses: progressStatuses,
    );
  }

  Widget _buildQuestionArea(dynamic engine, SettingsProvider settings, ThemeData theme) {
    final isNormal = engine.currentDirection == KanaQuestionDirection.normal;
    final questionText = isNormal ? engine.currentQuestion.kana : engine.currentQuestion.romaji;
    final isFr = settings.currentLocaleCode.startsWith("fr");
    final double cardSize = (MediaQuery.of(context).size.shortestSide * 0.65).clamp(200.0, 300.0);

    return Column(
      children: [
        Text(
          isNormal 
              ? (isFr ? "Quel est ce kana ?" : "What is this kana?")
              : (isFr ? "Trouvez le kana pour :" : "Find the kana for:"),
          style: TextStyle(fontSize: 18, color: theme.colorScheme.onSurface.withValues(alpha: 0.7)),
        ),
        const SizedBox(height: 20),
        Card(
          elevation: 24,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
          color: theme.colorScheme.surface,
          child: Container(
            width: cardSize,
            height: cardSize,
            alignment: Alignment.center,
            child: Text(
              questionText,
              style: TextStyle(
                fontSize: isNormal ? 80 : 40,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface,
              ),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildAnswerArea(BuildContext context, KanaQuizProvider provider, dynamic engine, ThemeData theme) {
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

  Widget _buildButton(int index, KanaQuizProvider provider) {
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
          fontSize: 24.0,
          onClick: () => provider.submitAnswer(index),
        ),
      ),
    );
  }
}

