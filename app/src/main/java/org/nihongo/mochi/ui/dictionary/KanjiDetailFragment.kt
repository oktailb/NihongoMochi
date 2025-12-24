package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentKanjiDetailBinding
import org.nihongo.mochi.databinding.ItemExampleBinding
import org.nihongo.mochi.databinding.ItemReadingBinding
import org.xmlpull.v1.XmlPullParser
import java.util.HashSet

class KanjiDetailFragment : Fragment() {

    private var _binding: FragmentKanjiDetailBinding? = null
    private val binding get() = _binding!!
    private val args: KanjiDetailFragmentArgs by navArgs()

    private var kanjiId: String? = null
    private var kanjiCharacter: String? = null
    private var kanjiStrokes: Int = 0
    private var jlptLevel: String? = null
    private var schoolGrade: String? = null
    private var kanjiStructure: String? = null
    private var kanjiMeanings = mutableListOf<String>()
    private var kanjiReadings = mutableListOf<ReadingItem>()
    private var kanjiComponents = mutableListOf<ComponentItem>()

    data class ReadingItem(val type: String, val reading: String, val frequency: Int)
    data class ExampleItem(val word: String, val reading: String)
    data class ComponentItem(val character: String, val kanjiRef: String?)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKanjiDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        kanjiId = args.kanjiId
        
        loadKanjiDetails()
        
        binding.textKanjiLarge.text = kanjiCharacter
        binding.textMeanings.text = kanjiMeanings.joinToString(", ")
        binding.textJlpt.text = jlptLevel ?: "-"
        binding.textGrade.text = schoolGrade ?: "-"
        binding.textStrokes.text = kanjiStrokes.toString()
        
        setupComponents()
        
        // Setup Reading Lists
        val onReadings = kanjiReadings.filter { it.type == "on" }
        val kunReadings = kanjiReadings.filter { it.type == "kun" }
        
        setupReadingAdapter(binding.recyclerOnReadings, onReadings, isOn = true)
        setupReadingAdapter(binding.recyclerKunReadings, kunReadings, isOn = false)

