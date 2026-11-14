"""把两个 1.18.2 来源的结构 NBT 迁入 1.21.1 数据包。

输出：
- src/main/resources/data/yvanchuxiuzhen/structure/<名>.nbt（GZip，DataVersion 保持 2975）
- docs/reference/conversion/structure_manifest.csv（来源、来源 SHA-1、目标、调色板中的方块名）

把字符串值里出现的来源命名空间（`yvanchuxitong:`）换成本 Mod 的命名空间，覆盖方块调色板、方块实体 ID、
容器内物品 ID，以及方块实体里存放的任务、对话、商品等机械设定文本中嵌入的 ID（这些文本由所属系统解析）。
实体/方块实体 nbt 的直接子键 ForgeData 改为 NeoForgeData，使来源结构保存的自定义字段进入目标持久数据。
传承之光方块实体的持久字段 weizhi 按十一种来源符头名称转为稳定整数编号，其他身份的同名字段保持原类型。
结构 entities[].nbt 及其 Passengers 中的来源 Mod 实体单独升级属性字段与已核实属性 ID；原版实体仍交由游戏数据修复器处理。
NBT 没有复合与列表的长度前缀，只有字符串带长度，因此按标签逐个走一遍，原样复制字节，遇到需要改写的字符串时重写其长度。
规则见 docs/development/engineering.md“资源转换管线”。
"""
from __future__ import annotations

import argparse
import csv
import gzip
import hashlib
import struct
from pathlib import Path

from build_ledger import NAMESPACE, ROOT
from convert_content import CONVERSION, SOURCES

DATA = ROOT / 'src/main/resources/data' / NAMESPACE / 'structure'
MANIFEST = CONVERSION / 'structure_manifest.csv'
OLD_PREFIXES = tuple(f'{mod}:'.encode() for mod in SOURCES if mod != NAMESPACE)
FIXED_SIZES = {1: 1, 2: 2, 3: 4, 4: 8, 5: 4, 6: 8}
INHERITANCE_ID = f'{NAMESPACE}:chuancheng'
INHERITANCE_HEADS = {name: index for index, name in enumerate((
    '风来神王', '炎君', '扫把星', '紫微帝君', '丹王', '八方剑魂',
    '大梦圣君', '天狼王', '六界渡者', '星河龟灵', '雷帝',
), 1)}
ATTRIBUTE_IDS = {
    'forge:entity_gravity': 'minecraft:generic.gravity',
    'forge:swim_speed': 'neoforge:swim_speed',
    'forge:nametag_distance': 'neoforge:nametag_distance',
}
# 来源 step_height_addition 全为 0 且没有修饰器；精确 Forge 40.1.0 未注册此项，保留 ID 并报告未映射。
# 不将加值 0 写为目标 generic.step_height 的绝对高度 0。
ATTRIBUTE_FIELDS = {'Name': 'id', 'Base': 'base', 'Modifiers': 'modifiers'}
MODIFIER_OPERATIONS = {0: 'add_value', 1: 'add_multiplied_base', 2: 'add_multiplied_total'}
# 本批全部 283 个来源修饰器均为此名称，按 AttributeModifierIdFix 的 NAME_MAP 转换。
MODIFIER_IDS = {'Random spawn bonus': 'minecraft:random_spawn_bonus'}


def nbt_string(value: str) -> bytes:
    raw = value.encode('utf-8')
    return struct.pack('>H', len(raw)) + raw


def named_string(name: str, value: str) -> bytes:
    return bytes((8,)) + nbt_string(name) + nbt_string(value)


