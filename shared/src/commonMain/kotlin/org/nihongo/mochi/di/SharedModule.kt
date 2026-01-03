package org.nihongo.mochi.di

import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.kana.ComposeResourceLoader
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.levels.LevelsRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.HomeViewModel
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.ui.gamerecap.GameRecapViewModel
import org.nihongo.mochi.ui.gojuon.KanaRecapViewModel
import org.nihongo.mochi.ui.wordlist.WordListViewModel
import org.nihongo.mochi.ui.writingrecap.WritingRecapViewModel

val sharedModule = module {
    // --- Data / Repositories ---
    single<ResourceLoader> { ComposeResourceLoader() }
    singleOf(::KanaRepository)
    singleOf(::LevelsRepository)
    single { KanjiRepository(get(), get(), get()) } // Explicit constructor needed if params don't match exactly or multiple constructors
    singleOf(::WordRepository)
    singleOf(::MeaningRepository)
    singleOf(::SettingsRepository)
    singleOf(::LevelContentProvider)
    singleOf(::StatisticsEngine)

    // --- ViewModels ---
    factoryOf(::KanjiDetailViewModel)
    factoryOf(::SettingsViewModel)
    // RecognitionViewModel, ReadingViewModel, WritingViewModel removed
    factoryOf(::WordListViewModel)
    factoryOf(::HomeViewModel) // Added HomeViewModel
    
    // ViewModels with parameters
    factory { params ->
        GameRecapViewModel(
            levelContentProvider = get(),
            kanjiRepository = get(),
            baseColorInt = params.get()
        )
    }
    
    factory { params ->
        KanaRecapViewModel(
            kanaRepository = get(),
            baseColorInt = params.get()
        )
    }

    factory { params ->
        WritingRecapViewModel(
            levelContentProvider = get(),
            kanjiRepository = get(),
            baseColorInt = params.get()
        )
    }

    factory { params ->
        ResultsViewModel(
            cloudSaveService = params.get(),
            statisticsEngine = get()
        )
    }
    
    // DictionaryViewModel requires HandwritingRecognizer which is platform specific.
    factory { DictionaryViewModel(get(), get(), get(), get(), get()) }
}
