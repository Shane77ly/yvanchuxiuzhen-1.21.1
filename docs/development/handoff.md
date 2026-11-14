---
topic: handoff
role: guide
status: current
updated: 2026-11-14
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-14）

- 本轮分支：`port/full-migration-20261112`，由方法修订提交 `6d1db082` 建立；本轮最终提交以 `git log -1` 为准，避免在同一提交内自引用哈希。
- 可定位基线：`ee36413a` 已收口41号符、01蛊虫、书籍答题与18号符四个在途包；蛊笛必要单项随后收口，`31c8ecd9`收口银行必要单项，`09fbb55c`接入三名首领、飞斧与三枚蛋，`f9f3af43`补首领存活重进必要单项，`294de764`登记拍卖拒启动批准；宝箱奖励与开箱族本批已收口，证据见所属月志。本次最终提交以 `git log -1` 为准。`main`与本地`origin/main`仍为`5cdc045c`，未联网刷新远端。
- 当前任务：继续已激活的完整移植goal，覆盖两个Mod的全部批准范围。三个独立实现者分别推进经济剩余链、NPC/任务/城镇、副本/宝藏；主agent负责跨包接口、集成、公共进度、Git提交和唯一串行重型验证。本分支不推送、不自动合main。
- 实现已覆盖C、D和部分E，但Skill及后续多个系统仍未闭合；不把当前修Skill缺口表述为“整个项目退回C”。具体范围与证据见开放工作清单。
- Creature Boss候选已完成来源复核并整合；静态证据、实际运行结果与未验边界见[Creature月志](../systems/creature/logs/2026-11.md#2026-11-14-三名首领与飞斧来源增量)。

## 下一步

首领与宝箱必要单项已通过，范围分别见[Creature月志](../systems/creature/logs/2026-11.md#2026-11-14-首领注册与存活重进单项)和[Treasure月志](../systems/treasure/logs/2026-11.md#2026-11-14-奖励总表与01至11开箱族)。继续集成三个独立实现包，审阅来源与跨系统接口后定向转换、编译及必要自然消费单项。经济包与任务包先闭合银行四选项自然消费路径，再各自完成商店/收购/拍卖和案台/悬赏；副本入口/波次/回程继续实施。跨包接口由Quest保存执行对话，Economy提供菜单及商品会话公开操作。已批准的拍卖与城镇调整、其余来源差异和不确定项从[统一问题索引](../reference/legacy/source-survey.md#具体问题与裁定位置)接续，后续集中处理。按可闭合系统链交付并顺序集成验证，具体未实现范围和承接关系只在[开放清单](remaining-work.md)维护。

书籍答题的自然入口断链、任意栈通用归属与37契约仍开放，见[书籍正文](../systems/book/architecture.md)。World目标树指纹的换行缺陷已修复，修复后完整转换未复跑，自然生成与两版本实机仍待验，见[World月志](../systems/world/logs/2026-11.md#2026-11-13-目标树实现指纹换行基线修复)与[R06](remaining-work.md)。其余开放事项均以统一清单为准。

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
