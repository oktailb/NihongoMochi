package org.nihongo.mochi.ui.writing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.presentation.writing.WritingViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class WritingFragment : Fragment() {

    private val viewModel: WritingViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    val categories by viewModel.writingCategories.collectAsState()
                    val userListInfo by viewModel.userListInfo.collectAsState()
                    
                    WritingScreen(
                        categories = categories,
                        userListInfo = userListInfo,
                        onLevelClick = { levelKey ->
                            onLevelClicked(levelKey)
                        }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.refreshStatistics()
            }
        }
    }

    private fun onLevelClicked(level: String) {
        val action = WritingFragmentDirections.actionNavWritingToWritingRecap(level)
        findNavController().navigate(action)
    }
}
