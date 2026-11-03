"""把已写入内容表的方块与物品所需的模型、方块状态与纹理从 1.18.2 来源迁入 src/main/resources/assets（B1）。

按内容表中的 ID 追溯来源：方块读 blockstates/<路径>.json，物品读 models/item/<路径>.json，递归复制 parent 与引用的模型，
再复制模型引用的纹理。目录 blocks/、items/ 改为 1.21.1 的 block/、item/，两个来源命名空间统一为 yvanchuxiuzhen，
JSON 中的引用同步改写。方块 Java 通过 registerRenderLayer 声明的渲染层写成模型的 render_type（NeoForge 读取）。
每个输出文件登记到 docs/reference/conversion/manifest.csv（来源路径、来源哈希、目标路径）。
同一目标路径来自内容不同的来源文件时报错。
"""
from __future__ import annotations

import csv
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

from build_ledger import NAMESPACE, ROOT, read_ledgers
from convert_content import CONTENT, SOURCES

ASSETS = ROOT / 'src/main/resources/assets' / NAMESPACE
MANIFEST = ROOT / 'docs/reference/conversion/manifest.csv'
RENDER_TYPES = {'cutout': 'minecraft:cutout', 'cutoutMipped': 'minecraft:cutout_mipped', 'translucent': 'minecraft:translucent'}
TEXTURE_DIRS = {'blocks': 'block', 'items': 'item'}
VANILLA_RESOURCES = ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar'


