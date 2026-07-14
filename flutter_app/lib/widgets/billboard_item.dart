import 'package:flutter/material.dart';
import '../models/saga.dart';

class BillboardItem extends StatelessWidget {
  final StatisticsType type;
  final int progress;
  final bool isLeftSide;
  final VoidCallback onClick;

  const BillboardItem({
    super.key,
    required this.type,
    required this.progress,
    required this.isLeftSide,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    final String imageAsset = _getImageAsset();
    final Color color = _getColor(context);

    Widget content = Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.9),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withValues(alpha: 0.5)),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 4),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Image.asset(
            imageAsset,
            width: 36,
            height: 36,
            fit: BoxFit.contain,
          ),
          Text(
            "$progress%",
            style: const TextStyle(fontSize: 10, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );

    return GestureDetector(
      onTap: onClick,
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (isLeftSide) content,
          Icon(
            isLeftSide ? Icons.arrow_right : Icons.arrow_left,
            color: color.withValues(alpha: 0.7),
          ),
          if (!isLeftSide) content,
        ],
      ),
    );
  }

  String _getImageAsset() {
    switch (type) {
      case StatisticsType.recognition: return 'assets/drawable/recognising.webp';
      case StatisticsType.reading: return 'assets/drawable/reading.webp';
      case StatisticsType.grammar: return 'assets/drawable/grammar.webp';
      case StatisticsType.writing: return 'assets/drawable/writing.webp';
      case StatisticsType.games: return 'assets/drawable/recognising.webp';
    }
  }

  Color _getColor(BuildContext context) {
    switch (type) {
      case StatisticsType.recognition: return Colors.blue;
      case StatisticsType.reading: return Colors.green;
      case StatisticsType.grammar: return Colors.orange;
      case StatisticsType.writing: return Colors.purple;
      case StatisticsType.games: return Colors.teal;
    }
  }
}
