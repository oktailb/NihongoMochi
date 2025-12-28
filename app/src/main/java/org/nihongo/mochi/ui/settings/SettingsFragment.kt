package org.nihongo.mochi.ui.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import org.nihongo.mochi.MochiApplication
import org.nihongo.mochi.R
import org.nihongo.mochi.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    
    private val settingsRepository get() = MochiApplication.settingsRepository

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

        setupLanguageSpinner()
        setupPronunciationRadioGroup()
        setupDefaultUserListCheckboxes()
        setupTextSizeSeekBar()
        setupAnimationSpeedSeekBar()
        setupThemeSwitch()
    }

    private fun setupLanguageSpinner() {
        languages = listOf(
            LanguageItem("ar_SA", getString(R.string.language_ar_ar), R.drawable.flag_sa_sa),
            LanguageItem("bn_BD", getString(R.string.language_bn_bn), R.drawable.flag_bn),
            LanguageItem("de_DE", getString(R.string.language_de_de), R.drawable.flag_de),
            LanguageItem("en_GB", getString(R.string.language_en_gb), R.drawable.flag_en_gb),
            LanguageItem("es_ES", getString(R.string.language_es_sp), R.drawable.flag_es),
            LanguageItem("fr_FR", getString(R.string.language_fr_fr), R.drawable.flag_fr_fr),
            LanguageItem("in_ID", getString(R.string.language_in_id), R.drawable.flag_id),
            LanguageItem("it_IT", getString(R.string.language_it_it), R.drawable.flag_it),
            LanguageItem("ko_KR", getString(R.string.language_ko_kr), R.drawable.flag_kr),
            LanguageItem("pt_BR", getString(R.string.language_pt_br), R.drawable.flag_pt_br),
            LanguageItem("ru_RU", getString(R.string.language_ru_ru), R.drawable.flag_ru),
            LanguageItem("th_TH", getString(R.string.language_th), R.drawable.flag_th_th),
            LanguageItem("vi_VN", getString(R.string.language_vi_vn), R.drawable.flag_vn),
            LanguageItem("zh_CN", getString(R.string.language_zh_cn), R.drawable.flag_cn),
            )

        val adapter = LanguageAdapter(requireContext(), languages)
        binding.spinnerLanguage.adapter = adapter

        val currentLangCode = settingsRepository.getAppLocale()
        val currentLangIndex = languages.indexOfFirst { it.code == currentLangCode }.takeIf { it != -1 } ?: 0
        binding.spinnerLanguage.setSelection(currentLangIndex, false)

        binding.spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedLanguage = languages[position]

                if (selectedLanguage.code != settingsRepository.getAppLocale()) {
                    setAppLocale(selectedLanguage.code)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupPronunciationRadioGroup() {
        val savedPronunciation = settingsRepository.getPronunciation()
        if (savedPronunciation == "Roman") {
            binding.radioGroupPronunciation.check(R.id.radio_roman_alphabet)
        } else {
            binding.radioGroupPronunciation.check(R.id.radio_hiragana)
        }

        binding.radioGroupPronunciation.setOnCheckedChangeListener { _, checkedId ->
            val pronunciation = if (checkedId == R.id.radio_roman_alphabet) "Roman" else "Hiragana"
            settingsRepository.setPronunciation(pronunciation)
        }
    }

    private fun setupDefaultUserListCheckboxes() {
        binding.checkboxAddWrongAnswers.isChecked = settingsRepository.shouldAddWrongAnswers()
        binding.checkboxRemoveGoodAnswers.isChecked = settingsRepository.shouldRemoveGoodAnswers()

        binding.checkboxAddWrongAnswers.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setAddWrongAnswers(isChecked)
        }

        binding.checkboxRemoveGoodAnswers.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setRemoveGoodAnswers(isChecked)
        }
    }

    private fun setupTextSizeSeekBar() {
        binding.seekbarTextSize.max = 39
        
        val savedTextSize = settingsRepository.getTextSize()
        binding.seekbarTextSize.progress = ((savedTextSize * 10) - 1).toInt().coerceIn(0, 39)
        binding.textTextSizeValue.text = "%.1fx".format(savedTextSize)

        binding.seekbarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newTextSize = (progress + 1) / 10.0f
                binding.textTextSizeValue.text = "%.1fx".format(newTextSize)
                if (fromUser) {
                    settingsRepository.setTextSize(newTextSize)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupAnimationSpeedSeekBar() {
        binding.seekbarAnimationSpeed.max = 39
        
        val savedAnimationSpeed = settingsRepository.getAnimationSpeed()
        binding.seekbarAnimationSpeed.progress = ((savedAnimationSpeed * 10) - 1).toInt().coerceIn(0, 39)
        binding.textAnimationSpeedValue.text = "%.1fx".format(savedAnimationSpeed)

        binding.seekbarAnimationSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newAnimationSpeed = (progress + 1) / 10.0f
                binding.textAnimationSpeedValue.text = "%.1fx".format(newAnimationSpeed)
                if (fromUser) {
                    settingsRepository.setAnimationSpeed(newAnimationSpeed)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupThemeSwitch() {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        binding.switchTheme.isChecked = currentNightMode == Configuration.UI_MODE_NIGHT_YES

        binding.switchTheme.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            settingsRepository.setTheme(if(isChecked) "dark" else "light")
        }
    }

    private fun setAppLocale(localeCode: String) {
        settingsRepository.setAppLocale(localeCode)

        val localeTag = localeCode.replace('_', '-')
        val appLocale = LocaleListCompat.forLanguageTags(localeTag)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
