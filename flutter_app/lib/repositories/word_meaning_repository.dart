import 'dart:convert';
import 'package:flutter/services.dart';

class WordMeaningRepository {
  final Map<String, Map<String, String>> _cachedMeanings = {};

  Future<Map<String, String>> getWordMeanings(String locale) async {
    if (_cachedMeanings.containsKey(locale)) return _cachedMeanings[locale]!;

    final fileName = _getFileName(locale);
    try {
      final String jsonString = await rootBundle.loadString('assets/files/$fileName');
      final Map<String, dynamic> data = json.decode(jsonString);

      // Structure attendue : {"word_meanings": [{"id": "...", "meaning": "..."}, ...]}
      final List<dynamic> entries = data['word_meanings'] ?? [];
      final Map<String, String> meaningsMap = {
        for (var item in entries) item['id'].toString(): item['meaning'].toString()
      };

      _cachedMeanings[locale] = meaningsMap;
      return meaningsMap;
    } catch (e) {
      print("Error loading word meanings for $locale: $e");
      // Fallback vers l'anglais si ce n'est pas déjà la langue demandée
      if (locale != 'en' && locale != 'en_GB') {
        return getWordMeanings('en');
      }
      return {};
    }
  }

  String _getFileName(String locale) {
    // Normalisation du nom de fichier pour correspondre à la structure Kotlin
    // Exemple: fr_FR -> meanings/word_meanings_fr_rFR.json
    final parts = locale.split('_');
    if (parts.length == 2 && parts[1].length == 2) {
      return 'words/meanings/word_meanings_${parts[0]}_r${parts[1]}.json';
    }
    return 'words/meanings/word_meanings_$locale.json';
  }
}
