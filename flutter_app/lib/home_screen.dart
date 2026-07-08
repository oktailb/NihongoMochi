import 'package:flutter/material.dart';
import 'repositories/level_repository.dart';
import 'services/string_provider.dart';
import 'models/level.dart';
import 'dictionary_screen.dart';
import 'grammar_screen.dart';
import 'kana_screen.dart';
import 'kana_quiz_screen.dart';
import 'word_quiz_screen.dart';
import 'writing_quiz_screen.dart';
import 'saga_map_screen.dart';
import 'settings_screen.dart';
import 'about_screen.dart';
import 'models/kana.dart';
import 'widgets/mochi_background.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final LevelRepository _levelRepo = LevelRepository();
  final StringProvider _stringProvider = StringProvider();

  List<LevelDefinition> _levels = [];
  int _selectedIndex = 0;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    _initData();
  }

  Future<void> _initData() async {
    await _stringProvider.loadStrings('fr-rFR');
    final levels = await _levelRepo.getFlattenedLevels();
    setState(() {
      _levels = levels;
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final currentLevel = _levels.isNotEmpty ? _levels[_selectedIndex] : null;

    return Scaffold(
      body: MochiBackground(
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                // Logo
                Container(
                  height: 120,
                  margin: const EdgeInsets.only(bottom: 16),
                  child: Image.asset(
                    'assets/drawable/nihongomochi.png',
                    fit: BoxFit.contain,
                    errorBuilder: (context, error, stackTrace) => const Icon(Icons.apps, size: 80, color: Colors.pink)
                  ),
                ),

                // Sélecteur de niveau (Slider)
                if (currentLevel != null) _buildLevelSelector(currentLevel),
                const SizedBox(height: 16),

                // Section Vocabulaire
                _buildSectionCard("VOCABULAIRE", [
                  _buildHomeBlockCard("Reconnaissance", "認識", () {
                    Navigator.push(context, MaterialPageRoute(builder: (context) => const KanaQuizScreen(type: KanaType.hiragana)));
                  }),
                  _buildHomeBlockCard("Lecture", "読解", () {
                    if (currentLevel != null) {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => WordQuizScreen(levelId: currentLevel.id)));
                    }
                  }),
                  _buildHomeBlockCard("Écriture", "書取", () {
                    if (currentLevel != null) {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => WritingQuizScreen(levelId: currentLevel.id)));
                    }
                  }),
                ]),

                const SizedBox(height: 12),

                // Section Grammaire
                _buildSectionCard("GRAMMAIRE", [
                  _buildHomeBlockCard("Bases", "基本", () => _navigateToGrammar("dependencies_basics")),
                  _buildHomeBlockCard("Verbes", "活用", () => _navigateToGrammar("conjugaison")),
                  _buildHomeBlockCard("Syntaxe", "文法", () => _navigateToGrammar("rules")),
                ]),

                const SizedBox(height: 12),

                // Outils
                Row(
                  children: [
                    Expanded(
                      child: _buildSmallUtilityCard("Dictionnaire", Icons.search, () {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const DictionaryScreen()));
                      }),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _buildSmallUtilityCard("Kanas", Icons.grid_on, () {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const KanaScreen(type: KanaType.hiragana, title: "Hiragana")));
                      }),
                    ),
                  ],
                ),
                const SizedBox(height: 12),

                Row(
                  children: [
                    Expanded(child: _buildSmallUtilityCard("Résultats", Icons.map, () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const SagaMapScreen()));
                    })),
                    const SizedBox(width: 8),
                    Expanded(child: _buildSmallUtilityCard("Paramètres", Icons.settings, () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const SettingsScreen()));
                    })),
                    const SizedBox(width: 8),
                    Expanded(child: _buildSmallUtilityCard("À propos", Icons.info, () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const AboutScreen()));
                    })),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _navigateToGrammar(String block) {
    if (_levels.isEmpty) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => GrammarScreen(maxLevelId: _levels[_selectedIndex].id),
      ),
    );
  }

  Widget _buildLevelSelector(LevelDefinition level) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: Colors.white.withOpacity(0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(
              level.name.toUpperCase(),
              style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.pink),
            ),
            const SizedBox(height: 4),
            Slider(
              value: _selectedIndex.toDouble(),
              min: 0,
              max: (_levels.length - 1).toDouble().clamp(0, double.infinity),
              divisions: (_levels.length > 1) ? _levels.length - 1 : 1,
              activeColor: Colors.pink,
              onChanged: (value) {
                setState(() => _selectedIndex = value.round());
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionCard(String title, List<Widget> items) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      color: Colors.white.withOpacity(0.8),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: Colors.black54)),
            const SizedBox(height: 12),
            Row(
              children: items.map((item) => Expanded(
                child: Padding(padding: const EdgeInsets.symmetric(horizontal: 4), child: item)
              )).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHomeBlockCard(String title, String kanji, VoidCallback onClick) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 1,
      child: InkWell(
        onTap: onClick,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          height: 90,
          padding: const EdgeInsets.all(4),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(kanji, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Colors.pink)),
              Text(title, textAlign: TextAlign.center, style: const TextStyle(fontSize: 10, fontWeight: FontWeight.w500)),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSmallUtilityCard(String title, IconData icon, VoidCallback onClick) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: Colors.white.withOpacity(0.9),
      child: InkWell(
        onTap: onClick,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 4),
          child: Column(
            children: [
              Icon(icon, color: Colors.pink, size: 24),
              const SizedBox(height: 4),
              Text(title, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
            ],
          ),
        ),
      ),
    );
  }
}
