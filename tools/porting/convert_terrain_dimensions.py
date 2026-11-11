"""维度迁移的精确样条结构和1.18.2离散滑移转换；完整维度出口尚未接入。"""
from __future__ import annotations

from copy import deepcopy
import json
import math
from pathlib import Path

from catalog_world_biomes import DIMENSIONS
from convert_content import NAMESPACE, ROOT, SOURCES

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{NAMESPACE}'


def convert_spline(source, coordinates: dict):
    """保持全部float节点值与导数，仅由调用者显式提供坐标DensityFunction。

    ridges在1.18.2表示对float weirdness逐步计算的peaksAndValleys，不能直接
    绑定1.21.1的原始ridges或使用double通用运算近似；调用者须提供精确坐标。
    """
    if isinstance(source, (float, int)) and not isinstance(source, bool):
        return source
    if not isinstance(source, dict) or set(source) != {'coordinate', 'points'}:
        raise ValueError(f'未识别的来源样条节点：{source}')
    name = source['coordinate']
    if name not in coordinates:
        raise ValueError(f'样条坐标没有显式目标映射：{name}')
    points = []
    previous = -math.inf
    for point in source['points']:
        if set(point) != {'location', 'derivative', 'value'}:
            raise ValueError(f'未识别的来源样条控制点：{point}')
        if point['location'] <= previous:
            raise ValueError('来源样条控制点未严格递增')
        previous = point['location']
        points.append({'location': point['location'], 'derivative': point['derivative'],
                       'value': convert_spline(point['value'], coordinates)})
    if not points:
        raise ValueError('来源样条没有控制点')
    return {'coordinate': deepcopy(coordinates[name]), 'points': points}


def binary(kind: str, left, right) -> dict:
    return {'type': 'minecraft:' + kind, 'argument1': left, 'argument2': right}


def source_lerp(weight: float, target: float, value):
    """保持Mth.clampedLerp的端点分支与target+t*(value-target)运算顺序。"""
    if weight <= 0:
        return target
    if weight >= 1:
        return deepcopy(value)
    return binary('add', target, binary('mul', weight, binary('add', -target, deepcopy(value))))


def cell_interval(cell: int, height: int) -> tuple[int, int]:
    """Java整数除法向零截断的单元区间，返回整数Y的半开区间。"""
    if cell < 0:
        return (cell - 1) * height + 1, cell * height + 1
    if cell > 0:
        return cell * height, (cell + 1) * height
    return 1 - height, height


def apply_slider(value, slider: dict, *, top: bool, min_cell: int, cells: int, cell_height: int):
    """将来源NoiseSlider展开为有限range_choice阶梯，覆盖合法世界高度的整数Y。

    目标连续y_clamped_gradient只在噪声单元边界与来源一致；来源先执行
    (int)blockY/cellHeight，因此这里保留负Y向零截断和每层的离散权重。
    """
    if set(slider) != {'target', 'size', 'offset'}:
        raise ValueError(f'未识别的来源NoiseSlider：{slider}')
    target, size, offset = slider['target'], slider['size'], slider['offset']
    if not isinstance(size, int) or size < 0 or not isinstance(offset, int):
        raise ValueError(f'来源NoiseSlider整型字段越界：{slider}')
    if size == 0:
        return deepcopy(value)
    # bottom低处为target，top低处为value；中间仅size-1个单元权重。
    zero_cell = min_cell + cells - offset if top else min_cell + offset
    full_cell = zero_cell - size if top else zero_cell + size
    boundary = cell_interval(max(zero_cell, full_cell), cell_height)[0]
    high = target if top else deepcopy(value)
    low = deepcopy(value) if top else target
    result = {'type': 'minecraft:range_choice', 'input': 'minecraft:y',
              'min_inclusive': boundary, 'max_exclusive': 1000000.0,
              'when_in_range': high, 'when_out_of_range': low}
    for index in range(1, size):
        cell = zero_cell - index if top else zero_cell + index
        minimum, maximum = cell_interval(cell, cell_height)
        result = {'type': 'minecraft:range_choice', 'input': 'minecraft:y',
                  'min_inclusive': minimum, 'max_exclusive': maximum,
                  'when_in_range': source_lerp(index / size, target, value), 'when_out_of_range': result}
    return result


def convert_slide(source_noise: dict, value):
    """按来源NoiseRouterData.applySlide先top再bottom，保留世界高度内整数Y行为。"""
    height = source_noise['size_vertical'] * 4
    min_cell = source_noise['min_y'] // height
    cells = source_noise['height'] // height
    top = apply_slider(value, source_noise['top_slide'], top=True, min_cell=min_cell,
                       cells=cells, cell_height=height)
    return apply_slider(top, source_noise['bottom_slide'], top=False, min_cell=min_cell,
                        cells=cells, cell_height=height)


