"""提取副本定义与阵法配方为 JSON 数据表，修正 Q81/Q109/Q04，并离线升级相关 .nbt 结构移除套壳子复合与魔法分隔串。"""
from __future__ import annotations

import json
from pathlib import Path
import re
import nbtlib

ROOT = Path(__file__).resolve().parents[2]
RESOURCES = ROOT / "src/main/resources"
STRUCTURES = RESOURCES / "data/yvanchuxiuzhen/structure"
DUNGEON_JSON = RESOURCES / "content/dungeon/dungeons.json"
FORMATION_JSON = RESOURCES / "content/zhenfa/formations.json"

ZHENYAOTA_OFFSET = (1268, 63, -600)


def remap(value: str) -> str:
    return value.replace("yvanchuxitong:", "yvanchuxiuzhen:")


def parse_number(value: str) -> float:
    try:
        return float(value.strip())
    except ValueError:
        return 0.0


def parse_reward(raw: str) -> dict:
    fields = raw.split("/")
    item_id = remap(fields[0].lower())
    custom_name = "" if fields[1] == "无" else fields[1]
    weight = int(parse_number(fields[2]))
    count = int(parse_number(fields[3]))

    numbers: dict[str, float] = {}
    texts: dict[str, str] = {}
    cursor = 4
    while cursor + 2 < len(fields):
        key = fields[cursor]
        kind = fields[cursor + 1]
        val = fields[cursor + 2]
        if kind in ("char", "str"):
            texts[key] = val
        elif kind == "int":
            numbers[key] = float(int(parse_number(val)))
        elif kind in ("double", "doube"):
            numbers[key] = parse_number(val)
        cursor += 3

    if "jinengName" in texts:
        payload = {
            "type": "skill",
            "name": texts["jinengName"],
            "grade": numbers.get("belongTopinjie", 0.0),
            "level": numbers.get("belongToLevel", 0.0),
            "body": texts.get("belongTobody", ""),
            "intro": texts.get("belongTojianjie", ""),
            "numbers": numbers,
            "texts": texts,
        }
    elif "tuzhiname" in texts:
        ingredients = []
        for idx in range(9):
            k = f"cailiao0{idx}"
            ingredients.append({
                "id": remap(texts.get(k, "")),
                "name": texts.get(k + "name", ""),
                "count": int(numbers.get(k + "num", 0)),
            })
        payload = {
            "type": "blueprint",
            "name": texts["tuzhiname"],
            "description": texts.get("tuzhishuoming", ""),
            "output_item_id": remap(texts.get("fabaoid", "")),
            "output_description": texts.get("fabaoshuoming", ""),
            "output_type": texts.get("fabaotype", ""),
            "item_level_text": texts.get("item_level", ""),
            "item_level_num": int(numbers.get("item_level_num", 0)),
            "ingredients": ingredients,
        }
    elif "danyaoname" in texts:
        ingredients = []
        for idx in range(1, 9):
            k = f"cailiao0{idx}"
            ingredients.append({
                "id": remap(texts.get(k, "")),
                "name": texts.get(k + "name", ""),
            })
        payload = {
            "type": "medicine_recipe",
            "name": texts["danyaoname"],
            "recipe_description": texts.get("danfangshuoming", ""),
            "medicine_description": texts.get("danyaoshuoming", ""),
            "item_level_text": texts.get("item_level", ""),
            "item_level_num": int(numbers.get("item_level_num", 0)),
            "ingredients": ingredients,
        }
    elif "TreasureTypeBig" in texts:
        payload = {
            "type": "treasure",
            "category": texts["TreasureTypeBig"],
            "subtype": texts.get("TreasureTypeSmall", ""),
            "level": numbers.get("TreasureLEVEL", 0.0),
            "minimum": numbers.get("minimumvalue", 0.0),
            "maximum": numbers.get("Maximumvalue", 0.0),
        }
    else:
        payload = {
            "type": "item",
            "material_level": int(numbers.get("cailiaolevel", 0)),
            "material_grade": int(numbers.get("cailiaopinjie", 0)),
        }

    return {
        "item_id": item_id,
        "custom_name": custom_name,
        "weight": weight,
        "count": count,
        "payload": payload,
    }


