"""按锁定的 1.18.2 原初系统源码提取宝箱奖励池配置 JSON。

从 B 的 30 个纯奖励过程读取条件边界、随机分支、材料灵气/品阶、丹药装瓶区间、符箓等阶及功法/技能/丹方强类型字段，
消除魔法分隔符 `%`、`/` 与硬编码 Java 分支，生成 `src/main/resources/content/treasure/reward_tables.json`
及输入/输出 SHA-256 指纹证据；保留原有条件顺序、随机权重与不可达分支过滤语义。
"""
from __future__ import annotations

import hashlib
import json
import re
from pathlib import Path
from typing import Any

from build_ledger import ROOT
from convert_content import SOURCES
from equipment_commands import equipment_command

SOURCE = SOURCES['yvanchuxitong'][0] / 'src/main/java/net/mcreator/yvanchuxitong/procedures'
OUTPUT = ROOT / 'src/main/resources/content/treasure/reward_tables.json'
EVIDENCE = ROOT / 'docs/reference/conversion/treasure_reward_tables.json'

CATEGORIES = {
    '书架': {'书籍堆': '0101', '书桶': '0102', '符箓堆': 'Fulu', '丹方': 'Liandantuzhi', '功法书': 'Gongfa', '技能书': 'Jineng'},
    '药架': {'草药堆': 'Caoyao', '丹药堆': 'Danyao', '恢复类丹药堆': 'Huifudanyao', '状态类丹药堆': 'Zhuangtaidanyao', '精进修为丹药堆': 'Xiuweidanyao'},
    '武器架': {'武器架': 'Wuqi', '法宝架': 'Fabao', '盔甲架': 'Hujia', '饰品箱': 'Shipin', '装备': 'Zhuangbei'},
    '宝箱': {'妖兽材料': 'Yaoshoucailiao', '异火': 'Yihuocailiao', '金属材料': 'Jinshucailiao', '灵石': 'Lingshi', '宝石': 'Baoshicailiao',
           '妖丹': 'Yaodancailiao', '布匹': 'Bupi', '综合': 'Cailiao'},
}

LITERAL = re.compile(r'"((?:[^"\\]|\\.)*)"')
STAT_KEYS = [
    'TechniqueHealth', 'TechniqueHeathRec', 'Techniquepower', 'TechniquepowerRec',
    'TechniqueSoul', 'TechniqueSoulRec', 'TechniqueAttack', 'TechniqueAttackDef',
    'TechniqueMana', 'TechniqueManaDef', 'TechniqueSouatt', 'TechniqueSouDef', 'TechniqueSpeed',
]
ELEMENT_KEYS = [('Gold', 'gold'), ('Water', 'water'), ('Tree', 'tree'), ('Fire', 'fire'), ('Soil', 'soil')]


def pool_id(procedure_name: str) -> str:
    return procedure_name.removeprefix('Baoxiang').removesuffix('Procedure').lower()


def unescape_java(text: str) -> str:
    text = text.replace('yvanchuxitong:', 'yvanchuxiuzhen:')
    return re.sub(r'\\u([0-9A-Fa-f]{4})', lambda m: chr(int(m.group(1), 16)), text)


def extract_body(procedure_name: str, text: str) -> tuple[str, str]:
    match = re.search(r'public static (String|double) execute\([^)]*\) \{', text)
    if match is None:
        raise ValueError(f'{procedure_name}: 不是纯奖励过程')
    body = text[match.end():text.rfind('}')].rstrip()
    body = body[:body.rfind('}')]
    body = re.sub(r'"((?:[^"\\]|\\.)*)"', lambda m: '"' + unescape_java(m.group(1)) + '"', body)
    body = body.replace('YvanchuxitongModVariables.MapVariables.get(world).baoxianglevellinshi', 'level')
    body = body.replace('Mth.nextInt(new Random(),', 'Mth.nextInt(random,')
    return match.group(1), body


def find_matching_brace(text: str, open_pos: int) -> int:
    depth = 0
    in_string = False
    escaped = False
    for index in range(open_pos, len(text)):
        ch = text[index]
        if in_string:
            if escaped:
                escaped = False
            elif ch == '\\':
                escaped = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
        elif ch == '{':
            depth += 1
        elif ch == '}':
            depth -= 1
            if depth == 0:
                return index
    raise ValueError('花括号未闭合')


