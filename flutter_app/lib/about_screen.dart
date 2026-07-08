import 'package:flutter/material.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:package_info_plus/package_info_plus.dart';
import 'widgets/mochi_background.dart';

class AboutScreen extends StatefulWidget {
  const AboutScreen({super.key});

  @override
  State<AboutScreen> createState() => _AboutScreenState();
}

class _AboutScreenState extends State<AboutScreen> {
  String _version = "1.0.0";

  @override
  void initState() {
    super.initState();
    _loadPackageInfo();
  }

  Future<void> _loadPackageInfo() async {
    final info = await PackageInfo.fromPlatform();
    setState(() {
      _version = info.version;
    });
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
    return Scaffold(
      appBar: AppBar(
        title: const Text("À propos"),
        backgroundColor: Colors.transparent,
        elevation: 0,
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
                    const Text(
                      "Nihongo\nMochi",
                      style: TextStyle(
                        fontSize: 34,
                        fontWeight: FontWeight.bold,
                        color: Colors.pink,
                      ),
                    ),
                    Image.asset('assets/drawable/nihongomochi.png', width: 96),
                  ],
                ),
                const SizedBox(height: 24),
                _buildSectionCard(
                  title: "Informations",
                  icon: Icons.info_outline,
                  children: [
                    _buildInfoRow("Version", _version),
                    _buildInfoRow("Date", "2024"),
                    const SizedBox(height: 16),
                    Row(
                      children: [
                        ClipRRect(
                          borderRadius: BorderRadius.circular(8),
                          child: Image.asset('assets/drawable/ebi.png', width: 48, height: 48, errorBuilder: (_, __, ___) => const Icon(Icons.pets, size: 48)),
                        ),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Text(
                            "In memoriam Ebi, qui nous a quittés durant le développement de cette application.",
                            style: TextStyle(fontSize: 12, color: Colors.black54, fontStyle: FontStyle.italic),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    _buildFullWidthButton("Signaler un bug", Icons.bug_report, () => _launchUrl("https://github.com/oktailb/NihongoMochi/issues")),
                    _buildFullWidthButton("Noter l'application", Icons.star, () {}),
                    _buildFullWidthButton("Licences Open Source", Icons.description, () => showLicensePage(context: context)),
                  ],
                ),
                const SizedBox(height: 16),
                _buildSectionCard(
                  title: "Crédits",
                  icon: Icons.people_outline,
                  children: [
                    const Text("Design & Développement", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.pink)),
                    const Text("LECOQ Vincent"),
                    const SizedBox(height: 16),
                    _buildFullWidthButton("Soutenir sur Patreon", Icons.favorite, () => _launchUrl("https://www.patreon.com/nihongomochi")),
                    _buildFullWidthButton("Soutenir sur Tipeee", Icons.volunteer_activism, () => _launchUrl("https://fr.tipeee.com/nihongomochi")),
                    const SizedBox(height: 16),
                    const Text("Conseils pédagogiques", style: TextStyle(fontWeight: FontWeight.bold, color: Colors.pink)),
                    const Text("À venir..."),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildSectionCard({required String title, required IconData icon, required List<Widget> children}) {
    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      color: Colors.white.withOpacity(0.9),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(title, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold)),
                Icon(icon, color: Colors.pink),
              ],
            ),
            const SizedBox(height: 16),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: Row(
        children: [
          Text(label, style: const TextStyle(fontWeight: FontWeight.bold)),
          const SizedBox(width: 16),
          Text(value),
        ],
      ),
    );
  }

  Widget _buildFullWidthButton(String text, IconData icon, VoidCallback onTap) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8.0),
      child: ElevatedButton.icon(
        onPressed: onTap,
        icon: Icon(icon, size: 18),
        label: Text(text),
        style: ElevatedButton.styleFrom(
          minimumSize: const Size(double.infinity, 45),
          backgroundColor: Colors.pink,
          foregroundColor: Colors.white,
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
      ),
    );
  }
}
