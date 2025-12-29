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
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import com.google.android.gms.games.SnapshotsClient
import com.google.android.gms.games.snapshot.SnapshotMetadata
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentResultsBinding
import org.nihongo.mochi.domain.statistics.ResultsViewModel
import org.nihongo.mochi.domain.statistics.StatisticsEngine
import org.nihongo.mochi.domain.statistics.StatisticsType
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.SagaAction
import org.nihongo.mochi.presentation.SagaMapScreen
import org.nihongo.mochi.services.AndroidCloudSaveService
import org.nihongo.mochi.ui.theme.AppTheme // Import du nouveau thème

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    
    private val levelContentProvider: LevelContentProvider by inject()

    private lateinit var statisticsEngine: StatisticsEngine
    private lateinit var androidCloudSaveService: AndroidCloudSaveService

    private val viewModel: ResultsViewModel by viewModels {
        viewModelFactory {
            initializer<ResultsViewModel> {
                val saveService = AndroidCloudSaveService(requireActivity())
                val statsEngine = StatisticsEngine(get())
                ResultsViewModel(saveService, statsEngine)
            }
        }
    }

    private val achievementsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Log.d("ResultsFragment", "Achievements activity returned OK.")
        }
    }

    private val savedGamesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val intent = result.data!!
            if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)) {
                val snapshotMetadata = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA, SnapshotMetadata::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(SnapshotsClient.EXTRA_SNAPSHOT_METADATA)
                }
                
                if (snapshotMetadata != null) {
                    viewModel.setCurrentSaveName(snapshotMetadata.uniqueName)
                    lifecycleScope.launch {
                        val data = androidCloudSaveService.loadGame(snapshotMetadata.uniqueName)
                        if (data != null) {
                            viewModel.loadGame(data)
                        }
                    }
                }
            } else if (intent.hasExtra(SnapshotsClient.EXTRA_SNAPSHOT_NEW)) {
                val unique = java.math.BigInteger(281, java.util.Random()).toString(13)
                viewModel.setCurrentSaveName("NihongoMochiSnapshot-$unique")
                viewModel.saveGame()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        if (!::statisticsEngine.isInitialized) {
             statisticsEngine = StatisticsEngine(levelContentProvider)
        }
        if (!::androidCloudSaveService.isInitialized) {
            androidCloudSaveService = AndroidCloudSaveService(requireActivity())
        }

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    SagaMapScreen(
                        viewModel = viewModel,
                        onNodeClick = { nodeId, type ->
                            navigateToGame(nodeId, type)
                        },
                        onAction = { action ->
                            handleSagaAction(action)
                        }
                    )
                }
            }
        }
        
        setupObservers()
    }
    
    private fun handleSagaAction(action: SagaAction) {
        when(action) {
            SagaAction.SIGN_IN -> viewModel.signIn()
            SagaAction.ACHIEVEMENTS -> showAchievements()
            SagaAction.BACKUP -> showSavedGamesUI()
            SagaAction.RESTORE -> showSavedGamesUI()
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
                    findNavController().navigate(R.id.nav_recognition, bundle)
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
            }
        } catch (e: Exception) {
            Log.e("ResultsFragment", "Navigation failed: ${e.message}")
            Toast.makeText(requireContext(), "Coming soon or Navigation Error", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // isAuthenticated flow is now observed inside Compose screen via ViewModel
                launch {
                    viewModel.message.collect { msg ->
                        if (msg != null) {
                            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                            viewModel.clearMessage()
                        }
                    }
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
                 val intent = androidCloudSaveService.getAchievementsIntent()
                 achievementsLauncher.launch(intent)
             } catch (e: Exception) {
                 Toast.makeText(requireContext(), getString(R.string.about_coming_soon) + ": " + e.message, Toast.LENGTH_SHORT).show()
             }
        }
    }

    private fun showSavedGamesUI() {
        lifecycleScope.launch {
            try {
                val intent = androidCloudSaveService.getSavedGamesIntent(
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
}
