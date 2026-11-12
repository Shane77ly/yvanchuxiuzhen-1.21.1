"""更新系统内容台账的迁移状态与备注（人工列 status、note，build_ledger.py 按 (registry, legacy_id) 保留）。

用法：`python -B -X utf8 tools/porting/mark_ledger.py <系统> <状态> "<备注>" <元素名>...`
状态取 待处理、已注册、行为已迁移、已验收、批准调整；元素名取台账 element 列。台账中没有的元素、归属未读的行报错。
"""
from __future__ import annotations

import csv
import io
import sys

from build_ledger import ROOT, STATUSES


def main() -> None:
    system, status, note, *names = sys.argv[1:]
    if status not in STATUSES:
        raise SystemExit(f'状态必须是 {STATUSES}')
    path = ROOT / f'docs/systems/{system}/ledger/content.csv'
    rows = list(csv.reader(io.StringIO(path.read_text(encoding='utf-8'), newline='')))
    header, body = rows[0], rows[1:]
    index = {name: header.index(name) for name in ('element', 'ownership', 'status', 'note')}
    marked = set()
    for row in body:
        if row[index['element']] in names:
            if status != STATUSES[0] and row[index['ownership']] != '已读':
                raise SystemExit(f'{row[index["element"]]} 的归属尚未读完')
            row[index['status']] = status
            if note:
                row[index['note']] = note
            marked.add(row[index['element']])
    missing = set(names) - marked
    if missing:
        raise SystemExit(f'台账中没有这些元素：{sorted(missing)}')
    out = io.StringIO(newline='')
    csv.writer(out, lineterminator='\n').writerows(rows)
    path.write_text(out.getvalue(), encoding='utf-8', newline='')
    print(f'已更新 {len(marked)} 行为 {status}')


if __name__ == '__main__':
    main()
