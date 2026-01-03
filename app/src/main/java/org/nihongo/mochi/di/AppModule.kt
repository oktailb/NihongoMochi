package org.nihongo.mochi.di

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.ui.dictionary.AndroidMlKitRecognizer
import org.nihongo.mochi.domain.game.TextNormalizer
import org.nihongo.mochi.ui.writinggame.AndroidTextNormalizer

val appModule = module {
    // Platform specific dependencies
    
    // Settings implementation for Android
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
    
    // Handwriting Recognition implementation (ML Kit)
    single<HandwritingRecognizer> { AndroidMlKitRecognizer() }
    
    // Text Normalizer implementation
    single<TextNormalizer> { AndroidTextNormalizer() }
    
    // Note: All shared dependencies and ViewModels are now in SharedModule.kt
}
