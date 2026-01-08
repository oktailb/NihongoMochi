package org.nihongo.mochi

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.koin.android.ext.android.inject
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.workers.DecayWorker
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gamesSignInClient: GamesSignInClient
    private val settingsRepository: SettingsRepository by inject()

    override fun attachBaseContext(newBase: Context) {
        // NUCLEAR FIX for Android 9 "One step behind" issue.
        // We intercept the context CREATION. This happens before onCreate.
        // We read the synchronous preference we saved in SettingsFragment.
        val prefs = newBase.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val localeCode = prefs.getString("AppLocale", "en_GB") ?: "en_GB"
        
        // Parse locale (e.g., "en_GB" -> "en" + "GB")
        val parts = localeCode.split("_")
        val language = parts.getOrElse(0) { "en" }
        val country = parts.getOrElse(1) { "" }
        val locale = if (country.isNotEmpty()) Locale(language, country) else Locale(language)

        // Force system defaults immediately
        Locale.setDefault(locale)
        
        // Create a new configuration with this locale
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        
        // Create the wrapped context
        val context = newBase.createConfigurationContext(config)
        
        // Pass the WRAPPED context to super. This ensures all Inflaters use the new language.
        super.attachBaseContext(context)
        
        // Also tell AppCompat, just in case, but the Context wrap does the heavy lifting
        val localeList = LocaleListCompat.create(locale)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Standard Sync logic for repository
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

        // Apply stored theme
        val savedTheme = settingsRepository.getTheme()
        val nightMode = if (savedTheme == "dark") {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment not found!")
        }

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
        setupWorkers()
    }
    
    // ... workers and other methods remain unchanged
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
