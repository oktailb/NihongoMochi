import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/word_detail_provider.dart';
import 'repositories/word_repository.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'repositories/score_repository.dart';
import 'repositories/settings_repository.dart';
import 'dictionary_screen.dart'; // Pour DictionaryItemCard
import 'models/dictionary.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';

class WordDetailScreen extends StatelessWidget {
  final String wordText;

  const WordDetailScreen({super.key, required this.wordText});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = WordDetailProvider(
          context.read<WordRepository>(),
          context.read<DictionaryRepository>(),
          context.read<WordMeaningRepository>(),
          context.read<ScoreRepository>(),
          context.read<SettingsRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadWord(wordText, locale);
        return provider;
      },
      child: const WordDetailView(),
    );
  }
}

class WordDetailView extends StatelessWidget {
  const WordDetailView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<WordDetailProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      extendBodyBehindAppBar: true,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: theme.colorScheme.onBackground),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : provider.word == null
                  ? const Center(child: Text("Mot non trouvé"))
                  : SingleChildScrollView(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          _buildWordCard(context, provider),
                          const SizedBox(height: 24),
                          if (provider.meaning != null) ...[
                            _buildSectionHeader(context, "SIGNIFICATION"),
                            _buildMeaningBox(context, provider.meaning!),
                            const SizedBox(height: 24),
                          ],
                          if (provider.kanjiComponents.isNotEmpty) ...[
                            _buildSectionHeader(context, "COMPOSANTS KANJI"),
                            _buildKanjiComponentsList(context, provider.kanjiComponents),
                          ],
                        ],
                      ),
                    ),
        ),
      ),
    );
  }

  Widget _buildWordCard(BuildContext context, WordDetailProvider provider) {
    final theme = Theme.of(context);
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface.withOpacity(0.9),
      child: Container(
        width: double.infinity,
        padding: const EdgeInsets.all(24),
        child: Stack(
          children: [
            Align(
              alignment: Alignment.topRight,
              child: IconButton(
                icon: Icon(
                  provider.isInRevisionList ? Icons.star : Icons.star_border,
                  color: provider.isInRevisionList ? theme.colorScheme.primary : theme.colorScheme.onSurface.withOpacity(0.5),
                ),
                onPressed: () => provider.toggleRevisionList(),
              ),
            ),
            Column(
              children: [
                const SizedBox(height: 16),
                Text(
                  provider.word!.text,
                  style: TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.primary,
                  ),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    if (provider.word!.phonetics.isNotEmpty)
                      Text(
                        provider.word!.phonetics,
                        style: TextStyle(
                          fontSize: 24,
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    const SizedBox(width: 16),
                    IconButton(
                      icon: Icon(Icons.volume_up, color: theme.colorScheme.primary, size: 32),
                      onPressed: () => provider.speak(),
                    ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(BuildContext context, String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          text,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: Theme.of(context).colorScheme.onBackground.withOpacity(0.7),
            letterSpacing: 1.2,
          ),
        ),
      ),
    );
  }

  Widget _buildMeaningBox(BuildContext context, String meaning) {
    final theme = Theme.of(context);
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline.withOpacity(0.2)),
      ),
      child: Text(
        meaning,
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 18, color: theme.colorScheme.onSurface),
      ),
    );
  }

  Widget _buildKanjiComponentsList(BuildContext context, List<DictionaryItem> components) {
    return Column(
      children: components.map((item) => DictionaryItemRow(item: item)).toList(),
    );
  }
}

