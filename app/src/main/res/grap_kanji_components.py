import xml.etree.ElementTree as ET
import urllib.request
import urllib.parse
import time
from bs4 import BeautifulSoup
import re
import os

# ============ CONFIGURATION ============
KANJI_XML_FILE = "kanji_details.xml"  # Ton fichier source
BASE_URL = "https://www.kanshudo.com/kanji/"
REQUEST_DELAY = 0.1  # Délai entre requêtes (IMPORTANT!)
SAVE_INTERVAL = 10  # Sauvegarde tous les N kanjis

# ============ FONCTIONS SPÉCIFIQUES ============

def fetch_kanji_page(kanji_char):
    """Télécharge la page HTML d'un kanji."""
    try:
        encoded_kanji = urllib.parse.quote(kanji_char.encode('utf-8'))
        url = f"{BASE_URL}{encoded_kanji}"

        headers = {'User-Agent': 'Mozilla/5.0'}
        req = urllib.request.Request(url, headers=headers)

        with urllib.request.urlopen(req, timeout=10) as response:
            return response.read().decode('utf-8')

    except Exception as e:
        print(f"  ❌ Erreur : {e}")
        return None

def extract_components_exact(html, kanji_char):
    """
    Extrait les composants EXACTEMENT du format HTML de Kanshudo.
    Format: ⿵&nbsp;&nbsp;<a href='/kanji/丷'>丷</a>&nbsp;(<a href='/kanji/八'>八</a>)&nbsp;eight &nbsp;<a href='/kanji/人'>人</a>&nbsp;person
    """
    if not html:
        return None

    soup = BeautifulSoup(html, 'html.parser')
    result = {'structure': None, 'components': []}

    # 1. Chercher la section "Components"
    components_section = None
    for div in soup.find_all('div', class_='g-row'):
        col_title = div.find('div', class_='col-1-4')
        if col_title and 'Components' in col_title.get_text():
            components_section = div.find('div', class_='col-3-4')
            break

    if not components_section:
        return None

    # 2. Extraire tout le HTML de cette section
    section_html = str(components_section)

    # 3. Chercher le symbole de structure
    structure_pattern = r'([⿰⿱⿲⿳⿴⿵⿶⿷⿸⿹⿺⿻])'
    structure_match = re.search(structure_pattern, section_html)
    if structure_match:
        result['structure'] = structure_match.group(1)

    # 4. Parser le format EXACT HTML
    # Trouver tous les liens <a> dans la section
    links = components_section.find_all('a', href=True)

    i = 0
    while i < len(links):
        link = links[i]
        href = link['href']

        # Vérifier si c'est un lien vers un kanji
        if '/kanji/' in href:
            char_display = link.get_text(strip=True)
            char_ref = char_display  # Par défaut

            # Vérifier s'il y a une parenthèse avec référence différente
            # Format: &nbsp;(<a href='/kanji/REF'>REF</a>)
            if i + 1 < len(links):
                # Vérifier le texte entre les liens
                next_text = ""
                current = link
                for _ in range(3):
                    current = current.next_sibling
                    if current is None:
                        break
                    if isinstance(current, str):
                        next_text += current

                # Si on trouve '(' avant le prochain lien
                if '(' in next_text and i + 1 < len(links):
                    next_link = links[i + 1]
                    next_href = next_link['href']

                    # Vérifier si c'est bien un lien kanji et que le texte correspond
                    if '/kanji/' in next_href:
                        # Vérifier que ce lien est bien dans une parenthèse
                        link_before_paren = next_link.find_previous(string=re.compile(r'\('))
                        if link_before_paren:
                            char_ref = next_link.get_text(strip=True)
                            i += 1  # Sauter ce lien

            # Ajouter seulement si différent du kanji principal
            if char_display != kanji_char:
                result['components'].append({
                    'display': char_display,
                    'ref': char_ref
                })

        i += 1

    # 5. Si pas de composants, vérifier si c'est un kanji simple
    if not result['components']:
        # Vérifier dans le texte si c'est marqué comme simple
        section_text = components_section.get_text()
        if 'No components' in section_text or 'Single component' in section_text:
            # Kanji simple (est son propre composant)
            result['components'].append({
                'display': kanji_char,
                'ref': kanji_char
            })

    # 6. Filtrer les doublons
    seen = set()
    unique_components = []
    for comp in result['components']:
        key = (comp['display'], comp['ref'])
        if key not in seen:
            seen.add(key)
            unique_components.append(comp)

    result['components'] = unique_components

    return result if result['components'] else None

