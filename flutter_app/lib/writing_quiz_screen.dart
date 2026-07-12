import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/quiz_models.dart';
import 'models/kanji_sort_order.dart';
import 'providers/writing_quiz_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'utils/romaji_to_kana.dart';
import 'widgets/mochi_background.dart';
import 'providers/settings_provider.dart';
import 'services/level_content_provider.dart';
import 'services/audio_service.dart';
import 'services/statistics_service.dart';
import 'widgets/game_components.dart';

class WritingQuizScreen extends StatelessWidget {
  final String levelId;
  final int quizSize;
  final List<String>? customKanjiList;
  final KanjiSortOrder sortOrder;

  const WritingQuizScreen({
    super.key,
    required this.levelId,
    this.quizSize = 80,
    this.customKanjiList,
    this.sortOrder = KanjiSortOrder.defaultOrder,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WritingQuizProvider(
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
          context.read<LevelContentProvider>(),
          context.read<AudioService>(),
          context.read<StatisticsService>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.startQuiz(
          levelId,
          locale,
          quizSize: quizSize,
          customKanjiList: customKanjiList,
          sortOrder: sortOrder,
        );
        return provider;
      },
      child: WritingQuizView(
        levelId: levelId,
        quizSize: quizSize,
        customKanjiList: customKanjiList,
        sortOrder: sortOrder,
      ),
    );
  }
}

class WritingQuizView extends StatefulWidget {
  final String levelId;
  final int quizSize;
  final List<String>? customKanjiList;
  final KanjiSortOrder sortOrder;

  const WritingQuizView({
    super.key,
    required this.levelId,
    required this.quizSize,
    this.customKanjiList,
    required this.sortOrder,
  });

  @override
  State<WritingQuizView> createState() => _WritingQuizViewState();
}

class _WritingQuizViewState extends State<WritingQuizView> {
  final TextEditingController _controller = TextEditingController();
  final FocusNode _focusNode = FocusNode();
  bool _canPop = false;
  late WritingQuizProvider _provider;

  @override
  void initState() {
    super.initState();
    _provider = context.read<WritingQuizProvider>();
    _provider.addListener(_onProviderChange);
  }

  @override
  void dispose() {
    _provider.removeListener(_onProviderChange);
    _controller.dispose();
    _focusNode.dispose();
    super.dispose();
  }

  void _onProviderChange() {
    final provider = context.read<WritingQuizProvider>();
    if (provider.state == GameState.waitingForAnswer && !provider.isProcessing) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_focusNode.hasFocus) {
          _focusNode.requestFocus();
        }
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WritingQuizProvider>();
    final settings = context.watch<SettingsProvider>();

