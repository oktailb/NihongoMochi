package org.nihongo.mochi.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.presentation.MochiBackground

@Composable
fun GameSetupTemplate(
    title: String,
    subtitle: String,
    onPlayClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    MochiBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header (Titre puis sous titre) - Fixe
                Text(
                    text = title,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )

                Text(
                    text = subtitle,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 2 & 3. Config box & Scores box - Scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    content()
                    // Bottom spacer to ensure the last item isn't hidden by the PlayButton if scrolling
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Play Button - Position fixe en bas
                PlayButton(
                    onClick = onPlayClick,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
}
