import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/dictionary_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/level_repository.dart';
import 'models/dictionary.dart';
import 'widgets/mochi_background.dart';
import 'widgets/drawing_board.dart';
import 'kanji_detail_screen.dart';

class DictionaryScreen extends StatelessWidget {
  const DictionaryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final locale = context.read<SettingsProvider>().currentLocaleCode;
    return ChangeNotifierProvider(
      create: (context) => DictionaryProvider(
        context.read<DictionaryRepository>(),
        context.read<LevelRepository>(),
        locale,
      ),
      child: const DictionaryView(),
    );
  }
}

class DictionaryView extends StatelessWidget {
  const DictionaryView({super.key});

  void _showDrawingDialog(BuildContext context, DictionaryProvider provider) {
    showDialog(
      context: context,
      builder: (context) => ComposeDrawingDialog(
        provider: provider,
        onDismiss: () => Navigator.pop(context),
        onConfirm: () => Navigator.pop(context),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DictionaryProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        title: Text(
          settings.getString("dictionary_title"),
          style: TextStyle(color: theme.colorScheme.onBackground),
        ),
        elevation: 0,
        backgroundColor: Colors.transparent,
        iconTheme: IconThemeData(color: theme.colorScheme.onBackground),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(12.0),
            child: Column(
              children: [
                _buildFilterPanel(context, provider, settings, theme),
                const SizedBox(height: 8),
                Expanded(
                  child: provider.isLoading
                      ? const Center(child: CircularProgressIndicator())
                      : _buildResultsList(provider, settings),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildFilterPanel(
    BuildContext context,
    DictionaryProvider provider,
    SettingsProvider settings,
    ThemeData theme,
  ) {
    // Determine selected option label
    final selectedOption = provider.levelOptions.firstWhere(
      (opt) => opt.id == provider.selectedLevelId,
      orElse: () => LevelFilterOption(id: "ALL", labelKey: "word_type_all"),
    );
    final displayLabel = settings.getString(selectedOption.labelKey);

    return Container(
      padding: const EdgeInsets.all(12.0),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withOpacity(0.9),
        borderRadius: BorderRadius.circular(8),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.1),
            blurRadius: 4,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Text Search & Drawing Button
          Row(
            children: [
              Expanded(
                child: TextField(
                  controller: TextEditingController.fromValue(
                    TextEditingValue(
                      text: provider.textQuery,
                      selection: TextSelection.collapsed(offset: provider.textQuery.length),
                    ),
                  ),
                  onChanged: provider.onSearchTextChange,
                  decoration: InputDecoration(
                    labelText: settings.getString("dictionary_search_hint_text"),
                    border: const OutlineInputBorder(),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              IconButton(
                icon: Icon(Icons.edit, color: theme.colorScheme.primary),
                onPressed: () => _showDrawingDialog(context, provider),
                style: IconButton.styleFrom(
                  minimumSize: const Size(64, 64),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),

          // Search Mode Radio Buttons
          Row(
            children: [
              Radio<SearchMode>(
                value: SearchMode.reading,
                groupValue: provider.searchMode,
                onChanged: (val) => provider.setSearchMode(val!),
              ),
              GestureDetector(
                onTap: () => provider.setSearchMode(SearchMode.reading),
                child: Text(
                  settings.getString("reading"),
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurface,
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Radio<SearchMode>(
                value: SearchMode.meaning,
                groupValue: provider.searchMode,
                onChanged: (val) => provider.setSearchMode(val!),
              ),
              GestureDetector(
                onTap: () => provider.setSearchMode(SearchMode.meaning),
                child: Text(
                  settings.getString("meaning"),
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurface,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),

          // Level Filter Dropdown (PopupMenuButton)
          Row(
            children: [
              PopupMenuButton<String>(
                onSelected: (val) => provider.setLevel(val),
                itemBuilder: (context) => provider.levelOptions.map((opt) {
                  return PopupMenuItem<String>(
                    value: opt.id,
                    child: Text(settings.getString(opt.labelKey)),
                  );
                }).toList(),
                child: Container(
                  width: 160,
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  decoration: BoxDecoration(
                    border: Border.all(color: theme.colorScheme.outline),
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(
                          displayLabel,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: const TextStyle(fontSize: 14),
                        ),
                      ),
                      const Icon(Icons.arrow_drop_down),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),

          // Stroke Count, Exact Match, Drawing Preview
          Row(
            children: [
              SizedBox(
                width: 100,
                child: TextField(
                  controller: TextEditingController.fromValue(
                    TextEditingValue(
                      text: provider.strokeQuery,
                      selection: TextSelection.collapsed(offset: provider.strokeQuery.length),
                    ),
                  ),
                  onChanged: provider.setStrokeQuery,
                  keyboardType: TextInputType.number,
                  decoration: InputDecoration(
                    labelText: settings.getString("dictionary_search_hint_strokes"),
                    border: const OutlineInputBorder(),
                    contentPadding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
                  ),
                ),
              ),
              const SizedBox(width: 8),
              Checkbox(
                value: provider.exactMatch,
                onChanged: (val) => provider.setExactMatch(val ?? false),
              ),
              GestureDetector(
                onTap: () => provider.setExactMatch(!provider.exactMatch),
                child: Text(
                  settings.getString("dictionary_match_exact"),
                  style: TextStyle(
                    fontSize: 12,
                    color: theme.colorScheme.onSurface,
                  ),
                ),
              ),
              const Spacer(),
              if (provider.currentStrokes.isNotEmpty) ...[
                GestureDetector(
                  onTap: () => _showDrawingDialog(context, provider),
                  child: DrawingThumbnail(strokes: provider.currentStrokes, size: 48),
                ),
                IconButton(
                  icon: const Icon(Icons.close),
                  onPressed: () => provider.clearDrawing(),
                ),
              ],
            ],
          ),
          const SizedBox(height: 8),

          // Results Count
          Align(
            alignment: Alignment.centerRight,
            child: Text(
              settings.getString(
                "dictionary_results_count_format",
                [provider.results.length],
              ),
              style: TextStyle(
                fontSize: 12,
                color: theme.colorScheme.onSurface,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildResultsList(DictionaryProvider provider, SettingsProvider settings) {
    if (provider.results.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.search_off, size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 16),
            Text(
              settings.getString("dictionary_no_results") != "dictionary_no_results"
                  ? settings.getString("dictionary_no_results")
                  : "Aucun Kanji trouvé",
              style: const TextStyle(color: Colors.grey),
            ),
          ],
        ),
      );
    }
    return ListView.builder(
      itemCount: provider.results.length,
      itemBuilder: (context, index) {
        final item = provider.results[index];
        return Column(
          children: [
            DictionaryItemRow(item: item),
            Divider(color: Theme.of(context).colorScheme.outlineVariant.withOpacity(0.5)),
          ],
        );
      },
    );
  }
}

class DictionaryItemRow extends StatelessWidget {
  final DictionaryItem item;
  const DictionaryItemRow({super.key, required this.item});

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    final levelTextList = item.displayLabelKeys.map((key) => settings.getString(key));
    final levelText = levelTextList.join(" • ");

    final onReadings = item.readings.where((r) => r.type == "on").map((r) => _hiraganaToKatakana(r.text));
    final kunReadings = item.readings.where((r) => r.type == "kun").map((r) => r.text);

    final List<String> readingsParts = [];
    if (onReadings.isNotEmpty) {
      readingsParts.add("On: ${onReadings.join(', ')}");
    }
    if (kunReadings.isNotEmpty) {
      readingsParts.add("Kun: ${kunReadings.join(', ')}");
    }
    final readingText = readingsParts.join("  ");

    return InkWell(
      borderRadius: BorderRadius.circular(8),
      onTap: () {
        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => KanjiDetailScreen(kanjiId: item.id),
          ),
        );
      },
      child: Container(
        padding: const EdgeInsets.all(12.0),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface.withOpacity(0.7),
          borderRadius: BorderRadius.circular(8),
        ),
        child: Row(
          children: [
            // Kanji character block
            Container(
              width: 50,
              alignment: Alignment.center,
              child: Text(
                item.character,
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.primary,
                ),
              ),
            ),
            const SizedBox(width: 16),
            // Information column
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    readingText,
                    style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    item.meanings.join(", "),
                    maxLines: 2,
                    overflow: TextOverflow.ellipsis,
                    style: TextStyle(
                      fontSize: 14,
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  if (levelText.isNotEmpty) ...[
                    const SizedBox(height: 4),
                    Text(
                      levelText,
                      style: TextStyle(
                        fontSize: 12,
                        color: theme.colorScheme.secondary,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            const SizedBox(width: 8),
            Text(
              "${item.strokeCount} traits",
              style: TextStyle(
                fontSize: 12,
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
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
