import 'package:flutter/services.dart';
import 'language_pack_manager.dart';

class ResourceLoader {
  final LanguagePackManager _lpManager;

  ResourceLoader(this._lpManager);

  /// Loads a resource from local storage (if downloaded) or assets (fallback).
  /// [fileName] is the name of the file (e.g., 'meanings.json').
  /// [locale] is the locale folder (e.g., 'en_GB').
  /// [assetPath] is the full path in assets if it's not in the standard 'langs/$locale/' folder.
  Future<String> loadString(String fileName, {String? locale, String? assetPath}) async {
    // 1. Try local storage (LanguagePackManager)
    try {
      final downloaded = await _lpManager.loadLocalResource(fileName, locale: locale);
      if (downloaded != null) return downloaded;
    } catch (e) {
      // Log error but continue to fallback
      print("ResourceLoader: Error loading $fileName from local storage: $e");
    }

    // 2. Try assets
    String finalAssetPath = assetPath ?? (locale != null 
        ? 'assets/files/langs/$locale/$fileName' 
        : 'assets/files/$fileName');

    return await rootBundle.loadString(finalAssetPath);
  }
}
