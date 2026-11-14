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
- 可定位基线：`ee36413a` 已收口41号符、01蛊虫、书籍答题与18号符四个在途包；`0b22b5c1`登记本轮并行调度，之后已整合蛊笛必要单项成果，本次提交以 `git log -1` 为准。`main`与本地`origin/main`仍为`5cdc045c`，未联网刷新远端。
- 当前任务：继续已激活的完整移植goal，覆盖两个Mod的全部批准范围。R10蛊笛必要单项已通过；R13银行账户与高级通宝玉对话必要单项已通过；公共进度与提交由主agent收口。并行代码使用独立worktree，本分支不推送、不自动合main。
- 实现已覆盖C、D和部分E，但Skill及后续多个系统仍未闭合；不把当前修Skill缺口表述为“整个项目退回C”。具体范围与证据见开放工作清单。
- Creature Boss `bcc1418b` 仍只在 `port/skill-damage-area`，未在本玩法基线；后续所属批次先审查再整合。

## 下一步

R10蛊笛＋六格分页释放GUI已整合并通过本包必要单项，证据见[蛊虫月志](../systems/guchong/logs/2026-11.md#2026-11-14-蛊笛六罐释放与潜行收回)。R13银行账户、登录存取菜单与高级通宝玉单步入口已通过必要客户端单项，127条检查无失败，证据见[经济月志](../systems/economy/logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)；银行方块四选项、商店/出售/拍卖、普通通宝玉购买与完整卡获取仍未接通。下一步整合并验证Creature Boss候选。只读审查未发现已核行为偏差，但发现内容台账和月志状态不一致；接入时先核读并更新飞斧归属，再根据转换/注册证据校正七条内容状态。候选仍在独立工作树rebuild-r07-boss-parity的port/r07-boss-parity分支，未整合到本分支。

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
