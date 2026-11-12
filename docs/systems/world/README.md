---
topic: world
role: entry
status: partial
updated: 2026-11-12
---

# 世界

标识 `world`，类别：系统。职责：维度、群系、地物、结构投放（[系统设计](../../development/system-design.md)）。

2026-11-11 扩大批次接入32种矿物、33种普通配置型地物、24种模板，以及41个来源群系中的28棵内联树、8种内联圆盘、171个内联投放和16个树装饰器；新增温泉完整流体族和随机结构主世界开关。89种独立Feature登记“已注册”。2026-11-12已正式生成41个来源群系与人间道、妖魂道资源；专服到达`Done`并保存两道。随后各生成一处两道样点新区块，已加载位置的群系身份分别确认为`biomeautumn_01`和`biomefeng_01`，人间道Y=16样点为石头；人工configured feature调用与高度图告警边界详见[月志](logs/2026-11.md)。这不等于41群系全量地形、自然地物或出生验收；新树/晶洞自然投放也仍待验收。

当前阶段：D/E自然供给整批装配与运行验证。55个原版生成根静态核验全部闭合：38项可复用、14项来源自定义实现、3项洞穴配置，0项待核。最新严格计划为41个群系、2973条生成操作、483条出生记录，全部依赖blocker=0（`tmp/source-biomes-plan-next.json`）；原16个实体缺口已由Creature、Guchong对应实现闭合。统一数据转换已写出41个群系和两道各自的dimension、dimension_type、noise_settings及5个密度函数，目标现有42个群系（含虚空）；转换清单2765项。完整`build compileSmokeJava`通过，含test/check与ArchUnit（`tmp/world-natural-resume-build.log`）。

维度转换门禁已补查全部计划数据中的BlockState `Name`，以内容表、手工行为台账和精确目标blockstates核对身份；模拟缺失quicksand以及默认方块、默认流体、嵌套地表规则未知ID的针对性验证均准确拒绝，现有16份计划通过。2026-11-12正式资源加载到`Done`，注册导出与台账检查0问题；该次加载未强制加载自定义维度区块。后续样点区块运行证据见上方摘要和月志。

首次1.21.1版本专服启动在解析妖魂道地表规则时失败：来源quicksand方块尚未注册（`tmp/world-natural-server.log`）。已按来源职责将流沙方块及源/流态改归World并实现完整流体族。2026-11-12重跑完成：专服到达`Done`，42个群系进入注册表导出，两道维度均参与原生保存，台账4939行0问题，日志证据为`tmp/world-natural-registry-server.log`。随后在512 MB堆编译并登记`minecraft:bee`的无约束出生位置，使用单worker、1 GB堆专服复核到`Done`，蜂类缺失放置注册错误消失；执行`save-all flush`与`stop`后正常保存退出，证据`tmp/world-natural-bee-placement-server.log`。静态核对确认41群系的483条出生记录与目标数据完全一致；32条、涉及11种实体的类别差异按来源保留，因为刷怪额度由群系类别与实体自身类别共同计算，详见[Creature月志](../creature/logs/2026-11.md)。两道样点新区块由一次1 GB堆专服生成，`biomeautumn_01`、`biomefeng_01`的已加载位置检查均命中；人间道Y=16抽查为石头。证据分别为`tmp/world-biome-postcheck-server.log`与`tmp/world-minimal-biome-check-server.log`。后者的`/place feature yvanchuxiuzhen:cailiao_191ore`调用成功，但记录`Unprimed heightmap: OCEAN_FLOOR_WG`，只证明configured feature命令可执行，不证明自然placed feature正确；自然地物与实体出生、41群系全量实机覆盖、客户端视觉仍未验收。本轮未启动客户端。新树/晶洞实际放置与自然生成、19个未实现方块材料、完整视觉、1.18.2版本实机和两版本对照仍未验收。缺失方块以显式可选标签保留，未补占位；滴水石历史受控检查的高度图ERROR与Redirect未独立观测边界仍保留。World与全量active goal均保持未完成，不推送、不合main。

同日沿用现有客户端检查，正式维度下裂隙传送/返回12项通过；直接触发与真实碰撞各自的覆盖及未验收范围见[技能正文](../skill/architecture.md#虚空裂隙2026-11-07)，不据此升级整个世界系统的状态。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 来源归属与阅读范围见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)，已读条数不作为功能验收数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [变更记录](changes.md)：历史批次与月志索引。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

晶洞与树补充：`SourceGeodeFeature`及17类来源树实现、Codec和注册已接入；晶洞4资源、树29资源完成统一转换和源码指纹核验，保留来源近似逆平方根、根系、竹林、丛林及草甸树算法。上述构建和客户端完成Codec加载回归，实际放置与自然生成仍未验收。

## 公开操作

其他系统通过原生服务端查找 `yvanchuxiuzhen:xvkong`、`yvanchuxiuzhen:dimensionrenjian`与`yvanchuxiuzhen:dimensionyaohun`。虚空类型为同名维度类型，唯一群系为 `yvanchuxiuzhen:xvkongqvnxi`；两道同名维度类型与41个来源群系已落盘并通过专服加载，人间道与妖魂道各一处新生成样点区块分别命中`biomeautumn_01`、`biomefeng_01`。这仅是两处群系身份样本；全41群系地形分布、自然地物投放及自然出生仍待验收。

2026-11-11 自然地物批次新增 World 自有操作，已完成启动装配，运行覆盖由本批月志分别登记：

- `WorldGeneration.randomStructuresAllowed(MinecraftServer)`：查询主世界共享随机结构开关，对应来源`!MapVariables.jiegoushengcheng`，初始允许。
- `WorldGeneration.setRandomStructuresAllowed(MinecraftServer, boolean)`：唯一写入入口，更新不可变Settings并标脏；其他系统不得另存或直接修改同值。
- `WorldGeneration.register()`：启动时安装主世界`LevelEvent.Load`和服务端结束监听。主世界加载且初始化出生区块前读取SavedData，通过线程安全服务端索引发布；地物生成线程只读取volatile Settings，结束后移除索引。
- `WorldGenerationCommands.register()`：安装实际原初管理命令中的两个结构分支，语法`/yvanchu <当前维度玩家显示名> unlock|lock jiegoushengcheng [true]`，权限等级1。来源没有`go`字面子命令。

已对照 `typed_field_accesses.csv`：`XvkongDimension`、`XvkongqvnxiBiome`没有玩家或世界持久字段读写；技能裂隙的返回编号`tizhiitem.weidu`仍由xiulian公开操作管理。随机结构字段包含`Suijijiegou01FuJiaDeShengChengTiaoJianProcedure`一次条件读取，以及`YvanchuGoProcedure`的unlock/lock两次写入，分别由六个地物查询与管理命令调用World拥有者承担。其余世界状态、结构操作与外部字段访问随对应批次列出；world不写玩家档案。

本轮变化记录在[本系统月志](logs/2026-11.md)与[项目月志](../project/logs/2026-11.md)。