def unary(kind: str, value) -> dict:
    return {'type': 'minecraft:' + kind, 'argument': value}


def blend_spline(spline: dict, source_blend) -> dict:
    # 来源DensityFunctions.lerp的三函数重载；保持缓存与两项相加的运算结构。
    alpha = unary('cache_once', {'type': 'minecraft:blend_alpha'})
    inverse = binary('add', binary('mul', alpha, -1.0), 1.0)
    lerp = binary('add', binary('mul', source_blend, inverse), binary('mul', spline, alpha))
    return unary('flat_cache', unary('cache_2d', lerp))


def terrain_graph(noise: dict, dimension: str) -> dict[str, dict]:
    """从真实来源样条生成专属地形图，保留已核对的原版气候与洞穴引用。"""
    if noise['size_horizontal'] != 1 or noise['size_vertical'] != 2:
        raise ValueError('当前精确BlendedNoise转换只覆盖已核对的4×8来源噪声单元')
    sampling = noise['sampling']
    if set(sampling) != {'xz_scale', 'y_scale', 'xz_factor', 'y_factor'}:
        raise ValueError(f'未知来源sampling字段：{sampling}')
    path = f'legacy/{dimension}'
    ref = lambda name: f'{NAMESPACE}:{path}/{name}'
    result = {}
    limits = {'offset': (-0.81, 2.5), 'factor': (0.0, 8.0), 'jaggedness': (0.0, 1.28)}
    if set(noise['terrain_shaper']) != set(limits):
        raise ValueError('来源terrain_shaper字段不匹配')
    for kind, (minimum, maximum) in limits.items():
        # LegacyTerrainSpline本身解码来源四种字符串坐标，执行完整float语义。
        raw = deepcopy(noise['terrain_shaper'][kind])
        convert_spline(raw, {name: name for name in ('continents', 'erosion', 'ridges', 'weirdness')})
        function = {'type': NAMESPACE + ':legacy_terrain_spline',
                    'continentalness': 'minecraft:overworld/continents', 'erosion': 'minecraft:overworld/erosion',
                    'weirdness': 'minecraft:overworld/ridges', 'spline': raw, 'kind': kind,
                    'min_value': minimum, 'max_value': maximum}
        result[f'{path}/{kind}'] = blend_spline(function, {'type': 'minecraft:blend_offset'} if kind == 'offset' else 10.0 if kind == 'factor' else 0.0)
    result[f'{path}/depth'] = binary('add', {'type': 'minecraft:y_clamped_gradient', 'from_y': -64,
                                          'to_y': 320, 'from_value': 1.5, 'to_value': -1.5}, ref('offset'))
    base_noise = {'type': NAMESPACE + ':legacy_blended_noise', 'cell_width': 4, 'cell_height': 8,
                  'source_y_scale': sampling['y_scale'],
                  'argument': {'type': 'minecraft:old_blended_noise', 'xz_scale': sampling['xz_scale'] / 4,
                               'y_scale': sampling['y_scale'] / 8, 'xz_factor': sampling['xz_factor'],
                               'y_factor': sampling['y_factor'], 'smear_scale_multiplier': 8.0}}
    jagged = {'type': 'minecraft:noise', 'noise': 'minecraft:jagged', 'xz_scale': 1500.0, 'y_scale': 0.0}
    jagged_depth = binary('add', ref('depth'), binary('mul', ref('jaggedness'), unary('half_negative', jagged)))
    gradient = binary('mul', 4.0, unary('quarter_negative', binary('mul', jagged_depth, ref('factor'))))
    result[f'{path}/sloped_cheese'] = binary('add', gradient, base_noise)
    return result


def rewrite_router(node, noise: dict, dimension: str):
    if isinstance(node, str):
        terrain = ('offset', 'factor', 'jaggedness', 'depth', 'sloped_cheese')
        for name in terrain:
            if node == 'minecraft:overworld/' + name:
                return f'{NAMESPACE}:legacy/{dimension}/{name}'
        return node
    if isinstance(node, list):
        return [rewrite_router(item, noise, dimension) for item in node]
    if isinstance(node, dict):
        if node.get('type') == 'minecraft:slide':
            if set(node) != {'type', 'argument'}:
                raise ValueError(f'未知来源slide字段：{node}')
            return convert_slide(noise, rewrite_router(node['argument'], noise, dimension))
        return {key: rewrite_router(value, noise, dimension) for key, value in node.items()}
    return node


