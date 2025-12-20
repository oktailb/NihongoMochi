package org.oktail.kanjimori.ui.gojuon

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import org.oktail.kanjimori.R
import org.oktail.kanjimori.data.KanjiScore
import org.oktail.kanjimori.data.ScoreManager
import org.oktail.kanjimori.data.ScoreManager.ScoreType
import org.oktail.kanjimori.databinding.FragmentHiraganaBinding
import org.xmlpull.v1.XmlPullParser

class HiraganaFragment : Fragment() {

    private var _binding: FragmentHiraganaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHiraganaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateHiraganaGrid()
        binding.buttonPlayHiraganaQuiz.setOnClickListener {
            findNavController().navigate(R.id.action_nav_hiragana_to_hiragana_quiz)
        }
    }

    private fun populateHiraganaGrid() {
        val hiraganaCharacters = loadHiragana()
        val hiraganaByLine = hiraganaCharacters.groupBy { it.line }.toSortedMap()

        binding.gridHiragana.removeAllViews()

        for ((_, charactersInLine) in hiraganaByLine) {
            // Add character TextViews
            for (hiragana in charactersInLine) {
                val score = ScoreManager.getScore(requireContext(), hiragana.value, ScoreType.RECOGNITION)
                val textView = TextView(context).apply {
                    text = hiragana.value
                    textSize = 24f
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    setTextColor(Color.BLACK) // Force black text for readability
                    setBackgroundColor(calculateColor(score))
                    val params = android.widget.GridLayout.LayoutParams().apply {
                        width = 0
                        height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                        columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                        setMargins(4, 4, 4, 4)
                    }
                    layoutParams = params
                }
                binding.gridHiragana.addView(textView)
            }

            // Add spacers to fill the rest of the line
            val spacersNeeded = 5 - charactersInLine.size
            if (spacersNeeded > 0) {
                for (i in 1..spacersNeeded) {
                    val spacer = Space(context).apply {
                        layoutParams = android.widget.GridLayout.LayoutParams().apply {
                            width = 0
                            height = android.widget.GridLayout.LayoutParams.WRAP_CONTENT
                            columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                        }
                    }
                    binding.gridHiragana.addView(spacer)
                }
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

    private fun calculateColor(score: KanjiScore): Int {
        val balance = score.successes - score.failures
        val percentage = (balance.toFloat() / 10.0f).coerceIn(-1.0f, 1.0f)

        return when {
            percentage > 0 -> lerpColor(Color.WHITE, Color.GREEN, percentage)
            percentage < 0 -> lerpColor(Color.WHITE, Color.RED, -percentage)
            else -> Color.WHITE
        }
    }

    private fun lerpColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val a = (startA + fraction * (endA - startA)).toInt()
        val r = (startR + fraction * (endR - startR)).toInt()
        val g = (startG + fraction * (endG - startG)).toInt()
        val b = (startB + fraction * (endB - startB)).toInt()

        return Color.argb(a, r, g, b)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}