def split_if_chain(text: str, start: int) -> tuple[list[tuple[str | None, str]], int]:
    """解析连续的 `if (...) { ... } else if (...) { ... } else { ... }` 分支链。"""
    branches: list[tuple[str | None, str]] = []
    pos = start
    expect_first = True
    while True:
        while pos < len(text) and text[pos].isspace():
            pos += 1
        if expect_first:
            if not text.startswith('if', pos):
                raise ValueError(f'期望 if：{text[pos:pos + 40]}')
            pos += 2
            expect_first = False
        else:
            if text.startswith('else if', pos):
                pos += 7
            elif text.startswith('else', pos) and (pos + 4 == len(text) or not text[pos + 4].isalnum()):
                pos += 4
                brace = text.find('{', pos)
                end = find_matching_brace(text, brace)
                branches.append((None, text[brace + 1:end]))
                return branches, end + 1
            else:
                return branches, pos
        paren = text.find('(', pos)
        depth = 0
        end_paren = paren
        for i in range(paren, len(text)):
            if text[i] == '(':
                depth += 1
            elif text[i] == ')':
                depth -= 1
                if depth == 0:
                    end_paren = i
                    break
        cond = text[paren + 1:end_paren].strip()
        brace = text.find('{', end_paren)
        end_brace = find_matching_brace(text, brace)
        branches.append((cond, text[brace + 1:end_brace]))
        pos = end_brace + 1


def parse_number(value: str) -> float:
    try:
        return float(value.strip())
    except ValueError:
        return 0.0


def parse_reward_string(raw: str) -> dict[str, Any]:
    if not raw:
        return {'kind': 'empty'}
    if raw.startswith('指令%'):
        return {'kind': 'command', 'command': equipment_command(raw.split('%', 1)[1])}

    tokens = re.split(r'%(?=(?:[A-Za-z0-9_]+/(?:char|int|double|doube|logic)/|无(?:%|$)))', raw)
    identity = tokens[0].split('/')
    if len(identity) < 3:
        return {'kind': 'empty'}
    id_kind = identity[0]
    item_id = identity[2].lower()

    texts: dict[str, str] = {}
    numbers: dict[str, float] = {}
    for index, token in enumerate(tokens[1:], 1):
        parts = token.split('/', 2)
        if parts[0] == '无':
            continue
        field_type = parts[1] if len(parts) > 1 else ''
        field_val = parts[2] if len(parts) > 2 else ''
        if index == 1:
            texts[parts[0]] = field_val
        elif field_type == 'char':
            texts[parts[0]] = field_val
        elif field_type == 'int':
            numbers[parts[0]] = float(int(parse_number(field_val)))
        elif field_type in ('double', 'doube'):
            numbers[parts[0]] = parse_number(field_val)

    if 'ExercisesName' in texts:
        elements = {
            target_key: numbers.get(f'Exercises{source_key}', 0.0)
            for source_key, target_key in ELEMENT_KEYS
            if f'Exercises{source_key}' in numbers
        }
        return {
            'kind': 'technique',
            'item': item_id,
            'name': texts.get('ExercisesName', ''),
            'grade_text': texts.get('belongTopinjietext', ''),
            'grade': numbers.get('belongTopinjie', 0.0),
            'level_text': texts.get('belongToLeveltext', ''),
            'level': numbers.get('belongToLevel', 0.0),
            'body': texts.get('belongTobody', ''),
            'intro': texts.get('belongTojianjie', ''),
            'elements': elements,
            'tiers': [texts.get(stat_key, '') for stat_key in STAT_KEYS],
        }

    if any(k in texts for k in ('jinengName', 'shenfaName', 'flyskillName')):
        if 'jinengName' in texts:
            skill_kind, name_key = 'jineng', 'jinengName'
        elif 'shenfaName' in texts:
            skill_kind, name_key = 'shenfa', 'shenfaName'
        else:
            skill_kind, name_key = 'flyskill', 'flyskillName'
        extra_texts = {
            k: v for k, v in texts.items()
            if k not in (name_key, 'belongTopinjietext', 'belongToLeveltext', 'belongTobody', 'belongTojianjie')
        }
        skill_numbers = {
            k: v for k, v in numbers.items()
            if k not in ('belongTopinjie', 'belongToLevel')
        }
        result: dict[str, Any] = {
            'kind': 'skill',
            'item': item_id,
            'skill_kind': skill_kind,
            'name': texts.get(name_key, ''),
            'grade': numbers.get('belongTopinjie', 0.0),
            'level': numbers.get('belongToLevel', 0.0),
            'body': texts.get('belongTobody', ''),
            'intro': texts.get('belongTojianjie', ''),
        }
        if skill_numbers:
            result['numbers'] = skill_numbers
        if extra_texts:
            result['texts'] = extra_texts
        return result

    if id_kind == 'fuluid':
        return {'kind': 'fulu', 'item': item_id}

    if id_kind == 'danyaoid':
        effects: list[dict[str, Any]] = []
        idx = 0
        while f'danyaoxiaoguo{idx}' in texts:
            effects.append({
                'id': texts[f'danyaoxiaoguo{idx}'],
                'level': int(numbers.get(f'danyaolevel{idx}', 0)),
                'duration': int(numbers.get(f'danyaotick{idx}', 0)),
            })
            idx += 1
        return {
            'kind': 'medicine',
            'item': item_id,
            'name': texts.get('danyaoname', ''),
            'dose': int(numbers.get('shuliang', 0)),
            'effects': effects,
        }

    if id_kind == 'danfangid':
        ingredients: list[dict[str, str]] = []
        idx = 1
        while f'cailiao0{idx}' in texts:
            ingredients.append({
                'id': texts[f'cailiao0{idx}'],
                'name': texts.get(f'cailiao0{idx}name', ''),
            })
            idx += 1
        return {
            'kind': 'recipe',
            'item': item_id,
            'medicine_name': texts.get('danyaoname', ''),
            'recipe_description': texts.get('danfangshuoming', ''),
            'medicine_description': texts.get('danyaoshuoming', ''),
            'level_text': texts.get('item_level', ''),
            'level': int(numbers.get('item_level_num', 0)),
            'ingredients': ingredients,
        }

    if id_kind in ('cailiaoID', 'cailiaoid'):
        return {'kind': 'item', 'item': item_id}

    raise ValueError(f'未知奖励前缀 {id_kind}: {raw}')


