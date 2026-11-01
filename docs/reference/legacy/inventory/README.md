---
topic: legacy-source/inventory
role: data
status: generated
updated: 2026-11-01
---

# 旧工程静态清单

本目录由 `tools/porting/inventory_legacy.py` 生成，记录两个 1.18.2 工程的注册声明、`PlayerVariables` 字段和来源文件指纹。

| 文件 | 用途 |
| --- | --- |
| [summary.json](summary.json) | 来源相对路径、注册表计数、字段数、跨命名空间同路径碰撞及提取限制 |
| [registrations.csv](registrations.csv) | 注册表类型、旧 ID、Java 符号、来源文件与行号 |
| [player_fields.csv](player_fields.csv) | 两份旧玩家变量的字段、类型、初值与位置 |
| [source_manifest.csv](source_manifest.csv) | 纳入提取范围的文件大小与 SHA-256 |

生成命令（在本项目根目录）：

```powershell
python -B -X utf8 tools/porting/inventory_legacy.py
python -B -X utf8 tools/porting/inventory_legacy.py --check
```

提取器默认读取同级的 `yuanchuxiuzhen-1.21.1`；来源目录变化或换机器时使用 `--source-workspace <path>`。清单是静态来源基线，不代表内容行为、玩家入口或移植完成度。注册声明中无法由当前解析器判定的复杂表达式会进入 `summary.json` 的 `unresolved_declarations`，必须在对应系统人工补核。
