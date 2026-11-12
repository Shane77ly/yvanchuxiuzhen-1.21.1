"""核对 36 种公共书的来源研习入口，登记共用物品工厂并更新书籍内容表。

只改变 book_001–book_036 的工厂；其他属性、未迁移钩子和装订台方块物品保留。
统一 convert_content 可依据 factory_overrides 重建相同的工厂接线。
"""
from __future__ import annotations

import csv
import json
import re

from build_ledger import NAMESPACE
from convert_content import CONTENT, CONVERSION, SOURCES, write_rows

MOD = 'yvanchuxiuzhen'
FACTORY = 'book:study_book'
REASON = '公共书载体潜行研习分派与答题计数重置（StudyBookItem）'


def study_books() -> tuple[str, ...]:
    root, prefix = SOURCES[MOD]
    java = root / f'src/main/java/net/mcreator/{MOD}'
    registrations = (java / f'init/{prefix}ModItems.java').read_text(encoding='utf-8')
    books = re.findall(r'REGISTRY\.register\("(book_\d{3})",\s*\(\)\s*->\s*new (Book\d{3}Item)\(\)\)', registrations)
    expected = {f'book_{number:03d}': f'Book{number:03d}Item' for number in range(1, 37)}
    if len(books) != len(expected) or dict(books) != expected:
        raise ValueError(f'公共书注册与锁定的 36 种载体不一致：{books}')
    for identity, class_name in books:
        text = (java / f'item/{class_name}.java').read_text(encoding='utf-8')
        if not re.search(r'BookRightClickProcedure\.execute\(world,\s*x,\s*y,\s*z,\s*entity,\s*itemstack\);', text):
            raise ValueError(f'{identity} 没有预期的公共书右键入口：{class_name}')
    return tuple(sorted(expected))


def main() -> None:
    books = study_books()
    identities = {f'{NAMESPACE}:{identity}' for identity in books}
    item_path = CONTENT / 'book/items.json'
    items = json.loads(item_path.read_text(encoding='utf-8'))
    if {row['id'] for row in items if row['id'] in books} != set(books):
        raise ValueError('书籍内容表没有包含全部 36 种公共书载体')
    for row in items:
        if row['id'] in books:
            row['factory'] = FACTORY

    override_path = CONVERSION / 'factory_overrides.csv'
    with override_path.open(encoding='utf-8', newline='') as handle:
        reader = csv.DictReader(handle)
        columns = reader.fieldnames
        overrides = [row for row in reader if not (row['registry'] == 'item' and row['target_id'] in identities)]
    overrides.extend({'registry': 'item', 'target_id': f'{NAMESPACE}:{identity}', 'factory': FACTORY,
                      'owner': 'book', 'reason': REASON} for identity in books)
    with override_path.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.DictWriter(handle, fieldnames=columns, lineterminator='\n')
        writer.writeheader()
        writer.writerows(overrides)
    write_rows(item_path, items)
    print('book_study items', len(books))


if __name__ == '__main__':
    main()