def parse_material_quality(body: str) -> list[dict[str, int]]:
    """从 SetcailiaopinjieProcedure 解析 1..100 概率区间并转为权重分布。"""
    m = re.search(r'if\s*\(', body)
    assert m is not None
    branches, _ = split_if_chain(body, m.start())
    counts: dict[int, int] = {}
    for roll in range(1, 101):
        chosen: int | None = None
        for cond, block in branches:
            if cond is None:
                matched = True
            else:
                expr = cond.replace('&&', ' and ').replace('jilv', str(roll))
                matched = bool(eval(expr, {'__builtins__': {}}, {}))
            if matched:
                assign = re.search(r'cailiaopinjie\s*=\s*(\d+)', block)
                assert assign is not None
                chosen = int(assign.group(1))
                break
        assert chosen is not None
        counts[chosen] = counts.get(chosen, 0) + 1
    return [{'weight': weight, 'quality': quality} for quality, weight in sorted(counts.items(), reverse=True)]


def parse_choice_block(block: str, has_material_ctx: bool, quality_dist: list[dict[str, int]],
                       fixed_quality: int | None) -> tuple[list[dict[str, Any]], dict[str, Any]]:
    """解析单个等级分段内的随机分支及材料/丹药参数。"""
    entries: list[dict[str, Any]] = []
    tier_ctx: dict[str, Any] = {}

    if fixed_quality is not None:
        tier_ctx['material_qualities'] = [{'weight': 1, 'quality': fixed_quality}]
    elif has_material_ctx:
        tier_ctx['material_qualities'] = quality_dist

    mat_range = re.search(r'cailiaolevel\s*=\s*Mth\.nextInt\(random,\s*(\d+),\s*(\d+)\);', block)
    mat_fixed = re.search(r'cailiaolevel\s*=\s*(\d+);', block)
    if mat_range:
        tier_ctx['material_level'] = {'min': int(mat_range.group(1)), 'max': int(mat_range.group(2))}
    elif mat_fixed:
        val = int(mat_fixed.group(1))
        tier_ctx['material_level'] = {'min': val, 'max': val}

    roll_m = re.search(r'suijizhi\s*=\s*Mth\.nextInt\(random,\s*(\d+),\s*(\d+)\);', block)
    if roll_m:
        lo, hi = int(roll_m.group(1)), int(roll_m.group(2))
        if_pos = block.find('if', roll_m.end())
        cases: dict[int, str] = {}
        while if_pos != -1:
            branches, end = split_if_chain(block, if_pos)
            matched: set[int] = set()
            for cond, sub in branches:
                lit_m = re.search(r'neirong\s*=\s*"([^"]*)";', sub)
                call_m = re.search(r'(Baoxiang\w+Procedure)\.execute', sub)
                val = f'__CALL__:{pool_id(call_m.group(1))}' if call_m else (lit_m.group(1) if lit_m else '')
                if cond is None:
                    for roll in range(lo, hi + 1):
                        if roll not in matched:
                            cases[roll] = val
                else:
                    eq_m = re.match(r'suijizhi\s*==\s*(\d+)$', cond)
                    assert eq_m is not None, f'未支持随机条件：{cond}'
                    roll = int(eq_m.group(1))
                    matched.add(roll)
                    cases[roll] = val
            # 来源有相邻但独立的抽签判断链，后段只覆盖命中的签位。
            if_pos = end if re.match(r'if\s*\(suijizhi\s*==', block[end:]) else -1
        for roll in range(lo, hi + 1):
            raw = cases.get(roll, '')
            reward = {'kind': 'pool', 'pool': raw.split(':', 1)[1]} if raw.startswith('__CALL__:') else parse_reward_string(raw)
            if has_material_ctx and reward.get('kind') == 'item':
                reward = {'kind': 'material', 'item': reward['item']}
            entries.append({'weight': 1, 'reward': reward})
    else:
        lit_m = re.search(r'neirong\s*=\s*"([^"]*)";', block)
        if lit_m:
            reward = parse_reward_string(lit_m.group(1))
            if has_material_ctx and reward.get('kind') == 'item':
                reward = {'kind': 'material', 'item': reward['item']}
            entries.append({'weight': 1, 'reward': reward})

    merged: list[dict[str, Any]] = []
    for entry in entries:
        if merged and merged[-1]['reward'] == entry['reward']:
            merged[-1]['weight'] += entry['weight']
        else:
            merged.append(entry)
    return merged, tier_ctx


