"""为 Pao10/11/12 与 Pao13 生成独立接线计划，不修改公共转换登记。"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]


def build() -> dict:
    java_root = ROOT / "src/main/java/net/yvanchuxiuzhen/collection/festival"
    resources = ROOT / "src/main/resources/content/festival"
    files = [
        "BlockFireworkKind.java", "BlockFirework.java", "AttachedFirecrackerBlock.java",
        "BlockFireworkBlockEntity.java", "BlockFireworks.java", "FestivalFireworkDelays.java",
        "Pao13Entity.java", "Pao13Fireworks.java", "Pao13Item.java",
    ]
    copies = {f"src/main/java/net/yvanchuxiuzhen/collection/festival/{name}": str(java_root / name) for name in files}
    copies.update({f"src/main/resources/content/festival/{name}": str(resources / name) for name in ("blocks.json", "items.json", "entities.json")})
    fingerprints = {key: hashlib.sha256(Path(value).read_bytes()).hexdigest() for key, value in copies.items()}
    return {
        "ids": ["pao_10", "pao_11", "pao_12", "projectile_pao_13", "pao_13"],
        "copies": copies,
        "source_fingerprints": fingerprints,
        "verification": {"static_source": "Pao10/11/12 计时、零槽库存、点燃、粒子、声音、替换和 Pao13 八档已实现", "json": "已解析", "build": "未执行", "client": "未执行", "server": "未执行"},
        "open_questions": ["来源 Pao13 的延迟回调持有已移除弹体引用，当前保留坐标读取语义；主线统一验收。"],
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--output", type=Path, default=ROOT / "tmp/r15-festival-firework-blocks-integration.json")
    args = parser.parse_args()
    report = build()
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"festival firework blocks: {len(report['ids'])} identities")


if __name__ == "__main__":
    main()
