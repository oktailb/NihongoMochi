package org.nihongo.mochi.ui.gojuon

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.XmlRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import org.nihongo.mochi.R
import org.nihongo.mochi.data.ScoreManager
import org.nihongo.mochi.databinding.FragmentKanaQuizBinding
import org.nihongo.mochi.ui.settings.ANIMATION_SPEED_PREF_KEY
import org.xmlpull.v1.XmlPullParser

enum class KanaQuizGameStatus { NOT_ANSWERED, PARTIAL, CORRECT, INCORRECT }
enum class KanaQuestionDirection { NORMAL, REVERSE } // NORMAL: Kana -> Romaji, REVERSE: Romaji -> Kana
data class KanaProgress(var normalSolved: Boolean = false, var reverseSolved: Boolean = false)

abstract class BaseKanaQuizFragment : Fragment() {

    private var _binding: FragmentKanaQuizBinding? = null
    protected val binding get() = _binding!!

    abstract val quizType: QuizType

    private lateinit var allKana: List<KanaCharacter>
    private var currentKanaSet = mutableListOf<KanaCharacter>()
    private var revisionList = mutableListOf<KanaCharacter>()
    private val kanaStatus = mutableMapOf<KanaCharacter, KanaQuizGameStatus>()
    private val kanaProgress = mutableMapOf<KanaCharacter, KanaProgress>()
    private var kanaListPosition = 0
    private lateinit var currentQuestion: KanaCharacter
    private val answerButtons: MutableList<Button> = mutableListOf()
    private val progressIndicators: MutableList<ImageView> = mutableListOf()
    private var currentDirection: KanaQuestionDirection = KanaQuestionDirection.NORMAL
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanaQuizBinding.inflate(inflater, container, false)
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

        allKana = loadKana(quizType.xmlResId).shuffled()
        startNewSet()

        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerSelected(button) }
        }
    }

    private fun startNewSet() {
        revisionList.clear()
        kanaStatus.clear()
        kanaProgress.clear()

        if (kanaListPosition >= allKana.size) {
            parentFragmentManager.popBackStack()
            return
        }

        val nextSet = allKana.drop(kanaListPosition).take(10)
        kanaListPosition += nextSet.size

        currentKanaSet.clear()
        currentKanaSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKanaSet.forEach {
            kanaStatus[it] = KanaQuizGameStatus.NOT_ANSWERED
            kanaProgress[it] = KanaProgress()
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
        val progress = kanaProgress[currentQuestion]!!

        currentDirection = when {
            !progress.normalSolved && !progress.reverseSolved -> if (Math.random() < 0.5) KanaQuestionDirection.NORMAL else KanaQuestionDirection.REVERSE
            !progress.normalSolved -> KanaQuestionDirection.NORMAL
            else -> KanaQuestionDirection.REVERSE
        }

        if (currentDirection == KanaQuestionDirection.NORMAL) {
            binding.textKanaCharacter.text = currentQuestion.value
            binding.textKanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 120f)
        } else {
            binding.textKanaCharacter.text = currentQuestion.phonetics
            binding.textKanaCharacter.setTextSize(TypedValue.COMPLEX_UNIT_SP, 80f)
        }

        val answerOptions = generateAnswerOptions(currentQuestion)

        answerButtons.zip(answerOptions).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.button_background))
            button.isEnabled = true

            if (currentDirection == KanaQuestionDirection.REVERSE) {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
            } else {
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
            }
        }
    }

    private fun generateAnswerOptions(correct: KanaCharacter): List<String> {
        val isNormal = currentDirection == KanaQuestionDirection.NORMAL
        val correctAnswer = if (isNormal) correct.phonetics else correct.value
        val incorrectAnswers = allKana
            .map { if (isNormal) it.phonetics else it.value }
            .distinct()
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)
        return (incorrectAnswers + correctAnswer).shuffled()
    }

    private fun onAnswerSelected(selectedButton: Button) {
        val selectedAnswer = selectedButton.text.toString()
        val isCorrect = if (currentDirection == KanaQuestionDirection.NORMAL) {
            selectedAnswer == currentQuestion.phonetics
        } else {
            selectedAnswer == currentQuestion.value
        }

        ScoreManager.saveScore(requireContext(), currentQuestion.value, isCorrect, ScoreManager.ScoreType.RECOGNITION)

        if (isCorrect) {
            selectedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_correct))
            
            val progress = kanaProgress[currentQuestion]!!
            if (currentDirection == KanaQuestionDirection.NORMAL) progress.normalSolved = true
            else progress.reverseSolved = true

            if (progress.normalSolved && progress.reverseSolved) {
                kanaStatus[currentQuestion] = KanaQuizGameStatus.CORRECT
                revisionList.remove(currentQuestion)
            } else {
                kanaStatus[currentQuestion] = KanaQuizGameStatus.PARTIAL
                selectedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_neutral))
            }
        } else {
            selectedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.answer_incorrect))
            kanaStatus[currentQuestion] = KanaQuizGameStatus.INCORRECT
        }

        updateProgressBar()
        answerButtons.forEach { it.isEnabled = false }

        val animationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        val delay = (1000 * animationSpeed).toLong()

        Handler(Looper.getMainLooper()).postDelayed({ displayQuestion() }, delay)
    }

    private fun updateProgressBar() {
        for (i in 0 until 10) {
            if (i < currentKanaSet.size) {
                val kana = currentKanaSet[i]
                val status = kanaStatus[kana]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                indicator.clearColorFilter()
                when (status) {
                    KanaQuizGameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    KanaQuizGameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    KanaQuizGameStatus.PARTIAL -> {
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

    private fun loadKana(@XmlRes xmlResId: Int): List<KanaCharacter> {
        val kanaList = mutableListOf<KanaCharacter>()
        val parser = resources.getXml(xmlResId)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "character") {
                    val line = parser.getAttributeValue(null, "line").toInt()
                    val phonetics = parser.getAttributeValue(null, "phonetics")
                    val value = parser.nextText()
                    kanaList.add(KanaCharacter(value, line, phonetics))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return kanaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

enum class QuizType(@XmlRes val xmlResId: Int, val title: String) {
    HIRAGANA(R.xml.hiragana, "Hiragana Quiz"),
    KATAKANA(R.xml.katakana, "Katakana Quiz")
}
