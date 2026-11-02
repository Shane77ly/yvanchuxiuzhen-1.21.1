---
topic: system-design
role: proposal
status: draft
updated: 2026-11-02
---

# 系统设计（重审方案）

状态：第二版，第 1–3 项已决定，第 4、5 项待定（见“负责人决定”）；全部决定前 B0 步骤 2 起继续暂停。负责人 2026-11-02 指出系统命名与划分不成熟，要求先定系统设计。第一版（`bb522a5`）靠目录和名称规则批量归组；负责人审阅后指出归属不能靠脚本判定，第二版改为按规则判定，并读来源代码重核了争议范围。全部决定后本文取代[整体架构](architecture.md)“系统所有权”的清单；在此之前现有文档不变，本文不作为实现依据。

## 现有 19 系统的问题

19 系统来自 `tools/porting/build_architecture_catalog.py` 的目录对照表，是归档时的分组，没有经过系统设计；2026-11-02 的工程重设计沿用了它，未作审查。

| 问题 | 证据 |
| --- | --- |
| 不是系统的大杂烩 | `life` 占全部元素 31%（2,326）：建筑方块、作物、树木、食物、家具、牌匾、节日烟花并列 |
| 清单外另有两组 | `presentation`（453）、`gameplay`（195）在系统表之外，粒子、玩家面板、标签、指令没有归宿 |
| 缺少真实系统 | 副职业：10 种职业由同一个 `FuzhiyeLevelUp` 升级，有独立面板页，却被拆给各系统。组队好友：`Ling15`“团队招募令”等 77 个元素被放在 `combat` |
| 归属错误造成假依赖 | 全部状态效果归 `combat`，使其反向依赖妖兽与符箓；灵兽袋从生物拆出后，妖兽类全部反向调用它 |
| 命名无规则 | `gongfa` 拼音，其余 18 个英文；`abilities`、`life`、`knowledge` 含义模糊，`life` 还与生命值、寿元混淆；`talismans`、`refining` 需要回译 |
| 名称与内容不符 | 文档标题“技能、施法、移动与异火”“维度、投放、场景、副本与城镇”说明一个名字盖不住内容 |

元素数不代表工作量：`gongfa` 的 415 个元素是星宿、穴位学习界面的显示条件。

## 第一版的问题

| 问题 | 证据 |
| --- | --- |
| 实现者答不出“归谁” | 陶罐的物品与菜单在 `storage`，方块在 `guchong`；交接说明把家具箱的归属留作待复核，把蚕蛾写成 `creatures`，清单却归 `craft` |
| 目录和名称放错功能 | `Tiandao*` 把玩家副本入口放进管理工具，`~/tool` 把牌位的写入过程放进 `craft`，`~/decorate/OtherDecorative` 把符头解锁放进 `decor` |
| 标准筛不掉内容集合 | “状态或规则”没有限定规则，带右键行为的方块都算，`festival`、`decor` 因此成了系统 |
| 依赖从来源访问推导 | “没有发现必须打破次序的玩法依赖”只对 Procedure 调用成立；更根本的是来源访问量不代表目标需要的依赖（见“依赖规则”） |
| 脚本看不见主要连接 | 陶罐的物品与方块靠字符串 ID 相连，传承之光靠命令字符串，`world` 与 `dungeon` 靠 NBT 键。按挂接关系检出 14 个跨系统功能族，其中没有陶罐 |

## 什么是系统

系统同时满足三条：

1. **有玩法状态或共用规则**：存在跨越多次操作、影响后续结果的状态；或存在被其他系统调用、把多个系统的产物组合成新结果的规则。
2. **有完整流程**：玩家能走通至少一条从入口到结果的路径。
3. **一词可名**：能用一个原作概念命名；名字需要用“与”连接时应当拆开，或说明它们共用同一份状态。

只作用于元素自身的行为不算第 1 条：开门、坐下、显示牌匾文字、放烟花。门的开合、引信计时、牌匾文字是元素自身状态。同一模板按木种或颜色复制，也不算共用规则。

不满足的不是系统：

| 类别 | 成员 | 依赖规则 |
| --- | --- | --- |
| 基座 | `platform`、`particle` | 无玩法规则，供所有系统使用 |
| 编排 | `flow` | 跨系统流程与全局事件分派，不拥有状态 |
| 汇总 | `panel` | 只显示各系统数据；可依赖所有系统的 `api`，不被系统依赖 |
| 内容族 | `decor`、`festival`、`tiandao` | 元素自身行为的集合，没有 `api`；可依赖系统的 `api`，任何代码不得依赖它，其他系统只通过注册 ID 或标签引用其内容 |

