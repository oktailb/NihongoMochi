import 'package:flutter/material.dart';
import '../models/kana.dart';
import '../repositories/kana_repository.dart';
import '../repositories/score_repository.dart';

class KanaProvider extends ChangeNotifier {
  final KanaRepository _repository;
  final ScoreRepository _scoreRepo;

  Map<int, List<KanaEntry>> _charactersByLine = {};
  List<int> _linesToShow = [];
  Map<String, Color> _kanaColors = {};
  int _currentPage = 0;
  int _totalPages = 0;
  List<int> _allLineKeys = [];

  // Getters
  Map<int, List<KanaEntry>> get charactersByLine => _charactersByLine;
  List<int> get linesToShow => _linesToShow;
  Map<String, Color> get kanaColors => _kanaColors;
  int get currentPage => _currentPage;
  int get totalPages => _totalPages;

  KanaProvider(this._repository, this._scoreRepo);

  Future<void> loadKana(KanaType type, {int pageSize = 5}) async {
    final allCharacters = await _repository.getKanaEntries(type);

    // Groupement par ligne (logique Kotlin)
    _charactersByLine = {};
    for (var kana in allCharacters) {
      _charactersByLine.putIfAbsent(kana.line, () => []).add(kana);
    }

    _allLineKeys = _charactersByLine.keys.toList()..sort();
    _totalPages = (_allLineKeys.length / pageSize).ceil();

    if (_currentPage >= _totalPages) {
      _currentPage = (_totalPages - 1).clamp(0, _totalPages);
    }

    await _updateCurrentPageItems(pageSize);
  }

  Future<void> _updateCurrentPageItems(int pageSize) async {
    final start = _currentPage * pageSize;
    final end = (start + pageSize).clamp(0, _allLineKeys.length);

    if (start < _allLineKeys.length) {
      _linesToShow = _allLineKeys.sublist(start, end);
    } else {
      _linesToShow = [];
    }

    await _refreshScores();
  }

  Future<void> _refreshScores() async {
    _kanaColors = {};
    for (var line in _charactersByLine.values) {
      for (var kana in line) {
        final score = await _scoreRepo.getScore(kana.character, ScoreType.recognition);
        _kanaColors[kana.character] = _getScoreColor(score?.successes ?? 0, score?.failures ?? 0);
      }
    }
    notifyListeners();
  }

  Color _getScoreColor(int successes, int failures) {
    final score = successes - failures;
    if (score <= -5) return Colors.red.shade200;
    if (score < 0) return Colors.orange.shade200;
    if (score == 0) return Colors.grey.shade200;
    if (score < 5) return Colors.lightGreen.shade200;
    return Colors.green.shade200;
  }

  void nextPage(int pageSize) {
    if (_currentPage < _totalPages - 1) {
      _currentPage++;
      _updateCurrentPageItems(pageSize);
    }
  }

  void prevPage(int pageSize) {
    if (_currentPage > 0) {
      _currentPage--;
      _updateCurrentPageItems(pageSize);
    }
  }
}
