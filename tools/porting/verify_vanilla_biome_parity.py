"""逐项验证可复用原版群系引用及独立实现引用的注册图和算法源码。"""
from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from audit_vanilla_biome_graph import ALGORITHMS, Catalog, SNAPSHOT, audit
from convert_content import ROOT
from convert_legacy_biome_vanilla import PLACED, plan as legacy_plan
from legacy_fossils import plan as fossil_plan, verify_templates
from legacy_lakes import plan as lake_plan
from legacy_geodes import plan as geode_plan
from legacy_trees import ROOTS as TREE_ROOTS, integration as tree_integration, plan as tree_plan
from legacy_carvers import plan as carver_plan
from legacy_block_tags import ids as block_tag_ids

ALGORITHM_HASHES = {
    'minecraft:fossil': ('8fde9cfa118e83d255b731e7ccd87020d2d88e8b0e9f067b3420d1b31913df7e', 'd3a06281975de41cd3d559ce05fa5c5da954c58d86adb0c80b7925f1d4cc0195'),
    'minecraft:lake': ('cb7d3b1edeb969eeb6a9b4410fd2846f1e77be7c9c7129fa195dd2a0b310287b', '9b83559cffa9903bf138d7fc43772a2ec841fc4be93081e8b64a5da10ee735a4'),
    'minecraft:flower': ('2ff975a6eff2fb365915c238fed72e40cab7d42cc34a7095336e22c79a3b4bdd', '9a5f443710866964e59a4abe7597c6b68f5b574bd837e9c088348573e67538c2'),
    'minecraft:ore': ('eae38fb85ba1456d79b3f3a614a7094052eb5f56ec3dbf1381f1da5d233faf4f', 'be84c16b69a170bbe5b517c8d7ead26cc113d39300b82b2a5e122feeb76235af'),
    'minecraft:random_patch': ('2ff975a6eff2fb365915c238fed72e40cab7d42cc34a7095336e22c79a3b4bdd', '9a5f443710866964e59a4abe7597c6b68f5b574bd837e9c088348573e67538c2'),
    'minecraft:simple_block': ('e250368f96f90ee966440a429faf540514763fce4a7d56588e9c2a5489968028', '3c42ff92cf10f961268cffbe6819b6dd453c6cfd2b2493afcd238c5bc99f81ba'),
    'minecraft:block_column': ('ec3982ef17d104c7165b21aa0d56170400639b126a561105720627a5c3a26c4c', 'daa64e75bd94e02b6625abdc03822c742cef5c4d43d8dcf1a0429cd12776eb0b'),
    'minecraft:random_selector': ('2efdcd6f96194ae999d9807ea40a22a2ac7a3afd62acd96be0662a7ec78c69e5', '2426f9b8592c98d1b0a0aaff441fc3a7178b0af640b8d98420f534faa8b768b5'),
    'minecraft:simple_random_selector': ('5c8cab17597a52c0e971960cbbb879630375928d8bd14288f5220e078053e144', '8559cd959afaae95b66bd76d9a6be93cb76d73f9e4d8f722650defe9360aa5a7'),
    'minecraft:random_boolean_selector': ('c62f646271f9906995782ccaa77fc0dd330490f72374b3555df191c6666cfc22', '83408a0250229609bc17de48d1122144e01994594e95491dbdc40df42268db6d'),
    'minecraft:vines': ('478364508be15f89f2854e76df5fdfbafe3ea369446f157c6759f7c5fd221693', 'e458f3f5db96c7614ba695ae32555b205a9b28a1c2674a3a5e97db5bf76b269e'),
    'minecraft:freeze_top_layer': ('15a090e2005e9ec6a725d2713df2b00ac0389ef8bd399b984e556f872f210844', 'cf46a59fa517e1ff180f6a1e3d81e202032e76a090c2375bbf82a85293c67c6e'),
    'minecraft:underwater_magma': ('6261bd5e2e684e664ad73766357142407ebf69fd54d642ba3c6b8565460c5a1c', '3134cf3f2b6d625a35288badf6d96b4dd536b1d28300e1ed65664f94abc5a76d'),
    'minecraft:pointed_dripstone': ('df6f0fc6e5d3f0c9f56d319e0d218d8ceca5129fa6b9950a18e219a0508daf43', '332546bffc645db878a2dfa4f44c46e85bc4f1f4c28351371f5a984e59c021bf'),
    'minecraft:dripstone_cluster': ('f69900bc2344215f741ab5489b2e1923a75ee4105ea4cd56f0367cddb993574c', 'f6884d711bd4aec861b7cbd5fc16543c8edf72074863a0527dcbbac90df47578'),
    'minecraft:vegetation_patch': ('f06116273c6fed5740a91e10ab579d1eef226c2ed9a2757cb69b49d4abcd378c', '696e78b6f144bd541921aa1508e41170d75cdaae1793d57505ffcf498b2b8c0f'),
    'minecraft:waterlogged_vegetation_patch': ('603562b376eacc58bd7144fed4f2ee0666017b679b16c412375ea021d0884f29', '1d048b8ceaedd40bee86fca9a6a3a322a1bb6a385ae5d5448e7fc0b7604b1328'),
    'minecraft:iceberg': ('b02eeeaa5b313ec742dec0c902a4749cc3e9602e9716e5b5e42f52abacae00c9', 'e41b1e028dccc784960dc1a0de02082e271b2c85917047277bdc3e5fad0f394a'),
}
CARVER_SOURCE_HASHES = {
    'net/minecraft/world/level/levelgen/carver/CaveWorldCarver.java': (
        '0c63efd03bba444c82f5d64362c42fa15cc460b81f2f4015152788d2cfb36f3d',
        'a3407594fad3bcafc9aee425f5265921e5cdfc645b5709c2e24f116312bcd68d'),
    'net/minecraft/world/level/levelgen/carver/CanyonWorldCarver.java': (
        '49caaea21273b0b1f921e09ed43ba42df0ec3e581154a6481391be382631a458',
        '40aaf4138a1c2ad5728794dd3853b6e6ea212960abccb98e52209fb30f63e6b5'),
    'net/minecraft/world/level/levelgen/carver/WorldCarver.java': (
        '19fffd1280f135ad56a1049356b2ebf94d474d47b7c4db7b69c32256e5e0dff4',
        '987f3245bcb25bc9060c32ff8f28977f078d2946ff67f3e2cf22e21e706c64b5'),
}

