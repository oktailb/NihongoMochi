import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'providers/settings_provider.dart';
import 'widgets/mochi_background.dart';

class LanguageItem {
  final String code;
  final String name;
  final String flagAsset;

  LanguageItem(this.code, this.name, this.flagAsset);
}

class SettingsScreen extends StatelessWidget {
  const SettingsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();

    final languages = [
      LanguageItem("ar_SA", "العربية", "assets/drawable/flag_sa_sa.png"),
      LanguageItem("bn_BD", "বাংলা", "assets/drawable/flag_bn.png"),
      LanguageItem("de_DE", "Deutsch", "assets/drawable/flag_de.png"),
      LanguageItem("en_GB", "English", "assets/drawable/flag_en_gb.png"),
      LanguageItem("es_ES", "Español", "assets/drawable/flag_es.png"),
      LanguageItem("fr_FR", "Français", "assets/drawable/flag_fr_fr.png"),
      LanguageItem("in_ID", "Bahasa Indonesia", "assets/drawable/flag_id.png"),
      LanguageItem("it_IT", "Italiano", "assets/drawable/flag_it.png"),
      LanguageItem("ja_JP", "日本語", "assets/drawable/flag_jp.png"),
      LanguageItem("ko_KR", "한국어", "assets/drawable/flag_kr.png"),
      LanguageItem("mn_MN", "Монгол", "assets/drawable/flag_mn.png"),
      LanguageItem("pt_BR", "Português", "assets/drawable/flag_pt_br.png"),
      LanguageItem("ru_RU", "Русский", "assets/drawable/flag_ru.png"),
      LanguageItem("th_TH", "ไทย", "assets/drawable/flag_th_th.png"),
      LanguageItem("ua_UA", "Українська", "assets/drawable/flag_ua.png"),
      LanguageItem("vi_VN", "Tiếng Việt", "assets/drawable/flag_vn.png"),
      LanguageItem("zh_CN", "简体中文", "assets/drawable/flag_cn.png"),
    ];

    final selectedLanguage = languages.firstWhere(
      (l) => l.code == settings.currentLocaleCode,
      orElse: () => languages.firstWhere((l) => l.code == "en_GB", orElse: () => languages.first),
    );

