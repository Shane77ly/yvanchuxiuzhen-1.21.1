---
topic: handoff
role: guide
status: current
updated: 2026-11-07
---

# 交接说明

本页写给接手移植的新会话与负责人。它只给出当前状态、阅读顺序、环境事实和下一步；规则正文在各自文档中，这里只链接。

## 当前状态（2026-11-07）

| 项 | 状态 |
| --- | --- |
| 分支与提交 | `main`（combat 已合入）；当前系统 skill 在分支 `port/c-skill`（已推送到 `origin/port/c-skill`，尚未合入 `main`；当前最新提交以 `git log` 为准） |
| 已完成 | 两个 1.18.2 工程的全量静态调查与索引；系统调查正文（2026-11-02 按[系统设计](system-design.md)重组为 28 个系统、3 个内容族与玩家面板）；通用移植技能；开发环境（构建、开发服务端与客户端加载已实测） |
| 代码 | 公共入口与客户端入口；attribute 系统（贡献汇总、玩家附件、原版属性写入，2026-11-03）；relation 系统（敌友判定、团队、好友与申请界面、按钮 payload，2026-11-03）；xiulian 系统（修炼档案、出身、修炼推进与突破、金丹元婴、丹田界面、血道，2026-11-03）；gongfa 系统（功法定义、功法书组件、研习、功法栏、参悟台，2026-11-03）；combat 系统（伤害结算与 `/shanghai`、85 个附魔数据定义与命中、护甲、挖掘行为、状态效果登记基础与 47 个效果行为、连锁采集、穿石、木人桩，2026-11-04；首次接入 GeckoLib 4.9.3、实体内容表与菜单界面）；`platform/content` 的内容表加载器与工厂（15 种方块、物品、工具、盔甲）按单位登记 1,127 个方块、2,063 个物品与 150 个声音事件，29 个有内容的单位各有一个 `<名称>Setup`；ArchUnit、台账工具、归属证据工具、片段合并与客户端冒烟检查已接入；skill 系统进行中（框架、14 个元素弹体、动画实体基础与火土金冰光风雷水木各系实体技能、剑法、灵纹、火鸟术、起尸诀、虚空裂隙实体与传送、伤害范围、元婴出窍、十种技能箭与箭种选择、身法轻功、技能界面与 HUD 的部分实现，见下节）|
| 批次进度 | B0 完成步骤 1–5 后按负责人指示结束（2026-11-02），B1 于 2026-11-03 出口核对并经负责人认可后结束，当前执行 C：attribute、relation、xiulian、gongfa 2026-11-03、combat 2026-11-04 完成并合入 `main`（xiulian 按依赖方向调到 gongfa 之前，见[功法](../systems/gongfa/README.md)“依赖方向”），combat 2026-11-04 完成并合入 `main`，当前是 skill（进行中，见下节）；[系统设计](system-design.md) 2026-11-02 全部批准并完成迁移；命名空间统一为 `yvanchuxiuzhen`。逐步进度见[月度记录](../systems/project/logs/2026-11.md) |
| 已批准 | 2026-11-02 负责人批准整体架构、[工程结构与长期维护](engineering.md)、[实施路线](roadmap.md)（`3e0585c` 版本），记录见[项目状态](../systems/project/README.md)；不含存档导入、界面复刻程度、license、Q01–Q14 |
| 未执行 | attribute、relation、xiulian、gongfa、combat 以外的玩法实现、1.18.2 实机对照、存档与视觉验收；按路线推进完整移植的 goal 已于 2026-11-07 启动 |

## 进行中

C 玩家与战斗核心，combat 已完成并合入 `main`，当前系统 skill（分支 `port/c-skill`，从 `main` 建立；最新提交以 `git log` 为准）：2026-11-04 第 1 阶段“框架”完成（技能书、技能栏与选择、施放与冷却、命令、藏宝与附赠、霸体与禁施；Q36–Q39；冒烟 85 项），按五个阶段分批提交，阶段表见[技能架构](../systems/skill/architecture.md)“迁移阶段”；skill 全部阶段完成后才合入 `main`。负责人 2026-11-03 指示（/loop）：每完成一个路线步骤即提交、合入 `main` 并进入下一步，直到移植完成或出现重大问题；判断不定的问题先评估影响，轻微的记入文档后继续，重大的停下商议；客户端测试由会话自己完成。运行约定（负责人 2026-11-03）：变换工具、Gradle、开发服务端与冒烟检查逐个运行，不同时进行（负责人的 VS Code 两次因内存不足崩溃，均在大批文件写入之后）。B1 范围内需要新代码类型的行（`skipped.csv`）与其余语言键随所属系统迁移。盔甲挥臂闪烁与药草疑似缺失两项反馈由负责人指示暂缓（2026-11-03）。

