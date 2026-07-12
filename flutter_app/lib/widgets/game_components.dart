import 'package:flutter/material.dart';
import 'dart:math' as math;
import 'package:provider/provider.dart';
import '../models/quiz_models.dart';
import '../providers/settings_provider.dart';

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
    final theme = Theme.of(context);
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
                color = theme.colorScheme.error;
                break;
              case GameStatus.partial:
                icon = Icons.history;
                color = const Color(0xFF4FC3F7);
                break;
              default:
                icon = Icons.radio_button_unchecked;
                color = theme.colorScheme.onSurface.withOpacity(0.4);
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

  InlineSpan _formatQuestionText(String text, TextStyle baseStyle, Color quoteColor) {
    final regex = RegExp(r'「([^」]+)」');
    final matches = regex.allMatches(text);
    if (matches.isEmpty) {
      return TextSpan(text: text, style: baseStyle);
    }

    final spans = <InlineSpan>[];
    int lastIndex = 0;
    for (final match in matches) {
      if (match.start > lastIndex) {
        spans.add(TextSpan(text: text.substring(lastIndex, match.start), style: baseStyle));
      }
      spans.add(TextSpan(
        text: '「${match.group(1)}」',
        style: baseStyle.copyWith(
          fontWeight: FontWeight.bold,
          decoration: TextDecoration.underline,
          color: quoteColor,
        ),
      ));
      lastIndex = match.end;
    }
    if (lastIndex < text.length) {
      spans.add(TextSpan(text: text.substring(lastIndex), style: baseStyle));
    }
    return TextSpan(children: spans);
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
    final theme = Theme.of(context);
    final double cardSize = (MediaQuery.of(context).size.shortestSide * 0.65).clamp(200.0, 320.0);
    return Card(
      elevation: 24,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
      child: Container(
        width: cardSize,
        height: cardSize,
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: theme.colorScheme.surface,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Stack(
          children: [
            if (!isBack)
              Align(
                alignment: Alignment.topRight,
                child: Icon(Icons.rotate_right, color: theme.colorScheme.onSurface.withOpacity(0.3), size: 32),
              ),
            Center(
              child: isBack
                  ? Text(
                      text,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: fontSize,
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                    )
                  : RichText(
                      textAlign: TextAlign.center,
                      text: _formatQuestionText(
                        text,
                        TextStyle(
                          fontSize: fontSize,
                          fontWeight: FontWeight.bold,
                          color: widget.color ?? theme.colorScheme.onSurface,
                        ),
                        const Color(0xFFD32F2F),
                      ),
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

class ExitConfirmationDialog extends StatefulWidget {
  final VoidCallback onConfirm;
  final VoidCallback onDismiss;
  final VoidCallback? onPause;
  final VoidCallback? onResume;
  final VoidCallback? onSaveAndExit;

  const ExitConfirmationDialog({
    super.key,
    required this.onConfirm,
    required this.onDismiss,
    this.onPause,
    this.onResume,
    this.onSaveAndExit,
  });

  @override
  State<ExitConfirmationDialog> createState() => _ExitConfirmationDialogState();
}

class _ExitConfirmationDialogState extends State<ExitConfirmationDialog> {
  @override
  void initState() {
    super.initState();
    widget.onPause?.call();
  }

  @override
  void dispose() {
    widget.onResume?.call();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final settings = context.read<SettingsProvider>();
    final theme = Theme.of(context);
    return AlertDialog(
      title: Text(settings.getString("exit_dialog_title")),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(settings.getString("exit_dialog_message")),
          if (widget.onSaveAndExit == null) ...[
            const SizedBox(height: 8),
            Text(
              settings.getString("exit_dialog_lose_progress"),
              style: TextStyle(
                color: theme.colorScheme.error,
                fontSize: 12,
              ),
            ),
          ],
        ],
      ),
      actions: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            ElevatedButton(
              onPressed: widget.onDismiss,
              style: ElevatedButton.styleFrom(
                backgroundColor: theme.colorScheme.primary,
                foregroundColor: theme.colorScheme.onPrimary,
              ),
              child: Text(settings.getString("exit_dialog_resume")),
            ),
            if (widget.onSaveAndExit != null) ...[
              const SizedBox(height: 8),
              OutlinedButton(
                onPressed: widget.onSaveAndExit,
                style: OutlinedButton.styleFrom(
                  foregroundColor: theme.colorScheme.primary,
                  side: BorderSide(color: theme.colorScheme.primary),
                ),
                child: Text(settings.getString("exit_dialog_pause_exit")),
              ),
            ],
            const SizedBox(height: 8),
            TextButton(
              onPressed: widget.onConfirm,
              style: TextButton.styleFrom(
                foregroundColor: theme.colorScheme.error,
              ),
              child: Text(settings.getString("exit_dialog_quit_lose_progress")),
            ),
          ],
        ),
      ],
    );
  }
}

class GameResultOverlay extends StatelessWidget {
  final bool isVictory;
  final String? score;
  final String? bestScore;
  final List<MapEntry<String, String>> stats;
  final VoidCallback onReplayClick;
  final VoidCallback onMenuClick;
  final String title;

  const GameResultOverlay({
    super.key,
    required this.isVictory,
    required this.title,
    this.score,
    this.bestScore,
    this.stats = const [],
    required this.onReplayClick,
    required this.onMenuClick,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final settings = context.read<SettingsProvider>();
    
    return Container(
      color: Colors.black.withOpacity(0.7),
      padding: const EdgeInsets.all(24),
      alignment: Alignment.center,
      child: Card(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
        elevation: 16,
        color: theme.colorScheme.surface,
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Title
              Text(
                title,
                style: TextStyle(
                  fontSize: 32,
                  fontWeight: FontWeight.w800,
                  color: isVictory ? const Color(0xFF4CAF50) : theme.colorScheme.error,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),

              // Score Area
              if (score != null) ...[
                Text(
                  score!,
                  style: TextStyle(
                    fontSize: 48,
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.primary,
                  ),
                ),
                Text(
                  settings.getString("game_score_label").toUpperCase(),
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],

              if (bestScore != null) ...[
                const SizedBox(height: 8),
                Text(
                  bestScore!,
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.bold,
                    color: theme.colorScheme.secondary,
                  ),
                ),
              ],

              // Additional Stats
              if (stats.isNotEmpty) ...[
                const SizedBox(height: 24),
                ...stats.map((stat) => Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        stat.key,
                        style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
                      ),
                      Text(
                        stat.value,
                        style: const TextStyle(fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                )),
              ],

              const SizedBox(height: 32),

              // Buttons
              ElevatedButton(
                onPressed: onReplayClick,
                style: ElevatedButton.styleFrom(
                  minimumSize: const Size.fromHeight(56),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  backgroundColor: theme.colorScheme.primary,
                  foregroundColor: theme.colorScheme.onPrimary,
                ),
                child: Text(
                  settings.getString("game_replay_button").toUpperCase(),
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
              const SizedBox(height: 12),
              OutlinedButton(
                onPressed: onMenuClick,
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size.fromHeight(56),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                  foregroundColor: theme.colorScheme.primary,
                  side: BorderSide(color: theme.colorScheme.primary),
                ),
                child: Text(
                  settings.getString("game_menu_button").toUpperCase(),
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