只有字段没有玩法的概念不立系统：傀儡、御鬼、御虫、血道目前只确认了副职业等级字段，归 `profession`，发现完整玩法再评估。

### 争议分组的读取结论

| 分组 | 读到的规则 | 跨操作状态 | 与 `world` 的关系 | 结论 |
| --- | --- | --- | --- | --- |
| `festival` | 鞭炮花炮、祈愿灯、财神服、大红包：单品自带抛射、粒子与随机奖励 | 只有 `Pao10`–`Pao12` 的引信计时（`begin`、`tick`，库存容量 0）；不写玩家字段 | 无；被其他代码引用 2 处 | 内容族 |
| `decor` | 75% 的元素在建筑方块目录；179 个过程去掉木种约 77 个模板，主要是门、墙、椅的多格放置。按玩家变量、命令、给物品、效果、伤害、传送筛查命中 11 个元素，逐个读后只有传承之光离开元素自身 | 门、家具箱、牌匾文字 | `world`、`plant` 只引用其方块 | 内容族，传承之光移出 |
| `tiandao` | 第一版自述“专属工具留在所属系统”，成员却含副本入口、NPC 设置、功法界面 | 无 | 不适用 | 内容族；专属工具开工时拆回 |
| `treasure` | `Baoxiangzong` 按容器类型、类别、等级分派到 `BaoxiangFulu`、`BaoxiangGongfa`、`BaoxiangJineng` 等，组合多个系统的产物 | 开箱标记 `dakai` | 无调用，无共同 NBT 键 | 系统 |
| `town` | 案台存入木、石、羊毛，足量后放置建筑（`Building`、`Jiegoufangzhi`），录入居民 | 方块实体 `build_data`、`team`，`worlddata` | 无调用，只共用 `team` 等通用键 | 系统 |
| `dungeon` | 进入校验（`Fubenjinru`）、波次刷怪（`FubenblockGengXinYouXiKe`）、玩家 tick 与回程 | 控制方块阶段，玩家 `tizhiitem` 中的副本身份 | 共用 13 个 NBT 键，原因是下表 3 个过程放错 | 系统，修正边界 |

## 归属判定

归属由规则决定，不由脚本决定。步骤：

1. **单位是功能，不是元素**：从玩家入口读到结果，链上的物品、方块、菜单、实体、过程是一个功能。字符串 ID、命令字符串、NBT 键都算连接。
2. **归给赋予它玩法意义的规则**：形态、目录、名称、创造栏只是线索。容器不等于 `storage`，实体不等于 `creature`，方块不等于 `decor`，名字带 Tiandao 不等于 `tiandao`。
3. **一个过程跨两个系统时拆分支**，不整体归属。
4. **显示条件复用不影响归属**，重写界面后消失。
5. **读完规则仍不能判定才提请负责人裁定**，并写入所属系统 README。

脚本的用途是产出候选归属和“该读什么”：信号筛查找到了传承之光，NBT 键交集找到了 `world` 与 `dungeon` 的错放。结果只决定阅读顺序。

约束来源：台账 `content.csv` 增加归属状态列（候选、已读），`check_ledger.py` 在内容状态高于“待处理”而归属仍为候选时失败。

### 已读裁定

