import 'package:flutter/material.dart';
import 'package:flutter/foundation.dart';
import 'dart:math';
import '../models/handwriting.dart';
import '../providers/dictionary_provider.dart';
import '../providers/settings_provider.dart';
import 'package:provider/provider.dart';

class DrawingBoard extends StatefulWidget {
  final Function(HandwritingStroke) onStrokeDone;
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
                  final stroke = HandwritingStroke();
                  for (final point in _points) {
                    stroke.points.add(HandwritingPoint(
                      x: point.dx,
                      y: point.dy,
                      t: DateTime.now().millisecondsSinceEpoch
                    ));
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

class DrawingThumbnail extends StatelessWidget {
  final List<HandwritingStroke> strokes;
  final double size;

  const DrawingThumbnail({
    super.key,
    required this.strokes,
    this.size = 56,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      width: size,
      height: size,
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceVariant,
        borderRadius: BorderRadius.circular(4),
      ),
      child: CustomPaint(
        painter: ThumbnailPainter(
          strokes: strokes,
          strokeColor: theme.colorScheme.onSurface,
        ),
        size: Size(size, size),
      ),
    );
  }
}

class ThumbnailPainter extends CustomPainter {
  final List<HandwritingStroke> strokes;
  final Color strokeColor;

  ThumbnailPainter({required this.strokes, required this.strokeColor});

  @override
  void paint(Canvas canvas, Size size) {
    if (strokes.isEmpty) return;

    double minX = double.maxFinite;
    double minY = double.maxFinite;
    double maxX = -double.maxFinite;
    double maxY = -double.maxFinite;

    for (final stroke in strokes) {
      for (final point in stroke.points) {
        minX = min(minX, point.x);
        minY = min(minY, point.y);
        maxX = max(maxX, point.x);
        maxY = max(maxY, point.y);
      }
    }

    final drawingWidth = maxX - minX;
    final drawingHeight = maxY - minY;
    if (drawingWidth <= 0 || drawingHeight <= 0) return;

    // Center and scale to fit inside size.width and size.height
    final scale = min(size.width / drawingWidth, size.height / drawingHeight) * 0.8;
    final offsetX = (size.width - (drawingWidth * scale)) / 2 - (minX * scale);
    final offsetY = (size.height - (drawingHeight * scale)) / 2 - (minY * scale);

    final paint = Paint()
      ..color = strokeColor
      ..strokeCap = StrokeCap.round
      ..strokeJoin = StrokeJoin.round
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    for (final stroke in strokes) {
      final path = Path();
      for (int i = 0; i < stroke.points.length; i++) {
        final point = stroke.points[i];
        final px = point.x * scale + offsetX;
        final py = point.y * scale + offsetY;
        if (i == 0) {
          path.moveTo(px, py);
        } else {
          path.lineTo(px, py);
        }
      }
      canvas.drawPath(path, paint);
    }
  }

  @override
  bool shouldRepaint(covariant ThumbnailPainter oldDelegate) => true;
}

class ComposeDrawingDialog extends StatefulWidget {
  final DictionaryProvider provider;
  final VoidCallback onDismiss;
  final VoidCallback onConfirm;

  const ComposeDrawingDialog({
    super.key,
    required this.provider,
    required this.onDismiss,
    required this.onConfirm,
  });

  @override
  State<ComposeDrawingDialog> createState() => _ComposeDrawingDialogState();
}

class _ComposeDrawingDialogState extends State<ComposeDrawingDialog> {
  final List<Offset> _points = [];
  final List<List<Offset>> _allStrokes = [];