REUSABLE = {
    'minecraft:brown_mushroom_taiga', 'minecraft:cave_vines', 'minecraft:classic_vines_cave_feature',
    'minecraft:fossil_lower', 'minecraft:fossil_upper',
    'minecraft:iceberg_blue', 'minecraft:iceberg_packed',
    'minecraft:flower_meadow', 'minecraft:freeze_top_layer', 'minecraft:ore_coal_lower',
    'minecraft:ore_coal_upper', 'minecraft:ore_copper', 'minecraft:ore_diamond',
    'minecraft:ore_diamond_buried', 'minecraft:ore_diamond_large', 'minecraft:ore_gold',
    'minecraft:ore_gold_lower', 'minecraft:ore_iron_middle', 'minecraft:ore_iron_small',
    'minecraft:ore_iron_upper', 'minecraft:ore_lapis', 'minecraft:ore_lapis_buried',
    'minecraft:ore_redstone', 'minecraft:ore_redstone_lower', 'minecraft:patch_dead_bush_2',
    'minecraft:patch_grass_jungle', 'minecraft:patch_grass_plain', 'minecraft:patch_grass_taiga_2',
    'minecraft:patch_large_fern', 'minecraft:patch_pumpkin', 'minecraft:patch_sugar_cane',
    'minecraft:patch_tall_grass', 'minecraft:red_mushroom_taiga', 'minecraft:seagrass_simple',
    'minecraft:spore_blossom', 'minecraft:underwater_magma', 'minecraft:pointed_dripstone',
    'minecraft:dripstone_cluster',
}
CUSTOM_ALGORITHMS = {
    'minecraft:amethyst_geode': {
        'source_entry': 'net/minecraft/world/level/levelgen/feature/GeodeFeature.java',
        'source_sha256': '07a9332dfd4873d950a743588fefad6f89ad6ffc000d1db2cdfc4e02bfb37577',
        'target_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceGeodeFeature.java',
        'target_sha256': '715c061b2f16b7d25c1e5ab0adfbeaaa5c62aabf00e4ff26de331b8d1a7a3a37',
        'type': 'legacy_geode',
        'review': '保留来源两处近似逆平方根、分布点和裂缝密度、随机调用顺序及晶簇遍历；精确来源标签独立封闭。NormalNoise双Perlin、LegacyRandomSource位置分叉和ImprovedNoise采样链已逐段静态核对。',
    },
    'minecraft:bamboo': {
        'source_entry': 'net/minecraft/world/level/levelgen/feature/BambooFeature.java',
        'source_sha256': 'af102b075ff6d4f0f128cbd2446e12f7bacba875b41af6cf6160801a23183661',
        'target_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceBambooFeature.java',
        'target_sha256': '6696094a9646d64f819a4187f9b03ab8a1754e67ad0fc9bce6cf89492d7ff626',
        'type': 'legacy_bamboo',
        'review': '保留高度及概率随机顺序、半径和podzol圆盘遍历、逐格生长与最终大叶位置；生存与podzol落点分别使用旧版BAMBOO_PLANTABLE_ON及DIRT精确成员。',
    },
    'minecraft:desert_well': {
        'source_entry': 'net/minecraft/world/level/levelgen/feature/DesertWellFeature.java',
        'source_sha256': '72025021b4edb9d91e231b781bdcd4f8ab097a842f289dc1365174bc84883bf0',
        'target_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceDesertWellFeature.java',
        'target_sha256': 'f3ccec9552e734fac5a28204e4a8b14b93fca83c38cb64a3f117bdb2196bd1de',
        'type': 'legacy_desert_well',
        'review': '保留来源上方定位、纯沙判定、基底条件和结构放置顺序；特意保留来源两层地基与无额外随机调用的规则。',
    },
    'minecraft:forest_rock': {
        'source_entry': 'net/minecraft/world/level/levelgen/feature/BlockBlobFeature.java',
        'source_sha256': '9331aa790e526fcab06f16fd5acdb0cc4a0c4f2dceb89e15e127b86d8b3f56dd',
        'target_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceBlockBlobFeature.java',
        'target_sha256': 'a7830bf137c2be155958da8860d124e2c7cc72da4a82604ea75fa9a810031ef2',
        'type': 'legacy_forest_rock',
        'review': '保留三团循环、每团三次nextInt(2)、团内遍历与球形判定、4写入标志及每团后位置随机偏移；泥土与石头谓词由来源精确标签实现。',
    },
    'minecraft:large_dripstone': {
        'source_entry': 'net/minecraft/world/level/levelgen/feature/LargeDripstoneFeature.java',
        'source_sha256': '2f8e1581e90bd00b76b2f98bfa976bfa9a9280f9b3b87f795567460d85849d56',
        'target_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceLargeDripstoneFeature.java',
        'target_sha256': '7685c75c9d46d53dd37ca1ae508a624eb39bd4548e1708c105f458b2ef541036',
        'type': 'legacy_large_dripstone',
        'review': '复用目标大型滴水石完整生成算法；通过仅包裹本自定义地物同步委托调用的ThreadLocal标记，使nested WindOffsetter.offset的redirect恢复来源坐标相加后取整语义。',
        'target_algorithm_sha256': '35241b289a01746b5bb606300551293dec8a15b393348dedae750e81e84158ce',
        'mixin_path': ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/terrain/mixin/LargeDripstoneOffsetMixin.java',
        'mixin_sha256': '05e06a948d0d543e70c70ab065c44795b918c7bca83aae681b6841fb525c202e',
    },
}
LUSH_OVERRIDES = {
    'minecraft:lush_caves_vegetation': ('legacy_vanilla/lush_caves_vegetation.json', 'legacy_moss_patch'),
    'minecraft:lush_caves_ceiling_vegetation': ('legacy_vanilla/lush_caves_ceiling_vegetation.json', 'legacy_moss_patch_ceiling'),
    'minecraft:lush_caves_clay': ('legacy_vanilla/lush_caves_clay.json', 'legacy_lush_caves_clay'),
}
PENDING_ACTIONS = {}