已完成（2026-11-03，均已合入 `main`）：
- attribute：Q17 裁定实际属性计入炼器数值，Q18 暂按来源行为待再议；见[属性](../systems/attribute/README.md)。留待其他系统：重算粒子随 particle；提交贡献的真实路径随 xiulian、gongfa、equipment、fulu、economy；equipment 去掉妖化防具的生命修饰符。
- relation：敌友判定、团队与阵营、好友与申请界面；Q19、Q20；见[关系](../systems/relation/README.md)。留待其他系统：方块实体的敌友判定随首个调用者；好友行境界名随 xiulian。
- gongfa：功法定义 67 本、功法书组件、研习、功法栏与提交、功法修炼与闭关经验、升级成长、提示文本、书架藏宝、参悟台（参悟与继承）；Q26–Q28；见[功法](../systems/gongfa/README.md)。留待其他系统：书的右键（研习、书架藏宝、答题计数重置）随 book；研习附赠技能（监听 `TechniqueStudiedEvent`）随 skill；`/technique` 命令随 flow；功法页放入取出随 panel；被闪电击中的功法效果 `EntityThunder` 需要 combat 的效果。
- xiulian：修炼档案与公开操作、出身、修炼推进与突破、闭关、金丹元婴、丹田界面、血道、加点、灵根重置、升仙令与家族令牌；Q21–Q25；见[修炼](../systems/xiulian/README.md)。留待其他系统：雷劫过程原随 combat 推迟，已于 2026-11-07 在当前分支接回（见下文）；远古石盘随 world 的传送阵位置与 particle；传承灵物、家族百宝袋随 material 与 fulu 的实例数据；妖丹实例等级条件随 material；境界提升与血流滴落粒子随 particle；本命培养与进度提交随 equipment；面板加点与丹田按钮、返回分页随 panel；`/yvanchu` 灵根子命令、出身发放物品与 `keepInventory` 随 flow；被闪电击中的功法效果随 gongfa；元婴出窍随 skill。

combat（2026-11-04 完成，合入 `main`）：伤害主链归属阅读与 Q29–Q35；85 个附魔转数据定义并按标签表达类别；状态效果登记基础（`effects.json`、`EffectBehavior`）与 47 个效果的行为；伤害结算（`Shanghai.deal`、`/shanghai`、`HurtHandler`、四个伤害类型）；命中附魔、连锁采集、穿石（破碎的基岩）；木人桩（GeckoLib 首次接入、实体内容表 `entities.json`、设置界面）；公开操作写在[战斗 README](../systems/combat/README.md)，设计、借用事件（`DamageImmunityEvent`、`LethalDamageEvent`、`ArmorStatEvent`、`HitEvent`、`EffectExperienceEvent`、`GatherExperienceEvent`）与各节写在[战斗架构](../systems/combat/architecture.md)；开发客户端 `CombatCheck` 46 项，全系统冒烟 254 项全部符合。留待其他系统：自定义粒子随 particle 登记后接入发出处；`qishi`、`lingwen`（skill）、`starsky`（fulu）订阅 `HitEvent`；`breakfree` 随 flow；淬毒归 lianqi（Q35）；伤害范围指示实体 `Shanghaifanwei` 归 skill；`effect_023`、`024` 归 yihuo，`effect_035` 等归 skill、xiulian、cooking、creature、guchong（登记为标记）。

xiulian 推迟的完整雷劫已于 2026-11-07 在当前分支接回：`ThunderBegin`、劫云 `Jieyvn`、天雷实体 `Tianjie03/05/07` 与七种天雷 `Leijie0101–0107`。通过具体伤害请求事件接入 combat，保持 combat → xiulian；共享模型与渲染移至 platform。仅一次雷劫专项 5 项全部通过，build、ArchUnit 及专用服务器启动/正常停服通过，实体导出 67 个。Q56、时序与未验收范围见[修炼架构](../systems/xiulian/architecture.md)“完整雷劫”。弓与拂尘已在下述 equipment 批次接回；下一步为其余 equipment、yihuo、world 依赖及视觉收口。

