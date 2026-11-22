"""核对 Pao10-Pao13 的目标资源闭合性并标记来源孤儿资源。

脚本只检查已接入内容所引用的模型、方块状态、物品模型、贴图和粒子图集，
不把来源目录中没有任何调用者的资源当作目标运行时依赖。
"""

from __future__ import annotations

import argparse
import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
NAMESPACE = "yvanchuxiuzhen"
TARGET_ASSETS = ROOT / "src/main/resources/assets" / NAMESPACE


REQUIRED = {
    "pao_10": (
        "blockstates/pao_10.json",
        "models/block/pao_10.json",
        "models/block/pao_10__cutout.json",
        "models/custom/pao1001.json",
        "models/item/pao_10.json",
        "textures/block/pao1001.png",
        "textures/item/pao10.png",
    ),
    "pao_11": (
        "blockstates/pao_11.json",
        "models/block/pao_11.json",
        "models/block/pao_11__cutout.json",
        "models/custom/pao1101.json",
        "models/item/pao_11.json",
        "textures/block/pao1101.png",
        "textures/item/pao11.png",
    ),
    "pao_12": (
        "blockstates/pao_12.json",
        "models/block/pao_12.json",
        "models/block/pao_12__cutout.json",
        "models/custom/pao12.json",
        "models/item/pao_12.json",
        "textures/block/pao1201.png",
    ),
    "pao_13": (
        "models/item/pao_13.json",
        "textures/item/pao13.png",
    ),
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--source-root",
        type=Path,
        default=ROOT.parent / "yuanchuxiuzhen-1.21.1",
        help="1.18.2 来源工程根目录，用于记录来源孤儿资源。",
    )
    return parser.parse_args()


def scan_references() -> set[str]:
    references: set[str] = set()
    for path in TARGET_ASSETS.rglob("*.json"):
        try:
            text = path.read_text(encoding="utf-8")
        except UnicodeDecodeError:
            continue
        for match in re.finditer(r"(?:^|[\"'])" + re.escape(NAMESPACE) + r":([^\"']+)", text):
            references.add(match.group(1))
    return references


def source_orphan(source_root: Path, references: set[str]) -> str | None:
    source_asset = source_root / "reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen/src/main/resources/assets" / NAMESPACE / "textures/particle/pao1002.png"
    if not source_asset.is_file():
        return None
    if "textures/particle/pao1002" in references:
        return None
    return source_asset.relative_to(source_root).as_posix()


def main() -> int:
    args = parse_args()
    missing = {
        item: [path for path in paths if not (TARGET_ASSETS / path).is_file()]
        for item, paths in REQUIRED.items()
    }
    missing = {item: paths for item, paths in missing.items() if paths}
    references = scan_references()
    report = {
        "target": str(TARGET_ASSETS),
        "required": {item: list(paths) for item, paths in REQUIRED.items()},
        "missing": missing,
        "source_only_unreferenced": source_orphan(args.source_root, references),
        "status": "FAIL" if missing else "PASS",
    }
    print(json.dumps(report, ensure_ascii=False, indent=2))
    return 1 if missing else 0


if __name__ == "__main__":
    raise SystemExit(main())
