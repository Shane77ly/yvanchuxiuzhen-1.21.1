---
topic: system-design
role: proposal
status: draft
updated: 2026-11-02
---

# 系统设计（重审方案）

状态：待负责人批准。负责人 2026-11-02 指出系统命名与划分不成熟，要求先定系统设计，B0 步骤 2 起暂停。批准后本文取代[整体架构](architecture.md)“系统所有权”的清单；批准前现有文档不变，本文不作为实现依据。

## 现有 19 系统的问题

19 系统来自 `tools/porting/build_architecture_catalog.py` 的目录对照表，是归档时的分组，没有经过系统设计；2026-11-02 的工程重设计沿用了它，未作审查。

| 问题 | 证据 |
| --- | --- |
| 不是系统的大杂烩 | `life` 占全部元素 31%（2,326）：建筑方块、作物、树木、食物、家具、牌匾、节日烟花并列 |
| 清单外另有两组 | `presentation`（453）、`gameplay`（195）在系统表之外，粒子、玩家面板、标签、指令没有归宿 |
| 缺少真实系统 | 副职业：10 种职业由同一个 `FuzhiyeLevelUp` 升级，有独立面板页，却被拆给各系统。组队好友：`Ling15`“团队招募令”等 77 个元素被放在 `combat`。天道工具：作者设有“原初天道（生存模式不可得物品）”分类，被散入 `world` |
| 归属错误造成假依赖 | 全部状态效果归 `combat`，使其反向依赖妖兽与符箓；灵兽袋从生物拆出后，妖兽类全部反向调用它 |
| 命名无规则 | `gongfa` 拼音，其余 18 个英文；`abilities`、`life`、`knowledge` 含义模糊，`life` 还与生命值、寿元混淆；`talismans`、`refining` 需要回译 |
| 名称与内容不符 | 文档标题“技能、施法、移动与异火”“维度、投放、场景、副本与城镇”说明一个名字盖不住内容 |

元素数不代表工作量：`gongfa` 的 415 个元素是星宿、穴位学习界面的显示条件。

## 什么是系统

系统同时满足三条：

1. **有所有权**：存在只有它能写入的状态或只由它定义的规则。
2. **有完整流程**：玩家能走通至少一条从入口到结果的路径。
3. **一词可名**：能用一个原作概念命名；名字需要用“与”连接时应当拆开，或说明它们共用同一份状态。

不满足的不是系统：

| 类别 | 成员 | 处理 |
| --- | --- | --- |
| 基座 | `platform`、`particle` | 无玩法规则，供所有系统使用 |
| 编排 | `flow` | 跨系统流程与全局事件分派，不拥有状态 |

只有字段没有玩法的概念不立系统：傀儡、御鬼、御虫、血道目前只确认了副职业等级字段，归 `profession`，发现完整玩法再评估。

## 命名规则

1. **修仙世界观专有词用拼音**：英文无对等概念的术语，共 9 个：`gongfa`、`xiulian`、`yihuo`、`liandan`、`lianqi`、`fulu`、`guchong`、`zhenfa`、`tiandao`。
2. **Minecraft 或一般游戏已有的概念用英文单数**：其余全部，如 `attribute`、`skill`、`material`。
3. 中文名取游戏内实际用词，标识与中文一一对应，代码包、文档目录、台账和分支使用同一标识；子概念用子包，如 `equipment.fabao`、`xiulian.leijie`、`creature.yaoshou`。

依据：作者工程和邻近工程的系统名都是这种混合方式（`gongfa`、`lianqi`、`fulu` 与 `book`、`cooking`、`decor` 并用），负责人的工作语汇也是如此。游戏文本中“技能”出现 49 次、“神通”0 次，因此取 `skill`，不取邻近工程的 `shentong`。

## 依赖次序

清单编号就是依赖方向：**编号大的系统可以调用编号小的系统的 `api`，反向禁止**。一张全序表比“分层”更简单，也能直接写成 ArchUnit 规则。下层需要通知上层时（如击杀结果触发任务）由 `flow` 协调。

