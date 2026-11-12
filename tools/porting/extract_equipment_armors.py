"""提取装备系统剩余的 57 件 GeckoLib 护甲及其来源资源。

范围为 Armor01–13（Armor10 只有胸甲）、Clothes05 和 Clothes19；已实现的妖化护甲与
festival 的 Fuqi02 不在此工具内。物品注册、材质、提示、模型路径、循环动画与
八个部位骨骼均读取锁定的 1.18.2 Java。更新装备内容表、语言片段、armor_models.json，
复制物品图标及几何、动画、纹理，并合并 factory_overrides、system_assets 与 manifest。
其他物品、材质、语言键和转换登记保留。后续 convert_content / convert_assets 可重建相同输出。
"""
from __future__ import annotations

import csv
import json
import re
import sys
from pathlib import Path

from build_ledger import INVENTORY, NAMESPACE, ROOT, read_ledgers
from convert_assets import MANIFEST, Converter
from convert_content import CONTENT, CONVERSION, LANG, LANGUAGES, Source, write_rows
from legacy_items import parse_armor_material, parse_item_properties, tooltip_lines

MOD = 'yvanchuxiuzhen'
CLASSES = tuple(f'Armor{number:02d}Item' for number in range(1, 14)) + ('Clothes05Item', 'Clothes19Item')
SLOTS = {'helmet': 'HELMET', 'chestplate': 'CHESTPLATE', 'leggings': 'LEGGINGS', 'boots': 'BOOTS'}
BONES = {'head': 'head', 'body': 'body', 'rightArm': 'right_arm', 'leftArm': 'left_arm',
         'rightLeg': 'right_leg', 'leftLeg': 'left_leg', 'rightBoot': 'right_boot', 'leftBoot': 'left_boot'}


def read_json(path: Path):
    return json.loads(path.read_text(encoding='utf-8'))


def read_csv(path: Path) -> tuple[list[str], list[dict[str, str]]]:
    with path.open(encoding='utf-8', newline='') as handle:
        reader = csv.DictReader(handle)
        rows = list(reader)
        return reader.fieldnames, rows


def write_csv(path: Path, columns: list[str], rows: list[dict[str, str]]) -> None:
    with path.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, lineterminator='\n')
        writer.writeheader()
        writer.writerows(rows)


def material_and_visual(source: Source, class_name: str) -> tuple[dict, int, dict]:
    """读取一族的材质、动画控制器与渲染器；资源中的动画和八个骨骼必须实际存在。"""
    name = class_name.removesuffix('Item')
    item = source.java('item', class_name)
    material = parse_armor_material(item, source.item_const)
    multiplier = material.pop('durability_multiplier')
    material.update(texture=material['id'], animated=True)
    model = source.java('item/model', f'{name}Model')
    resources = re.findall(r'new ResourceLocation\("yvanchuxiuzhen", "([^"]+)"\)', model)
    if len(resources) != 3:
        raise ValueError(f'{name} 的模型资源引用未识别')
    geometry, = [path for path in resources if path.startswith('geo/') and path.endswith('.geo.json')]
    animation, = [path for path in resources if path.startswith('animations/') and path.endswith('.animation.json')]
    texture, = [path for path in resources if path.startswith('textures/items/') and path.endswith('.png')]
    idle, = re.findall(r'addAnimation\("([^"]+)", EDefaultLoopTypes.LOOP\)', item)
    renderer = source.java('client/renderer', f'{name}ArmorRenderer')
    if 'return RenderType.entityTranslucent(' not in renderer:
        raise ValueError(f'{name} 的渲染层未识别')
    source_bones = dict(re.findall(r'this\.(\w+)Bone = "([^"]+)"', renderer))
    if source_bones.keys() != BONES.keys():
        raise ValueError(f'{name} 的八个部位骨骼未识别：{source_bones}')
    assets = source.root / f'src/main/resources/assets/{MOD}'
    if set(read_json(assets / animation)['animations']) != {idle}:
        raise ValueError(f'{name} 的来源动画不只包含已登记的循环：{idle}')
    actual_bones = {bone['name'] for bone in read_json(assets / geometry)['minecraft:geometry'][0]['bones']}
    if not set(source_bones.values()).issubset(actual_bones):
        raise ValueError(f'{name} 的渲染器引用了缺失骨骼')
    visual = {'id': material['id'], 'geometry': geometry, 'animation': animation,
              'texture': texture.replace('textures/items/', 'textures/item/', 1), 'idle': idle,
              'bones': {BONES[key]: value for key, value in source_bones.items()}}
    return material, multiplier, visual


def update_language(source: Source, items: list[dict], paths: dict[str, str]) -> None:
    ids = {row['id'] for row in items}
    for language in LANGUAGES:
        file = LANG / language / 'equipment.json'
        entries = read_json(file)
        name_keys = {f'item.{NAMESPACE}.{item_id}' for item_id in ids}
        tooltip_prefixes = tuple(f'tooltip.{NAMESPACE}.item.{item_id}.' for item_id in ids)
        entries = {key: value for key, value in entries.items()
                   if key not in name_keys and not key.startswith(tooltip_prefixes)}
        for row in items:
            name = source.lang[language].get(f'item.{MOD}.{paths[row["id"]]}')
            if name is not None:
                entries[f'item.{NAMESPACE}.{row["id"]}'] = name
            for number, line in enumerate(row.get('tooltip', []), start=1):
                entries[f'tooltip.{NAMESPACE}.item.{row["id"]}.{number}'] = line
        file.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def update_overrides(items: list[dict]) -> None:
    file = CONVERSION / 'factory_overrides.csv'
    columns, rows = read_csv(file)
    ids = {f'{NAMESPACE}:{row["id"]}' for row in items}
    rows = [row for row in rows if not (row['registry'] == 'item' and row['target_id'] in ids)]
    rows.extend({'registry': 'item', 'target_id': f'{NAMESPACE}:{row["id"]}', 'factory': row['factory'],
                 'owner': 'equipment', 'reason': '来源动画护甲的材质、部位、骨骼模型与循环动画（EquipmentGeoArmorItem）'}
                for row in items)
    write_csv(file, columns, rows)


