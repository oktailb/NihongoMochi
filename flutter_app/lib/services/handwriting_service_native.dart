import 'package:google_mlkit_digital_ink_recognition/google_mlkit_digital_ink_recognition.dart';
import '../models/handwriting.dart';
import 'handwriting_service.dart';

class NativeHandwritingService implements HandwritingService {
  final DigitalInkRecognizerModelManager _modelManager = DigitalInkRecognizerModelManager();
  DigitalInkRecognizer? _recognizer;
  final String _modelTag = 'ja';

  ModelStatus _status = ModelStatus.notDownloaded;
  @override
  ModelStatus get status => _status;

  bool _isInitialized = false;
  @override
  bool get isInitialized => _isInitialized;

  @override
  Future<void> checkModel() async {
    final bool isDownloaded = await _modelManager.isModelDownloaded(_modelTag);
    if (isDownloaded) {
      _status = ModelStatus.downloaded;
      _initRecognizer();
    } else {
      _status = ModelStatus.notDownloaded;
    }
  }

  @override
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

  @override
  Future<List<String>> recognize(List<HandwritingStroke> handwritingStrokes) async {
    if (!_isInitialized || _recognizer == null) return [];
    try {
      final ink = Ink();
      for (final hStroke in handwritingStrokes) {
        final stroke = Stroke();
        for (final p in hStroke.points) {
          stroke.points.add(Point(x: p.x, y: p.y, t: p.t));
        }
        ink.strokes.add(stroke);
      }
      final candidates = await _recognizer!.recognize(ink);
      return candidates.map((c) => c.text).toList();
    } catch (e) {
      print('Recognition error: $e');
      return [];
    }
  }

  @override
  void dispose() {
    _recognizer?.close();
  }
}

HandwritingService getHandwritingService() => NativeHandwritingService();
