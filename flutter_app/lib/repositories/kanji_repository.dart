import '../models/dictionary.dart';

class KanjiRepository {
  List<DictionaryItem>? _cachedKanji;
  Map<String, DictionaryItem>? _characterMap;
  String? _cachedLocale;

  Future<List<DictionaryItem>> getAllKanji(String locale, {required dynamic dictionaryRepo}) async {
    if (_cachedKanji != null && _cachedLocale == locale) return _cachedKanji!;
    _cachedLocale = locale;

    // In our Flutter port, DictionaryRepository already does the heavy lifting of merging meanings.
    // We use it to get the enriched items.
    _cachedKanji = await dictionaryRepo.getFullDictionary(locale);
    _characterMap = {for (var k in _cachedKanji!) k.character: k};

    return _cachedKanji!;
  }

  DictionaryItem? getKanjiByCharacter(String char) {
    return _characterMap?[char];
  }

  Future<List<DictionaryItem>> getKanjiByLevel(String levelId, String locale, {required dynamic dictionaryRepo}) async {
    final all = await getAllKanji(locale, dictionaryRepo: dictionaryRepo);
    final lowerLevel = levelId.toLowerCase();
    return all.where((k) => k.levelIds.any((l) => l.toLowerCase() == lowerLevel)).toList();
  }

  Future<List<DictionaryItem>> getNativeKanji(String locale, {required dynamic dictionaryRepo}) async {
    final all = await getAllKanji(locale, dictionaryRepo: dictionaryRepo);
    return all.where((k) => k.categories.isEmpty && k.readings.isNotEmpty).toList();
  }

  Future<List<DictionaryItem>> getNoReadingKanji(String locale, {required dynamic dictionaryRepo}) async {
    final all = await getAllKanji(locale, dictionaryRepo: dictionaryRepo);
    return all.where((k) => k.categories.isEmpty && k.readings.isEmpty && k.meanings.isNotEmpty).toList();
  }

  Future<List<DictionaryItem>> getNoMeaningKanji(String locale, {required dynamic dictionaryRepo}) async {
    final all = await getAllKanji(locale, dictionaryRepo: dictionaryRepo);
    return all.where((k) => k.categories.isEmpty && k.meanings.isEmpty).toList();
  }
}
