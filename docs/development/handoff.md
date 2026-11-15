---
topic: handoff
role: guide
status: current
updated: 2026-11-15
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-15）

- 实际分支`port/full-migration-20261112`，本批从`605894d0`接续；最终提交与未提交内容以`git log -1`、`git status`为准。已有实现和验证边界以[开放清单](remaining-work.md)及所属系统月志为准，不重新应用已集成的独立worktree补丁。
- 乾坤道人/门徒已在`b938271d`集成，本批补齐菜单归属、七条台账、烟雾/编号同步、中文和资产转换；编译及六项架构检查通过，`qiankun`客户端单项43条通过且真实保存重进通过，中文编号/加号/金色截图已复看，两次客户端均正常退出并恢复选项。证据与开放边界见[NPC本月记录](../systems/npc/logs/2026-11.md#2026-11-15-乾坤道人与门徒必要单项)。
- 完整移植goal保持active，不推送、不合main。`main`与本地`origin/main`仍为`5cdc045c`，本次未联网刷新远端。

## 下一步

本批已接回任务方块编辑器、11枚固定商人蛋的生产与资源、精血认主/隐藏开灵工具、酒蛊采集夹具及二踢脚/方块烟花/Pao13节庆包；`compileSmokeJava`、`processResources`、文档检查、台账检查和`git diff --check`已通过。固定商人蛋与Pao10资源仍待必要客户端单项；`pao1002.png`保持Q176待核。二踢脚、任务编辑器和采药锄upper只运行必要单项，不重跑已过目录/节庆路径。最新包与证据见[开放清单](remaining-work.md)，当前客户端已保存退出、Java0、选项恢复。

NPC管理本批补齐NPC五基类档案与来源顺序，去除夹具主动资源重载后，768 MB单项已通过四代表实体资质/属性/20END和真实保存重进；报告127 INFO、2个粒子数量采样FAIL、DONE。粒子数量、其他所有者和原版Mob仍开放，本批不重复整轮客户端。祈愿灯首轮必要单项已过；阵基/阵旗/阵元必要单项已过。详细边界只在[开放清单](remaining-work.md)与对应月志维护。

转换、Gradle及游戏唯一执行者串行，768 MB、单worker；每次运行前检查残留客户端，运行后核对终态与进程退出并恢复选项。差异和拿不准的问题先入统一索引，未批准项保持来源，独立移植继续推进。

2026-11-15补充：`guchongconstitutionuse,spiritstone`最新单项报告为79 INFO、0 FAIL、DONE，完整报告及建设场景超时边界见[项目月志](../systems/project/logs/2026-11.md#2026-11-15-酒蛊创造与九档灵石真实服用复验)。此前103 INFO摘要的原报告已被覆盖，不作为可复核验收；Q175/Q176客户端边界仍开放。当前已确认客户端/Java进程为0。保留的stash `wip: preserve current festival and smoke fixes before r11 integration`尚未删除。

当前三个子agent在独立worktree并行推进R10蛊虫05生命周期、R13九档灵石开封与Q162只读审计、R14空间系效果；主线负责共享记录、集成、唯一串行验证。

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
