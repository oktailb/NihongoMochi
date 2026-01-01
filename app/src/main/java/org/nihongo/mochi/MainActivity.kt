package org.nihongo.mochi

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
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var gamesSignInClient: GamesSignInClient
    
    private val settingsRepository: SettingsRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply stored theme
        val savedTheme = settingsRepository.getTheme()
        val nightMode = if (savedTheme == "dark") {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            // Default to system if not set or "light" (assuming default is light/system)
            // If you want to force light when "light" is stored:
            AppCompatDelegate.MODE_NIGHT_NO
        }
        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
        
        // Apply stored locale from SettingsRepository
        val savedLocale = settingsRepository.getAppLocale()
        val localeTag = savedLocale.replace('_', '-')
        
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != localeTag) {
             val appLocale = LocaleListCompat.forLanguageTags(localeTag)
             AppCompatDelegate.setApplicationLocales(appLocale)
        }

        setContentView(R.layout.activity_main)

        // Ensure NavHostFragment is properly set up
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment
        if (navHostFragment == null) {
            Log.e("MainActivity", "NavHostFragment not found!")
        }

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
        
        setupWorkers()
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

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // No AppBarConfiguration needed anymore as we don't have an ActionBar
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
