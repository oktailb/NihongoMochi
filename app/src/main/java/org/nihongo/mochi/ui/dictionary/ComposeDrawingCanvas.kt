package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.google.mlkit.vision.digitalink.Ink

@Composable
fun ComposeDrawingCanvas(
    modifier: Modifier = Modifier,
    inkBuilder: Ink.Builder, // External builder to keep state if needed, or we manage it here
    onInkUpdate: (Ink) -> Unit
) {
    // Visual paths
    val paths = remember { mutableStateListOf<Path>() }
    // We keep a temporary path for the current stroke being drawn
    val currentPath = remember { Path() }
    
    // Trigger to force recomposition when currentPath is updated internally
    val pathUpdateTrigger = remember { mutableStateOf(0L) }
    
    // We need to track the current Ink stroke builder
    // We use a ref to hold mutable non-compose state
    val currentStrokeBuilder = remember { androidx.compose.runtime.mutableStateOf<Ink.Stroke.Builder?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // Start visual path
                        currentPath.reset()
                        currentPath.moveTo(offset.x, offset.y)
                        pathUpdateTrigger.value++
                        
                        // Start Ink stroke
                        val t = System.currentTimeMillis()
                        val sb = Ink.Stroke.builder()
                        sb.addPoint(Ink.Point.create(offset.x, offset.y, t))
                        currentStrokeBuilder.value = sb
                    },
                    onDrag = { change, _ ->
                        val pos = change.position
                        // Update visual path
                        currentPath.lineTo(pos.x, pos.y)
                        pathUpdateTrigger.value++
                        
                        // Update Ink stroke
                        val t = System.currentTimeMillis()
                        currentStrokeBuilder.value?.addPoint(Ink.Point.create(pos.x, pos.y, t))
                    },
                    onDragEnd = {
                        // Finalize visual path
                        paths.add(Path().apply { addPath(currentPath) })
                        currentPath.reset()
                        pathUpdateTrigger.value++
                        
                        // Finalize Ink stroke
                        currentStrokeBuilder.value?.let { sb ->
                            inkBuilder.addStroke(sb.build())
                            onInkUpdate(inkBuilder.build())
                        }
                        currentStrokeBuilder.value = null
                    },
                    onDragCancel = {
                        currentPath.reset()
                        pathUpdateTrigger.value++
                        currentStrokeBuilder.value = null
                    }
                )
            }
    ) {
        // Read the trigger to ensure recomposition happens when it changes
        pathUpdateTrigger.value
        
        // Draw completed paths
        paths.forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = Stroke(
                    width = 12f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
        
        // Draw current path
        drawPath(
            path = currentPath,
            color = Color.Black,
            style = Stroke(
                width = 12f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
