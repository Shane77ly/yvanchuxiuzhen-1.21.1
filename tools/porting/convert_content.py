"""由 1.18.2 来源生成内容表与语言片段（B1）。

读取两个来源的方块、物品注册与生成 Java，按台账 owner 把每条注册写入所属单位的
src/main/resources/content/<单位>/blocks.json、items.json，名称与提示文本写入 src/main/lang/<语言>/<单位>.json。
遇到不由内容表承载的类（有专属基类的物品、动画方块、流体、传送门、未识别写法）不生成行，记入跳过清单，
由所属系统迁移时处理。所属系统给内容表的行换用自己的方块或物品类型时，登记在 docs/reference/conversion/factory_overrides.csv；
菜单的台账行不再是“待处理”后，来源中该菜单的界面文本键（gui.<命名空间>.<菜单>.*）写入所属单位的语言片段；
已登记附魔、状态效果与实体类型的名称、说明键以及状态效果表、实体表（content/<单位>/effects.json、entities.json）同样按台账所属单位写入；
其余由系统代码直接引用的语言键按 docs/reference/conversion/lang_prefixes.csv 的键前缀写入所属单位的语言片段。
材料目录直接读取 WorldbeginProcedure 的 lianqicailiaolist，保存来源行序及完整三元组次序到 content/material/definitions.json。
工具可重复运行，输出只取决于来源与台账。
"""
from __future__ import annotations

import csv
import argparse
import json
import re
import sys
from collections import defaultdict
from pathlib import Path

from build_ledger import INVENTORY, NAMESPACE, ROOT, expected_rows, read_ledgers
from legacy_blocks import BASES, parse_block
from legacy_items import TOOL_KINDS, parse_armor_material, parse_armor_piece, parse_item, parse_item_properties, parse_tool

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
ANIMATED_BLOCK_FACTORIES = {'lianqi:repair_table', 'fulu:inheritance', 'cooking:iron_pot', 'cooking:clay_pot',
                            'craft:stone_mill', 'craft:stone_roller', 'craft:loom', 'craft:carpentry', 'craft:silk_reel'}
# 1.18.2 原版创造栏在 1.21.1 的对应；临时对应，待负责人确认。
VANILLA_TABS = {'TAB_MISC': 'minecraft:tools_and_utilities', 'TAB_DECORATIONS': 'minecraft:building_blocks',
                'TAB_TOOLS': 'minecraft:tools_and_utilities'}
LANGUAGES = ('zh_cn', 'en_us')
OUTPUT_OWNER: str | None = None


class Source:
    """一个来源 Mod 的生成代码索引。"""

    def __init__(self, mod: str) -> None:
        self.mod = mod
        self.root, self.prefix = SOURCES[mod]
        init = self.root / f'src/main/java/net/mcreator/{mod}/init'
        blocks_text = (init / f'{self.prefix}ModBlocks.java').read_text(encoding='utf-8')
        items_text = (init / f'{self.prefix}ModItems.java').read_text(encoding='utf-8')
        self.items_text = items_text
        self.entities_text = (init / f'{self.prefix}ModEntities.java').read_text(encoding='utf-8')
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
    if OUTPUT_OWNER is not None and path.parent.name != OUTPUT_OWNER:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    text = '[\n' + ',\n'.join(json.dumps(row, ensure_ascii=False, separators=(',', ':')) for row in rows) + '\n]\n'
    path.write_text(text, encoding='utf-8', newline='\n')


def write_lang(unit_entries: dict[str, dict[str, dict[str, str]]]) -> None:
    for unit, by_language in unit_entries.items():
        if OUTPUT_OWNER is not None and unit != OUTPUT_OWNER:
            continue
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
        if OUTPUT_OWNER is None or stale.stem == OUTPUT_OWNER:
            stale.unlink()
    for unit, fragment in fragments.items():
        if OUTPUT_OWNER is not None and unit != OUTPUT_OWNER:
            continue
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


# 台账注册表对应的语言键：(键前缀, 说明键后缀)。名称键为 <前缀>.<命名空间>.<名称>。
REGISTRY_LANG = {'enchantment': ('enchantment', '.desc'), 'mob_effect': ('effect', '.description'),
                 'entity_type': ('entity', None), 'biome': ('biome', None)}


