package org.nihongo.mochi.ui.dictionary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentDictionaryBinding
import org.nihongo.mochi.databinding.ItemDictionaryBinding
import org.nihongo.mochi.ui.writinggame.RomajiToKana
import org.xmlpull.v1.XmlPullParser
import kotlin.math.max
import kotlin.math.min

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DictionaryViewModel by activityViewModels()
    private lateinit var adapter: DictionaryAdapter
    private var textWatcher: TextWatcher? = null

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
            val action = DictionaryFragmentDirections.actionDictionaryToKanjiDetail(item.id)
            findNavController().navigate(action)
        }

        binding.recyclerDictionary.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDictionary.adapter = adapter

        setupFilterListeners()
        observeDrawingResults()

        if (!viewModel.isDataLoaded) {
            viewLifecycleOwner.lifecycleScope.launch {
                loadDictionaryData()
                viewModel.isDataLoaded = true
                applyFilters()
            }
        } else {
            restoreUIState()
        }
    }

    private fun restoreUIState() {
        binding.editSearch.setText(viewModel.textQuery)
        binding.editStrokeCount.setText(viewModel.strokeQuery)
        binding.checkExactMatch.isChecked = viewModel.exactMatch
        binding.radioGroupSearchMode.check(
            if (viewModel.searchMode == SearchMode.READING) R.id.radio_mode_reading else R.id.radio_mode_meaning
        )
        adapter.submitList(viewModel.lastResults)
        updateDrawingThumbnail()
        binding.textResultCount.text = "${viewModel.lastResults.size} résultats"
    }

    private fun setupFilterListeners() {
        binding.buttonDrawSearch.setOnClickListener {
            DrawingDialogFragment().show(parentFragmentManager, "DrawingDialog")
        }
        
        binding.buttonClearDrawing.setOnClickListener {
            viewModel.clearDrawingFilter()
            applyFilters()
        }
        
        binding.buttonApplyFilters.setOnClickListener {
            applyFilters()
        }
        
        binding.radioGroupSearchMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.searchMode = if (checkedId == R.id.radio_mode_reading) SearchMode.READING else SearchMode.MEANING
        }
        
        binding.editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilters()
                true
            } else false
        }
        binding.editStrokeCount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                applyFilters()
                true
            } else false
        }
        
        setupTextWatcher()
    }
    
    private fun setupTextWatcher() {
        binding.editSearch.removeTextChangedListener(textWatcher)
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                if (s == null || viewModel.searchMode != SearchMode.READING) return
                
                val input = s.toString()
                val replacement = RomajiToKana.checkReplacement(input)
                if (replacement != null) {
                    val (suffixLen, kana) = replacement
                    val start = input.length - suffixLen
                    val end = input.length
                    
                    binding.editSearch.removeTextChangedListener(this)
                    s.replace(start, end, kana)
                    binding.editSearch.addTextChangedListener(this)
                }
            }
        }
        binding.editSearch.addTextChangedListener(textWatcher)
    }
    
    private fun observeDrawingResults() {
        viewModel.recognitionResults.observe(viewLifecycleOwner) { candidates ->
            if (candidates != null) {
                viewModel.drawingCandidates = candidates
                applyFilters() // Apply drawing filter immediately
            }
        }
    }

    private fun applyFilters() {
        // Update ViewModel with current UI state before filtering
        viewModel.textQuery = binding.editSearch.text.toString()
        viewModel.strokeQuery = binding.editStrokeCount.text.toString()
        viewModel.exactMatch = binding.checkExactMatch.isChecked
        
        var filteredList = viewModel.allKanjiList.toList()

        // 1. Drawing filter
        viewModel.drawingCandidates?.let { candidates ->
            val candidateSet = candidates.toSet()
            filteredList = filteredList.filter { candidateSet.contains(it.character) }
        }

        // 2. Stroke count filter
        val strokeCount = viewModel.strokeQuery.toIntOrNull()
        if (strokeCount != null) {
            filteredList = filteredList.filter { it.strokeCount == strokeCount }
        }

        // 3. Text filter
        val query = viewModel.textQuery.trim().lowercase()
        if (query.isNotEmpty()) {
            if (viewModel.searchMode == SearchMode.READING) {
                 val cleanQuery = query.replace(".", "")
                 filteredList = filteredList.filter { item ->
                     item.readings.any { it.replace(".", "").lowercase().contains(cleanQuery) }
                 }
            } else { // MEANING
                 filteredList = filteredList.filter { item ->
                     item.meanings.any { it.lowercase().contains(query) }
                 }
            }
        }
        
        // Update UI
        viewModel.lastResults = filteredList
        adapter.submitList(filteredList)
        binding.textResultCount.text = "${filteredList.size} résultats"
        updateDrawingThumbnail()
    }
    
    private fun updateDrawingThumbnail() {
        if (viewModel.lastInk != null) {
            binding.cardDrawingThumbnail.visibility = View.VISIBLE
            binding.buttonClearDrawing.visibility = View.VISIBLE
            val bitmap = renderInkToBitmap(viewModel.lastInk!!, 100)
            binding.imageDrawingThumbnail.setImageBitmap(bitmap)
        } else {
            binding.cardDrawingThumbnail.visibility = View.GONE
            binding.buttonClearDrawing.visibility = View.GONE
        }
    }
    
    // --- Data Loading ---
    private suspend fun loadDictionaryData() = withContext(Dispatchers.IO) {
        if (viewModel.isDataLoaded) return@withContext
        parseKanjiDetails()
        parseMeanings()
        viewModel.allKanjiList.addAll(viewModel.kanjiDataMap.values.sortedBy { it.id.toIntOrNull() ?: 0 })
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
                when(eventType) {
                    XmlPullParser.START_TAG -> {
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
                                } catch (e: Exception) { /* Ignore */ }
                            }
                            "reading" -> {
                                val r = parser.nextText()
                                if (r.isNotEmpty()) currentReadings.add(r)
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                         if (parser.name == "kanji" && currentId != null && currentCharacter != null) {
                            val item = DictionaryItem(
                                id = currentId,
                                character = currentCharacter,
                                readings = ArrayList(currentReadings),
                                strokeCount = currentStrokes,
                                meanings = mutableListOf()
                            )
                            viewModel.kanjiDataMap[currentId] = item
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseMeanings() {
        val parser = resources.getXml(R.xml.meanings)
        try {
            var eventType = parser.eventType
            var currentId: String? = null
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "kanji" -> currentId = parser.getAttributeValue(null, "id")
                        "meaning" -> {
                            val meaning = parser.nextText()
                            if (currentId != null && meaning.isNotEmpty()) {
                                viewModel.kanjiDataMap[currentId]?.meanings?.add(meaning)
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
    
    private fun renderInkToBitmap(ink: Ink, targetSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE
        if (ink.strokes.isEmpty()) return bitmap

        for (stroke in ink.strokes) {
            for (point in stroke.points) {
                minX = min(minX, point.x)
                minY = min(minY, point.y)
                maxX = max(maxX, point.x)
                maxY = max(maxY, point.y)
            }
        }

        val drawingWidth = maxX - minX
        val drawingHeight = maxY - minY
        if (drawingWidth <= 0 || drawingHeight <= 0) return bitmap

        val margin = targetSize * 0.1f
        val effectiveSize = targetSize - (2 * margin)
        val scale = min(effectiveSize / drawingWidth, effectiveSize / drawingHeight)

        val dx = -minX * scale + margin
        val dy = -minY * scale + margin

        canvas.translate(dx, dy)
        canvas.scale(scale, scale)

        val paint = Paint().apply {
            color = Color.BLACK
            isAntiAlias = true
            strokeWidth = 3f / scale // Keep stroke width constant regardless of scale
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        for (stroke in ink.strokes) {
            val path = Path()
            if (stroke.points.isNotEmpty()) {
                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                for (i in 1 until stroke.points.size) {
                    path.lineTo(stroke.points[i].x, stroke.points[i].y)
                }
                canvas.drawPath(path, paint)
            }
        }
        return bitmap
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        textWatcher = null
    }

    data class DictionaryItem(
        val id: String,
        val character: String,
        val readings: List<String>,
        val strokeCount: Int,
        val meanings: MutableList<String>
    )

    class DictionaryAdapter(private val onItemClick: (DictionaryItem) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private var list: List<DictionaryItem> = emptyList()

        fun submitList(newList: List<DictionaryItem>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val binding = ItemDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return object : RecyclerView.ViewHolder(binding.root){}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val item = list[position]
            val binding = ItemDictionaryBinding.bind(holder.itemView)
            binding.root.setOnClickListener { onItemClick(item) }
            binding.textKanji.text = item.character
            binding.textReading.text = item.readings.joinToString(", ")
            binding.textMeanings.text = item.meanings.joinToString(", ")
            binding.textId.visibility = View.GONE // Hide the ID field
            binding.textStrokeCount.text = item.strokeCount.toString()
        }

        override fun getItemCount() = list.size
    }
}