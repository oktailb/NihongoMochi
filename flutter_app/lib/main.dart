import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';

import 'db/database.dart';
import 'repositories/score_repository.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/word_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'repositories/grammar_repository.dart';
import 'repositories/kana_repository.dart';
import 'home_screen.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();

  final database = MochiDatabase();

  runApp(
    MultiProvider(
      providers: [
        // Base de données
        Provider<MochiDatabase>.value(value: database),

        // Repositories (Singletons)
        Provider<ScoreRepository>(create: (_) => ScoreRepository(database)),
        Provider<DictionaryRepository>(create: (_) => DictionaryRepository()),
        Provider<WordRepository>(create: (_) => WordRepository()),
        Provider<WordMeaningRepository>(create: (_) => WordMeaningRepository()),
        Provider<GrammarRepository>(create: (_) => GrammarRepository()),
        Provider<KanaRepository>(create: (_) => KanaRepository()),
      ],
      child: const NihongoMochiApp(),
    ),
  );
}

class NihongoMochiApp extends StatelessWidget {
  const NihongoMochiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Nihongo Mochi',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: const Color(0xFFE91E63),
          brightness: Brightness.light,
        ),
        textTheme: GoogleFonts.notoSansTextTheme(),
      ),
      home: const HomeScreen(),
    );
  }
}
