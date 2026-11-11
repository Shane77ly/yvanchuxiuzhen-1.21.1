"""生成逐源核对的20种自然模板树木地物，保留来源注册ID与来源SHA-1。"""
from __future__ import annotations

import re

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'
SOURCE_FEATURES = (
    'Cassia06', 'Elm07', 'Elm08', 'Laurel06', 'Laurel08', 'Maple01', 'Mulberry02',
    'Peach01', 'Poplar01', 'Poplar03', 'Privet05', 'Sandalwoodtree01',
    'Treejiegou01', 'Treejiegou03', 'Treejiegou07', 'Treejiegou09',
    'Treejiegou12', 'Treejiegou15', 'Treejiegou17', 'Treejiegou18',
)


def generate(converter) -> None:
    mod = 'yvanchuxiuzhen'
    root = SOURCES[mod][0]
    for name in SOURCE_FEATURES:
        source = root / f'src/main/java/net/mcreator/{mod}/world/features/{name}Feature.java'
        text = source.read_text(encoding='utf-8')
        feature_name = re.search(r'FeatureUtils.register\("yvanchuxiuzhen:([^"]+)"', text)[1]
        feature = f'{NAMESPACE}:{feature_name}'
        converter.write(DATA / NAMESPACE / f'worldgen/configured_feature/{feature_name}.json',
                        {'type': feature, 'config': {}}, [(mod, source)])
        converter.write(DATA / NAMESPACE / f'worldgen/placed_feature/{feature_name}.json',
                        {'feature': feature, 'placement': []}, [(mod, source)])
