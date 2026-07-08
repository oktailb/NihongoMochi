import '../repositories/kana_repository.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../models/kana.dart';

class LevelContentProvider {
  final KanaRepository kanaRepo;
  final DictionaryRepository dictionaryRepo;
  final WordRepository wordRepo;
  final ScoreRepository scoreRepo;

  LevelContentProvider({
    required this.kanaRepo,
    required this.dictionaryRepo,
    required this.wordRepo,
    required this.scoreRepo,
  });

  Future<List<String>> getItemsForLevel(String levelKey, ScoreType type, String locale) async {
    final lowerKey = levelKey.toLowerCase();

    // 1. Cas spéciaux statiques
    if (lowerKey == "hiragana") {
      final entries = await kanaRepo.getKanaEntries(KanaType.hiragana);
      return entries.map((e) => e.character).toList();
    }
    if (lowerKey == "katakana") {
      final entries = await kanaRepo.getKanaEntries(KanaType.katakana);
      return entries.map((e) => e.character).toList();
    }

    // 2. Liste de révision personnalisée
    if (lowerKey == "user_custom_list") {
      final listName = _getListNameForType(type);
      return await scoreRepo.getListItems(listName);
    }

    // 3. Kanji & Défis techniques
    final allKanji = await dictionaryRepo.getFullDictionary(locale);

    if (lowerKey.contains("native_challenge")) {
      return allKanji.where((k) => k.levelIds.isEmpty && k.readings.isNotEmpty).map((k) => k.character).toList();
    }
    if (lowerKey.contains("no_reading")) {
      return allKanji.where((k) => k.levelIds.isEmpty && k.readings.isEmpty && k.meanings.isNotEmpty).map((k) => k.character).toList();
    }
    if (lowerKey.contains("no_meaning")) {
      return allKanji.where((k) => k.levelIds.isEmpty && k.meanings.isEmpty).map((k) => k.character).toList();
    }

    // 4. Par défaut : Filtrage par niveau JLPT/Ecole
    return allKanji.where((k) => k.levelIds.any((l) => l.toLowerCase() == lowerKey)).map((k) => k.character).toList();
  }

  String _getListNameForType(ScoreType type) {
    switch (type) {
      case ScoreType.recognition: return "Recognition_List";
      case ScoreType.reading: return "Reading_List";
      case ScoreType.writing: return "Writing_List";
      case ScoreType.grammar: return "Grammar_List";
    }
  }
}
