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
      LanguageItem("en_GB", "English", "assets/drawable/flag_en_gb.png"),
      LanguageItem("fr_FR", "Français", "assets/drawable/flag_fr_fr.png"),
      LanguageItem("ja_JP", "日本語", "assets/drawable/flag_jp.png"),
      // Ajouter les autres selon les assets disponibles
    ];

    final selectedLanguage = languages.firstWhere(
      (l) => l.code == settings.currentLocaleCode,
      orElse: () => languages.first,
    );

    return Scaffold(
      appBar: AppBar(
        title: const Text("Paramètres"),
        backgroundColor: Colors.transparent,
        elevation: 0,
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              children: [
                _buildSection(
                  title: "Interface",
                  children: [
                    SwitchListTile(
                      title: const Text("Mode Sombre"),
                      value: settings.isDarkMode,
                      onChanged: (val) => settings.toggleTheme(val),
                    ),
                    const Divider(),
                    ListTile(
                      title: const Text("Langue"),
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
                  title: "Audio & Animations",
                  children: [
                    _buildSliderTile(
                      title: "Vitesse des animations",
                      value: settings.animationSpeed,
                      onChanged: (val) => settings.updateAnimationSpeed(val),
                      min: 0.5,
                      max: 2.0,
                    ),
                    const Divider(),
                    _buildSliderTile(
                      title: "Volume des sons",
                      value: settings.audioVolume,
                      onChanged: (val) => settings.updateAudioVolume(val),
                      min: 0.0,
                      max: 1.0,
                    ),
                    const Divider(),
                    _buildSliderTile(
                      title: "Vitesse de lecture (TTS)",
                      value: settings.ttsRate,
                      onChanged: (val) => settings.updateTtsRate(val),
                      min: 0.5,
                      max: 2.0,
                    ),
                  ],
                ),
                const SizedBox(height: 16),
                _buildSection(
                  title: "Apprentissage",
                  children: [
                    ListTile(
                      title: const Text("Mode"),
                      trailing: DropdownButton<String>(
                        value: settings.currentMode,
                        items: ["JLPT", "School", "Challenge"]
                            .map((m) => DropdownMenuItem(value: m, child: Text(m)))
                            .toList(),
                        onChanged: (val) => settings.updateMode(val!),
                      ),
                    ),
                    const Divider(),
                    ListTile(
                      title: const Text("Prononciation"),
                      trailing: DropdownButton<String>(
                        value: settings.pronunciation,
                        items: ["Roman", "Hiragana"]
                            .map((p) => DropdownMenuItem(value: p, child: Text(p)))
                            .toList(),
                        onChanged: (val) => settings.updatePronunciation(val!),
                      ),
                    ),
                    const Divider(),
                    CheckboxListTile(
                      title: const Text("Ajouter erreurs à la révision"),
                      value: settings.addWrongAnswers,
                      onChanged: (val) => settings.toggleAddWrongAnswers(val!),
                    ),
                    CheckboxListTile(
                      title: const Text("Retirer succès de la révision"),
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

  Widget _buildSection({required String title, required List<Widget> children}) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: Colors.white.withOpacity(0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              title,
              style: const TextStyle(fontSize: 18, fontWeight: FontWeight.bold, color: Colors.pink),
            ),
            const SizedBox(height: 8),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildSliderTile({
    required String title,
    required double value,
    required ValueChanged<double> onChanged,
    required double min,
    required double max,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(title),
        Row(
          children: [
            Expanded(
              child: Slider(
                value: value,
                min: min,
                max: max,
                activeColor: Colors.pink,
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