class Rewriter:
    """按标签走一遍 NBT 字节流并改写字符串；记录调色板中的方块名。"""

    def __init__(self, data: bytes) -> None:
        self.data = data
        self.position = 0
        self.out = bytearray()
        self.palette_names: set[str] = set()
        self.in_palette = 0
        self.persistent_data_tags = 0
        self.inheritance_fields = 0
        self.attribute_entities = 0
        self.attribute_entries = 0
        self.attribute_modifiers = 0
        self.attribute_id_mappings = 0
        self.unmapped_attribute_ids: dict[str, int] = {}

    def copy(self, count: int) -> None:
        self.out += self.data[self.position:self.position + count]
        self.position += count

    def string(self, attribute_id: bool = False) -> str:
        length = struct.unpack_from('>H', self.data, self.position)[0]
        raw = self.data[self.position + 2:self.position + 2 + length]
        self.position += 2 + length
        for prefix in OLD_PREFIXES:
            raw = raw.replace(prefix, f'{NAMESPACE}:'.encode())
        if attribute_id:
            value = raw.decode('utf-8')
            if value in ATTRIBUTE_IDS:
                raw = ATTRIBUTE_IDS[value].encode('utf-8')
                self.attribute_id_mappings += 1
            elif value.startswith('forge:'):
                self.unmapped_attribute_ids[value] = self.unmapped_attribute_ids.get(value, 0) + 1
        self.out += struct.pack('>H', len(raw)) + raw
        return raw.decode('utf-8', 'replace')

    def attributes(self, start: int, end: int) -> bytes:
        """仅缓存单个实体的属性列表；实体身份读完后再决定是否替换。"""
        reader = Rewriter(bytes(self.out[start:end]))
        reader.copy(1)
        length = struct.unpack_from('>H', reader.data, reader.position)[0]
        reader.position += 2 + length
        reader.out += nbt_string('attributes')
        reader.payload(9, context='attributes')
        self.attribute_entities += 1
        self.attribute_entries += reader.attribute_entries
        self.attribute_modifiers += reader.attribute_modifiers
        self.attribute_id_mappings += reader.attribute_id_mappings
        for identity, count in reader.unmapped_attribute_ids.items():
            self.unmapped_attribute_ids[identity] = self.unmapped_attribute_ids.get(identity, 0) + count
        return bytes(reader.out)

    def payload(self, tag: int, key: str = '', persistent_fields: list[tuple[int, int, str]] | None = None,
                context: str = '') -> str | None:
        data = self.data
        if tag in FIXED_SIZES:
            self.copy(FIXED_SIZES[tag])
        elif tag == 7 or tag == 11 or tag == 12:
            count = struct.unpack_from('>i', data, self.position)[0]
            self.copy(4 + count * {7: 1, 11: 4, 12: 8}[tag])
        elif tag == 8:
            value = self.string(context == 'attribute' and key == 'Name')
            if key == 'Name' and self.in_palette:
                self.palette_names.add(value)
            return value
        elif tag == 9:
            element = data[self.position]
            count = struct.unpack_from('>i', data, self.position + 1)[0]
            self.copy(5)
            if element in FIXED_SIZES:
                self.copy(count * FIXED_SIZES[element])
            else:
                in_palette = key in ('palette', 'palettes')
                self.in_palette += in_palette
                child_context = {'entity_list': 'entity_wrapper', 'passengers': 'entity',
                                 'attributes': 'attribute', 'modifiers': 'modifier'}.get(context, '')
                for _ in range(count):
                    self.payload(element, key, context=child_context)
                self.in_palette -= in_palette
        elif tag == 10:
            entity_id = ''
            inheritance_fields: list[tuple[int, int, str]] = []
            attribute_fields: list[tuple[int, int]] = []
            edits: list[tuple[int, int, bytes]] = []
            modifier_name = ''
            modifier_name_span = (0, 0)
            while True:
                kind = data[self.position]
                tag_start = len(self.out)
                self.copy(1)
                if kind == 0:
                    if key == 'nbt' and entity_id == INHERITANCE_ID:
                        # 来源结构的 id 位于 ForgeData 之后，复合标签读完再按身份替换。
                        for start, end, value in inheritance_fields:
                            if value in INHERITANCE_HEADS:
                                name = b'weizhi'
                                edits.append((start, end, bytes((3,)) + struct.pack('>H', len(name)) + name + struct.pack('>i', INHERITANCE_HEADS[value])))
                                self.inheritance_fields += 1
                    if context == 'entity' and entity_id.partition(':')[0] in SOURCES:
                        for start, end in attribute_fields:
                            edits.append((start, end, self.attributes(start, end)))
                    if context == 'attribute':
                        self.attribute_entries += 1
                    if context == 'modifier':
                        if modifier_name not in MODIFIER_IDS:
                            raise ValueError(f'未经核实的来源属性修饰器：{modifier_name!r}')
                        edits.append((*modifier_name_span, named_string('id', MODIFIER_IDS[modifier_name])))
                        self.attribute_modifiers += 1
                    for start, end, replacement in sorted(edits, reverse=True):
                        self.out[start:end] = replacement
                    return
                length = struct.unpack_from('>H', data, self.position)[0]
                raw_name = data[self.position + 2:self.position + 2 + length]
                name = raw_name.decode('utf-8', 'replace')
                converted_name = ATTRIBUTE_FIELDS.get(name) if context == 'attribute' else None
                if context == 'modifier' and name in ('Amount', 'Operation'):
                    converted_name = name.lower()
                if key == 'nbt' and kind == 10 and name == 'ForgeData':
                    converted_name = 'NeoForgeData'
                    self.persistent_data_tags += 1
                if converted_name is not None:
                    self.position += 2 + length
                    self.out += nbt_string(converted_name)
                else:
                    self.copy(2 + length)
                fields = inheritance_fields if key == 'nbt' and kind == 10 and name == 'ForgeData' else None
                child_context = ''
                if context == 'structure' and kind == 9 and name == 'entities':
                    child_context = 'entity_list'
                elif context == 'entity_wrapper' and kind == 10 and name == 'nbt':
                    child_context = 'entity'
                elif context == 'entity' and kind == 9 and name == 'Passengers':
                    child_context = 'passengers'
                elif context == 'attribute' and kind == 9 and name == 'Modifiers':
                    child_context = 'modifiers'
                elif context == 'attribute' and kind == 8 and name == 'Name':
                    child_context = 'attribute'
                if context == 'modifier' and name == 'Operation':
                    if kind != 3:
                        raise ValueError('来源属性修饰器 Operation 不是整数标签')
                    operation = struct.unpack_from('>i', data, self.position)[0]
                    self.position += 4
                    self.out[tag_start] = 8
                    self.out += nbt_string(MODIFIER_OPERATIONS[operation])
                    value = None
                else:
                    value = self.payload(kind, name, fields, child_context)
                if (key == 'nbt' or context == 'entity') and kind == 8 and name == 'id':
                    entity_id = value
                if context == 'entity' and kind == 9 and name == 'Attributes':
                    attribute_fields.append((tag_start, len(self.out)))
                if context == 'modifier' and name == 'UUID':
                    edits.append((tag_start, len(self.out), b''))
                if context == 'modifier' and kind == 8 and name == 'Name':
                    modifier_name = value
                    modifier_name_span = (tag_start, len(self.out))
                if persistent_fields is not None and kind == 8 and name == 'weizhi':
                    persistent_fields.append((tag_start, len(self.out), value))
        else:
            raise ValueError(f'未知 NBT 标签类型 {tag}')

    def run(self) -> bytes:
        tag = self.data[0]
        if tag != 10:
            raise ValueError('结构 NBT 的根不是复合标签')
        self.copy(1)
        length = struct.unpack_from('>H', self.data, self.position)[0]
        self.copy(2 + length)
        self.payload(10, context='structure')
        if self.position != len(self.data):
            raise ValueError('NBT 末尾有多余字节')
        return bytes(self.out)


