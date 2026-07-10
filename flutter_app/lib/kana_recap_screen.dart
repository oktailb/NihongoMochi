import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'providers/kana_recap_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'kana_quiz_screen.dart';

class KanaRecapScreen extends StatelessWidget {
  final KanaType type;

  const KanaRecapScreen({super.key, required this.type});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanaRecapProvider(
          context.read<KanaRepository>(),
          context.read<ScoreRepository>(),
        );
        provider.loadKana(type);
        return provider;
      },
      child: const KanaRecapView(),
    );
  }
}

class KanaRecapView extends StatelessWidget {
  const KanaRecapView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaRecapProvider>();
    final title = provider.currentType == KanaType.hiragana ? "Hiragana" : "Katakana";

    return Scaffold(
      appBar: AppBar(
        title: Text(title),
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
                      Expanded(
                        child: _buildGojuonGrid(provider),
                      ),
                      const SizedBox(height: 16),
                      PaginationControls(
                        currentPage: provider.currentPage,
                        totalPages: provider.totalPages,
                        onPrevClick: provider.prevPage,
                        onNextClick: provider.nextPage,
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: () {
                          // Navigate to Quiz
                          final type = provider.charactersByLine.values.first.first.type;
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => KanaQuizScreen(type: type),
                            ),
                          );
                        },
                        style: ElevatedButton.styleFrom(
                          backgroundColor: Colors.pink,
                          foregroundColor: Colors.white,
                          minimumSize: const Size(double.infinity, 80),
                          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
                        ),
                        child: const Text(
                          "JOUER",
                          style: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
                        ),
                      ),
                    ],
                  ),
                ),
        ),
      ),
    );
  }

  Widget _buildGojuonGrid(KanaRecapProvider provider) {
    return GridView.builder(
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 5,
        mainAxisSpacing: 8,
        crossAxisSpacing: 8,
      ),
      itemCount: provider.linesToShow.length * 5,
      itemBuilder: (context, index) {
        final lineIndex = index ~/ 5;
        final colIndex = (index % 5) + 1;
        final lineKey = provider.linesToShow[lineIndex];
        final chars = provider.charactersByLine[lineKey] ?? [];
        
        final kana = chars.cast<KanaEntry?>().firstWhere(
          (c) => c?.column == colIndex,
          orElse: () => null,
        );

        if (kana == null) return const SizedBox.shrink();

        final color = provider.kanaColors[kana.character] ?? Colors.grey.shade300;

        return RecapGridItem(
          character: kana.character,
          color: color,
          onClick: () {
            // Optionnel: Voir détail du kana
          },
        );
      },
    );
  }
}
