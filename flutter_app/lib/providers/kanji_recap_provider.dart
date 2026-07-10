import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../models/quiz_models.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../services/level_content_provider.dart';
import '../utils/score_presentation_utils.dart';

enum KanjiSortOrder { defaultOrder, frequency, strokes }

class KanjiRecapProvider extends ChangeNotifier {
  final LevelContentProvider _contentProvider;
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;

  KanjiRecapProvider(this._contentProvider, this._dictionaryRepo, this._scoreRepo);

  bool _isLoading = false;
  List<DictionaryItem> _allKanji = [];
  List<MapEntry<DictionaryItem, Color>> _currentPageItems = [];
  int _currentPage = 0;
  int _totalPages = 0;
  final int _pageSize = 80;

  String _gameMode = "meaning"; // "meaning" or "reading"
  KanjiSortOrder _sortOrder = KanjiSortOrder.defaultOrder;
  bool _isReviewEnabled = false;

  bool get isLoading => _isLoading;
  List<MapEntry<DictionaryItem, Color>> get currentPageItems => _currentPageItems;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;
  String get gameMode => _gameMode;
  KanjiSortOrder get sortOrder => _sortOrder;
  bool get isReviewEnabled => _isReviewEnabled;

  Future<void> loadLevel(String levelId, String locale) async {
    _isLoading = true;
    notifyListeners();

    final characters = await _contentProvider.getItemsForLevel(levelId, ScoreType.recognition, locale);
    final allDictionary = await _dictionaryRepo.getFullDictionary(locale);
    final kanjiMap = {for (var k in allDictionary) k.character: k};

    _allKanji = characters.map((c) => kanjiMap[c]).whereType<DictionaryItem>().toList();

    _applySort();
    await _refreshCurrentPage();

    _isLoading = false;
    notifyListeners();
  }

  void setGameMode(String mode) {
    _gameMode = mode;
    _refreshCurrentPage();
    notifyListeners();
  }

  void setSortOrder(KanjiSortOrder order) {
    _sortOrder = order;
    _applySort();
    _refreshCurrentPage();
    notifyListeners();
  }

  void _applySort() {
    switch (_sortOrder) {
      case KanjiSortOrder.frequency:
        // Frequency is not directly in DictionaryItem currently, let's assume it might be in metadata or use id as proxy
        _allKanji.sort((a, b) => (int.tryParse(a.id) ?? 0).compareTo(int.tryParse(b.id) ?? 0));
        break;
      case KanjiSortOrder.strokes:
        _allKanji.sort((a, b) => a.strokeCount.compareTo(b.strokeCount));
        break;
      case KanjiSortOrder.defaultOrder:
        // Keep as is from LevelContentProvider
        break;
    }
    _currentPage = 0;
    _totalPages = (_allKanji.length / _pageSize).ceil();
  }

  Future<void> _refreshCurrentPage() async {
    final start = _currentPage * _pageSize;
    final end = (start + _pageSize).clamp(0, _allKanji.length);
    final pageKanji = _allKanji.sublist(start, end);

    final scoreType = _gameMode == "meaning" ? ScoreType.recognition : ScoreType.reading;
    final listName = _gameMode == "meaning" ? "Recognition_List" : "Reading_List";
    
    final revisionList = await _scoreRepo.getListItems(listName);

    final List<MapEntry<DictionaryItem, Color>> items = [];
    bool reviewAvailable = false;

    for (var kanji in pageKanji) {
      final entity = await _scoreRepo.getScore(kanji.character, scoreType);
      final score = LearningScore(
        successes: entity?.successes ?? 0,
        failures: entity?.failures ?? 0,
      );
      items.add(MapEntry(kanji, ScorePresentationUtils.getScoreColor(score, Colors.white.withOpacity(0.9))));
      if (revisionList.contains(kanji.character)) {
        reviewAvailable = true;
      }
    }

    _currentPageItems = items;
    _isReviewEnabled = reviewAvailable;
  }

  void nextPage() async {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      await _refreshCurrentPage();
      notifyListeners();
    }
  }

  void prevPage() async {
    if (_currentPage > 0) {
      _currentPage--;
      await _refreshCurrentPage();
      notifyListeners();
    }
  }
}
