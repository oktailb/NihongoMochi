package org.nihongo.mochi.ui.dictionary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.mlkit.vision.digitalink.Ink
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentDictionaryBinding
import org.nihongo.mochi.databinding.ItemDictionaryBinding
import org.nihongo.mochi.domain.dictionary.DictionaryItem
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.dictionary.SearchMode
import org.nihongo.mochi.domain.kana.RomajiToKana
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionPoint
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import kotlin.math.max
import kotlin.math.min

class DictionaryFragment : Fragment() {

    private var _binding: FragmentDictionaryBinding? = null
    private val binding get() = _binding!!
    
    // We instantiate AndroidMlKitRecognizer here to inject it into ViewModel
    private val androidRecognizer = AndroidMlKitRecognizer()

    private val viewModel: DictionaryViewModel by activityViewModels {
        viewModelFactory {
            initializer {
                DictionaryViewModel(
                    handwritingRecognizer = androidRecognizer,
                    kanjiRepository = get(),
                    meaningRepository = get(),
                    settingsRepository = get()
                )
            }
        }
    }
    
    private lateinit var adapter: DictionaryAdapter
    private var textWatcher: TextWatcher? = null
    
    // Keep track of the last Ink object for UI rendering purposes only
    private var lastUiInk: Ink? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDictionaryBinding.inflate(inflater, container, false)
        adapter = DictionaryAdapter { item ->
            val action = DictionaryFragmentDirections.actionDictionaryToKanjiDetail(item.id)
            findNavController().navigate(action)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerDictionary.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDictionary.adapter = adapter

        setupFilterListeners()
        setupStateObservation()
        setupDrawingObservation()
        setupModelObservation()

        // Trigger initial load if needed
        viewModel.loadDictionaryData()
        
        // Restore UI State implicitly via StateFlow collection
        restoreUIState()
        
        // Ensure model is downloaded
        viewModel.downloadModel()
    }
    
    private fun restoreUIState() {
        binding.editSearch.setText(viewModel.textQuery)
        binding.editStrokeCount.setText(viewModel.strokeQuery)
        binding.checkExactMatch.isChecked = viewModel.exactMatch
        binding.radioGroupSearchMode.check(
            if (viewModel.searchMode == SearchMode.READING) R.id.radio_mode_reading else R.id.radio_mode_meaning
        )
    }

    private fun setupFilterListeners() {
        binding.buttonDrawSearch.setOnClickListener {
            // Check model status before showing dialog
            if (viewModel.modelStatus.value != ModelStatus.DOWNLOADED) {
                Toast.makeText(context, "Model not ready: ${viewModel.modelStatus.value}", Toast.LENGTH_SHORT).show()
                viewModel.downloadModel() // Try again
                return@setOnClickListener
            }
            
            val dialog = DrawingDialogFragment()
            dialog.onInkDrawn = { ink ->
                lastUiInk = ink
                updateDrawingThumbnail()
                val strokes = convertInkToStrokes(ink)
                Log.i("DictionaryFragment", "Submitting ${strokes.size} strokes for recognition")
                viewModel.recognizeInk(strokes)
            }
            dialog.show(parentFragmentManager, "DrawingDialog")
        }

        binding.buttonClearDrawing.setOnClickListener {
            lastUiInk = null
            updateDrawingThumbnail()
            viewModel.clearDrawingFilter()
        }

        binding.buttonApplyFilters.visibility = View.GONE

        binding.radioGroupSearchMode.setOnCheckedChangeListener { _, checkedId ->
            viewModel.searchMode = if (checkedId == R.id.radio_mode_reading) SearchMode.READING else SearchMode.MEANING
            viewModel.applyFilters()
        }

        binding.editStrokeCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.strokeQuery = s.toString()
                viewModel.applyFilters()
            }
        })
        
        binding.checkExactMatch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.exactMatch = isChecked
            viewModel.applyFilters()
        }

        setupTextWatcher()
    }
    
    private fun convertInkToStrokes(ink: Ink): List<RecognitionStroke> {
        val strokes = mutableListOf<RecognitionStroke>()
        for (inkStroke in ink.strokes) {
            val points = mutableListOf<RecognitionPoint>()
            for (inkPoint in inkStroke.points) {
                points.add(RecognitionPoint(inkPoint.x, inkPoint.y, inkPoint.timestamp ?: 0L))
            }
            strokes.add(RecognitionStroke(points))
        }
        return strokes
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
                viewModel.textQuery = binding.editSearch.text.toString()
                viewModel.applyFilters()
            }
        }
        binding.editSearch.addTextChangedListener(textWatcher)
    }
    
    private fun setupStateObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.lastResults.collect { results ->
                        adapter.submitList(results)
                        binding.textResultCount.text = getString(R.string.dictionary_results_count_format, results.size)
                    }
                }
            }
        }
    }
    
    private fun setupModelObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                 viewModel.modelStatus.collect { status ->
                     Log.d("DictionaryFragment", "Model Status: $status")
                     // Could update a UI indicator here (e.g., a small icon)
                 }
            }
        }
    }
    
    private fun setupDrawingObservation() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.recognitionResults.collect { candidates ->
                     if (candidates == null) {
                         if (lastUiInk != null) {
                             lastUiInk = null
                             updateDrawingThumbnail()
                         }
                     } else {
                         // Results received
                         Log.i("DictionaryFragment", "Recognition results received: ${candidates.size} candidates")
                         val strokes = viewModel.lastStrokes
                         if (strokes != null) {
                             lastUiInk = strokesToInk(strokes)
                             updateDrawingThumbnail()
                         }
                     }
                }
            }
        }
    }

    private fun strokesToInk(strokes: List<RecognitionStroke>): Ink {
        val builder = Ink.builder()
        for (stroke in strokes) {
            val inkStrokeBuilder = Ink.Stroke.builder()
            for (point in stroke.points) {
                inkStrokeBuilder.addPoint(Ink.Point.create(point.x, point.y, point.t))
            }
            builder.addStroke(inkStrokeBuilder.build())
        }
        return builder.build()
    }

    private fun updateDrawingThumbnail() {
        if (lastUiInk != null) {
            binding.cardDrawingThumbnail.visibility = View.VISIBLE
            binding.buttonClearDrawing.visibility = View.VISIBLE
            val bitmap = renderInkToBitmap(lastUiInk!!, 100)
            binding.imageDrawingThumbnail.setImageBitmap(bitmap)
        } else {
            binding.cardDrawingThumbnail.visibility = View.GONE
            binding.buttonClearDrawing.visibility = View.GONE
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
