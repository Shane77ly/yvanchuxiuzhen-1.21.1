"""从锁定1.18.2缓存准备独立原版注册表导出；仅--run启动Java，输出限于tmp。"""
from __future__ import annotations

import argparse
import csv
import hashlib
import io
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES


def mapped_launcher(source_root: Path, output: Path, mapped_jar: Path) -> tuple[Path, list[dict]]:
    """锁定缓存rename阶段只有类名映射；按该阶段TSRG与官方CSV明确转换成员名。"""
    mappings = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/mergeMappings/output.jar'))
    if len(mappings) != 1:
        raise ValueError('来源rename阶段TSRG必须唯一')
    gradle_cache = Path(os.environ.get('GRADLE_USER_HOME', Path.home() / '.gradle'))
    official = gradle_cache / 'caches/forge_gradle/mcp_repo/net/minecraft/mapping/1.18.2/mapping-1.18.2-mapping.zip'
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

    def member(class_name: str, name: str) -> str:
        prefix = 'f_' if name.isupper() else 'm_'
        selected = [srg for srg, official_name in classes[class_name] if official_name == name and srg.startswith(prefix)]
        if len(selected) != 1:
            raise ValueError(f'来源成员映射必须唯一：{class_name}.{name} -> {selected}')
        with zipfile.ZipFile(mapped_jar) as archive:
            if selected[0].encode() not in archive.read(class_name.replace('.', '/') + '.class'):
                raise ValueError(f'映射成员不在实际class中：{class_name}.{selected[0]}')
        return selected[0]

    text = (ROOT / 'tools/porting/java/LegacyWorldgenDump.java').read_text(encoding='utf-8')
    replacements = {
        'SharedConstants.tryDetectVersion': ('net.minecraft.SharedConstants', 'tryDetectVersion'),
        'Bootstrap.bootStrap': ('net.minecraft.server.Bootstrap', 'bootStrap'),
        'RegistryAccess.BUILTIN': ('net.minecraft.core.RegistryAccess', 'BUILTIN'),
        'RegistryOps.create': ('net.minecraft.resources.RegistryOps', 'create'),
        'registries.ownedRegistryOrThrow': ('net.minecraft.core.RegistryAccess', 'ownedRegistryOrThrow'),
        'Registry.CONFIGURED_FEATURE_REGISTRY': ('net.minecraft.core.Registry', 'CONFIGURED_FEATURE_REGISTRY'),
        'Registry.PLACED_FEATURE_REGISTRY': ('net.minecraft.core.Registry', 'PLACED_FEATURE_REGISTRY'),
        'Registry.CONFIGURED_CARVER_REGISTRY': ('net.minecraft.core.Registry', 'CONFIGURED_CARVER_REGISTRY'),
        'ConfiguredFeature.DIRECT_CODEC': ('net.minecraft.world.level.levelgen.feature.ConfiguredFeature', 'DIRECT_CODEC'),
        'PlacedFeature.DIRECT_CODEC': ('net.minecraft.world.level.levelgen.placement.PlacedFeature', 'DIRECT_CODEC'),
        'ConfiguredWorldCarver.DIRECT_CODEC': ('net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver', 'DIRECT_CODEC'),
        'registry.entrySet': ('net.minecraft.core.Registry', 'entrySet'),
        '.location(': ('net.minecraft.resources.ResourceKey', 'location'),
        '.getNamespace(': ('net.minecraft.resources.ResourceLocation', 'getNamespace'),
        '.getPath(': ('net.minecraft.resources.ResourceLocation', 'getPath'),
    }
    for before, (class_name, name) in replacements.items():
        replacement = member(class_name, name)
        if before.startswith('.'):
            after = '.' + replacement + '('
        else:
            after = before.rsplit('.', 1)[0] + '.' + replacement
        text = text.replace(before, after)
    target = output / 'LegacyWorldgenDump.java'
    target.write_text(text, encoding='utf-8')
    return target, [{'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
                    for path in [mappings[0], official]]


def prepare(source_root: Path, output: Path) -> dict:
    output = output.resolve()
    if not output.is_relative_to((ROOT / 'tmp').resolve()):
        raise ValueError('独立来源数据导出必须位于项目tmp目录内')
    cache = source_root / 'build/fg_cache'
    servers = list(cache.glob('mcp/*/joined/*/downloadServer/server.jar'))
    mapped = list(cache.glob('mcp/*/joined/*/rename/output.jar'))
    clients = list(cache.glob('mcp/*/joined/*/downloadClient/client.jar'))
    if len(servers) != 1 or len(mapped) != 1 or len(clients) != 1:
        raise ValueError(f'锁定1.18.2缓存必须唯一：server={servers}, mapped={mapped}, client={clients}')
    libraries = output / 'libraries'
    libraries.mkdir(parents=True, exist_ok=True)
    dependencies = []
    with zipfile.ZipFile(servers[0]) as archive:
        for line in archive.read('META-INF/libraries.list').decode('utf-8').splitlines():
            expected_hash, coordinate, path = line.split('\t')
            blob = archive.read('META-INF/libraries/' + path)
            actual_hash = hashlib.sha256(blob).hexdigest()
            if actual_hash != expected_hash:
                raise ValueError(f'来源服务端依赖SHA256不符：{coordinate}')
            target = libraries / Path(path).name
            target.write_bytes(blob)
            dependencies.append({'coordinate': coordinate, 'sha256': actual_hash, 'path': str(target)})
    java = shutil.which('java')
    if java is None:
        raise ValueError('缺少java可执行文件')
    launcher, mapping_artifacts = mapped_launcher(source_root, output, mapped[0])
    classpath = os.pathsep.join(str(path.resolve()) for path in
        [mapped[0], clients[0], *(Path(row['path']) for row in dependencies)])
    command = [java, '-Djava.awt.headless=true', '-cp', classpath,
        str(launcher), str(output / 'data')]
    report = {'source_root': str(source_root), 'scope': '锁定缓存中的1.18.2原版Minecraft注册数据，不含来源Mod或Forge扩展',
        'artifacts': [{'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}
                      for path in [servers[0], mapped[0], clients[0]]],
        'mapping_artifacts': mapping_artifacts, 'dependencies': dependencies, 'command': command,
        'output': str(output / 'data'), 'executed': False,
        'limitations': ['导出数据之后仍须核对来源Forge补丁、Mod修改、目标配置图与实际算法，不能仅按JSON相同宣布对齐。']}
    (output / 'provenance.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return report


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-root', type=Path, default=SOURCES['yvanchuxiuzhen'][0])
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/legacy-vanilla-worldgen')
    parser.add_argument('--run', action='store_true', help='在串行窗口启动独立Java数据导出；不是Gradle或游戏')
    args = parser.parse_args()
    report = prepare(args.source_root, args.output)
    if args.run:
        result = subprocess.run(report['command'], cwd=args.output, capture_output=True, text=True,
            encoding='utf-8', errors='replace', check=False)
        (args.output / 'stdout.log').write_text(result.stdout, encoding='utf-8')
        (args.output / 'stderr.log').write_text(result.stderr, encoding='utf-8')
        report.update(executed=True, exit_code=result.returncode)
        (args.output / 'provenance.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        if result.returncode:
            raise RuntimeError(f'1.18.2注册表导出失败：exit={result.returncode}；详见{args.output}/stderr.log')
        print(result.stdout[-3000:])
    else:
        print('PREPARED', len(report['dependencies']), 'libraries; Java not started')


if __name__ == '__main__':
    main()
