import 'dart:math' as math;
import 'package:flutter/material.dart';
import '../providers/grammar_provider.dart';

class GrammarGraphPainter extends CustomPainter {
  final List<GrammarNode> nodes;
  final List<GrammarLevelSeparator> separators;
  final Color lineColor;
  final double scale;

  GrammarGraphPainter({
    required this.nodes,
    required this.separators,
    required this.lineColor,
    required this.scale,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = lineColor.withOpacity(0.4)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.0 * scale
      ..strokeCap = StrokeCap.round;

    final double centerX = size.width / 2;
    final double canvasW = size.width;
    final double canvasH = size.height;

    final nodesMap = {for (var n in nodes) n.rule.id: n};
    final nodesByLevel = <String, List<GrammarNode>>{};
    for (var n in nodes) {
      nodesByLevel.putIfAbsent(n.rule.level, () => []).add(n);
    }

    // --- ALGORITHME D'ALLOCATION DE CANAUX (TRACKS) DE KOTLIN ---
    final List<double> tracksLeft = [];
    final List<double> tracksRight = [];
    final Map<String, int> mapping = {};
    const double bufferY = 0.02;

    final List<Map<String, dynamic>> connections = [];
    for (var child in nodes) {
      for (var parentId in child.rule.dependencies) {
        final parent = nodesMap[parentId];
        if (parent != null && parent.rule.level == child.rule.level) {
          final isLeft = child.x < 0.5;
          final isParentLeft = parent.x < 0.5;
          if (isLeft == isParentLeft) {
            final minY = math.min(parent.y, child.y);
            final maxY = math.max(parent.y, child.y);
            connections.add({
              'key': '$parentId-${child.rule.id}',
              'minY': minY,
              'maxY': maxY,
              'length': maxY - minY,
              'isLeft': isLeft,
            });
          }
        }
      }
    }

    connections.sort((a, b) => (a['length'] as double).compareTo(b['length'] as double));

    for (var info in connections) {
      final isLeft = info['isLeft'] as bool;
      final minY = info['minY'] as double;
      final maxY = info['maxY'] as double;
      final key = info['key'] as String;

      final tracks = isLeft ? tracksLeft : tracksRight;
      int allocatedTrack = -1;
      for (int i = 0; i < tracks.length; i++) {
        if (tracks[i] + bufferY < minY) {
          tracks[i] = maxY;
          allocatedTrack = i;
          break;
        }
      }
      if (allocatedTrack == -1) {
        tracks.add(maxY);
        allocatedTrack = tracks.length - 1;
      }
      mapping[key] = allocatedTrack;
    }

    final double nodeHalfWidthPx = 55.0 * scale;
    final double cornerRadius = 16.0 * scale;
    final double trackSpacingPx = 4.0 * scale;

    // --- 1. DESSINER LES LIENS VERS LES TORII (SÉPARATEURS DE NIVEAUX) ---
    for (var separator in separators) {
      final double gateY = separator.y * canvasH;
      final levelNodes = nodesByLevel[separator.levelId] ?? [];
      for (var node in levelNodes) {
        final double childAnchorX = node.x < 0.5 
            ? (node.x * canvasW) + nodeHalfWidthPx 
            : (node.x * canvasW) - nodeHalfWidthPx;
        final double childCenterY = node.y * canvasH;
        final double childChannelX = centerX;

        final path = Path();
        path.moveTo(childAnchorX, childCenterY);
        final double distY = gateY - childCenterY;
        final double dir = distY.sign;

        if (distY.abs() > cornerRadius * 1.5) {
          final double turnY = childCenterY + (dir * cornerRadius);
          path.quadraticBezierTo(childChannelX, childCenterY, childChannelX, turnY);
          path.lineTo(childChannelX, gateY);
        } else {
          path.cubicTo(childChannelX, childCenterY, childChannelX, gateY, childChannelX, gateY);
        }
        canvas.drawPath(path, paint);
      }
    }

    // --- 2. DESSINER LES DÉPENDANCES ENTRE LEÇONS ---
    for (var node in nodes) {
      for (var dependencyId in node.rule.dependencies) {
        final parentNode = nodesMap[dependencyId];
        if (parentNode != null) {
          final double parentCenterY = parentNode.y * canvasH;
          final double childCenterY = node.y * canvasH;
          final bool isInterLevel = parentNode.rule.level != node.rule.level;
          final path = Path();

          if (isInterLevel) {
            final possibleGates = separators.where((s) => s.y < node.y).toList();
            if (possibleGates.isNotEmpty) {
              possibleGates.sort((a, b) => b.y.compareTo(a.y));
              final gate = possibleGates.first;
              final double gateY = gate.y * canvasH;
              final double parentAnchorX = parentNode.x < 0.5 
                  ? (parentNode.x * canvasW) + nodeHalfWidthPx 
                  : (parentNode.x * canvasW) - nodeHalfWidthPx;
              final double parentChannelX = centerX;

              final pathParentToGate = Path();
              pathParentToGate.moveTo(parentAnchorX, parentCenterY);
              final double distToGate = gateY - parentCenterY;

              if (distToGate.abs() > cornerRadius * 1.5) {
                final double turnY = distToGate > 0 
                    ? parentCenterY + cornerRadius 
                    : parentCenterY - cornerRadius;
                pathParentToGate.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turnY);
                pathParentToGate.lineTo(parentChannelX, gateY);
              } else {
                pathParentToGate.cubicTo(parentChannelX, parentCenterY, centerX, parentCenterY + distToGate * 0.5, centerX, gateY);
              }
              canvas.drawPath(pathParentToGate, paint);

              final double childAnchorX = node.x < 0.5 
                  ? (node.x * canvasW) + nodeHalfWidthPx 
                  : (node.x * canvasW) - nodeHalfWidthPx;
              final double childChannelX = centerX;
              path.moveTo(childChannelX, gateY);
              final double distFromGate = childCenterY - gateY;

              if (distFromGate.abs() > cornerRadius * 1.5) {
                final double turnY = childCenterY - cornerRadius;
                path.lineTo(childChannelX, turnY);
                path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY);
              } else {
                path.cubicTo(childChannelX, gateY + distFromGate * 0.5, childChannelX, childCenterY, childAnchorX, childCenterY);
              }
            }
          } else {
            final bool isCrossing = (parentNode.x < 0.5) != (node.x < 0.5);
            final double parentAnchorX = parentNode.x < 0.5 
                ? (parentNode.x * canvasW) + nodeHalfWidthPx 
                : (parentNode.x * canvasW) - nodeHalfWidthPx;

            if (isCrossing) {
              final double childAnchorX = node.x < 0.5 
                  ? (node.x * canvasW) + nodeHalfWidthPx 
                  : (node.x * canvasW) - nodeHalfWidthPx;
              final double parentChannelX = centerX;
              final double childChannelX = centerX;
              path.moveTo(parentAnchorX, parentCenterY);
              final double distY = childCenterY - parentCenterY;
              final double dir = distY.sign;

              if (distY.abs() > cornerRadius * 2.5) {
                final double turn1Y = parentCenterY + (dir * cornerRadius);
                final double midY = parentCenterY + (distY / 2.0);
                final double turn2Y = childCenterY - (dir * cornerRadius);
                path.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turn1Y);
                path.lineTo(parentChannelX, midY - (dir * cornerRadius));
                path.cubicTo(parentChannelX, midY, childChannelX, midY, childChannelX, midY + (dir * cornerRadius));
                path.lineTo(childChannelX, turn2Y);
                path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY);
              } else {
                path.cubicTo(parentChannelX, parentCenterY, childChannelX, childCenterY, childAnchorX, childCenterY);
              }
            } else {
              final double parentAnchorXOuter = parentNode.x < 0.5 
                  ? (parentNode.x * canvasW) - nodeHalfWidthPx 
                  : (parentNode.x * canvasW) + nodeHalfWidthPx;
              final double childAnchorXOuter = node.x < 0.5 
                  ? (node.x * canvasW) - nodeHalfWidthPx 
                  : (node.x * canvasW) + nodeHalfWidthPx;
              final int trackIndex = mapping['$dependencyId-${node.rule.id}'] ?? 0;
              final double totalOffset = 40.0 * scale + (trackIndex.toDouble() * trackSpacingPx);
              final double controlOffset = parentNode.x < 0.5 ? -totalOffset : totalOffset;
              final double outerChannelX = parentAnchorXOuter + controlOffset;

              path.moveTo(parentAnchorXOuter, parentCenterY);
              final double distY = childCenterY - parentCenterY;
              final double dir = distY.sign;

              if (distY.abs() > cornerRadius * 2.0) {
                final double turn1Y = parentCenterY + (dir * cornerRadius);
                final double turn2Y = childCenterY - (dir * cornerRadius);
                path.quadraticBezierTo(outerChannelX, parentCenterY, outerChannelX, turn1Y);
                path.lineTo(outerChannelX, turn2Y);
                path.quadraticBezierTo(outerChannelX, childCenterY, childAnchorXOuter, childCenterY);
              } else {
                path.cubicTo(outerChannelX, parentCenterY, outerChannelX, childCenterY, childAnchorXOuter, childCenterY);
              }
            }
          }
          canvas.drawPath(path, paint);
        }
      }
    }
  }

  @override
  bool shouldRepaint(covariant GrammarGraphPainter oldDelegate) =>
      oldDelegate.nodes != nodes || 
      oldDelegate.separators != separators || 
      oldDelegate.lineColor != lineColor ||
      oldDelegate.scale != scale;
}
