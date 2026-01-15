#!/usr/bin/python3
import os
import xml.etree.ElementTree as ET
import glob
from deep_translator import GoogleTranslator
import time

# Configuration
SOURCE_FILE = 'shared/src/commonMain/composeResources/values/strings.xml'
TARGET_DIRS_PATTERN = 'shared/src/commonMain/composeResources/values-*'
SOURCE_LANG = 'en'

# Mapping des dossiers Android vers les codes langues Google Translate
LANG_MAP = {
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
    'values-zh-rCN': 'zh-CN'
}

def load_xml_as_dict(file_path):
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        data = {}
        for string in root.findall('string'):
            name = string.get('name')
            translatable = string.get('translatable')
            data[name] = {
                'text': string.text,
                'translatable': translatable != 'false',
                'element': string
            }
        return tree, root, data
    except Exception as e:
        print(f"Erreur lecture {file_path}: {e}")
        return None, None, {}

def main():
    print(f"Chargement de la source ({SOURCE_LANG}): {SOURCE_FILE}")
    _, _, source_data = load_xml_as_dict(SOURCE_FILE)
    
    target_dirs = glob.glob(TARGET_DIRS_PATTERN)
    
    for target_dir in target_dirs:
        dir_name = os.path.basename(target_dir)
        target_lang = LANG_MAP.get(dir_name)
        
        if not target_lang:
            print(f"Skipping {dir_name} (Langue non reconnue dans le mapping)")
            continue
            
        target_file = os.path.join(target_dir, 'strings.xml')
        print(f"\nTraitement de {dir_name} -> {target_lang}...")
        
        # S'assurer que le répertoire existe avant d'essayer de charger le fichier
        if not os.path.exists(target_file):
            print(f"Le fichier {target_file} n'existe pas encore. Il sera créé par sync_strings.py.")
            continue

        tree, root, target_data = load_xml_as_dict(target_file)
        if not tree:
            continue
            
        translator = GoogleTranslator(source=SOURCE_LANG, target=target_lang)
        
        modified = False
        updates_count = 0
        
        # Liste des clés à traduire
        keys_to_translate = []
        
        for key, info in target_data.items():
            # Si la clé existe dans la source
            if key in source_data:
                source_text = source_data[key]['text']
                target_text = info['text']
                
                # Critère : Si le texte cible est IDENTIQUE au texte source (copie brute)
                # ET que c'est traduisible
                # ET que le texte n'est pas vide
                if (target_text == source_text and 
                    info['translatable'] and 
                    source_text and 
                    len(source_text.strip()) > 0):
                    
                    # On ignore les noms de langues qui sont marqués translatable=false normalement,
                    # mais double sécurité ici
                    if not key.startswith("language_"):
                        keys_to_translate.append((key, source_text, info['element']))

        print(f"  -> {len(keys_to_translate)} chaînes à traduire.")
        
        # Traduction par lot ou un par un
        for key, text, element in keys_to_translate:
            try:
                print(f"    Traduction de '{key}'...", end='', flush=True)
                translated = translator.translate(text)
                
                if translated and translated != text:
                    element.text = translated
                    print(f" OK: {translated}")
                    modified = True
                    updates_count += 1
                else:
                    print(" Ignoré (identique ou vide)")
                
                # Petit délai pour éviter le rate limit
                time.sleep(0.2)
                
            except Exception as e:
                print(f" ERREUR: {e}")
        
        if modified:
            ET.indent(tree, space="    ", level=0)
            tree.write(target_file, encoding='utf-8', xml_declaration=True)
            print(f"  -> Fichier sauvegardé with {updates_count} traductions.")
        else:
            print("  -> Aucune modification.")

if __name__ == "__main__":
    main()
