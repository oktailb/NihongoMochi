import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kanji_sort_order.dart';
import 'providers/game_recap_provider.dart';
import 'providers/writing_recap_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'services/level_content_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'widgets/game_components.dart';
import 'kanji_detail_screen.dart';
import 'writing_quiz_screen.dart';
import 'main.dart'; // To access routeObserver

class WritingRecapScreen extends StatelessWidget {
  final String levelId;
  final String levelTitle;

  const WritingRecapScreen({
    super.key,
    required this.levelId,
    required this.levelTitle,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WritingRecapProvider(
          context.read<LevelContentProvider>(),
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadLevel(levelId, locale);
        return provider;
      },
      child: WritingRecapView(levelTitle: levelTitle, levelId: levelId),
    );
  }
}

class WritingRecapView extends StatefulWidget {
  final String levelId;
  final String levelTitle;

  const WritingRecapView({
    super.key,
    required this.levelId,
    required this.levelTitle,
  });

  @override
  State<WritingRecapView> createState() => _WritingRecapViewState();
}

class _WritingRecapViewState extends State<WritingRecapView> with RouteAware {
  final TextEditingController _quizSizeController = TextEditingController(text: "80");

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    routeObserver.subscribe(this, ModalRoute.of(context) as PageRoute);
  }

  @override
  void dispose() {
    routeObserver.unsubscribe(this);
    _quizSizeController.dispose();
    super.dispose();
  }

  @override
  void didPopNext() {
    final provider = context.read<WritingRecapProvider>();
    final settings = context.read<SettingsProvider>();
    provider.loadLevel(widget.levelId, settings.currentLocaleCode);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WritingRecapProvider>();
    final settings = context.watch<SettingsProvider>();

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
                      settings.getString("writing_game_recap_title").toUpperCase(),
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).colorScheme.onSurfaceVariant,
                        letterSpacing: 1.5,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      titleToDisplay,
                      style: TextStyle(
                        fontSize: 26,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).colorScheme.onSurface,
                      ),
                      textAlign: TextAlign.center,
                    ),
                    const SizedBox(height: 16),
                    _buildHeader(provider, settings),
                    Expanded(child: _buildKanjiGrid(provider)),
                    _buildPagination(provider, settings),
                    _buildFooterButtons(context, provider, settings),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildHeader(WritingRecapProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          Expanded(
            flex: 7,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.5),
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.black12),
              ),
              child: DropdownButtonHideUnderline(
                child: DropdownButton<KanjiSortOrder>(
                  value: provider.sortOrder,
                  isExpanded: true,
                  items: [
                    DropdownMenuItem(
                      value: KanjiSortOrder.defaultOrder,
                      child: Text(settings.getString("game_recap_sort_default")),
                    ),
                    DropdownMenuItem(
                      value: KanjiSortOrder.frequency,
                      child: Text(settings.getString("game_recap_sort_frequency")),
                    ),
                    DropdownMenuItem(
                      value: KanjiSortOrder.strokes,
                      child: Text(settings.getString("game_recap_sort_strokes")),
                    ),
                  ],
                  onChanged: (val) {
                    if (val != null) {
                      provider.setSortOrder(val);
                    }
                  },
                ),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            flex: 3,
            child: TextField(
              controller: _quizSizeController,
              keyboardType: TextInputType.number,
              decoration: InputDecoration(
                labelText: settings.getString("size").isNotEmpty ? settings.getString("size") : "Taille",
                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                filled: true,
                fillColor: Colors.white.withOpacity(0.5),
                contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildKanjiGrid(WritingRecapProvider provider) {
    if (provider.kanjiListWithColors.isEmpty) {
      return const Center(child: Text("Aucun item trouvé pour ce niveau."));
    }
    return GridView.builder(
      padding: const EdgeInsets.all(12),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 8,
        crossAxisSpacing: 6,
        mainAxisSpacing: 6,
      ),
      itemCount: provider.kanjiListWithColors.length,
      itemBuilder: (context, index) {
        final item = provider.kanjiListWithColors[index];
        return RecapGridItem(
          character: item.kanji.character,
          color: item.color,
          onClick: () {
            final locale = context.read<SettingsProvider>().currentLocaleCode;
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => KanjiDetailScreen(kanjiId: item.kanji.id)),
            ).then((_) {
              provider.loadLevel(widget.levelId, locale);
            });
          },
        );
      },
    );
  }

  Widget _buildPagination(WritingRecapProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: PaginationControls(
        currentPage: provider.currentPage,
        totalPages: provider.totalPages,
        onPrevClick: provider.prevPage,
        onNextClick: provider.nextPage,
      ),
    );
  }

  Widget _buildFooterButtons(BuildContext context, WritingRecapProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: PlayAndReviewButtons(
        onPlayClick: () {
          final size = int.tryParse(_quizSizeController.text) ?? 80;
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => WritingQuizScreen(
                levelId: widget.levelId,
                quizSize: size,
                sortOrder: provider.sortOrder,
              ),
            ),
          );
        },
        onReviewClick: () async {
          final filteredRevision = await provider.getRevisionKanjiForLevel();
          if (context.mounted && filteredRevision.isNotEmpty) {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => WritingQuizScreen(
                  levelId: widget.levelId,
                  quizSize: filteredRevision.length,
                  customKanjiList: filteredRevision,
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
