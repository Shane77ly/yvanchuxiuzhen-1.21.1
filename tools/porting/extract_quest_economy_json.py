#!/usr/bin/env python3
"""
阶段 2B 离线数据迁移工具：
1. 将商店目录、拍卖清单、估价表、全部对话/任务树与结构预设提取为强类型 JSON（零 % / 拆串）。
2. 将 14 个内置 .nbt 结构模板及 spawn_data.json 中的 NPC/伪装方块脚本与商店离线升级为强类型 ID。
3. 清理已迁入强类型 JSON 的旧 % / 脚本与商品语言键及 commerce_catalog.json。
"""
from __future__ import annotations

import json
import pathlib
import re
from typing import Any
import nbtlib
from equipment_commands import equipment_command

ROOT = pathlib.Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "src/main/resources"
STRUCT_DIR = RESOURCES / "data/yvanchuxiuzhen/structure"

NBT_FILES = [
    "chongguanhecun", "fanhuacheng03", "fanhuacheng04", "fanhuacheng08",
    "fanhuacheng09", "fanhuacheng11", "fanhuacheng12", "huafanchengjiegou",
    "konggucanjvan06", "yewaiyingdi", "yongbingyingdi", "yvnqingpai",
    "zhaowunpc", "zhenyaota",
]

TECHNIQUE_TIER_KEYS = [
    "TechniqueHealth", "TechniqueHeathRec", "Techniquepower", "TechniquepowerRec",
    "TechniqueSoul", "TechniqueSoulRec", "TechniqueAttack", "TechniqueAttackDef",
    "TechniqueMana", "TechniqueManaDef", "TechniqueSouatt", "TechniqueSouDef",
    "TechniqueSpeed",
]

ELEMENT_KEY_MAP = {
    "ExercisesGold": "gold",
    "ExercisesWater": "water",
    "ExercisesTree": "tree",
    "ExercisesFire": "fire",
    "ExercisesSoil": "soil",
}


def norm_ns(s: str) -> str:
    return s.replace("yvanchuxitong:", "yvanchuxiuzhen:") if s.startswith("yvanchuxitong:") else s


def parse_double(val: str) -> float:
    cleaned = re.sub(r"[^0-9.\-EefFdD]", "", val)
    return float(cleaned) if cleaned else 0.0


def parse_int(val: str) -> int:
    cleaned = re.sub(r"[^0-9\-]", "", val.split(".")[0])
    return int(cleaned) if cleaned else 0


def parse_block_pos(x_s: str, y_s: str, z_s: str) -> list[int]:
    return [int(float(x_s)), int(float(y_s)), int(float(z_s))]


def extract_kv_triples(tokens: list[str]) -> list[tuple[str, str, str]]:
    triples: list[tuple[str, str, str]] = []
    i = 0
    while i + 2 < len(tokens):
        triples.append((tokens[i], tokens[i + 1], tokens[i + 2]))
        i += 3
    if i < len(tokens) and tokens[i] != "无":
        raise ValueError(f"Unexpected trailing attribute tokens: {tokens[i:]}")
    return triples


