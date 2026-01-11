package org.nihongo.mochi

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.koin.android.ext.android.inject
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.ui.navigation.MochiNavGraph
import org.nihongo.mochi.ui.theme.AppTheme
import org.nihongo.mochi.workers.DecayWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gamesSignInClient: GamesSignInClient
    private val settingsRepository: SettingsRepository by inject()

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val localeCode = prefs.getString("AppLocale", "en_GB") ?: "en_GB"
        
        val parts = localeCode.split("_")
        val language = parts.getOrElse(0) { "en" }
        val country = parts.getOrElse(1) { "" }
        val locale = if (country.isNotEmpty()) Locale(language, country) else Locale(language)

        Locale.setDefault(locale)
        
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
        
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Locale and Theme Sync
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        if (!currentAppLocales.isEmpty) {
            val primaryLocale = currentAppLocales.get(0)
            if (primaryLocale != null) {
                val tag = primaryLocale.toLanguageTag().replace('-', '_')
                if (settingsRepository.getAppLocale() != tag) {
                     settingsRepository.setAppLocale(tag)
                }
            }
        }

        val savedTheme = settingsRepository.getTheme()
        val nightMode = if (savedTheme == "dark") {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        setContent {
            val sdf = SimpleDateFormat("dd MMM. yyyy HH:mm:ss", Locale.getDefault())
            val currentDate = sdf.format(Date())

            AppTheme {
                val navController = rememberNavController()
                MochiNavGraph(
                    navController = navController,
                    versionName = BuildConfig.VERSION_NAME,
                    currentDate = currentDate,
                    onOpenUrl = { url -> openUrl(url) },
                    onThemeChanged = { isDark -> changeTheme(isDark) },
                    onLocaleChanged = { newLocale -> changeLocale(newLocale) }
                )
            }
        }

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
        setupWorkers()
    }

    private fun changeTheme(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun changeLocale(localeCode: String) {
        val prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("AppLocale", localeCode).commit()

        val localeTag = localeCode.replace('_', '-')
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
    
    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to open URL: $url", e)
        }
    }

    private fun setupWorkers() {
        val decayWorkRequest = PeriodicWorkRequestBuilder<DecayWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.DAYS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MochiDecayWork",
            ExistingPeriodicWorkPolicy.KEEP,
            decayWorkRequest
        )
    }

    override fun onResume() {
        super.onResume()
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            if (isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated) {
                Log.d("MainActivity", "Google Play Games sign-in successful.")
            } else {
                Log.d("MainActivity", "Google Play Games sign-in failed or not authenticated.")
            }
        }
    }
}
