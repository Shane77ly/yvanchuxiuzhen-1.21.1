---
topic: handoff
role: guide
status: current
updated: 2026-11-15
---

# 交接说明

本页只提供实际基线、当前任务、下一动作与环境入口。实现覆盖和开放缺口的唯一调度正文是[当前实现覆盖与开放工作](remaining-work.md)，不要从历史日志里的“下一步”重启旧任务。

## 当前状态（2026-11-15）

- 实际分支`port/full-migration-20261112`，当前基线为`657950ab`，工作树干净。最近集成了答题任意栈归属、天道NBT编辑器装配、Q162运行边界、六组天道门户及其NeoForge POI谓词修正、门户与盘菜客户端资源路径修正，并收口Q171静态调用者核对、Q172按键命名空间核对和十个物品模型纹理目录修正；不重新应用已集成的独立worktree补丁。
- 乾坤道人/门徒已在`b938271d`集成，本批补齐菜单归属、七条台账、烟雾/编号同步、中文和资产转换；编译及六项架构检查通过，`qiankun`客户端单项43条通过且真实保存重进通过，中文编号/加号/金色截图已复看，两次客户端均正常退出并恢复选项。证据与开放边界见[NPC本月记录](../systems/npc/logs/2026-11.md#2026-11-15-乾坤道人与门徒必要单项)。
- 当前验证：`compileSmokeJava`、`compileTestJava`通过；`materialunlock`单项客户端76条检查成功退出并生成`DONE`，无FAIL；门户旧`blocks/`路径、盘菜空纹理路径及六张来源门户贴图已修正，复跑客户端`BUILD SUCCESSFUL`、最新日志`ERROR/FATAL=0`且无Java残留；开发服务端注册表导出1173个方块且`check_ledger.py`通过；`check_docs.py`、Q162静态链、宝箱静态链和`git diff --check`均通过。门户两道已落盘维度往返、缺失维度空目标、多人和保存重进仍未执行。
- 完整移植goal保持active，不推送、不合main。`main`与本地`origin/main`仍为`5cdc045c`，本次未联网刷新远端。

## 下一步

先按[开放工作清单](remaining-work.md)中的R06继续做门户两道已落盘维度的最小往返、缺失维度拒绝和保存重进；运行前保持Gradle/客户端单worker、768 MB串行，并把报告写入World月志。随后再接R05、R09、R10、R11、R13、R14中尚未覆盖的具体消费者，不把静态接线或编译通过当作自然行为完成。

已接回独立交付`a729799e`本命培养必要夹具、`6e3c9b6c`分次传承/家族奖励、`4fb1eacb`固定保护/负硬度转换、`f9c1d82f`多结构生成器、`aacf7e7d`NBT编辑器装配、`96517b5c`Q162运行边界和`fc59ab32`门户闭包；各包仅应用一次，后续只处理当前开放清单中的具体缺口。

三个子agent持续使用各自worktree推进通用NBT编辑器、落魄家族初始奖励、建筑笔等独立缺口。Q197–Q203由对应正文落盘后统一入来源问题索引，后续编号从Q204接续；未批准来源疑点保持来源，不逐项暂停独立移植。不要把独立树交付等同于主线集成或运行通过。

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
