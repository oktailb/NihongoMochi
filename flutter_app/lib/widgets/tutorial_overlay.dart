import 'package:flutter/material.dart';
import '../models/tutorial.dart';

class TutorialOverlay extends StatefulWidget {
  final List<TutorialStep> steps;
  final bool isVisible;
  final VoidCallback onFinished;

  const TutorialOverlay({
    super.key,
    required this.steps,
    required this.isVisible,
    required this.onFinished,
  });

  @override
  State<TutorialOverlay> createState() => _TutorialOverlayState();
}

class _TutorialOverlayState extends State<TutorialOverlay> {
  int _currentStepIndex = 0;

  @override
  Widget build(BuildContext context) {
    if (!widget.isVisible || widget.steps.isEmpty || _currentStepIndex >= widget.steps.size) {
      return const SizedBox.shrink();
    }

    final currentStep = widget.steps[_currentStepIndex];

    return Material(
      color: Colors.transparent,
      child: Stack(
        children: [
          // Background Dim
          GestureDetector(
            onTap: _nextStep,
            child: Container(
              color: Colors.black.withOpacity(0.4),
              fill: true,
            ),
          ),

          // Tooltip Bubble
          Align(
            alignment: currentStep.targetAnchor,
            child: Padding(
              padding: const EdgeInsets.all(32.0),
              child: _buildTooltipBubble(currentStep),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTooltipBubble(TutorialStep step) {
    final bool isLastStep = _currentStepIndex == widget.steps.length - 1;

    return Card(
      elevation: 8,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 280,
        padding: const EdgeInsets.all(20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              step.text,
              style: const TextStyle(fontSize: 16),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                TextButton(
                  onPressed: widget.onFinished,
                  child: const Text("Passer", style: TextStyle(color: Colors.grey)),
                ),
                ElevatedButton(
                  onPressed: _nextStep,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.pink,
                    foregroundColor: Colors.white,
                    shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                  ),
                  child: Text(isLastStep ? "Terminer" : "Suivant"),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  void _nextStep() {
    if (_currentStepIndex < widget.steps.length - 1) {
      setState(() {
        _currentStepIndex++;
      });
    } else {
      widget.onFinished();
    }
  }
}

extension on List {
  int get size => length;
}
