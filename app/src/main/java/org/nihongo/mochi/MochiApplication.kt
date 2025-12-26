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

class MochiApplication : Application() {

    companion object {
        lateinit var kanaRepository: KanaRepository
            private set
        lateinit var kanjiRepository: KanjiRepository
            private set
    }

    override fun onCreate() {
        super.onCreate()
        
        // Init Settings
        val scoresSettings = SharedPreferencesSettings(getSharedPreferences("scores", Context.MODE_PRIVATE))
        val userListSettings = SharedPreferencesSettings(getSharedPreferences("user_lists", Context.MODE_PRIVATE))
        val appSettings = SharedPreferencesSettings(getSharedPreferences("settings", Context.MODE_PRIVATE))
        
        ScoreManager.init(scoresSettings, userListSettings, appSettings)

        // Init Domain Repositories
        val resourceLoader = AndroidResourceLoader(this)
        
        kanaRepository = KanaRepository(resourceLoader)
        kanjiRepository = KanjiRepository(resourceLoader)
        
        // Init Services depending on repositories
        KanaToRomaji.init(kanaRepository)
        RomajiToKana.init(kanaRepository)
    }
}
