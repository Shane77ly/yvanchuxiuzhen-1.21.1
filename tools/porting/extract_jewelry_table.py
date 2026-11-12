"""提取饰品制作台的方块属性、二维方块物品、来源名称及界面文本。

朝向/形状由锁定的 ShipintaiBlock 读取；动画状态与方块实体由 equipment 专属类型承担。
只合并本批内容表和转换登记，不重写其他系统或此前专属提取器的输出。
"""
from __future__ import annotations

import csv
import json
import re

from build_ledger import INVENTORY, NAMESPACE, read_ledgers
from convert_assets import MANIFEST, Converter
from convert_content import CONTENT, CONVERSION, LANG, LANGUAGES, Source, write_rows
from extract_equipment_armors import read_csv, write_csv
from extract_jewelry_table_assets import main as register_assets, resources
from legacy_blocks import _method, _shape


def main() -> None:
    source = Source('yvanchuxiuzhen')
    identity = f'{NAMESPACE}:shipintai'
    for rows in read_ledgers().values():
        for row in rows:
            if row['legacy_id'] == identity and row['registry'] in ('block', 'item') and row['owner'] != 'equipment':
                raise ValueError(f'饰品台归属不一致：{row}')
    text = source.java('block', 'ShipintaiBlock')
    strength = re.search(r'\.strength\(([\d.]+)f?,\s*([\d.]+)f?\)', text)
    block = {
        'id': 'shipintai', 'factory': 'equipment:shipintai',
        'material': re.search(r'Properties\.of\(Material\.(\w+)\)', text)[1],
        'sound': re.search(r'\.sound\(SoundType\.(\w+)\)', text)[1],
        'strength': [float(strength[1]), float(strength[2])],
        'no_occlusion': '.noOcclusion()' in text,
        'redstone_conductor': False,
        'states': ['FACING'], 'placement': 'horizontal',
        'shape': _shape(_method(text, 'getShape')),
        'light_block': int(re.search(r'return (\d+);', _method(text, 'getLightBlock'))[1]),
        'skylight': 'always', 'rotate': True,
        'fallback_drop': {'item': 'self', 'count': 1},
    }
    block_path = CONTENT / 'equipment/blocks.json'
    blocks = json.loads(block_path.read_text(encoding='utf-8')) if block_path.exists() else []
    write_rows(block_path, [row for row in blocks if row['id'] != 'shipintai'] + [block])

    with (INVENTORY / 'registrations.csv').open(encoding='utf-8', newline='') as handle:
        items = [row for row in csv.DictReader(handle) if row['registry'] == 'item']
    order = next(index + 1 for index, row in enumerate(items) if row['legacy_id'] == identity)
    registration = re.search(r'SHIPINTAI = REGISTRY\.register\([^;]+;', source.items_text, re.S)[0]
    tab = re.search(r'\.tab\(YvanchuxiuzhenModTabs\.(\w+)\)', registration)[1]
    item = {'id': 'shipintai', 'factory': 'block_item', 'block': 'shipintai', 'order': order, 'tab': source.tabs[tab]}
    item_path = CONTENT / 'equipment/items.json'
    item_rows = json.loads(item_path.read_text(encoding='utf-8'))
    write_rows(item_path, sorted([row for row in item_rows if row['id'] != 'shipintai'] + [item], key=lambda row: row['order']))

    for language in LANGUAGES:
        path = LANG / language / 'equipment.json'
        entries = json.loads(path.read_text(encoding='utf-8'))
        for key, value in source.lang[language].items():
            if key == f'block.{NAMESPACE}.shipintai' or key.startswith(f'gui.{NAMESPACE}.gu_ishipintai.'):
                entries[key] = value
        path.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')

    path = CONVERSION / 'factory_overrides.csv'
    columns, rows = read_csv(path)
    rows = [row for row in rows if not (row['target_id'] == identity and row['registry'] in ('block', 'item'))]
    rows.extend([
        dict(zip(columns, ('block', identity, 'equipment:shipintai', 'equipment', '饰品台方块实体、四槽菜单与类别动画'))),
        dict(zip(columns, ('item', identity, 'block_item', 'equipment', '来源普通二维图标，复用原生方块物品'))),
    ])
    write_csv(path, columns, rows)
    path = CONVERSION / 'skipped.csv'
    columns, rows = read_csv(path)
    write_csv(path, columns, [row for row in rows if not (row['legacy_id'] == identity and row['registry'] in ('block', 'item'))])

    register_assets()
    converter = Converter()
    parents = converter.block(NAMESPACE, 'shipintai', 'shipintai', 'minecraft:cutout')
    converter.item(NAMESPACE, 'shipintai', 'shipintai', parents)
    for _, kind, name in resources():
        if kind == 'texture':
            converter.copy_texture(name, NAMESPACE)
        else:
            folder, suffix = {'geo': ('geo', '.geo.json'), 'animation': ('animations', '.animation.json')}[kind]
            converter.copy_plain(name, folder, suffix, NAMESPACE)
    columns, rows = read_csv(MANIFEST)
    by_target = {row['target']: row for row in rows}
    for row in converter.manifest:
        entry = dict(zip(columns, row))
        by_target[entry['target']] = entry
    write_csv(MANIFEST, columns, sorted(by_target.values(), key=lambda row: row['target']))
    print('jewelry_table blocks 1 items 1 assets', len(converter.manifest), 'missing', converter.missing,
          'dropped_parents', converter.dropped_parents)


if __name__ == '__main__':
    main()
