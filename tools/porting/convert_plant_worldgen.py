"""Generate Shicai001 worldgen data with source-hash tracking."""
from __future__ import annotations

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'


def generate(converter) -> None:
    mod = 'yvanchuxiuzhen'
    root = SOURCES[mod][0]
    source = root / 'src/main/java/net/mcreator/yvanchuxiuzhen/world/features/plants/Shicai001Plant08Feature.java'
    feature = f'{NAMESPACE}:shicai_001_plant_08'

    configured = {
        'type': feature,
        'config': {
            'tries': 1,
            'xz_spread': 7,
            'y_spread': 3,
            'feature': {
                'feature': {
                    'type': 'minecraft:simple_block',
                    'config': {
                        'to_place': {
                            'type': 'minecraft:simple_state_provider',
                            'state': {
                                'Name': feature,
                                'Properties': {'half': 'lower'},
                            },
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
    placed = {
        'feature': feature,
        'placement': [
            {'type': 'minecraft:count', 'count': 1},
            {'type': 'minecraft:rarity_filter', 'chance': 32},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'},
            {'type': 'minecraft:biome'},
        ],
    }

    converter.write(DATA / NAMESPACE / 'worldgen/configured_feature/shicai_001_plant_08.json', configured,
                    [(mod, source)])
    converter.write(DATA / NAMESPACE / 'worldgen/placed_feature/shicai_001_plant_08.json', placed,
                    [(mod, source)])
