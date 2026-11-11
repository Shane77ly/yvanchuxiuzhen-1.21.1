"""生成 1.18.2 CAVE/CANYON 的来源可替换方块集合。"""
from __future__ import annotations

import re
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES
from legacy_block_tags import entries

MOD = 'yvanchuxiuzhen'
DATA = ROOT / 'src/main/resources/data'
WORLD_CARVER_ENTRY = 'net/minecraft/world/level/levelgen/carver/WorldCarver.java'
DEFAULT_UNIQUE_BLOCKS = 43
FIXER_CLASSES = 8
EXPECTED_MEMBERS = 60


def source_members(source_root: Path | None = None, source_jar: Path | None = None) -> tuple[list[str], list[Path]]:
    source_root = source_root or SOURCES[MOD][0]
    if source_jar is None:
        candidates = list((source_root / 'build/fg_cache').glob(
            'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
        if len(candidates) != 1:
            raise ValueError('鎖定的 1.18.2 原版源碼 Jar 必須唯一')
        source_jar = candidates[0]
    with zipfile.ZipFile(source_jar) as archive:
        world_carver = archive.read(WORLD_CARVER_ENTRY).decode('utf-8')
    default_match = re.search(r'replaceableBlocks\s*=\s*ImmutableSet\.of\((.*?)\);', world_carver, re.S)
    if default_match is None:
        raise ValueError('1.18.2 WorldCarver 默认 replaceableBlocks 初始化结构发生变化')
    defaults = [f'minecraft:{name.lower()}' for name in re.findall(r'Blocks\.([A-Z0-9_]+)', default_match[1])]
    if len(set(defaults)) != DEFAULT_UNIQUE_BLOCKS:
        raise ValueError(f'WorldCarver 默认成员数变化：count={len(defaults)}, unique={len(set(defaults))}')

    blocks_file = source_root / 'src/main/java/net/mcreator/yvanchuxiuzhen/init/YvanchuxiuzhenModBlocks.java'
    blocks_source = blocks_file.read_text(encoding='utf-8')
    block_ids = dict(re.findall(
        r'RegistryObject<Block>\s+(\w+)\s*=\s*REGISTRY\.register\("([\w]+)"', blocks_source))
    dimension_dir = source_root / 'src/main/java/net/mcreator/yvanchuxiuzhen/world/dimension'
    fixers = []
    additions = set()
    for path in sorted(dimension_dir.glob('*.java')):
        text = path.read_text(encoding='utf-8')
        if not re.search(r'WorldCarver\.(?:CAVE|CANYON)\.replaceableBlocks', text):
            continue
        if not re.search(r'WorldCarver\.CAVE\.replaceableBlocks', text) or not re.search(
                r'WorldCarver\.CANYON\.replaceableBlocks', text):
            raise ValueError(f'来源 Fixer 只修改一个 carver：{path.name}')
        fixers.append(path)
        for expression in re.findall(r'replaceableBlocks\.add\((.*?)\);', text, re.S):
            expression = re.sub(r'\s+', '', expression)
            vanilla = re.fullmatch(r'Blocks\.([A-Z0-9_]+)', expression)
            mod_block = re.fullmatch(r'YvanchuxiuzhenModBlocks\.(\w+)\.get\(\)', expression)
            if vanilla:
                additions.add(f'minecraft:{vanilla[1].lower()}')
            elif mod_block:
                path_name = block_ids.get(mod_block[1])
                if path_name is None:
                    raise ValueError(f'来源 Fixer 引用未注册方块：{expression}')
                additions.add(f'{MOD}:{path_name}')
            else:
                raise ValueError(f'来源 Fixer 出现未识别的可替换方块表达式：{path.name}: {expression}')
    if len(fixers) != FIXER_CLASSES:
        raise ValueError(f'来源 carver Fixer 数量变化：expected={FIXER_CLASSES}, actual={len(fixers)}')
    members = sorted(set(defaults) | additions)
    if len(members) != EXPECTED_MEMBERS:
        raise ValueError(f'来源 carver 可替换成员数变化：expected={EXPECTED_MEMBERS}, actual={len(members)}')
    return members, [source_jar, blocks_file, *fixers]


def plan(source_root: Path | None = None, source_jar: Path | None = None):
    members, origins = source_members(source_root, source_jar)
    return [(DATA / 'minecraft/tags/block/overworld_carver_replaceables.json',
        {'replace': True, 'values': entries(members)}, [(MOD, path) for path in origins])]
