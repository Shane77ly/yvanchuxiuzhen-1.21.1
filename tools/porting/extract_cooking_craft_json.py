"""将 cooking 与 craft 中硬编码的酿酒、饮酒、制墨坯、摆盘、容器、木工、石磨、缫丝、织布与陶轮配方提取为 JSON 数据表。"""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
COOKING_JSON = ROOT / "src/main/resources/content/cooking/recipes.json"
CRAFT_JSON = ROOT / "src/main/resources/content/craft/recipes.json"


def build_cooking_recipes() -> list[dict]:
    rows: list[dict] = []

    still_recipes = [
        ("yvanchuxiuzhen:jiuniang_001", {"yvanchuxiuzhen:shicai_002": 3, "yvanchuxiuzhen:ginseng": 1}),
        ("yvanchuxiuzhen:jiuniang_002", {"yvanchuxiuzhen:shicai_002": 4}),
        ("yvanchuxiuzhen:jiuniang_003", {"minecraft:wheat": 2, "yvanchuxiuzhen:shicai_005": 2}),
        ("yvanchuxiuzhen:jiuniang_004", {"yvanchuxiuzhen:jiuniang_001": 1, "yvanchuxiuzhen:fragranceflower": 3}),
        ("yvanchuxiuzhen:jiuniang_005", {"yvanchuxiuzhen:shicai_032": 4}),
        ("yvanchuxiuzhen:jiuniang_006", {"yvanchuxiuzhen:peachpeach": 4}),
        ("yvanchuxiuzhen:jiuniang_007", {"yvanchuxiuzhen:jiuniang_001": 1, "yvanchuxiuzhen:peachflower": 3}),
        ("yvanchuxiuzhen:jiuniang_008", {"yvanchuxiuzhen:jiuniang_001": 1, "yvanchuxiuzhen:xvejing": 3}),
    ]
    for output_id, ingredients in still_recipes:
        rows.append({
            "kind": "still_brewing",
            "output_id": output_id,
            "brew_ticks": 100,
            "ingredients": ingredients,
        })

    wine_drinks = [
        ("yvanchuxiuzhen:jiuniang_001", 18.0, 5.0, 0.0, None, [], [{"effect_id": "yvanchuxiuzhen:effect_010", "duration_multiplier": 1.0}]),
        ("yvanchuxiuzhen:jiuniang_002", 50.0, 5.0, 0.0, None, [], [
            {"effect_id": "minecraft:luck", "duration_multiplier": 1.0},
            {"effect_id": "minecraft:water_breathing", "duration_multiplier": 1.0},
        ]),
        ("yvanchuxiuzhen:jiuniang_003", 70.0, 5.0, 0.0, None, [], [{"effect_id": "yvanchuxiuzhen:effect_017", "duration_multiplier": 1.0}]),
        ("yvanchuxiuzhen:jiuniang_004", 35.0, 20.0, 0.0, None, [], [
            {"effect_id": "minecraft:regeneration", "duration_multiplier": 1.0},
            {"effect_id": "yvanchuxiuzhen:effect_010", "duration_multiplier": 1.0},
            {"effect_id": "yvanchuxiuzhen:effect_017", "duration_multiplier": 1.0},
        ]),
        ("yvanchuxiuzhen:jiuniang_005", 50.0, 5.0, 0.0, None, [], [{"effect_id": "yvanchuxiuzhen:effect_031", "duration_multiplier": 1.0}]),
        ("yvanchuxiuzhen:jiuniang_006", 10.0, 15.0, 0.0, None, [], []),
        ("yvanchuxiuzhen:jiuniang_007", 20.0, 5.0, 0.0, None, [], [{"effect_id": "yvanchuxiuzhen:effect_025", "duration_multiplier": 1.0}]),
        ("yvanchuxiuzhen:jiuniang_008", 0.0, 0.0, 40.0, "yvanchuxiuzhen:effect_004",
         [{"effect_id": "yvanchuxiuzhen:effect_013", "duration_multiplier": 1.0, "fixed_amplifier": 0}],
         [{"effect_id": "yvanchuxiuzhen:duyao_08", "duration_multiplier": 2.0}]),
    ]
    for wine_id, alcohol, exp, blood_exp, blood_remove, blood_effects, effects in wine_drinks:
        row: dict = {
            "kind": "wine_drink",
            "wine_id": wine_id,
            "alcohol": alcohol,
            "experience": exp,
            "blood_experience": blood_exp,
            "blood_effects": blood_effects,
            "effects": effects,
        }
        if blood_remove is not None:
            row["blood_removes_effect"] = blood_remove
        rows.append(row)

    rows.append({
        "kind": "ink_blank",
        "delay_ticks": 10,
        "material_tag": "yvanchuxiuzhen:cailiao",
        "category_groups": [
            {"category": 1, "tags": ["yvanchuxiuzhen:yingke", "yvanchuxiuzhen:guge"]},
            {"category": 2, "tags": ["yvanchuxiuzhen:pige", "yvanchuxiuzhen:linpian"]},
            {"category": 3, "tags": ["yvanchuxiuzhen:yachi", "yvanchuxiuzhen:lizhua"]},
        ],
        "reagents": [
            {"item_id": "yvanchuxiuzhen:yanhuiitem", "count": 3},
            {"item_id": "yvanchuxiuzhen:fuluzhushaitem", "count": 3},
        ],
        "grade_tiers": [
            {"min_average": 31.0, "output_id": "yvanchuxiuzhen:motiao_0401"},
            {"min_average": 21.0, "output_id": "yvanchuxiuzhen:motiao_0301"},
            {"min_average": 11.0, "output_id": "yvanchuxiuzhen:motiao_0201"},
            {"min_average": 0.0, "output_id": "yvanchuxiuzhen:motiao_0101"},
        ],
    })

    meal_plates = [
        (1, "yvanchuxiuzhen:zhushi", "yvanchuxiuzhen:panzhuang", "yvanchuxiuzhen:foodcipan", 12, 2, "yvanchuxiuzhen:effect_028"),
        (2, "yvanchuxiuzhen:caipin", "yvanchuxiuzhen:wanzhuang", "yvanchuxiuzhen:foodwan", 2, 12, "yvanchuxiuzhen:effect_029"),
        (3, "yvanchuxiuzhen:tangshui", "yvanchuxiuzhen:dapenzhuang", "yvanchuxiuzhen:fooddawan", 8, 4, "yvanchuxiuzhen:effect_030"),
        (4, "yvanchuxiuzhen:chadian", "yvanchuxiuzhen:tiepanzhuang", "yvanchuxiuzhen:foodtiepan", 2, 2, None),
    ]
    for category, item_tag, block_tag, container_block, nutrition, saturation, marker_effect in meal_plates:
        row = {
            "kind": "meal_plate",
            "category": category,
            "item_tag": item_tag,
            "block_tag": block_tag,
            "container_block": container_block,
            "nutrition": nutrition,
            "saturation": saturation,
        }
        if marker_effect is not None:
            row["marker_effect"] = marker_effect
        rows.append(row)

    containers = [
        (0, "yvanchuxiuzhen:foodwan", "§4需要在左侧四个容器栏位的第一个位置放置一个瓷碗！", "bowl"),
        (1, "yvanchuxiuzhen:foodcipan", "§4需要在左侧四个容器栏位的第二个位置放置一个瓷盘！", "plate"),
        (2, "yvanchuxiuzhen:fooddawan", "§4需要在左侧四个容器栏位的第三个位置放置一个大碗！", "large_bowl"),
        (3, "yvanchuxiuzhen:foodtiepan", "§4需要在左侧四个容器栏位的第二个位置放置一个铁盘！", "iron_plate"),
    ]
    for slot_offset, item_id, pot_error, steamer_error_key in containers:
        rows.append({
            "kind": "pot_container",
            "slot_offset": slot_offset,
            "item_id": item_id,
            "pot_error": pot_error,
            "steamer_error_key": steamer_error_key,
        })

    return rows


