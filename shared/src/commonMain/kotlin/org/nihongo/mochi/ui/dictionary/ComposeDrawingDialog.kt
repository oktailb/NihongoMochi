package org.nihongo.mochi.ui.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.nihongo.mochi.domain.dictionary.DictionaryViewModel
import org.nihongo.mochi.domain.recognition.ModelStatus
import org.nihongo.mochi.domain.recognition.RecognitionPoint
import org.nihongo.mochi.domain.recognition.RecognitionStroke
import org.nihongo.mochi.shared.generated.resources.Res
import org.nihongo.mochi.shared.generated.resources.*

@Composable
fun ComposeDrawingDialog(
    viewModel: DictionaryViewModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val modelStatus by viewModel.modelStatus.collectAsState()

    // Automatically trigger download if the model is not downloaded or failed
    LaunchedEffect(modelStatus) {
        if (modelStatus == ModelStatus.NOT_DOWNLOADED || modelStatus == ModelStatus.FAILED) {
            viewModel.downloadModel()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.dictionary_draw_kanji),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (modelStatus) {
                    ModelStatus.NOT_DOWNLOADED, ModelStatus.DOWNLOADING, ModelStatus.FAILED -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusText = when (modelStatus) {
                            ModelStatus.DOWNLOADING -> "Downloading model..."
                            ModelStatus.FAILED -> "Download failed. Retrying..."
                            else -> "Initializing model..."
                        }
                        Text(statusText)
                    }
                    ModelStatus.DOWNLOADED -> {
                        var clearTrigger by remember { mutableStateOf(0) }
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                .background(Color.White)
                        ) {
                            ComposeDrawingCanvas(
                                onStrokeComplete = { strokes ->
                                    val recognitionStrokes = strokes.map { inkStroke ->
                                        val points = inkStroke.getPoints().map { inkPoint ->
                                            RecognitionPoint(inkPoint.x, inkPoint.y, inkPoint.t)
                                        }
                                        RecognitionStroke(points)
                                    }
                                    viewModel.recognizeInk(recognitionStrokes)
                                },
                                clearTrigger = clearTrigger
                            )
                            
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .clickable { 
                                        clearTrigger++ 
                                        viewModel.clearDrawingFilter()
                                    },
                                tint = Color.Gray
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val results by viewModel.recognitionResults.collectAsState()
                        
                        if (results != null) {
                            Text("Candidates: ${results!!.joinToString(", ")}")
                        } else {
                            Text("Draw something...")
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row {
                            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = { 
                                onConfirm()
                                onDismiss()
                            }, modifier = Modifier.weight(1f)) {
                                Text(stringResource(Res.string.dictionary_recognize))
                            }
                        }
                    }
                }
            }
        }
    }
}
