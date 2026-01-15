package org.nihongo.mochi.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
actual fun MochiWebView(
    html: String,
    modifier: Modifier,
    isDarkMode: Boolean
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = false // Sécurité, pas besoin pour des leçons statiques
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(0) // Transparent par défaut
            }
        },
        update = { webView ->
            // On injecte un petit script de style pour s'assurer que le fond est correct
            // si le CSS ne le fait pas déjà.
            val bgColor = if (isDarkMode) "#121212" else "#FFFFFF"
            val fullHtml = """
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { background-color: $bgColor; margin: 0; padding: 0; }
                    </style>
                </head>
                <body>
                    $html
                </body>
                </html>
            """.trimIndent()
            
            webView.loadDataWithBaseURL(
                "file:///android_asset/", 
                fullHtml, 
                "text/html", 
                "utf-8", 
                null
            )
        }
    )
}
