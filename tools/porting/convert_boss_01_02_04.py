"""提取三名Boss与飞斧实体的来源模型、渲染、刷怪蛋及注册数据。"""
from __future__ import annotations

import argparse
import csv
import json
from pathlib import Path
import re

from convert_content import ROOT, Source
from convert_yaoshou_019_036 import model_row

MOD = 'yvanchuxiuzhen'
ENTITIES = (
    ('Ycxzboss01', 'ycxzboss_01'),
    ('Ycxzboss02', 'ycxzboss_02'),
    ('Ycxzboss0201', 'ycxzboss_0201'),
    ('Ycxzboss04', 'ycxzboss_04'),
)
EGG_IDS = ('ycxzboss_01', 'ycxzboss_02', 'ycxzboss_04')


def required(pattern: str, text: str, label: str) -> re.Match[str]:
    match = re.search(pattern, text, re.S)
    if match is None:
        raise ValueError(f'missing source field: {label}')
    return match


def build() -> dict:
    source = Source(MOD)
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    assets_root = source.root / f'src/main/resources/assets/{MOD}'
    models, eggs, registrations, geometry, assets = [], [], [], [], []

    def add_asset(kind: str, path: str) -> None:
        row = {'owner': 'creature', 'kind': kind, 'id': f'{MOD}:{path}'}
        if row not in assets:
            assets.append(row)

    for name, identifier in ENTITIES:
        model = model_row(java, name, identifier)
        renderer = (java / f'client/renderer/{name}Renderer.java').read_text(encoding='utf-8')
        scales = required(r'stack\.scale\(([\d.]+)f,\s*([\d.]+)f,\s*([\d.]+)f\)', renderer,
                          f'{name} renderer scale')
        scale_values = tuple(float(scales[index]) for index in (1, 2, 3))
        if not (scale_values[0] == scale_values[1] == scale_values[2]):
            raise ValueError(f'{name}: non-uniform renderer scale cannot be represented: {scale_values}')
        model['rendering']['scale'] = scale_values[0]

        geo_path = assets_root / f'geo/{model["geo"]}.geo.json'
        animation_path = assets_root / f'animations/{model["animation"]}.animation.json'
        geo = json.loads(geo_path.read_text(encoding='utf-8'))['minecraft:geometry'][0]['description']
        json.loads(animation_path.read_text(encoding='utf-8'))
        geometry.append({
            'id': identifier,
            **{key: geo[key] for key in ('texture_width', 'texture_height', 'visible_bounds_width',
                                        'visible_bounds_height', 'visible_bounds_offset') if key in geo},
        })
        models.append(model)

        add_asset('geo', model['geo'])
        add_asset('animation', model['animation'])
        add_asset('texture', f'entities/{model["texture"]}')
        if 'glow' in model:
            add_asset('texture', f'entities/{model["glow"]}')

        registration = required(
            rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);',
            source.entities_text, f'{identifier} entity registration')
        chain = registration[2]
        size = required(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain, f'{identifier} size')
        tracking = required(r'\.setTrackingRange\((\d+)\)', chain, f'{identifier} tracking range')
        interval = required(r'\.setUpdateInterval\((\d+)\)', chain, f'{identifier} update interval')
        registrations.append({
            'id': identifier,
            'category': registration[1],
            'width': float(size[1]),
            'height': float(size[2]),
            'tracking_range': int(tracking[1]),
            'update_interval': int(interval[1]),
            'fire_immune': '.fireImmune()' in chain,
        })

    for identifier in EGG_IDS:
        egg_id = identifier + '_spawn_egg'
        egg = required(
            rf'register\("{egg_id}",\s*\(\)\s*->\s*new ForgeSpawnEggItem\(\w+\.\w+,\s*(-?\d+),\s*(-?\d+),',
            source.items_text, f'{egg_id} spawn egg colors')
        eggs.append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
        add_asset('item', egg_id)

    overrides = [
        {'registry': 'item', 'target_id': f'{MOD}:{identifier}_spawn_egg',
         'factory': f'creature:spawn_egg_{identifier}', 'owner': 'creature',
         'reason': f'{identifier} 来源刷怪蛋及对应实体类型'}
        for identifier in EGG_IDS
    ]
    language_keys = [f'entity.{MOD}.{identifier}' for _, identifier in ENTITIES]
    language_keys.extend(f'item.{MOD}.{identifier}_spawn_egg' for identifier in EGG_IDS)
    languages = {language: {key: names[key] for key in language_keys}
                 for language, names in source.lang.items()}
    return {'models': models, 'eggs': eggs, 'registrations': registrations, 'geometry': geometry,
            'languages': languages,
            'system_assets': assets, 'factory_overrides': overrides}


def merge_rows(path: Path, fields: list[str], rows: list[dict], key_fields: tuple[str, ...]) -> None:
    with path.open(encoding='utf-8', newline='') as handle:
        reader = csv.DictReader(handle)
        if reader.fieldnames != fields:
            raise ValueError(f'unexpected CSV columns in {path}: {reader.fieldnames}')
        existing = list(reader)
    positions = {tuple(row[field] for field in key_fields): index for index, row in enumerate(existing)}
    for row in rows:
        key = tuple(row[field] for field in key_fields)
        if key in positions:
            existing[positions[key]] = row
        else:
            positions[key] = len(existing)
            existing.append(row)
    with path.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.DictWriter(handle, fieldnames=fields, lineterminator='\n')
        writer.writeheader()
        writer.writerows(existing)


def write_resources(report: dict) -> None:
    content = ROOT / 'src/main/resources/content/creature'
    outputs = {
        content / 'boss_models.json': report['models'],
        content / 'boss_eggs.json': report['eggs'],
    }
    for path, value in outputs.items():
        path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')

    entities_path = content / 'entities.json'
    entities = {row['id']: row for row in json.loads(entities_path.read_text(encoding='utf-8'))}
    entities.update({row['id']: row for row in report['registrations']})
    entities_path.write_text('[\n' + ',\n'.join(json.dumps(entities[key], ensure_ascii=False, separators=(',', ':'))
                                               for key in sorted(entities)) + '\n]\n',
                             encoding='utf-8', newline='\n')
    for language, rows in report['languages'].items():
        path = ROOT / f'src/main/lang/{language}/creature.json'
        names = json.loads(path.read_text(encoding='utf-8'))
        names.update(rows)
        path.write_text(json.dumps(names, ensure_ascii=False, indent=2) + '\n',
                        encoding='utf-8', newline='\n')

    conversion = ROOT / 'docs/reference/conversion'
    merge_rows(conversion / 'system_assets.csv', ['owner', 'kind', 'id'], report['system_assets'], ('owner', 'kind', 'id'))
    merge_rows(conversion / 'factory_overrides.csv',
               ['registry', 'target_id', 'factory', 'owner', 'reason'], report['factory_overrides'], ('registry', 'target_id'))


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--write-resources', action='store_true')
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/boss-01-02-04-resources.json')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.write_resources:
        write_resources(report)
    print(json.dumps({'models': len(report['models']), 'eggs': len(report['eggs']),
                      'registrations': len(report['registrations']),
                      'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
