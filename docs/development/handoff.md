---
topic: handoff
role: guide
status: current
updated: 2026-11-16
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-16）

- 集成主线`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支`port/full-migration-20261112`；本轮可定位实现基线`e0d809aa`，包含R05/R06两独立包、牌匾/牌位`dac6596e`（合并`9764f26a`）、余家具`8552edc4`（合并`e0d809aa`）和建筑笔工厂表列修复`4798ed57`。最新HEAD以Git为准。原25个暂存条目及其它既有脏改动保留；本轮保护前移证据`tmp/dispatch-astra-20261116/r15-final.promotion.json`，此前首批证据同目录`promotion.json`。
- 有效证据和完整缺口只在[统一工作清单](remaining-work.md)及系统正文维护。此前六维度客户端在768 MB下发生区块生成/加载OOM，该六维度自然生成场景仍需按原边界处理，详见[World月志](../systems/world/logs/2026-11.md#2026-11-16-六维度注册修复与区块生成oom)；本批高草单项正常通过，26 INFO、0 FAIL、DONE及11维度保存结果见[Plant月志](../systems/plant/logs/2026-11.md)。主线`runServer`加载至`Done`，但由Ctrl+C结束且没有原生保存退出记录，不作为专服专项验收。Q218仍待来源调度核实。
- R07棺材/鬼怪01等前序成果仍在主线脏工作树；本轮R15牌匾/牌位及余家具已分别独立提交并合并，必要客户端与真实重进通过，详细验证边界见Decor月志；2026-11-16 15:13:36生产与Smoke编译通过，原件为`build/diagnostics/r05-r06-20261116/compile.log`。R09桃栗供给与R05八类飞行法器必要单项已有有效报告，按[当前调度](remaining-work.md#当前调度)复用。三个装饰菜单归属已按来源改为“已读”；灵兽袋染色菜单恢复来源ID且仍为待处理。棺材56、阵法自然链30、城镇12模板842、灵兽袋染色60 INFO均0 FAIL、DONE、退出码0；前三者真实保存重进通过，染色只验方法/库存。城镇日志1条物品框旧坐标ERROR保留，产品修复包括楼梯来源实体材质Q221及动态建筑语言键Q223；详情见该表和所属月志。旧C盘独立树均不作为活动执行目录。
- 全量目标尚未完成，不推送、不合main；main与本地origin/main为`5cdc045c`，未联网刷新。重型验证唯一执行者串行，768 MB、单worker。

## 下一步

下一包为R15签筒/装饰梯子：主线已有生产实现与`animateddecor`选择器，先将该内容按来源边界提取到新的独立功能分支，再核对40刻抽签、动画触发、库存/保存与客户端表现的必要路径；Q214/Q215保持来源，不补造专服客户端入口。当前两R15分支的必要客户端、真实保存重进及有限截图已经通过，证据与未验范围见[Decor月志](../systems/decor/logs/2026-11.md)，无变化不重复运行。其他开放范围继续从[统一工作清单](remaining-work.md#当前调度)接手；旧33路径候选和此前保留树保持。重型任务唯一执行者768 MB、单worker串行，不自动加堆、不推送、不合main。

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
