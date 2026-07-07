import 'package:flutter/services.dart';
import 'package:xml/xml.dart';

class StringProvider {
  Map<String, String> _localizedStrings = {};

  Future<void> loadStrings(String locale) async {
    // Try specific locale, fallback to default 'values'
    String path = 'assets/values-$locale/strings.xml';
    try {
      await _loadFromPath(path);
    } catch (e) {
      try {
        await _loadFromPath('assets/values/strings.xml');
      } catch (e) {
        print("Could not load strings for $locale or default");
      }
    }
  }

  Future<void> _loadFromPath(String path) async {
    final xmlString = await rootBundle.loadString(path);
    final document = XmlDocument.parse(xmlString);
    final resources = document.findAllElements('string');

    _localizedStrings = {
      for (var element in resources)
        element.getAttribute('name') ?? '': element.innerText
    };
  }

  String getString(String key) {
    return _localizedStrings[key] ?? key;
  }
}
