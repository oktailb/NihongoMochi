import 'package:flutter/material.dart';

/// Reusable software back button styled for NihongoMochi.
class MochiBackButton extends StatelessWidget {
  final VoidCallback? onPressed;
  final Color? color;

  const MochiBackButton({
    super.key,
    this.onPressed,
    this.color,
  });

  @override
  Widget build(BuildContext context) {
    final iconColor = color ?? Theme.of(context).colorScheme.onSurface;
    return IconButton(
      icon: Icon(Icons.arrow_back, color: iconColor),
      tooltip: MaterialLocalizations.of(context).backButtonTooltip,
      onPressed: onPressed ?? () => Navigator.of(context).maybePop(),
    );
  }
}
