package org.nihongo.mochi.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreRepository
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.grammar.ExerciseRepository
import org.nihongo.mochi.domain.grammar.GrammarRepository
import org.nihongo.mochi.domain.kana.ComposeResourceLoader
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.meaning.WordMeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.HomeViewModel
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.ui.gamerecap.GameRecapViewModel
import org.nihongo.mochi.domain.game.RecognitionGameViewModel
import org.nihongo.mochi.domain.game.WritingGameViewModel
import org.nihongo.mochi.domain.game.KanaQuizViewModel
import org.nihongo.mochi.ui.wordquiz.WordQuizViewModel
import org.nihongo.mochi.ui.games.memorize.MemorizeViewModel
import org.nihongo.mochi.ui.games.simon.SimonViewModel
import org.nihongo.mochi.ui.games.taquin.TaquinViewModel
import org.nihongo.mochi.ui.games.kanadrop.KanaDropViewModel
import org.nihongo.mochi.ui.games.crossword.CrosswordViewModel
import org.nihongo.mochi.ui.games.snake.SnakeViewModel
import org.nihongo.mochi.ui.gojuon.KanaRecapViewModel
import org.nihongo.mochi.ui.grammar.GrammarQuizViewModel
import org.nihongo.mochi.ui.grammar.GrammarViewModel
import org.nihongo.mochi.ui.wordlist.WordListViewModel
import org.nihongo.mochi.ui.writingrecap.WritingRecapViewModel
import org.koin.core.qualifier.named
import org.nihongo.mochi.db.DatabaseDriverFactory
import org.nihongo.mochi.db.MochiDatabase
import org.nihongo.mochi.domain.services.StringProvider
import org.nihongo.mochi.ui.ComposeStringProvider

val sharedModule = module {
    // --- Database ---
    single { 
        val driver = get<DatabaseDriverFactory>().createDriver()
        MochiDatabase(driver)
    }

    // --- Data / Repositories ---
    single<ResourceLoader> { ComposeResourceLoader() }
    singleOf(::KanaRepository)
    singleOf(::LevelsRepository)
    single { KanjiRepository(get(), get(), get()) } 
    singleOf(::WordRepository)
    singleOf(::MeaningRepository)
    singleOf(::WordMeaningRepository)
    singleOf(::SettingsRepository)
    singleOf(::LevelContentProvider)
    singleOf(::StatisticsEngine)
    singleOf(::GrammarRepository)
    singleOf(::ExerciseRepository)
    
    // String Provider for ViewModels
    single<StringProvider> { ComposeStringProvider() }

    // ScoreManager with database and legacy settings for migration
    single<ScoreRepository> { 
        ScoreManager(
            database = get(),
            scoresSettings = get(named("scoresSettings")),
            userListSettings = get(named("userListSettings")),
            appSettings = get(named("appSettings"))
        )
    }

    // --- ViewModels ---
    factoryOf(::KanjiDetailViewModel)
    factoryOf(::SettingsViewModel)
    factoryOf(::WordListViewModel)
    factoryOf(::HomeViewModel) 
    
    // ViewModels with persistent state during navigation
    singleOf(::DictionaryViewModel)
    
    // Games are singles to share state between Setup and Game screens.
    singleOf(::SimonViewModel)
    singleOf(::TaquinViewModel)
    singleOf(::MemorizeViewModel)
    singleOf(::KanaDropViewModel)
    singleOf(::CrosswordViewModel)
    singleOf(::SnakeViewModel)

    // Game/Quiz ViewModels as factory for those with single-screen flow
    factoryOf(::GrammarViewModel)
    factoryOf(::RecognitionGameViewModel)
    
    // WritingGameViewModel: ensure optional parameters like textNormalizer are handled
    // Koin will use the primary constructor and get() for all parameters including AudioPlayer
    factoryOf(::WritingGameViewModel)
    
    factoryOf(::KanaQuizViewModel)
    factoryOf(::WordQuizViewModel)
    
    // ViewModels with parameters
    factory { params ->
        GameRecapViewModel(
            levelContentProvider = get(),
            kanjiRepository = get(),
            scoreRepository = get(),
            baseColorInt = params.get()
        )
    }
    
    factory { params ->
        KanaRecapViewModel(
            kanaRepository = get(),
            scoreRepository = get(),
            baseColorInt = params.get()
        )
    }

    factory { params ->
        WritingRecapViewModel(
            levelContentProvider = get(),
            kanjiRepository = get(),
            scoreRepository = get(),
            baseColorInt = params.get()
        )
    }

    factory { params ->
        ResultsViewModel(
            cloudSaveService = params.get(),
            statisticsEngine = get(),
            scoreRepository = get(),
            stringProvider = get()
        )
    }

    factory { params ->
        GrammarQuizViewModel(
            exerciseRepository = get(),
            settingsRepository = get(),
            scoreRepository = get(),
            audioPlayer = get(),
            grammarTags = params.get<List<String>>()
        )
    }
}
