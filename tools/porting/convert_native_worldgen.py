"""逐来源表达式转换全部33种非矿物配置型地物，维度门由同名Feature承载。"""
from __future__ import annotations

import json
import re
import zipfile
from dataclasses import dataclass
from pathlib import Path

from catalog_world_biomes import calls
from convert_biome_inline_worldgen import InlineParser, integer, int_provider, invocation
from convert_content import NAMESPACE, ROOT, SOURCES

DATA = ROOT / f'src/main/resources/data/{NAMESPACE}/worldgen'
MOD = 'yvanchuxiuzhen'
CLASSES = (
    'Barrenstonestingshengcheng', 'Bluestoneshengcheng', 'Diplidisdiandrashengcheng', 'Duckweedshengcheng',
    'Gucidici', 'Huaguangjiegou', 'Icelakeshengcheng', 'Icerock02', 'Icerock', 'Ironropeshengcheng',
    'Lanhuashengcheng', 'Mushroomblue01shengcheng', 'Mushroomblue02fangzhi', 'Mushroomblue03fangzhi',
    'Mushroompinkwhite01fangzhi', 'Mushroompinkwhite02fangzhi', 'Mushroompinkwhite03fangzhi',
    'Ricciafluitansshengcheng', 'Rockpile01', 'Rottingbambooshengcheng', 'Rushesshengcheng',
    'Shanhu2shengcheng', 'Shanhu3shengcheng', 'Shanhushengcheng', 'Snowdriftshengcheng',
    'Stonetree01', 'Stonetree05', 'Swampsiltdiwu', 'Tudibankuai01', 'Wenqvandiwu',
    'Witheredshrubshengcheng', 'Zishuijing', 'Zishuijingzhu',
)
FEATURE_CLASSES = {
    'RandomPatchFeature', 'SimpleBlockFeature', 'BlockBlobFeature', 'BlockPileFeature', 'LakeFeature',
    'BlockColumnFeature', 'CoralClawFeature', 'CoralMushroomFeature', 'CoralTreeFeature', 'ReplaceBlobsFeature',
}


@dataclass(frozen=True)
class NativeFeature:
    source: Path
    name: str
    feature_class: str
    configuration: dict
    placement: tuple[dict, ...]
    dimensions: tuple[str, ...]
    biomes: tuple[str, ...] | None


def one(pattern: str, text: str, source: Path) -> re.Match:
    result = list(re.finditer(pattern, text))
    if len(result) != 1:
        raise ValueError(f'来源应有一处匹配，实际{len(result)}处：{source}: {pattern}')
    return result[0]


def locations(expression: str, dimension: bool) -> tuple[str, ...]:
    result = []
    for item in invocation(expression, 'Set.of'):
        if dimension:
            key, item = invocation(item, 'ResourceKey.create')
            if key != 'Registry.DIMENSION_REGISTRY':
                raise ValueError(f'不是维度键：{key}')
        value, = invocation(item, 'new ResourceLocation')
        match = re.fullmatch(r'"([a-z0-9_:]+)"', value)
        if match is None:
            raise ValueError(f'不是来源注册ID：{value}')
        result.append(match[1])
    if len(set(result)) != len(result):
        raise ValueError(f'来源集合重复：{expression}')
    return tuple(result)


