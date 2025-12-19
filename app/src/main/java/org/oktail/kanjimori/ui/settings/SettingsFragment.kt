package org.oktail.kanjimori.ui.settings

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import org.oktail.kanjimori.R
import org.oktail.kanjimori.databinding.FragmentSettingsBinding

const val TEXT_SIZE_PREF_KEY = "TextSize"
const val ANIMATION_SPEED_PREF_KEY = "AnimationSpeed"
const val PRONUNCIATION_PREF_KEY = "Pronunciation"
const val ADD_WRONG_ANSWERS_PREF_KEY = "AddWrongAnswers"
const val REMOVE_GOOD_ANSWERS_PREF_KEY = "RemoveGoodAnswers"

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var languages: List<LanguageItem>
    private lateinit var sharedPreferences: SharedPreferences

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

        sharedPreferences = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        setupLanguageSpinner()
        setupPronunciationRadioGroup()
        setupDefaultUserListCheckboxes()
        setupTextSizeSeekBar()
        setupAnimationSpeedSeekBar()
    }

    private fun setupLanguageSpinner() {
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

    private fun setupPronunciationRadioGroup() {
        val savedPronunciation = sharedPreferences.getString(PRONUNCIATION_PREF_KEY, "Hiragana")
        if (savedPronunciation == "Roman") {
            binding.radioGroupPronunciation.check(R.id.radio_roman_alphabet)
        } else {
            binding.radioGroupPronunciation.check(R.id.radio_hiragana)
        }

        binding.radioGroupPronunciation.setOnCheckedChangeListener { _, checkedId ->
            val pronunciation = if (checkedId == R.id.radio_roman_alphabet) "Roman" else "Hiragana"
            sharedPreferences.edit().putString(PRONUNCIATION_PREF_KEY, pronunciation).apply()
        }
    }

    private fun setupDefaultUserListCheckboxes() {
        binding.checkboxAddWrongAnswers.isChecked = sharedPreferences.getBoolean(ADD_WRONG_ANSWERS_PREF_KEY, true)
        binding.checkboxRemoveGoodAnswers.isChecked = sharedPreferences.getBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, true)

        binding.checkboxAddWrongAnswers.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(ADD_WRONG_ANSWERS_PREF_KEY, isChecked).apply()
        }

        binding.checkboxRemoveGoodAnswers.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean(REMOVE_GOOD_ANSWERS_PREF_KEY, isChecked).apply()
        }
    }

    private fun setupTextSizeSeekBar() {
        // Range from 0.1 to 4.0. We'll use a range of 0-39 in the SeekBar
        binding.seekbarTextSize.max = 39
        
        // Load saved value, default to 1.0f (which corresponds to progress 9)
        val savedTextSize = sharedPreferences.getFloat(TEXT_SIZE_PREF_KEY, 1.0f)
        binding.seekbarTextSize.progress = ((savedTextSize * 10) - 1).toInt().coerceIn(0, 39)
        binding.textTextSizeValue.text = "%.1fx".format(savedTextSize)

        binding.seekbarTextSize.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newTextSize = (progress + 1) / 10.0f
                binding.textTextSizeValue.text = "%.1fx".format(newTextSize)
                if (fromUser) {
                    sharedPreferences.edit().putFloat(TEXT_SIZE_PREF_KEY, newTextSize).apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupAnimationSpeedSeekBar() {
        // Range from 0.1 to 4.0. We'll use a range of 0-39 in the SeekBar
        binding.seekbarAnimationSpeed.max = 39
        
        // Load saved value, default to 1.0f (which corresponds to progress 9)
        val savedAnimationSpeed = sharedPreferences.getFloat(ANIMATION_SPEED_PREF_KEY, 1.0f)
        binding.seekbarAnimationSpeed.progress = ((savedAnimationSpeed * 10) - 1).toInt().coerceIn(0, 39)
        binding.textAnimationSpeedValue.text = "%.1fx".format(savedAnimationSpeed)

        binding.seekbarAnimationSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val newAnimationSpeed = (progress + 1) / 10.0f
                binding.textAnimationSpeedValue.text = "%.1fx".format(newAnimationSpeed)
                if (fromUser) {
                    sharedPreferences.edit().putFloat(ANIMATION_SPEED_PREF_KEY, newAnimationSpeed).apply()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun getCurrentAppLocale(): String {
        return sharedPreferences.getString("AppLocale", "fr_FR")!!
    }

    private fun setAppLocale(localeCode: String) {
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