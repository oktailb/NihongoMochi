package org.nihongo.mochi.ui.grammar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sign

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammarScreen(
    viewModel: GrammarViewModel,
    onBackClick: () -> Unit
) {
    val nodes by viewModel.nodes.collectAsState()
    val separators by viewModel.separators.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    Scaffold(
        // No TopBar
    ) { paddingValues ->
        MochiBackground {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(scrollState)
                ) {
                    val estimatedHeight = (nodes.size * 120).dp + (separators.size * 250).dp + 300.dp
                    val minCanvasHeight = 2000.dp
                    val canvasHeight = max(minCanvasHeight, estimatedHeight)
                    
                    val canvasWidth = maxWidth
                    
                    val nodesById = remember(nodes) { nodes.associateBy { it.rule.id } }
                    val nodesByLevel = remember(nodes) { nodes.groupBy { it.rule.level } }
                    
                    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    val separatorLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                    val nodeWidthDp = 100.dp
                    val nodeHeightDp = 60.dp
                    val nodeHalfWidthPx = with(density) { (nodeWidthDp / 2).toPx() }
                    // Used implicitly for visual vertical centering calculations
                    val nodeHeightPx = with(density) { nodeHeightDp.toPx() } 
                    val centerChannelPadding = with(density) { 12.dp.toPx() }
                    val cornerRadius = with(density) { 32.dp.toPx() }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(canvasHeight)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height
                            val centerX = canvasW / 2

                            // 1. Draw Links from Nodes to their Level's Toori (Structural, Faint)
                            separators.forEach { separator ->
                                val gateY = separator.y * canvasH
                                val levelNodes = nodesByLevel[separator.levelId] ?: emptyList()
                                
                                levelNodes.forEach { node ->
                                    val childAnchorX = if (node.x < 0.5f) {
                                        (node.x * canvasW) + nodeHalfWidthPx // Right edge
                                    } else {
                                        (node.x * canvasW) - nodeHalfWidthPx // Left edge
                                    }
                                    val childCenterY = node.y * canvasH
                                    
                                    val childChannelX = if (node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                    
                                    val path = Path()
                                    path.moveTo(childAnchorX, childCenterY)
                                    
                                    val distY = gateY - childCenterY
                                    val dir = sign(distY)

                                    if (abs(distY) > cornerRadius * 1.5f) {
                                        val turnY = childCenterY + (dir * cornerRadius)
                                        // 1. Curve into channel
                                        path.quadraticBezierTo(childChannelX, childCenterY, childChannelX, turnY)
                                        // 2. Vertical line to Gate Y
                                        path.lineTo(childChannelX, gateY)
                                    } else {
                                        // Too close: S-Curve
                                        path.cubicTo(
                                            childChannelX, childCenterY, 
                                            childChannelX, gateY, 
                                            childChannelX, gateY
                                        )
                                    }
                                    
                                    drawPath(
                                        path = path,
                                        color = lineColor.copy(alpha = 0.2f), // Very faint
                                        style = Stroke(
                                            width = 1.dp.toPx(),
                                            pathEffect = null // Solid line, no dashes
                                        )
                                    )
                                }
                            }

                            // 2. Draw Dependency Connections
                            nodes.forEach { node ->
                                node.rule.dependencies.forEach { dependencyId ->
                                    val parentNode = nodesById[dependencyId]
                                    if (parentNode != null) {
                                        val parentCenterY = parentNode.y * canvasH
                                        val childCenterY = node.y * canvasH
                                        
                                        // Robust check for Inter-Level Dependency
                                        val isInterLevel = parentNode.rule.level != node.rule.level
                                        
                                        val path = Path()

                                        if (isInterLevel) {
                                            // Inter-level: Pipe Style via Gate
                                            val gate = separators
                                                .filter { it.y < node.y }
                                                .maxByOrNull { it.y }
                                            
                                            if (gate != null) {
                                                val gateY = gate.y * canvasH
                                                
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                val childAnchorY = childCenterY
                                                
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                val parentAnchorY = parentCenterY
                                                
                                                val parentChannelX = if(parentNode.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                // Part A: Parent -> Gate
                                                val pathParentToGate = Path()
                                                pathParentToGate.moveTo(parentAnchorX, parentAnchorY)

                                                val distToGate = gateY - parentAnchorY
                                                if (abs(distToGate) > cornerRadius * 1.5f) {
                                                    val turnY = if(distToGate > 0) parentAnchorY + cornerRadius else parentAnchorY - cornerRadius
                                                    pathParentToGate.quadraticBezierTo(parentChannelX, parentAnchorY, parentChannelX, turnY)
                                                    pathParentToGate.lineTo(parentChannelX, gateY)
                                                } else {
                                                    pathParentToGate.cubicTo(parentChannelX, parentAnchorY, centerX, parentAnchorY + distToGate * 0.5f, centerX, gateY)
                                                }
                                                
                                                drawPath(
                                                    path = pathParentToGate,
                                                    color = lineColor, 
                                                    style = Stroke(width = 2.dp.toPx())
                                                )

                                                // Part B: Gate -> Child
                                                val childChannelX = if(node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                path.moveTo(childChannelX, gateY)
                                                
                                                val distFromGate = childAnchorY - gateY
                                                if (abs(distFromGate) > cornerRadius * 1.5f) {
                                                     val turnY = childAnchorY - cornerRadius 
                                                     path.lineTo(childChannelX, turnY)
                                                     path.quadraticBezierTo(childChannelX, childAnchorY, childAnchorX, childAnchorY)
                                                } else {
                                                    path.cubicTo(childChannelX, gateY + distFromGate * 0.5f, childChannelX, childAnchorY, childAnchorX, childAnchorY)
                                                }
                                            }
                                        } else {
                                            // Intra-level
                                            val isCrossing = (parentNode.x < 0.5f) != (node.x < 0.5f)
                                            
                                            if (isCrossing) {
                                                // Pipe Style: Inner Side -> Center Channel -> Cross -> Center Channel -> Inner Side
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) + nodeHalfWidthPx else (parentNode.x * canvasW) - nodeHalfWidthPx
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) + nodeHalfWidthPx else (node.x * canvasW) - nodeHalfWidthPx
                                                
                                                val parentChannelX = if(parentNode.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                val childChannelX = if(node.x < 0.5f) centerX - centerChannelPadding else centerX + centerChannelPadding
                                                
                                                path.moveTo(parentAnchorX, parentCenterY)
                                                
                                                val distY = childCenterY - parentCenterY
                                                val dir = sign(distY)
                                                
                                                if (abs(distY) > cornerRadius * 2.5f) {
                                                    // Vertical Pipe with Crossing
                                                    val turn1Y = parentCenterY + (dir * cornerRadius)
                                                    val midY = parentCenterY + (distY / 2)
                                                    val turn2Y = childCenterY - (dir * cornerRadius)
                                                    
                                                    // 1. Enter Channel
                                                    path.quadraticBezierTo(parentChannelX, parentCenterY, parentChannelX, turn1Y)
                                                    // 2. Vertical to near Mid
                                                    path.lineTo(parentChannelX, midY - (dir * cornerRadius))
                                                    // 3. Cross Over (S-Curve)
                                                    path.cubicTo(parentChannelX, midY, childChannelX, midY, childChannelX, midY + (dir * cornerRadius))
                                                    // 4. Vertical to near Child
                                                    path.lineTo(childChannelX, turn2Y)
                                                    // 5. Exit Channel
                                                    path.quadraticBezierTo(childChannelX, childCenterY, childAnchorX, childCenterY)
                                                } else {
                                                    // Too close for full pipe: S-Curve across center
                                                    path.cubicTo(
                                                        parentChannelX, parentCenterY,
                                                        childChannelX, childCenterY,
                                                        childAnchorX, childCenterY
                                                    )
                                                }
                                            } else {
                                                // Pipe Style: Outer Side -> Outer Channel -> Vertical -> Outer Channel -> Outer Side
                                                val parentAnchorX = if (parentNode.x < 0.5f) (parentNode.x * canvasW) - nodeHalfWidthPx else (parentNode.x * canvasW) + nodeHalfWidthPx
                                                val childAnchorX = if (node.x < 0.5f) (node.x * canvasW) - nodeHalfWidthPx else (node.x * canvasW) + nodeHalfWidthPx
                                                
                                                val baseOffset = 40f
                                                val controlOffset = if (parentNode.x < 0.5f) -baseOffset else baseOffset
                                                val outerChannelX = parentAnchorX + controlOffset
                                                
                                                path.moveTo(parentAnchorX, parentCenterY)
                                                
                                                val distY = childCenterY - parentCenterY
                                                val dir = sign(distY)

                                                if (abs(distY) > cornerRadius * 2f) {
                                                    val turn1Y = parentCenterY + (dir * cornerRadius)
                                                    val turn2Y = childCenterY - (dir * cornerRadius)
                                                    
                                                    // 1. Enter Outer Channel
                                                    path.quadraticBezierTo(outerChannelX, parentCenterY, outerChannelX, turn1Y)
                                                    // 2. Vertical Line
                                                    path.lineTo(outerChannelX, turn2Y)
                                                    // 3. Exit Outer Channel
                                                    path.quadraticBezierTo(outerChannelX, childCenterY, childAnchorX, childCenterY)
                                                } else {
                                                    // Too close: Simple Bezier
                                                    path.cubicTo(
                                                        outerChannelX, parentCenterY,
                                                        outerChannelX, childCenterY,
                                                        childAnchorX, childCenterY
                                                    )
                                                }
                                            }
                                        }

                                        if (!path.isEmpty) {
                                            drawPath(
                                                path = path,
                                                color = lineColor,
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    pathEffect = null
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Draw Separators (Toori Gates + Text)
                        separators.forEach { separator ->
                            val yPosPx = separator.y * canvasHeight.value
                            val levelName = ResourceUtils.resolveStringResource("level_${separator.levelId}")?.let { 
                                stringResource(it) 
                            } ?: separator.levelId.uppercase()

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(y = (yPosPx).dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // The Line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(
                                            color = separatorLineColor,
                                            shape = RoundedCornerShape(1.dp)
                                        )
                                )
                                
                                // The Gate and Text
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.offset(y = (-180).dp)
                                ) {
                                    Image(
                                        painter = painterResource(Res.drawable.toori),
                                        contentDescription = null,
                                        modifier = Modifier.size(280.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                    
                                    Text(
                                        text = levelName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .offset(y = (-100).dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Draw Nodes
                        nodes.forEach { node ->
                            val xPosPx = node.x * canvasWidth.value
                            val yPosPx = node.y * canvasHeight.value
                            
                            GrammarNodeItem(
                                node = node,
                                modifier = Modifier
                                    .offset(
                                        x = (xPosPx - 50).dp, // Center horizontally (width 100)
                                        y = (yPosPx - 30).dp  // Center vertically (height 60)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GrammarNodeItem(
    node: GrammarNode,
    modifier: Modifier = Modifier
) {
    val description = ResourceUtils.resolveStringResource(node.rule.description)?.let { 
        stringResource(it) 
    } ?: node.rule.id

    Box(
        modifier = modifier
            .width(100.dp)
            .height(60.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = description,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 3,
            lineHeight = 12.sp
        )
    }
}
