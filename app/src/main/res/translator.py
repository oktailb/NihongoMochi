#!/usr/bin/python3

import xml.etree.ElementTree as ET
import argparse
from deep_translator import GoogleTranslator
import time
import sys

def translate_xml(source_file, target_lang):
    # Charger le fichier XML
    tree = ET.parse(source_file)
    root = tree.getroot()
    
    # Initialiser le traducteur
    translator = GoogleTranslator(source='en', target=target_lang)
    
    # Compter le nombre total de significations
    total_meanings = sum(len(kanji.findall('meaning')) for kanji in root.findall('kanji'))
    print(f"Traduction de {total_meanings} significations vers {target_lang}...")
    
    # Traduire toutes les significations
    translated_count = 0
    errors = 0
    
    for kanji in root.findall('kanji'):
        for meaning in kanji.findall('meaning'):
            try:
                # Traduire le texte
                translation = translator.translate(meaning.text)
                # Remplacer par la traduction
                meaning.text = translation
                translated_count += 1
                
                # Afficher la progression
                if translated_count % 100 == 0:
                    print(f"Progression : {translated_count}/{total_meanings} - erreurs : {errors}")
                
                # Pause pour éviter de surcharger l'API
                time.sleep(0.1)
                
            except Exception as e:
                print(f"Erreur lors de la traduction de '{meaning.text}' : {e}")
                errors += 1
                # Pause plus longue en cas d'erreur
                time.sleep(1)
    
    print(f"Traduction terminée : {translated_count} traduits, {errors} erreurs")
    
    # Mettre à jour la locale
    locale_map = {
        'fr': 'fr_FR',
        'es': 'es_ES',
        'de': 'de_DE',
        'it': 'it_IT',
        'pt': 'pt_PT',
        'ru': 'ru_RU',
        'ja': 'ja_JP',
        'zh': 'zh_CN',
        'ko': 'ko_KR'
    }
    
    new_locale = locale_map.get(target_lang, f'{target_lang}_{target_lang.upper()}')
    root.set('locale', new_locale)
    
    # Sauvegarder dans un nouveau fichier
    output_file = f"meanings_{target_lang}.xml"
    tree.write(output_file, encoding='utf-8', xml_declaration=True)
    print(f"Fichier sauvegardé : {output_file}")

if __name__ == "__main__":
    # Configuration des arguments en ligne de commande
    parser = argparse.ArgumentParser(description='Traduire le fichier meanings.xml vers une autre langue')
    parser.add_argument('lang', help='Code de langue cible (ex: fr, es, de, it, etc.)')
    
    args = parser.parse_args()
    
    # Appeler la fonction de traduction
    translate_xml('meanings.xml', args.lang)
    
