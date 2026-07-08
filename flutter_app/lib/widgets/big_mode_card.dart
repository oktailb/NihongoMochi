import 'package:flutter/material.dart';

class BigModeCard extends StatelessWidget {
  final String title;
  final String subtitle;
  final String kanjiTitle;
  final VoidCallback onClick;
  final bool enabled;

  const BigModeCard({
    super.key,
    required this.title,
    required this.subtitle,
    required this.kanjiTitle,
    required this.onClick,
    this.enabled = true,
  });

  @override
  Widget build(BuildContext context) {
    final double alpha = enabled ? 1.0 : 0.5;

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: enabled ? 2 : 0,
      color: Colors.white.withOpacity(enabled ? 0.9 : 0.5),
      child: InkWell(
        onTap: enabled ? onClick : null,
        borderRadius: BorderRadius.circular(16),
        child: Padding(
          padding: const EdgeInsets.all(16.0),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      title,
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                        color: Colors.black.withOpacity(alpha),
                      ),
                    ),
                    const SizedBox(height: 6),
                    Text(
                      subtitle,
                      style: TextStyle(
                        fontSize: 11,
                        color: Colors.black54.withOpacity(alpha),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              Text(
                kanjiTitle,
                style: TextStyle(
                  fontSize: 40,
                  fontWeight: FontWeight.bold,
                  color: Colors.pink.withOpacity(alpha),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
