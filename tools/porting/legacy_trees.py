"""生成四组来源原版树的完整配置、放置和精确材料/标签闭包。"""
from __future__ import annotations

import copy
import hashlib
import json
import re
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES
from legacy_block_tags import entries
from legacy_lakes import expand_archive_tag

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}'
ROOTS = ('bamboo_vegetation', 'trees_jungle', 'trees_meadow', 'rooted_azalea_tree')
CONFIGURED = ('bamboo_vegetation', 'fancy_oak', 'jungle_bush', 'mega_jungle_tree',
              'patch_grass_jungle', 'jungle_tree', 'trees_jungle', 'fancy_oak_bees',
              'meadow_trees', 'super_birch_bees', 'azalea_tree', 'rooted_azalea_tree')
PLACED = ('bamboo_vegetation', 'fancy_oak_checked', 'jungle_bush', 'mega_jungle_tree_checked',
          'jungle_tree', 'trees_jungle', 'fancy_oak_bees', 'super_birch_bees',
          'trees_meadow', 'rooted_azalea_tree')
TRUNKS = ('straight_trunk_placer', 'fancy_trunk_placer', 'mega_jungle_trunk_placer', 'bending_trunk_placer')
FOLIAGE = {'blob_foliage_placer': 'blob_foliage_placer', 'fancy_foliage_placer': 'fancy_foliage_placer',
           'bush_foliage_placer': 'bush_foliage_placer', 'jungle_foliage_placer': 'mega_jungle_foliage_placer',
           'random_spread_foliage_placer': 'random_spread_foliage_placer'}
TAGS = {'minecraft:leaves': 'legacy_tree_leaves', 'minecraft:logs': 'legacy_tree_logs',
        'minecraft:dirt': 'legacy_tree_dirt', 'minecraft:replaceable_plants': 'legacy_tree_replaceable_plant',
        'minecraft:azalea_grows_on': 'legacy_tree_azalea_grows_on',
        'minecraft:azalea_root_replaceable': 'legacy_tree_azalea_root_replaceable'}
SOILS = ('grassblock', 'loess', 'rottingsoil', 'scorchedsoil', 'youbosoil')


def identifier(name: str) -> str:
    return MOD + ':legacy_vanilla/' + name


