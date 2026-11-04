"""把来源的 MCreator 过程压缩成便于阅读的文本（只用于归属阅读和行为核对，不改变源码，也不是转换工具）。

做的事：把 Unicode 转义还原成中文；去掉 import 与空行；把玩家变量读取、生命、效果等级、附魔等级、主副手与护甲槽位、
实体 UUID 查找、体质物品键读取、游戏模式判断、播放声音、符箓复活块等 MCreator 样板折叠成短写法。
折叠后的短写法只在这里使用（如 `ench(ENCH.X, main(e))`、`e.PV.字段`），原文以来源文件为准。

用法：`python -B -X utf8 tools/porting/read_procedure.py <过程名>...`，名称不含 `Procedure` 后缀也可；
前缀 `t:` 读原初系统的文件；环境变量 `DEPTH=1` 在每行前显示花括号深度，用来核对分支嵌套。
"""
from __future__ import annotations

import os
import re
import sys
from pathlib import Path

from convert_content import SOURCES

ROOTS = {mod: root / f'src/main/java/net/mcreator/{mod}' for mod, (root, _) in SOURCES.items()}

PV = re.compile(r'\(\(?(\w+(?:\.\w+\(\))?)\.getCapability\(\w+ModVariables\.PLAYER_VARIABLES_CAPABILITY, null\)\.orElse\(new \w+ModVariables\.PlayerVariables\(\)\)\)\)?\.(\w+)')
HP = re.compile(r'\((\w+) instanceof LivingEntity _livEnt \? _livEnt\.getHealth\(\) : -1\)')
HP2 = re.compile(r'(\w+) instanceof LivingEntity _livEnt \? _livEnt\.getHealth\(\) : -1')
HAS = re.compile(r'(\w+) instanceof LivingEntity _livEnt \? _livEnt\.hasEffect\((\w+\.\w+\.\w+)\.get\(\)\) : false')
SCORE = re.compile(r'new Object\(\) \{\s*public int getScore\(String score, Entity _ent\) \{.*?\}\s*\}\.getScore\(("[^"]*"|\w+), (\w+)\)', re.S)
CONV = re.compile(r'new Object\(\) \{\s*double convert\(String s\) \{.*?\}\s*\}\.convert\(', re.S)
UNI = re.compile(r'\\u([0-9A-Fa-f]{4})')

UUIDFN = re.compile(r'\(?world instanceof ServerLevel _serverLevelForGettingEntity \? \(new Function<String, Entity>\(\) \{.*?\}\)\.apply\(([^)]*)\) : null\)?', re.S)
ENCHL = re.compile(r'EnchantmentHelper\.getItemEnchantmentLevel\(')
MAINH = re.compile(r'\((\w+) instanceof LivingEntity _livEnt \? _livEnt\.getMainHandItem\(\) : ItemStack\.EMPTY\)')
OFFH = re.compile(r'\((\w+) instanceof LivingEntity _livEnt \? _livEnt\.getOffhandItem\(\) : ItemStack\.EMPTY\)')
TAGIS = re.compile(r'(\w+)\.getType\(\)\.is\(TagKey\.create\(Registry\.ENTITY_TYPE_REGISTRY, new ResourceLocation\(("[^"]*")\)\)\)')
NBTD = re.compile(r'\.getOrCreateTag\(\)\)?\.getDouble\(')
NBTD2 = re.compile(r'\.getOrCreateTag\(\)\.getString\(')


PLAY = re.compile(r'if \(world instanceof Level _level\) \{\s*if \(!_level\.isClientSide\(\)\) \{\s*_level\.playSound\(null, [^;]*?getValue\(new ResourceLocation\(("[^"]*")\)\), SoundSource\.(\w+), ([^,]+), ([^)]+)\);\s*\} else \{.*?\}\s*\}', re.S)
FULU = 'getInventory().contains(new ItemStack(ITEM.ITEMFULU_16))'


def collapse_blocks(text: str) -> str:
    while True:
        i = text.find(FULU)
        if i < 0:
            return text
        start = text.rfind(chr(10), 0, i) + 1
        j = text.find('{', i)
        depth, k = 0, j
        while k < len(text):
            if text[k] == '{':
                depth += 1
            elif text[k] == '}':
                depth -= 1
                if depth == 0:
                    break
            k += 1
        text = text[:start] + ' [FULU16-REVIVE-BLOCK]' + text[k + 1:]


