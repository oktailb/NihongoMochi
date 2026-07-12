import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

class MochiTheme {
  static ColorScheme get lightColorScheme => const ColorScheme(
        brightness: Brightness.light,
        primary: Color(0xFF4DB6AC),
        onPrimary: Color(0xFFFFFFFF),
        primaryContainer: Color(0xFFB2DFDB),
        onPrimaryContainer: Color(0xFF004D40),
        secondary: Color(0xFF8BC34A),
        onSecondary: Color(0xFFFFFFFF),
        secondaryContainer: Color(0xFFDCEDC8),
        onSecondaryContainer: Color(0xFF33691E),
        tertiary: Color(0xFF2196F3),
        onTertiary: Color(0xFFFFFFFF),
        tertiaryContainer: Color(0xFFBBDEFB),
        onTertiaryContainer: Color(0xFF0D47A1),
        error: Color(0xFFB00020),
        onError: Color(0xFFFFFFFF),
        background: Color(0xFFE0F7FA),
        onBackground: Color(0xFF263238),
        surface: Color(0xFFFFFFFF),
        onSurface: Color(0xFF000000),
        outline: Color(0xFF79747E),
        surfaceVariant: Color(0xFFE0E2EC),
        onSurfaceVariant: Color(0xFF44474F),
      );

  static ColorScheme get darkColorScheme => const ColorScheme(
        brightness: Brightness.dark,
        primary: Color(0xFFFF6D00),
        onPrimary: Color(0xFFD6D6D6),
        primaryContainer: Color(0xFFE65100),
        onPrimaryContainer: Color(0xFFFFD180),
        secondary: Color(0xFF8BC34A),
        onSecondary: Color(0xFF1B360D),
        secondaryContainer: Color(0xFF33691E),
        onSecondaryContainer: Color(0xFFDCEDC8),
        tertiary: Color(0xFF90CAF9),
        onTertiary: Color(0xFF0D47A1),
        tertiaryContainer: Color(0xFF1976D2),
        onTertiaryContainer: Color(0xFFBBDEFB),
        error: Color(0xFFCF6679),
        onError: Color(0xFF601410),
        background: Color(0xFF011627),
        onBackground: Color(0xFFD6D6D6),
        surface: Color(0xFF1E2A38),
        onSurface: Color(0xFFD6D6D6),
        outline: Color(0xFF938F99),
        surfaceVariant: Color(0xFF44474F),
        onSurfaceVariant: Color(0xFFC4C7D0),
      );

  static ThemeData get lightTheme => ThemeData(
        useMaterial3: true,
        colorScheme: lightColorScheme,
        textTheme: GoogleFonts.notoSansJpTextTheme(ThemeData.light().textTheme),
      );

  static ThemeData get darkTheme => ThemeData(
        useMaterial3: true,
        colorScheme: darkColorScheme,
        textTheme: GoogleFonts.notoSansJpTextTheme(ThemeData.dark().textTheme),
      );
}
