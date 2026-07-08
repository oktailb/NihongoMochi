import 'package:just_audio/just_audio.dart';
import '../repositories/settings_repository.dart';

class AudioService {
  final SettingsRepository _settingsRepo;
  final Map<String, AudioPlayer> _players = {};

  AudioService(this._settingsRepo);

  /// Joue un effet sonore depuis les assets (ex: 'assets/files/sounds/correct.mp3')
  Future<void> playSound(String assetPath) async {
    final volume = _settingsRepo.getAudioVolume();
    if (volume <= 0) return;

    try {
      AudioPlayer? player = _players[assetPath];

      if (player == null) {
        player = AudioPlayer();
        // just_audio gère le pré-chargement des assets automatiquement
        await player.setAsset(assetPath);
        _players[assetPath] = player;
      }

      await player.setVolume(volume);

      // On rembobine au début avant de jouer
      if (player.processingState == ProcessingState.completed) {
        await player.seek(Duration.zero);
      }

      player.play();
    } catch (e) {
      print("Erreur AudioService (playSound): $e");
    }
  }

  void stopAll() {
    for (var player in _players.values) {
      player.stop();
    }
  }

  void dispose() {
    for (var player in _players.values) {
      player.dispose();
    }
    _players.clear();
  }
}
