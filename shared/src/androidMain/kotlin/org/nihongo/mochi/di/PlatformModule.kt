package org.nihongo.mochi.di

import android.app.Activity
import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import org.nihongo.mochi.db.DatabaseDriverFactory
import org.nihongo.mochi.domain.game.TextNormalizer
import org.nihongo.mochi.domain.recognition.HandwritingRecognizer
import org.nihongo.mochi.domain.services.AudioPlayer
import org.nihongo.mochi.domain.services.CloudSaveService
import org.nihongo.mochi.domain.services.TextToSpeech
import org.nihongo.mochi.services.AndroidAudioPlayer
import org.nihongo.mochi.services.AndroidCloudSaveService
import org.nihongo.mochi.services.AndroidTextToSpeech
import org.nihongo.mochi.ui.dictionary.AndroidMlKitRecognizer
import org.nihongo.mochi.ui.writinggame.AndroidTextNormalizer
import org.koin.core.qualifier.named

val platformModule = module {
    // Database
    single { DatabaseDriverFactory(androidContext()) }

    // Audio
    single<AudioPlayer> { AndroidAudioPlayer(androidContext()) }
    
    // Text To Speech
    single<TextToSpeech> { AndroidTextToSpeech(androidContext()) }

    // Shared Settings (legacy)
    single<Settings> {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }

    // Specific settings for ScoreManager
    single<Settings>(named("scoresSettings")) {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("KanjiScores", Context.MODE_PRIVATE)
        )
    }
    
    single<Settings>(named("userListSettings")) {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("UserLists", Context.MODE_PRIVATE)
        )
    }
    
    single<Settings>(named("appSettings")) {
        SharedPreferencesSettings(
            androidContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        )
    }
    
    // Handwriting Recognition implementation (ML Kit)
    single<HandwritingRecognizer> { AndroidMlKitRecognizer() }
    
    // Text Normalizer implementation
    single<TextNormalizer> { AndroidTextNormalizer() }

    // Cloud Save Service implementation (Google Play Games)
    factory<CloudSaveService> { (activity: Activity) ->
        AndroidCloudSaveService(activity)
    }
}
