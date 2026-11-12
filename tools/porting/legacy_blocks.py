"""读取 MCreator 生成的 1.18.2 方块类，提取内容表所需的基础属性。

生成的 Java 是来源的实际行为：构造器的 Properties 链、状态属性、放置规则、形状、光照、可燃、挖掘判定与回退掉落
都由这里解析。遇到未识别的写法时抛出 ValueError，不静默忽略，由调用者决定该方块是否留给所属系统。
"""
from __future__ import annotations

import re

from legacy_items import tooltip_lines

BASES = ('Block', 'SlabBlock', 'StairBlock', 'WallBlock', 'LeavesBlock', 'FenceBlock', 'FenceGateBlock', 'WoodButtonBlock',
         'PressurePlateBlock', 'IronBarsBlock', 'DoorBlock', 'TrapDoorBlock', 'FallingBlock', 'FlowerBlock', 'DoublePlantBlock')
# 类里出现这些覆写说明方块带有玩法行为，B1 只登记身份与基础属性，行为由所属系统迁移。
BEHAVIOR_HOOKS = ('use', 'onPlace', 'onRemove', 'tick', 'randomTick', 'neighborChanged', 'entityInside', 'setPlacedBy',
                  'wasExploded', 'onDestroyedByPlayer', 'onProjectileHit', 'attack', 'animateTick', 'triggerEvent',
                  'newBlockEntity', 'getMenuProvider', 'createMenu', 'getDisplayName', 'hasAnalogOutputSignal',
                  'getAnalogOutputSignal', 'isValidBonemealTarget', 'isBonemealSuccess', 'performBonemeal',
                  'canSustainPlant', 'canConnectRedstone', 'getAiPathNodeType', 'getCloneItemStack')
# 已由内容表字段承载的覆写。
HANDLED = {'getLightBlock', 'propagatesSkylightDown', 'getVisualShape', 'getShape', 'createBlockStateDefinition',
           'getStateForPlacement', 'getFluidState', 'updateShape', 'getDrops', 'getFlammability', 'getFireSpreadSpeed',
           'canHarvestBlock', 'rotate', 'mirror', 'getPistonPushReaction', 'getOffsetType', 'getEnchantPowerBonus',
           'getExplosionResistance', 'isRandomlyTicking', 'canBeReplaced', 'appendHoverText', 'canSurvive',
           'getPlantType', 'getEffectDuration', 'mayPlaceOn'}
EFFECTS = {'MOVEMENT_SLOWDOWN': 'slowness', 'MOVEMENT_SPEED': 'speed', 'DIG_SLOWDOWN': 'mining_fatigue', 'DIG_SPEED': 'haste',
           'DAMAGE_BOOST': 'strength', 'HARM': 'instant_damage', 'JUMP': 'jump_boost', 'CONFUSION': 'nausea',
           'DAMAGE_RESISTANCE': 'resistance', 'POISON': 'poison', 'REGENERATION': 'regeneration', 'FIRE_RESISTANCE': 'fire_resistance',
           'HUNGER': 'hunger', 'LUCK': 'luck', 'UNLUCK': 'unluck', 'WITHER': 'wither', 'HERO_OF_THE_VILLAGE': 'hero_of_the_village',
           'NIGHT_VISION': 'night_vision', 'GLOWING': 'glowing', 'SATURATION': 'saturation', 'WEAKNESS': 'weakness',
           'BLINDNESS': 'blindness'}


def _balanced(text: str, start: int) -> str:
    """返回 text[start] 处开括号对应的括号内内容。"""
    depth = 0
    for i in range(start, len(text)):
        if text[i] == '(':
            depth += 1
        elif text[i] == ')':
            depth -= 1
            if depth == 0:
                return text[start + 1:i]
    raise ValueError('括号不平衡')


def _method(source: str, name: str) -> str | None:
    match = re.search(r'(?:public|protected)\s+[\w<>\[\], ?.]+\s+' + name + r'\([^)]*\)\s*\{', source)
    if not match:
        return None
    depth = 1
    i = match.end()
    while depth:
        depth += {'{': 1, '}': -1}.get(source[i], 0)
        i += 1
    return source[match.end():i - 1]


def _number(text: str) -> float:
    return float(text.rstrip('fFdD'))


def _split_args(text: str) -> list[str]:
    parts, depth, current = [], 0, ''
    for char in text:
        if char == ',' and depth == 0:
            parts.append(current.strip())
            current = ''
            continue
        depth += {'(': 1, ')': -1}.get(char, 0)
        current += char
    if current.strip():
        parts.append(current.strip())
    return parts


