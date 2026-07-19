import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'dart:math';
import 'models/dictionary.dart';
import 'providers/kanji_detail_provider.dart';
import 'repositories/dictionary_repository.dart';
import 'repositories/score_repository.dart';
import 'repositories/word_repository.dart';
import 'repositories/word_meaning_repository.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';
import 'word_detail_screen.dart';

class KanjiDetailScreen extends StatelessWidget {
  final String kanjiId;

  const KanjiDetailScreen({super.key, required this.kanjiId});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = KanjiDetailProvider(
          context.read<DictionaryRepository>(),
          context.read<ScoreRepository>(),
          context.read<WordRepository>(),
          context.read<WordMeaningRepository>(),
        );
        final locale = context.read<SettingsProvider>().currentLocaleCode;
        provider.loadKanji(kanjiId, locale);
        return provider;
      },
      child: const KanjiDetailView(),
    );
  }
}

class KanjiDetailView extends StatelessWidget {
  const KanjiDetailView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<KanjiDetailProvider>();
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    final showGraph = provider.componentTree != null && provider.componentTree!.children.isNotEmpty;

    return Scaffold(
      backgroundColor: Colors.transparent,
      appBar: AppBar(
        backgroundColor: Colors.transparent,
        elevation: 0,
        leading: IconButton(
          icon: Icon(Icons.arrow_back, color: theme.colorScheme.onSurface),
          onPressed: () => Navigator.of(context).pop(),
        ),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: provider.isLoading
              ? const Center(child: CircularProgressIndicator())
              : provider.kanji == null
                  ? const Center(child: Text("Kanji non trouvé"))
                  : SingleChildScrollView(
                      padding: const EdgeInsets.all(16.0),
                      child: Column(
                        children: [
                          _buildKanjiCard(context, provider, theme),
                          const SizedBox(height: 24),
                          _buildSectionHeader(settings.getString("kanji_meanings").toUpperCase(), theme),
                          _buildMeaningsBox(provider.kanji!.meanings, theme),
                          const SizedBox(height: 24),
                          _buildSectionHeader(settings.getString("kanji_readings").toUpperCase(), theme),
                          Row(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Expanded(
                                child: _buildReadingColumn(
                                  "ON (Chinese)",
                                  provider.kanji!.readings.where((r) => r.type == 'on').toList(),
                                  true,
                                  theme,
                                ),
                              ),
                              const SizedBox(width: 16),
                              Expanded(
                                child: _buildReadingColumn(
                                  "KUN (Japanese)",
                                  provider.kanji!.readings.where((r) => r.type == 'kun').toList(),
                                  false,
                                  theme,
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: 24),
                          _buildStatsRow(provider.kanji!, theme),
                          const SizedBox(height: 24),
                          if (showGraph) ...[
                            _buildSectionHeader(settings.getString("kanji_detail_components").toUpperCase(), theme),
                            _buildGraphSection(context, provider, theme),
                          ] else if (provider.kanji!.components.isNotEmpty) ...[
                            _buildSectionHeader(settings.getString("kanji_detail_components").toUpperCase(), theme),
                            _buildComponentsSection(context, provider, theme),
                          ],
                          const SizedBox(height: 24),
                          _buildSectionHeader("EXAMPLES", theme),
                          _buildExamplesList(context, provider.examples, theme),
                        ],
                      ),
                    ),
        ),
      ),
    );
  }

