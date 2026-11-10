"""把已写入内容表的方块与物品所需的模型、方块状态与纹理从 1.18.2 来源迁入 src/main/resources/assets（B1）。

按内容表中的 ID 追溯来源：方块读 blockstates/<路径>.json，物品读 models/item/<路径>.json，递归复制 parent 与引用的模型，
再复制模型引用的纹理。目录 blocks/、items/ 改为 1.21.1 的 block/、item/，两个来源命名空间统一为 yvanchuxiuzhen，
JSON 中的引用同步改写。方块 Java 通过 registerRenderLayer 声明的渲染层写成模型的 render_type（NeoForge 读取）。
已迁移界面使用的贴图按 docs/reference/conversion/screen_textures.csv（所属单位、来源纹理 ID）复制；
所属系统自己注册的方块、物品（不在内容表中）与其另需的纹理按 docs/reference/conversion/system_assets.csv
（所属单位、种类 block/item/texture/geo/animation、目标 ID 或来源纹理 ID）迁移，geo、animation 是 GeckoLib 的几何与动画文件。来源桶模型的 forge:bucket 加载器改写为 neoforge:fluid_container。
每个输出文件登记到 docs/reference/conversion/manifest.csv（来源路径、来源哈希、目标路径）。
同一目标路径来自内容不同的来源文件时报错。
已登记状态效果的图标按来源注册 ID 读取 textures/mob_effect，按台账目标 ID 写入同一图集目录。
已登记粒子的 JSON 与全部 sprite 按来源注册 ID 读取，目标 JSON ID 跟随台账，纹理引用统一命名空间并保留帧顺序。
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
        self.created_dirs: set[Path] = set()
        self.copied_textures: set[str] = set()
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

    def prepare_parent(self, target: Path) -> None:
        if target.parent not in self.created_dirs:
            target.parent.mkdir(parents=True, exist_ok=True)
            self.created_dirs.add(target.parent)

    def emit_bytes(self, source: Path, mod: str, target: Path) -> None:
        content = source.read_bytes()
        self.emit(source, mod, target, content, content)

    def emit(self, source: Path, mod: str, target: Path, content: bytes, source_content: bytes | None = None) -> None:
        digest = hashlib.sha1(content).hexdigest()
        previous = self.written.get(target)
        if previous is not None:
            if previous != digest:
                raise ValueError(f'目标 {target.relative_to(ROOT).as_posix()} 来自内容不同的来源文件：{source}')
            return
        self.written[target] = digest
        self.prepare_parent(target)
        if not target.is_file() or target.read_bytes() != content:
            target.write_bytes(content)
        origin = hashlib.sha1(source.read_bytes() if source_content is None else source_content).hexdigest()
        self.manifest.append((f'{mod}/{source.relative_to(self.roots[mod].parents[3]).as_posix()}', origin,
                              target.relative_to(ROOT).as_posix()))

    def source_file(self, ref: str, kind: str, suffix: str, default_mod: str) -> tuple[Path, str] | None:
        namespace, _, path = ref.rpartition(':') if ':' in ref else ('minecraft', '', ref)
        if namespace == 'minecraft':
            return None
        file = self.roots[namespace] / namespace / kind / f'{path}{suffix}'
        return file, namespace

    def copy_texture(self, ref: str, default_mod: str) -> None:
        if ref in self.copied_textures:
            return
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
        self.emit_bytes(file, mod, target)
        meta = file.with_name(file.name + '.mcmeta')
        if meta.is_file():
            self.emit_bytes(meta, mod, target.with_name(target.name + '.mcmeta'))
        self.copied_textures.add(ref)

    def copy_plain(self, ref: str, folder: str, suffix: str, default_mod: str) -> None:
        """原样迁移 GeckoLib 的几何（geo/<名>.geo.json）与动画（animations/<名>.animation.json）。"""
        located = self.source_file(ref, folder, suffix, default_mod)
        file, mod = located
        if not file.is_file():
            self.missing.append((ref, folder))
            return
        self.emit_bytes(file, mod, ASSETS / folder / file.name)

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
            # ModelBakery 直接提供这三个内建模型，它们不对应资源包内的 JSON 文件。
            if parent.split(':')[-1] not in ('builtin/generated', 'builtin/entity', 'builtin/missing') and vanilla not in self.vanilla_models:
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
            self.prepare_parent(target)
            content = json_bytes({'parent': f'{NAMESPACE}:{path}', 'render_type': render_type})
            if not target.is_file() or target.read_bytes() != content:
                target.write_bytes(content)
            self.manifest.append(('(generated)', '', target.relative_to(ROOT).as_posix()))
        return f'{NAMESPACE}:{wrapper}'

    def block(self, mod: str, path: str, target: str, render_type: str | None) -> dict[str, str]:
        file = self.roots[mod] / mod / 'blockstates' / f'{path}.json'
        if not file.is_file():
            self.missing.append((f'{mod}:{path}', '方块状态'))
            return {}
        original = json.loads(file.read_text(encoding='utf-8'))
        data = self.rewrite_json(original)
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
            self.emit_bytes(file, mod, ASSETS / 'textures' / relative)

    def sound(self, name: str) -> None:
        """sounds.json 引用的声音文件 sounds/<名>.ogg，在两个来源中查找。"""
        relative = f'sounds/{name}.ogg'
        found = [(mod, self.roots[mod] / mod / relative) for mod in SOURCES if (self.roots[mod] / mod / relative).is_file()]
        if not found:
            self.missing.append((relative, '声音'))
            return
        mod, file = found[0]
        self.emit_bytes(file, mod, ASSETS / relative)

    def effect_icon(self, mod: str, path: str, target: str) -> None:
        """状态效果图标按注册 ID 从 mob_effect 图集读取，目标路径跟随台账映射。"""
        file = self.roots[mod] / mod / 'textures/mob_effect' / f'{path}.png'
        if not file.is_file():
            self.missing.append((f'{mod}:{path}', '状态效果图标'))
            return
        self.emit_bytes(file, mod, ASSETS / 'textures/mob_effect' / f'{target}.png')

    def particle(self, mod: str, path: str, target: str) -> None:
        """粒子 JSON 的 sprite 引用不含 particle/ 前缀，图集从 textures/particle 读取。"""
        file = self.roots[mod] / mod / 'particles' / f'{path}.json'
        data = json.loads(file.read_text(encoding='utf-8'))
        textures = []
        for ref in data['textures']:
            namespace, name = ref.split(':', 1)
            self.copy_texture(f'{namespace}:particle/{name}', mod)
            textures.append(f'{NAMESPACE if namespace in SOURCES else namespace}:{name}')
        data['textures'] = textures
        self.emit(file, mod, ASSETS / 'particles' / f'{target}.json', json_bytes(data))

    def item(self, mod: str, path: str, target: str, parents: dict[str, str]) -> None:
        file = self.roots[mod] / mod / 'models/item' / f'{path}.json'
        if not file.is_file():
            self.missing.append((f'{mod}:{path}', '物品模型'))
            return
        data = json.loads(file.read_text(encoding='utf-8'))
        if data.get('loader') == 'forge:bucket':
            bucket = {'parent': 'neoforge:item/bucket_drip', 'loader': 'neoforge:fluid_container', 'fluid': self.rewrite_ref(data['fluid'])}
            self.emit(file, mod, ASSETS / 'models/item' / f'{target}.json', json_bytes(bucket))
            return
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
    effects: list[dict] = []
    particles: list[dict] = []
    for rows in read_ledgers().values():
        for row in rows:
            if row['registry'] in ('block', 'item'):
                mod, path = row['legacy_id'].split(':', 1)
                origin[(row['registry'], row['target_id'].split(':', 1)[1])] = (mod, path)
            elif row['registry'] == 'mob_effect' and row['status'] != '待处理':
                effects.append(row)
            elif row['registry'] == 'particle_type' and row['status'] != '待处理':
                particles.append(row)
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
                if not row.get('animated', False):
                    converter.armor_textures(row['texture'])
    for row in effects:
        mod, path = row['legacy_id'].split(':', 1)
        converter.effect_icon(mod, path, row['target_id'].split(':', 1)[1])
    for row in particles:
        mod, path = row['legacy_id'].split(':', 1)
        converter.particle(mod, path, row['target_id'].split(':', 1)[1])
    with (ROOT / 'docs/reference/conversion/screen_textures.csv').open(encoding='utf-8') as handle:
        for row in csv.DictReader(handle):
            converter.copy_texture(row['texture'], row['texture'].split(':', 1)[0])
    with (ROOT / 'docs/reference/conversion/system_assets.csv').open(encoding='utf-8') as handle:
        for row in csv.DictReader(handle):
            if row['kind'] == 'texture':
                converter.copy_texture(row['id'], row['id'].split(':', 1)[0])
                continue
            if row['kind'] in ('geo', 'animation'):
                folder, suffix = ('geo', '.geo.json') if row['kind'] == 'geo' else ('animations', '.animation.json')
                converter.copy_plain(row['id'], folder, suffix, row['id'].split(':', 1)[0])
                continue
            target = row['id'].split(':', 1)[1]
            mod, path = origin[(row['kind'], target)]
            if row['kind'] == 'block':
                converter.block(mod, path, target, layers.get((mod, path)))
            else:
                converter.item(mod, path, target, {})
    for fragment in sorted((ROOT / 'src/main/sounds').glob('*.json')):
        for definition in json.loads(fragment.read_text(encoding='utf-8')).values():
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
