"""提取1.18.2版本功法与技能装配页的贴图和语言，合并本页转换记录。"""
from __future__ import annotations

import json
import re

from build_ledger import NAMESPACE
from convert_assets import MANIFEST, Converter
from convert_content import CONVERSION, LANG, LANGUAGES, Source
from extract_equipment_armors import read_csv, write_csv


def main() -> None:
    source = Source(NAMESPACE)
    screen = source.java('client/gui', 'PlayerGUI04Screen')
    textures = list(dict.fromkeys(re.findall(r'new ResourceLocation\("yvanchuxiuzhen:(textures/[^"\n]+\.png)"\)', screen)))
    path = CONVERSION / 'system_assets.csv'
    columns, rows = read_csv(path)
    seen = {(row['owner'], row['kind'], row['id']) for row in rows}
    converter = Converter()
    for texture in textures:
        resource = f'{NAMESPACE}:{texture.removeprefix("textures/").removesuffix(".png")}'
        entry = ('skill', 'texture', resource)
        if entry not in seen:
            rows.append(dict(zip(columns, entry)))
            seen.add(entry)
        converter.copy_texture(resource, NAMESPACE)
    if converter.missing:
        raise FileNotFoundError(converter.missing)
    write_csv(path, columns, rows)

    prefix = f'gui.{NAMESPACE}.player_gui_04.'
    for language in LANGUAGES:
        path = LANG / language / 'skill.json'
        entries = json.loads(path.read_text(encoding='utf-8'))
        entries.update({key: value for key, value in source.lang[language].items() if key.startswith(prefix)})
        path.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')

    path = CONVERSION / 'lang_prefixes.csv'
    columns, rows = read_csv(path)
    rows = [row for row in rows if not (row['source_mod'] == NAMESPACE and row['prefix'] == prefix)]
    rows.append(dict(zip(columns, ('skill', NAMESPACE, prefix))))
    write_csv(path, columns, rows)

    columns, rows = read_csv(MANIFEST)
    by_target = {row['target']: row for row in rows}
    for row in converter.manifest:
        entry = dict(zip(columns, row))
        by_target[entry['target']] = entry
    write_csv(MANIFEST, columns, sorted(by_target.values(), key=lambda row: row['target']))
    print('skill_loadout textures', len(textures), 'copied', len(converter.manifest))


if __name__ == '__main__':
    main()
