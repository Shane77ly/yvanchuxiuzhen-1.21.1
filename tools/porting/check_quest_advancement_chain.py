"""静态核对八个空谷残卷结构中的成就授予脚本与目标成就链。

该检查只验证来源资源、转换后的目标结构和目标 advancement 定义，不能替代
1.18.2/1.21.1 实机中的结构释放、右键交互、任务执行或成就显示验收。
"""
from __future__ import annotations

import argparse
import gzip
import hashlib
import json
import re
import struct
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]
NAMESPACE = "yvanchuxiuzhen"
TASK_BLOCK = f"{NAMESPACE}:renwuweizhuangfangkuai"
TEMPLATES = (
    ("konggucanjvan01.nbt", 1007, (2, 2, 10), "tasklist1", "chengjiu_001"),
    ("konggucanjvan02.nbt", 2024, (8, 2, 12), "tasklist1", "chengjiu_002"),
    ("konggucanjvan03.nbt", 3165, (18, 8, 5), "tasklist1", "chengjiu_003"),
    ("konggucanjvan04.nbt", 1271, (1, 2, 12), "tasklist1", "chengjiu_004"),
    ("konggucanjvan05.nbt", 4045, (18, 3, 8), "tasklist1", "chengjiu_005"),
    ("konggucanjvan06.nbt", 4616, (14, 1, 18), "renwulist1", "chengjiu_006"),
    ("konggucanjvan07.nbt", 17405, (17, 2, 17), "tasklist1", "chengjiu_007"),
    ("konggucanjvan08.nbt", 26782, (26, 4, 10), "tasklist1", "chengjiu_008"),
)
COMMAND_PATTERN = re.compile(r"advancement grant @p only yvanchuxiuzhen:chengjiu_\d{3}")


class NbtReader:
    """只读解析结构文件所需的完整 NBT 标签集合。"""

    NUMBERS = {1: ">b", 2: ">h", 3: ">i", 4: ">q", 5: ">f", 6: ">d"}

    def __init__(self, data: bytes) -> None:
        self.data = data
        self.position = 0

    def take(self, count: int) -> bytes:
        end = self.position + count
        if end > len(self.data):
            raise ValueError("NBT 数据提前结束")
        value = self.data[self.position:end]
        self.position = end
        return value

    def number(self, fmt: str) -> int | float:
        return struct.unpack(fmt, self.take(struct.calcsize(fmt)))[0]

    def string(self) -> str:
        size = self.number(">H")
        return self.take(size).decode("utf-8")

    def payload(self, tag: int) -> Any:
        if tag in self.NUMBERS:
            return self.number(self.NUMBERS[tag])
        if tag == 7:
            size = self.number(">i")
            return self.take(size)
        if tag == 8:
            return self.string()
        if tag == 9:
            element = self.number(">B")
            size = self.number(">i")
            if size < 0:
                raise ValueError("NBT 列表长度为负数")
            if element == 0:
                if size != 0:
                    raise ValueError(f"空标签类型的 NBT 列表长度不为零：position={self.position} size={size}")
                return []
            return [self.payload(element) for _ in range(size)]
        if tag == 10:
            value: dict[str, Any] = {}
            while True:
                child = self.number(">B")
                if child == 0:
                    return value
                name = self.string()
                value[name] = self.payload(child)
        if tag in (11, 12):
            size = self.number(">i")
            if size < 0:
                raise ValueError("NBT 数组长度为负数")
            return [self.number(">i" if tag == 11 else ">q") for _ in range(size)]
        raise ValueError(f"未知 NBT 标签类型：{tag}")

    def root(self) -> dict[str, Any]:
        if self.number(">B") != 10:
            raise ValueError("结构 NBT 根不是复合标签")
        self.string()
        value = self.payload(10)
        if self.position != len(self.data):
            raise ValueError("NBT 末尾存在多余字节")
        return value


def read_structure(path: Path) -> dict[str, Any]:
    return NbtReader(gzip.decompress(path.read_bytes())).root()


def default_source_workspace() -> Path:
    candidates = (
        ROOT.parent / "yuanchuxiuzhen-1.21.1",
        ROOT.parents[1] / "yuanchuxiuzhen-1.21.1",
    )
    for candidate in candidates:
        if (candidate / "reference/old-1.18.2-mcreator").is_dir():
            return candidate
    return candidates[0]


def source_paths(workspace: Path) -> tuple[Path, Path]:
    base = workspace / "reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen/src/main/resources/data/yvanchuxiuzhen"
    return base / "structures", base / "advancements"


def target_paths() -> tuple[Path, Path]:
    base = ROOT / "src/main/resources/data/yvanchuxiuzhen"
    return base / "structure", base / "advancement"


def sha256(value: str) -> str:
    return hashlib.sha256(value.encode("utf-8")).hexdigest()


def block_at(document: dict[str, Any], index: int) -> dict[str, Any]:
    blocks = document.get("blocks")
    if not isinstance(blocks, list) or index >= len(blocks):
        raise AssertionError(f"blocks[{index}] 不存在")
    block = blocks[index]
    if not isinstance(block, dict):
        raise AssertionError(f"blocks[{index}] 不是复合标签")
    return block


