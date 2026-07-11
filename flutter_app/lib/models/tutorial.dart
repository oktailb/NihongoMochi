import 'package:flutter/material.dart';

enum TooltipDirection { top, bottom, left, right }

class TutorialStep {
  final String text;
  final Alignment targetAnchor;
  final TooltipDirection tooltipDirection;
  final Rect? targetRect;

  const TutorialStep({
    required this.text,
    this.targetAnchor = Alignment.center,
    this.tooltipDirection = TooltipDirection.top,
    this.targetRect,
  });
}