class NativeParser(InlineParser):
    """复用群系转换器的严格方块映射及Java分参，不执行Java表达式。"""

    def provider_int(self, expression: str):
        if re.fullmatch(r'-?\d+', expression):
            return integer(expression)
        if expression.startswith('BiasedToBottomInt.of('):
            minimum, maximum = invocation(expression, 'BiasedToBottomInt.of')
            return {'type': 'minecraft:biased_to_bottom', 'min_inclusive': integer(minimum),
                    'max_inclusive': integer(maximum)}
        return int_provider(expression)

    def block(self, expression: str) -> str:
        return self.state(expression + '.defaultBlockState()')['Name']

    def predicate(self, expression: str) -> dict:
        if expression in ('BlockPredicate.ONLY_IN_AIR_PREDICATE', 'BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE'):
            blocks = ['minecraft:air']
            if expression == 'BlockPredicate.ONLY_IN_AIR_OR_WATER_PREDICATE':
                blocks.append('minecraft:water')
            return {'type': 'minecraft:matching_blocks', 'blocks': blocks}
        if expression.startswith('BlockPredicate.allOf('):
            return {'type': 'minecraft:all_of', 'predicates': [self.predicate(item)
                    for item in invocation(expression, 'BlockPredicate.allOf')]}
        if expression.startswith('BlockPredicate.matchesBlocks('):
            blocks, offset = invocation(expression, 'BlockPredicate.matchesBlocks')
            return {'type': 'minecraft:matching_blocks', 'blocks': [self.block(item)
                    for item in invocation(blocks, 'List.of')], 'offset': self.offset(offset)}
        if expression.startswith('BlockPredicate.solid('):
            offset, = invocation(expression, 'BlockPredicate.solid')
            return {'type': 'minecraft:solid', 'offset': self.offset(offset)}
        raise ValueError(f'未支持的来源方块谓词：{expression}')

    @staticmethod
    def offset(expression: str) -> list[int]:
        x, y, z = invocation(expression, 'new Vec3i')
        return [integer(x), integer(y), integer(z)]

    @staticmethod
    def anchor(expression: str) -> dict:
        if expression == 'VerticalAnchor.bottom()':
            return {'above_bottom': 0}
        if expression == 'VerticalAnchor.top()':
            return {'below_top': 0}
        value, = invocation(expression, 'VerticalAnchor.absolute')
        return {'absolute': integer(value)}

    def modifier(self, expression: str) -> dict:
        if expression == 'InSquarePlacement.spread()':
            return {'type': 'minecraft:in_square'}
        if expression == 'BiomeFilter.biome()':
            return {'type': 'minecraft:biome'}
        if expression.startswith('CountPlacement.of('):
            count, = invocation(expression, 'CountPlacement.of')
            return {'type': 'minecraft:count', 'count': self.provider_int(count)}
        if expression.startswith('RarityFilter.onAverageOnceEvery('):
            chance, = invocation(expression, 'RarityFilter.onAverageOnceEvery')
            return {'type': 'minecraft:rarity_filter', 'chance': integer(chance)}
        if expression.startswith('HeightmapPlacement.onHeightmap('):
            heightmap, = invocation(expression, 'HeightmapPlacement.onHeightmap')
            match = re.fullmatch(r'Heightmap.Types.(MOTION_BLOCKING|OCEAN_FLOOR_WG|WORLD_SURFACE_WG|OCEAN_FLOOR)', heightmap)
            if match is None:
                raise ValueError(f'未支持的高度图：{heightmap}')
            return {'type': 'minecraft:heightmap', 'heightmap': match[1]}
        if expression.startswith('HeightRangePlacement.uniform('):
            minimum, maximum = invocation(expression, 'HeightRangePlacement.uniform')
            return {'type': 'minecraft:height_range', 'height': {'type': 'minecraft:uniform',
                    'min_inclusive': self.anchor(minimum), 'max_inclusive': self.anchor(maximum)}}
        if expression.startswith('BlockPredicateFilter.forPredicate('):
            predicate, = invocation(expression, 'BlockPredicateFilter.forPredicate')
            return {'type': 'minecraft:block_predicate_filter', 'predicate': self.predicate(predicate)}
        if expression.startswith('EnvironmentScanPlacement.scanningFor('):
            direction, target, allowed, maximum = invocation(expression, 'EnvironmentScanPlacement.scanningFor')
            if direction != 'Direction.DOWN':
                raise ValueError(f'未核实的扫描方向：{direction}')
            return {'type': 'minecraft:environment_scan', 'direction_of_search': 'down',
                    'target_condition': self.predicate(target), 'allowed_search_condition': self.predicate(allowed),
                    'max_steps': integer(maximum)}
        raise ValueError(f'未支持的来源投放修饰器：{expression}')

    def configuration(self, expression: str, feature_class: str) -> dict:
        if feature_class in ('CoralClawFeature', 'CoralMushroomFeature', 'CoralTreeFeature'):
            if expression != 'NoneFeatureConfiguration.INSTANCE':
                raise ValueError(f'未支持的珊瑚配置：{expression}')
            return {}
        if feature_class == 'BlockColumnFeature':
            height, provider = invocation(expression, 'BlockColumnConfiguration.simple')
            return {'layers': [{'height': self.provider_int(height), 'provider': self.provider(provider)}],
                    'direction': 'up', 'allowed_placement': self.predicate('BlockPredicate.ONLY_IN_AIR_PREDICATE'),
                    'prioritize_tip': False}
        if feature_class == 'BlockBlobFeature':
            state, = invocation(expression, 'new BlockStateConfiguration')
            return {'state': self.state(state)}
        if feature_class == 'BlockPileFeature':
            provider, = invocation(expression, 'new BlockPileConfiguration')
            return {'state_provider': self.provider(provider)}
        if feature_class == 'SimpleBlockFeature':
            provider, = invocation(expression, 'new SimpleBlockConfiguration')
            return {'to_place': self.provider(provider)}
        if feature_class == 'LakeFeature':
            fluid, barrier = invocation(expression, 'new LakeFeature.Configuration')
            return {'fluid': self.provider(fluid), 'barrier': self.provider(barrier)}
        if feature_class == 'ReplaceBlobsFeature':
            target, state, radius = invocation(expression, 'new ReplaceSphereConfiguration')
            return {'target': self.state(target), 'state': self.state(state), 'radius': self.provider_int(radius)}
        if feature_class == 'RandomPatchFeature':
            tries, xz, y, filtered = invocation(expression, 'new RandomPatchConfiguration')
            feature, configuration, predicate = invocation(filtered, 'PlacementUtils.filtered')
            if feature != 'Feature.SIMPLE_BLOCK':
                raise ValueError(f'斑块内嵌地物尚未核实：{feature}')
            return {'tries': integer(tries), 'xz_spread': integer(xz), 'y_spread': integer(y),
                    'feature': {'feature': {'type': 'minecraft:simple_block',
                                           'config': self.configuration(configuration, 'SimpleBlockFeature')},
                                'placement': [{'type': 'minecraft:block_predicate_filter',
                                               'predicate': self.predicate(predicate)}]}}
        raise ValueError(f'未支持的来源Feature类别：{feature_class}')


