"""从 1.18.2 来源提取指定系统动画实体的模型表现（默认 skill，--owner xiulian 可提取雷劫实体）。

每个动画实体（MCreator 的 animated entity，GeckoLib 3）由四处生成代码决定表现：
- `entity/model/<名>Model.java`：几何文件、动画文件、头部骨骼（`getBone`），纹理固定为 `textures/entities/<实体的 TEXTURE 字符串>.png`；
- `entity/layer/<名>Layer.java`：可选的发光层，用 `RenderType.eyes` 再画一遍同一几何，纹理固定；
- `client/renderer/<名>Renderer.java`：阴影半径；
- `entity/<名>Entity.java`：默认纹理（`define(TEXTURE, …)`）、默认循环动画（`movementPredicate` 在没有过程动画时播放，取最后一个无条件的）、离地时的循环动画、
  五项生物属性（移速、生命、护甲、攻击、跟踪范围）、是否无 AI、导航/固定无重力/飞行与游泳速度/水下呼吸/水流推动/死亡移除刻数。`Skillyin01` 另有 walk、die、swim、sprint 的条件动画，由 Java 侧单独实现。
输出 src/main/resources/content/<系统>/entity_models.json（一行一个实体，键为注册 ID），并重写
docs/reference/conversion/system_assets.csv 中归该系统的几何、动画与实体纹理行（默认纹理、发光层纹理、
相关过程里 `setTexture("…")` 引用到的纹理）；保留其他类型的资源行。工具可重复运行，输出只取决于来源。
"""
from __future__ import annotations

import argparse
import csv
import io
import json
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

MOD = 'yvanchuxiuzhen'
BASE = SOURCES[MOD][0] / f'src/main/java/net/mcreator/{MOD}'
ASSETS = ROOT / 'docs/reference/conversion/system_assets.csv'
OWNERSHIP = ROOT / 'docs/reference/legacy/inventory/ownership_assignment.csv'

def read(path) -> str:
    return path.read_text(encoding='utf-8-sig') if path.exists() else ''


def squash(text: str) -> str:
    return re.sub(r'\s+', ' ', text)


def animated_entities(owner: str) -> list[str]:
    with OWNERSHIP.open(encoding='utf-8', newline='') as handle:
        return [row['element'] for row in csv.DictReader(handle)
                if row['owner'] == owner and row['type'] == 'animatedentity' and row['source_mod'] == MOD]


def registry_ids() -> dict[str, str]:
    text = read(BASE / 'init/YvanchuxiuzhenModEntities.java')
    return {cls: path for cls, path in re.findall(r'RegistryObject<EntityType<(\w+)Entity>> \w+ = register\("(\w+)"', text)}


