"""提取大红包、财神服及抛出烟花的独立接线计划，不修改公共转换登记。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from build_ledger import INVENTORY, ROOT
from convert_content import Source, parse_item_properties
from extract_equipment_armors import material_and_visual
from extract_entity_models import parse
from legacy_items import tooltip_lines

MOD = 'yvanchuxiuzhen'


def build() -> dict:
    source = Source(MOD)
    assets = source.root / f'src/main/resources/assets/{MOD}'
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    with (INVENTORY / 'registrations.csv').open(encoding='utf-8', newline='') as handle:
        registered = list(csv.DictReader(handle))
    order = {row['legacy_id']: number for number, row in enumerate(
        (row for row in registered if row['registry'] == 'item'), start=1)}
    material, multiplier, visual = material_and_visual(source, 'Fuqi02Item')
    initializer = source.java('init', f'{source.prefix}ModGeckoLibArmors')
    armor_rows = []
    for suffix, slot in (('helmet', 'HELMET'), ('chestplate', 'CHESTPLATE'), ('leggings', 'LEGGINGS'), ('boots', 'BOOTS')):
        item_id = f'fuqi_02_{suffix}'
        properties = re.search(rf'register\("{item_id}", \(\) -> new Fuqi02Item\(EquipmentSlot\.\w+, '
                               r'(new Item\.Properties\(\).*?)\)\);', initializer)[1]
        row = {'id': item_id, 'factory': f'festival:fortune_costume_{suffix}', 'order': order[f'{MOD}:{item_id}'],
               'armor': {'material': material['id'], 'slot': slot}, 'durability_multiplier': multiplier,
               **parse_item_properties(properties, source.tabs)}
        if suffix == 'helmet':
            row['tooltip'] = tooltip_lines(source.java('item', 'Fuqi02Item'))
        armor_rows.append(row)
    item_factories = [{'id': 'fuqi_05', 'factory': 'festival:costume_gift'},
                      {'id': 'fuqi_06', 'factory': 'festival:red_packet'}]
    copies = {}
    system_assets = []
    for path in (visual['geometry'], visual['animation'], visual['texture'].replace('textures/item/', 'textures/items/')):
        target = path.replace('textures/items/', 'textures/item/')
        copies[f'assets/{MOD}/{target}'] = str(assets / path)
    for row in armor_rows:
        item_id = row['id']
        model_path = assets / f'models/item/{item_id}.json'
        copies[f'assets/{MOD}/models/item/{item_id}.json'] = str(model_path)
        model = json.loads(model_path.read_text(encoding='utf-8'))
        for texture in model.get('textures', {}).values():
            namespace, relative = texture.split(':', 1)
            copies[f'assets/{MOD}/textures/{relative.replace("items/", "item/", 1)}.png'] = str(
                assets / f'textures/{relative}.png')
            system_assets.append(['festival', 'texture', texture])
    system_assets.extend([
        ['festival', 'geo', f'{MOD}:fuqi0201'],
        ['festival', 'animation', f'{MOD}:fuqi0201'],
        ['festival', 'texture', f'{MOD}:items/fuqi0206'],
    ])
    origins = ['item/Fuqi02Item.java', 'item/Fuqi05Item.java', 'item/Fuqi06Item.java',
               'item/model/Fuqi02Model.java', 'client/renderer/Fuqi02ArmorRenderer.java',
               'procedures/Fuqi02TouKuiShiJianMeiYouXiKeProcedure.java',
               'procedures/Fuqi05DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java',
               'procedures/Fuqi06DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java']
    models, entities = [], []
    for number in (4, 5, 6):
        name = f'Pao{number:02d}01'
        entity_id = f'pao_{number:02d}01'
        item_id = f'pao_{number:02d}'
        model = {'id': entity_id, **parse(name)}
        models.append(model)
        match = re.search(rf'register\("{entity_id}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
                          source.entities_text, re.S)
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', match[2])
        entities.append({'id': entity_id, 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
                         'tracking_range': int(re.search(r'setTrackingRange\((\d+)\)', match[2])[1]),
                         'update_interval': int(re.search(r'setUpdateInterval\((\d+)\)', match[2])[1]),
                         'fire_immune': '.fireImmune()' in match[2]})
        item_factories.append({'id': item_id, 'factory': f'festival:thrown_pao_{number:02d}'})
        for relative, kind, reference in (
            (f'geo/{model["geo"]}.geo.json', 'geo', model['geo']),
            (f'animations/{model["animation"]}.animation.json', 'animation', model['animation']),
            (f'textures/entities/{model["texture"]}.png', 'texture', 'entities/' + model['texture']),
        ):
            copies[f'assets/{MOD}/{relative}'] = str(assets / relative)
            system_assets.append(['festival', kind, f'{MOD}:{reference}'])
        origins.extend([f'item/Pao{number:02d}Item.java', f'entity/{name}Entity.java',
                        f'entity/model/{name}Model.java', f'client/renderer/{name}Renderer.java',
                        f'procedures/{name}DangShiTiGengXinKeShiProcedure.java'])
    origins.extend(['procedures/Pao04DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java',
                    f'init/{source.prefix}ModGeckoLibArmors.java', f'init/{source.prefix}ModEntities.java'])
    ids = {row['id'] for row in armor_rows} | {row['id'] for row in item_factories}
    languages = {}
    for language, names in source.lang.items():
        entries = {f'item.{MOD}.{item_id}': names[f'item.{MOD}.{item_id}'] for item_id in ids}
        entries.update({f'entity.{MOD}.{row["id"]}': names[f'entity.{MOD}.{row["id"]}'] for row in entities})
        for row in armor_rows:
            for index, line in enumerate(row.get('tooltip', []), start=1):
                entries[f'tooltip.{MOD}.item.{row["id"]}.{index}'] = line
        languages[language] = entries
    for origin in copies.values():
        if not Path(origin).is_file():
            raise FileNotFoundError(origin)
    return {'source': str(source.root), 'ids': sorted(ids), 'armor_rows_for_integration': armor_rows,
            'armor_material_rows_for_integration': [material], 'armor_visual': visual,
            'entity_rows_for_integration': entities,
            'outputs': {'content/festival/thrown_firework_models.json': models},
            'item_factory_rows_for_integration': item_factories, 'copies': copies,
            'lang_for_integration': languages, 'system_assets_for_integration': system_assets,
            'source_fingerprints': {f'src/main/java/net/mcreator/{MOD}/{relative}':
                                    hashlib.sha256((java / relative).read_bytes()).hexdigest() for relative in origins},
            'verification': {'static_source': '红包58档、礼包四部位、胸甲独立计时、三类抛出烟花及Geo表现已核对',
                             'build': '未执行', 'client': '未执行', 'server': '未执行',
                             'source_game': '未执行', 'cross_version': '未执行'}}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/r15-festival-integration.json')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(f'festival integration plan: {len(report["ids"])} item identities, {len(report["copies"])} resources')


if __name__ == '__main__':
    main()
