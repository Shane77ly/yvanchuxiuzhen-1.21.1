"""读取 MCreator 生成的 1.18.2 普通物品类（直接继承 Item），提取内容表所需的基础属性。

使用、右键、食用结算等覆写属于玩法行为，只记入 pending_hooks；未识别的覆写抛出 ValueError。
"""
from __future__ import annotations

import re

# 由内容表字段承载的覆写：食物的使用动画与时长随 food 字段。
HANDLED = {'appendHoverText', 'getUseDuration', 'getUseAnimation'}
HOOKS = {'use', 'useOn', 'finishUsingItem', 'releaseUsing', 'onUsingTick', 'inventoryTick', 'getDisplayName',
         'createMenu', 'initCapabilities', 'getShareTag', 'readShareTag', 'initializeClient', 'getItemStackRenderer',
         'registerControllers', 'getFactory', 'hasCraftingRemainingItem', 'getContainerItem', 'getDestroySpeed',
         'isCorrectToolForDrops', 'canPerformAction', 'getArmorModel',
         'mineBlock', 'hurtEnemy', 'getEnchantmentValue', 'onEntitySwing', 'getDefaultAttributeModifiers'}


def tooltip_lines(source: str) -> list[str]:
    """按顺序返回 appendHoverText 中 list.add(new TextComponent("...")) 的文本；Java 转义已还原。"""
    match = re.search(r'appendHoverText\([^)]*\)\s*\{(.*?)\n\t\}', source, re.S)
    if not match:
        return []
    lines = []
    for literal in re.findall(r'list\.add\(new TextComponent\(\s*"((?:[^"\\]|\\.)*)"\s*\)\)', match.group(1)):
        lines.append(re.sub(r'\\u([0-9A-Fa-f]{4})', lambda m: chr(int(m.group(1), 16)), literal).replace('\\"', '"').replace('\\\\', '\\'))
    if match.group(1).count('list.add(') != len(lines):
        raise ValueError('提示文本含未识别写法')
    return lines


def parse_item(source: str, tabs: dict[str, str]) -> dict:
    """解析直接继承 Item 的物品类；tabs 把 TAB_* 常量名映射为创造栏 ID。"""
    props = re.search(r'super\((new Item\.Properties\(\).*?)\);\n', source, re.S)
    chain = re.sub(r'\s+', ' ', props.group(1))
    row: dict = {}
    tab = re.search(r'\.tab\((?:\w+\.)?(\w+)\)', chain).group(1)
    if tab != 'null':
        row['tab'] = tabs[tab]
    stack = re.search(r'\.stacksTo\((\d+)\)', chain)
    if stack and int(stack.group(1)) != 64:
        row['stack'] = int(stack.group(1))
    rarity = re.search(r'\.rarity\(Rarity\.(\w+)\)', chain)
    if rarity and rarity.group(1) != 'COMMON':
        row['rarity'] = rarity.group(1)
    if '.fireResistant()' in chain:
        row['fire_resistant'] = True
    durability = re.search(r'\.durability\((\d+)\)', chain)
    if durability:
        row['durability'] = int(durability.group(1))
    food = re.search(r'\.nutrition\((-?\d+)\)\s*\.saturationMod\(([\d.]+)f\)(.*?)\.build\(\)', chain)
    if food:
        row['food'] = {'nutrition': int(food.group(1)), 'saturation_mod': float(food.group(2))}
        if '.alwaysEat()' in food.group(3):
            row['food']['always_eat'] = True
        if '.meat()' in food.group(3):
            row['food']['meat'] = True
        duration = re.search(r'getUseDuration\([^)]*\)\s*\{\s*return (\d+);', source)
        if duration:
            row['food']['use_ticks'] = int(duration.group(1))
    elif '.food(' in chain:
        raise ValueError('食物属性未识别：' + chain[:160])
    lines = tooltip_lines(source)
    if lines:
        row['tooltip'] = lines
    names = set(re.findall(r'@Override\s+public\s+[\w<>\[\], ?.]+\s+(\w+)\(', source))
    hooks = sorted(names & HOOKS)
    if hooks:
        row['pending_hooks'] = hooks
    unknown = sorted(names - HANDLED - HOOKS)
    if unknown:
        raise ValueError('覆写未识别：' + ','.join(unknown))
    return row


TOOL_KINDS = {'SwordItem': 'sword', 'AxeItem': 'axe', 'PickaxeItem': 'pickaxe', 'ShovelItem': 'shovel', 'TieredItem': 'tiered'}


def _ingredient(text: str, items: dict[str, str]) -> list[str]:
    """Ingredient.EMPTY 或 Ingredient.of(new ItemStack(ModItems.X.get()), ...) 的物品路径。"""
    if text.strip() == 'Ingredient.EMPTY':
        return []
    refs = re.findall(r'new ItemStack\(\w+ModItems\.(\w+)\.get\(\)\)', text)
    if not refs or re.sub(r'Ingredient\.of\(|new ItemStack\(\w+ModItems\.\w+\.get\(\)\)|[\s,)]', '', text):
        raise ValueError('修复材料未识别：' + text.strip()[:100])
    return [items[ref] for ref in refs]


