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
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.reading.ReadingViewModel
import org.nihongo.mochi.presentation.recognition.RecognitionViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.presentation.writing.WritingViewModel
import org.nihongo.mochi.ui.wordlist.WordListViewModel

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
    factoryOf(::RecognitionViewModel)
    factoryOf(::ReadingViewModel)
    factoryOf(::WritingViewModel)
    factoryOf(::WordListViewModel) // Added this line
    
    // DictionaryViewModel requires HandwritingRecognizer which is platform specific.
    factory { DictionaryViewModel(get(), get(), get(), get(), get()) }
}
