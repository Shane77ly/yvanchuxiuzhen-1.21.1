"""把两个 1.18.2 来源的附魔类转换为 1.21.1 附魔数据定义与标签，由 convert_data.py 调用。

来源附魔是只覆盖稀有度、类别、槽位和少数方法的 `Enchantment` 子类，效果由订阅过程读取等级，不在类里。输出：
- src/main/resources/data/yvanchuxiuzhen/enchantment/<名>.json
- src/main/resources/data/yvanchuxiuzhen/tags/item/enchantable/<weapon|armor|digger|breakable>.json
  （1.18.2 的附魔类别 WEAPON、ARMOR、DIGGER、BREAKABLE 是按物品类判断的，目标版本用物品标签表达：原版标签加本 Mod 内容表里对应种类的物品）
- src/main/resources/data/minecraft/tags/enchantment/<in_enchanting_table|non_treasure|treasure|on_random_loot|tradeable>.json
  （对应 1.18.2 的 isTreasureOnly、isDiscoverable、isTradeable 默认值与覆盖）

来源方法只允许 getMaxLevel、isTradeable、isTreasureOnly 三种覆盖，出现其他覆盖时报错，由人决定如何表达。
"""
from __future__ import annotations

import json
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import CONTENT, SOURCES

DATA = ROOT / 'src/main/resources/data'
ALLOWED_OVERRIDES = {'getMaxLevel', 'isTradeable', 'isTreasureOnly'}
# 稀有度到权重与铁砧代价，取自 1.18.2 的 Enchantment.Rarity（权重 10/5/2/1，铁砧倍数 1/2/4/8）。
RARITY = {'COMMON': (10, 1), 'UNCOMMON': (5, 2), 'RARE': (2, 4), 'VERY_RARE': (1, 8)}
# 1.18.2 Enchantment 的默认费用 getMinCost(level) = 1 + level * 10，getMaxCost = getMinCost + 5，换成基础值加每级增量。
MIN_COST = {'base': 11, 'per_level_above_first': 10}
MAX_COST = {'base': 16, 'per_level_above_first': 10}
CATEGORY_TAG = {'WEAPON': 'weapon', 'ARMOR': 'armor', 'DIGGER': 'digger', 'BREAKABLE': 'breakable'}
VANILLA_ITEM_TAGS = {
    'weapon': ['#minecraft:enchantable/sword'],
    'armor': ['#minecraft:enchantable/armor'],
    'digger': ['#minecraft:pickaxes', '#minecraft:axes', '#minecraft:shovels', '#minecraft:hoes'],
    'breakable': ['#minecraft:enchantable/durability'],
}


def _method_body(text: str, name: str) -> str | None:
    match = re.search(rf'public \w+ {name}\([^)]*\)\s*\{{(.*?)\n\t\}}', text, re.S)
    return match[1] if match else None


def parse_enchantments() -> list[dict]:
    """两个来源的附魔：目标路径、稀有度、类别、最高等级、可交易、仅宝藏，以及来源类文件。"""
    rows = []
    for mod, (root, prefix) in SOURCES.items():
        registry = root / f'src/main/java/net/mcreator/{mod}/init/{prefix}ModEnchantments.java'
        if not registry.is_file():
            continue
        names = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+)Enchantment\(\)\)', registry.read_text(encoding='utf-8')))
        for path, cls in names.items():
            file = root / f'src/main/java/net/mcreator/{mod}/enchantment/{cls}Enchantment.java'
            text = file.read_text(encoding='utf-8-sig')
            header = re.search(r'super\(Enchantment\.Rarity\.(\w+), EnchantmentCategory\.(\w+), slots\)', text)
            overrides = set(re.findall(r'@Override\s+public \w+ (\w+)\(', text))
            if not header or overrides - ALLOWED_OVERRIDES:
                raise ValueError(f'{file} 的附魔类超出已核对的形态：{overrides - ALLOWED_OVERRIDES or "构造器"}')
            category = header[2]

            def flag(method: str, default: bool) -> bool:
                body = _method_body(text, method)
                return default if body is None else re.search(r'return (true|false);', body)[1] == 'true'

            max_level = _method_body(text, 'getMaxLevel')
            rows.append({
                'mod': mod, 'path': path, 'file': file, 'rarity': header[1], 'category': category,
                'max_level': int(re.search(r'return (\d+);', max_level)[1]) if max_level else 1,
                'tradeable': flag('isTradeable', True), 'treasure': flag('isTreasureOnly', False),
            })
    return rows


def mod_items_by_tag() -> dict[str, list[str]]:
    """本 Mod 内容表中属于各附魔类别的物品：剑、挖掘工具、盔甲，以及有耐久的工具、盔甲和物品。"""
    result = {tag: [] for tag in CATEGORY_TAG.values()}
    for path in sorted(CONTENT.glob('*/items.json')):
        for row in json.loads(path.read_text(encoding='utf-8')):
            id_ = f'{NAMESPACE}:{row["id"]}'
            tool = row.get('tool')
            if tool and tool['kind'] == 'sword':
                result['weapon'].append(id_)
            elif tool and tool['kind'] in ('axe', 'pickaxe', 'shovel', 'hoe'):
                result['digger'].append(id_)
            if row.get('factory') == 'armor':
                result['armor'].append(id_)
            if (tool and tool['tier']['uses'] > 0) or (row.get('factory') == 'armor' and row['durability_multiplier'] > 0) or row.get('durability', 0) > 0:
                result['breakable'].append(id_)
    return result


def generate(converter) -> None:
    rows = parse_enchantments()
    for row in rows:
        weight, anvil = RARITY[row['rarity']]
        definition = {
            'description': {'translate': f'enchantment.{NAMESPACE}.{row["path"]}'},
            'supported_items': f'#{NAMESPACE}:enchantable/{CATEGORY_TAG[row["category"]]}',
            'weight': weight,
            'max_level': row['max_level'],
            'min_cost': MIN_COST,
            'max_cost': MAX_COST,
            'anvil_cost': anvil,
            # 来源的注册没有给出装备槽位（构造器参数为空），附魔不参与按槽位生效的效果。
            'slots': [],
        }
        converter.write(DATA / NAMESPACE / 'enchantment' / f'{row["path"]}.json', definition, [(row['mod'], row['file'])])

    origins = [(row['mod'], row['file']) for row in rows]
    items = mod_items_by_tag()
    for tag, vanilla in VANILLA_ITEM_TAGS.items():
        converter.write(DATA / NAMESPACE / 'tags/item/enchantable' / f'{tag}.json',
                        {'replace': False, 'values': vanilla + items[tag]}, origins)

    def ids(select) -> list[str]:
        return [f'{NAMESPACE}:{row["path"]}' for row in rows if select(row)]

    enchantment_tags = {
        'in_enchanting_table': ids(lambda row: not row['treasure']),
        'non_treasure': ids(lambda row: not row['treasure']),
        'treasure': ids(lambda row: row['treasure']),
        'on_random_loot': ids(lambda row: True),
        'tradeable': ids(lambda row: row['tradeable']),
    }
    for tag, values in enchantment_tags.items():
        converter.write(DATA / 'minecraft/tags/enchantment' / f'{tag}.json', {'replace': False, 'values': values}, origins)
