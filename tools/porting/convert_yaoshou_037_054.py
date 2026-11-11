"""转换037–054妖兽专属资源和整合计划；保留逐编号实现状态，不写共享登记表。"""
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
NUMBERS = tuple(number for number in range(37, 55) if number != 44)
IMPLEMENTED_NUMBERS = (38, 41, 42, 43, 45, 49, 52)


def build() -> dict:
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

    for number in NUMBERS:
        name, identifier = f'Yaoshou{number:03}', f'yaoshou_{number:03}'
        path = java / f'entity/{name}Entity.java'
        body = path.read_text(encoding='utf-8')
        model = (java / f'entity/model/{name}Model.java').read_text(encoding='utf-8')
        renderer = (java / f'client/renderer/{name}Renderer.java').read_text(encoding='utf-8')
        definitions = {method['name']: method['source'] for method in methods(body)}
        row = {'id': identifier,
               'geo': re.search(r'geo/(\w+)\.geo\.json', model)[1],
               'animation': re.search(r'animations/(\w+)\.animation\.json', model)[1],
               'texture': re.search(r'define\(TEXTURE, "(\w+)"\)', body)[1],
               'rendering': {'shadow': float(re.search(r'shadowRadius = ([\d.]+)f', renderer)[1]), 'scale': 1.0},
               'no_ai': 'setNoAi(true)' in body}
        if 'YaoshoudaxiaoxianshiProcedure.execute(entity)' not in renderer:
            raise ValueError(f'{name}不是来源动态体型渲染，需逐项适配')
        death_rotation = re.search(r'getDeathMaxRotation\([^}]+return ([\d.]+)F;', renderer)
        if death_rotation:
            row['rendering']['death_rotation'] = float(death_rotation[1])
        head = re.search(r'getBone\("(\w+)"\)', model)
        if head:
            row['head'] = head[1]
        movement = definitions['movementPredicate']
        animations = re.findall(r'addAnimation\("([^"]+)", EDefaultLoopTypes\.(\w+)\)', movement)
        row['idle'] = animations[-1][0]
        for key, attribute in (('speed', 'MOVEMENT_SPEED'), ('health', 'MAX_HEALTH'), ('armor', 'ARMOR'),
                               ('attack', 'ATTACK_DAMAGE'), ('follow', 'FOLLOW_RANGE')):
            row[key] = float(re.search(rf'Attributes\.{attribute}, ([\d.]+)', body)[1])
        navigation = re.search(r'return new (\w+PathNavigation)\(this, world\)', body)
        immunities = []
        for condition in re.findall(r'if \((.*?)\)\s*return false;', definitions.get('hurt', '')):
            if condition not in DAMAGE_IMMUNITIES:
                raise ValueError(f'{name}未处理的伤害免疫：{condition}')
            immunities.extend(DAMAGE_IMMUNITIES[condition])
        row['lifecycle'] = {'navigation': {'FlyingPathNavigation': 'flying', 'WaterBoundPathNavigation': 'water'}[navigation[1]] if navigation else 'ground',
            'no_gravity': bool(re.search(r'public void setNoGravity\(boolean \w+\) \{\s*super.setNoGravity\(true\)', body)),
            'breathes_underwater': 'canBreatheUnderwater' in definitions,
            'pushed_by_fluid': 'return false;' not in definitions.get('isPushedByFluid', ''),
            'death_ticks': int(re.search(r'this.deathTime == (\d+)', definitions['tickDeath'])[1]),
            'ignores_fall_damage': 'return false;' in definitions.get('causeFallDamage', ''),
            'damage_immunities': immunities,
            'animation_transition_ticks': int(re.search(r'new AnimationController<>\(this, "movement", (\d+),', body)[1])}
        death_animation = re.search(r'if \(this.isDeadOrDying\(\)\) \{\s*event.getController\(\).setAnimation\(new AnimationBuilder\(\).addAnimation\("([^"]+)", EDefaultLoopTypes.PLAY_ONCE\)', movement)
        if death_animation:
            row['lifecycle']['death_animation'] = death_animation[1]
        for attribute, field in (('FLYING_SPEED', 'flying_speed'), ('SWIM_SPEED.get()', 'swim_speed')):
            value = re.search(re.escape(attribute) + r', ([\d.]+)', body)
            if value:
                row['lifecycle'][field] = float(value[1])
        report['models'].append(row)
        for kind, relative, value in (('geo', f'geo/{row["geo"]}.geo.json', row['geo']),
                                     ('animation', f'animations/{row["animation"]}.animation.json', row['animation']),
                                     ('texture', f'textures/entities/{row["texture"]}.png', 'entities/' + row['texture'])):
            copy(relative)
            asset(kind, value)
        layer = java / f'entity/layer/{name}Layer.java'
        if layer.is_file():
            glow = re.search(r'textures/entities/(\w+)\.png', layer.read_text(encoding='utf-8'))
            if glow:
                row['glow'] = glow[1]
                copy(f'textures/entities/{glow[1]}.png')
                asset('texture', 'entities/' + glow[1])

        pending = list(set(PROCEDURE.findall(body)))
        closed = {}
        procedure_bodies = []
        while pending:
            procedure = pending.pop()
            if procedure in closed:
                continue
            procedure_path = procedures[procedure]
            text = procedure_path.read_text(encoding='utf-8')
            procedure_bodies.append(text)
            calls = sorted(set(PROCEDURE.findall(text)))
            closed[procedure] = {'source': procedure_path.relative_to(source.root).as_posix(),
                'sha256': hashlib.sha256(procedure_path.read_bytes()).hexdigest(), 'calls': calls,
                'content_references': sorted(set(re.findall(r'YvanchuxiuzhenMod\w+\.\w+', text))),
                'capability_access': 'PLAYER_VARIABLES_CAPABILITY' in text}
            pending.extend(calls)
            for texture in re.findall(r'setTexture\("([^\"]+)"\)', text):
                copy(f'textures/entities/{texture}.png')
                asset('texture', 'entities/' + texture)
        builder = re.search(rf'register\("{identifier}",\s*EntityType\.Builder\.<\w+>of\(\w+::new, MobCategory\.(\w+)\)(.*?)\)\);', source.entities_text, re.S)
        chain = builder[2]
        size = re.search(r'\.sized\(([\d.]+)f?,\s*([\d.]+)f?', chain)
        tracking = re.search(r'setTrackingRange\((\d+)\)', chain)
        interval = re.search(r'setUpdateInterval\((\d+)\)', chain)
        report['entity_rows_for_integration'].append({'id': identifier, 'category': builder[1],
            'width': float(size[1]), 'height': float(size[2]), 'tracking_range': int(tracking[1]) if tracking else 5,
            'update_interval': int(interval[1]) if interval else 3, 'fire_immune': '.fireImmune()' in chain})
        egg_id = identifier + '_spawn_egg'
        egg = re.search(rf'register\("{egg_id}",\s*\(\) -> new ForgeSpawnEggItem\(\w+\.\w+, (-?\d+), (-?\d+), (new Item\.Properties\(\).*?)\)\);', source.items_text, re.S)
        report['eggs'].append({'id': identifier, 'base_color': int(egg[1]), 'spot_color': int(egg[2])})
        report['item_rows_for_integration'].append({'id': egg_id, 'factory': 'creature:spawn_egg_' + identifier,
            'order': orders[MOD + ':' + egg_id], **parse_item_properties(egg[3], source.tabs)})
        report['factory_overrides_for_integration'].append({'registry': 'item', 'target_id': MOD + ':' + egg_id,
            'factory': 'creature:spawn_egg_' + identifier, 'owner': 'creature', 'reason': name + '来源刷怪蛋；实体行为闭合后接入'})
        copy(f'models/item/{egg_id}.json')
        asset('item', egg_id)
        loot_paths = sorted((data / 'loot_tables/entities').glob(identifier + '*.json'))
        for loot in loot_paths:
            missing = []
            converted = converter.convert_loot(json.loads(loot.read_text(encoding='utf-8')), missing)
            if missing:
                raise ValueError(f'{identifier}掉落引用未闭合：{missing}')
            report['generated_data'][f'data/{MOD}/loot_table/entities/{loot.name}'] = converted
        for language, names in source.lang.items():
            for key in (f'entity.{MOD}.{identifier}', f'item.{MOD}.{egg_id}'):
                report['lang_for_integration'][language][key] = names[key]
        sound_sources = '\n'.join([body, *procedure_bodies])
        for sound in sorted(set(re.findall(r'new ResourceLocation\("' + MOD + r':([^\"]+)"\)', sound_sources))):
            if sound not in sounds:
                continue
            report['sounds_for_integration'][sound] = sounds[sound]
            for entry in sounds[sound]['sounds']:
                resource = entry if isinstance(entry, str) else entry['name']
                namespace, filename = resource.split(':', 1)
                if namespace != MOD:
                    raise ValueError(f'未处理外部声音：{resource}')
                copy(f'sounds/{filename}.ogg')
            subtitle = sounds[sound].get('subtitle', 'subtitles.' + sound)
            for language, names in source.lang.items():
                if subtitle in names:
                    report['lang_for_integration'][language][subtitle] = names[subtitle]
        report['source_proof'].append({'id': identifier, 'source': path.relative_to(source.root).as_posix(),
            'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'methods': definitions,
            'procedure_closure': closed, 'render_scale': 'YaoshoudaxiaoxianshiProcedure.execute(entity)',
            'animations_in_source_order': animations,
            'loot_sources': [loot.relative_to(source.root).as_posix() for loot in loot_paths],
            'loot_binding': '来源列表用的分表保留独立ID，不将其改为默认死亡掉落。',
            'implementation_status': '实体生命周期与逐刻技能已接回，尚待共享接线及构建/实机' if number in IMPLEMENTED_NUMBERS else '待实现，不能仅凭资源准备接入占位实体',
            'target_entity_class': 'net.yvanchuxiuzhen.system.creature.entity.yaoshou.' + name + 'Entity' if number in IMPLEMENTED_NUMBERS else None})
    report['implemented_entity_ids'] = [f'yaoshou_{number:03}' for number in IMPLEMENTED_NUMBERS]
    report['pending_entity_ids'] = [f'yaoshou_{number:03}' for number in NUMBERS if number not in IMPLEMENTED_NUMBERS]
    report['integration'] = ['模型row的scale=1只是动态渲染的基数；碰撞体与渲染体型继续读取YaoshouEntity的来源尺寸属性。',
        '实体、属性、每刻技能、效果和灵兽袋死亡链完整闭合后，才合并对应entity/item/factory/语言/声音及客户端注册行。',
        '037–043、045–054全部保持逐编号注册身份；044没有来源注册，不生成占位。',
        '仅implemented_entity_ids包含本批可接线的独立实体实现，其余编号保留资源准备状态。',
        '042沿来源安排5/10/15/20服务端刻的雷粒子回调，使用已有SkillDelays公开入口；043落雷使用既有lightninglanding命令并从Relations读取团队。',
        '052调用的吹摆过程仅在053类上播放动画，目标同样保留此编号条件；伤害、冻结和范围循环仍对052执行。']
    report['verification'] = {'static_source': '已提取编号注册、来源方法及过程闭包、动态缩放依据、资源哈希与语言。',
        'build': '未执行', 'client': '未执行', 'server': '未执行',
        'behavior': '038/041/042/043/045/049/052生命周期与逐刻技能已实现，尚未共享接线和运行验收；其余10个编号未实现行为。'}
    return report


def write_resources(report: dict) -> None:
    target = ROOT / 'src/main/resources'
    for relative, value in {**report['generated_data'],
                            'content/creature/yaoshou_037_054_models.json': report['models'],
                            'content/creature/yaoshou_037_054_eggs.json': report['eggs']}.items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8', newline='\n')
    for relative, entry in report['copies'].items():
        path = target / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(Path(entry['source']).read_bytes())


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/yaoshou-037-054-migration-plan.json')
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
