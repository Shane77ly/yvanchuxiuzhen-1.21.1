"""由来源注册与归属表同步各单位的内容台账 docs/systems/<单位>/ledger/content.csv。

每条来源注册一行。来源列（注册位置、编辑器元素、归属）每次按 registrations.csv、element_files.csv 与
ownership_assignment.csv 重写；人工列（target_id、status、note）按 (registry, legacy_id) 保留。
归属改变时行移到新单位的台账。没有编辑器元素的注册（声音事件等）放在 project 台账，归属为“待读”。
"""
from __future__ import annotations

import csv
import io
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
INVENTORY = ROOT / 'docs/reference/legacy/inventory'
SYSTEMS = ROOT / 'docs/systems'
UNASSIGNED = 'project'
COLUMNS = ['registry', 'legacy_id', 'source_mod', 'element', 'source_file', 'line',
           'owner', 'ownership', 'target_id', 'status', 'note']
MANUAL = ('target_id', 'status', 'note')
STATUSES = ('待处理', '已注册', '行为已迁移', '已验收', '批准调整')
# MCreator 由一个元素派生出的注册：弹体、流体的流动态、刷怪蛋、维度传送门、流体桶、盔甲四件。
DERIVED_PREFIXES = ('projectile_', 'flowing_')
DERIVED_SUFFIXES = ('_spawn_egg', '_portal', '_bucket', '_helmet', '_chestplate', '_leggings', '_boots')


def read_csv(path: Path) -> list[dict[str, str]]:
    with path.open(encoding='utf-8', newline='') as handle:
        return list(csv.DictReader(handle))


def element_index() -> tuple[dict[tuple[str, str], set[str]], dict[tuple[str, str], str]]:
    by_registry_name: dict[tuple[str, str], set[str]] = defaultdict(set)
    by_lower_name: dict[tuple[str, str], str] = {}
    for row in read_csv(INVENTORY / 'element_files.csv'):
        if row['registry_name']:
            by_registry_name[(row['source_mod'], row['registry_name'])].add(row['element'])
        by_lower_name[(row['source_mod'], row['element'].lower())] = row['element']
    return by_registry_name, by_lower_name


def source_element(registration: dict[str, str], by_registry_name, by_lower_name) -> str:
    """返回声明该注册的编辑器元素名；没有元素时返回空串。一个注册对应多个元素时报错。

    声音事件由工作区声音清单注册，不来自编辑器元素；与某个元素同名不说明归属。
    """
    if registration['registry'] == 'sound_event':
        return ''
    mod = registration['source_mod']
    path = registration['legacy_id'].split(':', 1)[1]
    candidates = [path]
    candidates += [path.removeprefix(prefix) for prefix in DERIVED_PREFIXES if path.startswith(prefix)]
    candidates += [path.removesuffix(suffix) for suffix in DERIVED_SUFFIXES if path.endswith(suffix)]
    for candidate in candidates:
        elements = by_registry_name.get((mod, candidate))
        if elements:
            if len(elements) > 1:
                raise ValueError(f'{registration["legacy_id"]} 对应多个元素：{sorted(elements)}')
            return next(iter(elements))
    return by_lower_name.get((mod, path.replace('_', '')), '')


def expected_rows() -> dict[tuple[str, str], dict[str, str]]:
    """按来源与归属表计算每条注册的来源列。"""
    assignment = {(r['source_mod'], r['element']): r for r in read_csv(INVENTORY / 'ownership_assignment.csv')}
    by_registry_name, by_lower_name = element_index()
    rows = {}
    for registration in read_csv(INVENTORY / 'registrations.csv'):
        element = source_element(registration, by_registry_name, by_lower_name)
        assigned = assignment[(registration['source_mod'], element)] if element else None
        key = (registration['registry'], registration['legacy_id'])
        if key in rows:
            raise ValueError(f'来源注册重复：{key}')
        rows[key] = {
            'registry': registration['registry'],
            'legacy_id': registration['legacy_id'],
            'source_mod': registration['source_mod'],
            'element': element,
            'source_file': registration['source_file'],
            'line': registration['line'],
            'owner': assigned['owner'] if assigned else '',
            'ownership': assigned['status'] if assigned else '待读',
        }
    return rows


def ledger_dir(row: dict[str, str]) -> str:
    return row['owner'] or UNASSIGNED


def read_ledgers() -> dict[Path, list[dict[str, str]]]:
    return {path: read_csv(path) for path in sorted(SYSTEMS.glob('*/ledger/content.csv'))}


def csv_text(rows: list[dict[str, str]]) -> str:
    buffer = io.StringIO()
    writer = csv.DictWriter(buffer, fieldnames=COLUMNS, lineterminator='\n')
    writer.writeheader()
    writer.writerows(rows)
    return buffer.getvalue()


def main() -> None:
    expected = expected_rows()
    manual = {}
    for path, rows in read_ledgers().items():
        for row in rows:
            key = (row['registry'], row['legacy_id'])
            if key not in expected:
                raise ValueError(f'{path} 的行不在来源注册中：{key}')
            manual[key] = {column: row[column] for column in MANUAL}
    grouped: dict[str, list[dict[str, str]]] = defaultdict(list)
    for key, row in sorted(expected.items()):
        row.update(manual.get(key, {'target_id': row['legacy_id'], 'status': STATUSES[0], 'note': ''}))
        grouped[ledger_dir(row)].append(row)
    for path in SYSTEMS.glob('*/ledger/content.csv'):
        if path.parent.parent.name not in grouped:
            path.unlink()
    for unit, rows in grouped.items():
        directory = SYSTEMS / unit
        if not directory.is_dir():
            raise ValueError(f'单位 {unit} 没有文档目录 {directory}')
        (directory / 'ledger').mkdir(exist_ok=True)
        (directory / 'ledger/content.csv').write_text(csv_text(rows), encoding='utf-8', newline='\n')
    print({unit: len(rows) for unit, rows in sorted(grouped.items(), key=lambda item: -len(item[1]))})


if __name__ == '__main__':
    main()
