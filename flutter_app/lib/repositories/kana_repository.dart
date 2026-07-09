import 'dart:convert';
import '../models/kana.dart';
import '../services/resource_loader.dart';

class KanaRepository {
  final ResourceLoader _loader;
  List<KanaEntry>? _hiraganaCache;
  List<KanaEntry>? _katakanaCache;

  KanaRepository(this._loader);

  Future<List<KanaEntry>> getKanaEntries(KanaType type) async {
    if (type == KanaType.hiragana && _hiraganaCache != null) return _hiraganaCache!;
    if (type == KanaType.katakana && _katakanaCache != null) return _katakanaCache!;

    final assetPath = type == KanaType.hiragana
        ? 'assets/files/kana/hiragana.json'
        : 'assets/files/kana/katakana.json';
    final localPath = type == KanaType.hiragana ? 'kana/hiragana.json' : 'kana/katakana.json';

    try {
      final jsonString = await _loader.loadString(localPath, assetPath: assetPath);
      
      final Map<String, dynamic> jsonData = json.decode(jsonString);
      final result = KanaData.fromJson(jsonData).characters;

      if (type == KanaType.hiragana) _hiraganaCache = result;
      if (type == KanaType.katakana) _katakanaCache = result;

      return result;
    } catch (e) {
      print("Error loading kana data: $e");
      return [];
    }
  }

  Future<List<NumberEntry>> getNumberEntries() async {
    try {
      final jsonString = await _loader.loadString('suuji.json', assetPath: 'assets/files/suuji.json');
      final Map<String, dynamic> jsonData = json.decode(jsonString);
      return NumberData.fromJson(jsonData).numbers;
    } catch (e) {
      print("Error loading numbers: $e");
      return [];
    }
  }
}