| 功能 | 第一版 | 裁定 | 依据 |
| --- | --- | --- | --- |
| 蛊罐：3 物品、3 方块、3 菜单、释放界面、虫卵、标签 | `storage`、`guchong`、`platform` | `guchong` | 菜单槽位只收 `forge:guchong`（`Taoguan01GUIMenu:89`）；精血认主写 `belongToMasterUUID`（`Taoguan01ZaiFangKuaiShangYouJian`）；方块 tick 按队伍释放（`Taoguan01GengXinYouXiKe`）；方块实体的 `createMenu` 是 `ChestMenu` 占位，不存在通用容器用途 |
| `TaoguanGuipageup`、`TaoguanGuipagedown` | `craft` | `craft` | 由 `TaolunGuiButtonMessage` 触发，翻的是陶轮配方页 |
| 蚕、蚕蛾 | `craft`（交接说明写 `creatures`） | `craft` | 蚕蛾只由 `CanheGengXinYouXiKe:594` 生成，实体类没有专属过程，只有闲逛目标，不使用等级、驯服等 `creature` 规则；结茧、羽化由蚕盒驱动 |
| 家具箱 `Beechfurniture14` | `decor` | `decor` | 27 格无槽位过滤，没有 `storage` 的规则 |
| 传承之光 `Chuancheng` | `decor` | `fulu` | 按方块配置执行 `unlock futou add 1`–`11` |
| 家族百宝袋 `Jiazubaibaodai` | `storage` | `xiulian` | 校验出身 `birth` 为“世家纨绔”后分次发放物品，没有容器规则 |
| `SetPaiwei` | `craft` | `decor` | 牌位界面的写入过程，写 `plaquetxt` |
| 天道副本 `Tiandaofuben` | `tiandao` | `dungeon` | 非创造模式打开 `FubenopenMenu`，是玩家副本入口 |
| `Fabao009weiduDangWanJiaJinRuWeiDuShi` | `world` | `dungeon` | 进入画里乾坤时放置 `fuben00x`，写 `fubenweizhi` 与副本坐标 |
| `Zhenyaotashengchengbuchong` | `world` | `dungeon` | 向镇妖塔控制方块写 `fubenshujv` |
| `Weidubianhuan` | `world` | 拆分支 | 按 `gamemoshi` 恢复游戏模式的分支归 `dungeon`，其余留 `world` |

## 命名规则

1. **修仙世界观专有词用拼音**：英文无对等概念的术语，共 9 个：`gongfa`、`xiulian`、`yihuo`、`liandan`、`lianqi`、`fulu`、`guchong`、`zhenfa`、`tiandao`。
2. **Minecraft 或一般游戏已有的概念用英文单数**：其余全部，如 `attribute`、`skill`、`material`。
3. 中文名取游戏内实际用词，标识与中文一一对应，代码包、文档目录、台账和分支使用同一标识；子概念用子包，如 `equipment.fabao`、`xiulian.leijie`、`creature.yaoshou`。

依据：作者工程和邻近工程的系统名都是这种混合方式（`gongfa`、`lianqi`、`fulu` 与 `book`、`cooking`、`decor` 并用），负责人的工作语汇也是如此。游戏文本中“技能”出现 49 次、“神通”0 次，因此取 `skill`，不取邻近工程的 `shentong`。

## 依赖规则

1. **系统之间不得相互依赖**。ArchUnit 检查 `system` 下各系统包之间没有循环，违反即构建失败。不设编号次序或依赖表；`api` 限制以及内容族、`panel` 的规则见上文与[工程结构](engineering.md)。
2. **依赖由职责和数据归属决定**。先定每个系统做什么、数据放在哪里，依赖随之确定，数量少且方向自明。1.18.2 版本的过程可以直接读写任何字段，访问量反映的是生成方式，不是目标需要的依赖。本文初稿曾按来源访问统计逆向依赖并据此提出编号次序，这一做法已撤回；来源访问清单只用作“公开操作”的覆盖核对。
3. **已定的职责与数据归属**：

| 范围 | 决定 | 来源依据 |
| --- | --- | --- |
| `combat` | 只读 `attribute` 的最终值；灵根与功法的五行加成、抗性由 `attribute` 计算。其他系统以伤害类型造成伤害，`combat` 在伤害事件中结算，不被直接调用。状态效果与受伤时扣减灵力、神识，使用 `xiulian` 的扣减操作 | `DamageIntegrationProcedure` 直接读攻击方灵根 `body_Fire` 等与受击方功法 `ExercisesFire`、`ExercisesLevel`；其他系统 209 处经 `/shanghai` 命令进入伤害结算；`Duyao11`、`Effect010`、`EntityHurt` 等写 `power`、`soul` |
| 本命 | 本命槽位 `benmingfabao` 是 `xiulian` 丹田的一部分；本命培养数据（`benmingexp` 等）在物品上，归 `equipment` | 金丹流程判定槽内物品（含妖丹）；`equipment` 使用 `xiulian`（法宝、武器消耗灵力等 55 处），槽位放在 `equipment` 会形成相互依赖 |
| `attribute` | 修炼、功法、装备向 `attribute` 提交贡献，`attribute` 不读取它们（见“写入收口”） | 来源修炼直接写属性 171 处，属性计算读修炼 9 处 |

本命一项改变[整体架构](architecture.md)中 `equipment` 拥有本命关系的记述，迁移时同步。伤害事件与伤害类型的具体 API 在实现 `combat` 时按目标源码核对。其余相互访问在所属系统确定职责时处理，结果写入系统 README；构建检查保证不留下相互依赖。

