import 'package:json_annotation/json_annotation.dart';

part 'shiritori.g.dart';

@JsonSerializable()
class ShiritoriWord {
  final String word;
  final String phonetics;
  final String meaning;
  final bool isPlayer;
  final int timestamp;

  ShiritoriWord({
    required this.word,
    required this.phonetics,
    required this.meaning,
    required this.isPlayer,
    required this.timestamp,
  });

  factory ShiritoriWord.fromJson(Map<String, dynamic> json) => _$ShiritoriWordFromJson(json);
  Map<String, dynamic> toJson() => _$ShiritoriWordToJson(this);
}

enum ShiritoriGameState {
  idle,
  loading,
  playerTurn,
  aiTurn,
  gameOver,
  paused
}

@JsonSerializable()
class ShiritoriGameResult {
  final String levelId;
  final int score;
  final int timeSeconds;
  final int timestamp;

  ShiritoriGameResult({
    required this.levelId,
    required this.score,
    required this.timeSeconds,
    required this.timestamp,
  });

  factory ShiritoriGameResult.fromJson(Map<String, dynamic> json) => _$ShiritoriGameResultFromJson(json);
  Map<String, dynamic> toJson() => _$ShiritoriGameResultToJson(this);
}

class ShiritoriGameStateData {
  final List<ShiritoriWord> playedWords;
  final String lastKana;
  final int score;
  final int gameTimeSeconds;
  final List<String> usedPhonetics;
  final ShiritoriGameState gameState;

  ShiritoriGameStateData({
    required this.playedWords,
    required this.lastKana,
    required this.score,
    required this.gameTimeSeconds,
    required this.usedPhonetics,
    required this.gameState,
  });

  factory ShiritoriGameStateData.fromJson(Map<String, dynamic> json) {
    return ShiritoriGameStateData(
      playedWords: (json['playedWords'] as List)
          .map((e) => ShiritoriWord.fromJson(e as Map<String, dynamic>))
          .toList(),
      lastKana: json['lastKana'] as String,
      score: json['score'] as int,
      gameTimeSeconds: json['gameTimeSeconds'] as int,
      usedPhonetics: (json['usedPhonetics'] as List).cast<String>(),
      gameState: ShiritoriGameState.values.firstWhere(
        (e) => e.toString().split('.').last == json['gameState'],
        orElse: () => ShiritoriGameState.idle,
      ),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'playedWords': playedWords.map((e) => e.toJson()).toList(),
      'lastKana': lastKana,
      'score': score,
      'gameTimeSeconds': gameTimeSeconds,
      'usedPhonetics': usedPhonetics,
      'gameState': gameState.toString().split('.').last,
    };
  }
}
