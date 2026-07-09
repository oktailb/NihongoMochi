import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_widget_from_html/flutter_widget_from_html.dart';
import 'providers/grammar_provider.dart';
import 'repositories/grammar_repository.dart';
import 'repositories/score_repository.dart';
import 'widgets/grammar_painter.dart';
import 'widgets/grammar_node_item.dart';

class GrammarScreen extends StatelessWidget {
  final String maxLevelId;

  const GrammarScreen({super.key, required this.maxLevelId});

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = GrammarProvider(
          context.read<GrammarRepository>(),
          context.read<ScoreRepository>(),
        );
        provider.loadGraph(maxLevelId);
        return provider;
      },
      child: const GrammarView(),
    );
  }
}

class GrammarView extends StatefulWidget {
  const GrammarView({super.key});

  @override
  State<GrammarView> createState() => _GrammarViewState();
}

class _GrammarViewState extends State<GrammarView> {
  final ScrollController _scrollController = ScrollController();
  String _detectedLevelName = "";
  bool _initialScrollDone = false;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
  }

  void _onScroll() {
    final provider = context.read<GrammarProvider>();
    if (provider.separators.isEmpty) return;

    final double scrollY = _scrollController.offset;
    final double viewportHeight = MediaQuery.of(context).size.height;
    final double canvasHeight = provider.totalLayoutSlots * 70.0 + 200.0;

    // Detection du niveau actuel (similaire au snapshotFlow Kotlin)
    final double centerViewY = scrollY + (viewportHeight / 3.0);

    GrammarLevelSeparator? currentSep;
    for (var sep in provider.separators) {
      if ((sep.y * canvasHeight) > centerViewY) {
        currentSep = sep;
        break;
      }
    }
    currentSep ??= provider.separators.last;

    if (currentSep.levelId != _detectedLevelName) {
      setState(() {
        _detectedLevelName = currentSep!.levelId;
      });
    }
  }

  void _showLesson(BuildContext context, GrammarNode node) async {
    final provider = context.read<GrammarProvider>();
    final repo = context.read<GrammarRepository>();
    final isDark = Theme.of(context).brightness == Brightness.dark;

    final css = await repo.loadCss(isDark);
    final htmlContent = await repo.loadLessonHtml(node.rule.id, "fr"); // TODO: i18n locale

    if (!context.mounted) return;

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => Container(
        height: MediaQuery.of(context).size.height * 0.85,
        decoration: BoxDecoration(
          color: isDark ? const Color(0xFF1A1A1A) : Colors.white,
          borderRadius: const BorderRadius.vertical(top: Radius.circular(20)),
            image: DecorationImage(
            image: AssetImage(
              isDark ? 'assets/drawable/lesson_bg_dark.webp' : 'assets/drawable/lesson_bg_light.webp',
            ),
            fit: BoxFit.fill,
          ),
        ),
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.all(16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      node.rule.description,
                      style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.close),
                    onPressed: () => Navigator.pop(context),
                  ),
                ],
              ),
            ),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 16),
                child: HtmlWidget(
                  "<style>$css</style>$htmlContent",
                  textStyle: TextStyle(color: isDark ? Colors.white : Colors.black87),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<GrammarProvider>();
    final isDark = Theme.of(context).brightness == Brightness.dark;

    if (provider.isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final double canvasWidth = MediaQuery.of(context).size.width;
    final double canvasHeight = provider.totalLayoutSlots * 70.0 + 200.0;

    // Initial Scroll logic
    if (!_initialScrollDone && provider.separators.isNotEmpty) {
      WidgetsBinding.instance.addPostFrameCallback((_) {
        final targetIndex = provider.separators.indexWhere((s) => s.levelId.toUpperCase() == provider.currentLevelId.toUpperCase());
        if (targetIndex != -1) {
          final double targetY = targetIndex > 0 ? provider.separators[targetIndex - 1].y * canvasHeight : 0;
          final double finalScroll = (targetY - 100).clamp(0, _scrollController.position.maxScrollExtent);
          _scrollController.jumpTo(finalScroll);
          _initialScrollDone = true;
        }
      });
    }

    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topCenter,
            end: Alignment.bottomCenter,
            colors: [Color(0xFFFDFCFB), Color(0xFFE2D1C3)],
          ),
        ),
        child: Stack(
          children: [
            SingleChildScrollView(
              controller: _scrollController,
              child: SizedBox(
                width: canvasWidth,
                height: canvasHeight,
                child: Stack(
                  children: [
                    // 1. Stone Path (Repeated Background)
                    Positioned.fill(
                      child: Align(
                        alignment: Alignment.topCenter,
                        child: SizedBox(
                          width: 48,
                          child: ListView.builder(
                            physics: const NeverScrollableScrollPhysics(),
                            shrinkWrap: true,
                            itemBuilder: (context, index) => Image.asset(
                              'assets/drawable/stonepath.webp',
                              fit: BoxFit.fitWidth,
                              opacity: const AlwaysStoppedAnimation(0.9),
                            ),
                          ),
                        ),
                      ),
                    ),
                    // 2. Custom Painter for Connections
                    CustomPaint(
                      size: Size(canvasWidth, canvasHeight),
                      painter: GrammarGraphPainter(
                        nodes: provider.nodes,
                        separators: provider.separators,
                        lineColor: isDark ? Colors.white54 : Colors.brown.shade300,
                      ),
                    ),
                    // 3. Torii Separators
                    ...provider.separators.map((sep) => Positioned(
                      top: (sep.y * canvasHeight) - 150,
                      left: 0,
                      right: 0,
                      child: Column(
                        children: [
                          ElevatedButton.icon(
                            onPressed: () { /* TODO: Start Exam */ },
                            icon: const Icon(Icons.assignment, size: 18),
                            label: const Text("Passer l'examen"),
                            style: ElevatedButton.styleFrom(
                              backgroundColor: Colors.teal,
                              foregroundColor: Colors.white,
                              shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                            ),
                          ),
                          const SizedBox(height: 8),
                          Image.asset(
                            'assets/drawable/toori.webp',
                            height: 280,
                            fit: BoxFit.contain,
                          ),
                          Transform.translate(
                            offset: const Offset(0, -100),
                            child: Container(
                              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                              decoration: BoxDecoration(
                                color: Colors.white.withOpacity(0.8),
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                "${sep.levelId.toUpperCase()} (${sep.completionPercentage}%)",
                                style: const TextStyle(fontWeight: FontWeight.bold, color: Colors.blue),
                              ),
                            ),
                          ),
                        ],
                      ),
                    )),
                    // 4. Grammar Nodes
                    ...provider.nodes.map((node) => Positioned(
                      left: (node.x * canvasWidth) - 55,
                      top: (node.y * canvasHeight) - 67,
                      child: GrammarNodeItem(
                        node: node,
                        isLeft: node.x < 0.5,
                        onNodeClick: () { /* TODO: Start Quiz */ },
                        onLessonClick: () => _showLesson(context, node),
                      ),
                    )),
                  ],
                ),
              ),
            ),
            // Floating Level Header
            if (_detectedLevelName.isNotEmpty)
              Positioned(
                top: MediaQuery.of(context).padding.top + 10,
                left: 0,
                right: 0,
                child: Center(
                  child: Container(
                    padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
                    decoration: BoxDecoration(
                      color: Colors.blue.withOpacity(0.9),
                      borderRadius: BorderRadius.circular(30),
                      boxShadow: const [BoxShadow(color: Colors.black26, blurRadius: 10)],
                    ),
                    child: Text(
                      _detectedLevelName.toUpperCase(),
                      style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  ),
                ),
              ),
            // Back Button
            Positioned(
              top: MediaQuery.of(context).padding.top + 10,
              left: 16,
              child: CircleAvatar(
                backgroundColor: Colors.white,
                child: IconButton(
                  icon: const Icon(Icons.arrow_back, color: Colors.black),
                  onPressed: () => Navigator.pop(context),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }
}
