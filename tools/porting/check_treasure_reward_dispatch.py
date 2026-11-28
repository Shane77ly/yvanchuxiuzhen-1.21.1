"""核对 R11 宝箱奖励池 JSON 资产、24 类分派、来源指纹与强类型奖励解释器。

该检查只读取来源过程、生成后的 `reward_tables.json`、JSON 抽样器与奖励构造器。它不能替代完整随机
奖励消费、客户端、专用服务器、多人或 1.18.2 版本实机。
"""
from __future__ import annotations

import argparse
import ast
import hashlib
import json
import re
from pathlib import Path
from typing import Any


ROOT = Path(__file__).resolve().parents[2]
EXTRACTOR = ROOT / "tools/porting/extract_treasure_rewards.py"
TARGET_JSON = ROOT / "src/main/resources/content/treasure/reward_tables.json"
TARGET_TABLES = ROOT / "src/main/java/net/yvanchuxiuzhen/system/treasure/TreasureRewardTables.java"
TARGET_REWARDS = ROOT / "src/main/java/net/yvanchuxiuzhen/system/treasure/TreasureRewards.java"
EVIDENCE = ROOT / "docs/reference/conversion/treasure_reward_tables.json"
SOURCE_RELATIVE = Path("reference/1.18.2yvanchuxitong/src/main/java/net/mcreator/yvanchuxitong/procedures")


def load_categories() -> dict[str, dict[str, str]]:
    """从提取器读取唯一的 24 项分派定义，避免在检查器复制概率表。"""
    tree = ast.parse(EXTRACTOR.read_text(encoding="utf-8"), filename=str(EXTRACTOR))
    for statement in tree.body:
        if isinstance(statement, ast.Assign) and any(
            isinstance(target, ast.Name) and target.id == "CATEGORIES" for target in statement.targets
        ):
            value = ast.literal_eval(statement.value)
            if not isinstance(value, dict) or not all(
                isinstance(category, str)
                and isinstance(subtypes, dict)
                and all(isinstance(subtype, str) and isinstance(suffix, str) for subtype, suffix in subtypes.items())
                for category, subtypes in value.items()
            ):
                raise AssertionError("extract_treasure_rewards.py 的 CATEGORIES 不是字符串映射")
            return value
    raise AssertionError("extract_treasure_rewards.py 缺少 CATEGORIES")


def expected_dispatch(categories: dict[str, dict[str, str]]) -> dict[str, dict[str, str]]:
    return {
        category: {subtype: suffix.lower() for subtype, suffix in subtypes.items()}
        for category, subtypes in categories.items()
    }


def parse_json_dispatch(pools: list[dict[str, Any]]) -> tuple[dict[str, dict[str, str]], set[str]]:
    actual: dict[str, dict[str, str]] = {}
    pool_ids: set[str] = set()
    for pool in pools:
        pool_id = pool["id"]
        if pool_id in pool_ids:
            raise AssertionError(f"JSON 奖励池重复 ID：{pool_id}")
        pool_ids.add(pool_id)
        for key in pool.get("dispatches", []):
            category = key["category"]
            subtype = key["subtype"]
            bucket = actual.setdefault(category, {})
            if subtype in bucket:
                raise AssertionError(f"JSON 分派键重复：{category}/{subtype}")
            bucket[subtype] = pool_id
    return actual, pool_ids


def sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def check_dispatch(categories: dict[str, dict[str, str]], pools: list[dict[str, Any]]) -> dict[str, Any]:
    expected = expected_dispatch(categories)
    actual, pool_ids = parse_json_dispatch(pools)
    entries = [
        {
            "category": category,
            "subtype": subtype,
            "expected_pool": expected_pool,
            "actual_pool": actual.get(category, {}).get(subtype),
            "matches": actual.get(category, {}).get(subtype) == expected_pool,
        }
        for category, subtypes in expected.items()
        for subtype, expected_pool in subtypes.items()
    ]
    extra = {
        category: sorted(set(subtypes) - set(expected.get(category, {})))
        for category, subtypes in actual.items()
        if set(subtypes) - set(expected.get(category, {}))
    }
    missing_categories = sorted(set(expected) - set(actual))
    extra_categories = sorted(set(actual) - set(expected))
    expected_pools = {pool_id for subtypes in expected.values() for pool_id in subtypes.values()}
    return {
        "categories": categories,
        "entries": entries,
        "expected_entries": sum(len(subtypes) for subtypes in expected.values()),
        "actual_entries": sum(len(subtypes) for subtypes in actual.values()),
        "missing_categories": missing_categories,
        "extra_categories": extra_categories,
        "extra_subtypes": extra,
        "missing_pools": sorted(expected_pools - pool_ids),
        "matches": (
            not missing_categories
            and not extra_categories
            and not extra
            and all(item["matches"] for item in entries)
            and not (expected_pools - pool_ids)
        ),
    }


def check_fingerprints(source_workspace: Path, evidence: dict[str, Any], target_bytes: bytes) -> dict[str, Any]:
    source_dir = source_workspace / SOURCE_RELATIVE
    records = []
    issues = []
    for record in evidence.get("inputs", []):
        source_id = record.get("source", "")
        name = source_id.removeprefix("B:")
        path = source_dir / f"{name}.java"
        exists = path.is_file()
        actual = sha256_bytes(path.read_text(encoding="utf-8").encode("utf-8")) if exists else None
        matches = actual == record.get("sha256")
        records.append({
            "source": source_id,
            "path": str(path),
            "exists": exists,
            "expected_sha256": record.get("sha256"),
            "actual_sha256": actual,
            "matches": matches,
        })
        if not matches:
            issues.append(source_id)
    output = evidence.get("output")
    output_path = ROOT / output if isinstance(output, str) else TARGET_JSON
    expected_output = evidence.get("output_sha256")
    actual_output = sha256_bytes(target_bytes)
    output_matches = output_path == TARGET_JSON and actual_output == expected_output
    if not output_matches:
        issues.append("target output")
    return {
        "source_directory": str(source_dir),
        "records": records,
        "records_checked": len(records),
        "records_match": not issues,
        "output": output,
        "output_path_matches_target": output_path == TARGET_JSON,
        "expected_output_sha256": expected_output,
        "actual_output_sha256": actual_output,
        "output_match": output_matches,
        "mismatches": issues,
    }


def find_required(text: str, patterns: dict[str, str]) -> dict[str, dict[str, Any]]:
    result = {}
    for name, pattern in patterns.items():
        match = re.search(pattern, text, re.DOTALL)
        result[name] = {"present": match is not None, "pattern": pattern}
    return result


def check_reward_interpreter(text: str, table_text: str) -> dict[str, Any]:
    api_checks = find_required(text, {
        "techniques.createBook": r"\bTechniques\.createBook\s*\(",
        "skills.createBook": r"\bSkills\.createBook\s*\(",
        "fulu.createReward": r"\bFulu\.createReward\s*\(",
        "liandan.createMedicineReward": r"\bLiandan\.createMedicineReward\s*\(",
        "liandan.createRecipeReward": r"\bLiandan\.createRecipeReward\s*\(",
        "materials.setInstance": r"\bMaterials\.setInstance\s*\(",
    })
    behavior_checks = find_required(text, {
        "empty selection stops current open": r"if \(selected\.isEmpty\(\)\) \{\s*return;",
        "command branch": r"if \(drop\.reward\(\) instanceof CommandReward command\)",
        "permission 4 command source": r"new CommandSourceStack\([^;]+, 4,",
        "suppressed command output": r"\.withSuppressedOutput\(\)",
        "perform prefixed command": r"performPrefixedCommand\(",
        "codec json loader present": r"ContentFiles\.read\(RESOURCE_PATH, TreasurePoolDefinition\.CODEC\)",
        "invalid numeric text returns zero": r"catch \(NumberFormatException exception\)\s*\{\s*return 0;",
    })
    loader_checks = find_required(table_text, {
        "codec json loader present": r"ContentFiles\.read\(RESOURCE_PATH, TreasurePoolDefinition\.CODEC\)",
        "unknown dispatch returns empty": r"if \(pool == null\) \{\s*return Optional\.empty\(\);",
    })
    behavior_checks["codec json loader present"] = loader_checks["codec json loader present"]
    behavior_checks["unknown dispatch returns empty"] = loader_checks["unknown dispatch returns empty"]
    return {
        "owner_api": api_checks,
        "field_and_empty_rules": behavior_checks,
        "owner_api_all_present": all(item["present"] for item in api_checks.values()),
        "field_and_empty_rules_all_present": all(item["present"] for item in behavior_checks.values()),
    }


