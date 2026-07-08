import 'dart:async';
import 'language_pack_manager.dart';

class WebLanguagePackManager implements LanguagePackManager {
  final _statusController = StreamController<Map<String, DownloadStatus>>.broadcast();

  @override
  Stream<Map<String, DownloadStatus>> get statusStream => _statusController.stream;

  @override
  DownloadStatus getPackStatus(String locale) => DownloadStatus.idle;

  @override
  Future<bool> isPackDownloaded(String locale) async {
    // Sur Web, on considère que rien n'est téléchargé localement
    // On pourrait utiliser IndexedDB mais pour un portage initial on simplifie
    return false;
  }

  @override
  Future<bool> downloadPack(String locale) async {
    // Non implémenté sur le Web pour le moment
    return false;
  }

  @override
  Future<String?> loadLocalResource(String fileName, {String? locale}) async {
    // Sur Web, on ne peut pas lire le système de fichier
    return null;
  }

  @override
  Future<void> deletePack(String locale) async {
    // Rien à faire
  }
}

LanguagePackManager getLanguagePackManager() => WebLanguagePackManager();
