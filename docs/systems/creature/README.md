---
topic: creature
role: entry
status: partial
updated: 2026-11-14
---

# 生物

2026-11-13 补入18号神傀符的妖兽驯服消费者，完整来源限制、概率叠加和成功/失败反馈已实现，静态核对、编译和六项结构检查通过；真实001妖兽限制、概率、原生认主及保存重读必要单项已过，全部编号与自然驯养仍待；结果统一记[符箓月志](../fulu/logs/2026-11.md#2026-11-13-神傀符18完整路径)。

2026-11-13 霓裳草开花召引已接回：Creature订阅Plant同步事件，保留21群系完整分组、16次选址、已有妖兽召引与生成前共享阵营顺序；来源54成员妖兽标签已核对恢复。静态核对、必要编译与六项结构检查通过，秋林03真实七槽召引和正常六维保存退出通过，见[本批正文](architecture.md#霓裳草开花召引2026-11-13)及[符箓月志](../fulu/logs/2026-11.md#2026-11-13-荫翠符30与霓裳草开花链)。

2026-11-12 `yaoshou_01701`模型表补齐必需的生命周期字段，修复客户端初始化解码崩溃；既有Skill客户端场景加载通过，01701自身行为、形态转换与视觉仍待专项验收，见[本月日志](logs/2026-11.md)。

2026-11-12 已接入鬼火`guiguai_02`与小尸王`guiguai_03`，保留各自实体身份与来源AI；二者均加入`minecraft:undead`实体类型标签。Guiguai03完整候选技能和阶段调用鬼火召唤，包含与其依赖的Guiguai02；来源未启用自然生成，本批不添加自然出生。低内存`compileJava`通过；运行期注册、AI、召唤、客户端模型和视觉均未验收，见[本月日志](logs/2026-11.md)。

标识 `creature`，类别：系统。职责：妖兽、Boss、鬼怪、天魔、绿林、环境生物，驯服与灵兽袋，通用怪物技能（[系统设计](../../development/system-design.md)）。

当前阶段：D/E自然供给依赖。环境生物`organism_01`–`organism_12`整族、刷怪蛋及模型资源已接入；01–08为同构蝴蝶，09–12保留各自移动与交互分支。完整构建、专服基础加载与客户端资源加载冒烟已有记录；关闭时序警告、[`organism_11`掉落表上下文Q110](architecture.md#q110-organism_11掉落上下文)及行为专项未验，详见[本月日志](logs/2026-11.md)。52个来源编号妖兽及魔兽001/003、桃木精、修者0101和关联实体已完成接线；统一完整构建通过，专服注册表导出确认216个实体、台账检查0问题。后续专服复核确认World显式登记的`minecraft:bee`无约束出生放置消除了原NeoForge开发期告警。静态核对确认41群系483条出生记录与目标数据一致；32条/11种实体的出生类别与目标类型注册类别不一致，按来源保留，类别差异影响刷怪额度。8种相关原版实体在1.18.2与1.21.1的注册类别相同，差异来自来源群系配置，不是版本迁移造成；对应影响已与两版`NaturalSpawner`计算路径核对。该批未追加客户端或多场景游戏运行，内存约束下优先使用静态核对。两道样点新区块已生成并核对群系身份，但新增实体自然出生、行为和视觉仍未验；其余生物、灵兽袋本体、完整技能与生命周期尚未全部实现或验收，不能将内容身份登记计为玩法完成。

已接入三名首领`ycxzboss_01/02/04`、飞斧`ycxzboss_0201`及三枚来源刷怪蛋，保留各自AI、技能状态、属性、死亡时序和模型资源。来源、定向转换、必要编译与六项结构检查已有证据，四实体与三枚蛋的注册导出已确认；三名首领的客户端同步、渲染映射与存活保存重进单项通过。此前02号重进失败已定位为夹具弹道引发首领互相攻击，隔离后通过；自然战斗、完整技能/死亡与全部表现仍待验。规则见[首领正文](architecture.md#三名首领与飞斧实体2026-11-14)，结果和边界见[本月日志](logs/2026-11.md#2026-11-14-首领注册与存活重进单项)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 704 个元素，其中 595 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。[编号妖兽来源证据](ledger/yaoshou-source-evidence.csv)固定52个编号的实体及递归过程闭包身份；[来源证据](ledger/source-evidence.csv)记录霓裳草召引及首领实体、初始化和技能过程，不代表完整行为验收。
- [变更记录](changes.md)：按日期索引本系统批次记录。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`TalismanCorpseSealing`处理来源死亡妖兽潜行收纳入口并订阅`CorpseReleaseEvent`，Creature选择封存字段和恢复实体，库存交Fulu、团队交Relation。50／5001不属于镇妖塔结构解封；字段重置及未验自然采集影响见[Q114](architecture.md#q114-尸体封存的选定字段与状态重置)。

`CreatureBounties.prepare(Mob,int)`仅按B`Xvanshangzhuizongheji:44`在`finalizeSpawn`前写入妖兽`LEVELnumber`；境界、属性和技能仍由既有妖兽`sourceSpawn`生命周期初始化。悬赏系统只提交等级，不直接操作妖兽属性或持久字段。

| 操作 | 调用与数据边界 |
| --- | --- |
| `TalismanTaming`订阅`TalismanTamingEvent` | Creature拥有妖兽限制、完整驯服概率、原生认主和状态清理；Relation替换好友表与团队，Fulu仅提供命中事实；来源定位见[驯服正文](architecture.md#神傀符妖兽驯服2026-11-13) |
| `NichangGrassAttraction`订阅`NichangGrassBloomEvent` | Creature拥有召引目标选择、既有妖兽自由NBT及新实体生命周期；阵营经Relation操作，Plant只提供同步开花坐标 |
| `CreatureSetup.entityType(String)` | 本系统客户端装配读取已登记类型，不暴露内容表或可变实例数据；跨系统自然生成按注册ID引用 |
| `Butterflies.register / Organisms.register / YaoshouCreatures.register / NaturalCreatures.register / GhostCreatures.register` | 本系统注册实体、属性、刷怪蛋工厂和原生刷怪位置；妖兽当前装配52个编号，自然生物补入魔兽001/003、桃木精和修者0101；鬼怪02/03保留来源注册且不添加自然生成；群系出生列表由World保持来源顺序及参数 |
| `BossCreatures.register / models` | 本系统装配首领01/02/04、02飞斧及三枚刷怪蛋；客户端通过只读模型行注册渲染，实例技能、属性和自由NBT由Creature写入；来源四个实体的`init`均为空，不新增自然出生 |
| `ButterflyEntity.mobInteract`及09–12各自原生回调 | 服务端拥有驯服、治疗、状态和生成后代，客户端仅返回来源交互反馈和消费同步表现；实例存档沿用各自原生父类及来源自由NBT |
| `SpiritBeastBags.onCreatureDeath(Entity)` | 生物系统负责按来源袋UUID重建同维度主人首个匹配袋，并清理`zhaohuanwu == 1`召唤物；原生死亡回调在`super.die`之后调用，不反写其他系统玩家档案 |
| `DungeonCreatures.prepare(实体, 等级, 临时攻击者)` | 生物写来源`LEVELnumber`与非空`linshigongjizhe`；Dungeon持有副本标志/控制点、负责生成顺序；默认三波实际生成种类/等级已核，完整自然战斗未验，见[副本记录](../dungeon/logs/2026-11.md) |

Creature还订阅蛊虫系统的`GuchongVictimDataEvent`与`GuchongUpgradeEvent`：前者通过Creature实体等级字段答复击杀成长查询；后者按死者`yaoshou`标签在Creature系统内重算死者妖兽属性。该来源分支读取死者标签而不是升级蛊虫的类型；编译、结构检查及受控真实击杀分支随R10专项通过，完整边界和运行状态见[蛊虫正文](../guchong/architecture.md#2026-11-13-编号01完整来源包)。

001–033、035–043、045–054共52个编号已装入`CreatureSetup`，每个编号保留独立`YaoshouNNNEntity`类、注册ID、刷怪蛋与来源资源；[编号对应及生命周期](architecture.md#编号妖兽直接迁移2026-11-11)集中核对。同步体型属性和001/002皮肤属性、来源模型/动画、动态尺寸及纹理渲染均已接线。来源`Material.GRASS`出生条件按锁定材料快照的8种方块保留。52编号及关联实体通过当前整合构建和专服注册表核对；运行行为、客户端视觉、自然出生和背包清袋仍待专项验收。收纳/释放等袋本体操作尚未实现。skill的延迟回调、毒蔓鞭、冰锥和落雷生成通过[skill公开API](../skill/README.md)调用，不直接访问其实现包。

本批按`Organism01`–`Organism12`核对`typed_field_accesses.csv`未检出强类型外部玩家档案读写；原生主人、生命与本体NBT只由本系统实体修改。死亡清袋入口仅读实体来源自由NBT、袋物品自有`CUSTOM_DATA`、同维度玩家原生物品能力和原生计分板，不涉及强类型玩家档案。其他未实现灵兽袋操作和通用技能仍需逐项核对。

本批来源、验证与未完成项见[本月日志](logs/2026-11.md)；项目里程碑仍记入[项目月度日志](../project/logs/2026-11.md)。

2026-11-07 读取 `Yaoshou033skill01`、`Yaoshou040skill0201`、`Yaoshou040skill03` 后，确认三者只有原生贴图粒子表现和物理，归属与台账转入[粒子库](../particle/README.md)；妖兽的实际发出时机仍随本系统迁移。
