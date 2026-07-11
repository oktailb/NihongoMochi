import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/dictionary.dart';
import 'providers/kanji_detail_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'db/database.dart';
import 'providers/settings_provider.dart';

class KanjiDetailScreen extends StatelessWidget {
  final String kanjiId;

  const KanjiDetailScreen({super.key, required this.kanjiId});

  @override
  Widget build(BuildContext context) {
    // Note: Dans une application réelle, ces repositories seraient fournis plus haut dans l'arbre
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanjiDetailProvider(
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadKanji(kanjiId, locale);
        return provider;
      },
      child: const KanjiDetailView(),
    );
  }
}

class KanjiDetailView extends StatelessWidget {
  const KanjiDetailView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanjiDetailProvider>();

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
            colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)], // Simule MochiBackground
          ),
        ),
        child: provider.isLoading
            ? const Center(child: CircularProgressIndicator())
            : provider.kanji == null
                ? const Center(child: Text("Kanji non trouvé"))
                : SingleChildScrollView(
                    padding: const EdgeInsets.all(16.0),
                    child: Column(
                      children: [
                        _buildKanjiCard(context, provider),
                        const SizedBox(height: 24),
                        _buildSectionHeader("SIGNIFICATIONS"),
                        _buildMeaningsBox(provider.kanji!.meanings),
                        const SizedBox(height: 24),
                        _buildSectionHeader("LECTURES"),
                        Row(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Expanded(
                              child: _buildReadingColumn(
                                "ON (Chinoise)",
                                provider.kanji!.readings.where((r) => r.type == 'on').toList(),
                                true,
                              ),
                            ),
                            const SizedBox(width: 16),
                            Expanded(
                              child: _buildReadingColumn(
                                "KUN (Japonaise)",
                                provider.kanji!.readings.where((r) => r.type == 'kun').toList(),
                                false,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 24),
                        _buildStatsRow(provider.kanji!),
                        const SizedBox(height: 24),
                        if (provider.kanji!.components.isNotEmpty) ...[
                          _buildSectionHeader("COMPOSANTS"),
                          _buildComponentsSection(context, provider.kanji!),
                        ],
                        const SizedBox(height: 24),
                        _buildSectionHeader("EXEMPLES"),
                        _buildExamplesList(provider.examples),
                      ],
                    ),
                  ),
      ),
    );
  }

  Widget _buildKanjiCard(BuildContext context, KanjiDetailProvider provider) {
    return Card(
      elevation: 16,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 300,
        height: 300,
        padding: const EdgeInsets.all(16),
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
            Center(
              child: Text(
                provider.kanji!.character,
                style: const TextStyle(
                  fontSize: 180,
                  fontFamily: 'KanjiStrokeOrders',
                  color: Colors.blue,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String text) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: Colors.black54,
          letterSpacing: 1.2,
        ),
      ),
    );
  }

  Widget _buildMeaningsBox(List<String> meanings) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Text(
        meanings.join(", "),
        textAlign: TextAlign.center,
        style: const TextStyle(fontSize: 18),
      ),
    );
  }

  Widget _buildReadingColumn(String title, List<ReadingInfo> readings, bool isOn) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Column(
        children: [
          Text(
            title,
            style: const TextStyle(fontWeight: FontWeight.bold),
          ),
          const Divider(),
          ...readings.map((r) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4.0),
                child: Text(
                  isOn ? _hiraganaToKatakana(r.text) : r.text,
                  style: const TextStyle(fontSize: 16),
                ),
              )),
        ],
      ),
    );
  }

  Widget _buildStatsRow(DictionaryItem kanji) {
    String jlpt = "-";
    for (var l in kanji.levelIds) {
      if (l.startsWith("N")) jlpt = l;
    }

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildStatItem("JLPT", jlpt),
          _buildStatItem("TRAITS", kanji.strokeCount.toString()),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.grey),
        ),
        Text(
          value,
          style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
      ],
    );
  }

  Widget _buildComponentsSection(BuildContext context, DictionaryItem kanji) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey.shade300),
      ),
      child: Wrap(
        alignment: WrapAlignment.center,
        spacing: 16,
        children: [
          if (kanji.structure != null)
            Text(
              kanji.structure!,
              style: const TextStyle(fontSize: 40, fontWeight: FontWeight.bold),
            ),
          ...kanji.components.map((comp) {
            return GestureDetector(
              onTap: () {
                if (comp.kanjiRef != null) {
                  // Navigation vers un autre Kanji
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(
                      builder: (context) => KanjiDetailScreen(kanjiId: comp.kanjiRef!),
                    ),
                  );
                }
              },
              child: Column(
                children: [
                  Text(
                    comp.text ?? "",
                    style: const TextStyle(fontSize: 32, fontFamily: 'KanjiStrokeOrders'),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildExamplesList(List<DictionaryItem> examples) {
    return Column(
      children: examples.map((ex) => _buildExampleRow(ex)).toList(),
    );
  }

  Widget _buildExampleRow(DictionaryItem item) {
    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      child: ListTile(
        title: Text(
          item.character,
          style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.blue),
        ),
        subtitle: Text(item.meanings.join(", ")),
        trailing: const Icon(Icons.chevron_right),
        onTap: () {
          // TODO: Word Detail
        },
      ),
    );
  }

  String _hiraganaToKatakana(String s) {
    return s.runes.map((r) {
      if (r >= 0x3041 && r <= 0x3096) {
        return r + 0x60;
      }
      return r;
    }).map((r) => String.fromCharCode(r)).join();
  }
}
