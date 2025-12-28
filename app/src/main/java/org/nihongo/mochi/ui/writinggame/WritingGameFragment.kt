package org.nihongo.mochi.ui.writinggame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentWritingGameBinding
import org.nihongo.mochi.domain.game.QuestionType
import org.nihongo.mochi.domain.game.WritingGameViewModel
import org.nihongo.mochi.domain.kana.KanaUtils
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.GameState
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.Reading
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.util.LevelContentProvider
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY

class WritingGameFragment : Fragment() {

    private var _binding: FragmentWritingGameBinding? = null
    private val binding get() = _binding!!
    private val args: WritingGameFragmentArgs by navArgs()
    
    private val levelContentProvider: LevelContentProvider by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val meaningRepository: MeaningRepository by inject()
    private val kanjiRepository: KanjiRepository by inject()
    
    private val viewModel: WritingGameViewModel by viewModels {
        viewModelFactory {
            initializer {
                WritingGameViewModel()
            }
        }
    }

    private lateinit var sharedPreferences: SharedPreferences

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            if (s == null) return
            // Only convert to kana if we are in READING mode
            if (viewModel.currentQuestionType == QuestionType.READING) {
                val input = s.toString()
                val replacement = RomajiToKana.checkReplacement(input)
                if (replacement != null) {
                    val (suffixLen, kana) = replacement
                    val start = input.length - suffixLen
                    val end = input.length
                    
                    // Prevent infinite loop by removing listener during modification
                    binding.editTextAnswer.removeTextChangedListener(this)
                    s.replace(start, end, kana)
                    binding.editTextAnswer.addTextChangedListener(this)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWritingGameBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        viewModel.setAnimationSpeed(animationSpeed)
        
        if (!viewModel.isGameInitialized) {
            initializeGame()
        }

        setupStateObservation()

        binding.buttonSubmitAnswer.setOnClickListener { checkAnswer() }
        binding.editTextAnswer.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                checkAnswer()
                true
            } else {
                false
            }
        }
        