## 系统清单

清单顺序只为阅读，不表示依赖方向。元素、类为候选口径：已读裁定已计入，其余仍是目录和名称规则的结果，所属系统开工时按“归属判定”确认。全部 7,465 个元素和 7,874 个类都有候选归属。

### 基座与非系统

| 标识 | 中文 | 类别 | 内容 | 元素 / 类 |
| --- | --- | --- | --- | ---: |
| `platform` | 平台 | 基座 | 注册与内容表、payload、界面控件、创造栏、公共标签 | 96 / 54 |
| `particle` | 粒子库 | 基座 | 全部粒子类型与客户端实现，供各系统按 ID 触发 | 307 / 307 |
| `flow` | 流程 | 编排 | `PlayerTick`、`EntityTick`、`Worldbegin` 等全局入口；能归属单一系统的逻辑进入该系统的监听，只有确实依赖跨系统先后次序的部分留下 | 67 / 62 |
| `panel` | 玩家面板 | 汇总 | 六个分页的导航框架、HUD 与覆盖层；各页的菜单归所属系统 | 124 / 181 |
| `decor` | 装饰 | 内容族 | 建筑方块、家具、牌匾、牌位 | 1,045 / 836 |
| `festival` | 节日 | 内容族 | 烟花爆竹等节日物品 | 54 / 75 |
| `tiandao` | 天道 | 内容族 | 生存模式不可得的通用管理工具；专属工具归所属系统 | 36 / 45 |

### 规则基座

| 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| --- | --- | --- | --- | --- | ---: |
| `attribute` | 属性 | 基础属性、贡献汇总、派生值，含灵根与功法的五行加成和抗性 | 派生结果 | `systemset` 的属性计算 | 9 / 7 |
| `relation` | 关系 | 组队、好友、敌友判定 | 队伍、好友 | `item/zawu/ling15`、`Friendpanding*` | 84 / 88 |
| `material` | 材料 | 编号材料身份、品阶、等级、辨识 | 材料定义、辨识进度 | `item/{metal,monstermaterial,gemstone,lingmu,herbmedicine,DemonBeastInnerPill}` | 372 / 318 |
| `storage` | 储物 | 乾坤袋、材料袋 | 容器内容、玩家材料仓 | `item/zawu/qiankundai`、`zawu/cailiaodai` | 116 / 120 |
| `profession` | 副职业 | 10 种副职业的等级、经验与升级 | 全部副职业进度 | `FuzhiyeLevelUp`、`Zhiye*` | 21 / 21 |

### 修行与战斗

| 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| --- | --- | --- | --- | --- | ---: |
| `gongfa` | 功法 | 功法书、研习、参悟、星宿穴位学习 | 书籍实例、功法栏、学习会话 | `gongfa`、`systemset/gongfa`、`tool/canwutai` | 475 / 485 |
| `xiulian` | 修炼 | 境界、修为、灵力、寿元、出身、灵根体质、丹田、金丹元婴、本命槽位、雷劫、轮回 | 玩家成长档案、本命槽位 | `systemset/{LEVELup,leijie}`、`GUI/{dantian,playeruplevel}`、`Jiazubaibaodai*` | 155 / 170 |
| `combat` | 战斗 | 伤害结算（读 `attribute` 最终值）、通用状态效果、附魔、木人桩 | 战斗运行态 | `systemset/damage`、`effect`、`enchanting`、`tool/murenzhuang` | 248 / 254 |
| `skill` | 技能 | 技能栏、施法、冷却、12 系技能、身法、轻功、飞行、武器技 | 技能物品栏、施法状态 | `skill` | 375 / 479 |
| `yihuo` | 异火 | 18 种异火的材料、投掷、火块与命中效果 | 异火定义 | `item/fire` | 97 / 133 |
| `equipment` | 装备 | 武器、防具、工具、饰品、飞行法器、法宝与本命培养、词条 | 装备实例、饰品栏、本命培养数据 | `weapon`、`Gongjv*` | 337 / 400 |

### 生产

