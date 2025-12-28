package org.nihongo.mochi.di

import org.koin.dsl.module
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository

val commonModule = module {
    single { KanaRepository(get()) }
    single { KanjiRepository(get(), get(), get()) }
    single { WordRepository(get()) }
    single { MeaningRepository(get()) }
    single { SettingsRepository(get()) }
    single { LevelContentProvider(get(), get(), get()) }
}