def convert_registry_lang(sources: dict[str, Source], add_lang) -> int:
    """已登记附魔、状态效果、实体与群系的名称键和说明键写入台账所属单位的语言片段，命名空间统一。
    说明键可能在另一个来源的语言文件中（原初系统的 chain 说明在原初修真的语言文件里）。"""
    count = 0
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] not in REGISTRY_LANG or row['status'] == '待处理':
                continue
            prefix, description = REGISTRY_LANG[row['registry']]
            mod, path = row['legacy_id'].split(':', 1)
            target = f'{prefix}.{NAMESPACE}.{row["target_id"].split(":", 1)[1]}'
            for language in LANGUAGES:
                for suffix in ('', description) if description else ('',):
                    for source in sources.values():
                        text = source.lang[language].get(f'{prefix}.{mod}.{path}{suffix}')
                        if text is not None:
                            add_lang(row['owner'], language, target + suffix, text)
                            count += 1
                            break
    return count


def convert_effects(sources: dict[str, Source]) -> dict[str, list[dict]]:
    """已登记状态效果的内容表行（ID、类别、颜色），取自来源 MobEffect 子类的构造器。"""
    result: dict[str, list[dict]] = defaultdict(list)
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] != 'mob_effect' or row['status'] == '待处理':
                continue
            text = sources[row['source_mod']].java('potion', f'{row["element"]}MobEffect')
            header = re.search(r'super\(MobEffectCategory\.(\w+), (-?\d+)\)', text)
            result[row['owner']].append({'id': row['target_id'].split(':', 1)[1], 'category': header[1], 'color': int(header[2])})
    return result


def convert_entities(sources: dict[str, Source]) -> dict[str, list[dict]]:
    """已登记实体类型的内容表行，取自来源 ModEntities 中的 EntityType.Builder：类别、碰撞箱、跟踪范围、更新间隔、火焰免疫。
    来源没有写的跟踪范围、更新间隔取 1.18.2 构建器的默认值 5 与 3。"""
    result: dict[str, list[dict]] = defaultdict(list)
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] != 'entity_type' or row['status'] == '待处理':
                continue
            # 此实体由来源自身嵌套 Register 注册，目标同样独立登记 noSave 与最大更新间隔，不进入内容表。
            if row['legacy_id'] == 'yvanchuxitong:colorful_lightning_bolt':
                continue
            mod, path = row['legacy_id'].split(':', 1)
            match = re.search(rf'register\("{path}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);', sources[mod].entities_text, re.S)
            if match is None:
                raise ValueError(f'无法提取实体构建器：{row["legacy_id"]}，来源 {row["source_file"]}')
            chain = match[2]
            size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
            tracking = re.search(r'setTrackingRange\((\d+)\)', chain)
            interval = re.search(r'setUpdateInterval\((\d+)\)', chain)
            result[row['owner']].append({
                'id': row['target_id'].split(':', 1)[1], 'category': match[1], 'width': float(size[1]), 'height': float(size[2]),
                'tracking_range': int(tracking[1]) if tracking else 5, 'update_interval': int(interval[1]) if interval else 3,
                'fire_immune': '.fireImmune()' in chain})
    return result


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