        loadExamples()
    }

    private fun setupComponents() {
        if (kanjiComponents.isEmpty()) {
            binding.layoutComponentsContainer.visibility = View.GONE
            return
        }
        
        binding.layoutComponentsContainer.visibility = View.VISIBLE
        binding.textStructure.text = kanjiStructure ?: ""
        
        binding.layoutComponentsList.removeAllViews()
        
        // Resolve default text color from theme (android:textColor)
        val typedValue = TypedValue()
        context?.theme?.resolveAttribute(android.R.attr.textColor, typedValue, true)
        val defaultTextColor = typedValue.data
        
        for (component in kanjiComponents) {
            val componentLayout = LinearLayout(context)
            componentLayout.orientation = LinearLayout.VERTICAL
            componentLayout.gravity = Gravity.CENTER_HORIZONTAL
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 0, 16, 0)
            componentLayout.layoutParams = layoutParams

            // Main Char
            val tvChar = TextView(context)
            tvChar.text = component.character
            tvChar.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            tvChar.setTextColor(defaultTextColor)
            tvChar.gravity = Gravity.CENTER
            componentLayout.addView(tvChar)

            // Ref Char (if different)
            if (!component.kanjiRef.isNullOrEmpty() && component.kanjiRef != component.character) {
                val tvRef = TextView(context)
                tvRef.text = "(${component.kanjiRef})"
                tvRef.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                tvRef.setTextColor(defaultTextColor)
                tvRef.gravity = Gravity.CENTER
                componentLayout.addView(tvRef)
            }

            // Click listener
            if (!component.kanjiRef.isNullOrEmpty()) {
                val outValue = TypedValue()
                context?.theme?.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
                componentLayout.setBackgroundResource(outValue.resourceId)
                
                componentLayout.isClickable = true
                componentLayout.setOnClickListener {
                    navigateToKanji(component.kanjiRef)
                }
            }

            binding.layoutComponentsList.addView(componentLayout)
        }
    }

    private fun navigateToKanji(character: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val id = findKanjiIdByCharacter(character)
            withContext(Dispatchers.Main) {
                if (id != null) {
                    val bundle = Bundle().apply { putString("kanjiId", id) }
                    try {
                        findNavController().navigate(R.id.nav_kanji_detail, bundle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun findKanjiIdByCharacter(character: String): String? {
        val parser = resources.getXml(R.xml.kanji_details)
        try {
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "kanji") {
                    val c = parser.getAttributeValue(null, "character")
                    if (c == character) {
                        return parser.getAttributeValue(null, "id")
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun setupReadingAdapter(recyclerView: RecyclerView, readings: List<ReadingItem>, isOn: Boolean) {
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = object : RecyclerView.Adapter<ReadingViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReadingViewHolder {
                val binding = ItemReadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ReadingViewHolder(binding)
            }

            override fun onBindViewHolder(holder: ReadingViewHolder, position: Int) {
                holder.bind(readings[position], isOn)
            }

            override fun getItemCount() = readings.size
        }
    }
    
    class ReadingViewHolder(private val binding: ItemReadingBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ReadingItem, isOn: Boolean) {
            var text = item.reading
            if (isOn) {
                text = hiraganaToKatakana(text)
            }
            binding.textReadingValue.text = text
        }
        
        private fun hiraganaToKatakana(s: String): String {
            return s.map { c ->
                if (c in '\u3041'..'\u3096') {
                    (c + 0x60)
                } else {
                    c
                }
            }.joinToString("")
        }
    }

    private fun setupExampleAdapter(recyclerView: RecyclerView, examples: List<ExampleItem>) {
        val layoutManager = FlexboxLayoutManager(context)
        layoutManager.flexDirection = FlexDirection.ROW
        layoutManager.flexWrap = FlexWrap.WRAP
        layoutManager.justifyContent = JustifyContent.FLEX_START
        recyclerView.layoutManager = layoutManager
        
        recyclerView.adapter = object : RecyclerView.Adapter<ExampleViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExampleViewHolder {
                val binding = ItemExampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                return ExampleViewHolder(binding)
            }

            override fun onBindViewHolder(holder: ExampleViewHolder, position: Int) {
                holder.bind(examples[position])
            }

            override fun getItemCount() = examples.size
        }
    }

    class ExampleViewHolder(private val binding: ItemExampleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ExampleItem) {
            binding.textExampleWord.text = item.word
            binding.textExampleReading.text = item.reading
        }
    }

    private fun loadKanjiDetails() {
        if (kanjiId == null) return

        kanjiReadings.clear()
        kanjiMeanings.clear()
        kanjiComponents.clear()
        kanjiStructure = null

        // Load basic details and readings
        val parser = resources.getXml(R.xml.kanji_details)
        try {
            var eventType = parser.eventType
            var isTarget = false
            var inReadings = false
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (parser.name == "kanji") {
                        val id = parser.getAttributeValue(null, "id")
                        if (id == kanjiId) {
                            isTarget = true
                            kanjiCharacter = parser.getAttributeValue(null, "character")
                        } else {
                            isTarget = false
                        }
                    } else if (isTarget) {
                        when (parser.name) {
                            "strokes" -> kanjiStrokes = parser.nextText().toIntOrNull() ?: 0
                            "jlpt_level" -> jlptLevel = parser.nextText()
                            "school_grade" -> schoolGrade = parser.nextText()
                            "readings" -> inReadings = true
                            "reading" -> {
                                if (inReadings) {
                                    val type = parser.getAttributeValue(null, "type")
                                    val freq = parser.getAttributeValue(null, "frequency")?.toIntOrNull() ?: 0
                                    val text = parser.nextText()
                                    kanjiReadings.add(ReadingItem(type, text, freq))
                                }
                            }
                            "components" -> {
                                kanjiStructure = parser.getAttributeValue(null, "structure")
                            }
                            "component" -> {
                                val ref = parser.getAttributeValue(null, "kanji_ref")
                                val rawText = parser.nextText()
                                val cleanText = rawText?.trim() ?: ""
                                val displayText = if (cleanText.isNotEmpty()) cleanText else ref ?: ""
                                if (displayText.isNotEmpty()) {
                                    kanjiComponents.add(ComponentItem(displayText, ref))
                                }
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.name == "kanji" && isTarget) {
                        break
                    }
                    if (parser.name == "readings") {
                        inReadings = false
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Load Meanings
        val meaningsParser = resources.getXml(R.xml.meanings)
        try {
            var eventType = meaningsParser.eventType
            var isTarget = false
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (meaningsParser.name == "kanji") {
                        val id = meaningsParser.getAttributeValue(null, "id")
                        isTarget = (id == kanjiId)
                    } else if (isTarget && meaningsParser.name == "meaning") {
                        kanjiMeanings.add(meaningsParser.nextText())
                    }
                } else if (eventType == XmlPullParser.END_TAG && meaningsParser.name == "kanji" && isTarget) {
                    break
                }
                eventType = meaningsParser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadExamples() {
        if (kanjiCharacter == null) return

        lifecycleScope.launch(Dispatchers.IO) {
            val withOkurigana = mutableListOf<ExampleItem>()
            val withoutOkurigana = mutableListOf<ExampleItem>()
            val seenWords = HashSet<String>()
            
            val resourcesToCheck = listOf(
                R.xml.jlpt_wordlist_n5, R.xml.jlpt_wordlist_n4, R.xml.jlpt_wordlist_n3, 
                R.xml.jlpt_wordlist_n2, R.xml.jlpt_wordlist_n1,
                R.xml.bccwj_wordlist_1000, R.xml.bccwj_wordlist_2000, R.xml.bccwj_wordlist_3000,
                R.xml.bccwj_wordlist_4000, R.xml.bccwj_wordlist_5000, R.xml.bccwj_wordlist_6000,
                R.xml.bccwj_wordlist_7000, R.xml.bccwj_wordlist_8000
            )
            
            val targetKanji = kanjiCharacter!!
            
            for (resId in resourcesToCheck) {
                if (withOkurigana.size >= 10) break 

                try {
                    val parser = resources.getXml(resId)
                    var eventType = parser.eventType
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG && parser.name == "word") {
                            val reading = parser.getAttributeValue(null, "phonetics")
                            val word = parser.nextText()
                            
                            if (word != null && reading != null && !seenWords.contains(word) && word.contains(targetKanji) && word.length > 1) {
                                seenWords.add(word)
                                val cleanReading = removeOkurigana(word, reading)
                                val item = ExampleItem(word, cleanReading)
                                
                                val hasKana = word.any { isKana(it) }
                                if (hasKana) {
                                    withOkurigana.add(item)
                                } else {
                                    withoutOkurigana.add(item)
                                }
                            }
                        }
                        eventType = parser.next()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            val combined = (withOkurigana + withoutOkurigana).take(5)
            
            withContext(Dispatchers.Main) {
                setupExampleAdapter(binding.recyclerExamples, combined)
            }
        }
    }

    private fun removeOkurigana(word: String, reading: String): String {
        var wIndex = word.length - 1
        var rIndex = reading.length - 1
        while (wIndex >= 0 && rIndex >= 0) {
            val wChar = word[wIndex]
            val rChar = reading[rIndex]
            if (isKana(wChar) && wChar == rChar) {
                wIndex--
                rIndex--
            } else {
                break
            }
        }
        return if (rIndex < 0) "" else reading.substring(0, rIndex + 1)
    }

    private fun isKana(c: Char): Boolean {
        return c in '\u3040'..'\u309F' || c in '\u30A0'..'\u30FF'
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
