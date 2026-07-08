import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'package:nihongo_mochi_flutter/db/database.dart';
import 'package:nihongo_mochi_flutter/repositories/score_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/dictionary_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/word_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/word_meaning_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/grammar_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/kana_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/settings_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/kanji_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/exercise_repository.dart';
import 'package:nihongo_mochi_flutter/repositories/level_repository.dart';
import 'package:nihongo_mochi_flutter/providers/settings_provider.dart';
import 'package:nihongo_mochi_flutter/providers/saga_provider.dart';
import 'package:nihongo_mochi_flutter/services/audio_service.dart';
import 'package:nihongo_mochi_flutter/services/level_content_provider.dart';
import 'package:nihongo_mochi_flutter/services/statistics_service.dart';
import 'package:nihongo_mochi_flutter/services/language_pack_manager.dart';
import 'package:nihongo_mochi_flutter/home_screen.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final database = MochiDatabase();
  final prefs = await SharedPreferences.getInstance();

  runApp(
    MultiProvider(
      providers: [
        // Base de données
        Provider<MochiDatabase>.value(value: database),

        // Repositories (Singletons)
        Provider<SettingsRepository>(create: (_) => SettingsRepository(prefs)),
        Provider<ScoreRepository>(create: (_) => ScoreRepository(database)),
        Provider<DictionaryRepository>(create: (_) => DictionaryRepository()),
        Provider<WordRepository>(create: (_) => WordRepository()),
        Provider<WordMeaningRepository>(create: (_) => WordMeaningRepository()),
        Provider<GrammarRepository>(create: (_) => GrammarRepository()),
        Provider<KanaRepository>(create: (_) => KanaRepository()),
        Provider<KanjiRepository>(create: (_) => KanjiRepository()),
        Provider<ExerciseRepository>(create: (_) => ExerciseRepository()),
        Provider<LevelRepository>(create: (_) => LevelRepository()),

        // Services
        Provider<LanguagePackManager>(create: (_) => LanguagePackManager()),
        ProxyProvider<SettingsRepository, AudioService>(
          update: (_, settings, __) => AudioService(settings),
          dispose: (_, audio) => audio.dispose(),
        ),
        ProxyProvider4<KanaRepository, DictionaryRepository, WordRepository, ScoreRepository, LevelContentProvider>(
          update: (_, kana, dict, word, score, __) => LevelContentProvider(
            kanaRepo: kana,
            dictionaryRepo: dict,
            wordRepo: word,
            scoreRepo: score,
          ),
        ),
        ProxyProvider2<LevelContentProvider, ScoreRepository, StatisticsService>(
          update: (_, content, score, __) => StatisticsService(
            levelContentProvider: content,
            scoreRepo: score,
          ),
        ),

        // Global Providers (State)
        ChangeNotifierProvider(
          create: (context) => SettingsProvider(context.read<SettingsRepository>()),
        ),
        ChangeNotifierProxyProvider2<LevelRepository, StatisticsService, SagaProvider>(
          create: (context) => SagaProvider(context.read<LevelRepository>(), context.read<StatisticsService>()),
          update: (context, levelRepo, stats, saga) => saga ?? SagaProvider(levelRepo, stats),
        ),
      ],
      child: const NihongoMochiApp(),
    ),
  );
}

class NihongoMochiApp extends StatelessWidget {
  const NihongoMochiApp({super.key});

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();

    return MaterialApp(
      title: 'Nihongo Mochi',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFE91E63),
          brightness: settings.isDarkMode ? Brightness.dark : Brightness.light,
        ),
        textTheme: GoogleFonts.notoSansTextTheme(),
      ),
      home: const HomeScreen(),
    );
  }
}
