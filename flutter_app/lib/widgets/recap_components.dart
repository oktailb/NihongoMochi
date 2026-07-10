import 'package:flutter/material.dart';

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
        child: Text(
          character,
          style: TextStyle(
            fontSize: 20,
            color: textColor,
            fontWeight: FontWeight.bold,
          ),
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
    return Row(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        IconButton(
          onPressed: currentPage > 0 ? onPrevClick : null,
          icon: const Icon(Icons.arrow_back),
        ),
        Text(
          "Page ${currentPage + 1} of $totalPages",
          style: const TextStyle(fontWeight: FontWeight.bold),
        ),
        IconButton(
          onPressed: currentPage < totalPages - 1 ? onNextClick : null,
          icon: const Icon(Icons.arrow_forward),
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
    return Column(
      children: [
        if (title != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 4.0),
            child: Text(
              title!,
              style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: Colors.black54),
            ),
          ),
        Container(
          margin: const EdgeInsets.symmetric(horizontal: 8),
          decoration: BoxDecoration(
            color: Colors.white.withOpacity(0.5),
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
                            color: Colors.pink.withOpacity(0.2),
                            borderRadius: BorderRadius.circular(12),
                            border: Border.all(color: Colors.pink, width: 2),
                          )
                        : null,
                    child: Text(
                      entry.key,
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontWeight: FontWeight.bold,
                        color: isSelected ? Colors.pink : Colors.black54,
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
    return Row(
      children: [
        Expanded(
          child: _BigButton(
            label: "JOUER",
            onPressed: onPlayClick,
            color: Colors.pink,
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _BigButton(
            label: "RÉVISER",
            onPressed: isReviewEnabled ? onReviewClick : null,
            color: Colors.orange,
          ),
        ),
      ],
    );
  }
}

class _BigButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final Color color;

  const _BigButton({
    required this.label,
    required this.onPressed,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: color,
        foregroundColor: Colors.white,
        padding: const EdgeInsets.symmetric(vertical: 20),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        elevation: 8,
      ),
      child: Text(
        label,
        style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
      ),
    );
  }
}
