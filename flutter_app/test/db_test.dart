import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:nihongo_mochi_flutter/db/database.dart';
import 'package:nihongo_mochi_flutter/repositories/score_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/settings_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  // Mock path_provider MethodChannel
  const MethodChannel('plugins.flutter.io/path_provider')
      .setMockMethodCallHandler((MethodCall methodCall) async {
    return '.';
  });

  late MochiDatabase db;
  late SettingsRepository settingsRepo;
  late ScoreRepository scoreRepo;

  setUp(() async {
    SharedPreferences.setMockInitialValues({});
    final prefs = await SharedPreferences.getInstance();
    settingsRepo = SettingsRepository(prefs);

    // MochiDatabase opens the local SQLite database
    db = MochiDatabase();
    scoreRepo = ScoreRepository(db, settingsRepo);

    // Clean tables before test
    await db.delete(db.learningScoreEntities).go();
    await db.delete(db.userLists).go();
  });

  tearDown(() async {
    await db.close();
  });

  test('Test saveScore wrong answer adds to revision list', () async {
    // 1. Verify initially empty
    final initialList = await scoreRepo.getListItems('Recognition_List');
    expect(initialList, isEmpty);

    // 2. Save a wrong answer
    await scoreRepo.saveScore(
      key: '漢',
      wasCorrect: false,
      type: ScoreType.recognition,
    );

    // 3. Verify it is saved in scores
    final score = await scoreRepo.getScore('漢', ScoreType.recognition);
    expect(score, isNotNull);
    expect(score!.successes, equals(0));
    expect(score.failures, equals(1));

    // 4. Verify it was added to the revision list
    final revisionList = await scoreRepo.getListItems('Recognition_List');
    expect(revisionList, contains('漢'));
  });

  test('Test saveScore correct answer removes from revision list when balance is >= 10', () async {
    // 1. Manually add to revision list
    await scoreRepo.addItemToList('Recognition_List', '字');

    // 2. Verify it is in list
    var revisionList = await scoreRepo.getListItems('Recognition_List');
    expect(revisionList, contains('字'));

    // 3. Save a correct answer (successes = 1, balance = 1)
    await scoreRepo.saveScore(
      key: '字',
      wasCorrect: true,
      type: ScoreType.recognition,
    );

    // 4. Verify it is still in list because balance is 1 (< 10)
    revisionList = await scoreRepo.getListItems('Recognition_List');
    expect(revisionList, contains('字'));

    // 5. Save 9 more correct answers (successes = 10, failures = 0, balance = 10)
    for (int i = 0; i < 9; i++) {
      await scoreRepo.saveScore(
        key: '字',
        wasCorrect: true,
        type: ScoreType.recognition,
      );
    }

    // 6. Verify it is now removed from list because balance is 10 (>= 10)
    revisionList = await scoreRepo.getListItems('Recognition_List');
    expect(revisionList, isNot(contains('字')));
  });
}
