"""由 1.18.2 来源生成内容表与语言片段（B1）。

读取两个来源的方块、物品注册与生成 Java，按台账 owner 把每条注册写入所属单位的
src/main/resources/content/<单位>/blocks.json、items.json，名称与提示文本写入 src/main/lang/<语言>/<单位>.json。
遇到不由内容表承载的类（有专属基类的物品、动画方块、流体、传送门、未识别写法）不生成行，记入跳过清单，
由所属系统迁移时处理。工具可重复运行，输出只取决于来源与台账。
"""
from __future__ import annotations

import csv
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

from build_ledger import INVENTORY, NAMESPACE, ROOT, expected_rows, read_ledgers
from legacy_blocks import BASES, parse_block
from legacy_items import parse_item

WORKSPACE = Path('D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1/reference')
SOURCES = {
    'yvanchuxiuzhen': (WORKSPACE / 'old-1.18.2-mcreator/1.18.2yvanchuxiuzhen', 'Yvanchuxiuzhen'),
    'yvanchuxitong': (WORKSPACE / '1.18.2yvanchuxitong', 'Yvanchuxitong'),
}
CONTENT = ROOT / 'src/main/resources/content'
LANG = ROOT / 'src/main/lang'
CONVERSION = ROOT / 'docs/reference/conversion'
FACTORIES = {'Block': 'block', 'SlabBlock': 'slab', 'StairBlock': 'stairs', 'WallBlock': 'wall', 'LeavesBlock': 'leaves',
             'FenceBlock': 'fence', 'FenceGateBlock': 'fence_gate', 'WoodButtonBlock': 'button',
             'PressurePlateBlock': 'pressure_plate', 'IronBarsBlock': 'pane', 'DoorBlock': 'door',
             'TrapDoorBlock': 'trapdoor', 'FallingBlock': 'falling'}
# 1.18.2 原版创造栏在 1.21.1 的对应；临时对应，待负责人确认。
VANILLA_TABS = {'TAB_MISC': 'minecraft:tools_and_utilities', 'TAB_DECORATIONS': 'minecraft:building_blocks',
                'TAB_TOOLS': 'minecraft:tools_and_utilities'}
LANGUAGES = ('zh_cn', 'en_us')


class Source:
    """一个来源 Mod 的生成代码索引。"""

    def __init__(self, mod: str) -> None:
        self.mod = mod
        self.root, self.prefix = SOURCES[mod]
        init = self.root / f'src/main/java/net/mcreator/{mod}/init'
        blocks_text = (init / f'{self.prefix}ModBlocks.java').read_text(encoding='utf-8')
        items_text = (init / f'{self.prefix}ModItems.java').read_text(encoding='utf-8')
        self.items_text = items_text
        tabs_text = (init / f'{self.prefix}ModTabs.java').read_text(encoding='utf-8')
        self.block_class = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+)\(\)\)', blocks_text))
        self.block_const = {c: p for c, p in re.findall(r'RegistryObject<Block> (\w+) = \w+\.register\("(\w+)"', blocks_text)}
        self.item_class = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+Item)\(\)\)', items_text))
        self.item_const = {c: p for c, p in re.findall(r'RegistryObject<Item> (\w+) = \w+\.register\("(\w+)"', items_text)}
        self.block_items = {}
        for const, helper, block, tab in re.findall(r'RegistryObject<Item> (\w+) = (block|doubleBlock)\(\w+\.(\w+), (?:\w+\.)?(\w+)\)', items_text):
            path = self.block_const[block]
            self.block_items[path] = {'double': helper == 'doubleBlock', 'tab': tab}
            self.item_const[const] = path
        self.tabs = {}
        for const, name in re.findall(r'(TAB_\w+) = new CreativeModeTab\("(\w+)"\)', tabs_text):
            self.tabs[const] = f'{NAMESPACE}:{name}'
        self.tabs.update(VANILLA_TABS)
        self.tab_icons = dict(re.findall(r'new CreativeModeTab\("(\w+)"\) \{\s*@Override\s*public ItemStack makeIcon\(\) \{\s*return new ItemStack\(\w+\.(\w+)\.get\(\)\)', tabs_text))
        lang = {}
        for language in LANGUAGES:
            lang[language] = json.loads((self.root / f'src/main/resources/assets/{mod}/lang/{language}.json').read_text(encoding='utf-8'))
        self.lang = lang

    def special_item_reason(self, path: str) -> str:
        """专属类注册的物品（动画方块物品、刷怪蛋、盔甲部件、桶等）：返回其实例化的类名。"""
        const = next((c for c, p in self.item_const.items() if p == path), None) or path.upper()
        match = re.search(rf'RegistryObject<Item> {const} = [^;]*?new ([\w.]+)\(', self.items_text, re.S)
        if match is None:
            raise ValueError(f'无法识别物品注册形式：{path}')
        return f'物品类 {match.group(1)} 由所属系统迁移'

    def java(self, kind: str, class_name: str) -> str:
        return (self.root / f'src/main/java/net/mcreator/{self.mod}/{kind}/{class_name}.java').read_text(encoding='utf-8')


def ledger_targets() -> dict[tuple[str, str], str]:
    return {(row['registry'], row['legacy_id']): row['target_id'] for rows in read_ledgers().values() for row in rows}


def write_rows(path: Path, rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    text = '[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in rows) + '\n]\n'
    path.write_text(text, encoding='utf-8', newline='\n')


