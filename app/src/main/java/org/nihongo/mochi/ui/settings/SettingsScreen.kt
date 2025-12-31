package org.nihongo.mochi.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.nihongo.mochi.R
import org.nihongo.mochi.presentation.MochiBackground
import java.util.Locale

// Data class for language items, now used in Compose
data class LanguageItem(val code: String, val name: String, val flagResId: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onThemeChanged: (Boolean) -> Unit,
    onLocaleChanged: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    val languages = remember {
        listOf(
            LanguageItem("ar_SA", "العربية", R.drawable.flag_sa_sa),
            LanguageItem("bn_BD", "বাংলা", R.drawable.flag_bn),
            LanguageItem("de_DE", "Deutsch", R.drawable.flag_de),
            LanguageItem("en_GB", "English", R.drawable.flag_en_gb),
            LanguageItem("es_ES", "Español", R.drawable.flag_es),
            LanguageItem("fr_FR", "Français", R.drawable.flag_fr_fr),
            LanguageItem("in_ID", "Bahasa Indonesia", R.drawable.flag_id),
            LanguageItem("it_IT", "Italiano", R.drawable.flag_it),
            LanguageItem("ko_KR", "한국어", R.drawable.flag_kr),
            LanguageItem("pt_BR", "Português", R.drawable.flag_pt_br),
            LanguageItem("ru_RU", "Русский", R.drawable.flag_ru),
            LanguageItem("th_TH", "ไทย", R.drawable.flag_th_th),
            LanguageItem("vi_VN", "Tiếng Việt", R.drawable.flag_vn),
            LanguageItem("zh_CN", "简体中文", R.drawable.flag_cn)
        )
    }

    val learningModes = remember {
        listOf("JLPT", "School", "Challenge")
    }

    MochiBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            SettingsSection(title = stringResource(R.string.settings_category_general)) {
                // Language Spinner
                var expanded by remember { mutableStateOf(false) }
                val selectedLanguage = languages.find { it.code == uiState.currentLocaleCode } ?: languages.first()

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedLanguage.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.settings_language)) },
                        leadingIcon = {
                            Image(painter = painterResource(selectedLanguage.flagResId), contentDescription = null, modifier = Modifier.size(24.dp))
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        languages.forEach { language ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(painter = painterResource(language.flagResId), contentDescription = null, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text(language.name)
                                    }
                                },
                                onClick = {
                                    viewModel.onLocaleChanged(language.code)
                                    onLocaleChanged(language.code) // Notify Fragment to trigger system locale change
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                
                // Pronunciation
                Text(
                    text = stringResource(R.string.settings_pronunciation),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onPronunciationChanged("Roman") }) {
                        RadioButton(selected = uiState.pronunciation == "Roman", onClick = { viewModel.onPronunciationChanged("Roman") })
                        Text(stringResource(R.string.settings_pronunciation_roman), color = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.onPronunciationChanged("Hiragana") }) {
                        RadioButton(selected = uiState.pronunciation == "Hiragana", onClick = { viewModel.onPronunciationChanged("Hiragana") })
                        Text(stringResource(R.string.settings_pronunciation_hiragana), color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Learning Mode Section (New)
            SettingsSection(title = "Learning Mode") { // Use resource string in prod
                Column {
                    learningModes.forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.onModeChanged(mode) }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = (uiState.currentMode == mode),
                                onClick = { viewModel.onModeChanged(mode) }
                            )
                            Text(
                                text = mode,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SettingsSection(title = stringResource(R.string.settings_category_learning)) {
                // Default User List
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.addWrongAnswers, onCheckedChange = { viewModel.onAddWrongAnswersChanged(it) })
                    Text(stringResource(R.string.settings_add_wrong_answers), color = MaterialTheme.colorScheme.onSurface)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = uiState.removeGoodAnswers, onCheckedChange = { viewModel.onRemoveGoodAnswersChanged(it) })
                    Text(stringResource(R.string.settings_remove_good_answers), color = MaterialTheme.colorScheme.onSurface)
                }
            }
            
            Spacer(Modifier.height(16.dp))

            SettingsSection(title = stringResource(R.string.settings_category_interface)) {
                // Text Size
                Text(stringResource(R.string.settings_text_size), color = MaterialTheme.colorScheme.onSurface)
                SliderWithLabel(value = uiState.textSize, onValueChange = { viewModel.onTextSizeChanged(it) })
                
                Spacer(Modifier.height(8.dp))

                // Animation Speed
                Text(stringResource(R.string.settings_animation_speed), color = MaterialTheme.colorScheme.onSurface)
                SliderWithLabel(value = uiState.animationSpeed, onValueChange = { viewModel.onAnimationSpeedChanged(it) })
                
                Spacer(Modifier.height(8.dp))
                
                // Theme
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.settings_theme), color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.weight(1f))
                    Switch(checked = uiState.isDarkMode, onCheckedChange = {
                        viewModel.onThemeChanged(it)
                        onThemeChanged(it)
                    })
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            content()
        }
    }
}

@Composable
fun SliderWithLabel(value: Float, onValueChange: (Float) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.1f..4.0f,
            steps = 38, // (4.0 - 0.1) / 0.1 - 1 = 39 - 1 = 38 steps for 39 intervals
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "%.1fx".format(Locale.US, value),
            modifier = Modifier.widthIn(min = 40.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
