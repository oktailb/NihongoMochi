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
import 'dart:ui' as ui;

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
      final locale = ui.PlatformDispatcher.instance.locale.toString();
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

            // Bottom Navigation Tabs
            Positioned(
              bottom: 20,
              left: 16,
              right: 16,
              child: _buildSagaTabBar(context, provider),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSagaTabBar(BuildContext context, SagaProvider provider) {
    return Container(
      padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.9),
        borderRadius: BorderRadius.circular(30),
        boxShadow: [BoxShadow(color: Colors.black.withOpacity(0.1), blurRadius: 10)],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _TabButton(
            icon: Icons.star,
            label: "JLPT",
            isSelected: provider.currentTab == SagaTab.jlpt,
            onTap: () => provider.loadSaga(SagaTab.jlpt, ui.PlatformDispatcher.instance.locale.toString()),
          ),
          _TabButton(
            icon: Icons.school,
            label: "SCHOOL",
            isSelected: provider.currentTab == SagaTab.school,
            onTap: () => provider.loadSaga(SagaTab.school, ui.PlatformDispatcher.instance.locale.toString()),
          ),
          _TabButton(
            icon: Icons.emoji_events,
            label: "CHALLENGES",
            isSelected: provider.currentTab == SagaTab.challenges,
            onTap: () => provider.loadSaga(SagaTab.challenges, ui.PlatformDispatcher.instance.locale.toString()),
          ),
        ],
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
    final color = isSelected ? Colors.pink : Colors.grey;
    return GestureDetector(
      onTap: onTap,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, color: color),
          Text(label, style: TextStyle(color: color, fontSize: 10, fontWeight: isSelected ? FontWeight.bold : FontWeight.normal)),
        ],
      ),
    );
  }
}

class SagaMapContent extends StatelessWidget {
  const SagaMapContent({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<SagaProvider>();
    final steps = provider.steps;

    return LayoutBuilder(
      builder: (context, constraints) {
        final double width = constraints.maxWidth;
        final double centerX = width / 2;
        final double amplitude = (width / 2) - 80;
        const double nodeSpacing = 250.0;

        // Calculer tous les points du chemin pour le Painter
        final List<Offset> pathPoints = [];
        for (int i = 0; i < steps.length; i++) {
          final double phase = i * 0.8;
          final double x = centerX + sin(phase) * amplitude;
          final double y = i * nodeSpacing + (nodeSpacing / 2);
          pathPoints.add(Offset(x, y));
        }

        return SingleChildScrollView(
          padding: const EdgeInsets.only(bottom: 120, top: 40),
          child: SizedBox(
            height: steps.length * nodeSpacing,
            width: width,
            child: Stack(
              children: [
                // 1. Le chemin dessiné
                CustomPaint(
                  size: Size(width, steps.length * nodeSpacing),
                  painter: SagaPathPainter(points: pathPoints, color: Colors.brown.shade300),
                ),

                // 2. Les nœuds et les billboards
                ...steps.asMap().entries.map((entry) {
                  final int index = entry.key;
                  final SagaStep step = entry.value;
                  final double phase = index * 0.8;
                  final double nodeX = centerX + sin(phase) * amplitude;
                  final double nodeY = index * nodeSpacing + (nodeSpacing / 2);

                  // On suppose ici un seul nœud par étape pour simplifier le winding road initial
                  final node = step.nodes.first;
                  final progress = provider.nodeProgress[node.id] ?? UserSagaProgress();

                  return Stack(
                    children: [
                      // Nœud principal
                      Positioned(
                        left: nodeX - 50,
                        top: nodeY - 50,
                        child: SagaNodeItem(
                          node: node,
                          progress: progress,
                          onNodeClick: (id, type) {
                            Navigator.push(context, MaterialPageRoute(
                              builder: (context) => GameRecapScreen(levelId: id, levelTitle: node.title),
                            ));
                          },
                        ),
                      ),

                      // Billboards (Activités secondaires)
                      if (node.readingId != null && index < steps.length - 1)
                        Positioned(
                          left: nodeX + (sin(phase + 0.4) * 40) + (sin(phase) > 0 ? -120 : 40),
                          top: nodeY + 80,
                          child: BillboardItem(
                            type: StatisticsType.reading,
                            progress: progress.readingIndex,
                            isLeftSide: sin(phase) > 0,
                            onClick: () {
                              Navigator.push(context, MaterialPageRoute(
                                builder: (context) => GameRecapScreen(levelId: node.readingId!, levelTitle: "${node.title} - Lecture"),
                              ));
                            },
                          ),
                        ),
                    ],
                  );
                }),
              ],
            ),
          ),
        );
      },
    );
  }
}
