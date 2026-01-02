package org.nihongo.mochi.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.nihongo.mochi.domain.kana.ComposeResourceLoader
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.koin.android.ext.koin.androidContext
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.presentation.reading.ReadingViewModel
import org.nihongo.mochi.presentation.recognition.RecognitionViewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.presentation.writing.WritingViewModel

val appModule = module {
    // Switched to ComposeResourceLoader which is KMP compatible and uses the new resources system
    single<ResourceLoader> { ComposeResourceLoader() }
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
    
    viewModel { KanjiDetailViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get()) } // Added StatisticsEngine
    viewModel { RecognitionViewModel(get(), get()) }
    viewModel { ReadingViewModel(get(), get()) }
    viewModel { WritingViewModel(get()) }
}
