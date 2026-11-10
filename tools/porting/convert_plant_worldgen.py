"""Generate source-verified crop features with source-hash tracking."""
from __future__ import annotations

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'


def generate(converter) -> None:
    for number in ('001', '002', '004', '005', '006'):
        generate_cereal(converter, number)
    for number in ('007', '011', '012', '013', '014', '016', '025', '026', '027',
                   '017', '018', '019', '020', '021', '022', '023', '024',
                   '028', '029', '030', '031', '032', '033'):
        generate_crop(converter, number)


def generate_cereal(converter, number: str) -> None:
    generate_crop(converter, number, double=True)


def generate_crop(converter, number: str, *, double: bool = False) -> None:
    mod = 'yvanchuxiuzhen'
    root = SOURCES[mod][0]
    stage = '08' if double else '04'
    source = root / f'src/main/java/net/mcreator/yvanchuxiuzhen/world/features/plants/Shicai{number}Plant{stage}Feature.java'
    feature = f'{NAMESPACE}:shicai_{number}_plant_{stage}'
    state = {'Name': feature}
    if double:
        state['Properties'] = {'half': 'lower'}

    configured = {
        'type': feature,
        'config': {
            'tries': 4 if number == '023' else 2 if number == '032' else 1,
            'xz_spread': 7,
            'y_spread': 3,
            'feature': {
                'feature': {
                    'type': 'minecraft:simple_block',
                    'config': {
                        'to_place': {
                            'type': 'minecraft:simple_state_provider',
                            'state': state,
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
            {'type': 'minecraft:count', 'count': 2 if number == '032' else 1},
            {'type': 'minecraft:rarity_filter', 'chance': 32},
            {'type': 'minecraft:in_square'},
            {'type': 'minecraft:heightmap', 'heightmap': 'MOTION_BLOCKING'},
            {'type': 'minecraft:biome'},
        ],
    }

    converter.write(DATA / NAMESPACE / f'worldgen/configured_feature/shicai_{number}_plant_{stage}.json', configured,
                    [(mod, source)])
    converter.write(DATA / NAMESPACE / f'worldgen/placed_feature/shicai_{number}_plant_{stage}.json', placed,
                    [(mod, source)])
