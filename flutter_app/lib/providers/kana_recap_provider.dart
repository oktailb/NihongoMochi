import 'package:flutter/material.dart';
import '../models/kana.dart';
import '../models/quiz_models.dart';
import '../repositories/kana_repository.dart';
import '../repositories/score_repository.dart';
import '../utils/score_presentation_utils.dart';

class KanaRecapProvider extends ChangeNotifier {
  final KanaRepository _kanaRepo;
  final ScoreRepository _scoreRepo;

  KanaRecapProvider(this._kanaRepo, this._scoreRepo);

  bool _isLoading = false;
  KanaType? _currentType;
  Map<int, List<KanaEntry>> _charactersByLine = {};
  List<int> _allLineKeys = [];
  List<int> _linesToShow = [];
  Map<String, Color> _kanaColors = {};
  int _currentPage = 0;
  int _totalPages = 0;
  final int _pageSize = 8; // Number of lines per page

  bool get isLoading => _isLoading;
  KanaType? get currentType => _currentType;
  Map<int, List<KanaEntry>> get charactersByLine => _charactersByLine;
  List<int> get linesToShow => _linesToShow;
  Map<String, Color> get kanaColors => _kanaColors;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;

  Future<void> loadKana(KanaType type) async {
    _isLoading = true;
    _currentType = type;
    notifyListeners();

    final allCharacters = await _kanaRepo.getKanaEntries(type);
    
    // Group by line
    final grouped = <int, List<KanaEntry>>{};
    for (var char in allCharacters) {
      grouped.putIfAbsent(char.line, () => []).add(char);
    }
    
    _charactersByLine = grouped;
    _allLineKeys = grouped.keys.toList()..sort();
    _totalPages = (_allLineKeys.length / _pageSize).ceil();
    _currentPage = 0;

    await _refreshScores();
    _updateLinesToShow();

    _isLoading = false;
    notifyListeners();
  }

  Future<void> _refreshScores() async {
    final colorMap = <String, Color>{};
    for (var line in _charactersByLine.values) {
      for (var kana in line) {
        final entity = await _scoreRepo.getScore(kana.character, ScoreType.recognition);
        final score = LearningScore(
          successes: entity?.successes ?? 0,
          failures: entity?.failures ?? 0,
        );
        colorMap[kana.character] = ScorePresentationUtils.getScoreColor(score, Colors.white.withOpacity(0.9));
      }
    }
    _kanaColors = colorMap;
  }

  void _updateLinesToShow() {
    final start = _currentPage * _pageSize;
    final end = (start + _pageSize).clamp(0, _allLineKeys.length);
    _linesToShow = _allLineKeys.sublist(start, end);
  }

  void nextPage() {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      _updateLinesToShow();
      notifyListeners();
    }
  }

  void prevPage() {
    if (_currentPage > 0) {
      _currentPage--;
      _updateLinesToShow();
      notifyListeners();
    }
  }
}
