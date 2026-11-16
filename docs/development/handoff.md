---
topic: handoff
role: guide
status: current
updated: 2026-11-16
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-16）

- 集成主线`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支`port/full-migration-20261112`，核对HEAD为`cb47aa03`。最近三次提交为装备消费者验证、答题根归属/丹药消费、门户两道往返；本页提交后最新HEAD以`git log -1`为准。主线有暂存/未暂存/未跟踪交付，不能写为干净树或整树重放旧补丁。
- UI工作目录`C:/Users/lxc/.codex/worktrees/r11-rewards-next/yuanchuxiuzhen-rebuild-1.21.1`停在detached HEAD `a876affe`且有历史修改；全局进度以集成主线核对。旧工作树/stash全部保留，涉及相关内容时逐文件比较。
- 有效证据和完整缺口只在[统一工作清单](remaining-work.md)及系统正文维护。R06旧两道往返/四道拒绝/重进已通过；四道新维度资源和六道往返夹具已接入，尚无新运行结论。Q203及丹方消费有有限报告，候选未抽中/预置供给边界保持；最新被中断的增量编译无成功结论。
- 三个实现子agent独立树均为`C:/Users/lxc/.codex/worktrees/<名称>/yuanchuxiuzhen-rebuild-1.21.1`，从`cb47aa03`创建：`r07-coffin-ghost-20261116`、`r15-inscribed-decor-20261116`、`r06-world-supply-20261116`。分工在[当前调度](remaining-work.md#当前调度)，不等待旧失效任务。
- 全量目标尚未完成，不推送、不合main；main与本地origin/main为`5cdc045c`，未联网刷新。重型验证唯一执行者串行，768 MB、单worker。

## 下一步

主线收口盘点和文档检查后，逐文件接收三个完整实现包及既有古镜/42号符夹具，统一共享工厂/资源/中文和受影响台账，集中编译；R06续验接入后六道维度和保存重进。只重开新证据影响的问题，旧客户端已修结果与有效单项复用。按实际实现和必要检查逐包提交，集成/编译/夹具数量不等于全量验收。

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
