package org.nihongo.mochi.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.nihongo.mochi.BuildConfig
import org.nihongo.mochi.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AboutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val sdf = SimpleDateFormat("dd MMM. yyyy HH:mm:ss", Locale.getDefault())
                val currentDate = sdf.format(Date())

                AppTheme {
                    AboutScreen(
                        versionName = BuildConfig.VERSION_NAME,
                        currentDate = currentDate,
                        onIssueTrackerClick = { openUrl("https://github.com/oktailb/KanjiMori/issues") },
                        onRateAppClick = { openUrl("market://details?id=${requireContext().packageName}") },
                        onPatreonClick = { openUrl("https://www.patreon.com/Oktail") },
                        onTipeeeClick = { openUrl("https://en.tipeee.com/lecoq-vincent") },
                        onKanjiDataClick = { openUrl("https://github.com/davidluzgouveia/kanji-data") }
                    )
                }
            }
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle exception if no browser or market app is available
        }
    }
}
