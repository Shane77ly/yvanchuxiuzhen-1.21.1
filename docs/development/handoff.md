---
topic: handoff
role: guide
status: current
updated: 2026-11-13
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-13）

- 本轮分支：`port/full-migration-20261112`，由方法修订提交 `6d1db082` 建立；本轮最终提交以 `git log -1` 为准，避免在同一提交内自引用哈希。
- 玩法基线：`5cdc045c` 已合并 `port/c-skill-continue` 的 `a9cfe140`，包含此前D材料/生产与D/E世界供给。开始本轮时本地 `origin/main` 同指该提交；未联网刷新远端。
- 当前任务：继续完整移植goal，推进全部批准范围。2026-11-13负责人恢复测试期间的源码核对与独立工作包并行；并行写代码使用独立worktree，主agent统一调度，公共进度文档与Git提交统一收口，重型验证由唯一执行者串行处理。本分支不推送、不自动合main。
- 实现已覆盖C、D和部分E，但Skill及后续多个系统仍未闭合；不把当前修Skill缺口表述为“整个项目退回C”。具体范围与证据见开放工作清单。
- Creature Boss `bcc1418b` 仍只在 `port/skill-damage-area`，未在本玩法基线；后续所属批次先审查再整合。

## 下一步

方法与文档修订已于 `6d1db082` 完成，验证结果见[项目月志](../systems/project/logs/2026-11.md#2026-11-12-移植方法与进度治理修订)。R01火鸟已闭合，来源复核、夹具隔离和必要单项均完成，见[Skill月志](../systems/skill/logs/2026-11.md#2026-11-12-火鸟夹具隔离与单项闭合)。起尸诀完整单机生命周期已闭合：修复1.21.1击杀归属差异导致的成长断链，单项`-PsmokeOnly=corpse`通过，见[Skill月志](../systems/skill/logs/2026-11.md#2026-11-12-起尸诀完整生命周期与击杀成长修复)。

`fabao_013` 的必要单项复跑与冒烟正常保存退出已于 `fa49d789` 提交，包含 `18b59105` 的产品实现及退出修复；结果和仍未覆盖的自然行为见[装备月志](../systems/equipment/logs/2026-11.md#2026-11-13穿界古镜单项复核与正常退出)。首轮卡死和退出夹具失败样本保留，诊断边界见[项目月志](../systems/project/logs/2026-11.md#2026-11-13-冒烟结束保存退出与报告边界)。异火材料成本与补燃料差异已于`df40cc92`收口，来源逐类核对、受控入口费用及真实弹体补料必要单项完成，失败与通过边界见[项目月志](../systems/project/logs/2026-11.md#2026-11-13-异火材料成本与补燃料修复)。

摄虫网`fabao_017/01701`公开收纳、个体物品和既有蛊罐保存路径及困虫兜最小单项已通过，见[装备月志](../systems/equipment/logs/2026-11.md#2026-11-13摄虫网收纳与蛊罐保存)。下一动作由主agent审查并整合独立worktree中已就绪的符箓/书籍工作包，串行处理必要重型验证；蛊虫只续补R10未闭合内容和自然生命周期。R04只续补未覆盖的客户端自然全链、接触／同步和炉鼎耗尽，不重复已通过的费用或法宝场景。

Skill自然生命周期及完整表现仍按R03保留待验，后续只补有效证据未覆盖的路径。Q68只在改变断线保存行为前需要裁定，不阻塞独立任务；World/Creature所属依赖不要求提前在Skill内实现整个后续系统。完整移植goal已按本次明确授权启动；具体调度见[统一清单](remaining-work.md#当前调度)。

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
