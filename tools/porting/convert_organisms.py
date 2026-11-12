"""按锁定 1.18.2 来源转换 Organism09–12；只写族专属资源，共享登记行交由主会话合并。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from catalog_world_biomes import external_spawns, registered_entities
from catalog_world_entities import methods
from convert_content import ROOT, Source, parse_item_properties
from convert_data import Converter
from extract_entity_models import parse

MOD = 'yvanchuxiuzhen'
IDS = tuple(f'organism_{number:02}' for number in range(9, 13))


def build() -> dict:
    source = Source(MOD)
    resources = source.root / f'src/main/resources/assets/{MOD}'
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    data_root = source.root / f'src/main/resources/data/{MOD}'
    copies, models, eggs, entities, items, proof = {}, [], [], [], [], []
    lang = {language: {} for language in source.lang}
    sounds = json.loads((resources / 'sounds.json').read_text(encoding='utf-8'))
    used_sounds = {}
    overrides, assets = [], []
    loot_converter = Converter()
    generated_data = {}
    registrations = list(csv.DictReader((ROOT / 'docs/reference/legacy/inventory/registrations.csv').open(encoding='utf-8')))
    item_order = {row['legacy_id']: number for number, row in enumerate((row for row in registrations if row['registry'] == 'item'), 1)}
    dimension_biomes = {entry['biome'] for dimension in ('dimensionrenjian', 'dimensionyaohun')
                        for entry in json.loads((data_root / f'dimension/{dimension}.json').read_text(encoding='utf-8'))['generator']['biome_source']['biomes']}
    spawn_events = external_spawns(source.root, java, dimension_biomes, registered_entities(java), set())

    def copy(relative: str, path: Path) -> None:
        if not path.is_file():
            raise FileNotFoundError(path)
        copies[relative] = {'source': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}

    for identifier in IDS:
        number = int(identifier.rsplit('_', 1)[1])
        name = f'Organism{number:02}'
        path = java / f'entity/{name}Entity.java'
        body = path.read_text(encoding='utf-8')
        definitions = methods(body)
        row = {'id': identifier, **parse(name)}
        models.append(row)
        for kind, relative, asset_id in (('geo', f'geo/{row["geo"]}.geo.json', row['geo']),
                                          ('animation', f'animations/{row["animation"]}.animation.json', row['animation']),
                                          ('texture', f'textures/entities/{row["texture"]}.png', 'entities/' + row['texture'])):
            copy(f'assets/{MOD}/{relative}', resources / relative)
            assets.append({'owner': 'creature', 'kind': kind, 'id': MOD + ':' + asset_id})
        if 'glow' in row:
            relative = f'textures/entities/{row["glow"]}.png'
            copy(f'assets/{MOD}/{relative}', resources / relative)
            assets.append({'owner': 'creature', 'kind': 'texture', 'id': MOD + ':entities/' + row['glow']})
        builder = re.search(rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);', source.entities_text, re.S)
        if builder is None:
            raise ValueError(f'未找到实体构建器：{identifier}')
        chain = builder[2]
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
        tracking = re.search(r'setTrackingRange\((\d+)\)', chain)
        interval = re.search(r'setUpdateInterval\((\d+)\)', chain)
        entities.append({'id': identifier, 'category': builder[1], 'width': float(size[1]), 'height': float(size[2]),
                         'tracking_range': int(tracking[1]) if tracking else 5,
                         'update_interval': int(interval[1]) if interval else 3, 'fire_immune': '.fireImmune()' in chain})
        egg_id = identifier + '_spawn_egg'
        egg = re.search(rf'register\("{egg_id}",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);', source.items_text, re.S)
        if egg is None:
            raise ValueError(f'未找到刷怪蛋：{egg_id}')
        eggs.append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
        items.append({'id': egg_id, 'factory': 'creature:spawn_egg_' + identifier,
                      'order': item_order[MOD + ':' + egg_id], **parse_item_properties(egg[3], source.tabs)})
        overrides.append({'registry': 'item', 'target_id': MOD + ':' + egg_id,
                          'factory': 'creature:spawn_egg_' + identifier, 'owner': 'creature',
                          'reason': f'{name}来源颜色及创造栏刷怪蛋'})
        assets.append({'owner': 'creature', 'kind': 'item', 'id': MOD + ':' + egg_id})
        relative = f'models/item/{egg_id}.json'
        copy(f'assets/{MOD}/{relative}', resources / relative)
        loot = data_root / f'loot_tables/entities/{identifier}.json'
        if loot.is_file():
            missing = []
            converted = loot_converter.convert_loot(json.loads(loot.read_text(encoding='utf-8')), missing)
            if missing:
                raise ValueError(f'{identifier}掉落引用未登记：{missing}')
            generated_data[f'data/{MOD}/loot_table/entities/{identifier}.json'] = converted
        for language, names in source.lang.items():
            for key in (f'entity.{MOD}.{identifier}', f'item.{MOD}.{egg_id}'):
                lang[language][key] = names[key]
        ambient = next((method['source'] for method in definitions if method['name'] == 'getAmbientSound'), '')
        for sound_id in re.findall(r'new ResourceLocation\("' + MOD + r':([^\"]+)"\)', ambient):
            if sound_id not in sounds:
                raise ValueError(f'声音定义缺失：{sound_id}')
            used_sounds[sound_id] = sounds[sound_id]
            for sound in sounds[sound_id]['sounds']:
                filename = sound if isinstance(sound, str) else sound['name']
                namespace, filename = filename.split(':', 1)
                if namespace != MOD:
                    raise ValueError(f'未处理的外部声音：{namespace}:{filename}')
                copy(f'assets/{MOD}/sounds/{filename}.ogg', resources / f'sounds/{filename}.ogg')
            for language, names in source.lang.items():
                key = sounds[sound_id].get('subtitle', 'subtitles.' + sound_id)
                if key in names:
                    lang[language][key] = names[key]
        proof.append({'id': identifier, 'source': path.relative_to(source.root).as_posix(),
                      'sha256': hashlib.sha256(body.encode('utf-8')).hexdigest(),
                      'base_class': re.search(r'public class \w+ extends (\w+)', body)[1],
                      'methods': [method['name'] for method in definitions],
                      'spawn_placement_source': next((method['source'] for method in definitions if method['name'] == 'init'), ''),
                      'source_biome_events': [event for event in spawn_events if any(spawn['entity_id'] == MOD + ':' + identifier for spawn in event['spawns'])],
                      'loot_source': loot.relative_to(source.root).as_posix() if loot.is_file() else None})
    return {'source': str(source.root), 'ids': IDS,
            'outputs': {'content/creature/organism_models.json': models, 'content/creature/organism_eggs.json': eggs},
            'generated_data': generated_data, 'copies': copies, 'entity_rows_for_integration': entities,
            'item_rows_for_integration': items, 'factory_overrides_for_integration': overrides,
            'system_assets_for_integration': assets, 'lang_for_integration': lang,
            'sounds_for_integration': used_sounds, 'source_proof': proof,
            'integration': ['CreatureSetup: CONTENT.register前调用Organisms.register(CONTENT,modBus)。',
                            'Creature客户端装配: 调用OrganismClient.register(modBus)。',
                            '共享entity/items、factory_overrides、system_assets、creature语言和声音片段由主会话合并本报告对应行。',
                            '完成台账登记后convert_content --owner creature重建共享表；convert_assets按system_assets复制资源。',
                            'convert_source_biomes自动合并createBiome.addSpawn及生物BiomeLoadingEvent；09没有自然刷怪入口。'],
            'verification': {'static_source': '已提取来源注册、模型属性、出生规则、语言声音与掉落表；行为复核见族来源证据文档。',
                             'build': '未执行', 'client': '未执行', 'server': '未执行'}}


def write_resources(report: dict) -> None:
    target = ROOT / 'src/main/resources'
    for relative, value in {**report['outputs'], **report['generated_data']}.items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    for relative, entry in report['copies'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(Path(entry['source']).read_bytes())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/organism-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.write_resources:
        write_resources(report)
    print(json.dumps({'entities': len(IDS), 'copied_assets': len(report['copies']),
                      'loot_tables': len(report['generated_data']), 'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
