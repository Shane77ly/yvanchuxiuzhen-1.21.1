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
