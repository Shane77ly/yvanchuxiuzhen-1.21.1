"""把已读完的元素在归属表中标为“已读”，并写入归属系统与阅读结论（C 批次各系统的归属阅读用）。

用法：`python -B -X utf8 tools/porting/mark_ownership.py <系统> "<结论>" <元素名>...`
元素名取归属表的 element 列（不含 `Procedure` 后缀），两个来源 Mod 中同名元素一并处理；前缀 `t:` 只处理原初系统。
归属系统与表中现有的不同时（改判）须设置环境变量 `REASSIGN=1`，否则报错，避免误改同名元素；结论写入 evidence 列。运行后需执行 `build_ledger.py` 与 `build_architecture_catalog.py`。
"""
from __future__ import annotations

import csv
import io
import os
import sys

from build_ledger import ROOT

TABLE = ROOT / 'docs/reference/legacy/inventory/ownership_assignment.csv'


def main() -> None:
    owner, evidence, *names = sys.argv[1:]
    text = TABLE.read_text(encoding='utf-8')
    rows = list(csv.reader(io.StringIO(text, newline='')))
    header, body = rows[0], rows[1:]
    index = {name: header.index(name) for name in ('source_mod', 'element', 'owner', 'status', 'evidence')}
    wanted = {}
    for name in names:
        mod, element = ('yvanchuxitong', name[2:]) if name.startswith('t:') else (None, name)
        wanted[element] = mod
    marked = set()
    for row in body:
        element = row[index['element']]
        if element in wanted and wanted[element] in (None, row[index['source_mod']]):
            if row[index['owner']] != owner and not os.environ.get('REASSIGN'):
                raise SystemExit(f'{element} 现归 {row[index["owner"]]}，改判需设置 REASSIGN=1')
            row[index['owner']] = owner
            row[index['status']] = '已读'
            row[index['evidence']] = evidence
            marked.add(element)
    missing = set(wanted) - marked
    if missing:
        raise SystemExit(f'归属表中没有这些元素：{sorted(missing)}')
    out = io.StringIO(newline='')
    csv.writer(out, lineterminator='\n').writerows(rows)
    TABLE.write_text(out.getvalue(), encoding='utf-8', newline='')
    print(f'已标记 {len(marked)} 个元素为 {owner} 已读')


if __name__ == '__main__':
    main()
