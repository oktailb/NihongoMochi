import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/kanji_sort_order.dart';
import 'game_recap_provider.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';

class WritingRecapProvider extends ChangeNotifier {
  final LevelContentProvider _contentProvider;
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;

  WritingRecapProvider(this._contentProvider, this._dictionaryRepo, this._scoreRepo);

  bool _isLoading = false;
  List<DictionaryItem> _allKanji = [];
  List<RecapItem> _kanjiListWithColors = [];
  int _currentPage = 0;
  int _totalPages = 0;
  final int _pageSize = 80;
  KanjiSortOrder _sortOrder = KanjiSortOrder.defaultOrder;
  bool _isReviewEnabled = false;

  bool get isLoading => _isLoading;
  List<RecapItem> get kanjiListWithColors => _kanjiListWithColors;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  KanjiSortOrder get sortOrder => _sortOrder;
  bool get isReviewEnabled => _isReviewEnabled;

  Future<void> loadLevel(String level, String locale) async {
    _isLoading = true;
    notifyListeners();

    // Technical key: if level is custom list, use ScoreType.writing, otherwise ScoreType.recognition (matching Kotlin)
    final scoreType = level == "user_custom_list" ? ScoreType.writing : ScoreType.recognition;
    final characters = await _contentProvider.getItemsForLevel(level, scoreType, locale);

    final all = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in all) k.character: k};

    _allKanji = characters.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList();

    await _applySortAndRefresh();
    _isLoading = false;
    notifyListeners();
  }

  Future<void> setSortOrder(KanjiSortOrder order) async {
    _sortOrder = order;
    await _applySortAndRefresh();
    notifyListeners();
  }

  Future<List<String>> getRevisionKanjiForLevel() async {
    final revisionList = await _scoreRepo.getListItems("Writing_List");
    final revisionSet = revisionList.toSet();
    return _allKanji
        .map((k) => k.character)
        .where((char) => revisionSet.contains(char))
        .toList();
  }

  Future<void> _applySortAndRefresh() async {
    switch (_sortOrder) {
      case KanjiSortOrder.frequency:
        _allKanji.sort((a, b) {
          final fa = a.frequency ?? 99999;
          final fb = b.frequency ?? 99999;
          return fa.compareTo(fb);
        });
        break;
      case KanjiSortOrder.strokes:
        _allKanji.sort((a, b) => a.strokeCount.compareTo(b.strokeCount));
        break;
      case KanjiSortOrder.defaultOrder:
        break;
    }

    _currentPage = 0;
    _totalPages = (_allKanji.length / _pageSize).ceil();
    await _updateCurrentPageItems();
    await _checkReviewAvailability();
  }

  Future<void> nextPage() async {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      await _updateCurrentPageItems();
      notifyListeners();
    }
  }

  Future<void> prevPage() async {
    if (_currentPage > 0) {
      _currentPage--;
      await _updateCurrentPageItems();
      notifyListeners();
    }
  }

  Future<void> _updateCurrentPageItems() async {
    final start = _currentPage * _pageSize;
    final end = (start + _pageSize).clamp(0, _allKanji.length);

    if (start < _allKanji.length) {
      final subset = _allKanji.sublist(start, end);
      final List<RecapItem> localItems = [];
      for (var kanji in subset) {
        final score = await _scoreRepo.getScore(kanji.character, ScoreType.writing);
        localItems.add(RecapItem(
          kanji: kanji,
          color: _getScoreColor(score?.successes ?? 0, score?.failures ?? 0),
        ));
      }
      _kanjiListWithColors = localItems;
    } else {
      _kanjiListWithColors = [];
    }
  }

  Future<void> _checkReviewAvailability() async {
    final revisionList = await _scoreRepo.getListItems("Writing_List");
    final revisionSet = revisionList.toSet();
    _isReviewEnabled = _allKanji.any((k) => revisionSet.contains(k.character));
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
