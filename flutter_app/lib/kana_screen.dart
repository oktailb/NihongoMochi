import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'providers/kana_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'kana_quiz_screen.dart';

class KanaScreen extends StatelessWidget {
  final KanaType type;
  final String title;

  const KanaScreen({super.key, required this.type, required this.title});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanaProvider(
          context.read<KanaRepository>(),
          context.read<ScoreRepository>(),
        );
        provider.loadKana(type);
        return provider;
      },
      child: KanaView(type: type),
    );
  }
}

class KanaView extends StatelessWidget {
  final KanaType type;
  const KanaView({super.key, required this.type});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text(provider.currentPage == 0 ? "Gojuon" : (provider.currentPage == 1 ? "Dakuon" : "Yoon")),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.linesToShow.isEmpty
              ? const Center(child: CircularProgressIndicator())
              : Column(
                  children: [
                    Expanded(
                      child: GridView.builder(
                        padding: const EdgeInsets.all(16),
                        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                          crossAxisCount: 5,
                          crossAxisSpacing: 8,
                          mainAxisSpacing: 8,
                        ),
                        itemCount: provider.linesToShow.length * 5,
                        itemBuilder: (context, index) {
                          final lineIndex = index ~/ 5;
                          final col = (index % 5) + 1;
                          final lineKey = provider.linesToShow[lineIndex];
                          final charsInLine = provider.charactersByLine[lineKey] ?? [];

                          // Trouver le kana correspondant à cette colonne
                          final kana = charsInLine.cast<KanaEntry?>().firstWhere(
                            (k) => k?.column == col,
                            orElse: () => null,
                          );

                          if (kana == null) return const SizedBox.shrink();

                          final color = provider.kanaColors[kana.character] ?? Colors.grey.shade200;
                          return _buildKanaItem(kana.character, color);
                        },
                      ),
                    ),
                    PaginationControls(
                      currentPage: provider.currentPage,
                      totalPages: provider.totalPages,
                      onPrevClick: () => provider.prevPage(5),
                      onNextClick: () => provider.nextPage(5),
                    ),
                    const SizedBox(height: 8),
                    Padding(
                      padding: const EdgeInsets.all(16.0),
                      child: PlayButton(
                        onClick: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => KanaQuizScreen(type: type),
                            ),
                          );
                        },
                      ),
                    ),
                  ],
                ),
        ),
      ),
    );
  }

  Widget _buildKanaItem(String char, Color color) {
    final textColor = ThemeData.estimateBrightnessForColor(color) == Brightness.light ? Colors.black : Colors.white;
    return Container(
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.05), blurRadius: 4, offset: const Offset(0, 2)),
        ],
      ),
      alignment: Alignment.center,
      child: Text(
        char,
        style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: textColor),
      ),
    );
  }
}
