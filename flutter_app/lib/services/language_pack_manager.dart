import 'language_pack_manager_stub.dart'
    if (dart.library.js_interop) 'language_pack_manager_web.dart'
    if (dart.library.io) 'language_pack_manager_native.dart';

enum DownloadStatus { idle, downloading, success, error }

abstract class LanguagePackManager {
  factory LanguagePackManager() => getLanguagePackManager();

  Stream<Map<String, DownloadStatus>> get statusStream;
  DownloadStatus getPackStatus(String locale);

  Future<bool> isPackDownloaded(String locale);
  Future<bool> downloadPack(String locale);
  Future<String?> loadLocalResource(String fileName, {String? locale});
  Future<void> deletePack(String locale);
}
