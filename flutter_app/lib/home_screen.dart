import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'repositories/level_repository.dart';
import 'repositories/settings_repository.dart';
import 'services/string_provider.dart';
import 'services/language_pack_manager.dart';
import 'models/level.dart';
import 'dictionary_screen.dart';
import 'grammar_screen.dart';
import 'kana_screen.dart';
import 'kana_recap_screen.dart';
import 'game_recap_screen.dart';
import 'word_list_screen.dart';
import 'writing_recap_screen.dart';
import 'saga_map_screen.dart';
import 'settings_screen.dart';
import 'about_screen.dart';
import 'games_screen.dart';
import 'models/kana.dart';
import 'widgets/mochi_background.dart';
import 'providers/settings_provider.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  List<LevelDefinition> _levels = [];
  int _selectedIndex = 0;
  bool _isLoading = true;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _initData();
    });
  }

  Future<void> _initData() async {
    final lpManager = context.read<LanguagePackManager>();
    final levelRepo = context.read<LevelRepository>();
    final settings = context.read<SettingsRepository>();
    
    final locale = settings.getAppLocale(); // ex: "fr_FR"

    // 1. Déclenche le téléchargement/mise à jour MD5 en arrière-plan
    // On ne bloque pas l'UI, mais on attend que ce soit prêt si possible
    await lpManager.downloadPack(locale);

    // 2. Initialise les niveaux
    final levels = await levelRepo.getFlattenedLevels();

    setState(() {
      _levels = levels;
      _isLoading = false;
    });
  }

  List<LevelDefinition> _getFilteredLevels(String mode) {
    if (mode == "JLPT") {
      return _levels.where((l) => l.id.toLowerCase().startsWith('n')).toList();
    } else if (mode == "School") {
      return _levels.where((l) => l.id.toLowerCase().startsWith('grade')).toList();
    } else if (mode == "Challenge") {
      return _levels.where((l) => l.id.toLowerCase().contains('challenge')).toList();
    }
    return _levels;
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final settings = context.watch<SettingsProvider>();
    final mode = settings.currentMode;
    final filteredLevels = _getFilteredLevels(mode);
    
    // On s'assure que l'index est valide pour la liste filtrée
    int displayIndex = _selectedIndex.clamp(0, (filteredLevels.length - 1).clamp(0, 999));
    final currentLevel = filteredLevels.isNotEmpty ? filteredLevels[displayIndex] : null;

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
                    'assets/drawable/nihongomochi.webp',
                    fit: BoxFit.contain,
                    errorBuilder: (context, error, stackTrace) => const Icon(Icons.apps, size: 80, color: Colors.pink)
                  ),
                ),

                // Sélecteur de niveau (Slider)
                if (filteredLevels.isNotEmpty) _buildLevelSelector(filteredLevels, displayIndex),
                const SizedBox(height: 16),

                // Section Vocabulaire
                _buildSectionCard(settings.getString("activity_type_vocabulary").toUpperCase(), [
                  _buildHomeBlockCard(settings.getString("results_recognition_title"), "見覚え", () {
                    if (currentLevel != null) {
                      if (currentLevel.id == "hiragana") {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const KanaRecapScreen(type: KanaType.hiragana)));
                      } else if (currentLevel.id == "katakana") {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const KanaRecapScreen(type: KanaType.katakana)));
                      } else {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => GameRecapScreen(levelId: currentLevel.id, levelTitle: currentLevel.name)));
                      }
                    }
                  }),
                  _buildHomeBlockCard(settings.getString("results_reading_title"), "読み方", () {
                    if (currentLevel != null) {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => WordListScreen(levelId: currentLevel.id, levelTitle: currentLevel.name)));
                    }
                  }),
                  _buildHomeBlockCard(settings.getString("results_writing_title"), "書き方", () {
                    if (currentLevel != null) {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => WritingRecapScreen(levelId: currentLevel.id, levelTitle: currentLevel.name)));
                    }
                  }),
                ]),

                const SizedBox(height: 12),

                // Section Grammaire
                _buildSectionCard(settings.getString("activity_type_grammar").toUpperCase(), [
                  _buildHomeBlockCard(settings.getString("section_fundamentals"), "基本", () => _navigateToGrammar("dependencies_basics")),
                  _buildHomeBlockCard(settings.getString("activity_type_grammar_verbs"), "活用", () => _navigateToGrammar("conjugaison")),
                  _buildHomeBlockCard(settings.getString("activity_type_grammar_syntax"), "文法", () => _navigateToGrammar("rules")),
                ]),

                const SizedBox(height: 12),

                // Outils
                Row(
                  children: [
                    Expanded(
                      child: _buildSmallUtilityCard(settings.getString("menu_dictionary"), Icons.search, () {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const DictionaryScreen()));
                      }),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: _buildSmallUtilityCard(settings.getString("games_title"), Icons.videogame_asset, () {
                        Navigator.push(context, MaterialPageRoute(builder: (context) => const GamesScreen()));
                      }),
                    ),
                  ],
                ),
                const SizedBox(height: 12),

                Row(
                  children: [
                    Expanded(child: _buildSmallUtilityCard(settings.getString("results_title"), Icons.map, () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const SagaMapScreen()));
                    })),
                    const SizedBox(width: 8),
                    Expanded(child: _buildSmallUtilityCard(settings.getString("settings_title"), Icons.settings, () {
                      Navigator.push(context, MaterialPageRoute(builder: (context) => const SettingsScreen()));
                    })),
                    const SizedBox(width: 8),
                    Expanded(child: _buildSmallUtilityCard(settings.getString("menu_about"), Icons.info, () {
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
    final mode = context.read<SettingsProvider>().currentMode;
    final filteredLevels = _getFilteredLevels(mode);
    if (filteredLevels.isEmpty) return;
    
    int displayIndex = _selectedIndex.clamp(0, (filteredLevels.length - 1).clamp(0, 999));
    
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => GrammarScreen(maxLevelId: filteredLevels[displayIndex].id),
      ),
    );
  }

  Widget _buildLevelSelector(List<LevelDefinition> levels, int index) {
    final level = levels[index];
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
              style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.pink),
            ),
            if (level.description.isNotEmpty) ...[
              const SizedBox(height: 2),
              Text(
                level.description,
                style: const TextStyle(fontSize: 12, color: Colors.black54),
              ),
            ],
            const SizedBox(height: 4),
            Slider(
              value: index.toDouble(),
              min: 0,
              max: (levels.length - 1).toDouble().clamp(0, double.infinity),
              divisions: (levels.length > 1) ? levels.length - 1 : 1,
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
              Text(kanji, style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: Color(0xFF33A3A3))),
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
              Icon(icon, color: const Color(0xFF33A3A3), size: 24),
              const SizedBox(height: 4),
              Text(title, style: const TextStyle(fontSize: 12, fontWeight: FontWeight.bold), textAlign: TextAlign.center),
            ],
          ),
        ),
      ),
    );
  }
}