def select_structures(files: list[tuple[str, Path, Path]], names: list[str] | None) -> list[tuple[str, Path, Path]]:
    """按模板名选择，未知名称在写入任何输出前报错。"""
    if names is None:
        return files
    selected = {name.removesuffix('.nbt') for name in names}
    missing = selected - {file.stem for _, _, file in files}
    if missing:
        raise ValueError(f'未知来源结构：{", ".join(sorted(missing))}')
    return [row for row in files if row[2].stem in selected]


def main(argv: list[str] | None = None) -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument('--structure', action='append', metavar='NAME', help='只生成指定模板，可重复指定；省略时生成全部模板')
    args = parser.parse_args(argv)
    manifest = []
    persistent_data_tags = 0
    inheritance_fields = 0
    attribute_entities = 0
    attribute_entries = 0
    attribute_modifiers = 0
    attribute_id_mappings = 0
    unmapped_attribute_ids: dict[str, int] = {}
    targets: dict[str, str] = {}
    files = []
    for mod, (root, _) in SOURCES.items():
        directory = root / 'src/main/resources/data' / mod / 'structures'
        for file in sorted(directory.glob('*.nbt')):
            if file.name in targets:
                raise ValueError(f'结构 {file.name} 在两个来源中同名：{targets[file.name]} 与 {mod}')
            targets[file.name] = mod
            files.append((mod, root, file))
    files = select_structures(files, args.structure)
    existing_manifest = []
    if args.structure is not None:
        with MANIFEST.open(encoding='utf-8', newline='') as handle:
            existing_manifest = list(csv.reader(handle))[1:]
    DATA.mkdir(parents=True, exist_ok=True)
    for mod, root, file in files:
        source = file.read_bytes()
        raw = gzip.decompress(source)
        rewriter = Rewriter(raw)
        rewritten = rewriter.run()
        persistent_data_tags += rewriter.persistent_data_tags
        inheritance_fields += rewriter.inheritance_fields
        attribute_entities += rewriter.attribute_entities
        attribute_entries += rewriter.attribute_entries
        attribute_modifiers += rewriter.attribute_modifiers
        attribute_id_mappings += rewriter.attribute_id_mappings
        for identity, count in rewriter.unmapped_attribute_ids.items():
            unmapped_attribute_ids[identity] = unmapped_attribute_ids.get(identity, 0) + count
        remaining = rewritten.count(b'yvanchuxitong')
        if remaining:
            print(f'{file.name}: 改写后仍有 {remaining} 处 yvanchuxitong（不是“命名空间:”形式）')
        target = DATA / file.name
        # mtime 固定为 0，重复转换得到相同的字节。
        target.write_bytes(gzip.compress(rewritten, compresslevel=6, mtime=0))
        manifest.append((f'{mod}/{file.relative_to(root).as_posix()}', hashlib.sha1(source).hexdigest(),
                         target.relative_to(ROOT).as_posix(), ' '.join(sorted(rewriter.palette_names))))
    generated = len(manifest)
    if args.structure is not None:
        replacements = {row[2]: row for row in manifest}
        manifest = [replacements.pop(row[2], row) for row in existing_manifest]
        manifest.extend(replacements.values())
    with MANIFEST.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['source', 'source_sha1', 'target', 'palette_names'])
        writer.writerows(manifest)
    print('structures', generated, 'persistent_data_tags', persistent_data_tags, 'inheritance_fields', inheritance_fields,
          'attribute_entities', attribute_entities, 'attribute_entries', attribute_entries,
          'attribute_modifiers', attribute_modifiers, 'attribute_id_mappings', attribute_id_mappings)
    if unmapped_attribute_ids:
        print('unmapped_attribute_ids', dict(sorted(unmapped_attribute_ids.items())))


if __name__ == '__main__':
    main()
