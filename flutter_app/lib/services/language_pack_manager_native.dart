import 'dart:io';
import 'package:http/http.dart' as http;
import 'package:archive/archive.dart';
import 'package:crypto/crypto.dart';
import 'package:path_provider/path_provider.dart';
import 'package:path/path.dart' as p;
import 'dart:async';
import 'language_pack_manager.dart';

class NativeLanguagePackManager implements LanguagePackManager {
  final String _baseUrl = "https://raw.githubusercontent.com/oktailb/NihongoMochi-Data/main";
  final _statusController = StreamController<Map<String, DownloadStatus>>.broadcast();
  final Map<String, DownloadStatus> _statusMap = {};

  @override
  Stream<Map<String, DownloadStatus>> get statusStream => _statusController.stream;

  @override
  DownloadStatus getPackStatus(String locale) => _statusMap[locale] ?? DownloadStatus.idle;

  @override
  Future<bool> isPackDownloaded(String locale) async {
    final dir = await _getLocaleDir(locale);
    return File(p.join(dir.path, 'meanings.json')).exists();
  }

  Future<Directory> _getLocaleDir(String locale) async {
    final docDir = await getApplicationDocumentsDirectory();
    final dir = Directory(p.join(docDir.path, 'langs', locale));
    if (!await dir.exists()) {
      await dir.create(recursive: true);
    }
    return dir;
  }

  @override
  Future<bool> downloadPack(String locale) async {
    if (_statusMap[locale] == DownloadStatus.downloading) return false;

    _statusMap[locale] = DownloadStatus.downloading;
    _statusController.add(_statusMap);

    try {
      final files = ['data', 'lessons', 'grammar'];
      final dir = await _getLocaleDir(locale);

      for (var name in files) {
        final zipUrl = "$_baseUrl/langs/$locale/$name.zip";
        final md5Url = "$_baseUrl/langs/$locale/$name.md5";

        // 1. Check MD5
        final responseMd5 = await http.get(Uri.parse(md5Url));
        if (responseMd5.statusCode != 200) throw Exception("Failed to load MD5");
        final remoteMd5 = responseMd5.body.trim();

        final localMd5File = File(p.join(dir.path, "$name.md5"));
        if (await localMd5File.exists()) {
          final localMd5 = await localMd5File.readAsString();
          if (localMd5.trim() == remoteMd5) continue;
        }

        // 2. Download ZIP
        final responseZip = await http.get(Uri.parse(zipUrl));
        if (responseZip.statusCode != 200) throw Exception("Failed to load ZIP");
        final bytes = responseZip.bodyBytes;

        // 3. Verify MD5
        if (md5.convert(bytes).toString() != remoteMd5) {
          throw Exception("MD5 mismatch");
        }

        // 4. Extract
        final archive = ZipDecoder().decodeBytes(bytes);
        for (final file in archive) {
          final filename = file.name;
          if (file.isFile) {
            final data = file.content as List<int>;
            File(p.join(dir.path, filename))
              ..createSync(recursive: true)
              ..writeAsBytesSync(data);
          } else {
            Directory(p.join(dir.path, filename)).createSync(recursive: true);
          }
        }

        // 5. Save local MD5
        await localMd5File.writeAsString(remoteMd5);
      }

      _statusMap[locale] = DownloadStatus.success;
      _statusController.add(_statusMap);
      return true;
    } catch (e) {
      print("Download error: $e");
      _statusMap[locale] = DownloadStatus.error;
      _statusController.add(_statusMap);
      return false;
    }
  }

  @override
  Future<String?> loadLocalResource(String fileName, {String? locale}) async {
    final docDir = await getApplicationDocumentsDirectory();
    File? file;
    if (locale != null) {
      file = File(p.join(docDir.path, 'langs', locale, fileName));
      if (await file.exists()) return file.readAsString();
    }

    file = File(p.join(docDir.path, 'common', fileName));
    if (await file.exists()) return file.readAsString();

    return null;
  }

  @override
  Future<void> deletePack(String locale) async {
    final dir = await _getLocaleDir(locale);
    if (await dir.exists()) {
      await dir.delete(recursive: true);
    }
    _statusMap[locale] = DownloadStatus.idle;
    _statusController.add(_statusMap);
  }
}

LanguagePackManager getLanguagePackManager() => NativeLanguagePackManager();