# ============ GESTION DU XML ============

def get_kanji_from_xml():
    """Récupère tous les kanjis du fichier XML qui n'ont pas de composants."""

    print(f"📖 Lecture du fichier : {KANJI_XML_FILE}")

    if not os.path.exists(KANJI_XML_FILE):
        print(f"❌ Fichier {KANJI_XML_FILE} introuvable!")
        return None, None

    try:
        tree = ET.parse(KANJI_XML_FILE)
        root = tree.getroot()
    except ET.ParseError as e:
        print(f"❌ Erreur XML : {e}")
        return None, None

    # Trouver tous les éléments <kanji>
    kanji_elements = root.findall('.//kanji')

    if not kanji_elements:
        print("⚠️  Aucun élément <kanji> trouvé.")
        # Essayer une autre structure
        kanji_elements = []
        for elem in root.iter():
            if elem.get('character'):
                kanji_elements.append(elem)

    print(f"✅ {len(kanji_elements)} élément(s) <kanji> trouvé(s)")

    # Identifier les kanjis SANS composants
    to_process = []
    for elem in kanji_elements:
        char = elem.get('character')
        if not char and elem.text:
            char = elem.text.strip()

        if char and len(char.strip()) == 1:
            char = char.strip()

            # Vérifier si déjà a des composants
            has_components = elem.find('components') is not None

            if not has_components:
                to_process.append((elem, char))

    print(f"📥 {len(to_process)} kanji(s) sans composants")

    return tree, to_process

def update_xml_component(tree, kanji_elem, components_data):
    """Met à jour l'élément kanji avec les composants extraits."""

    # Supprimer l'ancien élément components s'il existe
    old_components = kanji_elem.find('components')
    if old_components is not None:
        kanji_elem.remove(old_components)

    # Créer le nouvel élément components
    components_elem = ET.SubElement(kanji_elem, 'components')

    # Ajouter l'attribut structure
    if components_data['structure']:
        components_elem.set('structure', components_data['structure'])
    else:
        components_elem.set('structure', '⿰')  # Valeur par défaut

    # Ajouter chaque composant au FORMAT EXACT
    for comp in components_data['components']:
        comp_elem = ET.SubElement(components_elem, 'component')
        comp_elem.set('kanji_ref', comp['ref'])

        # Le texte de l'élément est le caractère AFFICHÉ seulement si différent
        if comp['display'] != comp['ref']:
            comp_elem.text = comp['display']

    return True

# ============ FONCTION PRINCIPALE ============

