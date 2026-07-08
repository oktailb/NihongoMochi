import '../models/handwriting.dart';
import 'handwriting_service_stub.dart'
    if (dart.library.js_interop) 'handwriting_service_web.dart'
    if (dart.library.io) 'handwriting_service_native.dart';

abstract class HandwritingService {
  factory HandwritingService() => getHandwritingService();

  ModelStatus get status;
  bool get isInitialized;

  Future<void> checkModel();
  Future<void> downloadModel();
  Future<List<String>> recognize(List<HandwritingStroke> strokes);
  void dispose();
}
