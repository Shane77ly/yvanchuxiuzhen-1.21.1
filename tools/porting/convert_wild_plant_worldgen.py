"""按来源定义转换非编号野生植物的随机斑块地物。"""
from __future__ import annotations

from dataclasses import dataclass

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'


@dataclass(frozen=True)
class WildPlant:
    name: str
    source_name: str
    tries: int
    count: int
    rarity: int | None
    height: str
    biomes: tuple[str, ...] | None
    dimensions: tuple[str, ...]


# 本族均使用 SIMPLE_BLOCK 默认状态和空地面过滤列表；各地物的维度约束
# 由 WildPlantFeatures 的独立来源注册承载，群系集合保留供 World 装配。
WILD_PLANTS = (
    WildPlant('androsace_02', 'Androsace02Feature.java', 64, 20, 32, 'HEIGHTMAP', ('biomepeach_02', 'biomeautumn_03', 'biomepeach_01'), ('dimensionrenjian',)),
    WildPlant('androsace', 'AndrosaceFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomepeach_02', 'biomeautumn_03', 'biomepeach_01'), ('dimensionrenjian',)),
    WildPlant('annualfleabane', 'AnnualfleabaneFeature.java', 16, 28, 32, 'HEIGHTMAP', ('biomewind_01',), ('dimensionrenjian',)),
    WildPlant('asiaticdayflower', 'AsiaticdayflowerFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomeautumn_03',), ('dimensionrenjian',)),
    WildPlant('biggrass', 'BiggrassFeature.java', 64, 20, None, 'HEIGHTMAP_WORLD_SURFACE', ('biometiger_02',), ('dimensionyaohun',)),
    WildPlant('bitegrass_01', 'Bitegrass01Feature.java', 64, 3, 32, 'HEIGHTMAP', ('biomewind_02', 'biomejade_02', 'biomeautumn_03'), ('dimensionrenjian',)),
    WildPlant('bladegrass', 'BladegrassFeature.java', 64, 8, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomediling_05',), ('dimensiondiling',)),
    WildPlant('bluegrass', 'BluegrassFeature.java', 6, 3, 32, 'HEIGHTMAP', ('biomewinter_03',), ('dimensionrenjian',)),
    WildPlant('cannibalmoss', 'CannibalmossFeature.java', 64, 5, 32, 'HEIGHTMAP', ('biomejade_01',), ('dimensionrenjian',)),
    WildPlant('chineseviolet', 'ChinesevioletFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomeautumn_03',), ('dimensionrenjian',)),
    WildPlant('chongshicao', 'ChongshicaoFeature.java', 6, 2, 32, 'HEIGHTMAP', ('biomepeach_02', 'biomepeach_01'), ('dimensionrenjian',)),
    WildPlant('coldleaf', 'ColdleafFeature.java', 5, 3, 32, 'HEIGHTMAP', ('biomewinter_01',), ('dimensionrenjian',)),
    WildPlant('columbinemimosa', 'ColumbinemimosaFeature.java', 64, 10, 32, 'FULL_RANGE', ('biomewind_03',), ('dimensionrenjian',)),
    WildPlant('corpsegrass', 'CorpsegrassFeature.java', 64, 16, None, 'FULL_RANGE', ('biomeshijve_02', 'biomeshijve_01', 'biomeshijve_03'), ('dimensionwugui',)),
    WildPlant('fragranceflower', 'FragranceflowerFeature.java', 64, 10, 32, 'HEIGHTMAP', ('biomeautumn_02', 'biomeautumn_03'), ('dimensionrenjian',)),
    WildPlant('frostflowers', 'FrostflowersFeature.java', 64, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomewinter_03',), ('dimensionrenjian',)),
    WildPlant('gardenia', 'GardeniaFeature.java', 32, 5, 32, 'HEIGHTMAP', ('biomewind_02',), ('dimensionrenjian',)),
    WildPlant('goldenboilinggrass', 'GoldenboilinggrassFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomeautumn_03',), ('dimensionrenjian',)),
    WildPlant('greenbristlegrass', 'GreenbristlegrassFeature.java', 16, 8, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomewind_01', 'biomewind_03', 'biomewind_02', 'biomewind_04'), ('dimensionrenjian',)),
    WildPlant('higanbana', 'HiganbanaFeature.java', 64, 12, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomediling_01',), ('dimensiondiling',)),
    WildPlant('iceboundrock_02', 'Iceboundrock02Feature.java', 16, 5, 32, 'HEIGHTMAP', ('biomewinter_03', 'biomewinter_02'), ('dimensionrenjian',)),
    WildPlant('iceboundrock', 'IceboundrockFeature.java', 16, 2, 32, 'HEIGHTMAP', ('biomewinter_01', 'biomewinter_02'), ('dimensionrenjian',)),
    WildPlant('icecrystal', 'IcecrystalFeature.java', 32, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomewinter_03', 'biomewinter_01', 'biomewinter_02'), ('dimensionrenjian',)),
    WildPlant('icicleflower', 'IcicleflowerFeature.java', 32, 5, 32, 'HEIGHTMAP', ('biomewinter_02',), ('dimensionrenjian',)),
    WildPlant('ironbamboo', 'IronbambooFeature.java', 14, 4, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomesand_03',), ('dimensionyaohun',)),
    WildPlant('ironbambootail', 'IronbambootailFeature.java', 10, 3, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomesand_03',), ('dimensionyaohun',)),
    WildPlant('jadefairy', 'JadefairyFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomejade_01',), ('dimensionrenjian',)),
    WildPlant('lamiumamplexicaule', 'LamiumamplexicauleFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomewind_01', 'biomewind_02', 'biomeautumn_03'), ('dimensionrenjian',)),
    WildPlant('lurker', 'LurkerFeature.java', 5, 2, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomesand_03',), ('dimensionyaohun',)),
    WildPlant('ma_plant_04', 'MaPlant04Feature.java', 2, 1, 32, 'HEIGHTMAP', ('biomejade_01', 'biomejade_02'), ('dimensionrenjian', 'dimensionyaohun')),
    WildPlant('mian_plant_05', 'MianPlant05Feature.java', 3, 1, 32, 'HEIGHTMAP', ('biomeautumn_01', 'biomeautumn_03'), ('dimensionrenjian', 'dimensionyaohun')),
    WildPlant('mint', 'MintFeature.java', 16, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomelong_02',), ('dimensionyaohun',)),
    WildPlant('nautilus', 'NautilusFeature.java', 8, 3, 32, 'HEIGHTMAP', ('biomestone_02',), ('dimensionrenjian',)),
    WildPlant('nichanggrass_06', 'Nichanggrass06Feature.java', 3, 2, 32, 'HEIGHTMAP', None, ('dimensionrenjian', 'dimensionyaohun')),
    WildPlant('opticalprism', 'OpticalprismFeature.java', 64, 10, 32, 'HEIGHTMAP', ('biomejade_01',), ('dimensionrenjian',)),
    WildPlant('purslane', 'PurslaneFeature.java', 8, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomeautumn_01', 'biomeautumn_02'), ('dimensionrenjian',)),
    WildPlant('raincloud', 'RaincloudFeature.java', 64, 10, 32, 'HEIGHTMAP', ('biomewind_02', 'biomecloud_02', 'biomejade_02'), ('dimensionrenjian',)),
    WildPlant('scorchedgrass', 'ScorchedgrassFeature.java', 64, 10, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomecloud_03',), ('dimensionrenjian',)),
    WildPlant('skybluealfalfa', 'SkybluealfalfaFeature.java', 64, 20, 32, 'HEIGHTMAP', ('biomeautumn_02', 'biomeautumn_03'), ('dimensionrenjian',)),
    WildPlant('soulgrass', 'SoulgrassFeature.java', 64, 8, None, 'FULL_RANGE', ('biomebaichuan_02', 'biomebaichuan_01', 'biomebaichuan_03'), ('dimensionwugui',)),
    WildPlant('spiritgrass', 'SpiritgrassFeature.java', 64, 10, 32, 'HEIGHTMAP', ('biomejade_01', 'biomejade_03', 'biomejade_02'), ('dimensionrenjian',)),
    WildPlant('tailbladegrass', 'TailbladegrassFeature.java', 64, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomediling_05',), ('dimensiondiling',)),
    WildPlant('tailgreenbristlegrass', 'TailgreenbristlegrassFeature.java', 32, 5, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomewind_02', 'biomewind_04'), ('dimensionrenjian',)),
    WildPlant('tailreed', 'TailreedFeature.java', 64, 10, None, 'FULL_RANGE', ('biomewind_04',), ('dimensionrenjian',)),
    WildPlant('thistlesandthorns', 'ThistlesandthornsFeature.java', 10, 3, None, 'HEIGHTMAP_WORLD_SURFACE', ('biomesand_01',), ('dimensionyaohun',)),
    WildPlant('thundergrass', 'ThundergrassFeature.java', 12, 4, 32, 'HEIGHTMAP', ('biomecloud_03',), ('dimensionrenjian',)),
    WildPlant('tripterygiumwilfordii', 'TripterygiumwilfordiiFeature.java', 4, 4, 32, 'HEIGHTMAP', ('biomesand_04',), ('dimensionyaohun',)),
)


def generate(converter) -> None:
    mod = 'yvanchuxiuzhen'
    source_root = SOURCES[mod][0] / 'src/main/java/net/mcreator/yvanchuxiuzhen/world/features/plants'
    for plant in WILD_PLANTS:
        feature = f'{NAMESPACE}:{plant.name}'
        configured = {
            'type': feature,
            'config': {
                'tries': plant.tries,
                'xz_spread': 7,
                'y_spread': 3,
                'feature': {
                    'feature': {
                        'type': 'minecraft:simple_block',
                        'config': {
                            'to_place': {
                                'type': 'minecraft:simple_state_provider',
                                'state': {'Name': feature},
                            },
                        },
                    },
                    'placement': [{
                        'type': 'minecraft:block_predicate_filter',
                        'predicate': {
                            'type': 'minecraft:matching_blocks',
                            'blocks': ['minecraft:air'],
                        },
                    }],
                },
            },
        }
        placement = [{'type': 'minecraft:count', 'count': plant.count}]
        if plant.rarity is not None:
            placement.append({'type': 'minecraft:rarity_filter', 'chance': plant.rarity})
        placement.append({'type': 'minecraft:in_square'})
        if plant.height == 'FULL_RANGE':
            placement.append({
                'type': 'minecraft:height_range',
                'height': {
                    'type': 'minecraft:uniform',
                    'min_inclusive': {'above_bottom': 0},
                    'max_inclusive': {'below_top': 0},
                },
            })
        else:
            placement.append({
                'type': 'minecraft:heightmap',
                'heightmap': 'WORLD_SURFACE_WG' if plant.height == 'HEIGHTMAP_WORLD_SURFACE' else 'MOTION_BLOCKING',
            })
        placement.append({'type': 'minecraft:biome'})
        source = source_root / plant.source_name
        converter.write(DATA / NAMESPACE / f'worldgen/configured_feature/{plant.name}.json',
                        configured, [(mod, source)])
        converter.write(DATA / NAMESPACE / f'worldgen/placed_feature/{plant.name}.json',
                        {'feature': feature, 'placement': placement}, [(mod, source)])