def main():
    """Fonction principale qui orchestre tout."""

    print("🚀 ENRICHISSEMENT DES COMPOSANTS KANJI")
    print("="*50)

    # 1. Récupérer les kanjis depuis le XML
    tree, to_process = get_kanji_from_xml()

    if not tree or not to_process:
        print("❌ Aucun kanji à traiter.")
        return

    # 2. Demander confirmation pour le traitement batch
    if len(to_process) > 100:
        print(f"\n⚠️  ATTENTION : {len(to_process)} kanjis à traiter")
        print(f"⏱️  Temps estimé : {len(to_process) * REQUEST_DELAY / 60:.1f} minutes")

        response = input("Continuer ? [o/N] : ")
        if response.lower() != 'o':
            print("❌ Arrêt demandé.")
            return

    # 3. Statistiques
    success_count = 0
    no_components_count = 0
    error_count = 0

    # 4. Traitement de chaque kanji
    print(f"\n🚀 Démarrage du traitement...")
    start_time = time.time()

    for i, (kanji_elem, kanji_char) in enumerate(to_process, 1):
        print(f"\n[{i}/{len(to_process)}] {kanji_char}", end="")

        # Télécharger la page
        html = fetch_kanji_page(kanji_char)
        if not html:
            error_count += 1
            print(" → ❌ Téléchargement échoué")
            continue

        # Extraire les composants
        components_data = extract_components_exact(html, kanji_char)

        if not components_data:
            no_components_count += 1
            print(" → ⚠️  Pas de composants")
            continue

        # Mettre à jour le XML
        try:
            update_xml_component(tree, kanji_elem, components_data)
            success_count += 1

            # Afficher un résumé succint
            comps_summary = []
            for comp in components_data['components']:
                if comp['display'] != comp['ref']:
                    comps_summary.append(f"{comp['display']}({comp['ref']})")
                else:
                    comps_summary.append(comp['ref'])

            print(f" → ✅ {components_data['structure']} {' '.join(comps_summary)}")

        except Exception as e:
            error_count += 1
            print(f" → ❌ Erreur XML: {e}")
            continue

        # 5. Sauvegarde régulière
        if i % SAVE_INTERVAL == 0 or i == len(to_process):
            try:
                # Indenter le XML pour une meilleure lisibilité
                def indent(elem, level=0):
                    indent_str = "\n" + level * "  "
                    if len(elem):
                        if not elem.text or not elem.text.strip():
                            elem.text = indent_str + "  "
                        if not elem.tail or not elem.tail.strip():
                            elem.tail = indent_str
                        for child in elem:
                            indent(child, level + 1)
                        if not child.tail or not child.tail.strip():
                            child.tail = indent_str
                    else:
                        if level and (not elem.tail or not elem.tail.strip()):
                            elem.tail = indent_str

                indent(tree.getroot())
                tree.write(KANJI_XML_FILE, encoding='utf-8', xml_declaration=True)
                print(f"  💾 Sauvegarde ({i}/{len(to_process)})")
            except Exception as e:
                print(f"  ❌ Erreur sauvegarde: {e}")

        # 6. Délai entre les requêtes
        time.sleep(REQUEST_DELAY)

        # 7. Afficher la progression périodiquement
        if i % 50 == 0:
            elapsed = time.time() - start_time
            items_per_second = i / elapsed if elapsed > 0 else 0
            remaining = (len(to_process) - i) * REQUEST_DELAY / 60

            print(f"\n📊 PROGRESSION: {i}/{len(to_process)} ({i/len(to_process)*100:.1f}%)")
            print(f"⏱️  Temps écoulé: {elapsed/60:.1f} min")
            print(f"⏱️  Temps restant: {remaining:.1f} min")

    # 8. Sauvegarde finale
    print(f"\n💾 Sauvegarde finale...")
    try:
        tree.write(KANJI_XML_FILE, encoding='utf-8', xml_declaration=True)
    except Exception as e:
        print(f"❌ Erreur sauvegarde finale: {e}")

    # 9. Résumé final
    total_time = time.time() - start_time
    print(f"\n{'='*50}")
    print("✅ TRAITEMENT TERMINÉ")
    print(f"{'='*50}")
    print(f"📊 RÉSULTATS :")
    print(f"  ✅ Succès: {success_count}")
    print(f"  ⚠️  Sans composants: {no_components_count}")
    print(f"  ❌ Erreurs: {error_count}")
    print(f"  📋 Total traité: {len(to_process)}")
    print(f"\n⏱️  Temps total: {total_time/60:.1f} minutes")
    print(f"📁 Fichier mis à jour: {KANJI_XML_FILE}")

# ============ TEST RAPIDE ============

def test_quick():
    """Test rapide avec quelques kanjis."""

    test_kanjis = ['火', '大', '音', '水', '日', '月']

    print("🧪 TEST RAPIDE")
    print("="*50)

    for kanji in test_kanjis:
        print(f"\n{kanji}:")

        html = fetch_kanji_page(kanji)
        if not html:
            print("  ❌ Téléchargement échoué")
            continue

        data = extract_components_exact(html, kanji)

        if not data:
            print("  ⚠️  Pas de composants")
            continue

        # Afficher le XML qui serait généré
        print(f"  Structure: {data['structure']}")
        print(f"  XML:")
        print(f'  <components structure="{data["structure"]}">')
        for comp in data['components']:
            if comp['display'] != comp['ref']:
                print(f'    <component kanji_ref="{comp["ref"]}">{comp["display"]}</component>')
            else:
                print(f'    <component kanji_ref="{comp["ref"]}" />')
        print('  </components>')

        time.sleep(1)

# ============ LANCEMENT ============
if __name__ == "__main__":
    print("🚀 ENRICHISSEUR DE COMPOSANTS KANJI")
    print("="*50)

    # Option 1: Test rapide
    # test_quick()

    # Option 2: Traitement complet
    main()