def build_craft_recipes() -> list[dict]:
    rows: list[dict] = []

    def c(item_id: str, count: int) -> dict:
        return {"item_id": item_id, "count": count}

    beech = [
        ("yvanchuxiuzhen:beechfurniture_01", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_02", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_03", [c("yvanchuxiuzhen:beechplanks", 12)]),
        ("yvanchuxiuzhen:beechfurniture_04", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_05", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_06", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_07", [c("yvanchuxiuzhen:beechplanks", 4), c("minecraft:hay_block", 1)]),
        ("yvanchuxiuzhen:beechfurniture_08", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_09", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_10", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_11", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_12", [c("yvanchuxiuzhen:beechplanks", 12)]),
        ("yvanchuxiuzhen:beechfurniture_13", [c("yvanchuxiuzhen:beechplanks", 12)]),
        ("yvanchuxiuzhen:beechfurniture_14", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_1501", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_1601", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_1701", [c("yvanchuxiuzhen:beechplanks", 6)]),
        ("yvanchuxiuzhen:beechfurniture_18", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_19", [c("yvanchuxiuzhen:beechplanks", 8), c("yvanchuxiuzhen:book_005", 2)]),
        ("yvanchuxiuzhen:beechfurniture_20", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_21", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_22", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_23", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_24", [c("yvanchuxiuzhen:beechplanks", 4)]),
        ("yvanchuxiuzhen:beechfurniture_2501", [c("yvanchuxiuzhen:beechplanks", 8), c("minecraft:paper", 2)]),
        ("yvanchuxiuzhen:beechfurniture_26", [c("yvanchuxiuzhen:beechplanks", 8), c("minecraft:paper", 2)]),
        ("yvanchuxiuzhen:beechfurniture_2701", [c("yvanchuxiuzhen:beechplanks", 4), c("minecraft:paper", 1)]),
        ("yvanchuxiuzhen:beechfurniture_2702", [c("yvanchuxiuzhen:beechplanks", 4), c("minecraft:paper", 1)]),
        ("yvanchuxiuzhen:beechfurniture_2703", [c("yvanchuxiuzhen:beechplanks", 4), c("minecraft:paper", 1)]),
        ("yvanchuxiuzhen:beechfurniture_2704", [c("yvanchuxiuzhen:beechplanks", 4), c("minecraft:paper", 1)]),
        ("yvanchuxiuzhen:beechfurniture_28", [c("yvanchuxiuzhen:beechplanks", 6)]),
        ("yvanchuxiuzhen:beechfurniture_29", [c("yvanchuxiuzhen:beechplanks", 8)]),
        ("yvanchuxiuzhen:beechfurniture_30", [c("yvanchuxiuzhen:beechplanks", 2), c("minecraft:candle", 1), c("minecraft:paper", 2)]),
        ("yvanchuxiuzhen:beechfurniture_31", [c("yvanchuxiuzhen:beechplanks", 2), c("minecraft:candle", 1), c("minecraft:paper", 2)]),
        ("yvanchuxiuzhen:beechfurniture_32", [c("yvanchuxiuzhen:beechplanks", 2), c("minecraft:candle", 1), c("minecraft:paper", 2)]),
    ]
    for output_id, costs in beech:
        rows.append({"kind": "carpentry", "category": "榉木", "output_id": output_id, "costs": costs})

    misc = [
        ("yvanchuxiuzhen:itemdanyaohulu_04", [c("minecraft:oak_planks", 3)]),
        ("yvanchuxiuzhen:itemdanyaohulu_05", [c("minecraft:oak_planks", 6), c("yvanchuxiuzhen:cailiao_174", 1)]),
    ]
    for output_id, costs in misc:
        rows.append({"kind": "carpentry", "category": "杂物", "output_id": output_id, "costs": costs})

    grinding = [
        ("yvanchuxiuzhen:shicai_001", 4, "yvanchuxiuzhen:shicai_00102", None),
        ("yvanchuxiuzhen:shicai_002", 4, "yvanchuxiuzhen:shicai_00202", None),
        ("minecraft:wheat", 4, "yvanchuxiuzhen:shicai_003", None),
        ("yvanchuxiuzhen:fuluzhushablock", 1, "yvanchuxiuzhen:fuluzhushaitem", None),
        ("yvanchuxiuzhen:shicai_046", 4, "yvanchuxiuzhen:food_039", "yvanchuxiuzhen:foodwan"),
        ("yvanchuxiuzhen:shicai_048", 4, "yvanchuxiuzhen:food_003", "yvanchuxiuzhen:foodwan"),
        ("yvanchuxiuzhen:shicai_058", 4, "yvanchuxiuzhen:tiaowei_08", None),
    ]
    for input_id, cost, output_id, aux in grinding:
        row: dict = {"kind": "grinding", "input_id": input_id, "cost": cost, "output_id": output_id}
        if aux is not None:
            row["auxiliary_id"] = aux
        rows.append(row)

    silk_reel = [
        ("yvanchuxiuzhen:silkwormchrysalis", "yvanchuxiuzhen:silkthread"),
        ("yvanchuxiuzhen:silkwormchrysalisgold", "yvanchuxiuzhen:silkthreadgold"),
        ("yvanchuxiuzhen:silkwormchrysalisred", "yvanchuxiuzhen:silkthreadred"),
    ]
    for input_id, output_id in silk_reel:
        rows.append({"kind": "silk_reel", "input_id": input_id, "output_id": output_id})

    loom_basic = [
        ("yvanchuxiuzhen:mianitem", "yvanchuxiuzhen:cailiao_171", 4),
        ("yvanchuxiuzhen:maitem", "yvanchuxiuzhen:cailiao_172", 9),
        ("yvanchuxiuzhen:cailiao_039", "yvanchuxiuzhen:cailiao_173", 14),
        ("yvanchuxiuzhen:silkthread", "yvanchuxiuzhen:cailiao_174", 19),
        ("yvanchuxiuzhen:silkthreadgold", "yvanchuxiuzhen:cailiao_175", 24),
        ("yvanchuxiuzhen:silkthreadred", "yvanchuxiuzhen:cailiao_176", 29),
        ("yvanchuxiuzhen:cailiao_17701", "yvanchuxiuzhen:cailiao_177", 34),
        ("yvanchuxiuzhen:cailiao_17801", "yvanchuxiuzhen:cailiao_178", 39),
    ]
    for input_id, output_id, level in loom_basic:
        rows.append({"kind": "loom_basic", "input_id": input_id, "output_id": output_id, "material_level": level})

    rows.append({
        "kind": "loom_pair",
        "input_a": "yvanchuxiuzhen:cailiao_209",
        "input_b": "yvanchuxiuzhen:cailiao_219",
        "output_id": "yvanchuxiuzhen:cailiao_179",
        "count": 2,
        "material_level": 41,
    })

    loom_cloud = [
        ("yvanchuxiuzhen:cailiao_179", 41),
        ("yvanchuxiuzhen:cailiao_178", 40),
        ("yvanchuxiuzhen:cailiao_177", 35),
        ("yvanchuxiuzhen:cailiao_176", 30),
        ("yvanchuxiuzhen:cailiao_175", 25),
        ("yvanchuxiuzhen:cailiao_174", 20),
        ("yvanchuxiuzhen:cailiao_173", 15),
        ("yvanchuxiuzhen:cailiao_172", 10),
        ("yvanchuxiuzhen:cailiao_171", 5),
    ]
    for aux_id, level in loom_cloud:
        rows.append({
            "kind": "loom_cloud",
            "main_id": "yvanchuxiuzhen:thundergrass",
            "auxiliary_id": aux_id,
            "output_id": "yvanchuxiuzhen:cailiao_243",
            "material_level": level,
        })

    pottery = [
        ("yvanchuxiuzhen:taoguan_01item", "minecraft:clay_ball", 4),
        ("yvanchuxiuzhen:taoguan_02item", "minecraft:clay_ball", 16),
        ("yvanchuxiuzhen:taoguan_03item", "minecraft:clay_ball", 36),
    ]
    for output_id, mat_id, cost in pottery:
        rows.append({"kind": "pottery", "output_id": output_id, "material_id": mat_id, "cost": cost})

    return rows


def write_json(path: Path, rows: list[dict]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(
        "[\n" + ",\n".join(json.dumps(r, ensure_ascii=False, separators=(",", ":")) for r in rows) + "\n]\n",
        encoding="utf-8",
        newline="\n",
    )


def main() -> None:
    cooking = build_cooking_recipes()
    craft = build_craft_recipes()
    write_json(COOKING_JSON, cooking)
    write_json(CRAFT_JSON, craft)
    print(f"Extracted {len(cooking)} cooking rules and {len(craft)} craft recipes.")


if __name__ == "__main__":
    main()
