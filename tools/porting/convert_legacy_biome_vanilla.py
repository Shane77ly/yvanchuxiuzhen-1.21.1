"""转换已确认存在算法差异的原版群系默认地物；配置取自锁定1.18.2注册。"""
from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

from catalog_world_biomes import calls
from convert_biome_inline_worldgen import InlineParser, invocation
from convert_content import ROOT, SOURCES
from legacy_fossils import plan as fossil_plan
from legacy_lakes import plan as lake_plan
from legacy_geodes import plan as geode_plan
from legacy_trees import plan as tree_plan

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}'
PLACED = {'minecraft:desert_well': MOD + ':legacy_vanilla/desert_well',
          'minecraft:amethyst_geode': MOD + ':legacy_vanilla/amethyst_geode',
          'minecraft:fossil_upper': MOD + ':legacy_vanilla/fossil_upper',
          'minecraft:fossil_lower': MOD + ':legacy_vanilla/fossil_lower',
          'minecraft:forest_rock': MOD + ':legacy_vanilla/forest_rock',
          'minecraft:bamboo': MOD + ':legacy_vanilla/bamboo',
          'minecraft:bamboo_vegetation': MOD + ':legacy_vanilla/bamboo_vegetation',
          'minecraft:rooted_azalea_tree': MOD + ':legacy_vanilla/rooted_azalea_tree',
          'minecraft:trees_jungle': MOD + ':legacy_vanilla/trees_jungle',
          'minecraft:trees_meadow': MOD + ':legacy_vanilla/trees_meadow',
          'minecraft:large_dripstone': MOD + ':legacy_vanilla/large_dripstone',
          'minecraft:lake_lava_surface': MOD + ':legacy_vanilla/lake_lava_surface',
          'minecraft:lake_lava_underground': MOD + ':legacy_vanilla/lake_lava_underground',
          'minecraft:lush_caves_vegetation': MOD + ':legacy_vanilla/lush_caves_vegetation',
          'minecraft:lush_caves_ceiling_vegetation': MOD + ':legacy_vanilla/lush_caves_ceiling_vegetation',
          'minecraft:lush_caves_clay': MOD + ':legacy_vanilla/lush_caves_clay'}


