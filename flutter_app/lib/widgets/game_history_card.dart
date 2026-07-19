import 'package:flutter/material.dart';

class GameHistoryCard extends StatelessWidget {
  final List<dynamic> history;
  final String emptyMessage;
  final Widget Function(dynamic) itemBuilder;

  const GameHistoryCard({
    super.key,
    required this.history,
    required this.emptyMessage,
    required this.itemBuilder,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: Colors.white.withValues(alpha: 0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              "SCORES RÉCENTS",
              style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.black54),
            ),
            const SizedBox(height: 12),
            if (history.isEmpty)
              Padding(
                padding: const EdgeInsets.symmetric(vertical: 20),
                child: Center(
                  child: Text(
                    emptyMessage,
                    style: const TextStyle(color: Colors.grey, fontStyle: FontStyle.italic),
                  ),
                ),
              )
            else
              ...history.take(5).map((item) => itemBuilder(item)),
          ],
        ),
      ),
    );
  }
}

class GameHistoryRow extends StatelessWidget {
  final String label;
  final String score;
  final String time;

  const GameHistoryRow({
    super.key,
    required this.label,
    required this.score,
    required this.time,
  });

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            flex: 2,
            child: Text(label, style: const TextStyle(fontWeight: FontWeight.w500)),
          ),
          Expanded(
            child: Text(score, textAlign: TextAlign.center, style: const TextStyle(color: Colors.pink, fontWeight: FontWeight.bold)),
          ),
          Expanded(
            child: Text(time, textAlign: TextAlign.end, style: const TextStyle(color: Colors.black54)),
          ),
        ],
      ),
    );
  }
}
