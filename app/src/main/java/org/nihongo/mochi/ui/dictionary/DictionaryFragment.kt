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
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentDictionaryBinding
import org.nihongo.mochi.databinding.ItemDictionaryBinding
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.kanji.KanjiEntry
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
        binding.textResultCount.text = getString(R.string.dictionary_results_count_format, viewModel.lastResults.size)
    }

    private fun setupFilterListeners() {
        binding.buttonDrawSearch.setOnClickListener {
            DrawingDialogFragment().show(parentFragmentManager, "DrawingDialog")
        }

        binding.buttonClearDrawing.setOnClickListener {
            viewModel.clearDrawingFilter()
            applyFilters()
        }

        // Hide the apply filters button as search is now reactive
        binding.buttonApplyFilters.visibility = View.GONE

        binding.radioGroupSearchMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.searchMode = if (checkedId == R.id.radio_mode_reading) SearchMode.READING else SearchMode.MEANING
            applyFilters()
        }

        binding.editStrokeCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilters()
            }
        })

        setupTextWatcher()
    }

    private fun setupTextWatcher() {
        binding.editSearch.removeTextChangedListener(textWatcher)
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && viewModel.searchMode == SearchMode.READING) {
                    binding.editSearch.removeTextChangedListener(this)
                    val input = s.toString()
                    val replacement = RomajiToKana.checkReplacement(input)
                    if (replacement != null) {
                        val (suffixLen, kana) = replacement
                        val start = input.length - suffixLen
                        val end = input.length
                        s.replace(start, end, kana)
                    }
                    binding.editSearch.addTextChangedListener(this)
                }
                applyFilters()
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
                     item.readings.any { it.text.replace(".", "").lowercase().contains(cleanQuery) }
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
        binding.textResultCount.text = getString(R.string.dictionary_results_count_format, filteredList.size)
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
        loadKanjiDetailsFromRepo()
        parseMeanings()
        viewModel.allKanjiList.addAll(viewModel.kanjiDataMap.values.sortedBy { it.id.toIntOrNull() ?: 0 })
    }

    private fun loadKanjiDetailsFromRepo() {
        // Use Shared Repository instead of XML
        val allKanji = MochiApplication.kanjiRepository.getAllKanji()
        
        for (kanjiEntry in allKanji) {
            val readings = kanjiEntry.readings?.reading?.map { 
                ReadingInfo(it.value, it.type)
            } ?: emptyList()
            
            val strokes = kanjiEntry.strokes?.toIntOrNull() ?: 0
            
            val item = DictionaryItem(
                id = kanjiEntry.id,
                character = kanjiEntry.character,
                readings = readings,
                strokeCount = strokes,
                meanings = mutableListOf()
            )
            viewModel.kanjiDataMap[kanjiEntry.id] = item
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

    data class ReadingInfo(val text: String, val type: String)

    data class DictionaryItem(
        val id: String,
        val character: String,
        val readings: List<ReadingInfo>,
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
            
            val onReadings = item.readings.filter { it.type == "on" }.map { hiraganaToKatakana(it.text) }
            val kunReadings = item.readings.filter { it.type == "kun" }.map { it.text }
            
            val sb = StringBuilder()
            if (onReadings.isNotEmpty()) {
                sb.append(binding.root.context.getString(R.string.dictionary_reading_on)).append(" ").append(onReadings.joinToString(", "))
            }
            if (kunReadings.isNotEmpty()) {
                if (sb.isNotEmpty()) sb.append("  ")
                sb.append(binding.root.context.getString(R.string.dictionary_reading_kun)).append(" ").append(kunReadings.joinToString(", "))
            }
            binding.textReading.text = sb.toString()
            
            binding.textMeanings.text = item.meanings.joinToString(", ")
            binding.textId.visibility = View.GONE // Hide the ID field
            binding.textStrokeCount.text = item.strokeCount.toString()
        }

        override fun getItemCount() = list.size
        
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
}
