"""提取001、002号妖兽的来源注册、模型、皮肤、刷怪蛋与语言；不声明行为闭合。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from convert_content import ROOT, Source, parse_item_properties
from convert_yaoshou_019_036 import model_row

MOD = 'yvanchuxiuzhen'


def build() -> dict:
    source = Source(MOD)
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    assets = source.root / f'src/main/resources/assets/{MOD}'
    registrations = list(csv.DictReader((ROOT / 'docs/reference/legacy/inventory/registrations.csv').open(encoding='utf-8')))
    orders = {row['legacy_id']: index for index, row in enumerate((row for row in registrations if row['registry'] == 'item'), 1)}
    report = {key: [] for key in ('models', 'eggs', 'entity_rows_for_integration', 'item_rows_for_integration',
                                  'factory_overrides_for_integration', 'system_assets_for_integration', 'source_proof')}
    report.update({'copies': {}, 'lang_for_integration': {language: {} for language in source.lang}})

    def copy(kind: str, identifier: str, relative: str) -> None:
        path = assets / relative
        report['copies'][f'assets/{MOD}/{relative}'] = {'source': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
        report['system_assets_for_integration'].append({'owner': 'creature', 'kind': kind, 'id': MOD + ':' + identifier})

    for number in (1, 2):
        name, identifier = f'Yaoshou{number:03}', f'yaoshou_{number:03}'
        model = model_row(java, name, identifier)
        report['models'].append(model)
        copy('geo', model['geo'], f'geo/{model["geo"]}.geo.json')
        copy('animation', model['animation'], f'animations/{model["animation"]}.animation.json')
        skins = ('yaoshou001', 'yaoshou00102', 'yaoshou00103') if number == 1 else tuple(f'yaoshou0020{skin}' for skin in range(1, 6))
        for skin in skins:
            copy('texture', 'entities/' + skin, f'textures/entities/{skin}.png')
        builder = re.search(rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);', source.entities_text, re.S)
        chain = builder[2]
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
        report['entity_rows_for_integration'].append({'id': identifier, 'category': builder[1],
            'width': float(size[1]), 'height': float(size[2]),
            'tracking_range': int(re.search(r'setTrackingRange\((\d+)\)', chain)[1]),
            'update_interval': int(re.search(r'setUpdateInterval\((\d+)\)', chain)[1]), 'fire_immune': '.fireImmune()' in chain})
        egg_id = identifier + '_spawn_egg'
        egg = re.search(rf'register\("{egg_id}",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);', source.items_text, re.S)
        factory = 'creature:spawn_egg_' + identifier
        report['eggs'].append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
        report['item_rows_for_integration'].append({'id': egg_id, 'factory': factory,
            'order': orders[MOD + ':' + egg_id], **parse_item_properties(egg[3], source.tabs)})
        report['factory_overrides_for_integration'].append({'registry': 'item', 'target_id': MOD + ':' + egg_id,
            'factory': factory, 'owner': 'creature', 'reason': f'{name}来源颜色与创造栏刷怪蛋；行为验证单独登记'})
        copy('item', egg_id, f'models/item/{egg_id}.json')
        for language, names in source.lang.items():
            for key in (f'entity.{MOD}.{identifier}', f'item.{MOD}.{egg_id}'):
                report['lang_for_integration'][language][key] = names[key]
        path = java / f'entity/{name}Entity.java'
        report['source_proof'].append({'id': identifier, 'source': str(path),
            'sha256': hashlib.sha256(path.read_bytes()).hexdigest(),
            'experience_reward': int(re.search(r'xpReward = (\d+);', path.read_text(encoding='utf-8'))[1])})
    for language, names in source.lang.items():
        for key, value in names.items():
            if key.startswith('attribute.') and any(identifier in key for identifier in ('entitydaxiao', 'yaoshoupifuxianshi')):
                report['lang_for_integration'][language][key] = value
    report['verification'] = {'static_source': '来源注册、属性、模型、皮肤、经验、刷怪蛋与语言已核对。',
        'build': '未执行', 'client': '未执行', 'server': '未执行',
        'remaining': '灵兽袋死亡清理和完整行为验收未闭合；群系投放由World批次处理。'}
    return report


def write_resources(report: dict) -> None:
    target = ROOT / 'src/main/resources'
    for suffix, rows in (('models', report['models']), ('eggs', report['eggs'])):
        (target / f'content/creature/yaoshou_001_002_{suffix}.json').write_text(json.dumps(rows, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    for relative, entry in report['copies'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(Path(entry['source']).read_bytes())
    materials = json.loads((ROOT / 'tools/porting/data/legacy_block_materials.json').read_text(encoding='utf-8'))
    grass = sorted(row['target_id'] for row in materials['blocks'] if row['material'] == 'GRASS')
    path = target / f'data/{MOD}/tags/block/legacy_grass_material.json'
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps({'replace': False, 'values': grass}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/yaoshou-001-002-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.write_resources:
        write_resources(report)
    print(json.dumps({'entities': 2, 'source_assets': len(report['copies']), 'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
