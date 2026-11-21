"""只读核对植物转换结果及锁定来源中的空气谓词。"""
from __future__ import annotations

import hashlib
import json
import sys
import zipfile
from pathlib import Path

import convert_plant_worldgen
import convert_wild_plant_worldgen
from convert_content import SOURCES


class MemoryOutputs:
    def __init__(self) -> None:
        self.outputs: dict[Path, dict] = {}

    def write(self, path: Path, data: dict, origins: object) -> None:
        if path in self.outputs:
            raise ValueError(f'重复输出: {path}')
        self.outputs[path] = data


def main() -> int:
    source = SOURCES['yvanchuxiuzhen'][0]
    jar = source / 'build/fg_cache/net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/forge-1.18.2-40.1.0_mapped_official_1.18.2-sources.jar'
    digest = hashlib.sha1(jar.read_bytes()).hexdigest()
    expected = '3448006b1ee5ae3e6f84a0669f1651446881fe78'
    if digest != expected:
        print(f'来源jar SHA-1不匹配: {digest}', file=sys.stderr)
        return 1
    with zipfile.ZipFile(jar) as archive:
        feature = archive.read('net/minecraft/data/worldgen/features/FeatureUtils.java').decode('utf-8')
    if 'blockpredicate = BlockPredicate.ONLY_IN_AIR_PREDICATE;' not in feature or 'PlacementUtils.filtered(p_206481_, p_206482_, simplePatchPredicate(p_206483_))' not in feature:
        print('来源helper未匹配预期调用', file=sys.stderr)
        return 1
    outputs = MemoryOutputs()
    convert_plant_worldgen.generate(outputs)
    # 空底材列表不限制脚下的方块，但来源 helper 始终限制落点本身。
    air = [{'type': 'minecraft:block_predicate_filter', 'predicate': {
        'type': 'minecraft:matching_blocks', 'blocks': ['minecraft:air']}}]
    invalid = [p for p, data in outputs.outputs.items() if p.parent.name == 'configured_feature'
               and 'tries' in data['config'] and data['config']['feature']['placement'] != air]
    if invalid:
        print('成熟作物地物未保留来源空气谓词：', *invalid, sep='\n', file=sys.stderr)
        return 1
    convert_wild_plant_worldgen.generate(outputs)
    mismatches = [path for path, data in outputs.outputs.items()
                  if not path.is_file() or json.loads(path.read_text(encoding='utf-8')) != data]
    print(f'来源jar SHA-1: {digest}; 输出: {len(outputs.outputs)}; 不一致: {len(mismatches)}')
    for path in mismatches:
        print(path)
    return bool(mismatches)


if __name__ == '__main__':
    sys.exit(main())
