"""递归核对来源原版生成配置、放置、标签及已人工核对的算法；不写正式资源。"""
from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from convert_content import ROOT, SOURCES

MOD = 'yvanchuxiuzhen'
SNAPSHOT = ROOT / 'tools/porting/data/legacy_vanilla_biome_snapshot.json'
# 这些结论来自方法体、随机调用、循环边界、更新标志与下游辅助方法的实际核对。
# 配置图或标签有差异时，即使算法已核对，也不会把该根引用判为可复用。
ALGORITHMS = {
    'minecraft:ore': ('OreFeature', '椭球链采样、包络剔除、遍历顺序、随机调用、空气暴露概率和section写入一致；try-with-resources与来源展开的close/suppressed异常路径等价；Feature.checkNeighbors/isAdjacentToAir一致。'),
    'minecraft:random_patch': ('RandomPatchFeature', '每次按x/y/z各两次nextInt相减，次数与成功计数、子地物调用顺序一致。'),
    'minecraft:flower': ('RandomPatchFeature', 'FLOWER实际注册RandomPatchFeature；每次六次偏移随机与子地物顺序一致。'),
    'minecraft:simple_block': ('SimpleBlockFeature', 'place方法除Override和空白外一致；先canSurvive，双层植物检查上方空气并以flag2放置。'),
    'minecraft:block_column': ('BlockColumnFeature', '各层高度采样、向前检查、truncate首尾次序、逐层状态采样和flag2一致。'),
    'minecraft:random_selector': ('RandomSelectorFeature', '逐项nextFloat<chance，首个命中返回，其余调用defaultFeature，顺序一致。'),
    'minecraft:simple_random_selector': ('SimpleRandomSelectorFeature', '一次nextInt(size)选择，随后以原上下文放置所选地物，顺序一致。'),
    'minecraft:random_boolean_selector': ('RandomBooleanSelectorFeature', '一次nextBoolean选择true/false地物；目标仅删除泛型多余cast，执行顺序一致。'),
    'minecraft:vines': ('VinesFeature', 'place方法除Override、空白和独立循环增量前后缀外一致。'),
    'minecraft:freeze_top_layer': ('SnowAndFreezeFeature', '16×16遍历、MOTION_BLOCKING高度、Biome.shouldFreeze/shouldSnow、冰雪及snowy标志写入顺序和flag2一致。'),
    'minecraft:underwater_magma': ('UnderwaterMagmaFeature', 'Column.scan水底查找、整型闭区间体积、每格概率先于落点检查、邻四向与下方检查及flag2一致；AABB和BoundingBox输入端点都是同一整型闭区间。'),
    'minecraft:vegetation_patch': ('VegetationPatchFeature', 'XZ半径采样顺序、边缘概率、垂直扫描边界、深度/额外底层随机、地表支撑检查、覆盖写入及植被抽样顺序一致；来源与目标实现均以同一已解析replaceable HolderSet判定。'),
    'minecraft:waterlogged_vegetation_patch': ('WaterloggedVegetationPatchFeature', '基础地表扫描继承VegetationPatchFeature；暴露方向顺序、含水地块替换、植被放置和WATERLOGGED回写条件与顺序一致。'),
    'minecraft:iceberg': ('IcebergFeature', '椭圆/圆形主体、切空区随机调用、半径曲线、每格放置顺序与平滑遍历一致；Material.AIR在来源原版三类空气方块上等价于目标isAir。'),
    'minecraft:pointed_dripstone': ('PointedDripstoneFeature', '旧版Random与目标RandomSource调用次序、垂直尖端选择、四向扩散和深度随机一致；DripstoneUtils高度、支撑与尖刺生成规则一致。'),
    'minecraft:dripstone_cluster': ('DripstoneClusterFeature', '配置采样、列扫描、湿润水池、上下尖刺概率、高度采样、邻水条件和方块写入顺序一致；来源与目标可替换标签展开后均为相同六种基岩方块。'),
    'minecraft:fossil': ('FossilFeature', '旋转与模板序号随机调用、模板尺寸与海底高度扫描顺序、深度偏移随机、空角判定、骨架后覆盖处理器的调用顺序一致；StructureTemplate对选中调色板、处理器逐块执行、边界过滤、结构写入与更新顺序一致。来源与目标八组骨骼/煤覆盖NBT除DataVersion外字段相同，处理器链均为0.9/0.1腐化、FEATURES_CANNOT_REPLACE保护与煤矿替换深层钻石矿。'),
    'minecraft:lake': ('LakeFeature', '洞形椭球、边界判定遍历、配置provider取样、洞内流体/空气写入、刻更新与post-processing、屏障概率随机及冻结检查顺序一致；仅Material谓词由来源材料快照标签保留。'),
}
CONFIG_LINKS = {
    'minecraft:random_patch': {'feature': 'placed_feature'},
    'minecraft:flower': {'feature': 'placed_feature'},
    'minecraft:random_selector': {'default': 'placed_feature'},
    'minecraft:random_boolean_selector': {'feature_true': 'placed_feature', 'feature_false': 'placed_feature'},
    'minecraft:vegetation_patch': {'vegetation_feature': 'placed_feature'},
    'minecraft:waterlogged_vegetation_patch': {'vegetation_feature': 'placed_feature'},
    'minecraft:root_system': {'feature': 'placed_feature'},
}
TAG_FIELDS = {'tag', 'replaceable', 'root_replaceable', 'can_grow_through', 'cannot_replace', 'invalid_blocks'}
PROVIDER_VALUE_TYPES = {'minecraft:uniform', 'minecraft:biased_to_bottom', 'minecraft:clamped_normal', 'minecraft:trapezoid'}


