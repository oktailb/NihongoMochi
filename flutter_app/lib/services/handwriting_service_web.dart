import '../models/handwriting.dart';
import 'handwriting_service.dart';

class WebHandwritingService implements HandwritingService {
  @override
  ModelStatus get status => ModelStatus.notDownloaded;

  @override
  bool get isInitialized => false;

  @override
  Future<void> checkModel() async {
    // Non supporté sur le web pour le moment
  }

  @override
  Future<void> downloadModel() async {
    // Non supporté sur le web
  }

  @override
  Future<List<String>> recognize(List<HandwritingStroke> strokes) async {
    // On retourne une liste vide sur le web
    return [];
  }

  @override
  void dispose() {
    // Rien à libérer
  }
}

HandwritingService getHandwritingService() => WebHandwritingService();
