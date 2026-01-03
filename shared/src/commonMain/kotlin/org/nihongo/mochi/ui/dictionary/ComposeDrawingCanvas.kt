package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ComposeDrawingCanvas(
    modifier: Modifier = Modifier,
    onStrokeComplete: (List<InkStroke>) -> Unit,
    clearTrigger: Int
) {
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val completedStrokes = remember { mutableStateOf<List<InkStroke>>(emptyList()) }
    val completedPaths = remember { mutableStateOf<List<Path>>(emptyList()) }

    LaunchedEffect(clearTrigger) {
        currentPoints = emptyList()
        completedStrokes.value = emptyList()
        completedPaths.value = emptyList()
    }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    currentPoints = listOf(offset)
                },
                onDrag = { change, _ ->
                    currentPoints = currentPoints + change.position
                },
                onDragEnd = {
                    val strokeBuilder = newStrokeBuilder()
                    currentPoints.forEach { offset ->
                        strokeBuilder.addPoint(createInkPoint(offset.x, offset.y, 0L))
                    }
                    val newStroke = strokeBuilder.build()
                    completedStrokes.value = completedStrokes.value + newStroke
                    onStrokeComplete(completedStrokes.value)

                    val newPath = Path()
                    if (currentPoints.isNotEmpty()) {
                        newPath.moveTo(currentPoints.first().x, currentPoints.first().y)
                        currentPoints.forEach { newPath.lineTo(it.x, it.y) }
                    }
                    completedPaths.value = completedPaths.value + newPath
                    currentPoints = emptyList()
                },
                onDragCancel = {
                    currentPoints = emptyList()
                }
            )
        }
    ) {
        val strokeStyle = Stroke(
            width = 12f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        completedPaths.value.forEach { path ->
            drawPath(
                path = path,
                color = Color.Black,
                style = strokeStyle
            )
        }

        if (currentPoints.isNotEmpty()) {
            val currentPath = Path()
            currentPath.moveTo(currentPoints.first().x, currentPoints.first().y)
            currentPoints.forEach { currentPath.lineTo(it.x, it.y) }
            drawPath(
                path = currentPath,
                color = Color.Black,
                style = strokeStyle
            )
        }
    }
}
