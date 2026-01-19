#!/usr/bin/python3
import os
import glob
import time
from bs4 import BeautifulSoup
from deep_translator import GoogleTranslator

# Configuration
LESSONS_DIR = 'shared/src/commonMain/composeResources/files/grammar/lessons'
SOURCE_LANG = 'en'
# Ajoutez ici les langues que vous souhaitez supporter (doit correspondre aux noms de dossiers)
TARGET_LANGS = {
    'fr': 'fr',
    'es': 'es',
    'de': 'de',
    'it': 'it',
    'ja': 'ja',
    'th': 'th',
    'mn': 'mn',
    'ko': 'ko',
    'zh': 'zh-CN',
    'ar': 'ar',
    'bn': 'bn',
    'id': 'id',
    'pt': 'pt',
    'ru': 'ru',
    'uk': 'uk',
    'vi': 'vi'
}

def translate_html(html_content, target_lang):
    soup = BeautifulSoup(html_content, 'html.parser')
    translator = GoogleTranslator(source=SOURCE_LANG, target=target_lang)
    
    # On traduit le contenu des balises textuelles courantes
    tags_to_translate = ['h1', 'h2', 'h3', 'p', 'li', 'th', 'td', 'strong', 'em']
    
    for tag_name in tags_to_translate:
        for tag in soup.find_all(tag_name):
            # Ne traduire que si la balise contient du texte direct et n'est pas vide
            if tag.string and tag.string.strip():
                try:
                    translated = translator.translate(tag.string)
                    if translated:
                        tag.string.replace_with(translated)
                    time.sleep(0.1) # Petit délai pour le rate limit
                except Exception as e:
                    print(f"Erreur de traduction pour '{tag.string[:20]}...': {e}")
            
            # Gérer les balises avec des enfants (ex: <p>Texte <strong>Gras</strong> Texte</p>)
            # Pour simplifier et éviter de casser le HTML, on traite les segments de texte
            elif len(tag.contents) > 1:
                for content in tag.contents:
                    if hasattr(content, 'name') is False and content.strip(): # C'est un NavigableString
                        try:
                            translated = translator.translate(content)
                            if translated:
                                content.replace_with(translated)
                            time.sleep(0.1)
                        except Exception as e:
                            print(f"Erreur de traduction segment: {e}")

    return str(soup)

def main():
    # Liste des fichiers source (ceux à la racine de LESSONS_DIR)
    source_files = glob.glob(os.path.join(LESSONS_DIR, "*.html"))
    
    for lang_code, translator_code in TARGET_LANGS.items():
        target_dir = os.path.join(LESSONS_DIR, lang_code)
        
        # Créer le dossier s'il n'existe pas
        if not os.path.exists(target_dir):
            os.makedirs(target_dir)
            print(f"Création du dossier: {target_dir}")

        print(f"\n--- Traduction vers {lang_code.upper()} ---")
        
        for source_path in source_files:
            filename = os.path.basename(source_path)
            target_path = os.path.join(target_dir, filename)
            
            # On traduit si le fichier n'existe pas ou s'il est plus vieux que la source
            if not os.path.exists(target_path) or os.path.getmtime(source_path) > os.path.getmtime(target_path):
                print(f"Traduction de {filename}...")
                
                with open(source_path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                translated_html = translate_html(content, translator_code)
                
                with open(target_path, 'w', encoding='utf-8') as f:
                    f.write(translated_html)
                
                print(f"  -> Sauvegardé dans {target_dir}")
            else:
                print(f"Skipping {filename} (déjà à jour)")

if __name__ == "__main__":
    main()
