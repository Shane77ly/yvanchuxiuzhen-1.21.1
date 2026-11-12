"""Build a source-traceable migration plan for Yaoshou019-036 (excluding 034)."""
from __future__ import annotations

import argparse
import csv
import hashlib
import json
from pathlib import Path
import re

from catalog_world_entities import methods, PROCEDURE
from convert_content import ROOT, Source, parse_item_properties
from convert_data import Converter
from extract_entity_models import DAMAGE_IMMUNITIES

MOD = 'yvanchuxiuzhen'
NUMBERS = tuple(number for number in range(19, 37) if number != 34)


def model_row(java: Path, name: str, identifier: str) -> dict:
    model_source = (java / f'entity/model/{name}Model.java').read_text(encoding='utf-8')
    entity_source = (java / f'entity/{name}Entity.java').read_text(encoding='utf-8')
    renderer_source = (java / f'client/renderer/{name}Renderer.java').read_text(encoding='utf-8')
    result = {'id': identifier,
        'geo': re.search(r'geo/([\w]+)\.geo\.json', model_source)[1],
        'animation': re.search(r'animations/([\w]+)\.animation\.json', model_source)[1],
        'texture': re.search(r'define\(TEXTURE, "([\w]+)"\)', entity_source)[1],
        'rendering': {'shadow': float(re.search(r'shadowRadius = ([\d.]+)f', renderer_source)[1]), 'scale': 1.0}}
    head = re.search(r'getBone\("([\w]+)"\)', model_source)
    if head:
        result['head'] = head[1]
    layer = java / f'entity/layer/{name}Layer.java'
    if layer.is_file():
        glow = re.search(r'textures/entities/([\w]+)\.png', layer.read_text(encoding='utf-8'))
        if glow:
            result['glow'] = glow[1]
    predicate = re.search(r'PlayState movementPredicate\(.*?\n\t\}', entity_source, re.S)
    animations = re.findall(r'addAnimation\("([^"]+)", EDefaultLoopTypes\.(\w+)\)', predicate[0] if predicate else '')
    if animations:
        result['idle'] = animations[-1][0]
    result['no_ai'] = 'setNoAi(true)' in entity_source
    definitions = {method['name']: method['source'] for method in methods(entity_source)}
    navigation = re.search(r'return new (\w+PathNavigation)\(this, world\)', entity_source)
    immunities = []
    for condition in re.findall(r'if \((.*?)\)\s*return false;', definitions.get('hurt', '')):
        if condition not in DAMAGE_IMMUNITIES:
            raise ValueError(f'{name}: unhandled immunity {condition}')
        immunities.extend(DAMAGE_IMMUNITIES[condition])
    result['lifecycle'] = {
        'navigation': {'FlyingPathNavigation': 'flying', 'WaterBoundPathNavigation': 'water'}[navigation[1]] if navigation else 'ground',
        'no_gravity': bool(re.search(r'public void setNoGravity\(boolean \w+\) \{\s*super.setNoGravity\(true\)', entity_source)),
        'breathes_underwater': 'canBreatheUnderwater' in definitions,
        'pushed_by_fluid': 'return false;' not in definitions.get('isPushedByFluid', ''),
        'death_ticks': int(re.search(r'this.deathTime == (\d+)', definitions['tickDeath'])[1]),
        'ignores_fall_damage': 'return false;' in definitions.get('causeFallDamage', ''),
        'damage_immunities': immunities,
        'animation_transition_ticks': int(re.search(r'new AnimationController<>\(this, "movement", (\d+),', entity_source)[1])}
    death_rotation = re.search(r'getDeathMaxRotation\([^}]+return ([\d.]+)F;', renderer_source)
    if death_rotation:
        result['rendering']['death_rotation'] = float(death_rotation[1])
    death_animation = re.search(r'if \(this.isDeadOrDying\(\)\) \{\s*event.getController\(\).setAnimation\(new AnimationBuilder\(\).addAnimation\("([^"]+)", EDefaultLoopTypes.PLAY_ONCE\)', predicate[0])
    if death_animation:
        result['lifecycle']['death_animation'] = death_animation[1]
    for attribute, field in (('Attributes.FLYING_SPEED', 'flying_speed'), ('ForgeMod.SWIM_SPEED.get()', 'swim_speed')):
        value = re.search(re.escape(attribute) + r', ([\d.]+)', entity_source)
        if value:
            result['lifecycle'][field] = float(value[1])
    for key, attribute in (('speed', 'MOVEMENT_SPEED'), ('health', 'MAX_HEALTH'), ('armor', 'ARMOR'),
                           ('attack', 'ATTACK_DAMAGE'), ('follow', 'FOLLOW_RANGE')):
        value = re.search(rf'Attributes\.{attribute}, ([\d.]+)', entity_source)
        if value:
            result[key] = float(value[1])
    return result


