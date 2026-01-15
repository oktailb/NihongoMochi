#!/usr/bin/python3
import os
import json
import glob

# Configuration
WORDS_DIR = 'shared/src/commonMain/composeResources/files/words'
OUTPUT_FILE = os.path.join(WORDS_DIR, 'merged_wordlist.json')

def load_json(file_path):
    if not os.path.exists(file_path):
        return None
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
    except Exception as e:
        print(f"Erreur lors du chargement de {file_path}: {e}")
        return None

def main():
    # Dictionnaire pour fusionner les mots. Clé unique : texte du mot
    merged_words = {}

    # 1. Traitement des fichiers JLPT
    jlpt_files = glob.glob(os.path.join(WORDS_DIR, 'jlpt_wordlist_n*.json'))
    jlpt_files.sort(reverse=True) # N5 -> N1

    for file_path in jlpt_files:
        level = os.path.basename(file_path).split('_')[-1].replace('.json', '').upper()
        print(f"Traitement de {file_path} (Niveau {level})...")
        data = load_json(file_path)
        if not data: continue
        
        for w in data.get('words', {}).get('word', []):
            text = w.get('#text')
            phonetic = w.get('@phonetics', '').strip()
            if not text: continue
            
            if text not in merged_words:
                merged_words[text] = {
                    "text": text,
                    "phonetics": phonetic,
                    "jlpt": level,
                    "rank": None,
                    "type": w.get('@type'),
                    "is_bccwj": False
                }
            else:
                # Mise à jour JLPT si présent
                merged_words[text]["jlpt"] = level
                # Si on n'a pas encore de source BCCWJ, on peut mettre à jour la phonétique
                if not merged_words[text]["is_bccwj"] and phonetic:
                    merged_words[text]["phonetics"] = phonetic

    # 2. Traitement des fichiers BCCWJ
    bccwj_files = glob.glob(os.path.join(WORDS_DIR, 'bccwj_wordlist_*.json'))
    for file_path in bccwj_files:
        print(f"Traitement de {file_path}...")
        data = load_json(file_path)
        if not data: continue
        
        for w in data.get('words', {}).get('word', []):
            text = w.get('#text')
            phonetic = w.get('@phonetics', '').strip()
            rank = w.get('@rank')
            if not text: continue
            
            if text not in merged_words:
                merged_words[text] = {
                    "text": text,
                    "phonetics": phonetic,
                    "jlpt": None,
                    "rank": rank,
                    "type": w.get('@type'),
                    "is_bccwj": True
                }
            else:
                # Priorité absolue au phonetics du BCCWJ comme demandé
                if phonetic:
                    merged_words[text]["phonetics"] = phonetic
                
                # Mise à jour du rang (on garde le meilleur rang)
                if rank:
                    if not merged_words[text]["rank"] or int(rank) < int(merged_words[text]["rank"]):
                        merged_words[text]["rank"] = rank
                
                if not merged_words[text]["type"]:
                    merged_words[text]["type"] = w.get('@type')
                
                merged_words[text]["is_bccwj"] = True

    # Conversion en liste finale avec IDs générés
    # Tri par rang (puis alphabétique si pas de rang)
    sorted_texts = sorted(merged_words.keys(), key=lambda t: (
        int(merged_words[t]['rank']) if merged_words[t]['rank'] else 999999,
        t
    ))
    
    final_list = []
    for i, text in enumerate(sorted_texts, 1):
        data = merged_words[text]
        entry = {
            "id": str(i),
            "text": data["text"],
            "phonetics": data["phonetics"]
        }
        if data["jlpt"]: entry["jlpt"] = data["jlpt"]
        if data["rank"]: entry["rank"] = data["rank"]
        if data["type"]: entry["type"] = data["type"]
        
        final_list.append(entry)

    output_data = {
        "words": final_list
    }

    with open(OUTPUT_FILE, 'w', encoding='utf-8') as f:
        json.dump(output_data, f, ensure_ascii=False, indent=2)
    
    print(f"\nFusion terminée : {len(final_list)} mots uniques sauvegardés dans {OUTPUT_FILE}")

if __name__ == "__main__":
    main()
