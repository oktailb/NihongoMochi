import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/big_mode_card.dart';
import 'simon_setup_screen.dart';
import 'memorize_setup_screen.dart';
import 'taquin_setup_screen.dart';
import 'kana_link_setup_screen.dart';
import 'crossword_setup_screen.dart';
import 'snake_setup_screen.dart';
import 'shiritori_setup_screen.dart';
import 'particle_defender_screen.dart';

class GamesScreen extends StatelessWidget {
  const GamesScreen({super.key});

  String _str(SettingsProvider settings, String key, String fallback) {
    final val = settings.getString(key);
    return (val.isNotEmpty && val != key) ? val : fallback;
  }

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();

    final screenTitle = _str(settings, "games_title", "Games");
    final sectionTitle = _str(settings, "section_challenges", "CHALLENGES").toUpperCase();

    return Scaffold(
      appBar: AppBar(
        title: Text(screenTitle),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  sectionTitle,
                  style: const TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: Colors.black54,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 16),
                BigModeCard(
                  title: _str(settings, "game_taquin_title", "Taquin"),
                  subtitle: _str(settings, "game_taquin_subtitle", "Put the characters in order"),
                  kanjiTitle: "パズル",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const TaquinSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_simon_title", "Simon"),
                  subtitle: _str(settings, "game_simon_subtitle", "Memorize and repeat the sequence"),
                  kanjiTitle: "記憶",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const SimonSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_memorize_title", "Memorize"),
                  subtitle: _str(settings, "game_memorize_subtitle", "Find pairs of cards"),
                  kanjiTitle: "神経衰弱",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const MemorizeSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_kana_link_title", "Kana Link"),
                  subtitle: _str(settings, "game_kana_link_subtitle", "Link Kanas to form words and clear blocks"),
                  kanjiTitle: "リンク",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const KanaLinkSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_crosswords_title", "Crosswords"),
                  subtitle: _str(settings, "game_crosswords_subtitle", "Thematic crosswords by JLPT level"),
                  kanjiTitle: "十字語",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const CrosswordSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_snake_title", "Snake"),
                  subtitle: _str(settings, "game_snake_subtitle", "Learn characters by eating them in order"),
                  kanjiTitle: "ヘビ",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const SnakeSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_shiritori_title", "Shiritori"),
                  subtitle: _str(settings, "game_shiritori_subtitle", "Japanese word chain game"),
                  kanjiTitle: "しりとり",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const ShiritoriSetupScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: _str(settings, "game_particles_title", "Particle Defender"),
                  subtitle: _str(settings, "game_particles_subtitle", "Find the right particle"),
                  kanjiTitle: "助詞",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const ParticleDefenderScreen(),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 24),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

