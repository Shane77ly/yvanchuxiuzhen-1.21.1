"""只读检查文档导航、仓库本地链接、主文档归属和状态，不生成缓存或修改文档。"""

from __future__ import annotations

import argparse
from collections import defaultdict
from dataclasses import dataclass
import json
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit


PRIMARY_ROLES = {"entry", "design", "architecture", "balance", "guide", "data", "changes"}
ROLES = PRIMARY_ROLES | {"log", "proposal", "record", "experience"}
STATUSES = {"current", "partial", "draft", "historical", "generated"}
PRESERVED_DIRS = ("docs/reference/legacy/", "docs/licenses/", "docs/structure_exports/")


@dataclass(frozen=True)
class Issue:
    code: str
    path: str
    message: str


def without_code(text: str, *, inline: bool = True) -> str:
    """保留行数，排除围栏代码和行内代码中的链接示例。"""
    lines = []
    fence = None
    for line in text.splitlines():
        match = re.match(r"^\s{0,3}(`{3,}|~{3,})", line)
        if fence:
            if match and match[1][0] == fence[0] and len(match[1]) >= len(fence):
                fence = None
            lines.append("")
        elif match:
            fence = match[1]
            lines.append("")
        else:
            lines.append(line)
    clean = "\n".join(lines)
    return re.sub(r"(`+)(.*?)\1", "", clean) if inline else clean


def metadata(text: str) -> dict[str, str]:
    lines = text.lstrip("\ufeff").splitlines()
    if not lines or lines[0] != "---":
        return {}
    result = {}
    for line in lines[1:]:
        if line == "---":
            return result
        match = re.fullmatch(r"([a-z_]+):\s*(.*?)\s*", line)
        if match:
            if match[1] in result:
                return {}
            result[match[1]] = match[2].strip("\"'")
    return {}


def destination(value: str) -> str:
    value = value.strip()
    if value.startswith("<") and ">" in value:
        return value[1:value.index(">")]
    return re.split(r'\s+["\']', value, maxsplit=1)[0].strip()


def links(text: str) -> tuple[list[str], list[str]]:
    """读取内联及引用式链接；括号路径和中文/空格路径均按完整目标处理。"""
    clean = without_code(text)
    definitions = {
        m[1].strip().casefold(): destination(m[2])
        for m in re.finditer(r"(?m)^\s{0,3}\[([^\]]+)\]:\s*(.+)$", clean)
    }
    clean = re.sub(r"(?m)^\s{0,3}\[[^\]]+\]:.*$", "", clean)
    found = []
    missing = []
    pattern = re.compile(r"!?\[([^\]\n]*)\]")
    pos = 0
    while match := pattern.search(clean, pos):
        pos = match.end()
        if pos < len(clean) and clean[pos] == "(":
            start = pos + 1
            depth = 1
            pos = start
            angle = False
            while pos < len(clean) and depth:
                char = clean[pos]
                if char == "\\":
                    pos += 2
                    continue
                if char == "<":
                    angle = True
                elif char == ">":
                    angle = False
                elif not angle and char == "(":
                    depth += 1
                elif not angle and char == ")":
                    depth -= 1
                pos += 1
            if depth == 0:
                found.append(destination(clean[start:pos - 1]))
        elif pos < len(clean) and clean[pos] == "[":
            end = clean.find("]", pos + 1)
            if end >= 0:
                key = (clean[pos + 1:end] or match[1]).strip().casefold()
                if key in definitions:
                    found.append(definitions[key])
                else:
                    missing.append(key)
                pos = end + 1
        elif match[1].strip().casefold() in definitions:
            found.append(definitions[match[1].strip().casefold()])
    return found, missing


def anchors(text: str) -> set[str]:
    ids = set(re.findall(r'<a\s+(?:name|id)=["\']([^"\']+)', text))
    counts: dict[str, int] = defaultdict(int)
    # 标题中的代码文字仍参与 GitHub 锚点，因此这里只去掉反引号标记。
    clean = without_code(text, inline=False).replace("`", "")
    for match in re.finditer(r"(?m)^#{1,6}\s+(.+?)\s*#*\s*$", clean):
        title = re.sub(r"\[([^\]]+)\]\([^)]*\)", r"\1", match[1])
        slug = re.sub(r"[^\w\-\s]", "", title.lower()).strip().replace(" ", "-")
        suffix = f"-{counts[slug]}" if counts[slug] else ""
        ids.add(slug + suffix)
        counts[slug] += 1
    return ids


