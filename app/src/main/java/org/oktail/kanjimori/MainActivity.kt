package org.oktail.kanjimori

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.google.android.gms.games.GamesSignInClient
import com.google.android.gms.games.PlayGames
import org.oktail.kanjimori.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var gamesSignInClient: GamesSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)

        gamesSignInClient = PlayGames.getGamesSignInClient(this)
    }

    override fun onResume() {
        super.onResume()
        checkSignInStatus()
    }

    private fun checkSignInStatus() {
        gamesSignInClient.isAuthenticated.addOnCompleteListener { isAuthenticatedTask ->
            if (isAuthenticatedTask.isSuccessful && isAuthenticatedTask.result.isAuthenticated) {
                Log.d("MainActivity", "Google Play Games sign-in successful.")
                // The player is signed in. You can now use the Games SDK.
            } else {
                Log.d("MainActivity", "Google Play Games sign-in failed or not authenticated.")
                // Player could not be signed in.
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
