package org.oktail.kanjimori.ui.gojuon

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.data.ScoreManager.ScoreType
import org.oktail.kanjimori.databinding.FragmentKatakanaQuizBinding
import org.oktail.kanjimori.ui.settings.ANIMATION_SPEED_PREF_KEY
import org.xmlpull.v1.XmlPullParser

data class KatakanaProgress(var normalSolved: Boolean = false, var reverseSolved: Boolean = false)

class KatakanaQuizFragment : Fragment() {

    private var _binding: FragmentKatakanaQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var allKatakana: List<KatakanaCharacter>
    private var currentKatakanaSet = mutableListOf<KatakanaCharacter>()
    private var revisionList = mutableListOf<KatakanaCharacter>()
    private val katakanaStatus = mutableMapOf<KatakanaCharacter, GameStatus>()
    private val katakanaProgress = mutableMapOf<KatakanaCharacter, KatakanaProgress>()
    private var katakanaListPosition = 0
    private lateinit var currentQuestion: KatakanaCharacter
    private val answerButtons: MutableList<Button> = mutableListOf()
    private val progressIndicators: MutableList<ImageView> = mutableListOf()
    private var currentDirection: QuestionDirection = QuestionDirection.NORMAL
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKatakanaQuizBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        answerButtons.addAll(listOf(
            binding.buttonAnswer1,
            binding.buttonAnswer2,
            binding.buttonAnswer3,
            binding.buttonAnswer4
        ))

        val progressBar = binding.progressBarGame
        for (i in 0 until progressBar.childCount) {
            (progressBar.getChildAt(i) as? ImageView)?.let { progressIndicators.add(it) }
        }

        allKatakana = loadKatakana().shuffled()
        startNewSet()

        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerSelected(button) }
        }
    }

    private fun startNewSet() {
        revisionList.clear()
        katakanaStatus.clear()
        katakanaProgress.clear()

        if (katakanaListPosition >= allKatakana.size) {
            parentFragmentManager.popBackStack()
            return
        }

        val nextSet = allKatakana.drop(katakanaListPosition).take(10)
        katakanaListPosition += nextSet.size

        currentKatakanaSet.clear()
        currentKatakanaSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKatakanaSet.forEach { 
            katakanaStatus[it] = GameStatus.NOT_ANSWERED
            katakanaProgress[it] = KatakanaProgress()
        }

        updateProgressBar()
        displayQuestion()
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentQuestion = revisionList.random()
        val progress = katakanaProgress[currentQuestion]!!

        // Determine direction
        currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Math.random() < 0.5) QuestionDirection.NORMAL else QuestionDirection.REVERSE
            !progress.normalSolved -> QuestionDirection.NORMAL
            else -> QuestionDirection.REVERSE
        }

        if (currentDirection == QuestionDirection.NORMAL) {
            // Standard: Show Kana, guess Romaji
            binding.textKatakanaCharacter.text = currentQuestion.value
            binding.textKatakanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120f)
        } else {
            // Reverse: Show Romaji, guess Kana
            binding.textKatakanaCharacter.text = currentQuestion.phonetics
            // Adjust text size for Romaji
            binding.textKatakanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80f)
        }

        val answerOptions = generateAnswerOptions(currentQuestion)

        answerButtons.zip(answerOptions).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
            
            if (currentDirection == QuestionDirection.REVERSE) {
                // Button shows Kana
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            } else {
                // Button shows Romaji
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            }
        }
    }

    private fun generateAnswerOptions(correct: KatakanaCharacter): List<String> {
        if (currentDirection == QuestionDirection.NORMAL) {
            // Generate Romaji answers
            val correctAnswer = correct.phonetics
            val incorrectAnswers = allKatakana
                .map { it.phonetics }
                .distinct()
                .filter { it != correctAnswer }
                .shuffled()
                .take(3)
            return (incorrectAnswers + correctAnswer).shuffled()
        } else {
            // Generate Kana answers (REVERSE)
            val correctAnswer = correct.value
            val incorrectAnswers = allKatakana
                .map { it.value }
                .distinct()
                .filter { it != correctAnswer }
                .shuffled()
                .take(3)
            return (incorrectAnswers + correctAnswer).shuffled()
        }
    }

    private fun onAnswerSelected(selectedButton: Button) {
        val selectedAnswer = selectedButton.text.toString()
        val isCorrect = if (currentDirection == QuestionDirection.NORMAL) {
             selectedAnswer == currentQuestion.phonetics
        } else {
             selectedAnswer == currentQuestion.value
        }

        ScoreManager.saveScore(requireContext(), currentQuestion.value, isCorrect, ScoreType.RECOGNITION)

        if (isCorrect) {
            selectedButton.setBackgroundColor(Color.GREEN)
            
            val progress = katakanaProgress[currentQuestion]!!
            if (currentDirection == QuestionDirection.NORMAL) progress.normalSolved = true
            else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                katakanaStatus[currentQuestion] = GameStatus.CORRECT
                revisionList.remove(currentQuestion)
            } else {
                katakanaStatus[currentQuestion] = GameStatus.PARTIAL
                selectedButton.setBackgroundColor(Color.parseColor("#FFA500")) // Orange
            }
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            katakanaStatus[currentQuestion] = GameStatus.INCORRECT
        }

        updateProgressBar()

        answerButtons.forEach { it.isEnabled = false }

        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        val delay = (1000 * animationSpeed).toLong()

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, delay)
    }

    private fun updateProgressBar() {
        for (i in 0 until 10) {
            if (i < currentKatakanaSet.size) {
                val katakana = currentKatakanaSet[i]
                val status = katakanaStatus[katakana]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                
                indicator.clearColorFilter()
                
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    GameStatus.PARTIAL -> {
                         indicator.setImageResource(android.R.drawable.ic_menu_recent_history)
                         indicator.setColorFilter(Color.parseColor("#FFA500"))
                    }
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    private fun loadKatakana(): List<KatakanaCharacter> {
        val katakanaList = mutableListOf<KatakanaCharacter>()
        val parser = resources.getXml(R.xml.katakana)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "character") {
                    val line = parser.getAttributeValue(null, "line").toInt()
                    val phonetics = parser.getAttributeValue(null, "phonetics")
                    val value = parser.nextText()
                    katakanaList.add(KatakanaCharacter(value, line, phonetics))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return katakanaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
