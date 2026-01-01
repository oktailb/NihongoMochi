package org.nihongo.mochi.ui.reading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.reading.ReadingViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class ReadingFragment : Fragment() {

    private val viewModel: ReadingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val state by viewModel.state.collectAsState()
                    
                    // We need to ensure userListInfo has the correct display name from Android resources
                    // The ViewModel's state might have an empty string initially or after recomposition if not refreshed
                    // However, we call refreshData in onResume/onViewCreated.
                    
                    // Since we can't easily modify the state inside the composable without triggering loop,
                    // we rely on the ViewModel having the correct data via refreshData.

                    ReadingScreen(
                        jlptLevels = state.jlptLevels,
                        wordLevels = state.wordLevels,
                        userListInfo = state.userListInfo,
                        onLevelClick = { levelId ->
                            navigateToWordList(levelId)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshViewModelData()
    }
    
    override fun onResume() {
        super.onResume()
        refreshViewModelData()
    }
    
    private fun refreshViewModelData() {
        val userListLabel = getString(R.string.reading_user_list)
        viewModel.refreshData { userListLabel }
    }

    private fun navigateToWordList(listId: String) {
        val action = ReadingFragmentDirections.actionNavReadingToWordList(listId)
        findNavController().navigate(action)
    }
}
