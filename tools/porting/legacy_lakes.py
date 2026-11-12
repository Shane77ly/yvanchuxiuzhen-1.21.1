"""生成来源熔岩湖材料和替换规则覆盖资源。"""
from __future__ import annotations

import json
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES
from legacy_block_tags import entries

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}'


def expand_archive_tag(archive: zipfile.ZipFile, identifier: str) -> list[str]:
    namespace, name = identifier.split(':', 1)
    tag = json.loads(archive.read(f'data/{namespace}/tags/blocks/{name}.json'))
    values = []
    for entry in tag['values']:
        values.extend(expand_archive_tag(archive, entry.removeprefix('#')) if entry.startswith('#') else [entry])
    return sorted(set(values))


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    source_root = source_root or SOURCES[MOD][0]
    source_jars = list((source_root / 'build/fg_cache').glob(
        'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
    clients = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(source_jars) != 1 or len(clients) != 1:
        raise ValueError('锁定来源原版源码与资源Jar必须唯一')
    snapshot = json.loads((ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json').read_text(encoding='utf-8'))
    material_snapshot = json.loads((ROOT / 'tools/porting/data/legacy_block_materials.json').read_text(encoding='utf-8'))
    for flag in ('solid', 'liquid'):
        expected = sorted({row['target_id'] for row in material_snapshot['blocks'] if row[flag]})
        if material_snapshot['tag_values']['legacy_lake_' + flag] != expected:
            raise ValueError(f'来源材料标签与逐方块快照不一致：{flag}')
    for row in material_snapshot['blocks']:
        if any(row[flag] != material_snapshot['materials'][row['material']][flag] for flag in ('solid', 'liquid')):
            raise ValueError(f'方块材料位与锁定材料定义不一致：{row["legacy_id"]}')
    outputs = []
    for name in ('legacy_lake_solid', 'legacy_lake_liquid'):
        values = material_snapshot['tag_values'][name]
        outputs.append((DATA / f'tags/block/{name}.json',
            {'replace': False, 'values': entries(values)}, [(MOD, source_jars[0])]))
    for material in ('GRASS', 'DIRT', 'STONE', 'WOOD'):
        values = sorted({row['target_id'] for row in material_snapshot['blocks'] if row['material'] == material})
        outputs.append((DATA / f'tags/block/legacy_material_{material.lower()}.json',
            {'replace': False, 'values': entries(values)}, [(MOD, source_jars[0])]))
    with zipfile.ZipFile(clients[0]) as client:
        for source_name, target_name in (
                ('minecraft:features_cannot_replace', 'legacy_worldgen_features_cannot_replace'),
                ('minecraft:lava_pool_stone_cannot_replace', 'legacy_lake_barrier_cannot_replace')):
            values = expand_archive_tag(client, source_name)
            outputs.append((DATA / f'tags/block/{target_name}.json',
                {'replace': False, 'values': values}, [(MOD, clients[0])]))
    configured = json.loads(json.dumps(snapshot['registries']['configured_feature']['minecraft:lake_lava']))
    configured['type'] = MOD + ':legacy_lake'
    outputs.append((DATA / 'worldgen/configured_feature/legacy_vanilla/lake_lava.json', configured,
        [(MOD, source_jars[0]), (MOD, clients[0])]))
    for name in ('lake_lava_surface', 'lake_lava_underground'):
        placed = json.loads(json.dumps(snapshot['registries']['placed_feature']['minecraft:' + name]))
        placed['feature'] = MOD + ':legacy_vanilla/lake_lava'
        outputs.append((DATA / f'worldgen/placed_feature/legacy_vanilla/{name}.json', placed,
            [(MOD, source_jars[0]), (MOD, clients[0])]))
    return outputs
