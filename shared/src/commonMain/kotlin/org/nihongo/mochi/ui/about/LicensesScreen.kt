package org.nihongo.mochi.ui.about

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.nihongo.mochi.domain.kana.ResourceLoader
import org.nihongo.mochi.presentation.MochiBackground
import org.nihongo.mochi.ui.components.MochiWebView
import org.nihongo.mochi.ui.theme.AppTheme

@Serializable
data class LicenseReport(
    val dependencies: List<DependencyInfo> = emptyList()
)

@Serializable
data class DependencyInfo(
    val moduleName: String? = null,
    val moduleUrl: String? = null,
    val moduleVersion: String? = null,
    val moduleLicense: String? = null,
    val moduleLicenseUrl: String? = null
)

@Composable
fun LicensesScreen(
    onBackClick: () -> Unit
) {
    val resourceLoader: ResourceLoader = koinInject()
    var htmlContent by remember { mutableStateOf("Loading...") }

    LaunchedEffect(Unit) {
        val actualJson = try {
            resourceLoader.loadJson("licenses.json")
        } catch (e: Exception) {
            null
        }

        val automatedDeps = if (actualJson != null && actualJson != "{}" && actualJson.length > 10) {
            try {
                val report = Json { ignoreUnknownKeys = true }.decodeFromString<LicenseReport>(actualJson)
                report.dependencies
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        val allDeps = getManualLicenses() + automatedDeps
        htmlContent = generateLicensesHtml(allDeps)
    }

    AppTheme {
        MochiBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Open Source Licenses",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                MochiWebView(
                    html = htmlContent,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Add your manual data/asset licenses here
 */
private fun getManualLicenses(): List<DependencyInfo> {
    return listOf(
        DependencyInfo(
            moduleName = "Kanji Data derived/corrected from Davidluzgouveia original work",
            moduleUrl = "https://github.com/davidluzgouveia/kanji-data",
            moduleLicense = "MIT License",
            moduleLicenseUrl = "https://github.com/davidluzgouveia/kanji-data/blob/master/LICENSE"
        ),
        DependencyInfo(
            moduleName = "JMDict / KANJIDIC",
            moduleUrl = "http://www.edrdg.org/edrdg/licence.html",
            moduleLicense = "Creative Commons Attribution-ShareAlike 3.0",
            moduleLicenseUrl = "https://creativecommons.org/licenses/by-sa/3.0/"
        ),
        DependencyInfo(
            moduleName = "KanjiStrokOrders TTF font",
            moduleUrl = "https://www.nihilist.org.uk/",
            moduleLicense = "Copyright (C) 2004-2020 Ulrich Apel, the AAAA project and the Wadoku project\n" +
                    "All rights reserved.",
            moduleLicenseUrl = "https://drive.google.com/uc?export=download&id=17U7mLrzGgfX9BZhU8mcUqqop-J14MPKI"
        ),
        DependencyInfo(
            moduleName = "notosansjp_regular TTF font",
            moduleUrl = "https://fonts.google.com/noto/specimen/Noto+Sans+JP",
            moduleLicense = "SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007",
            moduleLicenseUrl = "https://fonts.google.com/noto/specimen/Noto+Sans+JP/license"
        ),
        DependencyInfo(
            moduleName = "Game sounds",
            moduleUrl = "https://freesound.org/",
            moduleLicense = "SIL OPEN FONT LICENSE Version 1.1 - 26 February 2007",
            moduleLicenseUrl = "https://creativecommons.org/publicdomain/zero/1.0/"
        )
        // Add more here...
    )
}

private fun generateLicensesHtml(dependencies: List<DependencyInfo>): String {
    val body = dependencies.distinctBy { it.moduleName }.joinToString("<br><hr><br>") { dep ->
        """
        <div style="margin-bottom: 16px;">
            <b style="font-size: 1.1em;">${dep.moduleName ?: "Unknown"}</b><br>
            ${if (dep.moduleVersion != null) "Version: ${dep.moduleVersion}<br>" else ""}
            ${if (dep.moduleLicenseUrl != null) "<a href='${dep.moduleLicenseUrl}'>${dep.moduleLicense ?: "License"}</a>" else (dep.moduleLicense ?: "")}
        </div>
        """.trimIndent()
    }

    return """
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: sans-serif; padding: 16px; line-height: 1.5; color: #333; background-color: transparent; }
                a { color: #2196F3; text-decoration: none; }
                hr { border: 0; border-top: 1px solid #eee; margin: 0px 0; }
            </style>
        </head>
        <body>
            $body
        </body>
        </html>
    """.trimIndent()
}
