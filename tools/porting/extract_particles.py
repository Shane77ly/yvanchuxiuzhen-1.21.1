"""从已登记粒子的来源客户端类提取原生贴图粒子参数，不执行编辑器中的推测配置。

构造、刻推进与光照方法必须符合已核实的 MCreator 模板；遇到其他行为直接报告来源，不能静默丢弃。
粒子 JSON 与 sprite 由 convert_assets.py 按同一台账映射迁移。
"""
from __future__ import annotations

import json
import re

from build_ledger import ROOT, read_ledgers
from convert_content import SOURCES


def compact(text: str) -> str:
    return re.sub(r'\s+', '', text)


def number(text: str) -> float:
    return float(text.removesuffix('f'))


def definition(row: dict) -> dict:
    mod, path = row['legacy_id'].split(':', 1)
    source = SOURCES[mod][0] / f'src/main/java/net/mcreator/{mod}/client/particle/{row["element"]}Particle.java'
    text = source.read_text(encoding='utf-8')
    constructor = re.search(r'protected \w+Particle\([^\n]+\) \{(.*?)\n\t\}', text, re.S)[1]
    tick = re.search(r'public void tick\(\) \{(.*?)\n\t\}', text, re.S)[1]
    size = re.search(r'this.setSize\(([^,]+), ([^)]+)\);', constructor)
    scale = re.search(r'this.quadSize \*= ([^;]+);', constructor)
    values = dict(re.findall(r'this\.(\w+) = ([^;]+);', constructor))
    life = values['lifetime']
    spread = 0
    if not life.isdigit():
        random_life = re.fullmatch(r'\(int\) Math.max\(1, (\d+) \+ \(this.random.nextInt\((\d+)\) - (\d+)\)\)', life)
        if random_life is None or int(random_life[2]) != int(random_life[3]) * 2:
            raise ValueError(f'{source}: 未核实的寿命表达式 {life}')
        lifetime, spread = int(random_life[1]), int(random_life[3])
    else:
        lifetime = int(life)
    speed = [re.fullmatch(rf'{axis} \* ([\d.]+)', values[field]) for axis, field in zip(('vx', 'vy', 'vz'), ('xd', 'yd', 'zd'))]
    if not all(speed) or len({match[1] for match in speed}) != 1:
        raise ValueError(f'{source}: 未核实的速度表达式')
    animation = re.search(r'spriteSet.get\(\(this.age / (\d+)\) % (\d+) \+ 1, (\d+)\)', tick)
    if animation and animation[2] != animation[3]:
        raise ValueError(f'{source}: 不同的帧计数与 sprite 分母')
    rotating = 'angularVelocity' in values
    expected_constructor = 'super(world,x,y,z);this.spriteSet=spriteSet;'
    expected_constructor += f'this.setSize({size[1]},{size[2]});'
    if scale:
        expected_constructor += f'this.quadSize*={scale[1]};'
    for field in ('lifetime', 'gravity', 'hasPhysics', 'xd', 'yd', 'zd'):
        expected_constructor += f'this.{field}={values[field]};'
    if rotating:
        for field in ('angularVelocity', 'angularAcceleration'):
            expected_constructor += f'this.{field}={values[field]};'
    expected_constructor += 'this.setSpriteFromAge(spriteSet);' if animation else 'this.pickSprite(spriteSet);'
    expected_tick = 'super.tick();'
    if rotating:
        expected_tick += 'this.oRoll=this.roll;this.roll+=this.angularVelocity;this.angularVelocity+=this.angularAcceleration;'
    if animation:
        expected_tick += f'if(!this.removed){{this.setSprite(this.spriteSet.get((this.age/{animation[1]})%{animation[2]}+1,{animation[3]}));}}'
    methods = set(re.findall(r'(?:public|protected) (?:static )?\w+ (\w+)\(', text))
    if compact(constructor) != compact(expected_constructor) or compact(tick) != expected_tick or methods - {'provider', 'createParticle', 'getLightColor', 'getRenderType', 'tick'}:
        raise ValueError(f'{source}: 粒子模板含未核实行为')
    light = re.search(r'public int getLightColor\(float partialTick\) \{(.*?)\}', text, re.S)
    if light and compact(light[1]) != 'return15728880;':
        raise ValueError(f'{source}: 未核实的光照方法')
    render = re.search(r'return ParticleRenderType.PARTICLE_SHEET_(\w+);', text)[1].lower()
    if render not in ('opaque', 'lit', 'translucent'):
        raise ValueError(f'{source}: 未核实的渲染层 {render}')
    render_body = re.search(r'public ParticleRenderType getRenderType\(\) \{(.*?)\}', text, re.S)[1]
    factory_body = re.search(r'public Particle createParticle\([^\n]+\) \{(.*?)\}', text, re.S)[1]
    if compact(render_body) != f'returnParticleRenderType.PARTICLE_SHEET_{render.upper()};' or compact(factory_body) != f'returnnew{row["element"]}Particle(worldIn,x,y,z,xSpeed,ySpeed,zSpeed,this.spriteSet);':
        raise ValueError(f'{source}: 未核实的渲染或提供器逻辑')
    registry = (SOURCES[mod][0] / row['source_file']).read_text(encoding='utf-8')
    registration = re.search(rf'(\w+) = REGISTRY.register\("{path}", \(\) -> new SimpleParticleType\((true|false)\)\)', registry)
    providers = (SOURCES[mod][0] / row['source_file'].replace('ModParticleTypes.java', 'ModParticles.java')).read_text(encoding='utf-8')
    if not re.search(rf'particleEngine.register\(\(SimpleParticleType\) \w+\.{registration[1]}\.get\(\), {row["element"]}Particle::provider\)', providers):
        raise ValueError(f'{source}: 提供器没有按该粒子类型登记')
    always = registration[2] == 'true'
    return {
        'id': row['target_id'].split(':', 1)[1],
        'width': number(size[1]), 'height': number(size[2]), 'scale': number(scale[1]) if scale else 1.0,
        'lifetime': lifetime, 'lifetime_jitter': spread,
        'gravity': number(values['gravity']), 'physics': values['hasPhysics'] == 'true', 'speed': float(speed[0][1]),
        'angular_velocity': number(values['angularVelocity']) if rotating else 0.0,
        'angular_acceleration': number(values['angularAcceleration']) if rotating else 0.0,
        'render': render, 'full_bright': light is not None,
        'frames': int(animation[2]) if animation else 0, 'frame_ticks': int(animation[1]) if animation else 1,
        'always_show': always,
    }


def main() -> None:
    rows = [row for unit in read_ledgers().values() for row in unit if row['registry'] == 'particle_type' and row['status'] != '待处理']
    definitions = sorted((definition(row) for row in rows), key=lambda item: item['id'])
    output = ROOT / 'src/main/resources/content/particle/particles.json'
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text('[\n' + ',\n'.join(json.dumps(item, ensure_ascii=False, separators=(',', ':')) for item in definitions) + '\n]\n', encoding='utf-8', newline='')
    print('particles', len(definitions))


if __name__ == '__main__':
    main()
