package org.nihongo.mochi.ui.grammar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.max
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.toori
import org.nihongo.mochi.ui.ResourceUtils

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grammar Tree") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
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
                    val estimatedHeight = (nodes.size * 120).dp + (separators.size * 200).dp
                    val minCanvasHeight = 2000.dp
                    val canvasHeight = max(minCanvasHeight, estimatedHeight)
                    
                    val canvasWidth = maxWidth
                    
                    val nodesById = remember(nodes) { nodes.associateBy { it.rule.id } }
                    
                    // Get colors outside the Canvas scope
                    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
                    val separatorLineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(canvasHeight)
                    ) {
                        // Draw Connections
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val canvasW = size.width
                            val canvasH = size.height

                            nodes.forEach { node ->
                                node.rule.dependencies.forEach { dependencyId ->
                                    val parentNode = nodesById[dependencyId]
                                    if (parentNode != null) {
                                        val start = Offset(
                                            x = parentNode.x * canvasW,
                                            y = parentNode.y * canvasH
                                        )
                                        val end = Offset(
                                            x = node.x * canvasW,
                                            y = node.y * canvasH
                                        )
                                        drawLine(
                                            color = lineColor,
                                            start = start,
                                            end = end,
                                            strokeWidth = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    }
                                }
                            }
                        }

                        // Draw Separators (Toori Gates)
                        separators.forEach { separator ->
                            val yPosPx = separator.y * canvasHeight.value
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .offset(y = (yPosPx).dp)
                                    .background(
                                        color = separatorLineColor,
                                        shape = RoundedCornerShape(1.dp)
                                    )
                            )
                            
                            Image(
                                painter = painterResource(Res.drawable.toori),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.TopCenter)
                                    .offset(y = (yPosPx - 30).dp), // Centered on the line
                                contentScale = ContentScale.Fit
                            )
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