def _shape_expr(text: str) -> dict:
    """把形状表达式解析为树：box、or、subtract（Shapes.join ONLY_FIRST）、empty。"""
    text = text.strip()
    if text.startswith('(') and text.endswith(')'):
        text = text[1:-1].strip()
    if text.endswith('.move(offset.x, offset.y, offset.z)'):
        text = text[:-len('.move(offset.x, offset.y, offset.z)')]
    if text == 'Shapes.empty()':
        return {'empty': True}
    if text.startswith('box(') and text.endswith(')'):
        values = [_number(v) for v in text[4:-1].split(',')]
        if len(values) != 6:
            raise ValueError('方块盒参数数量不对：' + text)
        return {'box': values}
    if text.startswith('Shapes.or(') and text.endswith(')'):
        return {'or': [_shape_expr(a) for a in _split_args(text[len('Shapes.or('):-1])]}
    if text.startswith('Shapes.join(') and text.endswith(')'):
        args = _split_args(text[len('Shapes.join('):-1])
        if len(args) != 3 or args[2] != 'BooleanOp.ONLY_FIRST':
            raise ValueError('Shapes.join 运算未识别：' + text[:80])
        return {'subtract': [_shape_expr(args[0]), _shape_expr(args[1])]}
    raise ValueError('形状含未识别写法：' + text[:80])


def _shape(body: str) -> dict[str, dict]:
    body = body.strip()
    switch = re.search(r'switch \(state\.getValue\((\w+)\)\) \{(.*)\}', body, re.S)
    if not switch:
        single = re.search(r'return (.*);\s*$', body, re.S)
        return {'default': _shape_expr(single.group(1))}
    result = {}
    for label, expression in re.findall(r'(default|case \w+) -> (.*?);\s*(?=default|case |\}|\Z)', switch.group(2), re.S):
        result[label.replace('case ', '').lower()] = _shape_expr(expression)
    return result


def _item_ref(expression: str, items: dict[str, str], blocks: dict[str, str]) -> tuple[str, int]:
    """把回退掉落的物品表达式转为 (物品 ID, 数量)。"""
    expression = expression.strip()
    count = 1
    if ',' in expression and not expression.startswith('new'):
        expression, amount = expression.rsplit(',', 1)
        count = int(amount)
    if expression == 'this':
        return 'self', count
    if expression.startswith('Blocks.') or expression.startswith('Items.'):
        return 'minecraft:' + expression.split('.', 1)[1].removesuffix('.asItem()').lower(), count
    found = re.fullmatch(r'\w+ModItems\.(\w+)\.get\(\)', expression)
    if found:
        return items[found.group(1)], count
    found = re.fullmatch(r'\w+ModBlocks\.(\w+)\.get\(\)', expression)
    if found:
        return blocks[found.group(1)], count
    raise ValueError('无法解析掉落物品：' + expression)


