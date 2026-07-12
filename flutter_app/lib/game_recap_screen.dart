import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'main.dart';
import 'models/kanji_sort_order.dart';
import 'providers/game_recap_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'services/level_content_provider.dart';
import 'kanji_detail_screen.dart';
import 'recognition_quiz_screen.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'providers/settings_provider.dart';

class GameRecapScreen extends StatelessWidget {
  final String levelId;
  final String levelTitle;

  const GameRecapScreen({
    super.key,
    required this.levelId,
    required this.levelTitle,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = GameRecapProvider(
          LevelContentProvider(
            kanaRepo: context.read(),
            dictionaryRepo: context.read(),
            wordRepo: context.read(),
            scoreRepo: context.read(),
          ),
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadLevel(levelId, "meaning", locale);
        return provider;
      },
      child: GameRecapView(levelTitle: levelTitle, levelId: levelId),
    );
  }
}

class GameRecapView extends StatefulWidget {
  final String levelTitle;
  final String levelId;

  const GameRecapView({super.key, required this.levelTitle, required this.levelId});

  @override
  State<GameRecapView> createState() => _GameRecapViewState();
}

class _GameRecapViewState extends State<GameRecapView> with RouteAware {
  String _gameMode = "meaning";
  String _readingMode = "common";
  final TextEditingController _quizSizeController = TextEditingController(text: "80");

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    debugPrint("[RECAP_SCREEN] didChangeDependencies: subscribing to routeObserver.");
    routeObserver.subscribe(this, ModalRoute.of(context) as PageRoute);
  }

  @override
  void dispose() {
    debugPrint("[RECAP_SCREEN] dispose: unsubscribing from routeObserver.");
    routeObserver.unsubscribe(this);
    _quizSizeController.dispose();
    super.dispose();
  }

  @override
  void didPopNext() {
    debugPrint("[RECAP_SCREEN] didPopNext triggered! Reloading level scores...");
    final provider = context.read<GameRecapProvider>();
    final settings = context.read<SettingsProvider>();
    provider.loadLevel(widget.levelId, _gameMode, settings.currentLocaleCode);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<GameRecapProvider>();
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
                      settings.getString("game_recap_title").toUpperCase(),
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
                    _buildModeSelector(provider, settings),
                    _buildFooterButtons(context, provider, settings),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildHeader(GameRecapProvider provider, SettingsProvider settings) {
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
                      provider.setSortOrder(val, _gameMode);
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

  Widget _buildKanjiGrid(GameRecapProvider provider) {
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
            final settings = context.read<SettingsProvider>();
            final locale = settings.currentLocaleCode;
            Navigator.push(
              context,
              MaterialPageRoute(builder: (context) => KanjiDetailScreen(kanjiId: item.kanji.id)),
            ).then((_) {
              provider.loadLevel(widget.levelId, _gameMode, locale);
            });
          },
        );
      },
    );
  }

  Widget _buildPagination(GameRecapProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: PaginationControls(
        currentPage: provider.currentPage,
        totalPages: provider.totalPages,
        onPrevClick: () => provider.prevPage(_gameMode),
        onNextClick: () => provider.nextPage(_gameMode),
      ),
    );
  }

  Widget _buildModeSelector(GameRecapProvider provider, SettingsProvider settings) {
    final titleMeaning = settings.getString("game_recap_meaning").isNotEmpty ? settings.getString("game_recap_meaning") : "Sens";
    final titleReading = settings.getString("game_recap_reading").isNotEmpty ? settings.getString("game_recap_reading") : "Lecture";

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Column(
        children: [
          ModeSelector<String>(
            options: [
              MapEntry(titleMeaning.toUpperCase(), "meaning"),
              MapEntry(titleReading.toUpperCase(), "reading"),
            ],
            selectedOption: _gameMode,
            onOptionSelected: (val) {
              setState(() => _gameMode = val);
              final locale = context.read<SettingsProvider>().currentLocaleCode;
              provider.loadLevel(widget.levelId, _gameMode, locale);
            },
          ),
          if (_gameMode == "reading") ...[
            const SizedBox(height: 8),
            ModeSelector<String>(
              options: [
                MapEntry(settings.getString("game_recap_common_pronunciations").toUpperCase(), "common"),
                MapEntry(settings.getString("game_recap_random_pronunciations").toUpperCase(), "random"),
              ],
              selectedOption: _readingMode,
              onOptionSelected: (val) {
                setState(() => _readingMode = val);
              },
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildFooterButtons(BuildContext context, GameRecapProvider provider, SettingsProvider settings) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: PlayAndReviewButtons(
        onPlayClick: () {
          final size = int.tryParse(_quizSizeController.text) ?? 80;
          debugPrint("[RECAP_SCREEN] Launching RecognitionQuizScreen (Play).");
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => RecognitionQuizScreen(
                levelId: widget.levelId,
                gameMode: _gameMode,
                readingMode: _readingMode,
                quizSize: size,
              ),
            ),
          );
        },
        onReviewClick: () async {
          final filteredRevision = await provider.getRevisionKanjiForLevel(_gameMode);
          
          if (context.mounted && filteredRevision.isNotEmpty) {
            debugPrint("[RECAP_SCREEN] Launching RecognitionQuizScreen (Review) with ${filteredRevision.length} kanjis.");
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => RecognitionQuizScreen(
                  levelId: widget.levelId,
                  gameMode: _gameMode,
                  readingMode: _readingMode,
                  quizSize: filteredRevision.length,
                  customKanjiList: filteredRevision,
                ),
              ),
            );
          } else {
            debugPrint("[RECAP_SCREEN] Review click: filteredRevision is empty or context not mounted!");
          }
        },
        isReviewEnabled: provider.isReviewEnabled,
      ),
    );
  }
}
