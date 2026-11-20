---
topic: handoff
role: guide
status: current
updated: 2026-11-20
---

# 交接说明

## 本次执行基线（2026-11-20）

本轮固定人间界优先。当前集成树 `D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-continuation-20261119`、分支 `port/batch0-integration-20261117`，当前已合入的功能基线为 `32410f8a`（`Merge gongfa training and inheritance lifecycle`），本轮从 `47cdb863` 接续；`8cc14afd`、`37790119`、`9e9675a1` 与 `7d5288dd` 仅作较早历史定位，未回退到旧基线。Q40 已合入且不再无变化复跑。修炼与战斗、世界与植物、生物与蛊虫三个完整模块由各自负责人继续在原模块树增量推进；负责人自行依据锁定的1.18.2版本源码、资源与调用链整理完整族谱、覆盖矩阵和缺口并持续批量实现，不按单个妖兽、方块或神通切碎任务，也不因单体专项测试未完而停止编码。各模块未提交增量及野羊/结构候选状态见[当前调度](remaining-work.md#当前调度)。先审阅原工作树差异再续改，不整体搬运；接收、必要验证、Git收口和重型验证由主线程串行处理。 负责人要求子 agent 使用 `gpt-6.1-terra`；现有 agent 的实际模型无法由当前接口核实或切换，不应记为已确认。

本轮接收树木家族 `5d22297e` / 合并 `06a44985`、妖兽蛊虫击杀成长族 `ecc28cd6` / 合并 `820561dd`、人间界野生植物及玉林资源族 `0267b2d4` / 合并 `661b4397`、蛊罐及蛊笛放虫状态恢复 `1319661d` / 合并 `61dc40e7`；各批编译与文档检查通过，成长族六项架构检查通过，真实玩法及存档验收边界见所属系统月志。技能装配实例候选 `114df8e6` 保存在 `port/skill-loadout-lifecycle-20261120`，静音客户端前置研习/界面检查失败并发生夹具越界，未抵达本批验证，暂未合入；日志与报告在该功能树 `build/diagnostics/skill-loadout-lifecycle-20261120/run-01/`。 功法装配与参悟 `43c8911c` / 合并 `32410f8a` 已接收，客户端范围及未验项见功法月志。植物空底材列表的来源辅助方法仍有空气谓词；49份地物与转换器已由 `87edf62b` / 合并 `7a790255` 更正，178份输出只读比对一致，实际自然生成和采收重进仍未验。

双 Mod 全量移植仅排除 Q28。实际集成树为 `D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-continuation-20261119`，分支 `port/batch0-integration-20261117`；本轮从 `47cdb863` 接续，现已通过 `8ae16879` 合入成熟单格作物功能 `a6d154cf`，其中 `469f79cc` 的引雷草空战利品回退已非快进合入，技能右键入口与技能书炼器重量门槛已按来源修订；成熟植物Feature的ONLY_IN_AIR谓词先前误删，现已按来源恢复并同步转换器。原 `rebuild-batch0-integration-20261117` 检出改挂 `codex/renjian-wind-candidate-preserved-20261119`。七个风系草稿及其原状态已逐一核对哈希，记录在新集成树 `tmp/renjian-candidate-preservation-20261119.json`。

天阙七群系功能`9e9c4caf`由`292d3a13`、无归十群系功能`29f44f24`由`6e86380d`非快进合入。无归正式转换24资源、双编译/六架构及限定wugui,wuguiportal通过，受控生成真实重进与首次及重进后再次完整往返成立；该批完成时为六个自定义维度/60目标群系。规则、首轮一般门户人间道随机结构OOM和无效自然地形截图边界只在[World正文](../systems/world/architecture.md#无归十群系与门户接收2026-11-18)及[月志](../systems/world/logs/2026-11.md#2026-11-18-无归十群系接收与限定门户客户端)维护。必要限定单项通过不关闭旧高视距OOM/停服重排。

毒液、蓝焰火和水枪物品的原生使用/松手入口功能`478576d5`由`4a40a6fc`非快进合入；定向转换、双编译、六项架构及修订后受控客户端通过，证据与未验边界见[Creature月志](../systems/creature/logs/2026-11.md#2026-11-18-三件妖兽投射物品接收与必要客户端)。

当前仍是七个自定义维度/60目标群系。地灵接收树`a035fa96`保留密集模板同种子复验与失败证据，不合入；修炼战斗v3接收树`92a78462`保留战斗、起尸与异火耗尽/残热接触分项证据。本轮只从该树独立提取人间道所需的普通重生逻辑，不接整包。三个完整模块继续推进，候选、结果边界和下一动作见[当前调度](remaining-work.md#当前调度)。

原主树`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`保持`port/full-migration-20261112`、`28f05bc7`及全部既有脏改动；本次复核为313条工作区状态项（含25条暂存），未在该树操作。不前移、不清理。既有保护证据为集成树`tmp/session-close-20261117/main-protection-check.json`。

主agent统一公共进度/Git与重型队列，转换、Gradle、客户端、专服串行，768 MB、单worker；不加堆、不无变化复跑，不推送、不合main。独立功能按必要检查结果及时`merge --no-ff`，具体证据与旧失败均保留原路径。

负责人要求客户端测试静音：每次启动前检查对应工作树 `run/smoke/options.txt` 的 `soundCategory_master:0.0`，新测试目录也先配置静音；不修改系统全局音量。引雷草空战利品回退已由 `7d5288dd` 合入，仅代表该直接掉落函数的限定验证，不提升植物模块整体状态。后续从[当前调度](remaining-work.md#当前调度)继续三个模块及待验路径。

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
