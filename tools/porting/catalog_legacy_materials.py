"""提取锁定1.18.2原版和两个Mod的材料真值；仅--run启动独立Java并写持久快照。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import io
import json
import os
from pathlib import Path
import re
import subprocess
import zipfile

from convert_content import ROOT, SOURCES, ledger_targets
from convert_data import VANILLA_RENAMES
from dump_legacy_vanilla_worldgen import prepare as prepare_worldgen


def fingerprint(path: Path) -> dict:
    return {'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}


def source_members(source_root: Path) -> tuple[dict[str, list[tuple[str, str]]], list[dict]]:
    mappings = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/mergeMappings/output.jar'))
    if len(mappings) != 1:
        raise ValueError('锁定来源成员映射必须唯一')
    cache = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle'))
    official = cache / 'caches/forge_gradle/mcp_repo/net/minecraft/mapping/1.18.2/mapping-1.18.2-mapping.zip'
    with zipfile.ZipFile(official) as archive:
        names = {row['searge']: row['name'] for name in ('fields.csv', 'methods.csv')
                 for row in csv.DictReader(io.StringIO(archive.read(name).decode('utf-8')))}
    classes = {}
    current = None
    for line in mappings[0].read_text(encoding='utf-8').splitlines()[1:]:
        if not line.startswith('\t'):
            current = line.split()[1].replace('/', '.')
            classes[current] = []
        elif not line.startswith('\t\t'):
            columns = line.split()
            if columns and columns[-1] in names:
                classes[current].append((columns[-1], names[columns[-1]]))
    return classes, [fingerprint(path) for path in (mappings[0], official)]


def material_definitions(source_root: Path) -> tuple[dict, dict]:
    jars = list((source_root / 'build/fg_cache').glob(
        'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
    if len(jars) != 1:
        raise ValueError('来源Forge40.1.0源码包必须唯一')
    with zipfile.ZipFile(jars[0]) as archive:
        blob = archive.read('net/minecraft/world/level/material/Material.java')
    definitions = {}
    for name, expression in re.findall(r'public static final Material (\w+)\s*=\s*([^;]+);', blob.decode('utf-8')):
        if 'new Material.Builder(' not in expression or not expression.endswith('.build()'):
            raise ValueError(f'来源材料定义写法未处理：{name}')
        definitions[name] = {'solid': '.nonSolid()' not in expression, 'liquid': '.liquid()' in expression}
    if not definitions:
        raise ValueError('未读到来源材料定义')
    return definitions, {**fingerprint(jars[0]),
        'entry': 'net/minecraft/world/level/material/Material.java',
        'entry_sha256': hashlib.sha256(blob).hexdigest()}


def prepare(source_root: Path, output: Path) -> dict:
    """仅准备tmp目录中的依赖、映射后的Java源码和运行清单，不启动Java。"""
    report = prepare_worldgen(source_root, output)
    classes, mapping_proof = source_members(source_root)
    mapped_jar = Path(report['artifacts'][1]['path'])

    def member(owner: str, name: str) -> str:
        prefix = 'f_' if name.isupper() else 'm_'
        matches = [mapped for mapped, official in classes[owner] if official == name and mapped.startswith(prefix)]
        if len(matches) != 1:
            raise ValueError(f'来源成员映射必须唯一：{owner}.{name} -> {matches}')
        with zipfile.ZipFile(mapped_jar) as archive:
            if matches[0].encode() not in archive.read(owner.replace('.', '/') + '.class'):
                raise ValueError(f'实际来源class不含映射成员：{owner}.{name}')
        return matches[0]

    launcher = ROOT / 'tools/porting/java/LegacyMaterialDump.java'
    text = launcher.read_text(encoding='utf-8')
    replacements = {
        'SharedConstants.tryDetectVersion': ('net.minecraft.SharedConstants', 'tryDetectVersion'),
        'Bootstrap.bootStrap': ('net.minecraft.server.Bootstrap', 'bootStrap'),
        '.defaultBlockState(': ('net.minecraft.world.level.block.Block', 'defaultBlockState'),
        '.getMaterial(': ('net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase', 'getMaterial'),
        '.getKey(': ('net.minecraft.core.Registry', 'getKey'),
        '.isSolid(': ('net.minecraft.world.level.material.Material', 'isSolid'),
        '.isLiquid(': ('net.minecraft.world.level.material.Material', 'isLiquid'),
        'Registry.BLOCK': ('net.minecraft.core.Registry', 'BLOCK'),
    }
    for before, (owner, name) in replacements.items():
        mapped = member(owner, name)
        after = '.' + mapped + '(' if before.startswith('.') else before.rsplit('.', 1)[0] + '.' + mapped
        text = text.replace(before, after)
    target = output.resolve() / 'LegacyMaterialDump.java'
    target.write_text(text, encoding='utf-8', newline='\n')
    raw = output.resolve() / 'vanilla-materials.json'
    report.update(command=[*report['command'][:-2], str(target), str(raw)], output=str(raw),
        scope='原版运行时材料和方块身份，不加载来源Mod；Mod方块另行静态核对',
        mapping_artifacts=mapping_proof, launcher=fingerprint(launcher),
        material_fields={mapped: official for mapped, official in classes['net.minecraft.world.level.material.Material']
                         if mapped.startswith('f_') and official.isupper()})
    (output / 'provenance.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return report


def mod_blocks(definitions: dict) -> tuple[list[dict], list[dict]]:
    """完整覆盖两个来源注册；不接受未识别构造、额外材料链或材料覆写。"""
    targets = ledger_targets()
    blocks, proof = [], []
    for mod, (root, prefix) in SOURCES.items():
        registration = root / f'src/main/java/net/mcreator/{mod}/init/{prefix}ModBlocks.java'
        text = registration.read_text(encoding='utf-8')
        entries = re.findall(r'public static final RegistryObject<Block> (\w+) = REGISTRY.register\('
            r'"([^"]+)",\s*\(\) -> new (\w+)\(\)\);', text)
        all_entries = re.findall(r'REGISTRY.register\("([^"]+)"', text)
        if len(entries) != len(all_entries) or {path for _, path, _ in entries} != set(all_entries):
            raise ValueError(f'来源方块注册未完整解析：{mod}')
        proof.append(fingerprint(registration))
        parsed_paths = set()
        for _, identifier, class_name in entries:
            path = root / f'src/main/java/net/mcreator/{mod}/block/{class_name}.java'
            body = path.read_text(encoding='utf-8')
            materials = re.findall(r'(?:BlockBehaviour\.)?Properties\.of\(Material\.(\w+)\b', body)
            all_properties = re.findall(r'\bProperties\.(?:of|copy)\(', body)
            if len(materials) != 1 or len(all_properties) != 1 or re.search(r'\bgetMaterial\s*\(', body):
                raise ValueError(f'来源材料构造或覆写未处理：{path}')
            material = materials[0]
            if material not in definitions:
                raise ValueError(f'来源方块未知材料：{path} -> {material}')
            legacy_id = f'{mod}:{identifier}'
            blocks.append({'legacy_id': legacy_id, 'target_id': targets[('block', legacy_id)],
                'material': material, **definitions[material],
                'source': str(path.relative_to(root)), 'source_sha256': fingerprint(path)['sha256']})
            parsed_paths.add(path)
        all_paths = set((root / f'src/main/java/net/mcreator/{mod}/block').glob('*.java'))
        if parsed_paths != all_paths:
            raise ValueError(f'来源方块类与注册覆盖不一致：{mod}, 未注册={all_paths-parsed_paths}, 缺类={parsed_paths-all_paths}')
    return blocks, proof


def snapshot(report: dict, source_root: Path) -> dict:
    raw = json.loads(Path(report['output']).read_text(encoding='utf-8'))
    definitions, definition_proof = material_definitions(source_root)
    runtime = {report['material_fields'][name]: flags for name, flags in raw['materials'].items()}
    if runtime != definitions:
        raise ValueError('来源Material源码与运行时材料位不一致')
    rows = []
    for identifier, field in sorted(raw['blocks'].items()):
        if not identifier.startswith('minecraft:'):
            raise ValueError(f'原版运行时含非原版方块：{identifier}')
        material = report['material_fields'][field]
        rows.append({'legacy_id': identifier, 'target_id': VANILLA_RENAMES.get(identifier, identifier),
            'material': material, **definitions[material]})
    mod_rows, mod_proof = mod_blocks(definitions)
    rows.extend(mod_rows)
    rows.sort(key=lambda row: row['legacy_id'])
    target_flags = {}
    for row in rows:
        flags = (row['solid'], row['liquid'])
        if row['target_id'] in target_flags and target_flags[row['target_id']] != flags:
            raise ValueError(f'来源合并目标材料位冲突：{row["target_id"]}')
        target_flags[row['target_id']] = flags
    return {'minecraft_version': '1.18.2', 'forge_version': '40.1.0',
        'scope': '锁定原版运行时 + 两个来源Mod全部注册方块的材料位；不表示目标方块已全部实现',
        'provenance': {'runtime_artifacts': report['artifacts'], 'mapping_artifacts': report['mapping_artifacts'],
            'launcher': report['launcher'], 'raw_dump': fingerprint(Path(report['output'])),
            'material_source': definition_proof, 'mod_registrations': mod_proof},
        'materials': definitions, 'blocks': rows,
        'tag_values': {'legacy_lake_solid': sorted(target for target, flags in target_flags.items() if flags[0]),
                       'legacy_lake_liquid': sorted(target for target, flags in target_flags.items() if flags[1])},
        'counts': {'vanilla_blocks': len(raw['blocks']), 'mod_blocks': len(mod_rows),
                   'total_source_blocks': len(rows), 'unique_target_blocks': len(target_flags)}}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-root', type=Path, default=SOURCES['yvanchuxiuzhen'][0])
    parser.add_argument('--work', type=Path, default=ROOT / 'tmp/legacy-materials')
    parser.add_argument('--output', type=Path, default=ROOT / 'tools/porting/data/legacy_block_materials.json')
    parser.add_argument('--run', action='store_true', help='串行启动独立Java并写入锁定快照，不运行Gradle或游戏')
    args = parser.parse_args()
    report = prepare(args.source_root, args.work)
    if not args.run:
        print('PREPARED; Java未启动，持久快照未写入；使用--run完成提取')
        return
    result = subprocess.run(report['command'], cwd=args.work, capture_output=True, text=True,
        encoding='utf-8', errors='replace', check=False)
    (args.work / 'stdout.log').write_text(result.stdout, encoding='utf-8')
    (args.work / 'stderr.log').write_text(result.stderr, encoding='utf-8')
    report.update(executed=True, exit_code=result.returncode)
    (args.work / 'provenance.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    if result.returncode:
        raise RuntimeError(f'来源材料导出失败：exit={result.returncode}，详见{args.work}/stderr.log')
    value = snapshot(report, args.source_root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    print(json.dumps(value['counts'], ensure_ascii=False))


if __name__ == '__main__':
    main()