  Widget _buildKanjiCard(BuildContext context, KanjiDetailProvider provider, ThemeData theme) {
    return Card(
      elevation: 16,
      shadowColor: Colors.black.withValues(alpha: 0.3),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface,
      child: Container(
        width: 300,
        height: 300,
        padding: const EdgeInsets.all(16),
        child: Stack(
          children: [
            Align(
              alignment: Alignment.topRight,
              child: IconButton(
                icon: Icon(
                  provider.isInRevisionList ? Icons.star : Icons.star_border,
                  color: provider.isInRevisionList ? theme.colorScheme.primary : theme.colorScheme.onSurfaceVariant,
                ),
                onPressed: () => provider.toggleRevisionList(),
              ),
            ),
            Center(
              child: Text(
                provider.kanji!.character,
                style: TextStyle(
                  fontSize: 180,
                  fontFamily: 'KanjiStrokeOrders',
                  color: theme.colorScheme.primary,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildSectionHeader(String text, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Align(
        alignment: Alignment.centerLeft,
        child: Text(
          text,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onSurface,
            letterSpacing: 1.2,
          ),
        ),
      ),
    );
  }

  Widget _buildMeaningsBox(List<String> meanings, ThemeData theme) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Text(
        meanings.join(", "),
        textAlign: TextAlign.center,
        style: TextStyle(fontSize: 18, color: theme.colorScheme.onSurface),
      ),
    );
  }

  Widget _buildReadingColumn(String title, List<ReadingInfo> readings, bool isOn, ThemeData theme) {
    return Container(
      padding: const EdgeInsets.all(8),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Column(
        children: [
          Text(
            title,
            style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
          ),
          Divider(color: theme.colorScheme.outline),
          ...readings.map((r) => Padding(
                padding: const EdgeInsets.symmetric(vertical: 4.0),
                child: Text(
                  isOn ? _hiraganaToKatakana(r.text) : r.text,
                  style: TextStyle(fontSize: 16, color: theme.colorScheme.onSurface),
                ),
              )),
        ],
      ),
    );
  }

  Widget _buildStatsRow(DictionaryItem kanji, ThemeData theme) {
    String jlpt = "-";
    for (var l in kanji.levelIds) {
      if (l.startsWith("N")) jlpt = l;
    }

    String schoolGrade = "-";
    for (var cat in kanji.categories) {
      if (cat.startsWith("Grade") || cat.startsWith("grade")) {
        schoolGrade = cat.replaceAll("Grade ", "").replaceAll("grade ", "");
      }
    }

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
        children: [
          _buildStatItem("JLPT", jlpt, theme),
          _buildStatItem("GRADE", schoolGrade, theme),
          _buildStatItem("STROKES", kanji.strokeCount.toString(), theme),
        ],
      ),
    );
  }

  Widget _buildStatItem(String label, String value, ThemeData theme) {
    return Column(
      children: [
        Text(
          label,
          style: TextStyle(fontSize: 12, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurfaceVariant),
        ),
        Text(
          value,
          style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
        ),
      ],
    );
  }

  Widget _buildGraphSection(BuildContext context, KanjiDetailProvider provider, ThemeData theme) {
    return Container(
      height: 380,
      width: double.infinity,
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: KanjiGraphWidget(
        rootNode: provider.componentTree!,
        structure: provider.kanji!.structure,
        onKanjiClick: (char) {
          final targetId = provider.characterToIdMap[char];
          if (targetId != null) {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (context) => KanjiDetailScreen(kanjiId: targetId),
              ),
            );
          }
        },
      ),
    );
  }

