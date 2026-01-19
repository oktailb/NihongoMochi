import json
import re
import os

def is_romaji(text):
    # Vérifie si le texte est composé uniquement de Romaji (standard ou full-width),
    # chiffres, espaces et ponctuation commune.
    # [\u0041-\u005A\u0061-\u007A] : A-Z, a-z
    # [\uFF21-\uFF3A\uFF41-\uFF5A] : Ａ-Ｚ, ａ-ｚ
    romaji_pattern = re.compile(r'^[a-zA-Z0-9\uff21-\uff3a\uff41-\uff5a\uff10-\uff19\s&!?.()\-+*/%]+$')
    return bool(romaji_pattern.match(text))

def convert_to_hiragana(text):
    """Convertit les Katakana en Hiragana."""
    result = ""
    for char in text:
        code = ord(char)
        # Katakana range: 30A1 - 30F6
        if 0x30A1 <= code <= 0x30F6:
            result += chr(code - 0x60)
        else:
            result += char
    return result

def convert_to_katakana(text):
    """Convertit les Hiragana en Katakana."""
    result = ""
    for char in text:
        code = ord(char)
        # Hiragana range: 3041 - 3096
        if 0x3041 <= code <= 0x3096:
            result += chr(code + 0x60)
        else:
            result += char
    return result

def main():
    file_path = 'shared/src/commonMain/composeResources/files/words/merged_wordlist.json'
    
    if not os.path.exists(file_path):
        print(f"Erreur : Le fichier {file_path} est introuvable.")
        return

    print(f"Chargement de {file_path}...")
    with open(file_path, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError as e:
            print(f"Erreur lors de la lecture du JSON : {e}")
            return

    words = data.get('words', [])
    count_to_hira = 0
    count_to_kata = 0

    print("Normalisation en cours...")
    for word in words:
        text = word.get('text', '')
        phonetics = word.get('phonetics', '')
        
        if not phonetics:
            continue

        if is_romaji(text):
            # Cas Romaji -> Katakana
            new_phonetics = convert_to_katakana(phonetics)
            if new_phonetics != phonetics:
                word['phonetics'] = new_phonetics
                count_to_kata += 1
        else:
            # Tout le reste -> Hiragana
            new_phonetics = convert_to_hiragana(phonetics)
            if new_phonetics != phonetics:
                word['phonetics'] = new_phonetics
                count_to_hira += 1

    print(f"Sauvegarde des modifications...")
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

    print(f"Terminé !")
    print(f" - Mots convertis en Hiragana : {count_to_hira}")
    print(f" - Mots convertis en Katakana : {count_to_kata}")

if __name__ == "__main__":
    main()