def parse_level_bounds(cond: str) -> dict[str, float]:
    cond = cond.replace('TreasureLEVEL', 'level')
    m = re.match(r'^level\s*(<=|==|>=)\s*(\d+(?:\.\d+)?)$', cond)
    if m is None:
        raise ValueError(f'未支持等级条件：{cond}')
    op, val = m.group(1), float(m.group(2))
    if op == '<=':
        return {'max_level': val}
    if op == '>=':
        return {'min_level': val}
    return {'min_level': val, 'max_level': val}


def parse_fulu_tiers(body: str) -> dict[int, tuple[int, str]]:
    """提取 tableFulu 第一段 `TreasureLEVEL` 对应的符箓等级与品阶名称。"""
    m = re.search(r'if\s*\(TreasureLEVEL', body)
    assert m is not None
    branches, _ = split_if_chain(body, m.start())
    result: dict[int, tuple[int, str]] = {}
    for lvl in range(0, 12):
        for cond, sub in branches:
            expr = cond.replace('||', ' or ').replace('TreasureLEVEL', str(lvl))
            if eval(expr, {'__builtins__': {}}, {}):
                s_lvl = int(re.search(r'suijilevel\s*=\s*(\d+)', sub).group(1))
                s_txt = re.search(r'fululeveltxt\s*=\s*"([^"]*)"', sub).group(1)
                result[lvl] = (s_lvl, s_txt)
                break
    return result


