import 'dart:convert';
import '../services/resource_loader.dart';

class WordMeaningRepository {
  final ResourceLoader _loader;
  final Map<String, Map<String, String>> _cachedMeanings = {};

  WordMeaningRepository(this._loader);

  Future<Map<String, String>> getWordMeanings(String locale) async {
    if (_cachedMeanings.containsKey(locale)) return _cachedMeanings[locale]!;

    String effectiveLocale = locale;
    if (locale.startsWith('en')) effectiveLocale = 'en_GB';

    try {
      String jsonString = await _loader.loadString(
        'word_meanings.json',
        locale: effectiveLocale,
      );

      final decoded = json.decode(jsonString);
      List<dynamic> entries = [];
      
      if (decoded is Map) {
        final root = decoded['word_meanings'];
        if (root is List) {
          entries = root;
        } else if (root is Map) {
          final rawEntries = root['entries'] ?? root['entry'] ?? [];
          entries = rawEntries is List ? rawEntries : [rawEntries];
        }
      } else if (decoded is List) {
        entries = decoded;
      }

      final Map<String, String> meaningsMap = {};
      for (var item in entries) {
        if (item is Map) {
          final id = item['@id']?.toString() ?? item['id']?.toString();
          final meaning = item['meaning']?.toString();
          if (id != null && meaning != null) {
            meaningsMap[id] = meaning;
          }
        }
      }

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
