package org.nihongo.mochi.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import org.nihongo.mochi.domain.game.QuestionDirection
import org.nihongo.mochi.domain.game.QuestionType
import org.nihongo.mochi.domain.game.RecognitionGameViewModel
import org.nihongo.mochi.domain.game.WritingGameViewModel
import org.nihongo.mochi.ui.wordquiz.WordQuizViewModel
import org.nihongo.mochi.domain.game.KanaQuizViewModel
import org.nihongo.mochi.domain.kana.KanaType
import org.nihongo.mochi.domain.services.NoOpCloudSaveService
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanaQuestionDirection
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.HomeViewModel
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.presentation.SagaMapScreen
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.ui.about.AboutScreen
import org.nihongo.mochi.ui.home.HomeScreen
import org.nihongo.mochi.ui.dictionary.ComposeDrawingDialog
import org.nihongo.mochi.ui.dictionary.DictionaryScreen
import org.nihongo.mochi.ui.dictionary.KanjiDetailScreen
import org.nihongo.mochi.ui.gamerecap.GameRecapScreen
import org.nihongo.mochi.ui.gamerecap.GameRecapViewModel
import org.nihongo.mochi.ui.games.GamesScreen
import org.nihongo.mochi.ui.games.memorize.MemorizeGameScreen
import org.nihongo.mochi.ui.games.memorize.MemorizeSetupScreen
import org.nihongo.mochi.ui.games.memorize.MemorizeViewModel
import org.nihongo.mochi.ui.games.simon.SimonGameScreen
import org.nihongo.mochi.ui.games.simon.SimonSetupScreen
import org.nihongo.mochi.ui.games.simon.SimonViewModel
import org.nihongo.mochi.ui.games.taquin.TaquinGameScreen
import org.nihongo.mochi.ui.games.taquin.TaquinSetupScreen
import org.nihongo.mochi.ui.games.taquin.TaquinViewModel
import org.nihongo.mochi.ui.games.kanadrop.KanaDropGameScreen
import org.nihongo.mochi.ui.games.kanadrop.KanaDropSetupScreen
import org.nihongo.mochi.ui.games.kanadrop.KanaDropViewModel
import org.nihongo.mochi.ui.games.kanadrop.KanaLinkMode
import org.nihongo.mochi.ui.grammar.GrammarScreen
import org.nihongo.mochi.ui.grammar.GrammarViewModel
import org.nihongo.mochi.ui.grammar.GrammarQuizViewModel
import org.nihongo.mochi.ui.recognitiongame.RecognitionGameScreen
import org.nihongo.mochi.ui.settings.SettingsScreen
import org.nihongo.mochi.ui.writinggame.WritingGameScreen
import org.nihongo.mochi.ui.writingrecap.WritingRecapScreen
import org.nihongo.mochi.ui.writingrecap.WritingRecapViewModel
import org.nihongo.mochi.ui.gojuon.KanaRecapScreen
import org.nihongo.mochi.ui.gojuon.KanaRecapViewModel
import org.nihongo.mochi.ui.gojuon.KanaQuizScreen
import org.nihongo.mochi.ui.wordlist.WordListScreen
import org.nihongo.mochi.ui.wordlist.WordListViewModel
import org.nihongo.mochi.ui.wordquiz.WordQuizScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object About : Screen("about")
    data object Settings : Screen("settings")
    data object Results : Screen("results")
    data object Dictionary : Screen("dictionary")
    data object KanjiDetail : Screen("kanji_detail/{kanjiId}") {
        fun createRoute(kanjiId: String) = "kanji_detail/$kanjiId"
    }
    data object Grammar : Screen("grammar/{levelId}") {
        fun createRoute(levelId: String) = "grammar/$levelId"
    }
    data object Games : Screen("games")
    data object SimonSetup : Screen("simon_setup")
    data object SimonGame : Screen("simon_game")
    data object TaquinSetup : Screen("taquin_setup")
    data object TaquinGame : Screen("taquin_game")
    data object MemorizeSetup : Screen("memorize_setup")
    data object MemorizeGame : Screen("memorize_game")
    data object KanaDropSetup : Screen("kanadrop_setup")
    data object KanaDropGame : Screen("kanadrop_game/{levelId}/{mode}") {
        fun createRoute(levelId: String, mode: String) = "kanadrop_game/$levelId/$mode"
    }
    
    data object RecognitionRecap : Screen("recognition_recap/{levelId}") {
        fun createRoute(levelId: String) = "recognition_recap/$levelId"
    }
    data object RecognitionGame : Screen("recognition_game/{levelId}/{gameMode}/{readingMode}") {
        fun createRoute(levelId: String, gameMode: String, readingMode: String) = 
            "recognition_game/$levelId/$gameMode/$readingMode"
    }

    data object WritingRecap : Screen("writing_recap/{levelId}") {
        fun createRoute(levelId: String) = "writing_recap/$levelId"
    }
    data object WritingGame : Screen("writing_game/{levelId}") {
        fun createRoute(levelId: String) = "writing_game/$levelId"
    }

    data object HiraganaRecap : Screen("hiragana_recap")
    data object KatakanaRecap : Screen("katakana_recap")
    data object KanaQuiz : Screen("kana_quiz/{type}/{mode}/{level}") {
        fun createRoute(type: String, mode: String, level: String) = "kana_quiz/$type/$mode/$level"
    }

    data object WordList : Screen("word_list/{levelId}") {
        fun createRoute(levelId: String) = "word_list/$levelId"
    }
    data object WordQuiz : Screen("word_quiz/{levelId}") {
        fun createRoute(levelId: String) = "word_quiz/$levelId"
    }
}

