package org.nihongo.mochi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.nihongo.mochi.databinding.ActivityMainBinding
import org.nihongo.mochi.workers.DecayWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var gamesSignInClient: GamesSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply stored locale from SettingsRepository
        val settingsRepo = MochiApplication.settingsRepository
        val savedLocale = settingsRepo.getAppLocale()
        val localeTag = savedLocale.replace('_', '-')
        
        if (AppCompatDelegate.getApplicationLocales().toLanguageTags() != localeTag) {
             val appLocale = LocaleListCompat.forLanguageTags(localeTag)
             AppCompatDelegate.setApplicationLocales(appLocale)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

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
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