GAMEMODE = re.compile(r'new Object\(\) \{\s*public boolean checkGamemode\(Entity _ent\) \{.*?GameType\.(\w+);.*?\}\.checkGamemode\((\w+)\)', re.S)


def condense(text: str) -> str:
    text = UNI.sub(lambda m: chr(int(m[1], 16)), text)
    text = '\n'.join(l for l in text.splitlines() if l.strip() and not l.startswith('import ') and not l.startswith('package '))
    text = PV.sub(lambda m: f'{m[1]}.PV.{m[2]}', text)
    text = HAS.sub(lambda m: f'hasEffect({m[1]},{m[2]})', text)
    text = HP.sub(lambda m: f'HP({m[1]})', text)
    text = HP2.sub(lambda m: f'HP({m[1]})', text)
    text = SCORE.sub(lambda m: f'score({m[1]},{m[2]})', text)
    text = CONV.sub('num(', text)
    text = re.sub(r'YvanchuxiuzhenModEnchantments\.(\w+)\.get\(\)', r'ENCH.\1', text)
    text = re.sub(r'YvanchuxiuzhenModMobEffects\.(\w+)\.get\(\)', r'EFFECT.\1', text)
    text = re.sub(r'YvanchuxiuzhenModItems\.(\w+)\.get\(\)', r'ITEM.\1', text)
    text = re.sub(r'YvanchuxiuzhenModBlocks\.(\w+)\.get\(\)', r'BLOCK.\1', text)
    text = re.sub(r'\(entity instanceof LivingEntity _entGetArmor \? _entGetArmor\.getItemBySlot\(EquipmentSlot\.(\w+)\) : ItemStack\.EMPTY\)', r'armor(\1)', text)
    text = re.sub(r'\(\((\w+) instanceof LivingEntity _entGetArmor \? _entGetArmor\.getItemBySlot\(EquipmentSlot\.(\w+)\) : ItemStack\.EMPTY\)\)', r'armor(\1,\2)', text)
    text = PLAY.sub(lambda m: f'playSound({m[1]},{m[2]})', text)
    text = collapse_blocks(text)
    text = GAMEMODE.sub(lambda m: f'gamemode({m[2]},{m[1]})', text)
    text = UUIDFN.sub(lambda m: f'byUUID({m[1]})', text)
    text = ENCHL.sub('ench(', text)
    text = MAINH.sub(lambda m: f'main({m[1]})', text)
    text = OFFH.sub(lambda m: f'off({m[1]})', text)
    text = TAGIS.sub(lambda m: f'isTag({m[1]},{m[2]})', text)
    text = NBTD.sub('.nbtD(', text)
    text = NBTD2.sub('.nbtS(', text)
    text = re.sub(r'\((\w+) instanceof LivingEntity _livEnt && _livEnt\.hasEffect\((\w+\.\w+)\) \? _livEnt\.getEffect\(\2\)\.getAmplifier\(\) : 0\)', r'amp(\1,\2)', text)
    text = re.sub(r'\t', ' ', text)
    text = re.sub(r' {2,}', ' ', text)
    return text


def main() -> None:
    for name in sys.argv[1:]:
        root = ROOTS['yvanchuxiuzhen']
        if name.startswith('t:'):
            root, name = ROOTS['yvanchuxitong'], name[2:]
        found = list(root.rglob(name if name.endswith('.java') else name + '.java'))
        if not found:
            found = list(root.rglob(name + 'Procedure.java'))
        for path in found:
            body = condense(path.read_text(encoding='utf-8-sig'))
            print(f'=== {path.name} ({len(body.splitlines())} 行)')
            depth = 0
            for number, line in enumerate(body.splitlines(), 1):
                prefix = f'{number}[{depth}]' if os.environ.get('DEPTH') else f'{number}'
                print(f'{prefix}: {line}')
                depth += line.count('{') - line.count('}')


if __name__ == '__main__':
    main()