def _getter(source: str, name: str) -> str:
    found = re.search(name + r'\([^)]*\)\s*\{\s*return (.*?);\s*\}', source, re.S)
    if not found:
        raise ValueError('缺少 ' + name)
    return found.group(1).strip()


def _number(text: str) -> float:
    return float(text.rstrip('fFdD'))


def parse_tool(source: str, base: str, tabs: dict[str, str], items: dict[str, str]) -> dict:
    """继承 SwordItem/AxeItem/PickaxeItem/ShovelItem/TieredItem 的工具：匿名 Tier 与攻击参数。"""
    tier = {
        'uses': int(_getter(source, 'getUses')),
        'speed': _number(_getter(source, 'getSpeed')),
        'damage_bonus': _number(_getter(source, 'getAttackDamageBonus')),
        'level': int(_getter(source, 'getLevel')),
        'enchantability': int(_getter(source, 'getEnchantmentValue')),
    }
    repair = _ingredient(_getter(source, 'getRepairIngredient'), items)
    if repair:
        tier['repair'] = repair
    tail = re.search(r'\}, (?:(\d+), (-?[\d.]+)f, )?(new Item\.Properties\(\).*?)\);\n', source, re.S)
    row = parse_item_properties(tail.group(3), tabs)
    tool = {'kind': TOOL_KINDS[base], 'tier': tier}
    if tail.group(1) is not None:
        tool['damage'] = int(tail.group(1))
        tool['attack_speed'] = float(tail.group(2))
    row['tool'] = tool
    _finish(source, row)
    return row


def parse_armor_material(source: str, items: dict[str, str]) -> dict:
    """ArmorItem 抽象基类里的匿名 ArmorMaterial。defense 的顺序为靴子、护腿、胸甲、头盔（EquipmentSlot 索引）。"""
    durability = re.search(r'getDurabilityForSlot.*?return new int\[\]\{13, 15, 16, 11\}\[slot\.getIndex\(\)\] \* (\d+);', source, re.S)
    defense = re.search(r'getDefenseForSlot.*?return new int\[\]\{(\d+), (\d+), (\d+), (\d+)\}\[slot\.getIndex\(\)\];', source, re.S)
    if not durability or not defense:
        raise ValueError('盔甲耐久或防御写法未识别')
    if _getter(source, 'getEquipSound') != 'ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation(""))':
        raise ValueError('盔甲装备音效未识别')
    material = {
        'id': _getter(source, 'getName').strip('"'),
        'durability_multiplier': int(durability.group(1)),
        'defense': [int(v) for v in defense.groups()],
        'enchantability': int(_getter(source, 'getEnchantmentValue')),
        'toughness': _number(_getter(source, 'getToughness')),
        'knockback': _number(_getter(source, 'getKnockbackResistance')),
    }
    repair = _ingredient(_getter(source, 'getRepairIngredient'), items)
    if repair:
        material['repair'] = repair
    return material


def parse_armor_piece(source: str, piece: str, tabs: dict[str, str]) -> dict:
    """盔甲部件内部类（Helmet 等）：创造栏与盔甲贴图（来源写成 getArmorTexture 的返回值）。"""
    body = re.search(r'public static class ' + piece + r' extends \w+ \{(.*?)\n\t\}', source, re.S).group(1)
    row = parse_item_properties(re.search(r'(new Item\.Properties\(\)[^;]*?)\);', body, re.S).group(1), tabs)
    texture = re.search(r'getArmorTexture\([^)]*\)\s*\{\s*return "(\w+):textures/models/armor/(\w+?)_layer_[12]\.png";', body)
    if texture is None:
        if re.search(r'getArmorTexture\([^)]*\)\s*\{\s*return "\w+:textures/entities/\w+\.png";', body):
            raise ValueError('盔甲使用自定义模型类与 textures/entities 单文件贴图，由所属系统随客户端模型迁移')
        raise ValueError('盔甲贴图写法未识别')
    row['armor_texture'] = texture.group(2)
    return row


def parse_item_properties(chain: str, tabs: dict[str, str]) -> dict:
    """解析 new Item.Properties() 链（创造栏、堆叠、稀有度、防火、耐久）。"""
    chain = re.sub(r'\s+', ' ', chain)
    row: dict = {}
    tab = re.search(r'\.tab\((?:\w+\.)?(\w+)\)', chain).group(1)
    if tab != 'null':
        row['tab'] = tabs[tab]
    if '.fireResistant()' in chain:
        row['fire_resistant'] = True
    return row


def _finish(source: str, row: dict) -> None:
    lines = tooltip_lines(source)
    if lines:
        row['tooltip'] = lines
    names = set(re.findall(r'@Override\s+public\s+[\w<>\[\], ?.]+\s+(\w+)\(', source))
    hooks = sorted(names & HOOKS)
    if hooks:
        row['pending_hooks'] = hooks
    unknown = sorted(names - HANDLED - HOOKS - {'getArmorTexture'})
    if unknown:
        raise ValueError('覆写未识别：' + ','.join(unknown))
