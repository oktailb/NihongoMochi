import 'package:flutter/material.dart';
import '../models/quiz_models.dart';
import '../repositories/word_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/word_meaning_repository.dart';

class WordRecapItem {
  final WordEntry word;
  final Color color;
  final String? meaning;

  WordRecapItem({required this.word, required this.color, this.meaning});
}

class WordListProvider extends ChangeNotifier {
  final WordRepository _wordRepo;
  final ScoreRepository _scoreRepo;
  final WordMeaningRepository _meaningRepo;

  WordListProvider(this._wordRepo, this._scoreRepo, this._meaningRepo);

  bool _isLoading = false;
  List<WordEntry> _allWords = [];
  List<WordEntry> _filteredWords = [];
  List<WordRecapItem> _displayedWords = [];
  int _currentPage = 0;
  int _totalPages = 0;
  final int _pageSize = 80;
  bool _isReviewEnabled = false;

  // Filters
  bool _filterKanjiOnly = false;
  bool _filterSimpleWords = false;
  bool _filterCompoundWords = false;
  bool _filterIgnoreKnown = false;
  String _selectedWordType = "Tous";
  List<MapEntry<String, String>> _wordTypeOptions = [const MapEntry("Tous", "All")];

  // Getters
  bool get isLoading => _isLoading;
  List<WordRecapItem> get displayedWords => _displayedWords;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  bool get isReviewEnabled => _isReviewEnabled;

  bool get filterKanjiOnly => _filterKanjiOnly;
  bool get filterSimpleWords => _filterSimpleWords;
  bool get filterCompoundWords => _filterCompoundWords;
  bool get filterIgnoreKnown => _filterIgnoreKnown;
  String get selectedWordType => _selectedWordType;
  List<MapEntry<String, String>> get wordTypeOptions => _wordTypeOptions;

  Future<void> loadList(String levelId, String locale) async {
    _isLoading = true;
    notifyListeners();

    _allWords = await _wordRepo.getWordsForLevel(levelId);
    
    // Extract unique types for the filter options
    final types = _allWords.map((w) => w.type).whereType<String>().toSet().toList()..sort();
    _wordTypeOptions = [
      const MapEntry("Tous", "All"),
      ...types.map((t) => MapEntry(t, t))
    ];
    if (!_wordTypeOptions.any((opt) => opt.key == _selectedWordType)) {
      _selectedWordType = "Tous";
    }

    await _applyFiltersAndRefresh(locale);
    _isLoading = false;
    notifyListeners();
  }

  Future<void> setFilterKanjiOnly(bool enabled, String locale) async {
    _filterKanjiOnly = enabled;
    await _applyFiltersAndRefresh(locale);
    notifyListeners();
  }

  Future<void> setFilterSimpleWords(bool enabled, String locale) async {
    _filterSimpleWords = enabled;
    await _applyFiltersAndRefresh(locale);
    notifyListeners();
  }

  Future<void> setFilterCompoundWords(bool enabled, String locale) async {
    _filterCompoundWords = enabled;
    await _applyFiltersAndRefresh(locale);
    notifyListeners();
  }

  Future<void> setFilterIgnoreKnown(bool enabled, String locale) async {
    _filterIgnoreKnown = enabled;
    await _applyFiltersAndRefresh(locale);
    notifyListeners();
  }

  Future<void> setWordType(String type, String locale) async {
    _selectedWordType = type;
    await _applyFiltersAndRefresh(locale);
    notifyListeners();
  }

  Future<List<String>> getRevisionWordList() async {
    final revisionList = await _scoreRepo.getListItems("Reading_List");
    final revisionSet = revisionList.toSet();
    return _filteredWords
        .map((w) => w.text)
        .where((text) => revisionSet.contains(text))
        .toList();
  }

  List<String> getGameWordList() {
    return _filteredWords.map((w) => w.text).toList();
  }

  Future<void> _applyFiltersAndRefresh(String locale) async {
    final List<WordEntry> results = [];
    final revisionList = await _scoreRepo.getListItems("Reading_List");
    final revisionSet = revisionList.toSet();

    for (var word in _allWords) {
      var include = true;

      if (_filterKanjiOnly) {
        // Kanji Unicode range: 0x4E00 to 0x9FAF. All characters must be Kanji (no Hiragana/Katakana okurigana)
        include = include && word.text.runes.every((r) => r >= 0x4E00 && r <= 0x9FAF);
      }

      if (_filterSimpleWords) {
        include = include && word.text.length == 1;
      }

      if (_filterCompoundWords) {
        include = include && word.text.length > 1;
      }

      if (_filterIgnoreKnown) {
        final score = await _scoreRepo.getScore(word.text, ScoreType.reading);
        final successes = score?.successes ?? 0;
        final failures = score?.failures ?? 0;
        include = include && (successes - failures) < 10;
      }

      if (_selectedWordType != "Tous") {
        include = include && word.type == _selectedWordType;
      }

      if (include) {
        results.add(word);
      }
    }

    _filteredWords = results;
    _currentPage = 0;
    _totalPages = (_filteredWords.length / _pageSize).ceil();
    await _updateCurrentPageItems(locale);
    _isReviewEnabled = _filteredWords.any((w) => revisionSet.contains(w.text));
  }

  Future<void> nextPage(String locale) async {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      await _updateCurrentPageItems(locale);
      notifyListeners();
    }
  }

  Future<void> prevPage(String locale) async {
    if (_currentPage > 0) {
      _currentPage--;
      await _updateCurrentPageItems(locale);
      notifyListeners();
    }
  }

  Future<void> _updateCurrentPageItems(String locale) async {
    final start = _currentPage * _pageSize;
    final end = (start + _pageSize).clamp(0, _filteredWords.length);

    if (start < _filteredWords.length) {
      final subset = _filteredWords.sublist(start, end);
      final List<WordRecapItem> localItems = [];
      
      // Load meanings cache
      final meanings = await _meaningRepo.getWordMeanings(locale);

      for (var word in subset) {
        final score = await _scoreRepo.getScore(word.text, ScoreType.reading);
        final meaning = meanings[word.id];
        localItems.add(WordRecapItem(
          word: word,
          color: _getScoreColor(score?.successes ?? 0, score?.failures ?? 0),
          meaning: meaning,
        ));
      }
      _displayedWords = localItems;
    } else {
      _displayedWords = [];
    }
  }

  Color _getScoreColor(int successes, int failures) {
    final score = successes - failures;
    if (score <= -5) return Colors.red.shade200;
    if (score < 0) return Colors.orange.shade200;
    if (score == 0) return Colors.grey.shade200;
    if (score < 5) return Colors.lightGreen.shade200;
    return Colors.green.shade200;
  }
}
