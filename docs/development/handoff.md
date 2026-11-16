---
topic: handoff
role: guide
status: current
updated: 2026-11-16
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-16）

- 集成主线`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支`port/full-migration-20261112`，当前HEAD为`9dd9f0e4`。该主线工作树有233条暂存、未暂存和未跟踪路径（19条暂存、66条未暂存、148条未跟踪）；均为既有交付，必须逐文件集成与保留，不得清理或整树重放。UI所在R11树停在detached HEAD `a876affe`，也有未提交修改；全局进度以集成主线和本页证据为准。
- 有效证据和完整缺口只在[统一工作清单](remaining-work.md)及系统正文维护。R06旧报告只证明四道新维度资源接入前的门户行为。2026-11-16六维度增量的`compileSmokeJava compileTestJava`以768 MB、单worker通过，但新世界注册阶段因22个configured feature引用未注册worldgen/feature类型而失败，未进入行为场景；证据在`build/diagnostics/r06-six-dimensions-20261116-registry-failed/result.json`。流体包目前只有静态检查，未正式转换或运行。Q203及丹方消费的有限报告和未覆盖边界见remaining-work及所属系统记录。
- R07棺材/鬼怪01包已在独立树完成静态实现，尚未集成或运行；R15牌匾/牌位实现已进入主线脏工作树，尚未在该集成状态下编译。R13十二模板映射Smoke补充已完成，仍待主线逐文件集成，尚未验证其余模板实际摆放。当前并行实现包及各自基线见[当前调度](remaining-work.md#当前调度)。
- 全量目标尚未完成，不推送、不合main；main与本地origin/main为`5cdc045c`，未联网刷新。重型验证唯一执行者串行，768 MB、单worker。

## 下一步

先接收R06世界供给、R07棺材/鬼怪01和R15牌匾/牌位交付，按文件整合共享注册入口、转换、中文和台账；确认22个worldgen feature类型全部注册后，再串行执行必要编译与六维度门户单项。随后接回R13映射Smoke并继续R09烹饪供给链、R14阵法自然触发/消费链。Gradle、客户端和专服均768 MB、单worker串行；不推送、不合并main。旧客户端已修结果继续复用，只因新证据重开对应问题。

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
