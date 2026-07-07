import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'providers/kana_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';

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
      child: const KanaView(),
    );
  }
}

class KanaView extends StatelessWidget {
  const KanaView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanaProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text(provider.currentPage == 0 ? "Gojuon" : (provider.currentPage == 1 ? "Dakuon" : "Yoon")),
        elevation: 0,
      ),
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)],
          ),
        ),
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
                  _buildPaginationControls(provider),
                  Padding(
                    padding: const EdgeInsets.all(16.0),
                    child: ElevatedButton(
                      onPressed: () {
                        // TODO: Lancer le quiz pour cette page
                      },
                      style: ElevatedButton.styleFrom(
                        minimumSize: const Size(double.infinity, 50),
                        backgroundColor: Colors.pink,
                        foregroundColor: Colors.white,
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      child: const Text("S'ENTRAÎNER", style: TextStyle(fontWeight: FontWeight.bold)),
                    ),
                  ),
                ],
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

  Widget _buildPaginationControls(KanaProvider provider) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          icon: const Icon(Icons.chevron_left),
          onPressed: provider.currentPage > 0 ? () => provider.prevPage(5) : null,
        ),
        Text("Page ${provider.currentPage + 1} / ${provider.totalPages}"),
        IconButton(
          icon: const Icon(Icons.chevron_right),
          onPressed: provider.currentPage < provider.totalPages - 1 ? () => provider.nextPage(5) : null,
        ),
      ],
    );
  }
}
