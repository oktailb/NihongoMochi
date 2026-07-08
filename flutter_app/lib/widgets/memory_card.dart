import 'dart:math';
import 'package:flutter/material.dart';
import '../models/memorize.dart';

class MemoryCard extends StatelessWidget {
  final MemorizeCardState state;
  final VoidCallback onClick;

  const MemoryCard({
    super.key,
    required this.state,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onClick,
      child: TweenAnimationBuilder<double>(
        tween: Tween<double>(begin: 0, end: (state.isFaceUp || state.isMatched) ? 180 : 0),
        duration: const Duration(milliseconds: 400),
        builder: (context, rotation, child) {
          final isBack = rotation < 90;
          return Transform(
            transform: Matrix4.identity()
              ..setEntry(3, 2, 0.001) // perspective
              ..rotateY(rotation * pi / 180),
            alignment: Alignment.center,
            child: isBack
                ? _buildBack(context)
                : Transform(
                    transform: Matrix4.identity()..rotateY(pi),
                    alignment: Alignment.center,
                    child: _buildFront(context),
                  ),
          );
        },
      ),
    );
  }

  Widget _buildBack(BuildContext context) {
    return Card(
      elevation: 4,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      color: Theme.of(context).colorScheme.primary,
      child: const Center(
        child: Text(
          "?",
          style: TextStyle(fontSize: 24, color: Colors.white, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildFront(BuildContext context) {
    final bool isDark = Theme.of(context).brightness == Brightness.dark;
    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
      color: state.isMatched
          ? (isDark ? Colors.grey.shade800 : Colors.grey.shade200)
          : (isDark ? Colors.grey.shade900 : Colors.white),
      child: Center(
        child: Text(
          state.item.character,
          style: TextStyle(
            fontSize: 32,
            color: state.isMatched ? Colors.grey : (isDark ? Colors.white : Colors.black),
          ),
        ),
      ),
    );
  }
}
