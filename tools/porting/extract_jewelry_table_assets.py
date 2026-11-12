"""从 1.18.2 饰品制作台的模型类与屏幕提取资源引用，登记到系统资源表。

本工具只登记 Geo 方块模型、动画、纹理与屏幕实际使用的贴图；后续由 convert_assets.py
迁移文件。方块模型父链和二维物品图标仍由内容表对应的普通方块、物品资源转换处理。
"""
from __future__ import annotations

import csv
import io
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

MOD = 'yvanchuxiuzhen'
SOURCE = SOURCES[MOD][0]
JAVA = SOURCE / f'src/main/java/net/mcreator/{MOD}'
RESOURCES = SOURCE / f'src/main/resources/assets/{MOD}'
ASSETS = ROOT / 'docs/reference/conversion/system_assets.csv'


def resources() -> list[list[str]]:
    model = (JAVA / 'block/model/ShipintaiBlockModel.java').read_text(encoding='utf-8-sig')
    screen = (JAVA / 'client/gui/GUIshipintaiScreen.java').read_text(encoding='utf-8-sig')
    paths = re.findall(r'new ResourceLocation\("yvanchuxiuzhen", "([^"\n]+)"\)', model)
    paths.extend(re.findall(r'new ResourceLocation\("yvanchuxiuzhen:(textures/[^"\n]+\.png)"\)', screen))
    rows = []
    for path in dict.fromkeys(paths):
        source = RESOURCES / path
        if not source.is_file():
            raise FileNotFoundError(source)
        folder, name = path.split('/', 1)
        kind, suffix = {'geo': ('geo', '.geo.json'), 'animations': ('animation', '.animation.json'),
                        'textures': ('texture', '.png')}[folder]
        rows.append(['equipment', kind, f'{NAMESPACE}:{name.removesuffix(suffix)}'])
    return rows


def main() -> None:
    rows = resources()
    with ASSETS.open(encoding='utf-8', newline='') as handle:
        existing = list(csv.reader(handle))
    seen = set()
    merged = []
    for row in existing + rows:
        if tuple(row) not in seen:
            seen.add(tuple(row))
            merged.append(row)
    out = io.StringIO(newline='')
    csv.writer(out, lineterminator='\n').writerows(merged)
    ASSETS.write_text(out.getvalue(), encoding='utf-8', newline='')
    print('jewelry_table_resources', len(rows))


if __name__ == '__main__':
    main()