def copy_assets(items: list[dict], paths: dict[str, str], visuals: list[dict]) -> int:
    converter = Converter()
    declared: set[tuple[str, str]] = set()
    for row in items:
        converter.item(MOD, paths[row['id']], row['id'], {})
    for visual in visuals:
        geometry = visual['geometry'].removeprefix('geo/').removesuffix('.geo.json')
        animation = visual['animation'].removeprefix('animations/').removesuffix('.animation.json')
        texture = visual['texture'].removeprefix('textures/').removesuffix('.png').replace('item/', 'items/', 1)
        for kind, name, folder, suffix in (('geo', geometry, 'geo', '.geo.json'),
                                           ('animation', animation, 'animations', '.animation.json')):
            reference = f'{MOD}:{name}'
            converter.copy_plain(reference, folder, suffix, MOD)
            declared.add((kind, reference))
        reference = f'{MOD}:{texture}'
        converter.copy_texture(reference, MOD)
        declared.add(('texture', reference))
    if converter.missing:
        raise ValueError(f'动画护甲来源资源缺失：{converter.missing}')
    file = CONVERSION / 'system_assets.csv'
    columns, rows = read_csv(file)
    existing = {(row['owner'], row['kind'], row['id']) for row in rows}
    rows.extend({'owner': 'equipment', 'kind': kind, 'id': reference}
                for kind, reference in sorted(declared) if ('equipment', kind, reference) not in existing)
    write_csv(file, columns, rows)
    columns, manifest = read_csv(MANIFEST)
    targets = {row[2] for row in converter.manifest}
    manifest = [row for row in manifest if row['target'] not in targets]
    manifest.extend(dict(zip(columns, row)) for row in converter.manifest)
    write_csv(MANIFEST, columns, sorted(manifest, key=lambda row: row['target']))
    return len(converter.manifest)


def main() -> None:
    source = Source(MOD)
    registrations = {path: cls for path, cls in source.geckolib_armor.items() if cls in CLASSES}
    expected = {f'armor_{number:02d}_{slot}' for number in range(1, 14) for slot in SLOTS
                if number != 10 or slot == 'chestplate'}
    expected.update(f'{material}_{slot}' for material in ('clothes_05', 'clothes_19') for slot in SLOTS)
    if set(registrations) != expected or len(registrations) != 57:
        raise ValueError(f'57 件动画护甲注册范围不符：{sorted(set(registrations) ^ expected)}')
    _, inventory = read_csv(INVENTORY / 'registrations.csv')
    order = {row['legacy_id']: number for number, row in enumerate(
        (row for row in inventory if row['registry'] == 'item'), start=1)}
    ledger = {(row['registry'], row['legacy_id']): row for rows in read_ledgers().values() for row in rows}
    sets = {cls: material_and_visual(source, cls) for cls in CLASSES}
    initializer = source.java('init', f'{source.prefix}ModGeckoLibArmors')
    items, paths = [], {}
    for path, class_name in registrations.items():
        legacy_id = f'{MOD}:{path}'
        mapped = ledger[('item', legacy_id)]
        if mapped['owner'] != 'equipment':
            raise ValueError(f'{legacy_id} 不属于装备系统')
        target = mapped['target_id'].removeprefix(f'{NAMESPACE}:')
        material, multiplier, _ = sets[class_name]
        slot = SLOTS[path.rsplit('_', 1)[1]]
        properties = re.search(rf'register\("{path}", \(\) -> new {class_name}\(EquipmentSlot\.\w+, '
                               r'(new Item\.Properties\(\).*?)\)\);', initializer)[1]
        row = {'id': target, 'factory': f'equipment:{target}', 'order': order[legacy_id],
               'armor': {'material': material['id'], 'slot': slot}, 'durability_multiplier': multiplier,
               **parse_item_properties(properties, source.tabs)}
        tooltips = tooltip_lines(source.java('item', class_name))
        if tooltips:
            row['tooltip'] = tooltips
        items.append(row)
        paths[target] = path
    items.sort(key=lambda row: row['order'])
    ids = set(paths)
    file = CONTENT / 'equipment/items.json'
    combined = [row for row in read_json(file) if row['id'] not in ids] + items
    write_rows(file, sorted(combined, key=lambda row: row['order']))
    materials = [values[0] for values in sets.values()]
    material_ids = {row['id'] for row in materials}
    file = CONTENT / 'equipment/armor_materials.json'
    combined = [row for row in read_json(file) if row['id'] not in material_ids] + materials
    write_rows(file, sorted(combined, key=lambda row: row['id']))
    visuals = [values[2] for values in sets.values()]
    write_rows(CONTENT / 'equipment/armor_models.json', visuals)
    update_language(source, items, paths)
    update_overrides(items)
    file = CONVERSION / 'skipped.csv'
    columns, skipped = read_csv(file)
    migrated = {f'{MOD}:{path}' for path in paths.values()}
    write_csv(file, columns, [row for row in skipped
                             if not (row['registry'] == 'item' and row['legacy_id'] in migrated)])
    files = copy_assets(items, paths, visuals)
    print(f'动画护甲 {len(items)} 件、材质 {len(materials)} 族、表现 {len(visuals)} 组、资源 {files} 个')


if __name__ == '__main__':
    sys.exit(main())
