"""把两个 1.18.2 来源的 data 资源转换为 1.21.1 数据包：配方与标签。

输出：
- src/main/resources/data/yvanchuxiuzhen/recipe/<名>.json
- src/main/resources/data/yvanchuxiuzhen/loot_table/<blocks|entities>/<名>.json
- src/main/resources/data/yvanchuxiuzhen/advancement/<名>.json
- src/main/resources/data/<yvanchuxiuzhen|minecraft>/tags/<种类>/<路径>.json
- src/main/resources/data/yvanchuxiuzhen/enchantment/<名>.json 与附魔相关标签（convert_enchantments.py）
- 已核对的维度、维度类型、群系与洞穴可替换标签（convert_world.py）
- docs/reference/conversion/data_manifest.csv（来源、来源 SHA-1、目标）
- docs/reference/conversion/data_skipped.csv（未转换的文件或条目及原因）

规则见 docs/development/engineering.md“资源转换管线”。引用本 Mod 尚未登记的物品、方块或标签的配方整条跳过，
战利品表引用未登记的物品时同样整条跳过，标签只去掉未登记的成员，都写入 data_skipped.csv；所属系统登记内容后重跑本工具即可补全。
"""
from __future__ import annotations

import csv
import hashlib
import json
import zipfile
from pathlib import Path

from build_ledger import NAMESPACE, ROOT, read_ledgers
from convert_content import CONTENT, CONVERSION, SOURCES
from convert_enchantments import generate as generate_enchantments
from convert_world import generate as generate_world
from convert_plant_worldgen import generate as generate_plant_worldgen

DATA = ROOT / 'src/main/resources/data'
MANIFEST = CONVERSION / 'data_manifest.csv'
SKIPPED = CONVERSION / 'data_skipped.csv'
VANILLA_RESOURCES = ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar'
TAG_DIRS = {'items': 'item', 'blocks': 'block', 'entity_types': 'entity_type', 'fluids': 'fluid'}
# 1.18.2 到 1.21.1 之间改名的原版物品 ID（来源数据中实际出现的）。
VANILLA_RENAMES = {'minecraft:scute': 'minecraft:turtle_scute'}
COOKING = {'minecraft:smelting', 'minecraft:blasting', 'minecraft:smoking', 'minecraft:campfire_cooking'}


def read_rows(path: Path) -> list[dict]:
    return json.loads(path.read_text(encoding='utf-8'))


class Registered:
    """本 Mod 已登记的方块与物品（内容表）、所属系统自己登记的内容（台账状态为“行为已迁移”的行），
    和原版 1.21.1 的方块、物品（物品模型与方块状态文件）、实体类型（语言文件中的名称键）。"""

    def __init__(self) -> None:
        self.mod = {'item': set(), 'block': set()}
        # 内容表中声明了堆叠上限的物品；其余物品按默认的 64 处理。
        self.stack = {}
        for path in sorted(CONTENT.glob('*/items.json')):
            for row in read_rows(path):
                self.mod['item'].add(f'{NAMESPACE}:{row["id"]}')
                if 'stack' in row:
                    self.stack[f'{NAMESPACE}:{row["id"]}'] = row['stack']
        for path in sorted(CONTENT.glob('*/blocks.json')):
            self.mod['block'] |= {f'{NAMESPACE}:{row["id"]}' for row in read_rows(path)}
        # 来源注册表中出现过的 ID（目标 ID），用于区分“尚未迁移”与“来源标签引用了来源本身没有登记的 ID”。
        self.in_source = set()
        for rows in read_ledgers().values():
            for row in rows:
                self.in_source.add((row['registry'], row['target_id']))
                if row['status'] == '行为已迁移':
                    self.mod.setdefault(row['registry'], set()).add(row['target_id'])
        with zipfile.ZipFile(VANILLA_RESOURCES) as archive:
            lang = json.loads(archive.read('assets/minecraft/lang/en_us.json').decode('utf-8'))
            files = archive.namelist()

        def files_in(directory: str) -> set[str]:
            return {f'minecraft:{name[len(directory):-5]}' for name in files if name.startswith(directory) and name.endswith('.json')}

        # 语言文件保留了已改名物品的旧键（如 scute），物品与方块以实际存在的模型和方块状态为准。
        self.vanilla = {'item': files_in('assets/minecraft/models/item/'), 'block': files_in('assets/minecraft/blockstates/'),
                        'entity_type': {f'minecraft:{key[len("entity.minecraft."):]}' for key in lang if key.startswith('entity.minecraft.')}}

    def reason(self, kind: str, id_: str) -> str:
        if id_.startswith(f'{NAMESPACE}:') and kind in self.mod and (kind, id_) not in self.in_source:
            return f'成员 {id_} 来源没有登记该 ID（来源标签的悬空引用）'
        return f'成员 {id_} 未登记，由所属系统迁移'

    def has(self, kind: str, id_: str) -> bool:
        """本 Mod 的成员必须已登记（实体类型等尚无登记集合的种类一律未登记）；原版成员按语言文件核对，流体等无核对集合的种类不核对。"""
        if id_.startswith(f'{NAMESPACE}:'):
            return id_ in self.mod.get(kind, set())
        return id_ in self.vanilla[kind] if kind in self.vanilla else True


