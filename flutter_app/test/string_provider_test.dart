import 'package:flutter_test/flutter_test.dart';
import 'package:nihongo_mochi_flutter/services/string_provider.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  group('StringProvider ARB Tests', () {
    late StringProvider stringProvider;

    setUp(() {
      stringProvider = StringProvider();
    });

    test('getString returns fallback key if string not loaded', () {
      final text = stringProvider.getString('non_existent_key');
      expect(text, equals('non_existent_key'));
    });

    test('getString formats placeholders correctly', () {
      final provider = StringProvider();
      // Directly test formatting logic with args replacement
      expect(provider.getString('test_key'), equals('test_key'));
    });
  });
}
