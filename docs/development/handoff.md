---
topic: handoff
role: guide
status: current
updated: 2026-11-17
---

# 交接说明

## 本次执行基线（2026-11-17）

本会话从收尾提交`a98414af`接续，双Mod全量Goal为active，仅排除Q28。原生协作工具可用，已恢复`cultivation_combat_owner`、`world_plant_owner`、`creature_guchong_owner`三名完整模块负责人，均在原D盘独立树继续；三个模块全部“进行中”，职责、具体交付及下一动作只在[完整模块计划及当前调度](remaining-work.md#当前调度)维护。

实际集成树是`D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-20261117`，分支`port/batch0-integration-20261117`。生产基线含`18a4c231`的模块规范和传送阵API；Q68功能`1f222223`已通过必要单项并由`f7156900`非快进合入；Q218五花型随机刻修复`c758428c`已通过受控必要单项并由`cd9227fa`非快进合入；R04异火客户端链路修订`3c9d8c1e`已通过run-02必要单项并由`1ecb095d`非快进合入。最新公共记录提交后的HEAD以Git为准；所有集成操作显式使用该树。

原主树`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`仍保留`port/full-migration-20261112`、`28f05bc7`及全部既有脏改动和25条暂存。本轮未前移、提交或清理它；保护证据仍在集成树`tmp/session-close-20261117/main-protection-check.json`。

R06地府差异检查在功能树`worktrees/rebuild-r06-diling-packed-20261117`、分支`port/r06-diling-packed-20261117`、接收基线`97155e59`、最新WIP`eba1a0f1`继续，候选未合入。原视距run-03退出忙循环已保留线程/堆证据；run-04小模板对照及run-05原视距12/模拟12的只读停服诊断均正常保存退出，run-05为43 INFO、0 FAIL、DONE、保存约1.599秒；停服快照未复现异常holder，故具体触发原因仍待定位。完整地府首次进入与真实保存重进仍未验；World所属证据和卸载回调审查已收口，不再无变化重跑或自动加堆。Q68受控断线重开单项已集成，来源行为调整仍待裁定。

R04机制v2已验证并集成，燃料耗尽整链、完整视觉/自然获取、真实重进、多人与专服仍开放；起尸诀UUID、风刃、灵兽袋v2/指纹修复及R10地上罐等已冻结，具体路径、未验边界和接收顺序只在[当前调度](remaining-work.md#当前调度)维护。公共文档、共享入口和Git由主agent收口；转换、Gradle、客户端及专服唯一队列串行、768 MB、单worker，不推送、不合main。

本页只保留分支、可定位基线与下一动作；系统证据及问题详情见[开放工作清单](remaining-work.md)，跨系统协调见[项目月志](../systems/project/logs/2026-11.md#2026-11-17-q68验证集成与地府退出定位)。

## 历史状态（截至2026-11-16）

- 集成主线`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支`port/full-migration-20261112`；本批功能基线`5bd48e5b`，含签筒/装饰梯子功能`5c313980`与非快进合并`3fc04648`、城镇放置对齐功能`f08359d1`与非快进合并`5bd48e5b`。最新文档提交及HEAD以Git为准。此前`a7b66105`及其已合入功能保持。
- 原25个暂存条目与其他既有脏改动继续保留；本批工作区/index保护快照及前移结果见`tmp/dispatch-sol-20261116-batch2/mainline.before.json`和`promotion.json`，此前两批记录仍在`tmp/dispatch-astra-20261116/`。只清理本批新建、已归档且干净的功能树，旧树和脏组合树保留。
- 实现覆盖、有效证据和未验边界只在[统一工作清单](remaining-work.md)及所属系统正文维护。本批签筒/梯子的必要客户端、真实保存重进与有限截图通过；城镇复用有效十二模板报告，Q223修复后的卷轴名/中心列表画面仍未验。见[Decor月志](../systems/decor/logs/2026-11.md#2026-11-16-签筒与装饰梯子独立包)和[Town月志](../systems/town/logs/2026-11.md#2026-11-16-三类城镇十二模板真实建造夹具)。
- 全量目标尚未完成，不推送、不合main；main与本地origin/main为`5cdc045c`，本次未联网刷新。已有六维度低内存OOM、Q218来源调度、自然获取/完整表现等开放边界继续按统一清单处理。重型验证唯一执行者串行，768 MB、单worker。

## 历史下一步（2026-11-16）

第二批功能已独立提交并按顺序合入；本轮只完成轻量文档与保护收口，没有重做有效客户端验收或启动第三批。下次从[当前调度](remaining-work.md#当前调度)选择尚未闭合的具体功能边界，复用未失效证据；不要重启签筒/梯子或城镇十二模板的已通过单项。完整自然获取、全部表现与多人/专服等仍按所属系统正文接续。

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