  Widget _buildComponentsSection(BuildContext context, KanjiDetailProvider provider, ThemeData theme) {
    final kanji = provider.kanji!;
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: theme.colorScheme.surface.withValues(alpha: 0.7),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: theme.colorScheme.outline),
      ),
      child: Wrap(
        alignment: WrapAlignment.center,
        spacing: 16,
        runSpacing: 16,
        children: [
          if (kanji.structure != null)
            Text(
              kanji.structure!,
              style: TextStyle(fontSize: 40, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
            ),
          ...kanji.components.map((comp) {
            final targetChar = comp.text ?? comp.kanjiRef;
            final targetId = targetChar != null ? provider.characterToIdMap[targetChar] : null;

            return GestureDetector(
              onTap: () {
                if (targetId != null) {
                  Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => KanjiDetailScreen(kanjiId: targetId),
                    ),
                  );
                }
              },
              child: Column(
                children: [
                  Text(
                    comp.text ?? comp.kanjiRef ?? "",
                    style: TextStyle(
                      fontSize: 32,
                      fontFamily: 'KanjiStrokeOrders',
                      color: targetId != null ? theme.colorScheme.primary : theme.colorScheme.onSurface,
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  Widget _buildExamplesList(BuildContext context, List<DictionaryItem> examples, ThemeData theme) {
    if (examples.isEmpty) {
      return Container(
        width: double.infinity,
        padding: const EdgeInsets.all(16),
        alignment: Alignment.center,
        child: const Text("Aucun exemple disponible", style: TextStyle(color: Colors.grey)),
      );
    }
    return Column(
      children: examples.map((ex) => _buildExampleRow(context, ex, theme)).toList(),
    );
  }

  Widget _buildExampleRow(BuildContext context, DictionaryItem item, ThemeData theme) {
    return Card(
      elevation: 0,
      margin: const EdgeInsets.only(bottom: 8),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(8),
        side: BorderSide(color: theme.colorScheme.outlineVariant.withValues(alpha: 0.5)),
      ),
      color: theme.colorScheme.surface.withValues(alpha: 0.7),
      child: ListTile(
        title: Text(
          item.character,
          style: TextStyle(fontSize: 24, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
        ),
        subtitle: Text(
          item.meanings.join(", "),
          style: TextStyle(color: theme.colorScheme.onSurfaceVariant),
        ),
        trailing: Icon(Icons.chevron_right, color: theme.colorScheme.onSurfaceVariant),
        onTap: () {
          Navigator.push(
            context,
            MaterialPageRoute(
              builder: (context) => WordDetailScreen(wordText: item.character),
            ),
          );
        },
      ),
    );
  }

  String _hiraganaToKatakana(String s) {
    return s.runes.map((r) {
      if (r >= 0x3041 && r <= 0x3096) {
        return r + 0x60;
      }
      return r;
    }).map((r) => String.fromCharCode(r)).join();
  }
}

// Graph Tree Classes
class VisualNode {
  final String character;
  final double x;
  final double y;
  final int level;
  final String? id;
  final List<String> onReadings;

  VisualNode({
    required this.character,
    required this.x,
    required this.y,
    required this.level,
    this.id,
    this.onReadings = const [],
  });
}

class VisualEdge {
  final VisualNode parent;
  final VisualNode child;

  VisualEdge({required this.parent, required this.child});
}

class KanjiGraphWidget extends StatefulWidget {
  final ComponentNode rootNode;
  final String? structure;
  final Function(String) onKanjiClick;

  const KanjiGraphWidget({
    super.key,
    required this.rootNode,
    this.structure,
    required this.onKanjiClick,
  });

  @override
  State<KanjiGraphWidget> createState() => _KanjiGraphWidgetState();
}

class _KanjiGraphWidgetState extends State<KanjiGraphWidget> {
  final List<VisualNode> _visualNodes = [];
  final List<VisualEdge> _visualEdges = [];
  final Map<String, double> _subtreeWidths = {};
  Set<String> _rootOnReadings = {};

  @override
  void initState() {
    super.initState();
    _layoutGraph();
  }

  @override
  void didUpdateWidget(KanjiGraphWidget oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.rootNode != widget.rootNode) {
      _layoutGraph();
    }
  }

  void _layoutGraph() {
    _visualNodes.clear();
    _visualEdges.clear();
    _subtreeWidths.clear();
    _rootOnReadings = widget.rootNode.onReadings.toSet();

    _calculateWidths(widget.rootNode);
    // Let's start the root at x = 0.0, y = 40.0
    _layoutNode(widget.rootNode, 0.0, 40.0, 0);
  }

  double _calculateWidths(ComponentNode node) {
    if (node.children.isEmpty) {
      _subtreeWidths[node.character] = 90.0;
      return 90.0;
    }
    double totalWidth = 0.0;
    for (var child in node.children) {
      totalWidth += _calculateWidths(child);
    }
    double w = max(90.0, totalWidth);
    _subtreeWidths[node.character] = w;
    return w;
  }

  VisualNode _layoutNode(ComponentNode node, double x, double y, int level) {
    final vNode = VisualNode(
      character: node.character,
      x: x,
      y: y,
      level: level,
      id: node.id,
      onReadings: node.onReadings,
    );
    _visualNodes.add(vNode);

    if (node.children.isNotEmpty) {
      double childrenSumWidth = 0.0;
      for (var child in node.children) {
        childrenSumWidth += _subtreeWidths[child.character] ?? 0.0;
      }
      double currentX = x - childrenSumWidth / 2.0;

      for (var child in node.children) {
        final childWidth = _subtreeWidths[child.character] ?? 0.0;
        final childCenterX = currentX + childWidth / 2.0;

        final childVNode = _layoutNode(child, childCenterX, y + 100.0, level + 1);
        _visualEdges.add(VisualEdge(parent: vNode, child: childVNode));

        currentX += childWidth;
      }
    }
    return vNode;
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    // Calculate bounds of layout to size the canvas appropriately
    double minX = 0.0;
    double maxX = 0.0;
    double maxY = 40.0;

    for (var node in _visualNodes) {
      if (node.x < minX) minX = node.x;
      if (node.x > maxX) maxX = node.x;
      if (node.y > maxY) maxY = node.y;
    }

    final graphWidth = (maxX - minX) + 120.0;
    final graphHeight = maxY + 60.0;

    return Center(
      child: InteractiveViewer(
        boundaryMargin: const EdgeInsets.all(50),
        minScale: 0.7,
        maxScale: 2.0,
        child: SizedBox(
          width: max(320.0, graphWidth),
          height: max(350.0, graphHeight),
          child: Stack(
            children: [
              // Dash box for structure character
              if (widget.structure != null && widget.structure!.isNotEmpty)
                Positioned(
                  top: 16,
                  right: 16,
                  child: Text(
                    widget.structure!,
                    style: TextStyle(
                      fontSize: 48,
                      fontWeight: FontWeight.bold,
                      color: theme.colorScheme.onSurface.withValues(alpha: 0.3),
                    ),
                  ),
                ),
              GestureDetector(
                onTapUp: (details) {
                  final tapPos = details.localPosition;
                  final offsetX = max(320.0, graphWidth) / 2.0;

                  for (var node in _visualNodes) {
                    final nodeCenter = Offset(node.x + offsetX, node.y);
                    final radius = node.level == 0 ? 30.0 : 22.0;
                    final distance = (tapPos - nodeCenter).distance;
                    if (distance <= radius * 1.5) {
                      if (node.id != null && node.id!.isNotEmpty) {
                        widget.onKanjiClick(node.character);
                      }
                      break;
                    }
                  }
                },
                child: CustomPaint(
                  size: Size.infinite,
                  painter: KanjiGraphPainter(
                    visualNodes: _visualNodes,
                    visualEdges: _visualEdges,
                    rootOnReadings: _rootOnReadings,
                    nodeColorMain: theme.colorScheme.tertiary,
                    nodeColorSub: theme.colorScheme.primary,
                    textColor: Colors.white,
                    edgeColor: theme.colorScheme.onSurface,
                    labelBgColor: theme.colorScheme.surfaceContainerHighest,
                    labelTextColor: theme.colorScheme.onSurfaceVariant,
                    linkIndicatorColor: theme.colorScheme.secondary,
                    offsetX: max(320.0, graphWidth) / 2.0,
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class KanjiGraphPainter extends CustomPainter {
  final List<VisualNode> visualNodes;
  final List<VisualEdge> visualEdges;
  final Set<String> rootOnReadings;
  final Color nodeColorMain;
  final Color nodeColorSub;
  final Color textColor;
  final Color edgeColor;
  final Color labelBgColor;
  final Color labelTextColor;
  final Color linkIndicatorColor;
  final double offsetX;

  KanjiGraphPainter({
    required this.visualNodes,
    required this.visualEdges,
    required this.rootOnReadings,
    required this.nodeColorMain,
    required this.nodeColorSub,
    required this.textColor,
    required this.edgeColor,
    required this.labelBgColor,
    required this.labelTextColor,
    required this.linkIndicatorColor,
    required this.offsetX,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final edgePaint = Paint()
      ..color = edgeColor.withValues(alpha: 0.8)
      ..style = PaintingStyle.stroke;

    // 1. Draw Edges
    for (final edge in visualEdges) {
      final parent = edge.parent;
      final child = edge.child;

      final start = Offset(child.x + offsetX, child.y);
      final end = Offset(parent.x + offsetX, parent.y);

      // Check if child shares Onyomi reading with root
      String? sharedReading;
      for (var r in child.onReadings) {
        if (rootOnReadings.contains(r)) {
          sharedReading = r;
          break;
        }
      }

      final isImportant = sharedReading != null;
      edgePaint.strokeWidth = isImportant ? 3.0 : 1.5;

      // Draw the line
      canvas.drawLine(start, end, edgePaint);

      // Draw the arrow
      final dx = end.dx - start.dx;
      final dy = end.dy - start.dy;
      final angle = atan2(dy, dx);
      final arrowSize = isImportant ? 10.0 : 7.0;
      final nodeRadius = parent.level == 0 ? 30.0 : 22.0;
      
      final endX = end.dx - cos(angle) * nodeRadius;
      final endY = end.dy - sin(angle) * nodeRadius;

      final arrowPath = Path()
        ..moveTo(endX, endY)
        ..lineTo(endX - arrowSize * cos(angle - 0.5), endY - arrowSize * sin(angle - 0.5))
        ..lineTo(endX - arrowSize * cos(angle + 0.5), endY - arrowSize * sin(angle + 0.5))
        ..close();

      final arrowPaint = Paint()
        ..color = edgeColor.withValues(alpha: 0.8)
        ..style = PaintingStyle.fill;
      canvas.drawPath(arrowPath, arrowPaint);

      // Draw reading bubble if shared
      if (isImportant) {
        final midX = (start.dx + end.dx) / 2;
        final midY = (start.dy + end.dy) / 2;

        final textPainter = TextPainter(
          text: TextSpan(
            text: sharedReading,
            style: TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.bold,
              color: labelTextColor,
            ),
          ),
          textDirection: TextDirection.ltr,
        );
        textPainter.layout();

        final bubblePos = Offset(midX + 6.0, midY - textPainter.height / 2);
        final bubbleRect = Rect.fromLTWH(
          bubblePos.dx - 4,
          bubblePos.dy - 2,
          textPainter.width + 8,
          textPainter.height + 4,
        );

        final bubblePaint = Paint()..color = labelBgColor;
        canvas.drawRRect(
          RRect.fromRectAndRadius(bubbleRect, const Radius.circular(4)),
          bubblePaint,
        );

        textPainter.paint(canvas, bubblePos);
      }
    }

    // 2. Draw Nodes
    for (final node in visualNodes) {
      final pos = Offset(node.x + offsetX, node.y);
      final radius = node.level == 0 ? 30.0 : 22.0;
      final color = node.level == 0 ? nodeColorMain : nodeColorSub;

      // Circle Fill
      final circlePaint = Paint()
        ..color = color
        ..style = PaintingStyle.fill;
      canvas.drawCircle(pos, radius, circlePaint);

      // Circle Outline
      final outlinePaint = Paint()
        ..color = Colors.white
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.0;
      canvas.drawCircle(pos, radius, outlinePaint);

      // Character text
      final textPainter = TextPainter(
        text: TextSpan(
          text: node.character,
          style: TextStyle(
            fontSize: node.level == 0 ? 22.0 : 16.0,
            fontWeight: FontWeight.bold,
            color: textColor,
            fontFamily: 'KanjiStrokeOrders',
          ),
        ),
        textDirection: TextDirection.ltr,
      );
      textPainter.layout();
      textPainter.paint(
        canvas,
        pos - Offset(textPainter.width / 2, textPainter.height / 2),
      );

      // Clickable indicator (green dot)
      if (node.id != null && node.id!.isNotEmpty) {
        const indicatorRadius = 5.0;
        const angle = -45.0 * (pi / 180.0);
        final indicatorCenter = pos + Offset(radius * cos(angle), radius * sin(angle));

        final indBgPaint = Paint()
          ..color = Colors.white
          ..style = PaintingStyle.fill;
        canvas.drawCircle(indicatorCenter, indicatorRadius, indBgPaint);

        final indFgPaint = Paint()
          ..color = linkIndicatorColor
          ..style = PaintingStyle.fill;
        canvas.drawCircle(indicatorCenter, indicatorRadius * 0.7, indFgPaint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}
