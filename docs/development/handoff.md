---
topic: handoff
role: guide
status: current
updated: 2026-11-12
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-12）

- 本轮分支：`docs/porting-workflow-repair`，由主线 `5cdc045c` 建立；本轮最终提交以 `git log -1` 为准，避免在同一提交内自引用哈希。
- 玩法基线：`5cdc045c` 已合并 `port/c-skill-continue` 的 `a9cfe140`，包含此前D材料/生产与D/E世界供给。开始本轮时本地 `origin/main` 同指该提交；未联网刷新远端。
- 当前任务：负责人批准修正移植Skill和文档工作方式。只修改方法、状态与导航；不改变玩法、不运行游戏、不重建完整移植goal。本分支不推送、不自动合main。
- 实现已覆盖C、D和部分E，但Skill及后续多个系统仍未闭合；不把当前修Skill缺口表述为“整个项目退回C”。具体范围与证据见开放工作清单。
- Creature Boss `bcc1418b` 仍只在 `port/skill-damage-area`，未在本玩法基线；后续所属批次先审查再整合。

## 下一步

本次方法与文档修订已完成，验证结果见[项目月志](../systems/project/logs/2026-11.md#2026-11-12-移植方法与进度治理修订)。继续玩法实现时，先处理开放清单R01：核对火鸟检查前置条件、来源期望和空列表异常，再运行必要的单项 `firebird`，不先重跑完整Skill或Cooking。

随后按开放清单处理Skill自身缺口，再接yihuo、equipment与后续工作包。Q68只在改变断线保存行为前需要裁定，不阻塞独立任务；World/Creature所属依赖不要求提前在Skill内实现整个后续系统。当前请求不授权重新建立自动长期goal。

## 先读顺序

1. `AGENTS.md`、本页与[开放工作清单](remaining-work.md)。
2. [移植技能](../../.claude/skills/minecraft-mod-porting/SKILL.md)，只按任务读取相关参考。
3. 当前功能所属系统的README、正文、相关台账和已有效的来源/运行证据。
4. 仅在改动相关边界时查[系统设计](system-design.md)、[工程结构](engineering.md)、[路线](roadmap.md)；不每次重新读全项目历史。

## 本机环境事实

| 项 | 位置与约束 |
| --- | --- |
| 目标版本 | `gradle.properties`：Minecraft 1.21.1、NeoForge 21.1.265，最低支持21.1.228；Java 21 |
| JDK | 已登记Temurin 21.0.9，`C:/Program Files/Eclipse Adoptium/jdk-21.0.9.10-hotspot`；运行前按需核实本机配置 |
| Gradle缓存 | `D:/Gradle`；wrapper 8.8。首次下载耗时属于历史环境记录，不作为每次构建耗时承诺 |
| 来源 | [来源说明](../reference/legacy/README.md)；邻近1.21.1工程仅作工程参考 |
| 目标源码 | `build/moddev/artifacts/neoforge-<版本>-sources.jar`；FML在Gradle缓存 |
| 运行与内存 | [工程结构的验证基础设施](engineering.md#验证基础设施)：Gradle/游戏串行、单worker、默认768 MB堆、显式单项Smoke，不默认全量构建/巡检 |
| 文档检查 | `python -B -X utf8 tools/docs/check_docs.py`；其他检查按[文档维护](documentation.md)选择 |

存档导入、界面复刻与发布许可证等批准边界见[项目范围](../design/scope.md)和[项目决定](../systems/project/README.md)。

## 历史证据

最近Skill结果见[Skill月志](../systems/skill/logs/2026-11.md)，世界装配及低内存专服见[World月志](../systems/world/logs/2026-11.md)，其他系统从[系统导航](../systems/README.md)进入。旧版交接全文保留在Git提交 `a9cfe140` 的本文件中，用于追溯当时状态，不再作为当前调度指令；本次没有删除原始运行报告或改写历史日志。
