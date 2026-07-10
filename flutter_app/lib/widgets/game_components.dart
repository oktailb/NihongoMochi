import 'package:flutter/material.dart';
import 'dart:math' as math;
import '../models/quiz_models.dart';

class GameProgressBar extends StatelessWidget {
  final List<GameStatus> statuses;
  final int maxItems;

  const GameProgressBar({
    super.key,
    required this.statuses,
    this.maxItems = 10,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 4, vertical: 4),
      color: Colors.white.withOpacity(0.5),
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(4)),
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 8),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: List.generate(maxItems, (index) {
            final status = index < statuses.length ? statuses[index] : null;
            
            IconData icon;
            Color color;

            switch (status) {
              case GameStatus.correct:
                icon = Icons.check_circle;
                color = const Color(0xFF00E676);
                break;
              case GameStatus.incorrect:
                icon = Icons.close;
                color = Colors.red;
                break;
              case GameStatus.partial:
                icon = Icons.history;
                color = const Color(0xFF4FC3F7);
                break;
              default:
                icon = Icons.radio_button_unchecked;
                color = Colors.grey.withOpacity(0.4);
            }

            return Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 2),
                child: Icon(icon, color: color, size: 24),
              ),
            );
          }),
        ),
      ),
    );
  }
}

class FlippableQuestionCard extends StatefulWidget {
  final String frontText;
  final String backText;
  final double frontFontSize;
  final double backFontSize;
  final Color? color;

  const FlippableQuestionCard({
    super.key,
    required this.frontText,
    required this.backText,
    this.frontFontSize = 100,
    this.backFontSize = 24,
    this.color,
  });

  @override
  State<FlippableQuestionCard> createState() => _FlippableQuestionCardState();
}

class _FlippableQuestionCardState extends State<FlippableQuestionCard> with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;
  bool _isFront = true;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 500),
      vsync: this,
    );
    _animation = Tween<double>(begin: 0, end: 1).animate(_controller);
  }

  @override
  void didUpdateWidget(FlippableQuestionCard oldWidget) {
    super.didUpdateWidget(oldWidget);
    // Reset rotation when the text changes (new question)
    if (oldWidget.frontText != widget.frontText) {
      _controller.reverse();
      _isFront = true;
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  void _flip() {
    if (_isFront) {
      _controller.forward();
    } else {
      _controller.reverse();
    }
    setState(() {
      _isFront = !_isFront;
    });
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: _flip,
      child: AnimatedBuilder(
        animation: _animation,
        builder: (context, child) {
          final angle = _animation.value * math.pi;
          return Transform(
            transform: Matrix4.identity()
              ..setEntry(3, 2, 0.001) // perspective
              ..rotateY(angle),
            alignment: Alignment.center,
            child: angle < math.pi / 2
                ? _buildCard(widget.frontText, widget.frontFontSize, false)
                : Transform(
                    transform: Matrix4.identity()..rotateY(math.pi),
                    alignment: Alignment.center,
                    child: _buildCard(widget.backText, widget.backFontSize, true),
                  ),
          );
        },
      ),
    );
  }

  Widget _buildCard(String text, double fontSize, bool isBack) {
    return Card(
      elevation: 12,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: 300,
        height: 300,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Stack(
          children: [
            if (!isBack)
              const Align(
                alignment: Alignment.topRight,
                child: Icon(Icons.rotate_right, color: Colors.black26, size: 32),
              ),
            Center(
              child: Text(
                text,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: fontSize,
                  fontWeight: FontWeight.bold,
                  color: isBack ? Colors.blue : (widget.color ?? Colors.blue),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
