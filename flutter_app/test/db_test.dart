import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:nihongo_mochi_flutter/db/database.dart';
import 'package:nihongo_mochi_flutter/repositories/score_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/settings_repository.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
      .setMockMethodCallHandler(
    const MethodChannel('plugins.flutter.io/path_provider'),
    (MethodCall methodCall) async => '.',
  );


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

  test('Test decayScores reduces successes for items unreviewed for >= 1 week', () async {
    final twoWeeksAgo = DateTime.now().subtract(const Duration(days: 14)).millisecondsSinceEpoch;

    // Insert score reviewed 2 weeks ago with 10 successes
    await db.into(db.learningScoreEntities).insert(
      LearningScoreEntity(
        key: '木',
        type: 'RECOGNITION',
        successes: 10,
        failures: 0,
        lastReviewDate: twoWeeksAgo,
      ),
    );

    // Run decay (2 weeks passed = 20% decay => 10 * 0.8 = 8 successes)
    final decayed = await scoreRepo.decayScores();
    expect(decayed, isTrue);

    final score = await scoreRepo.getScore('木', ScoreType.recognition);
    expect(score, isNotNull);
    expect(score!.successes, equals(8));
  });

  test('Test getAllScoresGroupedByType returns batch map of scores', () async {
    await scoreRepo.saveScore(key: '日', wasCorrect: true, type: ScoreType.recognition);
    await scoreRepo.saveScore(key: '月', wasCorrect: true, type: ScoreType.reading);

    final map = await scoreRepo.getAllScoresGroupedByType();
    expect(map.containsKey('RECOGNITION'), isTrue);
    expect(map['RECOGNITION']?.containsKey('日'), isTrue);
    expect(map.containsKey('READING'), isTrue);
    expect(map['READING']?.containsKey('月'), isTrue);
  });
}

