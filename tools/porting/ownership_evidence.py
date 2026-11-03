"""为归属阅读汇总每个编辑器元素的来源连接，输出 build/ownership/。

只列出“该读什么”：元素自身的触发过程及其调用闭包、引用该元素的其他代码、引用它的数据文件与语言文本。
归属由阅读这些来源后按系统设计“归属判定”写入归属表，本工具不判定归属。
"""
from __future__ import annotations

import argparse
import csv
import json
import re
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
INVENTORY = ROOT / 'docs/reference/legacy/inventory'
SYSTEMS = ROOT / 'docs/systems'
OUTPUT = ROOT / 'build/ownership'
SOURCES = {
    'yvanchuxiuzhen': 'reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen',
    'yvanchuxitong': 'reference/1.18.2yvanchuxitong',
}
PREFIX = {'yvanchuxiuzhen': 'YvanchuxiuzhenMod', 'yvanchuxitong': 'YvanchuxitongMod'}
CONSTANT = re.compile(r'\b(Yvanchuxiuzhen|Yvanchuxitong)Mod(Items|Blocks)\.([A-Z0-9_]+)\b')
STRING_ID = re.compile(r'\b(yvanchuxiuzhen|yvanchuxitong):([a-z0-9_]+)\b')
# 只做登记的生成文件：其中的引用是注册本身，不是玩法连接。
REGISTRY_FILES = re.compile(r'/init/')
DATA_KINDS = ('recipes', 'tags', 'loot_tables', 'advancements', 'functions')


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding='utf-8', newline='') as handle:
        return list(csv.DictReader(handle))


def ledger_rows() -> list[dict[str, str]]:
    rows = []
    for path in sorted(SYSTEMS.glob('*/ledger/content.csv')):
        rows += read_csv(path)
    return rows


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-workspace', type=Path,
                        default=ROOT.parent / 'yuanchuxiuzhen-1.21.1')
    parser.add_argument('--registries', default='block,item')
    args = parser.parse_args()
    registries = set(args.registries.split(','))

    ownership = {(r['source_mod'], r['element']): r for r in read_csv(INVENTORY / 'ownership_assignment.csv')}
    element_of_file: dict[tuple[str, str], str] = {}
    for row in read_csv(INVENTORY / 'element_files.csv'):
        element_of_file[(row['source_mod'], row['generated_file'])] = row['element']

    symbol_id: dict[tuple[str, str, str], str] = {}
    for row in read_csv(INVENTORY / 'registrations.csv'):
        if row['symbol'] and row['registry'] in ('item', 'block'):
            symbol_id[(row['source_mod'], row['registry'], row['symbol'])] = row['legacy_id']

    element_ids: dict[tuple[str, str], set[str]] = defaultdict(set)
    id_element: dict[str, tuple[str, str]] = {}
    for row in ledger_rows():
        if row['element'] and row['registry'] in registries:
            key = (row['source_mod'], row['element'])
            element_ids[key].add(row['registry'] + ' ' + row['legacy_id'])
            id_element[row['legacy_id']] = key

    triggers: dict[tuple[str, str], list[tuple[str, str]]] = defaultdict(list)
    for row in read_csv(INVENTORY / 'element_triggers.csv'):
        name = json.loads(row['value']).get('name') if row['value'].startswith('{') else None
        if name:
            triggers[(row['source_mod'], row['element'])].append((row['property'], name))

    calls: dict[tuple[str, str], set[str]] = defaultdict(set)
    for row in read_csv(INVENTORY / 'procedure_calls.csv'):
        if row['kind'] == 'procedure_call':
            calls[(row['source_mod'], row['caller_class'])].add(row['target_mod'] + ':' + row['target_class'])

    def closure(mod: str, procedure: str) -> list[str]:
        seen, stack = [], [f'{mod}:{procedure}Procedure']
        while stack:
            current = stack.pop()
            if current in seen:
                continue
            seen.append(current)
            current_mod, cls = current.split(':', 1)
            stack += sorted(calls.get((current_mod, cls), ()))
        return seen

    def referrer(mod: str, relative: str) -> str:
        element = element_of_file.get((mod, relative))
        if element:
            owner = ownership.get((mod, element), {}).get('owner', '?')
            return f'{mod}:{element} [{owner}]'
        return f'{mod}:{Path(relative).stem} [class]'

    code_refs: dict[str, set[str]] = defaultdict(set)
    data_refs: dict[str, set[str]] = defaultdict(set)
    for mod, relative_root in SOURCES.items():
        root = args.source_workspace / relative_root
        for path in (root / 'src/main/java').rglob('*.java'):
            relative = path.relative_to(root).as_posix()
            if REGISTRY_FILES.search(relative):
                continue
            text = path.read_text(encoding='utf-8-sig')
            found = set()
            for match in CONSTANT.finditer(text):
                target_mod = 'yvanchuxiuzhen' if match[1] == 'Yvanchuxiuzhen' else 'yvanchuxitong'
                legacy = symbol_id.get((target_mod, match[2].lower().rstrip('s'), match[3]))
                if legacy:
                    found.add(legacy)
            for match in STRING_ID.finditer(text):
                found.add(f'{match[1]}:{match[2]}')
            who = referrer(mod, relative)
            for legacy in found:
                code_refs[legacy].add(who)
        resources = root / 'src/main/resources'
        for path in resources.rglob('*'):
            if not path.is_file() or path.suffix not in ('.json', '.mcfunction'):
                continue
            parts = path.relative_to(resources).parts
            if parts[0] == 'data' and len(parts) > 2 and parts[2] in DATA_KINDS:
                label = f'{parts[2]}/{"/".join(parts[3:])}'
            elif parts[0] == 'assets' and len(parts) > 2 and parts[2] == 'lang':
                label = f'lang/{parts[-1]}'
            else:
                continue
            text = path.read_text(encoding='utf-8-sig')
            for match in STRING_ID.finditer(text):
                data_refs[f'{match[1]}:{match[2]}'].add(f'{mod}:{label}')

    OUTPUT.mkdir(parents=True, exist_ok=True)
    by_owner: dict[str, list[dict]] = defaultdict(list)
    for key in sorted(element_ids):
        mod, element = key
        assignment = ownership[key]
        ids = sorted(element_ids[key])
        legacy_ids = [entry.split(' ', 1)[1] for entry in ids]
        own_files = {f'{mod}:{element} [{assignment["owner"]}]'}
        record = {
            'element': f'{mod}:{element}', 'type': assignment['type'], 'folder': assignment['folder'],
            'owner': assignment['owner'], 'status': assignment['status'], 'ids': ids,
            'triggers': [f'{prop}={name}' for prop, name in triggers.get(key, [])],
            'procedures': sorted({p for _, name in triggers.get(key, []) for p in closure(mod, name)}),
            'code_refs': sorted(set().union(*(code_refs.get(i, set()) for i in legacy_ids)) - own_files),
            'data_refs': sorted(set().union(*(data_refs.get(i, set()) for i in legacy_ids))),
        }
        by_owner[assignment['owner']].append(record)
    for owner, records in by_owner.items():
        with (OUTPUT / f'{owner}.jsonl').open('w', encoding='utf-8', newline='\n') as handle:
            for record in records:
                handle.write(json.dumps(record, ensure_ascii=False) + '\n')
    total = sum(len(v) for v in by_owner.values())
    print(f'{total} 个元素，{len(by_owner)} 个候选单位，输出 {OUTPUT.relative_to(ROOT).as_posix()}')


if __name__ == '__main__':
    main()
