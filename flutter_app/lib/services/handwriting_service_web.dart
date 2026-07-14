import 'dart:convert';
import 'package:http/http.dart' as http;
import 'package:flutter/foundation.dart';
import '../models/handwriting.dart';
import 'handwriting_service.dart';

class WebHandwritingService implements HandwritingService {
  @override
  ModelStatus get status => ModelStatus.downloaded; // Directement utilisable en ligne

  @override
  bool get isInitialized => true;

  @override
  Future<void> checkModel() async {
    // Toujours prêt en ligne sur le web
  }

  @override
  Future<void> downloadModel() async {
    // Pas de modèle local à télécharger
  }

  @override
  Future<List<String>> recognize(List<HandwritingStroke> strokes) async {
    if (strokes.isEmpty) return [];

    try {
      // 1. Sous-échantillonnage des coordonnées & simplification des timestamps.
      // Cela évite de dépasser la taille maximale autorisée pour une URL par les proxies CORS (souvent 1024/2048 caractères).
      final inkStrokes = strokes.map((stroke) {
        final List<int> xs = [];
        final List<int> ys = [];
        final List<int> ts = [];
        
        int? lastX;
        int? lastY;
        int t = 0;
        
        for (final p in stroke.points) {
          final x = p.x.toInt();
          final y = p.y.toInt();
          // Filtre de distance de Manhattan > 4 pixels
          if (lastX == null || lastY == null || (x - lastX).abs() > 4 || (y - lastY).abs() > 4) {
            xs.add(x);
            ys.add(y);
            ts.add(t++);
            lastX = x;
            lastY = y;
          }
        }
        
        // Sécurité : s'assurer d'avoir au moins le premier et le dernier point du tracé
        if (stroke.points.isNotEmpty && xs.length < 2) {
          xs.clear();
          ys.clear();
          ts.clear();
          final first = stroke.points.first;
          xs.add(first.x.toInt());
          ys.add(first.y.toInt());
          ts.add(0);
          
          if (stroke.points.length > 1) {
            final last = stroke.points.last;
            xs.add(last.x.toInt());
            ys.add(last.y.toInt());
            ts.add(1);
          }
        }
        
        return [xs, ys, ts];
      }).toList();

      final bodyPayload = {
        'app_version': 0.4,
        'requests': [
          {
            'writing_area_width': 300,
            'writing_area_height': 300,
            'ink': inkStrokes,
            'language': 'ja',
          }
        ]
      };
      
      final String bodyString = json.encode(bodyPayload);
      debugPrint("[WebHandwritingService] POST payload : $bodyString");

      // Utilisation de corsproxy.io en POST
      const targetUrl = 'https://inputtools.google.com/request?itc=ja-t-i0-handwrit&app=translate';
      const proxyUrl = 'https://corsproxy.io/?$targetUrl';

      debugPrint("[WebHandwritingService] Envoi POST via proxy : $proxyUrl");
      String? responseBody;
      
      try {
        final response = await http.post(
          Uri.parse(proxyUrl),
          headers: {'Content-Type': 'application/json'},
          body: bodyString,
        ).timeout(const Duration(seconds: 6));
        
        debugPrint("[WebHandwritingService] Réponse reçue du proxy. Status Code = ${response.statusCode}");
        if (response.statusCode == 200) {
          responseBody = response.body;
        }
      } catch (e) {
        debugPrint('[WebHandwritingService] Proxy POST failed ($proxyUrl) : $e. Tentative de repli en direct...');
      }

      if (responseBody == null) {
        try {
          final response = await http.post(
            Uri.parse(targetUrl),
            headers: {'Content-Type': 'application/json'},
            body: bodyString,
          ).timeout(const Duration(seconds: 4));
          debugPrint("[WebHandwritingService] Réponse reçue en direct. Status Code = ${response.statusCode}");
          if (response.statusCode == 200) {
            responseBody = response.body;
          }
        } catch (e) {
          debugPrint('[WebHandwritingService] Direct POST failed : $e');
        }
      }

      if (responseBody != null) {
        debugPrint("[WebHandwritingService] Réponse brute = $responseBody");
        final decoded = json.decode(responseBody);
        if (decoded is List && decoded.length > 1) {
          final results = decoded[1];
          if (results is List && results.isNotEmpty) {
            final firstResult = results[0];
            if (firstResult is List && firstResult.length > 1) {
              final candidates = firstResult[1];
              if (candidates is List) {
                final list = List<String>.from(candidates);
                debugPrint("[WebHandwritingService] Candidats reconnus décodés = $list");
                return list;
              }
            } else if (firstResult is Map && firstResult.containsKey('candidate')) {
              final candidates = firstResult['candidate'];
              if (candidates is List) {
                final list = List<String>.from(candidates);
                debugPrint("[WebHandwritingService] Candidats reconnus décodés = $list");
                return list;
              }
            }
          }
        }
      }
      
      debugPrint("[WebHandwritingService] Aucun résultat décodé");
      return [];
    } catch (e) {
      debugPrint('[WebHandwritingService] Web recognition error: $e');
      return [];
    }
  }

  @override
  void dispose() {
    // Rien à libérer
  }
}

HandwritingService getHandwritingService() => WebHandwritingService();
