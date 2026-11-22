"""提取阳系三实体、模型资源和来源指纹；默认只输出计划，--install安装转换输入。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from build_ledger import ROOT
from convert_content import Source
from extract_entity_models import parse


def build() -> dict:
    sources = {mod: Source(mod) for mod in ('yvanchuxitong', 'yvanchuxiuzhen')}
    falling = {'id': 'zhenjineng_03', **parse('Zhenjineng03')}
    assets = [
        {'owner': 'zhenfa', 'kind': 'texture', 'id': 'yvanchuxitong:entities/zhenfashiti'},
        {'owner': 'zhenfa', 'kind': 'texture', 'id': 'yvanchuxitong:entities/zhenjineng01'},
        {'owner': 'zhenfa', 'kind': 'texture', 'id': 'yvanchuxiuzhen:entities/' + falling['texture']},
        {'owner': 'zhenfa', 'kind': 'geo', 'id': 'yvanchuxiuzhen:' + falling['geo']},
        {'owner': 'zhenfa', 'kind': 'animation', 'id': 'yvanchuxiuzhen:' + falling['animation']},
    ]
    files = {
        'yvanchuxitong': [
            'entity/ZhenfashitiEntity.java', 'entity/Zhenyang01Entity.java', 'item/Zhenyang01Item.java',
            'client/model/Modelzhenfashiti.java', 'client/model/Modelzhenjineng01.java',
            'client/renderer/ZhenfashitiRenderer.java', 'client/renderer/Zhenyang01Renderer.java',
            'init/YvanchuxitongModEntities.java', 'procedures/ZhenfashitiDangShiTiGengXinKeShiProcedure.java',
            'procedures/Zhenyang01TouZhiWuFeiXingTickProcedure.java', 'procedures/Zhenyang01DangTouZhiWuJiZhongShiTiShiProcedure.java',
            *[f'procedures/Zhenjineng{number:02d}Procedure.java' for number in range(1, 8)],
        ],
        'yvanchuxiuzhen': [
            'entity/Zhenjineng03Entity.java', 'entity/model/Zhenjineng03Model.java',
            'client/renderer/Zhenjineng03Renderer.java', 'init/YvanchuxiuzhenModEntities.java',
            'procedures/Zhenjineng03DangShiTiGengXinKeShiProcedure.java',
        ],
    }
    evidence = []
    for mod, relative_paths in files.items():
        for relative in relative_paths:
            path = sources[mod].root / f'src/main/java/net/mcreator/{mod}/{relative}'
            evidence.append({'source_mod': mod, 'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    b = sources['yvanchuxitong']
    effects = []
    for number in range(2, 8):
        text = b.java('procedures', f'Zhenjineng{number:02d}Procedure')
        effects.append(text[text.index('\t\tif (((world.getBlockEntity'):])
    if len(set(effects)) != 1:
        raise ValueError('阴与五行效果正文存在差异，需要逐项复核')
    entities = []
    for mod, entity_id in [('yvanchuxitong', 'projectile_zhenyang_01'), ('yvanchuxitong', 'zhenfashiti'), ('yvanchuxiuzhen', 'zhenjineng_03')]:
        match = re.search(rf'register\("{entity_id}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
                          sources[mod].entities_text, re.S)
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', match[2])
        entities.append({'id': entity_id, 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
                         'tracking_range': int(re.search(r'setTrackingRange\((\d+)\)', match[2])[1]),
                         'update_interval': int(re.search(r'setUpdateInterval\((\d+)\)', match[2])[1]), 'fire_immune': '.fireImmune()' in match[2]})
    for row in assets:
        mod, name = row['id'].split(':', 1)
        folder, suffix = {'texture': ('textures', '.png'), 'geo': ('geo', '.geo.json'), 'animation': ('animations', '.animation.json')}[row['kind']]
        path = sources[mod].root / f'src/main/resources/assets/{mod}/{folder}/{name}{suffix}'
        if not path.is_file():
            raise FileNotFoundError(path)
        evidence.append({'source_mod': mod, 'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()})
    return {'entity_models': [falling], 'entities': entities, 'system_assets': assets, 'source_evidence': evidence,
            'five_elements_equal_yin_effect_body': True,
            'factory_override': {'registry': 'item', 'target_id': 'yvanchuxiuzhen:zhenyang_01', 'factory': 'zhenfa:yang_arrow',
                                 'owner': 'zhenfa', 'reason': '阳一弹体物品保持按住使用/释放发射与来源箭行为'},
            'verification': {'static_source': '已核对', 'compile': '未执行', 'client': '未执行', 'server': '未执行',
                             'source_game': '未执行', 'cross_version': '未执行'}}


def install(report: dict) -> None:
    output = ROOT / 'src/main/resources/content/zhenfa/entity_models.json'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(report['entity_models'], ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    for filename, incoming in [('system_assets.csv', report['system_assets']), ('factory_overrides.csv', [report['factory_override']])]:
        path = ROOT / 'docs/reference/conversion' / filename
        with path.open(encoding='utf-8', newline='') as handle:
            reader = csv.DictReader(handle)
            fields, rows = reader.fieldnames, list(reader)
        keys = ('owner', 'kind', 'id') if filename == 'system_assets.csv' else ('registry', 'target_id')
        replacement = {tuple(row[key] for key in keys): row for row in incoming}
        rows = [row for row in rows if tuple(row[key] for key in keys) not in replacement]
        with path.open('w', encoding='utf-8', newline='') as handle:
            writer = csv.DictWriter(handle, fieldnames=fields, lineterminator='\n')
            writer.writeheader()
            writer.writerows([*rows, *incoming])


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/r15-formation-entities.json')
    parser.add_argument('--install', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.install:
        install(report)
    print(f'formation integration plan: {len(report["entities"])} entities, {len(report["system_assets"])} assets, {len(report["source_evidence"])} source fingerprints')


if __name__ == '__main__':
    main()