def normalize(value):
    if isinstance(value, list):
        return [normalize(child) for child in value]
    if not isinstance(value, dict):
        return value
    result = {key: normalize(child) for key, child in value.items()}
    kind = result.get('type')
    if kind in {'minecraft:uniform', 'minecraft:biased_to_bottom', 'minecraft:clamped_normal',
                'minecraft:trapezoid'} and isinstance(result.get('value'), dict):
        result.update(result.pop('value'))
    if result.get('Name') == 'minecraft:grass':
        result['Name'] = 'minecraft:short_grass'
    if result.get('Name', '').endswith('_leaves'):
        result['Properties']['waterlogged'] = 'false'
    if kind == 'minecraft:tree':
        result['type'] = MOD + ':legacy_tree'
    elif kind == 'minecraft:root_system':
        result['type'] = MOD + ':legacy_root_system'
    elif kind and kind.removeprefix('minecraft:') in TRUNKS:
        result['type'] = MOD + ':legacy_' + kind.removeprefix('minecraft:')
    elif kind and kind.removeprefix('minecraft:') in FOLIAGE:
        result['type'] = MOD + ':legacy_' + FOLIAGE[kind.removeprefix('minecraft:')]
    elif kind == 'minecraft:leave_vine':
        result['type'] = MOD + ':legacy_leave_vine'
    elif kind == 'minecraft:simple_block':
        result['type'] = MOD + ':legacy_tree_groundcover'
    elif kind == 'minecraft:solid':
        result = {'type': 'minecraft:matching_block_tag', 'tag': MOD + ':legacy_lake_solid',
                  **({'offset': result['offset']} if 'offset' in result else {})}
    elif kind == 'minecraft:would_survive':
        state = result['state']['Name']
        if state not in {'minecraft:oak_sapling', 'minecraft:birch_sapling', 'minecraft:jungle_sapling'}:
            raise ValueError(f'四组来源树出现未核对的存活谓词：{state}')
        result = {'type': 'minecraft:matching_block_tag', 'offset': [0, -1, 0],
                  'tag': MOD + ':legacy_tree_sapling_ground'}
    for key, child in list(result.items()):
        if key == 'tag' and isinstance(child, str) and child in TAGS:
            result[key] = MOD + ':' + TAGS[child]
        elif key == 'root_replaceable' and child == '#minecraft:azalea_root_replaceable':
            result[key] = '#' + MOD + ':legacy_tree_azalea_root_replaceable'
        elif isinstance(child, str) and child.startswith('minecraft:'):
            name = child.removeprefix('minecraft:')
            if key in {'feature', 'default'} and name in set(CONFIGURED) | set(PLACED):
                result[key] = identifier(name)
    return result


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    source_root = source_root or SOURCES[MOD][0]
    source_jars = list((source_root / 'build/fg_cache').glob(
        'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
    clients = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(source_jars) != 1 or len(clients) != 1:
        raise ValueError('锁定来源原版源码与资源 Jar 必须唯一')
    snapshot = json.loads((ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json').read_text(encoding='utf-8'))
    materials = json.loads((ROOT / 'tools/porting/data/legacy_block_materials.json').read_text(encoding='utf-8'))
    origins = [(MOD, source_jars[0]), (MOD, clients[0])]
    outputs = []
    tag_values = {}
    with zipfile.ZipFile(clients[0]) as client:
        for source_name, target_name in TAGS.items():
            if source_name == 'minecraft:replaceable_plants':
                values = sorted({row['target_id'] for row in materials['blocks']
                                 if row['material'] == 'REPLACEABLE_PLANT'})
                values = ['minecraft:short_grass' if value == 'minecraft:grass' else value for value in values]
            else:
                values = expand_archive_tag(client, source_name)
                for mod_source, _ in SOURCES.values():
                    extension = mod_source / f'src/main/resources/data/minecraft/tags/blocks/{source_name.split(":")[1]}.json'
                    if extension.is_file():
                        data = json.loads(extension.read_text(encoding='utf-8'))
                        if data['replace'] or any(not isinstance(value, str) or value.startswith('#') for value in data['values']):
                            raise ValueError(f'来源树标签扩展需要重新核对：{extension}')
                        values.extend(data['values'])
                values = sorted(set(values))
            tag_values[target_name] = values
            outputs.append((DATA / f'tags/block/{target_name}.json',
                            {'replace': True, 'values': entries(values)}, origins))
    ground = tag_values['legacy_tree_dirt'] + ['minecraft:farmland']
    for name in SOILS:
        source = source_root / f'src/main/java/net/mcreator/yvanchuxiuzhen/block/{name.capitalize()}Block.java'
        text = source.read_text(encoding='utf-8')
        if not re.search(r'boolean canSustainPlant\([^}]+\{\s*return true;\s*\}', text):
            raise ValueError(f'来源树苗支撑回调发生变化：{source}')
        ground.append(MOD + ':' + name)
    outputs.append((DATA / 'tags/block/legacy_tree_sapling_ground.json',
                    {'replace': True, 'values': entries(sorted(set(ground)))}, origins))
    for name in CONFIGURED:
        data = normalize(copy.deepcopy(snapshot['registries']['configured_feature']['minecraft:' + name]))
        outputs.append((DATA / f'worldgen/configured_feature/legacy_vanilla/{name}.json', data, origins))
    for name in PLACED:
        data = normalize(copy.deepcopy(snapshot['registries']['placed_feature']['minecraft:' + name]))
        outputs.append((DATA / f'worldgen/placed_feature/legacy_vanilla/{name}.json', data, origins))
    return outputs


def integration(source: zipfile.ZipFile, target: zipfile.ZipFile, source_root: Path) -> dict:
    """核对来源/目标源码锁定、全部生成资源和 Java 接线；不替代实机生成验收。"""
    fingerprints = json.loads((ROOT / 'tools/porting/data/legacy_tree_source_fingerprints.json').read_text(encoding='utf-8'))
    if len(fingerprints) != 31 or len({row['name'] for row in fingerprints}) != 31:
        raise ValueError('来源树生成及辅助链指纹范围发生变化')
    for row in fingerprints:
        if (hashlib.sha256(source.read(row['source_entry'])).hexdigest() != row['source_sha256']
                or hashlib.sha256(target.read(row['target_entry'])).hexdigest() != row['target_sha256']):
            raise ValueError(f'来源树辅助链源码与静态核对版本不符：{row["name"]}')
    outputs = plan(source_root)
    if len(outputs) != 29:
        raise ValueError('四组来源树的配置、放置或标签输出范围发生变化')
    for path, expected, _ in outputs:
        if not path.is_file() or json.loads(path.read_text(encoding='utf-8')) != expected:
            raise ValueError(f'来源树资源缺失或与锁定计划不符：{path}')
    java = ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world'
    registration = (java / 'LegacyBiomeFeatures.java').read_text(encoding='utf-8')
    components = (java / 'SourceTreeComponents.java').read_text(encoding='utf-8')
    tree = (java / 'SourceTreeFeature.java').read_text(encoding='utf-8')
    trunk = (java / 'SourceTrunkPlacer.java').read_text(encoding='utf-8')
    foliage = (java / 'SourceFoliagePlacer.java').read_text(encoding='utf-8')
    vine = (java / 'SourceLeaveVineDecorator.java').read_text(encoding='utf-8')
    root = (java / 'SourceRootSystemFeature.java').read_text(encoding='utf-8')
    setup = (java / 'WorldSetup.java').read_text(encoding='utf-8')
    checks = {
        'twenty_nine_outputs_match_locked_plan': True,
        'thirty_one_source_target_fingerprints_match': True,
        'three_feature_types_registered': all(f'FEATURES.register("{name}", {implementation}::new);' in registration
            for name, implementation in [('legacy_tree', 'SourceTreeFeature'),
                ('legacy_root_system', 'SourceRootSystemFeature'), ('legacy_tree_groundcover', 'SourceTreeGroundcoverFeature')]),
        'component_types_on_world_lifecycle': 'SourceTreeComponents.register(modBus);' in registration
            and 'LegacyBiomeFeatures.register(modBus)' in setup,
        'four_trunk_five_foliage_one_decorator_types': components.count('TRUNKS.register("') == 4
            and components.count('FOLIAGE.register("') == 5 and components.count('DECORATORS.register("') == 1,
        'source_tree_three_hashsets': all(f'Set<BlockPos> {name} = new HashSet<>();' in tree
            for name in ['trunks', 'leaves', 'decorations']),
        'source_six_layer_leaf_updates': 'layer < 6' in tree and 'state.hasProperty(BlockStateProperties.DISTANCE)' in tree,
        'source_trunk_soil_and_replacement': 'state.is(SourceTreeFeature.DIRT)' in trunk
            and 'SourceTreeFeature.validTreePos' in trunk and 'onTreeGrow' not in trunk,
        'source_leaf_state_without_waterlogging_rewrite': 'SourceTreeFeature.validTreePos' in foliage
            and 'configuration.foliageProvider.getState(random, position)' in foliage and 'WATERLOGGED' not in foliage,
        'source_integer_leaf_vine_rng': vine.count('context.random().nextInt(4) == 0') == 4,
        'source_root_material_solid': 'legacy_lake_solid' in root and '!level.getBlockState(blockpos).is(SOLID)' in root,
    }
    if not all(checks.values()):
        raise ValueError(f'来源树注册、辅助链或生成配置闭环核对失败：{checks}')
    helpers = ['SourceTreeFeature', 'SourceRootSystemFeature', 'SourceTreeGroundcoverFeature',
               'SourceTreeComponents', 'SourceTrunkPlacer', 'SourceFoliagePlacer', 'SourceLeaveVineDecorator',
               'SourceStraightTrunkPlacer', 'SourceFancyTrunkPlacer', 'SourceGiantTrunkPlacer',
               'SourceMegaJungleTrunkPlacer', 'SourceBendingTrunkPlacer', 'SourceBlobFoliagePlacer',
               'SourceFancyFoliagePlacer', 'SourceBushFoliagePlacer', 'SourceMegaJungleFoliagePlacer',
               'SourceRandomSpreadFoliagePlacer']
    expected_implementations = json.loads((ROOT / 'tools/porting/data/legacy_tree_implementation_fingerprints.json').read_text(encoding='utf-8'))
    actual_implementations = {name: hashlib.sha256((java / (name + '.java')).read_bytes()).hexdigest()
                              for name in helpers}
    if set(expected_implementations) != set(helpers) or expected_implementations != actual_implementations:
        raise ValueError('来源树独立实现与已静态核对的十七项源码指纹不符')
    checks['seventeen_pinned_implementation_hashes_match'] = True
    return {'integration_checks': checks, 'source_and_target_fingerprints': fingerprints,
            'implementation_sha256': actual_implementations,
            'helper_review': '四类树干与五类树冠保持来源形状、循环、随机调用顺序；共享土壤、叶片位置和距离更新使用来源规则。目标TrunkVine/Cocoa逐句行为一致，Beehive保持无种子Collections.shuffle及2至3只蜜蜂，只适配Occupant存储API。SourceRootSystem保留来源材料solid与全部循环。树苗和草蕨支撑集合含来源五类全植物支撑Mod土壤；logs含两来源Mod扩展，REPLACEABLE_PLANT含shicai_008plant_03。未执行两版本树木随机自动对照或实机生成对照。'}
