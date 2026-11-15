"""提取二踢脚注册、语言与资源接线计划；不修改共享转换表。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

from convert_content import ROOT, Source

MOD = 'yvanchuxiuzhen'


def build() -> dict:
    source = Source(MOD)
    match = re.search(r'register\("pao_0901",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
                      source.entities_text, re.S)
    size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', match[2])
    row = {'id': 'pao_0901', 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
           'tracking_range': int(re.search(r'setTrackingRange\((\d+)\)', match[2])[1]),
           'update_interval': int(re.search(r'setUpdateInterval\((\d+)\)', match[2])[1]),
           'fire_immune': '.fireImmune()' in match[2]}
    java = f'src/main/java/net/mcreator/{MOD}/'
    assets = f'src/main/resources/assets/{MOD}/'
    origins = [java + relative for relative in (
        'item/Pao09Item.java', 'entity/Pao0901Entity.java', 'client/model/Modelpao0901.java',
        'client/renderer/Pao0901Renderer.java', 'procedures/Pao09DangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure.java',
        'procedures/Pao0901DangYouJianDianJiShiTiProcedure.java', 'procedures/Pao0901DangShiTiGengXinKeShiProcedure.java',
        f'init/{source.prefix}ModEntities.java')]
    copies = {}
    for relative in ('textures/entities/pao0901.png', 'textures/items/pao09.png', 'models/item/pao_09.json'):
        origin = assets + relative
        if not (source.root / origin).is_file():
            raise FileNotFoundError(source.root / origin)
        copies[f'assets/{MOD}/{relative.replace("textures/items/", "textures/item/")}'] = str(source.root / origin)
        origins.append(origin)
    for relative in ('elements/Pao09.mod.json', 'elements/Pao0901.mod.json'):
        origins.append(relative)
    return {
        'source': str(source.root), 'ids': ['pao_09', 'pao_0901'],
        'entity_rows_for_integration': [row],
        'item_factory_rows_for_integration': [{'id': 'pao_09', 'factory': 'festival:double_bang'}],
        'lang_for_integration': {language: {key: names[key] for key in (
            f'item.{MOD}.pao_09', f'entity.{MOD}.pao_0901')} for language, names in source.lang.items()},
        'system_assets_for_integration': [['festival', 'texture', f'{MOD}:entities/pao0901'],
                                           ['festival', 'texture', f'{MOD}:items/pao09']],
        'copies': copies,
        'source_fingerprints': {relative: hashlib.sha256((source.root / relative).read_bytes()).hexdigest()
                                for relative in origins},
        'verification': {'static_source': '自然放置、两阶段计时、近邻传燃、伤害、模型与注册已核对',
                         'build': '未执行', 'client': '未执行', 'server': '未执行',
                         'source_game': '未执行', 'cross_version': '未执行'},
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/r15-double-bang-integration.json')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(f'double-bang integration plan: {len(report["ids"])} identities, {len(report["copies"])} resources')


if __name__ == '__main__':
    main()
