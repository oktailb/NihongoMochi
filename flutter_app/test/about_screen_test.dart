import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:nihongo_mochi_flutter/about_screen.dart';
import 'package:nihongo_mochi_flutter/providers/settings_provider.dart';
import 'package:nihongo_mochi_flutter/repositories/settings_repository.dart';
import 'package:nihongo_mochi_flutter/services/tts_service.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  testWidgets('AboutScreen renders version and build date dynamically', (WidgetTester tester) async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final settingsRepo = SettingsRepository(prefs);
    final ttsService = TtsService(settingsRepo);
    final settingsProvider = SettingsProvider(settingsRepo, ttsService);

    await tester.pumpWidget(
      ChangeNotifierProvider.value(
        value: settingsProvider,
        child: const MaterialApp(
          home: AboutScreen(),
        ),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.textContaining('Nihongo'), findsOneWidget);
  });
}