| 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| --- | --- | --- | --- | --- | ---: |
| `plant` | 植物 | 作物、树木、野生植物的生长与收获 | 作物与方块状态 | `plant` | 812 / 744 |
| `cooking` | 烹饪 | 食材、厨具、菜谱、食用效果 | 厨具状态、饮食状态 | `food`、`item/food`、`yvanchupengren` | 322 / 351 |
| `liandan` | 炼丹 | 丹炉、丹方、炸炉、分合丹、药理、毒药 | 药效定义、丹药实例、丹炉 | `yvanchuliandan`、`tool/liandanlu` | 150 / 201 |
| `lianqi` | 炼器 | 鼎炉、图纸、熔炼、精炼、修理 | 图纸定义、炉次状态 | `yvanchulianqi`、`tool/xiulitai` | 143 / 158 |
| `fulu` | 符箓 | 符头符胆知识与解锁、画符、成品释放与效果 | 解锁状态、符箓实例、符台 | `huafu`、`item/huafu`、`Chuancheng*` | 233 / 270 |
| `craft` | 工艺 | 木匠台、陶轮、石磨、织布机与蚕丝链 | 工作台状态、蚕盒进度 | `mujiangtai`、`zawu/taolun`、`tool`、`Silk*` | 128 / 176 |

### 世界与众生

| 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| --- | --- | --- | --- | --- | ---: |
| `world` | 世界 | 维度、群系、地物、结构投放 | 维度状态 | `Dimension`、`biome`、`decorate/jiegou`、`xvnzhanyiji` | 104 / 154 |
| `treasure` | 宝箱 | 宝箱分级、奖励生成 | 宝箱定义 | `baoxiang`、`tag/baoxiang` | 114 / 105 |
| `town` | 城镇 | 城镇建设、居民与建筑 | 城镇实例 | `chengzhen` | 36 / 47 |
| `dungeon` | 副本 | 副本入口、实例、波次、进出与回程 | 副本实例 | `fuben`、`Tiandaofuben*` | 43 / 60 |
| `creature` | 生物 | 妖兽、Boss、鬼怪、天魔、绿林、环境生物，驯服与灵兽袋，通用怪物技能 | 实体等级、形态、主人、培养 | `entity`、`item/zawu/lingshoudai` | 703 / 817 |
| `guchong` | 蛊虫 | 蛊虫实体、蛊罐、认主与释放 | 蛊虫个体、蛊罐内容 | `entity/guchong`、`guchong`、`item/zawu/taoguan` | 71 / 85 |
| `npc` | 修士 | 修士身份、AI、交互入口、门徒 | NPC 实例、对话会话 | `entity/npc`、`tool/xvnlong` | 72 / 127 |
| `zhenfa` | 阵法 | 阵位、阵元、阵旗、阵法效果 | 阵基与阵元状态 | `yvanchuzhenfa`、`skill/zhenfa`、`tag/zhenfatag` | 59 / 66 |

### 社会

| 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| --- | --- | --- | --- | --- | ---: |
| `economy` | 经济 | 灵石货币、银行、商店、现场拍卖 | 账户、商品、拍卖状态 | `shop`、`item/Lingshi`、`tool/yinhang` | 263 / 277 |
| `quest` | 任务 | 对话动作、任务、悬赏、成就 | 玩家任务、公共悬赏 | `duihua`、`xvanshang`、`chengjiu` | 124 / 129 |
| `book` | 书籍 | 公共书籍、阅读、答题、装订 | 书籍内容定义、答题会话 | `item/book`、`zawu/makebook` | 70 / 72 |

## 写入收口

依赖规则管住系统之间的方向，写入收口管住数据。来源的玩家字段没有所有权：175 个有写入记录的字段中 148 个被所有者以外的代码写入，共 681 处；除去 `flow` 与 `panel` 这类入口后仍有 408 处。

| 所有者 | 外部写入 | 主要来源 | 目标形式 |
| --- | ---: | --- | --- |
| `xiulian` | 280 | `power`、`soul`、`lifetime`、`experience_Stand` 被异火、装备、经济、技能、战斗、符箓直接修改 | 消耗/恢复灵力与神识、增减寿元、增加待吸收修为等操作 |
| `skill` | 108 | 面板翻页与槽位、符箓、装备 | 菜单操作与施放入口 |
| `profession` | 80 | 各生产系统增加经验 | `addExperience(职业, 数量)` 一个入口 |
| `attribute` | 71 | 修炼、功法、符箓写属性 | 改为提交贡献，由 `attribute` 计算 |
| `quest`、`xiulian` 等 | 83 | 隐藏载体 `renwuitem`、`tizhiitem` 混装多系统数据 | 拆到各所有者（见[数据与运行时](data-and-runtime.md)） |

