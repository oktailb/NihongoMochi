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
import org.oktail.kanjimori.databinding.FragmentKatakanaQuizBinding
import org.xmlpull.v1.XmlPullParser

class KatakanaQuizFragment : Fragment() {

    private var _binding: FragmentKatakanaQuizBinding? = null
    private val binding get() = _binding!!

    private lateinit var allKatakana: List<KatakanaCharacter>
    private var currentKatakanaSet = mutableListOf<KatakanaCharacter>()
    private var revisionList = mutableListOf<KatakanaCharacter>()
    private val katakanaStatus = mutableMapOf<KatakanaCharacter, GameStatus>()
    private var katakanaListPosition = 0
    private lateinit var currentQuestion: KatakanaCharacter
    private val answerButtons: MutableList<Button> = mutableListOf()
    private val progressIndicators: MutableList<ImageView> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKatakanaQuizBinding.inflate(inflater, container, false)
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

        if (katakanaListPosition >= allKatakana.size) {
            parentFragmentManager.popBackStack()
            return
        }

        val nextSet = allKatakana.drop(katakanaListPosition).take(10)
        katakanaListPosition += nextSet.size

        currentKatakanaSet.clear()
        currentKatakanaSet.addAll(nextSet)
        revisionList.addAll(nextSet)
        currentKatakanaSet.forEach { katakanaStatus[it] = GameStatus.NOT_ANSWERED }

        updateProgressBar()
        displayQuestion()
    }

    private fun displayQuestion() {
        if (revisionList.isEmpty()) {
            startNewSet()
            return
        }

        currentQuestion = revisionList.random()
        binding.textKatakanaCharacter.text = currentQuestion.value

        val answerOptions = generateAnswerOptions(currentQuestion.phonetics)

        answerButtons.zip(answerOptions).forEach { (button, answerText) ->
            button.text = answerText
            button.setBackgroundColor(Color.LTGRAY)
            button.isEnabled = true
        }
    }

    private fun generateAnswerOptions(correctAnswer: String): List<String> {
        val incorrectAnswers = allKatakana
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
            katakanaStatus[currentQuestion] = GameStatus.CORRECT
            revisionList.remove(currentQuestion)
        } else {
            selectedButton.setBackgroundColor(Color.RED)
            katakanaStatus[currentQuestion] = GameStatus.INCORRECT
        }

        updateProgressBar()

        answerButtons.forEach { it.isEnabled = false }

        Handler(Looper.getMainLooper()).postDelayed({
            displayQuestion()
        }, 1000)
    }

    private fun updateProgressBar() {
        for (i in 0 until 10) {
            if (i < currentKatakanaSet.size) {
                val katakana = currentKatakanaSet[i]
                val status = katakanaStatus[katakana]
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
