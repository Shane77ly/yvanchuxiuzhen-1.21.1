"""从锁定1.18.2来源提取默认副本和镇妖塔定义，保留字符串与固定坐标。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/net/yvanchuxiuzhen/system/dungeon/DungeonSourceData.java"
EVIDENCE = ROOT / "docs/reference/conversion/dungeon_definitions.json"


def extract(workspace: Path) -> tuple[str, dict]:
    source = workspace / "reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen/src/main/java/net/mcreator/yvanchuxiuzhen/procedures"
    default_file = source / "TiandaofubenFangZhiFangKuaiShiProcedure.java"
    tower_file = source / "ZhenyaotashengchengbuchongProcedure.java"
    default_source = default_file.read_text(encoding="utf-8")
    tower_source = tower_file.read_text(encoding="utf-8")
    literal = r'"(?:[^"\\]|\\.)*"'
    default_match = re.search(r'putString\("fubenshujv",\s*(' + literal + r')\)', default_source)
    default = json.loads(default_match[1])
    pattern = r'BlockPos _bp = new BlockPos\((-?\d+), (-?\d+), (-?\d+)\);.*?putString\("fubenshujv",\s*(' + literal + r')\)'
    towers = [(int(x), int(y), int(z), json.loads(value)) for x, y, z, value in re.findall(pattern, tower_source, re.S)]
    floors = [(int(x), int(y), int(z)) for x, y, z in re.findall(r'world.setBlock\(new BlockPos\((-?\d+), (-?\d+), (-?\d+)\), YvanchuxiuzhenModBlocks.GINKGOFLOOR', tower_source)]
    assert len(towers) == 44
    assert floors == [(x, y - 1, z) for x, y, z, _ in towers]
    quote = lambda value: json.dumps(value, ensure_ascii=False)
    rows = '\n'.join(f'            new Tower({x}, {y}, {z}, {quote(value)})' + (',' if i < len(towers) - 1 else '') for i, (x, y, z, value) in enumerate(towers))
    java = '''package net.yvanchuxiuzhen.system.dungeon;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 由extract_dungeon_definitions.py生成；默认定义与镇妖塔44个坐标逐字保留锁定来源。 */
public final class DungeonSourceData {
    public static final String DEFAULT = ''' + quote(default) + ''';
    private static final List<Tower> TOWERS = List.of(
''' + rows + ''');

    private DungeonSourceData() {
    }

    public static void configureTower(ServerLevel level) {
        for (Tower tower : TOWERS) {
            BlockPos pos = new BlockPos(tower.x(), tower.y(), tower.z());
            BlockEntity block = level.getBlockEntity(pos);
            if (block != null) {
                block.getPersistentData().putString("fubenshujv", tower.definition());
                block.setChanged();
                level.sendBlockUpdated(pos, block.getBlockState(), block.getBlockState(), 3);
            }
            level.setBlock(pos.below(), DungeonPlayers.block("ginkgofloor").defaultBlockState(), 3);
        }
    }

    private record Tower(int x, int y, int z, String definition) {
    }
}
'''
    evidence = {
        "source_version": "1.18.2", "tower_definitions": len(towers),
        "source_sha256": {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in [default_file, tower_file]},
        "output_sha256": hashlib.sha256(java.encode("utf-8")).hexdigest(),
        "tower_positions": [[x, y, z] for x, y, z, _ in towers],
        "default_sha256": hashlib.sha256(default.encode("utf-8")).hexdigest(),
        "largest_definition_utf8_bytes": max(len(value.encode("utf-8")) for _, _, _, value in towers),
    }
    return java, evidence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-workspace", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    java, evidence = extract(args.source_workspace)
    encoded = json.dumps(evidence, ensure_ascii=False, indent=2) + "\n"
    if args.check:
        assert JAVA.read_text(encoding="utf-8") == java
        assert EVIDENCE.read_text(encoding="utf-8") == encoded
    else:
        JAVA.write_text(java, encoding="utf-8", newline="\n")
        EVIDENCE.write_text(encoded, encoding="utf-8", newline="\n")
    print("默认定义1份、镇妖塔44份；来源与输出SHA256已核对。")


if __name__ == "__main__":
    main()
