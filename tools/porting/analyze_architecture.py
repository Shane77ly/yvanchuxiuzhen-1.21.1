"""索引两个 1.18.2 工程的架构入口、动态键、状态访问与生命周期。

扫描保留完整表达式和来源位置；语法形状匹配不替代 Java 类型解析或游戏运行。
"""
from __future__ import annotations

import argparse
from bisect import bisect_right
from collections import Counter, defaultdict, deque
import csv
import json
from pathlib import Path
import re

from inventory_legacy import SOURCES
from investigate_legacy import PREFIX, csv_text, mask_java

ROOT = Path(__file__).resolve().parents[2]
DEST = ROOT / 'docs/reference/legacy/inventory'
METHOD = re.compile(r'(?m)^[ \t]*(?:public|protected|private)\s+(?:(?:static|final|synchronized)\s+)*(?:[\w.$<>?,\[\] ]+\s+)?(?P<name>\w+)\s*\((?P<args>[^;{}]*)\)\s*(?:throws [^{]+)?\{')
KEY_CALL = re.compile(r'\.(get(?:Double|Int|String|Boolean|Compound|Long|Float|List|Byte|Short|IntArray|LongArray|ByteArray)|put(?:Double|Int|String|Boolean|Long|Float|Byte|Short|IntArray|LongArray|ByteArray)?|contains|remove)\s*\(')
PROC_CALL = re.compile(r'\b(\w+Procedure)\s*\.\s*execute\s*\(')
VARS = '|'.join(value + 'Variables' for value in PREFIX.values())


def compact(text: str) -> str:
    return re.sub(r'\s+', ' ', text).strip()


def brackets(mask: str) -> dict[int, int]:
    stack, result = [], {}
    for match in re.finditer(r'[(){}\[\]]', mask):
        i, char = match.start(), match[0]
        if char in '({[':
            stack.append(i)
        else:
            start = stack.pop()
            if '({['.index(mask[start]) != ')}]'.index(char):
                raise ValueError(f'括号不匹配：{start} / {i}')
            result[start] = i
            result[i] = start
    if stack:
        raise ValueError(f'未闭合括号：{stack[-1]}')
    return result


def first_arg(text: str, mask: str, pairs: dict, start: int) -> str:
    i = start + 1
    while i < pairs[start]:
        if mask[i] in '([{':
            i = pairs[i] + 1
        elif mask[i] == ',':
            break
        else:
            i += 1
    return compact(text[start + 1:i])


def arguments(text: str, mask: str, pairs: dict, start: int) -> list[str]:
    values=[]
    i=begin=start+1
    while i<pairs[start]:
        if mask[i] in '([{':
            i=pairs[i]+1
        elif mask[i]==',':
            values.append(compact(text[begin:i]));begin=i+1;i+=1
        else:
            i+=1
    values.append(compact(text[begin:i]))
    return values


def receiver(text: str, mask: str, pairs: dict, dot: int) -> str:
    i = dot - 1
    while i >= 0:
        if mask[i].isspace():
            i -= 1
        elif mask[i] in ')]':
            i = pairs[i] - 1
        elif mask[i].isalnum() or mask[i] in '_$.':
            i -= 1
        else:
            break
    return compact(text[i + 1:dot])


def key_kind(expr: str) -> str:
    while expr.startswith('(') and expr.endswith(')'):
        masked = mask_java(expr)
        pair = brackets(masked)
        if pair[0] != len(expr) - 1:
            break
        expr = expr[1:-1].strip()
    return 'literal' if re.fullmatch(r'"(?:\\.|[^"\\])*"', expr) else 'dynamic'