def spawn_target() -> list[dict]:
    # 两版OverworldBiomeBuilder.spawnTarget相同；来源NoiseRouter创建时固定调用该方法。
    return [{'temperature': [-1.0, 1.0], 'humidity': [-1.0, 1.0], 'continentalness': [-0.11, 1.0],
             'erosion': [-1.0, 1.0], 'depth': 0.0, 'weirdness': weirdness, 'offset': 0.0}
            for weirdness in ([-1.0, -0.16], [0.16, 1.0])]


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    """生成完整维度计划供内存核对；正式写出由generate的群系依赖门禁控制。"""
    source_root = source_root or SOURCES[MOD][0]
    outputs = []
    for dimension in DIMENSIONS:
        source = source_root / f'src/main/resources/data/{MOD}/dimension/{dimension}.json'
        document = json.loads(source.read_text(encoding='utf-8'))
        generator = document['generator']
        settings = generator['settings']
        noise = settings['noise']
        for name, graph in terrain_graph(noise, dimension).items():
            outputs.append((DATA / f'worldgen/density_function/{name}.json', graph, [(MOD, source)]))
        target_settings = {key: deepcopy(settings[key]) for key in (
            'default_block', 'default_fluid', 'surface_rule', 'sea_level', 'disable_mob_generation',
            'aquifers_enabled', 'ore_veins_enabled', 'legacy_random_source')}
        target_settings['noise'] = {key: noise[key] for key in ('min_y', 'height', 'size_horizontal', 'size_vertical')}
        target_settings['noise_router'] = rewrite_router(settings['noise_router'], noise, dimension)
        target_settings['spawn_target'] = spawn_target()
        outputs.append((DATA / f'worldgen/noise_settings/{dimension}.json', target_settings, [(MOD, source)]))
        target_dimension = deepcopy(document)
        target_dimension['generator'] = {'type': NAMESPACE + ':legacy_dimension', 'seed': generator['seed'],
                                         'biome_source': deepcopy(generator['biome_source']),
                                         'settings': NAMESPACE + ':' + dimension}
        outputs.append((DATA / f'dimension/{dimension}.json', target_dimension, [(MOD, source)]))
        type_source = source_root / f'src/main/resources/data/{MOD}/dimension_type/{dimension}.json'
        dimension_type = json.loads(type_source.read_text(encoding='utf-8'))
        # 来源Monster要求block light=0且local brightness<=random.nextInt(8)。
        dimension_type['monster_spawn_block_light_limit'] = 0
        dimension_type['monster_spawn_light_level'] = {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7}
        outputs.append((DATA / f'dimension_type/{dimension}.json', dimension_type, [(MOD, type_source)]))
    targets = [target for target, _, _ in outputs]
    if len(targets) != len(set(targets)):
        raise ValueError('维度转换存在重名目标')
    return outputs


def require_biome_closure(outputs) -> None:
    """正式写入前核对全部群系及其中Mod实体与地物，不安装半套维度。"""
    missing = set()
    entities = set()
    for path in (ROOT / 'src/main/resources/content').glob('*/entities.json'):
        entities.update(NAMESPACE + ':' + row['id'] for row in json.loads(path.read_text(encoding='utf-8')))
    for target, document, _ in outputs:
        if target.parent.name != 'dimension':
            continue
        for entry in document['generator']['biome_source']['biomes']:
            namespace, name = entry['biome'].split(':', 1)
            if namespace != NAMESPACE:
                continue
            biome = DATA / f'worldgen/biome/{name}.json'
            if not biome.is_file():
                missing.add(entry['biome'])
                continue
            data = json.loads(biome.read_text(encoding='utf-8'))
            for spawns in data['spawners'].values():
                for spawn in spawns:
                    if spawn['type'].startswith(NAMESPACE + ':') and spawn['type'] not in entities:
                        missing.add(spawn['type'])
            for stage in data['features']:
                for feature in stage:
                    if isinstance(feature, str) and feature.startswith(NAMESPACE + ':'):
                        placed = DATA / ('worldgen/placed_feature/' + feature.split(':', 1)[1] + '.json')
                        if not placed.is_file():
                            missing.add(feature)
    if missing:
        raise ValueError('人间道/妖魂道群系依赖尚未闭合，拒绝写出正式维度：' + '、'.join(sorted(missing)))


def generate(converter) -> int:
    outputs = plan()
    require_biome_closure(outputs)
    for target, document, origins in outputs:
        converter.write(target, document, origins)
    return len(outputs)
