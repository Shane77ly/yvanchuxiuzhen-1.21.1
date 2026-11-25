---
topic: xiulian
role: entry
status: partial
updated: 2026-11-18
---

# 修炼

标识 `xiulian`，类别：系统。职责：境界、修为、灵力、寿元、出身、灵根体质、丹田、金丹元婴、本命槽位、雷劫、轮回（[系统设计](../../development/system-design.md)）。

当前阶段：基础部分于2026-11-03合入`main`。修炼档案、公开操作、灵根/体质、出身界面、修炼推进、突破、闭关、金丹元婴、丹田和血道已有必要开发客户端证据；2026-11-15管理命令及本人同步101项通过，专用保存重进未验。传承灵物与家族百宝袋的服务端条件、次数和奖励已接，Q197/Q198保持来源。升仙令与传承灵物另有[跨系统对话入口](../quest/architecture.md#升仙令与传承灵物自然任务入口2026-11-23)，不改变分次奖励表。落魄家族初始flow已注册，2026-11-17八出身扩展已集成并编译；受控八分支单项通过且正常保存退出，真实重进与完整自然入口未验，见[奖励正文](architecture.md#出身初始奖励扩展2026-11-17)。远古石盘已集成；2026-11-18完成首次客户端use、服务端和本人状态同步、可见粒子与简体中文截图及正常保存退出。自然奖励入口、全部提示分支和真实存档重进仍待，详见[石盘正文](architecture.md#远古石盘引导2026-11-17)。World坐标、BookTexts和Lianqi载体身份接口现已存在，家族袋外部生产者待核。完整雷劫已有必要单项与局部画面，完整表现仍待；元婴出窍和被闪电击中的功法效果归skill/gongfa。B1内容身份见台账。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- [突破冷却与四档妖丹](architecture.md#突破冷却与四档妖丹)：丹田装入、过劫清槽、同档重进与普通元婴通过 `dantianrealmchain` 受控客户端检查，详见[月志](logs/2026-11.md#2026-11-21-丹田过劫与重进专项)。
- 人间道实际入场已接入升仙令善后与个人一次性赠礼；规则见[人间道个人入场](architecture.md#人间道个人入场2026-11-18)。
- 好友实时境界查询已实现：本系统监听relation公开事件并仅读当前`LEVEL_NAME`回填；编译、架构及受控好友行刷新检查通过，规则及验证边界见[关系正文](../relation/architecture.md#好友行实时境界2026-11-18)。
- 每秒恢复入口已调用relation的本体团队刷新公开操作，顺序与来源一致；等待真实每秒计时推进的受控恢复检查通过，唯一规则及现有关系场景扩展见[关系正文](../relation/architecture.md#每秒本体团队刷新2026-11-18)。
- 归属范围 201 个元素，201 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

2026-11-15 接回第 2 页属性加点原生菜单、36 格背包、六项预览与加点、五个跨页请求及四行说明。加点沿用既有 `SkillPoints`，没有新增重置操作；来源说明疑点 Q187 保留待裁定。资源和语言已转换，编译及本页必要实机通过，公共导航由 panel 统一装配；有限运行边界见[本批集成记录](../project/logs/2026-11.md#2026-11-15-页面命令阵法与展示家具集成验证)。规则与边界见[属性加点页](architecture.md#属性加点页2026-11-15)，本批结果见[修炼月志](logs/2026-11.md)。

## 依赖方向

2026-11-03 会话决定（gongfa 开工时）：来源中 gongfa 读 xiulian 的境界、灵根、功德、罡煞、寿元与闭关天数（研习条件与功法修炼），xiulian 读主修功法的名字、品阶与归属（特殊功法效果、修为、金丹与元婴）。按“系统之间不得相互依赖”定为 gongfa → xiulian 单向：xiulian 需要的主修功法信息由 gongfa 向 xiulian 提交（与 attribute 的贡献同一形式），xiulian 不读 gongfa。因此 C 中 xiulian 先于 gongfa 实现。 本系统据此提供“提交主修功法信息”的写操作与境界、灵根等查询。

远古石盘消费已在集成树接收并通过首次客户端use、本人状态同步与粒子画面检查；自然奖励获取、所有提示分支、存档重进与完整来源表现仍未验，见[石盘正文](architecture.md#远古石盘引导2026-11-17)。

人间道受控客户端已通过蒲团闭关、凡人首次突破练气一层及同档磁盘重进，具体前置与未验范围见[修炼月志](logs/2026-11.md#2026-11-18-人间道早期闭关与首次突破)。

## 公开操作

已对照 `typed_field_accesses.csv` 汇总 42 个修炼字段的外部读写点（不含本系统）：写入集中在灵力（经济、异火、技能、战斗、装备、功法共 60 余处）、待吸收修为（经济、战斗、烹饪）、寿元、罡煞、功德，形式只有“在当前值上加减”“乘系数后写回”“直接赋值”三种，均不在写入处截断上限；读取遍及战斗、技能、装备、面板、流程等。代码为 `system/xiulian/api/Cultivation`。

| 操作 | 形式 | 调用者 |
| --- | --- | --- |
| 每二十刻推进事件 | `CultivationSecondEvent(玩家)`：闭关修炼之后、吸收与恢复之前发出 | gongfa推进功法修炼；skill按来源推进元婴出窍耗神 |
| 突破处理后每秒事件 | `CultivationAfterBreakthroughEvent(玩家)`：同一每秒段突破处理之后、闭关冷却之前发出 | cooking 按来源顺序代谢自有酒精 |
| 恢复后每秒事件 | `CultivationAfterRecoveryEvent(玩家)`：资源恢复之后发出 | gongfa 比较主修功法种类与名称 |
| 查询 | `get(玩家, CultivationValue)`：境界层数、修为三项与效率、灵力、神识、灵根与阴阳、罡煞、功德、雷劫、血脉、属性点、寿元、闭关天数等 35 项；`get(玩家, CultivationText)`：境界名、种族、血脉、体质、出身、`fuhuo` | 全部读取方 |
| 好友境界查询响应 | 监听relation的`FriendRealmNameEvent`，回填已匹配玩家当前保存的`LEVEL_NAME` | relation生成好友行；保持xiulian→relation，不写关系数据 |
| 加减、赋值 | `add(玩家, 项, 增量)`、`set(玩家, 项, 值)`、`set(玩家, 文本项, 文本)`；乘法由调用方读后赋值 | 经济、异火、技能、战斗、装备、功法、符箓、流程等 |
| 雷劫开关 | `thunderOpen`、`setThunderOpen` | 流程、世界、符箓、技能 |
| 出身分次补给 | `BirthSupplyEvent(玩家, INHERITANCE/FAMILY, double次数)`：服务端条件通过且载体次数写入后同步发出 | flow按来源精确整数分支生成奖励，材料、符箓、丹药、技能与宝匣实例归各内容所有者 |
| 出身初始奖励 | `BirthInitialRewardsEvent(玩家, 菜单打开维度)`：关闭后第二个服务端结束刻、应用体质之前同步发出 | flow按执行时出身发奖，跨系统实例交所属公开API构造；随后保持来源函数条件 |
| 石盘初始实例 | `BirthItems.ancientStoneDisc(Vec3全局坐标)`：构造`ling_11`并按来源写三个double副本 | flow查询World后提交坐标；石盘右键另读全局值 |
| 雷劫落雷请求 | `TribulationLightningEvent`：服务端维度、原始落点、攻击量、倍率、直径、颜色 | combat 监听并进入共享落雷 |
| 雷劫实体伤害请求 | `TribulationDamageEvent`：服务端维度、目标生物、施法实体、整数攻击量、小数倍率 | combat 监听并结算雷伤 |
| 妖兽雷柱生成 | `CultivationSummons.summonLightPillars(服务端世界, 施放者, 目标, 攻击量)`：按来源目标四角偏移、团队共享、目标UUID和攻击量写入后生成`tianjie_05` | creature 040来源落雷过程 |
| 本命槽位 | `benming`、`setBenming` | equipment |
| 修炼三页 | `Retreats.openPage(玩家, 坐标)`；`RetreatPageEvent` 请求其他主分页 | panel 打开06；07/08内部切换保持坐标和主分页6 |
| 闭关功法归属查询 | `RetreatTechniqueEvent` 区分无功法、未研习、他人所有、本人所有 | gongfa读取自身功法实例后回填，不建立xiulian对gongfa依赖 |
| 观想酒精炼化 | `MeditationRefineEvent` 在减速之后、寿元/资源结算之前发出 | cooking调用自有 `Alcohol.refine` |
| 丹田界面 | `Dantian.open`；返回按钮发出 `DantianReturnEvent`（取消时不关闭界面） | panel |
| 本命培养进度 | `benmingProgress`、`setBenmingProgress`（`BenmingProgress`：认主、等级、经验、所需经验，供丹田界面显示） | equipment |
| 主修功法 | `mainTechnique`、`setMainTechnique`（`MainTechnique`：有无、本人所有、名字、五行、品阶、层数） | gongfa |
| 属性加点界面 | `openSkillPoints(玩家, 最终打开坐标)`；`SkillPointPageEvent(玩家, 目标页, 当前菜单坐标)` 请求切换 1/3/4/5/6 页 | panel 统一打开、记页和导航 |
| 加点 | `spendSkillPoint(玩家, SkillPoint)`（六项：生命、灵力、神识上限与物理、法术、神识攻击） | 本系统第 2 页菜单 |
| 灵根重置 | `resetRoots`、`maximizeRoots`（管理命令 `/yvanchu` 的两个子命令，已接真实命令入口） | flow |
| 管理命令 | `command(命令源维度, 目标玩家, action, subject, operation, value, feedback)`：识别并执行本系统数值、灵根、体质标记与世界规则分支 | flow 唯一`/yvanchu`入口 |
| 吸血石 | `BloodStone.addBlood`、`blood`（流血效果给脚下的吸血石加充血量） | combat |
| 体质与修正值 | `tizhiDouble/Boolean/String` 与对应的 `set`、`editTizhi`（同一次同步内修改多个键，供逐刻推进的持续效果）：来源隐藏载体 `tizhiitem` 的键值（335 个键及读写系统见 [tizhiitem_keys.csv](ledger/tizhiitem_keys.csv)） | 战斗（属性修正）、技能（冷却与消耗修正）、流程（重生修正）、装备（武器培养速度）等 |

约定：写操作只在服务端调用，写入后同步给本人；灵根与阴阳变化时自动向 attribute 提交基础层贡献 `xiulian:roots`。`tizhiitem` 由本系统整体保管（会话决定，2026-11-03，评估为轻微）：其中蛊虫、战斗、副本等系统自己写入的键，在这些系统实现时可移到各自的数据。轮回由流程层协调本系统复位及灵根重抽，具体边界见[轮回正文](architecture.md#寿尽与轮回生命周期2026-11-20)。

2026-11-07 为属性汇总页接回 `CultivationValue.POWER_DISPLAY/SOUL_DISPLAY`：存活玩家每秒恢复前复制当前资源，随修炼档案保存并同步本人；panel 只查询这两个来源显示值。

雷劫依赖方向为 combat → xiulian。xiulian 生成劫云与天雷实体、推进渡劫状态、筛选范围内的敌方并发出上述具体事件；combat 订阅后调用共享落雷和伤害入口。xiulian 不导入 combat，固定效果 `effect_022` 的等级按注册键从玩家当前效果读取。实体表现复用 platform 的模型记录、外观接口与 GeckoLib 渲染器；xiulian 不依赖 skill。

本系统本轮变化见[修炼月志](logs/2026-11.md)，跨系统流程变化见[项目月度日志](../project/logs/2026-11.md)。

2026-11-07 共享渲染复核已恢复劫云3倍、化灵雷龙4倍渲染缩放，四个雷劫渲染器均按来源关闭死亡侧翻；碰撞尺寸分别仍为3、2、1、1倍。最终build/ArchUnit通过，现有雷劫五项通过并DONE、正常保存退出，劫云截图可见；规则与证据边界见[完整雷劫](architecture.md#完整雷劫2026-11-07)。完整视觉和两版本对照仍未验收。

## 普通重生（2026-11-08）

已接回来源 `Entitybereborn` 的普通 `PlayerRespawnEvent` 入口（目标 `Rebirth`）。重生后按目标属性最终值的 20% 设置生命，并按 `RebirthCorrectHealth*`、`RebirthCorrectPower*`、`RebirthCorrectJingli*` 先加固定值、再乘百分比；灵力与神识写入修炼档案。待吸收修为先加 `RebirthCorrectExperience`，再按 `RebirthCorrectExperiencePercentage` 相乘。寿元每次先减 30 天，再应用来源修正并发送原文案消息。

来源寿元分支在 `RebirthCorrectLifetime` 非零时误读 `RebirthCorrectJingli`，目标保留这一行为。人间道有来源死亡自动置位、同维保留、主世界再次死亡后返回人间道以及第20结束刻重算已在目标客户端实测。来源公式和实现边界见[架构正文](architecture.md#普通重生2026-11-08)。

## 修炼、观想与功法参悟页（2026-11-15）

来源06/07/08三页已接入原生菜单和screen，包含寿元与日数显示、六个日数按钮、启动判定、观想结算、说明与全部分页按钮。panel统一接六主分页和K记页；07/08切换不改变主分页6。日数边界和观想遇坐具提前终止扫描保持来源，具体规则、疑点与未执行验证见[正文](architecture.md#修炼观想与功法参悟页2026-11-15)。本批未运行Gradle、客户端、专服和两版本对照。
