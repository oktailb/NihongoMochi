package org.nihongo.mochi.ui.writingrecap

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.domain.kanji.KanjiEntry
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.ui.theme.AppTheme

class WritingRecapViewModel(
    private val levelContentProvider: LevelContentProvider,
    private val kanjiRepository: KanjiRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _kanjiList = MutableStateFlow<List<Pair<KanjiEntry, Color>>>(emptyList())
    val kanjiList: StateFlow<List<Pair<KanjiEntry, Color>>> = _kanjiList.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val pageSize = 80
    private var allKanjiEntries: List<KanjiEntry> = emptyList()

    fun loadLevel(levelKey: String) {
        viewModelScope.launch {
            val characters = levelContentProvider.getCharactersForLevel(levelKey)
            allKanjiEntries = characters.mapNotNull { kanjiRepository.getKanjiByCharacter(it) }
            
            _totalPages.value = if (allKanjiEntries.isEmpty()) 0 else (allKanjiEntries.size + pageSize - 1) / pageSize
            updateCurrentPageItems()
        }
    }

    fun nextPage() {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems()
        }
    }

    fun prevPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems()
        }
    }

    private fun updateCurrentPageItems() {
        val startIndex = _currentPage.value * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(allKanjiEntries.size)
        
        if (startIndex < allKanjiEntries.size) {
            val pageItems = allKanjiEntries.subList(startIndex, endIndex)
            _kanjiList.value = pageItems.map { kanji ->
                val score = ScoreManager.getScore(kanji.character, ScoreType.WRITING)
                val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
                kanji to Color(colorInt)
            }
        } else {
             _kanjiList.value = emptyList()
        }
    }
}

class WritingRecapFragment : Fragment() {

    private val args: WritingRecapFragmentArgs by navArgs()
    private val levelContentProvider: LevelContentProvider by inject()
    private val kanjiRepository: KanjiRepository by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            
            setContent {
                AppTheme {
                    val baseColor = ContextCompat.getColor(context, R.color.recap_grid_base_color)
                    val viewModel: WritingRecapViewModel = viewModel {
                        WritingRecapViewModel(levelContentProvider, kanjiRepository, baseColor)
                    }

                    // Initial load
                    androidx.compose.runtime.LaunchedEffect(args.level) {
                        viewModel.loadLevel(args.level)
                    }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.loadLevel(args.level)
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                             lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val kanjiList by viewModel.kanjiList.collectAsState()
                    val currentPage by viewModel.currentPage.collectAsState()
                    val totalPages by viewModel.totalPages.collectAsState()
                    
                    // Try to resolve human readable title for the level key if possible
                    val title = resolveLevelTitle(args.level)

                    WritingRecapScreen(
                        levelTitle = title,
                        kanjiListWithColors = kanjiList,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onKanjiClick = { kanjiEntry ->
                            val action = WritingRecapFragmentDirections.actionWritingRecapToKanjiDetail(kanjiEntry.id)
                            findNavController().navigate(action)
                        },
                        onPrevPage = { viewModel.prevPage() },
                        onNextPage = { viewModel.nextPage() },
                        onPlayClick = {
                            val action = WritingRecapFragmentDirections.actionWritingRecapToWritingGame(args.level, null)
                            findNavController().navigate(action)
                        }
                    )
                }
            }
        }
    }
    
    private fun resolveLevelTitle(key: String): String {
        // Try to map key (e.g. "n5", "grade1") to string resource (e.g. "level_n5", "level_grade_1")
        // If exact match fails, try adding prefixes.
        val context = requireContext()
        var resId = context.resources.getIdentifier(key, "string", context.packageName)
        if (resId != 0) return context.getString(resId)
        
        // Try level_ prefix
        resId = context.resources.getIdentifier("level_$key", "string", context.packageName)
        if (resId != 0) return context.getString(resId)
        
        // Try section_ prefix (less likely but possible)
        resId = context.resources.getIdentifier("section_$key", "string", context.packageName)
        if (resId != 0) return context.getString(resId)
        
        return key
    }
}
