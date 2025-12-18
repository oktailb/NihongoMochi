package org.oktail.kanjimori.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var languages: List<LanguageItem>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        languages = listOf(
            LanguageItem("fr_FR", getString(R.string.language_fr_fr), R.drawable.flag_fr_fr),
            LanguageItem("en_GB", getString(R.string.language_en_gb), R.drawable.flag_en_gb),
            LanguageItem("it_IT", getString(R.string.language_it_it), R.drawable.flag_it),
            LanguageItem("de_DE", getString(R.string.language_de_de), R.drawable.flag_de),
            LanguageItem("es_SP", getString(R.string.language_es_sp), R.drawable.flag_es),
            LanguageItem("bn_BN", getString(R.string.language_bn_bn), R.drawable.flag_bn),
            LanguageItem("th", getString(R.string.language_th), R.drawable.flag_th_th),
            LanguageItem("ar_AR", getString(R.string.language_ar_ar), R.drawable.flag_sa_sa),
            LanguageItem("pt_BR", getString(R.string.language_pt_br), R.drawable.flag_pt_br),
            LanguageItem("ko_KR", getString(R.string.language_ko_kr), R.drawable.flag_kr),
            LanguageItem("ru_RU", getString(R.string.language_ru_ru), R.drawable.flag_ru),
            LanguageItem("in_ID", getString(R.string.language_in_id), R.drawable.flag_id),
            LanguageItem("zh_CN", getString(R.string.language_zh_cn), R.drawable.flag_cn),
            LanguageItem("vi_VN", getString(R.string.language_vi_vn), R.drawable.flag_vn)
        )

        val adapter = LanguageAdapter(requireContext(), languages)
        binding.spinnerLanguage.adapter = adapter

        val currentLangCode = getCurrentAppLocale()
        val currentLangIndex = languages.indexOfFirst { it.code == currentLangCode }.takeIf { it != -1 } ?: 0
        binding.spinnerLanguage.setSelection(currentLangIndex, false)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]

                if (selectedLanguage.code != getCurrentAppLocale()) {
                    setAppLocale(selectedLanguage.code)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun getCurrentAppLocale(): String {
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("AppLocale", "fr_FR")!!
    }

    private fun setAppLocale(localeCode: String) {
        val sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("AppLocale", localeCode).apply()

        val localeTag = localeCode.replace('_', '-')
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}