import 'dart:math';
import '../models/crossword.dart';
import '../repositories/word_repository.dart';

class CrosswordGenerator {
  final List<WordEntry> availableWords;
  final int targetWordCount;
  final CrosswordMode mode;
  final int gridSize;

  CrosswordGenerator({
    required this.availableWords,
    required this.targetWordCount,
    this.mode = CrosswordMode.kanas,
    this.gridSize = 16,
  });

  List<List<String>> _grid = [];
  final List<CrosswordWord> _placedWords = [];
  final Random _random = Random();

  String _cleanPhonetics(String p) {
    return p.split("/").first.replaceAll(".", "").replaceAll(" ", "");
  }

  bool _isPureKanji(String text) {
    if (text.isEmpty) return false;
    return text.runes.all((r) => (r >= 0x4E00 && r <= 0x9FAF) || (r >= 0x3400 && r <= 0x4DBF));
  }

  Map<String, dynamic> generate() {
    _grid = List.generate(gridSize, (_) => List.generate(gridSize, (_) => ""));
    _placedWords.clear();

    final candidates = availableWords
        .map((entry) {
          final cleanP = _cleanPhonetics(entry.phonetics);
          final solution = mode == CrosswordMode.kanjis ? entry.text : cleanP;
          return {'entry': entry, 'solution': solution, 'length': solution.length};
        })
        .where((item) {
          final solution = item['solution'] as String;
          final entry = item['entry'] as WordEntry;
          final bool lengthOk = solution.length >= 2 && solution.length <= 8;
          if (mode == CrosswordMode.kanjis) {
            return lengthOk && _isPureKanji(entry.text);
          }
          return lengthOk;
        })
        .toList();

    if (candidates.isEmpty) return {'cells': <CrosswordCell>[], 'words': <CrosswordWord>[]};

    candidates.shuffle();
    candidates.sort((a, b) => (b['length'] as int).compareTo(a['length'] as int));

    // Placer le premier mot au centre
    final first = candidates[0];
    final firstEntry = first['entry'] as WordEntry;
    _placeWord(firstEntry, gridSize ~/ 2, (gridSize - (first['length'] as int)) ~/ 2, true);

    int candidatesIdx = 1;
    int attempts = 0;
    while (_placedWords.length < targetWordCount && candidatesIdx < candidates.length && attempts < 100) {
      final entry = candidates[candidatesIdx]['entry'] as WordEntry;
      if (_tryPlaceWord(entry)) {
        attempts = 0;
      } else {
        attempts++;
      }
      candidatesIdx++;
    }

    return _finalizeGrid();
  }

  bool _tryPlaceWord(WordEntry wordEntry) {
    final word = mode == CrosswordMode.kanjis ? wordEntry.text : _cleanPhonetics(wordEntry.phonetics);
    final List<Map<String, dynamic>> possiblePositions = [];

    for (var placed in _placedWords) {
      for (int i = 0; i < placed.word.length; i++) {
        for (int j = 0; j < word.length; j++) {
          if (placed.word[i] == word[j]) {
            if (placed.isHorizontal) {
              final startRow = placed.row - j;
              final startCol = placed.col + i;
              if (_canPlace(word, startRow, startCol, false)) {
                possiblePositions.add({'r': startRow, 'c': startCol, 'h': false});
              }
            } else {
              final startRow = placed.row + i;
              final startCol = placed.col - j;
              if (_canPlace(word, startRow, startCol, true)) {
                possiblePositions.add({'r': startRow, 'c': startCol, 'h': true});
              }
            }
          }
        }
      }
    }

    if (possiblePositions.isEmpty) return false;
    final pos = possiblePositions[_random.nextInt(possiblePositions.length)];
    _placeWord(wordEntry, pos['r'], pos['c'], pos['h']);
    return true;
  }

  bool _canPlace(String word, int row, int col, bool isHorizontal) {
    if (row < 0 || col < 0) return false;
    if (isHorizontal && col + word.length > gridSize) return false;
    if (!isHorizontal && row + word.length > gridSize) return false;

    // Check boundary before the start of the word (unconditionally)
    if (isHorizontal) {
      if (_isOccupied(row, col - 1)) return false;
    } else {
      if (_isOccupied(row - 1, col)) return false;
    }

    // Check boundary after the end of the word (unconditionally)
    if (isHorizontal) {
      if (_isOccupied(row, col + word.length)) return false;
    } else {
      if (_isOccupied(row + word.length, col)) return false;
    }

    for (int i = 0; i < word.length; i++) {
      final r = isHorizontal ? row : row + i;
      final c = isHorizontal ? col + i : col;
      final existing = _grid[r][c];

      if (existing != "" && existing != word[i]) return false;

      if (existing == "") {
        // Parallel neighbor checks to keep words isolated
        if (isHorizontal) {
          if (_isOccupied(r - 1, c) || _isOccupied(r + 1, c)) return false;
        } else {
          if (_isOccupied(r, c - 1) || _isOccupied(r, c + 1)) return false;
        }
      }
    }
    return true;
  }

  bool _isOccupied(int r, int c) {
    if (r < 0 || r >= gridSize || c < 0 || c >= gridSize) return false;
    return _grid[r][c] != "";
  }

  void _placeWord(WordEntry entry, int row, int col, bool isHorizontal) {
    final solution = mode == CrosswordMode.kanjis ? entry.text : _cleanPhonetics(entry.phonetics);
    _placedWords.add(CrosswordWord(
      number: _placedWords.length + 1,
      word: solution,
      kanji: entry.text,
      meaning: "", // Rempli par le Provider plus tard
      phonetics: entry.phonetics,
      row: row,
      col: col,
      isHorizontal: isHorizontal,
    ));
    for (int i = 0; i < solution.length; i++) {
      final r = isHorizontal ? row : row + i;
      final c = isHorizontal ? col + i : col;
      _grid[r][c] = solution[i];
    }
  }

  Map<String, dynamic> _finalizeGrid() {
    final List<CrosswordCell> cells = [];
    final sortedWords = List<CrosswordWord>.from(_placedWords)
      ..sort((a, b) => a.row != b.row ? a.row.compareTo(b.row) : a.col.compareTo(b.col));

    final finalWords = sortedWords.asMap().entries.map((e) => e.value.copyWith()).toList();

    for (int r = 0; r < gridSize; r++) {
      for (int c = 0; c < gridSize; c++) {
        final char = _grid[r][c];
        final wordAtStart = finalWords.cast<CrosswordWord?>().firstWhere(
          (w) => w?.row == r && w?.col == c,
          orElse: () => null,
        );
        cells.add(CrosswordCell(
          r: r,
          c: c,
          solution: char,
          isBlack: char == "",
          number: wordAtStart?.number,
        ));
      }
    }
    return {'cells': cells, 'words': finalWords};
  }
}

extension AllExtension<T> on Iterable<T> {
  bool all(bool Function(T) test) {
    for (var element in this) {
      if (!test(element)) return false;
    }
    return true;
  }
}
