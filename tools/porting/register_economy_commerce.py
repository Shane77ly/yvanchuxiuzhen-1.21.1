"""登记R13经济包的共享输入；默认只预览，--apply供统一整合者应用。"""
from __future__ import annotations

import argparse
import csv
import io
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def csv_update(path: Path, additions: list[dict[str, str]], keys: tuple[str, ...]) -> str:
    with path.open(encoding='utf-8', newline='') as handle:
        reader = csv.DictReader(handle)
        columns = reader.fieldnames
        rows = list(reader)
    indexed = {tuple(row[key] for key in keys): row for row in rows}
    for row in additions:
        identity = tuple(row[key] for key in keys)
        if identity in indexed:
            previous = indexed[identity]
            if any(previous[key] != row[key] for key in row if key != 'reason'):
                raise ValueError(f'{path}: 已有登记与R13输入冲突：{identity}')
            continue
        rows.append(row)
        indexed[identity] = row
    output = io.StringIO(newline='')
    writer = csv.DictWriter(output, fieldnames=columns, lineterminator='\n')
    writer.writeheader()
    writer.writerows(rows)
    return output.getvalue()


def changes(root: Path) -> dict[Path, str]:
    result = {}
    conversion = root / 'docs/reference/conversion'
    factories = []
    for registry, identity, factory, reason in [
        ('block', 'shopblock_01', 'shop', '交易方块七槽库存、经营设置和购买入口'),
        ('block', 'paimaiblock', 'auction', '拍卖台二十一槽库存和二十刻现场状态机'),
        ('block', 'paimaichair', 'auction_chair', '拍卖椅乘坐与令牌回收入口'),
        ('block', 'tiandiyinhang', 'bank', '天地银行九槽库存、几何动画和自然对话入口'),
        ('item', 'tiandiyinhang', 'bank', '天地银行物品几何渲染'),
        ('item', 'ling_20', 'auction_token', '拍卖令牌放置、身份和回收'),
    ]:
        factories.append({'registry': registry, 'target_id': 'yvanchuxiuzhen:' + identity,
                          'factory': 'economy:' + factory, 'owner': 'economy', 'reason': reason})
    for tier in range(1, 10):
        factories.append({'registry': 'item', 'target_id': f'yvanchuxiuzhen:cailiao_{190 + tier}',
                          'factory': f'economy:spirit_stone_{tier}', 'owner': 'economy',
                          'reason': '灵石食用后增加当前灵力及来源待吸收修为'})
    path = conversion / 'factory_overrides.csv'
    result[path] = csv_update(path, factories, ('registry', 'target_id'))
    textures = json.loads((root / 'src/main/resources/economy/assets.json').read_text(encoding='utf-8'))
    assets = [{'owner': 'economy', 'kind': 'texture', 'id': 'yvanchuxitong:screens/' + name.removesuffix('.png')} for name in textures]
    assets.append({'owner': 'decor', 'kind': 'texture', 'id': 'yvanchuxitong:entities/shiti_yizi'})
    for kind, identity in [('geo', 'tiandiyinhang'), ('animation', 'tiandiyinhang'), ('texture', 'blocks/tiandiyinhang')]:
        assets.append({'owner': 'economy', 'kind': kind, 'id': 'yvanchuxiuzhen:' + identity})
    path = conversion / 'system_assets.csv'
    result[path] = csv_update(path, assets, ('owner', 'kind', 'id'))
    path = root / 'tools/porting/convert_content.py'
    converter = path.read_text(encoding='utf-8')
    animated = re.search(r'^ANIMATED_BLOCK_FACTORIES = \{[^}]*\}', converter, re.MULTILINE)
    if animated is None:
        raise ValueError('无法定位动画方块工厂白名单')
    if "'economy:bank'" not in animated.group():
        converter = converter[:animated.end() - 1] + ", 'economy:bank'" + converter[animated.end() - 1:]
    if "'economy:'" not in converter:
        needle = "'guchong:taoguan_', 'fulu:explosive_'"
        if converter.count(needle) != 1:
            raise ValueError('无法唯一定位经济方块已实现钩子白名单')
        converter = converter.replace(needle, "'economy:', " + needle)
    if "'economy:auction_token'" not in converter:
        needle = "'economy:bank_card',"
        if converter.count(needle) != 1:
            raise ValueError('无法唯一定位经济物品工厂白名单')
        converter = converter.replace(needle, needle + " 'economy:auction_token',")
    if "'economy:spirit_stone_'" not in converter:
        needle = "'economy:bank_card',"
        if converter.count(needle) != 1:
            raise ValueError('无法唯一定位经济灵石工厂白名单')
        converter = converter.replace(needle, needle + " 'economy:spirit_stone_',")
    result[path] = converter
    language = json.loads((root / 'src/main/resources/economy/language_zh_cn.json').read_text(encoding='utf-8'))
    # 七页来源GUI键由convert_menu_lang随已登记菜单输出，不能再次写入运行片段。
    runtime = {key: value for key, value in language.items()
               if not key.startswith('gui.') or key.startswith('gui.yvanchuxiuzhen.commerce.')}
    for locale in ('zh_cn', 'en_us'):
        path = root / f'src/main/lang/{locale}/economy_commerce_runtime.json'
        for sibling in path.parent.glob('*.json'):
            if sibling == path:
                continue
            existing = json.loads(sibling.read_text(encoding='utf-8-sig'))
            overlap = runtime.keys() & existing.keys()
            if overlap:
                raise ValueError(f'{sibling}: 运行语言键重复：{sorted(overlap)}')
        result[path] = json.dumps(runtime, ensure_ascii=False, indent=2) + '\n'
    return {path: text for path, text in result.items()
            if not path.exists() or path.read_text(encoding='utf-8') != text}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--apply', action='store_true')
    parser.add_argument('--root', type=Path, default=ROOT)
    args = parser.parse_args()
    root = args.root.resolve()
    pending = changes(root)
    for path, text in pending.items():
        print(('写入 ' if args.apply else '待写 ') + path.relative_to(root).as_posix())
        if args.apply:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(text, encoding='utf-8', newline='\n')
    print(f'{len(pending)} 个文件' + ('已更新' if args.apply else '待应用；没有修改文件'))


if __name__ == '__main__':
    main()
