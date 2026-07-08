import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kanji_sort_order.dart';
import 'providers/game_recap_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'services/level_content_provider.dart';
import 'kanji_detail_screen.dart';
import 'word_quiz_screen.dart';
import 'kana_quiz_screen.dart';
import 'models/kana.dart';
import 'widgets/mochi_background.dart';
import 'dart:ui' as ui;

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
        final locale = ui.PlatformDispatcher.instance.locale.toString();
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

class _GameRecapViewState extends State<GameRecapView> {
  String _gameMode = "meaning";
  final TextEditingController _quizSizeController = TextEditingController(text: "80");

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<GameRecapProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.levelTitle),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : Column(
                  children: [
                    _buildHeader(provider),
                    Expanded(child: _buildKanjiGrid(provider)),
                    _buildPagination(provider),
                    _buildModeSelector(provider),
                    _buildFooterButtons(context, provider),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildHeader(GameRecapProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          Expanded(
            child: DropdownButton<KanjiSortOrder>(
              value: provider.sortOrder,
              isExpanded: true,
              items: const [
                DropdownMenuItem(value: KanjiSortOrder.defaultOrder, child: Text("Ordre par défaut")),
                DropdownMenuItem(value: KanjiSortOrder.strokes, child: Text("Trier par traits")),
              ],
              onChanged: (val) => provider.setSortOrder(val!, _gameMode),
            ),
          ),
          const SizedBox(width: 16),
          SizedBox(
            width: 60,
            child: TextField(
              controller: _quizSizeController,
              keyboardType: TextInputType.number,
              decoration: const InputDecoration(labelText: "Taille"),
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
        crossAxisCount: 5,
        crossAxisSpacing: 8,
        mainAxisSpacing: 8,
      ),
      itemCount: provider.kanjiListWithColors.length,
      itemBuilder: (context, index) {
        final item = provider.kanjiListWithColors[index];
        return GestureDetector(
          onTap: () => Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => KanjiDetailScreen(kanjiId: item.kanji.id)),
          ),
          child: Container(
            decoration: BoxDecoration(
              color: item.color,
              borderRadius: BorderRadius.circular(4),
              boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 2)],
            ),
            alignment: Alignment.center,
            child: Text(
              item.kanji.character,
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
            ),
          ),
        );
      },
    );
  }

  Widget _buildPagination(GameRecapProvider provider) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          icon: const Icon(Icons.chevron_left),
          onPressed: provider.currentPage > 0 ? () => provider.prevPage(_gameMode) : null,
        ),
        Text("Page ${provider.currentPage + 1} / ${provider.totalPages}"),
        IconButton(
          icon: const Icon(Icons.chevron_right),
          onPressed: provider.currentPage < provider.totalPages - 1 ? () => provider.nextPage(_gameMode) : null,
        ),
      ],
    );
  }

  Widget _buildModeSelector(GameRecapProvider provider) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: SegmentedButton<String>(
        segments: const [
          ButtonSegment(value: "meaning", label: Text("Sens"), icon: Icon(Icons.translate)),
          ButtonSegment(value: "reading", label: Text("Lecture"), icon: Icon(Icons.menu_book)),
        ],
        selected: {_gameMode},
        onSelectionChanged: (val) {
          setState(() => _gameMode = val.first);
          final locale = ui.PlatformDispatcher.instance.locale.toString();
          provider.loadLevel(widget.levelId, _gameMode, locale);
        },
      ),
    );
  }

  Widget _buildFooterButtons(BuildContext context, GameRecapProvider provider) {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: ElevatedButton.icon(
              onPressed: () {
                if (_gameMode == "meaning") {
                   // Note: Adapté selon le type de niveau
                   if (widget.levelId.contains("kana")) {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const KanaQuizScreen(type: KanaType.hiragana)));
                   } else {
                      // Kanji Quiz non encore implémenté, on redirige vers Word Quiz par défaut
                      Navigator.push(context, MaterialPageRoute(builder: (context) => WordQuizScreen(levelId: widget.levelId)));
                   }
                } else {
                  Navigator.push(context, MaterialPageRoute(builder: (context) => WordQuizScreen(levelId: widget.levelId)));
                }
              },
              icon: const Icon(Icons.play_arrow),
              label: const Text("JOUER"),
              style: ElevatedButton.styleFrom(
                backgroundColor: Colors.pink,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
          ),
          if (provider.isReviewEnabled) ...[
            const SizedBox(width: 12),
            Expanded(
              child: OutlinedButton.icon(
                onPressed: () {
                   Navigator.push(context, MaterialPageRoute(builder: (context) => WordQuizScreen(levelId: "user_custom_list")));
                },
                icon: const Icon(Icons.star),
                label: const Text("RÉVISER"),
                style: OutlinedButton.styleFrom(
                  foregroundColor: Colors.pink,
                  padding: const EdgeInsets.symmetric(vertical: 12),
                ),
              ),
            ),
          ],
        ],
      ),
    );
  }
}
