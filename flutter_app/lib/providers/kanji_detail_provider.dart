import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';

class ComponentNode {
  final String? id;
  final String character;
  final List<String> onReadings;
  final List<ComponentNode> children;

  ComponentNode({
    this.id,
    required this.character,
    this.onReadings = const [],
    this.children = const [],
  });
}

class KanjiDetailProvider extends ChangeNotifier {
  final DictionaryRepository _dictionaryRepo;
  final ScoreRepository _scoreRepo;

  DictionaryItem? _kanji;
  bool _isLoading = false;
  bool _isInRevisionList = false;
  ComponentNode? _componentTree;
  List<DictionaryItem> _examples = [];

  // Getters
  DictionaryItem? get kanji => _kanji;
  bool get isLoading => _isLoading;
  bool get isInRevisionList => _isInRevisionList;
  ComponentNode? get componentTree => _componentTree;
  List<DictionaryItem> get examples => _examples;

  KanjiDetailProvider(this._dictionaryRepo, this._scoreRepo);

  Future<void> loadKanji(String kanjiId, String locale) async {
    _isLoading = true;
    notifyListeners();

    final allItems = await _dictionaryRepo.getFullDictionary(locale);
    _kanji = allItems.firstWhere((item) => item.id == kanjiId);

    if (_kanji != null) {
      // 1. Liste de révision
      _isInRevisionList = await _scoreRepo.isInList("Recognition_List", _kanji!.character);

      // 2. Arbre des composants
      _componentTree = _buildComponentTree(_kanji!.character, allItems, 0);

      // 3. Exemples (Recherche simple dans le dictionnaire pour l'instant)
      _examples = allItems
          .where((item) => item.character.contains(_kanji!.character) && item.id != _kanji!.id)
          .take(10)
          .toList();
    }

    _isLoading = false;
    notifyListeners();
  }

  ComponentNode _buildComponentTree(String character, List<DictionaryItem> allItems, int depth) {
    if (depth > 5) return ComponentNode(character: character);

    final entry = allItems.firstWhere(
      (item) => item.character == character,
      orElse: () => DictionaryItem(id: '', character: character, readings: [], strokeCount: 0, meanings: []),
    );

    final List<ComponentNode> children = [];
    // Dans les données JSON réelles, les composants sont dans 'item.categories' ou un champ dédié
    // Pour cette démo, on simule l'extraction si elle était présente.

    return ComponentNode(
      id: entry.id.isNotEmpty ? entry.id : null,
      character: character,
      onReadings: entry.readings.where((r) => r.type == 'on').map((r) => r.text).toList(),
      children: children,
    );
  }

  Future<void> toggleRevisionList() async {
    if (_kanji == null) return;

    if (_isInRevisionList) {
      await _scoreRepo.removeItemFromList("Recognition_List", _kanji!.character);
    } else {
      await _scoreRepo.addItemToList("Recognition_List", _kanji!.character);
    }
    _isInRevisionList = !_isInRevisionList;
    notifyListeners();
  }
}
