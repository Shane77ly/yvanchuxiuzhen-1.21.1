---
topic: handoff
role: guide
status: current
updated: 2026-11-14
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-14）

- 实际分支`port/full-migration-20261112`，接续基线`05f0e771`；该基线已完成寻灵盘自然解封与任务商品刷新。本批最终提交以`git log -1`为准。
- 本批接入居民21–30/十蛋、兔笼、临时队伍03页、八储物家具、08飞梭、宝匣，并修Q130结构Mod实体属性格式与Q131无方向库存能力。必要编译、架构与有限客户端单项已过；结果与限制见各系统月志及[开放清单](remaining-work.md)，不代表全系统或两版本验收。
- 22个含Mod实体结构已定向再生，六居民模板属性及重进已验证，其余16未实机；150条附加步高语义仍待核。成功报告、日志、架构XML及八截图归档`tmp/residents-furniture-casket-final/`；首轮失败保留`tmp/residents-cages-team/`。运行选项已按原字节恢复。
- 完整移植goal保持active，不推送、不合main。`main`与本地`origin/main`仍为`5cdc045c`，未联网刷新远端。

## 下一步

先确认本批已提交，不重复复制已集成的居民、兔笼、家具、飞梭、宝匣与临时队伍冻结文件。下一批接入以下交付，主线统一文档/台账、定向转换、必要编译与单项验证：

- 天道宝箱工具：`C:/Users/lxc/.codex/worktrees/r14-treasure-controller/yuanchuxiuzhen-rebuild-1.21.1/tmp/r14-treasure-tool-handoff.md`，三个工具patch；Q131独立patch已集成，勿重复应用。
- 建筑锤：`C:/Users/lxc/.codex/worktrees/decor-building-hammer/yuanchuxiuzhen-rebuild-1.21.1/tmp/r15-building-hammer/handoff.md`，三个新文件及六共享增量；共享文件不可整份覆盖。
- 阴司五NPC仍在`npc-underworld-five`独立树实现，交付后按实际来源/消费者范围集成；阵法与节日族在新的独立worktree接续，不改原冻结包。

新来源差异和不确定项从Q137统一登记，按来源保持至集中裁定；实现、静态核对、目标实机与两版本对照分别记账。转换、Gradle及游戏唯一执行者串行，768 MB、单worker；不要为未变更已通过场景重跑全套。

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