def registered(text: str, constant: str) -> dict:
    match = re.search(r'public static final [^;\n]*\b' + re.escape(constant) + r'\s*=\s*([^;]+);', text)
    if match is None:
        raise ValueError(f'来源原版注册不存在：{constant}')
    parsed = calls(match[1])
    if not parsed or parsed[0]['name'] not in ('FeatureUtils.register', 'PlacementUtils.register'):
        raise ValueError(f'来源原版注册类型变化：{constant}')
    return parsed[0]


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    source_root = source_root or SOURCES[MOD][0]
    sources = list((source_root / 'build/fg_cache').glob('net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
    if len(sources) != 1:
        raise ValueError('旧版来源Jar必须唯一')
    clients = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(clients) != 1:
        raise ValueError('旧版来源资源Jar必须唯一')
    parser = InlineParser(source_root)
    outputs = []
    with zipfile.ZipFile(sources[0]) as archive:
        configured = archive.read('net/minecraft/data/worldgen/features/MiscOverworldFeatures.java').decode('utf-8').replace('\r', '')
        vegetation = archive.read('net/minecraft/data/worldgen/features/VegetationFeatures.java').decode('utf-8').replace('\r', '')
        placed = archive.read('net/minecraft/data/worldgen/placement/MiscOverworldPlacements.java').decode('utf-8').replace('\r', '')
        vegetation_placed = archive.read('net/minecraft/data/worldgen/placement/VegetationPlacements.java').decode('utf-8').replace('\r', '')
        for constant, path, vanilla_type, legacy_type in (
                ('BAMBOO_SOME_PODZOL', 'bamboo', 'BAMBOO', 'legacy_bamboo'),
                ('DESERT_WELL', 'desert_well', 'DESERT_WELL', 'legacy_desert_well'),
                ('FOREST_ROCK', 'forest_rock', 'FOREST_ROCK', 'legacy_forest_rock')):
            config_args = registered(vegetation if constant == 'BAMBOO_SOME_PODZOL' else configured, constant)['arguments']
            registration_path = 'bamboo_some_podzol' if constant == 'BAMBOO_SOME_PODZOL' else path
            if json.loads(config_args[0]) != registration_path or config_args[1] != 'Feature.' + vanilla_type:
                raise ValueError(f'旧版配置注册发生变化：{constant}')
            if constant == 'BAMBOO_SOME_PODZOL':
                if len(config_args) != 3 or config_args[2].startswith('new ProbabilityFeatureConfiguration(') is False:
                    raise ValueError('来源竹子应使用ProbabilityFeatureConfiguration')
                probability, = invocation(config_args[2], 'new ProbabilityFeatureConfiguration')
                config = {'probability': float(probability.rstrip('fF'))}
            elif constant == 'DESERT_WELL':
                if len(config_args) != 2:
                    raise ValueError('来源水井应使用NoneFeatureConfiguration')
                config = {}
            else:
                if len(config_args) != 3:
                    raise ValueError('来源石团应使用BlockStateConfiguration')
                state, = invocation(config_args[2], 'new BlockStateConfiguration')
                config = {'state': parser.state(state)}
            target_id = MOD + ':legacy_vanilla/' + path
            outputs.append((DATA / f'worldgen/configured_feature/legacy_vanilla/{path}.json',
                {'type': MOD + ':' + legacy_type, 'config': config}, [(MOD, sources[0])]))
            placement_constant = 'BAMBOO' if constant == 'BAMBOO_SOME_PODZOL' else constant
            placement_args = registered(vegetation_placed if constant == 'BAMBOO_SOME_PODZOL' else placed, placement_constant)['arguments']
            placement_path = 'bamboo' if constant == 'BAMBOO_SOME_PODZOL' else path
            configured_holder = 'VegetationFeatures.BAMBOO_SOME_PODZOL' if constant == 'BAMBOO_SOME_PODZOL' else 'MiscOverworldFeatures.' + constant
            if json.loads(placement_args[0]) != placement_path or placement_args[1] != configured_holder:
                raise ValueError(f'旧版放置注册发生变化：{constant}')
            modifiers = [parser.modifier(value) for value in placement_args[2:]]
            outputs.append((DATA / f'worldgen/placed_feature/legacy_vanilla/{path}.json',
                {'feature': target_id, 'placement': modifiers}, [(MOD, sources[0])]))
    snapshot = json.loads((ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json').read_text(encoding='utf-8'))
    outputs.extend(fossil_plan(sources[0], clients[0], snapshot))
    outputs.extend(lake_plan(source_root))
    outputs.extend(geode_plan(source_root))
    outputs.extend(tree_plan(source_root))
    large_config = normalize_provider_codecs(snapshot['registries']['configured_feature']['minecraft:large_dripstone'])
    large_config['type'] = MOD + ':legacy_large_dripstone'
    outputs.append((DATA / 'worldgen/configured_feature/legacy_vanilla/large_dripstone.json',
        large_config, [(MOD, sources[0])]))
    large_placement = normalize_provider_codecs(snapshot['registries']['placed_feature']['minecraft:large_dripstone'])
    large_placement['feature'] = MOD + ':legacy_vanilla/large_dripstone'
    outputs.append((DATA / 'worldgen/placed_feature/legacy_vanilla/large_dripstone.json',
        large_placement, [(MOD, sources[0])]))
    target_data_path = ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar'
    configured_names = {'moss_patch': 'legacy_moss_patch',
        'moss_patch_ceiling': 'legacy_moss_patch_ceiling', 'lush_caves_clay': 'legacy_lush_caves_clay',
        'clay_with_dripleaves': 'legacy_clay_with_dripleaves',
        'clay_pool_with_dripleaves': 'legacy_clay_pool_with_dripleaves'}
    placed_names = {'lush_caves_vegetation': 'legacy_vanilla/lush_caves_vegetation',
        'lush_caves_ceiling_vegetation': 'legacy_vanilla/lush_caves_ceiling_vegetation',
        'lush_caves_clay': 'legacy_vanilla/lush_caves_clay'}
    with zipfile.ZipFile(target_data_path) as target_data:
        for name, legacy_name in configured_names.items():
            resource = f'data/minecraft/worldgen/configured_feature/{name}.json'
            data = json.loads(target_data.read(resource))
            rewrite_feature_references(data, configured_names)
            rewrite_legacy_replaceable(data)
            outputs.append((DATA / f'worldgen/configured_feature/{legacy_name}.json', data,
                [(MOD, sources[0]), (MOD, clients[0])]))
        for name, legacy_name in placed_names.items():
            resource = f'data/minecraft/worldgen/placed_feature/{name}.json'
            data = json.loads(target_data.read(resource))
            data['feature'] = MOD + ':' + configured_names[data['feature'].split(':', 1)[1]]
            outputs.append((DATA / f'worldgen/placed_feature/{legacy_name}.json', data,
                [(MOD, sources[0]), (MOD, clients[0])]))
    snapshot = json.loads((ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json').read_text(encoding='utf-8'))
    with zipfile.ZipFile(clients[0]) as client:
        for source_tag, target_tag in (('moss_replaceable', 'legacy_moss_replaceable'),
                ('lush_ground_replaceable', 'legacy_lush_ground_replaceable'),
                ('bamboo_plantable_on', 'legacy_bamboo_plantable_on')):
            values = expand_archive_tag(client, 'minecraft:' + source_tag)
            if source_tag != 'bamboo_plantable_on' and values != expand_snapshot_tag(snapshot, 'minecraft:' + source_tag):
                raise ValueError(f'锁定快照与来源资源标签不一致：{source_tag}')
            outputs.append((ROOT / f'src/main/resources/data/{MOD}/tags/block/{target_tag}.json',
                {'replace': True, 'values': values}, [(MOD, clients[0])]))
    return outputs


def rewrite_feature_references(value, names: dict[str, str]) -> None:
    if isinstance(value, dict):
        for key, child in value.items():
            if key == 'feature' and isinstance(child, str) and child.startswith('minecraft:'):
                source_name = child.split(':', 1)[1]
                if source_name in names:
                    value[key] = MOD + ':' + names[source_name]
                else:
                    rewrite_feature_references(child, names)
            else:
                rewrite_feature_references(child, names)
    elif isinstance(value, list):
        for child in value:
            rewrite_feature_references(child, names)


def rewrite_legacy_replaceable(value) -> None:
    if isinstance(value, dict):
        for key, child in value.items():
            if key == 'replaceable' and child == '#minecraft:moss_replaceable':
                value[key] = '#yvanchuxiuzhen:legacy_moss_replaceable'
            elif key == 'replaceable' and child == '#minecraft:lush_ground_replaceable':
                value[key] = '#yvanchuxiuzhen:legacy_lush_ground_replaceable'
            else:
                rewrite_legacy_replaceable(child)
    elif isinstance(value, list):
        for child in value:
            rewrite_legacy_replaceable(child)


def expand_snapshot_tag(snapshot: dict, identifier: str) -> list[str]:
    tags = snapshot['block_tags']
    values = []
    for entry in tags[identifier]['values']:
        if entry.startswith('#'):
            values.extend(expand_snapshot_tag(snapshot, entry.removeprefix('#')))
        else:
            values.append(entry)
    return sorted(set(values))


def expand_archive_tag(archive: zipfile.ZipFile, identifier: str) -> list[str]:
    namespace, name = identifier.split(':', 1)
    tag = json.loads(archive.read(f'data/{namespace}/tags/blocks/{name}.json'))
    values = []
    for entry in tag['values']:
        values.extend(expand_archive_tag(archive, entry.removeprefix('#')) if entry.startswith('#') else [entry])
    return sorted(set(values))


def normalize_provider_codecs(value):
    if isinstance(value, dict):
        result = {key: normalize_provider_codecs(child) for key, child in value.items()}
        if result.get('type') in {'minecraft:uniform', 'minecraft:biased_to_bottom',
                'minecraft:clamped_normal', 'minecraft:trapezoid'} and isinstance(result.get('value'), dict):
            provider = result.pop('value')
            if result.keys() & provider.keys():
                raise ValueError(f'来源数值提供器字段冲突：{value}')
            result.update(provider)
        return result
    if isinstance(value, list):
        return [normalize_provider_codecs(child) for child in value]
    return value


def generate(converter) -> int:
    outputs = plan()
    for target, data, origins in outputs:
        converter.write(target, data, origins)
    return len(outputs)
