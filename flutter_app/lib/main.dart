import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:nihongo_mochi_flutter/l10n/gen/app_localizations.dart';
import 'package:provider/provider.dart';
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
import 'package:nihongo_mochi_flutter/providers/taquin_provider.dart';
import 'package:nihongo_mochi_flutter/providers/snake_provider.dart';
import 'package:nihongo_mochi_flutter/providers/crossword_provider.dart';
import 'package:nihongo_mochi_flutter/providers/particle_defender_provider.dart';
import 'package:nihongo_mochi_flutter/providers/simon_provider.dart';
import 'package:nihongo_mochi_flutter/providers/memorize_provider.dart';
import 'package:nihongo_mochi_flutter/providers/kana_link_provider.dart';
import 'package:nihongo_mochi_flutter/providers/shiritori_provider.dart';
import 'package:nihongo_mochi_flutter/services/audio_service.dart';
import 'package:nihongo_mochi_flutter/services/level_content_provider.dart';
import 'package:nihongo_mochi_flutter/services/statistics_service.dart';
import 'package:nihongo_mochi_flutter/services/language_pack_manager.dart';
import 'package:nihongo_mochi_flutter/services/resource_loader.dart';
import 'package:nihongo_mochi_flutter/services/tts_service.dart';
import 'package:nihongo_mochi_flutter/home_screen.dart';
import 'package:nihongo_mochi_flutter/theme/mochi_theme.dart';


