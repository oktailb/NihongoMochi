import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/dictionary.dart';

class DictionaryRepository {
  List<DictionaryItem>? _cachedAllKanji;

  Future<List<DictionaryItem>> getFullDictionary(String locale) async {
    if (_cachedAllKanji != null) return _cachedAllKanji!;

    try {
      final String kanjiJson = await rootBundle.loadString('assets/files/kanji/kanji_details.json');
      final Map<String, dynamic> root = json.decode(kanjiJson);
      final List<dynamic> kanjiData = root['kanji_details']?['kanji'] ?? [];

      Map<String, List<String>> meaningsMap = {};
      try {
        final langCode = locale.split('_')[0].split('-')[0];
        final String meaningsJson = await rootBundle.loadString('assets/files/meanings/meanings_$langCode.json');
        final Map<String, dynamic> meaningsData = json.decode(meaningsJson);
        meaningsMap = meaningsData.map((key, value) => MapEntry(key, List<String>.from(value)));
      } catch (e) {
        print("Meanings for $locale not found, fallback to en");
        try {
           final String meaningsJson = await rootBundle.loadString('assets/files/meanings/meanings_en.json');
           final Map<String, dynamic> meaningsData = json.decode(meaningsJson);
           meaningsMap = meaningsData.map((key, value) => MapEntry(key, List<String>.from(value)));
        } catch(e2) {}
      }

      _cachedAllKanji = kanjiData.map((item) {
        final String id = item['id'].toString();
        final List<dynamic> readingsRaw = item['readings']?['reading'] ?? [];
        // Handle case where it might be a single object instead of a list (based on Kotlin serializer)
        List<dynamic> readingsList = readingsRaw is List ? readingsRaw : [readingsRaw];

        final componentsData = item['components'];
        final List<dynamic> componentsRaw = componentsData?['component'] ?? [];
        List<dynamic> componentsList = componentsRaw is List ? componentsRaw : [componentsRaw];

        return DictionaryItem(
          id: id,
          character: item['character'] ?? '',
          strokeCount: int.tryParse(item['strokes']?.toString() ?? '0') ?? 0,
          readings: readingsList.map((r) => ReadingInfo(
            text: r['#text'] ?? '',
            type: r['type'] ?? 'on',
          )).toList(),
          meanings: meaningsMap[id] ?? [],
          categories: item['category'] is List ? List<String>.from(item['category']) : [item['category']?.toString() ?? ''],
          levelIds: item['level'] is List ? List<String>.from(item['level']) : [item['level']?.toString() ?? ''],
          displayLabelKeys: item['level'] is List ? List<String>.from(item['level']) : [item['level']?.toString() ?? ''],
          structure: componentsData?['structure']?.toString(),
          components: componentsList.map((c) => ComponentEntry(
            kanjiRef: c['kanji_ref']?.toString(),
            text: c['#text']?.toString(),
          )).toList(),
        );
      }).toList();

      return _cachedAllKanji!;
    } catch (e) {
      print("Error loading dictionary: $e");
      return [];
    }
  }
}
