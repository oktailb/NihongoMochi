#!/usr/bin/python3
import os
import xml.etree.ElementTree as ET
import glob

# Configuration
BASE_STRINGS_PATH = 'shared/src/commonMain/composeResources/values/strings.xml'
TARGET_DIRS_PATTERN = 'shared/src/commonMain/composeResources/values-*'

def load_keys(file_path):
    """Charge les clés et valeurs d'un fichier strings.xml"""
    try:
        tree = ET.parse(file_path)
        root = tree.getroot()
        keys = {}
        for string in root.findall('string'):
            name = string.get('name')
            translatable = string.get('translatable')
            keys[name] = {
                'value': string.text,
                'translatable': translatable
            }
        return keys
    except Exception as e:
        print(f"Erreur lecture {file_path}: {e}")
        return {}

def update_file(target_path, base_keys):
    """Met à jour un fichier cible avec les clés manquantes"""
    print(f"Traitement de {target_path}...")
    
    try:
        # Lire le fichier existant
        if os.path.exists(target_path):
            tree = ET.parse(target_path)
            root = tree.getroot()
        else:
            # Créer si n'existe pas
            root = ET.Element('resources')
            tree = ET.ElementTree(root)

        # Identifier les clés existantes
        existing_keys = set()
        for string in root.findall('string'):
            existing_keys.add(string.get('name'))
            
            # Mise à jour des attributs translatable=false si nécessaire (ex: noms de langues)
            name = string.get('name')
            if name in base_keys:
                base_attr = base_keys[name].get('translatable')
                if base_attr == 'false':
                    string.set('translatable', 'false')
                    # Force la valeur native pour les noms de langues
                    string.text = base_keys[name]['value']

        # Ajouter les clés manquantes
        added_count = 0
        for key, data in base_keys.items():
            if key not in existing_keys:
                new_elem = ET.SubElement(root, 'string')
                new_elem.set('name', key)
                if data['translatable']:
                    new_elem.set('translatable', data['translatable'])
                
                # Marquer comme TODO pour traduction future, mais mettre la valeur par défaut pour éviter crash
                # Sauf si translatable="false", on met la valeur directe
                if data.get('translatable') == 'false':
                    new_elem.text = data['value']
                else:
                    new_elem.text = data['value'] # Pour l'instant on copie la valeur de base (FR) ou EN si dispo
                
                added_count += 1

        if added_count > 0:
            # Indenter pour une jolie sortie XML
            ET.indent(tree, space="    ", level=0)
            tree.write(target_path, encoding='utf-8', xml_declaration=True)
            print(f"  -> {added_count} clés ajoutées.")
        else:
            print("  -> À jour.")

    except Exception as e:
        print(f"Erreur mise à jour {target_path}: {e}")

def main():
    # 1. Charger les clés de base (le fichier que j'ai mis à jour avec les clés FR/EN complètes)
    print(f"Chargement de {BASE_STRINGS_PATH}...")
    base_keys = load_keys(BASE_STRINGS_PATH)
    print(f"{len(base_keys)} clés trouvées.")

    # 2. Parcourir tous les dossiers values-*
    target_dirs = glob.glob(TARGET_DIRS_PATTERN)
    
    for target_dir in target_dirs:
        target_file = os.path.join(target_dir, 'strings.xml')
        update_file(target_file, base_keys)

if __name__ == "__main__":
    main()