def check(root: Path) -> tuple[list[Issue], int]:
    root = root.resolve()
    paths = sorted((root / "docs").rglob("*.md"))
    if (root / "README.md").is_file():
        paths.append(root / "README.md")
    texts = {}
    issues = []
    for path in paths:
        try:
            texts[path.resolve()] = path.read_text(encoding="utf-8-sig")
        except (OSError, UnicodeError) as exc:
            issues.append(Issue("encoding", path.relative_to(root).as_posix(), str(exc)))
    graph: dict[Path, set[Path]] = defaultdict(set)
    owners: dict[tuple[str, str], list[str]] = defaultdict(list)
    for path, text in texts.items():
        name = path.relative_to(root).as_posix()
        preserved = name.startswith(PRESERVED_DIRS) and path.name != "README.md"
        fields = metadata(text)
        if name != "README.md" and not preserved:
            for key in ("topic", "role", "status"):
                if not fields.get(key):
                    issues.append(Issue("missing-status", name, f"缺少文档登记字段 {key}"))
            if fields.get("status") and fields["status"] not in STATUSES:
                issues.append(Issue("invalid-status", name, fields["status"]))
            if fields.get("role") and fields["role"] not in ROLES:
                issues.append(Issue("invalid-role", name, fields["role"]))
            if fields.get("role") in PRIMARY_ROLES and fields.get("status") not in {"draft", "historical"}:
                owners[(fields.get("topic", ""), fields["role"])].append(name)
        targets, undefined = links(text)
        for label in undefined:
            issues.append(Issue("undefined-reference", name, label))
        for target in targets:
            windows_path = bool(re.match(r"^[A-Za-z]:[/\\]", target))
            parsed = urlsplit(target) if not windows_path else None
            if parsed and (parsed.scheme or parsed.netloc):
                continue
            raw_path = unquote(target if windows_path else parsed.path)
            raw_path = re.sub(r"\\([() ])", r"\1", raw_path)
            if windows_path:
                resolved = Path(raw_path).resolve()
            elif raw_path.startswith("/"):
                resolved = (root / raw_path.lstrip("/")).resolve()
            else:
                resolved = (path.parent / raw_path).resolve() if raw_path else path
            if not resolved.is_relative_to(root):
                issues.append(Issue("outside-repository", name, target))
                continue
            if not resolved.exists():
                issues.append(Issue("broken-link", name, target))
                continue
            if resolved.is_dir() and (resolved / "README.md").exists():
                resolved = resolved / "README.md"
            if resolved in texts:
                graph[path].add(resolved)
                fragment = unquote(parsed.fragment) if parsed else ""
                if fragment and fragment not in anchors(texts[resolved]):
                    issues.append(Issue("broken-anchor", name, target))
    for (topic, role), registered in owners.items():
        if len(registered) > 1:
            issues.append(Issue("duplicate-primary", ", ".join(registered), f"{topic} / {role} 重复主文档"))
    entry = root / "docs" / "README.md"
    if entry not in texts:
        issues.append(Issue("missing-entry", "docs/README.md", "总入口缺失"))
    visited = set()
    pending = [entry]
    while pending:
        current = pending.pop()
        if current in visited:
            continue
        visited.add(current)
        pending.extend(graph[current] - visited)
    for path in texts:
        if path != root / "README.md" and path not in visited:
            issues.append(Issue("unreachable", path.relative_to(root).as_posix(), "从 docs/README.md 没有导航路径"))
    return sorted(issues, key=lambda issue: (issue.path, issue.code, issue.message)), len(texts)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--json", action="store_true", help="向标准输出打印 JSON；不写报告文件")
    args = parser.parse_args()
    issues, count = check(args.root)
    if args.json:
        print(json.dumps({"documents": count, "issues": [vars(item) for item in issues]}, ensure_ascii=False, indent=2))
    else:
        for issue in issues:
            print(f"{issue.code}: {issue.path}: {issue.message}")
        print(f"检查 {count} 篇 Markdown，发现 {len(issues)} 项问题。")
    return 1 if issues else 0


if __name__ == "__main__":
    raise SystemExit(main())
