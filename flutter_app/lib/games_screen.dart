import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/big_mode_card.dart';
import 'simon_setup_screen.dart';
import 'providers/simon_provider.dart';
import 'memorize_setup_screen.dart';
import 'providers/memorize_provider.dart';
import 'taquin_setup_screen.dart';
import 'providers/taquin_provider.dart';
import 'kana_link_setup_screen.dart';
import 'providers/kana_link_provider.dart';
import 'crossword_setup_screen.dart';
import 'providers/crossword_provider.dart';
import 'snake_setup_screen.dart';
import 'providers/snake_provider.dart';
import 'shiritori_setup_screen.dart';
import 'providers/shiritori_provider.dart';
import 'particle_defender_screen.dart';
import 'providers/particle_defender_provider.dart';

class GamesScreen extends StatelessWidget {
  const GamesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("Jeux"),
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
                const Text(
                  "DÉFIS LUDIQUES",
                  style: TextStyle(
                    fontSize: 12,
                    fontWeight: FontWeight.bold,
                    color: Colors.black54,
                    letterSpacing: 1.2,
                  ),
                ),
                const SizedBox(height: 16),
                BigModeCard(
                  title: "Taquin",
                  subtitle: "Reconstituez l'image ou l'ordre des kanas",
                  kanjiTitle: "パズル",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => TaquinProvider(
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                          ),
                          child: const TaquinSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Simon",
                  subtitle: "Mémorisez et répétez la séquence",
                  kanjiTitle: "記憶",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => SimonProvider(
                            context.read(), context.read(), context.read(),
                            context.read(), context.read(), context.read(),
                            context.read(),
                          ),
                          child: const SimonSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Memorize",
                  subtitle: "Trouvez les paires correspondantes",
                  kanjiTitle: "神経衰弱",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => MemorizeProvider(
                            context.read(), context.read(), context.read(),
                            context.read(), context.read(), context.read(),
                          ),
                          child: const MemorizeSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Kana Link",
                  subtitle: "Reliez les kanas pour vider la grille",
                  kanjiTitle: "リンク",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => KanaLinkProvider(
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                          ),
                          child: const KanaLinkSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Mots Croisés",
                  subtitle: "Complétez la grille de vocabulaire",
                  kanjiTitle: "十字語",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => CrosswordProvider(
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                          ),
                          child: const CrosswordSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Snake",
                  subtitle: "Mangez les bons kanas dans l'ordre",
                  kanjiTitle: "ヘビ",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => SnakeProvider(
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                          ),
                          child: const SnakeSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Shiritori",
                  subtitle: "Le jeu du dernier mot",
                  kanjiTitle: "しりとり",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => ShiritoriProvider(
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                            context.read(),
                          ),
                          child: const ShiritoriSetupScreen(),
                        ),
                      ),
                    );
                  },
                ),
                const SizedBox(height: 12),
                BigModeCard(
                  title: "Particules",
                  subtitle: "Défendez votre base avec les bonnes particules",
                  kanjiTitle: "助詞",
                  onClick: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => ChangeNotifierProvider(
                          create: (context) => ParticleDefenderProvider(context.read()),
                          child: const ParticleDefenderScreen(),
                        ),
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