用来源调用校验这一次序：按本文归组后跨系统调用 1,272 处，逆向 48 处（3.8%）。

| 逆向调用 | 处数 | 性质 | 目标处理 |
| --- | ---: | --- | --- |
| `liandan→lianqi`、`town→quest`、`xiulian→panel`、`skill→panel` | 32 | 界面显示条件复用 | 重写界面后消失 |
| `combat→creature` | 8 | 状态效果调用妖兽等级显示 | 改由显示工具或所属系统 |
| 其余 6 条边 | 8 | 单点调用（如 `combat→skill` 的施法中断） | 随所属系统逐条处理 |

没有发现必须打破次序的玩法依赖。

## 系统清单

元素、类为来源口径，全部 7,465 个元素和 7,874 个类都有唯一归属。

### 基座

| 标识 | 中文 | 内容 | 元素 / 类 |
| --- | --- | --- | ---: |
| `platform` | 平台 | 注册与内容表、payload、界面控件、创造栏、公共标签 | 98 / 54 |
| `particle` | 粒子库 | 全部粒子类型与客户端实现，供各系统按 ID 触发 | 307 / 307 |

### 规则基座

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 01 | `attribute` | 属性 | 基础属性、贡献汇总、派生值 | 派生结果 | `systemset` 的属性计算 | 9 / 7 |
| 02 | `relation` | 关系 | 组队、好友、敌友判定 | 队伍、好友 | `item/zawu/ling15`、`Friendpanding*` | 84 / 88 |
| 03 | `material` | 材料 | 编号材料身份、品阶、等级、辨识 | 材料定义、辨识进度 | `item/{metal,monstermaterial,gemstone,lingmu,herbmedicine,DemonBeastInnerPill}` | 372 / 318 |
| 04 | `storage` | 储物 | 乾坤袋、陶罐、材料袋 | 容器内容、玩家材料仓 | `item/zawu/{qiankundai,taoguan}`、`zawu/cailiaodai` | 151 / 160 |
| 05 | `profession` | 副职业 | 10 种副职业的等级、经验与升级 | 全部副职业进度 | `FuzhiyeLevelUp`、`Zhiye*` | 21 / 21 |
| 06 | `combat` | 战斗 | 伤害结算、通用状态效果、附魔、木人桩 | 战斗运行态 | `systemset/damage`、`effect`、`enchanting`、`tool/murenzhuang` | 248 / 254 |

### 修士

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 07 | `gongfa` | 功法 | 功法书、研习、参悟、星宿穴位学习 | 书籍实例、功法栏、学习会话 | `gongfa`、`systemset/gongfa`、`tool/canwutai` | 475 / 485 |
| 08 | `xiulian` | 修炼 | 境界、修为、灵力、寿元、灵根体质、丹田、金丹元婴、雷劫、轮回 | 玩家成长档案 | `systemset/{LEVELup,leijie}`、`GUI/{dantian,playeruplevel}` | 153 / 168 |
| 09 | `skill` | 技能 | 技能栏、施法、冷却、12 系技能、身法、轻功、飞行、武器技 | 技能物品栏、施法状态 | `skill` | 375 / 479 |
| 10 | `yihuo` | 异火 | 18 种异火的材料、投掷、火块与命中效果 | 异火定义 | `item/fire` | 97 / 133 |
| 11 | `equipment` | 装备 | 武器、防具、工具、饰品、飞行法器、法宝、本命、词条 | 装备实例、饰品栏、本命关系 | `weapon`、`Gongjv*` | 337 / 400 |

