package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
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
import kotlin.math.pow
import kotlin.math.sqrt

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
    modifier: Modifier = Modifier,
    onNodeClick: (String) -> Unit
) {
    // Dynamic Theme colors
    val nodeColorMain = MaterialTheme.colorScheme.tertiary
    val nodeColorSub = MaterialTheme.colorScheme.primary
    val textColor = Color.White
    val edgeColor = MaterialTheme.colorScheme.onSurface
    val labelBackgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val labelTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val linkIndicatorColor = MaterialTheme.colorScheme.secondary

    val kanjiFont = FontFamily(Font(Res.font.KanjiStrokeOrders))
    val textMeasurer = rememberTextMeasurer()

    // Build the layout (Pyramid / Tree)
    val (visualNodes, visualEdges) = remember(rootNode) {
        val nodes = mutableListOf<VisualNode>()
        val edges = mutableListOf<Pair<VisualNode, VisualNode>>()
        
        val levelHeight = 300f
        val nodeUnitWidth = 250f 
        
        val subtreeWidths = mutableMapOf<ComponentNode, Float>()
        
        // 1. Calculate widths (Post-order traversal)
        fun calculateWidths(node: ComponentNode): Float {
            if (node.children.isEmpty()) {
                val w = nodeUnitWidth
                subtreeWidths[node] = w
                return w
            }
            var totalWidth = 0f
            node.children.forEach { child ->
                totalWidth += calculateWidths(child)
            }
            val w = maxOf(nodeUnitWidth, totalWidth)
            subtreeWidths[node] = w
            return w
        }
        
        calculateWidths(rootNode)
        
        // 2. Layout nodes (Pre-order traversal)
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
                val childrenSumWidth = node.children.sumOf { (subtreeWidths[it] ?: 0f).toDouble() }.toFloat()
                var currentX = x - childrenSumWidth / 2f
                
                node.children.forEach { child ->
                    val childWidth = subtreeWidths[child] ?: 0f
                    val childCenterX = currentX + childWidth / 2f
                    
                    val childVNode = layoutNode(child, childCenterX, y + levelHeight, level + 1)
                    edges.add(vNode to childVNode)
                    
                    currentX += childWidth
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

    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier.clipToBounds()) {
        val width = with(density) { maxWidth.toPx() }
        val height = with(density) { maxHeight.toPx() }
        val minVisibleEdge = with(density) { 50.dp.toPx() }

        // Zoom and Pan State
        var scale by remember { mutableStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        // Determine initial layout info (min/max of nodes in "auto-scaled" 1.0 space)
        val initialLayoutInfo = remember(visualNodes, width, height) {
            val minX = visualNodes.minOfOrNull { it.x } ?: 0f
            val maxX = visualNodes.maxOfOrNull { it.x } ?: 0f
            val maxY = visualNodes.maxOfOrNull { it.y } ?: 0f
            val minY = visualNodes.minOfOrNull { it.y } ?: 0f
            
            val padding = with(density) { 60.dp.toPx() }
            val graphWidth = (maxX - minX) + padding * 2
            val graphHeight = (maxY - minY) + padding * 2
            
            val scaleX = if (graphWidth > 0) width / graphWidth else 1f
            val scaleY = if (graphHeight > 0) height / graphHeight else 1f
            val autoScaleValue = minOf(scaleX, scaleY, 0.8f)
            
            val graphCenterXValue = (minX + maxX) / 2f
            val rootScreenPosValue = Offset(width / 2f, with(density) { 60.dp.toPx() })

            // Pre-calculate content bounds in autoScaled space (before user zoom/pan)
            val minX_as = rootScreenPosValue.x + (minX - graphCenterXValue) * autoScaleValue
            val maxX_as = rootScreenPosValue.x + (maxX - graphCenterXValue) * autoScaleValue
            val minY_as = rootScreenPosValue.y + (minY) * autoScaleValue
            val maxY_as = rootScreenPosValue.y + (maxY) * autoScaleValue

            object {
                val autoScale = autoScaleValue
                val graphCenterX = graphCenterXValue
                val rootScreenPos = rootScreenPosValue
                val contentMinX = minX_as
                val contentMaxX = maxX_as
                val contentMinY = minY_as
                val contentMaxY = maxY_as
            }
        }

        val state = rememberTransformableState { zoomChange, offsetChange, _ ->
            val newScale = (scale * zoomChange).coerceIn(0.5f, 5f)
            val viewportCenter = Offset(width / 2f, height / 2f)

            // Calculate current content boundaries in screen space with the NEW scale
            val left = (initialLayoutInfo.contentMinX - viewportCenter.x) * newScale + viewportCenter.x + (offset.x + offsetChange.x)
            val right = (initialLayoutInfo.contentMaxX - viewportCenter.x) * newScale + viewportCenter.x + (offset.x + offsetChange.x)
            val top = (initialLayoutInfo.contentMinY - viewportCenter.y) * newScale + viewportCenter.y + (offset.y + offsetChange.y)
            val bottom = (initialLayoutInfo.contentMaxY - viewportCenter.y) * newScale + viewportCenter.y + (offset.y + offsetChange.y)

            var finalOffsetChangeX = offsetChange.x
            var finalOffsetChangeY = offsetChange.y

            // Block movement if it makes the whole graph invisible
            if (offsetChange.x < 0 && right < minVisibleEdge) finalOffsetChangeX = 0f
            if (offsetChange.x > 0 && left > width - minVisibleEdge) finalOffsetChangeX = 0f
            if (offsetChange.y < 0 && bottom < minVisibleEdge) finalOffsetChangeY = 0f
            if (offsetChange.y > 0 && top > height - minVisibleEdge) finalOffsetChangeY = 0f

            scale = newScale
            offset += Offset(finalOffsetChangeX, finalOffsetChangeY)
        }

        // Reset offset and scale when rootNode changes
        LaunchedEffect(rootNode) {
            scale = 1f
            offset = Offset.Zero
        }

        fun toScreenFixedTop(x: Float, y: Float, currentScale: Float, currentOffset: Offset): Offset {
            val dx = x - initialLayoutInfo.graphCenterX
            val basePos = initialLayoutInfo.rootScreenPos + Offset(dx * initialLayoutInfo.autoScale, y * initialLayoutInfo.autoScale)
            val viewportCenter = Offset(width / 2f, height / 2f)
            return (basePos - viewportCenter) * currentScale + viewportCenter + currentOffset
        }

        Canvas(modifier = Modifier
            .fillMaxSize()
            .transformable(state = state)
            .pointerInput(visualNodes, initialLayoutInfo, scale, offset) {
                detectTapGestures { tapOffset ->
                    visualNodes.forEach { node ->
                        val pos = toScreenFixedTop(node.x, node.y, scale, offset)
                        val radius = (if (node.level == 0) 40.dp.toPx() else 30.dp.toPx()) * initialLayoutInfo.autoScale * scale
                        val hitRadius = radius * 1.5f
                        val distance = sqrt((tapOffset.x - pos.x).pow(2) + (tapOffset.y - pos.y).pow(2))
                        if (distance <= hitRadius) {
                             if (!node.id.isNullOrEmpty()) {
                                 onNodeClick(node.character)
                             }
                        }
                    }
                }
            }
        ) {
            // Draw Edges
            visualEdges.forEach { (parent, child) ->
                val start = toScreenFixedTop(child.x, child.y, scale, offset)
                val end = toScreenFixedTop(parent.x, parent.y, scale, offset)
                val sharedOnReading = child.onReadings.find { it in rootOnReadings }
                val isImportantPath = sharedOnReading != null
                val currentStrokeWidth = if (isImportantPath) 4.dp.toPx() * initialLayoutInfo.autoScale * scale else 2.dp.toPx() * initialLayoutInfo.autoScale * scale
                val currentArrowScale = if (isImportantPath) 2.0f else 1.0f

                drawLine(color = edgeColor, start = start, end = end, strokeWidth = currentStrokeWidth, alpha = 0.8f)
                
                val arrowPath = Path().apply {
                    val dx = (end.x - start.x).toDouble()
                    val dy = (end.y - start.y).toDouble()
                    val angle = atan2(dy, dx)
                    val arrowSize = 15f * initialLayoutInfo.autoScale * scale * currentArrowScale
                    val nodeRadius = 30.dp.toPx() * initialLayoutInfo.autoScale * scale
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

                if (isImportantPath && sharedOnReading != null) {
                    val midX = (start.x + end.x) / 2
                    val midY = (start.y + end.y) / 2
                    val textLayoutResult = textMeasurer.measure(
                        text = sharedOnReading,
                        style = TextStyle(fontSize = 16.sp * initialLayoutInfo.autoScale * scale, fontWeight = FontWeight.Bold, color = labelTextColor)
                    )
                    val textPos = Offset(midX + 10f * initialLayoutInfo.autoScale * scale, midY)
                    val textPadding = 4.dp.toPx() * initialLayoutInfo.autoScale * scale
                    drawRect(
                        color = labelBackgroundColor,
                        topLeft = textPos - Offset(textPadding, textPadding),
                        size = Size(textLayoutResult.size.width + textPadding * 2, textLayoutResult.size.height + textPadding * 2)
                    )
                    drawText(textLayoutResult = textLayoutResult, topLeft = textPos)
                }
            }
            
            // Draw Nodes
            visualNodes.forEach { node ->
                val pos = toScreenFixedTop(node.x, node.y, scale, offset)
                val baseRadius = if (node.level == 0) 40.dp.toPx() else 30.dp.toPx()
                val radius = baseRadius * initialLayoutInfo.autoScale * scale
                val color = if (node.level == 0) nodeColorMain else nodeColorSub
                
                drawCircle(color = color, radius = radius, center = pos)
                drawCircle(color = Color.White, radius = radius, center = pos, style = Stroke(width = 2.dp.toPx() * initialLayoutInfo.autoScale * scale))

                val fontSize = (if (node.level == 0) 32 else 24).sp * initialLayoutInfo.autoScale * scale
                val textLayoutResult = textMeasurer.measure(
                    text = node.character,
                    style = TextStyle(fontSize = fontSize, fontWeight = FontWeight.Bold, color = textColor, fontFamily = kanjiFont)
                )
                drawText(textLayoutResult = textLayoutResult, topLeft = pos - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f))

                if (!node.id.isNullOrEmpty()) {
                    val indicatorRadius = 8.dp.toPx() * initialLayoutInfo.autoScale * scale
                    val angle = -45.0 * (Math.PI / 180.0) 
                    val indicatorCenter = pos + Offset((radius * cos(angle)).toFloat(), (radius * sin(angle)).toFloat())
                    drawCircle(color = Color.White, radius = indicatorRadius, center = indicatorCenter)
                    drawCircle(color = linkIndicatorColor, radius = indicatorRadius * 0.7f, center = indicatorCenter)
                }
            }
        }
    }
}