def parse_dungeon_definition(def_id: str, tower_pos: list[int] | None, raw: str, q81_fallback: str | None) -> dict:
    if tower_pos == [1278, 83, -587] and "%" not in raw:
        assert q81_fallback is not None, "Missing Q81 template in zhenyaota.nbt"
        q81_parts = q81_fallback.split("%")
        # 保留 ZhenyaotashengchengbuchongProcedure 的奖励清单，补齐 zhenyaota.nbt 同点位原本的头（山中无老虎%3%480）与三波刷怪配置。
        raw = "%".join(q81_parts[:3] + [raw] + q81_parts[4:])

    parts = raw.split("%")
    parts = [
        p.replace("降低30//jinengneeditem/", "降低30%/jinengneeditem/")
        .replace("/jinengjieyin/int//", "/jinengjieyin/int/0/")
        for p in parts
    ]
    name = parts[0]
    wave_count = int(parse_number(parts[1]))
    time_limit = int(parse_number(parts[2]))
    gift_str = parts[3]

    rewards = [parse_reward(g) for g in gift_str.split("&")] if gift_str else []

    cursor = 4
    waves = []
    for _ in range(wave_count):
        mob_count = int(parse_number(parts[cursor]))
        cursor += 1
        spawns = []
        for _ in range(mob_count):
            spawns.append({
                "entity_id": remap(parts[cursor]),
                "level": int(parse_number(parts[cursor + 1])),
                "marker_id": parts[cursor + 2],
            })
            cursor += 3
        waves.append(spawns)
    assert cursor == len(parts), f"{def_id}: cursor {cursor} != {len(parts)}"

    row: dict = {
        "id": def_id,
        "name": name,
        "time_limit": time_limit,
    }
    if tower_pos is not None:
        row["tower_pos"] = tower_pos
    row["rewards"] = rewards
    row["waves"] = waves
    return row


def extract_dungeons() -> list[dict]:
    procedures_dir = (
        ROOT.parent
        / "yuanchuxiuzhen-1.21.1/reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen/src/main/java/net/mcreator/yvanchuxiuzhen/procedures"
    )
    default_source = (procedures_dir / "TiandaofubenFangZhiFangKuaiShiProcedure.java").read_text(encoding="utf-8")
    tower_source = (procedures_dir / "ZhenyaotashengchengbuchongProcedure.java").read_text(encoding="utf-8")
    literal = r'"(?:[^"\\]|\\.)*"'
    m_def = re.search(r'putString\("fubenshujv",\s*(' + literal + r')\)', default_source)
    assert m_def, "Missing default fubenshujv in TiandaofubenFangZhiFangKuaiShiProcedure.java"
    default_str = json.loads(m_def[1])

    pattern = r'BlockPos _bp = new BlockPos\((-?\d+), (-?\d+), (-?\d+)\);.*?putString\("fubenshujv",\s*(' + literal + r')\)'
    towers = [(int(x), int(y), int(z), json.loads(value)) for x, y, z, value in re.findall(pattern, tower_source, re.S)]
    assert len(towers) == 44, f"Expected 44 towers, got {len(towers)}"

    zhenyaota_tag = nbtlib.load(STRUCTURES / "zhenyaota.nbt")
    q81_fallback = None
    ox, oy, oz = ZHENYAOTA_OFFSET
    for b in zhenyaota_tag["blocks"]:
        be = b.get("nbt")
        if be and str(be.get("id", "")) == "yvanchuxiuzhen:tiandaofuben":
            pos = [int(v) for v in b["pos"]]
            if (pos[0] + ox, pos[1] + oy, pos[2] + oz) == (1278, 83, -587):
                state = be.get("dungeon_state", be)
                if "fubenshujv" in state:
                    q81_fallback = str(state["fubenshujv"])
                break
    if q81_fallback is None and DUNGEON_JSON.exists():
        for row in json.loads(DUNGEON_JSON.read_text(encoding="utf-8")):
            if row.get("id") == "tower_1278_83_-587":
                parts = [row["name"], str(len(row["waves"])), str(row["time_limit"]), ""]
                for wave in row["waves"]:
                    parts.append(str(len(wave)))
                    for s in wave:
                        parts.extend([s["entity_id"], str(s["level"]), s["marker_id"]])
                q81_fallback = "%".join(parts)
                break

    rows = [parse_dungeon_definition("default", None, default_str, None)]
    for x, y, z, raw in towers:
        did = f"tower_{x}_{y}_{z}"
        rows.append(parse_dungeon_definition(did, [x, y, z], raw, q81_fallback))
    return rows


def extract_formations() -> list[dict]:
    recipes = []
    yang_mult = [0.5, 0.5, 1.0, 2.0]
    yin_mult = [0.1, 0.5, 1.2, 1.5]
    for stage in range(1, 5):
        recipes.append({
            "code": f"00000{stage}",
            "name": f"阳{stage}",
            "category": "阳",
            "cost_multiplier": yang_mult[stage - 1],
        })
    for stage in range(1, 5):
        recipes.append({
            "code": f"00000-{stage}",
            "name": f"阴{stage}",
            "category": "阴",
            "cost_multiplier": yin_mult[stage - 1],
        })
    space_codes = ["060040", "002640", "222220", "333330", "555554"]
    space_mult = [0.1, 0.2, 0.1, 2.0, 8.0]
    for idx, code in enumerate(space_codes):
        recipes.append({
            "code": code,
            "name": f"空间{idx + 1}",
            "category": "空间",
            "cost_multiplier": space_mult[idx],
        })
    elements = ["金", "水", "木", "火", "土"]
    # Q04 修复：火2类别修正为 "火"（原被误写为 "金"），五行阵补充阶梯耗能倍率 [0.5, 1.0, 1.5, 2.0]。
    wuxing_mult = [0.5, 1.0, 1.5, 2.0]
    for e_idx, category in enumerate(elements):
        for stage in range(1, 5):
            digits = ["0"] * 6
            digits[e_idx] = str(stage * 2)
            recipes.append({
                "code": "".join(digits),
                "name": f"{category}{stage}",
                "category": category,
                "cost_multiplier": wuxing_mult[stage - 1],
            })
    return recipes