        binding.editTextAnswer.addTextChangedListener(textWatcher)
    }
    
    private fun setupStateObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    renderState(state)
                }
            }
        }
    }

    private fun renderState(state: GameState) {
        when(state) {
            GameState.Loading -> { 
                // Could show loading indicator
            }
            GameState.WaitingForAnswer -> {
                displayQuestion()
            }
            is GameState.ShowingResult -> {
                showResult(state.isCorrect)
            }
            GameState.Finished -> {
                findNavController().popBackStack()
            }
        }
    }

    private fun initializeGame() {
        val level = args.level ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            loadAllKanjiDetails()
            val kanjiCharsForLevel = levelContentProvider.getCharactersForLevel(level)

            viewModel.allKanjiDetails.clear()
            viewModel.allKanjiDetails.addAll(
                viewModel.allKanjiDetailsXml.filter { kanjiCharsForLevel.contains(it.character) }
            )
            viewModel.allKanjiDetails.shuffle()
            viewModel.kanjiListPosition = 0

            if (viewModel.allKanjiDetails.isNotEmpty()) {
                viewModel.startGame()
                viewModel.isGameInitialized = true
            } else {
                Log.e("WritingGameFragment", "No kanji loaded for level: $level.")
                findNavController().popBackStack()
            }
        }
    }
    
    private fun displayQuestion() {
        binding.textKanjiToGuess.text = viewModel.currentKanji.character
        binding.editTextAnswer.text.clear()
        
        // Ensure editable state
        binding.editTextAnswer.isEnabled = true
        binding.buttonSubmitAnswer.isEnabled = true
        binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.leaf_green))
        binding.layoutCorrectionFeedback.visibility = View.INVISIBLE

        binding.textQuestionLabel.text = if (viewModel.currentQuestionType == QuestionType.MEANING) {
            getString(R.string.game_writing_label_meaning)
        } else {
            getString(R.string.game_writing_label_reading)
        }

        // request focus and show keyboard if possible
        binding.editTextAnswer.requestFocus()
        
        updateProgressBar()
    }

    private fun checkAnswer() {
        val userAnswer = binding.editTextAnswer.text.toString()
        viewModel.submitAnswer(userAnswer)
        // Disable UI immediately, state update will follow
        binding.buttonSubmitAnswer.isEnabled = false
    }
    
    private fun showResult(isCorrect: Boolean) {
        binding.buttonSubmitAnswer.isEnabled = false

        if (isCorrect) {
            val status = viewModel.kanjiStatus[viewModel.currentKanji]

            if (status == GameStatus.CORRECT) {
                binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
            } else {
                binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
            }
            
            // Delay is handled by engine in shared code
        } else {
            binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_incorrect))
            showCorrectionFeedbackUI()
        }

        updateProgressBar()
    }
    
    private fun showCorrectionFeedbackUI() {
        binding.layoutCorrectionFeedback.visibility = View.VISIBLE
        
        if (viewModel.currentQuestionType == QuestionType.MEANING) {
            // Show simple meaning text
            binding.textCorrectionSimple.visibility = View.VISIBLE
            binding.layoutReadingsColumns.visibility = View.GONE
            
            val meaningsText = viewModel.currentKanji.meanings.joinToString(", ")
            binding.textCorrectionSimple.text = meaningsText
        } else {
            // Show Readings Columns
            binding.textCorrectionSimple.visibility = View.GONE
            binding.layoutReadingsColumns.visibility = View.VISIBLE
            
            val onReadings = viewModel.currentKanji.readings.filter { it.type == "on" }.joinToString("\n") { KanaUtils.hiraganaToKatakana(it.value) }
            val kunReadings = viewModel.currentKanji.readings.filter { it.type == "kun" }.joinToString("\n") { it.value }
            
            binding.textReadingsOn.text = if (onReadings.isNotEmpty()) onReadings else "-"
            binding.textReadingsKun.text = if (kunReadings.isNotEmpty()) kunReadings else "-"
        }
    }

    private fun updateProgressBar() {
        val progressIndicators = (0 until binding.progressBarGame.childCount).map {
            binding.progressBarGame.getChildAt(it) as ImageView
        }

        for (i in 0 until 10) {
            if (i < viewModel.currentKanjiSet.size) {
                val kanji = viewModel.currentKanjiSet[i]
                val status = viewModel.kanjiStatus[kanji]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                
                // Reset basic state
                indicator.clearColorFilter()
                
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    GameStatus.PARTIAL -> {
                        // Use a clock icon to symbolize "in progress" / hourglass, tinted Orange
                        indicator.setImageResource(android.R.drawable.ic_menu_recent_history)
                        indicator.setColorFilter(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
                    }
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    private fun loadMeanings(): Map<String, List<String>> {
        val locale = settingsRepository.getAppLocale()
        return meaningRepository.getMeanings(locale)
    }

    private fun loadAllKanjiDetails() {
        val meanings = loadMeanings()
        
        // Load details from Shared Repository (JSON)
        val allKanjiEntries = kanjiRepository.getAllKanji()
        
        viewModel.allKanjiDetailsXml.clear()
        
        for (entry in allKanjiEntries) {
            val id = entry.id
            val character = entry.character
            val kanjiMeanings = meanings[id] ?: emptyList()
            
            // Map JSON readings to UI Readings
            val readingsList = mutableListOf<Reading>()
            entry.readings?.reading?.forEach { readingEntry ->
                 val freq = readingEntry.frequency?.toIntOrNull() ?: 0
                 readingsList.add(Reading(readingEntry.value, readingEntry.type, freq))
            }
            
            val kanjiDetail = KanjiDetail(id, character, kanjiMeanings, readingsList)
            viewModel.allKanjiDetailsXml.add(kanjiDetail)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