def read_features() -> tuple[NativeFeature, ...]:
    source_root, _ = SOURCES[MOD]
    directory = source_root / f'src/main/java/net/mcreator/{MOD}/world/features'
    parser = NativeParser(source_root)
    sources = []
    for source in sorted(directory.glob('*.java')):
        text = source.read_text(encoding='utf-8')
        match = re.search(r'public class \w+ extends (\w+)', text)
        if match is not None and match[1] in FEATURE_CLASSES:
            sources.append((source, text, match[1]))
    expected = {name + 'Feature.java' for name in CLASSES}
    actual = {source.name for source, _, _ in sources}
    if actual != expected:
        raise ValueError(f'来源普通地物族发生变化：新增{sorted(actual - expected)}，缺失{sorted(expected - actual)}')
    features = []
    for source, text, feature_class in sources:
        configured, = [item for item in calls(text) if item['name'] == 'FeatureUtils.register']
        placed, = [item for item in calls(text) if item['name'] == 'PlacementUtils.register']
        raw_name, instance, configuration = configured['arguments']
        placed_name, configured_instance, modifiers = placed['arguments']
        if instance != 'FEATURE' or configured_instance != 'CONFIGURED_FEATURE' or raw_name != placed_name:
            raise ValueError(f'地物注册关系变化：{source}')
        name = one(r'"yvanchuxiuzhen:([a-z0-9_]+)"', raw_name, source)[1]
        biome_expression = one(r'GENERATE_BIOMES\s*=\s*([^;]+);', text, source)[1].strip()
        biomes = None if biome_expression == 'null' else locations(biome_expression, False)
        gates = list(re.finditer(r'generateDimensions\s*=\s*([^;]+);', text))
        if gates:
            gate, = gates
            dimensions = locations(gate[1].strip(), True)
            compact = re.sub(r'\s+', '', text)
            place = one(r'publicbooleanplace\(FeaturePlaceContext<[^>]+>context\)\{([^}]+)\}', compact, source)[1]
            if place != ('WorldGenLevelworld=context.level();'
                         'if(!generateDimensions.contains(world.getLevel().dimension()))returnfalse;'
                         'returnsuper.place(context);'):
                raise ValueError(f'地物来源有额外place行为：{source}: {place}')
        else:
            dimensions = ()
            if re.search(r'boolean\s+place\s*\(', text):
                raise ValueError(f'无维度门地物有自定义place：{source}')
        configuration = parser.configuration(configuration, feature_class)
        placement = tuple(parser.modifier(item) for item in invocation(modifiers, 'List.of'))
        features.append(NativeFeature(source, name, feature_class, configuration, placement, dimensions, biomes))
    return tuple(features)


