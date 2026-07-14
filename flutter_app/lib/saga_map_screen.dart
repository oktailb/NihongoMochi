import 'dart:math';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'models/saga.dart';
import 'providers/saga_provider.dart';
import 'widgets/mochi_background.dart';
import 'widgets/saga_node_item.dart';
import 'widgets/billboard_item.dart';
import 'widgets/saga_path_painter.dart';
import 'game_recap_screen.dart';
import 'providers/settings_provider.dart';

class SagaMapScreen extends StatefulWidget {
  const SagaMapScreen({super.key});

  @override
  State<SagaMapScreen> createState() => _SagaMapScreenState();
}

class _SagaMapScreenState extends State<SagaMapScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      final locale = context.read<SettingsProvider>().currentLocaleCode;
      context.read<SagaProvider>().loadSaga(SagaTab.jlpt, locale);
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SagaProvider>();

    return Scaffold(
      body: MochiBackground(
        child: Stack(
          children: [
            if (provider.isLoading)
              const Center(child: CircularProgressIndicator())
            else
              const SagaMapContent(),

            // Bottom controls overlay
            Positioned(
              bottom: 0,
              left: 0,
              right: 0,
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _buildCloudActionsBar(context, provider),
                  _buildSagaTabBar(context, provider),
                  const SizedBox(height: 12),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCloudActionsBar(BuildContext context, SagaProvider provider) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: isDark ? Colors.grey.shade900.withValues(alpha: 0.95) : Colors.white.withValues(alpha: 0.95),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5)),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 10, offset: const Offset(0, 4))
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          if (!provider.isAuthenticated)
            _ActionButton(
              icon: Icons.login,
              label: "Sign In",
              onTap: () => _showSignInDialog(context, provider),
            )
          else ...[
            _ActionButton(
              icon: Icons.emoji_events,
              label: "Trophies",
              onTap: () => _showAchievementsDialog(context, provider),
            ),
            _ActionButton(
              icon: Icons.leaderboard,
              label: "Rankings",
              onTap: () => _showRankingsDialog(context, provider),
            ),
            _ActionButton(
              icon: Icons.cloud_upload,
              label: "Backup",
              onTap: () => _showBackupDialog(context, provider),
            ),
            _ActionButton(
              icon: Icons.cloud_download,
              label: "Restore",
              onTap: () => _showRestoreDialog(context, provider),
            ),
            _ActionButton(
              icon: Icons.logout,
              label: "Log Out",
              onTap: () => provider.signOut(),
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSagaTabBar(BuildContext context, SagaProvider provider) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      decoration: BoxDecoration(
        color: isDark ? Colors.grey.shade900.withValues(alpha: 0.95) : Colors.white.withValues(alpha: 0.95),
        borderRadius: BorderRadius.circular(30),
        border: Border.all(color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5)),
        boxShadow: [
          BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 10, offset: const Offset(0, 2))
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _TabButton(
            icon: Icons.star,
            label: "JLPT",
            isSelected: provider.currentTab == SagaTab.jlpt,
            onTap: () => provider.loadSaga(SagaTab.jlpt, context.read<SettingsProvider>().currentLocaleCode),
          ),
          _TabButton(
            icon: Icons.school,
            label: "SCHOOL",
            isSelected: provider.currentTab == SagaTab.school,
            onTap: () => provider.loadSaga(SagaTab.school, context.read<SettingsProvider>().currentLocaleCode),
          ),
          _TabButton(
            icon: Icons.lock,
            label: "CHALLENGES",
            isSelected: provider.currentTab == SagaTab.challenges,
            onTap: () => provider.loadSaga(SagaTab.challenges, context.read<SettingsProvider>().currentLocaleCode),
          ),
        ],
      ),
    );
  }

  void _showSignInDialog(BuildContext context, SagaProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        String customName = "";
        String selectedAvatar = "assets/drawable/nihongomochi.webp";

        return StatefulBuilder(
          builder: (context, setState) {
            return AlertDialog(
              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(24)),
              title: const Text("Connexion Profil", style: TextStyle(fontWeight: FontWeight.bold)),
              content: SingleChildScrollView(
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Text(
                      "Suivez votre progression de n'importe où. Choisissez une connexion Chrome, Firefox ou créez un profil local.",
                      style: TextStyle(fontSize: 13, color: Colors.grey),
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.blue.shade600,
                        foregroundColor: Colors.white,
                        minimumSize: const Size(double.infinity, 44),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      icon: const Icon(Icons.chrome_reader_mode),
                      label: const Text("Se connecter avec Google (Chrome)"),
                      onPressed: () async {
                        Navigator.pop(context);
                        _showLoadingAndSignIn(context, provider, "Google");
                      },
                    ),
                    const SizedBox(height: 8),
                    ElevatedButton.icon(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Colors.orange.shade700,
                        foregroundColor: Colors.white,
                        minimumSize: const Size(double.infinity, 44),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      icon: const Icon(Icons.language),
                      label: const Text("Se connecter avec Firefox"),
                      onPressed: () async {
                        Navigator.pop(context);
                        _showLoadingAndSignIn(context, provider, "Firefox");
                      },
                    ),
                    const Divider(height: 32),
                    const Text("Ou profil étudiant local :", style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold)),
                    const SizedBox(height: 8),
                    TextField(
                      onChanged: (val) => customName = val,
                      decoration: InputDecoration(
                        hintText: "Votre pseudonyme",
                        contentPadding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                        border: OutlineInputBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                    ),
                    const SizedBox(height: 12),
                    const Text("Choisir une mascotte Mochi :", style: TextStyle(fontSize: 11, color: Colors.grey)),
                    const SizedBox(height: 8),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _buildMascotteSelect("assets/drawable/nihongomochi.webp", selectedAvatar, (val) {
                          setState(() => selectedAvatar = val);
                        }),
                        _buildMascotteSelect("assets/drawable/ebi.webp", selectedAvatar, (val) {
                          setState(() => selectedAvatar = val);
                        }),
                        _buildMascotteSelect("assets/drawable/toori.webp", selectedAvatar, (val) {
                          setState(() => selectedAvatar = val);
                        }),
                      ],
                    ),
                    const SizedBox(height: 16),
                    ElevatedButton(
                      style: ElevatedButton.styleFrom(
                        backgroundColor: Theme.of(context).colorScheme.primary,
                        foregroundColor: Theme.of(context).colorScheme.onPrimary,
                        minimumSize: const Size(double.infinity, 44),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                      ),
                      onPressed: () {
                        Navigator.pop(context);
                        provider.signIn("Local", customName: customName, customAvatar: selectedAvatar);
                      },
                      child: const Text("Créer le profil local"),
                    ),
                  ],
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildMascotteSelect(String asset, String selectedAsset, ValueChanged<String> onSelected) {
    final isSelected = asset == selectedAsset;
    return GestureDetector(
      onTap: () => onSelected(asset),
      child: Container(
        padding: const EdgeInsets.all(4),
        decoration: BoxDecoration(
          border: Border.all(color: isSelected ? Colors.pink : Colors.transparent, width: 2),
          borderRadius: BorderRadius.circular(12),
        ),
        child: Image.asset(asset, width: 44, height: 44, fit: BoxFit.contain),
      ),
    );
  }

  void _showLoadingAndSignIn(BuildContext context, SagaProvider provider, String type) {
    final navigator = Navigator.of(context);
    showDialog(
      context: context,
      barrierDismissible: false,
      builder: (dialogContext) {
        Future.delayed(const Duration(milliseconds: 1200), () {
          navigator.pop();
          provider.signIn(type);
        });
        return AlertDialog(
          content: Row(
            children: [
              const CircularProgressIndicator(),
              const SizedBox(width: 20),
              Text("Connexion au compte $type..."),
            ],
          ),
        );
      },
    );
  }

  void _showAchievementsDialog(BuildContext context, SagaProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Row(
            children: [
              Icon(Icons.emoji_events, color: Colors.orange),
              SizedBox(width: 10),
              Text("Trophées Débloqués", style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          content: SizedBox(
            width: double.maxFinite,
            child: ListView(
              shrinkWrap: true,
              children: [
                _buildAchievementRow("Premiers Pas", "Terminer le premier niveau du Hiragana", true),
                _buildAchievementRow("Guerrier des Kanjis", "Atteindre 50% au niveau N5", false),
                _buildAchievementRow("Écrivain Assidu", "Tracer 10 caractères dans l'outil d'écriture", true),
                _buildAchievementRow("Savant de la Grammaire", "Compléter votre première leçon de syntaxe", false),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Fermer"),
            )
          ],
        );
      },
    );
  }

  Widget _buildAchievementRow(String title, String desc, bool unlocked) {
    return ListTile(
      leading: Icon(
        unlocked ? Icons.check_circle : Icons.lock,
        color: unlocked ? Colors.green : Colors.grey,
      ),
      title: Text(title, style: TextStyle(fontWeight: unlocked ? FontWeight.bold : FontWeight.normal)),
      subtitle: Text(desc, style: const TextStyle(fontSize: 12)),
    );
  }

  void _showRankingsDialog(BuildContext context, SagaProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
          title: const Row(
            children: [
              Icon(Icons.leaderboard, color: Colors.blue),
              SizedBox(width: 10),
              Text("Classement Global", style: TextStyle(fontWeight: FontWeight.bold)),
            ],
          ),
          content: SizedBox(
            width: double.maxFinite,
            child: ListView(
              shrinkWrap: true,
              children: [
                _buildRankRow(1, "Tanaka-sensei", "100%", false),
                _buildRankRow(2, "Kenji", "85%", false),
                _buildRankRow(3, provider.displayName ?? "Vous", "45%", true),
                _buildRankRow(4, "Sakura", "32%", false),
                _buildRankRow(5, "Mochi Fan", "12%", false),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Fermer"),
            )
          ],
        );
      },
    );
  }

  Widget _buildRankRow(int rank, String name, String progress, bool isCurrentUser) {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 4),
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: isCurrentUser ? Colors.pink.shade50.withValues(alpha: 0.8) : Colors.transparent,
        borderRadius: BorderRadius.circular(12),
        border: isCurrentUser ? Border.all(color: Colors.pink.shade200) : null,
      ),
      child: Row(
        children: [
          Text("#$rank", style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(width: 16),
          Expanded(
            child: Text(
              name,
              style: TextStyle(fontWeight: isCurrentUser ? FontWeight.bold : FontWeight.normal),
            ),
          ),
          Text(progress, style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.grey)),
        ],
      ),
    );
  }

  void _showBackupDialog(BuildContext context, SagaProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text("Sauvegarde Cloud"),
          content: const Text("Voulez-vous sauvegarder votre progression actuelle dans le stockage local persistant simulé ?"),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Annuler"),
            ),
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("Sauvegarde effectuée avec succès !")),
                );
              },
              child: const Text("Sauvegarder"),
            ),
          ],
        );
      },
    );
  }

  void _showRestoreDialog(BuildContext context, SagaProvider provider) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          title: const Text("Restauration Cloud"),
          content: const Text("Voulez-vous restaurer la progression de votre compte ? Cela écrasera les données locales actuelles."),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text("Annuler"),
            ),
            ElevatedButton(
              onPressed: () {
                Navigator.pop(context);
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text("Restauration effectuée avec succès !")),
                );
              },
              child: const Text("Restaurer"),
            ),
          ],
        );
      },
    );
  }
}

