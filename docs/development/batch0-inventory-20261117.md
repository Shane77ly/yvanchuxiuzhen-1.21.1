---
topic: migration-batch0-20261117
role: record
status: current
updated: 2026-11-17
---

# 2026-11-17 批次0基线盘点

本记录只登记基线、现存改动保护与接续责任，不提升玩法或验收状态。实际模块调度仍在[开放工作清单](remaining-work.md#当前调度)。负责人本次授权从批次0继续双Mod全量移植；Q28是唯一排除项，Q68/Q163等未裁定项保持来源。

开始时本聊天Goal为空，已按本次授权建立active完整移植Goal；没有活动子agent，旧`world_plant_module`未重启。集成分支为`port/full-migration-20261112`，HEAD为`a368950a2a040bc3cb9d1f249c996b633b3ce609`。main与本地origin/main仍为`5cdc045c`，未联网刷新。没有推送或合并main。

主工作树默认`git status --short`为313条；使用`--untracked-files=all`逐文件展开为343条：25条含暂存修改、133条含未暂存修改、189条未跟踪，其中4条为MM，三个计数不能直接相加。326个现存文件与`tmp/dispatch-sol-20261116-batch2/promotion.json`保护指纹相同；另外17个文件是后续方法/范围/文档与台账工具修改，仍保留且不随本批提交。旧暂存25条全部保护，不以清空工作树作为恢复基线手段。

完整逐文件清单、候选责任、未确认来源和处置见`tmp/batch0-20261117-084348/ownership.csv`；原始清单见同目录`inventory.json`与`files.csv`。每树目录保存原始status、二进制暂存/未暂存补丁、文件字节副本、SHA-256和HEAD/index对象标识。责任候选仅用于指派复核，不代表已确认原作者或批准采纳；归属未明内容统一隔离，只读，不移动、覆盖、清理或提交。首次较慢盘点留在`tmp/batch0-20261117-084155/`，以完整的084348快照为准。

共检查44个D盘Git工作树：39个属于当前仓库注册树，5个是中央worktrees下其他仓库的工作树；其中30个有改动。另有空目录`c-history`没有Git标记，原样保留。以下数量均为逐文件展开；其他仓库只盘点，不纳入本移植的实现或集成依据。

| 工作树（相对项目父目录） | HEAD | 分支 | 暂存/未暂存/未跟踪 | 接续处置 |
| --- | --- | --- | --- | --- |
| `worktrees/ldlib2-mcp-tool` | `57493d88` | `codex/ldlib2-mcp-tool` | 0/0/11 | 其他仓库；全部保留，不集成 |
| `worktrees/legacy-production` | `ec5fea90` | `legacy-production` | 0/530/192 | 其他仓库；全部保留，不集成 |
| `worktrees/r05-r06-candidate-20261116` | `a6822c6d` | `(detached)` | 0/22/11 | world_plant_owner只读核对旧包；不直接复用 |
| `worktrees/r05-tool-machine-20261116` | `a2304438` | `(detached)` | 0/2/7 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/r06-reactive-plants-20261116` | `a2304438` | `(detached)` | 0/3/4 | world_plant_owner只读核对旧包；不直接复用 |
| `worktrees/r07-spiritbag-dye-20261115` | `cb47aa03` | `(detached)` | 0/6/8 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/r09-cultivation-command-20261115` | `1ac778c5` | `codex/cultivation-command-smoke-20261115` | 0/0/0 | cultivation_combat_owner只读核对旧包；不直接复用 |
| `worktrees/r09-profession-command-20261115` | `54bf1015` | `codex/r09-profession-command-20261115` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/r12-panel02-20261115` | `62898695` | `codex/r12-panel02-20261115` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/r12-panel05-20261115` | `eba6f98a` | `codex/r12-panel05-20261115` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/r13-town-placement-parity-20261116` | `f08359d1` | `port/r13-town-placement-parity-20261116` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/r15-animated-decor-20261116` | `5c313980` | `port/r15-animated-decor-20261116` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-astra-batch2-20261116` | `922c1a7f` | `port/astra-batch2-integration-20261116` | 16/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-attribute-feedback-20261116` | `a368950a` | `port/attribute-feedback-20261116` | 0/0/0 | cultivation_combat_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-immediate-integration-20261116` | `a368950a` | `docs/q28-scope-and-integration-20261116` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r04-yihuo` | `18b59105` | `port/r04-yihuo` | 0/5/0 | cultivation_combat_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r06-structure-release` | `4612e463` | `port/r06-structure-release` | 0/8/13 | world_plant_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r07-boss-parity` | `1504b3cd` | `port/r07-boss-parity` | 0/0/0 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r08-corpse-sealing` | `4612e463` | `port/r08-corpse-sealing` | 0/5/5 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r08-fulu-special` | `52b32f6e` | `port/r08-fulu-special` | 0/21/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r08-fulu30` | `df40cc92` | `port/r08-fulu30-growth` | 0/36/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r10-creative-use-20261115` | `553bee77` | `codex/r10-creative-use-20261115` | 0/6/0 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r10-guchong` | `f66be020` | `port/r10-guchong-storage` | 0/18/7 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r10-guchong-flute` | `2da61ceb` | `port/r10-guchong-flute` | 21/0/0 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r10-guchong-lifecycle` | `37a48be8` | `port/r10-guchong-lifecycle` | 0/10/2 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r10-remaining-20261115` | `bd571d18` | `codex/r10-remaining-20261115` | 0/0/0 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-r11-book-binding` | `4dda670d` | `port/r11-book-binding` | 0/12/7 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r11-book-reading` | `df40cc92` | `port/r11-book-reading` | 0/19/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r11-consumers-20261115` | `3a615a9a` | `codex/r11-consumers-20261115` | 0/0/2 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r11-remaining-20261115` | `70b169ee` | `codex/r11-remaining-20261115` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r13-economy-bank` | `2da61ceb` | `port/r13-economy-bank` | 37/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r13-quest-block-refresh` | `4612e463` | `codex/r13-quest-block-refresh` | 0/1/5 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-r13-town-building-20261115` | `9e072131` | `codex/r13-town-building-20261115` | 0/2/1 | 协调者隔离保留；后续模块接收前复核 |
| `worktrees/rebuild-spirit-bag-20261116` | `a368950a` | `port/r07-spirit-bag-20261116` | 0/0/0 | creature_guchong_owner只读核对旧包；不直接复用 |
| `worktrees/rebuild-world-runtime-20261116` | `a368950a` | `port/r06-world-runtime-20261116` | 0/12/132 | 144条脏改动；142条与主树同字节，另2条未确认；world负责人只读复核 |
| `worktrees/yihuo` | `c8523742` | `yihuo` | 0/0/0 | 其他仓库；全部保留，不集成 |
| `worktrees/ying-yang-vfx-study` | `bac1c9f1` | `ying-yang-vfx-study` | 0/8/318 | 其他仓库；全部保留，不集成 |
| `worktrees/zongmen` | `991f92e1` | `gongfa` | 0/30/141 | 其他仓库；全部保留，不集成 |
| `yuanchuxiuzhen-rebuild-1.21.1` | `a368950a` | `port/full-migration-20261112` | 25/133/189 | 协调者保护全部旧改动；模块候选逐文件复核 |
| `yuanchuxiuzhen-rebuild-1.21.1/tmp/npc-management-owner-worktree` | `b0c3ff7f` | `codex/npc-management-owner-20261115` | 0/12/17 | 协调者隔离保留；后续模块接收前复核 |
| `yuanchuxiuzhen-rebuild-1.21.1/tmp/r13-economy-followup-worktree` | `d00a1995` | `codex/r13-q162-static-20261115` | 0/0/0 | 协调者隔离保留；后续模块接收前复核 |
| `yuanchuxiuzhen-rebuild-1.21.1/worktrees/rebuild-r11-dungeon-treasure` | `09fbb55c` | `port/r11-dungeon-treasure` | 0/30/58 | 协调者隔离保留；后续模块接收前复核 |
| `yuanchuxiuzhen-rebuild-1.21.1/worktrees/rebuild-r13-economy-rest` | `09fbb55c` | `port/r13-economy-rest` | 0/12/45 | 协调者隔离保留；后续模块接收前复核 |
| `yuanchuxiuzhen-rebuild-1.21.1/worktrees/rebuild-r13-npc-quest-town` | `09fbb55c` | `port/r13-npc-quest-town` | 0/27/44 | 协调者隔离保留；后续模块接收前复核 |

## 第一批安全边界

三模块均从已提交的`a368950a`在D盘新建独立功能分支和worktree，创建后确认无暂存、未暂存或未跟踪修改；这些干净起点不包含主树未提交候选，也不继承旧脏树的完成结论。协调树`worktrees/rebuild-batch0-coordination-20261117`只提交本次新增文档片段和本记录，前序未提交文档差异保持原处。

修炼与战斗先避开主树gongfa/skill的原暂存候选，按R02–R04完整职责复核来源并推进无冲突的异火、修炼和生命周期路径。世界与植物先复核六维度OOM原始诊断和Q218调度；旧世界树142条同字节内容仍是候选，不代表已提交。生物与蛊虫先核对旧Creature增量和干净Guchong生产路径；不重复实现已存在的灵兽袋候选，不补造Q163培养规则。具体接收差异必须说明来源、输入指纹、责任及与旧改动的关系，再由协调者决定独立接入。

每位负责人持续承担完整模块的代码、资源/中文、系统正文与台账/月志、验证计划和交接；可交付功能单位不改变模块总出口。所有公共文件、共享接口、Git和重型验证由协调者收口；模块负责人不自行转换、Gradle、客户端、专服或提交。重型队列初始空闲，本批次0只做Git/文档/指纹盘点；Gradle、游戏、多人、保存重进、1.18.2版本实机和两版本对照均未执行。客户端操作与截图出口继续按[验证参考](../../.claude/skills/minecraft-mod-porting/references/verification.md#客户端交互与视觉验收)。
