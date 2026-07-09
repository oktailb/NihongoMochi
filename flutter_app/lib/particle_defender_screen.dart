import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/particle_defender.dart';
import 'providers/particle_defender_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/game_hud.dart';

class ParticleDefenderScreen extends StatelessWidget {
  const ParticleDefenderScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ParticleDefenderProvider>();
    final state = provider.uiState;

    return Scaffold(
      appBar: AppBar(
        title: const Text("Particle Defender"),
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back),
          onPressed: () => _showExitDialog(context, provider),
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: state.isLoading
              ? const Center(child: CircularProgressIndicator())
              : GestureDetector(
                  onPanUpdate: (details) {
                    final x = details.localPosition.dx / MediaQuery.of(context).size.width;
                    provider.onShipMove(x);
                  },
                  onTap: () => provider.onShoot(),
                  child: Column(
                    children: [
                      GameHUD(
                        primaryLabel: "PARTICULES",
                        primaryValue: "",
                        secondaryLabel: "SCORE",
                        secondaryValue: state.score.toString(),
                        timeSeconds: 0, // Pas de timer ici
                      ),
                      _buildLivesIndicator(state.lives),
                      _buildSentenceCard(state),
                      Expanded(
                        child: Stack(
                          children: [
                            // Zone de jeu vide pour capter les touches
                            Container(color: Colors.transparent),

                            // Ennemis (Particules qui tombent)
                            ...state.activeParticles.map((particle) => _buildParticleEnemy(context, particle)),

                            // Vaisseau du joueur (Bas)
                            _buildPlayerShip(context, state.shipX),

                            if (state.isGameOver)
                              _buildGameOverOverlay(context, provider),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
        ),
      ),
    );
  }

  Widget _buildLivesIndicator(int lives) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: List.generate(3, (index) => Icon(
          index < lives ? Icons.favorite : Icons.favorite_border,
          color: Colors.red,
        )),
      ),
    );
  }

  Widget _buildSentenceCard(ParticleGameState state) {
    return Card(
      margin: const EdgeInsets.all(16),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(20.0),
        child: Text(
          "${state.currentSentencePrefix}「 ? 」${state.currentSentenceSuffix}",
          textAlign: TextAlign.center,
          style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
        ),
      ),
    );
  }

  Widget _buildParticleEnemy(BuildContext context, ParticleEnemy enemy) {
    final width = MediaQuery.of(context).size.width;
    final height = MediaQuery.of(context).size.height * 0.6; // Zone de jeu estimée

    return Positioned(
      left: enemy.position.dx * width - 28,
      top: enemy.position.dy * height,
      child: Container(
        width: 56,
        height: 56,
        decoration: BoxDecoration(
          shape: BoxShape.circle,
          gradient: RadialGradient(
            colors: [Colors.blue.shade300, Colors.blue.shade700],
          ),
          boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.2), blurRadius: 4)],
        ),
        alignment: Alignment.center,
        child: Text(
          enemy.char,
          style: const TextStyle(color: Colors.white, fontSize: 24, fontWeight: FontWeight.w900),
        ),
      ),
    );
  }

  Widget _buildPlayerShip(BuildContext context, double shipX) {
    final width = MediaQuery.of(context).size.width;

    return Positioned(
      bottom: 20,
      left: shipX * width - 32,
      child: Container(
        width: 64,
        height: 64,
        decoration: BoxDecoration(
          color: Colors.blueGrey.withOpacity(0.9),
          shape: BoxShape.circle,
        ),
        alignment: Alignment.center,
        child: const Text("🚀", style: TextStyle(fontSize: 32)),
      ),
    );
  }

  Widget _buildGameOverOverlay(BuildContext context, ParticleDefenderProvider provider) {
    return Container(
      color: Colors.black54,
      child: Center(
        child: Card(
          margin: const EdgeInsets.all(32),
          child: Padding(
            padding: const EdgeInsets.all(32.0),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.flash_off, size: 80, color: Colors.orange),
                const SizedBox(height: 16),
                const Text("Défense échouée", style: TextStyle(fontSize: 28, fontWeight: FontWeight.bold)),
                const SizedBox(height: 24),
                Text("Score : ${provider.uiState.score}"),
                const SizedBox(height: 32),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                  children: [
                    TextButton(onPressed: () => Navigator.pop(context), child: const Text("MENU")),
                    ElevatedButton(
                      onPressed: () => provider.startGame(),
                      child: const Text("RÉESSAYER"),
                    ),
                  ],
                )
              ],
            ),
          ),
        ),
      ),
    );
  }

  void _showExitDialog(BuildContext context, ParticleDefenderProvider provider) {
    provider.pauseGame();
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Abandonner ?"),
        content: const Text("Voulez-vous vraiment quitter la défense ?"),
        actions: [
          TextButton(onPressed: () { provider.resumeGame(); Navigator.pop(context); }, child: const Text("NON")),
          TextButton(onPressed: () { Navigator.pop(context); Navigator.pop(context); }, child: const Text("OUI")),
        ],
      ),
    );
  }
}
