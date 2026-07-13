import 'package:flutter_tts/flutter_tts.dart';
import '../repositories/settings_repository.dart';

class TtsService {
  final SettingsRepository _settingsRepo;
  final FlutterTts _tts = FlutterTts();
  List<String> _availableVoices = [];

  TtsService(this._settingsRepo);

  List<String> get availableVoices => _availableVoices;

  Future<void> init() async {
    try {
      final dynamic voices = await _tts.getVoices;
      if (voices is List) {
        final List<String> jaVoices = [];
        for (var v in voices) {
          if (v is Map) {
            final name = v['name'] as String?;
            final locale = v['locale'] as String?;
            if (locale != null && (locale.toLowerCase() == 'ja-jp' || locale.toLowerCase().replaceAll('_', '-').startsWith('ja-'))) {
              if (name != null) {
                jaVoices.add(name);
              }
            }
          } else if (v is String) {
            if (v.toLowerCase().contains('ja')) {
              jaVoices.add(v);
            }
          }
        }

        // KMP-matching voice filtering logic
        final highQualityGoogle = jaVoices.where((name) => name.contains('jab-local') || name.contains('jad-local')).toList();
        _availableVoices = highQualityGoogle.isNotEmpty ? highQualityGoogle : jaVoices;
      }
    } catch (e) {
      print("Error loading TTS voices: $e");
    }
  }

  Future<void> speak(String text) async {
    final rate = _settingsRepo.getTtsRate();
    final voiceId = _settingsRepo.getTtsVoiceId();
    
    await _tts.setLanguage("ja-JP");
    await _tts.setSpeechRate(rate);

    if (voiceId != null && voiceId.isNotEmpty && _availableVoices.contains(voiceId)) {
      await _tts.setVoice({"name": voiceId, "locale": "ja-JP"});
    }

    await _tts.speak(text);
  }

  Future<void> testSpeak() async {
    await speak("日本語を勉強しています");
  }

  Future<void> stop() async {
    await _tts.stop();
  }
}
