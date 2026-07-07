import 'package:flutter/material.dart';
import 'package:flutter_tts/flutter_tts.dart';
import '../models/dictionary.dart';
import '../repositories/word_repository.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/word_meaning_repository.dart';
import '../repositories/score_repository.dart';

class WordDetailProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final DictionaryRepository _dictionaryRepo;
  final WordMeaningRepository _wordMeaningRepo;
  final ScoreRepository _scoreRepo;
  final FlutterTts _tts = FlutterTts();

  WordEntry? _word;
  String? _meaning;
  List<DictionaryItem> _kanjiComponents = [];
  bool _isLoading = false;
  bool _isInRevisionList = false;

  // Getters
  WordEntry? get word => _word;
  String? get meaning => _meaning;
  List<DictionaryItem> get kanjiComponents => _kanjiComponents;
  bool get isLoading => _isLoading;
  bool get isInRevisionList => _isInRevisionList;

  WordDetailProvider(this._wordRepo, this._dictionaryRepo, this._wordMeaningRepo, this._scoreRepo);

  Future<void> loadWord(String wordText, String locale) async {
    _isLoading = true;
    notifyListeners();

    final allWords = await _wordRepo.getAllWords();
    _word = allWords.firstWhere(
      (w) => w.text == wordText,
      orElse: () => WordEntry(id: '', text: wordText, phonetics: '')
    );

    if (_word != null) {
      // 1. Charger la signification du mot
      final meanings = await _wordMeaningRepo.getWordMeanings(locale);
      _meaning = meanings[_word!.id];

      // 2. Charger les composants Kanji
      final allKanji = await _dictionaryRepo.getFullDictionary(locale);

      final kanjiChars = wordText.runes
          .map((r) => String.fromCharCode(r))
          .where((char) => char.runes.first >= 0x4E00 && char.runes.first <= 0x9FAF)
          .toSet();

      _kanjiComponents = allKanji.where((item) => kanjiChars.contains(item.character)).toList();

      // 3. Vérifier liste de révision
      _isInRevisionList = await _scoreRepo.isInList("Reading_List", wordText);
    }

    _isLoading = false;
    notifyListeners();
  }

  Future<void> toggleRevisionList() async {
    if (_word == null) return;
    final text = _word!.text;

    if (_isInRevisionList) {
      await _scoreRepo.removeItemFromList("Reading_List", text);
    } else {
      await _scoreRepo.addItemToList("Reading_List", text);
    }
    _isInRevisionList = !_isInRevisionList;
    notifyListeners();
  }

  Future<void> speak() async {
    if (_word == null) return;
    await _tts.setLanguage("ja-JP");
    await _tts.setSpeechRate(0.5);
    await _tts.speak(_word!.phonetics.isNotEmpty ? _word!.phonetics : _word!.text);
  }

  @override
  void dispose() {
    _tts.stop();
    super.dispose();
  }
}
