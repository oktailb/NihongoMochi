import 'package:flutter_test/flutter_test.dart';
import 'package:nihongo_mochi_flutter/providers/simon_provider.dart';
import 'package:nihongo_mochi_flutter/repositories/kana_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/dictionary_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/settings_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/score_repository.dart';
import 'package:nihongo_mochi_flutter/services/level_content_provider.dart';
import 'package:nihongo_mochi_flutter/services/audio_service.dart';
import 'package:nihongo_mochi_flutter/services/resource_loader.dart';
import 'package:nihongo_mochi_flutter/services/language_pack_manager.dart';
import 'package:nihongo_mochi_flutter/db/database.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  test('SimonProvider can be initialized without KanjiRepository', () async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    final settingsRepo = SettingsRepository(prefs);
    final db = MochiDatabase();
    final scoreRepo = ScoreRepository(db, settingsRepo);
    final lpManager = LanguagePackManager();
    final loader = ResourceLoader(lpManager);

    final kanaRepo = KanaRepository(loader);
    final dictRepo = DictionaryRepository(loader);
    final wordRepo = WordRepository(loader);
    final audioService = AudioService(settingsRepo);
    final contentProvider = LevelContentProvider(
      kanaRepo: kanaRepo,
      dictionaryRepo: dictRepo,
      wordRepo: wordRepo,
      scoreRepo: scoreRepo,
    );

    final simonProvider = SimonProvider(
      kanaRepo,
      dictRepo,
      settingsRepo,
      contentProvider,
      scoreRepo,
      audioService,
    );

    expect(simonProvider, isNotNull);
    await db.close();
  });
}
