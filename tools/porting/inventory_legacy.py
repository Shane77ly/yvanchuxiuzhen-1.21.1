"""提取两个1.18.2 工程的静态证据；清单不判定玩家行为或移植完成度。"""

from __future__ import annotations

import argparse
from collections import Counter
from concurrent.futures import ThreadPoolExecutor
import csv
import hashlib
import io
import json
from pathlib import Path
import re


ROOT = Path(__file__).resolve().parents[2]
SOURCES = {
    "yvanchuxiuzhen": "reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen",
    "yvanchuxitong": "reference/1.18.2yvanchuxitong",
}
CATEGORIES = {
    "Items": "item", "Blocks": "block", "Entities": "entity_type",
    "Menus": "menu", "BlockEntities": "block_entity_type",
    "MobEffects": "mob_effect", "Enchantments": "enchantment",
    "Sounds": "sound_event", "ParticleTypes": "particle_type",
    "Fluids": "fluid", "Biomes": "biome", "Paintings": "painting_variant",
    "Features": "feature", "GeckoLibArmors": "item", "Attributes": "attribute", "GameRules": "game_rule",
}
DECLARATION = re.compile(
    r'public\s+static\s+final\s+[^;=]+?\s+(\w+)\s*=\s*([^;]+);', re.S
)
REGISTRATION = re.compile(r'(?:\w+\.)?register\(\s*"([^"\n]+)"')
BLOCK_ITEM = re.compile(r'(?:block|doubleBlock)\(\s*\w+ModBlocks\.(\w+)\s*,')
BLOCK_PATH_ITEM = re.compile(r'REGISTRY\.register\(\s*\w+ModBlocks\.(\w+)\.getId\(\)\.getPath\(\)')


def csv_text(rows: list[dict], columns: list[str]) -> str:
    stream = io.StringIO(newline="")
    writer = csv.DictWriter(stream, fieldnames=columns, lineterminator="\n")
    writer.writeheader()
    writer.writerows(rows)
    return stream.getvalue()


