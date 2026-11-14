"""逐字提取1.18.2版本任务方块右键修补使用的陈氏说明和完整商品串。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/net/yvanchuxiuzhen/system/quest/QuestBlockRefreshData.java"
EVIDENCE = ROOT / "docs/reference/conversion/quest_block_refresh.json"
LITERAL = r'"(?:[^"\\]|\\.)*"'


def extract(workspace: Path) -> tuple[str, dict]:
    source = workspace / "reference/1.18.2yvanchuxitong/src/main/java/net/mcreator/yvanchuxitong/procedures"
    patch_file = source / "RenwubudingProcedure.java"
    event_file = source / "PlayerRightClickBlockProcedure.java"
    patch = patch_file.read_text(encoding="utf-8")
    event = event_file.read_text(encoding="utf-8")
    patterns = {
        "OLD_CLOTHING_INFORMATION": r'getString\("shopinformation1"\)\)\.equals\((' + LITERAL + r')\)',
        "CLOTHING_INFORMATION": r'putString\("shopinformation1",\s*(' + LITERAL + r')\)',
        "CLOTHING_PRODUCTS": r'putString\("productlist1",\s*(' + LITERAL + r')\)',
    }
    values = {}
    for name, pattern in patterns.items():
        matches = re.findall(pattern, patch)
        assert len(matches) == 1, (name, len(matches))
        values[name] = json.loads(matches[0]).replace("yvanchuxitong:", "yvanchuxiuzhen:")
    assert "if (x == -103 && y == 64 && z == -263)" in patch
    assert "else if (x == -51 && y == 64 && z == -320)" in patch
    assert 'putString("productlist1", Cailiaoshop01Procedure.execute())' in patch
    assert event.index("event.getHand() != event.getPlayer().getUsedItemHand()") < event.index("execute(event,")
    assert event.index("RenwubudingProcedure.execute(world, x, y, z)") < event.index("if (blockstate.is(")
    assert len(values["CLOTHING_PRODUCTS"].split("%")) == 55
    fields = "\n".join(f"    static final String {name} = {json.dumps(value, ensure_ascii=False)};" for name, value in values.items())
    java = '''package net.yvanchuxiuzhen.system.quest;

/** 由extract_quest_block_refresh.py生成；陈氏服饰修补的说明和11件完整商品逐字保留来源。 */
final class QuestBlockRefreshData {
''' + fields + '''

    private QuestBlockRefreshData() {
    }
}
'''
    evidence = {
        "source_version": "1.18.2", "clothing_product_count": 11,
        "source_sha256": {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in (patch_file, event_file)},
        "output_sha256": hashlib.sha256(java.encode("utf-8")).hexdigest(),
        "string_sha256": {name: hashlib.sha256(value.encode("utf-8")).hexdigest() for name, value in values.items()},
        "refresh_position": [-103, 64, -263], "clothing_position": [-51, 64, -320],
        "entry_order": "used item hand filter -> task block patch -> remaining source right-click branches",
    }
    return java, evidence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-workspace", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    java, evidence = extract(args.source_workspace)
    outputs = {JAVA: java, EVIDENCE: json.dumps(evidence, ensure_ascii=False, indent=2) + "\n"}
    for path, value in outputs.items():
        if args.check:
            assert path.read_text(encoding="utf-8") == value, path
        else:
            path.write_text(value, encoding="utf-8", newline="\n")
    print("任务方块右键补丁：2处坐标、陈氏3字符串、11件商品；来源与输出SHA256已核对。")


if __name__ == "__main__":
    main()