equipment 弓与拂尘依赖批次（2026-11-07，前一提交 `5d0fcff`，本批提交以 `git log -1` 为准）：18 把弓保留剑类型并接回来源条件流程、九把拂尘的挥动与选中冷却、隐藏物品及弹体已接回。Q57 记录两个来源均未找到 `synchronizedata.rightclicking` 写入者，自然右键断链保持；不补造鼠标同步。build/ArchUnit、检查源集编译通过，仅一次 `-PsmokeOnly=equipmentweapon` 五项全部符合并 DONE，含人工标记的弓蓄力/扣费/普通箭存档及拂尘入口/命中/寿命；客户端正常保存退出。专用服务器一次启动到 Done，原生 stop 正常保存退出，ERROR 0，实体导出 68 个。证据在 `tmp/equipment-{build,smoke,server}.log`、`tmp/equipment-report.txt`。下一步按 CodeGraph 与来源核对品阶、饰品飞行减耗、妖化，再接 yihuo/world 依赖；旧神通不重跑，完整表现与两版本对照未验收，skill 未完成，不合 main。

**下次开工的第一步**（先于下面的条目）：
- `git status` 应干净，位于 `port/c-skill`（唯一未跟踪项 `.eclipse/` 是 IDE 的启动配置，不属于本项目提交）。
- 已运行一次 skill 冒烟（`gradlew runSmokeClient -PsmokeOnly=skill`）：报告以 `DONE` 结束，精简后的 `CorpseCheck` 28 项全部符合；其余检查也完成。`LingwenCheck` 的数量 1/2 差异已由 Q48 解释为来源同序的首刻命中时序，不再重复整套冒烟。
- 虚空裂隙的实体、传送与公开入口已实现，`VoidRiftCheck` 12 项通过（`-PsmokeOnly=voidrift`，往返使用 smoke 测试维度，正式维度仍随 world）；本次不再重复。伤害范围实体与 `Skills.showDamageArea` 已实现，`DamageAreaCheck` 6 项通过；九处 creature 生成者仍待接入，控雷阵落雷已接回。元婴出窍的实体与 `Skills.toggleYuanying` 已实现，`YuanyingCheck` 5 项通过；菜单按钮与出窍粒子已接入，视觉认可待核。十种技能箭与服务端选择已实现；弓入口随 equipment、轮盘已在阶段 5 接入，阳火炙烤持续行为随 yihuo。身法与轻功 7 项针对性检查已通过，不重复运行。阶段 5 的界面、五个功能入口、C 格挡和技能 HUD 已部分实现；一次 skillui 检查 9 项中 8 项通过，首项检查时序修正后仅编译，未重跑。剩余 26 项已核清，满天花雨已补齐且两项检查一次通过；315种粒子的提供器与完整资源已登记并通过粒子专项检查，元素弹体、灵纹弹和技能箭的粒子发出处已接回，动画实体、生命感知、轻功、研习、出窍和自爆的有效粒子调用也已接回；共享落雷命令与彩色闪电已接回天雷术、五雷轰顶和控雷阵；完整雷劫主路径已通过；下一步处理 equipment/yihuo/world 依赖与视觉收口。
- 本机有一个小陷阱：在 bash 的 here-document 里写含引号的长 Python 脚本会因引号错位而整段不执行，改用 Write 写到暂存目录再运行。
- `port/c-skill` 已推送到 `origin/port/c-skill`；`.eclipse/` 仍是唯一未跟踪的 IDE 配置文件。

