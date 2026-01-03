package org.nihongo.mochi.ui.dictionary

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.ui.theme.AppTheme
import kotlin.math.max
import kotlin.math.min

class DictionaryFragment : Fragment() {

    private val viewModel: DictionaryViewModel by viewModel()

    private val showDrawingDialog = mutableStateOf(false)
    private val drawingBitmapState = mutableStateOf<Bitmap?>(null)
    private var lastStrokes: List<RecognitionStroke>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    DictionaryScreen(
                        viewModel = viewModel,
                        drawingBitmap = drawingBitmapState.value?.asImageBitmap(),
                        onOpenDrawing = { showDrawingDialog.value = true },
                        onClearDrawing = { clearDrawing() },
                        onItemClick = { item ->
                            val action = DictionaryFragmentDirections.actionDictionaryToKanjiDetail(item.id)
                            findNavController().navigate(action)
                        }
                    )

                    if (showDrawingDialog.value) {
                        ComposeDrawingDialog(
                            viewModel = viewModel,
                            onDismiss = { showDrawingDialog.value = false },
                            onConfirm = {
                                // The drawing is already submitted on stroke completion
                                // This is now just to confirm/close the dialog
                                showDrawingDialog.value = false
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadDictionaryData()
        
        // Observe strokes from ViewModel to render thumbnail
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.lastStrokes.collect { strokes ->
                    lastStrokes = strokes
                    updateDrawingThumbnail()
                }
            }
        }
    }
    
    private fun clearDrawing() {
        viewModel.clearDrawingFilter()
        lastStrokes = null
        updateDrawingThumbnail()
    }

    private fun updateDrawingThumbnail() {
        drawingBitmapState.value = lastStrokes?.let { renderStrokesToBitmap(it, 100) }
    }
    
    private fun renderStrokesToBitmap(strokes: List<RecognitionStroke>, targetSize: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT)

        if (strokes.isEmpty()) return bitmap

        var minX = Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxX = Float.MIN_VALUE
        var maxY = Float.MIN_VALUE

        for (stroke in strokes) {
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
            strokeWidth = 3f / scale
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        for (stroke in strokes) {
            val path = Path()
            stroke.points.firstOrNull()?.let { path.moveTo(it.x, it.y) }
            for (i in 1 until stroke.points.size) {
                path.lineTo(stroke.points[i].x, stroke.points[i].y)
            }
            canvas.drawPath(path, paint)
        }
        return bitmap
    }
}
