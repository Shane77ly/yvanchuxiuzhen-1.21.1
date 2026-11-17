---
topic: handoff
role: guide
status: current
updated: 2026-11-18
---

# 交接说明

## 本次执行基线（2026-11-18）

本会话从收尾提交`a98414af`接续，双Mod全量Goal为active，仅排除Q28。原生协作工具可用，已恢复`cultivation_combat_owner`、`world_plant_owner`、`creature_guchong_owner`三名完整模块负责人，均在原D盘独立树继续；三个模块全部“进行中”，职责、具体交付及下一动作只在[完整模块计划及当前调度](remaining-work.md#当前调度)维护。

实际集成树是`D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-20261117`，分支`port/batch0-integration-20261117`。生产基线含`18a4c231`的模块规范和传送阵API；Q68功能`1f222223`已通过必要单项并由`f7156900`非快进合入；Q218五花型随机刻修复`c758428c`已通过受控必要单项并由`cd9227fa`非快进合入；R04异火客户端链路修订`3c9d8c1e`已通过run-02必要单项并由`1ecb095d`非快进合入；R08出身奖励与Ling11已由`28391ef9`接收并通过编译，出身奖励八分支受控单项已通过。Ling11首次use单项于2026-11-18通过，双端追踪同步、可见粒子与中文截图均已核验，低视距下正常保存退出；自然获取、全部提示与真实重进仍待。Plant ground功能`a2c9a328`已通过标准转换、双编译和`plantground`受控客户端必要单项，并由`e423170d`非快进合入；79场景断言、81 INFO、0 FAIL、DONE、所有维度保存、Gradle退出0，范围与未验边界见[Plant月志](../systems/plant/logs/2026-11.md#2026-11-18-八种野生植物土壤引用)。当前功能基线为`19c63649`；最新公共记录HEAD以Git为准。所有集成操作显式使用该树。

原主树`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`仍保留`port/full-migration-20261112`、`28f05bc7`及全部既有脏改动和25条暂存。本轮未前移、提交或清理它；保护证据仍在集成树`tmp/session-close-20261117/main-protection-check.json`。

R06地府差异检查在功能树`worktrees/rebuild-r06-diling-packed-20261117`、分支`port/r06-diling-packed-20261117`、接收基线`97155e59`、最新WIP`eba1a0f1`继续，候选未合入。原视距run-03退出忙循环已保留线程/堆证据；run-04小模板对照及run-05原视距12/模拟12的只读停服诊断均正常保存退出，run-05为43 INFO、0 FAIL、DONE、保存约1.599秒；停服快照未复现异常holder，故具体触发原因仍待定位。完整地府首次进入与真实保存重进仍未验；World所属证据和卸载回调审查已收口，不再无变化重跑或自动加堆。Q68受控断线重开单项已集成，来源行为调整仍待裁定。
R08在非地府场景先复现公共停服重排循环；此前`birthinitialrewards`退出等待修复及证据见项目月志。本次`ling11` run-03在视距/模拟距离12、768 MB下完成四项行为断言后，保存Overworld区块时于`ChunkSerializer.write`/`PalettedContainer.pack`发生OOM；停服事件到达但没有全部维度保存、`DONE`或Gradle 0。停服快照记录2601个known holders、1961个visible，原件见`build/diagnostics/ling11-guidance-20261118/run-03-common-exit-wait/`。run-04在视距4下正常保存退出，但模拟距离4被拒收且粒子设为最少，截图不作为视觉验收。run-05改用合法模拟距离5、全粒子及`zh_cn`，取得6 INFO、0 FAIL、DONE、所有维度保存和Gradle 0；停服快照1225个known holders，截图可见粒子和“远古石盘”，options已按哈希恢复，原件见`build/diagnostics/ling11-guidance-20261118/run-05-visual/`。该对照说明视距4的这次场景可在768 MB下保存完成，但不证明高视距OOM的唯一根因或生产卸载问题已闭合；公共生产入口仍保留回调诊断和待定位边界。

R04机制v2、出身奖励/Ling11有限单项与风刃UUID修复的有效证据保持。灵兽袋功能`79d411d1`已由`1752e9d5`合入，修订轮必要客户端及真实重进通过；现有41群系目录与六门户中英文功能`7fafde8c`已由`f0734b81`合入，最终资源合并已核。Smoke Future诊断首轮正常、修订轮普通阶段密集重排最终收敛；具体引用持有者与旧停服问题仍开放。当前集成仍四维度/43群系，候选四道/25群系未接收。好友实时境界/团队镜像功能`2c026475`已由`d028ce5c`合入，必要关系单项与真实重进通过；12个既有成熟Feature台账功能`ba3ddea7`已由`6d28aab3`合入，R10地上罐功能`48b08930`已由`77293e91`非快进合入，必要满槽/原位重进/收罐再放置单项通过，Plant堆肥/燃料功能`94f19440`已由`c298a2a6`合入，转换、编译及原生机器必要单项通过；Q28功法模块文档`f9273e29`已由`141a36bd`合入。Plant ground功能`a2c9a328`由`e423170d`非快进合入；柳垂枝功能`95a1ae3d`已由`50b3ab71`非快进合入，标准转换、生产/Smoke编译及受控放置单项八项断言通过、11 INFO、0 FAIL、DONE且全部维度保存。具体边界见[Plant月志](../systems/plant/logs/2026-11.md#2026-11-18-柳树垂枝原位替换)。尸草功能`3e4a18fb`已由`41155fa8`非快进合入，标准转换、双编译及一次`corpsegrass`七项受控原生检查通过，所有维度保存且Gradle0，范围见[尸草月志](../systems/plant/logs/2026-11.md#2026-11-18-尸草原位替换)。Dungeon模式恢复接口`d5525854`已通过生产编译，由`4876df0e`非快进合入；普通重生尾链与重算粒子前置包仍待接收/运行，见[Dungeon月志](../systems/dungeon/logs/2026-11.md#2026-11-18-普通重生游戏模式公开接口)。Q226门槛纠正及普通妖兽交互功能`d71266a6`已由`d9d7765d`非快进合入，双编译、六项架构检查及一次必要客户端通过，中文与绿色粒子两图已核，范围见[Creature月志](../systems/creature/logs/2026-11.md#2026-11-18-q226外层门槛纠正与普通交互)。旧普通狼异常的来源规格解释已撤回，其余有效袋证据复用；自然供给断链登记Q229。五种Decor土壤支持/掉落功能`2e7bfd97`已由`7015f6f0`非快进合入；标准转换、双编译/六架构和一次plantsoils受控327功能检查通过，真实重进、自然供给和草色画面仍未验，见[Plant月志](../systems/plant/logs/2026-11.md#2026-11-18-五种来源土壤支持与掉落回退)。天魔002完整族功能`c03eb1dc`已由`19c63649`非快进合入，标准转换、双编译/六架构及一次必要客户端通过，真实重进/死亡和近景已核，见[Creature月志](../systems/creature/logs/2026-11.md#2026-11-18-天魔002接收与必要客户端)。下一步接收已完成静态预核的天阙七群系包，保留天阙01出生行并做标准转换/必要运行；属性重算/普通重生、equipment和独立属性入口/Clone别名包已冻结待接；R04负责人继续耗尽E1–E4夹具，World负责人继续无归道闭包，Creature负责人继续棺材/鬼怪01补充包。其余队列按[当前调度](remaining-work.md#当前调度)推进，有效未受影响单项继续复用。主agent收口公共文档/Git，重型队列串行、768 MB、单worker，不推送、不合main。

本页只保留分支、可定位基线与下一动作；系统证据及问题详情见[开放工作清单](remaining-work.md)，跨系统协调见[项目月志](../systems/project/logs/2026-11.md#2026-11-17-q68验证集成与地府退出定位)。

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
