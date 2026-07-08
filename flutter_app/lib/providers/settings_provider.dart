import 'package:flutter/material.dart';
import '../repositories/settings_repository.dart';

class SettingsProvider extends ChangeNotifier {
  final SettingsRepository _repository;

  SettingsProvider(this._repository) {
    _loadSettings();
  }

  bool _isDarkMode = false;
  String _currentLocaleCode = "en_GB";
  double _animationSpeed = 1.0;
  double _audioVolume = 1.0;
  double _ttsRate = 1.0;
  String _pronunciation = "Hiragana";
  String _currentMode = "JLPT";
  bool _addWrongAnswers = true;
  bool _removeGoodAnswers = true;

  bool get isDarkMode => _isDarkMode;
  String get currentLocaleCode => _currentLocaleCode;
  double get animationSpeed => _animationSpeed;
  double get audioVolume => _audioVolume;
  double get ttsRate => _ttsRate;
  String get pronunciation => _pronunciation;
  String get currentMode => _currentMode;
  bool get addWrongAnswers => _addWrongAnswers;
  bool get removeGoodAnswers => _removeGoodAnswers;

  void _loadSettings() {
    _isDarkMode = _repository.getTheme() == "dark";
    _currentLocaleCode = _repository.getAppLocale();
    _animationSpeed = _repository.getAnimationSpeed();
    _audioVolume = _repository.getAudioVolume();
    _ttsRate = _repository.getTtsRate();
    _pronunciation = _repository.getPronunciation();
    _currentMode = _repository.getMode();
    _addWrongAnswers = _repository.shouldAddWrongAnswers();
    _removeGoodAnswers = _repository.shouldRemoveGoodAnswers();
    notifyListeners();
  }

  Future<void> toggleTheme(bool isDark) async {
    _isDarkMode = isDark;
    await _repository.setTheme(isDark ? "dark" : "light");
    notifyListeners();
  }

  Future<void> updateLocale(String code) async {
    _currentLocaleCode = code;
    await _repository.setAppLocale(code);
    notifyListeners();
  }

  Future<void> updateAnimationSpeed(double value) async {
    _animationSpeed = value;
    await _repository.setAnimationSpeed(value);
    notifyListeners();
  }

  Future<void> updateAudioVolume(double value) async {
    _audioVolume = value;
    await _repository.setAudioVolume(value);
    notifyListeners();
  }

  Future<void> updateTtsRate(double value) async {
    _ttsRate = value;
    await _repository.setTtsRate(value);
    notifyListeners();
  }

  Future<void> updatePronunciation(String value) async {
    _pronunciation = value;
    await _repository.setPronunciation(value);
    notifyListeners();
  }

  Future<void> updateMode(String value) async {
    _currentMode = value;
    await _repository.setMode(value);
    notifyListeners();
  }

  Future<void> toggleAddWrongAnswers(bool value) async {
    _addWrongAnswers = value;
    await _repository.setAddWrongAnswers(value);
    notifyListeners();
  }

  Future<void> toggleRemoveGoodAnswers(bool value) async {
    _removeGoodAnswers = value;
    await _repository.setRemoveGoodAnswers(value);
    notifyListeners();
  }
}
