import 'dart:convert';
import '../models/dictionary.dart';
import '../services/resource_loader.dart';

class DictionaryRepository {
  final ResourceLoader _loader;
  List<DictionaryItem>? _cachedAllKanji;
  String? _cachedLocale;

  DictionaryRepository(this._loader);

  Future<List<DictionaryItem>> getFullDictionary(String locale) async {
    if (_cachedAllKanji != null && _cachedLocale == locale) return _cachedAllKanji!;
    _cachedLocale = locale;

    // Normalisation de la locale pour le Web/Assets
    String effectiveLocale = locale;
    if (locale.startsWith('en')) effectiveLocale = 'en_GB';

    try {
      final String kanjiJson = await _loader.loadString(
        'kanji_details.json',
        assetPath: 'assets/files/kanji/kanji_details.json',
      );
      final Map<String, dynamic> root = json.decode(kanjiJson);
      
      // Protection contre le cas où 'kanji' est un objet seul au lieu d'une liste
      var kanjiDataRaw = root['kanji_details']?['kanji'] ?? [];
      final List<dynamic> kanjiData = kanjiDataRaw is List ? kanjiDataRaw : [kanjiDataRaw];

      Map<String, List<String>> meaningsMap = {};
      try {
        final String meaningsJsonStr = await _loader.loadString(
          'meanings.json',
          locale: effectiveLocale,
        );
        
        final decoded = json.decode(meaningsJsonStr);
        List<dynamic> kanjiList = [];
        if (decoded is Map) {
          final root = decoded['meanings'];
          if (root is Map) {
            final rawKanji = root['kanji'] ?? [];
            kanjiList = rawKanji is List ? rawKanji : [rawKanji];
          }
        }
        
        meaningsMap = {};
        for (var item in kanjiList) {
          if (item is Map) {
            final id = item['@id']?.toString() ?? item['id']?.toString();
            if (id != null) {
              var mRaw = item['meaning'] ?? [];
              List<String> mList = [];
              if (mRaw is List) {
                for (var elem in mRaw) {
                  if (elem is String) {
                    mList.add(elem);
                  } else if (elem is Map) {
                    final text = elem['#text'] ?? elem['text'] ?? elem.toString();
                    mList.add(text.toString());
                  } else if (elem != null) {
                    mList.add(elem.toString());
                  }
                }
              } else if (mRaw is Map) {
                final text = mRaw['#text'] ?? mRaw['text'] ?? mRaw.toString();
                mList.add(text.toString());
              } else if (mRaw != null) {
                mList.add(mRaw.toString());
              }
              meaningsMap[id] = mList;
            }
          }
        }
      } catch (e) {
        print("Meanings for $effectiveLocale not found, fallback to en_GB: $e");
        try {
           final String meaningsJsonStr = await _loader.loadString('meanings.json', locale: 'en_GB');
           final decoded = json.decode(meaningsJsonStr);
           List<dynamic> kanjiList = [];
           if (decoded is Map) {
             final root = decoded['meanings'];
             if (root is Map) {
               final rawKanji = root['kanji'] ?? [];
               kanjiList = rawKanji is List ? rawKanji : [rawKanji];
             }
           }
           for (var item in kanjiList) {
             if (item is Map) {
               final id = item['@id']?.toString() ?? item['id']?.toString();
               if (id != null) {
                 var mRaw = item['meaning'] ?? [];
                 List<String> mList = [];
                 if (mRaw is List) {
                   for (var elem in mRaw) {
                     if (elem is String) {
                       mList.add(elem);
                     } else if (elem is Map) {
                       final text = elem['#text'] ?? elem['text'] ?? elem.toString();
                       mList.add(text.toString());
                     } else if (elem != null) {
                       mList.add(elem.toString());
                     }
                   }
                 } else if (mRaw is Map) {
                   final text = mRaw['#text'] ?? mRaw['text'] ?? mRaw.toString();
                   mList.add(text.toString());
                 } else if (mRaw != null) {
                   mList.add(mRaw.toString());
                 }
                 meaningsMap[id] = mList;
               }
             }
           }
        } catch(e2) {}
      }

      _cachedAllKanji = kanjiData.map((item) {
        if (item is! Map) return DictionaryItem(id: '', character: '', readings: [], strokeCount: 0, meanings: []);
        
        final String id = item['id']?.toString() ?? item['@id']?.toString() ?? '';
        final readingsData = item['readings'];
        dynamic readingsRaw;
        if (readingsData is Map) {
          readingsRaw = readingsData['reading'] ?? [];
        } else if (readingsData is List) {
          readingsRaw = readingsData;
        } else {
          readingsRaw = [];
        }
        
        // Handle case where it might be a single object instead of a list
        final List<dynamic> readingsList = readingsRaw is List ? readingsRaw : [readingsRaw];

        final componentsData = item['components'];
        dynamic componentsRaw;
        if (componentsData is Map) {
          componentsRaw = componentsData['component'] ?? [];
        } else if (componentsData is List) {
          componentsRaw = componentsData;
        } else {
          componentsRaw = [];
        }
        final List<dynamic> componentsList = componentsRaw is List ? componentsRaw : [componentsRaw];

        return DictionaryItem(
          id: id,
          character: item['character'] ?? '',
          strokeCount: int.tryParse(item['strokes']?.toString() ?? '0') ?? 0,
          readings: readingsList.where((r) => r is Map).map((r) => ReadingInfo(
            text: r['#text'] ?? '',
            type: r['type'] ?? 'on',
          )).toList(),
          meanings: meaningsMap[id] ?? [],
          categories: item['category'] is List ? List<String>.from(item['category']) : (item['category'] != null ? [item['category'].toString()] : []),
          levelIds: item['level'] is List ? List<String>.from(item['level']) : (item['level'] != null ? [item['level'].toString()] : []),
          displayLabelKeys: item['level'] is List ? List<String>.from(item['level']) : (item['level'] != null ? [item['level'].toString()] : []),
          structure: componentsData is Map ? componentsData['structure']?.toString() : null,
          components: componentsList.where((c) => c is Map).map((c) => ComponentEntry(
            kanjiRef: c['kanji_ref']?.toString() ?? c['@kanji_ref']?.toString(),
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
