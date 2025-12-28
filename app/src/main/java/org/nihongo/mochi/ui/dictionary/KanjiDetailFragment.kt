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
import org.koin.android.ext.android.inject
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentKanjiDetailBinding
import org.nihongo.mochi.databinding.ItemExampleBinding
import org.nihongo.mochi.databinding.ItemReadingBinding
import org.nihongo.mochi.domain.kanji.KanjiRepository
import org.nihongo.mochi.domain.meaning.MeaningRepository
import org.nihongo.mochi.domain.settings.SettingsRepository
import org.nihongo.mochi.domain.words.WordRepository

class KanjiDetailFragment : Fragment() {

    private var _binding: FragmentKanjiDetailBinding? = null
    private val binding get() = _binding!!
    private val args: KanjiDetailFragmentArgs by navArgs()

    private val kanjiRepository: KanjiRepository by inject()
    private val meaningRepository: MeaningRepository by inject()
    private val wordRepository: WordRepository by inject()
    private val settingsRepository: SettingsRepository by inject()

    private var kanjiId: String? = null
    private var kanjiCharacter: String? = null
    private var kanjiStrokes: Int = 0
    private var jlptLevel: String? = null
    private var schoolGrade: String? = null
    private var kanjiStructure: String? = null
    private var kanjiMeanings = mutableListOf<String>()
    private var kanjiReadings = mutableListOf<ReadingItem>()
    private var kanjiComponents = mutableListOf<ComponentItem>()
    private var exampleWords = mutableListOf<ExampleItem>()

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
                    navigateToKanji(component.kanjiRef!!)
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
                    try {
                        val bundle = Bundle().apply { putString("kanjiId", id) }
                        findNavController().navigate(R.id.action_kanji_detail_to_kanji_detail, bundle)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun findKanjiIdByCharacter(character: String): String? {
        val entry = kanjiRepository.getKanjiByCharacter(character)
        return entry?.id
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
        val id = kanjiId ?: return

        kanjiReadings.clear()
        kanjiMeanings.clear()
        kanjiComponents.clear()
        kanjiStructure = null

        // Load details from Shared Repository (JSON)
        val entry = kanjiRepository.getKanjiById(id)
        if (entry != null) {
            kanjiCharacter = entry.character
            kanjiStrokes = entry.strokes?.toIntOrNull() ?: 0
            jlptLevel = entry.jlptLevel
            schoolGrade = entry.schoolGrade

            entry.readings?.reading?.forEach { r ->
                val freq = r.frequency?.toIntOrNull() ?: 0
                kanjiReadings.add(ReadingItem(r.type, r.value, freq))
            }

            kanjiStructure = entry.components?.structure
            entry.components?.component?.forEach { c ->
                val character = c.text ?: c.kanjiRef ?: ""
                kanjiComponents.add(ComponentItem(character, c.kanjiRef))
            }

            // Load Meanings from the new repository
            val locale = settingsRepository.getAppLocale()
            val meanings = meaningRepository.getMeanings(locale)
            kanjiMeanings.addAll(meanings[id] ?: emptyList())
        }
    }

    private fun loadExamples() {
        val character = kanjiCharacter ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val words = wordRepository.getWordsContainingKanji(character)
            exampleWords.clear()
            exampleWords.addAll(words.map { ExampleItem(it.text, it.phonetics) })
            withContext(Dispatchers.Main) {
                if(isAdded) {
                   setupExampleAdapter(binding.recyclerExamples, exampleWords)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
