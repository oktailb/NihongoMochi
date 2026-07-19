#!/usr/bin/python3
import os
import re
import json
import xml.etree.ElementTree as ET
import glob

# Configuration des chemins
BASE_XML_FILE = 'shared/src/commonMain/composeResources/values/strings.xml'
TARGET_XML_DIRS = 'shared/src/commonMain/composeResources/values-*'
OUTPUT_ARB_DIR = 'flutter_app/lib/l10n'

# Conversion des noms de répertoires Android vers les identifiants de langue BCP47/Flutter
DIR_TO_LOCALE = {
    'values': 'en',
    'values-ar-rSA': 'ar',
    'values-bn-rBD': 'bn',
    'values-de-rDE': 'de',
    'values-en-rGB': 'en',
    'values-es-rES': 'es',
    'values-fr-rFR': 'fr',
    'values-in-rID': 'id',
    'values-it-rIT': 'it',
    'values-ja-rJP': 'ja',
    'values-ko-rKR': 'ko',
    'values-mn-rMN': 'mn',
    'values-pt-rBR': 'pt',
    'values-ru-rRU': 'ru',
    'values-th-rTH': 'th',
    'values-ua-rUA': 'uk',
    'values-vi-rVN': 'vi',
    'values-zh-rCN': 'zh'
}

def convert_placeholders(text):
    """
    Convertit les placeholders Android (%1$s, %2$d, %s, %d) vers le format ARB ({param1}, {param2}).
    Retourne (texte_converti, liste_des_noms_de_params).
    """
    if not text:
        return "", []

    # Unescape common XML escapes
    text = text.replace(r"\'", "'").replace(r'\"', '"')
    
    # Track existing parameters found
    params = set()

    # 1. Match indexed placeholders: %1$s, %2$d, %1$d, %2$s, etc.
    def repl_indexed(match):
        idx = match.group(1)
        param_name = f"param{idx}"
        params.add(param_name)
        return f"{{{param_name}}}"

    text = re.sub(r'%(\d+)\$[ds]', repl_indexed, text)

    # 2. Match unindexed placeholders: %s, %d
    unindexed_counter = [1]
    def repl_unindexed(match):
        while f"param{unindexed_counter[0]}" in params:
            unindexed_counter[0] += 1
        param_name = f"param{unindexed_counter[0]}"
        params.add(param_name)
        unindexed_counter[0] += 1
        return f"{{{param_name}}}"

    text = re.sub(r'%[ds]', repl_unindexed, text)

    # 3. Handle Escaped % (%% -> %)
    text = text.replace("%%", "%")

    # Order parameters param1, param2...
    ordered_params = sorted(list(params), key=lambda p: int(p.replace("param", "")) if p.replace("param", "").isdigit() else p)
    return text, ordered_params

def parse_xml_to_arb(xml_file_path, is_template=False):
    """
    Lit un fichier strings.xml et renvoie un dictionnaire au format ARB.
    """
    if not os.path.exists(xml_file_path):
        return {}

    try:
        tree = ET.parse(xml_file_path)
        root = tree.getroot()
    except Exception as e:
        print(f"Erreur de lecture XML {xml_file_path}: {e}")
        return {}

    arb_dict = {}

    for elem in root.findall('string'):
        key = elem.get('name')
        if not key:
            continue

        raw_text = elem.text or ""
        converted_text, params = convert_placeholders(raw_text)

        arb_dict[key] = converted_text

        # Si c'est le fichier modèle (app_en.arb), on génère la métadonnée @key avec ses placeholders
        if is_template and params:
            metadata = {
                "placeholders": {
                    p: {"type": "Object"} for p in params
                }
            }
            arb_dict[f"@{key}"] = metadata

    return arb_dict

def main():
    os.makedirs(OUTPUT_ARB_DIR, exist_ok=True)

    print("=== Conversion des ressources XML vers ARB (Flutter L10n) ===")
    
    # 1. Modèle principal (English / values/strings.xml -> app_en.arb)
    print(f"Conversion du modèle principal {BASE_XML_FILE} -> app_en.arb")
    template_arb = parse_xml_to_arb(BASE_XML_FILE, is_template=True)
    
    with open(os.path.join(OUTPUT_ARB_DIR, 'app_en.arb'), 'w', encoding='utf-8') as f:
        json.dump(template_arb, f, ensure_ascii=False, indent=2)

    # 2. Conversion de toutes les langues cibles
    target_dirs = glob.glob(TARGET_XML_DIRS)
    count = 0

    for target_dir in target_dirs:
        dir_name = os.path.basename(target_dir)
        locale = DIR_TO_LOCALE.get(dir_name)
        if not locale:
            print(f"Ignoré: {dir_name} (Locale non reconnue)")
            continue

        xml_file = os.path.join(target_dir, 'strings.xml')
        arb_file_name = f"app_{locale}.arb"
        arb_file_path = os.path.join(OUTPUT_ARB_DIR, arb_file_name)

        arb_dict = parse_xml_to_arb(xml_file, is_template=False)
        if arb_dict:
            # Merge keys from template if missing to guarantee full key coverage
            for key, val in template_arb.items():
                if not key.startswith("@") and key not in arb_dict:
                    # Conversion fallback
                    arb_dict[key] = val

            with open(arb_file_path, 'w', encoding='utf-8') as f:
                json.dump(arb_dict, f, ensure_ascii=False, indent=2)
            print(f"  [OK] {dir_name} -> {arb_file_name} ({len(arb_dict)} clés)")
            count += 1

    print(f"\nTotal: {count + 1} fichiers ARB générés avec succès dans {OUTPUT_ARB_DIR}.\n")

if __name__ == "__main__":
    main()
