package org.nihongo.mochi.ui.wordlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.ui.theme.AppTheme

class WordListFragment : Fragment() {

    private val args: WordListFragmentArgs by navArgs()
    private val viewModel: WordListViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                AppTheme {
                    LaunchedEffect(args.wordList) {
                        viewModel.loadList(args.wordList)
                    }
                    WordListFragmentContent()
                }
            }
        }
    }

    @Composable
    private fun WordListFragmentContent() {
        val wordsWithScores by viewModel.displayedWords.collectAsState()
        val currentPage by viewModel.currentPage.collectAsState()
        val totalPages by viewModel.totalPages.collectAsState()
        
        val filterKanjiOnly by viewModel.filterKanjiOnly.collectAsState()
        val filterSimpleWords by viewModel.filterSimpleWords.collectAsState()
        val filterCompoundWords by viewModel.filterCompoundWords.collectAsState()
        val filterIgnoreKnown by viewModel.filterIgnoreKnown.collectAsState()
        val selectedWordType by viewModel.selectedWordType.collectAsState()
        val screenTitleKey by viewModel.screenTitleKey.collectAsState()
        
        val context = LocalContext.current
        val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)

        val wordsWithColors = wordsWithScores.map {
            val color = Color(ScorePresentationUtils.getScoreColor(it.second, baseColor))
            Triple(it.first, color, it.third)
        }
        
        val wordTypeOptions = listOf(
            "Tous" to context.getString(R.string.word_type_all),
            "和" to context.getString(R.string.word_type_wa),
            "固" to context.getString(R.string.word_type_ko),
            "外" to context.getString(R.string.word_type_gai),
            "混" to context.getString(R.string.word_type_kon),
            "漢" to context.getString(R.string.word_type_kan),
            "記号" to context.getString(R.string.word_type_kigo)
        )
        
        val listTitle = if (screenTitleKey != null) {
            val resId = context.resources.getIdentifier(screenTitleKey, "string", context.packageName)
            if (resId != 0) context.getString(resId) else args.wordList
        } else {
            args.wordList
        }

        WordListScreen(
            listTitle = listTitle,
            wordsWithColors = wordsWithColors,
            currentPage = currentPage,
            totalPages = totalPages,
            filterKanjiOnly = filterKanjiOnly,
            filterSimpleWords = filterSimpleWords,
            filterCompoundWords = filterCompoundWords,
            filterIgnoreKnown = filterIgnoreKnown,
            selectedWordType = selectedWordType,
            wordTypeOptions = wordTypeOptions,
            onFilterKanjiOnlyChange = { viewModel.setFilterKanjiOnly(it) },
            onFilterSimpleWordsChange = { viewModel.setFilterSimpleWords(it) },
            onFilterCompoundWordsChange = { viewModel.setFilterCompoundWords(it) },
            onFilterIgnoreKnownChange = { viewModel.setFilterIgnoreKnown(it) },
            onWordTypeChange = { viewModel.setWordType(it) },
            onPrevPage = { viewModel.prevPage() },
            onNextPage = { viewModel.nextPage() },
            onPlayClick = {
                val customWordList = viewModel.getGameWordList()
                val action = WordListFragmentDirections.actionNavWordListToNavWordQuiz(customWordList)
                findNavController().navigate(action)
            }
        )
    }
}
