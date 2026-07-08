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
    final IconData icon = _getIcon();
    final Color color = _getColor(context);

    Widget content = Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.9),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.5)),
        boxShadow: [
          BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 4),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 24, color: color),
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
            color: color.withOpacity(0.7),
          ),
          if (!isLeftSide) content,
        ],
      ),
    );
  }

  IconData _getIcon() {
    switch (type) {
      case StatisticsType.recognition: return Icons.visibility;
      case StatisticsType.reading: return Icons.menu_book;
      case StatisticsType.grammar: return Icons.g_translate;
      case StatisticsType.writing: return Icons.edit;
      case StatisticsType.games: return Icons.videogame_asset;
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
