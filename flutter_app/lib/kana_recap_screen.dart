import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/kana.dart';
import 'providers/kana_recap_provider.dart';
import 'repositories/kana_repository.dart';
import 'repositories/score_repository.dart';
import 'widgets/mochi_background.dart';
import 'widgets/recap_components.dart';
import 'kana_quiz_screen.dart';
import 'providers/settings_provider.dart';

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
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);
    
    final title = provider.currentType == KanaType.hiragana ? "Hiragana" : "Katakana";

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: theme.colorScheme.onSurface),
          onPressed: () => Navigator.pop(context),
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
                  child: Column(
                    children: [
                      const SizedBox(height: 8),
                      Text(
                        settings.getString("game_recap_title").toUpperCase(),
                        style: TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.bold,
                          color: theme.colorScheme.onSurfaceVariant,
                          letterSpacing: 1.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 4),
                      Text(
                        title,
                        style: TextStyle(
                          fontSize: 26,
                          fontWeight: FontWeight.bold,
                          color: theme.colorScheme.onSurface,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      Expanded(
                        child: Center(
                          child: ConstrainedBox(
                            constraints: const BoxConstraints(maxWidth: 500),
                            child: _buildGojuonGrid(provider),
                          ),
                        ),
                      ),
                      const SizedBox(height: 16),
                      PaginationControls(
                        currentPage: provider.currentPage,
                        totalPages: provider.totalPages,
                        onPrevClick: provider.prevPage,
                        onNextClick: provider.nextPage,
                      ),
                      const SizedBox(height: 16),
                      PlayButton(
                        onClick: () {
                          final type = provider.currentType ?? KanaType.hiragana;
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => KanaQuizScreen(type: type),
                            ),
                          ).then((_) {
                            provider.refreshScoresOnly();
                          });
                        },
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
