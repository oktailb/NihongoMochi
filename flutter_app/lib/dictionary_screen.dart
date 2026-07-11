import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/dictionary_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'models/dictionary.dart';
import 'models/handwriting.dart';
import 'widgets/drawing_board.dart';
import 'kanji_detail_screen.dart';

class DictionaryScreen extends StatelessWidget {
  const DictionaryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final locale = context.read<SettingsProvider>().currentLocaleCode;
    return ChangeNotifierProvider(
      create: (context) => DictionaryProvider(context.read<DictionaryRepository>(), locale),
      child: const DictionaryView(),
    );
  }
}

class DictionaryView extends StatelessWidget {
  const DictionaryView({super.key});

  void _showDrawingBoard(BuildContext context, DictionaryProvider provider) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.6,
        decoration: const BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
        ),
        child: DrawingBoard(
          onStrokeDone: (stroke) => provider.addStroke(stroke),
          onClear: () => provider.clearDrawing(),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DictionaryProvider>();

    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: const Text('Dictionnaire'),
        elevation: 0,
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
      ),
      body: Column(
        children: [
          _buildFilterPanel(context, provider),
          if (provider.modelStatus == ModelStatus.notDownloaded)
            _buildDownloadBanner(provider),
          Expanded(
            child: provider.isLoading
                ? const Center(child: CircularProgressIndicator())
                : _buildResultsList(provider),
          ),
        ],
      ),
    );
  }

  Widget _buildDownloadBanner(DictionaryProvider provider) {
    return Container(
      color: Colors.blue.shade50,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Row(
        children: [
          const Icon(Icons.info_outline, size: 20, color: Colors.blue),
          const SizedBox(width: 8),
          const Expanded(
            child: Text(
              "Télécharger le modèle pour la reconnaissance d'écriture ?",
              style: TextStyle(fontSize: 12),
            ),
          ),
          TextButton(
            onPressed: () => provider.downloadModel(),
            child: const Text("Télécharger"),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterPanel(BuildContext context, DictionaryProvider provider) {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 8, 16, 16),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          )
        ],
      ),
      child: Column(
        children: [
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
                    hintText: provider.searchMode == SearchMode.reading
                        ? "Lecture (romaji/kana)..."
                        : "Sens (français)...",
                    prefixIcon: const Icon(Icons.search),
                    border: OutlineInputBorder(
                      borderRadius: BorderRadius.circular(12),
                      borderSide: BorderSide.none,
                    ),
                    filled: true,
                    fillColor: Colors.grey.shade100,
                    contentPadding: const EdgeInsets.symmetric(horizontal: 16),
                  ),
                ),
              ),
              const SizedBox(width: 12),
              GestureDetector(
                onTap: () => _showDrawingBoard(context, provider),
                child: Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: provider.currentStrokes.isNotEmpty
                        ? Colors.blue.shade100
                        : Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(
                    Icons.edit,
                    color: provider.currentStrokes.isNotEmpty ? Colors.blue : Colors.black54,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              _buildSearchModeChip(provider, SearchMode.reading, "Lecture"),
              const SizedBox(width: 8),
              _buildSearchModeChip(provider, SearchMode.meaning, "Sens"),
              const Spacer(),
              _buildLevelDropdown(provider),
            ],
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              SizedBox(
                width: 100,
                child: TextField(
                  onChanged: provider.setStrokeQuery,
                  keyboardType: TextInputType.number,
                  decoration: InputDecoration(
                    labelText: "Traits",
                    isDense: true,
                    border: OutlineInputBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                ),
              ),
              const Spacer(),
              if (provider.currentStrokes.isNotEmpty)
                TextButton.icon(
                  onPressed: () => provider.clearDrawing(),
                  icon: const Icon(Icons.clear, size: 16),
                  label: const Text("Effacer dessin"),
                ),
              Text(
                "${provider.results.length} résultats",
                style: const TextStyle(fontSize: 12, color: Colors.grey, fontWeight: FontWeight.bold),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildSearchModeChip(DictionaryProvider provider, SearchMode mode, String label) {
    final isSelected = provider.searchMode == mode;
    return ChoiceChip(
      label: Text(label, style: TextStyle(fontSize: 12, color: isSelected ? Colors.white : Colors.black)),
      selected: isSelected,
      onSelected: (_) => provider.setSearchMode(mode),
      selectedColor: Colors.blue,
      backgroundColor: Colors.grey.shade200,
    );
  }

  Widget _buildLevelDropdown(DictionaryProvider provider) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      decoration: BoxDecoration(
        color: Colors.grey.shade100,
        borderRadius: BorderRadius.circular(8),
      ),
      child: DropdownButtonHideUnderline(
        child: DropdownButton<String>(
          value: provider.selectedLevelId,
          style: const TextStyle(fontSize: 13, color: Colors.black),
          items: ["ALL", "N5", "N4", "N3", "N2", "N1"]
              .map((l) => DropdownMenuItem(value: l, child: Text(l)))
              .toList(),
          onChanged: (val) => provider.setLevel(val!),
        ),
      ),
    );
  }

  Widget _buildResultsList(DictionaryProvider provider) {
    if (provider.results.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.search_off, size: 64, color: Colors.grey.shade300),
            const SizedBox(height: 16),
            const Text("Aucun Kanji trouvé", style: TextStyle(color: Colors.grey)),
          ],
        ),
      );
    }
    return ListView.builder(
      padding: const EdgeInsets.all(12),
      itemCount: provider.results.length,
      itemBuilder: (context, index) => DictionaryItemCard(item: provider.results[index]),
    );
  }
}

class DictionaryItemCard extends StatelessWidget {
  final DictionaryItem item;
  const DictionaryItemCard({super.key, required this.item});

  @override
  Widget build(BuildContext context) {
    final onReadings = item.readings.where((r) => r.type == 'on').map((r) => r.text).join(", ");
    final kunReadings = item.readings.where((r) => r.type == 'kun').map((r) => r.text).join(", ");

    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 12),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(16),
        side: BorderSide(color: Colors.grey.shade200),
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () {
          Navigator.push(context, MaterialPageRoute(builder: (context) => KanjiDetailScreen(kanjiId: item.id)));
        },
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                width: 60,
                height: 60,
                alignment: Alignment.center,
                decoration: BoxDecoration(
                  color: Colors.blue.shade50,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  item.character,
                  style: const TextStyle(fontSize: 32, color: Colors.blue, fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    if (onReadings.isNotEmpty || kunReadings.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(bottom: 4),
                        child: Text(
                          onReadings.isNotEmpty ? "On: $onReadings" : "Kun: $kunReadings",
                          style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 14),
                        ),
                      ),
                    Text(
                      item.meanings.join(", "),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                      style: TextStyle(color: Colors.grey.shade600, fontSize: 13),
                    ),
                    const SizedBox(height: 8),
                    Row(
                      children: [
                        ...item.displayLabelKeys.map((label) => Padding(
                          padding: const EdgeInsets.only(right: 8),
                          child: Container(
                            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: Colors.orange.shade50,
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              label,
                              style: const TextStyle(fontSize: 10, color: Colors.orange, fontWeight: FontWeight.bold),
                            ),
                          ),
                        )),
                        const Spacer(),
                        Text(
                          "${item.strokeCount} traits",
                          style: const TextStyle(fontSize: 11, color: Colors.grey),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