def build(numbers: tuple[int, ...] = NUMBERS) -> dict:
    source = Source(MOD)
    java = source.root / f'src/main/java/net/mcreator/{MOD}'
    assets = source.root / f'src/main/resources/assets/{MOD}'
    data = source.root / f'src/main/resources/data/{MOD}'
    procedures = {path.stem: path for path in (java / 'procedures').glob('*.java')}
    sounds = json.loads((assets / 'sounds.json').read_text(encoding='utf-8'))
    registrations = list(csv.DictReader((ROOT / 'docs/reference/legacy/inventory/registrations.csv').open(encoding='utf-8')))
    orders = {row['legacy_id']: index for index, row in enumerate((row for row in registrations if row['registry'] == 'item'), 1)}
    report = {key: [] for key in ('models', 'entity_rows_for_integration', 'item_rows_for_integration',
                                  'eggs', 'factory_overrides_for_integration', 'system_assets_for_integration', 'source_proof')}
    report.update({'source': str(source.root), 'copies': {}, 'generated_data': {},
                   'lang_for_integration': {language: {} for language in source.lang}, 'sounds_for_integration': {}})
    converter = Converter()

    def copy(relative: str) -> None:
        path = assets / relative
        if not path.is_file():
            raise FileNotFoundError(path)
        report['copies'][f'assets/{MOD}/{relative}'] = {
            'source': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest()}

    def asset(kind: str, identifier: str) -> None:
        row = {'owner': 'creature', 'kind': kind, 'id': f'{MOD}:{identifier}'}
        if row not in report['system_assets_for_integration']:
            report['system_assets_for_integration'].append(row)

    for number in numbers:
        name, identifier = f'Yaoshou{number:03}', f'yaoshou_{number:03}'
        path = java / f'entity/{name}Entity.java'
        body = path.read_text(encoding='utf-8')
        model = model_row(java, name, identifier)
        report['models'].append(model)
        for kind, relative, value in (('geo', f'geo/{model["geo"]}.geo.json', model['geo']),
                                      ('animation', f'animations/{model["animation"]}.animation.json', model['animation']),
                                      ('texture', f'textures/entities/{model["texture"]}.png', 'entities/' + model['texture'])):
            copy(relative)
            asset(kind, value)
        if 'glow' in model:
            copy(f'textures/entities/{model["glow"]}.png')
            asset('texture', 'entities/' + model['glow'])

        definitions = methods(body)
        pending = list(sorted(set(PROCEDURE.findall(body))))
        closed = {}
        while pending:
            procedure = pending.pop()
            if procedure in closed:
                continue
            procedure_path = procedures.get(procedure)
            if procedure_path is None:
                raise ValueError(f'{identifier}: missing procedure {procedure}')
            procedure_text = procedure_path.read_text(encoding='utf-8')
            calls = sorted(set(PROCEDURE.findall(procedure_text)))
            closed[procedure] = {'source': procedure_path.relative_to(source.root).as_posix(),
                'sha256': hashlib.sha256(procedure_path.read_bytes()).hexdigest(), 'calls': calls,
                'content_references': sorted(set(re.findall(r'YvanchuxiuzhenMod\w+\.\w+', procedure_text))),
                'capability_access': 'PLAYER_VARIABLES_CAPABILITY' in procedure_text}
            pending.extend(calls)
            for texture in re.findall(r'setTexture\("([^\"]+)"\)', procedure_text):
                copy(f'textures/entities/{texture}.png')
                asset('texture', 'entities/' + texture)

        builder = re.search(rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);', source.entities_text, re.S)
        if builder is None:
            raise ValueError(f'{identifier}: entity registration missing')
        chain = builder[2]
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
        tracking = re.search(r'setTrackingRange\((\d+)\)', chain)
        interval = re.search(r'setUpdateInterval\((\d+)\)', chain)
        report['entity_rows_for_integration'].append({'id': identifier, 'category': builder[1],
            'width': float(size[1]), 'height': float(size[2]), 'tracking_range': int(tracking[1]) if tracking else 5,
            'update_interval': int(interval[1]) if interval else 3, 'fire_immune': '.fireImmune()' in chain})

        egg_id = identifier + '_spawn_egg'
        egg = re.search(rf'register\("{egg_id}",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);', source.items_text, re.S)
        if egg is None:
            raise ValueError(f'{egg_id}: spawn egg registration missing')
        report['eggs'].append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
        report['item_rows_for_integration'].append({'id': egg_id, 'factory': 'creature:spawn_egg_' + identifier,
            'order': orders[MOD + ':' + egg_id], **parse_item_properties(egg[3], source.tabs)})
        report['factory_overrides_for_integration'].append({'registry': 'item', 'target_id': MOD + ':' + egg_id,
            'factory': 'creature:spawn_egg_' + identifier, 'owner': 'creature',
            'reason': name + '来源刷怪蛋；实体行为闭合后接入'})
        copy(f'models/item/{egg_id}.json')
        asset('item', egg_id)

        loot_paths = sorted((data / 'loot_tables/entities').glob(identifier + '*.json'))
        for loot in loot_paths:
            missing = []
            converted = converter.convert_loot(json.loads(loot.read_text(encoding='utf-8')), missing)
            if missing:
                raise ValueError(f'{identifier}: unresolved loot references {missing}')
            report['generated_data'][f'data/{MOD}/loot_table/entities/{loot.name}'] = converted
        for language, names in source.lang.items():
            for key in (f'entity.{MOD}.{identifier}', f'item.{MOD}.{egg_id}'):
                report['lang_for_integration'][language][key] = names[key]

        sound_methods = '\n'.join(method['source'] for method in definitions if method['name'] in
                                  ('getAmbientSound', 'getHurtSound', 'getDeathSound', 'playStepSound'))
        sound_ids = sorted(set(re.findall(r'new ResourceLocation\("' + MOD + r':([^\"]+)"\)', sound_methods)))
        for sound_id in sound_ids:
            sound_definition = sounds.get(sound_id)
            if sound_definition is None:
                raise ValueError(f'{identifier}: missing sound definition {sound_id}')
            report['sounds_for_integration'][sound_id] = sound_definition
            for entry in sound_definition['sounds']:
                resource = entry if isinstance(entry, str) else entry['name']
                namespace, filename = resource.split(':', 1)
                if namespace != MOD:
                    raise ValueError(f'{identifier}: external sound not handled: {resource}')
                sound_path = assets / f'sounds/{filename}.ogg'
                if not sound_path.is_file():
                    raise FileNotFoundError(sound_path)
                report['copies'][f'assets/{MOD}/sounds/{filename}.ogg'] = {
                    'source': str(sound_path), 'sha256': hashlib.sha256(sound_path.read_bytes()).hexdigest()}
            subtitle = sound_definition.get('subtitle', 'subtitles.' + sound_id)
            for language, names in source.lang.items():
                if subtitle in names:
                    report['lang_for_integration'][language][subtitle] = names[subtitle]

        report['source_proof'].append({'id': identifier, 'source': path.relative_to(source.root).as_posix(),
            'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'methods': definitions,
            'procedure_closure': closed,
            'loot_sources': [loot.relative_to(source.root).as_posix() for loot in loot_paths],
            'loot_binding': 'Source list tables retain their own identifiers; they are not assigned as default death loot.'})

    report['integration'] = [
        '模型表 rendering.scale=1.0 仅为静态基准；动态体型由 YaoshouEntity.getDimensions() 按 entitydaxiao 计算。',
        'CreatureSetup、YaoshouBeasts、客户端渲染器与共享工厂入口由主会话按本计划合并。',
        '实体、技能、效果、灵兽袋死亡链闭合后，再合并本报告中的实体、物品、工厂、资源、语言及声音行。',
        '034明确排除；当前报告不声明行为完成。']
    report['source_closure_contracts'] = {
        'death_bag_clear': {
            'source_procedure': 'LingshoudaiqingkongProcedure',
            'when': '实体 die 调用 super.die 后执行',
            'linked_entity_fields': ['lingshoudaiuuid', 'belongToMasterUUID'],
            'owner_lookup': '仅在线玩家列表中按 UUID 匹配 belongToMasterUUID',
            'inventory_match': '按来源 forge:lingshoudai 物品标签查找且比较物品 NBT lingshoudaiuuid',
            'replacement': '在原物品栏位置构造同 registry item 的新物品并保留显示名；来源代码不复制其他NBT',
            'target_dependency': 'Creature 对玩家物品栏/袋内容所有权的公开操作；当前未实现，不能声明死亡链闭合'
        },
        'summoned_death': {
            'source_procedure': 'LingshoudaiqingkongProcedure',
            'condition': 'scoreboard objective zhaohuanwu == 1',
            'effect': '按来源具体实体播放对应粒子，然后服务端 discard；仅相关召唤物适用'
        }
    }
    report['verification'] = {'static_source': '注册、模型、过程闭包、资源哈希、语言及掉落来源已提取。',
        'build': '未执行', 'client': '未执行', 'server': '未执行',
        'behavior': '019与020非清袋行为及动画控制已接入；两者死亡清袋依赖未闭合。021-036尚未实现行为。'}
    return report


def write_resources(report: dict) -> None:
    target = ROOT / 'src/main/resources'
    outputs = {**report['generated_data'],
        'content/creature/yaoshou_019_036_models.json': report['models'],
        'content/creature/yaoshou_019_036_eggs.json': report['eggs']}
    for relative, value in outputs.items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    for relative, entry in report['copies'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(Path(entry['source']).read_bytes())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/yaoshou-019-036-migration-plan.json')
    parser.add_argument('--write-resources', action='store_true')
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    if args.write_resources:
        write_resources(report)
    print(json.dumps({'entities': len(report['models']), 'assets': len(report['copies']),
        'loot_tables': len(report['generated_data']), 'write_resources': args.write_resources}, ensure_ascii=False))


if __name__ == '__main__':
    main()
