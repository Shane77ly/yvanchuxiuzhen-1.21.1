"""审查41个来源群系的实体闭包；只写审查报告，不修改正式注册或资源。"""
from __future__ import annotations

import argparse
from collections import Counter, defaultdict
import csv
import hashlib
import json
from pathlib import Path
import re

from catalog_world_biomes import matching
from convert_content import ROOT, SOURCES

MOD = 'yvanchuxiuzhen'
PROCEDURE = re.compile(r'\b(\w+Procedure)\.execute\s*\(')
METHOD = re.compile(r'^\s*(?:public|protected|private)\s+(?:static\s+)?(?:final\s+)?(?:<[^\n]+?>\s+)?[\w<>?,.\[\] ]+\s+(\w+)\s*\([^;]*?\)\s*\{', re.M)


def methods(text: str) -> list[dict]:
    result = []
    for match in METHOD.finditer(text):
        opening = text.index('{', match.start(), match.end())
        end = matching(text, opening)
        result.append({'name': match[1], 'line': text.count('\n', 0, match.start()) + 1,
                       'source': text[match.start():end + 1].strip()})
    return result


def build(plan_path: Path) -> dict:
    plan = json.loads(plan_path.read_text(encoding='utf-8'))
    missing = {row['id'] for row in plan['blockers'] if row['kind'] == 'missing_entity_type'}
    root = SOURCES[MOD][0]
    java = root / f'src/main/java/net/mcreator/{MOD}'
    register = (java / 'init/YvanchuxiuzhenModEntities.java').read_text(encoding='utf-8')
    registered = {}
    pattern = re.compile(r'RegistryObject<EntityType<(\w+)>>\s+(\w+)\s*=\s*register\("([^\"]+)"')
    for match in pattern.finditer(register):
        registered[MOD + ':' + match[3]] = {'class': match[1], 'field': match[2],
            'line': register.count('\n', 0, match.start()) + 1}
    unknown = missing - registered.keys()
    if unknown:
        raise ValueError(f'缺失实体不在真实来源注册中：{sorted(unknown)}')

    # 一次读取两个来源，反向索引实体和过程真实引用；不以名称前缀代替调用链。
    texts = {}
    for mod, (source, *_) in SOURCES.items():
        for path in (source / 'src/main/java').rglob('*.java'):
            texts[(mod, path.relative_to(source).as_posix())] = path.read_text(encoding='utf-8-sig')
    procedures = {(mod, Path(path).stem): (path, text)
                  for (mod, path), text in texts.items() if '/procedures/' in path}
    graph = {key: sorted(set(PROCEDURE.findall(text))) for key, (_, text) in procedures.items()}
    owners = {}
    for path in (ROOT / 'docs/systems').glob('*/ledger/content.csv'):
        with path.open(encoding='utf-8', newline='') as stream:
            for row in csv.DictReader(stream):
                if row['registry'] == 'entity_type':
                    owners[row['legacy_id']] = {'owner': row['owner'], 'ownership': row['ownership'],
                        'status': row['status'], 'element': row['element'], 'note': row['note']}
    class_owners = {}
    with (ROOT / 'docs/reference/legacy/inventory/class_ownership.csv').open(encoding='utf-8', newline='') as stream:
        for row in csv.DictReader(stream):
            class_owners[(row['source_mod'], row['class'])] = row['proposed_owner']
    names = {}
    for mod, (source, *_) in SOURCES.items():
        language = source / f'src/main/resources/assets/{mod}/lang/zh_cn.json'
        names.update(json.loads(language.read_text(encoding='utf-8')))

    reverse = defaultdict(list)
    lookup = {registration['class']: identifier for identifier, registration in registered.items() if identifier in missing}
    field_lookup = {registration['field']: identifier for identifier, registration in registered.items() if identifier in missing}
    token_pattern = re.compile(r'\b(?:' + '|'.join(map(re.escape, sorted(lookup))) + r')\b|\bYvanchuxiuzhenModEntities\.([A-Z_0-9]+)')
    for (mod, path), text in texts.items():
        for number, line in enumerate(text.splitlines(), 1):
            if line.lstrip().startswith('import '):
                continue
            found = set()
            for match in token_pattern.finditer(line):
                identifier = field_lookup.get(match[1]) if match[1] else lookup[match[0]]
                if identifier:
                    found.add(identifier)
            for identifier in sorted(found):
                reverse[identifier].append({'source_mod': mod, 'path': path, 'line': number,
                    'owner_candidate': class_owners.get((mod, Path(path).stem), ''), 'source': line.strip()})

    result = []
    for identifier in sorted(missing):
        registration = registered[identifier]
        name = registration['class']
        path = f'src/main/java/net/mcreator/{MOD}/entity/{name}.java'
        text = texts[(MOD, path)]
        direct = sorted(set(PROCEDURE.findall(text)))
        pending, closed, unresolved = list(direct), {}, set()
        while pending:
            procedure = pending.pop()
            if procedure in closed or procedure in unresolved:
                continue
            key = (MOD, procedure)
            if key not in procedures:
                unresolved.add(procedure)
                continue
            source, body = procedures[key]
            closed[procedure] = {'path': source, 'owner_candidate': class_owners.get(key, ''),
                'sha256': hashlib.sha256(body.encode('utf-8')).hexdigest(),
                'calls': graph[key], 'persistent_keys': sorted(set(re.findall(r'getPersistentData\(\)\.\w+\("([^\"]+)"', body))),
                'content_references': sorted(set(re.findall(r'(YvanchuxiuzhenMod\w+\.\w+)', body))),
                'capability_access': 'PLAYER_VARIABLES_CAPABILITY' in body,
                'commands': sorted(set(re.findall(r'"(/?(?:shanghai|execute|effect|summon|kill|tp|particle)[^"\n]*)"', body)))}
            pending.extend(graph[key])
        hooks = methods(text)
        resource_sources = []
        resources = set()
        texture = re.search(r'define\(TEXTURE, "([^\"]+)"\)', text)
        if texture:
            resources.add(f'textures/entities/{texture[1]}.png')
        base = name.removesuffix('Entity')
        for relative in (f'entity/model/{base}Model.java', f'entity/layer/{base}Layer.java', f'client/renderer/{base}Renderer.java'):
            source = java / relative
            if source.is_file():
                body = source.read_text(encoding='utf-8')
                paths = re.findall(r'(?:"(?:yvanchuxiuzhen:)?)(?P<path>(?:geo|animations|textures)/[^\"]+)', body)
                resources.update(paths)
                resource_sources.append({'path': source.relative_to(root).as_posix(), 'source': body})
        for procedure in closed:
            body = procedures[(MOD, procedure)][1]
            resources.update(f'textures/entities/{item}.png' for item in re.findall(r'setTexture\("([^\"]+)"\)', body))
        resource_status = [{'path': item, 'source_exists': (root / f'src/main/resources/assets/{MOD}' / item).is_file(),
            'target_exists': (ROOT / f'src/main/resources/assets/{MOD}' / item).is_file()}
            for item in sorted(resources)]
        spawns = []
        for biome in plan['outputs']:
            for category, entries in biome['data']['spawners'].items():
                for index, spawn in enumerate(entries):
                    if spawn['type'] == identifier:
                        spawns.append({'biome': biome['id'], 'category': category, 'index': index, **spawn})
        external = [row for row in reverse[identifier] if row['path'] != path
                    and (not row['path'].startswith(f'src/main/java/net/mcreator/{MOD}/init/')
                         or row['path'].endswith('/EntityAnimationFactory.java'))
                    and not any(token in row['path'] for token in ('/client/renderer/', '/entity/model/', '/entity/layer/'))]
        result.append({'id': identifier, 'name_zh_cn': names.get('entity.' + identifier.replace(':', '.'), ''),
            'registration': registration, 'ownership': owners.get(identifier), 'source': path,
            'source_sha256': hashlib.sha256(text.encode('utf-8')).hexdigest(),
            'base': re.search(r'class\s+' + name + r'\s+extends\s+(\w+)', text)[1],
            'goals': [line.strip() for line in text.splitlines() if '.addGoal(' in line],
            'attributes': re.findall(r'\.add\(Attributes\.(\w+),\s*([^\)]+)\)', text),
            'shared_attribute_additions': [row for row in reverse[identifier] if '/init/' in row['path'] and 'Attributes' in row['path']],
            'synced_fields': re.findall(r'EntityDataAccessor<[^>]+>\s+(\w+)', text),
            'hooks': hooks, 'direct_procedures': direct, 'procedure_closure': closed,
            'unresolved_procedures': sorted(unresolved), 'resource_sources': resource_sources,
            'resources': resource_status, 'biome_spawn_entries': spawns,
            'external_entity_references': external,
            'loot_source': f'src/main/resources/data/{MOD}/loot_tables/entities/{identifier.split(":", 1)[1]}.json',
            'loot_exists': (root / f'src/main/resources/data/{MOD}/loot_tables/entities/{identifier.split(":", 1)[1]}.json').is_file()})
    return {'source': str(root), 'plan': str(plan_path), 'entity_count': len(result),
        'base_counts': dict(Counter(row['base'] for row in result)),
        'owner_counts': dict(Counter((row['ownership'] or {}).get('owner', 'unknown') for row in result)),
        'entities': result,
        'limitations': ['反向引用为真实Java文本位置，owner_candidate仅为既有索引候选，不替代归属源码裁定。',
            'procedure_closure仅沿实体直接调用的Procedure.execute递归；外部全局钩子另列，不把未关联全局过程冒充已闭合。',
            '报告未创建实体或改变生成权重；没有构建、游戏或视觉验收。']}


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan', type=Path, default=ROOT / 'tmp/source-biomes-plan.json')
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/world-entities-audit.json')
    args = parser.parse_args()
    report = build(args.plan)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({key: report[key] for key in ('entity_count', 'base_counts', 'owner_counts')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