    return Scaffold(
      appBar: AppBar(
        title: Text(settings.getString("settings_title"), style: TextStyle(color: Theme.of(context).colorScheme.onBackground)),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: IconThemeData(color: Theme.of(context).colorScheme.onBackground),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                _buildSection(
                  context,
                  title: settings.getString("settings_category_interface"),
                  children: [
                    SwitchListTile(
                      title: Text(settings.getString("settings_theme")),
                      value: settings.isDarkMode,
                      onChanged: (val) => settings.toggleTheme(val),
                    ),
                    const Divider(),
                    ListTile(
                      title: Text(settings.getString("settings_language")),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Image.asset(selectedLanguage.flagAsset, width: 24),
                          const SizedBox(width: 8),
                          Text(selectedLanguage.name),
                          const Icon(Icons.arrow_drop_down),
                        ],
                      ),
                      onTap: () => _showLanguagePicker(context, settings, languages),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                _buildSection(
                  context,
                  title: settings.getString("settings_category_general"),
                  children: [
                    _buildSliderTile(
                      context,
                      title: settings.getString("settings_animation_speed"),
                      value: settings.animationSpeed,
                      onChanged: (val) => settings.updateAnimationSpeed(val),
                      min: 0.5,
                      max: 2.0,
                    ),
                    const Divider(),
                    _buildSliderTile(
                      context,
                      title: settings.getString("settings_audio_volume"),
                      value: settings.audioVolume,
                      onChanged: (val) => settings.updateAudioVolume(val),
                      min: 0.0,
                      max: 1.0,
                    ),
                    const Divider(),
                    _buildSliderTile(
                      context,
                      title: settings.getString("settings_tts_speed"),
                      value: settings.ttsRate,
                      onChanged: (val) => settings.updateTtsRate(val),
                      min: 0.5,
                      max: 2.0,
                    ),
                    const Divider(),
                    ListTile(
                      title: Text(settings.getString("settings_tts_voice_selection")),
                      trailing: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          DropdownButton<String?>(
                            value: settings.selectedVoiceId,
                            items: [
                              DropdownMenuItem<String?>(
                                value: null,
                                child: Text(settings.getString("settings_tts_voice_default")),
                              ),
                              ...settings.availableVoices.map((voiceId) => DropdownMenuItem<String?>(
                                    value: voiceId,
                                    child: Text(_getFriendlyVoiceName(voiceId, settings)),
                                  )),
                            ],
                            onChanged: settings.currentLocaleCode.startsWith("ar") 
                                ? null 
                                : (val) => settings.updateTtsVoiceId(val),
                          ),
                          const SizedBox(width: 8),
                          IconButton(
                            icon: const Icon(Icons.play_arrow),
                            onPressed: () => settings.testSpeak(),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                _buildSection(
                  context,
                  title: settings.getString("settings_category_learning"),
                  children: [
                    ListTile(
                      title: Text(settings.getString("settings_learning_mode")),
                      trailing: DropdownButton<String>(
                        value: settings.currentMode,
                        items: ["JLPT", "School", "Challenge"]
                            .map((m) => DropdownMenuItem(
                                  value: m,
                                  child: Text(m == "JLPT"
                                      ? settings.getString("section_jlpt")
                                      : m == "School"
                                          ? settings.getString("section_school")
                                          : settings.getString("section_challenges")),
                                ))
                            .toList(),
                        onChanged: (val) => settings.updateMode(val!),
                      ),
                    ),
                    const Divider(),
                    ListTile(
                      title: Text(settings.getString("settings_pronunciation")),
                      trailing: DropdownButton<String>(
                        value: settings.pronunciation,
                        items: ["Roman", "Hiragana"]
                            .map((p) => DropdownMenuItem(
                                  value: p,
                                  child: Text(p == "Roman"
                                      ? settings.getString("settings_pronunciation_roman")
                                      : settings.getString("settings_pronunciation_hiragana")),
                                ))
                            .toList(),
                        onChanged: (val) => settings.updatePronunciation(val!),
                      ),
                    ),
                    const Divider(),
                    CheckboxListTile(
                      title: Text(settings.getString("settings_add_wrong_answers")),
                      value: settings.addWrongAnswers,
                      onChanged: (val) => settings.toggleAddWrongAnswers(val!),
                    ),
                    CheckboxListTile(
                      title: Text(settings.getString("settings_remove_good_answers")),
                      value: settings.removeGoodAnswers,
                      onChanged: (val) => settings.toggleRemoveGoodAnswers(val!),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  String _getFriendlyVoiceName(String? voiceId, SettingsProvider settings) {
    if (voiceId == null || voiceId.isEmpty) {
      return settings.getString("settings_tts_voice_default");
    }
    final name = voiceId.toLowerCase();
    if (name.contains("jad-local") || name.contains("-m-") || name.contains("male")) {
      return settings.getString("settings_tts_voice_male");
    }
    if (name.contains("jab-local") || name.contains("-f-") || name.contains("female")) {
      return settings.getString("settings_tts_voice_female");
    }
    if (name.contains("sjp-local")) {
      return "${settings.getString("settings_tts_voice_male")} (Samsung)";
    }
    if (name.contains("sja-local")) {
      return "${settings.getString("settings_tts_voice_female")} (Samsung)";
    }
    return voiceId;
  }

  Widget _buildSection(BuildContext context, {required String title, required List<Widget> children}) {
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface.withOpacity(0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
            ),
            const SizedBox(height: 8),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildSliderTile(
    BuildContext context, {
    required String title,
    required double value,
    required ValueChanged<double> onChanged,
    required double min,
    required double max,
  }) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title, style: TextStyle(color: theme.colorScheme.onSurface)),
        Row(
          children: [
            Expanded(
              child: Slider(
                value: value,
                min: min,
                max: max,
                activeColor: theme.colorScheme.primary,
                onChanged: onChanged,
              ),
            ),
            Text("${value.toStringAsFixed(1)}x"),
          ],
        ),
      ],
    );
  }

  void _showLanguagePicker(BuildContext context, SettingsProvider settings, List<LanguageItem> languages) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (context) => ListView.builder(
        shrinkWrap: true,
        itemCount: languages.length,
        itemBuilder: (context, index) {
          final lang = languages[index];
          return ListTile(
            leading: Image.asset(lang.flagAsset, width: 32),
            title: Text(lang.name),
            onTap: () {
              settings.updateLocale(lang.code);
              Navigator.pop(context);
            },
          );
        },
      ),
    );
  }
}
