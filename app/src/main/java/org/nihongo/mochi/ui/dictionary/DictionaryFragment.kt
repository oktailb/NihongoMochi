package org.nihongo.mochi.ui.dictionary

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentDictionaryBinding
import org.nihongo.mochi.databinding.ItemDictionaryBinding
import org.nihongo.mochi.ui.writinggame.RomajiToKana
import org.xmlpull.v1.XmlPullParser

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: DictionaryAdapter
    
    // Full database in memory
    private val allKanjiList = mutableListOf<DictionaryItem>()
    // Helper map to merge data
    private val kanjiDataMap = mutableMapOf<String, DictionaryItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DictionaryAdapter { item ->
            // On Item Click
            val action = DictionaryFragmentDirections.actionDictionaryToKanjiDetail(item.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerDictionary.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDictionary.adapter = adapter

        // Setup search listeners
        binding.buttonSearch.setOnClickListener { performSearch() }
        
        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else {
                false
            }
        }

        // Live search and romaji to kana conversion
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { 
                if (s == null) return
                
                // Attempt Romaji -> Kana conversion
                val input = s.toString()
                val replacement = RomajiToKana.checkReplacement(input)
                if (replacement != null) {
                    val (suffixLen, kana) = replacement
                    val start = input.length - suffixLen
                    val end = input.length
                    
                    // Prevent infinite loop by removing listener during modification
                    binding.editSearch.removeTextChangedListener(this)
                    s.replace(start, end, kana)
                    binding.editSearch.addTextChangedListener(this)
                    
                    // Since we modified text, the cursor might need adjustment, but usually replace handles it ok.
                    // If we wanted, we could trigger search here for "live search" experience
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        
        // Load data in background
        viewLifecycleOwner.lifecycleScope.launch {
            loadDictionaryData()
        }
    }

    private suspend fun loadDictionaryData() = withContext(Dispatchers.IO) {
        kanjiDataMap.clear()
        allKanjiList.clear()
        
        // 1. Parse kanji_details.xml
        parseKanjiDetails()
        
        // 2. Parse meanings.xml
        parseMeanings()

        // 3. Flatten to list
        allKanjiList.addAll(kanjiDataMap.values)
        
        // Sort by ID or other criteria if needed? For now, keep order or random.
        // allKanjiList.sortBy { it.id.toIntOrNull() }
        
        // Initial state: empty list
        withContext(Dispatchers.Main) {
             adapter.submitList(emptyList())
        }
    }

    private fun parseKanjiDetails() {
        val parser = resources.getXml(R.xml.kanji_details)
        try {
            var eventType = parser.eventType
            var currentId: String? = null
            var currentCharacter: String? = null
            var currentStrokes = 0
            val currentReadings = mutableListOf<String>()
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "kanji" -> {
                            currentId = parser.getAttributeValue(null, "id")
                            currentCharacter = parser.getAttributeValue(null, "character")
                            currentReadings.clear()
                            currentStrokes = 0
                        }
                        "strokes" -> {
                            try {
                                currentStrokes = parser.nextText().toInt()
                            } catch (e: Exception) { }
                        }
                        "reading" -> {
                            val r = parser.nextText()
                            if (r.isNotEmpty()) currentReadings.add(r)
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    if (parser.name == "kanji" && currentId != null && currentCharacter != null) {
                        val item = DictionaryItem(
                            id = currentId,
                            character = currentCharacter,
                            readings = ArrayList(currentReadings),
                            strokeCount = currentStrokes,
                            meanings = mutableListOf()
                        )
                        kanjiDataMap[currentId] = item
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseMeanings() {
        // resources.getXml(R.xml.meanings) will automatically pick the localized version
        // e.g., values-fr/meanings.xml if locale is French
        val parser = resources.getXml(R.xml.meanings)
        try {
            var eventType = parser.eventType
            var currentId: String? = null
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "kanji" -> {
                            currentId = parser.getAttributeValue(null, "id")
                        }
                        "meaning" -> {
                            val meaning = parser.nextText()
                            if (currentId != null && meaning.isNotEmpty()) {
                                kanjiDataMap[currentId]?.meanings?.add(meaning)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun performSearch() {
        val query = binding.editSearch.text.toString().trim().lowercase()
        val strokeStr = binding.editStrokeCount.text.toString().trim()
        val exactMatch = binding.checkExactMatch.isChecked
        
        // If query and strokes are empty, do nothing or clear list
        if (query.isEmpty() && strokeStr.isEmpty()) {
            adapter.submitList(emptyList())
            return
        }

        val strokeCount = strokeStr.toIntOrNull()

        val filtered = allKanjiList.filter { item ->
            // Stroke filter
            val matchStroke = if (strokeCount == null) true else item.strokeCount == strokeCount
            
            // Text filter
            val matchQuery = if (query.isEmpty()) true else {
                if (exactMatch) {
                     item.character == query
                } else {
                     item.character.contains(query, ignoreCase = true) ||
                     item.readings.any { it.contains(query, ignoreCase = true) } ||
                     item.meanings.any { it.contains(query, ignoreCase = true) }
                }
            }
            
            matchStroke && matchQuery
        }
        
        adapter.submitList(filtered)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    data class DictionaryItem(
        val id: String, 
        val character: String, 
        val readings: List<String>, 
        val strokeCount: Int,
        val meanings: MutableList<String>
    )

    class DictionaryAdapter(private val onItemClick: (DictionaryItem) -> Unit) : RecyclerView.Adapter<DictionaryAdapter.ViewHolder>() {
        private var list: List<DictionaryItem> = emptyList()

        fun submitList(newList: List<DictionaryItem>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding, onItemClick)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size

        class ViewHolder(private val binding: ItemDictionaryBinding, private val onClick: (DictionaryItem) -> Unit) : RecyclerView.ViewHolder(binding.root) {
            fun bind(item: DictionaryItem) {
                binding.root.setOnClickListener { onClick(item) }
                binding.textKanji.text = item.character
                
                // Format readings
                binding.textReading.text = item.readings.take(4).joinToString(", ")
                
                // Format meanings
                binding.textMeanings.text = item.meanings.take(3).joinToString(", ")

                binding.textId.text = "ID: ${item.id}"
                binding.textStrokeCount.text = "${item.strokeCount}"
            }
        }
    }
}
