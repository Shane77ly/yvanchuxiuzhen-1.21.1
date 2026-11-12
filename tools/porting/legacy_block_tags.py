"""保留来源方块标签身份，对尚未移植的成员显式声明可选依赖。"""
from __future__ import annotations

import json

from build_ledger import read_ledgers
from convert_content import CONTENT


def entries(identifiers: list[str]) -> list[str | dict]:
    registered = {f'yvanchuxiuzhen:{row["id"]}'
        for path in CONTENT.glob('*/blocks.json')
        for row in json.loads(path.read_text(encoding='utf-8'))}
    registered.update(row['target_id'] for rows in read_ledgers().values() for row in rows
        if row['registry'] == 'block' and row['status'] == '行为已迁移')
    return [{'id': identifier, 'required': False}
        if identifier.startswith('yvanchuxiuzhen:') and identifier not in registered else identifier
        for identifier in identifiers]


def ids(values: list[str | dict]) -> list[str]:
    return [value if isinstance(value, str) else value['id'] for value in values]
