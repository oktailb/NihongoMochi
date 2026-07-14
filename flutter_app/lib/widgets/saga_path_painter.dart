import 'package:flutter/material.dart';

class SagaRoad {
  final Offset start;
  final Offset end;
  final double spacing;

  SagaRoad({required this.start, required this.end, required this.spacing});
}

class SagaPathPainter extends CustomPainter {
  final List<SagaRoad> roads;
  final Color color;

  SagaPathPainter({required this.roads, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    if (roads.isEmpty) return;

    final paint = Paint()
      ..color = color.withValues(alpha: 0.5)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 6.0
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;

    for (final road in roads) {
      final path = Path();
      path.moveTo(road.start.dx, road.start.dy);

      final p0 = road.start;
      final p3 = road.end;

      // Calcul des points de contrôle pour une courbe de Bézier cubique fluide
      final double controlY1 = p0.dy + road.spacing * 0.5;
      final double controlY2 = p3.dy - road.spacing * 0.5;

      path.cubicTo(
        p0.dx, controlY1,
        p3.dx, controlY2,
        p3.dx, p3.dy,
      );

      canvas.drawPath(path, paint);
    }
  }

  @override
  bool shouldRepaint(covariant SagaPathPainter oldDelegate) => true;
}
