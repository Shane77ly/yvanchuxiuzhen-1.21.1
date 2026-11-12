"""从 1.18.2 文王鼓与赶仙鞭的模型类提取几何、动画和纹理引用，登记到系统资源表。

仅登记资源引用；后续由 convert_assets.py 保持原文件内容迁移模型、动画与纹理。
"""
from __future__ import annotations

import csv
import io
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

MOD = 'yvanchuxiuzhen'
SOURCE = SOURCES[MOD][0]
MODELS = SOURCE / f'src/main/java/net/mcreator/{MOD}/item/model'
RESOURCES = SOURCE / f'src/main/resources/assets/{MOD}'
ASSETS = ROOT / 'docs/reference/conversion/system_assets.csv'


def resources(name: str) -> list[list[str]]:
    model = (MODELS / f'{name}ItemModel.java').read_text(encoding='utf-8-sig')
    rows = []
    for kind, folder, suffix in (('geo', 'geo', '.geo.json'), ('animation', 'animations', '.animation.json'),
                                 ('texture', 'textures', '.png')):
        path = re.search(rf'"{folder}/([^"\n]+){re.escape(suffix)}"', model)[1]
        source = RESOURCES / folder / f'{path}{suffix}'
        if not source.is_file():
            raise FileNotFoundError(source)
        rows.append(['equipment', kind, f'{NAMESPACE}:{path}'])
    return rows


def main() -> None:
    rows = [row for name in ('Fabao021', 'Fabao022') for row in resources(name)]
    with ASSETS.open(encoding='utf-8', newline='') as handle:
        existing = list(csv.reader(handle))
    seen = set()
    unique = []
    for row in existing + rows:
        if tuple(row) not in seen:
            seen.add(tuple(row))
            unique.append(row)
    out = io.StringIO(newline='')
    csv.writer(out, lineterminator='\n').writerows(unique)
    ASSETS.write_text(out.getvalue(), encoding='utf-8', newline='')
    print('animated_fabao', len(rows) // 3, 'resources', len(rows))


if __name__ == '__main__':
    main()