def build_pool(name: str, body: str, dispatches: list[dict[str, str]],
               quality_dist: list[dict[str, int]]) -> dict[str, Any]:
    has_material_ctx = 'SetcailiaopinjieProcedure' in body or 'cailiaopinjie' in body
    fixed_quality_m = re.search(r'\ncailiaopinjie\s*=\s*(\d+);', body)
    fixed_quality = int(fixed_quality_m.group(1)) if fixed_quality_m else None
    medicine_range_m = re.search(r'danyaonum\s*=\s*Mth\.nextInt\(random,\s*(\d+),\s*(\d+)\);', body)
    medicine_pack = (
        {'min': int(medicine_range_m.group(1)), 'max': int(medicine_range_m.group(2))}
        if medicine_range_m else None
    )

    fulu_map = parse_fulu_tiers(body) if name == 'BaoxiangFuluProcedure' else {}
    scan_start = 0
    if fulu_map:
        m_first = re.search(r'if\s*\(TreasureLEVEL', body)
        assert m_first is not None
        _, scan_start = split_if_chain(body, m_first.start())

    tiers: list[dict[str, Any]] = []
    lvl_if = re.search(r'if\s*\(\s*(?:level|TreasureLEVEL)\b', body[scan_start:])
    if lvl_if:
        branches, _ = split_if_chain(body, scan_start + lvl_if.start())
        for cond, block in branches:
            bounds = parse_level_bounds(cond) if cond is not None else {}
            entries, tier_ctx = parse_choice_block(block, has_material_ctx, quality_dist, fixed_quality)
            if medicine_pack is not None:
                tier_ctx['medicine_pack_count'] = medicine_pack
            if fulu_map:
                min_lvl = bounds.get('min_level', bounds.get('max_level', 1.0))
                max_lvl = bounds.get('max_level')
                sample_lvl = int(min_lvl)
                if sample_lvl in fulu_map:
                    fl_val, fl_txt = fulu_map[sample_lvl]
                    tier_ctx['fulu_level'] = fl_val
                    tier_ctx['fulu_tier_text'] = fl_txt
                if max_lvl is None and int(min_lvl) == 8:
                    tier_8 = {'min_level': 8.0, 'max_level': 8.0, 'context': dict(tier_ctx), 'entries': entries}
                    tiers.append(tier_8)
                    # 等级为 double；恰好 8 优先匹配上一段，其余大于 8 的等级品阶为空。
                    above_8 = {**tier_ctx, 'fulu_level': 0, 'fulu_tier_text': ''}
                    tiers.append({'min_level': 8.0, 'context': above_8, 'entries': entries})
                    continue
            tier: dict[str, Any] = dict(bounds)
            if tier_ctx:
                tier['context'] = tier_ctx
            tier['entries'] = entries
            tiers.append(tier)
    else:
        entries, tier_ctx = parse_choice_block(body[scan_start:], has_material_ctx, quality_dist, fixed_quality)
        if medicine_pack is not None:
            tier_ctx['medicine_pack_count'] = medicine_pack
        tier = {}
        if tier_ctx:
            tier['context'] = tier_ctx
        tier['entries'] = entries
        tiers.append(tier)

    pool: dict[str, Any] = {'id': pool_id(name)}
    if dispatches:
        pool['dispatches'] = dispatches
    pool['tiers'] = tiers
    return pool


def main() -> None:
    names = {f'Baoxiang{suffix}Procedure' for categories in CATEGORIES.values() for suffix in categories.values()}
    pending = list(names)
    texts: dict[str, str] = {}
    while pending:
        name = pending.pop()
        if name in texts:
            continue
        text = (SOURCE / f'{name}.java').read_text(encoding='utf-8')
        texts[name] = text
        calls = re.findall(r'(Baoxiang\w+Procedure|SetcailiaopinjieProcedure)\.execute\(', text)
        pending.extend(call for call in calls if call not in texts)

    dispatch_by_proc: dict[str, list[dict[str, str]]] = {}
    for category, subtypes in CATEGORIES.items():
        for subtype, suffix in subtypes.items():
            proc = f'Baoxiang{suffix}Procedure'
            dispatch_by_proc.setdefault(proc, []).append({'category': category, 'subtype': subtype})

    _, quality_body = extract_body('SetcailiaopinjieProcedure', texts['SetcailiaopinjieProcedure'])
    quality_dist = parse_material_quality(quality_body)

    pools: list[dict[str, Any]] = []
    for name in sorted(texts):
        if name == 'SetcailiaopinjieProcedure':
            continue
        ret_type, body = extract_body(name, texts[name])
        if ret_type != 'String':
            raise ValueError(f'{name}: 非期望返回类型 {ret_type}')
        pools.append(build_pool(name, body, dispatch_by_proc.get(name, []), quality_dist))

    serialized = json.dumps(pools, ensure_ascii=False, indent=2) + '\n'
    OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT.write_text(serialized, encoding='utf-8', newline='\n')
    records = [{'source': f'B:{name}', 'sha256': hashlib.sha256(texts[name].encode('utf-8')).hexdigest()} for name in sorted(texts)]
    EVIDENCE.write_text(
        json.dumps(
            {
                'inputs': records,
                'output': str(OUTPUT.relative_to(ROOT)).replace('\\', '/'),
                'output_sha256': hashlib.sha256(serialized.encode('utf-8')).hexdigest(),
            },
            ensure_ascii=False,
            indent=2,
        ) + '\n',
        encoding='utf-8',
        newline='\n',
    )
    total_entries = sum(len(t['entries']) for p in pools for t in p['tiers'])
    print(f'reward_tables.json: {len(pools)} pools, {sum(len(p.get("dispatches", [])) for p in pools)} dispatches, {total_entries} weighted entries')


if __name__ == '__main__':
    main()
