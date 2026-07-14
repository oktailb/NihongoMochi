import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';

class GameRestoreCard extends StatelessWidget {
  final VoidCallback onResumeClick;
  final VoidCallback onNewGameClick;

  const GameRestoreCard({
    super.key,
    required this.onResumeClick,
    required this.onNewGameClick,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final settings = Provider.of<SettingsProvider>(context, listen: false);

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.primaryContainer,
      elevation: 8,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(
              settings.getString("exit_dialog_title"),
              style: TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: theme.colorScheme.onPrimaryContainer,
              ),
            ),
            const SizedBox(height: 12),
            ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                minimumSize: const Size.fromHeight(48),
                backgroundColor: theme.colorScheme.primary,
                foregroundColor: theme.colorScheme.onPrimary,
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              icon: const Icon(Icons.history),
              label: Text(settings.getString("exit_dialog_resume")),
              onPressed: onResumeClick,
            ),
            const SizedBox(height: 8),
            TextButton(
              onPressed: onNewGameClick,
              child: Text(
                settings.getString("exit_dialog_quit_lose_progress"),
                style: TextStyle(color: theme.colorScheme.onPrimaryContainer.withValues(alpha: 0.8)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
