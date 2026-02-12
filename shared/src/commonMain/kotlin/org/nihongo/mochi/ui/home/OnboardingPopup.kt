package org.nihongo.mochi.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.nihongo.mochi.domain.services.DownloadStatus
import org.nihongo.mochi.presentation.OnboardingViewModel
import org.nihongo.mochi.shared.generated.resources.*
import org.nihongo.mochi.ui.ResourceUtils
import org.nihongo.mochi.ui.settings.LanguageItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingPopup(
    viewModel: OnboardingViewModel,
    onLocaleChanged: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isFirstRun) {
        Dialog(
            onDismissRequest = { /* Force completion */ },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .animateContentSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.app_name),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        when (uiState.currentStep) {
                            0 -> LanguageStep(uiState, viewModel, onLocaleChanged)
                            1 -> ModeStep(uiState, viewModel)
                            2 -> HighlightStep(viewModel)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { viewModel.nextStep() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                if (uiState.currentStep < 2) stringResource(Res.string.onboarding_next)
                                else stringResource(Res.string.onboarding_finish)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageStep(
    uiState: OnboardingViewModel.OnboardingUiState,
    viewModel: OnboardingViewModel,
    onLocaleChanged: (String) -> Unit
) {
    val languages = remember {
        listOf(
            LanguageItem("ar_SA", "العربية", Res.drawable.flag_sa_sa),
            LanguageItem("bn_BD", "বাংলা", Res.drawable.flag_bn),
            LanguageItem("de_DE", "Deutsch", Res.drawable.flag_de),
            LanguageItem("en_GB", "English", Res.drawable.flag_en_gb),
            LanguageItem("es_ES", "Español", Res.drawable.flag_es),
            LanguageItem("fr_FR", "Français", Res.drawable.flag_fr_fr),
            LanguageItem("in_ID", "Bahasa Indonesia", Res.drawable.flag_id),
            LanguageItem("it_IT", "Italiano", Res.drawable.flag_it),
            LanguageItem("ja_JP", "日本語", Res.drawable.flag_jp),
            LanguageItem("ko_KR", "한국어", Res.drawable.flag_kr),
            LanguageItem("mn_MN", "Монгол", Res.drawable.flag_mn),
            LanguageItem("pt_BR", "Português", Res.drawable.flag_pt_br),
            LanguageItem("ru_RU", "Русский", Res.drawable.flag_ru),
            LanguageItem("th_TH", "ไทย", Res.drawable.flag_th_th),
            LanguageItem("ua_UA", "Українська", Res.drawable.flag_ua),
            LanguageItem("vi_VN", "Tiếng Việt", Res.drawable.flag_vn),
            LanguageItem("zh_CN", "简体中文", Res.drawable.flag_cn)
        )
    }

    var expanded by remember { mutableStateOf(false) }
    val selectedLanguage = languages.find { it.code == uiState.currentLocaleCode } ?: languages.first()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(Res.string.onboarding_choose_language),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedLanguage.name,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(Res.string.settings_language)) },
                leadingIcon = {
                    if (uiState.downloadStatus == DownloadStatus.DOWNLOADING) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Image(painter = painterResource(selectedLanguage.flagRes), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                languages.forEach { language ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Image(painter = painterResource(language.flagRes), contentDescription = null, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(language.name)
                            }
                        },
                        onClick = {
                            viewModel.onLocaleChanged(language.code)
                            onLocaleChanged(language.code)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeStep(uiState: OnboardingViewModel.OnboardingUiState, viewModel: OnboardingViewModel) {
    var expanded by remember { mutableStateOf(false) }
    
    @Composable
    fun getModeLabel(mode: String): String {
        val key = "section_" + mode.lowercase()
        val resource = ResourceUtils.resolveStringResource(key) ?: ResourceUtils.resolveStringResource(mode.lowercase())
        return if (resource != null) stringResource(resource) else mode.replaceFirstChar { it.titlecase() }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(Res.string.settings_learning_mode),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = getModeLabel(uiState.currentMode),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(Res.string.onboarding_mode_label)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.availableModes.forEach { mode ->
                    DropdownMenuItem(
                        text = { Text(getModeLabel(mode)) },
                        onClick = {
                            viewModel.onModeChanged(mode)
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(Res.string.onboarding_mode_description),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun HighlightStep(viewModel: OnboardingViewModel) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Res.drawable.flag_jp.let { painterResource(it) }, 
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.Unspecified
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            stringResource(Res.string.onboarding_ready_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            stringResource(Res.string.onboarding_ready_description),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(24.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    stringResource(Res.string.onboarding_slider_hint), 
                    fontSize = 12.sp, 
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
