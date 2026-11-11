"""生成保留 1.18.2 晶洞近似算法的配置、放置及精确方块标签。"""
from __future__ import annotations

import json
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES
from legacy_lakes import expand_archive_tag

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}'


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    source_root = source_root or SOURCES[MOD][0]
    source_jars = list((source_root / 'build/fg_cache').glob(
        'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
    clients = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(source_jars) != 1 or len(clients) != 1:
        raise ValueError('锁定来源原版源码与资源 Jar 必须唯一')
    snapshot = json.loads((ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json').read_text(encoding='utf-8'))
    configured = json.loads(json.dumps(snapshot['registries']['configured_feature']['minecraft:amethyst_geode']))
    if configured['type'] != 'minecraft:geode':
        raise ValueError('来源 amethyst_geode 注册类型发生变化')
    configured['type'] = MOD + ':legacy_geode'
    for name in ('outer_wall_distance', 'distribution_points', 'point_offset'):
        provider = configured['config'][name]
        if set(provider) != {'type', 'value'} or provider['type'] != 'minecraft:uniform':
            raise ValueError(f'来源晶洞整数提供器格式变化：{name}')
        configured['config'][name] = {'type': provider['type'], **provider['value']}
    outputs = []
    with zipfile.ZipFile(clients[0]) as client:
        for source_name, target_name, field, size in (
                ('features_cannot_replace', 'legacy_geode_cannot_replace', 'cannot_replace', 4),
                ('geode_invalid_blocks', 'legacy_geode_invalid_blocks', 'invalid_blocks', 6)):
            source_tag = 'minecraft:' + source_name
            if configured['config']['blocks'][field] != '#' + source_tag:
                raise ValueError(f'来源晶洞方块标签引用变化：{field}')
            values = expand_archive_tag(client, source_tag)
            if len(values) != size:
                raise ValueError(f'来源晶洞方块标签成员数变化：{source_name}')
            if values != sorted(snapshot['block_tags'][source_tag]['values']):
                raise ValueError(f'来源晶洞标签与锁定注册快照不一致：{source_name}')
            configured['config']['blocks'][field] = '#' + MOD + ':' + target_name
            outputs.append((DATA / f'tags/block/{target_name}.json',
                {'replace': True, 'values': values}, [(MOD, clients[0])]))
    placed = json.loads(json.dumps(snapshot['registries']['placed_feature']['minecraft:amethyst_geode']))
    if placed['feature'] != 'minecraft:amethyst_geode':
        raise ValueError('来源晶洞放置引用发生变化')
    placed['feature'] = MOD + ':legacy_vanilla/amethyst_geode'
    outputs.append((DATA / 'worldgen/configured_feature/legacy_vanilla/amethyst_geode.json', configured,
        [(MOD, source_jars[0]), (MOD, clients[0])]))
    outputs.append((DATA / 'worldgen/placed_feature/legacy_vanilla/amethyst_geode.json', placed,
        [(MOD, source_jars[0]), (MOD, clients[0])]))
    return outputs