def parse_item_payload(item_id: str, triples: list[tuple[str, str, str]]) -> dict[str, Any]:
    if not triples:
        return {"kind": "none"}
    kv = {k: (t, v) for k, t, v in triples}
    keys = set(kv.keys())

    # 1. Casket command item (zhiling)
    if "zhiling" in kv:
        return {"kind": "casket_command", "command": equipment_command(kv["zhiling"][1])}

    # 2. Material level/quality
    if "cailiaolevel" in kv or "cailiaopinjie" in kv:
        return {
            "kind": "material",
            "level": parse_int(kv.get("cailiaolevel", ("int", "0"))[1]),
            "quality": parse_int(kv.get("cailiaopinjie", ("int", "0"))[1]),
        }

    # 3. Cooking recipe
    if "MenuName" in kv:
        ingredients = []
        for idx in range(9):
            key = f"cailiao{idx:02d}"
            if key in kv and kv[key][1] and kv[key][1] != "无":
                ingredients.append(kv[key][1])
        return {
            "kind": "cooking_recipe",
            "recipe": {
                "menu_name": kv["MenuName"][1],
                "grade": parse_double(kv.get("belongTopinjie", ("int", "0"))[1]),
                "grade_text": kv.get("belongTopinjietext", ("char", ""))[1],
                "description": kv.get("belongTojianjie", ("char", ""))[1],
                "animation": parse_int(kv.get("cookanimation", ("int", "0"))[1]),
                "needed_container": kv.get("needcontainer", ("char", ""))[1],
                "output_id": kv.get("Menuid", ("char", ""))[1],
                "pot_type": kv.get("MenuType", ("char", ""))[1],
                "ingredients": ingredients,
            },
        }

    # 4. Technique book
    if "ExercisesName" in kv:
        elements: dict[str, float] = {}
        for src_k, dst_k in ELEMENT_KEY_MAP.items():
            if src_k in kv:
                elements[dst_k] = parse_double(kv[src_k][1])
        tiers = [kv.get(k, ("char", "最下档"))[1] for k in TECHNIQUE_TIER_KEYS]
        return {
            "kind": "technique",
            "item": kv.get("belongToItemID", ("char", item_id))[1],
            "name": kv["ExercisesName"][1],
            "grade_text": kv.get("belongTopinjietext", ("char", ""))[1],
            "grade": parse_double(kv.get("belongTopinjie", ("int", "0"))[1]),
            "level_text": kv.get("belongToLeveltext", ("char", ""))[1],
            "level": parse_double(kv.get("belongToLevel", ("int", "0"))[1]),
            "body": kv.get("belongTobody", ("char", "无"))[1],
            "intro": kv.get("belongTojianjie", ("char", ""))[1],
            "elements": elements,
            "tiers": tiers,
        }

    # 5. Skill book (jineng / smanship / shenfa / flyskill)
    skill_kind = None
    name_key = None
    for sk, nk in [("jineng", "jinengName"), ("smanship", "smanshipName"), ("shenfa", "shenfaName"), ("flyskill", "flyskillName")]:
        if nk in kv:
            skill_kind = sk
            name_key = nk
            break
    if skill_kind is not None:
        common_keys = {
            name_key, "belongToItemID", "belongTopinjietext", "belongTopinjie",
            "belongToLeveltext", "belongToLevel", "belongTobody", "belongTojianjie",
        }
        numbers: dict[str, float] = {}
        texts: dict[str, str] = {}
        for k, (t, v) in kv.items():
            if k in common_keys:
                continue
            if t in ("int", "double", "doube"):
                numbers[k] = parse_double(v)
            else:
                texts[k] = v
        return {
            "kind": "skill",
            "skill_kind": skill_kind,
            "name": kv[name_key][1],
            "grade": parse_double(kv.get("belongTopinjie", ("int", "0"))[1]),
            "level": parse_double(kv.get("belongToLevel", ("int", "0"))[1]),
            "body": kv.get("belongTobody", ("char", "无"))[1],
            "intro": kv.get("belongTojianjie", ("char", ""))[1],
            "numbers": numbers,
            "texts": texts,
        }

    # 6. Fulu learning book
    if "fuluget" in kv or "fulumingling" in kv:
        return {
            "kind": "fulu_learning",
            "learnable": kv.get("fuluget", ("logic", "真"))[1] in ("真", "true", "True", "1"),
            "command": kv.get("fulumingling", ("char", ""))[1],
        }

    # 7. Finished fulu talisman
    if "fululevel" in kv or "fuluid" in kv:
        return {
            "kind": "fulu",
            "level": parse_int(kv.get("fululevel", ("int", "1"))[1]),
            "level_text": kv.get("fululeveltxt", ("char", ""))[1],
        }

    # 8. Medicine (finished pill)
    if "danyaoid" in kv and "danyaoxiaoguo0" in kv:
        effects = []
        idx = 0
        while f"danyaoxiaoguo{idx}" in kv:
            effects.append({
                "id": kv[f"danyaoxiaoguo{idx}"][1],
                "level": parse_int(kv.get(f"danyaolevel{idx}", ("int", "1"))[1]),
                "duration": parse_int(kv.get(f"danyaotick{idx}", ("int", "0"))[1]),
            })
            idx += 1
        return {
            "kind": "medicine",
            "name": kv.get("danyaoname", ("char", ""))[1],
            "dose": parse_int(kv.get("danyaonum", ("int", "1"))[1]),
            "pack_count": parse_int(kv.get("shuliang", ("int", "1"))[1]),
            "treasure": False,
            "effects": effects,
        }

    # 9. Medicine recipe
    if "danfangid" in kv or "danfangshuoming" in kv:
        ingredients = []
        for idx in range(1, 10):
            id_key = f"cailiao{idx:02d}"
            name_key = f"cailiao{idx:02d}name"
            if id_key in kv:
                ingredients.append({
                    "id": kv[id_key][1],
                    "name": kv.get(name_key, ("char", ""))[1],
                })
        return {
            "kind": "recipe",
            "medicine_name": kv.get("danyaoname", ("char", ""))[1],
            "recipe_description": kv.get("danfangshuoming", ("char", ""))[1],
            "medicine_description": kv.get("danyaoshuoming", ("char", ""))[1],
            "level_text": kv.get("item_level", ("char", ""))[1],
            "level": parse_int(kv.get("item_level_num", ("int", "1"))[1]),
            "recipe_id": kv.get("danfangid", ("char", ""))[1],
            "ingredients": ingredients,
        }

    # 10. Lianqi blueprint
    if "fabaoid" in kv or "tuzhiname" in kv:
        ingredients = []
        for idx in range(0, 9):
            id_key = f"cailiao{idx:02d}"
            if id_key in kv:
                ingredients.append({
                    "id": kv[id_key][1],
                    "name": kv.get(f"{id_key}name", ("char", ""))[1],
                    "count": parse_int(kv.get(f"{id_key}num", ("int", "1"))[1]),
                })
        return {
            "kind": "blueprint",
            "blueprint_name": kv.get("tuzhiname", ("char", ""))[1],
            "blueprint_description": kv.get("tuzhishuoming", ("char", ""))[1],
            "artifact_id": kv.get("fabaoid", ("char", ""))[1],
            "artifact_description": kv.get("fabaoshuoming", ("char", ""))[1],
            "artifact_type": kv.get("fabaotype", ("char", ""))[1],
            "level_text": kv.get("item_level", ("char", ""))[1],
            "level": parse_int(kv.get("item_level_num", ("int", "1"))[1]),
            "belong_to_item_id": kv.get("belongToItemID", ("char", item_id))[1],
            "ingredients": ingredients,
        }

    # 11. Book text display
    if "textdisplay" in kv or "bookname" in kv:
        return {"kind": "book_text", "text": kv.get("textdisplay", ("char", ""))[1]}

    # 12. Treasure container
    if "baoxiangname" in kv or "baoxiangtype" in kv:
        return {
            "kind": "treasure_container",
            "category": kv.get("baoxiangtype", ("char", ""))[1],
            "subtype": kv.get("baoxiangname", ("char", ""))[1],
            "level": parse_double(kv.get("baoxianglevel", ("double", "0"))[1]),
            "minimum": parse_double(kv.get("baoxiangmin", ("double", "0"))[1]),
            "maximum": parse_double(kv.get("baoxiangmax", ("double", "0"))[1]),
        }

    # 13. Non-component legacy attack_num on ling_12 (ignored by Stage 1 component model)
    if keys == {"attack_num"}:
        return {"kind": "none"}

    raise ValueError(f"Unmapped payload keys for {item_id}: {keys} -> {triples}")


