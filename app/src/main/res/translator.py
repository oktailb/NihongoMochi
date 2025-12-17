#!/usr/bin/python3
import xml.etree.ElementTree as ET
import argparse
import os
import json
from deep_translator import GoogleTranslator
import time
import sys

class TranslationManager:
    def __init__(self, source_file, target_lang):
        self.source_file = source_file
        self.target_lang = target_lang
        self.output_file = f"meanings_{target_lang}.xml"
        self.progress_file = f"translation_progress_{target_lang}.json"
        
        # Charger le fichier XML
        self.tree = ET.parse(source_file)
        self.root = self.tree.getroot()
        
        # Initialiser le traducteur
        self.translator = GoogleTranslator(source='en', target=target_lang)
        
        # Charger ou initialiser la progression
        self.progress = self.load_progress()
    
    def load_progress(self):
        """Charger la progression depuis le fichier"""
        if os.path.exists(self.progress_file):
            try:
                with open(self.progress_file, 'r', encoding='utf-8') as f:
                    return json.load(f)
            except:
                pass
        
        # Initialisation par défaut
        return {
            'last_kanji_id': 0,
            'total_translated': 0,
            'last_save_time': time.time(),
            'errors': 0
        }
    
    def save_progress(self, kanji_id, total_translated, errors):
        """Sauvegarder la progression"""
        self.progress = {
            'last_kanji_id': kanji_id,
            'total_translated': total_translated,
            'last_save_time': time.time(),
            'errors': errors
        }
        
        with open(self.progress_file, 'w', encoding='utf-8') as f:
            json.dump(self.progress, f, ensure_ascii=False, indent=2)
    
    def save_xml(self):
        """Sauvegarder le fichier XML"""
        # Mettre à jour la locale
        locale_map = {
            'fr': 'fr_FR',
            'es': 'es_ES',
            'de': 'de_DE',
            'it': 'it_IT',
            'pt': 'pt_PT',
            'br': 'pt_BR',
            'ru': 'ru_RU',
            'ja': 'ja_JP',
            'zh': 'zh_CN',
            'ko': 'ko_KR',
            'ar' : 'ar_AR',
            'de' : 'de_DE',
            'th' : 'th_TH',
            'vi' : 'vt_VT',
            'hy' : 'hy_AR',
            'bn' : 'bn_BN',
            'zh-CN' : 'zh_CN',
            'zh-TW' : 'zh_TW',
            'af' : 'af_AF',
            'co' : 'co_CO',
            'nl' : 'nl_NL',
            'eo' : 'eo_EO',
            'et' : 'et_ET',
            'tl' : 'tl_TL',
            'fi' : 'fi_FI',
            'el' : 'el_GR',
            'hi' : 'hi_IN',
            'lo' : 'lo_LO',
            'la' : 'la_LA',
            'lv' : 'lv_LV',
            'lt' : 'lt_LT',
            'mn' : 'mn_MO',
            'my' : 'my_MY',
            'ne' : 'ne_NE',
            'no' : 'no_NO',
            'fa' : 'fa_IR',
            'pl' : 'pl_PL',
            'ro' : 'ro_RO'
        }
 

        new_locale = locale_map.get(self.target_lang, f'{self.target_lang}_{self.target_lang.upper()}')
        self.root.set('locale', new_locale)
        
        # Sauvegarder
        self.tree.write(self.output_file, encoding='utf-8', xml_declaration=True)
    
    def translate_batch_with_retry(self, texts, max_retries=3):
        """Traduire un lot avec réessai en cas d'échec"""
        for attempt in range(max_retries):
            try:
                return self.translator.translate_batch(texts)
            except Exception as e:
                if attempt == max_retries - 1:
                    raise e
                print(f"Tentative {attempt + 1} échouée, nouvel essai dans {2 ** attempt} secondes...")
                time.sleep(2 ** attempt)  # Attente exponentielle
        return None
    
    def run(self, save_interval=10, batch_size=30):
        """Exécuter la traduction"""
        # Collecter tous les textes et éléments à traduire
        all_items = []
        
        for kanji in self.root.findall('kanji'):
            kanji_id = int(kanji.get('id'))
            # Si nous avons déjà traduit ce kanji (reprise)
            if kanji_id <= self.progress['last_kanji_id']:
                continue
                
            for meaning in kanji.findall('meaning'):
                all_items.append({
                    'kanji_id': kanji_id,
                    'meaning': meaning,
                    'text': meaning.text
                })
        
        total_items = len(all_items)
        print(f"Éléments restants à traduire : {total_items}")
        
        if total_items == 0:
            print("Tout est déjà traduit !")
            return
        
        # Traduire par lots
        translated_count = self.progress['total_translated']
        errors = self.progress['errors']
        
        for i in range(0, total_items, batch_size):
            batch = all_items[i:i+batch_size]
            batch_texts = [item['text'] for item in batch]
            
            try:
                # Traduire le lot
                translations = self.translate_batch_with_retry(batch_texts)
                
                # Mettre à jour les éléments
                for item, translation in zip(batch, translations):
                    item['meaning'].text = translation
                
                translated_count += len(batch)
                last_kanji_id = batch[-1]['kanji_id']
                
                # Sauvegarder périodiquement
                if (i // batch_size) % save_interval == 0 or i + batch_size >= total_items:
                    print(f"Progression : {translated_count}/{total_items + self.progress['total_translated']} | "
                          f"Dernier kanji : {last_kanji_id} | Erreurs : {errors}")
                    
                    self.save_xml()
                    self.save_progress(last_kanji_id, translated_count, errors)
                    print(f"Sauvegarde effectuée à {time.strftime('%H:%M:%S')}")
                
                # Pause pour éviter les limites d'API
                time.sleep(2)
                
            except Exception as e:
                print(f"Erreur sur le lot {i//batch_size} : {e}")
                errors += len(batch)
                
                # Sauvegarder l'état actuel en cas d'erreur
                if i > 0:
                    last_kanji_id = batch[0]['kanji_id'] - 1  # Retour au lot précédent
                    self.save_xml()
                    self.save_progress(last_kanji_id, translated_count, errors)
                
                print("Pause de 30 secondes avant de réessayer...")
                time.sleep(30)
        
        # Sauvegarde finale
        self.save_xml()
        self.save_progress(0, translated_count, errors)  # 0 indique la fin
        
        print(f"Traduction terminée !")
        print(f"Total traduit : {translated_count}")
        print(f"Erreurs : {errors}")
        print(f"Fichier sauvegardé : {self.output_file}")
        
        # Supprimer le fichier de progression
        if os.path.exists(self.progress_file):
            os.remove(self.progress_file)

def main():
    parser = argparse.ArgumentParser(description='Traduire le fichier meanings.xml vers une autre langue')
    parser.add_argument('lang', help='Code de langue cible (ex: fr, es, de, it, etc.)')
    parser.add_argument('--save-interval', type=int, default=10, 
                       help='Intervalle de sauvegarde (en lots)')
    parser.add_argument('--batch-size', type=int, default=30,
                       help='Taille des lots de traduction')
    parser.add_argument('--resume', action='store_true',
                       help='Reprendre la traduction à partir de la dernière sauvegarde')
    
    args = parser.parse_args()
    
    # Vérifier si le fichier source existe
    if not os.path.exists('meanings.xml'):
        print("Erreur : fichier 'meanings.xml' introuvable")
        sys.exit(1)
    
    # Créer le gestionnaire de traduction
    manager = TranslationManager('meanings.xml', args.lang)
    
    # Vérifier si on veut reprendre ou recommencer
    if not args.resume and os.path.exists(manager.progress_file):
        response = input("Une progression existante a été trouchée. Voulez-vous la supprimer et recommencer ? (o/N) ")
        if response.lower() == 'o':
            os.remove(manager.progress_file)
            manager.progress = manager.load_progress()  # Recharger avec valeurs par défaut
    
    # Exécuter la traduction
    try:
        manager.run(save_interval=args.save_interval, batch_size=args.batch_size)
    except KeyboardInterrupt:
        print("\nTraduction interrompue par l'utilisateur")
        print("La progression a été sauvegardée. Utilisez --resume pour reprendre.")
    except Exception as e:
        print(f"Erreur fatale : {e}")
        print("La progression a été sauvegardée. Utilisez --resume pour reprendre.")

if __name__ == "__main__":
    main()