def convert_material_definitions(sources: dict[str, Source], targets: dict[tuple[str, str], str]) -> list[dict]:
    """保留材料目录完整三元组的来源次序，经验与特殊标记同样占据辨识序号。"""
    text = sources['yvanchuxitong'].java('procedures', 'WorldbeginProcedure')
    assignment = re.search(r'\.lianqicailiaolist\s*=\s*(.*?);', text, re.S).group(1)
    catalogue = ''.join(json.loads(literal) for literal in re.findall(r'"(?:[^"\\]|\\.)*"', assignment))
    rows: list[dict] = []
    seen: set[str] = set()
    for line in catalogue.split('%'):
        # Java String.split 会丢弃尾部空字段；不修补来源211–219号缺失的分隔符。
        parts = line.rstrip('/').split('/')
        # 220、230号是来源未注册的空目录行，仍保留其标识供辨识初始化读取。
        item_id = parts[0].replace('yvanchuxitong:', f'{NAMESPACE}:') if len(parts) == 1 else targets[('item', parts[0])]
        if item_id in seen:
            raise ValueError(f'重复材料定义 {item_id}')
        seen.add(item_id)
        row = {'item_id': item_id, 'attribute_entries': [], 'special_effect_entries': [], 'boolean_traits': []}
        if len(parts) >= 2:
            row['fixed_weight'] = int(parts[1])
        entries = []
        for ordinal in range(max(0, len(parts) - 2) // 3):
            namespace, name, raw_value = parts[2 + ordinal * 3:5 + ordinal * 3]
            value = int(raw_value) if re.fullmatch(r'-?\d+', raw_value) else raw_value
            entry = {'namespace': namespace, 'name': name, 'value': value}
            entries.append(entry)
            if namespace == 'attribute':
                if name == 'expnum':
                    row['exp_value'] = value
                else:
                    row['attribute_entries'].append({'name': name, 'value': value})
            elif namespace == 'teshu':
                row['boolean_traits'].append(name)
            else:
                row['special_effect_entries'].append(entry.copy())
        row['entries'] = entries
        rows.append(row)
    return rows


def convert_herb_definitions(sources: dict[str, Source], targets: dict[tuple[str, str], str]) -> list[dict]:
    """采用世界加载时覆盖的药草目录，保留重复药效及运算次序。"""
    text = sources['yvanchuxitong'].java('procedures', 'WorldbeginProcedure')
    assignment = re.search(r'\.caoyaolist\s*=\s*(.*?);', text, re.S).group(1)
    catalogue = ''.join(json.loads(literal) for literal in re.findall(r'"(?:[^"\\]|\\.)*"', assignment))
    rows = []
    for line in catalogue.split('%'):
        parts = line.split('/')
        count = int(parts[1])
        if len(parts) != 2 + count * 5:
            raise ValueError(f'药草目录长度不符 {parts[0]}')
        effects = []
        for ordinal in range(count):
            name, level_op, level, time_op, time = parts[2 + ordinal * 5:7 + ordinal * 5]
            effects.append({'name': name, 'level_op': level_op, 'level': int(level), 'time_op': time_op, 'time': int(time)})
        # 来源目录含未注册的 nichanggrass，不改指向实际注册的 nichanggrass_07。
        item_id = parts[0] if parts[0] == 'yvanchuxiuzhen:nichanggrass' else targets[('item', parts[0])]
        rows.append({'item_id': item_id, 'effects': effects})
    return rows


def convert_alchemy_devices(source: Source) -> tuple[list[dict], list[dict]]:
    """八种丹炉以系统动画类承担行为，内容表只保存来源基础属性与创造栏顺序。"""
    registrations = [r for r in csv.DictReader((INVENTORY / 'registrations.csv').open(encoding='utf-8'))
                     if r['registry'] == 'item']
    orders = {r['legacy_id']: i + 1 for i, r in enumerate(registrations)}
    blocks, items = [], []
    for tier in range(1, 9):
        path = f'alchemy_{tier:02}'
        text = source.java('block', f'Alchemy{tier:02}Block')
        # ANIMATION 与动态 render shape 由 AlchemyBlock 实现，不进入普通状态解析器。
        text = re.sub(r'public static final IntegerProperty ANIMATION = [^;]+;', '', text)
        text = re.sub(r'@Override\s+public RenderShape getRenderShape\([^)]*\) \{[^}]*\}', '', text)
        row = parse_block(text, source.item_const, source.block_const)
        row.pop('base')
        row.pop('pending_hooks', None)
        blocks.append({'id': path, 'factory': 'liandan:alchemy', **row})
        items.append({'id': path, 'factory': f'liandan:{path}', 'block': path,
                      'order': orders[f'{source.mod}:{path}'], 'tab': source.tabs['TAB_YVANCHU_BLOCK']})
    return blocks, items


def convert_drawing_table(source: Source, add_lang) -> list[dict]:
    """画符知识转统一键名，按来源五项分组保留有序配方及其专用显示名。"""
    recipes = []
    source_knowledge = {}
    for category, count in (('futou', 11), ('tiandizhu', 4), ('fudan', 8)):
        names = {}
        for number in range(1, count + 1):
            source_key = f'txt.yvanchuxitong.huafu_{category}_{number:02d}'
            names[source.lang['zh_cn'][source_key]] = number
            for language in LANGUAGES:
                add_lang('fulu', language, source_key.replace('yvanchuxitong', NAMESPACE), source.lang[language][source_key])
        source_knowledge[category] = names
    for language in LANGUAGES:
        tokens = source.lang[language]['list.yvanchuxitong.fululist'].split('/')
        if len(tokens) % 5:
            raise ValueError(f'画符表不是完整五项分组：{language}')
        for index in range(0, len(tokens), 5):
            head, core, name, item, ink = tokens[index:index + 5]
            path = item.split(':', 1)[1]
            add_lang('fulu', language, f'gui.{NAMESPACE}.huafu_gui.recipe.{path}', name)
            if language == 'zh_cn':
                recipes.append({'head': source_knowledge['futou'][head], 'core': source_knowledge['fudan'][core],
                                'item': f'{NAMESPACE}:{path}', 'ink': int(ink)})
    return recipes


def main() -> None:
    global OUTPUT_OWNER
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--owner', help='只写指定系统的内容、语言与声音片段，保留其他系统已迁移的输出。')
    OUTPUT_OWNER = parser.parse_args().owner
    expected = expected_rows()
    if OUTPUT_OWNER is not None and OUTPUT_OWNER not in {row['owner'] for row in expected.values()}:
        parser.error(f'未知系统：{OUTPUT_OWNER}')
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
        if overrides.get(('block', target)) in ANIMATED_BLOCK_FACTORIES:
            # 动画状态和实体渲染由系统类承担，基础属性仍取锁定来源。
            text = re.sub(r'public static final IntegerProperty ANIMATION = [^;]+;', '', text)
            text = re.sub(r'@Override\s+public RenderShape getRenderShape\([^)]*\) \{[^}]*\}', '', text)
        base = re.search(r'public class \w+ extends (\w+)', text).group(1)
        if base not in BASES and overrides.get(('block', target)) not in ANIMATED_BLOCK_FACTORIES:
            skipped.append(('block', legacy_id, f'方块基类 {base} 由所属系统迁移'))
            continue
        try:
            row = parse_block(text, source.item_const, source.block_const)
        except ValueError as error:
            skipped.append(('block', legacy_id, f'{class_name}：{error}'.replace('\n', ' ')[:200]))
            continue
        block_tooltips[(mod, path)] = row.pop('tooltip', [])
        base = row.pop('base')
        factory = overrides[('block', target)] if ('block', target) in overrides else FACTORIES[base]
        if factory in ('lianqi:blueprint_table', 'fulu:inheritance'):
            strength = re.search(r'\.strength\((-?[\d.]+)f?, ?([\d.]+)f?\)', text)
            row['strength'] = [float(strength.group(1)), float(strength.group(2))]
        if factory in ('liandan:medicine_container', 'liandan:medicine_separation', 'liandan:pharmacology', 'lianqi:tripod',
                       'lianqi:repair_table', 'lianqi:sewing_basket', 'lianqi:blueprint_table', 'lianqi:poison_cauldron',
                       'fulu:drawing_table', 'fulu:ash_burner', 'fulu:soot', 'fulu:inheritance', 'fulu:repelling_talisman',
                       'cooking:iron_pot', 'cooking:clay_pot', 'craft:stone_mill', 'craft:stone_roller', 'craft:loom', 'craft:carpentry', 'craft:silk_reel') or factory.startswith('fulu:explosive_'):
            row.pop('pending_hooks', None)
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
            if overrides.get(('item', target)) in ANIMATED_BLOCK_FACTORIES:
                row = {'id': target, 'factory': overrides[('item', target)], 'block': target,
                       'order': order, 'tab': source.tabs['TAB_YVANCHU_BLOCK']}
                tooltip = block_tooltips.get((mod, path), [])
                if tooltip:
                    row['tooltip'] = tooltip
                items_by_unit[unit].append(row)
                for number, line in enumerate(tooltip, start=1):
                    for language in LANGUAGES:
                        add_lang(unit, language, f'tooltip.{NAMESPACE}.item.{target}.{number}', line)
                continue
            if class_name is None and path in source.geckolib_armor and ('item', target) in overrides:
                class_name = source.geckolib_armor[path]
                text = source.java('item', class_name)
                material = parse_armor_material(text, source.item_const)
                multiplier = material.pop('durability_multiplier')
                armor_materials[(unit, material['id'])] = {**material, 'texture': material['id'], 'animated': True}
                slot = {'helmet': 'HELMET', 'chestplate': 'CHESTPLATE', 'leggings': 'LEGGINGS', 'boots': 'BOOTS'}[path.rsplit('_', 1)[1]]
                gecko_init = (source.root / f'src/main/java/net/mcreator/{mod}/init/{source.prefix}ModGeckoLibArmors.java').read_text(encoding='utf-8')
                properties = re.search(rf'register\("{re.escape(path)}", \(\) -> new \w+\(EquipmentSlot\.\w+, (new Item\.Properties\(\).*?)\)\);', gecko_init).group(1)
                row = {'id': target, 'factory': overrides[('item', target)], 'order': order,
                       'armor': {'material': material['id'], 'slot': slot}, 'durability_multiplier': multiplier,
                       **parse_item_properties(properties, source.tabs)}
                items_by_unit[unit].append(row)
                for language in LANGUAGES:
                    name = source.lang[language].get(f'item.{mod}.{path}')
                    if name is not None:
                        add_lang(unit, language, f'item.{NAMESPACE}.{target}', name)
                continue
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
        if row['factory'] in ('liandan:medicine_container', 'liandan:medicine_container_empty_box', 'lianqi:tripod_placement') or row['factory'].startswith(('fulu:talisman_', 'fulu:ranged_', 'fulu:passive_', 'fulu:explosive_')):
            row.pop('pending_hooks', None)
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
    registry_keys = convert_registry_lang(sources, add_lang)
    effect_rows = convert_effects(sources)
    entity_rows = convert_entities(sources)
    prefix_keys = convert_prefix_lang(sources, add_lang)
    material_definitions = convert_material_definitions(sources, targets)
    herb_definitions = convert_herb_definitions(sources, targets)
    drawing_recipes = convert_drawing_table(sources['yvanchuxitong'], add_lang)
    alchemy_blocks, alchemy_items = convert_alchemy_devices(sources['yvanchuxiuzhen'])
    blocks_by_unit['liandan'].extend(alchemy_blocks)
    items_by_unit['liandan'].extend(alchemy_items)
    alchemy_ids = {f'{NAMESPACE}:{row["id"]}' for row in alchemy_blocks}
    skipped = [row for row in skipped if row[1] not in alchemy_ids]
    for row in alchemy_blocks:
        for language in LANGUAGES:
            key = f'block.{NAMESPACE}.{row["id"]}'
            add_lang('liandan', language, key, sources['yvanchuxiuzhen'].lang[language][key])

    units = sorted(set(blocks_by_unit) | set(items_by_unit))
    for stale in (*CONTENT.glob('*/sounds.json'), *CONTENT.glob('*/effects.json'), *CONTENT.glob('*/entities.json')):
        if OUTPUT_OWNER is None or stale.parent.name == OUTPUT_OWNER:
            stale.unlink()
    for unit, unit_rows in entity_rows.items():
        write_rows(CONTENT / unit / 'entities.json', sorted(unit_rows, key=lambda r: r['id']))
    for unit, unit_rows in effect_rows.items():
        write_rows(CONTENT / unit / 'effects.json', sorted(unit_rows, key=lambda r: r['id']))
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
    write_rows(CONTENT / 'material' / 'definitions.json', material_definitions)
    write_rows(CONTENT / 'liandan' / 'herbs.json', herb_definitions)
    write_rows(CONTENT / 'fulu' / 'drawing_recipes.json', drawing_recipes)
    write_lang(lang)
    CONVERSION.mkdir(parents=True, exist_ok=True)
    if OUTPUT_OWNER is not None:
        existing = list(csv.DictReader((CONVERSION / 'skipped.csv').open(encoding='utf-8')))
        skipped = [row for row in skipped if expected[(row[0], row[1])]['owner'] == OUTPUT_OWNER]
        skipped.extend((row['registry'], row['legacy_id'], row['reason']) for row in existing
                       if expected[(row['registry'], row['legacy_id'])]['owner'] != OUTPUT_OWNER)
    with (CONVERSION / 'skipped.csv').open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['registry', 'legacy_id', 'reason'])
        writer.writerows(sorted(skipped))
    print({unit: (len(blocks_by_unit.get(unit, [])), len(items_by_unit.get(unit, []))) for unit in units})
    print('tabs', len(tabs), 'sounds', sum(len(r) for r in sound_rows.values()), 'advancement keys', advancement_keys, 'menu keys', menu_keys, 'registry keys', registry_keys, 'prefix keys', prefix_keys, 'skipped', len(skipped))
    print('material definitions', len(material_definitions), 'ordered entries', sum(len(row['entries']) for row in material_definitions))


if __name__ == '__main__':
    sys.exit(main())
