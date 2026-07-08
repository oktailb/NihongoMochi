import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import '../models/particle_defender.dart';
import '../models/grammar_quiz.dart';
import '../repositories/exercise_repository.dart';

class ParticleDefenderProvider extends ChangeNotifier {
  final ExerciseRepository _exerciseRepo;

  ParticleDefenderProvider(this._exerciseRepo) {
    startGame();
  }

  ParticleGameState _uiState = ParticleGameState();
  ParticleGameState get uiState => _uiState;

  Timer? _gameTimer;
  final List<String> _particlesToUse = ["は", "が", "を", "に", "へ", "と", "も", "で"];
  int _enemyIdCounter = 0;

  void startGame() async {
    _uiState = ParticleGameState(
      isLoading: true,
      isGameOver: false,
      isPaused: false,
      lives: 3,
      score: 0,
    );
    notifyListeners();

    await _loadNextExercise();
    _uiState = _uiState.copyWith(isLoading: false);
    notifyListeners();

    _runGameLoop();
  }

  void pauseGame() {
    _uiState = _uiState.copyWith(isPaused: true);
    notifyListeners();
  }

  void resumeGame() {
    _uiState = _uiState.copyWith(isPaused: false);
    notifyListeners();
  }

  Future<void> _loadNextExercise() async {
    final tags = ["particule_ha", "particule_ga", "particule_ni", "particule_wo", "particule_de", "particule_he"];
    final randomTag = tags[Random().nextInt(tags.length)];
    final exercises = await _exerciseRepo.getExercisesForTag(randomTag);

    final fillBlankExercises = exercises.where((e) => e.type == ExerciseType.fillBlank).toList();

    if (fillBlankExercises.isNotEmpty) {
      final exercise = fillBlankExercises[Random().nextInt(fillBlankExercises.length)];
      final payload = _exerciseRepo.parsePayload(exercise);

      if (payload is FillBlankPayload) {
        final sentence = payload.sentence;
        final correct = payload.correct;
        final parts = sentence.split(correct);

        _uiState = _uiState.copyWith(
          currentSentencePrefix: parts.isNotEmpty ? parts[0] : "",
          currentSentenceSuffix: parts.length > 1 ? parts[1] : "",
          activeParticles: [],
        );
        _spawnWave(correct);
        return;
      }
    }

    // Fallback
    _uiState = _uiState.copyWith(
      currentSentencePrefix: "私はパン",
      currentSentenceSuffix: "食べます。",
      activeParticles: [],
    );
    _spawnWave("を");
  }

  void _spawnWave(String correctParticle) {
    final newParticles = <ParticleEnemy>[];
    final xSlots = [0.15, 0.38, 0.62, 0.85]..shuffle();

    newParticles.add(
      ParticleEnemy(
        id: _enemyIdCounter++,
        char: correctParticle,
        position: Offset(xSlots[0], -0.1),
        isCorrect: true,
        speed: 0.004 + (Random().nextDouble() * 0.002),
      ),
    );

    final distractors = _particlesToUse.where((p) => p != correctParticle).toList()..shuffle();
    for (int i = 0; i < 3; i++) {
      newParticles.add(
        ParticleEnemy(
          id: _enemyIdCounter++,
          char: distractors[i],
          position: Offset(xSlots[i + 1], -0.1),
          isCorrect: false,
          speed: 0.004 + (Random().nextDouble() * 0.002),
        ),
      );
    }

    _uiState = _uiState.copyWith(activeParticles: newParticles);
    notifyListeners();
  }

  void _runGameLoop() {
    _gameTimer?.cancel();
    _gameTimer = Timer.periodic(const Duration(milliseconds: 16), (timer) {
      if (!_uiState.isGameOver) {
        if (!_uiState.isPaused) {
          _updateParticles();
        }
      } else {
        timer.cancel();
      }
    });
  }

  void _updateParticles() {
    final updated = _uiState.activeParticles.map((enemy) {
      return enemy.copyWith(
        position: Offset(enemy.position.dx, enemy.position.dy + enemy.speed),
      );
    }).toList();

    final correctReachedBottom = updated.any((e) => e.isCorrect && e.position.dy > 1.0);

    if (correctReachedBottom) {
      final newLives = _uiState.lives - 1;
      if (newLives <= 0) {
        _uiState = _uiState.copyWith(isGameOver: true, lives: 0, activeParticles: updated);
      } else {
        _uiState = _uiState.copyWith(lives: newLives, activeParticles: []);
      }
    } else {
      _uiState = _uiState.copyWith(activeParticles: updated.where((e) => e.position.dy <= 1.1).toList());
    }

    if (_uiState.activeParticles.isEmpty && !_uiState.isGameOver) {
      _loadNextExercise();
    }
    notifyListeners();
  }

  void onShipMove(double x) {
    if (_uiState.isPaused) return;
    _uiState = _uiState.copyWith(shipX: x.clamp(0.0, 1.0));
    notifyListeners();
  }

  void onShoot() {
    if (_uiState.isGameOver || _uiState.isPaused) return;

    final shipX = _uiState.shipX;
    // Find particle aligned with ship that is far enough down
    ParticleEnemy? target;
    double minY = 2.0;

    for (var enemy in _uiState.activeParticles) {
      if (enemy.position.dy > 0.4 && (enemy.position.dx - shipX).abs() < 0.12) {
        if (enemy.position.dy < minY) {
          minY = enemy.position.dy;
          target = enemy;
        }
      }
    }

    if (target != null) {
      if (target.isCorrect) {
        _uiState = _uiState.copyWith(score: _uiState.score + 10, activeParticles: []);
        _loadNextExercise();
      } else {
        final newLives = _uiState.lives - 1;
        _uiState = _uiState.copyWith(lives: newLives, isGameOver: newLives <= 0);
      }
      notifyListeners();
    }
  }

  @override
  void dispose() {
    _gameTimer?.cancel();
    super.dispose();
  }
}
