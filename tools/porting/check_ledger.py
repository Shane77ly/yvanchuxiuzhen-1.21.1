"""检查内容台账，规则见 docs/development/engineering.md“台账与检查工具”。发现问题时以退出码 1 结束。

检查项：列结构；每条来源注册恰好一行；行所在目录与归属一致；来源列与 build_ledger.py 的计算一致；
目标 ID 使用唯一命名空间且在同一注册表内不重复；状态只取约定值；状态高于“待处理”的行归属必须为“已读”；
状态高于“待处理”的方块与物品行必须出现在开发服务端的注册表导出中，导出里的每个 ID 都必须是台账的目标 ID。
导出由 `gradlew runServer` 写入 build/registry-export。
"""
from __future__ import annotations

import sys
from collections import Counter
from pathlib import Path

from build_ledger import (COLUMNS, MANUAL, NAMESPACE, ROOT, STATUSES, UNASSIGNED, expected_rows, ledger_dir,
                          read_ledgers)

EXPORT = ROOT / 'build/registry-export'
EXPORTED_REGISTRIES = ('block', 'item')
SOURCE_COLUMNS = [column for column in COLUMNS if column not in MANUAL]


def check_export(targets: Counter, status_by_target: dict) -> list[str]:
    """比对开发服务端导出的注册表与台账。"""
    registered = {key for key, status in status_by_target.items() if status != STATUSES[0] and key[0] in EXPORTED_REGISTRIES}
    if not EXPORT.is_dir():
        return [f'台账有 {len(registered)} 行状态高于“待处理”，但没有注册表导出 {EXPORT.relative_to(ROOT).as_posix()}，先运行 gradlew runServer'] if registered else []
    issues = []
    for registry in EXPORTED_REGISTRIES:
        exported = set(Path(EXPORT / f'{registry}.txt').read_text(encoding='utf-8').split())
        known = {target for (kind, target) in targets if kind == registry}
        issues += [f'导出的 {registry} {id_} 不是台账的目标 ID' for id_ in sorted(exported - known)]
        issues += [f'台账 {registry} {id_} 状态高于“待处理”但不在注册表导出中' for kind, id_ in sorted(registered) if kind == registry and id_ not in exported]
        print(f'注册表导出 {registry}：{len(exported)} 个')
    return issues


def check() -> list[str]:
    expected = expected_rows()
    issues = []
    seen = Counter()
    targets = Counter()
    status_by_target = {}
    status_counts = Counter()
    for path, rows in read_ledgers().items():
        unit = path.parent.parent.name
        if rows and list(rows[0]) != COLUMNS:
            issues.append(f'{path}: 列应为 {",".join(COLUMNS)}')
            continue
        for number, row in enumerate(rows, start=2):
            where = f'{path.relative_to(ROOT).as_posix()}:{number}'
            key = (row['registry'], row['legacy_id'])
            seen[key] += 1
            targets[(row['registry'], row['target_id'])] += 1
            status_by_target[(row['registry'], row['target_id'])] = row['status']
            status_counts[row['status']] += 1
            if key not in expected:
                issues.append(f'{where}: 不是来源注册 {key}')
                continue
            if not row['target_id'].startswith(NAMESPACE + ':'):
                issues.append(f'{where}: 目标 ID {row["target_id"]} 不在命名空间 {NAMESPACE} 下')
            if row['status'] not in STATUSES:
                issues.append(f'{where}: 状态 {row["status"]} 不在 {"/".join(STATUSES)} 中')
            if row['status'] != STATUSES[0] and row['ownership'] != '已读':
                issues.append(f'{where}: 状态为 {row["status"]} 但归属是 {row["ownership"]}，应先按归属判定读规则')
            stale = [column for column in SOURCE_COLUMNS if row[column] != expected[key][column]]
            if stale:
                issues.append(f'{where}: 来源列 {",".join(stale)} 与归属表不一致，运行 build_ledger.py 同步')
            if unit != ledger_dir(expected[key]):
                issues.append(f'{where}: 应在 {ledger_dir(expected[key])} 的台账（{UNASSIGNED} 只放没有归属的注册）')
    issues += check_export(targets, status_by_target)
    issues += [f'缺少来源注册 {key}' for key in expected if key not in seen]
    issues += [f'来源注册重复 {count} 行：{key}' for key, count in seen.items() if count > 1]
    issues += [f'目标 ID 重复 {count} 行：{key}，在 target_id 中改名并在 note 说明' for key, count in targets.items() if count > 1]
    print(f'台账 {sum(seen.values())} 行，来源注册 {len(expected)} 条，状态 {dict(status_counts)}')
    return issues


def main() -> int:
    issues = check()
    for issue in issues:
        print(issue)
    print(f'发现 {len(issues)} 项问题。')
    return 1 if issues else 0


if __name__ == '__main__':
    sys.exit(main())
