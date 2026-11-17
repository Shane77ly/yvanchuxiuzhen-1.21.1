---
topic: handoff
role: guide
status: current
updated: 2026-11-17
---

# 交接说明

## 本次执行基线（2026-11-17）

本会话从收尾提交`a98414af`接续，双Mod全量Goal为active，仅排除Q28。原生协作工具可用，已恢复`cultivation_combat_owner`、`world_plant_owner`、`creature_guchong_owner`三名完整模块负责人，均在原D盘独立树继续；三个模块全部“进行中”，职责、具体交付及下一动作只在[完整模块计划及当前调度](remaining-work.md#当前调度)维护。

实际集成树是`D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-20261117`，分支`port/batch0-integration-20261117`。本次收尾前生产基线`18a4c231`已包含模块计划修复`b8b5aedd`及传送阵公开API功能`8e8250fd`的非快进合并；本次公共交接提交后的HEAD以该树Git为准。新会话默认项目目录可能仍是原主树，所有集成操作必须显式使用上述工作树。

原主树`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`仍为`port/full-migration-20261112`、`28f05bc7`。343条展开状态、原25条暂存、索引条目及文件SHA与主树`tmp/session-recovery-20261117/`快照一致；本轮不前移、提交或清理原主树及三个模块树的旧候选。复核记录在集成树`tmp/session-close-20261117/main-protection-check.json`。此前批次0归属证据继续复用，不重新盘点全部旧工作树。

本轮未通过的两个独立功能已保存且未合入集成：R04为`port/r04-yihuo-client-20261117`、`0d73adb9`，工作树`worktrees/rebuild-r04-yihuo-client-20261117`；Q68为`port/r02-skill-open-disconnect-20261117`、`d742f4bf`，工作树`worktrees/rebuild-r02-skill-open-disconnect-20261117`。两树的实际结果、原始报告、冻结包和缺失夹具统一见[当前调度](remaining-work.md#当前调度)及各自系统月志；新会话先恢复模块负责人，再修正失败分类和验证前置，不直接重跑。R06地府接收树`worktrees/rebuild-r06-diling-packed-20261117`仍干净停在`18a4c231`，尚未复制候选。

接续前Java/Javaw进程查询为空；本轮先定向接收Q68截图回调及双端只读类型修复，在原Q68功能树进行必要编译和客户端验证。R04先分类失败、World先补等价差异夹具、Creature先补灵兽袋生命周期；不无变化重跑。转换、Gradle、客户端及专服继续由主agent唯一队列串行、768 MB、单worker；不自动加堆，不推送、不合main。模块负责人只修改并冻结所属文件，公共文档、共享入口和Git由主agent收口。

本页仅保存实际分支、可定位基线和下一动作；详细当前结论维护在[开放工作清单](remaining-work.md)，本次收尾过程维护在[项目月志](../systems/project/logs/2026-11.md#2026-11-17-验证失败保存与新会话交接)。

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
