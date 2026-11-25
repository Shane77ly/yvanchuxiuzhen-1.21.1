---
topic: handoff
role: guide
status: current
updated: 2026-11-25
---

# 交接说明

## 当前交接（2026-11-25 底层架构重构阶段 2B WIP 交接）

1. **当前基线**：
   - 主工作树：`D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`（唯一工作树，禁止新建额外 worktree）。
   - 当前分支：`refactor/p2b-quest-economy-codec-json`（从 `port/20261123-parity-integration@7d35dad3` 检出，收尾提交为 `wip: 阶段2B对话与商店JSON/Codec重构待完成烟测交接`，以实际 `git log -1` 为准；**尚未合入**集成分支）。
   - 集成分支 `port/20261123-parity-integration@7d35dad3` 已依次通过 `--no-ff` 合入阶段 1A～1D 与阶段 2A `refactor/p2a-treasure-codec-json`。
2. **负责人最新决定（2026-11-24）与阶段 2B 当前完成度**：
   - 继续执行**定位 B（零旧存档兼容、全面转向强类型组件与 JSON 数据驱动）**；不保留任何运行期旧 NBT 回退代码，不自动推送、不擅自合并 `main`。
   - 阶段 2B 已完成 JSON 提取（`tools/porting/extract_quest_economy_json.py`，生成 `shop_catalogs.json` 39 商店、`dialogues.json` 70 对话树、`structure_presets.json` 11 结构预设、`auction_goods.json` 与 `valuation.json`）、14 个 `.nbt` 结构离线升级、`spawn_data.json` 更新、`economy`/`quest`/`npc` 全套强类型 `Codec` 重构（删除 `CommerceGoods.java`、`NpcStructureData.java`、`QuestBlockRefreshData.java`，运行期 `split("%")` / `split("/")` 清零）及 7 个对应 Smoke 用例更新（详情见[项目月志](../systems/project/logs/2026-11.md#2026-11-25-阶段-2b-对话与商店-jsoncodec-重构wip-交接)）。
   - **已验证**：`.\gradlew.bat --no-daemon --max-workers=1 compileJava compileTestJava compileSmokeJava test`（退出码 `0`，`BUILD SUCCESSFUL in 4m 37s`）；**未验证**：阶段 2B 对应 `runSmokeClient` 场景尚未启动运行。
3. **立即执行的下一动作（按[当前调度：底层架构四大痛点重构方案与执行路线](remaining-work.md#2026-11-24-底层架构四大痛点重构方案与执行路线零旧存档兼容一步到位数据驱动)继续推进）**：
   - **阶段 2B 收尾（当前分支 `refactor/p2b-quest-economy-codec-json`）**：串行运行客户端 Smoke（保持 `run/smoke/options.txt` 中 `soundCategory_master:0.0` 静音）：
     `./gradlew.bat --no-daemon --max-workers=1 -PsmokeOnly=commerce,bank,questjournal,questblockrefresh,questeditor,birthitemdialogue,qiankundialogue,npcmanagement,npcflyingshuttle,npcvillage,npcresidents runSmokeClient`
     如有 Smoke 断言或布局问题修复并重跑全量门禁，随后切回 `port/20261123-parity-integration` 执行 `git merge --no-ff refactor/p2b-quest-economy-codec-json`、删除临时分支并更新月志/路线。
   - **阶段 2C**：检出 `refactor/p2c-creature-ai-codec-json`，将 52 个 `Yaoshou001..054Behavior.java` 与 `NpcMeleeSkills.java` 提炼为 `CreatureCombatProfile` `Codec` + JSON 驱动。
   - **阶段 3**：检出 `refactor/p3-runtime-performance`，消除运行期命令拼接（`performPrefixedCommand`/`performCommand`）并优化大结构缓存。
   - **阶段 4**：检出 `refactor/p4-legacy-bug-cleanup`，集中清理 `source-survey.md` 中登记的 Q01–Q281 原版遗留 Bug 并更新测试断言与系统文档。
4. **每子批次合入门禁**：
   - 每个 `refactor/*` 分支完成后，串行执行 `./gradlew.bat --no-daemon --max-workers=1 compileJava compileTestJava compileSmokeJava test`、对应系统的 `runSmokeClient`（保持 `soundCategory_master:0.0` 静音）、`python -B -X utf8 tools/porting/check_ledger.py`、`python -B -X utf8 tools/docs/check_docs.py` 与 `git diff --check`，通过后以 `git merge --no-ff` 合入集成分支并更新交接进度；不自动推送、不擅自合并 `main`。

## 历史交接（2026-11-24 双源保真修复与边界收口）

当前本地集成分支 `port/20261123-parity-integration`，从 `main@a94dea8a` 开出；本轮未合 main、未推送。源码修复可由 `git log --first-parent a94dea8a..port/20261123-parity-integration` 定位。玩法证据见[当前调度](remaining-work.md#当前调度)。在完成 Q244–Q281 与任务一、五、六审计后，本轮已在同一集成线完成 Q244–Q281 中 30 项差异的代码/资源/标签/语言修复；`Q246`、`Q248`、`Q252`、`Q254`、`Q259`、`Q264`、`Q279`、`Q281` 共 8 项因既存 Smoke 契约、权限安全、服务端权威或跨块破坏风险保留当前实现并明确登记在[项目月志](../systems/project/logs/2026-11.md#2026-11-24-双源差异-q244q281-批量修复与保留边界收口)与[统一问题索引](../reference/legacy/source-survey.md#具体问题与裁定位置)。任务原书、新按钮界面与实机行为链仍待按实际验证门禁执行客户端/专服实测。

## 历史交接（2026-11-22 收口干净状态）

主工作目录 `D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1` 已恢复为**唯一且干净的工作树**，当前分支为 `port/batch0-integration-20261117`（基线包含 `faf99fa1` 及其之前全部已合入成果；最新 HEAD 以 Git 为准）。除 `main`（`5cdc045c`）与 `port/batch0-integration-20261117` 之外的 325 个历史分支和 136 个冗余 worktree（含原 `rebuild-batch0-integration-continuation-20261119` 与原主树 313 条旧改动）已全部打包归档至工作区外 `D:/yuanchuxiuzhen-1.21.1-port/archive/cleanup-20261122/`（含 `all-branches-20261122.bundle` 与 `dirty-worktrees/`）后彻底移除。本批合入与取舍记录见[项目月志](../systems/project/logs/2026-11.md#2026-11-22-接手验收合并与工作树收口)。

原集成树保留的 `build/diagnostics/`、`build/registry-export/` 与 `run/smoke/options.txt`（`soundCategory_master:0.0`）已迁回本主目录。`ef79aec2` 的蛊笛战斗平地 Smoke 夹具及今日新增/合入的合成材料实例修复、功法命中天外陨石、护甲每秒重算、境界提升粒子、战斗与附魔粒子、施法友表 API、修炼资源镜像与箭头怪经验归因均已编译、通过 ArchUnit 结构守卫与文档/台账校验，真实游戏客户端及专服运行尚未执行。

下一动作（2026-11-22 已合入 `main` 并推送 `origin/main`，负责人确认由执行会话补齐**与两个 1.18.2 源 Mod 二次比对发现的 4 处细节遗漏**，详见[当前调度](remaining-work.md#5-2026-11-22-双源-mod-二次比对-4-项补齐任务单)与[项目月志](../systems/project/logs/2026-11.md#2026-11-22-合入-main-推送与双源-mod-二次比对补齐任务单)）：

1. **当前基线**：主工作树 `D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-rebuild-1.21.1`，分支 `port/source-parity-patch-20261122`（基于 `main` `a72b7b98`）。已完成第 5 节双源 Mod 二次比对全部 4 项补齐（14 个数据包标签及 `data_manifest.csv`/`data_skipped.csv`、`/fuwuqimingling` 命令及 `EconomyCurrency`/`Lianqi` API、`oxygenxianshi.png` HUD 叠加层、浓雾群系与雷劫客户端雾效）与 `ownership_assignment.csv` 6 项 `已读` 更新，并通过 `compileJava compileTestJava compileSmokeJava test --tests net.yvanchuxiuzhen.ArchitectureTest jar`、`check_ledger.py`、`check_docs.py` 与 `git diff --check` 全量门禁。待负责人确认后合入 `main`（不自动推送、不擅自合并 `main`）。
2. **立即执行的双源比对 4 项补齐任务单（从 `main` 检出功能分支 `port/source-parity-patch-20261122` 执行）**：
   - **补项 1：数据包标签（Tags）全量重刷补齐**
     - 生成 B1 阶段因实体未注册而跳过的 7 个实体类型标签文件至 `src/main/resources/data/yvanchuxiuzhen/tags/entity_type/`：`fireentity.json`（8 项）、`goldentity.json`（5 项）、`soilentity.json`（4 项）、`treeentity.json`（15 项）、`waterentity.json`（10 项）、`jiunpc.json`（7 项）、`xiuzhe.json`（1 项），使 `Restraint.java` 五行相克与 `HurtStage.java`/`FabaoProjectile.java` 旧商人免伤在运行期生效；
     - 补齐 `data/minecraft/tags/block/mineable/axe.json` 后迁的 3 个方块（`treasure_chest_03`、`treasure_chest_11`、`tiandiyinhang`）与 `data/yvanchuxiuzhen/tags/entity_type/noentity.json` 后迁的 `shitiputuan`；
     - 生成 B1 阶段跳过（“随 world 迁移”）的 5 个原版结构群系标签至 `src/main/resources/data/minecraft/tags/worldgen/biome/has_structure/`：`buried_treasure.json`、`desert_pyramid.json`、`nether_fossil.json`、`shipwreck.json`、`shipwreck_beached.json`；同步更新 `data_manifest.csv` 与 `data_skipped.csv`。
   - **补项 2：`/fuwuqimingling` 管理员规则切换命令（来源 B:`FuwuqiminglingCommand` / `FuwuqiminglingGoProcedure`）**
     - 在 `SystemCommandFlow.java` 注册 `fuwuqimingling`（权限 2，支持 `sell true|false` 与 `weaponlevelup true|false`），经 `system.economy.api` 与 `system.lianqi.api` 公开接口修改对应 GameRule 并输出与 1.18.2 一致的中文提示（严守 ArchUnit 规则，不从 `flow` 直接访问非 `api` 包）。
   - **补项 3：水下氧气数值 HUD 叠加层（来源 A:`OxygentubiaoOverlay` / `OxygenxianshiProcedure` / `OxygenProcedure`）**
     - 迁移 `textures/screens/oxygenxianshi.png`（登记 `screen_textures.csv` 与 `manifest.csv`），在 `PanelClientSetup.java` 中当 `player.isInWater() && player.getAirSupply() < 300` 时于 `(x - 136, y + 111)` 绘制 9×9 图标、于 `(x - 124, y + 111)` 绘制氧气数值字符串。
   - **补项 4：浓雾群系与雷劫视野雾效（来源 A:`TiankongxvanranProcedure`）**
     - 按 1.18.2 `TiankongxvanranProcedure` 规则接入浓雾群系（`biomefog_01..04` 的 `wuqixianshi` 过渡、RGB `0,51,102` 雾色与 `[wuqixianshi, wuqixianshi + 30]` 雾距）及雷劫开启（`thunder_open` 的 RGB `10,10,10` 雾色与 `[8, 32]` 雾距）客户端雾效渲染，严格保持客户端代码在 `client` 子包内。
3. **验证与收口门禁**：
   - 串行运行 `./gradlew.bat --no-daemon --max-workers=1 compileJava compileTestJava compileSmokeJava test --tests net.yvanchuxiuzhen.ArchitectureTest jar`；
   - 运行 `python -B -X utf8 tools/porting/check_ledger.py`（0 项问题）、`python -B -X utf8 tools/docs/check_docs.py`（0 项问题）与 `git diff --check`；
   - 同步更新 `ownership_assignment.csv` 中上述已处理条目的 `已读` 状态、相关系统月志、[remaining-work.md](remaining-work.md) 与本文件，保持工作树干净。

## 历史接手记录（2026-11-22）

此前财神商店受控生成、右键、中文对话/商店、购买红包、保存重进、客户端使用、一次奖励自然拾取和再重进已验证，`festivalremaining` 也覆盖生存/创造红包直接开启；报告及截图在 `build/diagnostics/caishen-purchase-reward-20261122/`。自然出生与遇见、所有奖励档位、Q167多掉落合并总数、多人及专服仍未闭合，见[当前调度](remaining-work.md#当前调度)及经济、节日月志。

## 前次执行基线（2026-11-21）

本轮固定人间界优先。当前集成树 `D:/yuanchuxiuzhen-1.21.1-port/worktrees/rebuild-batch0-integration-continuation-20261119`、分支 `port/batch0-integration-20261117`，本次收尾 HEAD 为 `71cda008`（`fix: expose cultivation level mirror for combat consumers`），其父提交 `e43f9c49` 已选择性接收修炼与战斗共享击杀归因族，前置植物供给族已由 `6db3574f` 接收。负责人最初指定的 `9e9675a1` 仅作接续定位，未回退到旧提交；Q40 已合入且不再无变化复跑。集成树已通过 `git diff --check`、`python -B -X utf8 tools/docs/check_docs.py`（130篇、0项）和串行 `./gradlew.bat --no-daemon --max-workers=1 compileJava`；本次未启动客户端或专服，客户端静音前置仍为实际运行目录 `run/smoke/options.txt` 的 `soundCategory_master:0.0`。三个完整模块仍由各自 `gpt-6.1-terra`、`xhigh` 负责人按完整功能族和端到端路径在原模块树增量推进，不按单个妖兽、方块或神通切碎任务；模块树脏改动不整体搬运，未接收改动不视为集成或验证完成。野羊候选 `29619325` 与结构宝箱候选继续隔离，未编译或运行；原主树保持不动。

### 2026-11-21 收尾交接事实

- **修炼与战斗**：模块提交 `9a27406c` 已选择性接收为集成提交 `e43f9c49`，并以 `71cda008` 补齐公开的 `Cultivation.syncLegacyLevelMirror` 与缺失 import。覆盖直接攻击、技能实体/技能弹体/驯服技能实体的 attacker/killer 归因、死亡收益、`LEVELnumber` 镜像和普通重生前置；只完成静态来源核对与生产编译，真实击杀、技能命中、重生、多人、专服和存档重进未验。
- **世界与植物**：模块负责人本次未产生新生产提交；此前 `5b942ed5` 的谷物/蔬菜可重复供给族已接收为集成提交 `6db3574f`。模块树当前仍有大量暂存、未暂存和未跟踪改动，冰封岩冬季生成/双格采收回退/非精准采集回调及完整自然获取、保存重进未闭合。
- **生物与蛊虫**：本次未产生新生产提交；模块树保留 `3aa8bc5d` 以来的既有脏改动及本轮 `GuchongLifecycle`、蛊罐/蛊笛/灵兽袋等真实入口改动，未将 Smoke、`net/` 临时目录或未审阅内容带入集成树；蛊虫跨入口释放、成长、死亡清槽、容器恢复和完整生物族群自然/战斗/重进仍待接收与验证。
- **交接原则**：下一会话从 `71cda008` 接续，先核对三个模块原工作树差异，再按完整功能族选择性接收；不清理模块树，不回退原主树，不重复 Q40，不编译/运行野羊候选或结构宝箱候选，不推送、不合并 `main`。

31种矿物与霓裳草全群系入口 `21fd57e1` 经 `781e3201` 合入；生产/Smoke编译、静音客户端124群系去重与10维度排序检查通过，实际新区块生成和额外数据包未验，见[世界月志](../systems/world/logs/2026-11.md#2026-11-20-全群系矿物与霓裳草入口)。

共享植物生命周期族 `1226145b` 经 `4859f1fa` 合入；恢复叶距/计划刻/持久叶片、无含水交互、树苗父类计划刻及反应植物来源回退。编译、文档、台账与既有静音 `reactiveplants` 场景通过，自然供给、完整采收/消费与保存重进仍未验，详见植物月志。

三档蛊罐重建与战利品功能 `e0eeecc4` 经 `787de0af` 合入；生产/Smoke编译及静音生命周期场景通过，三档实际菜单全覆盖、额外数据包战利品与自然获取未验，见[蛊虫月志](../systems/guchong/logs/2026-11.md#2026-11-20-三档蛊罐重建与战利品)。

妖兽标签共享恢复功能 `b5681ecb` 经 `9ce6a11c` 合入；生产/Smoke编译、六项架构与静音受控恢复检查通过，自然战斗和真实重进未验，见[生物月志](../systems/creature/logs/2026-11.md#2026-11-20-妖兽共享恢复)。

毒液、蓝焰火、水枪与飞箭共享弹体生命周期 `cd2399ed` 经 `fdfff9bf` 合入；生产编译、文档与台账检查通过，实战和重进未验，见生物月志。野生药草、菌类与木竹13项采收回退 `7975c343` 经 `ffb82327` 合入；生产编译、配置对照、文档和台账检查通过，实际采收、消费及重进未验，见植物月志。七种符箓特效物品使用、弹体击退及技能弹体穿透同步 `54bd9340` 经 `8ebc0fa8` 合入；生产编译、架构、文档和台账检查通过，发射、命中和重进未验，见符箓及技能月志。反应花草采收回退与檀香叶片生命周期 `d46ad747` 经 `35792e05` 合入；生产编译、文档与台账检查通过，自然采收、叶片衰亡和重进未验，见植物月志。四种妖兽技能物品的使用与松手发射及游离光穿透 `0efd84b4` 经 `52ecb4bc` 合入；生产编译、文档与台账检查通过，游戏发射、命中和重进未验，见生物月志。游魂、魔兽及桃木精出生属性次序 `14ea6c56` 经 `e8b92bd9` 合入，生产编译及文档台账检查通过；自然出生和重进未验，见生物月志。技能箭01及03至10首刻读取修饰键 `3b6dc7d0` 经 `bf07de82` 合入，生产/Smoke源码编译及文档台账检查通过；夹具未运行，实际键盘时序、命中和重进未验，见技能月志。地上罐与蛊笛放虫关系次序 `88580eae` 经 `1a019d5f` 合入，生产编译与文档台账检查通过；放虫、好友变化和重进未验，见蛊虫月志。人间界栗子种植、冰封岩双格采收及来源冬季冰湖算法 `c614e939` 经 `479fb6e8` 合入，生产编译、六项架构守卫、文档和台账检查通过；自然生成、实际采收、自然获取及重进未验，见植物/世界月志。妖兽体型与主人跟随 `be797788` 由 `684d3dc6` 合入，生产编译、文档与台账检查通过；自然出生、碰撞表现、跟随和收放重进未验，见生物月志。蛊虫共享实体与单虫释放生命周期 `f2a32bde` 由 `92752fc2` 合入，生产编译、文档与台账检查通过；自然战斗、放虫及保存重进未验，见蛊虫月志。灵兽袋与封尸释放次序 `9b7863f3` 由 `410f7568` 合入，生产编译、文档与台账通过；空等级名、共享好友表与真实重进待验证，见生物月志。轮回生命周期 `b5d05e37` 由 `2b8e1bfd` 合入：静音轮回符受控场景8项通过并正常保存退出；架构修正后三处越界已消除、六项架构检查通过，丹药、寿尽、命令、菜单写回及真实重进未验，见修炼月志。丁香、梧桐与野生植物来源行为 `6e05d8dd` 由 `92423b87` 合入，生产编译、文档与台账检查通过；自然生成、采收、消费及真实重进未验，见植物月志。本轮接收树木家族 `5d22297e` / 合并 `06a44985`、妖兽蛊虫击杀成长族 `ecc28cd6` / 合并 `820561dd`、人间界野生植物及玉林资源族 `0267b2d4` / 合并 `661b4397`、蛊罐及蛊笛放虫状态恢复 `1319661d` / 合并 `61dc40e7`；各批编译与文档检查通过，成长族六项架构检查通过，真实玩法及存档验收边界见所属系统月志。技能装配实例候选 `114df8e6` 保存在 `port/skill-loadout-lifecycle-20261120`，静音客户端前置研习/界面检查失败并发生夹具越界，未抵达本批验证，暂未合入；日志与报告在该功能树 `build/diagnostics/skill-loadout-lifecycle-20261120/run-01/`。 功法装配与参悟 `43c8911c` / 合并 `32410f8a` 已接收，客户端范围及未验项见功法月志。植物空底材列表的来源辅助方法仍有空气谓词；49份地物与转换器已由 `87edf62b` / 合并 `7a790255` 更正，178份输出只读比对一致，实际自然生成和采收重进仍未验。 尸体共享战利品参数及随机源 `65521868` 与蛊罐菜单身份保存 `81681142` 已由 `e7e00a79` 合入，生产编译、文档与台账检查通过；静音采集运行28项无失败但停在等待世界退出后因堆紧张中断，无DONE及正常保存退出，详见生物月志；蛊罐菜单实际行为未验。 好友表、灵纹、出窍与玩家克隆共享链 `d68c3339` 由 `ed3f81a9` 合入；编译、两项数据验证、六项架构、文档与台账通过，多人、战斗与真实重进未验，详见关系和技能月志。

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


成熟食材地物共享生成谓词修正 `2d584ff4` 经 `840c5fcd` 合入；40份成熟作物/菌类/豆科/芝麻配置恢复来源空 placement，生产/Smoke编译通过，自然生成和玩家供给闭环仍未验。蛊虫跨入口生命周期 `309fc6f7` 经 `5b716a05`、`c828976f` 合入；妖兽与箭头共享生命周期 `07d19b54` 经 `2a3af60a`、`027cf43c` 合入；两模块均只完成代码/资源接收与编译，实际自然/收放/重进仍开放。
