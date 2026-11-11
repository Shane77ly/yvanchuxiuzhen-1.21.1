"""转换已逐源实现的24种自然景观与随机结构模板Feature数据身份。"""
from __future__ import annotations

import re

from build_ledger import NAMESPACE
from convert_content import ROOT, SOURCES

DATA = ROOT / 'src/main/resources/data'
SOURCE_FEATURES = (
    'Huaguangshengcheng',
    'Evilspirittree01', 'Evilspirittree02', 'Evilspirittree03', 'Evilspirittree04',
    'Loessrock', 'Dingxiang01', 'Arrowpoisonwood01',
    'Zhuzidaoyv01', 'Zhuzidaoyv02', 'Zhuzidaoyv03', 'Zhuzidaoyv04', 'Fukongjiaoshi',
    'Suijijiegou01', 'Suijijiegou02', 'Suijijiegou03', 'Suijijiegou04', 'Suijijiegou05', 'Suijijiegou06',
    'Stonetree02', 'Stonetree03', 'Stonetree04', 'Stonetree06', 'Stonetree07',
)
CALLBACKS = {
    'Huaguangshengcheng': ('HuaguangshengchengZaiJieGouCunDangShengChengShiProcedure',),
    **{f'Suijijiegou{number:02}': ('Suijijiegou01FuJiaDeShengChengTiaoJianProcedure',)
       for number in range(1, 7)},
}


def generate(converter) -> None:
    mod = 'yvanchuxiuzhen'
    root = SOURCES[mod][0] / f'src/main/java/net/mcreator/{mod}'
    for name in SOURCE_FEATURES:
        source = root / f'world/features/{name}Feature.java'
        text = re.sub(r'\s+', '', source.read_text(encoding='utf-8'))
        configuration = re.findall(
            r'CONFIGURED_FEATURE=FeatureUtils\.register\("yvanchuxiuzhen:([\w_]+)",'
            r'FEATURE,FeatureConfiguration\.NONE\);', text)
        if len(configuration) != 1 or f'publicclass{name}FeatureextendsFeature<NoneFeatureConfiguration>' not in text:
            raise ValueError(f'来源模板Feature配置未识别：{source}')
        feature_name = configuration[0]
        placement = re.findall(
            rf'PLACED_FEATURE=PlacementUtils\.register\("yvanchuxiuzhen:{feature_name}",'
            r'CONFIGURED_FEATURE,List\.of\(\)\);', text)
        if len(placement) != 1:
            raise ValueError(f'来源模板Feature放置配置未识别：{source}')
        actual_callbacks = set(re.findall(r'([A-Z]\w+Procedure)\.execute\(', text))
        expected_callbacks = set(CALLBACKS.get(name, ()))
        if actual_callbacks != expected_callbacks:
            raise ValueError(f'来源模板Feature回调变化：{source}: {actual_callbacks}')
        provenance = [(mod, source)]
        provenance.extend((mod, root / f'procedures/{callback}.java') for callback in sorted(expected_callbacks))
        if name.startswith('Suijijiegou'):
            provenance.append((mod, root / 'network/YvanchuxiuzhenModVariables.java'))
        feature = f'{NAMESPACE}:{feature_name}'
        converter.write(DATA / NAMESPACE / f'worldgen/configured_feature/{feature_name}.json',
                        {'type': feature, 'config': {}}, provenance)
        converter.write(DATA / NAMESPACE / f'worldgen/placed_feature/{feature_name}.json',
                        {'feature': feature, 'placement': []}, provenance)
