import 'dart:convert';
import 'package:flutter/services.dart';

class StringProvider {
  final Map<String, String> _localizedStrings = {};
  final Map<String, String> _defaultStrings = {};

  Future<void> loadStrings(String locale) async {
    // 1. Load English default strings as fallback if not loaded yet
    if (_defaultStrings.isEmpty) {
      try {
        final jsonString = await rootBundle.loadString('lib/l10n/app_en.arb');
        final Map<String, dynamic> jsonMap = json.decode(jsonString);
        jsonMap.forEach((key, value) {
          if (!key.startsWith('@') && value is String) {
            _defaultStrings[key] = value;
          }
        });
      } catch (e) {
        // Fallback loading failed
      }
    }

    _localizedStrings.clear();
    final arbLocale = _mapLocaleToArb(locale);

    if (arbLocale != 'en') {
      try {
        final arbPath = 'lib/l10n/app_$arbLocale.arb';
        final jsonString = await rootBundle.loadString(arbPath);
        final Map<String, dynamic> jsonMap = json.decode(jsonString);
        jsonMap.forEach((key, value) {
          if (!key.startsWith('@') && value is String) {
            _localizedStrings[key] = value;
          }
        });
      } catch (e) {
        // Ignored, will fall back to _defaultStrings
      }
    }
  }

  String _mapLocaleToArb(String locale) {
    String clean = locale;
    if (clean.startsWith('values-')) {
      clean = clean.replaceFirst('values-', '');
    }
    clean = clean.replaceAll('-r', '-').replaceAll('_', '-');
    final parts = clean.split('-');
    String lang = parts.isNotEmpty ? parts[0].toLowerCase() : 'en';

    // Map legacy/Android language codes to Flutter ARB names
    if (lang == 'in') return 'id';
    if (lang == 'ua') return 'uk';

    return lang;
  }

  String getString(String key, [List<dynamic>? args]) {
    String value = _localizedStrings[key] ?? _defaultStrings[key] ?? key;
    if (args == null || args.isEmpty) {
      return value;
    }

    // Replace ARB placeholders {param1}, {param2}... or {0}, {1}...
    for (int i = 0; i < args.length; i++) {
      value = value.replaceAll('{param${i + 1}}', args[i].toString());
      value = value.replaceAll('{$i}', args[i].toString());
    }

    // Replace indexed Android placeholders e.g. %1$d, %1$s
    for (int i = 0; i < args.length; i++) {
      final indexPlaceholderRegExp = RegExp('%${i + 1}\\\$[ds]');
      value = value.replaceAll(indexPlaceholderRegExp, args[i].toString());
    }

    // Replace unindexed placeholders e.g. %d, %s
    for (var arg in args) {
      final firstUnindexedRegExp = RegExp('%[ds]');
      if (firstUnindexedRegExp.hasMatch(value)) {
        value = value.replaceFirst(firstUnindexedRegExp, arg.toString());
      }
    }

    // Replace %% with %
    value = value.replaceAll('%%', '%');

    return value;
  }
}