class _ActionButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final VoidCallback onTap;

  const _ActionButton({required this.icon, required this.label, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, color: theme.colorScheme.primary),
            const SizedBox(height: 2),
            Text(label, style: TextStyle(color: theme.colorScheme.primary, fontSize: 9, fontWeight: FontWeight.w600)),
          ],
        ),
      ),
    );
  }
}

class _TabButton extends StatelessWidget {
  final IconData icon;
  final String label;
  final bool isSelected;
  final VoidCallback onTap;

  const _TabButton({required this.icon, required this.label, required this.isSelected, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final targetContainerColor = isSelected ? theme.colorScheme.primaryContainer : Colors.transparent;
    final targetContentColor = isSelected ? theme.colorScheme.onPrimaryContainer : theme.colorScheme.onSurfaceVariant;
    final fontWeight = isSelected ? FontWeight.bold : FontWeight.normal;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 250),
      decoration: BoxDecoration(
        color: targetContainerColor,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(12),
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(icon, color: targetContentColor),
                const SizedBox(height: 2),
                Text(
                  label,
                  style: TextStyle(
                    color: targetContentColor,
                    fontSize: 9,
                    fontWeight: fontWeight,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _BillboardSpec {
  final StatisticsType type;
  final int progress;
  final double t;
  final double horizontalOffset;

  _BillboardSpec(this.type, this.progress, {this.t = 0.0, this.horizontalOffset = 0.0});

  _BillboardSpec copyWith({double? t, double? horizontalOffset}) {
    return _BillboardSpec(
      type,
      progress,
      t: t ?? this.t,
      horizontalOffset: horizontalOffset ?? this.horizontalOffset,
    );
  }
}

class SagaMapContent extends StatelessWidget {
  const SagaMapContent({super.key});

  Offset _getBezierPoint(double t, Offset p0, Offset p1, Offset p2, Offset p3) {
    final double u = 1.0 - t;
    final double tt = t * t;
    final double uu = u * u;
    final double uuu = uu * u;
    final double ttt = tt * t;

    final double x = uuu * p0.dx + 3.0 * uu * t * p1.dx + 3.0 * u * tt * p2.dx + ttt * p3.dx;
    final double y = uuu * p0.dy + 3.0 * uu * t * p1.dy + 3.0 * u * tt * p2.dy + ttt * p3.dy;
    return Offset(x, y);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SagaProvider>();
    final settings = context.watch<SettingsProvider>();
    final steps = provider.steps;

    return LayoutBuilder(
      builder: (context, constraints) {
        final double width = constraints.maxWidth;
        final double centerX = width / 2;
        final double amplitude = (width / 2) - 80;
        const double nodeSpacing = 280.0;

        // 1. Calculer les coordonnées des nœuds pour chaque étape
        final List<List<double>> stepsNodePositionsX = [];
        for (int i = 0; i < steps.length; i++) {
          final step = steps[i];
          final double phase = i * 0.8;
          final double basePathX = centerX + sin(phase) * amplitude;

          if (step.nodes.length > 1) {
            const double spread = 160.0;
            final double leftX = (basePathX - (spread / 2)).clamp(50.0, width - 50.0);
            final double rightX = (basePathX + (spread / 2)).clamp(50.0, width - 50.0);
            stepsNodePositionsX.add([leftX, rightX]);
          } else {
            stepsNodePositionsX.add([basePathX]);
          }
        }

        // 2. Construire la liste des SagaRoads reliant les étapes
        final List<SagaRoad> roads = [];
        for (int i = 0; i < steps.length - 1; i++) {
          final currentPositions = stepsNodePositionsX[i];
          final nextPositions = stepsNodePositionsX[i + 1];
          final double startY = i * nodeSpacing + (nodeSpacing / 2);
          final double endY = (i + 1) * nodeSpacing + (nodeSpacing / 2);

          if (currentPositions.length == nextPositions.length) {
            for (int k = 0; k < currentPositions.length; k++) {
              roads.add(SagaRoad(
                start: Offset(currentPositions[k], startY),
                end: Offset(nextPositions[k], endY),
                spacing: nodeSpacing,
              ));
            }
          } else {
            for (final startX in currentPositions) {
              for (final endX in nextPositions) {
                roads.add(SagaRoad(
                  start: Offset(startX, startY),
                  end: Offset(endX, endY),
                  spacing: nodeSpacing,
                ));
              }
            }
          }
        }

        // 3. Déterminer où placer l'avatar du joueur (s'il est authentifié)
        int? avatarStepIndex;
        int? avatarNodeIndex;
        double? avatarT;
        Offset? avatarPos;

        if (provider.isAuthenticated && steps.isNotEmpty) {
          // Trouver le premier niveau non terminé à 100%
          bool foundActive = false;
          for (int i = 0; i < steps.length; i++) {
            final step = steps[i];
            for (int k = 0; k < step.nodes.length; k++) {
              final node = step.nodes[k];
              final progress = provider.nodeProgress[node.id] ?? UserSagaProgress();

              final scores = [
                if (node.recognitionId != null) progress.recognitionIndex,
                if (node.readingId != null) progress.readingIndex,
                if (node.grammarId != null) progress.grammarIndex,
              ];
              final double avgProgress = scores.isEmpty
                  ? 0.0
                  : scores.reduce((a, b) => a + b) / scores.length;

              if (avgProgress < 100.0) {
                avatarStepIndex = i;
                avatarNodeIndex = k;
                if (avgProgress > 0.0) {
                  avatarT = avgProgress / 100.0;
                }
                foundActive = true;
                break;
              }
            }
            if (foundActive) break;
          }

          // Si tout est terminé, positionner sur la dernière étape
          if (!foundActive) {
            avatarStepIndex = steps.length - 1;
            avatarNodeIndex = 0;
          }

          // Calculer la position spatiale réelle
          if (avatarStepIndex != null && avatarNodeIndex != null) {
            final double startY = avatarStepIndex * nodeSpacing + (nodeSpacing / 2);
            final double startX = stepsNodePositionsX[avatarStepIndex][avatarNodeIndex];

            if (avatarT != null && avatarStepIndex < steps.length - 1) {
              // Placé sur la courbe reliant cette étape à la suivante
              final nextPositions = stepsNodePositionsX[avatarStepIndex + 1];
              final double targetX = (stepsNodePositionsX[avatarStepIndex].length == nextPositions.length)
                  ? nextPositions[avatarNodeIndex]
                  : nextPositions.reduce((a, b) => a + b) / nextPositions.length; // Moyenne

              final double endY = (avatarStepIndex + 1) * nodeSpacing + (nodeSpacing / 2);

              final p0 = Offset(startX, startY);
              final p3 = Offset(targetX, endY);
              final p1 = Offset(p0.dx, p0.dy + nodeSpacing * 0.5);
              final p2 = Offset(p3.dx, p3.dy - nodeSpacing * 0.5);

              avatarPos = _getBezierPoint(avatarT.clamp(0.05, 0.95), p0, p1, p2, p3);
            } else {
              // Directement sur le nœud
              avatarPos = Offset(startX, startY);
            }
          }
        }

        return SingleChildScrollView(
          padding: const EdgeInsets.only(bottom: 220, top: 40),
          child: SizedBox(
            height: steps.length * nodeSpacing,
            width: width,
            child: Stack(
              children: [
                // 1. Dessiner les routes courbées
                CustomPaint(
                  size: Size(width, steps.length * nodeSpacing),
                  painter: SagaPathPainter(roads: roads, color: Colors.brown.shade300),
                ),

                // 2. Parcourir et dessiner les panneaux publicitaires d'activités (billboards)
                ...steps.asMap().entries.expand((entry) {
                  final int index = entry.key;
                  final SagaStep step = entry.value;
                  final double startY = index * nodeSpacing + (nodeSpacing / 2);

                  final widgetsList = <Widget>[];

                  // Si ce n'est pas la dernière étape, on génère les panneaux sur la route vers l'étape suivante
                  if (index < steps.length - 1) {
                    final nextPositions = stepsNodePositionsX[index + 1];
                    final double endY = (index + 1) * nodeSpacing + (nodeSpacing / 2);

                    for (int k = 0; k < step.nodes.length; k++) {
                      final node = step.nodes[k];
                      final progress = provider.nodeProgress[node.id] ?? UserSagaProgress();
                      final double startX = stepsNodePositionsX[index][k];
                      final double targetX = (stepsNodePositionsX[index].length == nextPositions.length)
                          ? nextPositions[k]
                          : nextPositions.reduce((a, b) => a + b) / nextPositions.length;

                      final p0 = Offset(startX, startY);
                      final p3 = Offset(targetX, endY);
                      final p1 = Offset(p0.dx, p0.dy + nodeSpacing * 0.5);
                      final p2 = Offset(p3.dx, p3.dy - nodeSpacing * 0.5);

                      // Récupérer les activités éligibles
                      final billboards = <_BillboardSpec>[];
                      if (node.recognitionId != null) {
                        billboards.add(_BillboardSpec(StatisticsType.recognition, progress.recognitionIndex));
                      }
                      if (node.readingId != null) {
                        billboards.add(_BillboardSpec(StatisticsType.reading, progress.readingIndex));
                      }
                      if (node.writingId != null) {
                        billboards.add(_BillboardSpec(StatisticsType.writing, progress.writingIndex));
                      }
                      if (node.grammarId != null) {
                        billboards.add(_BillboardSpec(StatisticsType.grammar, progress.grammarIndex));
                      }

                      // Positionner le long de la courbe de Bézier et trier
                      final placedBillboards = billboards
                          .map((spec) => spec.copyWith(t: 0.2 + (spec.progress / 100) * 0.6))
                          .toList();
                      placedBillboards.sort((a, b) => a.t.compareTo(b.t));

                      // Éviter les chevauchements en alternant gauche/droite
                      final finalBillboards = <_BillboardSpec>[];
                      int idx = 0;
                      while (idx < placedBillboards.length) {
                        final current = placedBillboards[idx];
                        final cluster = [current];
                        int nextIdx = idx + 1;
                        while (nextIdx < placedBillboards.length && (placedBillboards[nextIdx].t - current.t) < 0.1) {
                          cluster.add(placedBillboards[nextIdx]);
                          nextIdx++;
                        }
                        for (int clusterIdx = 0; clusterIdx < cluster.length; clusterIdx++) {
                          final offset = (clusterIdx % 2 == 0) ? -75.0 : 20.0;
                          finalBillboards.add(cluster[clusterIdx].copyWith(horizontalOffset: offset));
                        }
                        idx = nextIdx;
                      }

                      // Construire les composants graphiques BillboardItem positionnés
                      for (final spec in finalBillboards) {
                        final pos = _getBezierPoint(spec.t, p0, p1, p2, p3);
                        final isLeftSide = spec.horizontalOffset < 0;

                        widgetsList.add(
                          Positioned(
                            left: pos.dx + spec.horizontalOffset - 35,
                            top: pos.dy - 35,
                            child: BillboardItem(
                              type: spec.type,
                              progress: spec.progress,
                              isLeftSide: isLeftSide,
                              onClick: () {
                                final String? gameId = spec.type == StatisticsType.recognition
                                    ? node.recognitionId
                                    : spec.type == StatisticsType.reading
                                        ? node.readingId
                                        : spec.type == StatisticsType.writing
                                            ? node.writingId
                                            : node.grammarId;
                                if (gameId != null) {
                                  Navigator.push(context, MaterialPageRoute(
                                    builder: (context) => GameRecapScreen(
                                      levelId: gameId,
                                      levelTitle: "${settings.getString(node.title)} - ${spec.type.name.toUpperCase()}",
                                    ),
                                  ));
                                }
                              },
                            ),
                          ),
                        );
                      }
                    }
                  }

                  return widgetsList;
                }),

                // 3. Dessiner les nœuds d'étapes principaux (SagaNodeItem)
                ...steps.asMap().entries.expand((entry) {
                  final int index = entry.key;
                  final SagaStep step = entry.value;
                  final double startY = index * nodeSpacing + (nodeSpacing / 2);

                  return step.nodes.asMap().entries.map((nodeEntry) {
                    final int nodeIdx = nodeEntry.key;
                    final SagaNode node = nodeEntry.value;
                    final double nodeX = stepsNodePositionsX[index][nodeIdx];
                    final progress = provider.nodeProgress[node.id] ?? UserSagaProgress();

                    return Positioned(
                      left: nodeX - 55,
                      top: startY - 55,
                      child: SagaNodeItem(
                        node: node,
                        progress: progress,
                        onNodeClick: (id, type) {
                          Navigator.push(context, MaterialPageRoute(
                            builder: (context) => GameRecapScreen(levelId: id, levelTitle: settings.getString(node.title)),
                          ));
                        },
                      ),
                    );
                  });
                }),

                // 4. Dessiner l'avatar mobile du joueur (s'il est connecté)
                if (avatarPos != null && provider.avatarAsset != null)
                  Positioned(
                    left: avatarPos.dx - 30,
                    top: avatarPos.dy - 55,
                    child: _PlayerAvatarWidget(
                      avatarAsset: provider.avatarAsset!,
                      displayName: provider.displayName ?? "Vous",
                    ),
                  ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _PlayerAvatarWidget extends StatelessWidget {
  final String avatarAsset;
  final String displayName;

  const _PlayerAvatarWidget({required this.avatarAsset, required this.displayName});

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        Container(
          width: 50,
          height: 50,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: Colors.white,
            border: Border.all(color: Colors.white, width: 3),
            boxShadow: const [
              BoxShadow(color: Colors.black26, blurRadius: 8, offset: Offset(0, 4))
            ],
          ),
          child: ClipOval(
            child: Image.asset(avatarAsset, fit: BoxFit.contain),
          ),
        ),
        const SizedBox(height: 4),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.secondaryContainer,
            borderRadius: BorderRadius.circular(8),
            boxShadow: const [
              BoxShadow(color: Colors.black12, blurRadius: 2)
            ],
          ),
          child: Text(
            displayName,
            style: const TextStyle(fontSize: 9, fontWeight: FontWeight.bold),
          ),
        ),
      ],
    );
  }
}
