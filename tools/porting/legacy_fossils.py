"""来源化石处理器、模板闭包及目标资源计划。"""
from __future__ import annotations

import gzip
import hashlib
import json
import re
import struct
import zipfile
from pathlib import Path

from convert_content import ROOT

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}'
SOURCE_PROCESSORS = {
    'fossil_rot': 'ImmutableList.of(new BlockRotProcessor(0.9F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))',
    'fossil_coal': 'ImmutableList.of(new BlockRotProcessor(0.1F), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))',
    'fossil_diamonds': 'ImmutableList.of(new BlockRotProcessor(0.1F), new RuleProcessor(ImmutableList.of(new ProcessorRule(new BlockMatchTest(Blocks.COAL_ORE), AlwaysTrueTest.INSTANCE, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()))), new ProtectedBlockProcessor(BlockTags.FEATURES_CANNOT_REPLACE))',
}


def processors(source: zipfile.ZipFile) -> dict:
    text = source.read('net/minecraft/data/worldgen/ProcessorLists.java').decode('utf-8')
    result = {}
    for name, expression in SOURCE_PROCESSORS.items():
        match = re.search(r'\b' + name.upper() + r'\s*=\s*register\("' + name + r'",\s*([^;]+)\);', text)
        if match is None or re.sub(r'\s+', '', match[1]) != re.sub(r'\s+', '', expression):
            raise ValueError(f'来源化石处理器定义变化：{name}')
        chain = [{'processor_type': 'minecraft:block_rot', 'integrity': 0.9 if name == 'fossil_rot' else 0.1}]
        if name == 'fossil_diamonds':
            chain.append({'processor_type': 'minecraft:rule', 'rules': [{
                'input_predicate': {'predicate_type': 'minecraft:block_match', 'block': 'minecraft:coal_ore'},
                'location_predicate': {'predicate_type': 'minecraft:always_true'},
                'output_state': {'Name': 'minecraft:deepslate_diamond_ore'}}]})
        chain.append({'processor_type': 'minecraft:protected_blocks',
            'value': '#' + MOD + ':legacy_worldgen_features_cannot_replace'})
        result[name] = {'processors': chain}
    return result


def plan(source_jar: Path, client_jar: Path, snapshot: dict) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    outputs = []
    with zipfile.ZipFile(source_jar) as source:
        for name, data in processors(source).items():
            outputs.append((DATA / f'worldgen/processor_list/legacy_{name}.json', data, [(MOD, source_jar)]))
    for name in ('fossil_coal', 'fossil_diamonds'):
        data = json.loads(json.dumps(snapshot['registries']['configured_feature']['minecraft:' + name]))
        for key in ('fossil_processors', 'overlay_processors'):
            data['config'][key] = MOD + ':legacy_' + data['config'][key].split(':')[1]
        outputs.append((DATA / f'worldgen/configured_feature/legacy_vanilla/{name}.json', data, [(MOD, source_jar)]))
    for name, config in (('fossil_upper', 'fossil_coal'), ('fossil_lower', 'fossil_diamonds')):
        data = json.loads(json.dumps(snapshot['registries']['placed_feature']['minecraft:' + name]))
        data['feature'] = MOD + ':legacy_vanilla/' + config
        outputs.append((DATA / f'worldgen/placed_feature/legacy_vanilla/{name}.json', data, [(MOD, source_jar)]))
    return outputs


def verify_templates(source_client: zipfile.ZipFile, target_client: zipfile.ZipFile, config: dict) -> list[dict]:
    records = []
    old_version = b'\x03\x00\x0bDataVersion' + struct.pack('>i', 2975)
    new_version = b'\x03\x00\x0bDataVersion' + struct.pack('>i', 3955)
    for identifier in config['fossil_structures'] + config['overlay_structures']:
        namespace, name = identifier.split(':')
        old = gzip.decompress(source_client.read(f'data/{namespace}/structures/{name}.nbt'))
        new = gzip.decompress(target_client.read(f'data/{namespace}/structure/{name}.nbt'))
        # 锁定模板仅DataVersion字节变化，连同NBT序列顺序一起比较，不忽略其他字段。
        same = old.count(old_version) == 1 and new.count(new_version) == 1 and old.replace(old_version, new_version) == new
        records.append({'id': identifier, 'equal_except_data_version': same,
            'source_sha256': hashlib.sha256(old).hexdigest(), 'target_sha256': hashlib.sha256(new).hexdigest()})
    return records
