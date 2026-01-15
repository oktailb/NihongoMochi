package org.nihongo.mochi.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun MochiWebView(
    html: String,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
)
