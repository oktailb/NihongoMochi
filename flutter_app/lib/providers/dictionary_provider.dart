import 'package:flutter/material.dart';
import 'package:google_mlkit_digital_ink_recognition/google_mlkit_digital_ink_recognition.dart';
import 'dart:ui' as ui;
import '../models/dictionary.dart';
import '../repositories/dictionary_repository.dart';
import '../utils/romaji_to_kana.dart';
import '../services/handwriting_service.dart';

enum SearchMode { reading, meaning }

class DictionaryProvider extends ChangeNotifier {
  final DictionaryRepository _repository = DictionaryRepository();
  final HandwritingService _handwritingService = HandwritingService();

  List<DictionaryItem> _allKanji = [];
  List<DictionaryItem> _filteredResults = [];

  bool _isLoading = true;
  SearchMode _searchMode = SearchMode.reading;
  String _textQuery = "";
  String _strokeQuery = "";
  String _selectedLevelId = "ALL";

  List<String>? _drawingCandidates;
  final List<Stroke> _currentStrokes = [];

  // Getters
  List<DictionaryItem> get results => _filteredResults;
  bool get isLoading => _isLoading;
  SearchMode get searchMode => _searchMode;
  String get textQuery => _textQuery;
  String get strokeQuery => _strokeQuery;
  String get selectedLevelId => _selectedLevelId;
  ModelStatus get modelStatus => _handwritingService.status;
  List<Stroke> get currentStrokes => _currentStrokes;

  DictionaryProvider() {
    _init();
  }

  Future<void> _init() async {
    await _handwritingService.checkModel();
    await loadData();
  }

  Future<void> loadData() async {
    _isLoading = true;
    notifyListeners();

    final String locale = ui.PlatformDispatcher.instance.locale.languageCode;
    _allKanji = await _repository.getFullDictionary(locale);

    _isLoading = false;
    applyFilters();
  }

  void setSearchMode(SearchMode mode) {
    _searchMode = mode;
    applyFilters();
  }

  void onSearchTextChange(String newText) {
    String finalText = newText;
    if (_searchMode == SearchMode.reading && newText.length > _textQuery.length) {
      final replacement = RomajiToKana.checkReplacement(newText);
      if (replacement != null) {
        final entry = replacement.entries.first;
        finalText = newText.substring(0, newText.length - entry.key) + entry.value;
      }
    }
    _textQuery = finalText;
    applyFilters();
  }

  void setStrokeQuery(String value) {
    _strokeQuery = value;
    applyFilters();
  }

  void setLevel(String levelId) {
    _selectedLevelId = levelId;
    applyFilters();
  }

  void addStroke(Stroke stroke) {
    _currentStrokes.add(stroke);
    _recognizeDrawing();
  }

  void clearDrawing() {
    _currentStrokes.clear();
    _drawingCandidates = null;
    applyFilters();
  }

  Future<void> _recognizeDrawing() async {
    if (_currentStrokes.isEmpty) return;

    _drawingCandidates = await _handwritingService.recognize(_currentStrokes);
    if (_drawingCandidates != null && _drawingCandidates!.isNotEmpty) {
      if (_selectedLevelId != "ALL") {
        _selectedLevelId = "ALL";
      }
    }
    applyFilters();
  }

  Future<void> downloadModel() async {
    await _handwritingService.downloadModel();
    notifyListeners();
  }

  void applyFilters() {
    Iterable<DictionaryItem> filtered = _allKanji;

    if (_selectedLevelId != "ALL") {
      switch (_selectedLevelId.toLowerCase()) {
        case "native_challenge":
          filtered = filtered.where((item) =>
            item.levelIds.isEmpty && item.readings.isNotEmpty);
          break;
        case "no_reading":
          filtered = filtered.where((item) =>
            item.levelIds.isEmpty && item.readings.isEmpty && item.meanings.isNotEmpty);
          break;
        case "no_meaning":
          filtered = filtered.where((item) =>
            item.levelIds.isEmpty && item.meanings.isEmpty);
          break;
        default:
          filtered = filtered.where((item) =>
            item.levelIds.any((l) => l.toUpperCase() == _selectedLevelId.toUpperCase()));
      }
    }

    final strokes = int.tryParse(_strokeQuery);
    if (strokes != null) {
      filtered = filtered.where((item) => item.strokeCount == strokes);
    }

    if (_drawingCandidates != null && _drawingCandidates!.isNotEmpty) {
      final candidates = _drawingCandidates!.toSet();
      filtered = filtered.where((item) => candidates.contains(item.character));
    }

    final query = _textQuery.trim().toLowerCase();
    if (query.isNotEmpty) {
      if (_searchMode == SearchMode.reading) {
        final cleanQuery = query.replaceAll(".", "");
        filtered = filtered.where((item) => item.readings.any(
          (r) => r.text.replaceAll(".", "").toLowerCase().contains(cleanQuery)
        ));
      } else {
        filtered = filtered.where((item) => item.meanings.any(
          (m) => m.toLowerCase().contains(query)
        ));
      }
    }

    _filteredResults = filtered.toList();
    notifyListeners();
  }

  @override
  void dispose() {
    _handwritingService.dispose();
    super.dispose();
  }
}