def sha(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def algorithm_hashes(source: zipfile.ZipFile, target: zipfile.ZipFile, types: set[str]) -> dict:
    records = {}
    for feature_type in sorted(types):
        source_hash, target_hash = ALGORITHM_HASHES[feature_type]
        entry = 'net/minecraft/world/level/levelgen/feature/' + ALGORITHMS[feature_type][0] + '.java'
        actual_source = sha(source.read(entry))
        actual_target = sha(target.read(entry))
        records[feature_type] = {'entry': entry, 'source_sha256': actual_source,
            'target_sha256': actual_target, 'pinned_hashes_match': (actual_source, actual_target) == (source_hash, target_hash),
            'review': ALGORITHMS[feature_type][1]}
    return records


def geode_integration(source: zipfile.ZipFile, target: zipfile.ZipFile, source_root: Path) -> dict:
    fingerprints = json.loads((ROOT / 'tools/porting/data/legacy_geode_source_fingerprints.json').read_text(encoding='utf-8'))
    expected_names = {'GeodeFeature', 'NormalNoise', 'PerlinNoise', 'ImprovedNoise', 'LegacyRandomSource',
        'WorldgenRandom', 'BitRandomSource', 'Mth', 'SimplexNoise', 'Direction', 'BlockPos', 'Vec3i',
        'Feature', 'BuddingAmethystBlock'}
    if len(fingerprints) != len(expected_names) or {row['name'] for row in fingerprints} != expected_names:
        raise ValueError('晶洞算法及辅助链源码指纹范围发生变化')
    for row in fingerprints:
        if (sha(source.read(row['source_entry'])) != row['source_sha256']
                or sha(target.read(row['target_entry'])) != row['target_sha256']):
            raise ValueError(f'晶洞算法及辅助链源码与静态核对版本不符：{row["name"]}')
    outputs = {path: data for path, data, _ in geode_plan(source_root)}
    if len(outputs) != 4:
        raise ValueError('晶洞配置、放置及两个独立标签的输出范围发生变化')
    for path, expected in outputs.items():
        if not path.is_file() or json.loads(path.read_text(encoding='utf-8')) != expected:
            raise ValueError(f'晶洞目标资源缺失或与来源计划不精确相等：{path}')
    data_root = ROOT / 'src/main/resources/data/yvanchuxiuzhen'
    configured = outputs[data_root / 'worldgen/configured_feature/legacy_vanilla/amethyst_geode.json']
    placed = outputs[data_root / 'worldgen/placed_feature/legacy_vanilla/amethyst_geode.json']
    with zipfile.ZipFile(ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar') as resources:
        target_configured = json.loads(resources.read('data/minecraft/worldgen/configured_feature/amethyst_geode.json'))
    target_configured['type'] = 'yvanchuxiuzhen:legacy_geode'
    target_configured['config']['blocks']['cannot_replace'] = '#yvanchuxiuzhen:legacy_geode_cannot_replace'
    target_configured['config']['blocks']['invalid_blocks'] = '#yvanchuxiuzhen:legacy_geode_invalid_blocks'
    java_root = ROOT / 'src/main/java/net/yvanchuxiuzhen'
    registration = (java_root / 'system/world/LegacyBiomeFeatures.java').read_text(encoding='utf-8')
    setup = (java_root / 'system/world/WorldSetup.java').read_text(encoding='utf-8')
    mod = (java_root / 'YvanchuxiuzhenMod.java').read_text(encoding='utf-8')
    feature = (java_root / 'system/world/SourceGeodeFeature.java').read_text(encoding='utf-8')
    checks = {
        'four_outputs_match_locked_source_plan': True,
        'fourteen_source_and_target_fingerprints_match': True,
        'feature_registered': 'FEATURES.register("legacy_geode", SourceGeodeFeature::new);' in registration,
        'feature_holder_on_world_lifecycle': 'LegacyBiomeFeatures.register(modBus)' in setup,
        'world_setup_on_mod_lifecycle': 'WorldSetup.register(modBus)' in mod,
        'biome_placed_reference_redirected': PLACED.get('minecraft:amethyst_geode') == 'yvanchuxiuzhen:legacy_vanilla/amethyst_geode',
        'placed_points_to_custom_configuration': placed['feature'] == 'yvanchuxiuzhen:legacy_vanilla/amethyst_geode',
        'configured_matches_target_codec_after_type_and_tags': configured == target_configured,
        'source_inverse_square_root_constant': '6910469410427058090L - (bits >> 1)' in feature,
        'source_invalid_block_tag': 'legacy_geode_invalid_blocks' in feature and 'state.is(INVALID_BLOCKS)' in feature,
    }
    if not all(checks.values()):
        raise ValueError(f'晶洞注册、配置或算法闭环核对失败：{checks}')
    return {'integration_checks': checks, 'source_and_target_fingerprints': fingerprints,
        'helper_review': '静态核对NormalNoise两次Perlin初始化、octave_-4位置分叉、LegacyRandomSource LCG和位数、ImprovedNoise置换与插值；Direction及BlockPos顺序、safeSetBlock和晶簇生长谓词一致。未运行两版本噪声自动对照或游戏生成。'}


def verify(plan: dict, dump: Path, source_jar: Path, target_jar: Path) -> dict:
    report = audit(plan, dump)
    source = zipfile.ZipFile(source_jar)
    target = zipfile.ZipFile(target_jar)
    try:
        custom_plan = legacy_plan(Path(plan['source_root']))
        custom_plan.extend(lake_plan(Path(plan['source_root'])))
        carver_outputs = {path: data for path, data, _ in carver_plan(Path(plan['source_root']), source_jar)}
        planned = {path.relative_to(ROOT / 'src/main/resources/data/yvanchuxiuzhen/worldgen').as_posix(): data
            for path, data, _ in custom_plan if '/worldgen/' in path.as_posix()}
        planned_tags = {path.name: data for path, data, _ in custom_plan if path.parent.name == 'block'}
        snapshot = json.loads(SNAPSHOT.read_text(encoding='utf-8'))
        source_client_path = next((Path(plan['source_root']) / 'build/fg_cache').glob(
            'mcp/*/joined/*/downloadClient/client.jar'))
        with zipfile.ZipFile(source_client_path) as source_client, zipfile.ZipFile(
                ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar') as target_client:
            fossil_templates = verify_templates(source_client, target_client,
                snapshot['registries']['configured_feature']['minecraft:fossil_coal']['config'])
            for carver_id in ('cave', 'cave_extra_underground', 'canyon'):
                configured_carver = json.loads(target_client.read(
                    f'data/minecraft/worldgen/configured_carver/{carver_id}.json'))
                replaceable = configured_carver.get('config', {}).get('replaceable')
                if replaceable != '#minecraft:overworld_carver_replaceables':
                    raise ValueError(f'目标 {carver_id} 未使用 overworld_carver_replaceables：{replaceable}')
        fossil_outputs = {path: data for path, data, _ in fossil_plan(
            source_jar, source_client_path, snapshot)}
        lake_outputs = {path: data for path, data, _ in lake_plan(Path(plan['source_root']))}
        tree_outputs = {path: data for path, data, _ in tree_plan(Path(plan['source_root']))}
        report['unported_block_dependencies'] = {
            path.relative_to(ROOT).as_posix(): [value['id'] for value in data['values']
                if isinstance(value, dict) and value.get('required') is False]
            for path, data in {**carver_outputs, **lake_outputs, **tree_outputs}.items()
            if path.parent.name == 'block' and any(isinstance(value, dict) for value in data['values'])}
        carver_tag_path = ROOT / 'src/main/resources/data/minecraft/tags/block/overworld_carver_replaceables.json'
        expected_carver_tag = carver_outputs[carver_tag_path]
        if not expected_carver_tag['replace'] or block_tag_ids(expected_carver_tag['values']) != sorted(block_tag_ids(expected_carver_tag['values'])):
            raise ValueError('来源 carver 替换标签计划格式不符或成员顺序未规范')
        if not carver_tag_path.is_file() or json.loads(carver_tag_path.read_text(encoding='utf-8')) != expected_carver_tag:
            raise ValueError('目标 overworld_carver_replaceables 标签与锁定来源 60 项成员不精确相等')
        if len(expected_carver_tag['values']) != 60:
            raise ValueError(f'来源 carver 替换标签成员数变化：{len(expected_carver_tag["values"])}')
        source_carvers = {row['id']: row for row in report['records'] if row['registry'] == 'configured_carver'}
        expected_carver_ids = {'minecraft:cave', 'minecraft:cave_extra_underground', 'minecraft:canyon'}
        if not expected_carver_ids.issubset(source_carvers):
            raise ValueError('群系闭包缺少 cave/cave_extra_underground/canyon 配置引用')

        def without_replaceable(value):
            if isinstance(value, dict):
                return {key: without_replaceable(child) for key, child in value.items() if key != 'replaceable'}
            if isinstance(value, list):
                return [without_replaceable(child) for child in value]
            return value

        for carver_id in sorted(expected_carver_ids):
            record = source_carvers[carver_id]
            if without_replaceable(record['source_graph']) != without_replaceable(record['target_graph']):
                raise ValueError(f'来源 carver 配置参数与目标存在非替换标签差异：{carver_id}')
            source_type = record['source_graph']['type']
            algorithm_entry = 'net/minecraft/world/level/levelgen/carver/' + (
                'CaveWorldCarver.java' if source_type == 'minecraft:cave' else 'CanyonWorldCarver.java')
            carver_hashes = {}
            for entry in (algorithm_entry, 'net/minecraft/world/level/levelgen/carver/WorldCarver.java'):
                expected_source_hash, expected_target_hash = CARVER_SOURCE_HASHES[entry]
                actual_source_hash = sha(source.read(entry))
                actual_target_hash = sha(target.read(entry))
                if (actual_source_hash, actual_target_hash) != (expected_source_hash, expected_target_hash):
                    raise ValueError(f'carver 算法源码与静态审计指纹不符：{entry}')
                carver_hashes[entry] = {'source_sha256': actual_source_hash,
                    'target_sha256': actual_target_hash, 'pinned_hashes_match': True}
            record['replaceable_tag'] = 'minecraft:overworld_carver_replaceables'
            record['source_replaceable_members'] = expected_carver_tag['values']
            record['carver_algorithm_hashes'] = carver_hashes
            record['status'] = 'verified_source_replaceable_set_and_reviewed_vanilla_algorithm'
            record['unreviewed_algorithms'] = []
        if len(fossil_templates) != 16 or not all(row['equal_except_data_version'] for row in fossil_templates):
            raise ValueError('来源与目标的16份化石/煤覆盖结构NBT不等价')
        expected_fossil_tag = sorted(snapshot['block_tags']['minecraft:features_cannot_replace']['values'])
        fossil_tag_path = ROOT / 'src/main/resources/data/yvanchuxiuzhen/tags/block/legacy_worldgen_features_cannot_replace.json'
        planned_fossil_tag = next(data for path, data, _ in lake_plan(Path(plan['source_root']))
            if path == fossil_tag_path)
        if planned_fossil_tag['values'] != expected_fossil_tag or not fossil_tag_path.is_file() or json.loads(
                fossil_tag_path.read_text(encoding='utf-8')) != planned_fossil_tag:
            raise ValueError('化石结构保护标签未精确复制来源成员')
        for name in ('legacy_fossil_rot.json', 'legacy_fossil_coal.json', 'legacy_fossil_diamonds.json'):
            target_path = ROOT / 'src/main/resources/data/yvanchuxiuzhen/worldgen/processor_list' / name
            if not target_path.is_file():
                raise ValueError(f'缺少化石来源处理器数据：{name}')
            target_processor = json.loads(target_path.read_text(encoding='utf-8'))
            if target_processor != fossil_outputs[target_path]:
                raise ValueError(f'化石处理器顺序或参数与锁定来源不符：{name}')
        for target_path, expected in fossil_outputs.items():
            if target_path.parent.name == 'processor_list' or target_path.parent.name == 'block':
                continue
            if not target_path.is_file() or json.loads(target_path.read_text(encoding='utf-8')) != expected:
                raise ValueError(f'化石特征资源与锁定来源注册不符：{target_path}')
        lake_feature_path = ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/SourceLakeFeature.java'
        lake_registration = (ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/LegacyBiomeFeatures.java').read_text(encoding='utf-8')
        lake_registered = 'FEATURES.register("legacy_lake", SourceLakeFeature::new);' in lake_registration
        lake_source_hash = sha(lake_feature_path.read_bytes())
        if lake_source_hash != '0afd7b7ac76390de05765afd450a12924b44b6f7d646ca7f46dd5678b3225a8d' or not lake_registered:
            raise ValueError('来源熔岩湖Feature实现或注册与已核对版本不符')
        for target_path, expected in lake_outputs.items():
            if not target_path.is_file() or json.loads(target_path.read_text(encoding='utf-8')) != expected:
                raise ValueError(f'来源熔岩湖资源缺失或与锁定计划不符：{target_path}')
        required_custom = {'configured_feature/legacy_moss_patch.json',
            'configured_feature/legacy_moss_patch_ceiling.json', 'configured_feature/legacy_lush_caves_clay.json',
            'configured_feature/legacy_clay_with_dripleaves.json', 'configured_feature/legacy_clay_pool_with_dripleaves.json',
            'placed_feature/legacy_vanilla/lush_caves_vegetation.json',
            'placed_feature/legacy_vanilla/lush_caves_ceiling_vegetation.json',
            'placed_feature/legacy_vanilla/lush_caves_clay.json'}
        if not required_custom.issubset(planned):
            raise ValueError('苔藓与黏土的来源标签覆盖资源计划不完整')
        for source_name, target_name in (('minecraft:moss_replaceable', 'legacy_moss_replaceable.json'),
                ('minecraft:lush_ground_replaceable', 'legacy_lush_ground_replaceable.json')):
            if source_name not in report['source_tags'] or target_name not in planned_tags:
                raise ValueError(f'缺少来源标签闭包或旧规则覆盖：{source_name}')
            if planned_tags[target_name]['values'] != report['source_tags'][source_name]:
                raise ValueError(f'旧规则覆盖成员与来源标签展开结果不一致：{source_name}')
        tree_proof = tree_integration(source, target, Path(plan['source_root']))
        tree_reused_types = {'minecraft:random_patch', 'minecraft:random_selector'}
        tree_reused_hashes = algorithm_hashes(source, target, tree_reused_types)
        if not all(row['pinned_hashes_match'] for row in tree_reused_hashes.values()):
            raise ValueError('来源树闭包复用的随机选择器或随机草补丁源码指纹发生变化')
        tree_root_ids = {'minecraft:' + name for name in TREE_ROOTS}
        if {row['id'] for row in report['records']} & tree_root_ids != tree_root_ids:
            raise ValueError('四组来源树根未完整出现在55项群系依赖中')
        for record in report['records']:
            if record['registry'] == 'configured_carver':
                continue
            if record['id'] in tree_root_ids:
                name = record['id'].split(':', 1)[1]
                expected_types = ({'minecraft:root_system', 'minecraft:tree'} if name == 'rooted_azalea_tree'
                    else {'minecraft:random_patch', 'minecraft:random_selector', 'minecraft:simple_block', 'minecraft:tree'}
                    if name == 'bamboo_vegetation' else {'minecraft:random_selector', 'minecraft:tree'})
                if set(record['feature_types']) != expected_types:
                    raise ValueError(f'来源树根出现未核对的算法闭包：{record["id"]}')
                target_override = 'yvanchuxiuzhen:legacy_vanilla/' + name
                if PLACED.get(record['id']) != target_override:
                    raise ValueError(f'来源树根未接入群系放置重定向：{record["id"]}')
                record['status'] = 'verified_custom_source_tree_algorithm'
                record['target_override'] = target_override
                record['target_feature_type'] = ('yvanchuxiuzhen:legacy_root_system' if name == 'rooted_azalea_tree'
                                                else 'minecraft:random_selector')
                record['unreviewed_algorithms'] = []
                record['algorithm_hashes'] = tree_reused_hashes
                record['tree_helper_proof'] = tree_proof
                continue
            types = set(record['feature_types'])
            hashes = algorithm_hashes(source, target, types) if types and not record['unreviewed_algorithms'] else {}
            record['algorithm_hashes'] = hashes
            if record['id'] in ('minecraft:fossil_lower', 'minecraft:fossil_upper'):
                record['template_closure'] = fossil_templates
            if record['id'] in ('minecraft:lake_lava_surface', 'minecraft:lake_lava_underground'):
                if not record['configuration_graph_equal'] or record['unreviewed_algorithms'] or not all(
                        item['pinned_hashes_match'] for item in hashes.values()):
                    raise ValueError(f'来源湖泊配置或原版算法与已核对版本不符：{record["id"]}')
                record['status'] = 'verified_custom_source_material_algorithm'
                record['target_override'] = 'yvanchuxiuzhen:legacy_vanilla/' + record['id'].split(':', 1)[1]
                record['source_implementation_sha256'] = lake_source_hash
                continue
            if record['id'] in LUSH_OVERRIDES:
                expected_pointer = '/feature/$configured_feature/config/replaceable/$block_tag_values'
                if record['id'] == 'minecraft:lush_caves_clay':
                    expected_pointers = {d['pointer'] for d in record['differences']}
                    tags_only = all(pointer.endswith('/replaceable/$block_tag_values') for pointer in expected_pointers)
                else:
                    tags_only = all(d['pointer'] == expected_pointer for d in record['differences'])
                placed_path, configured_name = LUSH_OVERRIDES[record['id']]
                placement = planned['placed_feature/' + placed_path]
                expected_feature_id = 'yvanchuxiuzhen:' + configured_name
                feature_types_reviewed = not record['unreviewed_algorithms']
                hashes_match = all(item['pinned_hashes_match'] for item in hashes.values())
                if (tags_only and feature_types_reviewed and hashes_match and placement['feature'] == expected_feature_id
                        and all('yvanchuxiuzhen:legacy_' in feature for feature in [placement['feature']])):
                    record['status'] = 'verified_custom_configuration_and_reviewed_algorithm'
                    record['target_override'] = 'yvanchuxiuzhen:' + placed_path.removesuffix('.json')
                    record['algorithm_hashes'] = hashes
                    continue
            record['status'] = ('verified_reusable' if record['id'] in REUSABLE
                and record['configuration_graph_equal'] and not record['unreviewed_algorithms']
                and all(item['pinned_hashes_match'] for item in hashes.values()) else 'pending')
        custom_outputs = {(path.parent.parent.name, path.name): data for path, data, _ in custom_plan}
        expected_files = {'bamboo.json', 'desert_well.json', 'forest_rock.json'}
        if len(custom_plan) < 14 or any(('configured_feature', name) not in custom_outputs
                or ('placed_feature', name) not in custom_outputs for name in expected_files):
            raise ValueError('沙漠水井/森林石团来源配置闭包计划发生变化')
        custom = []
        for root_id, proof in CUSTOM_ALGORITHMS.items():
            actual_source = sha(source.read(proof['source_entry']))
            actual_target = sha(proof['target_path'].read_bytes())
            hashes_match = actual_source == proof['source_sha256'] and actual_target == proof['target_sha256']
            name = root_id.split(':')[1] + '.json'
            configured = custom_outputs[('configured_feature', name)]['type'] == 'yvanchuxiuzhen:' + proof['type']
            integration_checks = None
            geode_proof = None
            if root_id == 'minecraft:amethyst_geode':
                geode_proof = geode_integration(source, target, Path(plan['source_root']))
                integration_checks = geode_proof['integration_checks']
                hashes_match = hashes_match and all(integration_checks.values())
                if not hashes_match or not configured:
                    raise ValueError('晶洞独立实现源码指纹或配置类型与静态核对版本不符')
            if root_id == 'minecraft:large_dripstone':
                target_algorithm_entry = proof['source_entry']
                target_algorithm_source = target.read(target_algorithm_entry).decode('utf-8')
                target_algorithm_hash = sha(target_algorithm_source.encode('utf-8'))
                mixin = proof['mixin_path'].read_bytes()
                mixin_source = mixin.decode('utf-8')
                mixin_config = json.loads((ROOT / 'src/main/resources/yvanchuxiuzhen.terrain.mixins.json').read_text(encoding='utf-8'))
                registered_source = (ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/LegacyBiomeFeatures.java').read_text(encoding='utf-8')
                setup_source = (ROOT / 'src/main/java/net/yvanchuxiuzhen/system/world/WorldSetup.java').read_text(encoding='utf-8')
                planned_config = custom_outputs[('configured_feature', name)]
                planned_placement = custom_outputs[('placed_feature', name)]
                target_data_path = ROOT / 'build/moddev/artifacts/neoforge-21.1.265-client-extra-aka-minecraft-resources.jar'
                with zipfile.ZipFile(target_data_path) as target_data:
                    vanilla_config = json.loads(target_data.read('data/minecraft/worldgen/configured_feature/large_dripstone.json'))
                    vanilla_placement = json.loads(target_data.read('data/minecraft/worldgen/placed_feature/large_dripstone.json'))
                vanilla_config['type'] = 'yvanchuxiuzhen:legacy_large_dripstone'
                vanilla_placement['feature'] = 'yvanchuxiuzhen:legacy_vanilla/large_dripstone'
                exact_redirect = ('Lnet/minecraft/core/BlockPos;offset(III)Lnet/minecraft/core/BlockPos;' in mixin_source
                    and 'return BlockPos.containing(pos.getX() + windDelta.x, pos.getY(), pos.getZ() + windDelta.z);' in mixin_source
                    and 'if (!SourceLargeDripstoneFeature.usesLegacyOffset())' in mixin_source)
                target_call_matches = 'return pos.offset(Mth.floor(vec3.x), 0, Mth.floor(vec3.z));' in target_algorithm_source
                integration_checks = {'target_algorithm_hash_matches': target_algorithm_hash == proof['target_algorithm_sha256'],
                    'mixin_hash_matches': sha(mixin) == proof['mixin_sha256'],
                    'mixin_registered_in_existing_terrain_config': 'LargeDripstoneOffsetMixin' in mixin_config['mixins'],
                    'feature_registered_in_existing_world_feature_holder': 'legacy_large_dripstone' in registered_source,
                    'feature_holder_is_on_mod_lifecycle': 'LegacyBiomeFeatures.register(modBus)' in setup_source,
                    'target_contains_the_reviewed_integer_offset_call': target_call_matches,
                    'redirect_is_scoped_to_integer_offset_and_legacy_marker': exact_redirect,
                    'configured_resource_matches_target_codec_with_custom_type': planned_config == vanilla_config,
                    'placed_resource_matches_target_with_custom_configured_reference': planned_placement == vanilla_placement}
                hashes_match = hashes_match and all(integration_checks.values())
                if not hashes_match or not configured:
                    raise ValueError(f'large_dripstone闭环核对失败：hashes={hashes_match}, configured={configured}, checks={integration_checks}')
            custom.append({'id': root_id, 'registry': 'placed_feature',
                'status': 'verified_custom' if hashes_match and configured else 'pending',
                'target_override': 'yvanchuxiuzhen:legacy_vanilla/' + root_id.split(':', 1)[1],
                'target_feature_type': 'yvanchuxiuzhen:' + proof['type'],
                'source_registered_configuration_from_locked_declarations': configured,
                'algorithm_hashes': {'source_sha256': actual_source, 'target_sha256': actual_target,
                    'pinned_hashes_match': hashes_match, 'review': proof['review']}})
            if integration_checks is not None:
                custom[-1]['algorithm_hashes']['integration_checks'] = integration_checks
            if geode_proof is not None:
                custom[-1]['geode_helper_proof'] = geode_proof
                custom[-1]['unreviewed_algorithms'] = []
        for custom_record in custom:
            existing = next((row for row in report['records'] if row['id'] == custom_record['id']), None)
            if existing is None:
                raise ValueError(f'自定义实现根不在55项群系依赖中：{custom_record["id"]}')
            existing.update(custom_record)
        report['validation'] = {'roots': 55, 'verified_reusable': sum(row['status'] == 'verified_reusable' for row in report['records']),
            'verified_custom': sum(row['status'].startswith('verified_custom') for row in report['records']),
            'verified_carver': sum(row['status'] == 'verified_source_replaceable_set_and_reviewed_vanilla_algorithm' for row in report['records']),
            'pending': sum(row['status'] == 'pending' for row in report['records'])}
        report['next_actions'] = [{'registry': row['registry'], 'id': row['id'],
            'differences': row['differences'], 'unreviewed_algorithms': row['unreviewed_algorithms'],
            'required_followup': PENDING_ACTIONS.get(row['id'], '核对独立实现、注册生命周期与资源计划，确认闭合后更新核验状态。')}
            for row in report['records'] if row['status'] == 'pending']
        pending_ids = {row['id'] for row in report['next_actions']}
        if pending_ids != set(PENDING_ACTIONS):
            raise ValueError(f'未为每项pending引用指定闭合方案：missing={sorted(pending_ids - set(PENDING_ACTIONS))}, stale={sorted(set(PENDING_ACTIONS) - pending_ids)}')
        return report
    finally:
        source.close()
        target.close()


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan', type=Path, default=ROOT / 'tmp/source-biomes-plan.json')
    parser.add_argument('--snapshot', type=Path, default=SNAPSHOT)
    parser.add_argument('--source-jar', type=Path)
    parser.add_argument('--target-jar', type=Path, default=ROOT / 'build/moddev/artifacts/neoforge-21.1.265-sources.jar')
    parser.add_argument('--output', type=Path, default=ROOT / 'tmp/vanilla-biome-parity.json')
    args = parser.parse_args()
    plan_value = json.loads(args.plan.read_text(encoding='utf-8'))
    source_jar = args.source_jar
    if source_jar is None:
        candidates = list((Path(plan_value['source_root']) / 'build/fg_cache').glob(
            'net/minecraftforge/forge/1.18.2-40.1.0_mapped_official_1.18.2/*-sources.jar'))
        if len(candidates) != 1:
            raise ValueError('来源1.18.2原版源码Jar必须唯一')
        source_jar = candidates[0]
    report = verify(plan_value, args.snapshot, source_jar, args.target_jar)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(report['validation'], ensure_ascii=False))


if __name__ == '__main__':
    main()
