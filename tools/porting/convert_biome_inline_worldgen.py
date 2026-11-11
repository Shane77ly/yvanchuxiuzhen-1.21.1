"""转换两个来源维度实际引用群系中的内联树、圆盘与放置配置。"""
from __future__ import annotations

import json
import re
import zipfile
from pathlib import Path

from catalog_world_biomes import DIMENSIONS, arguments, calls, matching
from convert_content import NAMESPACE, ROOT, SOURCES

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{NAMESPACE}/worldgen'
VANILLA_FEATURES = {
    'AquaticFeatures.SEAGRASS_SHORT': 'seagrass_short',
    'VegetationFeatures.FLOWER_DEFAULT': 'flower_default',
    'VegetationFeatures.MUSHROOM_ISLAND_VEGETATION': 'mushroom_island_vegetation',
    'VegetationFeatures.PATCH_BROWN_MUSHROOM': 'patch_brown_mushroom',
    'VegetationFeatures.PATCH_GRASS': 'patch_grass',
    'VegetationFeatures.PATCH_RED_MUSHROOM': 'patch_red_mushroom',
    'VegetationFeatures.PATCH_SUGAR_CANE': 'patch_sugar_cane',
}
HEIGHTMAPS = {'HEIGHTMAP': 'MOTION_BLOCKING', 'HEIGHTMAP_TOP_SOLID': 'OCEAN_FLOOR_WG',
              'HEIGHTMAP_WORLD_SURFACE': 'WORLD_SURFACE_WG', 'HEIGHTMAP_OCEAN_FLOOR': 'OCEAN_FLOOR'}


def invocation(expression: str, name: str) -> list[str]:
    """仅接受完整指定调用，不允许丢弃后缀表达式。"""
    expression = expression.strip()
    match = re.match(re.escape(name) + r'\s*\(', expression)
    if match is None:
        raise ValueError(f'期待{name}调用，实际：{expression}')
    opening = expression.index('(')
    if matching(expression, opening) != len(expression) - 1:
        raise ValueError(f'调用包含未处理后缀：{expression}')
    return arguments(expression[opening + 1:-1])


def integer(expression: str) -> int:
    if re.fullmatch(r'-?\d+', expression.strip()) is None:
        raise ValueError(f'期待整数：{expression}')
    return int(expression)


def number(expression: str) -> float:
    if re.fullmatch(r'-?\d+(?:\.\d+)?[DdFf]?', expression.strip()) is None:
        raise ValueError(f'期待数值：{expression}')
    return float(expression.rstrip('DdFf'))


def int_provider(expression: str):
    if expression.startswith('ConstantInt.of('):
        value, = invocation(expression, 'ConstantInt.of')
        return integer(value)
    minimum, maximum = invocation(expression, 'UniformInt.of')
    return {'type': 'minecraft:uniform', 'min_inclusive': integer(minimum), 'max_inclusive': integer(maximum)}


def selected_biomes(source_root: Path) -> list[Path]:
    """沿维度biome_source和注册声明确定范围，不按类名推导归属。"""
    java = source_root / f'src/main/java/net/mcreator/{MOD}'
    registrations = dict(re.findall(r'REGISTRY.register\("([^\"]+)", \(\) -> (\w+)\.createBiome\(\)\)',
                                   (java / 'init/YvanchuxiuzhenModBiomes.java').read_text(encoding='utf-8')))
    ids = set()
    for dimension in DIMENSIONS:
        source = source_root / f'src/main/resources/data/{MOD}/dimension/{dimension}.json'
        entries = json.loads(source.read_text(encoding='utf-8'))['generator']['biome_source']['biomes']
        ids.update(entry['biome'] for entry in entries)
    result = []
    for biome_id in sorted(ids):
        namespace, path = biome_id.split(':', 1)
        if namespace != MOD:
            raise ValueError(f'来源维度出现范围外群系：{biome_id}')
        result.append(java / f'world/biome/{registrations[path]}.java')
    return result