def generate(converter) -> None:
    source_root, _ = SOURCES[MOD]
    registration = source_root / f'src/main/java/net/mcreator/{MOD}/init/YvanchuxiuzhenModBlocks.java'
    forge_directory = source_root / 'build/fg_cache/net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2'
    forge_resources = forge_directory / 'forge-1.18.2-40.1.0_mapped_official_1.18.2-recomp.jar'
    forge_sources = forge_directory / 'forge-1.18.2-40.1.0_mapped_official_1.18.2-sources.jar'
    with zipfile.ZipFile(forge_resources) as archive:
        stone = json.loads(archive.read('data/forge/tags/blocks/stone.json'))
    expected = ['minecraft:' + name for name in (
        'andesite', 'diorite', 'granite', 'infested_stone', 'stone', 'polished_andesite',
        'polished_diorite', 'polished_granite', 'deepslate', 'polished_deepslate', 'infested_deepslate', 'tuff')]
    if stone != {'replace': False, 'values': expected}:
        raise ValueError('来源Forge石料标签变化，须重新核对石团落点条件')
    with zipfile.ZipFile(forge_sources) as archive:
        feature_source = archive.read('net/minecraft/world/level/levelgen/feature/Feature.java').decode('utf-8')
    predicate = re.search(r'protected static boolean isStone\([^)]*\)\s*\{([^}]+)\}', feature_source)
    if predicate is None or 'net.minecraftforge.common.Tags.Blocks.STONE' not in predicate[1]:
        raise ValueError('来源Feature石料判定变化，须重新核对石团落点条件')
    converter.write(ROOT / f'src/main/resources/data/{NAMESPACE}/tags/block/legacy_worldgen_stone.json',
                    stone, [(MOD, forge_resources), (MOD, forge_sources)])
    clients = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(clients) != 1:
        raise ValueError('来源原版资源Jar必须唯一，须重新核对石团泥土地基')
    with zipfile.ZipFile(clients[0]) as archive:
        dirt = json.loads(archive.read('data/minecraft/tags/blocks/dirt.json'))
    expected_dirt = ['minecraft:' + name for name in (
        'dirt', 'grass_block', 'podzol', 'coarse_dirt', 'mycelium', 'rooted_dirt', 'moss_block')]
    dirt_predicate = re.search(r'public static boolean isDirt\([^)]*\)\s*\{([^}]+)\}', feature_source)
    if dirt != {'replace': False, 'values': expected_dirt} or dirt_predicate is None or 'BlockTags.DIRT' not in dirt_predicate[1]:
        raise ValueError('来源泥土标签或Feature泥土判定变化，须重新核对石团落点条件')
    if list((source_root / 'src/main/resources/data').glob('*/tags/blocks/dirt.json')):
        raise ValueError('来源Mod追加泥土标签，须重新核对石团落点条件')
    converter.write(ROOT / f'src/main/resources/data/{NAMESPACE}/tags/block/legacy_worldgen_dirt.json',
                    {'replace': True, 'values': expected_dirt}, [(MOD, clients[0]), (MOD, forge_sources)])
    for feature in read_features():
        feature_id = f'{NAMESPACE}:{feature.name}'
        provenance = [(MOD, feature.source), (MOD, registration)]
        converter.write(DATA / f'configured_feature/{feature.name}.json',
                        {'type': feature_id, 'config': feature.configuration}, provenance)
        converter.write(DATA / f'placed_feature/{feature.name}.json',
                        {'feature': feature_id, 'placement': list(feature.placement)}, provenance)
