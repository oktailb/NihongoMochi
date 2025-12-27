package org.nihongo.mochi.ui.writinggame

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.data.ScoreManager.ScoreType
import org.nihongo.mochi.databinding.FragmentWritingGameBinding
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.settings.ANIMATION_SPEED_PREF_KEY
import org.nihongo.mochi.domain.models.GameStatus
import org.nihongo.mochi.domain.models.KanjiDetail
import org.nihongo.mochi.domain.models.KanjiProgress
import org.nihongo.mochi.domain.models.Reading
import java.text.Normalizer
import java.util.Locale
import kotlin.math.max

class WritingGameFragment : Fragment() {

    private var _binding: FragmentWritingGameBinding? = null
    private val binding get() = _binding!!
    private val args: WritingGameFragmentArgs by navArgs()
    private val viewModel: WritingGameViewModel by viewModels()

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
        
        if (!viewModel.isGameInitialized) {
            initializeGame()
            viewModel.isGameInitialized = true
        } else {
             restoreUI()
        }

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
    
    private fun initializeGame() {
        val level = args.level ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            loadAllKanjiDetails()
            val kanjiCharsForLevel = loadKanjiCharsForLevel(level)

            viewModel.allKanjiDetails.clear()
            viewModel.allKanjiDetails.addAll(
                viewModel.allKanjiDetailsXml.filter { kanjiCharsForLevel.contains(it.character) }
            )
            viewModel.allKanjiDetails.shuffle()
            viewModel.kanjiListPosition = 0

            if (viewModel.allKanjiDetails.isNotEmpty()) {
                startNewSet()
            } else {
                Log.e("WritingGameFragment", "No kanji loaded for level: $level.")
                findNavController().popBackStack()
            }
        }
    }
    
    private fun restoreUI() {
        if (viewModel.allKanjiDetails.isEmpty()) return
        
        // Restore Question Display
        binding.textKanjiToGuess.text = viewModel.currentKanji.character
        binding.textQuestionLabel.text = if (viewModel.currentQuestionType == QuestionType.MEANING) {
            getString(R.string.game_writing_label_meaning)
        } else {
            getString(R.string.game_writing_label_reading)
        }
        
        // Restore Button State
        if (viewModel.isAnswerProcessing) {
             binding.buttonSubmitAnswer.isEnabled = false
             if (viewModel.lastAnswerStatus == true) {
                 binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
             } else if (viewModel.lastAnswerStatus == false) {
                 binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_incorrect))
             } else {
                 binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
             }
        } else {
             binding.buttonSubmitAnswer.isEnabled = true
             binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.leaf_green))
        }

        // Restore Feedback
        if (viewModel.showCorrectionFeedback) {
             showCorrectionFeedbackUI()
             
             if (viewModel.correctionDelayPending) {
                  Handler(Looper.getMainLooper()).postDelayed({
                      displayQuestion()
                  }, 2000)
                  viewModel.correctionDelayPending = false 
             }
        } else {
            binding.layoutCorrectionFeedback.visibility = View.INVISIBLE
        }
        
        updateProgressBar()
    }

    private fun startNewSet() {
        viewModel.revisionList.clear()
        viewModel.kanjiStatus.clear()
        viewModel.kanjiProgress.clear()

        if (viewModel.kanjiListPosition >= viewModel.allKanjiDetails.size) {
            findNavController().popBackStack()
            return
        }

        val nextSet = viewModel.allKanjiDetails.drop(viewModel.kanjiListPosition).take(10)
        viewModel.kanjiListPosition += nextSet.size

        viewModel.currentKanjiSet.clear()
        viewModel.currentKanjiSet.addAll(nextSet)
        viewModel.revisionList.addAll(nextSet)
        viewModel.currentKanjiSet.forEach {
            viewModel.kanjiStatus[it] = GameStatus.NOT_ANSWERED
            viewModel.kanjiProgress[it] = KanjiProgress()
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun displayQuestion() {
        if (viewModel.revisionList.isEmpty()) {
            startNewSet()
            return
        }

        viewModel.isAnswerProcessing = false
        viewModel.lastAnswerStatus = null
        viewModel.showCorrectionFeedback = false
        viewModel.correctionDelayPending = false
        
        viewModel.currentKanji = viewModel.revisionList.random()
        binding.textKanjiToGuess.text = viewModel.currentKanji.character
        binding.editTextAnswer.text.clear()
        // Ensure editable state
        binding.editTextAnswer.isEnabled = true
        binding.buttonSubmitAnswer.isEnabled = true
        binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.leaf_green))
        binding.layoutCorrectionFeedback.visibility = View.INVISIBLE

        // Determine question type based on what is missing
        val progress = viewModel.kanjiProgress[viewModel.currentKanji]!!
        viewModel.currentQuestionType = when {
            !progress.meaningSolved && !progress.readingSolved -> if (Math.random() < 0.5) QuestionType.MEANING else QuestionType.READING
            !progress.meaningSolved -> QuestionType.MEANING
            else -> QuestionType.READING // reading not solved
        }

        binding.textQuestionLabel.text = if (viewModel.currentQuestionType == QuestionType.MEANING) {
            getString(R.string.game_writing_label_meaning)
        } else {
            getString(R.string.game_writing_label_reading)
        }

        // request focus and show keyboard if possible
        binding.editTextAnswer.requestFocus()
    }

    private fun checkAnswer() {
        if (viewModel.isAnswerProcessing) return
        viewModel.isAnswerProcessing = true

        val userAnswer = binding.editTextAnswer.text.toString()
        val isCorrect = if (viewModel.currentQuestionType == QuestionType.MEANING) {
            checkMeaning(userAnswer)
        } else {
            checkReading(userAnswer)
        }

        ScoreManager.saveScore(viewModel.currentKanji.character, isCorrect, ScoreType.WRITING)

        // Do NOT disable editTextAnswer to keep keyboard open
        binding.buttonSubmitAnswer.isEnabled = false

        if (isCorrect) {
            val progress = viewModel.kanjiProgress[viewModel.currentKanji]!!
            if (viewModel.currentQuestionType == QuestionType.MEANING) progress.meaningSolved = true
            else progress.readingSolved = true

            if (progress.meaningSolved && progress.readingSolved) {
                // Fully solved
                binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
                viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.CORRECT
                viewModel.revisionList.remove(viewModel.currentKanji)
                viewModel.lastAnswerStatus = true
            } else {
                // Partially solved
                binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
                viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.PARTIAL
                viewModel.lastAnswerStatus = null
            }

            val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
            val delay = (1000 * animationSpeed).toLong()
            
            viewModel.correctionDelayPending = true
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) displayQuestion() // Check isAdded to avoid crash if fragment destroyed
            }, delay)

        } else {
            binding.buttonSubmitAnswer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_incorrect))
            viewModel.kanjiStatus[viewModel.currentKanji] = GameStatus.INCORRECT
            viewModel.lastAnswerStatus = false
            viewModel.showCorrectionFeedback = true

            // Setup Correction Feedback
            var delayMs = showCorrectionFeedbackUI()
            
            // Limit max delay to avoid waiting too long (e.g., 6 seconds max)
            delayMs = delayMs.coerceAtMost(6000L)

            val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
            val finalDelay = (delayMs * animationSpeed).toLong()
            
            viewModel.correctionDelayPending = true
            Handler(Looper.getMainLooper()).postDelayed({
                if (isAdded) displayQuestion()
            }, finalDelay)
        }

        updateProgressBar()
    }
    
    private fun showCorrectionFeedbackUI(): Long {
        var delayMs = 3000L
        
        binding.layoutCorrectionFeedback.visibility = View.VISIBLE
        
        if (viewModel.currentQuestionType == QuestionType.MEANING) {
            // Show simple meaning text
            binding.textCorrectionSimple.visibility = View.VISIBLE
            binding.layoutReadingsColumns.visibility = View.GONE
            
            val meaningsText = viewModel.currentKanji.meanings.joinToString(", ")
            binding.textCorrectionSimple.text = meaningsText
            
            // Calculate delay based on length
            delayMs = max(2000L, meaningsText.length * 100L)
        } else {
            // Show Readings Columns
            binding.textCorrectionSimple.visibility = View.GONE
            binding.layoutReadingsColumns.visibility = View.VISIBLE
            
            val onReadings = viewModel.currentKanji.readings.filter { it.type == "on" }.joinToString("\n") { hiraganaToKatakana(it.value) }
            val kunReadings = viewModel.currentKanji.readings.filter { it.type == "kun" }.joinToString("\n") { it.value }
            
            binding.textReadingsOn.text = if (onReadings.isNotEmpty()) onReadings else "-"
            binding.textReadingsKun.text = if (kunReadings.isNotEmpty()) kunReadings else "-"
            
            // Calculate delay
            val totalLength = onReadings.length + kunReadings.length
            delayMs = max(2000L, totalLength * 150L)
        }
        return delayMs
    }

    private fun normalizeForComparison(input: String, isReading: Boolean): String {
        var normalized = input.lowercase()

        // For meanings, also remove accents
        if (!isReading) {
            normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
                .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        }

        // For readings, convert everything to Hiragana
        if (isReading) {
            normalized = katakanaToHiragana(normalized)
        }

        // Remove common punctuation and spaces
        return normalized.replace(Regex("[.\\s-]"), "")
    }

    private fun checkMeaning(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = false)
        return viewModel.currentKanji.meanings.any { normalizeForComparison(it, isReading = false) == normalizedAnswer }
    }
    
    // Converts hiragana characters to katakana
    private fun hiraganaToKatakana(s: String): String {
        return s.map { c ->
            if (c in '\u3041'..'\u3096') {
                (c + 0x60)
            } else {
                c
            }
        }.joinToString("")
    }
    
    // Converts katakana characters to hiragana
    private fun katakanaToHiragana(s: String): String {
        return s.map { c ->
            if (c in '\u30A1'..'\u30F6') {
                (c - 0x60)
            } else {
                c
            }
        }.joinToString("")
    }

    private fun checkReading(answer: String): Boolean {
        val normalizedAnswer = normalizeForComparison(answer, isReading = true)
        return viewModel.currentKanji.readings.any { normalizeForComparison(it.value, isReading = true) == normalizedAnswer }
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

    // --- Loading Data (Migrated to use Shared Repository) ---

    private fun loadKanjiCharsForLevel(levelName: String): List<String> {
        val (type, value) = when {
            levelName.startsWith("N") -> "jlpt" to levelName
            levelName.startsWith("Grade ") -> {
                val grade = levelName.removePrefix("Grade ")
                "grade" to grade
            }
            // Add custom mapping if needed
            else -> return emptyList()
        }
        
        return MochiApplication.kanjiRepository.getKanjiByLevel(type, value).map { it.character }
    }

    private fun loadMeanings(): Map<String, List<String>> {
        val locale = Locale.getDefault().toString()
        return MochiApplication.meaningRepository.getMeanings(locale)
    }

    private fun loadAllKanjiDetails() {
        val meanings = loadMeanings()
        
        // Load details from Shared Repository (JSON)
        val allKanjiEntries = MochiApplication.kanjiRepository.getAllKanji()
        
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
