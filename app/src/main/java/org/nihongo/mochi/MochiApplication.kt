package org.nihongo.mochi

import android.app.Application
import android.content.Context
import com.russhwolf.settings.SharedPreferencesSettings
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.AndroidResourceLoader
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.domain.kana.KanaToRomaji
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
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
    }

    override fun onCreate() {
        super.onCreate()
        
        // Init Settings
        val scoresSettings = SharedPreferencesSettings(getSharedPreferences("scores", Context.MODE_PRIVATE))
        val userListSettings = SharedPreferencesSettings(getSharedPreferences("user_lists", Context.MODE_PRIVATE))
        // Changed "settings" to "AppSettings" to match previous Android implementation and share data
        val appSettings = SharedPreferencesSettings(getSharedPreferences("AppSettings", Context.MODE_PRIVATE))
        
        ScoreManager.init(scoresSettings, userListSettings, appSettings)
        
        settingsRepository = SettingsRepository(appSettings)

        // Init Domain Repositories
        val resourceLoader = AndroidResourceLoader(this)
        
        kanaRepository = KanaRepository(resourceLoader)
        kanjiRepository = KanjiRepository(resourceLoader)
        wordRepository = WordRepository(resourceLoader)
        meaningRepository = MeaningRepository(resourceLoader)
        
        // Init Services depending on repositories
        KanaToRomaji.init(kanaRepository)
        RomajiToKana.init(kanaRepository)
    }
}
