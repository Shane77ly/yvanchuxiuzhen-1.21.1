"""将已成功导出的1.18.2原版注册闭包固定为可复现的转换输入；不执行Java。"""
from __future__ import annotations

import argparse
import hashlib
import json
from collections import Counter
from pathlib import Path

from audit_vanilla_biome_graph import Catalog, SNAPSHOT
from convert_content import ROOT


def capture(plan: dict, dump_root: Path) -> dict:
    provenance = json.loads((dump_root / 'provenance.json').read_text(encoding='utf-8'))
    if provenance.get('executed') is not True or provenance.get('exit_code') != 0:
        raise ValueError('只接受已真实成功运行的来源原版注册导出')
    source = Catalog(True, Path(plan['source_root']), dump_root / 'data')
    try:
        for row in plan['vanilla_references']:
            source.holder(row['registry'], row['id'])
        registries = {}
        for key in sorted(source.nodes):
            kind, identifier = key.split('/', 1)
            registries.setdefault(kind, {})[identifier] = source.read(kind, identifier)
        block_tags = {identifier: source.raw_tag(identifier) for identifier in sorted(source.tags)}
        # 保存原始节点和标签；不得把比较用的展平图反写为来源配置。
        payload = {'registries': registries, 'block_tags': block_tags}
        return {'minecraft_version': '1.18.2',
            'scope': '41个来源维度群系引用的55项原版注册及其配置、放置和方块标签闭包；不含Forge或Mod扩展。',
            'provenance': {'artifacts': [{'name': Path(row['path']).name, 'sha256': row['sha256']}
                for row in provenance['artifacts'] + provenance['mapping_artifacts']],
                'dump_launcher_sha256': hashlib.sha256((ROOT / 'tools/porting/java/LegacyWorldgenDump.java').read_bytes()).hexdigest(),
                'payload_sha256': hashlib.sha256(json.dumps(payload, sort_keys=True).encode()).hexdigest()},
            'roots': [{key: row[key] for key in ('registry', 'id')} for row in plan['vanilla_references']],
            **payload}
    finally:
        source.close()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan', type=Path, default=ROOT / 'tmp/source-biomes-plan.json')
    parser.add_argument('--dump-root', type=Path, default=ROOT / 'tmp/legacy-vanilla-worldgen')
    parser.add_argument('--output', type=Path, default=SNAPSHOT)
    args = parser.parse_args()
    result = capture(json.loads(args.plan.read_text(encoding='utf-8')), args.dump_root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'roots': len(result['roots']),
        'nodes': {key: len(value) for key, value in result['registries'].items()},
        'block_tags': len(result['block_tags'])}, ensure_ascii=False))


if __name__ == '__main__':
    main()
