"""转换两个来源的完整普通矿物族，维度条件由同名 World Feature 承载。"""
from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'
SOURCE_CLASSES = (
    'Bluestone',
    'Cailiao16101', 'Cailiao16201', 'Cailiao16301', 'Cailiao16401', 'Cailiao16501',
    'Cailiao16601', 'Cailiao16701', 'Cailiao16801', 'Cailiao16901',
    'Cailiao191ore', 'Cailiao192ore', 'Cailiao193ore', 'Cailiao194ore', 'Cailiao195ore',
    'Cailiao196ore', 'Cailiao197ore', 'Cailiao198ore', 'Cailiao199ore',
    'Cailiao20101', 'Cailiao20301', 'Cailiao20501', 'Cailiao20701', 'Cailiao20801',
    'Cailiao20901', 'Cailiao21101', 'Cailiao21301', 'Cailiao21501', 'Cailiao21601',
    'Cailiao21701', 'Cailiao21901', 'Fuluzhushablock',
)


@dataclass(frozen=True)
class Ore:
    mod: str
    source: Path
    block_source: Path
    name: str
    block: str
    size: int
    count: int
    height: str
    minimum: int
    maximum: int
    dimensions: tuple[str, ...]
    biomes: tuple[str, ...] | None


def _one(pattern: str, text: str, source: Path) -> re.Match:
    matches = list(re.finditer(pattern, text))
    if len(matches) != 1:
        raise ValueError(f'矿物来源形式未识别（{len(matches)}处）：{source}: {pattern}')
    return matches[0]


def _locations(expression: str, dimensions: bool, source: Path) -> tuple[str, ...]:
    item = r'newResourceLocation\("([\w:]+)"\)'
    if dimensions:
        item = rf'ResourceKey\.create\(Registry\.DIMENSION_REGISTRY,{item}\)'
    if not re.fullmatch(rf'Set\.of\({item}(?:,{item})*\)', expression):
        raise ValueError(f'矿物生成条件未识别：{source}: {expression}')
    return tuple(re.findall(r'newResourceLocation\("([\w:]+)"\)', expression))


def read_ores() -> tuple[Ore, ...]:
    """逐类读取矿脉、四步放置、纯石头谓词及维度/群系条件。"""
    ores = []
    for mod, (root, prefix) in SOURCES.items():
        directory = root / f'src/main/java/net/mcreator/{mod}/world/features/ores'
        files = sorted(directory.glob('*.java'))
        expected = {f'{name}Feature.java' for name in SOURCE_CLASSES} if mod == 'yvanchuxiuzhen' else set()
        actual = {source.name for source in files}
        if actual != expected:
            raise ValueError(f'矿物来源族变化：{mod}，新增{sorted(actual - expected)}，缺失{sorted(expected - actual)}')
        if not files:
            continue
        block_source = root / f'src/main/java/net/mcreator/{mod}/init/{prefix}ModBlocks.java'
        block_names = dict(re.findall(
            r'RegistryObject<Block>\s+(\w+)\s*=\s*\w+\.register\("(\w+)"',
            block_source.read_text(encoding='utf-8')))
        for source in files:
            text = re.sub(r'\s+', '', source.read_text(encoding='utf-8'))
            configured = _one(
                rf'CONFIGURED_FEATURE=FeatureUtils\.register\("{mod}:([\w_]+)",FEATURE,'
                rf'newOreConfiguration\({source.stem}RuleTest\.INSTANCE,'
                rf'{prefix}ModBlocks\.(\w+)\.get\(\)\.defaultBlockState\(\),(\d+)\)\);',
                text, source)
            name, block_constant, size = configured.groups()
            placed = _one(
                rf'PLACED_FEATURE=PlacementUtils\.register\("{mod}:{name}",CONFIGURED_FEATURE,'
                r'List\.of\(CountPlacement\.of\((\d+)\),InSquarePlacement\.spread\(\),'
                r'HeightRangePlacement\.(uniform|triangle)\(VerticalAnchor\.absolute\((-?\d+)\),'
                r'VerticalAnchor\.absolute\((-?\d+)\)\),BiomeFilter\.biome\(\)\)\);',
                text, source)
            count, height, minimum, maximum = placed.groups()
            _one(r'base_blocks=List\.of\(Blocks\.STONE\);', text, source)
            _one(
                r'publicbooleantest\(BlockStateblockAt,Randomrandom\)\{'
                r'if\(base_blocks==null\)\{base_blocks=List\.of\(Blocks\.STONE\);\}'
                r'returnbase_blocks\.contains\(blockAt\.getBlock\(\)\);\}',
                text, source)
            _one(
                r'publicbooleanplace\(FeaturePlaceContext<OreConfiguration>context\)\{'
                r'WorldGenLevelworld=context\.level\(\);'
                r'if\(!generate_dimensions\.contains\(world\.getLevel\(\)\.dimension\(\)\)\)'
                r'returnfalse;returnsuper\.place\(context\);\}', text, source)
            dimensions = _locations(_one(r'generate_dimensions=([^;]+);', text, source)[1], True, source)
            biome_expression = _one(r'GENERATE_BIOMES=([^;]+);', text, source)[1]
            biomes = None if biome_expression == 'null' else _locations(biome_expression, False, source)
            ores.append(Ore(mod, source, block_source, name, block_names[block_constant], int(size),
                            int(count), height, int(minimum), int(maximum), dimensions, biomes))
    return tuple(ores)


def generate(converter) -> None:
    for ore in read_ores():
        feature = f'{NAMESPACE}:{ore.name}'
        height = {
            'type': 'minecraft:uniform' if ore.height == 'uniform' else 'minecraft:trapezoid',
            'min_inclusive': {'absolute': ore.minimum},
            'max_inclusive': {'absolute': ore.maximum},
        }
        if ore.height == 'triangle':
            height['plateau'] = 0
        # 来源32种RuleTest均只比较Blocks.STONE，不消费随机数；block_match语义相同。
        # 使用同名自定义OreFeature，在super.place前保留来源维度门槛。
        configured = {
            'type': feature,
            'config': {
                'size': ore.size,
                'discard_chance_on_air_exposure': 0.0,
                'targets': [{
                    'target': {'predicate_type': 'minecraft:block_match', 'block': 'minecraft:stone'},
                    'state': {'Name': f'{NAMESPACE}:{ore.block}'},
                }],
            },
        }
        placed = {
            'feature': feature,
            'placement': [
                {'type': 'minecraft:count', 'count': ore.count},
                {'type': 'minecraft:in_square'},
                {'type': 'minecraft:height_range', 'height': height},
                {'type': 'minecraft:biome'},
            ],
        }
        provenance = [(ore.mod, ore.source), (ore.mod, ore.block_source)]
        converter.write(DATA / NAMESPACE / f'worldgen/configured_feature/{ore.name}.json', configured, provenance)
        converter.write(DATA / NAMESPACE / f'worldgen/placed_feature/{ore.name}.json', placed, provenance)