class Converter:
    def __init__(self) -> None:
        self.registered = Registered()
        self.manifest: list[tuple[str, str, str]] = []
        self.skipped: list[tuple[str, str, str]] = []
        self.tags: dict[Path, dict] = {}
        self.tag_origin: dict[Path, list[tuple[str, Path]]] = {}
        # 本 Mod 命名空间下将要生成的物品与方块标签，配方和标签成员引用它们时据此核对。
        self.defined_tags: dict[str, set[str]] = {'item': set(), 'block': set()}

    # ---- 标签 ----

    def rename(self, id_: str) -> str:
        """来源物品、方块 ID 的目标 ID：两个来源命名空间统一为本 Mod，改名的原版 ID 换成新名。"""
        namespace, _, name = id_.partition(':')
        return f'{NAMESPACE}:{name}' if namespace in SOURCES else VANILLA_RENAMES.get(id_, id_)

    def source_tag_name(self, namespace: str, name: str) -> str:
        """来源标签 ID 的目标名称：来源 Forge 命名空间与两个来源 Mod 统一为本 Mod，原版命名空间保持。"""
        return f'{NAMESPACE}:{name}' if namespace in ('forge', *SOURCES) else f'{namespace}:{name}'

    def collect_tags(self) -> None:
        for mod, (root, _) in SOURCES.items():
            data = root / 'src/main/resources/data'
            for file in sorted(data.glob('*/tags/**/*.json')):
                namespace, _, rest = file.relative_to(data).as_posix().partition('/tags/')
                kind, _, path = rest.partition('/')
                if kind == 'worldgen':
                    self.skipped.append(('tag', f'{mod}/{file.relative_to(root).as_posix()}', '群系标签引用本 Mod 的群系，随 world 迁移'))
                    continue
                if kind not in TAG_DIRS:
                    raise ValueError(f'未知标签种类 {file}')
                target_namespace = NAMESPACE if namespace in ('forge', *SOURCES) else namespace
                target = DATA / target_namespace / 'tags' / TAG_DIRS[kind] / path
                content = json.loads(file.read_text(encoding='utf-8'))
                if content.get('replace'):
                    raise ValueError(f'标签 {file} 要求 replace，需要先裁定合并方式')
                self.tag_origin.setdefault(target, []).append((mod, file))
                values = self.tags.setdefault(target, {'replace': False, 'values': []})['values']
                for value in content['values']:
                    if value not in values:
                        values.append(value)
                if target_namespace == NAMESPACE and TAG_DIRS[kind] in self.defined_tags:
                    self.defined_tags[TAG_DIRS[kind]].add(f'{NAMESPACE}:{path[:-5]}')

    def filter_tag(self, target: Path, kind: str) -> list:
        """去掉未登记的成员，返回保留的成员；成员是标签引用时改写命名空间。"""
        values = []
        for value in self.tags[target]['values']:
            id_ = self.rename(value if isinstance(value, str) else value['id'])
            if id_.startswith('#'):
                namespace, _, name = id_[1:].partition(':')
                renamed = '#' + self.source_tag_name(namespace, name)
                if renamed[1:].startswith(f'{NAMESPACE}:') and renamed[1:] not in self.defined_tags.get(kind, set()):
                    self.skipped.append(('tag_value', self.rel(target), f'引用的标签 {renamed[1:]} 未定义'))
                    continue
                values.append(renamed)
            elif self.registered.has(kind, id_):
                values.append(id_ if isinstance(value, str) else {**value, 'id': id_})
            else:
                self.skipped.append(('tag_value', self.rel(target), self.registered.reason(kind, id_)))
        return values

    # ---- 配方 ----

    def ingredient(self, node):
        """来源材料：{"item"} 或 {"tag"}，也可以是备选列表。返回改写后的材料与第一个未登记的引用。"""
        if isinstance(node, list):
            out = []
            for entry in node:
                converted, missing = self.ingredient(entry)
                if missing:
                    return None, missing
                out.append(converted)
            return out, None
        if 'item' in node:
            item = self.rename(node['item'])
            return ({'item': item}, None) if self.registered.has('item', item) else (None, f'物品 {item}')
        tag = node['tag']
        namespace, _, name = tag.partition(':')
        renamed = self.source_tag_name(namespace, name)
        if renamed.startswith(f'{NAMESPACE}:'):
            return ({'tag': renamed}, None) if renamed in self.defined_tags['item'] else (None, f'标签 {renamed}')
        return {'tag': renamed}, None

    def recipe(self, mod: str, file: Path, root: Path) -> None:
        source = json.loads(file.read_text(encoding='utf-8'))
        kind = source['type']
        out: dict = {'type': kind}
        missing = None
        if kind in ('minecraft:crafting_shaped', 'minecraft:crafting_shapeless'):
            if kind == 'minecraft:crafting_shaped':
                out['pattern'] = source['pattern']
                out['key'] = {}
                for symbol, node in source['key'].items():
                    out['key'][symbol], missing = self.ingredient(node)
                    if missing:
                        break
            else:
                out['ingredients'], missing = self.ingredient(source['ingredients'])
            result = self.rename(source['result']['item'])
            if missing is None and not self.registered.has('item', result):
                missing = f'物品 {result}'
            out['result'] = {'id': result, 'count': source['result']['count']}
            # 1.21.1 的配方产物不允许超过物品的堆叠上限，来源 1.18.2 允许；物品的上限是它自己的属性，按负责人裁定（Q15）
            # 把配方产物数量降到上限。只核对内容表声明了上限的本 Mod 物品。
            limit = self.registered.stack.get(result)
            if missing is None and limit is not None and out['result']['count'] > limit:
                self.skipped.append(('recipe_count_clamped', f'{mod}/{file.relative_to(root).as_posix()}',
                                     f'产物 {result} 数量 {out["result"]["count"]} 超过堆叠上限 {limit}，改为 {limit}（Q15 裁定）'))
                out['result']['count'] = limit
        elif kind in COOKING or kind == 'minecraft:stonecutting':
            out['ingredient'], missing = self.ingredient(source['ingredient'])
            result = self.rename(source['result'])
            if kind in COOKING:
                out['experience'] = source['experience']
                out['cookingtime'] = source['cookingtime']
                out['result'] = {'id': result}
            else:
                out['result'] = {'id': result, 'count': source['count']}
            if missing is None and not self.registered.has('item', result):
                missing = f'物品 {result}'
        else:
            raise ValueError(f'未知配方类型 {kind}：{file}')
        relative = f'{mod}/{file.relative_to(root).as_posix()}'
        if missing:
            self.skipped.append(('recipe', relative, f'引用未登记的{missing}，由所属系统迁移'))
            return
        target = DATA / NAMESPACE / 'recipe' / file.name
        self.write(target, out, [(mod, file)])

    # ---- 战利品表与进度 ----

    def number_provider(self, value):
        """1.18.2 的 {min, max} 不带类型即均匀分布，1.21.1 要求写明类型。"""
        if isinstance(value, dict) and 'type' not in value:
            return {'type': 'minecraft:uniform', 'min': value['min'], 'max': value['max']}
        return value

    def convert_loot(self, node, missing: list[str]):
        """递归改写战利品表：均匀分布的类型、match_tool 的附魔谓词；记录引用的未登记物品。"""
        if isinstance(node, list):
            return [self.convert_loot(entry, missing) for entry in node]
        if not isinstance(node, dict):
            return node
        out = {}
        for key, value in node.items():
            if key in ('rolls', 'count'):
                out[key] = self.number_provider(value)
            elif key == 'predicate' and node.get('condition') == 'minecraft:match_tool':
                if set(value) != {'enchantments'}:
                    raise ValueError(f'match_tool 的谓词含有未处理的字段 {sorted(value)}')
                out[key] = {'predicates': {'minecraft:enchantments': [
                    {'enchantments': entry['enchantment'], 'levels': entry['levels']} for entry in value['enchantments']]}}
            elif key == 'name' and node.get('type') == 'minecraft:item':
                item = self.rename(value)
                if not self.registered.has('item', item) and item != 'minecraft:air':
                    missing.append(item)
                out[key] = item
            else:
                out[key] = self.convert_loot(value, missing)
        return out

    def loot_table(self, mod: str, file: Path, root: Path, group: str) -> None:
        missing: list[str] = []
        table = self.convert_loot(json.loads(file.read_text(encoding='utf-8')), missing)
        relative = f'{mod}/{file.relative_to(root).as_posix()}'
        if missing:
            self.skipped.append(('loot_table', relative, f'引用未登记的物品 {missing[0]}，由所属系统迁移'))
            return
        self.write(DATA / NAMESPACE / 'loot_table' / group / file.name, table, [(mod, file)])

    def advancement(self, mod: str, file: Path, root: Path) -> None:
        source = json.loads(file.read_text(encoding='utf-8'))
        relative = f'{mod}/{file.relative_to(root).as_posix()}'
        icon = self.rename(source['display']['icon']['item'])
        if not self.registered.has('item', icon):
            self.skipped.append(('advancement', relative, f'图标物品 {icon} 未登记，由所属系统迁移'))
            return
        out = {**source, 'display': {**source['display'], 'icon': {'id': icon}}}
        if 'parent' in out:
            out['parent'] = self.rename(out['parent'])
        self.write(DATA / NAMESPACE / 'advancement' / file.name, out, [(mod, file)])

    # ---- 输出 ----

    def rel(self, path: Path) -> str:
        return path.relative_to(ROOT).as_posix()

    def write(self, target: Path, data: dict, origins: list[tuple[str, Path]]) -> None:
        text = (json.dumps(data, ensure_ascii=False, indent=2) + '\n').encode('utf-8')
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(text)
        for mod, file in origins:
            root = SOURCES[mod][0]
            digest = hashlib.sha1(file.read_bytes()).hexdigest()
            self.manifest.append((f'{mod}/{file.relative_to(root).as_posix()}', digest, self.rel(target)))

    def clean_previous(self) -> None:
        if MANIFEST.is_file():
            for row in csv.DictReader(MANIFEST.open(encoding='utf-8')):
                (ROOT / row['target']).unlink(missing_ok=True)

    def run(self) -> None:
        self.clean_previous()
        self.collect_tags()
        generated = []
        for target, tag in sorted(self.tags.items()):
            directory = target.relative_to(DATA).parts[2]
            tag['values'] = self.filter_tag(target, directory)
            had_members = any(json.loads(file.read_text(encoding='utf-8'))['values'] for _, file in self.tag_origin[target])
            if tag['values'] or not had_members:
                generated.append(target)
            else:
                self.skipped.append(('tag', self.rel(target), '成员全部未登记，整个标签未生成，随所属系统迁移'))
                if target.relative_to(DATA).parts[0] == NAMESPACE and directory in self.defined_tags:
                    self.defined_tags[directory].discard(f'{NAMESPACE}:{target.relative_to(DATA / NAMESPACE / "tags" / directory).as_posix()[:-5]}')
        for mod, (root, _) in SOURCES.items():
            for file in sorted((root / 'src/main/resources/data' / mod / 'recipes').glob('*.json')):
                self.recipe(mod, file, root)
        for mod, (root, _) in SOURCES.items():
            data = root / 'src/main/resources/data' / mod
            for group in ('blocks', 'entities'):
                for file in sorted((data / 'loot_tables' / group).glob('*.json')) if (data / 'loot_tables' / group).is_dir() else []:
                    self.loot_table(mod, file, root, group)
            for file in sorted((data / 'advancements').glob('*.json')) if (data / 'advancements').is_dir() else []:
                self.advancement(mod, file, root)
            for file in sorted((data / 'functions').glob('*.mcfunction')) if (data / 'functions').is_dir() else []:
                self.skipped.append(('function', f'{mod}/{file.relative_to(root).as_posix()}', '函数引用本 Mod 的维度，随 world 迁移'))
        for target in generated:
            self.write(target, self.tags[target], self.tag_origin[target])
        generate_enchantments(self)
        generate_world(self)
        generate_plant_worldgen(self)
        CONVERSION.mkdir(parents=True, exist_ok=True)
        with MANIFEST.open('w', encoding='utf-8', newline='') as handle:
            writer = csv.writer(handle, lineterminator='\n')
            writer.writerow(['source', 'source_sha1', 'target'])
            writer.writerows(sorted(self.manifest))
        with SKIPPED.open('w', encoding='utf-8', newline='') as handle:
            writer = csv.writer(handle, lineterminator='\n')
            writer.writerow(['kind', 'source', 'reason'])
            writer.writerows(self.skipped)


def main() -> None:
    converter = Converter()
    converter.run()
    kinds = {}
    for kind, _, _ in converter.skipped:
        kinds[kind] = kinds.get(kind, 0) + 1
    print(f'files {len(converter.manifest)} skipped {kinds}')


if __name__ == '__main__':
    main()