void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  final database = MochiDatabase();
  final prefs = await SharedPreferences.getInstance();
  final settingsRepo = SettingsRepository(prefs);
  final scoreRepo = ScoreRepository(database, settingsRepo);

  // Trigger score decay in background on application launch
  scoreRepo.decayScores();

  runApp(
    MultiProvider(
      providers: [
        // Base de données
        Provider<MochiDatabase>.value(value: database),

        // Services
        Provider<LanguagePackManager>(create: (_) => LanguagePackManager()),
        ProxyProvider<LanguagePackManager, ResourceLoader>(
          update: (_, lp, _) => ResourceLoader(lp),
        ),

        // Repositories (Singletons)
        Provider<SettingsRepository>.value(value: settingsRepo),
        Provider<ScoreRepository>.value(value: scoreRepo),
        ProxyProvider<SettingsRepository, TtsService>(
          update: (_, settings, previous) => previous ?? (TtsService(settings)..init()),
        ),

        ProxyProvider<ResourceLoader, DictionaryRepository>(
          update: (_, loader, _) => DictionaryRepository(loader),
        ),
        ProxyProvider<ResourceLoader, WordRepository>(
          update: (_, loader, _) => WordRepository(loader),
        ),
        ProxyProvider<ResourceLoader, WordMeaningRepository>(
          update: (_, loader, _) => WordMeaningRepository(loader),
        ),
        ProxyProvider<ResourceLoader, GrammarRepository>(
          update: (_, loader, _) => GrammarRepository(loader),
        ),
        ProxyProvider<ResourceLoader, KanaRepository>(
          update: (_, loader, _) => KanaRepository(loader),
        ),
        Provider<KanjiRepository>(create: (_) => KanjiRepository()),
        ProxyProvider<ResourceLoader, ExerciseRepository>(
          update: (_, loader, _) => ExerciseRepository(loader),
        ),
        ProxyProvider<ResourceLoader, LevelRepository>(
          update: (_, loader, _) => LevelRepository(loader),
        ),
        ProxyProvider<SettingsRepository, AudioService>(
          update: (_, settings, _) => AudioService(settings),
          dispose: (_, audio) => audio.dispose(),
        ),
        ProxyProvider4<KanaRepository, DictionaryRepository, WordRepository, ScoreRepository, LevelContentProvider>(
          update: (_, kana, dict, word, score, _) => LevelContentProvider(
            kanaRepo: kana,
            dictionaryRepo: dict,
            wordRepo: word,
            scoreRepo: score,
          ),
        ),
        ProxyProvider2<LevelContentProvider, ScoreRepository, StatisticsService>(
          update: (_, content, score, _) => StatisticsService(
            levelContentProvider: content,
            scoreRepo: score,
          ),
        ),


        // Global Providers (State)
        ChangeNotifierProvider(
          create: (context) => SettingsProvider(
            context.read<SettingsRepository>(),
            context.read<TtsService>(),
          ),
        ),
        ChangeNotifierProxyProvider3<LevelRepository, StatisticsService, SettingsRepository, SagaProvider>(
          create: (context) => SagaProvider(
            context.read<LevelRepository>(),
            context.read<StatisticsService>(),
            context.read<SettingsRepository>(),
          ),
          update: (context, levelRepo, stats, settingsRepo, saga) =>
              saga ?? SagaProvider(levelRepo, stats, settingsRepo),
        ),
        ChangeNotifierProxyProvider4<KanaRepository, ScoreRepository, SettingsRepository, AudioService, TaquinProvider>(
          create: (context) => TaquinProvider(
            context.read<KanaRepository>(),
            context.read<ScoreRepository>(),
            context.read<SettingsRepository>(),
            context.read<AudioService>(),
          ),
          update: (context, kana, score, settings, audio, previous) => previous ?? TaquinProvider(kana, score, settings, audio),
        ),
        ChangeNotifierProxyProvider5<LevelContentProvider, WordRepository, ScoreRepository, SettingsRepository, AudioService, SnakeProvider>(
          create: (context) => SnakeProvider(
            context.read<LevelContentProvider>(),
            context.read<WordRepository>(),
            context.read<ScoreRepository>(),
            context.read<SettingsRepository>(),
            context.read<AudioService>(),
          ),
          update: (context, content, word, score, settings, audio, previous) => previous ?? SnakeProvider(content, word, score, settings, audio),
        ),
        ChangeNotifierProxyProvider5<WordRepository, WordMeaningRepository, ScoreRepository, SettingsRepository, AudioService, CrosswordProvider>(
          create: (context) => CrosswordProvider(
            context.read<WordRepository>(),
            context.read<WordMeaningRepository>(),
            context.read<ScoreRepository>(),
            context.read<SettingsRepository>(),
            context.read<AudioService>(),
          ),
          update: (context, word, meaning, score, settings, audio, previous) => previous ?? CrosswordProvider(word, meaning, score, settings, audio),
        ),
        ChangeNotifierProxyProvider<ExerciseRepository, ParticleDefenderProvider>(
          create: (context) => ParticleDefenderProvider(
            context.read<ExerciseRepository>(),
          ),
          update: (context, exercise, previous) => previous ?? ParticleDefenderProvider(exercise),
        ),
        ChangeNotifierProvider(
          create: (context) => SimonProvider(
            context.read<KanaRepository>(),
            context.read<DictionaryRepository>(),
            context.read<SettingsRepository>(),
            context.read<LevelContentProvider>(),
            context.read<ScoreRepository>(),
            context.read<AudioService>(),
          ),
        ),
        ChangeNotifierProvider(
          create: (context) => MemorizeProvider(
            context.read<DictionaryRepository>(),
            context.read<KanaRepository>(),
            context.read<SettingsRepository>(),
            context.read<LevelContentProvider>(),
            context.read<ScoreRepository>(),
            context.read<AudioService>(),
          ),
        ),
        ChangeNotifierProvider(
          create: (context) => KanaLinkProvider(
            context.read<WordRepository>(),
            context.read<ScoreRepository>(),
            context.read<SettingsRepository>(),
            context.read<LevelContentProvider>(),
            context.read<AudioService>(),
          ),
        ),
        ChangeNotifierProvider(
          create: (context) => ShiritoriProvider(
            context.read<WordRepository>(),
            context.read<WordMeaningRepository>(),
            context.read<ScoreRepository>(),
            context.read<SettingsRepository>(),
            context.read<AudioService>(),
          ),
        ),

      ],
      child: const NihongoMochiApp(),
    ),
  );
}

final RouteObserver<PageRoute> routeObserver = RouteObserver<PageRoute>();

class NihongoMochiApp extends StatelessWidget {
  const NihongoMochiApp({super.key});

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();

    // Resolve locale from currentLocaleCode (e.g. "fr_FR" -> Locale('fr', 'FR') or Locale('fr'))
    final localeParts = settings.currentLocaleCode.split('_');
    final locale = localeParts.isNotEmpty
        ? Locale(localeParts[0], localeParts.length > 1 ? localeParts[1] : null)
        : const Locale('en');

    return MaterialApp(
      title: 'Nihongo Mochi',
      debugShowCheckedModeBanner: false,
      navigatorObservers: [routeObserver],
      theme: MochiTheme.lightTheme,
      darkTheme: MochiTheme.darkTheme,
      themeMode: settings.isDarkMode ? ThemeMode.dark : ThemeMode.light,
      locale: locale,
      localizationsDelegates: const [
        AppLocalizations.delegate,
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: AppLocalizations.supportedLocales,
      home: const HomeScreen(),
    );
  }

}