def renamed(value: str) -> str:
    # 1.20.3将单格原版草的注册ID由grass改为short_grass；此处仅规范化比较值。
    return 'minecraft:short_grass' if value == 'minecraft:grass' else value


class Catalog:
    def __init__(self, legacy: bool, source_root: Path, dump: Path):
        self.legacy = legacy
        self.dump = dump
        self.nodes = {}
        self.types = set()
        self.tags = {}
        self.snapshot = json.loads(dump.read_text(encoding='utf-8')) if legacy and dump.is_file() else None
        if self.snapshot is not None:
            if self.snapshot['minecraft_version'] != '1.18.2':
                raise ValueError('来源注册快照版本不匹配')
            self.archive = None
        elif legacy:
            jars = list((source_root / 'build/fg_cache').glob('mcp/*/joined/*/downloadClient/client.jar'))
            if len(jars) != 1:
                raise ValueError('来源原版资源Jar必须唯一')
            self.archive = zipfile.ZipFile(jars[0])
        else:
            self.archive = zipfile.ZipFile(ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar')

    def close(self) -> None:
        if self.archive is not None:
            self.archive.close()

    def raw_tag(self, identifier: str) -> dict:
        if self.snapshot is not None:
            return self.snapshot['block_tags'][identifier]
        namespace, name = identifier.split(':', 1)
        folder = 'blocks' if self.legacy else 'block'
        return json.loads(self.archive.read(f'data/{namespace}/tags/{folder}/{name}.json'))

    def tag(self, identifier: str) -> list[str]:
        identifier = identifier.removeprefix('#')
        if identifier in self.tags:
            return self.tags[identifier]
        value = self.raw_tag(identifier)
        result = []
        for entry in value['values']:
            if not isinstance(entry, str):
                raise ValueError(f'标签含待核条件条目：{identifier}:{entry}')
            result.extend(self.tag(entry) if entry.startswith('#') else [renamed(entry)])
        self.tags[identifier] = sorted(set(result))
        return self.tags[identifier]

    def read(self, kind: str, identifier: str) -> dict:
        namespace, name = identifier.split(':', 1)
        if self.snapshot is not None:
            return self.snapshot['registries'][kind][identifier]
        if self.legacy:
            return json.loads((self.dump / namespace / 'worldgen' / kind / (name + '.json')).read_text(encoding='utf-8'))
        return json.loads(self.archive.read(f'data/{namespace}/worldgen/{kind}/{name}.json'))

    def holder(self, kind: str, value) -> dict:
        if isinstance(value, str):
            if value.startswith('#'):
                raise ValueError(f'生成注册表HolderSet标签尚未核对：{kind}:{value}')
            key = kind + '/' + value
            if key in self.nodes:
                return self.nodes[key]
            data = self.read(kind, value)
            result = self.expand(kind, data)
            self.nodes[key] = result
            return result
        if not isinstance(value, dict):
            raise ValueError(f'无法识别生成Holder：{kind}:{value}')
        return self.expand(kind, value)

    def generic(self, value, key: str = ''):
        if isinstance(value, dict):
            result = {name: self.generic(child, name) for name, child in value.items()}
            # 1.18.2的value载荷由Int/FloatProvider的dispatch Codec包裹，目标改为MapCodec展平。
            # 仅处理本批逐个核对过的四种提供器，不删除任何配置字段。
            if value.get('type') in PROVIDER_VALUE_TYPES and isinstance(value.get('value'), dict):
                payload = result.pop('value')
                if payload.keys() & result.keys():
                    raise ValueError(f'提供器载荷字段冲突：{value}')
                result.update(payload)
            return result
        if isinstance(value, list):
            return [self.generic(child, key) for child in value]
        if isinstance(value, str):
            if key in TAG_FIELDS and (value.startswith('minecraft:') or value.startswith('#minecraft:')):
                return {'$block_tag_values': self.tag(value)}
            return renamed(value)
        return value

    def expand(self, kind: str, data: dict) -> dict:
        result = self.generic(data)
        if kind == 'placed_feature':
            result['feature'] = {'$configured_feature': self.holder('configured_feature', data['feature'])}
        elif kind == 'configured_feature':
            feature_type = data['type']
            self.types.add(feature_type)
            config = data['config']
            for key, registry in CONFIG_LINKS.get(feature_type, {}).items():
                result['config'][key] = {'$' + registry: self.holder(registry, config[key])}
            if feature_type == 'minecraft:random_selector':
                result['config']['features'] = [{**self.generic(entry),
                    'feature': {'$placed_feature': self.holder('placed_feature', entry['feature'])}}
                    for entry in config['features']]
            elif feature_type == 'minecraft:simple_random_selector':
                if not isinstance(config['features'], list):
                    raise ValueError('simple_random_selector HolderSet必须是直接列表')
                result['config']['features'] = [{'$placed_feature': self.holder('placed_feature', entry)} for entry in config['features']]
        return result


def differences(old, new, pointer: str = '') -> list[dict]:
    if isinstance(old, dict) and isinstance(new, dict):
        result = []
        for key in sorted(old.keys() | new.keys()):
            if key not in old or key not in new:
                result.append({'pointer': pointer + '/' + key, 'source': old.get(key), 'target': new.get(key), 'missing_key': True})
            else:
                result.extend(differences(old[key], new[key], pointer + '/' + key))
        return result
    if isinstance(old, list) and isinstance(new, list):
        if len(old) != len(new):
            return [{'pointer': pointer, 'source': old, 'target': new, 'length_differs': True}]
        return [entry for index, (left, right) in enumerate(zip(old, new)) for entry in differences(left, right, pointer + '/' + str(index))]
    return [] if old == new else [{'pointer': pointer, 'source': old, 'target': new}]


def audit(plan: dict, dump: Path) -> dict:
    source_root = Path(plan['source_root'])
    old = Catalog(True, source_root, dump)
    new = Catalog(False, source_root, dump)
    records = []
    try:
        for reference in plan['vanilla_references']:
            old.types.clear()
            new.types.clear()
            # 每个根独立展开，避免缓存导致其算法类型被前一个根吸收。
            old.nodes.clear()
            new.nodes.clear()
            kind, identifier = reference['registry'], reference['id']
            left = old.holder(kind, identifier)
            right = new.holder(kind, identifier)
            changed = differences(left, right)
            unreviewed = sorted((old.types | new.types) - ALGORITHMS.keys())
            # configured_carver拥有独立算法，不会因其配置数值相等而自动通过。
            if kind == 'configured_carver':
                unreviewed.append('carver_algorithm_and_source_replaceable_set')
            records.append({'id': identifier, 'registry': kind, 'configuration_graph_equal': not changed,
                'differences': changed, 'feature_types': sorted(old.types | new.types),
                'unreviewed_algorithms': unreviewed,
                'status': 'configuration_and_reviewed_algorithm_equal' if not changed and not unreviewed else 'pending',
                'source_graph_sha256': hashlib.sha256(json.dumps(left, sort_keys=True).encode()).hexdigest(),
                'target_graph_sha256': hashlib.sha256(json.dumps(right, sort_keys=True).encode()).hexdigest(),
                'source_graph': left, 'target_graph': right})
        return {'records': records, 'algorithm_reviews': ALGORITHMS, 'source_tags': old.tags, 'target_tags': new.tags,
            'validation': {'roots': len(records), 'configuration_equal': sum(row['configuration_graph_equal'] for row in records),
                'reusable': sum(row['status'] == 'configuration_and_reviewed_algorithm_equal' for row in records)},
            'limitations': ['配置闭包会展开所有已知嵌套Holder与方块标签，保留列表顺序；不把引用存在或配置相等单独视为对齐。',
                '未列入ALGORITHMS的算法仍为待核，配置差异不会被默认字段兜底或静默删除。',
                '此工具只生成审查证据，不生成资源，不启动游戏。']}
    finally:
        old.close()
        new.close()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan', type=Path, default=ROOT / 'tmp/source-biomes-plan.json')
    parser.add_argument('--dump', type=Path, default=SNAPSHOT)
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/vanilla-biome-graph-audit.json')
    args = parser.parse_args()
    report = audit(json.loads(args.plan.read_text(encoding='utf-8')), args.dump)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report['validation'], ensure_ascii=False))


if __name__ == '__main__':
    main()
