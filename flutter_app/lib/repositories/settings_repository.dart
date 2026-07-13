import 'package:shared_preferences/shared_preferences.dart';

class SettingsRepository {
  final SharedPreferences _prefs;

  SettingsRepository(this._prefs);

  // Clés identiques au projet Kotlin pour référence
  static const String _isFirstRunKey = "is_first_run";
  static const String _pronunciationKey = "pronunciation";
  static const String _animationSpeedKey = "animation_speed";
  static const String _themeKey = "theme_pref";
  static const String _appLocaleKey = "app_locale";
  static const String _modeKey = "mode_pref";
  static const String _audioVolumeKey = "audio_volume";
  static const String _ttsRateKey = "tts_rate";
  static const String _addWrongAnswersKey = "add_wrong_answers";
  static const String _removeGoodAnswersKey = "remove_good_answers";
  static const String _selectedLevelKey = "selected_level";
  static const String _ttsVoiceIdKey = "tts_voice_id";

  bool isFirstRun() => _prefs.getBool(_isFirstRunKey) ?? true;
  Future<void> setFirstRunCompleted() => _prefs.setBool(_isFirstRunKey, false);

  String getPronunciation() => _prefs.getString(_pronunciationKey) ?? "Hiragana";
  Future<void> setPronunciation(String value) => _prefs.setString(_pronunciationKey, value);

  double getAnimationSpeed() => _prefs.getDouble(_animationSpeedKey) ?? 1.0;
  Future<void> setAnimationSpeed(double value) => _prefs.setDouble(_animationSpeedKey, value);

  String getTheme() => _prefs.getString(_themeKey) ?? "light";
  Future<void> setTheme(String value) => _prefs.setString(_themeKey, value);

  String getAppLocale() => _prefs.getString(_appLocaleKey) ?? "en_GB";
  Future<void> setAppLocale(String value) => _prefs.setString(_appLocaleKey, value);

  String getMode() => _prefs.getString(_modeKey) ?? "JLPT";
  Future<void> setMode(String value) => _prefs.setString(_modeKey, value);

  double getAudioVolume() => _prefs.getDouble(_audioVolumeKey) ?? 1.0;
  Future<void> setAudioVolume(double value) => _prefs.setDouble(_audioVolumeKey, value);

  double getTtsRate() => _prefs.getDouble(_ttsRateKey) ?? 1.0;
  Future<void> setTtsRate(double value) => _prefs.setDouble(_ttsRateKey, value);

  bool shouldAddWrongAnswers() => _prefs.getBool(_addWrongAnswersKey) ?? true;
  Future<void> setAddWrongAnswers(bool value) => _prefs.setBool(_addWrongAnswersKey, value);

  bool shouldRemoveGoodAnswers() => _prefs.getBool(_removeGoodAnswersKey) ?? true;
  Future<void> setRemoveGoodAnswers(bool value) => _prefs.setBool(_removeGoodAnswersKey, value);

  String getSelectedLevel() => _prefs.getString(_selectedLevelKey) ?? "";
  Future<void> setSelectedLevel(String value) => _prefs.setString(_selectedLevelKey, value);

  String? getTtsVoiceId() => _prefs.getString(_ttsVoiceIdKey);
  Future<void> setTtsVoiceId(String? value) {
    if (value == null || value.isEmpty) {
      return _prefs.remove(_ttsVoiceIdKey);
    }
    return _prefs.setString(_ttsVoiceIdKey, value);
  }
}

