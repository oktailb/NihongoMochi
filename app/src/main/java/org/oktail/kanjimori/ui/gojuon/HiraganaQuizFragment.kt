package org.oktail.kanjimori.ui.gojuon

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.databinding.FragmentHiraganaQuizBinding
import org.xmlpull.v1.XmlPullParser

enum class GameStatus { NOT_ANSWERED, CORRECT, INCORRECT }

class HiraganaQuizFragment : Fragment() {

    private var _binding: FragmentHiraganaQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var allHiragana: List<HiraganaCharacter>
    private var currentHiraganaSet = mutableListOf<HiraganaCharacter>()
    private var revisionList = mutableListOf<HiraganaCharacter>()
    private val hiraganaStatus = mutableMapOf<HiraganaCharacter, GameStatus>()
    private var hiraganaListPosition = 0
    private lateinit var currentQuestion: HiraganaCharacter
    private val answerButtons: MutableList<Button> = mutableListOf()
    private val progressIndicators: MutableList<ImageView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiraganaQuizBinding.inflate(inflater, container, false)
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

        allHiragana = loadHiragana().shuffled()
        startNewSet()

        answerButtons.forEach { button ->
            button.setOnClickListener { onAnswerSelected(button) }
        }
    }

    private fun startNewSet() {
        revisionList.clear()
        hiraganaStatus.clear()

        if (hiraganaListPosition >= allHiragana.size) {
            parentFragmentManager.popBackStack()
            return
        }

        val nextSet = allHiragana.drop(hiraganaListPosition).take(10)
        hiraganaListPosition += nextSet.size

        currentHiraganaSet.clear()
        currentHiraganaSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentHiraganaSet.forEach { hiraganaStatus[it] = GameStatus.NOT_ANSWERED }

        updateProgressBar()
        displayQuestion()
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentQuestion = revisionList.random()
        binding.textHiraganaCharacter.text = currentQuestion.value

        val answerOptions = generateAnswerOptions(currentQuestion.phonetics)

        answerButtons.zip(answerOptions).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
        }
    }

    private fun generateAnswerOptions(correctAnswer: String): List<String> {
        val incorrectAnswers = allHiragana
            .map { it.phonetics }
            .distinct()
            .filter { it != correctAnswer }
            .shuffled()
            .take(3)

        return (incorrectAnswers + correctAnswer).shuffled()
    }

    private fun onAnswerSelected(selectedButton: Button) {
        val selectedAnswer = selectedButton.text.toString()
        val isCorrect = selectedAnswer == currentQuestion.phonetics

        ScoreManager.saveScore(requireContext(), currentQuestion.value, isCorrect)

        if (isCorrect) {
            selectedButton.setBackgroundColor(Color.GREEN)
            hiraganaStatus[currentQuestion] = GameStatus.CORRECT
            revisionList.remove(currentQuestion)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            hiraganaStatus[currentQuestion] = GameStatus.INCORRECT
        }

        updateProgressBar()

        answerButtons.forEach { it.isEnabled = false }

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, 1000)
    }

    private fun updateProgressBar() {
        for (i in 0 until 10) {
            if (i < currentHiraganaSet.size) {
                val hiragana = currentHiraganaSet[i]
                val status = hiraganaStatus[hiragana]
                val indicator = progressIndicators[i]
                indicator.visibility = View.VISIBLE
                when (status) {
                    GameStatus.CORRECT -> indicator.setImageResource(android.R.drawable.presence_online)
                    GameStatus.INCORRECT -> indicator.setImageResource(android.R.drawable.ic_delete)
                    else -> indicator.setImageResource(android.R.drawable.checkbox_off_background)
                }
            } else {
                progressIndicators[i].visibility = View.GONE
            }
        }
    }

    private fun loadHiragana(): List<HiraganaCharacter> {
        val hiraganaList = mutableListOf<HiraganaCharacter>()
        val parser = resources.getXml(R.xml.hiragana)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "character") {
                    val line = parser.getAttributeValue(null, "line").toInt()
                    val phonetics = parser.getAttributeValue(null, "phonetics")
                    val value = parser.nextText()
                    hiraganaList.add(HiraganaCharacter(value, line, phonetics))
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return hiraganaList
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class HiraganaCharacter(val value: String, val line: Int, val phonetics: String)
