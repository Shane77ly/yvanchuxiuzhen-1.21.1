"""提取完整 Organism01–08 蝴蝶族；默认写隔离计划，--write-resources 仅落独立资源。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from catalog_world_entities import methods
from convert_content import ROOT, Source, parse_item_properties
from extract_entity_models import parse

MOD = 'yvanchuxiuzhen'
IDS = [f'organism_{number:02}' for number in range(1, 9)]


def build() -> dict:
    source = Source(MOD)
    resources = source.root / f'src/main/resources/assets/{MOD}'
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    baseline = None
    outputs = {}
    asset_copies = {}
    models, eggs, entities, items = [], [], [], []
    lang = {language: {} for language in source.lang}
    with (ROOT / 'docs/systems/creature/ledger/content.csv').open(encoding='utf-8', newline='') as stream:
        ledger = {row['legacy_id']: row for row in csv.DictReader(stream) if row['registry'] == 'item'}
    proof = []
    for number, identifier in enumerate(IDS, 1):
        name = f'Organism{number:02}'
        body = (java / f'entity/{name}Entity.java').read_text(encoding='utf-8')
        normalized = {method['name']: re.sub(r'Organism0[1-8]|organism0[1-8]|ORGANISM_0[1-8]', 'OrganismNN', method['source'])
                      for method in methods(body) if method['name'] not in ('addLivingEntityToBiomes', 'baseTick')}
        if baseline is None:
            baseline = normalized
        if normalized != baseline:
            raise ValueError(f'{name} 非同构来源：禁止套用蝴蝶族行为')
        proof.append({'id': identifier, 'source_sha256': hashlib.sha256(body.encode('utf-8')).hexdigest(),
                      'identical_normalized_methods': sorted(normalized)})
        row = {'id': identifier, **parse(name)}
        models.append(row)
        for kind, relative in (('geo', f'geo/{row["geo"]}.geo.json'),
                               ('animation', f'animations/{row["animation"]}.animation.json'),
                               ('texture', f'textures/entities/{row["texture"]}.png')):
            path = resources / relative
            if not path.is_file():
                raise FileNotFoundError(path)
            asset_copies[f'assets/{MOD}/{relative}'] = str(path)
        match = re.search(rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
                          source.entities_text, re.S)
        if match is None:
            raise ValueError(f'未找到实体构建器 {identifier}')
        chain = match[2]
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
        tracking = re.search(r'setTrackingRange\((\d+)\)', chain)
        interval = re.search(r'setUpdateInterval\((\d+)\)', chain)
        entities.append({'id': identifier, 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
                         'tracking_range': int(tracking[1]) if tracking else 5,
                         'update_interval': int(interval[1]) if interval else 3, 'fire_immune': '.fireImmune()' in chain})
        egg_id = identifier + '_spawn_egg'
        egg_match = re.search(rf'register\("{egg_id}",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);',
                              source.items_text, re.S)
        if egg_match is None:
            raise ValueError(f'未找到刷怪蛋 {egg_id}')
        eggs.append({'id': identifier, 'base_color': int(egg_match[1]), 'spot_color': int(egg_match[2])})
        item_order = int(ledger[MOD + ':' + egg_id]['line'])
        # 正式 convert_content 使用全量注册清单中的原始次序，集成时以其输出为准。
        items.append({'id': egg_id, 'factory': 'creature:spawn_egg_' + identifier,
                      'ledger_source_line': item_order, **parse_item_properties(egg_match[3], source.tabs)})
        egg_model = resources / f'models/item/{egg_id}.json'
        if not egg_model.is_file():
            raise FileNotFoundError(egg_model)
        asset_copies[f'assets/{MOD}/models/item/{egg_id}.json'] = str(egg_model)
        loot = source.root / f'src/main/resources/data/{MOD}/loot_tables/entities/{identifier}.json'
        if loot.is_file():
            asset_copies[f'data/{MOD}/loot_table/entities/{identifier}.json'] = str(loot)
        for language, names in source.lang.items():
            for key in (f'entity.{MOD}.{identifier}', f'item.{MOD}.{egg_id}'):
                lang[language][key] = names[key]
    outputs['content/creature/butterfly_models.json'] = models
    outputs['content/creature/butterfly_eggs.json'] = eggs
    return {'source': str(source.root), 'ids': IDS, 'outputs': outputs, 'copies': asset_copies,
            'entity_rows_for_integration': entities, 'item_factory_rows_for_integration': items,
            'lang_for_integration': lang, 'source_method_proof': proof,
            'integration': ['将16条实体/刷怪蛋台账状态、8条工厂覆盖与资源归属交由主会话同批更新后，运行正式转换。',
                            'CreatureSetup在CONTENT.register前调用Butterflies.register(CONTENT,modBus)。',
                            '客户端装配调用ButterflyClient.register(modBus,CONTENT)。',
                            '群系生成列表已由41群系计划记录，正式自然投放随World批次闭合。'],
            'verification': {'static_source': '八种完整方法归一同构核对；08刻过程单独为flash_ice粒子',
                             'build': '未执行', 'client': '未执行', 'server': '未执行'}}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/butterfly-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    if args.write_resources:
        target = ROOT / 'src/main/resources'
        for relative, rows in report['outputs'].items():
            path = target / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text('[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in rows) + '\n]\n', encoding='utf-8')
        for relative, source in report['copies'].items():
            path = target / relative
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(Path(source).read_bytes())
    print(json.dumps({'entities': len(report['ids']), 'copied_assets': len(report['copies']),
                      'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
