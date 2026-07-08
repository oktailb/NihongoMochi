import 'package:flutter/material.dart';

class MochiBackground extends StatelessWidget {
  final Widget child;

  const MochiBackground({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      height: double.infinity,
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)],
        ),
      ),
      child: child,
    );
  }
}
