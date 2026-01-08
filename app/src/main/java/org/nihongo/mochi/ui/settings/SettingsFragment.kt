package org.nihongo.mochi.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.nihongo.mochi.presentation.settings.SettingsViewModel
import org.nihongo.mochi.ui.theme.AppTheme

class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    SettingsScreen(
                        viewModel = viewModel,
                        onThemeChanged = { isDark -> changeTheme(isDark) },
                        onLocaleChanged = { newLocale -> changeLocale(newLocale) }
                    )
                }
            }
        }
    }

    private fun changeTheme(isDark: Boolean) {
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun changeLocale(localeCode: String) {
        // HACK: Force synchronous commit to SharedPreferences
        // This ensures that when MainActivity restarts, it sees the NEW value immediately.
        // We use the same file name "AppSettings" as defined in MochiApplication
        val prefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        prefs.edit().putString("AppLocale", localeCode).commit()

        // Apply to Android System
        val localeTag = localeCode.replace('_', '-')
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}
