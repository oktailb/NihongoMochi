import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:flutter_widget_from_html/flutter_widget_from_html.dart';
import 'providers/grammar_provider.dart';
import 'providers/settings_provider.dart';
import 'repositories/grammar_repository.dart';
import 'repositories/score_repository.dart';
import 'package:flutter/foundation.dart' show kIsWeb;
import 'widgets/grammar_painter.dart';
import 'widgets/grammar_node_item.dart';
import 'widgets/mochi_background.dart';
import 'grammar_quiz_screen.dart';

const double scale = kIsWeb ? 1.5 : 1.0;

class GrammarScreen extends StatelessWidget {
  final String maxLevelId;
  final String block;

  const GrammarScreen({
    super.key,
    required this.maxLevelId,
    this.block = "rules",
  });

  @override
  Widget build(BuildContext context) {
    return ChangeNotifierProvider(
      create: (context) {
        final provider = GrammarProvider(
          context.read<GrammarRepository>(),
          context.read<ScoreRepository>(),
        );
        provider.loadGraph(maxLevelId, block);
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
    final repo = context.read<GrammarRepository>();
    final settings = context.read<SettingsProvider>();
    final isDark = settings.isDarkMode;

    final css = await repo.loadCss(isDark);
    final htmlContent = await repo.loadLessonHtml(node.rule.id, settings.currentLocaleCode);

    if (!context.mounted) return;

    final parsedCss = _parseCss(css);
    final formattedHtml = _applyCommonStyle(htmlContent, css);

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
              padding: const EdgeInsets.symmetric(horizontal: 24.0, vertical: 16.0),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      settings.getString(node.rule.description),
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
                padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 8),
                child: HtmlWidget(
                  formattedHtml,
                  customStylesBuilder: (element) {
                    final Map<String, String> styles = {};
                    
                    // Style par nom de balise (ex: h1, strong, em, td)
                    final tag = element.localName?.toLowerCase();
                    if (tag != null && parsedCss.containsKey(tag)) {
                      styles.addAll(parsedCss[tag]!);
                    }
                    
                    // Style par classe CSS (ex: .example)
                    if (element.className.isNotEmpty) {
                      final classes = element.className.split(' ');
                      for (final cls in classes) {
                        final classSelector = '.${cls.trim().toLowerCase()}';
                        if (parsedCss.containsKey(classSelector)) {
                          styles.addAll(parsedCss[classSelector]!);
                        }
                      }
                    }
                    
                    return styles.isNotEmpty ? styles : null;
                  },
                  textStyle: TextStyle(color: isDark ? Colors.white : Colors.black87),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Map<String, Map<String, String>> _parseCss(String cssContent) {
    final Map<String, Map<String, String>> rules = {};
    
    // Nettoyer les commentaires CSS
    final cleanCss = cssContent.replaceAll(RegExp(r'\/\*[\s\S]*?\*\/'), '');
    
    // Séparer les blocs sélecteurs { propriétés }
    final regex = RegExp(r'([^{]+)\{([^}]+)\}');
    final matches = regex.allMatches(cleanCss);
    
    for (final match in matches) {
      final selectorsRaw = match.group(1)!;
      final declarationsRaw = match.group(2)!;
      
      final Map<String, String> declarations = {};
      final decRegex = RegExp(r'([^:]+):([^;]+);?');
      for (final decMatch in decRegex.allMatches(declarationsRaw)) {
        final prop = decMatch.group(1)!.trim().toLowerCase();
        final val = decMatch.group(2)!.trim();
        declarations[prop] = val;
      }
      
      final selectors = selectorsRaw.split(',');
      for (final s in selectors) {
        final selector = s.trim().toLowerCase();
        if (selector.isNotEmpty) {
          rules[selector] = declarations;
        }
      }
    }
    
    return rules;
  }

  String _applyCommonStyle(String htmlContent, String cssContent) {
    // Transform `text` to <strong>text</strong>
    var processedHtml = htmlContent.replaceAllMapped(
      RegExp(r'`([^`]+)`'),
      (match) => '<strong>${match.group(1)}</strong>',
    );
    processedHtml = processedHtml.replaceAll('«', '<strong>«');
    processedHtml = processedHtml.replaceAll('»', '»</strong>');

    final styleTag = '<style>\n$cssContent\n</style>';
    if (processedHtml.contains('</head>')) {
      return processedHtml.replaceAll('</head>', '$styleTag</head>');
    } else if (processedHtml.contains('<body>')) {
      return processedHtml.replaceAll('<body>', '<body>$styleTag');
    } else {
      return '$styleTag$processedHtml';
    }
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<GrammarProvider>();
    final isDark = Theme.of(context).brightness == Brightness.dark;

    if (provider.isLoading) {
      return const Scaffold(body: Center(child: CircularProgressIndicator()));
    }

    final double canvasWidth = MediaQuery.of(context).size.width;
    final double canvasHeight = provider.totalLayoutSlots * 70.0 * scale + 200.0 * scale;

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
      backgroundColor: Colors.transparent,
      body: MochiBackground(
        child: Stack(
          children: [
            SingleChildScrollView(
              controller: _scrollController,
              child: SizedBox(
                width: canvasWidth,
                height: canvasHeight,
                child: Stack(
                  children: [
                    // 1. Custom Painter for Connections (arrière-plan)
                    CustomPaint(
                      size: Size(canvasWidth, canvasHeight),
                      painter: GrammarGraphPainter(
                        nodes: provider.nodes,
                        separators: provider.separators,
                        lineColor: isDark ? Colors.white54 : Colors.brown.shade300,
                      ),
                    ),
                    // 2. Stonepath (au-dessus des lignes, en dessous du Torii)
                    Positioned(
                      top: 0,
                      bottom: 0,
                      left: (canvasWidth - 48 * scale) / 2,
                      width: 48 * scale,
                      child: Image.asset(
                        'assets/drawable/stonepath.webp',
                        repeat: ImageRepeat.repeatY,
                        opacity: const AlwaysStoppedAnimation(0.9),
                      ),
                    ),
                    // 3. Torii Separators
                    ...provider.separators.map((sep) => Positioned(
                      top: (sep.y * canvasHeight) - (150 * scale),
                      left: 0,
                      right: 0,
                      child: Column(
                        children: [
                          ElevatedButton.icon(
                            onPressed: () {
                              Navigator.push(
                                context,
                                MaterialPageRoute(
                                  builder: (context) => GrammarQuizScreen(grammarTags: sep.ruleIds),
                                ),
                              ).then((_) {
                                provider.loadGraph(provider.currentLevelId, provider.currentBlock);
                              });
                            },
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
                            height: 280 * scale,
                            fit: BoxFit.contain,
                          ),
                          Transform.translate(
                            offset: Offset(0, -100 * scale),
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
                    ...provider.nodes.map((node) => Positioned(
                      left: (node.x * canvasWidth) - (82 * scale),
                      top: (node.y * canvasHeight) - (67 * scale),
                      child: GrammarNodeItem(
                        node: node,
                        isLeft: node.x < 0.5,
                        onNodeClick: () {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => GrammarQuizScreen(grammarTags: [node.rule.id]),
                            ),
                          ).then((_) {
                            provider.loadGraph(provider.currentLevelId, provider.currentBlock);
                          });
                        },
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