def parse_block(source: str, items: dict[str, str], blocks: dict[str, str]) -> dict:
    """解析一个生成的方块类。items、blocks 把 ModItems/ModBlocks 常量名映射为注册 ID。"""
    base = re.search(r'public class \w+ extends (\w+)', source).group(1)
    row: dict = {'base': base}
    ctor = re.search(r'public \w+\(\) \{\s*super\((.*?)\);\n', source, re.S)
    chain = re.sub(r'\s+', ' ', ctor.group(1))
    properties = chain[chain.index('BlockBehaviour.Properties.of('):]
    material = re.match(r'BlockBehaviour\.Properties\.of\((Material\.\w+)(?:, (MaterialColor\.\w+))?\)', properties)
    row['material'] = material.group(1).split('.')[1]
    if material.group(2):
        row['map_color'] = material.group(2).split('.')[1]
    custom_sound = re.search(r'\.sound\(new ForgeSoundType\(1\.0f, 1\.0f, ((?:\(\) -> new SoundEvent\(new ResourceLocation\("[\w.]+"\)\)(?:, )?){5})\)\)', properties)
    if custom_sound:
        row['sound'] = dict(zip(('break', 'step', 'place', 'hit', 'fall'), re.findall(r'ResourceLocation\("([\w.]+)"\)', custom_sound.group(1))))
    else:
        row['sound'] = re.search(r'\.sound\(SoundType\.(\w+)\)', properties).group(1)
    strength = re.search(r'\.strength\(([\d.]+)f?, ?([\d.]+)f?\)', properties)
    if strength:
        row['strength'] = [_number(strength.group(1)), _number(strength.group(2))]
    for flag, key in (('instabreak', 'instabreak'), ('noOcclusion', 'no_occlusion'), ('noCollission', 'no_collision'),
                      ('randomTicks', 'random_ticks'), ('requiresCorrectToolForDrops', 'requires_tool'),
                      ('dynamicShape', 'dynamic_shape'), ('hasPostProcess', 'post_process'),
                      ('emissiveRendering', 'emissive'), ('noDrops', 'no_drops')):
        if f'.{flag}(' in properties:
            row[key] = True
    if '.isRedstoneConductor((bs, br, bp) -> false)' in properties:
        row['redstone_conductor'] = False
    light = re.search(r'\.lightLevel\(s -> (\d+)\)', properties)
    if light:
        row['light'] = int(light.group(1))
    for key, call in (('speed_factor', 'speedFactor'), ('jump_factor', 'jumpFactor'), ('friction', 'friction')):
        found = re.search(rf'\.{call}\(([\d.]+)f?\)', properties)
        if found:
            row[key] = _number(found.group(1))

    declared = re.findall(r'public static final (?:DirectionProperty|BooleanProperty|IntegerProperty|EnumProperty<[\w.]+>) (\w+) = ([^;]+);', source)
    row['states'] = [name for name, _ in declared]
    if ('FACING', 'DirectionalBlock.FACING') in declared:
        row['facing_all'] = True
    placement = _method(source, 'getStateForPlacement')
    if placement is not None:
        simple = {
            'FACING, context.getHorizontalDirection().getOpposite()': 'horizontal',
            'AXIS, context.getClickedFace().getAxis()': 'axis',
            'FACING, context.getClickedFace()': 'clicked_face',
            'FACING, context.getNearestLookingDirection().getOpposite()': 'look',
        }
        maple_resin_placement = ('context.getClickedFace().getAxis() == Direction.Axis.Y' in placement
                                 and 'FACING, Direction.NORTH' in placement
                                 and 'FACING, context.getClickedFace()' in placement)
        sets = re.findall(r'\.setValue\(([^;]*?)\)(?=\.setValue|;)', placement)
        if maple_resin_placement:
            sets = [value for value in sets if value != 'FACING, Direction.NORTH']
        kinds = [simple[s] for s in sets if s in simple]
        rest = [s for s in sets if s not in simple and not s.startswith('WATERLOGGED')]
        if rest or ('if (' in placement and not maple_resin_placement) or len(kinds) > 1:
            raise ValueError('放置规则未识别：' + placement.strip()[:160])
        if kinds:
            row['placement'] = kinds[0]
    if 'WATERLOGGED' in row['states']:
        row['waterlogged'] = True
    unknown_states = [s for s in row['states'] if s not in ('FACING', 'WATERLOGGED', 'AXIS')]
    if unknown_states:
        raise ValueError('状态属性未识别：' + ','.join(unknown_states))

    light_block = _method(source, 'getLightBlock')
    if light_block is not None:
        row['light_block'] = int(re.search(r'return (\d+);', light_block).group(1))
    skylight = _method(source, 'propagatesSkylightDown')
    if skylight is not None:
        row['skylight'] = 'always' if 'return true;' in skylight else 'fluid_empty'
    if _method(source, 'getVisualShape') is not None:
        row['empty_visual_shape'] = True
    shape = _method(source, 'getShape')
    if shape is not None:
        row['shape'] = _shape(shape)
        if '.move(offset.x, offset.y, offset.z)' in shape:
            row['shape_offset'] = True
    if _method(source, 'rotate') is not None:
        row['rotate'] = True
    for key, name in (('flammability', 'getFlammability'), ('fire_spread', 'getFireSpreadSpeed')):
        body = _method(source, name)
        if body is not None:
            row[key] = int(re.search(r'return (\d+);', body).group(1))
    push = _method(source, 'getPistonPushReaction')
    if push is not None:
        row['push_reaction'] = re.search(r'PushReaction\.(\w+)', push).group(1)
    offset = _method(source, 'getOffsetType')
    if offset is not None:
        row['offset'] = re.search(r'OffsetType\.(\w+)', offset).group(1)
    enchant = _method(source, 'getEnchantPowerBonus')
    if enchant is not None:
        row['enchant_power'] = _number(re.search(r'return ([\d.]+f?);', enchant).group(1))
    harvest = _method(source, 'canHarvestBlock')
    if harvest is not None:
        found = re.search(r'instanceof (\w+) tieredItem\)\s*return tieredItem\.getTier\(\)\.getLevel\(\) >= (\d+);', harvest)
        row['harvest'] = {'tool': found.group(1), 'level': int(found.group(2))}
    if base in ('FlowerBlock', 'DoublePlantBlock'):
        _parse_plant(source, chain, base, row, blocks)
    drops = _method(source, 'getDrops')
    if drops is not None:
        found = re.search(r'return Collections\.singletonList\(new ItemStack\((.*)\)\);', drops)
        if 'HALF) != DoubleBlockHalf.LOWER' in drops:
            row['drop_lower_only'] = True
        expression = found.group(1)
        if re.search(r', state\.getValue\(TYPE\) == SlabType\.DOUBLE \? 2 : 1$', expression):
            expression = expression.rsplit(',', 1)[0]
            row['double_slab_drops_two'] = True
        item, count = _item_ref(expression, items, blocks)
        row['fallback_drop'] = {'item': item, 'count': count}
    lines = tooltip_lines(source)
    if lines:
        row['tooltip'] = lines
    hooks = sorted({name for name in re.findall(r'@Override\s+(?:public|protected)\s+[\w<>\[\], ?.]+\s+(\w+)\(', source)
                    if name in BEHAVIOR_HOOKS})
    if hooks:
        row['pending_hooks'] = hooks
    other = sorted({name for name in re.findall(r'@Override\s+(?:public|protected)\s+[\w<>\[\], ?.]+\s+(\w+)\(', source)}
                   - HANDLED - set(BEHAVIOR_HOOKS))
    if other:
        raise ValueError('覆写未识别：' + ','.join(other))
    return row