下一步具体操作（skill；做法同前几个系统）：
0. 已提交（分支 `port/c-skill`，最新提交以 `git log` 为准）：第 1 阶段框架；第 2 阶段元素弹体 14 个；第 3 阶段动画实体基础与火、土、金、冰、光、风、雷、水（水蛇鞭、水连鞭、激流波、水龙吟、空浮球、陨河术、水灵阵、玲珑阵）、木（乱枝、灰木剑、乱木之种、灰木树梯、木篱术、毒蔓鞭、回身鞭、地蔓鞭）、剑法（`SwordEntities`、`WeaponArts`；平台 `ItemSwingEvent` 触发，2026-11-05）、灵纹（`LingwenHit`、`LingwenEntities`、`LingwenBolt`；Q45）、火鸟术（`FireBird`，基类 `TamedSkillEntity`；2026-11-07；Q46）、起尸诀（`Corpse`、`CorpseAi`、`Corpses`；2026-11-07；Q47）；虚空裂隙实体、生命周期与传送（`VoidRift`、`Skills.openVoidRift`；2026-11-07；Q49；按钮随阶段 5，正式维度随 world）；冒烟 `-PsmokeOnly=skill`（`SkillCheck`、`SkillProjectileCheck`、`SkillEntityCheck`、`SwordArtCheck`、`LingwenCheck`、`FireBirdCheck`、`CorpseCheck`；`Arena` 平地由后四者共用，此前弹体检查在不平地形上整批失败；动作列表类检查继承 `ActionCheck`）。读过程用 `TERSE=1`（去样板）、`DIFF=1`（同模板族）；新增元素族的做法见 `entity/FireEntities` 等：`SkillEntities.register` 里登记行为表，`SkillReleases.register` 登记施放，`mark_ownership.py`、`build_ledger.py`、`mark_ledger.py 已注册`、`convert_content.py`，冒烟加用例，再标 `行为已迁移`。
1. 第 3 阶段余项（按元素）：伤害范围实体与 `Skills.showDamageArea`、元婴出窍与 `Skills.toggleYuanying` 已实现；九处 creature 生成者与 `控雷阵` 经 `/lightninglanding` 的落雷已接回，生成者随 creature，元婴出窍和死亡粒子已接回。天雷术、五雷轰顶已接入 combat 共享落雷，两个入口的合并检查因选择取消失败，检查修正后仅编译。拂尘 `Weaponhossu01DangShiTiHuiDongWuPinShi` 的发射已由 equipment 接回，本批五项专项通过。可驯服实体（火鸟、尸体）已完成，基类与共用件见[技能架构](../systems/skill/architecture.md)“可驯服实体”；尸体的 `CorpseAi.breakFree` 与 `finish` 对应妖兽共用的 `Yaoshou001skillzhengtuo`、`Entityjinengjieshu`，creature 迁移时再决定是否公开。
2. 阶段 2：十种技能箭的实体、模式、命中、掉落、原生模型、内爆效果与服务端箭种选择已实现（Q50；共用实体命中过程实际 551 行）；`Lingwentexiao` 已随灵纹实现。待接入：equipment 的弓蓄力/发射/扣资源与 Ctrl/Alt 输入、yihuo 的炙烤持续行为；自定义粒子已接回。`SkillArrow00–10skill` 已核对为孤立过程，`ArrowSkillchufa` 仅有游离 Blockly 引用；`skill_arrowxinruzhishui` 已登记有益标记身份，未形成自然入口（Q53）。本批 `skillarrow` 冒烟 12 项全部通过并以 `DONE` 结束；最后 `build`、检查源集编译、开发服务端启动（ERROR 0 条）、台账与来源指纹检查通过。第 08 箭云命令后续静态修正已编译，该分支未运行；弓与完整炙烤链未验收。下一步为下条阶段 4–5。
3. 阶段 4 身法与轻功：`SkillMovement`、`SkillFlight`、W/S/A/D/空格/F 按下沿、每刻冷却、掠空与二十刻耗灵力、禁空已实现，7 项针对性检查一次全部通过（`-PsmokeOnly=skillmovement`），`build`、台账与文档检查通过；专用服务端启动到 Done、ERROR 0 条，未重跑旧神通。饰品飞行减耗由 equipment 监听 `FlightEnchantmentEvent`，有效轻功粒子已接回。阶段 5 已部分实现：G 键原生无槽技能菜单、四技能/翻页/十一箭种/连招、虚空/出窍/扫描/丹火/自爆、C 格挡显示与 Gedang 八刻移除，panel 的六组冷却/格挡/弓蓄力 HUD。Q52 记录扫描无粒子、自爆立即死亡和背景值 6 无写入者；保留来源行为。格挡不减伤，品阶经 `WeaponStatEvent` 的 `lianqizong.item_level_num`，lianqi/equipment 监听尚未接入。妖化依赖 equipment，丹火放置随 yihuo，出窍和自爆的有效粒子已接回。
4. 阶段 5 验证：仅一次 `-PsmokeOnly=skillui`，8/9 项通过并 `DONE`；首项检查早于客户端菜单打开（后续菜单按钮主路径正常），已拆开发按键与初始化、后移截图，仅重新编译未重跑。HUD 已查看，默认小窗口右侧冷却裁切；菜单截图早于界面，布局视觉认可尚未完成。最后 `build compileSmokeJava`、ArchUnit、专用服务端启动、台账、文档与来源指纹检查通过；服务端终端仍不转发 stop，任务经中断结束，未验证优雅停服。上一实现提交为 `df8f929`；本批代码与本页同一提交，最新提交号以 `git log -1` 为准。skill 归属 401/401 已读；满天花雨补齐五枚扇形飞叶，`skillremainder` 两项一次通过并 DONE。第 08 箭合成过程只显示111、不附毒（Q53）；十配方已逐项核对。70 个已登记效果的来源图标已进入资源转换；build（含 ArchUnit）与专用服务端启动/正常停服通过，效果导出增至70。粒子库前置已完成类型与提供器登记，弹体侧粒子已接回，其余有效 skill 发出处已接回，完整雷劫已在后续批次接回，下一步处理 equipment/yihuo/world 依赖及视觉认可；不得据此宣称 skill 完成。
5. creature 迁移时：妖兽技能放出 `skillwind_05`、`skillwind_06`，需要 skill 提供 api 放出技能实体（写入攻击量、攻击者、`mubiaoUUID`、`endtick`），尚未提供。
6. skill 全部阶段完成后更新 README、正文、月度日志与本页，合入 `main` 并推送，再建 yihuo 的分支。另有两项小事：gongfa 补 `/technique` 命令；`PlayerRightClick` 的开灵附魔分支归 equipment。

