---
topic: handoff
role: guide
status: current
updated: 2026-11-16
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-16）

- 集成主线`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支`port/full-migration-20261112`，本轮接手基线`9dd9f0e4`，最近盘点提交`4313e823`；最新HEAD以`git log -1`为准。接手时有233条既有变更路径（19暂存、66未暂存、148未跟踪），逐文件集成与保留。UI所在R11树停在detached HEAD `a876affe`，也有未提交修改；全局进度以集成主线和本页证据为准。
- 有效证据和完整缺口只在[统一工作清单](remaining-work.md)及系统正文维护。R06的22个feature注册和双流体代码已进入主线，随后编译通过并越过注册阶段；最新客户端在768 MB下进入世界后发生区块生成/加载OOM，没有本轮有效行为报告，详见[World月志](../systems/world/logs/2026-11.md#2026-11-16-六维度注册修复与区块生成oom)。R15在此之后进入主线，尚未编译；Q203及丹方消费的既有有效证据按原边界复用。
- R07棺材/鬼怪01包已在独立树完成静态实现，尚未集成或运行；R15牌匾/牌位实现已进入主线脏工作树，尚未在该集成状态下编译。R13十二模板映射Smoke补充已完成，仍待主线逐文件集成，尚未验证其余模板实际摆放。当前并行实现包及各自基线见[当前调度](remaining-work.md#当前调度)。
- 全量目标尚未完成，不推送、不合main；main与本地origin/main为`5cdc045c`，未联网刷新。重型验证唯一执行者串行，768 MB、单worker。

## 下一步

接回R07棺材/鬼怪01和R06签筒/装饰梯子实现；收口已集成R15牌匾/牌位的共享注册、中文、转换和台账，再集中编译。R06六维度先定位OOM的新证据，禁止无变化重复启动或提高堆；不重复实现已接回的22个feature。R09桃核/栗子自然供给与R14阵法链继续独立实现。后续重型验证仍768 MB、单worker串行，不推送、不合并main。

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
