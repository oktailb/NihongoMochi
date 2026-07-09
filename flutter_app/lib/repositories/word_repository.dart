import 'dart:convert';
import '../models/dictionary.dart';
import '../services/resource_loader.dart';

class WordEntry {
  final String id;
  final String text;
  final String phonetics;
  final String? type;
  final String? jlpt;
  final String? rank;

  WordEntry({
    required this.id,
    required this.text,
    required this.phonetics,
    this.type,
    this.jlpt,
    this.rank,
  });

  factory WordEntry.fromJson(Map<String, dynamic> json) {
    return WordEntry(
      id: json['id']?.toString() ?? '',
      text: json['text'] ?? '',
      phonetics: json['phonetics'] ?? '',
      type: json['type'],
      jlpt: json['jlpt'],
      rank: json['rank']?.toString(),
    );
  }
}

class WordRepository {
  final ResourceLoader _loader;
  List<WordEntry>? _allWordsCache;

  WordRepository(this._loader);

  Future<List<WordEntry>> getAllWords() async {
    if (_allWordsCache != null) return _allWordsCache!;

    try {
      String jsonString = await _loader.loadString(
        'words/merged_wordlist.json',
        assetPath: 'assets/files/words/merged_wordlist.json',
      );

      final Map<String, dynamic> root = json.decode(jsonString);
      var rawWordsData = root['words'] ?? [];
      final List<dynamic> wordsData = rawWordsData is List ? rawWordsData : [rawWordsData];

      _allWordsCache = wordsData.map((w) => WordEntry.fromJson(w)).toList();
      return _allWordsCache!;
    } catch (e) {
      print("Error loading words: $e");
      return [];
    }
  }

  Future<List<WordEntry>> getWordsForLevel(String levelId) async {
    final all = await getAllWords();
    final lowerId = levelId.toLowerCase();

    // Logic from Kotlin: if it's a JLPT level n5..n1
    if (lowerId.startsWith('n') && lowerId.length == 2) {
      return all.where((w) => w.jlpt?.toLowerCase() == lowerId).toList();
    }

    // Logic for specific wordlists
    if (lowerId.contains("wordlist")) {
      // In a full implementation, we might load a specific JSON file here.
      // For now, we return based on JLPT tags within the merged list if applicable.
    }

    return all.take(100).toList(); // Fallback
  }

  Future<List<WordEntry>> getWordsContainingKanji(String kanji) async {
    final all = await getAllWords();
    return all.where((w) => w.text.contains(kanji)).toList();
  }
}