粒子库前置（2026-11-07，本批前一实现提交 `3406d99`；本批提交号以 `git log -1` 为准）：315个来源粒子全部已读并登记原生提供器、参数、JSON及3625张独立贴图。particle归属315/315已读，creature705/586已读，fulu230/154已读；七个纯粒子归属已转入particle。仅一次 `-PsmokeOnly=particle` 两项通过并DONE，截图样本可见；build与ArchUnit、专用服务端启动/正常停服通过，ERROR0、导出315个粒子，台账核对无问题。证据为 `tmp/particle-{build,smoke,server}.log`、`tmp/particle-report.txt` 和 `build/smoke/screenshots/particle-library.png`。粒子台账保持“已注册”，弹体侧发出处随后已接回，动画实体、生命感知、轻功、研习、出窍与自爆的有效调用也已接回，全量视觉与两版本对照未执行。后续按来源逐处区分服务端 `sendParticles` 和客户端 `addParticle`；服务端原本为空的 `Level.addParticle` 不改成广播。

注意（2026-11-04 发现并修正）：此前 `SkillEntity` 默认纹理统一为透明的 `kong`，现在改为各实体来源 `define(TEXTURE, …)` 的纹理（`entity_models.json` 的 `texture`）；冒烟检查看不到画面，阶段 5 实机核对动画实体时要确认各实体可见、动画与碰撞箱（空浮球、陨河术有碰撞箱放大）正确。

测试要精简（负责人 2026-11-07 指示）：冒烟每步只写主路径的少量断言，整套每步只运行一次，不逐功能细测，纯检查代码的修正不重跑整套（改好、编译、如实写明未运行）。`LingwenCheck` 的 1/2 差异已记录为 Q48，检查改为接受来源顺序产生的两种结果；本次不再重复整套冒烟。

验证习惯（会话内）：按改动选择必要检查；内容表变化时运行 `convert_content.py`，新玩法只运行对应的小范围冒烟（含编译），不重复已通过的旧神通。纯检查代码修正只编译并记录未重跑。构建含结构检查；注册变化时启动一次服务端刷新导出，再运行 `check_ledger.py`。转换工具、Gradle 与游戏进程逐个运行。终端需向服务端发送 stop 时，`exec_command` 使用 `tty=true` 保留可写输入，并使用 `--no-configuration-cache --init-script tools/porting/server_stdin.init.gradle` 接通标准输入；2026-11-07 已实测正常保存、停服与任务成功退出。

