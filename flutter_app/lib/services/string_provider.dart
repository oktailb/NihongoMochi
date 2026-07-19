import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:xml/xml.dart';

class StringProvider {
  final Map<String, String> _localizedStrings = {};

  Future<void> loadStrings(String locale) async {
    _localizedStrings.clear();

    // 1. Try loading from generated ARB / JSON asset first
    bool loadedFromArb = false;
    final arbLocale = _mapLocaleToArb(locale);

    try {
      final arbPath = 'lib/l10n/app_$arbLocale.arb';
      final jsonString = await rootBundle.loadString(arbPath);
      final Map<String, dynamic> jsonMap = json.decode(jsonString);
      jsonMap.forEach((key, value) {
        if (!key.startsWith('@') && value is String) {
          _localizedStrings[key] = value;
        }
      });
      loadedFromArb = true;
    } catch (e) {
      // Fallback to XML
    }

    // 2. Fallback to XML if ARB was not loaded from rootBundle
    if (!loadedFromArb) {
      try {
        await _loadFromXmlPath('assets/values/strings.xml');
      } catch (e) {
        // Ignored
      }

      if (locale != 'values' && locale.isNotEmpty) {
        final path = 'assets/values-$locale/strings.xml';
        try {
          await _loadFromXmlPath(path);
        } catch (e) {
          // Ignored
        }
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
    return parts.isNotEmpty ? parts[0].toLowerCase() : 'en';
  }


  Future<void> _loadFromXmlPath(String path) async {
    final xmlString = await rootBundle.loadString(path);
    final document = XmlDocument.parse(xmlString);
    final resources = document.findAllElements('string');

    for (var element in resources) {
      final name = element.getAttribute('name');
      if (name != null) {
        _localizedStrings[name] = element.innerText;
      }
    }
  }

  String getString(String key, [List<dynamic>? args]) {
    String value = _localizedStrings[key] ?? key;
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

