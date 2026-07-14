import 'package:flutter/services.dart';
import 'package:xml/xml.dart';

class StringProvider {
  Map<String, String> _localizedStrings = {};

  Future<void> loadStrings(String locale) async {
    _localizedStrings.clear();
    // 1. Load default strings first to ensure fallbacks for missing keys
    try {
      await _loadFromPath('assets/values/strings.xml');
    } catch (e) {
      print("Could not load default strings: $e");
    }

    // 2. Load localized strings on top of default ones
    if (locale != 'values' && locale.isNotEmpty) {
      final path = 'assets/values-$locale/strings.xml';
      try {
        await _loadFromPath(path);
      } catch (e) {
        // Keep default fallbacks if localized file fails or is missing
      }
    }
  }

  Future<void> _loadFromPath(String path) async {
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

    // Replace indexed placeholders first, e.g. %1$d, %1$s, %2$d, etc.
    for (int i = 0; i < args.length; i++) {
      final indexPlaceholderRegExp = RegExp('%${i + 1}\\\$[ds]');
      value = value.replaceAll(indexPlaceholderRegExp, args[i].toString());
    }

    // Replace unindexed placeholders, e.g. %d, %s sequentially
    for (var arg in args) {
      final firstUnindexedRegExp = RegExp('%[ds]');
      if (firstUnindexedRegExp.hasMatch(value)) {
        value = value.replaceFirst(firstUnindexedRegExp, arg.toString());
      }
    }

    // Replace %% with % (standard escaping in format strings)
    value = value.replaceAll('%%', '%');

    return value;
  }
}
