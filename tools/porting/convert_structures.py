"""把两个 1.18.2 来源的结构 NBT 迁入 1.21.1 数据包。

输出：
- src/main/resources/data/yvanchuxiuzhen/structure/<名>.nbt（GZip，其余内容与来源一致，DataVersion 保持 2975）
- docs/reference/conversion/structure_manifest.csv（来源、来源 SHA-1、目标、调色板中的方块名）

唯一的改写是把字符串值里出现的来源命名空间（`yvanchuxitong:`）换成本 Mod 的命名空间，覆盖方块调色板、方块实体 ID、
容器内物品 ID，以及方块实体里存放的任务、对话、商品等机械设定文本中嵌入的 ID（这些文本由所属系统解析）。
NBT 没有复合与列表的长度前缀，只有字符串带长度，因此按标签逐个走一遍，原样复制字节，遇到需要改写的字符串时重写其长度。
规则见 docs/development/engineering.md“资源转换管线”。
"""
from __future__ import annotations

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


class Rewriter:
    """按标签走一遍 NBT 字节流并改写字符串；记录调色板中的方块名。"""

    def __init__(self, data: bytes) -> None:
        self.data = data
        self.position = 0
        self.out = bytearray()
        self.palette_names: set[str] = set()
        self.in_palette = 0

    def copy(self, count: int) -> None:
        self.out += self.data[self.position:self.position + count]
        self.position += count

    def string(self) -> str:
        length = struct.unpack_from('>H', self.data, self.position)[0]
        raw = self.data[self.position + 2:self.position + 2 + length]
        self.position += 2 + length
        for prefix in OLD_PREFIXES:
            raw = raw.replace(prefix, f'{NAMESPACE}:'.encode())
        self.out += struct.pack('>H', len(raw)) + raw
        return raw.decode('utf-8', 'replace')

    def payload(self, tag: int, key: str = '') -> None:
        data = self.data
        if tag in FIXED_SIZES:
            self.copy(FIXED_SIZES[tag])
        elif tag == 7 or tag == 11 or tag == 12:
            count = struct.unpack_from('>i', data, self.position)[0]
            self.copy(4 + count * {7: 1, 11: 4, 12: 8}[tag])
        elif tag == 8:
            value = self.string()
            if key == 'Name' and self.in_palette:
                self.palette_names.add(value)
        elif tag == 9:
            element = data[self.position]
            count = struct.unpack_from('>i', data, self.position + 1)[0]
            self.copy(5)
            if element in FIXED_SIZES:
                self.copy(count * FIXED_SIZES[element])
            else:
                in_palette = key in ('palette', 'palettes')
                self.in_palette += in_palette
                for _ in range(count):
                    self.payload(element, key)
                self.in_palette -= in_palette
        elif tag == 10:
            while True:
                kind = data[self.position]
                self.copy(1)
                if kind == 0:
                    return
                length = struct.unpack_from('>H', data, self.position)[0]
                name = data[self.position + 2:self.position + 2 + length].decode('utf-8', 'replace')
                self.copy(2 + length)
                self.payload(kind, name)
        else:
            raise ValueError(f'未知 NBT 标签类型 {tag}')

    def run(self) -> bytes:
        tag = self.data[0]
        if tag != 10:
            raise ValueError('结构 NBT 的根不是复合标签')
        self.copy(1)
        length = struct.unpack_from('>H', self.data, self.position)[0]
        self.copy(2 + length)
        self.payload(10)
        if self.position != len(self.data):
            raise ValueError('NBT 末尾有多余字节')
        return bytes(self.out)


def main() -> None:
    manifest = []
    targets: dict[str, str] = {}
    DATA.mkdir(parents=True, exist_ok=True)
    for mod, (root, _) in SOURCES.items():
        directory = root / 'src/main/resources/data' / mod / 'structures'
        for file in sorted(directory.glob('*.nbt')):
            if file.name in targets:
                raise ValueError(f'结构 {file.name} 在两个来源中同名：{targets[file.name]} 与 {mod}')
            targets[file.name] = mod
            source = file.read_bytes()
            raw = gzip.decompress(source)
            rewriter = Rewriter(raw)
            rewritten = rewriter.run()
            remaining = rewritten.count(b'yvanchuxitong')
            if remaining:
                print(f'{file.name}: 改写后仍有 {remaining} 处 yvanchuxitong（不是“命名空间:”形式）')
            target = DATA / file.name
            # mtime 固定为 0，重复转换得到相同的字节。
            target.write_bytes(gzip.compress(rewritten, compresslevel=6, mtime=0))
            manifest.append((f'{mod}/{file.relative_to(root).as_posix()}', hashlib.sha1(source).hexdigest(),
                             target.relative_to(ROOT).as_posix(), ' '.join(sorted(rewriter.palette_names))))
    with MANIFEST.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['source', 'source_sha1', 'target', 'palette_names'])
        writer.writerows(manifest)
    print('structures', len(manifest))


if __name__ == '__main__':
    main()
