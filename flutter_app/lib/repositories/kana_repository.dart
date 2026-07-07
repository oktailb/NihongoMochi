import 'dart:convert';
import 'package:flutter/services.dart';
import '../models/kana.dart';

class KanaRepository {
  List<KanaEntry>? _hiraganaCache;
  List<KanaEntry>? _katakanaCache;

  Future<List<KanaEntry>> getKanaEntries(KanaType type) async {
    if (type == KanaType.hiragana && _hiraganaCache != null) return _hiraganaCache!;
    if (type == KanaType.katakana && _katakanaCache != null) return _katakanaCache!;

    final fileName = type == KanaType.hiragana
        ? 'assets/files/kana/hiragana.json'
        : 'assets/files/kana/katakana.json';

    try {
      final String jsonString = await rootBundle.loadString(fileName);
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
      final String jsonString = await rootBundle.loadString('assets/files/suuji.json');
      final Map<String, dynamic> jsonData = json.decode(jsonString);
      return NumberData.fromJson(jsonData).numbers;
    } catch (e) {
      print("Error loading numbers: $e");
      return [];
    }
  }
}
