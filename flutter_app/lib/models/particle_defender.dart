import 'package:flutter/material.dart';

class ParticleEnemy {
  final int id;
  final String char;
  final Offset position;
  final bool isCorrect;
  final double speed;

  ParticleEnemy({
    required this.id,
    required this.char,
    required this.position,
    required this.isCorrect,
    required this.speed,
  });

  ParticleEnemy copyWith({Offset? position}) {
    return ParticleEnemy(
      id: id,
      char: char,
      position: position ?? this.position,
      isCorrect: isCorrect,
      speed: speed,
    );
  }
}

class ParticleGameState {
  final String currentSentencePrefix;
  final String currentSentenceSuffix;
  final List<ParticleEnemy> activeParticles;
  final int score;
  final int lives;
  final bool isGameOver;
  final bool isPaused;
  final bool isLoading;
  final double shipX;

  ParticleGameState({
    this.currentSentencePrefix = "",
    this.currentSentenceSuffix = "",
    this.activeParticles = const [],
    this.score = 0,
    this.lives = 3,
    this.isGameOver = false,
    this.isPaused = false,
    this.isLoading = true,
    this.shipX = 0.5,
  });

  ParticleGameState copyWith({
    String? currentSentencePrefix,
    String? currentSentenceSuffix,
    List<ParticleEnemy>? activeParticles,
    int? score,
    int? lives,
    bool? isGameOver,
    bool? isPaused,
    bool? isLoading,
    double? shipX,
  }) {
    return ParticleGameState(
      currentSentencePrefix: currentSentencePrefix ?? this.currentSentencePrefix,
      currentSentenceSuffix: currentSentenceSuffix ?? this.currentSentenceSuffix,
      activeParticles: activeParticles ?? this.activeParticles,
      score: score ?? this.score,
      lives: lives ?? this.lives,
      isGameOver: isGameOver ?? this.isGameOver,
      isPaused: isPaused ?? this.isPaused,
      isLoading: isLoading ?? this.isLoading,
      shipX: shipX ?? this.shipX,
    );
  }
}