因此系统设计必须包含公开操作，即一个系统提供给其他系统调用的查询与写操作：每个系统实现前，在 `docs/systems/<系统>/README.md`“公开操作”一节列出，代码对应 `system/<系统>/api`，并对照 `typed_field_accesses.csv` 确认覆盖全部外部读写点。字段读取的跨系统量大于写入，只查写入会漏掉主要依赖。这份清单是实现所属系统的开工条件，约束写入 AGENTS.md“工程边界”。清单尚未编写；现有各系统 `architecture.md` 的接口提案是未核对的草案，不是这份清单。

## 旧系统对照

| 旧 | 新 |
| --- | --- |
| `life` | `plant`、`cooking`、`craft`，内容族 `decor`、`festival`，工具归 `equipment` |
| `abilities` | `skill`、`yihuo` |
| `combat` | `combat`、`relation`；效果归各自来源系统 |
| `world` | `world`、`treasure`、`dungeon`、`town`，植物地物归 `plant`，通用工具归 `tiandao` |
| `creatures` | `creature`、`guchong`，门徒归 `npc` |
| `presentation` | `particle`、`panel`，副职业页归 `profession` |
| `gameplay` | `platform`、`flow` 及所属系统 |
| `storage` | `storage`；蛊罐归 `guchong` |
| `cultivation`、`alchemy`、`refining`、`talismans`、`formations`、`knowledge` | `xiulian`、`liandan`、`lianqi`、`fulu`、`zhenfa`、`book` |
| `attributes`、`materials`、`quests` | `attribute`、`material`、`quest` |
| 新增 | `profession`、`relation`、`treasure`、`town`、`dungeon` 等系统，`panel`，内容族 |

## 校验范围与局限

- 已读：上文“争议分组的读取结论”“已读裁定”与“依赖规则”依据栏涉及的来源代码。其余系统未按第二版标准重读，尤其是 `plant`、`book`、`yihuo`、`relation`；`tiandao` 除副本入口外的专属工具只看了目录和名称。
- 归属：除已读裁定外仍是第一版的批量结果（目录 5,763、元素名规则 1,147、元素类型 466、明确指定 66、根目录默认 23），只是候选。第一版归组结果在 `tmp/system_remap.csv`，已读裁定与访问统计的脚本未入库。
- 访问统计覆盖 Procedure 调用、模组命令字符串、三类 Variables 字段、注册常量与字面 ID，只作检查表。字段所有者取来源索引的候选归属，另把 `friendlist`、`team` 归 `relation`，副职业等级与经验字段归 `profession`，是近似口径。物品、实体、方块 NBT 未计入。
- 未执行：1.18.2 版本实机运行；本文全部结论是静态核对。

## 负责人决定

| 项 | 内容 | 状态 |
| --- | --- | --- |
| 1 | 命名规则 | 2026-11-02 采用本文方案 |
| 2 | 系统标准与清单 | 2026-11-02 采用收紧后的标准与 28 个系统、`decor`/`festival`/`tiandao` 内容族、`panel` 汇总；`yihuo` 暂定独立，未读来源 |
| 3 | 依赖规则 | 2026-11-02 采用“依赖规则”一节：系统之间不得相互依赖，由构建检查；不设编号次序；`combat` 只读 `attribute` 最终值；本命槽位归 `xiulian`，本命培养数据归 `equipment` |
| 4 | 公开操作清单（系统提供给其他系统的查询与写操作）是否作为每个系统的开工条件 | 待定 |
| 5 | 归属判定：是否采用“规则决定、脚本只给候选”，并在台账增加归属状态列 | 待定 |

## 批准后的迁移

1. 把第一版归组与已读裁定入库，更新 `build_architecture_catalog.py` 的候选对照表并重新生成归属台账。
2. 改写[整体架构](architecture.md)“系统所有权”（含本命归属）；[工程结构](engineering.md)改系统名，增加内容族目录与 ArchUnit 规则（系统之间无循环、不得依赖内容族、不得依赖 `panel`）；[路线](roadmap.md)按依赖关系重排批次。
3. 拆分与重命名 `docs/systems/<系统>`，保留现有调查内容。
4. AGENTS.md“工程边界”写入相互依赖禁止、归属判定与公开操作要求。
5. 重新标注[交接说明](handoff.md)中 B0 切片的所属系统（`Silkwormmoth` 归 `craft`，`Beechfurniture14` 归 `decor`），继续步骤 2。
