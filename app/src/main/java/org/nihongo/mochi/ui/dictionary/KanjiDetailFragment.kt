package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class KanjiDetailFragment : Fragment() {

    private val args: KanjiDetailFragmentArgs by navArgs()
    private val viewModel: KanjiDetailViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    KanjiDetailScreen(
                        viewModel = viewModel,
                        onKanjiClick = { kanjiChar -> navigateToKanji(kanjiChar) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load initial data
        args.kanjiId?.let { viewModel.loadKanji(it) }
    }

    private fun navigateToKanji(character: String) {
        // Navigation logic kept in Fragment as it depends on Android Navigation Component
        // ViewModel helper is used to find ID
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val id = viewModel.findKanjiIdByCharacter(character)
            withContext(Dispatchers.Main) {
                if (id != null) {
                    try {
                        val action = KanjiDetailFragmentDirections.actionKanjiDetailToKanjiDetail(id)
                        findNavController().navigate(action)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