其余 skill 粒子批次（2026-11-07，前一实现提交 `5f374c4`，本批提交号以 `git log -1` 为准）：有效来源粒子调用已接回，Q54 保留未绑定的金轮死亡过程。仅一次 `-PsmokeOnly=skillvisual` 两项通过并 `DONE`，客户端正常保存退出；血雾截图可见，光爆仅确认客户端后续粒子计数，截图未捕获可见效果。build、ArchUnit、台账与文档检查通过。证据在 `tmp/skill-other-particles-{build,smoke}.log`、`tmp/skill-other-particles-report.txt`。共享落雷与完整 xiulian 雷劫已在后续批次接回，下一步处理 equipment/yihuo/world 依赖与视觉认可。skill 仍未完成，不重复旧神通套件。

共享落雷批次（2026-11-07，前一实现提交 `8a78be4`；本批提交号以 `git log -1` 为准）：已实现 combat 共享 `/lightninglanding`、彩色闪电及技能落点；补出来源内部嵌套注册，台账总数 4939，来源指纹未变。Q55 保留空团队词导致参数前移。共享落雷验证（2026-11-07）：仅一次 `-PsmokeOnly=lightning`，四项中三项通过并 `DONE`，范围雷伤、红色同步/渲染器及控雷阵命令均走真实世界路径。两个技能入口的合并断言失败，检查辅助方法连续选同槽导致第二次取消；已在选择前清空并编译，未重跑，不能记为两个入口实机通过。截图早于有效画面，完整视觉待核。build（含 ArchUnit）通过，专用服务器启动、全部世界保存及正常退出通过，ERROR 0，实体导出为 63 个。该共享落雷批次未执行完整 xiulian 雷劫，后续实现与验证见完整雷劫批次。 证据在 `tmp/lightning-{build,smoke,server}.log`、`tmp/lightning-report.txt`、`tmp/lightning-check-compile.log` 及 `build/smoke/screenshots/lightning-red.png`。完整雷劫已在后续批次接回，其余跨系统依赖和视觉仍待闭合；不重跑旧神通套件。

## 先读顺序

1. 仓库根 `AGENTS.md`：项目档案、当前关口、工程与文档规则。
2. 本页。
3. `.claude/skills/minecraft-mod-porting/SKILL.md`：通用移植规则与系统工作单（规则正文）。
4. [工程结构与长期维护](engineering.md)：代码目录、边界检查、内容表、资源转换、台账、依赖治理、并行协作。
5. [系统设计](system-design.md)：系统划分、归属判定、依赖规则与公开操作。
6. [整体架构](architecture.md)与[数据与运行时](data-and-runtime.md)：源码边界、调用组织、状态归属。
7. [实施路线](roadmap.md)：批次、出口和系统完成条件。
8. 所接手系统的 `docs/systems/<系统>/README.md` 与 `architecture.md`。

## 开工前必须确认

AGENTS.md 的“当前关口”写明架构未批准时，只能做调查、文档与工具工作，不写玩法代码。2026-11-02 负责人已批准架构、工程结构与路线并记入 AGENTS.md 当前关口与[项目状态](../systems/project/README.md)，B0 已开始。批准只覆盖这三份文档；下列事项仍待负责人决定，遇到时在所属系统实现前提请裁定：

| 事项 | 当前方案 | 位置 |
| --- | --- | --- |
| 1.18.2 世界与玩家存档是否导入 | 未承诺 | [范围](../design/scope.md) |
| 界面复刻程度 | 保留全部操作与反馈，布局另评审 | [范围](../design/scope.md) |
| 发布 license | 暂填 All Rights Reserved | [架构](architecture.md) |
| 来源矛盾 Q01–Q14 | 逐项裁定，实现所属系统前解决 | [调查问题表](../reference/legacy/source-survey.md) |

## 本机环境事实