CANSURVIVE = re.compile(r'BlockPos blockpos = pos\.below\(\);\s*BlockState groundState = worldIn\.getBlockState\(blockpos\);\s*return this\.mayPlaceOn\(groundState, worldIn, blockpos\);')
CANSURVIVE_DOUBLE = re.compile(r'BlockPos blockpos = pos\.below\(\);\s*BlockState groundState = worldIn\.getBlockState\(blockpos\);\s*if \(blockstate\.getValue\(HALF\) == DoubleBlockHalf\.UPPER\)\s*return groundState\.is\(this\) && groundState\.getValue\(HALF\) == DoubleBlockHalf\.LOWER;\s*else\s*return this\.mayPlaceOn\(groundState, worldIn, blockpos\);')


def _parse_plant(source: str, chain: str, base: str, row: dict, blocks: dict[str, str]) -> None:
    """花与双格植物：效果（炖菜）、可种植的地面、默认偏移；canSurvive 只接受生成器的两种标准写法。"""
    if base == 'FlowerBlock':
        effect = re.match(r'(?:MobEffects\.(\w+)), \d+, ', chain)
        row['effect'] = 'minecraft:' + EFFECTS[effect.group(1)]
        row['effect_seconds'] = int(re.search(r'return (\d+);', _method(source, 'getEffectDuration')).group(1)) / 20
    plant = _method(source, 'getPlantType')
    if plant is not None and 'PlantType.PLAINS' not in plant:
        # 1.18.2 的 PlantType 只供地面的 canSustainPlant 查询；放置由 mayPlaceOn 决定，这里记录供种植类系统使用。
        row['plant_type'] = re.search(r'PlantType\.(\w+)', plant).group(1)
    may = _method(source, 'mayPlaceOn')
    if may is not None:
        grounds = []
        for ref in re.findall(r'groundState\.is\(([^)]*\)?)\)', may.split('return', 1)[1]):
            if ref.startswith('Blocks.'):
                grounds.append('minecraft:' + ref.split('.', 1)[1].lower())
            else:
                found = re.fullmatch(r'\w+ModBlocks\.(\w+)\.get\(\)', ref)
                grounds.append(blocks[found.group(1)])
        if not grounds or re.sub(r'groundState\.is\([^)]*\)?\)|\s|\|\||return|;', '', may):
            raise ValueError('可种植地面未识别：' + may.strip()[:120])
        row['ground'] = grounds
    survive = _method(source, 'canSurvive')
    if survive is not None:
        expected = CANSURVIVE_DOUBLE if base == 'DoublePlantBlock' else CANSURVIVE
        if not expected.search(survive):
            raise ValueError('canSurvive 未识别：' + survive.strip()[:120])
    row.setdefault('offset', 'XZ')
