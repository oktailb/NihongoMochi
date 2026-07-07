import 'package:flutter/material.dart';
import 'package:google_mlkit_digital_ink_recognition/google_mlkit_digital_ink_recognition.dart';

class DrawingBoard extends StatefulWidget {
  final Function(Stroke) onStrokeDone;
  final VoidCallback onClear;

  const DrawingBoard({
    super.key,
    required this.onStrokeDone,
    required this.onClear,
  });

  @override
  State<DrawingBoard> createState() => _DrawingBoardState();
}

class _DrawingBoardState extends State<DrawingBoard> {
  final List<Offset> _points = [];
  final List<List<Offset>> _allStrokes = [];

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: Container(
            margin: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              border: Border.all(color: Colors.grey),
              color: Colors.white,
            ),
            child: GestureDetector(
              onPanStart: (details) {
                setState(() {
                  _points.add(details.localPosition);
                });
              },
              onPanUpdate: (details) {
                setState(() {
                  _points.add(details.localPosition);
                });
              },
              onPanEnd: (details) {
                if (_points.isNotEmpty) {
                  final stroke = Stroke();
                  for (final point in _points) {
                    stroke.points.add(Point(x: point.dx, y: point.dy, t: DateTime.now().millisecondsSinceEpoch));
                  }
                  widget.onStrokeDone(stroke);
                  _allStrokes.add(List.from(_points));
                  _points.clear();
                }
              },
              child: CustomPaint(
                painter: DrawingPainter(strokes: _allStrokes, currentPoints: _points),
                size: Size.infinite,
              ),
            ),
          ),
        ),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: [
            TextButton(
              onPressed: () {
                setState(() {
                  _allStrokes.clear();
                  _points.clear();
                });
                widget.onClear();
              },
              child: const Text("Effacer"),
            ),
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Fermer"),
            ),
          ],
        ),
      ],
    );
  }
}

class DrawingPainter extends CustomPainter {
  final List<List<Offset>> strokes;
  final List<Offset> currentPoints;

  DrawingPainter({required this.strokes, required this.currentPoints});

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = Colors.black
      ..strokeCap = StrokeCap.round
      ..strokeWidth = 4.0;

    for (final stroke in strokes) {
      for (int i = 0; i < stroke.length - 1; i++) {
        canvas.drawLine(stroke[i], stroke[i + 1], paint);
      }
    }

    for (int i = 0; i < currentPoints.length - 1; i++) {
      canvas.drawLine(currentPoints[i], currentPoints[i + 1], paint);
    }
  }

  @override
  bool shouldRepaint(covariant DrawingPainter oldDelegate) => true;
}
