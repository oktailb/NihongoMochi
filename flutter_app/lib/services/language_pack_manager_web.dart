import 'dart:async';
import 'package:http/http.dart' as http;
import 'language_pack_manager.dart';

class WebLanguagePackManager implements LanguagePackManager {
  final _statusController = StreamController<Map<String, DownloadStatus>>.broadcast();
  final String _baseUrl = "https://raw.githubusercontent.com/oktailb/NihongoMochi-Data/main";

  @override
  Stream<Map<String, DownloadStatus>> get statusStream => _statusController.stream;

  @override
  DownloadStatus getPackStatus(String locale) => DownloadStatus.idle;

  @override
  Future<bool> isPackDownloaded(String locale) async {
    return false;
  }

  @override
  Future<bool> downloadPack(String locale) async {
    // Sur Web, on ne télécharge pas de ZIP, on fetch à la demande
    return true; 
  }

  @override
  Future<String?> loadLocalResource(String fileName, {String? locale}) async {
    try {
      // Construction de l'URL GitHub
      final url = locale != null 
        ? "$_baseUrl/langs/$locale/$fileName"
        : "$_baseUrl/common/$fileName";

      final response = await http.get(Uri.parse(url));
      if (response.statusCode == 200) {
        return response.body;
      }
    } catch (e) {
      print("WebLanguagePackManager: error fetching $fileName: $e");
    }
    return null;
  }

  @override
  Future<void> deletePack(String locale) async {}
}

LanguagePackManager getLanguagePackManager() => WebLanguagePackManager();