    if (provider.state == GameState.loading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final isFinished = provider.state == GameState.finished;

    // Request focus on new question (initial or fallback)
    if (provider.state == GameState.waitingForAnswer && !_focusNode.hasFocus) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        if (mounted && !_focusNode.hasFocus) {
          _focusNode.requestFocus();
        }
      });
    }

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
          title: Text(settings.getString("writing_game_recap_title")),
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
        ),
        extendBodyBehindAppBar: true,
        body: Stack(
          children: [
            MochiBackground(
              child: SafeArea(
                child: Column(
                  children: [
                    GameProgressBar(statuses: provider.currentSetStatus),
                    if (!isFinished) ...[
                      Expanded(
                        child: Center(
                          child: _buildKanjiCard(context, provider.currentKanji?.character ?? ""),
                        ),
                      ),
                      _buildInputArea(context, provider, settings),
                    ] else
                      const Spacer(),
                  ],
                ),
              ),
            ),
            if (provider.showCorrection && provider.currentKanji != null)
              Center(
                child: _buildCorrectionCard(provider),
              ),
            if (isFinished)
              GameResultOverlay(
                isVictory: true,
                title: settings.getString("game_result_lot_mastery_writing"),
                score: "${provider.sessionMastery}%",
                stats: [
                  MapEntry(settings.getString("game_result_title_session"), "${provider.sessionMastery}%"),
                  MapEntry(settings.getString("game_result_title_global"), "${provider.globalMastery}%"),
                  MapEntry(settings.getString("game_result_errors").replaceAll(":", ""), "${provider.errorCount}"),
                ],
                onReplayClick: () {
                  provider.startQuiz(
                    widget.levelId,
                    settings.currentLocaleCode,
                    quizSize: widget.quizSize,
                    customKanjiList: widget.customKanjiList,
                    sortOrder: widget.sortOrder,
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

  Widget _buildKanjiCard(BuildContext context, String character) {
    final theme = Theme.of(context);
    final double cardSize = (MediaQuery.of(context).size.shortestSide * 0.65).clamp(200.0, 320.0);
    return Card(
      elevation: 24,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: cardSize,
        height: cardSize,
        alignment: Alignment.center,
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Center(
          child: Text(
            character,
            style: TextStyle(
              fontSize: cardSize * 0.55,
              fontFamily: 'KanjiStrokeOrders',
              color: theme.colorScheme.primary,
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildInputArea(BuildContext context, WritingQuizProvider provider, SettingsProvider settings) {
    final theme = Theme.of(context);
    final hint = provider.currentQuestionType == QuestionType.reading
        ? settings.getString("game_writing_label_reading")
        : settings.getString("game_writing_label_meaning");

    final isProcessing = provider.isProcessing || provider.state == GameState.showingResult;

    final Color buttonColor = provider.state == GameState.showingResult
        ? (provider.isCorrect == true ? theme.colorScheme.primary : theme.colorScheme.error)
        : theme.colorScheme.secondary;

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.85),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24)),
        border: Border(top: BorderSide(color: theme.colorScheme.onSurface.withOpacity(0.08))),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            decoration: BoxDecoration(
              color: theme.colorScheme.surfaceVariant.withOpacity(0.8),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(
              hint,
              style: TextStyle(
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurfaceVariant,
                fontSize: 16,
              ),
            ),
          ),
          const SizedBox(height: 16),
          TextField(
            controller: _controller,
            focusNode: _focusNode,
            textAlign: TextAlign.center,
            style: const TextStyle(fontSize: 24),
            enableSuggestions: false,
            autocorrect: false,
            obscureText: false,
            keyboardType: TextInputType.visiblePassword, // UX Trick: disables predictive typing
            decoration: InputDecoration(
              hintText: provider.currentQuestionType == QuestionType.reading ? "romaji -> kana" : settings.getString("your_answer"),
              border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
              filled: true,
              fillColor: theme.colorScheme.surface.withOpacity(0.5),
            ),
            enabled: !isProcessing,
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
              setState(() {}); // Rebuild to update submit button disabled state
            },
            onSubmitted: (val) {
              if (val.trim().isNotEmpty) {
                provider.submitAnswer(val);
                _controller.clear();
              }
            },
          ),
          const SizedBox(height: 16),
          ElevatedButton(
            onPressed: isProcessing || _controller.text.trim().isEmpty ? null : () {
              provider.submitAnswer(_controller.text);
              _controller.clear();
            },
            style: ElevatedButton.styleFrom(
              minimumSize: const Size(double.infinity, 52),
              backgroundColor: theme.colorScheme.secondary,
              disabledBackgroundColor: provider.state == GameState.showingResult ? buttonColor : theme.colorScheme.onSurface.withOpacity(0.12),
              foregroundColor: theme.colorScheme.onSecondary,
              disabledForegroundColor: provider.state == GameState.showingResult ? Colors.white : theme.colorScheme.onSurface.withOpacity(0.38),
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              elevation: 4,
            ),
            child: Text(
              settings.getString("submit").toUpperCase(),
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold, letterSpacing: 1.2),
            ),
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
}
