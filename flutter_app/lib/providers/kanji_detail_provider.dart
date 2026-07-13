import 'package:flutter/material.dart';
import '../models/dictionary.dart';
import '../repositories/dictionary_repository.dart';
import '../repositories/score_repository.dart';
import '../repositories/word_repository.dart';
import '../repositories/word_meaning_repository.dart';

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
  final WordRepository _wordRepo;
  final WordMeaningRepository _wordMeaningRepo;

  DictionaryItem? _kanji;
  bool _isLoading = false;
  bool _isInRevisionList = false;
  ComponentNode? _componentTree;
  List<DictionaryItem> _examples = [];
  final Map<String, String> _characterToIdMap = {};

  // Getters
  DictionaryItem? get kanji => _kanji;
  bool get isLoading => _isLoading;
  bool get isInRevisionList => _isInRevisionList;
  ComponentNode? get componentTree => _componentTree;
  List<DictionaryItem> get examples => _examples;
  Map<String, String> get characterToIdMap => _characterToIdMap;

  KanjiDetailProvider(
    this._dictionaryRepo,
    this._scoreRepo,
    this._wordRepo,
    this._wordMeaningRepo,
  );

  Future<void> loadKanji(String kanjiId, String locale) async {
    _isLoading = true;
    notifyListeners();

    final allItems = await _dictionaryRepo.getFullDictionary(locale);
    _kanji = allItems.firstWhere((item) => item.id == kanjiId);

    if (_kanji != null) {
      // Remplir la map character -> id
      _characterToIdMap.clear();
      for (var item in allItems) {
        if (item.character.isNotEmpty) {
          _characterToIdMap[item.character] = item.id;
        }
      }

      // 1. Liste de révision
      _isInRevisionList = await _scoreRepo.isInList("Recognition_List", _kanji!.character);

      // 2. Arbre des composants
      _componentTree = _buildComponentTree(_kanji!.character, allItems, 0);

      // 3. Exemples
      try {
        final words = await _wordRepo.getWordsContainingKanji(_kanji!.character);
        final meaningsMap = await _wordMeaningRepo.getWordMeanings(locale);
        _examples = words.map((w) {
          return DictionaryItem(
            id: w.id,
            character: w.text,
            strokeCount: 0,
            readings: [ReadingInfo(text: w.phonetics, type: 'on')],
            meanings: meaningsMap[w.id] != null ? [meaningsMap[w.id]!] : [],
          );
        }).take(10).toList();
      } catch (e) {
        print("Error loading examples in detail provider: $e");
        _examples = [];
      }
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
    for (var comp in entry.components) {
      final char = comp.text ?? comp.kanjiRef ?? '';
      if (char.isNotEmpty && char != character) {
        children.add(_buildComponentTree(char, allItems, depth + 1));
      }
    }

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
