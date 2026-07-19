import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';

class RecapGridItem extends StatelessWidget {
  final String character;
  final Color color;
  final VoidCallback onClick;

  const RecapGridItem({
    super.key,
    required this.character,
    required this.color,
    required this.onClick,
  });

  @override
  Widget build(BuildContext context) {
    final textColor = color.computeLuminance() > 0.5 ? Colors.black : Colors.white;

    return InkWell(
      onTap: onClick,
      child: Container(
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(4),
        ),
        alignment: Alignment.center,
        child: LayoutBuilder(
          builder: (context, constraints) {
            final fontSize = (constraints.maxWidth * 0.45).clamp(18.0, 36.0);
            return Text(
              character,
              style: TextStyle(
                fontSize: fontSize,
                color: textColor,
                fontWeight: FontWeight.bold,
              ),
            );
          },
        ),
      ),
    );
  }
}

class PaginationControls extends StatelessWidget {
  final int currentPage;
  final int totalPages;
  final VoidCallback onPrevClick;
  final VoidCallback onNextClick;

  const PaginationControls({
    super.key,
    required this.currentPage,
    required this.totalPages,
    required this.onPrevClick,
    required this.onNextClick,
  });

  @override
  Widget build(BuildContext context) {
    final onBackground = Theme.of(context).colorScheme.onSurface;

    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          onPressed: currentPage > 0 ? onPrevClick : null,
          icon: Icon(Icons.arrow_back, color: onBackground),
        ),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            "Page ${currentPage + 1} of $totalPages",
            style: TextStyle(fontWeight: FontWeight.bold, color: onBackground),
          ),
        ),
        IconButton(
          onPressed: currentPage < totalPages - 1 ? onNextClick : null,
          icon: Icon(Icons.arrow_forward, color: onBackground),
        ),
      ],
    );
  }
}

class ModeSelector<T> extends StatelessWidget {
  final String? title;
  final List<MapEntry<String, T>> options;
  final T selectedOption;
  final ValueChanged<T> onOptionSelected;
  final bool enabled;

  const ModeSelector({
    super.key,
    this.title,
    required this.options,
    required this.selectedOption,
    required this.onOptionSelected,
    this.enabled = true,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        if (title != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 4.0),
            child: Text(
              title!,
              style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface.withValues(alpha: 0.7)),
            ),
          ),
        Container(
          margin: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.8),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: options.map((entry) {
              final isSelected = entry.value == selectedOption;
              return Expanded(
                child: InkWell(
                  onTap: enabled ? () => onOptionSelected(entry.value) : null,
                  borderRadius: BorderRadius.circular(12),
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    decoration: isSelected
                        ? BoxDecoration(
                            color: theme.colorScheme.primary.withValues(alpha: 0.2),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: theme.colorScheme.primary, width: 2),
                          )
                        : null,
                    child: Text(
                      entry.key,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: isSelected ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ),
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }
}

class PlayAndReviewButtons extends StatelessWidget {
  final VoidCallback onPlayClick;
  final VoidCallback onReviewClick;
  final bool isReviewEnabled;

  const PlayAndReviewButtons({
    super.key,
    required this.onPlayClick,
    required this.onReviewClick,
    required this.isReviewEnabled,
  });

  @override
  Widget build(BuildContext context) {
    final settings = context.read<SettingsProvider>();
    final theme = Theme.of(context);

    return Row(
      children: [
        Expanded(
          child: _BigButton(
            label: settings.getString("game_recap_play").toUpperCase(),
            onPressed: onPlayClick,
            backgroundColor: theme.colorScheme.primary,
            foregroundColor: theme.colorScheme.onPrimary,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _BigButton(
            label: settings.getString("mode_revise").toUpperCase(),
            onPressed: isReviewEnabled ? onReviewClick : null,
            backgroundColor: theme.colorScheme.primary,
            foregroundColor: theme.colorScheme.onPrimary,
            disabledBackgroundColor: theme.colorScheme.secondary.withValues(alpha: 0.3),
            disabledForegroundColor: theme.colorScheme.onSecondary.withValues(alpha: 0.3),
          ),
        ),
      ],
    );
  }
}

class PlayButton extends StatelessWidget {
  final VoidCallback onClick;

  const PlayButton({super.key, required this.onClick});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final settings = context.read<SettingsProvider>();

    return ElevatedButton(
      onPressed: onClick,
      style: ElevatedButton.styleFrom(
        backgroundColor: theme.colorScheme.primary,
        foregroundColor: theme.colorScheme.onPrimary,
        minimumSize: const Size.fromHeight(120),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        elevation: 16,
      ),
      child: Text(
        settings.getString("game_recap_play").toUpperCase(),
        style: const TextStyle(fontSize: 48, fontWeight: FontWeight.bold),
      ),
    );
  }
}

class _BigButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final Color backgroundColor;
  final Color foregroundColor;
  final Color? disabledBackgroundColor;
  final Color? disabledForegroundColor;

  const _BigButton({
    required this.label,
    required this.onPressed,
    required this.backgroundColor,
    required this.foregroundColor,
    this.disabledBackgroundColor,
    this.disabledForegroundColor,
  });

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: backgroundColor,
        foregroundColor: foregroundColor,
        disabledBackgroundColor: disabledBackgroundColor ?? backgroundColor.withValues(alpha: 0.3),
        disabledForegroundColor: disabledForegroundColor ?? foregroundColor.withValues(alpha: 0.3),
        minimumSize: const Size.fromHeight(120),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        elevation: 16,
      ),
      child: Text(
        label,
        style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
      ),
    );
  }
}