def check_template(source_dir: Path, target_dir: Path, template: tuple[str, int, tuple[int, int, int], str, str]) -> dict[str, Any]:
    filename, index, position, field, advancement = template
    source = read_structure(source_dir / filename)
    target = read_structure(target_dir / filename)
    source_block = block_at(source, index)
    target_block = block_at(target, index)
    source_nbt = source_block.get("nbt")
    target_nbt = target_block.get("nbt")
    if not isinstance(source_nbt, dict) or not isinstance(target_nbt, dict):
        raise AssertionError(f"{filename} 的目标块没有方块实体 NBT")
    if source_block.get("pos") != list(position) or target_block.get("pos") != list(position):
        raise AssertionError(f"{filename} 的 blocks[{index}] 相对坐标不符")
    source_data = source_nbt.get("ForgeData")
    target_data = target_nbt.get("NeoForgeData")
    if not isinstance(source_data, dict) or not isinstance(target_data, dict):
        raise AssertionError(f"{filename} 缺少 ForgeData/NeoForgeData")
    source_script = source_data.get(field)
    target_script = target_data.get(field)
    if not isinstance(source_script, str) or not isinstance(target_script, str):
        raise AssertionError(f"{filename} 缺少 {field}")
    command = f"advancement grant @p only {NAMESPACE}:{advancement}"
    source_commands = COMMAND_PATTERN.findall(source_script)
    target_commands = COMMAND_PATTERN.findall(target_script)
    if source_commands != [command] or target_commands != [command]:
        raise AssertionError(f"{filename} 的成就命令不匹配：{source_commands!r}/{target_commands!r}")
    if source_script != target_script:
        raise AssertionError(f"{filename} 的来源/目标脚本不一致")
    if source_nbt.get("id") != "yvanchuxitong:renwuweizhuangfangkuai":
        raise AssertionError(f"{filename} 来源任务方块 ID 不符")
    if target_nbt.get("id") != TASK_BLOCK:
        raise AssertionError(f"{filename} 目标任务方块 ID 不符")
    return {
        "template": filename,
        "block_index": index,
        "position": list(position),
        "script_field": field,
        "source_data_key": "ForgeData",
        "target_data_key": "NeoForgeData",
        "source_block_id": source_nbt["id"],
        "target_block_id": target_nbt["id"],
        "advancement": f"{NAMESPACE}:{advancement}",
        "command": command,
        "source_script_sha256": sha256(source_script),
        "target_script_sha256": sha256(target_script),
        "script_equal": True,
    }


def check_advancement(source_dir: Path, target_dir: Path, number: int) -> dict[str, Any]:
    name = f"chengjiu_{number:03}.json"
    source = json.loads((source_dir / name).read_text(encoding="utf-8"))
    target = json.loads((target_dir / name).read_text(encoding="utf-8"))
    key = f"chengjiu_{number:03}"
    expected_parent = None if number == 1 else f"{NAMESPACE}:chengjiu_{number - 1:03}"
    for label, document in (("来源", source), ("目标", target)):
        criteria = document.get("criteria")
        if criteria != {key: {"trigger": "minecraft:impossible"}}:
            raise AssertionError(f"{label} {name} criterion 不符")
        if document.get("rewards") != {"experience": 20}:
            raise AssertionError(f"{label} {name} 奖励不符")
        if document.get("parent") != expected_parent:
            raise AssertionError(f"{label} {name} parent 不符")
    target_icon = target.get("display", {}).get("icon", {})
    if target_icon.get("id") != f"{NAMESPACE}:book_022":
        raise AssertionError(f"目标 {name} 图标不符")
    source_icon = source.get("display", {}).get("icon", {})
    if source_icon.get("item") != f"{NAMESPACE}:book_022":
        raise AssertionError(f"来源 {name} 图标不符")
    return {
        "advancement": f"{NAMESPACE}:{key}",
        "criterion": "minecraft:impossible",
        "experience": 20,
        "parent": expected_parent,
        "source_icon_key": "item",
        "target_icon_key": "id",
    }


def run(source_workspace: Path, output: Path) -> dict[str, Any]:
    source_structure_dir, source_advancement_dir = source_paths(source_workspace)
    target_structure_dir, target_advancement_dir = target_paths()
    templates = [check_template(source_structure_dir, target_structure_dir, item) for item in TEMPLATES]
    advancements = [check_advancement(source_advancement_dir, target_advancement_dir, number) for number in range(1, 9)]
    result = {
        "check": "q162-advancement-chain",
        "source_version": "1.18.2",
        "target_version": "1.21.1",
        "source_workspace": str(source_workspace),
        "templates": templates,
        "advancements": advancements,
        "summary": {
            "templates_checked": len(templates),
            "advancements_checked": len(advancements),
            "script_matches": all(item["script_equal"] for item in templates),
            "runtime_validation": False,
        },
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source-workspace", type=Path, default=default_source_workspace(), help="锁定1.18.2来源工程根目录")
    parser.add_argument("--output", type=Path, default=ROOT / "build/diagnostics/q162-advancement-chain/static-parity.json")
    args = parser.parse_args()
    result = run(args.source_workspace.resolve(), args.output.resolve())
    summary = result["summary"]
    print(f"Q162静态核验通过：{summary['templates_checked']}个结构、{summary['advancements_checked']}个成就；未执行实机运行。")


if __name__ == "__main__":
    main()
