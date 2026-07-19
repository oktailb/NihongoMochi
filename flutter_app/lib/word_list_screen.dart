import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/word_list_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/word_repository.dart';
import 'repositories/score_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'word_detail_screen.dart';
import 'word_quiz_screen.dart';
import 'main.dart'; // To access routeObserver

class WordListScreen extends StatelessWidget {
  final String levelId;
  final String levelTitle;

  const WordListScreen({
    super.key,
    required this.levelId,
    required this.levelTitle,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WordListProvider(
          context.read<WordRepository>(),
          context.read<ScoreRepository>(),
          context.read<WordMeaningRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadList(levelId, locale);
        return provider;
      },
      child: WordListView(levelTitle: levelTitle, levelId: levelId),
    );
  }
}

class WordListView extends StatefulWidget {
  final String levelId;
  final String levelTitle;

  const WordListView({
    super.key,
    required this.levelId,
    required this.levelTitle,
  });

  @override
  State<WordListView> createState() => _WordListViewState();
}

class _WordListViewState extends State<WordListView> with RouteAware {
  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    routeObserver.subscribe(this, ModalRoute.of(context) as PageRoute);
  }

  @override
  void dispose() {
    routeObserver.unsubscribe(this);
    super.dispose();
  }

  @override
  void didPopNext() {
    final provider = context.read<WordListProvider>();
    final settings = context.read<SettingsProvider>();
    provider.loadList(widget.levelId, settings.currentLocaleCode);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WordListProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    final resolvedTitle = settings.getString(widget.levelId.toLowerCase());
    final titleToDisplay = resolvedTitle.isNotEmpty && resolvedTitle != widget.levelId.toLowerCase()
        ? resolvedTitle
        : widget.levelTitle;

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : Column(
                  children: [
                    const SizedBox(height: 8),
                    Text(
                      titleToDisplay,
                      style: TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.onSurface,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    _buildFilters(theme, provider, settings),
                    Expanded(
                      child: SingleChildScrollView(
                        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                        child: _buildWordList(context, provider),
                      ),
                    ),
                    _buildPagination(provider, settings),
                    _buildFooterButtons(context, provider, settings),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildFilters(ThemeData theme, WordListProvider provider, SettingsProvider settings) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(12),
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.8),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: theme.colorScheme.onSurface.withValues(alpha: 0.08)),
      ),
      child: Column(
        children: [
          Wrap(
            spacing: 8,
            runSpacing: 4,
            alignment: WrapAlignment.center,
            children: [
              FilterChip(
                label: Text(settings.getString("reading_kanji_solo").isNotEmpty ? settings.getString("reading_kanji_solo") : "Kanji Solo"),
                selected: provider.filterKanjiOnly,
                onSelected: (val) => provider.setFilterKanjiOnly(val, settings.currentLocaleCode),
              ),
              FilterChip(
                label: Text(settings.getString("reading_simple_words").isNotEmpty ? settings.getString("reading_simple_words") : "Mots simples"),
                selected: provider.filterSimpleWords,
                onSelected: (val) => provider.setFilterSimpleWords(val, settings.currentLocaleCode),
              ),
              FilterChip(
                label: Text(settings.getString("reading_compound_words").isNotEmpty ? settings.getString("reading_compound_words") : "Mots composés"),
                selected: provider.filterCompoundWords,
                onSelected: (val) => provider.setFilterCompoundWords(val, settings.currentLocaleCode),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12),
            decoration: BoxDecoration(
              color: theme.colorScheme.surface.withValues(alpha: 0.5),
              borderRadius: BorderRadius.circular(8),
              border: Border.all(color: theme.colorScheme.onSurface.withValues(alpha: 0.12)),
            ),
            child: DropdownButtonHideUnderline(
              child: DropdownButton<String>(
                value: provider.selectedWordType,
                isExpanded: true,
                items: provider.wordTypeOptions.map((opt) {
                  return DropdownMenuItem<String>(
                    value: opt.key,
                    child: Text(opt.value),
                  );
                }).toList(),
                onChanged: (val) {
                  if (val != null) {
                    provider.setWordType(val, settings.currentLocaleCode);
                  }
                },
              ),
            ),
          ),
          const SizedBox(height: 8),
          FilterChip(
            label: Text(settings.getString("reading_ignore_known_words").isNotEmpty ? settings.getString("reading_ignore_known_words") : "Ignorer connus"),
            selected: provider.filterIgnoreKnown,
            onSelected: (val) => provider.setFilterIgnoreKnown(val, settings.currentLocaleCode),
          ),
        ],
      ),
    );
  }

  Widget _buildWordList(BuildContext context, WordListProvider provider) {
    if (provider.displayedWords.isEmpty) {
      return const Center(child: Text("Aucun mot trouvé."));
    }
    return Wrap(
      spacing: 8,
      runSpacing: 8,
      children: provider.displayedWords.map((item) {
        return WordChip(
          text: item.word.text,
          meaning: item.meaning,
          backgroundColor: item.color,
          onClick: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => WordDetailScreen(wordText: item.word.text),
              ),
            ).then((_) {
              if (context.mounted) {
                final locale = context.read<SettingsProvider>().currentLocaleCode;
                provider.loadList(widget.levelId, locale);
              }
            });

          },
        );
      }).toList(),
    );
  }

  Widget _buildPagination(WordListProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: PaginationControls(
        currentPage: provider.currentPage,
        totalPages: provider.totalPages,
        onPrevClick: () => provider.prevPage(settings.currentLocaleCode),
        onNextClick: () => provider.nextPage(settings.currentLocaleCode),
      ),
    );
  }

  Widget _buildFooterButtons(BuildContext context, WordListProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: PlayAndReviewButtons(
        onPlayClick: () {
          final gameList = provider.getGameWordList();
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => WordQuizScreen(
                levelId: widget.levelId,
                customWordList: gameList,
              ),
            ),
          );
        },
        onReviewClick: () async {
          final revisionList = await provider.getRevisionWordList();
          if (context.mounted && revisionList.isNotEmpty) {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => WordQuizScreen(
                  levelId: widget.levelId,
                  customWordList: revisionList,
                ),
              ),
            );
          }
        },
        isReviewEnabled: provider.isReviewEnabled,
      ),
    );
  }
}

class WordChip extends StatelessWidget {
  final String text;
  final String? meaning;
  final Color backgroundColor;
  final VoidCallback onClick;

  const WordChip({
    super.key,
    required this.text,
    this.meaning,
    required this.backgroundColor,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onClick,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: backgroundColor,
          borderRadius: BorderRadius.circular(12),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.04),
              blurRadius: 4,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              text,
              style: TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
                color: theme.colorScheme.onSurface,
              ),
            ),
            if (meaning != null && meaning!.isNotEmpty) ...[
              const SizedBox(height: 2),
              Text(
                meaning!,
                style: TextStyle(
                  fontSize: 11,
                  color: theme.colorScheme.onSurface.withValues(alpha: 0.7),
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ],
        ),
      ),
    );
  }
}