def generate(source_workspace: Path) -> dict[str, str]:
    registrations, fields, files = [], [], []
    summary = {"schema_version": 1, "sources": {}, "limitations": [
        "静态注册声明、文件及玩家字段清单，不代表行为覆盖或游戏验收。",
        "物品包含方块物品；实体类型包含投射物和内部形态。",
        "跨命名空间碰撞按同类注册表计算；最终 ID 映射尚未制定。",
        "未自动解释过程中的动态注册、字符串表和脚本行为。",
    ]}
    for mod_id, relative_root in SOURCES.items():
        print(f"解析 {mod_id} 注册与字段", flush=True)
        root = source_workspace / relative_root
        if not root.is_dir():
            raise ValueError(f"1.18.2 工程不存在：{root}")
        java_root = root / "src/main/java/net/mcreator" / mod_id
        init = java_root / "init"
        prefix = "YvanchuxiuzhenMod" if mod_id == "yvanchuxiuzhen" else "YvanchuxitongMod"
        block_text = (init / f"{prefix}Blocks.java").read_text(encoding="utf-8-sig")
        block_ids = {}
        for declaration in DECLARATION.finditer(block_text):
            match = REGISTRATION.search(declaration[2])
            if match:
                block_ids[declaration[1]] = match[1]
        unresolved = []
        for suffix, category in CATEGORIES.items():
            source = init / f"{prefix}{suffix}.java"
            if not source.exists():
                continue
            content = source.read_text(encoding="utf-8-sig")
            if category == "painting_variant":
                for match in re.finditer(r'new Motive\([^;]+?\.setRegistryName\("([^"\n]+)"\)', content):
                    registrations.append({
                        "source_mod": mod_id, "registry": category,
                        "legacy_id": f"{mod_id}:{match[1]}", "symbol": "",
                        "declaration_kind": "event_registration", "source_file": source.relative_to(root).as_posix(),
                        "line": content.count("\n", 0, match.start()) + 1,
                    })
            if category == "sound_event":
                for match in re.finditer(r'REGISTRY\.put\(new ResourceLocation\("([^"\n]+)",\s*"([^"\n]+)"\)', content):
                    registrations.append({
                        "source_mod": mod_id, "registry": category,
                        "legacy_id": f"{match[1]}:{match[2]}", "symbol": "",
                        "declaration_kind": "map_registration", "source_file": source.relative_to(root).as_posix(),
                        "line": content.count("\n", 0, match.start()) + 1,
                    })
            for declaration in DECLARATION.finditer(content):
                symbol, expression = declaration[1], declaration[2]
                match = REGISTRATION.search(expression)
                block = BLOCK_ITEM.search(expression) or BLOCK_PATH_ITEM.search(expression)
                if block and category == "item":
                    resource_path = block_ids[block[1]]
                    kind = "block_item"
                elif match:
                    resource_path = match[1]
                    kind = "literal_registration"
                elif "RegistryObject<" in declaration[0] or "MenuType<" in declaration[0]:
                    unresolved.append({"file": source.relative_to(root).as_posix(), "symbol": symbol})
                    continue
                else:
                    continue
                registrations.append({
                    "source_mod": mod_id, "registry": category,
                    "legacy_id": f"{mod_id}:{resource_path}", "symbol": symbol,
                    "declaration_kind": kind, "source_file": source.relative_to(root).as_posix(),
                    "line": content.count("\n", 0, declaration.start()) + 1,
                })
        # 手写实体可在自身的嵌套类声明 DeferredRegister，不属于 MCreator 的 init 模板。
        for source in sorted(java_root.rglob("*.java")):
            if source.parent == init:
                continue
            content = source.read_text(encoding="utf-8-sig")
            if "DeferredRegister" not in content:
                continue
            declarations = list(DECLARATION.finditer(content))
            entity_registers = [declaration[1] for declaration in declarations
                                if re.search(r'DeferredRegister\.create\(\s*ForgeRegistries\.ENTITIES\s*,', declaration[2])]
            for declaration in declarations:
                for register in entity_registers:
                    match = re.search(r'\b' + re.escape(register) + r'\.register\(\s*"([^"\n]+)"', declaration[2])
                    if match:
                        registrations.append({
                            "source_mod": mod_id, "registry": "entity_type",
                            "legacy_id": f"{mod_id}:{match[1]}", "symbol": declaration[1],
                            "declaration_kind": "literal_registration", "source_file": source.relative_to(root).as_posix(),
                            "line": content.count("\n", 0, declaration.start()) + 1,
                        })
        variables = java_root / "network" / f"{prefix}Variables.java"
        content = variables.read_text(encoding="utf-8-sig")
        start = content.index("public static class PlayerVariables {")
        # 只读取 PlayerVariables 字段区；序列化与同步方法不属于字段声明。
        tail = re.split(r'\n[ \t]+public\s+(?:static\s+)?[\w<>]+\s+\w+\(', content[start:], maxsplit=1)[0]
        for match in re.finditer(r"(?m)^[ \t]*public\s+(\w+)\s+(\w+)\s*=\s*([^\n;]+);", tail):
            fields.append({"source_mod": mod_id, "field": match[2], "java_type": match[1],
                           "initial_value": match[3], "source_file": variables.relative_to(root).as_posix(),
                           "line": content.count("\n", 0, start + match.start()) + 1})
        paths = set()
        for directory in ("src", "elements"):
            if (root / directory).is_dir():
                paths.update(path for path in (root / directory).rglob("*") if path.is_file())
        paths.update(path for path in (root / "build.gradle", root / "gradle.properties") if path.is_file())
        print(f"读取 {mod_id} 的 {len(paths)} 份来源文件指纹", flush=True)
        def fingerprint(path: Path) -> dict:
            data = path.read_bytes()
            return {"source_mod": mod_id, "path": path.relative_to(root).as_posix(),
                    "bytes": len(data), "sha256": hashlib.sha256(data).hexdigest()}
        # 文件读取彼此独立；map 保持输入顺序，保证生成清单可重复比较。
        with ThreadPoolExecutor(max_workers=8) as pool:
            files.extend(pool.map(fingerprint, sorted(paths)))
        source_regs = [row for row in registrations if row["source_mod"] == mod_id]
        source_files = [row for row in files if row["source_mod"] == mod_id]
        summary["sources"][mod_id] = {
            "relative_root": relative_root,
            "registry_counts": dict(sorted(Counter(row["registry"] for row in source_regs).items())),
            "player_field_count": sum(row["source_mod"] == mod_id for row in fields),
            "file_count": len(source_files), "file_bytes": sum(row["bytes"] for row in source_files),
            "procedure_files": sum("/procedures/" in row["path"] and row["path"].endswith(".java") for row in source_files),
            "unresolved_declarations": unresolved,
        }
    identities = [(row["source_mod"], row["registry"], row["legacy_id"]) for row in registrations]
    if len(identities) != len(set(identities)):
        raise ValueError("同一来源注册表出现重复 ID，需要人工检查提取规则。")
    by_path = {}
    for row in registrations:
        key = (row["registry"], row["legacy_id"].split(":", 1)[1])
        by_path.setdefault(key, []).append(row["legacy_id"])
    summary["cross_namespace_collisions"] = [
        {"registry": key[0], "path": key[1], "legacy_ids": values}
        for key, values in sorted(by_path.items()) if len(values) > 1
    ]
    shared = {}
    for row in fields:
        shared.setdefault(row["field"], []).append(row["source_mod"])
    summary["shared_player_field_names"] = sorted(key for key, mods in shared.items() if len(set(mods)) > 1)
    summary["source_manifest_sha256"] = hashlib.sha256(csv_text(files, ["source_mod", "path", "bytes", "sha256"]).encode()).hexdigest()
    return {
        "registrations.csv": csv_text(registrations, ["source_mod", "registry", "legacy_id", "symbol", "declaration_kind", "source_file", "line"]),
        "player_fields.csv": csv_text(fields, ["source_mod", "field", "java_type", "initial_value", "source_file", "line"]),
        "source_manifest.csv": csv_text(files, ["source_mod", "path", "bytes", "sha256"]),
        "summary.json": json.dumps(summary, ensure_ascii=False, indent=2) + "\n",
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--source-workspace", type=Path, default=ROOT.parent / "yuanchuxiuzhen-1.21.1")
    parser.add_argument("--check", action="store_true", help="只比较来源与已生成清单，不写文件。")
    args = parser.parse_args()
    outputs = generate(args.source_workspace.resolve())
    target = ROOT / "docs/reference/legacy/inventory"
    differences = []
    for name, content in outputs.items():
        path = target / name
        if args.check:
            if not path.exists() or path.read_text(encoding="utf-8") != content:
                differences.append(name)
        else:
            target.mkdir(parents=True, exist_ok=True)
            path.write_text(content, encoding="utf-8", newline="\n")
    if differences:
        raise SystemExit("来源或清单发生变化：" + ", ".join(differences))
    summary = json.loads(outputs["summary.json"])
    print(json.dumps({"check": args.check, "sources": summary["sources"],
                      "collision_count": len(summary["cross_namespace_collisions"])}, ensure_ascii=False))


if __name__ == "__main__":
    main()