def carrier(expr: str) -> str:
    if 'getPersistentData(' in expr:
        return 'entity_persistent_data'
    if 'getTileData(' in expr:
        return 'block_entity_tile_data'
    if 'getOrCreateTag' in expr or 'getTag(' in expr:
        return 'item_tag_expression'
    if 'Variables' in expr:
        return 'variables_field_expression'
    return 'local_or_other_receiver'


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-workspace', type=Path, default=ROOT.parent / 'yuanchuxiuzhen-1.21.1')
    args = parser.parse_args()
    rows = defaultdict(list)
    proc_edges, roots, proc_names = defaultdict(set), defaultdict(set), set()
    fields = list(csv.DictReader((DEST / 'variables_detailed.csv').open(encoding='utf-8', newline='')))
    known_fields = {(r['source_mod'], r['scope'], r['name']) for r in fields}
    variables_re = re.compile(r'\.\s*(' + '|'.join(sorted({re.escape(r['name']) for r in fields}, key=len, reverse=True)) + r')\b')
    by_prefix = {value + 'Variables': key for key, value in PREFIX.items()}
    for mod, source in SOURCES.items():
        source_root = args.source_workspace / source
        paths = sorted((source_root / 'src/main/java').rglob('*.java'))
        print(f'架构扫描 {mod}: {len(paths)} Java', flush=True)
        for path in paths:
            text = path.read_text(encoding='utf-8-sig')
            mask = mask_java(text)
            pairs = brackets(mask)
            offsets = [m.start() for m in re.finditer('\n', text)]
            line = lambda p: bisect_right(offsets, p) + 1
            rel, stem = path.relative_to(source_root).as_posix(), path.stem
            base = {'source_mod': mod, 'class': stem, 'source_file': rel}
            class_scopes = []
            for cm in re.finditer(r'\bclass\s+(\w+)\b[^;{}]*\{', mask):
                opening = cm.end()-1
                annotation = mask[max(0,cm.start()-250):cm.start()]
                annotated = bool(re.search(r'@Mod\.EventBusSubscriber(?:\([^)]*\))?\s*(?:public\s+)?(?:static\s+)?$', annotation))
                class_scopes.append((opening,pairs[opening],cm[1],annotated))
            compound_names = set(re.findall(r'\bCompoundTag\s+(\w+)',mask))
            methods = []
            for match in METHOD.finditer(mask):
                opening = match.end() - 1
                methods.append((match.start(), pairs[opening], match['name'], compact(match['args'])))
            def method_at(position: int) -> str:
                applicable = [m for m in methods if m[0] <= position < m[1]]
                return applicable[-1][2] if applicable else '<class>'
            imports = {m[2]: m[1] for m in re.finditer(r'import net\.mcreator\.(\w+)\.procedures\.(\w+);', text)}
            event_methods = []
            for start, end, name, params in methods:
                before = mask[max(0, start-180):start].rstrip()
                if re.search(r'@SubscribeEvent(?:\([^)]*\))?\s*$', before):
                    enclosing = [c for c in class_scopes if c[0] < start < c[1]]
                    owner_class = enclosing[-1] if enclosing else None
                    automatic = bool(owner_class and owner_class[3])
                    event_methods.append(name)
                    rows['event_entries'].append({**base, 'line': line(start), 'method': name, 'parameters': params,
                        'class_subscriber': automatic, 'enclosing_class':owner_class[2] if owner_class else '<anonymous>',
                        'annotation': compact(text[max(0, start-180):start]).split('@SubscribeEvent')[-1]})
                    if automatic:
                        roots[(mod, stem)].add('automatic_event_subscriber')
            if '/procedures/' in rel:
                proc_names.add((mod, stem))
            for match in PROC_CALL.finditer(mask):
                target = (imports.get(match[1], mod), match[1])
                # 完整限定名优先于同名导入与当前包。
                qualified = re.search(r'net\.mcreator\.(\w+)\.procedures\.$', mask[max(0,match.start()-100):match.start()])
                if qualified:
                    target = (qualified[1], match[1])
                proc_edges[(mod, stem)].add(target)
                rows['method_calls'].append({**base, 'line': line(match.start()), 'method': method_at(match.start()),
                    'target_mod': target[0], 'target_class': target[1], 'arguments': compact(text[match.end():pairs[match.end()-1]])})
                if '/procedures/' not in rel:
                    roots[(mod, stem)].add('external_class_call')
            for match in KEY_CALL.finditer(mask):
                opening = match.end()-1
                key = first_arg(text, mask, pairs, opening)
                recv = receiver(text, mask, pairs, match.start())
                kind = key_kind(key) if key else 'empty'
                owner_kind = carrier(recv)
                if owner_kind == 'local_or_other_receiver' and recv.strip('()') in compound_names:
                    owner_kind = 'declared_compound_tag'
                resolved_key, binding = key, 'direct_expression'
                if re.fullmatch(r'\w+',key):
                    enclosing=[m for m in methods if m[0]<=match.start()<m[1]]
                    if enclosing:
                        method=enclosing[-1]
                        params=[p.strip() for p in method[3].split(',')]
                        indexes=[i for i,p in enumerate(params) if re.fullmatch(r'String\s+'+key,p)]
                        invocation=re.match(r'\s*}\s*\.\s*'+re.escape(method[2])+r'\s*\(',mask[method[1]+1:])
                        if indexes and invocation:
                            opening=method[1]+1+invocation.end()-1
                            values=arguments(text,mask,pairs,opening)
                            resolved_key,binding=values[indexes[0]],'inline_anonymous_helper_argument'
                rows['key_expressions'].append({**base, 'line': line(match.start()), 'method': method_at(match.start()),
                    'operation': match[1], 'receiver': recv, 'carrier': owner_kind, 'key_kind': kind, 'key_expression': key,
                    'resolved_key_expression':resolved_key,'key_binding':binding})
            # 每一个字段访问都保留解析依据；只认显式 Variables、orElse 构造类型和已绑定 lambda。
            lambdas = []
            for match in re.finditer(rf'getCapability\(\s*({VARS})\.PLAYER_VARIABLES_CAPABILITY\s*,\s*null\s*\)\s*\.ifPresent\(\s*(\w+)\s*->\s*\{{', mask):
                lambdas.append((match.end()-1, pairs[match.end()-1], match[2], by_prefix[match[1]]))
            if stem not in [v+'Variables' for v in PREFIX.values()]:
                for match in variables_re.finditer(mask):
                    recv = receiver(text, mask, pairs, match.start())
                    scope, owner, evidence = '', '', 'unresolved_member'
                    explicit = re.search(rf'({VARS})\.(WorldVariables|MapVariables)\s*\.get\(', recv)
                    player = re.search(rf'new\s+({VARS})\.PlayerVariables\s*\(', recv)
                    if explicit:
                        owner, scope, evidence = by_prefix[explicit[1]], explicit[2], 'explicit_saved_data_receiver'
                    elif player:
                        owner, scope, evidence = by_prefix[player[1]], 'PlayerVariables', 'explicit_player_variables_type'
                    else:
                        applicable = [v for v in lambdas if v[0] <= match.start() < v[1] and recv == v[2]]
                        if applicable:
                            owner, scope, evidence = applicable[-1][3], 'PlayerVariables', 'capability_lambda_binding'
                    tail = mask[match.end():match.end()+20]
                    access = 'write' if re.match(r'\s*(?:=(?!=)|[+\-*/]=|\+\+|--)',tail) else 'read_or_access'
                    if owner and (owner, scope, match[1]) not in known_fields:
                        evidence = 'resolved_type_unknown_field'
                    rows['typed_field_accesses'].append({**base, 'line':line(match.start()), 'method':method_at(match.start()),
                        'field':match[1], 'receiver':recv, 'access':access, 'owner_mod':owner, 'scope':scope,'evidence':evidence})
            if stem == PREFIX[mod]+'Variables':
                clone = next(m for m in methods if m[2]=='clonePlayer')
                clone_text = text[clone[0]:clone[1]]
                conditional = clone_text.index('if (!event.isWasDeath())')
                for match in re.finditer(r'clone\.(\w+)\s*=\s*original\.(\w+)',clone_text):
                    rows['clone_policy'].append({**base,'line':line(clone[0]+match.start()),'field':match[1],
                        'source_field':match[2],'copy_condition':'non_death_only' if match.start()>conditional else 'all_clone_events'})
            if '/world/inventory/' in rel:
                slots = re.findall(r'new Slot\(\w+,\s*(\d+),',mask)
                rows['menu_surfaces'].append({**base,'literal_slot_ids':','.join(slots),
                    'inventory_sizes':','.join(re.findall(r'new ItemStackHandler\((\d+)\)',mask)),
                    'bind_item':'readByte()' in mask and 'Hand.MAIN_HAND' in mask,
                    'bind_entity':'getEntity(' in mask,'bind_block':'getBlockEntity(' in mask,
                    'methods':'; '.join(m[2] for m in methods),
                    'still_valid_true':bool(re.search(r'boolean stillValid\([^)]*\)\s*\{\s*return true;',mask))})
            if '/network/' in rel and stem.endswith('Message'):
                rows['network_surfaces'].append({**base,'fields':'; '.join(compact(m[0]) for m in re.finditer(r'(?m)^\s*(?:private|public)\s+(?:int|double|boolean|String|BlockPos|ItemStack)\s+\w+\s*;',mask)),
                    'methods':'; '.join(m[2] for m in methods), 'enqueue_work':'enqueueWork(' in mask,
                    'chunk_check':'hasChunkAt(' in mask,'menu_check':'containerMenu' in mask,
                    'handlers':'; '.join(dict.fromkeys(m[1] for m in PROC_CALL.finditer(mask)))})
            for regex, surface in [(r'performCommand\s*\(', 'command_execution'),(r'Class\.forName\s*\(', 'reflection'),
                                   (r'getDeclaredMethod\s*\(', 'reflection_method'),(r'queueServerWork\s*\(', 'delayed_work'),
                                   (r'MinecraftForge\.EVENT_BUS\.register\s*\(', 'manual_event_registration'),
                                   (r'TranslatableComponent\s*\(', 'translation_lookup'),(r'ForgeRegistries\.\w+\.getValue\s*\(', 'registry_lookup')]:
                for match in re.finditer(regex,mask):
                    opening=match.end()-1
                    rows['indirect_surfaces'].append({**base,'line':line(match.start()),'method':method_at(match.start()),
                        'surface':surface,'arguments':compact(text[opening+1:pairs[opening]])})
            if '/command/' in rel:
                rows['command_surfaces'].append({**base, 'literals':'; '.join(re.findall(r'Commands\.literal\("([^"\n]+)"',text)),
                    'permission_levels':'; '.join(re.findall(r'hasPermission\((\d+)\)',mask)),
                    'handlers':'; '.join(dict.fromkeys(m[1] for m in PROC_CALL.finditer(mask)))})
            rows['class_surfaces'].append({**base,'method_count':len(methods),'event_count':len(event_methods),
                'client_imports':'; '.join(re.findall(r'import (net\.minecraft\.client\.[\w.]+);',text)),
                'client_annotation':'Dist.CLIENT' in mask,'server_side_checks':mask.count('isClientSide'),
                'manual_event_registration':mask.count('MinecraftForge.EVENT_BUS.register(this)'),
                'manual_event_unregistration':mask.count('MinecraftForge.EVENT_BUS.unregister(this)'),
                'save_hooks':'; '.join(m[2] for m in methods if m[2] in ('saveAdditional','load','addAdditionalSaveData','readAdditionalSaveData','serializeNBT','deserializeNBT','removed','invalidateCaps','getUpdateTag','getUpdatePacket'))})
            if '/entity/' in rel and '/model/' not in rel and '/block/entity/' not in rel:
                goals=[]
                for gm in re.finditer(r'(?:goalSelector|targetSelector)\.addGoal\s*\(',mask):
                    goals.append(f'{line(gm.start())}: '+compact(text[gm.start():pairs[gm.end()-1]+1]))
                rows['entity_surfaces'].append({**base,'base_type':'; '.join(re.findall(r'class\s+\w+\s+extends\s+(\w+)',mask)),
                    'goals':'; '.join(goals),'attributes':'; '.join(compact(m[0]) for m in re.finditer(r'\.add\(Attributes\.\w+,\s*[^)]+\)',mask)),
                    'synced_fields':'; '.join(re.findall(r'EntityDataAccessor<[^>]+>\s+(\w+)',mask)),
                    'hooks':'; '.join(m[2] for m in methods)})
        for path in sorted((source_root/'src/main/resources').rglob('*.json')):
            rel=path.relative_to(source_root).as_posix()
            payload=json.loads(path.read_text(encoding='utf-8-sig'))
            if '/lang/' in rel:
                for key,value in payload.items():
                    if isinstance(value,str) and (len(value)>200 or '%' in value or '&' in value):
                        rows['language_payloads'].append({'source_mod':mod,'source_file':rel,'key':key,'length':len(value),
                            'value':value,'evidence':'长度或分隔符候选；机器消费需结合调用位置'})
    queue=deque(roots)
    reached={key: key for key in roots}
    while queue:
        source=queue.popleft()
        for target in proc_edges[source]:
            if target not in reached:
                reached[target]=reached[source]
                queue.append(target)
    for mod,stem in sorted(proc_names):
        origin=reached.get((mod,stem))
        rows['procedure_reachability'].append({'source_mod':mod,'class':stem,
            'structural_reachable':bool(origin),'root_mod':origin[0] if origin else '',
            'root_class':origin[1] if origin else '',
            'evidence':'从事件注解或非过程类调用出发；未验证调用分支条件和来源类注册生效'})
    for name,values in rows.items():
        if values:
            (DEST/(name+'.csv')).write_text(csv_text(values),encoding='utf-8',newline='\n')
    keys=rows['key_expressions']
    dynamic=[r for r in keys if r['key_kind']=='dynamic']
    (DEST/'dynamic_keys.csv').write_text(csv_text(dynamic),encoding='utf-8',newline='\n')
    dynamic_states = [r for r in dynamic if r['carrier']!='local_or_other_receiver']
    (DEST/'dynamic_state_keys.csv').write_text(csv_text(dynamic_states),encoding='utf-8',newline='\n')
    summary={'counts':{k:len(v) for k,v in rows.items()},'dynamic_key_sites':len(dynamic),
        'dynamic_carriers':dict(Counter(r['carrier'] for r in dynamic)),
        'dynamic_state_key_sites':len(dynamic_states),
        'inline_key_bindings':sum(r['key_binding']=='inline_anonymous_helper_argument' for r in keys),
        'field_resolution':dict(Counter(r['evidence'] for r in rows['typed_field_accesses'])),
        'clone_policy':{mod:dict(Counter(r['copy_condition'] for r in rows['clone_policy'] if r['source_mod']==mod)) for mod in SOURCES},
        'structurally_reached_procedures':sum(r['structural_reachable'] for r in rows['procedure_reachability']),
        'limitations':['词法扫描不是完整 Java 语义解析。','可达性使用类级 Procedure 调用，不证明条件可满足或类已注册。',
            '动态键保留表达式；只有正文人工核对的范围才能视为已展开。','键访问包含 Map/集合/本地 CompoundTag，按 receiver 形状标记，不能全部当作持久化状态。',
            '字段访问显式解析不包含任意别名与跨方法返回值；未解析项保留原文。']}
    (DEST/'architecture_summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(summary,ensure_ascii=False))


if __name__=='__main__':
    main()