### 生产与生活

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 12 | `plant` | 植物 | 作物、树木、野生植物的生长与收获 | 作物与方块状态 | `plant` | 812 / 744 |
| 13 | `cooking` | 烹饪 | 食材、厨具、菜谱、食用效果 | 厨具状态、饮食状态 | `food`、`item/food`、`yvanchupengren` | 322 / 351 |
| 14 | `liandan` | 炼丹 | 丹炉、丹方、炸炉、分合丹、药理、毒药 | 药效定义、丹药实例、丹炉 | `yvanchuliandan`、`tool/liandanlu` | 150 / 201 |
| 15 | `lianqi` | 炼器 | 鼎炉、图纸、熔炼、精炼、修理 | 图纸定义、炉次状态 | `yvanchulianqi`、`tool/xiulitai` | 143 / 158 |
| 16 | `fulu` | 符箓 | 符头符胆知识、画符、成品释放与效果 | 解锁状态、符箓实例、符台 | `huafu`、`item/huafu` | 228 / 257 |
| 17 | `craft` | 工艺 | 木匠台、陶轮、石磨、织布机与蚕丝链 | 工作台状态 | `mujiangtai`、`zawu/taolun`、`tool`、`Silk*` | 129 / 177 |
| 18 | `decor` | 装饰 | 建筑方块、家具、牌匾、牌位 | 家具容器、牌匾内容 | `decorate`、`zhuangshiwu` | 1,049 / 848 |
| 19 | `festival` | 节日 | 烟花爆竹等节日物品 | 无持久状态 | `item/jieri` | 54 / 75 |

### 世界与众生

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 20 | `world` | 世界 | 维度、群系、地物、结构投放 | 维度状态 | `Dimension`、`biome`、`decorate/jiegou`、`xvnzhanyiji` | 106 / 156 |
| 21 | `treasure` | 宝箱 | 宝箱分级、奖励生成 | 宝箱定义 | `baoxiang`、`tag/baoxiang` | 114 / 105 |
| 22 | `town` | 城镇 | 城镇建设、居民与建筑 | 城镇实例 | `chengzhen` | 36 / 47 |
| 23 | `dungeon` | 副本 | 副本实例、波次、进出与回程 | 副本实例 | `fuben` | 35 / 44 |
| 24 | `creature` | 生物 | 妖兽、Boss、鬼怪、天魔、绿林、环境生物，驯服与灵兽袋，通用怪物技能 | 实体等级、形态、主人、培养 | `entity`、`item/zawu/lingshoudai` | 703 / 817 |
| 25 | `guchong` | 蛊虫 | 蛊虫实体与释放装置 | 蛊虫个体、装置计数 | `entity/guchong`、`guchong` | 36 / 47 |
| 26 | `npc` | 修士 | 修士身份、AI、交互入口、门徒 | NPC 实例、对话会话 | `entity/npc`、`tool/xvnlong` | 72 / 127 |
| 27 | `zhenfa` | 阵法 | 阵位、阵元、阵旗、阵法效果 | 阵基与阵元状态 | `yvanchuzhenfa`、`skill/zhenfa`、`tag/zhenfatag` | 59 / 66 |

### 社会

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 28 | `economy` | 经济 | 灵石货币、银行、商店、现场拍卖 | 账户、商品、拍卖状态 | `shop`、`item/Lingshi`、`tool/yinhang` | 263 / 277 |
| 29 | `quest` | 任务 | 对话动作、任务、悬赏、成就 | 玩家任务、公共悬赏 | `duihua`、`xvanshang`、`chengjiu` | 124 / 129 |
| 30 | `book` | 书籍 | 公共书籍、阅读、答题、装订 | 书籍内容定义、答题会话 | `item/book`、`zawu/makebook` | 70 / 72 |

### 汇总

| 编号 | 标识 | 中文 | 职责 | 拥有的状态 | 来源目录 | 元素 / 类 |
| ---: | --- | --- | --- | --- | --- | ---: |
| 31 | `panel` | 玩家面板 | 六个分页的导航框架、HUD 与覆盖层；各页的菜单归所属系统 | 无玩法状态 | `GUI`、`PlayerGUI*` | 124 / 181 |
| 32 | `tiandao` | 天道 | 生存模式不可得的通用管理工具；专属工具留在所属系统 | 工具设置 | `Tiandao*`、`Ling08`、`Ling16` | 42 / 59 |

`flow`：`PlayerTick`、`EntityTick`、`Worldbegin` 等全局入口（元素 67 / 类 62）。迁移时能归属单一系统的逻辑进入该系统的监听，只有确实依赖跨系统先后次序的部分留在 `flow`。