def write_lang(unit_entries: dict[str, dict[str, dict[str, str]]]) -> None:
    for unit, by_language in unit_entries.items():
        for language, entries in by_language.items():
            path = LANG / language / f'{unit}.json'
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(entries, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')


def main() -> None:
    expected = expected_rows()
    targets = ledger_targets()
    sources = {mod: Source(mod) for mod in SOURCES}
    registrations = [r for r in csv.DictReader((INVENTORY / 'registrations.csv').open(encoding='utf-8'))
                     if r['registry'] in ('block', 'item')]
    blocks_by_unit: dict[str, list[dict]] = defaultdict(list)
    items_by_unit: dict[str, list[dict]] = defaultdict(list)
    lang: dict[str, dict[str, dict[str, str]]] = defaultdict(lambda: {language: {} for language in LANGUAGES})
    skipped: list[tuple[str, str, str]] = []
    block_tooltips: dict[tuple[str, str], list[str]] = {}

    def add_lang(unit: str, language: str, key: str, value: str) -> None:
        lang[unit][language][key] = value

    for registration in (r for r in registrations if r['registry'] == 'block'):
        legacy_id, mod = registration['legacy_id'], registration['source_mod']
        source, path = sources[mod], legacy_id.split(':', 1)[1]
        unit = expected[('block', legacy_id)]['owner']
        target = targets[('block', legacy_id)].split(':', 1)[1]
        class_name = source.block_class[path]
        text = source.java('block', class_name)
        base = re.search(r'public class \w+ extends (\w+)', text).group(1)
        if base not in BASES:
            skipped.append(('block', legacy_id, f'方块基类 {base} 由所属系统迁移'))
            continue
        try:
            row = parse_block(text, source.item_const, source.block_const)
        except ValueError as error:
            skipped.append(('block', legacy_id, f'{class_name}：{error}'.replace('\n', ' ')[:200]))
            continue
        block_tooltips[(mod, path)] = row.pop('tooltip', [])
        blocks_by_unit[unit].append({'id': target, 'factory': FACTORIES[row.pop('base')], **row})
        for language in LANGUAGES:
            name = source.lang[language].get(f'block.{mod}.{path}')
            if name is not None:
                add_lang(unit, language, f'block.{NAMESPACE}.{target}', name)

    order = 0
    for registration in (r for r in registrations if r['registry'] == 'item'):
        order += 1
        legacy_id, mod = registration['legacy_id'], registration['source_mod']
        source, path = sources[mod], legacy_id.split(':', 1)[1]
        unit = expected[('item', legacy_id)]['owner']
        target = targets[('item', legacy_id)].split(':', 1)[1]
        if path in source.block_items:
            if (mod, path) not in block_tooltips:
                skipped.append(('item', legacy_id, '对应方块由所属系统迁移'))
                continue
            info = source.block_items[path]
            row = {'id': target, 'factory': 'block_item', 'block': target, 'order': order}
            if info['double']:
                row['double'] = True
            if info['tab'] != 'null':
                row['tab'] = source.tabs[info['tab']]
            if block_tooltips[(mod, path)]:
                row['tooltip'] = block_tooltips[(mod, path)]
        else:
            class_name = source.item_class.get(path)
            if class_name is None:
                skipped.append(('item', legacy_id, source.special_item_reason(path)))
                continue
            text = source.java('item', class_name)
            base = re.search(r'public class \w+ extends (\w+)', text).group(1)
            if base != 'Item':
                skipped.append(('item', legacy_id, f'物品基类 {base} 由所属系统迁移'))
                continue
            try:
                row = {'id': target, 'factory': 'item', 'order': order, **parse_item(text, source.tabs)}
            except ValueError as error:
                skipped.append(('item', legacy_id, f'{class_name}：{error}'[:200]))
                continue
            for language in LANGUAGES:
                name = source.lang[language].get(f'item.{mod}.{path}')
                if name is not None:
                    add_lang(unit, language, f'item.{NAMESPACE}.{target}', name)
        items_by_unit[unit].append(row)
        for number, line in enumerate(row.get('tooltip', []), start=1):
            for language in LANGUAGES:
                add_lang(unit, language, f'tooltip.{NAMESPACE}.item.{target}.{number}', line)

    tabs = []
    for mod, source in sources.items():
        for const, tab_id in source.tabs.items():
            if const in VANILLA_TABS:
                continue
            name = tab_id.split(':', 1)[1]
            icon_const = source.tab_icons[name]
            icon = source.block_const.get(icon_const) or source.item_const[icon_const]
            tabs.append({'id': name, 'icon': targets[('block' if icon_const in source.block_const else 'item', f'{mod}:{icon}')]})
            for language in LANGUAGES:
                title = source.lang[language].get(f'itemGroup.{mod}.{name}')
                if title is not None:
                    add_lang('platform', language, f'itemGroup.{NAMESPACE}.{name}', title)

    units = sorted(set(blocks_by_unit) | set(items_by_unit))
    for unit in units:
        if blocks_by_unit.get(unit):
            write_rows(CONTENT / unit / 'blocks.json', blocks_by_unit[unit])
        if items_by_unit.get(unit):
            write_rows(CONTENT / unit / 'items.json', items_by_unit[unit])
    write_rows(CONTENT / 'platform' / 'tabs.json', sorted(tabs, key=lambda t: t['id']))
    write_lang(lang)
    CONVERSION.mkdir(parents=True, exist_ok=True)
    with (CONVERSION / 'skipped.csv').open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['registry', 'legacy_id', 'reason'])
        writer.writerows(sorted(skipped))
    print({unit: (len(blocks_by_unit.get(unit, [])), len(items_by_unit.get(unit, []))) for unit in units})
    print('tabs', len(tabs), 'skipped', len(skipped))


if __name__ == '__main__':
    sys.exit(main())
