"""由 1.18.2 来源生成内容表与语言片段（B1）。

读取两个来源的方块、物品注册与生成 Java，按台账 owner 把每条注册写入所属单位的
src/main/resources/content/<单位>/blocks.json、items.json，名称与提示文本写入 src/main/lang/<语言>/<单位>.json。
遇到不由内容表承载的类（有专属基类的物品、动画方块、流体、传送门、未识别写法）不生成行，记入跳过清单，
由所属系统迁移时处理。所属系统给内容表的行换用自己的方块或物品类型时，登记在 docs/reference/conversion/factory_overrides.csv；
菜单的台账行不再是“待处理”后，来源中该菜单的界面文本键（gui.<命名空间>.<菜单>.*）写入所属单位的语言片段；
其余由系统代码直接引用的语言键按 docs/reference/conversion/lang_prefixes.csv 的键前缀写入所属单位的语言片段。
工具可重复运行，输出只取决于来源与台账。
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
from legacy_items import TOOL_KINDS, parse_armor_material, parse_armor_piece, parse_item, parse_tool

WORKSPACE = Path('D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1/reference')
SOURCES = {
    'yvanchuxiuzhen': (WORKSPACE / 'old-1.18.2-mcreator/1.18.2yvanchuxiuzhen', 'Yvanchuxiuzhen'),
    'yvanchuxitong': (WORKSPACE / '1.18.2yvanchuxitong', 'Yvanchuxitong'),
}
CONTENT = ROOT / 'src/main/resources/content'
LANG = ROOT / 'src/main/lang'
SOUNDS = ROOT / 'src/main/sounds'
CONVERSION = ROOT / 'docs/reference/conversion'
FACTORIES = {'Block': 'block', 'SlabBlock': 'slab', 'StairBlock': 'stairs', 'WallBlock': 'wall', 'LeavesBlock': 'leaves',
             'FenceBlock': 'fence', 'FenceGateBlock': 'fence_gate', 'WoodButtonBlock': 'button',
             'PressurePlateBlock': 'pressure_plate', 'IronBarsBlock': 'pane', 'DoorBlock': 'door',
             'TrapDoorBlock': 'trapdoor', 'FallingBlock': 'falling', 'FlowerBlock': 'flower',
             'DoublePlantBlock': 'double_plant'}
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
        # GeckoLib 盔甲不在 ModItems，由 ModGeckoLibArmors 登记（原初系统没有）。
        geckolib = init / f'{self.prefix}ModGeckoLibArmors.java'
        self.geckolib_armor = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+Item)\(EquipmentSlot',
                                              geckolib.read_text(encoding='utf-8'))) if geckolib.is_file() else {}
        tabs_text = (init / f'{self.prefix}ModTabs.java').read_text(encoding='utf-8')
        self.block_class = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+)\(\)\)', blocks_text))
        self.block_const = {c: p for c, p in re.findall(r'RegistryObject<Block> (\w+) = \w+\.register\("(\w+)"', blocks_text)}
        self.item_class = dict(re.findall(r'register\("(\w+)", \(\) -> new ([\w.]+Item(?:\.\w+)?)\(\)\)', items_text))
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
        if path in self.geckolib_armor:
            return f'物品类 {self.geckolib_armor[path]}（GeckoLib 盔甲）由所属系统迁移'
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


def convert_sounds(sources: dict[str, Source], add_lang, expected) -> dict[str, list[dict]]:
    """声音事件按台账 owner 分到所属单位：内容表 content/<单位>/sounds.json 只登记事件 ID，声音文件（可与事件名不同）、
    类别与字幕由片段 src/main/sounds/<单位>.json 声明，字幕文本写入该单位的语言片段。"""
    rows: dict[str, list[dict]] = defaultdict(list)
    fragments: dict[str, dict[str, dict]] = defaultdict(dict)
    seen: set[str] = set()
    for mod, (root, _) in SOURCES.items():
        file = root / f'src/main/resources/assets/{mod}/sounds.json'
        if not file.is_file():
            continue
        for event, definition in json.loads(file.read_text(encoding='utf-8')).items():
            if event in seen:
                raise ValueError(f'声音事件 {event} 在两个来源中重复')
            seen.add(event)
            for sound in definition['sounds']:
                if not sound['name'].startswith(f'{mod}:'):
                    raise ValueError(f'声音事件 {event} 引用了其他命名空间的文件 {sound["name"]}')
            unit = expected[('sound_event', f'{mod}:{event}')]['owner']
            if not unit:
                raise ValueError(f'声音事件 {mod}:{event} 没有归属，先在 registration_ownership.csv 登记')
            fragments[unit][event] = {**definition, 'sounds': [{**sound, 'name': f'{NAMESPACE}:{sound["name"].split(":", 1)[1]}'} for sound in definition['sounds']]}
            rows[unit].append({'id': event})
            subtitle = definition.get('subtitle')
            if subtitle:
                for language in LANGUAGES:
                    text = sources[mod].lang[language].get(subtitle)
                    if text is not None:
                        add_lang(unit, language, subtitle, text)
    SOUNDS.mkdir(parents=True, exist_ok=True)
    for stale in SOUNDS.glob('*.json'):
        stale.unlink()
    for unit, fragment in fragments.items():
        text = json.dumps(fragment, ensure_ascii=False, indent=2) + '\n'
        (SOUNDS / f'{unit}.json').write_text(text, encoding='utf-8', newline='\n')
    return rows


def convert_advancement_lang(sources: dict[str, Source], add_lang) -> int:
    """进度的标题与说明文本（键写在进度文件的 translate 字段）写入 platform 的语言片段。"""
    count = 0
    for mod, (root, _) in SOURCES.items():
        for file in sorted((root / f'src/main/resources/data/{mod}/advancements').glob('*.json')):
            display = json.loads(file.read_text(encoding='utf-8'))['display']
            for key in (display['title']['translate'], display['description']['translate']):
                for language in LANGUAGES:
                    add_lang('platform', language, key, sources[mod].lang[language][key])
                count += 1
    return count


def convert_menu_lang(sources: dict[str, Source], add_lang) -> int:
    """已迁移菜单的界面文本键 gui.<命名空间>.<菜单路径>.* 写入台账所属单位的语言片段，命名空间统一。"""
    count = 0
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] != 'menu' or row['status'] == '待处理':
                continue
            mod, path = row['legacy_id'].split(':', 1)
            prefix = f'gui.{mod}.{path}.'
            target = f'gui.{NAMESPACE}.{row["target_id"].split(":", 1)[1]}.'
            for language in LANGUAGES:
                for key, value in sources[mod].lang[language].items():
                    if key.startswith(prefix):
                        add_lang(row['owner'], language, target + key[len(prefix):], value)
                        count += 1
    return count


def convert_prefix_lang(sources: dict[str, Source], add_lang) -> int:
    """系统代码直接引用的语言键（docs/reference/conversion/lang_prefixes.csv：所属单位、来源、键前缀）原样写入所属单位的语言片段。"""
    count = 0
    with (CONVERSION / 'lang_prefixes.csv').open(encoding='utf-8') as handle:
        for row in csv.DictReader(handle):
            for language in LANGUAGES:
                for key, value in sources[row['source_mod']].lang[language].items():
                    if key.startswith(row['prefix']):
                        add_lang(row['owner'], language, key, value)
                        count += 1
    return count


def factory_overrides() -> dict[tuple[str, str], str]:
    """所属系统换用的方块或物品工厂：(注册表, 目标路径) → 工厂名（docs/reference/conversion/factory_overrides.csv）。"""
    with (CONVERSION / 'factory_overrides.csv').open(encoding='utf-8') as handle:
        return {(row['registry'], row['target_id'].split(':', 1)[1]): row['factory'] for row in csv.DictReader(handle)}


def main() -> None:
    expected = expected_rows()
    overrides = factory_overrides()
    targets = ledger_targets()
    sources = {mod: Source(mod) for mod in SOURCES}
    registrations = [r for r in csv.DictReader((INVENTORY / 'registrations.csv').open(encoding='utf-8'))
                     if r['registry'] in ('block', 'item')]
    blocks_by_unit: dict[str, list[dict]] = defaultdict(list)
    items_by_unit: dict[str, list[dict]] = defaultdict(list)
    lang: dict[str, dict[str, dict[str, str]]] = defaultdict(lambda: {language: {} for language in LANGUAGES})
    skipped: list[tuple[str, str, str]] = []
    block_tooltips: dict[tuple[str, str], list[str]] = {}
    armor_materials: dict[tuple[str, str], dict] = {}

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
        factory = FACTORIES[row.pop('base')]
        blocks_by_unit[unit].append({'id': target, 'factory': overrides.get(('block', target), factory), **row})
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
            outer, _, piece = class_name.partition('.')
            text = source.java('item', outer)
            base = re.search(r'public (?:abstract )?class \w+ extends (\w+)', text).group(1)
            try:
                if base == 'Item':
                    row = {'id': target, 'factory': 'item', 'order': order, **parse_item(text, source.tabs)}
                elif base in TOOL_KINDS:
                    row = {'id': target, 'factory': 'tool', 'order': order, **parse_tool(text, base, source.tabs, source.item_const)}
                elif base == 'ArmorItem' and piece:
                    material = parse_armor_material(text, source.item_const)
                    part = parse_armor_piece(text, piece, source.tabs)
                    texture = part.pop('armor_texture')
                    multiplier = material.pop('durability_multiplier')
                    key = (unit, material['id'])
                    known = armor_materials.setdefault(key, {**material, 'texture': texture})
                    if known['texture'] != texture:
                        raise ValueError('同一盔甲材质的部件贴图不一致')
                    row = {'id': target, 'factory': 'armor', 'order': order, 'armor': {'material': material['id'], 'slot': piece.upper()},
                           'durability_multiplier': multiplier, **part}
                else:
                    skipped.append(('item', legacy_id, f'物品基类 {base} 由所属系统迁移'))
                    continue
            except ValueError as error:
                skipped.append(('item', legacy_id, f'{class_name}：{error}'[:200]))
                continue
            for language in LANGUAGES:
                name = source.lang[language].get(f'item.{mod}.{path}')
                if name is not None:
                    add_lang(unit, language, f'item.{NAMESPACE}.{target}', name)
        if ('item', target) in overrides:
            row['factory'] = overrides[('item', target)]
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
                title = source.lang[language].get(f'itemGroup.{name}')
                if title is not None:
                    add_lang('platform', language, f'itemGroup.{NAMESPACE}.{name}', title)

    sound_rows = convert_sounds(sources, add_lang, expected)
    advancement_keys = convert_advancement_lang(sources, add_lang)
    menu_keys = convert_menu_lang(sources, add_lang)
    prefix_keys = convert_prefix_lang(sources, add_lang)

    units = sorted(set(blocks_by_unit) | set(items_by_unit))
    for stale in CONTENT.glob('*/sounds.json'):
        stale.unlink()
    for unit, unit_rows in sound_rows.items():
        write_rows(CONTENT / unit / 'sounds.json', unit_rows)
    for unit in units:
        if blocks_by_unit.get(unit):
            write_rows(CONTENT / unit / 'blocks.json', blocks_by_unit[unit])
        if items_by_unit.get(unit):
            write_rows(CONTENT / unit / 'items.json', items_by_unit[unit])
    for unit in sorted({u for u, _ in armor_materials}):
        write_rows(CONTENT / unit / 'armor_materials.json', [m for (u, _), m in sorted(armor_materials.items()) if u == unit])
    write_rows(CONTENT / 'platform' / 'tabs.json', sorted(tabs, key=lambda t: t['id']))
    write_lang(lang)
    CONVERSION.mkdir(parents=True, exist_ok=True)
    with (CONVERSION / 'skipped.csv').open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['registry', 'legacy_id', 'reason'])
        writer.writerows(sorted(skipped))
    print({unit: (len(blocks_by_unit.get(unit, [])), len(items_by_unit.get(unit, []))) for unit in units})
    print('tabs', len(tabs), 'sounds', sum(len(r) for r in sound_rows.values()), 'advancement keys', advancement_keys, 'menu keys', menu_keys, 'prefix keys', prefix_keys, 'skipped', len(skipped))


if __name__ == '__main__':
    sys.exit(main())
