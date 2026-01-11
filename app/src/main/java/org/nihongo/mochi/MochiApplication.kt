package org.nihongo.mochi

import android.app.Application
import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.di.platformModule
import org.nihongo.mochi.di.sharedModule
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.domain.words.WordRepository

class MochiApplication : Application() {

    companion object {
        lateinit var kanaRepository: KanaRepository
            private set
        lateinit var kanjiRepository: KanjiRepository
            private set
        lateinit var wordRepository: WordRepository
            private set
        lateinit var meaningRepository: MeaningRepository
            private set
        lateinit var settingsRepository: SettingsRepository
            private set
        lateinit var levelContentProvider: LevelContentProvider
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Init Koin
        startKoin {
            androidLogger()
            androidContext(this@MochiApplication)
            // Load platformModule (Android-specific from shared) and sharedModule
            modules(platformModule, sharedModule)
        }
        
        // Init Settings
        val scoresSettings = SharedPreferencesSettings(getSharedPreferences("scores", Context.MODE_PRIVATE))
        val userListSettings = SharedPreferencesSettings(getSharedPreferences("user_lists", Context.MODE_PRIVATE))
        val appSettings = SharedPreferencesSettings(getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
        
        ScoreManager.init(scoresSettings, userListSettings, appSettings)
        
        // Init Domain Repositories via Koin
        kanaRepository = get()
        kanjiRepository = get()
        wordRepository = get()
        meaningRepository = get()
        settingsRepository = get()
        levelContentProvider = get()

        // Init Services depending on repositories
        KanaToRomaji.init(kanaRepository)
        RomajiToKana.init(kanaRepository)
    }
}
