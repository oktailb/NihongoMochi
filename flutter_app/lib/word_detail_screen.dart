import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/word_detail_provider.dart';
import 'repositories/word_repository.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'repositories/score_repository.dart';
import 'dictionary_screen.dart'; // Pour DictionaryItemCard
import 'kanji_detail_screen.dart';
import 'dart:ui' as ui;

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
        );
        final locale = ui.PlatformDispatcher.instance.locale.toString();
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

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Colors.black),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)],
          ),
        ),
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
                          _buildSectionHeader("SIGNIFICATION"),
                          _buildMeaningBox(provider.meaning!),
                          const SizedBox(height: 24),
                        ],
                        if (provider.kanjiComponents.isNotEmpty) ...[
                          _buildSectionHeader("COMPOSANTS KANJI"),
                          _buildKanjiComponentsList(context, provider.kanjiComponents),
                        ],
                      ],
                    ),
                  ),
      ),
    );
  }

  Widget _buildWordCard(BuildContext context, WordDetailProvider provider) {
    return Card(
      elevation: 16,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
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
                  color: provider.isInRevisionList ? Colors.blue : Colors.grey,
                ),
                onPressed: () => provider.toggleRevisionList(),
              ),
            ),
            Column(
              children: [
                const SizedBox(height: 16),
                Text(
                  provider.word!.text,
                  style: const TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                    color: Colors.blue,
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
                          color: Colors.blueGrey.shade700,
                        ),
                      ),
                    const SizedBox(width: 16),
                    IconButton(
                      icon: const Icon(Icons.volume_up, color: Colors.blue, size: 32),
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

  Widget _buildSectionHeader(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          text,
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: Colors.black54,
            letterSpacing: 1.2,
          ),
        ),
      ),
    );
  }

  Widget _buildMeaningBox(String meaning) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Text(
        meaning,
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 18),
      ),
    );
  }

  Widget _buildKanjiComponentsList(BuildContext context, List<DictionaryItem> components) {
    return Column(
      children: components.map((item) => DictionaryItemCard(item: item)).toList(),
    );
  }
}
