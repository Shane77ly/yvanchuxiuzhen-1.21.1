"""提取财神完整来源配置；默认输出隔离计划，--write-resources 写入专属模型配置与资源。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

from convert_content import ROOT, Source, parse_item_properties
from extract_entity_models import parse

MOD = 'yvanchuxiuzhen'
IDENTIFIER = 'caishenye'
NAME = 'Caishenye'


def build() -> dict:
    source = Source(MOD)
    assets = source.root / f'src/main/resources/assets/{MOD}'
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    model = {'id': IDENTIFIER, **parse(NAME)}
    body = (java / f'entity/{NAME}Entity.java').read_text(encoding='utf-8')
    natural = (java / 'procedures/CaishenyeZiRanShiTiShengChengTiaoJianProcedure.java').read_text(encoding='utf-8')
    if 'Calendar.getInstance().get(Calendar.MONTH) == 2' not in natural:
        raise ValueError('财神来源自然条件已变化，必须重新核对日历规则')
    match = re.search(r'register\("caishenye",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
                      source.entities_text, re.S)
    if match is None:
        raise ValueError('未找到财神实体注册')
    size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', match[2])
    tracking = re.search(r'setTrackingRange\((\d+)\)', match[2])
    interval = re.search(r'setUpdateInterval\((\d+)\)', match[2])
    entity = {'id': IDENTIFIER, 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
              'tracking_range': int(tracking[1]) if tracking else 5,
              'update_interval': int(interval[1]) if interval else 3, 'fire_immune': '.fireImmune()' in match[2]}
    egg = re.search(r'register\("caishenye_spawn_egg",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);',
                    source.items_text, re.S)
    if egg is None:
        raise ValueError('未找到财神刷怪蛋')
    if (int(egg[1]), int(egg[2])) != (-3407872, -13261):
        raise ValueError('财神刷怪蛋颜色发生变化，必须同步 Java 工厂')
    copies = {}
    for relative in (f'geo/{model["geo"]}.geo.json', f'animations/{model["animation"]}.animation.json',
                     f'textures/entities/{model["texture"]}.png', 'models/item/caishenye_spawn_egg.json'):
        origin = assets / relative
        if not origin.is_file():
            raise FileNotFoundError(origin)
        copies[f'assets/{MOD}/{relative}'] = str(origin)
    loot = source.root / f'src/main/resources/data/{MOD}/loot_tables/entities/{IDENTIFIER}.json'
    if loot.is_file():
        copies[f'data/{MOD}/loot_table/entities/{IDENTIFIER}.json'] = str(loot)
    languages = {language: {key: names[key] for key in (
        f'entity.{MOD}.{IDENTIFIER}', f'item.{MOD}.caishenye_spawn_egg')}
        for language, names in source.lang.items()}
    declared_biomes = sorted(set(re.findall(r'new ResourceLocation\("([^"]+)"\)', body.split('SPAWN_BIOMES =', 1)[1].split(';', 1)[0])))
    origins = [f'entity/{NAME}Entity.java', f'entity/model/{NAME}Model.java', f'client/renderer/{NAME}Renderer.java',
               'procedures/CaishenyeZiRanShiTiShengChengTiaoJianProcedure.java',
               'procedures/CaishenyeDangYouJianDianJiShiTiProcedure.java',
               'procedures/CaishenyeDangShiTiBeiShaSiShiProcedure.java']
    fingerprints = {str(Path('src/main/java/net/mcreator') / MOD / relative):
                    hashlib.sha256((java / relative).read_bytes()).hexdigest() for relative in origins}
    return {'source': str(source.root), 'ids': [IDENTIFIER],
            'outputs': {'content/festival/caishenye_models.json': [model]}, 'copies': copies,
            'entity_rows_for_integration': [entity],
            'item_factory_rows_for_integration': [{'id': 'caishenye_spawn_egg', 'factory': 'festival:caishenye_spawn_egg',
                                                  **parse_item_properties(egg[3], source.tabs)}],
            'lang_for_integration': languages,
            'system_assets_for_integration': [['festival', 'geo', f'{MOD}:{model["geo"]}'],
                                               ['festival', 'animation', f'{MOD}:{model["animation"]}'],
                                               ['festival', 'texture', f'{MOD}:entities/{model["texture"]}']],
            'source_fingerprints': fingerprints,
            'natural_spawn': {'calendar_month_zero_based': 2, 'declared_biomes': declared_biomes,
                              'category': 'creature', 'weight': 3, 'min_count': 1, 'max_count': 1,
                              'integration': '由World来源群系转换器保留来源事件追加；不要重复安装额外modifier。'},
            'verification': {'static_source': '实体及三过程、注册、刷怪蛋、表现资源已核对',
                             'build': '未执行', 'client': '未执行', 'server': '未执行',
                             'source_game': '未执行', 'cross_version': '未执行'}}


def write_resources(report: dict) -> None:
    """只写本族独立模型配置与来源资源；正式内容、语言和manifest仍走共享转换器。"""
    target = ROOT / 'src/main/resources'
    for relative, rows in report['outputs'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text('[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in rows) + '\n]\n', encoding='utf-8')
    for relative, origin in report['copies'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(Path(origin).read_bytes())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/caishenye-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    if args.write_resources:
        write_resources(report)
    print(json.dumps({'entities': 1, 'assets': len(report['copies']), 'declared_biomes': len(report['natural_spawn']['declared_biomes']),
                      'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
