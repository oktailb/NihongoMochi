import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/settings_provider.dart';

class MochiBackground extends StatelessWidget {
  final Widget child;

  const MochiBackground({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    final isDark = context.watch<SettingsProvider>().isDarkMode;
    final bgImage = isDark ? 'assets/drawable/background_night.webp' : 'assets/drawable/background_day.webp';

    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: BoxDecoration(
        image: DecorationImage(
          image: AssetImage(bgImage),
          fit: BoxFit.cover,
        ),
      ),
      child: child,
    );
  }
}
