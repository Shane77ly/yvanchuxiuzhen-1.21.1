"""导出人间、妖魂维度实际引用群系的完整来源结构；不生成目标资源。"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES

MOD = 'yvanchuxiuzhen'
DIMENSIONS = ('dimensionrenjian', 'dimensionyaohun')
HELPER_ENTRY = 'net/minecraft/data/worldgen/BiomeDefaultFeatures.java'
CALL = re.compile(r'(?P<name>[A-Za-z_$][\w$]*(?:\.[A-Za-z_$][\w$]*)*)\s*\(')


def matching(text: str, start: int) -> int:
    """匹配括号并跳过Java字符串、字符和注释，未闭合直接报告来源错误。"""
    pairs = {'(': ')', '[': ']', '{': '}'}
    stack = [pairs[text[start]]]
    quote = None
    i = start + 1
    while i < len(text):
        ch = text[i]
        if quote:
            if ch == '\\':
                i += 2
                continue
            if ch == quote:
                quote = None
        elif ch in ('"', "'"):
            quote = ch
        elif text.startswith('//', i):
            end = text.find('\n', i)
            i = len(text) if end < 0 else end
            continue
        elif text.startswith('/*', i):
            end = text.find('*/', i + 2)
            if end < 0:
                raise ValueError(f'未闭合Java注释：位置{i}')
            i = end + 2
            continue
        elif ch in pairs:
            stack.append(pairs[ch])
        elif ch in ')]}':
            if ch != stack.pop():
                raise ValueError(f'Java括号不匹配：位置{i}')
            if not stack:
                return i
        i += 1
    raise ValueError(f'未闭合Java括号：位置{start}')


def arguments(text: str) -> list[str]:
    parts = []
    start = i = 0
    while i < len(text):
        if text[i] in '([{':
            i = matching(text, i) + 1
            continue
        if text[i] in ('"', "'"):
            quote = text[i]
            i += 1
            while i < len(text) and text[i] != quote:
                i += 2 if text[i] == '\\' else 1
        elif text[i] == ',':
            parts.append(text[start:i].strip())
            start = i + 1
        i += 1
    if text[start:].strip():
        parts.append(text[start:].strip())
    return parts


def calls(text: str, prefix: str | None = None) -> list[dict]:
    result = []
    for match in CALL.finditer(text):
        name = match['name']
        if prefix is not None and not name.startswith(prefix):
            continue
        opening = text.index('(', match.start(), match.end())
        end = matching(text, opening)
        result.append({'name': name, 'line': text.count('\n', 0, match.start()) + 1,
                       'expression': text[match.start():end + 1],
                       'arguments': arguments(text[opening + 1:end])})
    return result


def evidence(path: Path, source_root: Path) -> dict:
    return {'path': path.relative_to(source_root).as_posix(),
            'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}


def methods(text: str, name: str) -> list[dict]:
    found = []
    pattern = re.compile(r'public\s+static\s+[\w<>?.]+\s+' + re.escape(name) + r'\s*\(')
    for match in pattern.finditer(text):
        opening = text.index('(', match.start())
        params_end = matching(text, opening)
        body_start = text.index('{', params_end)
        body_end = matching(text, body_start)
        body = text[body_start + 1:body_end]
        found.append({'line': text.count('\n', 0, match.start()) + 1,
                      'parameters': arguments(text[opening + 1:params_end]),
                      'source': text[match.start():body_end + 1], 'calls': calls(body),
                      'has_control_flow': bool(re.search(r'\b(if|for|while|switch)\s*\(|\?', body))})
    return found


def all_json(node, pointer: str = ''):
    yield pointer, node
    if isinstance(node, dict):
        for key, child in node.items():
            yield from all_json(child, pointer + '/' + str(key))
    elif isinstance(node, list):
        for index, child in enumerate(node):
            yield from all_json(child, pointer + '/' + str(index))


def literal(expression: str):
    expression = expression.strip()
    if re.fullmatch(r'-?\d+', expression):
        return int(expression)
    if re.fullmatch(r'-?\d+(?:\.\d+)?[fFdD]?', expression):
        return float(expression.rstrip('fFdD'))
    if expression.startswith('"') and expression.endswith('"'):
        return json.loads(expression)
    if expression in ('true', 'false'):
        return expression == 'true'
    return {'unresolved_expression': expression}


def registered_entities(source_java: Path) -> dict[str, str]:
    text = (source_java / 'init/YvanchuxiuzhenModEntities.java').read_text(encoding='utf-8')
    return {constant: MOD + ':' + name for constant, name in re.findall(
        r'public static final RegistryObject<EntityType<[^;=]+>\s+(\w+)\s*=\s*register\("([^"]+)"', text)}


def target_entities() -> set[str]:
    result = set()
    for path in (ROOT / 'src/main/resources/content').glob('*/entities.json'):
        for row in json.loads(path.read_text(encoding='utf-8')):
            result.add(MOD + ':' + row['id'])
    return result


def external_features(source_root: Path, source_java: Path, biome_ids: set[str]) -> tuple[list[dict], dict]:
    registration_file = source_java / 'init/YvanchuxiuzhenModFeatures.java'
    text = registration_file.read_text(encoding='utf-8')
    classes = {path.stem: path for path in (source_java / 'world/features').rglob('*.java')}
    result = []
    registrations = re.finditer(
        r'public static final RegistryObject<Feature<\?>>\s+(\w+)\s*=\s*register\(', text)
    for match in registrations:
        opening = text.index('(', match.start())
        end = matching(text, opening)
        args = arguments(text[opening + 1:end])
        declaration = text[match.start():end + 1]
        name = MOD + ':' + json.loads(args[0])
        class_name = args[1].split('::')[0]
        path = classes[class_name]
        body = path.read_text(encoding='utf-8')
        selection = re.search(r'\bGENERATE_BIOMES\s*=\s*([^;]+);', body, re.S)
        if selection is None:
            raise ValueError(f'{path}:缺少GENERATE_BIOMES声明')
        selector = selection[1].strip()
        selected = re.findall(r'new ResourceLocation\("([^"]+)"\)', selector)
        unresolved = []
        if selector == 'null':
            applies = sorted(biome_ids)
            selector_kind = 'all_biomes'
        elif selector.startswith('Set.of(') and (selected or selector == 'Set.of()'):
            applies = sorted(biome_ids.intersection(selected))
            selector_kind = 'explicit_ids'
        else:
            applies = []
            selector_kind = 'unresolved'
            unresolved.append(selector)
        if not applies and not unresolved:
            continue
        stage = re.search(r'GenerationStep.Decoration\.(\w+)', args[2])
        placed = ROOT / f'src/main/resources/data/{MOD}/worldgen/placed_feature/{name.split(":")[1]}.json'
        result.append({'id': name, 'class': class_name, 'registration_line': text.count('\n', 0, match.start()) + 1,
                       'registration_expression': declaration, 'stage': stage[1] if stage else {'unresolved_expression': args[2]},
                       'source': evidence(path, source_root), 'selector': selector, 'selector_kind': selector_kind,
                       'declared_biomes': selected, 'applies_to': applies,
                       'generation_declarations': [line.strip() for line in body.splitlines() if 'generate_dimensions' in line],
                       'registration_calls': calls(body, 'FeatureUtils.register') + calls(body, 'PlacementUtils.register'),
                       'target_placed_feature_exists': placed.is_file(), 'unresolved_expressions': unresolved})
    event = text[text.index('@SubscribeEvent'):]
    return result, {'source': evidence(registration_file, source_root), 'event_source': event}


def read_helpers(source_root: Path, names: set[str]) -> dict:
    cache = source_root / 'build/fg_cache/net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2'
    jar = cache / 'forge-1.18.2-40.1.0_mapped_official_1.18.2-sources.jar'
    target_jar = ROOT / 'build/moddev/artifacts/neoforge-21.1.265-sources.jar'
    with zipfile.ZipFile(jar) as archive:
        source = archive.read(HELPER_ENTRY).decode('utf-8').replace('\r\r\n', '\n').replace('\r\n', '\n')
    with zipfile.ZipFile(target_jar) as archive:
        target = archive.read(HELPER_ENTRY).decode('utf-8')
    # 保留被调用方法的全部重载及内部helper调用，不按目标同名方法替代来源。
    pending = list(sorted(names))
    catalog = {}
    while pending:
        name = pending.pop(0)
        if name in catalog:
            continue
        definitions = methods(source, name)
        if not definitions:
            raise ValueError(f'来源helper方法未找到：{name}')
        catalog[name] = {'source_methods': definitions, 'target_methods': methods(target, name)}
        for definition in definitions:
            for call in definition['calls']:
                child = call['name'].removeprefix('BiomeDefaultFeatures.')
                if '.' not in child and child != name and methods(source, child):
                    pending.append(child)
    return {'source_jar': str(jar), 'source_entry': HELPER_ENTRY,
            'source_entry_sha256': hashlib.sha256(source.encode('utf-8')).hexdigest(),
            'target_jar': str(target_jar), 'target_entry': HELPER_ENTRY,
            'methods': catalog,
            'interpretation': '所有重载和分支原样保存；按调用参数选重载及条件求值仍须转换阶段核对。'}


def external_spawns(source_root: Path, source_java: Path, biome_ids: set[str], entity_ids: dict,
                    implemented_entities: set[str]) -> list[dict]:
    result = []
    for path in sorted(source_java.rglob('*.java')):
        text = path.read_text(encoding='utf-8')
        if 'BiomeLoadingEvent' not in text or path.name == 'YvanchuxiuzhenModFeatures.java':
            continue
        definitions = methods(text, 'addLivingEntityToBiomes')
        if not definitions:
            result.append({'source': evidence(path, source_root), 'unresolved_expression': text,
                           'reason': 'BiomeLoadingEvent入口方法未识别'})
            continue
        method = definitions[0]
        selection = re.search(r'\bSPAWN_BIOMES\s*=\s*([^;]+);', text, re.S)
        if selection:
            declared = re.findall(r'new ResourceLocation\("([^"]+)"\)', selection[1])
            applies = sorted(biome_ids.intersection(declared))
            selector = selection[1].strip()
        elif not method['has_control_flow']:
            declared = None
            applies = sorted(biome_ids)
            selector = 'unconditional'
        else:
            result.append({'source': evidence(path, source_root), 'unresolved_expression': method['source'],
                           'reason': '出生入口有无法归约的群系条件'})
            continue
        if not applies:
            continue
        spawns = []
        categories = calls(method['source'], 'getSpawner')
        constructors = calls(method['source'], 'MobSpawnSettings.SpawnerData')
        if len(categories) != len(constructors):
            raise ValueError(f'{path}:Spawner类别与构造数量不一致')
        for category, constructor in zip(categories, constructors):
            entity, weight, minimum, maximum = constructor['arguments']
            match = re.fullmatch(r'YvanchuxiuzhenModEntities\.(\w+)\.get\(\)', entity)
            if match is None:
                raise ValueError(f'{path}:无法解析事件生物引用：{entity}')
            entity_id = entity_ids[match[1]]
            spawns.append({'category': category['arguments'][0], 'entity_id': entity_id,
                           'weight': literal(weight), 'min_count': literal(minimum), 'max_count': literal(maximum),
                           'target_mod_entity_registered': entity_id in implemented_entities})
        result.append({'source': evidence(path, source_root), 'method': method,
                       'selector': selector, 'declared_biomes': declared, 'applies_to': applies, 'spawns': spawns})
    return result


def build(source_root: Path) -> dict:
    source_java = source_root / f'src/main/java/net/mcreator/{MOD}'
    source_data = source_root / f'src/main/resources/data/{MOD}'
    biome_registration = source_java / 'init/YvanchuxiuzhenModBiomes.java'
    text = biome_registration.read_text(encoding='utf-8')
    registrations = {MOD + ':' + name: (class_name, text.count('\n', 0, match.start()) + 1)
                     for match in re.finditer(r'REGISTRY.register\("([^\"]+)", \(\) -> (\w+)\.createBiome\(\)\)', text)
                     for name, class_name in [match.groups()]}
    dimensions = []
    memberships = {}
    for name in DIMENSIONS:
        path = source_data / f'dimension/{name}.json'
        dimension = json.loads(path.read_text(encoding='utf-8'))
        entries = dimension['generator']['biome_source']['biomes']
        surface = dimension['generator']['settings']['surface_rule']
        dimensions.append({'id': MOD + ':' + name, 'source': evidence(path, source_root),
                           'biome_source': dimension['generator']['biome_source'], 'surface_rule': surface})
        for index, entry in enumerate(entries):
            memberships.setdefault(entry['biome'], []).append({'dimension': MOD + ':' + name,
                'pointer': f'/generator/biome_source/biomes/{index}', 'parameters': entry['parameters']})
    entity_ids = registered_entities(source_java)
    implemented_entities = target_entities()
    biomes = []
    helper_names = set()
    unresolved = []
    for biome_id, membership in sorted(memberships.items()):
        class_name, registration_line = registrations[biome_id]
        path = source_java / f'world/biome/{class_name}.java'
        body = path.read_text(encoding='utf-8')
        create = methods(body, 'createBiome')[0]
        feature_calls = calls(body, 'biomeGenerationSettings.add')
        helper_calls = calls(body, 'BiomeDefaultFeatures.')
        helper_names.update(call['name'].split('.')[-1] for call in helper_calls)
        effect_assignment = re.search(r'BiomeSpecialEffects effects\s*=\s*([^;]+);', body, re.S)
        effects = []
        for call in calls(effect_assignment[1]):
            if call['name'].startswith('BiomeSpecialEffects.') or call['name'] == 'build':
                continue
            effects.append({**call, 'values': [literal(arg) for arg in call['arguments']]})
        climate_calls = [call for call in calls(body) if call['name'] in ('precipitation', 'biomeCategory', 'temperature', 'downfall', 'temperatureAdjustment')]
        spawns = []
        for call in calls(body, 'mobSpawnInfo.addSpawn'):
            args = call['arguments']
            constructors = [item for item in calls(args[1]) if item['name'] == 'MobSpawnSettings.SpawnerData']
            if len(constructors) != 1 or len(constructors[0]['arguments']) != 4:
                unresolved.append({'biome': biome_id, 'kind': 'spawn', **call})
                spawns.append({'unresolved_expression': call})
                continue
            entity, weight, minimum, maximum = constructors[0]['arguments']
            if entity.startswith('EntityType.'):
                entity_id = 'minecraft:' + entity.split('.')[1].lower()
            else:
                mod_entity = re.fullmatch(r'YvanchuxiuzhenModEntities\.(\w+)\.get\(\)', entity)
                entity_id = entity_ids[mod_entity[1]] if mod_entity else None
                if entity_id is None:
                    unresolved.append({'biome': biome_id, 'kind': 'entity', 'expression': entity})
            spawns.append({**call, 'category': args[0], 'entity_expression': entity, 'entity_id': entity_id,
                           'weight': literal(weight), 'min_count': literal(minimum), 'max_count': literal(maximum),
                           'target_mod_entity_registered': entity_id in implemented_entities if entity_id and entity_id.startswith(MOD + ':') else None})
        surface_rules = []
        for dimension in dimensions:
            for pointer, node in all_json(dimension['surface_rule']):
                if isinstance(node, dict) and node.get('type') == 'minecraft:condition':
                    condition = node['if_true']
                    if condition.get('type') == 'minecraft:biome' and biome_id in condition['biome_is']:
                        surface_rules.append({'dimension': dimension['id'],
                            'pointer': '/generator/settings/surface_rule' + pointer, 'rule': node})
        biomes.append({'id': biome_id, 'class': class_name, 'registration_line': registration_line,
                       'source': evidence(path, source_root), 'dimension_memberships': membership,
                       'imports': re.findall(r'^import ([^;]+);', body, re.M),
                       'create_biome_source': create['source'],
                       'climate': climate_calls, 'effects_expression': effect_assignment[1], 'effects': effects,
                       'generation_operations': sorted(feature_calls + helper_calls, key=lambda item: item['line']),
                       'direct_features': feature_calls, 'vanilla_helpers': helper_calls, 'spawns': spawns,
                       'other_spawn_calls': [call for call in calls(body, 'mobSpawnInfo.') if call['name'] not in ('mobSpawnInfo.addSpawn', 'mobSpawnInfo.build')],
                       'surface_rules': surface_rules,
                       'inline_registrations': calls(body, 'PlacementUtils.register') + calls(body, 'FeatureUtils.register')})
    helpers = read_helpers(source_root, helper_names)
    features, event = external_features(source_root, source_java, set(memberships))
    spawn_events = external_spawns(source_root, source_java, set(memberships), entity_ids, implemented_entities)
    for biome in biomes:
        biome['external_features'] = [feature['id'] for feature in features if biome['id'] in feature['applies_to']]
        biome['external_spawn_events'] = [entry for entry in spawn_events if biome['id'] in entry.get('applies_to', [])]
    missing_entities = sorted({spawn['entity_id'] for biome in biomes for spawn in biome['spawns']
        if spawn.get('target_mod_entity_registered') is False} | {spawn['entity_id'] for entry in spawn_events for spawn in entry.get('spawns', [])
        if spawn.get('target_mod_entity_registered') is False})
    return {'scope': '由dimensionrenjian和dimensionyaohun实际biome_source引用闭合；不按名称归组',
            'source_root': str(source_root), 'biome_registration': evidence(biome_registration, source_root),
            'dimensions': dimensions, 'biomes': biomes, 'vanilla_helper_definitions': helpers,
            'external_feature_loading': event, 'external_features': features, 'external_spawn_events': spawn_events,
            'unresolved_expressions': unresolved,
            'dependencies': {'missing_mod_entities': missing_entities,
                             'missing_external_placed_features': [feature['id'] for feature in features if not feature['target_placed_feature_exists']],
                             'helper_methods_with_control_flow': [name for name, entry in helpers['methods'].items() if any(item['has_control_flow'] for item in entry['source_methods'])]},
            'validation': {'biome_count': len(biomes), 'direct_feature_calls': sum(len(biome['direct_features']) for biome in biomes),
                           'helper_calls': sum(len(biome['vanilla_helpers']) for biome in biomes),
                           'spawn_entries': sum(len(biome['spawns']) for biome in biomes),
                           'external_feature_count': len(features), 'external_spawn_event_count': len(spawn_events),
                           'external_spawn_applications': sum(len(biome['external_spawn_events']) for biome in biomes)},
            'limitations': ['结构导出不是目标Biome转换；Java表达式和helper条件分支均保留原文，不执行Java求值。',
                            '目标已注册实体按content/*/entities.json核对，不代表完整Creature行为已验收。',
                            '未运行构建、数据转换、目标游戏或1.18.2版本游戏。']}


def markdown(data: dict) -> str:
    stats = data['validation']
    lines = ['# 人间与妖魂群系来源结构审计', '', data['scope'] + '。', '',
             f"共{stats['biome_count']}个群系；直接生成调用{stats['direct_feature_calls']}条；原版helper调用{stats['helper_calls']}条；直接出生条目{stats['spawn_entries']}条；外加Feature {stats['external_feature_count']}项；实体出生事件{stats['external_spawn_event_count']}个，覆盖{stats['external_spawn_applications']}个群系/事件组合。", '',
             'JSON保留每个群系的完整createBiome方法、注册行、SHA256、所有参数原文、surface_rule子树及完整维度surface_rule；helper保留来源和精确21.1.265目标方法全部重载，不能据同名方法推断对齐。', '',
             '| 群系ID | 来源类/行 | 直接生成/helper/出生/外加Feature | 地表分支 |',
             '| --- | --- | --- | --- |']
    for biome in data['biomes']:
        lines.append(f"| `{biome['id']}` | `{biome['source']['path']}` / 注册行{biome['registration_line']} | {len(biome['direct_features'])}/{len(biome['vanilla_helpers'])}/{len(biome['spawns'])}/{len(biome['external_features'])} | {len(biome['surface_rules'])} |")
    lines += ['', '## 完整转换的依赖缺口', '',
              '缺少目标注册的来源Mod生物：' + '、'.join('`' + item + '`' for item in data['dependencies']['missing_mod_entities']) + '。', '',
              '外加投放尚无目标placed_feature：' + '、'.join('`' + item + '`' for item in data['dependencies']['missing_external_placed_features']) + '。', '',
              '来源helper包含控制流：' + '、'.join('`' + item + '`' for item in data['dependencies']['helper_methods_with_control_flow']) + '；原文完整保留，转换需按具体重载参数及条件选分支。', '',
              '内联Tree/PlacedFeature需要逐表达式生成配置与放置数据；原版helper引用的注册常量需核实1.18.2→1.21.1数据ID及配置差异，不能只换同名helper。', '',
              '实体BiomeLoadingEvent追加出生与群系createBiome直接出生同时存在；类别、权重、数量可能不同，转换不能去重或只保留其中一路。Xiuzhe0101Entity入口无群系条件，对全部群系追加。', '',
              '两个维度的sampling、terrain_shaper、slide及独立seed仍需单独完成等价转换；本工具只导出群系及地表约束。', '',
              f"专门登记的未解析出生/选择表达式共{len(data['unresolved_expressions'])}项；所有Java配置表达式与helper控制流原文仍在JSON，不把结构提取等同于语义求值。", '',
              '## 原版helper', '']
    for name, entry in sorted(data['vanilla_helper_definitions']['methods'].items()):
        lines.append(f"- `{name}`：来源{len(entry['source_methods'])}个重载，目标{len(entry['target_methods'])}个同名重载。")
    lines += ['', '## 外加Feature加载入口', '',
              '`YvanchuxiuzhenModFeatures.addFeaturesToBiomes`按注册顺序追加地物；GENERATE_BIOMES为null时作用于所有群系。JSON记录事件原文、每条声明/阶段/群系列表与源文件SHA256，不把自然投放等同于Feature类型注册。', '',
              '## 验证边界', '', *data['limitations'], '']
    return '\n'.join(lines)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-root', type=Path, default=SOURCES[MOD][0])
    parser.add_argument('--output-dir', type=Path, default=ROOT / 'tmp')
    args = parser.parse_args()
    data = build(args.source_root)
    args.output_dir.mkdir(parents=True, exist_ok=True)
    (args.output_dir / 'world-biomes-audit.json').write_text(json.dumps(data, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    (args.output_dir / 'world-biomes-audit.md').write_text(markdown(data), encoding='utf-8')
    print(json.dumps(data['validation'], ensure_ascii=False))
    print('missing_mod_entities', len(data['dependencies']['missing_mod_entities']))
    print('missing_external_placed_features', len(data['dependencies']['missing_external_placed_features']))
    print('unresolved_expressions', len(data['unresolved_expressions']))


if __name__ == '__main__':
    main()
