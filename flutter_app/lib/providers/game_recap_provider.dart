import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/kanji_sort_order.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';

class RecapItem {
  final DictionaryItem kanji;
  final Color color;

  RecapItem({required this.kanji, required this.color});
}

class GameRecapProvider extends ChangeNotifier {
  final LevelContentProvider _contentProvider;
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;

  GameRecapProvider(this._contentProvider, this._dictionaryRepo, this._scoreRepo);

  List<RecapItem> _kanjiListWithColors = [];
  int _currentPage = 0;
  int _totalPages = 0;
  bool _isReviewEnabled = false;
  KanjiSortOrder _sortOrder = KanjiSortOrder.defaultOrder;
  bool _isLoading = false;

  List<DictionaryItem> _originalKanjiEntries = [];
  List<DictionaryItem> _allKanjiEntries = [];
  final int _pageSize = 80;

  // Getters
  List<RecapItem> get kanjiListWithColors => _kanjiListWithColors;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  bool get isReviewEnabled => _isReviewEnabled;
  KanjiSortOrder get sortOrder => _sortOrder;
  bool get isLoading => _isLoading;

  Future<void> loadLevel(String level, String gameMode, String locale) async {
    debugPrint("[RECAP_PROVIDER] loadLevel called for level: $level, gameMode: $gameMode, locale: $locale");
    _isLoading = true;
    notifyListeners();

    final scoreType = gameMode == "meaning" ? ScoreType.recognition : ScoreType.reading;
    final characters = await _contentProvider.getItemsForLevel(level, scoreType, locale);

    final allKanji = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in allKanji) k.character: k};

    _originalKanjiEntries = characters.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList();
    _allKanjiEntries = List.from(_originalKanjiEntries);

    debugPrint("[RECAP_PROVIDER] Loaded ${characters.length} characters for level, resolved ${_originalKanjiEntries.length} dictionary entries.");

    await _applySortAndRefresh(gameMode);
    _isLoading = false;
    notifyListeners();
  }

  Future<void> setSortOrder(KanjiSortOrder order, String gameMode) async {
    debugPrint("[RECAP_PROVIDER] setSortOrder called with order: $order");
    _sortOrder = order;
    await _applySortAndRefresh(gameMode);
    notifyListeners();
  }

  Future<List<String>> getRevisionKanjiForLevel(String gameMode) async {
    final listName = gameMode == "meaning" ? "Recognition_List" : "Reading_List";
    final revisionList = await _scoreRepo.getListItems(listName);
    final revisionSet = revisionList.toSet();
    final result = _allKanjiEntries
        .map((k) => k.character)
        .where((char) => revisionSet.contains(char))
        .toList();
    debugPrint("[RECAP_PROVIDER] getRevisionKanjiForLevel returning ${result.length} characters out of ${revisionList.length} global revision items.");
    return result;
  }

  Future<void> _applySortAndRefresh(String gameMode) async {
    debugPrint("[RECAP_PROVIDER] _applySortAndRefresh starting with sortOrder: $_sortOrder");
    _allKanjiEntries = List.from(_originalKanjiEntries);
    switch (_sortOrder) {
      case KanjiSortOrder.frequency:
        _allKanjiEntries.sort((a, b) {
          final fa = a.frequency ?? 99999;
          final fb = b.frequency ?? 99999;
          return fa.compareTo(fb);
        });
        break;
      case KanjiSortOrder.strokes:
        _allKanjiEntries.sort((a, b) => a.strokeCount.compareTo(b.strokeCount));
        break;
      case KanjiSortOrder.defaultOrder:
        // Keep original order
        break;
    }

    _currentPage = 0;
    _totalPages = (_allKanjiEntries.length / _pageSize).ceil();
    await _updateCurrentPageItems(gameMode);
    await _checkReviewAvailability(gameMode);
  }

  Future<void> nextPage(String gameMode) async {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      await _updateCurrentPageItems(gameMode);
      notifyListeners();
    }
  }

  Future<void> prevPage(String gameMode) async {
    if (_currentPage > 0) {
      _currentPage--;
      await _updateCurrentPageItems(gameMode);
      notifyListeners();
    }
  }

  Future<void> _updateCurrentPageItems(String gameMode) async {
    final scoreType = gameMode == "meaning" ? ScoreType.recognition : ScoreType.reading;
    final startIndex = _currentPage * _pageSize;
    final endIndex = (startIndex + _pageSize).clamp(0, _allKanjiEntries.length);

    debugPrint("[RECAP_PROVIDER] _updateCurrentPageItems: mode=$gameMode, page=$_currentPage/$_totalPages, range=$startIndex..$endIndex");

    if (startIndex < _allAllEntriesLength) {
       final subset = _allKanjiEntries.sublist(startIndex, endIndex);
       final List<RecapItem> localItems = [];
       for (var kanji in subset) {
         final score = await _scoreRepo.getScore(kanji.character, scoreType);
         if (score != null && (score.successes > 0 || score.failures > 0)) {
           debugPrint("[RECAP_PROVIDER]   Score updated: ${kanji.character} -> successes=${score.successes}, failures=${score.failures}");
         }
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

  int get _allAllEntriesLength => _allKanjiEntries.length;

  Future<void> _checkReviewAvailability(String gameMode) async {
    final listName = gameMode == "meaning" ? "Recognition_List" : "Reading_List";
    final revisionList = await _scoreRepo.getListItems(listName);
    final revisionSet = revisionList.toSet();
    _isReviewEnabled = _allKanjiEntries.any((k) => revisionSet.contains(k.character));
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