  @override
  void initState() {
    super.initState();
    final status = widget.provider.modelStatus;
    if (status == ModelStatus.notDownloaded || status == ModelStatus.failed) {
      widget.provider.downloadModel();
    }
    // Pre-fill existing strokes from provider if any
    for (var stroke in widget.provider.currentStrokes) {
      _allStrokes.add(stroke.points.map((p) => Offset(p.x, p.y)).toList());
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = widget.provider;
    final theme = Theme.of(context);
    final settings = context.read<SettingsProvider>();

    return Dialog(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: MediaQuery.of(context).size.width * 0.9,
        constraints: const BoxConstraints(maxWidth: 400),
        padding: const EdgeInsets.all(16),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              settings.getString("dictionary_draw_kanji"),
              style: theme.textTheme.titleLarge,
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 16),
            if (kIsWeb) ...[
              const Icon(Icons.warning_amber_rounded, color: Colors.orange, size: 48),
              const SizedBox(height: 16),
              const Text(
                "La reconnaissance d'écriture n'est pas supportée sur le Web.\n\nVeuillez utiliser l'application mobile (Android/iOS) pour tester cette fonctionnalité.",
                textAlign: TextAlign.center,
                style: TextStyle(fontWeight: FontWeight.bold),
              ),
            ] else if (provider.modelStatus == ModelStatus.notDownloaded ||
                provider.modelStatus == ModelStatus.downloading ||
                provider.modelStatus == ModelStatus.failed) ...[
              const CircularProgressIndicator(),
              const SizedBox(height: 16),
              Text(
                provider.modelStatus == ModelStatus.downloading
                    ? "Téléchargement du modèle..."
                    : provider.modelStatus == ModelStatus.failed
                        ? "Échec du téléchargement. Nouvel essai..."
                        : "Initialisation du modèle...",
                style: const TextStyle(fontWeight: FontWeight.bold),
              ),
            ] else ...[
              BoxWithDrawingCanvas(
                provider: provider,
                allStrokes: _allStrokes,
                points: _points,
                onStrokesChanged: () {
                  setState(() {});
                },
              ),
              const SizedBox(height: 16),
              if (provider.recognitionResults != null && provider.recognitionResults!.isNotEmpty)
                Text(
                  "Candidats : ${provider.recognitionResults!.join(', ')}",
                  style: const TextStyle(fontWeight: FontWeight.bold),
                )
              else
                const Text("Dessinez quelque chose..."),
            ],
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: ElevatedButton(
                    onPressed: widget.onDismiss,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: theme.colorScheme.surfaceVariant,
                      foregroundColor: theme.colorScheme.onSurfaceVariant,
                    ),
                    child: const Text("Annuler"),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: ElevatedButton(
                    onPressed: widget.onConfirm,
                    child: const Text("Confirmer"),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class BoxWithDrawingCanvas extends StatelessWidget {
  final DictionaryProvider provider;
  final List<List<Offset>> allStrokes;
  final List<Offset> points;
  final VoidCallback onStrokesChanged;

  const BoxWithDrawingCanvas({
    super.key,
    required this.provider,
    required this.allStrokes,
    required this.points,
    required this.onStrokesChanged,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: 300,
      decoration: BoxDecoration(
        color: Colors.white,
        border: Border.all(color: Colors.grey),
        borderRadius: BorderRadius.circular(4),
      ),
      child: Stack(
        children: [
          GestureDetector(
            onPanStart: (details) {
              points.add(details.localPosition);
              onStrokesChanged();
            },
            onPanUpdate: (details) {
              points.add(details.localPosition);
              onStrokesChanged();
            },
            onPanEnd: (details) {
              if (points.isNotEmpty) {
                final stroke = HandwritingStroke();
                for (final p in points) {
                  stroke.points.add(HandwritingPoint(
                    x: p.dx,
                    y: p.dy,
                    t: DateTime.now().millisecondsSinceEpoch,
                  ));
                }
                provider.addStroke(stroke);
                allStrokes.add(List.from(points));
                points.clear();
                onStrokesChanged();
              }
            },
            child: CustomPaint(
              painter: DrawingPainter(strokes: allStrokes, currentPoints: points),
              size: Size.infinite,
            ),
          ),
          Positioned(
            top: 8,
            right: 8,
            child: GestureDetector(
              onTap: () {
                allStrokes.clear();
                points.clear();
                provider.clearDrawing();
                onStrokesChanged();
              },
              child: const Icon(Icons.delete, color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }
}
