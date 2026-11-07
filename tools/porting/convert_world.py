"""转换已核对的世界数据；当前只接入技能所需的虚空维度，其余世界内容随所属批次迁移。

维度与维度类型读取来源 JSON，群系读取实际注册的 Java 定义。
1.18.2 的 slide 按 NoiseRouterData.applySlide/NoiseSlider 展开为 1.21.1 的高度梯度与加乘节点。
"""
from __future__ import annotations

import json
import re

from build_ledger import NAMESPACE, ROOT
from convert_content import SOURCES

DATA = ROOT / 'src/main/resources/data'


def slide(input_density: dict | str, noise: dict) -> dict:
    """上下滑移在噪声格点计算；垂直格高为 size_vertical × 4，先顶部再底部。"""
    def lerp(from_y: int, to_y: int, from_value: int, to_value: int, target: float, density) -> dict:
        return {'type': 'minecraft:add', 'argument1': target, 'argument2': {
            'type': 'minecraft:mul',
            'argument1': {'type': 'minecraft:y_clamped_gradient', 'from_y': from_y, 'to_y': to_y,
                          'from_value': from_value, 'to_value': to_value},
            'argument2': {'type': 'minecraft:add', 'argument1': -target, 'argument2': density},
        }}

    cell = noise['size_vertical'] * 4
    top, bottom = noise['top_slide'], noise['bottom_slide']
    top_end = noise['min_y'] + noise['height'] - top['offset'] * cell
    density = lerp(top_end - top['size'] * cell, top_end, 1, 0, top['target'], input_density)
    bottom_start = noise['min_y'] + bottom['offset'] * cell
    return lerp(bottom_start, bottom_start + bottom['size'] * cell, 0, 1, bottom['target'], density)


def generate(converter) -> None:
    mod = 'yvanchuxiuzhen'
    root = SOURCES[mod][0]
    source_data = root / 'src/main/resources/data' / mod
    source_java = root / 'src/main/java/net/mcreator' / mod / 'world'
    dimension_file = source_data / 'dimension/xvkong.json'
    dimension = json.loads(dimension_file.read_text(encoding='utf-8'))
    generator = dimension['generator']
    # 目标生成器不再接受独立 seed；该维度只有一个群系，地形与流体均为空气。
    del generator['seed']
    settings = generator['settings']
    noise = settings['noise']
    blend = settings['noise_router']['final_density']['argument']['argument2']['argument']
    blend['argument'] = slide(blend['argument']['argument'], noise)
    settings['noise'] = {key: noise[key] for key in ('min_y', 'height', 'size_horizontal', 'size_vertical')}
    # 旧开关已不在目标 Codec 中；来源没有洞穴、矿脉、地物或符合该群系的结构。
    for key in ('name', 'noodle_caves_enabled', 'noise_caves_enabled', 'deepslate_enabled', 'structures'):
        del settings[key]
    settings['spawn_target'] = []
    converter.write(DATA / NAMESPACE / 'dimension/xvkong.json', dimension, [(mod, dimension_file)])

    type_file = source_data / 'dimension_type/xvkong.json'
    dimension_type = json.loads(type_file.read_text(encoding='utf-8'))
    # 目标新增必填字段，沿用 1.18.2 Monster 的方块光 0、总亮度 nextInt(8) 条件。
    dimension_type['monster_spawn_block_light_limit'] = 0
    dimension_type['monster_spawn_light_level'] = {'type': 'minecraft:uniform', 'min_inclusive': 0, 'max_inclusive': 7}
    converter.write(DATA / NAMESPACE / 'dimension_type/xvkong.json', dimension_type, [(mod, type_file)])

    biome_file = source_java / 'biome/XvkongqvnxiBiome.java'
    biome_source = biome_file.read_text(encoding='utf-8')
    colors = {'fogColor': 'fog_color', 'waterColor': 'water_color', 'waterFogColor': 'water_fog_color',
              'skyColor': 'sky_color', 'foliageColorOverride': 'foliage_color', 'grassColorOverride': 'grass_color'}
    biome = {
        'has_precipitation': True,
        'temperature': float(re.search(r'\.temperature\(([-\d.]+)f\)', biome_source)[1]),
        'downfall': float(re.search(r'\.downfall\(([-\d.]+)f\)', biome_source)[1]),
        'effects': {target: int(re.search(rf'\.{method}\((-?\d+)\)', biome_source)[1]) for method, target in colors.items()},
        'carvers': {}, 'features': [], 'spawners': {}, 'spawn_costs': {},
    }
    converter.write(DATA / NAMESPACE / 'worldgen/biome/xvkongqvnxi.json', biome, [(mod, biome_file)])

    # 来源 Fixers 向全局 CAVE/CANYON 加入 AIR；目标的三个原版配置共用此标签，NETHER_CAVE 使用另一标签。
    converter.write(DATA / 'minecraft/tags/block/overworld_carver_replaceables.json',
                    {'replace': False, 'values': ['minecraft:air']}, [(mod, source_java / 'dimension/XvkongDimension.java')])
