package org.nihongo.mochi.ui.gojuon

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.domain.kana.KanaEntry
import org.nihongo.mochi.domain.kana.KanaRepository
import org.nihongo.mochi.presentation.ScorePresentationUtils
import org.nihongo.mochi.ui.theme.AppTheme

class KanaRecapViewModel(
    private val kanaRepository: KanaRepository,
    private val baseColorInt: Int
) : ViewModel() {

    private val _charactersByLine = MutableStateFlow<Map<Int, List<KanaEntry>>>(emptyMap())
    val charactersByLine: StateFlow<Map<Int, List<KanaEntry>>> = _charactersByLine.asStateFlow()

    private val _linesToShow = MutableStateFlow<List<Int>>(emptyList())
    val linesToShow: StateFlow<List<Int>> = _linesToShow.asStateFlow()
    
    private val _kanaColors = MutableStateFlow<Map<String, Color>>(emptyMap())
    val kanaColors: StateFlow<Map<String, Color>> = _kanaColors.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _totalPages = MutableStateFlow(0)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()
    
    private var allLineKeys: List<Int> = emptyList()

    fun loadKana(kanaType: org.nihongo.mochi.domain.kana.KanaType, pageSize: Int) {
        viewModelScope.launch {
            val allCharacters = kanaRepository.getKanaEntries(kanaType)
            val grouped = allCharacters.groupBy { it.line }.toSortedMap()
            _charactersByLine.value = grouped
            allLineKeys = grouped.keys.toList()
            
            _totalPages.value = if (allLineKeys.isEmpty()) 0 else (allLineKeys.size + pageSize - 1) / pageSize
            
            // Adjust current page if needed (e.g. rotation changing page size)
             if (_currentPage.value >= _totalPages.value) {
                _currentPage.value = (_totalPages.value - 1).coerceAtLeast(0)
            }
            
            updateCurrentPageItems(pageSize)
        }
    }
    
    fun refreshScores() {
         val currentChars = _charactersByLine.value.values.flatten()
         val colorMap = currentChars.associate { kana ->
            val score = ScoreManager.getScore(kana.character, ScoreManager.ScoreType.RECOGNITION)
            val colorInt = ScorePresentationUtils.getScoreColor(score, baseColorInt)
            kana.character to Color(colorInt)
         }
         _kanaColors.value = colorMap
    }

    fun nextPage(pageSize: Int) {
        if (_currentPage.value < _totalPages.value - 1) {
            _currentPage.value++
            updateCurrentPageItems(pageSize)
        }
    }

    fun prevPage(pageSize: Int) {
        if (_currentPage.value > 0) {
            _currentPage.value--
            updateCurrentPageItems(pageSize)
        }
    }

    private fun updateCurrentPageItems(pageSize: Int) {
        val startLineIndex = _currentPage.value * pageSize
        val endLineIndex = (startLineIndex + pageSize).coerceAtMost(allLineKeys.size)
        
        if (startLineIndex < allLineKeys.size) {
            _linesToShow.value = allLineKeys.subList(startLineIndex, endLineIndex)
        } else {
             _linesToShow.value = emptyList()
        }
        refreshScores()
    }
}

abstract class BaseKanaFragment : Fragment() {

    abstract val kanaType: KanaFragmentType
    private val kanaRepository: KanaRepository by inject()

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
                    val viewModel: KanaRecapViewModel = viewModel {
                        KanaRecapViewModel(kanaRepository, baseColor)
                    }
                    
                    val configuration = LocalConfiguration.current
                    val pageSize = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) 8 else 16

                    // Initial load
                    androidx.compose.runtime.LaunchedEffect(kanaType, pageSize) {
                        viewModel.loadKana(kanaType.domainType, pageSize)
                    }
                    
                    val lifecycleOwner = LocalLifecycleOwner.current
                    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                viewModel.refreshScores()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                             lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val charactersByLine by viewModel.charactersByLine.collectAsState()
                    val linesToShow by viewModel.linesToShow.collectAsState()
                    val kanaColors by viewModel.kanaColors.collectAsState()
                    val currentPage by viewModel.currentPage.collectAsState()
                    val totalPages by viewModel.totalPages.collectAsState()

                    KanaRecapScreen(
                        title = getString(kanaType.titleRes),
                        kanaListWithColors = emptyList(), // Not used in this specific screen variant
                        linesToShow = linesToShow,
                        charactersByLine = charactersByLine,
                        kanaColors = kanaColors,
                        currentPage = currentPage,
                        totalPages = totalPages,
                        onPrevPage = { viewModel.prevPage(pageSize) },
                        onNextPage = { viewModel.nextPage(pageSize) },
                        onPlayClick = {
                            findNavController().navigate(kanaType.navigationActionId)
                        }
                    )
                }
            }
        }
    }
}

enum class KanaFragmentType(val domainType: org.nihongo.mochi.domain.kana.KanaType, val titleRes: Int, val navigationActionId: Int) {
    HIRAGANA(org.nihongo.mochi.domain.kana.KanaType.HIRAGANA, R.string.level_hiragana, R.id.action_nav_hiragana_to_hiragana_quiz),
    KATAKANA(org.nihongo.mochi.domain.kana.KanaType.KATAKANA, R.string.level_katakana, R.id.action_nav_katakana_to_katakana_quiz)
}
