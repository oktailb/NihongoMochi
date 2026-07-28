import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'build_info.g.dart';
import 'widgets/mochi_background.dart';
import 'providers/settings_provider.dart';

class AboutScreen extends StatefulWidget {
  const AboutScreen({super.key});

  @override
  State<AboutScreen> createState() => _AboutScreenState();
}

class _AboutScreenState extends State<AboutScreen> {
  String _version = "0.9.8";
  String _buildDate = "";

  @override
  void initState() {
    super.initState();
    _loadPackageInfo();
  }

  Future<void> _loadPackageInfo() async {
    final info = await PackageInfo.fromPlatform();
    
    final year = kBuildTimestamp.year;
    final month = kBuildTimestamp.month.toString().padLeft(2, '0');
    final day = kBuildTimestamp.day.toString().padLeft(2, '0');

    if (mounted) {
      setState(() {
        _version = info.version;
        _buildDate = "$year-$month-$day";
      });
    }
  }

  Future<void> _launchUrl(String url) async {
    final Uri uri = Uri.parse(url);
    if (!await launchUrl(uri, mode: LaunchMode.externalApplication)) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('Impossible d\'ouvrir : $url')),
        );
      }

    }
  }

  @override
  Widget build(BuildContext context) {
    final settings = context.watch<SettingsProvider>();
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: Text(
          settings.getString("menu_about"),
          style: TextStyle(color: theme.colorScheme.onSurface),
        ),
        backgroundColor: Colors.transparent,
        elevation: 0,
        iconTheme: IconThemeData(color: theme.colorScheme.onSurface),
      ),
      extendBodyBehindAppBar: true,
      body: MochiBackground(
        child: SafeArea(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(16.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      "Nihongo\nMochi",
                      style: TextStyle(
                        fontSize: 34,
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                    Image.asset('assets/drawable/nihongomochi.webp', width: 96),
                  ],
                ),
                const SizedBox(height: 24),
                _buildSectionCard(
                  context,
                  title: settings.getString("about_category_informations"),
                  icon: Icons.info_outline,
                  children: [
                    _buildInfoRow(context, settings.getString("about_version_label"), _version),
                    _buildInfoRow(context, settings.getString("about_date_label"), _buildDate),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: Image.asset('assets/drawable/ebi.webp', width: 48, height: 48, errorBuilder: (_, _, _) => const Icon(Icons.pets, size: 48)),

                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Text(
                            settings.getString("in_memoriam"),
                            style: TextStyle(
                              fontSize: 12,
                              color: theme.colorScheme.onSurface.withValues(alpha: 0.7),
                              fontStyle: FontStyle.italic,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    _buildFullWidthButton(context, settings.getString("about_issue_tracker"), Icons.bug_report, () => _launchUrl("https://github.com/oktailb/NihongoMochi/issues")),
                    _buildFullWidthButton(context, settings.getString("about_rate_app"), Icons.star, () {}),
                    _buildFullWidthButton(context, "Open Source Licenses", Icons.description, () => showLicensePage(context: context)),
                  ],
                ),
                const SizedBox(height: 16),
                _buildSectionCard(
                  context,
                  title: settings.getString("about_category_credits"),
                  icon: Icons.people_outline,
                  children: [
                    Text(
                      settings.getString("about_design_dev"),
                      style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
                    ),
                    Text(
                      settings.getString("about_author_name"),
                      style: TextStyle(color: theme.colorScheme.onSurface),
                    ),
                    const SizedBox(height: 16),
                    _buildFullWidthButton(context, settings.getString("about_patreon"), Icons.favorite, () => _launchUrl("https://www.patreon.com/cw/Oktail")),
                    _buildFullWidthButton(context, settings.getString("about_tipeee"), Icons.volunteer_activism, () => _launchUrl("https://en.tipeee.com/lecoq-vincent/news/246869")),
                    const SizedBox(height: 16),
                    Text(
                      settings.getString("about_pedagogical"),
                      style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.primary),
                    ),
                    Text(
                      settings.getString("about_coming_soon"),
                      style: TextStyle(color: theme.colorScheme.onSurface),
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

  Widget _buildSectionCard(BuildContext context, {required String title, required IconData icon, required List<Widget> children}) {
    final theme = Theme.of(context);
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: theme.colorScheme.surface.withValues(alpha: 0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  title,
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
                ),
                Icon(icon, color: theme.colorScheme.primary),
              ],
            ),
            const SizedBox(height: 16),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(BuildContext context, String label, String value) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Row(
        children: [
          Text(
            label,
            style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.onSurface),
          ),
          const SizedBox(width: 16),
          Text(
            value,
            style: TextStyle(color: theme.colorScheme.onSurface),
          ),
        ],
      ),
    );
  }

  Widget _buildFullWidthButton(BuildContext context, String text, IconData icon, VoidCallback onTap) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: ElevatedButton.icon(
        onPressed: onTap,
        icon: Icon(icon, size: 18),
        label: Text(text),
        style: ElevatedButton.styleFrom(
          minimumSize: const Size(double.infinity, 45),
          backgroundColor: theme.colorScheme.primary,
          foregroundColor: theme.colorScheme.onPrimary,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
          elevation: 4,
        ),
      ),
    );
  }
}
