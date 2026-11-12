"""提取鬼火与小尸王的来源模型、动画、纹理和刷怪蛋颜色，不声明行为验收。"""
from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
import re

from convert_content import ROOT, Source
from convert_yaoshou_019_036 import model_row

MOD = 'yvanchuxiuzhen'


def build() -> dict:
    source = Source(MOD)
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    rows, eggs, assets = [], [], []
    for number in (2, 3):
        name, identifier = f'Guiguai{number:02}', f'guiguai_{number:02}'
        row = model_row(java, name, identifier)
        rows.append(row)
        assets.extend(({'owner': 'creature', 'kind': 'geo', 'id': f'{MOD}:{row["geo"]}'},
                       {'owner': 'creature', 'kind': 'animation', 'id': f'{MOD}:{row["animation"]}'}))
        for texture in {row['texture']} | ({row['glow']} if 'glow' in row else set()):
            assets.append({'owner': 'creature', 'kind': 'texture', 'id': f'{MOD}:entities/{texture}'})
        assets.append({'owner': 'creature', 'kind': 'item', 'id': f'{MOD}:{identifier}_spawn_egg'})
        egg = re.search(rf'register\("{identifier}_spawn_egg",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+),', source.items_text)
        eggs.append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
    return {'models': rows, 'eggs': eggs, 'system_assets': assets}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write-resources', action='store_true')
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/guiguai-002-003-resources.json')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.write_resources:
        for key in ('models', 'eggs'):
            (ROOT / f'src/main/resources/content/creature/guiguai_{key}.json').write_text(
                json.dumps(report[key], ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
        path = ROOT / 'docs/reference/conversion/system_assets.csv'
        with path.open(encoding='utf-8', newline='') as handle:
            reader = csv.DictReader(handle)
            fields, rows = reader.fieldnames, list(reader)
        for row in report['system_assets']:
            if row not in rows:
                rows.append(row)
        with path.open('w', encoding='utf-8', newline='') as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, lineterminator='\n')
            writer.writeheader()
            writer.writerows(rows)
    print(json.dumps({'models': len(report['models']), 'eggs': len(report['eggs']),
                      'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
