#!/usr/bin/python3
import os
import json
import time
import glob
from deep_translator import GoogleTranslator

# Configuration - Dossiers strictement séparés de ceux des Kanjis
MERGED_FILE = 'shared/src/commonMain/composeResources/files/words/merged_wordlist.json'
OUTPUT_DIR = 'shared/src/commonMain/composeResources/files/words/meanings'

LANG_MAP = {
    'ar_rSA': 'ar', 'bn_rBD': 'bn', 'de_rDE': 'de', 'en_rGB': 'en',
    'es_rES': 'es', 'fr_rFR': 'fr', 'in_rID': 'id', 'it_rIT': 'it',
    'ko_rKR': 'ko', 'mn_rMN': 'mn', 'pt_rBR': 'pt', 'ru_rRU': 'ru',
    'th_rTH': 'th', 'ua_rUA': 'uk', 'vi_rVN': 'vi', 'zh_rCN': 'zh-CN'
}

def load_json(file_path):
    if not os.path.exists(file_path): return None
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Erreur lecture {file_path}: {e}")
        return None

def save_json(file_path, data):
    try:
        data['word_meanings']['entries'].sort(key=lambda x: int(x['@id']))
        with open(file_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
    except Exception as e:
        print(f"Erreur sauvegarde {file_path}: {e}")

def main():
    if not os.path.exists(OUTPUT_DIR):
        os.makedirs(OUTPUT_DIR)

    print(f"Chargement du dictionnaire de mots: {MERGED_FILE}")
    merged_data = load_json(MERGED_FILE)
    if not merged_data: 
        print("Erreur: merged_wordlist.json introuvable. Lancez d'abord merge_wordlists.py")
        return
    
    words = merged_data.get('words', [])

    for locale, target_lang in LANG_MAP.items():
        if target_lang == 'ja': continue
        
        out_file = os.path.join(OUTPUT_DIR, f'word_meanings_{locale}.json')
        print(f"\n--- Traduction Vocabulaire: {locale} ({target_lang}) ---")
        
        target_data = load_json(out_file)
        # Structure spécifique aux mots pour éviter toute confusion avec les kanjis
        if not target_data:
            target_data = {"word_meanings": {"@locale": locale, "entries": []}}
        
        existing_meanings = {str(m['@id']): m for m in target_data['word_meanings'].get('entries', [])}
        
        translator = GoogleTranslator(source='ja', target=target_lang)
        updates_count = 0
        
        for w in words:
            w_id = str(w['id'])
            if w_id not in existing_meanings:
                try:
                    # Traduction directe du japonais vers la cible
                    translated = translator.translate(w['text'])
                    if translated:
                        meaning_text = translated.strip().capitalize()
                        new_entry = {"@id": w_id, "meaning": meaning_text}
                        target_data['word_meanings']['entries'].append(new_entry)
                        existing_meanings[w_id] = new_entry
                        print(f"    ID {w_id}: {w['text']} -> {meaning_text}")
                        updates_count += 1
                        
                        if updates_count % 100 == 0:
                            save_json(out_file, target_data)
                            print(f"    [Checkpoint] {updates_count} mots sauvegardés...")
                            
                        time.sleep(0.1) # Pause anti-ban
                except Exception as e:
                    print(f"    Erreur sur {w['text']}: {e}")
                    time.sleep(0.2)

        if updates_count > 0:
            save_json(out_file, target_data)
            print(f"  -> Terminé: {out_file} ({updates_count} nouveaux)")

if __name__ == "__main__":
    main()
