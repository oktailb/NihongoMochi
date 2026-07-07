import 'package:google_mlkit_digital_ink_recognition/google_mlkit_digital_ink_recognition.dart';

enum ModelStatus { notDownloaded, downloading, downloaded, failed }

class HandwritingService {
  final DigitalInkRecognizerModelManager _modelManager = DigitalInkRecognizerModelManager();
  late DigitalInkRecognizer _recognizer;
  final String _modelTag = 'ja';

  ModelStatus _status = ModelStatus.notDownloaded;
  ModelStatus get status => _status;

  bool _isInitialized = false;
  bool get isInitialized => _isInitialized;

  Future<void> checkModel() async {
    final bool isDownloaded = await _modelManager.isModelDownloaded(_modelTag);
    if (isDownloaded) {
      _status = ModelStatus.downloaded;
      _initRecognizer();
    } else {
      _status = ModelStatus.notDownloaded;
    }
  }

  Future<void> downloadModel() async {
    _status = ModelStatus.downloading;
    try {
      final bool success = await _modelManager.downloadModel(_modelTag);
      if (success) {
        _status = ModelStatus.downloaded;
        _initRecognizer();
      } else {
        _status = ModelStatus.failed;
      }
    } catch (e) {
      _status = ModelStatus.failed;
    }
  }

  void _initRecognizer() {
    _recognizer = DigitalInkRecognizer(languageCode: _modelTag);
    _isInitialized = true;
  }

  Future<List<String>> recognize(List<Stroke> strokes) async {
    if (!_isInitialized) return [];
    try {
      final ink = Ink();
      for (final stroke in strokes) {
        ink.strokes.add(stroke);
      }
      final candidates = await _recognizer.recognize(ink);
      return candidates.map((c) => c.text).toList();
    } catch (e) {
      print('Recognition error: $e');
      return [];
    }
  }

  void dispose() {
    if (_isInitialized) {
      _recognizer.close();
    }
  }
}
