import 'package:just_audio/just_audio.dart';
import '../repositories/settings_repository.dart';

class AudioService {
  final SettingsRepository _settingsRepo;
  final List<AudioPlayer> _activePlayers = [];

  AudioService(this._settingsRepo);

  /// Joue un effet sonore depuis les assets (ex: 'assets/files/sounds/correct.mp3')
  Future<void> playSound(String assetPath) async {
    final volume = _settingsRepo.getAudioVolume();
    if (volume <= 0) return;

    try {
      final player = AudioPlayer();
      _activePlayers.add(player);

      await player.setAsset(assetPath);
      await player.setVolume(volume);
      player.play();

      player.processingStateStream.listen((state) {
        if (state == ProcessingState.completed) {
          player.dispose();
          _activePlayers.remove(player);
        }
      });
    } catch (e) {
      print("Erreur AudioService (playSound): $e");
    }
  }

  void stopAll() {
    for (var player in _activePlayers) {
      player.stop();
    }
  }

  void dispose() {
    for (var player in _activePlayers) {
      player.dispose();
    }
    _activePlayers.clear();
  }
}
