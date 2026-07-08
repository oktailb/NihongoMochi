import 'package:flutter/material.dart';
import 'mochi_background.dart';
import 'play_button.dart';

class GameSetupTemplate extends StatelessWidget {
  final String title;
  final String subtitle;
  final VoidCallback onPlayClick;
  final List<Widget> children;

  const GameSetupTemplate({
    super.key,
    required this.title,
    required this.subtitle,
    required this.onPlayClick,
    required this.children,
  });

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: MochiBackground(
        child: SafeArea(
          child: Column(
            children: [
              // Header
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Text(
                      title,
                      style: TextStyle(
                        fontSize: 32,
                        fontWeight: FontWeight.bold,
                        color: Theme.of(context).colorScheme.primary,
                      ),
                    ),
                    Text(
                      subtitle,
                      style: TextStyle(
                        fontSize: 24,
                        color: Theme.of(context).colorScheme.secondary,
                      ),
                    ),
                  ],
                ),
              ),
              // Content
              Expanded(
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(horizontal: 16),
                  itemCount: children.length,
                  separatorBuilder: (context, index) => const SizedBox(height: 16),
                  itemBuilder: (context, index) => children[index],
                ),
              ),
              // Play Button
              Padding(
                padding: const EdgeInsets.all(16.0),
                child: PlayButton(onClick: onPlayClick),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