## 写入收口

依赖次序管住调用方向，写入收口管住数据。来源的玩家字段没有所有权：175 个有写入记录的字段中 148 个被所有者以外的代码写入，共 681 处；除去 `flow` 与 `panel` 这类入口后仍有 408 处。

| 所有者 | 外部写入 | 主要来源 | 目标形式 |
| --- | ---: | --- | --- |
| `xiulian` | 280 | `power`、`soul`、`lifetime`、`experience_Stand` 被异火、装备、经济、技能、战斗、符箓直接修改 | 消耗/恢复灵力与神识、增减寿元、增加待吸收修为等操作 |
| `skill` | 108 | 面板翻页与槽位、符箓、装备 | 菜单操作与施放入口 |
| `profession` | 80 | 各生产系统增加经验 | `addExperience(职业, 数量)` 一个入口 |
| `attribute` | 71 | 修炼、功法、符箓写属性 | 改为提交贡献，由 `attribute` 计算 |
| 隐藏载体 | 83 | `renwuitem`、`tizhiitem` 混装多系统数据 | 拆到各所有者（见[数据与运行时](data-and-runtime.md)） |

因此系统设计必须包含公开操作：每个系统实现前，在 README 列出 `api` 的写操作与查询，并对照 `typed_field_accesses.csv` 确认覆盖全部外部写入点。这份清单是实现所属系统的开工条件。

## 旧系统对照

| 旧 | 新 |
| --- | --- |
| `life` | `decor`、`plant`、`cooking`、`craft`、`festival`，工具归 `equipment` |
| `abilities` | `skill`、`yihuo` |
| `combat` | `combat`、`relation`；效果归各自来源系统 |
| `world` | `world`、`treasure`、`dungeon`、`town`，植物地物归 `plant`，通用工具归 `tiandao` |
| `creatures` | `creature`、`guchong`，门徒归 `npc` |
| `presentation` | `particle`、`panel`，副职业页归 `profession` |
| `gameplay` | `platform`、`flow` 及所属系统 |
| `cultivation`、`alchemy`、`refining`、`talismans`、`formations`、`knowledge` | `xiulian`、`liandan`、`lianqi`、`fulu`、`zhenfa`、`book` |
| `attributes`、`materials`、`quests` | `attribute`、`material`、`quest` |
| 新增 | `profession`、`relation`、`panel`、`tiandao`、`treasure`、`town`、`dungeon` 等 |

## 校验范围与局限

- 归组与校验已对全量来源索引实际运行；归属依据：目录 5,763、元素名规则 1,147、元素类型 466、明确指定 66、根目录默认 23。名称规则是批量判断，个别元素可能需要在所属系统开工时调整。
- 调用校验只覆盖 Procedure 调用；资源引用（如群系引用地物）和动态命令不在其中。字段统计只覆盖三类 Variables，物品、实体、方块 NBT 未计入，实际写入面更大。
- `tiandao` 与 `yihuo` 的边界、`festival` 是否独立，依据目录与显示名判断，未逐分支读源码。

## 待负责人决定

1. 命名规则：采用本文方案，或全部拼音，或全部英文。
2. 系统清单与边界：32 个系统是否合适，特别是 `treasure`/`town`/`dungeon` 是否从 `world` 拆出，`yihuo`、`festival` 是否独立。
3. 依赖次序：是否以编号全序作为强制规则。
4. 公开操作清单是否作为每个系统的开工条件。

## 批准后的迁移

1. 用本文规则更新 `build_architecture_catalog.py` 的对照表并重新生成归属台账。
2. 改写[整体架构](architecture.md)“系统所有权”、[工程结构](engineering.md)的系统名与 ArchUnit 规则；[路线](roadmap.md)按编号重排批次。
3. 拆分与重命名 `docs/systems/<系统>`，保留现有调查内容。
4. 重新标注[交接说明](handoff.md)中 B0 切片的所属系统，继续步骤 2。