class Converter:
    def __init__(self) -> None:
        self.roots = {mod: root / 'src/main/resources/assets' for mod, (root, _) in SOURCES.items()}
        self.written: dict[Path, str] = {}
        self.manifest: list[tuple[str, str, str]] = []
        self.missing: list[tuple[str, str]] = []
        self.dropped_parents: list[tuple[str, str]] = []
        with zipfile.ZipFile(VANILLA_RESOURCES) as archive:
            self.vanilla_models = {n for n in archive.namelist() if n.startswith('assets/minecraft/models/')}

    def rewrite_ref(self, ref: str, texture: bool = False) -> str:
        if ref.startswith('#'):
            return ref
        namespace, _, path = ref.rpartition(':') if ':' in ref else ('', '', ref)
        if namespace in SOURCES:
            namespace = NAMESPACE
        head, _, rest = path.partition('/')
        if head in TEXTURE_DIRS:
            path = f'{TEXTURE_DIRS[head]}/{rest}'
        return f'{namespace}:{path}' if namespace else path

    def rewrite_json(self, node, key: str = ''):
        if isinstance(node, dict):
            out = {}
            for k, v in node.items():
                if k in ('parent', 'model') and isinstance(v, str):
                    out[k] = self.rewrite_ref(v)
                elif k == 'textures' and isinstance(v, dict):
                    out[k] = {name: self.rewrite_ref(ref, True) for name, ref in v.items()}
                else:
                    out[k] = self.rewrite_json(v, k)
            return out
        if isinstance(node, list):
            return [self.rewrite_json(item, key) for item in node]
        return node

    def emit(self, source: Path, mod: str, target: Path, content: bytes) -> None:
        digest = hashlib.sha1(content).hexdigest()
        previous = self.written.get(target)
        if previous is not None:
            if previous != digest:
                raise ValueError(f'目标 {target.relative_to(ROOT).as_posix()} 来自内容不同的来源文件：{source}')
            return
        self.written[target] = digest
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
        origin = hashlib.sha1(source.read_bytes()).hexdigest()
        self.manifest.append((f'{mod}/{source.relative_to(self.roots[mod].parents[3]).as_posix()}', origin,
                              target.relative_to(ROOT).as_posix()))

    def source_file(self, ref: str, kind: str, suffix: str, default_mod: str) -> tuple[Path, str] | None:
        namespace, _, path = ref.rpartition(':') if ':' in ref else ('minecraft', '', ref)
        if namespace == 'minecraft':
            return None
        file = self.roots[namespace] / namespace / kind / f'{path}{suffix}'
        return (file, namespace) if file.is_file() else (file, namespace)

    def copy_texture(self, ref: str, default_mod: str) -> None:
        located = self.source_file(ref, 'textures', '.png', default_mod)
        if located is None:
            return
        file, mod = located
        if not file.is_file():
            self.missing.append((ref, '纹理'))
            return
        relative = file.relative_to(self.roots[mod] / mod / 'textures').as_posix()
        head, _, rest = relative.partition('/')
        relative = f'{TEXTURE_DIRS[head]}/{rest}' if head in TEXTURE_DIRS else relative
        target = ASSETS / 'textures' / relative
        self.emit(file, mod, target, file.read_bytes())
        meta = file.with_name(file.name + '.mcmeta')
        if meta.is_file():
            self.emit(meta, mod, target.with_name(target.name + '.mcmeta'), meta.read_bytes())

    def copy_model(self, ref: str, default_mod: str) -> None:
        located = self.source_file(ref, 'models', '.json', default_mod)
        if located is None:
            return
        file, mod = located
        if not file.is_file():
            self.missing.append((ref, '模型'))
            return
        relative = file.relative_to(self.roots[mod] / mod / 'models').as_posix()
        target = ASSETS / 'models' / relative
        if target in self.written:
            return
        data = json.loads(file.read_text(encoding='utf-8'))
        if 'loader' in data:
            raise ValueError(f'模型 {ref} 使用自定义加载器 {data["loader"]}')
        parent = data.get('parent')
        if parent is not None and (':' not in parent or parent.startswith('minecraft:')):
            vanilla = f'assets/minecraft/models/{parent.split(":")[-1]}.json'
            if vanilla not in self.vanilla_models:
                # 来源模型（Blockbench 导出）的 parent 指向不存在的原版模型：两个版本都回退为缺失模型的父级，不影响自身的 elements。
                self.dropped_parents.append((ref, parent))
                del data['parent']
        self.emit(file, mod, target, json_bytes(self.rewrite_json(data)))
        if 'parent' in data:
            self.copy_model(data['parent'], mod)
        for texture in data.get('textures', {}).values():
            if not texture.startswith('#'):
                self.copy_texture(texture, mod)

    def wrap_render_type(self, ref: str, render_type: str) -> str:
        """为方块模型生成带 render_type 的子模型，返回子模型的引用。"""
        namespace, _, path = ref.partition(':')
        suffix = render_type.split(':')[1]
        wrapper = f'{path}__{suffix}'
        target = ASSETS / 'models' / f'{wrapper.split(":")[-1]}.json'
        if target not in self.written:
            self.written[target] = 'generated'
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(json_bytes({'parent': f'{NAMESPACE}:{path}', 'render_type': render_type}))
            self.manifest.append(('(generated)', '', target.relative_to(ROOT).as_posix()))
        return f'{NAMESPACE}:{wrapper}'

    def block(self, mod: str, path: str, target: str, render_type: str | None) -> dict[str, str]:
        file = self.roots[mod] / mod / 'blockstates' / f'{path}.json'
        if not file.is_file():
            self.missing.append((f'{mod}:{path}', '方块状态'))
            return {}
        data = self.rewrite_json(json.loads(file.read_text(encoding='utf-8')))
        wrappers: dict[str, str] = {}

        def visit(node):
            if isinstance(node, dict):
                for key, value in node.items():
                    if key == 'model' and isinstance(value, str):
                        if render_type:
                            node[key] = wrappers.setdefault(value, self.wrap_render_type(value, render_type))
                    else:
                        visit(value)
            elif isinstance(node, list):
                for item in node:
                    visit(item)

        original = json.loads(file.read_text(encoding='utf-8'))
        for ref in collect_models(original):
            self.copy_model(ref, mod)
        visit(data)
        self.emit(file, mod, ASSETS / 'blockstates' / f'{target}.json', json_bytes(data))
        return wrappers

    def armor_textures(self, name: str) -> None:
        """盔甲材质的两层贴图 textures/models/armor/<名>_layer_1.png、_layer_2.png，在两个来源中查找。"""
        for layer in (1, 2):
            relative = f'models/armor/{name}_layer_{layer}.png'
            found = [(mod, self.roots[mod] / mod / 'textures' / relative) for mod in SOURCES if (self.roots[mod] / mod / 'textures' / relative).is_file()]
            if not found:
                self.missing.append((relative, '盔甲贴图'))
                continue
            mod, file = found[0]
            self.emit(file, mod, ASSETS / 'textures' / relative, file.read_bytes())

    def sound(self, name: str) -> None:
        """sounds.json 引用的声音文件 sounds/<名>.ogg，在两个来源中查找。"""
        relative = f'sounds/{name}.ogg'
        found = [(mod, self.roots[mod] / mod / relative) for mod in SOURCES if (self.roots[mod] / mod / relative).is_file()]
        if not found:
            self.missing.append((relative, '声音'))
            return
        mod, file = found[0]
        self.emit(file, mod, ASSETS / relative, file.read_bytes())

    def item(self, mod: str, path: str, target: str, parents: dict[str, str]) -> None:
        file = self.roots[mod] / mod / 'models/item' / f'{path}.json'
        if not file.is_file():
            self.missing.append((f'{mod}:{path}', '物品模型'))
            return
        data = json.loads(file.read_text(encoding='utf-8'))
        for ref in collect_models(data):
            self.copy_model(ref, mod)
        for texture in data.get('textures', {}).values():
            if not texture.startswith('#'):
                self.copy_texture(texture, mod)
        rewritten = self.rewrite_json(data)
        parent = rewritten.get('parent')
        if parent in parents:
            rewritten['parent'] = parents[parent]
        self.emit(file, mod, ASSETS / 'models/item' / f'{target}.json', json_bytes(rewritten))