def parse_shop(shop_id: str, raw_info: str, raw_products: str) -> dict[str, Any]:
    info_parts = raw_info.split("%") if raw_info else []
    info = {
        "store_name": info_parts[0] if len(info_parts) > 0 else "",
        "owner_name": info_parts[1] if len(info_parts) > 1 else "",
        "description": info_parts[2] if len(info_parts) > 2 else "",
    }
    products = []
    if raw_products:
        tokens = raw_products.split("%")
        if len(tokens) % 5 != 0:
            raise ValueError(f"Shop {shop_id} product tokens not divisible by 5: {len(tokens)}")
        for i in range(0, len(tokens), 5):
            item_s, display_s, currency_s, price_s, attr_s = tokens[i:i + 5]
            item_s = norm_ns(item_s)
            triples = [] if (not attr_s or attr_s == "无") else extract_kv_triples(attr_s.split("/"))
            payload = parse_item_payload(item_s, triples)
            entry: dict[str, Any] = {
                "item": item_s,
                "currency": currency_s,
                "price": parse_double(price_s),
            }
            if display_s and display_s != "无":
                entry["display_name"] = display_s
            if payload["kind"] != "none":
                entry["payload"] = payload
            products.append(entry)
    return {"id": shop_id, "info": info, "products": products}


