package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import androidx.compose.ui.text.font.FontFamily
import org.nihongo.mochi.presentation.dictionary.KanjiDetailViewModel.ComponentNode
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.atan2

// Visual Node structure for drawing
private data class VisualNode(
    val character: String,
    val x: Float,
    val y: Float,
    val level: Int,
    val id: String? = null,
    val onReadings: List<String> = emptyList()
)

@Composable
fun KanjiGraphComponent(
    rootNode: ComponentNode,
    modifier: Modifier = Modifier
) {
    // Dynamic Theme colors
    val nodeColorMain = MaterialTheme.colorScheme.tertiary
    val nodeColorSub = MaterialTheme.colorScheme.primary
    val textColor = Color.White // Text inside circles (usually keep white if bubbles are colored)
    val edgeColor = MaterialTheme.colorScheme.onSurface
    val labelBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    val kanjiFont = FontFamily(Font(Res.font.KanjiStrokeOrders))
    val textMeasurer = rememberTextMeasurer()

    // Build the layout (Pyramid / Tree)
    val (visualNodes, visualEdges) = remember(rootNode) {
        val nodes = mutableListOf<VisualNode>()
        val edges = mutableListOf<Pair<VisualNode, VisualNode>>()
        
        val levelHeight = 300f
        val siblingSpacing = 200f
        
        fun layoutNode(node: ComponentNode, x: Float, y: Float, level: Int): VisualNode {
            val vNode = VisualNode(
                character = node.character, 
                x = x, 
                y = y, 
                level = level, 
                id = node.id,
                onReadings = node.onReadings
            )
            nodes.add(vNode)
            
            if (node.children.isNotEmpty()) {
                val totalWidth = node.children.size * siblingSpacing
                var startX = x - totalWidth / 2f + siblingSpacing / 2f
                
                node.children.forEach { child ->
                    val childVNode = layoutNode(child, startX, y + levelHeight, level + 1)
                    edges.add(vNode to childVNode)
                    startX += siblingSpacing
                }
            }
            return vNode
        }

        layoutNode(rootNode, 0f, 0f, 0)
        nodes to edges
    }

    // Collect all ON readings from the root to check against children
    val rootOnReadings = remember(rootNode) {
        rootNode.onReadings.toSet()
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Calculate bounds to auto-fit
            val minX = visualNodes.minOfOrNull { it.x } ?: 0f
            val maxX = visualNodes.maxOfOrNull { it.x } ?: 0f
            val maxY = visualNodes.maxOfOrNull { it.y } ?: 0f
            
            val graphWidth = maxX - minX + 150.dp.toPx() // + padding
            val graphHeight = maxY + 150.dp.toPx()
            
            val scaleX = size.width / graphWidth
            val scaleY = size.height / graphHeight
            val autoScale = minOf(scaleX, scaleY, 0.8f) // Cap scale at 0.8
            
            val initialCenter = Offset(size.width / 2, 60.dp.toPx())
            
            // Draw Edges
            visualEdges.forEach { (parent, child) ->
                // Reverse edge direction visually: Draw from child (to) to parent (from)
                val start = initialCenter + Offset(child.x, child.y) * autoScale
                val end = initialCenter + Offset(parent.x, parent.y) * autoScale
                
                // Check for shared ON reading
                val sharedOnReading = child.onReadings.find { it in rootOnReadings }
                val isImportantPath = sharedOnReading != null
                
                val currentStrokeWidth = if (isImportantPath) 4.dp.toPx() * autoScale else 2.dp.toPx() * autoScale
                val currentArrowScale = if (isImportantPath) 2.0f else 1.0f

                drawLine(
                    color = edgeColor,
                    start = start,
                    end = end,
                    strokeWidth = currentStrokeWidth,
                    alpha = 0.8f 
                )
                
                // Draw arrow head
                val arrowPath = Path().apply {
                    val dx = (end.x - start.x).toDouble()
                    val dy = (end.y - start.y).toDouble()
                    val angle = atan2(dy, dx)
                    val arrowSize = 15f * autoScale * currentArrowScale
                    
                    val nodeRadius = 30.dp.toPx() * autoScale
                    
                    val endX = (end.x - cos(angle) * nodeRadius).toFloat()
                    val endY = (end.y - sin(angle) * nodeRadius).toFloat()
                    
                    moveTo(endX, endY)
                    
                    val p1X = (endX - arrowSize * cos(angle - 0.5)).toFloat()
                    val p1Y = (endY - arrowSize * sin(angle - 0.5)).toFloat()
                    lineTo(p1X, p1Y)
                    
                    val p2X = (endX - arrowSize * cos(angle + 0.5)).toFloat()
                    val p2Y = (endY - arrowSize * sin(angle + 0.5)).toFloat()
                    lineTo(p2X, p2Y)
                    
                    close()
                }
                drawPath(path = arrowPath, color = edgeColor)

                // Draw shared reading text next to the arrow if exists
                if (isImportantPath && sharedOnReading != null) {
                    val midX = (start.x + end.x) / 2
                    val midY = (start.y + end.y) / 2
                    
                    val textLayoutResult = textMeasurer.measure(
                        text = sharedOnReading,
                        style = TextStyle(
                            fontSize = 16.sp * autoScale,
                            fontWeight = FontWeight.Bold,
                            color = labelTextColor
                        )
                    )
                    
                    // Draw background rectangle for text readability
                    val textPos = Offset(midX + 10f * autoScale, midY)
                    val padding = 4.dp.toPx() * autoScale
                    drawRect(
                        color = labelBackgroundColor,
                        topLeft = textPos - Offset(padding, padding),
                        size = Size(
                            textLayoutResult.size.width + padding * 2,
                            textLayoutResult.size.height + padding * 2
                        )
                    )
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textPos
                    )
                }
            }
            
            // Draw Nodes
            visualNodes.forEach { node ->
                val pos = initialCenter + Offset(node.x, node.y) * autoScale
                val radius = (if (node.level == 0) 40.dp else 30.dp).toPx() * autoScale
                
                val color = if (node.level == 0) nodeColorMain else nodeColorSub
                
                drawCircle(
                    color = color,
                    radius = radius,
                    center = pos
                )
                
                drawCircle(
                    color = Color.White,
                    radius = radius,
                    center = pos,
                    style = Stroke(width = 2.dp.toPx() * autoScale)
                )

                val fontSize = (if (node.level == 0) 32 else 24).sp * autoScale
                
                val textLayoutResult = textMeasurer.measure(
                    text = node.character,
                    style = TextStyle(
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = kanjiFont
                    )
                )
                
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = pos - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)
                )
            }
        }
    }
}