def upgrade_nbt_structures() -> None:
    # 1. 阵法结构：chuansongdazhen, fanhuacheng11, huafanchengjiegou -> 拆解 formation_state 到方块实体根部。
    obsolete_formation_keys = {f"jisuan{i}" for i in range(1, 5)} | {f"jisuanzong{i}" for i in range(1, 5)}
    for name in ("chuansongdazhen.nbt", "fanhuacheng11.nbt", "huafanchengjiegou.nbt"):
        path = STRUCTURES / name
        nbt = nbtlib.load(path)
        changed = False
        for b in nbt["blocks"]:
            be = b.get("nbt")
            if not be or str(be.get("id", "")) != "yvanchuxiuzhen:zhenji":
                continue
            if "formation_state" in be:
                state = be.pop("formation_state")
                for k, v in state.items():
                    if k not in obsolete_formation_keys:
                        be[k] = v
                changed = True
        if changed:
            nbt.save(path)

    # 2. fuben002.nbt：fubenblock 与 fubenentity 拆解 dungeon_state 到根部，zhanwei1..9 转强类型 markers 列表。
    fuben002_path = STRUCTURES / "fuben002.nbt"
    nbt = nbtlib.load(fuben002_path)
    changed = False
    for b in nbt["blocks"]:
        be = b.get("nbt")
        if not be:
            continue
        bid = str(be.get("id", ""))
        if bid == "yvanchuxiuzhen:fubenentity" and "dungeon_state" in be:
            state = be.pop("dungeon_state")
            for k, v in state.items():
                be[k] = v
            changed = True
        elif bid == "yvanchuxiuzhen:fubenblock" and "dungeon_state" in be:
            state = be.pop("dungeon_state")
            markers = []
            idx = 1
            while f"zhanwei{idx}" in state:
                m = state.pop(f"zhanwei{idx}")
                markers.append(nbtlib.Compound({
                    "id": nbtlib.String(str(m.get("zhanweifuid", ""))),
                    "x": nbtlib.Int(int(m.get("entityshengchengX", 0))),
                    "y": nbtlib.Int(int(m.get("entityshengchengY", 0))),
                    "z": nbtlib.Int(int(m.get("entityshengchengZ", 0))),
                    "facing": nbtlib.Int(int(m.get("entityshengchengchaoxiang", 0))),
                    "attack": nbtlib.Byte(int(m.get("entityshengchenggongji", 0))),
                }))
                idx += 1
            for k, v in state.items():
                be[k] = v
            be["markers"] = nbtlib.List[nbtlib.Compound](markers)
            changed = True
    if changed:
        nbt.save(fuben002_path)

    # 3. zhenyaota.nbt：44 个 tiandaofuben 拆解 dungeon_state 到根部，移除 fubenshujv 魔法串，改写强引用 definition_id。
    zhenyaota_path = STRUCTURES / "zhenyaota.nbt"
    nbt = nbtlib.load(zhenyaota_path)
    ox, oy, oz = ZHENYAOTA_OFFSET
    changed = False
    for b in nbt["blocks"]:
        be = b.get("nbt")
        if not be or str(be.get("id", "")) != "yvanchuxiuzhen:tiandaofuben":
            continue
        if "dungeon_state" in be:
            state = be.pop("dungeon_state")
            for k, v in state.items():
                if k != "fubenshujv":
                    be[k] = v
            pos = [int(v) for v in b["pos"]]
            be["definition_id"] = nbtlib.String(f"tower_{pos[0] + ox}_{pos[1] + oy}_{pos[2] + oz}")
            changed = True
    if changed:
        nbt.save(zhenyaota_path)


def main() -> None:
    dungeons = extract_dungeons()
    formations = extract_formations()

    DUNGEON_JSON.parent.mkdir(parents=True, exist_ok=True)
    DUNGEON_JSON.write_text(
        "[\n" + ",\n".join(json.dumps(r, ensure_ascii=False, separators=(",", ":")) for r in dungeons) + "\n]\n",
        encoding="utf-8",
        newline="\n",
    )
    FORMATION_JSON.parent.mkdir(parents=True, exist_ok=True)
    FORMATION_JSON.write_text(
        "[\n" + ",\n".join(json.dumps(r, ensure_ascii=False, separators=(",", ":")) for r in formations) + "\n]\n",
        encoding="utf-8",
        newline="\n",
    )
    upgrade_nbt_structures()
    print(f"Extracted {len(dungeons)} dungeon definitions, {len(formations)} formation recipes, and upgraded 5 .nbt structures.")


if __name__ == "__main__":
    main()