def parse_dialogue_node(step: str) -> dict[str, Any]:
    p = step.split("/")
    op = p[0]
    if op == "文字":
        return {"kind": "text", "player_speak": False, "text": p[1]}
    if op == "自发文字":
        return {"kind": "text", "player_speak": True, "text": p[1]}
    if op == "选项":
        prompt = p[1]
        count = int(p[2])
        options = []
        for idx in range(count):
            options.append({"label": p[3 + idx * 2], "target": int(p[4 + idx * 2])})
        return {"kind": "choice", "prompt": prompt, "options": options}
    if op == "判断物品":
        return {
            "kind": "check_item",
            "item": norm_ns(p[1]),
            "count": parse_int(p[2]),
            "pass_target": int(p[3]),
            "fail_target": int(p[4]),
        }
    if op == "高级物品判断":
        item = norm_ns(p[1])
        count = parse_int(p[2])
        pass_target = int(p[3])
        fail_target = int(p[4])
        display_name = p[5]
        triples = extract_kv_triples(p[6:]) if len(p) > 6 and p[6] != "无" else []
        node: dict[str, Any] = {
            "kind": "check_advanced_item",
            "item": item,
            "count": count,
            "display_name": display_name,
            "pass_target": pass_target,
            "fail_target": fail_target,
        }
        if triples:
            node["filters"] = [{"key": k, "field_type": t, "value": v} for k, t, v in triples]
        return node
    if op == "判断任务":
        return {
            "kind": "check_quest",
            "quest_id": p[1],
            "completed_target": int(p[2]),
            "failed_target": int(p[3]),
            "otherwise_target": int(p[4]),
        }
    if op == "判断当前坐标":
        return {
            "kind": "check_pos",
            "x": parse_double(p[1]),
            "y": parse_double(p[2]),
            "z": parse_double(p[3]),
            "size": parse_double(p[4]),
            "inside_target": int(p[5]),
            "outside_target": int(p[6]),
        }
    if op == "判断当前维度":
        return {
            "kind": "check_dimension",
            "dimension": p[1],
            "match_target": int(p[2]),
            "mismatch_target": int(p[3]),
        }
    if op in ("判断nbt", "判断数值"):
        node = {
            "kind": "check_flag",
            "literal": op == "判断数值",
            "field_type": p[1],
            "key": p[2],
            "target_or_literal": p[3],
            "first_target": int(p[4]),
            "second_target": int(p[5]),
        }
        if len(p) > 6:
            node["equal_target"] = int(p[6])
        return node
    if op == "改变nbt":
        return {"kind": "modify_flag", "field_type": p[1], "key": p[2], "op": p[3], "value": p[4]}
    if op == "物品":
        give = p[1] == "加"
        item = norm_ns(p[2])
        count = parse_int(p[3])
        node = {"kind": "item_action", "give": give, "item": item, "count": count}
        if len(p) > 4 and p[4] != "无":
            if give:
                rest = p[4:]
                if len(rest) >= 3 and len(rest[0]) == 1 and rest[1] == "name":
                    node["color_code"] = rest[0]
                    node["display_name"] = rest[2]
                    rest = rest[3:]
                if rest and rest[0] != "无":
                    payload = parse_item_payload(item, extract_kv_triples(rest))
                    if payload["kind"] != "none":
                        node["payload"] = payload
            else:
                if len(p) > 5 and p[4] == "name":
                    node["display_name"] = p[5]
                    rest = p[6:]
                else:
                    rest = p[4:]
                if rest and rest[0] != "无":
                    node["filters"] = [{"key": k, "field_type": t, "value": v} for k, t, v in extract_kv_triples(rest)]
        return node
    if op == "跳转":
        return {"kind": "jump", "target": int(p[1])}
    if op == "任务进度更改":
        return {"kind": "set_quest_status", "status": p[1]}
    if op == "任务储存":
        return {"kind": "save_quest", "resume_cursor": int(p[1])}
    if op == "任务设定修改":
        return {"kind": "modify_quest_header", "field": p[1], "value": p[2]}
    if op == "打开界面":
        return {"kind": "open_menu", "menu": p[1]}
    if op == "改变坐标":
        return {"kind": "teleport", "x": parse_double(p[1]), "y": parse_double(p[2]), "z": parse_double(p[3])}
    if op == "删除方块":
        coords = p[1:]
        positions = [parse_block_pos(coords[i], coords[i + 1], coords[i + 2]) for i in range(0, len(coords), 3)]
        return {"kind": "remove_blocks", "positions": positions}
    if op == "裂隙解封":
        return {"kind": "unseal_rift"}
    if op in ("跟随玩家", "取消跟随", "删除实体", "伤害实体", "移动到坐标"):
        node = {
            "kind": "entity_control",
            "op": op,
            "entity_type": p[1],
            "x": parse_double(p[2]),
            "y": parse_double(p[3]),
            "z": parse_double(p[4]),
            "radius": parse_double(p[5]),
        }
        if len(p) > 6:
            node["extra_args"] = p[6:]
        return node
    if op == "药剂效果":
        node = {"kind": "effect", "op": p[1], "effect_id": p[2]}
        if len(p) > 3:
            node["amplifier"] = int(float(p[3]))
        if len(p) > 4:
            node["duration"] = int(float(p[4]))
        return node
    if op == "指令":
        return {"kind": "command", "command": "/".join(p[1:])}
    if op == "任务转接":
        return {"kind": "transfer_quest", "target_dialogue_id": p[1]}
    if op == "文本插入选项":
        return {"kind": "insert_chat_option", "prefix": p[1], "option": p[2], "suffix": p[3], "target": int(p[4])}
    if op == "结束":
        return {"kind": "end"}
    raise ValueError(f"Unknown dialogue opcode: {op} in step: {step}")