class InlineParser:
    """将已核对的来源Java构造精确展开为1.21.1 Codec字段。"""

    def __init__(self, source_root: Path):
        self.java = source_root / f'src/main/java/net/mcreator/{MOD}'
        text = (self.java / 'init/YvanchuxiuzhenModBlocks.java').read_text(encoding='utf-8')
        self.blocks = dict(re.findall(r'RegistryObject<Block> (\w+) = \w+\.register\("(\w+)"', text))
        source_jar = (source_root / 'build/fg_cache/net/minecraftforge/forge/'
                      '1.18.2-40.1.0_mapped_official_1.18.2/forge-1.18.2-40.1.0_mapped_official_1.18.2-sources.jar')
        with zipfile.ZipFile(source_jar) as jar:
            text = jar.read('net/minecraft/world/level/block/Blocks.java').decode('utf-8')
        self.vanilla = dict(re.findall(r'public static final Block (\w+) = register\("([^\"]+)"', text))
        self.decorator_origins: set[Path] = set()

    def state(self, expression: str) -> dict:
        match = re.fullmatch(r'(?:Blocks\.(\w+)|YvanchuxiuzhenModBlocks\.(\w+)\.get\(\))\.defaultBlockState\(\)', expression.strip())
        if match is None:
            raise ValueError(f'未支持的方块状态表达式：{expression}')
        block = 'minecraft:' + self.vanilla[match[1]] if match[1] else NAMESPACE + ':' + self.blocks[match[2]]
        return {'Name': block}

    def provider(self, expression: str) -> dict:
        state, = invocation(expression, 'BlockStateProvider.simple')
        return {'type': 'minecraft:simple_state_provider', 'state': self.state(state)}

    def decorator(self, expression: str) -> dict:
        if expression.startswith('new AlterGroundDecorator('):
            provider, = invocation(expression, 'new AlterGroundDecorator')
            return {'type': 'minecraft:alter_ground', 'provider': self.provider(provider)}
        match = re.fullmatch(r'(\w+)\.INSTANCE', expression)
        if match is None:
            raise ValueError(f'未支持的树修饰器：{expression}')
        path = self.java / f'world/features/treedecorators/{match[1]}.java'
        text = path.read_text(encoding='utf-8')
        ids = re.findall(r'\.setRegistryName\("([^\"]+)"\)', text)
        if len(ids) != 1 or 'Codec.unit(' not in text:
            raise ValueError(f'树修饰器不是单一无参注册：{path}')
        self.decorator_origins.add(path)
        return {'type': NAMESPACE + ':' + ids[0]}

    def tree(self, expression: str) -> dict:
        prefix = 'new TreeConfiguration.TreeConfigurationBuilder'
        if not expression.startswith(prefix + '('):
            raise ValueError(f'未支持的树构造：{expression}')
        opening = expression.index('(')
        end = matching(expression, opening)
        trunk, trunk_placer, foliage, foliage_placer, size = arguments(expression[opening + 1:end])
        result = {'trunk_provider': self.provider(trunk), 'trunk_placer': self.trunk(trunk_placer),
                  'foliage_provider': self.provider(foliage), 'foliage_placer': self.foliage(foliage_placer),
                  'dirt_provider': {'type': 'minecraft:simple_state_provider', 'state': {'Name': 'minecraft:dirt'}},
                  'minimum_size': self.size(size), 'decorators': [], 'ignore_vines': False, 'force_dirt': False}
        suffix = expression[end + 1:].strip()
        seen = set()
        while suffix:
            match = re.match(r'\.(\w+)\s*\(', suffix)
            if match is None:
                raise ValueError(f'未支持的树构造后缀：{suffix}')
            name = match[1]
            opening = suffix.index('(')
            end = matching(suffix, opening)
            args = arguments(suffix[opening + 1:end])
            suffix = suffix[end + 1:].strip()
            if name in seen:
                raise ValueError(f'重复树构造操作：{name}')
            seen.add(name)
            if name in ('ignoreVines', 'forceDirt') and not args:
                result['ignore_vines' if name == 'ignoreVines' else 'force_dirt'] = True
            elif name == 'decorators' and len(args) == 1:
                result['decorators'] = [self.decorator(item) for item in invocation(args[0], 'ImmutableList.of')]
            elif name == 'build' and not args and not suffix:
                return result
            else:
                raise ValueError(f'未支持的树构造操作：{name}({args})')
        raise ValueError('树构造缺少build()')

    @staticmethod
    def trunk(expression: str) -> dict:
        types = {'StraightTrunkPlacer': 'straight', 'ForkingTrunkPlacer': 'forking',
                 'GiantTrunkPlacer': 'giant', 'MegaJungleTrunkPlacer': 'mega_jungle'}
        match = re.match(r'new (\w+)\(', expression)
        if match is None or match[1] not in types:
            raise ValueError(f'未支持的树干：{expression}')
        base, rand_a, rand_b = invocation(expression, 'new ' + match[1])
        return {'type': 'minecraft:' + types[match[1]] + '_trunk_placer',
                'base_height': integer(base), 'height_rand_a': integer(rand_a), 'height_rand_b': integer(rand_b)}

    @staticmethod
    def foliage(expression: str) -> dict:
        types = {'BlobFoliagePlacer': 'blob', 'MegaJungleFoliagePlacer': 'jungle',
                 'AcaciaFoliagePlacer': 'acacia', 'MegaPineFoliagePlacer': 'mega_pine'}
        match = re.match(r'new (\w+)\(', expression)
        if match is None or match[1] not in types:
            raise ValueError(f'未支持的树冠：{expression}')
        args = invocation(expression, 'new ' + match[1])
        result = {'type': 'minecraft:' + types[match[1]] + '_foliage_placer',
                  'radius': int_provider(args[0]), 'offset': int_provider(args[1])}
        if match[1] == 'AcaciaFoliagePlacer':
            if len(args) != 2:
                raise ValueError(f'金合欢树冠参数数目不符：{expression}')
        elif match[1] == 'MegaPineFoliagePlacer':
            _, _, crown = args
            result['crown_height'] = int_provider(crown)
        else:
            _, _, height = args
            result['height'] = integer(height)
        return result

    @staticmethod
    def size(expression: str) -> dict:
        limit, lower, upper = invocation(expression, 'new TwoLayersFeatureSize')
        return {'type': 'minecraft:two_layers_feature_size', 'limit': integer(limit),
                'lower_size': integer(lower), 'upper_size': integer(upper)}

    def disk(self, expression: str) -> dict:
        state, radius, half_height, targets = invocation(expression, 'new DiskConfiguration')
        return {'state_provider': {'fallback': {'type': 'minecraft:simple_state_provider', 'state': self.state(state)}, 'rules': []},
                'target': {'type': 'minecraft:matching_blocks',
                           'blocks': [self.state(item)['Name'] for item in invocation(targets, 'List.of')]},
                'radius': int_provider(radius), 'half_height': integer(half_height)}

    def modifier(self, expression: str) -> dict:
        if expression.startswith('PlacementUtils.HEIGHTMAP'):
            return {'type': 'minecraft:heightmap', 'heightmap': HEIGHTMAPS[expression.split('.')[1]]}
        empty = {'InSquarePlacement.spread()': 'in_square', 'BiomeFilter.biome()': 'biome'}
        if expression in empty:
            return {'type': 'minecraft:' + empty[expression]}
        singles = {'CountPlacement.of': ('count', 'count'), 'RarityFilter.onAverageOnceEvery': ('rarity_filter', 'chance'),
                   'SurfaceWaterDepthFilter.forMaxDepth': ('surface_water_depth_filter', 'max_water_depth')}
        for name, (kind, field) in singles.items():
            if expression.startswith(name + '('):
                value, = invocation(expression, name)
                return {'type': 'minecraft:' + kind, field: integer(value)}
        if expression.startswith('NoiseThresholdCountPlacement.of('):
            noise, below, above = invocation(expression, 'NoiseThresholdCountPlacement.of')
            return {'type': 'minecraft:noise_threshold_count', 'noise_level': number(noise),
                    'below_noise': integer(below), 'above_noise': integer(above)}
        block, = invocation(expression, 'PlacementUtils.filteredByBlockSurvival')
        return {'type': 'minecraft:block_predicate_filter',
                'predicate': {'type': 'minecraft:would_survive', 'state': self.state(block + '.defaultBlockState()')}}

    def placements(self, expression: str) -> list[dict]:
        if expression.startswith('AquaticPlacements.seagrassPlacement('):
            count, = invocation(expression, 'AquaticPlacements.seagrassPlacement')
            return [self.modifier('InSquarePlacement.spread()'), self.modifier('PlacementUtils.HEIGHTMAP_TOP_SOLID'),
                    {'type': 'minecraft:count', 'count': integer(count)}, self.modifier('BiomeFilter.biome()')]
        return [self.modifier(item) for item in invocation(expression, 'List.of')]


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    """先完成所有表达式转换和重名检查，再将完整输出计划交给调用者。"""
    source_root = source_root or SOURCES[MOD][0]
    parser = InlineParser(source_root)
    outputs = []
    for source in selected_biomes(source_root):
        text = source.read_text(encoding='utf-8')
        for call in calls(text, 'FeatureUtils.register'):
            identifier, feature, config = call['arguments']
            identifier = json.loads(identifier)
            parser.decorator_origins.clear()
            if feature == 'Feature.TREE':
                out = {'type': 'minecraft:tree', 'config': parser.tree(config)}
            elif feature == 'Feature.DISK':
                out = {'type': NAMESPACE + ':legacy_biome_disk', 'config': parser.disk(config)}
            else:
                raise ValueError(f'{source}:未支持的内联Feature：{feature}')
            namespace, path = identifier.split(':', 1)
            if namespace != MOD:
                raise ValueError(f'内联Feature命名空间越界：{identifier}')
            origins = [(MOD, source)] + [(MOD, item) for item in sorted(parser.decorator_origins)]
            outputs.append((DATA / 'configured_feature' / f'{path}.json', out, origins))
        for call in calls(text, 'PlacementUtils.register'):
            identifier, feature, placements = call['arguments']
            identifier = json.loads(identifier)
            if feature.startswith('FeatureUtils.register('):
                feature = json.loads(invocation(feature, 'FeatureUtils.register')[0])
            else:
                feature = 'minecraft:' + VANILLA_FEATURES[feature]
            namespace, path = identifier.split(':', 1)
            if namespace != MOD:
                raise ValueError(f'内联PlacedFeature命名空间越界：{identifier}')
            outputs.append((DATA / 'placed_feature' / f'{path}.json',
                            {'feature': feature, 'placement': parser.placements(placements)}, [(MOD, source)]))
    targets = [target for target, _, _ in outputs]
    if len(targets) != len(set(targets)):
        raise ValueError('群系内联注册包含重名目标，禁止覆盖')
    return outputs


def generate(converter) -> int:
    outputs = plan()
    for target, data, origins in outputs:
        converter.write(target, data, origins)
    return len(outputs)
