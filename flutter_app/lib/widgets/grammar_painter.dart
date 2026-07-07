import 'package:flutter/material.dart';
import '../providers/grammar_provider.dart';

class GrammarGraphPainter extends CustomPainter {
  final List<GrammarNode> nodes;
  final List<GrammarLevelSeparator> separators;
  final Color lineColor;

  GrammarGraphPainter({
    required this.nodes,
    required this.separators,
    required this.lineColor,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = lineColor.withOpacity(0.4)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0
      ..strokeCap = StrokeCap.round;

    final double centerX = size.width / 2;
    final nodesMap = {for (var n in nodes) n.rule.id: n};

    // 1. Dessiner les liens vers les Torii (séparateurs de niveaux)
    for (var separator in separators) {
      final double gateY = separator.y * size.height;
      for (var ruleId in separator.ruleIds) {
        final node = nodesMap[ruleId];
        if (node == null) continue;

        final double nodeX = node.x * size.width;
        final double nodeY = node.y * size.height;

        final path = Path();
        path.moveTo(nodeX, nodeY);

        // Courbe vers le centre (le Torii)
        path.quadraticBezierTo(centerX, nodeY, centerX, gateY);
        canvas.drawPath(path, paint);
      }
    }

    // 2. Dessiner les dépendances entre leçons
    for (var node in nodes) {
      for (var depId in node.rule.dependencies) {
        final parent = nodesMap[depId];
        if (parent == null) continue;

        final double startX = parent.x * size.width;
        final double startY = parent.y * size.height;
        final double endX = node.x * size.width;
        final double endY = node.y * size.height;

        final path = Path();
        path.moveTo(startX, startY);

        if (parent.rule.level == node.rule.level) {
          // Même niveau : Courbe latérale ou directe
          if ((parent.x - 0.5).abs() < 0.1 && (node.x - 0.5).abs() < 0.1) {
             path.lineTo(endX, endY);
          } else {
             final double controlX = (startX + endX) / 2 + (startX < centerX ? -40 : 40);
             path.quadraticBezierTo(controlX, (startY + endY) / 2, endX, endY);
          }
        } else {
          // Niveaux différents : Passe par le centre
          path.cubicTo(centerX, startY, centerX, endY, endX, endY);
        }
        canvas.drawPath(path, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant GrammarGraphPainter oldDelegate) =>
      oldDelegate.nodes != nodes || oldDelegate.separators != separators;
}
