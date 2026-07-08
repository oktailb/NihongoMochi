import 'dart:math';
import 'package:flutter/material.dart';

class SagaPathPainter extends CustomPainter {
  final List<Offset> points;
  final Color color;

  SagaPathPainter({required this.points, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    if (points.length < 2) return;

    final paint = Paint()
      ..color = color.withOpacity(0.5)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 6.0
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round;

    final path = Path();
    path.moveTo(points[0].dx, points[0].dy);

    for (int i = 0; i < points.length - 1; i++) {
      final p0 = points[i];
      final p1 = points[i + 1];

      // Calcul du point de contrôle pour une courbe fluide (Bézier cubique)
      final midY = (p0.dy + p1.dy) / 2;
      path.cubicTo(
        p0.dx, midY,
        p1.dx, midY,
        p1.dx, p1.dy,
      );
    }

    canvas.drawPath(path, paint);
  }

  @override
  bool shouldRepaint(covariant SagaPathPainter oldDelegate) => points != oldDelegate.points;
}
