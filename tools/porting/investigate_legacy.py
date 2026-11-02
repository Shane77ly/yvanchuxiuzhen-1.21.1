"""提取 1.18.2 两个 Mod 的内容编号、状态字段与静态调用证据。"""

from __future__ import annotations

import argparse
from bisect import bisect_right
from collections import Counter, defaultdict
from concurrent.futures import ThreadPoolExecutor
import csv
import io
import json
from pathlib import Path
import re

from inventory_legacy import SOURCES

ROOT = Path(__file__).resolve().parents[2]
PREFIX = {"yvanchuxiuzhen": "YvanchuxiuzhenMod", "yvanchuxitong": "YvanchuxitongMod"}
TOKEN = re.compile(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*|/\*[\s\S]*?\*/')
FIELD = re.compile(r'(?m)^[ \t]*public[ \t]+(?P<modifiers>(?:(?:static|final)[ \t]+)*)(?P<type>[\w<>?, ]+?)[ \t]+(?P<name>\w+)[ \t]*=[ \t]*(?P<initial>[^\n]*);[ \t]*$')
CALL = re.compile(r'\b(\w+Procedure)\s*\.\s*execute\s*\(')
MODULE_RULES = [
    ("refining", r"lianqi|tripod|tiandaotuzhi|ronglian|jinglian|shuxingzhuanhuan|qiancailu"),
    ("alchemy", r"liandan|danyao|yaolitai|fendan|hedan|baicao|baicaojian|caoyaojiexi"),
    ("talismans", r"fulu|huafu|futou|fudan|motiao"),
    ("formations", r"zhenji|zhenyuan|zhenfa|flag|luopan|chuansongzhen"),
    ("economy", r"bank|paimai|shop|shougou|money|jiaoyi|jiaoge|jiage"),
    ("quests", r"renwu|task|chengjiu|xvanshang"),
    ("gongfa", r"gongfa|guanxiang|canwu|tiandaogongfa"),
    ("abilities", r"jineng|skill|qinggong|fly|feixing|shentong|wuqijineng"),
    ("equipment", r"fabao|benming|ornament|fuqi|xiuli|clothes"),
    ("cultivation", r"xiulian|jindan|yuanying|constitution|tizhi|level|lifetime|lunhui|tupo|tupuo|thunder|leijie"),
    ("creatures", r"yaoshou|organism|guchong|bug|puppet|lingchong"),
    ("npc", r"xiushi|businessman|npc|duihua"),
    ("world", r"weidu|dimension|biome|world|jiegou|shengcheng|fuben|yiji|chuansong|renjian|chengzhen"),
    ("life", r"food|shicai|guo|cook|anban|mujiang|mugong|zhenxian|makebook|plant|crop|drink|wine"),
    ("storage", r"qiankundai|cailiaodai|taoguan|baibaodai|chest|bag"),
    ("knowledge", r"book|shuji|tushu|zhishi"),
    ("combat", r"damage|hurt|attack|shanghai|gongji|defence|defense|dodge|health"),
]


def csv_text(rows: list[dict]) -> str:
    if not rows:
        return ""
    out = io.StringIO(newline="")
    writer = csv.DictWriter(out, fieldnames=list(rows[0]), lineterminator="\n")
    writer.writeheader()
    writer.writerows(rows)
    return out.getvalue()


def mask_java(text: str) -> str:
    """屏蔽字符串和注释，保留位置及换行供静态符号扫描定位。"""
    return TOKEN.sub(lambda m: re.sub(r'[^\n]', ' ', m[0]), text)


def module_candidate(name: str) -> str:
    for module, pattern in MODULE_RULES:
        if re.search(pattern, name, re.I):
            return module
    return "unclassified"


def extract_fields(mod: str, rel: str, text: str) -> tuple[list, list]:
    states, constants = [], []
    masked = mask_java(text)
    for scope in ("PlayerVariables", "WorldVariables", "MapVariables"):
        match = re.search(rf'public\s+static\s+class\s+{scope}\b[^{{]*\{{', masked)
        begin = match.end()
        depth, end = 1, begin
        while depth:
            depth += (masked[end] == '{') - (masked[end] == '}')
            end += 1
        header = text[begin:end]
        method = re.search(r'(?m)^\s*public\s+(?:static\s+)?[\w<>]+\s+\w+\(', header)
        header = header[:method.start()]
        for match in FIELD.finditer(header):
            name = match['name']
            serial = re.search(rf'nbt\.put\w*\("([^"\n]+)",\s*{re.escape(name)}\b', text[begin:end])
            row = {"source_mod": mod, "scope": scope, "name": name, "java_type": match['type'],
                   "initial_value": match['initial'], "source_file": rel,
                   "source_line": text.count('\n', 0, begin + match.start()) + 1,
                   "nbt_key": serial[1] if serial else "",
                   "identity": f"{mod}/{scope}/{name}"}
            if 'static' in match['modifiers']:
                constants.append(row)
            else:
                suffix = re.search(r'(\d+)$', name)
                row.update({"number_suffix": suffix[1] if suffix else "", "review_status": "声明已核对；行为待逐项核对",
                            "target_name": "", "target_owner": "", "review_note": ""})
                states.append(row)
    return states, constants


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source-workspace', type=Path, default=ROOT.parent / 'yuanchuxiuzhen-1.21.1')
    args = parser.parse_args()
    dest = ROOT / 'docs/reference/legacy/inventory'
    registrations = list(csv.DictReader((dest / 'registrations.csv').open(encoding='utf-8', newline='')))
    state_rows, constants, call_rows, files, menu_rows, proc_rows, nbt_rows = [], [], [], [], [], [], []
    source_meta, contents = {}, {}
    source_java = []
    for mod, relative in SOURCES.items():
        root = args.source_workspace / relative
        paths = sorted((root / 'src/main/java').rglob('*.java'))
        print(f'读取 {mod}：{len(paths)} 个 Java 文件', flush=True)
        with ThreadPoolExecutor(max_workers=8) as pool:
            texts = pool.map(lambda p: p.read_text(encoding='utf-8-sig'), paths)
            for path, text in zip(paths, texts):
                rel = path.relative_to(root).as_posix()
                source_java.append((mod, rel, path.stem, text))
                contents[(mod, path.stem)] = (rel, text)
        var_rel, var_text = contents[(mod, PREFIX[mod] + 'Variables')]
        states, const = extract_fields(mod, var_rel, var_text)
        state_rows.extend(states)
        constants.extend(const)
        resources = sorted(p for p in (root / 'src/main/resources').rglob('*') if p.is_file())
        resource_types = Counter()
        for path in resources:
            parts = path.relative_to(root / 'src/main/resources').parts
            category = '/'.join(parts[:3]) if len(parts) > 3 else '/'.join(parts[:-1])
            resource_types[category] += 1
        lang = root / f'src/main/resources/assets/{mod}/lang/zh_cn.json'
        translations = json.loads(lang.read_text(encoding='utf-8-sig'))
        source_meta[mod] = {"java_files": len(paths), "resources": len(resources), "resource_categories": dict(sorted(resource_types.items())),
                            "states": len(states), "constants": len(const), "source_root": relative,
                            "scope_counts": dict(Counter(r['scope'] for r in states))}
        for row in (r for r in registrations if r['source_mod'] == mod):
            path = row['legacy_id'].split(':', 1)[1]
            translation_kind = {'item': 'item', 'block': 'block', 'entity_type': 'entity', 'mob_effect': 'effect',
                                'enchantment': 'enchantment', 'biome': 'biome'}.get(row['registry'])
            keys = [f'{translation_kind}.{mod}.{path}'] if translation_kind else []
            if row['declaration_kind'] == 'block_item':
                keys.insert(0, f'block.{mod}.{path}')
            found = next((k for k in keys if k in translations), '')
            row.update({"display_name": translations[found] if found else '', "translation_key": found,
                        "number_markers": '/'.join(re.findall(r'\d+', path)), "family": re.sub(r'\d+', '#', path),
                        "candidate_module": module_candidate(path), "target_id": '', "review_note": ''})
    field_names = {r['name'] for r in state_rows}
    field_re = re.compile(r'\.\s*(' + '|'.join(sorted(map(re.escape, field_names), key=len, reverse=True)) + r')\b')
    access_rows = []
    incoming = Counter()
    for mod, rel, stem, text in source_java:
        masked = mask_java(text)
        offsets = [m.start() for m in re.finditer('\n', text)]
        line = lambda position: bisect_right(offsets, position) + 1
        imports = {m[2]: m[1] for m in re.finditer(r'import net\.mcreator\.(\w+)\.procedures\.(\w+);', text)}
        events = re.findall(r'@SubscribeEvent\s+public static void (\w+)\(([^)]+)\)', masked)
        calls = []
        for match in CALL.finditer(masked):
            callee_mod = imports.get(match[1], mod)
            call_rows.append({"source_mod": mod, "source_file": rel, "caller_class": stem,
                              "target_mod": callee_mod, "target_class": match[1], "line": line(match.start()),
                              "kind": "procedure_call" if '/procedures/' in rel else "external_caller"})
            incoming[(callee_mod, match[1])] += 1
            calls.append(match[1])
        files.append({"source_mod": mod, "source_file": rel, "class": stem,
                      "package_area": rel.split(f'net/mcreator/{mod}/')[-1].rsplit('/', 1)[0],
                      "candidate_module": module_candidate(stem), "lines": len(offsets) + 1,
                      "procedure_calls": len(calls), "subscribed_events": len(events)})
        if '/procedures/' in rel:
            proc_rows.append({"source_mod": mod, "class": stem, "source_file": rel,
                              "candidate_module": module_candidate(stem), "event_entries": '; '.join(f'{n}({a})' for n, a in events),
                              "calls": '; '.join(dict.fromkeys(calls)), "incoming_sites": 0,
                              "review_status": "静态调用已索引；行为未逐项验收"})
        if '/world/inventory/' in rel and 'Menu' in stem:
            menu_rows.append({"source_mod": mod, "class": stem, "source_file": rel,
                              "add_slot_statements": len(re.findall(r'\baddSlot\s*\(', masked)),
                              "procedure_calls": '; '.join(dict.fromkeys(calls)),
                              "note": "addSlot 为语句次数，循环次数与最终槽位数须查构造器"})
        if stem != PREFIX[mod] + 'Variables' and any(p + 'Variables' in text for p in PREFIX.values()):
            for match in field_re.finditer(masked):
                tail = masked[match.end():match.end() + 8]
                action = 'write' if re.match(r'\s*(?:=(?!=)|[+\-*/]=|\+\+|--)', tail) else 'read_or_access'
                access_rows.append({"source_mod": mod, "source_file": rel, "line": line(match.start()),
                                    "field": match[1], "access": action,
                                    "evidence": "同名成员访问且文件引用 Variables；接收对象待核"})
        for match in re.finditer(r'\.(get(?:Double|Int|String|Boolean|Compound|Long|Float)|put(?:Double|Int|String|Boolean|Long|Float)?)\(\s*"([^"\n]+)"', text):
            nbt_rows.append({"source_mod": mod, "source_file": rel, "line": line(match.start()),
                             "operation": match[1], "key": match[2], "evidence": "字面量键调用；具体容器归属待核"})
    for row in proc_rows:
        row['incoming_sites'] = incoming[(row['source_mod'], row['class'])]
    accesses = defaultdict(list)
    for row in access_rows:
        accesses[row['field']].append(row)
    for row in state_rows:
        candidates = accesses[row['name']]
        for label, selection in (('write', [r for r in candidates if r['access'] == 'write']), ('read', [r for r in candidates if r['access'] != 'write'])):
            row[f'{label}_candidate_sites'] = len(selection)
            row[f'{label}_examples'] = '; '.join(f"{r['source_mod']}/{r['source_file']}:{r['line']}" for r in selection[:2])
    key_groups = defaultdict(list)
    for row in nbt_rows:
        key_groups[(row['source_mod'], row['key'])].append(row)
    key_catalog = []
    for (mod, key), sites in sorted(key_groups.items()):
        key_catalog.append({'source_mod': mod, 'key': key,
                            'operations': '; '.join(sorted({r['operation'] for r in sites})),
                            'read_sites': sum(r['operation'].startswith('get') for r in sites),
                            'write_sites': sum(r['operation'].startswith('put') for r in sites),
                            'source_example': '; '.join(f"{r['source_file']}:{r['line']}" for r in sites[:2]),
                            'identity': f'{mod}/{key}',
                            'evidence': 'NBT 或字典字面量键候选；未按接收对象分离，同名不代表同一状态'})
    summary = {"source_version": "1.18.2", "target_version": "1.21.1", "sources": source_meta,
               "state_count": len(state_rows), "constant_count": len(constants), "registration_count": len(registrations),
               "java_count": len(files), "procedure_count": len(proc_rows), "call_sites": len(call_rows),
               "menu_classes": len(menu_rows), "field_access_candidates": len(access_rows), "nbt_key_candidates": len(nbt_rows), "literal_key_groups": len(key_catalog),
               "shared_state_names": sorted(n for n, count in Counter(r['name'] for r in state_rows).items() if count > 1),
               "procedure_module_candidates": dict(Counter(r['candidate_module'] for r in proc_rows)),
               "limitations": ["分组来自命名匹配，未人工确认。", "成员访问为候选证据，未作 Java 类型解析。", "无静态调用者不证明不可达；事件、命令、反射与生成入口需另核。", "声明次序仅是表格排列，不能充当原有编号。", "字面量键包含 NBT 与 Map 候选，不含动态拼接键的完整展开；同名键仍需按所属物品、实体或方块分离。"]}
    outputs = {'variables_detailed.csv': state_rows, 'storage_constants.csv': constants,
               'content_catalog.csv': registrations, 'java_files.csv': files, 'procedure_calls.csv': call_rows,
               'procedures.csv': proc_rows, 'menu_classes.csv': menu_rows, 'field_accesses.csv': access_rows,
               'nbt_key_calls.csv': nbt_rows, 'literal_key_catalog.csv': key_catalog}
    for name, rows in outputs.items():
        (dest / name).write_text(csv_text(rows), encoding='utf-8', newline='\n')
    (dest / 'variables_summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    payload = {"summary": summary, "variables": state_rows, "constants": constants, "content": registrations,
               "procedures": proc_rows, "menus": menu_rows, "literal_keys": key_catalog}
    (dest / 'workbook_data.json').write_text(json.dumps(payload, ensure_ascii=False), encoding='utf-8')
    print(json.dumps({k: v for k, v in summary.items() if k not in ('sources', 'limitations', 'procedure_module_candidates')}, ensure_ascii=False))


if __name__ == '__main__':
    main()
