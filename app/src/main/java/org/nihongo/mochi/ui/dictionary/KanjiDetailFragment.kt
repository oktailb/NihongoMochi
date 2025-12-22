package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentKanjiDetailBinding
import org.nihongo.mochi.databinding.ItemReadingBinding
import org.xmlpull.v1.XmlPullParser

class KanjiDetailFragment : Fragment() {

    private var _binding: FragmentKanjiDetailBinding? = null
    private val binding get() = _binding!!
    private val args: KanjiDetailFragmentArgs by navArgs()

    private var kanjiId: String? = null
    private var kanjiCharacter: String? = null
    private var kanjiStrokes: Int = 0
    private var jlptLevel: String? = null
    private var schoolGrade: String? = null
    private var kanjiMeanings = mutableListOf<String>()
    private var kanjiReadings = mutableListOf<ReadingItem>()

    data class ReadingItem(val type: String, val reading: String, val frequency: Int)

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
        
        // Setup Reading Lists
        val onReadings = kanjiReadings.filter { it.type == "on" }
        val kunReadings = kanjiReadings.filter { it.type == "kun" }
        
        setupReadingAdapter(binding.recyclerOnReadings, onReadings, isOn = true)
        setupReadingAdapter(binding.recyclerKunReadings, kunReadings, isOn = false)
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
            // For On readings, we typically use Katakana. For Kun, Hiragana.
            // The XML seems to provide Hiragana for both usually, or we might want to convert.
            // The RomajiToKana class converts to Hiragana.
            // Let's assume input is Hiragana from XML.
            
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

    private fun loadKanjiDetails() {
        if (kanjiId == null) return

        kanjiReadings.clear()
        kanjiMeanings.clear()

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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
