package org.nihongo.mochi.ui.results

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadata
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.nihongo.mochi.R
import org.nihongo.mochi.domain.statistics.OneTimeEvent
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.presentation.SagaMapScreen
import org.nihongo.mochi.services.AndroidCloudSaveService
import org.nihongo.mochi.ui.theme.AppTheme

class ResultsFragment : Fragment() {

    private val viewModel: ResultsViewModel by viewModel { 
        parametersOf(AndroidCloudSaveService(requireActivity())) 
    }

    private val achievementsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("ResultsFragment", "Achievements activity returned OK.")
        }
    }

    private val savedGamesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = result.data!!
            val snapshotName = if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                val snapshotMetadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA, SnapshotMetadata::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)
                }
                snapshotMetadata?.uniqueName
            } else { null }
            
            val isNew = intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)
            
            viewModel.handleSnapshotResult(snapshotName, isNew)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    // Handle OneTimeEvents from the ViewModel
                    LaunchedEffect(viewModel) {
                        viewModel.oneTimeEvent.collectLatest { event ->
                            when(event) {
                                is OneTimeEvent.ShowAchievements -> showAchievements()
                                is OneTimeEvent.ShowSavedGames -> showSavedGamesUI()
                            }
                        }
                    }
                    
                    // Show toast messages from the ViewModel
                    LaunchedEffect(viewModel) {
                         viewModel.message.collectLatest { msg ->
                            if (msg != null) {
                                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                                viewModel.clearMessage()
                            }
                        }
                    }

                    SagaMapScreen(
                        viewModel = viewModel,
                        onNodeClick = { nodeId, type -> navigateToGame(nodeId, type) },
                        onAction = { action -> viewModel.handleSagaAction(action) }
                    )
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        viewModel.checkSignInStatus()
    }

    private fun showAchievements() {
        lifecycleScope.launch {
             try {
                 val service = AndroidCloudSaveService(requireActivity())
                 val intent = service.getAchievementsIntent()
                 achievementsLauncher.launch(intent)
             } catch (e: Exception) {
                 Toast.makeText(requireContext(), getString(R.string.about_coming_soon) + ": " + e.message, Toast.LENGTH_SHORT).show()
             }
        }
    }

    private fun showSavedGamesUI() {
        lifecycleScope.launch {
            try {
                val service = AndroidCloudSaveService(requireActivity())
                val intent = service.getSavedGamesIntent(
                    "Sauvegardes", 
                    allowAdd = true, 
                    allowDelete = true, 
                    maxSnapshots = 5
                )
                savedGamesLauncher.launch(intent)
            } catch (e: Exception) {
                 Toast.makeText(requireContext(), getString(R.string.about_coming_soon) + ": " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToGame(levelId: String, type: StatisticsType) {
        try {
            when(type) {
                StatisticsType.RECOGNITION -> {
                    val bundle = Bundle().apply {
                        putString("level", levelId)
                        putString("gameMode", "meaning") 
                        putString("readingMode", "Hiragana")
                    }
                    findNavController().navigate(R.id.nav_game_recap, bundle)
                }
                StatisticsType.WRITING -> {
                     val bundle = Bundle().apply {
                        putString("level", levelId)
                    }
                    findNavController().navigate(R.id.nav_writing_recap, bundle)
                }
                StatisticsType.READING -> {
                    val bundle = Bundle().apply {
                        putString("wordList", levelId)
                    }
                    findNavController().navigate(R.id.nav_word_list, bundle)
                }
                StatisticsType.GRAMMAR -> {
                    Toast.makeText(requireContext(), "Grammar exercises coming soon!", Toast.LENGTH_SHORT).show()
                }
                StatisticsType.GAMES -> {
                    Toast.makeText(requireContext(), "Mini-games coming soon!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Log.e("ResultsFragment", "Navigation failed: ${e.message}")
            Toast.makeText(requireContext(), "Coming soon or Navigation Error", Toast.LENGTH_SHORT).show()
        }
    }
}
