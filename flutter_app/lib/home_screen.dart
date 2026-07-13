import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'repositories/level_repository.dart';
import 'repositories/settings_repository.dart';
import 'repositories/score_repository.dart';
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
    final settingsRepo = context.read<SettingsRepository>();
    
    final locale = settingsRepo.getAppLocale();

    await lpManager.downloadPack(locale);
    await levelRepo.loadLevels();

    final savedLevelId = settingsRepo.getSelectedLevel();

    setState(() {
      _isLoading = false;
    });

    if (savedLevelId.isNotEmpty) {
      final mode = settingsRepo.getMode();
      final levels = levelRepo.getLevelsForModeCached(mode);
      final idx = levels.indexWhere((l) => l.id == savedLevelId);
      if (idx != -1) {
        setState(() {
          _selectedIndex = idx;
        });
      }
    }
  }

  void _onLevelChanged(int index, List<LevelDefinition> levels) {
    setState(() => _selectedIndex = index);
    if (index >= 0 && index < levels.length) {
      context.read<SettingsRepository>().setSelectedLevel(levels[index].id);
    }
  }

  Future<Map<String, bool>> _getActivitiesStatus(LevelDefinition level) async {
    bool isRec = level.activities['RECOGNITION']?.enabled == true;
    bool isRead = level.activities['READING']?.enabled == true;
    bool isWrite = level.activities['WRITING']?.enabled == true;
    bool isGrammar = level.activities['GRAMMAR']?.enabled == true;

    if (level.id == 'user_custom_list') {
      final scoreRepo = context.read<ScoreRepository>();
      final recEmpty = (await scoreRepo.getListItems('Recognition_List')).isEmpty;
      final readEmpty = (await scoreRepo.getListItems('Reading_List')).isEmpty;
      final writeEmpty = (await scoreRepo.getListItems('Writing_List')).isEmpty;
      final grammarEmpty = (await scoreRepo.getAllScores(ScoreType.grammar)).isEmpty;

      isRec = isRec && !recEmpty;
      isRead = isRead && !readEmpty;
      isWrite = isWrite && !writeEmpty;
      isGrammar = isGrammar && !grammarEmpty;
    }

    return {
      'RECOGNITION': isRec,
      'READING': isRead,
      'WRITING': isWrite,
      'GRAMMAR': isGrammar,
    };
  }

  @override
  Widget build(BuildContext context) {
    if (_isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final settings = context.watch<SettingsProvider>();
    final mode = settings.currentMode;
    final levelRepo = context.read<LevelRepository>();
    final filteredLevels = levelRepo.getLevelsForModeCached(mode);
    
    int displayIndex = _selectedIndex.clamp(0, (filteredLevels.length - 1).clamp(0, 999));
    final currentLevel = filteredLevels.isNotEmpty ? filteredLevels[displayIndex] : null;

    return Scaffold(
      body: MochiBackground(
        child: SafeArea(
          child: FutureBuilder<Map<String, bool>>(
            future: currentLevel != null ? _getActivitiesStatus(currentLevel) : Future.value({}),
            builder: (context, snapshot) {
              final status = snapshot.data ?? {
                'RECOGNITION': false,
                'READING': false,
                'WRITING': false,
                'GRAMMAR': false,
              };

              final isRecEnabled = status['RECOGNITION'] ?? false;
              final isReadEnabled = status['READING'] ?? false;
              final isWriteEnabled = status['WRITING'] ?? false;
              final isGrammarEnabled = status['GRAMMAR'] ?? false;

              return SingleChildScrollView(
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
                        errorBuilder: (context, error, stackTrace) => Icon(
                          Icons.apps,
                          size: 80,
                          color: Theme.of(context).colorScheme.primary,
                        ),
                      ),
                    ),

                    // Sélecteur de niveau (Slider)
                    if (filteredLevels.isNotEmpty) _buildLevelSelector(filteredLevels, displayIndex),
                    const SizedBox(height: 16),

                    // Section Vocabulaire
                    _buildSectionCard(context, settings.getString("activity_type_vocabulary").toUpperCase(), [
                      _buildHomeBlockCard(context, settings.getString("results_recognition_title"), "見覚え", isRecEnabled, () {
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
                      _buildHomeBlockCard(context, settings.getString("results_reading_title"), "読み方", isReadEnabled, () {
                        if (currentLevel != null) {
                          Navigator.push(context, MaterialPageRoute(builder: (context) => WordListScreen(levelId: currentLevel.id, levelTitle: currentLevel.name)));
                        }
                      }),
                      _buildHomeBlockCard(context, settings.getString("results_writing_title"), "書き方", isWriteEnabled, () {
                        if (currentLevel != null) {
                          Navigator.push(context, MaterialPageRoute(builder: (context) => WritingRecapScreen(levelId: currentLevel.id, levelTitle: currentLevel.name)));
                        }
                      }),
                    ]),

                    const SizedBox(height: 12),

                    // Section Grammaire
                    _buildSectionCard(context, settings.getString("activity_type_grammar").toUpperCase(), [
                      _buildHomeBlockCard(context, settings.getString("section_fundamentals"), "基本", isGrammarEnabled, () => _navigateToGrammar("dependencies_basics", filteredLevels, displayIndex)),
                      _buildHomeBlockCard(context, settings.getString("activity_type_grammar_verbs"), "活用", isGrammarEnabled, () => _navigateToGrammar("conjugaison", filteredLevels, displayIndex)),
                      _buildHomeBlockCard(context, settings.getString("activity_type_grammar_syntax"), "文法", isGrammarEnabled, () => _navigateToGrammar("rules", filteredLevels, displayIndex)),
                    ]),

                    const SizedBox(height: 12),

                    // Outils
                    Row(
                      children: [
                        Expanded(
                          child: _buildSmallUtilityCard(context, settings.getString("menu_dictionary"), Icons.search, () {
                            Navigator.push(context, MaterialPageRoute(builder: (context) => const DictionaryScreen()));
                          }),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _buildSmallUtilityCard(context, settings.getString("games_title"), Icons.videogame_asset, () {
                            Navigator.push(context, MaterialPageRoute(builder: (context) => const GamesScreen()));
                          }),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),

                    Row(
                      children: [
                        Expanded(child: _buildSmallUtilityCard(context, settings.getString("results_title"), Icons.map, () {
                          Navigator.push(context, MaterialPageRoute(builder: (context) => const SagaMapScreen()));
                        })),
                        const SizedBox(width: 8),
                        Expanded(child: _buildSmallUtilityCard(context, settings.getString("settings_title"), Icons.settings, () {
                          Navigator.push(context, MaterialPageRoute(builder: (context) => const SettingsScreen()));
                        })),
                        const SizedBox(width: 8),
                        Expanded(child: _buildSmallUtilityCard(context, settings.getString("menu_about"), Icons.info, () {
                          Navigator.push(context, MaterialPageRoute(builder: (context) => const AboutScreen()));
                        })),
                      ],
                    ),
                  ],
                ),
              );
            },
          ),
        ),
      ),
    );
  }

  void _navigateToGrammar(String block, List<LevelDefinition> filteredLevels, int displayIndex) {
    if (filteredLevels.isEmpty) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => GrammarScreen(maxLevelId: filteredLevels[displayIndex].id),
      ),
    );
  }

  Widget _buildLevelSelector(List<LevelDefinition> levels, int index) {
    final level = levels[index];
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface.withOpacity(0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            Text(
              level.name.toUpperCase(),
              style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
            ),
            if (level.description.isNotEmpty) ...[
              const SizedBox(height: 2),
              Text(
                level.description,
                style: TextStyle(fontSize: 12, color: theme.colorScheme.onSurface.withOpacity(0.7)),
              ),
            ],
            const SizedBox(height: 4),
            Slider(
              value: index.toDouble(),
              min: 0,
              max: (levels.length - 1).toDouble().clamp(0, double.infinity),
              divisions: (levels.length > 1) ? levels.length - 1 : 1,
              activeColor: theme.colorScheme.primary,
              onChanged: (value) {
                _onLevelChanged(value.round(), levels);
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionCard(BuildContext context, String title, List<Widget> items) {
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      color: theme.colorScheme.surface.withOpacity(0.8),
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: TextStyle(fontSize: 14, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface.withOpacity(0.6))),
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

  Widget _buildHomeBlockCard(BuildContext context, String title, String kanji, bool enabled, VoidCallback onClick) {
    final theme = Theme.of(context);
    final alpha = enabled ? 1.0 : 0.4;
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: enabled ? 1 : 0,
      color: theme.colorScheme.surface.withOpacity(enabled ? 0.9 : 0.4),
      child: InkWell(
        onTap: enabled ? onClick : null,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          height: 95,
          padding: const EdgeInsets.all(4),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Text(
                kanji,
                style: TextStyle(
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                  color: theme.colorScheme.primary.withOpacity(alpha),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                title,
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 10,
                  fontWeight: FontWeight.w500,
                  color: theme.colorScheme.onSurface.withOpacity(alpha),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSmallUtilityCard(BuildContext context, String title, IconData icon, VoidCallback onClick) {
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      color: theme.colorScheme.surface.withOpacity(0.9),
      child: InkWell(
        onTap: onClick,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 12, horizontal: 4),
          child: Column(
            children: [
              Icon(icon, color: theme.colorScheme.primary, size: 24),
              const SizedBox(height: 4),
              Text(
                title,
                style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
