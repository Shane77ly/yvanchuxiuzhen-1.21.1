---
topic: handoff
role: guide
status: current
updated: 2026-11-14
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-14）

- 实际分支`port/full-migration-20261112`，本批从`6c5f0be0`接续；最终提交以`git log -1`为准。此前居民21–30/十蛋、兔笼、储物家具、临时队伍03页、08飞梭和宝匣均已提交，不重复应用冻结包。
- 本批已集成建筑锤、天道宝箱工具、阴司五NPC/五蛋、阵元及电光花/呲花/喷花。必要编译、六项架构检查与针对性客户端单项通过；证据及未验范围见各系统月志与[开放清单](remaining-work.md)。阵元Q148已复核Forge40.1.0父类，六面和无方向共库均为来源行为。
- 本批两组证据归档`build/diagnostics/r15-building-treasure-underworld-20261114/`与`build/diagnostics/r15-element-firework-20261114/`，后者含阵元真实保存重进；运行选项按原字节恢复。它们不代表自然获取、完整表现或全系统验收。
- 完整移植goal保持active，不推送、不合main。`main`与本地`origin/main`仍为`5cdc045c`，本次未联网刷新远端。

## 下一步

先用Git确认本批提交后继续以下独立包，勿重跑未改且已有有效证据的旧场景：

- NPC管理主包、来源等级循环/五档资质按钮及`npcmanagement` Smoke已集成；NPC定向内容转换和Smoke编译通过。首轮客户端场景执行到保存重进并暴露两类夹具误报，已按来源字段和累计粒子修正；修正后的768 MB复跑在重进资源加载阶段因Java heap space崩溃，需待内存条件稳定后重跑。冻结增量及验证边界见`C:/Users/lxc/.codex/worktrees/npc-qiankun-disciple/yuanchuxiuzhen-rebuild-1.21.1/tmp/npc-manager-review/smoke-handoff.md`；乾坤道人/门徒不属于该包已完成范围。
- 祈愿灯代码、资源和`festival`定向内容/资源转换已集成，首轮来源行为Smoke已通过；仍保留完整自然获取与表现边界。冻结来源及静态验证边界见`C:/Users/lxc/.codex/worktrees/festival-wish-lantern/yuanchuxiuzhen-rebuild-1.21.1/tmp/r15-wish-lantern/handoff.md`。
阵基检测、母子旗、罗盘及阴系效果代码/资源已从r14-zhenfa-family增量包接入；定向转换通过，编译和formation Smoke待串行执行。实际循环、来源费用/消费者边界和旗目标所有者沿正文核对。

差异和疑点统一进入来源问题索引；Q149起供新包收口，确定目标遗漏按来源修复，来源疑点保持至集中裁定。转换、Gradle及游戏唯一执行者串行，768 MB、单worker。

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
