"""提取锁定1.18.2版本的结构释放坐标和任务字段，保留顺序并统一注册命名空间。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "src/main/java/net/yvanchuxiuzhen/system"
EVIDENCE = ROOT / "docs/reference/conversion/structure_releases.json"
LITERAL = r'"(?:[^"\\]|\\.)*"'


def quote(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)


def text_value(raw: str) -> str:
    return json.loads(raw).replace("yvanchuxitong:", "yvanchuxiuzhen:")


def extract(workspace: Path) -> tuple[dict[Path, str], dict]:
    source = workspace / "reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen/src/main/java/net/mcreator/yvanchuxiuzhen/procedures"
    release_file = source / "SealreleaseGoProcedure.java"
    patch_file = source / "JiegoubudingProcedure.java"
    release = release_file.read_text(encoding="utf-8")
    patch = patch_file.read_text(encoding="utf-8")
    branches = list(re.finditer(r'if \(\(jiegouname\)\.equals\((' + LITERAL + r')\)\)', release))
    placements = []
    fields = []
    block_pattern = r'BlockPos _bp = new BlockPos\((-?\d+), (-?\d+), (-?\d+)\);(?:(?!BlockPos _bp =).)*?getTileData\(\)\.putString\((' + LITERAL + r'),\s*(' + LITERAL + r')\)'
    for i, branch in enumerate(branches):
        name = text_value(branch[1])
        body = release[branch.end():branches[i + 1].start() if i + 1 < len(branches) else len(release)]
        template_pattern = r'getOrCreate\(new ResourceLocation\("yvanchuxiuzhen", "([^"]+)"\)\).*?template\.placeInWorld\(_serverworld, new BlockPos\((-?\d+), (-?\d+), (-?\d+)\)'
        placements.extend((name, template, int(x), int(y), int(z)) for template, x, y, z in re.findall(template_pattern, body, re.S))
        fields.extend((name, int(x), int(y), int(z), text_value(key), text_value(value)) for x, y, z, key, value in re.findall(block_pattern, body, re.S))
    fields.extend(("花繁城", int(x), int(y), int(z), text_value(key), text_value(value)) for x, y, z, key, value in re.findall(block_pattern, patch, re.S))
    task_match = re.search(r'entityiterator.getPersistentData\(\)\.putString\("renwulist2",\s*(' + LITERAL + r')\)', patch)
    task = text_value(task_match[1])
    assert len(branches) == 28
    assert len(placements) == 42
    assert len({row[1] for row in placements}) == 42
    assert len(fields) == 14
    rows = ",\n".join(f'            new Placement({quote(name)}, {quote(template)}, new BlockPos({x}, {y}, {z}))' for name, template, x, y, z in placements)
    world = '''package net.yvanchuxiuzhen.system.world;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.api.WorldStructures;

/** 由extract_structure_releases.py生成；28个名字的42个模板按锁定来源顺序放置。 */
final class StructureReleaseData {
    private static final List<Placement> PLACEMENTS = List.of(
''' + rows + ''');

    private StructureReleaseData() {
    }

    static void place(ServerLevel level, String name) {
        for (Placement placement : PLACEMENTS) {
            if (placement.name().equals(name)) {
                WorldStructures.place(level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, placement.template()), placement.pos());
            }
        }
    }

    private record Placement(String name, String template, BlockPos pos) {
    }
}
'''
    rows = ",\n".join(f'            new Field({quote(name)}, new BlockPos({x}, {y}, {z}), {quote(key)}, {quote(value)})' for name, x, y, z, key, value in fields)
    quest = '''package net.yvanchuxiuzhen.system.quest;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

/** 由extract_structure_releases.py生成；结构任务和商店字段按来源坐标与字符串逐字迁移。 */
final class QuestStructureData {
    private static final List<Field> FIELDS = List.of(
''' + rows + ''');

    private QuestStructureData() {
    }

    static void apply(ServerLevel level, String name) {
        for (Field field : FIELDS) {
            if (!field.name().equals(name)) continue;
            BlockEntity block = level.getBlockEntity(field.pos());
            if (block != null) {
                block.getPersistentData().putString(field.key(), field.value());
                block.setChanged();
                level.sendBlockUpdated(field.pos(), block.getBlockState(), block.getBlockState(), 3);
            }
        }
    }

    private record Field(String name, BlockPos pos, String key, String value) {
    }
}
'''
    npc = '''package net.yvanchuxiuzhen.system.npc;

/** 由extract_structure_releases.py生成；花繁城酒馆NPC的来源第二项任务。 */
final class NpcStructureData {
    static final String TAVERN_TASK = ''' + quote(task) + ''';

    private NpcStructureData() {
    }
}
'''
    outputs = {BASE / "world/StructureReleaseData.java": world, BASE / "quest/QuestStructureData.java": quest,
               BASE / "npc/NpcStructureData.java": npc}
    evidence = {
        "source_version": "1.18.2", "structure_names": len(branches), "placements": len(placements),
        "unique_templates": len({row[1] for row in placements}), "block_string_fields": len(fields),
        "source_sha256": {path.name: hashlib.sha256(path.read_bytes()).hexdigest() for path in (release_file, patch_file)},
        "output_sha256": {path.relative_to(ROOT).as_posix(): hashlib.sha256(value.encode("utf-8")).hexdigest() for path, value in outputs.items()},
        "placement_table": [{"name": name, "template": template, "position": [x, y, z]} for name, template, x, y, z in placements],
        "field_table": [{"name": name, "position": [x, y, z], "field": key, "sha256": hashlib.sha256(value.encode("utf-8")).hexdigest()} for name, x, y, z, key, value in fields],
        "npc21_task_sha256": hashlib.sha256(task.encode("utf-8")).hexdigest(),
    }
    return outputs, evidence


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-workspace", type=Path, required=True)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    outputs, evidence = extract(args.source_workspace)
    outputs[EVIDENCE] = json.dumps(evidence, ensure_ascii=False, indent=2) + "\n"
    for path, value in outputs.items():
        if args.check:
            assert path.read_text(encoding="utf-8") == value, path
        else:
            path.write_text(value, encoding="utf-8", newline="\n")
    print("结构28名/42模板，任务方块字符串14项与NPC21任务1项；来源和输出SHA256已核对。")


if __name__ == "__main__":
    main()
