import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/kanji_recap_provider.dart';
import 'services/level_content_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'kanji_detail_screen.dart';
import 'recognition_quiz_screen.dart';
import 'providers/settings_provider.dart';

class KanjiRecapScreen extends StatelessWidget {
  final String levelId;
  final String levelName;

  const KanjiRecapScreen({
    super.key,
    required this.levelId,
    required this.levelName,
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanjiRecapProvider(
          context.read<LevelContentProvider>(),
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadLevel(levelId, locale);
        return provider;
      },
      child: KanjiRecapView(levelId: levelId, levelName: levelName),
    );
  }
}

class KanjiRecapView extends StatefulWidget {
  final String levelId;
  final String levelName;
  const KanjiRecapView({super.key, required this.levelId, required this.levelName});

  @override
  State<KanjiRecapView> createState() => _KanjiRecapViewState();
}

class _KanjiRecapViewState extends State<KanjiRecapView> {
  final TextEditingController _sizeController = TextEditingController(text: "80");

  @override
  void dispose() {
    _sizeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanjiRecapProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text("Récapitulatif - ${widget.levelName}"),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    children: [
                      Row(
                        children: [
                          Expanded(
                            child: _buildSortDropdown(context, provider),
                          ),
                          const SizedBox(width: 12),
                          SizedBox(
                            width: 80,
                            child: TextField(
                              controller: _sizeController,
                              keyboardType: TextInputType.number,
                              textAlign: TextAlign.center,
                              decoration: InputDecoration(
                                labelText: "Taille",
                                border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                                filled: true,
                                fillColor: Colors.white.withOpacity(0.5),
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      Expanded(
                        child: GridView.builder(
                          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                            crossAxisCount: 8,
                            mainAxisSpacing: 4,
                            crossAxisSpacing: 4,
                          ),
                          itemCount: provider.currentPageItems.length,
                          itemBuilder: (context, index) {
                            final item = provider.currentPageItems[index];
                            return RecapGridItem(
                              character: item.key.character,
                              color: item.value,
                              onClick: () {
                                Navigator.push(
                                  context,
                                  MaterialPageRoute(
                                    builder: (context) => KanjiDetailScreen(kanjiId: item.key.id),
                                  ),
                                );
                              },
                            );
                          },
                        ),
                      ),
                      const SizedBox(height: 12),
                      PaginationControls(
                        currentPage: provider.currentPage,
                        totalPages: provider.totalPages,
                        onPrevClick: provider.prevPage,
                        onNextClick: provider.nextPage,
                      ),
                      const SizedBox(height: 12),
                      ModeSelector<String>(
                        title: "MODE DE JEU",
                        options: const [
                          MapEntry("SENS", "meaning"),
                          MapEntry("LECTURE", "reading"),
                        ],
                        selectedOption: provider.gameMode,
                        onOptionSelected: (val) => provider.setGameMode(val),
                      ),
                      if (provider.gameMode == "reading") ...[
                        const SizedBox(height: 8),
                        ModeSelector<String>(
                          title: "PRONONCIATIONS",
                          options: const [
                            MapEntry("COMMUNES", "common"),
                            MapEntry("ALÉATOIRES", "random"),
                          ],
                          selectedOption: "common", 
                          onOptionSelected: (val) {
                            // provider.setReadingMode(val);
                          },
                        ),
                      ],
                      const SizedBox(height: 16),
                      PlayAndReviewButtons(
                        onPlayClick: () {
                          final size = int.tryParse(_sizeController.text) ?? 80;
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => RecognitionQuizScreen(
                                levelId: widget.levelId,
                                gameMode: provider.gameMode,
                                readingMode: "common", 
                                quizSize: size,
                              ),
                            ),
                          );
                        },
                        onReviewClick: () {
                          // TODO: Start Review Quiz
                        },
                        isReviewEnabled: provider.isReviewEnabled,
                      ),
                    ],
                  ),
                ),
        ),
      ),
    );
  }

  Widget _buildSortDropdown(BuildContext context, KanjiRecapProvider provider) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.5),
        borderRadius: BorderRadius.circular(12),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<KanjiSortOrder>(
          value: provider.sortOrder,
          isExpanded: true,
          onChanged: (val) {
            if (val != null) provider.setSortOrder(val);
          },
          items: const [
            DropdownMenuItem(value: KanjiSortOrder.defaultOrder, child: Text("Ordre par défaut")),
            DropdownMenuItem(value: KanjiSortOrder.frequency, child: Text("Fréquence")),
            DropdownMenuItem(value: KanjiSortOrder.strokes, child: Text("Nombre de traits")),
          ],
        ),
      ),
    );
  }
}
