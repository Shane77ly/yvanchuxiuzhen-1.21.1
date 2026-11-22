"""把来源植物扩展的静态堆肥概率与燃料时长转换为NeoForge物品数据映射。"""
from __future__ import annotations

import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

DATA = ROOT / 'src/main/resources/data/neoforge/data_maps/item'
MOD = 'yvanchuxiuzhen'


def generate(converter) -> None:
    source_root, prefix = SOURCES[MOD]
    init = source_root / f'src/main/java/net/mcreator/{MOD}/init'
    symbols = {}
    registrations = []
    for kind in ('Blocks', 'Items'):
        source = init / f'{prefix}Mod{kind}.java'
        registrations.append((MOD, source))
        for symbol, identifier in re.findall(
                r'RegistryObject<\w+>\s+(\w+)\s*=\s*REGISTRY.register\("(\w+)"',
                source.read_text(encoding='utf-8')):
            symbols[(kind, symbol)] = f'{NAMESPACE}:{identifier}'

    definitions = (
        ('CompostableItems', 'ComposterBlock.COMPOSTABLES.put(', 'compostables',
         r'ComposterBlock\.COMPOSTABLES\.put\(YvanchuxiuzhenMod(Blocks|Items)\.(\w+)\.get\(\)'
         r'(?:\.asItem\(\))?,([0-9.]+)f\);'),
        ('Fuels', 'event.setBurnTime(', 'furnace_fuels',
         r'if\(itemstack.getItem\(\)==YvanchuxiuzhenMod(Blocks|Items)\.(\w+)\.get\(\)'
         r'(?:\.asItem\(\))?\)event\.setBurnTime\((\d+)\);'),
    )
    for suffix, call, name, pattern in definitions:
        source = init / f'{prefix}Mod{suffix}.java'
        text = re.sub(r'\s+', '', source.read_text(encoding='utf-8'))
        entries = re.findall(pattern, text)
        if len(entries) != text.count(call):
            raise ValueError(f'来源{name}包含未识别的调用：{source}')
        values = {}
        for kind, symbol, value in entries:
            identifier = symbols[(kind, symbol)]
            if identifier in values:
                raise ValueError(f'来源{name}重复写入物品：{identifier}')
            if not converter.registered.has('item', identifier):
                raise ValueError(f'来源{name}引用尚未登记的物品：{identifier}')
            if name == 'compostables':
                values[identifier] = {'chance': float(value), 'can_villager_compost': False}
            else:
                values[identifier] = {'burn_time': int(value)}
        converter.write(DATA / f'{name}.json', {'replace': False, 'values': values},
                        [*registrations, (MOD, source)])
