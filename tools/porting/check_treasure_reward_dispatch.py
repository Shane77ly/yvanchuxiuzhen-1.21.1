"""核对 R11 宝箱奖励表的 24 类分派、来源指纹和奖励所有者接口。

该检查只读取来源过程、生成的奖励表和目标奖励解释器。它不能替代完整随机
奖励消费、空/畸形奖励串开箱、客户端、专用服务器、多人或 1.18.2 版本实机。
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
        category: {subtype: f"table{suffix}" for subtype, suffix in subtypes.items()}
        for category, subtypes in categories.items()
    }


def parse_target_dispatch(text: str) -> dict[str, dict[str, str]]:
    prefix = text.split("    /** 来源", 1)[0]
    actual: dict[str, dict[str, str]] = {}
    category_pattern = re.compile(
        r'case "(?P<category>[^"]+)" -> switch \(subtype\) \{(?P<body>.*?)\n\s+default -> "";\n\s+\};',
        re.DOTALL,
    )
    case_pattern = re.compile(r'case "(?P<subtype>[^"]+)" -> (?P<table>table\w+)\(level, random\);')
    for match in category_pattern.finditer(prefix):
        category = match.group("category")
        if category in actual:
            raise AssertionError(f"目标 select 重复类别：{category}")
        entries = {
            case.group("subtype"): case.group("table") for case in case_pattern.finditer(match.group("body"))
        }
        if len(entries) != len(case_pattern.findall(match.group("body"))):
            raise AssertionError(f"目标 select 类别 {category} 存在重复子类")
        actual[category] = entries
    return actual


def method_names(text: str) -> set[str]:
    return set(re.findall(r"private static (?:String|double) (table\w+)\(double level, RandomSource random\)", text))


def sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def check_dispatch(categories: dict[str, dict[str, str]], target_text: str) -> dict[str, Any]:
    expected = expected_dispatch(categories)
    actual = parse_target_dispatch(target_text)
    entries = [
        {
            "category": category,
            "subtype": subtype,
            "expected_method": expected_method,
            "actual_method": actual.get(category, {}).get(subtype),
            "matches": actual.get(category, {}).get(subtype) == expected_method,
        }
        for category, subtypes in expected.items()
        for subtype, expected_method in subtypes.items()
    ]
    extra = {
        category: sorted(set(subtypes) - set(expected.get(category, {})))
        for category, subtypes in actual.items()
        if set(subtypes) - set(expected.get(category, {}))
    }
    missing_categories = sorted(set(expected) - set(actual))
    extra_categories = sorted(set(actual) - set(expected))
    expected_methods = {method for subtypes in expected.values() for method in subtypes.values()}
    methods = method_names(target_text)
    return {
        "categories": categories,
        "entries": entries,
        "expected_entries": sum(len(subtypes) for subtypes in expected.values()),
        "actual_entries": sum(len(subtypes) for subtypes in actual.values()),
        "missing_categories": missing_categories,
        "extra_categories": extra_categories,
        "extra_subtypes": extra,
        "missing_methods": sorted(expected_methods - methods),
        "matches": (
            not missing_categories
            and not extra_categories
            and not extra
            and all(item["matches"] for item in entries)
            and not (expected_methods - methods)
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
        # Python 的来源提取器通过 read_text(encoding="utf-8") 读取，换行会按
        # universal-newline 规则归一；指纹必须复用这一字节语义，而不是比较
        # 工作区里的原始 CRLF 文件。
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
    output_path = ROOT / output if isinstance(output, str) else TARGET_TABLES
    expected_output = evidence.get("output_sha256")
    actual_output = sha256_bytes(target_bytes)
    output_matches = output_path == TARGET_TABLES and actual_output == expected_output
    if not output_matches:
        issues.append("target output")
    return {
        "source_directory": str(source_dir),
        "records": records,
        "records_checked": len(records),
        "records_match": not issues,
        "output": output,
        "output_path_matches_target": output_path == TARGET_TABLES,
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
        "reward percent split": r'String\[\] parts = reward\.split\("%"\);',
        "empty or malformed reward stops current open": r'if \(!parts\[0\]\.equals\("指令"\) && parts\[0\]\.split\("/"\)\.length < 3\) return;',
        "command branch": r'if \(parts\[0\]\.equals\("指令"\)\)',
        "permission 4 command source": r"new CommandSourceStack\([^;]+, 4,",
        "suppressed command output": r"\.withSuppressedOutput\(\)",
        "perform prefixed command": r"performPrefixedCommand\(",
        "ordinary reward returns stack": r"return stack;",
        "invalid numeric text returns zero": r"catch \(NumberFormatException exception\)\s*\{\s*return 0;",
        "doube spelling retained": r'case "double", "doube" ->',
        "unknown field type ignored": r"default -> \{ \}",
    })
    dispatch_defaults = len(re.findall(r'default -> "";', table_text.split("    /** 来源", 1)[0]))
    behavior_checks["unknown category and subtype return empty"] = {
        "present": dispatch_defaults == 5,
        "pattern": "five empty defaults (four subtype switches plus category switch)",
        "count": dispatch_defaults,
    }
    return {
        "owner_api": api_checks,
        "field_and_empty_rules": behavior_checks,
        "owner_api_all_present": all(item["present"] for item in api_checks.values()),
        "field_and_empty_rules_all_present": all(item["present"] for item in behavior_checks.values()),
    }


def default_source_workspace() -> Path:
    # 工作区路径深度并不固定；通过实际祖先逐层尝试，避免直接索引
    # 不存在的 `ROOT.parents[n]` 在浅层 Windows 路径上触发 IndexError。
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
    target_table_bytes = TARGET_TABLES.read_bytes()
    target_table_text = target_table_bytes.decode("utf-8")
    target_rewards_text = TARGET_REWARDS.read_text(encoding="utf-8")
    evidence = json.loads(EVIDENCE.read_text(encoding="utf-8"))
    dispatch = check_dispatch(categories, target_table_text)
    fingerprints = check_fingerprints(source_workspace, evidence, target_table_bytes)
    interpreter = check_reward_interpreter(target_rewards_text, target_table_text)
    result = {
        "check": "r11-treasure-reward-dispatch",
        "source_version": "1.18.2",
        "target_version": "1.21.1",
        "source_workspace": str(source_workspace),
        "dispatch": dispatch,
        "source_fingerprints": fingerprints,
        "reward_interpreter": interpreter,
        "runtime_validation": {
            "performed": False,
            "excluded": [
                "完整随机奖励消费",
                "空/畸形奖励串真实开箱",
                "客户端、专用服务器、多人",
                "1.18.2版本实机与两版本对照",
            ],
        },
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
    print("R11宝箱奖励分派静态核验通过：24项分派、30个来源过程指纹、奖励所有者API和空/畸形规则；未执行实机运行。")


if __name__ == "__main__":
    main()