| 项 | 值 |
| --- | --- |
| JDK | Temurin 21.0.9（`C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`）；另有 JDK 17 可用于运行 1.18.2 工程（未验证） |
| Gradle | wrapper 8.8；GRADLE_USER_HOME 为 `D:\Gradle`。本机网络约 25–40 KB/s，依赖变化后的首次构建可能需二十分钟以上（2026-11-02 升级到 NeoForge 21.1.265 用时 24 分钟），日志在 `--console=plain` 下不显示下载进度，可看 `D:\Gradle\caches\modules-2\files-2.1` 与 `D:\Gradle\.tmp` 的文件变化判断是否仍在下载 |
| 来源工作区 | `D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1`，两个 1.18.2 工程位置见[来源说明](../reference/legacy/README.md) |
| 目标源码 | 构建后的 `build/moddev/artifacts/neoforge-<版本>-sources.jar`，含 Minecraft 与 NeoForge 源码；FML 在 Gradle 缓存 `net.neoforged.fancymodloader` |
| 构建 | `./gradlew build` |
| 运行 | `./gradlew runServer`、`./gradlew runClient`；运行目录 `run/`（已忽略） |
| Git | `origin` = https://github.com/Shane77ly/yvanchuxiuzhen-1.21.1 （公开）；凭据由 Git Credential Manager 保存的 `Shane77ly` 账户提供，走 HTTPS（本机未配置 GitHub SSH）；提交作者 `Lix <207489130@qq.com>`；`.gitattributes` 规定换行 |
| 文档与来源检查 | `python -B -X utf8 tools/docs/check_docs.py`；`python -B -X utf8 tools/porting/inventory_legacy.py --check` |

运行检查注意：

- `runServer` 启动后等待控制台输入，不会自行退出。结束 Gradle 任务后按命令行查找残留进程并结束：

  ```powershell
  Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'yuanchuxiuzhen-rebuild' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
  ```

- 开发运行的专用服务器不要求 eula.txt；不要替负责人写入 EULA 同意。
- Windows 控制台以系统代码页显示中文 Mod 名时乱码；以 `run/logs/latest.log`（UTF-8）为准。
- 运行日志、临时脚本放 `tmp/`。
- 客户端行为由会话自己验证：`./gradlew runSmokeClient`（见[工程结构](engineering.md)“验证基础设施”），结果在 `build/smoke/report.txt`，截图在 `run/screenshots`；不要让负责人手工开客户端复现。

## 下一步：C 玩家与战斗核心

范围与出口在[路线](roadmap.md)“批次和出口”的 C 行：依次完成 attribute → relation → xiulian → gongfa → combat → skill → yihuo → equipment，每个系统按移植技能的系统工作单推进，完成后合入 `main` 再开下一个系统的分支。当前进展与具体操作见上文“进行中”。B1 已于 2026-11-03 结束，结果见[项目状态](../systems/project/README.md)“B1 出口核对”。

### B0 结束时的状态与未完成项去向

B0 完成步骤 1–5 后由负责人 2026-11-02 指示结束（逐步记录见[月度记录](../systems/project/logs/2026-11.md)）：

- 依赖：NeoForge 21.1.265、ModDevGradle 2.0.148，`neoforge.mods.toml` 下限为负责人决定的 21.1.228；此后版本不必追最新，只需不低于 21.1.228。
- 骨架：公共入口调用各单位的 `<名称>Setup.register`，客户端入口 `@Mod(value = ModId.VALUE, dist = Dist.CLIENT)`；Mod ID 常量 `platform/ModId` 同时是唯一命名空间。实测专用服务器只构造公共入口，客户端先公共后客户端。
- ArchUnit：`ArchitectureTest` 检查[工程结构](engineering.md)六条规则，随 `gradlew check` 运行。
- 台账：`build_ledger.py` 同步 4,938 条来源注册（B0 结束时为 4,867 条，2026-11-02 补入漏扫的 71 条，见月度记录）到 31 个单位与 `project` 台账；`check_ledger.py` 检查列、覆盖、目录与归属、目标 ID 命名空间与唯一性、状态值、非“待处理”须“已读”。
- 片段合并：`mergeAssetFragments` 把 `src/main/lang/<语言>/<单位>.json` 与 `src/main/sounds/<单位>.json` 合并为生成资源。

步骤 6 的内容表、资源转换与注册表导出并入[路线](roadmap.md)“B1 实施步骤”的步骤 2–4。步骤 7 的运行机制在首个需要它的系统批次按正式结构实现，在开发服务端与客户端实测后写回[工程结构](engineering.md)。B0 曾选定的来源实物与读过的归属仍可作为首个实例：