def parse_quest(template_id: str, raw_script: str) -> dict[str, Any]:
    sections = raw_script.split("%")
    header = sections[0].split("/")
    if len(header) < 7:
        raise ValueError(f"Quest {template_id} header has < 7 parts: {header}")
    return {
        "id": template_id,
        "quest_id": header[1],
        "title": header[0],
        "description": header[2],
        "initial_progress": int(header[3]) if header[3].lstrip("-").isdigit() else 0,
        "initial_status": header[4],
        "requirement": header[5],
        "reward_summary": header[6],
        "nodes": [parse_dialogue_node(step) for step in sections[1:]],
    }


def extract_java_string(path: pathlib.Path, pattern: str) -> str:
    src = path.read_text(encoding="utf-8")
    m = re.search(pattern, src, re.DOTALL)
    if not m:
        raise RuntimeError(f"Pattern not found in {path}: {pattern}")
    return m.group(1)


def main() -> None:
    shops: dict[str, dict[str, Any]] = {}
    shop_dedup: dict[tuple[str, str], str] = {}

    def register_shop(shop_id: str, raw_info: str, raw_products: str, allow_dedup: bool = False) -> str:
        key = (raw_info, raw_products)
        if allow_dedup and key in shop_dedup:
            return shop_dedup[key]
        parsed = parse_shop(shop_id, raw_info, raw_products)
        if shop_id in shops and shops[shop_id] != parsed:
            raise ValueError(f"Conflicting shop definition for {shop_id}")
        shops[shop_id] = parsed
        shop_dedup.setdefault(key, shop_id)
        return shop_id

    quests: dict[str, dict[str, Any]] = {}
    quest_dedup: dict[str, str] = {}

    def register_quest(template_id: str, raw_script: str, allow_dedup: bool = False) -> str:
        if allow_dedup and raw_script in quest_dedup:
            return quest_dedup[raw_script]
        parsed = parse_quest(template_id, raw_script)
        if template_id in quests and quests[template_id] != parsed:
            raise ValueError(f"Conflicting quest definition for {template_id}")
        quests[template_id] = parsed
        quest_dedup.setdefault(raw_script, template_id)
        return template_id

    # --- 1. Static shops & valuation ---
    catalog_path = RESOURCES / "economy/commerce_catalog.json"
    econ_lang_path = RESOURCES / "economy/language_zh_cn.json"
    commerce_catalog = json.loads(catalog_path.read_text(encoding="utf-8")) if catalog_path.exists() else {}
    econ_lang = json.loads(econ_lang_path.read_text(encoding="utf-8")) if econ_lang_path.exists() else {}
    valuation = json.loads((RESOURCES / "economy/valuation.json").read_text(encoding="utf-8"))

    # Convert herbs, talismans, basic_prices cleanly into valuation.json (idempotent)
    if isinstance(valuation.get("herbs"), dict):
        herbs_obj = valuation["herbs"]
    else:
        herb_parts = valuation["herbs"].split("%")
        herbs_obj = {norm_ns(herb_parts[i]): float(herb_parts[i + 1]) for i in range(0, len(herb_parts) - 1, 2)}

    if isinstance(valuation.get("talismans"), dict):
        talismans_obj = valuation["talismans"]
    else:
        talisman_parts = valuation["talismans"].split("%")
        talismans_obj = {}
        for entry in talisman_parts:
            k, v = entry.split("/", 1)
            talismans_obj[norm_ns(k)] = float(v)

    if isinstance(valuation.get("basic_prices"), dict) and not (RESOURCES / "economy/commerce_catalog.json").exists():
        basic_prices_obj = valuation["basic_prices"]
    else:
        bp_parts = commerce_catalog["basic_prices"].split("/")
        basic_prices_obj = {norm_ns(bp_parts[i]): float(bp_parts[i + 1]) for i in range(0, len(bp_parts) - 1, 2)}

    valuation["herbs"] = herbs_obj
    valuation["talismans"] = talismans_obj
    valuation["basic_prices"] = basic_prices_obj
    (RESOURCES / "economy/valuation.json").write_text(json.dumps(valuation, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    # Core shops
    register_shop("bank", commerce_catalog["bank_information"], commerce_catalog["bank_products"])
    for key in ["npc02", "npc03", "npc05", "npc06", "npc07", "npccaishenye"]:
        register_shop(
            key,
            econ_lang.get(f"txt.shopdescribe.{key}", ""),
            econ_lang.get(f"txt.shoplist.{key}", ""),
        )
    ores_raw = extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/npc/NpcInteractions.java",
        r'ORES = "([^"]+)"',
    )
    register_shop("npc09_ores", "矿石商人%矿石商人%在这里可以买到很多矿石材料。", ores_raw)
    register_shop("npc10_info", "中级材料商%中级材料%在此购买中级材料", "")
    register_shop("npc11_info", "高级材料商%高级材料%在此购买高级材料", "")

    refresh_src = (ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/QuestBlockRefreshData.java").read_text(encoding="utf-8")
    cloth_info = re.search(r'(?<!OLD_)CLOTHING_INFORMATION = "([^"]+)"', refresh_src).group(1)
    cloth_prod = re.search(r'CLOTHING_PRODUCTS = "([^"]+)"', refresh_src).group(1)
    register_shop("huafancheng_clothing_repaired", cloth_info, cloth_prod)

    # --- 2. Core dialogues & quests ---
    npc_zh_path = ROOT / "src/main/lang/zh_cn/npc.json"
    npc_zh = json.loads(npc_zh_path.read_text(encoding="utf-8"))
    for k, v in list(npc_zh.items()):
        if k.startswith("txt.renwulist."):
            tid = k[len("txt.renwulist."):]
            register_quest(tid, v)

    register_quest("default_shop", extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/npc/NpcInteractions.java",
        r'SHOP_SCRIPT = "([^"]+)"',
    ))
    register_quest("default_fanwu_shop", "商店任务/hvsdgfsdhfs/一个通用的商店任务。/0/未完成/无/无%打开界面/凡物出售%结束")
    register_quest("bank_dialogue", extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/api/BankDialogueSession.java",
        r'SCRIPT = "([^"]+)"',
    ))
    register_quest("ascension", extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/DialogueRuntime.java",
        r'ASCENSION_SCRIPT = "([^"]+)"',
    ))
    register_quest("inheritance", extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/DialogueRuntime.java",
        r'INHERITANCE_SCRIPT = "([^"]+)"',
    ))

    qd_src = (ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/QiankunDialogue.java").read_text(encoding="utf-8")
    intro_raw = re.search(r'SCRIPT = """\s*(.*?)\s*"""', qd_src, re.DOTALL).group(1)
    intro_lines = [line.strip() for line in intro_raw.splitlines() if line.strip()]
    register_quest("qiankun_intro", "%".join(intro_lines))
    arr_body = re.search(r'QUEST_SCRIPTS = Map\.of\((.*?)\);', qd_src, re.DOTALL).group(1)
    for m in re.finditer(r'(\d+),\s*"([^"]*)"', arr_body):
        idx = int(m.group(1))
        if m.group(2):
            register_quest(f"qiankun_{idx}", m.group(2))

    register_quest("yiqizhuatuzier", extract_java_string(
        ROOT / "src/main/java/net/yvanchuxiuzhen/system/npc/NpcStructureData.java",
        r'TAVERN_TASK = "([^"]+)"',
    ))

    # --- 3. QuestStructureData presets ---
    qsd_src = (ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/QuestStructureData.java").read_text(encoding="utf-8")
    preset_grouped: dict[tuple[str, tuple[int, int, int]], dict[str, str]] = {}
    for m in re.finditer(r'new Field\("([^"]+)",\s*new BlockPos\((-?\d+),\s*(-?\d+),\s*(-?\d+)\),\s*"([^"]+)",\s*"([^"]+)"\)', qsd_src):
        sname, x, y, z, key, val = m.group(1), int(m.group(2)), int(m.group(3)), int(m.group(4)), m.group(5), m.group(6)
        preset_grouped.setdefault((sname, (x, y, z)), {})[key] = val

    presets = []
    for (sname, (x, y, z)), fields in preset_grouped.items():
        preset: dict[str, Any] = {
            "structure_name": sname,
            "pos": [x, y, z],
        }
        if "fangxiang" in fields:
            preset["display_direction"] = fields["fangxiang"]
        if "renwulist1" in fields:
            raw_q = fields["renwulist1"]
            if raw_q.startswith("txt.renwulist."):
                preset["dialogue_id"] = raw_q[len("txt.renwulist."):]
            else:
                qid = parse_quest("_probe", raw_q)["quest_id"]
                if qid == "hvsdgfsdhfs":
                    qid = "default_shop"
                elif qid in quests and quests[qid] != parse_quest(qid, raw_q):
                    qid = f"preset_{len(presets)}_{qid}"
                preset["dialogue_id"] = register_quest(qid, raw_q, allow_dedup=True)
        if "shopinformation1" in fields or "productlist1" in fields:
            slug = f"preset_{x}_{y}_{z}".replace("-", "n")
            sid = register_shop(slug, fields.get("shopinformation1", ""), fields.get("productlist1", ""), allow_dedup=True)
            preset["shop_catalog_id"] = sid
        presets.append(preset)

    # --- 4. Upgrade all 14 .nbt structure templates & collect embedded scripts/shops ---
    for nbt_name in NBT_FILES:
        nbt_path = STRUCT_DIR / f"{nbt_name}.nbt"
        root_tag = nbtlib.load(nbt_path)
        modified = False

        for b in root_tag.get("blocks", []):
            nbt = b.get("nbt")
            if not nbt or str(nbt.get("id", "")) != "yvanchuxiuzhen:renwuweizhuangfangkuai":
                continue
            bx, by, bz = (int(v) for v in b["pos"])
            has_legacy = any(
                k.startswith(("renwulist", "tasklist", "productlist", "shopinformation"))
                for k in nbt.keys()
            )
            if not has_legacy:
                continue

            adv_ids = []
            ord_ids = []
            shop_ids = []
            for row in range(1, 11):
                r_raw = str(nbt.pop(f"renwulist{row}", ""))
                t_raw = str(nbt.pop(f"tasklist{row}", ""))
                pr_raw = str(nbt.pop(f"productlist{row}", ""))
                sh_raw = str(nbt.pop(f"shopinformation{row}", ""))

                if r_raw:
                    cand = parse_quest("_probe", r_raw)["quest_id"]
                    if cand == "hvsdgfsdhfs" and "凡物出售" in r_raw:
                        cand = "default_fanwu_shop"
                    elif cand == "hvsdgfsdhfs":
                        cand = "default_shop"
                    if cand in quests and quests[cand] != parse_quest(cand, r_raw):
                        cand = f"{nbt_name}_{bx}_{by}_{bz}_r{row}_{cand}"
                    adv_ids.append(register_quest(cand, r_raw, allow_dedup=True))
                else:
                    adv_ids.append("")

                if t_raw:
                    cand = parse_quest("_probe", t_raw)["quest_id"]
                    if cand in quests and quests[cand] != parse_quest(cand, t_raw):
                        cand = f"{nbt_name}_{bx}_{by}_{bz}_t{row}_{cand}"
                    ord_ids.append(register_quest(cand, t_raw, allow_dedup=True))
                else:
                    ord_ids.append("")

                if pr_raw or sh_raw:
                    cand_shop = f"{nbt_name}_{bx}_{by}_{bz}_s{row}".replace("-", "n")
                    shop_ids.append(register_shop(cand_shop, sh_raw, pr_raw, allow_dedup=True))
                else:
                    shop_ids.append("")

            nbt["advanced_quests"] = nbtlib.List[nbtlib.String]([nbtlib.String(x) for x in adv_ids])
            nbt["ordinary_quests"] = nbtlib.List[nbtlib.String]([nbtlib.String(x) for x in ord_ids])
            nbt["shop_ids"] = nbtlib.List[nbtlib.String]([nbtlib.String(x) for x in shop_ids])
            modified = True

        for idx, e in enumerate(root_tag.get("entities", [])):
            att = e.get("nbt", {}).get("neoforge:attachments")
            if not att or "yvanchuxiuzhen:npc_profile" not in att:
                continue
            prof = att["yvanchuxiuzhen:npc_profile"]
            new_scripts = []
            ent_changed = False
            for s_idx, raw_s in enumerate(prof.get("scripts", [])):
                rs = str(raw_s)
                if not rs:
                    new_scripts.append("")
                elif "%" in rs or "/" in rs:
                    cand = parse_quest("_probe", rs)["quest_id"]
                    if cand in quests and quests[cand] != parse_quest(cand, rs):
                        cand = f"{nbt_name}_ent{idx}_{s_idx}_{cand}"
                    new_scripts.append(register_quest(cand, rs, allow_dedup=True))
                    ent_changed = True
                else:
                    new_scripts.append(rs)

            pr_raw = str(prof.pop("products", "")) if "products" in prof else ""
            sh_raw = str(prof.pop("shop_info", "")) if "shop_info" in prof else ""
            if pr_raw or sh_raw:
                cand_shop = f"{nbt_name}_ent{idx}_shop"
                prof["shop_id"] = nbtlib.String(register_shop(cand_shop, sh_raw, pr_raw, allow_dedup=True))
                ent_changed = True

            if ent_changed:
                prof["scripts"] = nbtlib.List[nbtlib.String]([nbtlib.String(x) for x in new_scripts])
                modified = True

        if modified:
            root_tag.save(nbt_path)

    # --- 5. Upgrade spawn_data.json cleanly ---
    spawn_path = RESOURCES / "content/npc/spawn_data.json"
    spawn_list = json.loads(spawn_path.read_text(encoding="utf-8"))
    for row in spawn_list:
        eid = row["id"]
        st = row.get("state", {})
        loc = row.get("localized", {})
        if "renwulist1" in loc:
            key = loc.pop("renwulist1")
            st["dialogue_id"] = key.removeprefix("txt.renwulist.")
        if "renwulist1" in st:
            rs = st.pop("renwulist1")
            st["dialogue_id"] = register_quest(eid, rs, allow_dedup=True)
        if "productlist" in loc or "shopinformation" in loc:
            pkey = loc.pop("productlist", "")
            loc.pop("shopinformation", None)
            if pkey:
                st["shop_id"] = pkey.removeprefix("txt.shoplist.")
        if "productlist" in st or "shopinformation" in st:
            pr = st.pop("productlist", "")
            sh = st.pop("shopinformation", "")
            st["shop_id"] = register_shop(eid, sh, pr, allow_dedup=True)
    spawn_path.write_text("[\n" + ",\n".join(json.dumps(r, ensure_ascii=False, separators=(",", ":")) for r in spawn_list) + "\n]\n", encoding="utf-8")

    # --- 6. Emit typed JSON resources ---
    (RESOURCES / "content/economy/shop_catalogs.json").write_text(
        json.dumps(list(shops.values()), ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (RESOURCES / "content/economy/auction_goods.json").write_text("[]\n", encoding="utf-8")
    (RESOURCES / "content/quest/dialogues.json").write_text(
        json.dumps(list(quests.values()), ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (RESOURCES / "content/quest/structure_presets.json").write_text(
        json.dumps(presets, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )

    # --- 7. Remove obsolete script/shop lang keys & legacy catalog files ---
    for lang_rel in ["src/main/lang/zh_cn/npc.json", "src/main/lang/en_us/npc.json"]:
        lp = ROOT / lang_rel
        data = json.loads(lp.read_text(encoding="utf-8"))
        cleaned = {k: v for k, v in data.items() if not k.startswith("txt.renwulist.")}
        lp.write_text(json.dumps(cleaned, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    econ_rt_path = ROOT / "src/main/lang/zh_cn/economy_commerce_runtime.json"
    econ_rt = json.loads(econ_rt_path.read_text(encoding="utf-8"))
    cleaned_rt = {k: v for k, v in econ_rt.items() if not k.startswith(("txt.shoplist.", "txt.shopdescribe."))}
    econ_rt_path.write_text(json.dumps(cleaned_rt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

    for dead in ["economy/commerce_catalog.json", "economy/language_zh_cn.json"]:
        dp = RESOURCES / dead
        if dp.exists():
            dp.unlink()

    print(f"Extracted {len(shops)} shops, {len(quests)} dialogues, {len(presets)} structure presets.")


if __name__ == "__main__":
    main()