def collect_models(node) -> list[str]:
    found = []
    if isinstance(node, dict):
        for key, value in node.items():
            if key in ('model', 'parent') and isinstance(value, str):
                found.append(value)
            else:
                found += collect_models(value)
    elif isinstance(node, list):
        for item in node:
            found += collect_models(item)
    return found


def json_bytes(data) -> bytes:
    return (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8')


def main() -> None:
    converter = Converter()
    origin: dict[tuple[str, str], tuple[str, str]] = {}
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] in ('block', 'item'):
                mod, path = row['legacy_id'].split(':', 1)
                origin[(row['registry'], row['target_id'].split(':', 1)[1])] = (mod, path)
    layers = {}
    for mod, (root, prefix) in SOURCES.items():
        text = (root / f'src/main/java/net/mcreator/{mod}/init/{prefix}ModBlocks.java').read_text(encoding='utf-8')
        classes = dict(re.findall(r'register\("(\w+)", \(\) -> new (\w+)\(\)\)', text))
        for path, cls in classes.items():
            java = (root / f'src/main/java/net/mcreator/{mod}/block/{cls}.java').read_text(encoding='utf-8')
            found = re.search(r'renderType == RenderType\.(\w+)\(\)', java)
            if found:
                layers[(mod, path)] = RENDER_TYPES[found.group(1)]
    block_wrappers: dict[str, dict[str, str]] = {}
    units = sorted(p.name for p in CONTENT.iterdir() if p.is_dir() and p.name != 'platform')
    for unit in units:
        file = CONTENT / unit / 'blocks.json'
        if file.is_file():
            for row in json.loads(file.read_text(encoding='utf-8')):
                mod, path = origin[('block', row['id'])]
                block_wrappers[row['id']] = converter.block(mod, path, row['id'], layers.get((mod, path)))
    for unit in units:
        file = CONTENT / unit / 'items.json'
        if file.is_file():
            for row in json.loads(file.read_text(encoding='utf-8')):
                mod, path = origin[('item', row['id'])]
                parents = block_wrappers.get(row['block'], {}) if row['factory'] == 'block_item' else {}
                converter.item(mod, path, row['id'], parents)
    for unit in units:
        file = CONTENT / unit / 'armor_materials.json'
        if file.is_file():
            for row in json.loads(file.read_text(encoding='utf-8')):
                converter.armor_textures(row['texture'])
    for definition in json.loads((ROOT / 'src/main/sounds/platform.json').read_text(encoding='utf-8')).values():
        for sound in definition['sounds']:
            converter.sound(sound['name'].split(':', 1)[1])
    MANIFEST.parent.mkdir(parents=True, exist_ok=True)
    with MANIFEST.open('w', encoding='utf-8', newline='') as handle:
        writer = csv.writer(handle, lineterminator='\n')
        writer.writerow(['source', 'source_sha1', 'target'])
        writer.writerows(sorted(converter.manifest, key=lambda r: r[2]))
    print('files', len(converter.manifest), 'missing', len(converter.missing), 'dropped parents', len(converter.dropped_parents))
    for ref, kind in sorted(set(converter.missing))[:15]:
        print('  缺少', kind, ref)


if __name__ == '__main__':
    sys.exit(main())