| 机制 | 曾选实物 | 归属 | 预计批次 |
| --- | --- | --- | --- |
| 玩家附件（Codec、按[复制策略表](../reference/legacy/inventory/clone_policy.csv)设置死亡复制、同步给本人） | 2026-11-03 由 attribute 的贡献附件先实现并实测（做法见[工程结构](engineering.md)“数据与存档契约”）；原选实物 `colddownGold`（`non_death_only`，非死亡复制）随 skill 迁移 | attribute | C 已完成 |
| payload | 2026-11-03 由 relation 的界面按钮 `RelationButtonPayload` 先实现并实测（做法见[工程结构](engineering.md)“数据与存档契约”）；原选实物 `AnjianWMessage`（按键 W）随 skill 迁移 | relation | C 已完成 |
| 代码注册转数据定义 | 原初系统唯一的附魔 `yvanchuxitong:chain`（目标 `yvanchuxiuzhen:chain`） | combat | C |
| GameTest 独立源集（确认不进入发布 JAR）、JUnit 规则测试 | JUnit 已由 attribute 接入（2026-11-03）；GameTest 按首个需要世界内自动化的系统选取 | — | C |
| 方块实体与菜单 | 家具 `Beechfurniture14`：`RandomizableContainerBlockEntity`，27 格，自有菜单 `Beechfurniture14GuiMenu`；归属已读（27 格无槽位过滤，不属于 `storage`） | decor | D |
| GeckoLib 实体（选定 GeckoLib 版本） | `Silkwormmoth`：`PathfinderMob`，实现 `IAnimatable`；归属已读（只由蚕盒生成） | craft | D |

完整雷劫批次（2026-11-07，前一实现提交 `268d024`；本批与本页同一提交，最新提交号以 `git log -1` 为准）：自然突破、劫云与七种天雷、地滚雷/九霄雷柱/化灵雷龙、成功进阶和失败路径已接回。Q56 保持来源的功德动态循环、死亡状态误写、跳过后继续放雷与跨维度不补状态。`read_procedure.py` 已修正表达式延迟折叠，不存在此前工具产生的“延迟未启动”疑点。共享动画模型与渲染移到 platform，skill 只改引用；`extract_entity_models.py --owner xiulian` 与独立彩色闪电的内容转换路径已接通。build、ArchUnit、检查源集编译通过；仅一次 `-PsmokeOnly=tribulation`，5 项全部符合并 DONE；客户端正常保存退出，劫云截图可见。专用服务器一次启动、原生 stop 正常保存退出，ERROR 0，导出实体 67 个。证据在 `tmp/tribulation-{build,smoke,server}.log`、`tmp/tribulation-report.txt` 与 `build/smoke/screenshots/tribulation-cloud.png`。四个修炼实体台账改为“行为已迁移”，完整视觉、全分支时序、多人和两版本对照未执行。后续弓与拂尘已接回（见“进行中”）；下一步按 CodeGraph 与来源核对 equipment 的品阶、饰品飞行减耗与妖化，再接 yihuo/world 依赖及视觉收口；旧神通不重跑，skill 未完成，不合 main。

## 每次会话的流程

按 AGENTS.md 的“进度记录与交接”执行；分支、合并、worktree 与提交格式见[工程结构](engineering.md)的“Git 与会话协作”。工作中按移植技能的系统工作单推进，遇到来源矛盾先记录并提请裁定。

## 已知陷阱

- 邻近工程 `yuanchuxiuzhen-1.21.1` 的玩法、数值、删减和完成状态不是依据；它的 Parchment 配置（1.21.11）也不适用于本项目。
- 来源中按 Java 注册的附魔、群系、地物在 1.21.1 必须改为数据定义；燃料与堆肥改为 NeoForge 数据映射。
- 来源语言文件中混有机器配置（如商品列表），迁移时先拆出，不能当作普通翻译。
- 不要用“注册成功”“构建通过”“文件存在”标记玩法完成；状态按台账约定填写。

## 文档地图

| 内容 | 位置 |
| --- | --- |
| 总入口 | [docs/README.md](../README.md) |
| 项目状态与决定 | [项目状态](../systems/project/README.md)，日志 `docs/systems/project/logs/` |
| 来源证据与索引 | [来源说明](../reference/legacy/README.md)、[索引](../reference/legacy/inventory/README.md) |
| 目标 API 待验证范围 | [目标 API](../reference/target-api.md) |
| 编码与测试规则 | [开发规则](coding-policy.md) |
| 文档维护 | [文档维护](documentation.md) |