def default_source_workspace() -> Path:
    candidates = tuple(
        parent / "yuanchuxiuzhen-1.21.1"
        for parent in (ROOT.parent, *ROOT.parents)
    )
    for candidate in candidates:
        if (candidate / SOURCE_RELATIVE).is_dir():
            return candidate
    return candidates[0]


def run(source_workspace: Path, output: Path) -> dict[str, Any]:
    categories = load_categories()
    target_json_bytes = TARGET_JSON.read_text(encoding="utf-8").encode("utf-8")
    pools = json.loads(target_json_bytes.decode("utf-8"))
    target_table_text = TARGET_TABLES.read_text(encoding="utf-8")
    target_rewards_text = TARGET_REWARDS.read_text(encoding="utf-8")
    evidence = json.loads(EVIDENCE.read_text(encoding="utf-8"))
    dispatch = check_dispatch(categories, pools)
    fingerprints = check_fingerprints(source_workspace, evidence, target_json_bytes)
    interpreter = check_reward_interpreter(target_rewards_text, target_table_text)
    result = {
        "check": "r11-treasure-reward-dispatch",
        "source_version": "1.18.2",
        "target_version": "1.21.1",
        "source_workspace": str(source_workspace),
        "dispatch": dispatch,
        "source_fingerprints": fingerprints,
        "reward_interpreter": interpreter,
        "summary": {
            "dispatch_24_entries_match": dispatch["matches"] and dispatch["actual_entries"] == 24,
            "source_30_inputs_match": fingerprints["records_match"] and fingerprints["records_checked"] == 30,
            "target_output_matches": fingerprints["output_match"],
            "owner_api_checks_pass": interpreter["owner_api_all_present"],
            "empty_and_malformed_rules_present": interpreter["field_and_empty_rules_all_present"],
            "static_parity_pass": (
                dispatch["matches"]
                and dispatch["actual_entries"] == 24
                and fingerprints["records_match"]
                and fingerprints["records_checked"] == 30
                and fingerprints["output_match"]
                and interpreter["owner_api_all_present"]
                and interpreter["field_and_empty_rules_all_present"]
            ),
        },
    }
    output.parent.mkdir(parents=True, exist_ok=True)
    output.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    parser.add_argument("--source-workspace", type=Path, default=default_source_workspace(), help="锁定1.18.2来源工程根目录")
    parser.add_argument(
        "--output",
        type=Path,
        default=ROOT / "build/diagnostics/r11-treasure-reward-dispatch/static-parity.json",
    )
    args = parser.parse_args()
    result = run(args.source_workspace.resolve(), args.output.resolve())
    summary = result["summary"]
    if not summary["static_parity_pass"]:
        raise SystemExit("R11 宝箱奖励分派静态核验失败，详见输出 JSON")
    print("R11宝箱奖励分派静态核验通过：24项分派、30个来源过程指纹、奖励所有者API与JSON加载/空分支规则；未执行实机运行。")


if __name__ == "__main__":
    main()
