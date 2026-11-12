"""转换人间道与妖魂道的来源群系；依赖未闭合时只允许导出审查计划。"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import zipfile
from collections import Counter
from pathlib import Path

from catalog_world_biomes import arguments, build, calls, matching, target_entities
from convert_content import ROOT, SOURCES
from convert_legacy_biome_vanilla import PLACED as LEGACY_PLACED
from convert_legacy_biome_vanilla import plan as legacy_plan

MOD = 'yvanchuxiuzhen'
DATA = ROOT / f'src/main/resources/data/{MOD}/worldgen'
STAGES = ('RAW_GENERATION', 'LAKES', 'LOCAL_MODIFICATIONS', 'UNDERGROUND_STRUCTURES',
          'SURFACE_STRUCTURES', 'STRONGHOLDS', 'UNDERGROUND_ORES', 'UNDERGROUND_DECORATION',
          'FLUID_SPRINGS', 'VEGETAL_DECORATION', 'TOP_LAYER_MODIFICATION')
CATEGORIES = ('monster', 'creature', 'ambient', 'axolotls', 'underground_water_creature',
              'water_creature', 'water_ambient', 'misc')
EFFECTS = {'fogColor': 'fog_color', 'waterColor': 'water_color', 'waterFogColor': 'water_fog_color',
           'skyColor': 'sky_color', 'foliageColorOverride': 'foliage_color', 'grassColorOverride': 'grass_color'}


class DependencyError(ValueError):
    """保留完整计划，禁止把未闭合的注册引用写入正式数据。"""

    def __init__(self, report: dict):
        self.report = report
        counts = Counter(row['kind'] for row in report['blockers'])
        super().__init__('群系转换依赖未闭合：' + ', '.join(f'{key}={value}' for key, value in sorted(counts.items())))


def numeric(expression: str) -> int | float:
    expression = expression.strip()
    if re.fullmatch(r'-?\d+', expression):
        return int(expression)
    if re.fullmatch(r'-?\d+(?:\.\d+)?[fFdD]?', expression):
        return float(expression.rstrip('fFdD'))
    raise ValueError(f'不是来源数值字面量：{expression}')


def constant(expression: str, prefix: str, allowed) -> str:
    if not expression.startswith(prefix) or expression.removeprefix(prefix) not in allowed:
        raise ValueError(f'未识别来源枚举：{expression}')
    return expression.removeprefix(prefix)


class VanillaReferences:
    """从锁定来源注册声明取得ID，并保存目标注册配置供逐项语义核对。"""

    def __init__(self, source_jar: str, target_jar: str):
        self.source = zipfile.ZipFile(source_jar)
        self.target = zipfile.ZipFile(target_jar)
        self.cache = {}
        self.used = {}
        self.source_nodes = {}
        self.source_classes = {Path(entry).stem: entry for entry in self.source.namelist()
            if entry.startswith('net/minecraft/data/worldgen/') and entry.endswith('.java')}
        self.target_data = zipfile.ZipFile(ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar')

    def close(self) -> None:
        self.source.close()
        self.target.close()
        self.target_data.close()

    def text(self, archive: zipfile.ZipFile, entry: str) -> str:
        return archive.read(entry).decode('utf-8').replace('\r\r\n', '\n').replace('\r\n', '\n')

    def source_graph(self, expression: str) -> list[str]:
        """收集配置图里的来源静态字段；算法与未展开helper仍另行核对。"""
        references = []
        for class_name, member in re.findall(r'\b([A-Z]\w*)\.([A-Z][A-Z_0-9]*)\b', expression):
            key = class_name + '.' + member
            entry = self.source_classes.get(class_name)
            if entry is None:
                continue
            references.append(key)
            if key in self.source_nodes:
                continue
            text = self.text(self.source, entry)
            definition = re.search(r'public static final [^;\n]*\b' + re.escape(member) + r'\s*=\s*', text)
            if definition is None:
                continue
            value = text[definition.end():text.index(';', definition.end())]
            self.source_nodes[key] = {'entry': entry, 'line': text.count('\n', 0, definition.start()) + 1,
                'entry_sha256': hashlib.sha256(text.encode('utf-8')).hexdigest(), 'expression': value}
            self.source_nodes[key]['dependencies'] = self.source_graph(value)
        return references

    def target_snapshot(self, registry: str, identifier: str) -> dict | None:
        path = f'data/minecraft/worldgen/{registry}/{identifier.split(":")[1]}.json'
        if path not in self.target_data.namelist():
            return None
        data = json.loads(self.target_data.read(path))
        return {'entry': path, 'data': data}

    def resolve(self, expression: str, registry: str) -> str:
        if not re.fullmatch(r'\w+\.[A-Z][A-Z_0-9]*', expression):
            raise ValueError(f'未识别原版注册常量：{expression}')
        class_name, member = expression.split('.')
        entry = 'net/minecraft/data/worldgen/' + ('Carvers.java' if class_name == 'Carvers' else f'placement/{class_name}.java')
        if entry not in self.cache:
            self.cache[entry] = (self.text(self.source, entry), self.text(self.target, entry))
        old, new = self.cache[entry]
        declaration = re.search(r'public static final [^;\n]*\b' + re.escape(member) + r'\s*=\s*', old)
        if declaration is None:
            raise ValueError(f'1.18.2注册常量不存在：{expression}')
        end = old.index(';', declaration.end())
        source_expression = old[declaration.end():end]
        source_calls = calls(source_expression)
        if not source_calls or not source_calls[0]['arguments']:
            raise ValueError(f'1.18.2注册声明无法解析：{expression}')
        identifier = 'minecraft:' + json.loads(source_calls[0]['arguments'][0])
        if identifier not in self.used:
            target_declaration = re.search(r'public static final [^;\n]*\b' + re.escape(member) + r'\s*=\s*', new)
            target_id = None
            if target_declaration:
                declaration_end = new.index(';', target_declaration.end())
                target_expression = new[target_declaration.end():declaration_end]
                strings = re.findall(r'"([^"]+)"', target_expression)
                if len(strings) == 1:
                    target_id = 'minecraft:' + strings[0]
            target_calls = []
            for call in calls(new):
                if not call['name'].endswith('register'):
                    continue
                args = call['arguments']
                if ((registry == 'placed_feature' and len(args) > 1 and args[1] == member)
                        or (registry == 'configured_carver' and args and args[0] == member)):
                    target_calls.append(call['expression'])
            # 同名ID不构成配置等价证据；完整来源配置与目标bootstrap分别登记。
            self.used[identifier] = {'id': identifier, 'registry': registry, 'constant': expression,
                'source_entry': entry, 'source_entry_sha256': hashlib.sha256(old.encode('utf-8')).hexdigest(),
                'source_registration': source_expression, 'target_id': target_id,
                'source_configuration_dependencies': self.source_graph(source_expression),
                'target_entry': entry, 'target_entry_sha256': hashlib.sha256(new.encode('utf-8')).hexdigest(),
                'target_registration_calls': target_calls, 'target_resource': self.target_snapshot(registry, identifier),
                'parity': 'requires_configuration_and_algorithm_review'}
        return identifier


def helper_operations(name: str, args: list[str], definitions: dict, vanilla: VanillaReferences) -> list[dict]:
    methods = definitions[name]['source_methods']
    selected = [method for method in methods if len(method['parameters']) == len(args)]
    if len(selected) != 1:
        raise ValueError(f'来源助手重载无法唯一选择：{name}({args})')
    method = selected[0]
    bindings = {parameter.split()[-1]: value for parameter, value in zip(method['parameters'], args)}
    builder = method['parameters'][0].split()[-1]

    def substitute(value: str) -> str:
        if value in bindings:
            return bindings[value]
        ternary = re.fullmatch(r'(\w+)\s*\?\s*([\w.]+)\s*:\s*([\w.]+)', value)
        if ternary:
            choice = bindings[ternary[1]]
            if choice not in ('true', 'false'):
                raise ValueError(f'来源助手条件不是确定布尔值：{name}:{value}')
            return ternary[2] if choice == 'true' else ternary[3]
        return value

    operations = []
    for call in method['calls']:
        child = call['name'].removeprefix('BiomeDefaultFeatures.')
        if call['name'] in (builder + '.addFeature', builder + '.addCarver'):
            stage, reference = call['arguments']
            registry = 'placed_feature' if call['name'].endswith('addFeature') else 'configured_carver'
            prefix = 'GenerationStep.Decoration.' if registry == 'placed_feature' else 'GenerationStep.Carving.'
            stage = constant(stage, prefix, STAGES if registry == 'placed_feature' else ('AIR', 'LIQUID'))
            reference = substitute(reference)
            identifier = vanilla.resolve(reference, registry)
            if registry == 'placed_feature':
                identifier = LEGACY_PLACED.get(identifier, identifier)
            operations.append({'registry': registry, 'stage': stage,
                'id': identifier, 'helper': name,
                'source_line': method['line'] + call['line'], 'source_expression': call['expression']})
        elif child in definitions:
            operations.extend(helper_operations(child, [substitute(value) for value in call['arguments']], definitions, vanilla))
        else:
            raise ValueError(f'来源助手含未识别操作：{name}:{call["expression"]}')
    return operations


def vanilla_entities(archive: zipfile.ZipFile) -> dict[str, str]:
    text = archive.read('net/minecraft/world/entity/EntityType.java').decode('utf-8')
    return {name: 'minecraft:' + identifier for name, identifier in re.findall(
        r'public static final EntityType<[^;=]+>\s+(\w+)\s*=\s*register\("([^"]+)"', text)}


def particle_ids(source_root: Path) -> dict[str, str]:
    path = source_root / f'src/main/java/net/mcreator/{MOD}/init/YvanchuxiuzhenModParticleTypes.java'
    text = path.read_text(encoding='utf-8')
    return {name: MOD + ':' + identifier for name, identifier in re.findall(
        r'public static final RegistryObject<[^;=]+>\s+(\w+)\s*=\s*REGISTRY.register\("([^"]+)"', text)}


def biome_effects(biome: dict, particles: dict) -> dict:
    result = {}
    for effect in biome['effects']:
        name = effect['name']
        if name in EFFECTS:
            result[EFFECTS[name]] = numeric(effect['arguments'][0])
        elif name == 'ambientParticle':
            match = re.fullmatch(r'new AmbientParticleSettings\(\(SimpleParticleType\)\s*\(YvanchuxiuzhenModParticleTypes\.(\w+)\.get\(\)\),\s*([^()]+)\)', effect['arguments'][0])
            if match is None:
                raise ValueError(f'未识别群系环境粒子：{effect["expression"]}')
            result['particle'] = {'options': {'type': particles[match[1]]}, 'probability': numeric(match[2])}
        elif name not in ('AmbientParticleSettings',) and not name.startswith('YvanchuxiuzhenModParticleTypes.'):
            raise ValueError(f'未识别群系视觉字段：{effect["expression"]}')
    if not {'fog_color', 'water_color', 'water_fog_color', 'sky_color'} <= result.keys():
        raise ValueError(f'群系缺少必要视觉字段：{biome["id"]}')
    return result


def build_plan(source_root: Path | None = None, audit: dict | None = None) -> dict:
    """构造全部41群系，包括尚未闭合依赖；不向正式资源目录写入文件。"""
    source_root = source_root or SOURCES[MOD][0]
    audit = audit or build(source_root)
    definitions = audit['vanilla_helper_definitions']
    vanilla = VanillaReferences(definitions['source_jar'], definitions['target_jar'])
    try:
        entities = vanilla_entities(vanilla.source)
        particles = particle_ids(source_root)
        outputs = []
        referenced = {'entity_type': set(), 'placed_feature': set(), 'particle_type': set()}
        for biome in audit['biomes']:
            climate = {item['name']: item['arguments'][0] for item in biome['climate']}
            if climate.pop('biomeCategory') != 'Biome.BiomeCategory.NONE':
                raise ValueError(f'来源群系有未处理类别：{biome["id"]}')
            precipitation = constant(climate.pop('precipitation'), 'Biome.Precipitation.', ('RAIN', 'SNOW', 'NONE'))
            result = {'has_precipitation': precipitation != 'NONE', 'temperature': numeric(climate.pop('temperature')),
                'downfall': numeric(climate.pop('downfall')), 'effects': biome_effects(biome, particles),
                'carvers': {}, 'features': [[] for _ in STAGES],
                'spawners': {category: [] for category in CATEGORIES}, 'spawn_costs': {}}
            if climate:
                raise ValueError(f'群系含未处理气候字段：{biome["id"]}:{climate}')
            if 'particle' in result['effects']:
                referenced['particle_type'].add(result['effects']['particle']['options']['type'])
            operations = []
            # calls按文本位置扫描；直接调用与助手调用交错顺序保留。
            for call in calls(biome['create_biome_source']):
                if call['name'] == 'biomeGenerationSettings.addFeature':
                    stage, expression = call['arguments']
                    stage = constant(stage, 'GenerationStep.Decoration.', STAGES)
                    registration = calls(expression, 'PlacementUtils.register')
                    if len(registration) != 1:
                        raise ValueError(f'群系地物没有唯一内联注册：{biome["id"]}:{expression}')
                    identifier = json.loads(registration[0]['arguments'][0])
                    operations.append({'registry': 'placed_feature', 'stage': stage, 'id': identifier,
                        'source_line': call['line'], 'source_expression': call['expression']})
                elif call['name'].startswith('BiomeDefaultFeatures.'):
                    operations.extend(helper_operations(call['name'].split('.')[-1], call['arguments'], definitions['methods'], vanilla))
            for feature in audit['external_features']:
                if biome['id'] in feature['applies_to']:
                    operations.append({'registry': 'placed_feature', 'stage': feature['stage'], 'id': feature['id'],
                        'registration_line': feature['registration_line'], 'source': feature['source'], 'phase': 'feature_loading_event'})
            for operation in operations:
                if operation['registry'] == 'configured_carver':
                    result['carvers'].setdefault(operation['stage'].lower(), []).append(operation['id'])
                else:
                    result['features'][STAGES.index(operation['stage'])].append(operation['id'])
                    referenced['placed_feature'].add(operation['id'])

            spawn_operations = []
            for spawn in biome['spawns']:
                item = dict(spawn)
                if item['entity_expression'].startswith('EntityType.'):
                    item['entity_id'] = entities[item['entity_expression'].split('.')[1]]
                item['phase'] = 'create_biome'
                spawn_operations.append(item)
            # 事件之间的Forge加载顺序没有来源保证，按源路径稳定列出；每个事件内部顺序不变。
            for event in biome['external_spawn_events']:
                for spawn in event['spawns']:
                    spawn_operations.append({**spawn, 'phase': 'entity_loading_event', 'source': event['source']})
            for spawn in spawn_operations:
                category = constant(spawn['category'], 'MobCategory.', tuple(value.upper() for value in CATEGORIES)).lower()
                result['spawners'][category].append({'type': spawn['entity_id'], 'weight': spawn['weight'],
                    'minCount': spawn['min_count'], 'maxCount': spawn['max_count']})
                referenced['entity_type'].add(spawn['entity_id'])
            if biome.get('other_spawn_calls'):
                raise ValueError(f'群系含未处理出生配置：{biome["id"]}:{biome["other_spawn_calls"]}')
            outputs.append({'id': biome['id'], 'target': f'data/{MOD}/worldgen/biome/{biome["id"].split(":")[1]}.json',
                'data': result, 'source': biome['source'], 'source_precipitation': precipitation,
                'generation_operations': operations, 'spawn_operations': spawn_operations})
        # 实时核验锁定配置闭包与实际算法文件；不能依赖上次生成的审查JSON。
        from audit_vanilla_biome_graph import SNAPSHOT
        from verify_vanilla_biome_parity import verify

        references = sorted(vanilla.used.values(), key=lambda row: row['id'])
        parity = verify({'source_root': str(source_root), 'vanilla_references': references},
            SNAPSHOT, Path(vanilla.source.filename), Path(vanilla.target.filename))
        verified = {(row['registry'], row['id']): row for row in parity['records']}
        support = legacy_plan(source_root)
        planned_targets = {path for path, _, _ in support}
        blockers = []
        registered_entities = target_entities()
        for identifier in sorted(referenced['entity_type']):
            if identifier.startswith(MOD + ':') and identifier not in registered_entities:
                blockers.append({'kind': 'missing_entity_type', 'id': identifier})
        for identifier in sorted(referenced['placed_feature']):
            target = DATA / 'placed_feature' / (identifier.split(':')[1] + '.json')
            if identifier.startswith(MOD + ':') and not target.is_file() and target not in planned_targets:
                blockers.append({'kind': 'missing_placed_feature', 'id': identifier})
        # 不能因同名常量、同名ID或编译通过，就将原版配置图当作对齐完成。
        for reference in references:
            proof = verified[(reference['registry'], reference['id'])]
            reference['parity'] = proof['status']
            if proof['status'].startswith('verified_custom'):
                if proof['target_override'] != LEGACY_PLACED[reference['id']]:
                    raise ValueError(f'已核验覆盖与群系引用映射不一致：{reference["id"]}')
                reference['target_override'] = proof['target_override']
                reference['implementation'] = 'tools/porting/convert_legacy_biome_vanilla.py'
            elif proof['status'] not in ('verified_reusable', 'verified_source_replaceable_set_and_reviewed_vanilla_algorithm'):
                blockers.append({'kind': 'vanilla_reference_parity_pending', 'id': reference['id'], 'registry': reference['registry']})
        registered_particles = {MOD + ':' + row['id'] for row in json.loads(
            (ROOT / 'src/main/resources/content/particle/particles.json').read_text(encoding='utf-8'))}
        for identifier in sorted(referenced['particle_type']):
            if identifier not in registered_particles:
                blockers.append({'kind': 'missing_particle_type', 'id': identifier})
        for unresolved in audit['unresolved_expressions']:
            blockers.append({'kind': 'unresolved_source_expression', 'source': unresolved})
        return {'source_root': str(source_root), 'target_version': 'Minecraft 1.21.1 / NeoForge 21.1.265',
            'outputs': outputs, 'vanilla_references': references,
            'supporting_resources': [{'target': path.relative_to(ROOT / 'src/main/resources').as_posix(),
                'sources': [origin.relative_to(source_root).as_posix() for _, origin in origins]}
                for path, _, origins in support],
            'vanilla_parity': {'validation': parity['validation'], 'next_actions': parity['next_actions']},
            'vanilla_source_configuration_nodes': dict(sorted(vanilla.source_nodes.items())),
            'blockers': blockers, 'validation': {**audit['validation'], 'outputs': len(outputs),
                'expanded_generation_operations': sum(len(row['generation_operations']) for row in outputs),
                'spawn_entries_including_events': sum(len(row['spawn_operations']) for row in outputs),
                'vanilla_references': len(vanilla.used), 'blocker_counts': dict(sorted(Counter(row['kind'] for row in blockers).items()))},
            'limitations': ['完整计划不等于依赖闭合或运行验收；存在blocker时generate明确报错。',
                '各实体BiomeLoadingEvent之间的注册/加载顺序尚无确定证据；计划按来源路径稳定排序并保留重复条目与事件内顺序。',
                '1.18.2原版默认生成助手已按来源方法和实参展开；原版引用逐项实时核验，仍待闭合的配置与算法由vanilla_parity.next_actions列明。',
                '未执行构建、正式数据转换、1.18.2游戏或1.21.1游戏验收。']}
    finally:
        vanilla.close()


def plan(source_root: Path | None = None) -> list[tuple[Path, dict, list[tuple[str, Path]]]]:
    report = build_plan(source_root)
    if report['blockers']:
        raise DependencyError(report)
    root = Path(report['source_root'])
    outputs = [(ROOT / 'src/main/resources' / row['target'], row['data'], [(MOD, root / row['source']['path'])])
            for row in report['outputs']]
    outputs.extend(legacy_plan(root))
    return outputs


def generate(converter) -> int:
    outputs = plan()
    for target, data, origins in outputs:
        converter.write(target, data, origins)
    source_root = SOURCES[MOD][0]
    identifiers = [target.stem for target, _, _ in outputs if target.parent.name == 'biome']
    identifiers.append('quicksand')
    for language in ('zh_cn', 'en_us'):
        source_language = json.loads((source_root / f'src/main/resources/assets/{MOD}/lang/{language}.json').read_text(encoding='utf-8'))
        target_language_path = ROOT / f'src/main/lang/{language}/world.json'
        target_language = json.loads(target_language_path.read_text(encoding='utf-8'))
        for identifier in identifiers:
            for kind in ('biome', 'block', 'fluid'):
                key = f'{kind}.{MOD}.{identifier}'
                if key in source_language:
                    target_language[key] = source_language[key]
        target_language_path.write_text(json.dumps(target_language, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    return len(outputs)


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-root', type=Path, default=SOURCES[MOD][0])
    parser.add_argument('--audit', type=Path, help='复用来源审计JSON；依赖存在性仍实时核对')
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/source-biomes-plan.json')
    args = parser.parse_args()
    audit = json.loads(args.audit.read_text(encoding='utf-8')) if args.audit else None
    report = build_plan(args.source_root, audit)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report['validation'], ensure_ascii=False))


if __name__ == '__main__':
    main()