def parse(name: str) -> dict:
    model = read(BASE / f'entity/model/{name}Model.java')
    layer = read(BASE / f'entity/layer/{name}Layer.java')
    renderer = read(BASE / f'client/renderer/{name}Renderer.java')
    entity = read(BASE / f'entity/{name}Entity.java')
    predicate = re.search(r'PlayState movementPredicate\(.*?\n\t\}\n', entity, re.S)
    steps = re.findall(r'(?:if \(([^{]*?)\) \{ )?event\.getController\(\)\.setAnimation\(new AnimationBuilder\(\)\.addAnimation\("([\w.]+)", EDefaultLoopTypes\.(\w+)\)\)',
                       squash(predicate[0]).replace('if (this.animationprocedure.equals("empty")) { ', '')) if predicate else []
    row: dict = {
        'geo': re.search(r'geo/([\w]+)\.geo\.json"', model)[1],
        'animation': re.search(r'animations/([\w]+)\.animation\.json"', model)[1],
        'shadow': float(re.search(r'shadowRadius = ([\d.]+)f', renderer)[1]),
        'texture': re.search(r'define\(TEXTURE, "(\w+)"\)', entity)[1],
    }
    head = re.search(r'getBone\("(\w+)"\)', model)
    if head:
        row['head'] = head[1]
    glow = re.search(r'textures/entities/(\w+)\.png', layer)
    if glow:
        row['glow'] = glow[1]
    airborne = [animation for condition, animation, _ in steps if condition and '!this.isOnGround()' in condition]
    base = [(animation, loop) for condition, animation, loop in steps if not condition]
    if airborne:
        row['air'] = airborne[0]
    row['idle'] = base[-1][0]
    for key, attribute in (('speed', 'MOVEMENT_SPEED'), ('health', 'MAX_HEALTH'), ('armor', 'ARMOR'), ('attack', 'ATTACK_DAMAGE'), ('follow', 'FOLLOW_RANGE')):
        row[key] = float(re.search(rf'Attributes\.{attribute}, ([\d.]+)', entity)[1])
    row['no_ai'] = 'setNoAi(true)' in entity
    navigation = re.search(r'return new (\w+PathNavigation)\(this, world\)', entity)
    gravity = re.search(r'public void setNoGravity\(boolean \w+\) \{\s*super.setNoGravity\((true|false)\);', entity)
    death = re.search(r'protected void tickDeath\(\).*?this.deathTime == (\d+)', entity, re.S)
    row['lifecycle'] = {
        'navigation': {'FlyingPathNavigation': 'flying', 'WaterBoundPathNavigation': 'water'}[navigation[1]] if navigation else 'ground',
        'no_gravity': gravity is not None and gravity[1] == 'true',
        'breathes_underwater': bool(re.search(r'public boolean canBreatheUnderwater\(\) \{\s*return true;', entity)),
        'pushed_by_fluid': not bool(re.search(r'public boolean isPushedByFluid\(\) \{\s*return false;', entity)),
        'death_ticks': int(death[1]),
    }
    flying_speed = re.search(r'Attributes.FLYING_SPEED, ([\d.]+)', entity)
    if flying_speed:
        row['lifecycle']['flying_speed'] = float(flying_speed[1])
    swim_speed = re.search(r'ForgeMod.SWIM_SPEED.get\(\), ([\d.]+)', entity)
    if swim_speed:
        row['lifecycle']['swim_speed'] = float(swim_speed[1])
    return row


def referenced_textures(names: list[str]) -> set[str]:
    found = set()
    for path in (BASE / 'procedures').glob('*.java'):
        text = read(path)
        if not names or any(name + 'Entity' in text for name in names):
            found.update(re.findall(r'setTexture\("([^"]+)"\)', text))
    return found


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--owner', choices=('skill', 'xiulian'), default='skill')
    owner = parser.parse_args().owner
    output = ROOT / f'src/main/resources/content/{owner}/entity_models.json'
    ids = registry_ids()
    rows = {}
    names = animated_entities(owner)
    for name in names:
        rows[ids[name]] = parse(name)
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text('[\n' + ',\n'.join(json.dumps({'id': key, **value}, ensure_ascii=False, separators=(',', ':'))
                                       for key, value in sorted(rows.items())) + '\n]\n', encoding='utf-8', newline='\n')
    textures = {row['texture'] for row in rows.values()} | referenced_textures(names if owner != 'skill' else []) | {row['glow'] for row in rows.values() if 'glow' in row}
    with ASSETS.open(encoding='utf-8', newline='') as handle:
        kept = [row for row in csv.reader(handle) if row and (row[0] != owner or row[1] not in ('geo', 'animation')
                and not (row[1] == 'texture' and row[2].startswith(f'{NAMESPACE}:entities/')))]
    kept += [[owner, 'geo', f'{NAMESPACE}:{row["geo"]}'] for row in rows.values()]
    kept += [[owner, 'animation', f'{NAMESPACE}:{row["animation"]}'] for row in rows.values()]
    kept += [[owner, 'texture', f'{NAMESPACE}:entities/{texture}'] for texture in sorted(textures)]
    seen, unique = set(), []
    for row in kept:
        if tuple(row) not in seen:
            seen.add(tuple(row))
            unique.append(row)
    out = io.StringIO(newline='')
    csv.writer(out, lineterminator='\n').writerows(unique)
    ASSETS.write_text(out.getvalue(), encoding='utf-8', newline='')
    print('entity_models', len(rows), 'textures', len(textures))


if __name__ == '__main__':
    main()