@Composable
fun MochiNavGraph(
    navController: NavHostController,
    versionName: String,
    currentDate: String,
    onOpenUrl: (String) -> Unit,
    onThemeChanged: (Boolean) -> Unit,
    onLocaleChanged: (String) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            val homeViewModel: HomeViewModel = koinInject()
            val uiState by homeViewModel.uiState.collectAsState()

            HomeScreen(
                availableLevels = uiState.availableLevels,
                selectedLevelId = uiState.selectedLevelId,
                isRecognitionEnabled = uiState.isRecognitionEnabled,
                isReadingEnabled = uiState.isReadingEnabled,
                isWritingEnabled = uiState.isWritingEnabled,
                isGrammarEnabled = uiState.isGrammarEnabled,
                onLevelSelected = homeViewModel::onLevelSelected,
                onRecognitionClick = {
                    when(uiState.selectedLevelId) {
                        "hiragana" -> navController.navigate(Screen.HiraganaRecap.route)
                        "katakana" -> navController.navigate(Screen.KatakanaRecap.route)
                        else -> navController.navigate(Screen.RecognitionRecap.createRoute(uiState.selectedLevelId))
                    }
                },
                onReadingClick = {
                    val levelId = uiState.readingDataFile ?: uiState.selectedLevelId
                    navController.navigate(Screen.WordList.createRoute(levelId))
                },
                onWritingClick = {
                    navController.navigate(Screen.WritingRecap.createRoute(uiState.selectedLevelId))
                },
                onGrammarClick = { 
                    navController.navigate(Screen.Grammar.createRoute(uiState.selectedLevelId))
                },
                onGamesClick = {
                    navController.navigate(Screen.Games.route)
                },
                onDictionaryClick = {
                    navController.navigate(Screen.Dictionary.route)
                },
                onResultsClick = {
                    navController.navigate(Screen.Results.route)
                },
                onOptionsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClick = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                versionName = versionName,
                currentDate = currentDate,
                onIssueTrackerClick = { onOpenUrl("https://github.com/oktailb/KanjiMori/issues") },
                onRateAppClick = { onOpenUrl("market://details?id=org.nihongo.mochi") },
                onPatreonClick = { onOpenUrl("https://www.patreon.com/Oktail") },
                onTipeeeClick = { onOpenUrl("https://en.tipeee.com/lecoq-vincent") },
                onKanjiDataClick = { onOpenUrl("https://github.com/davidluzgouveia/kanji-data") }
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = koinInject()
            SettingsScreen(
                viewModel = settingsViewModel,
                onThemeChanged = onThemeChanged,
                onLocaleChanged = onLocaleChanged
            )
        }

        composable(Screen.Results.route) {
            val resultsViewModel: ResultsViewModel = koinInject { parametersOf(NoOpCloudSaveService()) }
            SagaMapScreen(
                viewModel = resultsViewModel,
                onNodeClick = { id, type -> /* TODO */ },
                onAction = { action -> resultsViewModel.handleSagaAction(action) }
            )
        }

        composable(Screen.Dictionary.route) {
            val dictionaryViewModel: DictionaryViewModel = koinInject()
            var showDrawingDialog by remember { mutableStateOf(false) }

            DictionaryScreen(
                viewModel = dictionaryViewModel,
                onOpenDrawing = { showDrawingDialog = true },
                onClearDrawing = { dictionaryViewModel.clearDrawingFilter() },
                onItemClick = { item ->
                    navController.navigate(Screen.KanjiDetail.createRoute(item.id))
                }
            )

            if (showDrawingDialog) {
                ComposeDrawingDialog(
                    viewModel = dictionaryViewModel,
                    onDismiss = { showDrawingDialog = false },
                    onConfirm = { showDrawingDialog = false }
                )
            }
        }

        composable(
            route = Screen.KanjiDetail.route,
            arguments = listOf(navArgument("kanjiId") { defaultValue = "" })
        ) { backStackEntry ->
            val kanjiId = backStackEntry.arguments?.getString("kanjiId") ?: ""
            val detailViewModel: KanjiDetailViewModel = koinInject()
            
            KanjiDetailScreen(
                viewModel = detailViewModel,
                kanjiId = kanjiId,
                onBackClick = { navController.popBackStack() },
                onKanjiClick = { nextKanjiChar -> 
                    navController.navigate(Screen.KanjiDetail.createRoute(nextKanjiChar))
                }
            )
        }

        composable(
            route = Screen.Grammar.route,
            arguments = listOf(navArgument("levelId") { defaultValue = "N5" })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "N5"
            val grammarViewModel: GrammarViewModel = koinInject()
            
            remember(levelId) {
                grammarViewModel.loadGraph(levelId)
                true
            }

            GrammarScreen(
                viewModel = grammarViewModel,
                onBackClick = { navController.popBackStack() },
                quizViewModelFactory = { tags, _ ->
                    koinInject { parametersOf(tags) }
                }
            )
        }

        composable(Screen.Games.route) {
            val homeViewModel: HomeViewModel = koinInject()
            val uiState by homeViewModel.uiState.collectAsState()
            
            GamesScreen(
                onBackClick = { navController.popBackStack() },
                onTaquinClick = { navController.navigate(Screen.TaquinSetup.route) },
                onSimonClick = { navController.navigate(Screen.SimonSetup.route) },
                onTetrisClick = { 
                    navController.navigate(Screen.KanaDropSetup.route) 
                },
                onCrosswordsClick = { /* TODO */ },
                onMemorizeClick = { navController.navigate(Screen.MemorizeSetup.route) },
                onParticlesClick = { /* TODO */ },
                onForgeClick = { /* TODO */ },
                onShiritoriClick = { /* TODO */ },
                onShadowClick = { /* TODO */ }
            )
        }

        composable(Screen.SimonSetup.route) {
            val viewModel: SimonViewModel = koinInject()
            SimonSetupScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
                onStartGame = { navController.navigate(Screen.SimonGame.route) }
            )
        }

        composable(Screen.SimonGame.route) {
            val viewModel: SimonViewModel = koinInject()
            SimonGameScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TaquinSetup.route) {
            val viewModel: TaquinViewModel = koinInject()
            TaquinSetupScreen(
                viewModel = viewModel,
                onStartGame = { navController.navigate(Screen.TaquinGame.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.TaquinGame.route) {
            val viewModel: TaquinViewModel = koinInject()
            TaquinGameScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MemorizeSetup.route) {
            val viewModel: MemorizeViewModel = koinInject()
            MemorizeSetupScreen(
                viewModel = viewModel,
                onStartGame = { navController.navigate(Screen.MemorizeGame.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.MemorizeGame.route) {
            val viewModel: MemorizeViewModel = koinInject()
            MemorizeGameScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.KanaDropSetup.route) {
            val viewModel: KanaDropViewModel = koinInject()
            val homeViewModel: HomeViewModel = koinInject()
            val uiState by homeViewModel.uiState.collectAsState()
            val levelId = uiState.readingDataFile ?: "jlpt_wordlist_n5"

            KanaDropSetupScreen(
                viewModel = viewModel,
                levelId = levelId,
                onStartGame = { mode -> 
                    navController.navigate(Screen.KanaDropGame.createRoute(levelId, mode.name))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.KanaDropGame.route,
            arguments = listOf(
                navArgument("levelId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "jlpt_wordlist_n5"
            val modeStr = backStackEntry.arguments?.getString("mode") ?: "TIME_ATTACK"
            val mode = KanaLinkMode.valueOf(modeStr)
            
            val viewModel: KanaDropViewModel = koinInject()
            
            remember(levelId, mode) {
                viewModel.initGame(levelId, mode)
                true
            }

            KanaDropGameScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Recognition Recap
        composable(
            route = Screen.RecognitionRecap.route,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "N5"
            val baseColor = MaterialTheme.colorScheme.surface.toArgb()
            val viewModel: GameRecapViewModel = koinInject { parametersOf(baseColor) }
            
            val kanjiList by viewModel.kanjiListWithColors.collectAsState(emptyList())
            val currentPage by viewModel.currentPage.collectAsState(0)
            val totalPages by viewModel.totalPages.collectAsState(0)
            
            var gameMode by remember { mutableStateOf("meaning") }
            var readingMode by remember { mutableStateOf("common") }
            
            remember(levelId, gameMode) {
                viewModel.loadLevel(levelId, gameMode)
                true
            }

            GameRecapScreen(
                levelTitle = levelId,
                kanjiListWithColors = kanjiList,
                currentPage = currentPage,
                totalPages = totalPages,
                gameMode = gameMode,
                readingMode = readingMode,
                isMeaningEnabled = true,
                isReadingEnabled = true,
                onKanjiClick = { kanji ->
                    navController.navigate(Screen.KanjiDetail.createRoute(kanji.id))
                },
                onPrevPage = { viewModel.prevPage(gameMode) },
                onNextPage = { viewModel.nextPage(gameMode) },
                onGameModeChange = { 
                    gameMode = it
                    viewModel.updateCurrentPageItems(it)
                },
                onReadingModeChange = { readingMode = it },
                onPlayClick = {
                    navController.navigate(
                        Screen.RecognitionGame.createRoute(levelId, gameMode, readingMode)
                    )
                }
            )
        }

        // Recognition Game
        composable(
            route = Screen.RecognitionGame.route,
            arguments = listOf(
                navArgument("levelId") { type = NavType.StringType },
                navArgument("gameMode") { type = NavType.StringType },
                navArgument("readingMode") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "N5"
            val gameMode = backStackEntry.arguments?.getString("gameMode") ?: "meaning"
            val readingMode = backStackEntry.arguments?.getString("readingMode") ?: "common"
            
            val viewModel: RecognitionGameViewModel = koinInject()
            val gameState by viewModel.state.collectAsState(GameState.Loading)
            val buttonStates by viewModel.buttonStates.collectAsState(emptyList())
            
            remember(levelId, gameMode, readingMode) {
                viewModel.initializeGame(gameMode, readingMode, levelId, null)
                true
            }

            if (viewModel.isGameInitialized) {
                val currentKanji = viewModel.currentKanji
                val direction = viewModel.currentDirection
                
                val questionText = if (direction == QuestionDirection.NORMAL) {
                    currentKanji.character
                } else {
                    if (gameMode == "meaning") {
                        currentKanji.meanings.firstOrNull() ?: ""
                    } else {
                        viewModel.getFormattedReadings(currentKanji)
                    }
                }
                
                val gameStatusList = viewModel.currentKanjiSet.map { 
                    viewModel.kanjiStatus[it] ?: org.nihongo.mochi.domain.models.GameStatus.NOT_ANSWERED 
                }

                RecognitionGameScreen(
                    kanji = currentKanji,
                    questionText = questionText,
                    gameStatus = gameStatusList,
                    answers = viewModel.currentAnswers,
                    buttonStates = buttonStates,
                    buttonsEnabled = viewModel.areButtonsEnabled,
                    direction = direction,
                    gameMode = gameMode,
                    onAnswerClick = { index, answer ->
                        viewModel.submitAnswer(answer, index)
                    }
                )
            } else {
                MochiBackground {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Writing Recap
        composable(
            route = Screen.WritingRecap.route,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "N5"
            val baseColor = MaterialTheme.colorScheme.surface.toArgb()
            val viewModel: WritingRecapViewModel = koinInject { parametersOf(baseColor) }
            
            val kanjiList by viewModel.kanjiList.collectAsState(emptyList())
            val currentPage by viewModel.currentPage.collectAsState(0)
            val totalPages by viewModel.totalPages.collectAsState(0)
            
            remember(levelId) {
                viewModel.loadLevel(levelId)
                true
            }

            WritingRecapScreen(
                levelTitle = levelId,
                kanjiListWithColors = kanjiList,
                currentPage = currentPage,
                totalPages = totalPages,
                onKanjiClick = { kanji ->
                    navController.navigate(Screen.KanjiDetail.createRoute(kanji.id))
                },
                onPrevPage = { viewModel.prevPage() },
                onNextPage = { viewModel.nextPage() },
                onPlayClick = {
                    navController.navigate(Screen.WritingGame.createRoute(levelId))
                }
            )
        }

        // Writing Game
        composable(
            route = Screen.WritingGame.route,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: "N5"
            val viewModel: WritingGameViewModel = koinInject()
            
            // Reactive State Collection
            val gameState by viewModel.state.collectAsState(GameState.Loading)
            val isProcessing by viewModel.isAnswerProcessing.collectAsState(false)
            val lastStatus by viewModel.lastAnswerStatus.collectAsState(null)
            val showCorrection by viewModel.showCorrectionFeedback.collectAsState(false)
            
            remember(levelId) {
                viewModel.initializeGame(levelId)
                true
            }

            if (viewModel.isGameInitialized) {
                val gameStatusList = viewModel.currentKanjiSet.map { 
                    viewModel.kanjiStatus[it] ?: org.nihongo.mochi.domain.models.GameStatus.NOT_ANSWERED 
                }

                WritingGameScreen(
                    kanji = viewModel.currentKanji,
                    questionType = viewModel.currentQuestionType,
                    gameStatus = gameStatusList,
                    onSubmitAnswer = { viewModel.submitAnswer(it) },
                    showCorrection = showCorrection,
                    isCorrect = lastStatus,
                    processingAnswer = isProcessing
                )
            } else {
                MochiBackground {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Hiragana Recap
        composable(Screen.HiraganaRecap.route) {
            val baseColor = MaterialTheme.colorScheme.surface.toArgb()
            val viewModel: KanaRecapViewModel = koinInject { parametersOf(baseColor) }
            
            val charactersByLine by viewModel.charactersByLine.collectAsState(emptyMap())
            val linesToShow by viewModel.linesToShow.collectAsState(emptyList())
            val kanaColors by viewModel.kanaColors.collectAsState(emptyMap())
            val currentPage by viewModel.currentPage.collectAsState(0)
            val totalPages by viewModel.totalPages.collectAsState(0)

            val pageSize = 10
            remember {
                viewModel.loadKana(KanaType.HIRAGANA, pageSize)
                true
            }

            KanaRecapScreen(
                title = "Hiragana",
                kanaListWithColors = emptyList(), 
                linesToShow = linesToShow,
                charactersByLine = charactersByLine,
                kanaColors = kanaColors,
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevPage = { viewModel.prevPage(pageSize) },
                onNextPage = { viewModel.nextPage(pageSize) },
                onPlayClick = {
                    navController.navigate(Screen.KanaQuiz.createRoute("hiragana", "Kana -> Romaji", "Gojūon"))
                }
            )
        }

        // Katakana Recap
        composable(Screen.KatakanaRecap.route) {
            val baseColor = MaterialTheme.colorScheme.surface.toArgb()
            val viewModel: KanaRecapViewModel = koinInject { parametersOf(baseColor) }
            
            val charactersByLine by viewModel.charactersByLine.collectAsState(emptyMap())
            val linesToShow by viewModel.linesToShow.collectAsState(emptyList())
            val kanaColors by viewModel.kanaColors.collectAsState(emptyMap())
            val currentPage by viewModel.currentPage.collectAsState(0)
            val totalPages by viewModel.totalPages.collectAsState(0)

            val pageSize = 10
            remember {
                viewModel.loadKana(KanaType.KATAKANA, pageSize)
                true
            }

            KanaRecapScreen(
                title = "Katakana",
                kanaListWithColors = emptyList(),
                linesToShow = linesToShow,
                charactersByLine = charactersByLine,
                kanaColors = kanaColors,
                currentPage = currentPage,
                totalPages = totalPages,
                onPrevPage = { viewModel.prevPage(pageSize) },
                onNextPage = { viewModel.nextPage(pageSize) },
                onPlayClick = {
                    navController.navigate(Screen.KanaQuiz.createRoute("katakana", "Kana -> Romaji", "Gojūon"))
                }
            )
        }

        // Kana Quiz
        composable(
            route = Screen.KanaQuiz.route,
            arguments = listOf(
                navArgument("type") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType },
                navArgument("level") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val typeStr = backStackEntry.arguments?.getString("type") ?: "hiragana"
            val modeStr = backStackEntry.arguments?.getString("mode") ?: "Kana -> Romaji"
            val levelStr = backStackEntry.arguments?.getString("level") ?: "Gojūon"
            
            val type = if (typeStr == "hiragana") KanaType.HIRAGANA else KanaType.KATAKANA
            val viewModel: KanaQuizViewModel = koinInject()

            remember(type, modeStr, levelStr) {
                viewModel.initializeGame(type, modeStr, levelStr)
                true
            }

            if (viewModel.isGameInitialized) {
                KanaQuizScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            } else {
                MochiBackground {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }

        // Word List
        composable(
            route = Screen.WordList.route,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val viewModel: WordListViewModel = koinInject()
            
            val displayedWords by viewModel.displayedWords.collectAsState(emptyList())
            val currentPage by viewModel.currentPage.collectAsState(0)
            val totalPages by viewModel.totalPages.collectAsState(0)
            val filterKanjiOnly by viewModel.filterKanjiOnly.collectAsState(false)
            val filterSimpleWords by viewModel.filterSimpleWords.collectAsState(false)
            val filterCompoundWords by viewModel.filterCompoundWords.collectAsState(false)
            val filterIgnoreKnown by viewModel.filterIgnoreKnown.collectAsState(false)
            val selectedWordType by viewModel.selectedWordType.collectAsState("Tous" to "All")
            val screenTitleKey by viewModel.screenTitleKey.collectAsState(null)

            remember(levelId) {
                viewModel.loadList(levelId)
                true
            }

            WordListScreen(
                listTitle = screenTitleKey ?: levelId,
                wordsWithColors = displayedWords.map { (word, score, isRed) -> 
                    val baseColor = MaterialTheme.colorScheme.surface.toArgb()
                    val colorInt = org.nihongo.mochi.presentation.ScorePresentationUtils.getScoreColor(score, baseColor)
                    Triple(word, androidx.compose.ui.graphics.Color(colorInt), isRed)
                },
                currentPage = currentPage,
                totalPages = totalPages,
                filterKanjiOnly = filterKanjiOnly,
                filterSimpleWords = filterSimpleWords,
                filterCompoundWords = filterCompoundWords,
                filterIgnoreKnown = filterIgnoreKnown,
                selectedWordType = selectedWordType,
                wordTypeOptions = listOf("Tous" to "All"),
                onFilterKanjiOnlyChange = { viewModel.setFilterKanjiOnly(it) },
                onFilterSimpleWordsChange = { viewModel.setFilterSimpleWords(it) },
                onFilterCompoundWordsChange = { viewModel.setFilterCompoundWords(it) },
                onFilterIgnoreKnownChange = { viewModel.setFilterIgnoreKnown(it) },
                onWordTypeChange = { viewModel.setWordType(it) },
                onPrevPage = { viewModel.prevPage() },
                onNextPage = { viewModel.nextPage() },
                onPlayClick = {
                    navController.navigate(Screen.WordQuiz.createRoute(levelId))
                }
            )
        }

        // Word Quiz
        composable(
            route = Screen.WordQuiz.route,
            arguments = listOf(navArgument("levelId") { type = NavType.StringType })
        ) { backStackEntry ->
            val levelId = backStackEntry.arguments?.getString("levelId") ?: ""
            val viewModel: WordQuizViewModel = koinInject()
            val wordRepository: WordRepository = koinInject()
            val levelContentProvider: LevelContentProvider = koinInject()
            
            val gameState by viewModel.state.collectAsState(GameState.Loading)
            val currentWord by viewModel.currentWord.collectAsState(null)
            val answers by viewModel.currentAnswers.collectAsState(emptyList())
            val buttonStates by viewModel.buttonStates.collectAsState(emptyList())
            val areButtonsEnabled by viewModel.areButtonsEnabled.collectAsState(true)
            val wordStatuses by viewModel.wordStatuses.collectAsState(emptyList())

            remember(levelId) {
                val wordsForQuiz = if (levelId == "user_custom_list") {
                    val texts = levelContentProvider.getCharactersForLevel(levelId)
                    wordRepository.getWordEntriesByText(texts)
                } else {
                    wordRepository.getWordEntriesForLevel(levelId)
                }
                viewModel.initializeGame(wordsForQuiz)
                true
            }

            if (gameState == GameState.Finished) {
                navController.popBackStack()
            } else {
                WordQuizScreen(
                    wordToGuess = currentWord?.text,
                    gameStatus = wordStatuses,
                    answers = answers,
                    buttonStates = buttonStates,
                    buttonsEnabled = areButtonsEnabled,
                    onAnswerClick = { index, answer -> 
                        viewModel.submitAnswer(answer, index)
                    }
                )
            }
        }
    }
}
