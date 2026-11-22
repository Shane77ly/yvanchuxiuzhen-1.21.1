"""转换画里乾坤维度、纯白群系与8×4来源噪声，复用精确地形种子和采样实现。"""
from __future__ import annotations

from copy import deepcopy
import json
import re

from catalog_world_biomes import external_features, external_spawns, registered_entities, target_entities
from convert_content import NAMESPACE, ROOT, SOURCES
from convert_source_biomes import CATEGORIES, STAGES, constant
from convert_terrain_dimensions import DATA, MOD, binary, require_biome_closure, rewrite_router, spawn_target, terrain_graph

DIMENSION = 'fabao_009weidu'
BIOME = 'tiandaochunbaikongjian'


def plan():
    root = SOURCES[MOD][0]
    source = root / f'src/main/resources/data/{MOD}/dimension/{DIMENSION}.json'
    document = json.loads(source.read_text(encoding='utf-8'))
    generator = document['generator']
    settings = generator['settings']
    noise = settings['noise']
    if noise['terrain_shaper'] != {'offset': 0, 'factor': 0, 'jaggedness': 0}:
        raise ValueError('画卷来源地形样条发生变化，需要重新核对末地密度链')
    graphs = terrain_graph(noise, DIMENSION)
    graph_path = f'legacy/{DIMENSION}'
    # 来源seedNoise将BASE_3D_NOISE替换为本维度的8×4格点采样；不能直接使用目标末地连续坐标噪声。
    base = graphs[f'{graph_path}/sloped_cheese']['argument2']
    graphs[f'{graph_path}/sloped_cheese'] = binary('add', {'type': 'minecraft:end_islands'}, base)
    outputs = [(DATA / f'worldgen/density_function/{graph_path}/{kind}.json', graphs[f'{graph_path}/{kind}'], [(MOD, source)])
               for kind in ('offset', 'depth', 'sloped_cheese')]

    def rewrite(node):
        if node == 'minecraft:end/sloped_cheese':
            return f'{NAMESPACE}:{graph_path}/sloped_cheese'
        if isinstance(node, list):
            return [rewrite(value) for value in node]
        if isinstance(node, dict):
            return {key: rewrite(value) for key, value in node.items()}
        return node

    target_settings = {key: deepcopy(settings[key]) for key in (
        'default_block', 'default_fluid', 'surface_rule', 'sea_level', 'disable_mob_generation',
        'aquifers_enabled', 'ore_veins_enabled', 'legacy_random_source')}
    target_settings['noise'] = {key: noise[key] for key in ('min_y', 'height', 'size_horizontal', 'size_vertical')}
    target_settings['noise_router'] = rewrite_router(rewrite(settings['noise_router']), noise, DIMENSION)
    target_settings['spawn_target'] = spawn_target()
    outputs.append((DATA / f'worldgen/noise_settings/{DIMENSION}.json', target_settings, [(MOD, source)]))
    target_dimension = deepcopy(document)
    target_dimension['generator'] = {'type': NAMESPACE + ':legacy_dimension', 'seed': generator['seed'],
                                     'biome_source': deepcopy(generator['biome_source']), 'settings': NAMESPACE + ':' + DIMENSION}
    outputs.append((DATA / f'dimension/{DIMENSION}.json', target_dimension, [(MOD, source)]))
    type_source = root / f'src/main/resources/data/{MOD}/dimension_type/{DIMENSION}.json'
    dimension_type = json.loads(type_source.read_text(encoding='utf-8'))
    dimension_type['monster_spawn_block_light_limit'] = 0
    dimension_type['monster_spawn_light_level'] = {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7}
    outputs.append((DATA / f'dimension_type/{DIMENSION}.json', dimension_type, [(MOD, type_source)]))
    biome_source = root / f'src/main/java/net/mcreator/{MOD}/world/biome/TiandaochunbaikongjianBiome.java'
    text = biome_source.read_text(encoding='utf-8')
    if re.search(r'(?:biomeGenerationSettings|mobSpawnInfo)\.(?!build\b)\w+\(', text):
        raise ValueError('画卷来源群系新增生成或出生调用，需要重新核对')
    colors = {'fogColor': 'fog_color', 'waterColor': 'water_color', 'waterFogColor': 'water_fog_color',
              'skyColor': 'sky_color', 'foliageColorOverride': 'foliage_color', 'grassColorOverride': 'grass_color'}
    biome = {'has_precipitation': True, 'temperature': float(re.search(r'\.temperature\(([-\d.]+)f\)', text)[1]),
             'downfall': float(re.search(r'\.downfall\(([-\d.]+)f\)', text)[1]),
             'effects': {target: int(re.search(rf'\.{method}\((-?\d+)\)', text)[1]) for method, target in colors.items()},
             'carvers': {}, 'features': [[] for _ in STAGES], 'spawners': {category: [] for category in CATEGORIES}, 'spawn_costs': {}}
    source_java = root / f'src/main/java/net/mcreator/{MOD}'
    features, feature_event = external_features(root, source_java, {f'{MOD}:{BIOME}'})
    spawns = external_spawns(root, source_java, {f'{MOD}:{BIOME}'}, registered_entities(source_java), target_entities())
    origins = [(MOD, biome_source), (MOD, root / feature_event['source']['path'])]
    for feature in features:
        if feature['unresolved_expressions'] or not feature['target_placed_feature_exists']:
            raise ValueError(f'画卷群系全局地物尚未闭合：{feature["id"]}')
        biome['features'][STAGES.index(feature['stage'])].append(feature['id'])
        origins.append((MOD, root / feature['source']['path']))
    for event in spawns:
        if 'unresolved_expression' in event:
            raise ValueError(f'画卷群系全局出生条件未识别：{event["source"]["path"]}')
        origins.append((MOD, root / event['source']['path']))
        for spawn in event['spawns']:
            if not spawn['target_mod_entity_registered']:
                raise ValueError(f'画卷群系全局出生尚未注册：{spawn["entity_id"]}')
            category = constant(spawn['category'], 'MobCategory.', tuple(value.upper() for value in CATEGORIES)).lower()
            biome['spawners'][category].append({'type': spawn['entity_id'], 'weight': spawn['weight'],
                                               'minCount': spawn['min_count'], 'maxCount': spawn['max_count']})
    outputs.append((DATA / f'worldgen/biome/{BIOME}.json', biome, origins))
    return outputs


def generate(converter):
    outputs = plan()
    for target, document, origins in outputs:
        if target.parent.name == 'biome':
            converter.write(target, document, origins)
    require_biome_closure(outputs)
    for target, document, origins in outputs:
        if target.parent.name != 'biome':
            converter.write(target, document, origins)
    root = SOURCES[MOD][0]
    for language in ('zh_cn', 'en_us'):
        source = json.loads((root / f'src/main/resources/assets/{MOD}/lang/{language}.json').read_text(encoding='utf-8'))
        target = ROOT / f'src/main/lang/{language}/world.json'
        translated = json.loads(target.read_text(encoding='utf-8'))
        key = f'biome.{NAMESPACE}.{BIOME}'
        translated[key] = source[key]
        target.write_text(json.dumps(translated, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return len(outputs)
