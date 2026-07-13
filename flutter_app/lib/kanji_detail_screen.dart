import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/dictionary.dart';
import 'providers/kanji_detail_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'word_detail_screen.dart';

class KanjiDetailScreen extends StatelessWidget {
  final String kanjiId;

  const KanjiDetailScreen({super.key, required this.kanjiId});

  @override
  Widget build(BuildContext context) {
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
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: theme.colorScheme.onBackground),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : provider.kanji == null
                  ? const Center(child: Text("Kanji non trouvé"))
                  : SingleChildScrollView(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          _buildKanjiCard(context, provider, theme),
                          const SizedBox(height: 24),
                          _buildSectionHeader(settings.getString("kanji_meanings").toUpperCase(), theme),
                          _buildMeaningsBox(provider.kanji!.meanings, theme),
                          const SizedBox(height: 24),
                          _buildSectionHeader(settings.getString("kanji_readings").toUpperCase(), theme),
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                child: _buildReadingColumn(
                                  "ON (Chinese)",
                                  provider.kanji!.readings.where((r) => r.type == 'on').toList(),
                                  true,
                                  theme,
                                ),
                              ),
                              const SizedBox(width: 16),
                              Expanded(
                                child: _buildReadingColumn(
                                  "KUN (Japanese)",
                                  provider.kanji!.readings.where((r) => r.type == 'kun').toList(),
                                  false,
                                  theme,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 24),
                          _buildStatsRow(provider.kanji!, theme),
                          const SizedBox(height: 24),
                          if (provider.kanji!.components.isNotEmpty) ...[
                            _buildSectionHeader(settings.getString("kanji_detail_components").toUpperCase(), theme),
                            _buildComponentsSection(context, provider, theme),
                          ],
                          const SizedBox(height: 24),
                          _buildSectionHeader("EXAMPLES", theme),
                          _buildExamplesList(context, provider.examples, theme),
                        ],
                      ),
                    ),
        ),
      ),
    );
  }

  Widget _buildKanjiCard(BuildContext context, KanjiDetailProvider provider, ThemeData theme) {
    return Card(
      elevation: 16,
      shadowColor: Colors.black.withOpacity(0.3),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface,
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
                  color: provider.isInRevisionList ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant,
                ),
                onPressed: () => provider.toggleRevisionList(),
              ),
            ),
            Center(
              child: Text(
                provider.kanji!.character,
                style: TextStyle(
                  fontSize: 180,
                  fontFamily: 'KanjiStrokeOrders',
                  color: theme.colorScheme.primary,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String text, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          text,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onBackground,
            letterSpacing: 1.2,
          ),
        ),
      ),
    );
  }

  Widget _buildMeaningsBox(List<String> meanings, ThemeData theme) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Text(
        meanings.join(", "),
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 18, color: theme.colorScheme.onSurface),
      ),
    );
  }

  Widget _buildReadingColumn(String title, List<ReadingInfo> readings, bool isOn, ThemeData theme) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Column(
        children: [
          Text(
            title,
            style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
          ),
          Divider(color: theme.colorScheme.outline),
          ...readings.map((r) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4.0),
                child: Text(
                  isOn ? _hiraganaToKatakana(r.text) : r.text,
                  style: TextStyle(fontSize: 16, color: theme.colorScheme.onSurface),
                ),
              )),
        ],
      ),
    );
  }

  Widget _buildStatsRow(DictionaryItem kanji, ThemeData theme) {
    String jlpt = "-";
    for (var l in kanji.levelIds) {
      if (l.startsWith("N")) jlpt = l;
    }

    String schoolGrade = "-";
    for (var cat in kanji.categories) {
      if (cat.startsWith("Grade") || cat.startsWith("grade")) {
        schoolGrade = cat.replaceAll("Grade ", "").replaceAll("grade ", "");
      }
    }

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildStatItem("JLPT", jlpt, theme),
          _buildStatItem("GRADE", schoolGrade, theme),
          _buildStatItem("STROKES", kanji.strokeCount.toString(), theme),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, String value, ThemeData theme) {
    return Column(
      children: [
        Text(
          label,
          style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurfaceVariant),
        ),
        Text(
          value,
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
        ),
      ],
    );
  }

  Widget _buildComponentsSection(BuildContext context, KanjiDetailProvider provider, ThemeData theme) {
    final kanji = provider.kanji!;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Wrap(
        alignment: WrapAlignment.center,
        spacing: 16,
        runSpacing: 16,
        children: [
          if (kanji.structure != null)
            Text(
              kanji.structure!,
              style: TextStyle(fontSize: 40, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
            ),
          ...kanji.components.map((comp) {
            final targetChar = comp.kanjiRef ?? comp.text;
            final targetId = targetChar != null ? provider.characterToIdMap[targetChar] : null;

            return GestureDetector(
              onTap: () {
                if (targetId != null) {
                  Navigator.pushReplacement(
                    context,
                    MaterialPageRoute(
                      builder: (context) => KanjiDetailScreen(kanjiId: targetId),
                    ),
                  );
                }
              },
              child: Column(
                children: [
                  Text(
                    comp.text ?? comp.kanjiRef ?? "",
                    style: TextStyle(
                      fontSize: 32,
                      fontFamily: 'KanjiStrokeOrders',
                      color: targetId != null ? theme.colorScheme.primary : theme.colorScheme.onSurface,
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildExamplesList(BuildContext context, List<DictionaryItem> examples, ThemeData theme) {
    return Column(
      children: examples.map((ex) => _buildExampleRow(context, ex, theme)).toList(),
    );
  }

  Widget _buildExampleRow(BuildContext context, DictionaryItem item, ThemeData theme) {
    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: theme.colorScheme.outlineVariant.withOpacity(0.5)),
      ),
      color: theme.colorScheme.surface.withOpacity(0.7),
      child: ListTile(
        title: Text(
          item.character,
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
        ),
        subtitle: Text(
          item.meanings.join(", "),
          style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
        ),
        trailing: Icon(Icons.chevron_right, color: theme.colorScheme.onSurfaceVariant),
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => WordDetailScreen(wordText: item.character),
            ),
          );
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
