---
topic: world
role: entry
status: partial
updated: 2026-11-26
---

# 世界

768 MB 客户端已通过地府内置模板核对、替换模板首入，以及原溢出种子的门户往返与实际重进。连续多维回归仍有地府首入及后续跨维的内存峰值，见[跨维时间线](logs/2026-11.md#2026-11-26-地府入场与后续跨维时间线)。

31种矿物和霓裳草已按来源恢复全群系共享地物入口；已加载群系与维度排序通过静音检查，实际自然生成仍待验证，见[世界正文](architecture.md#全群系矿物与霓裳草入口2026-11-20)。

人间道冬季冰湖改用来源湖泊算法；生产编译通过，自然新区块生成未验，见[世界月志](logs/2026-11.md#2026-11-20-人间道冬季冰湖)。

人间道新档的原生门户往返、固定首入与同档重进已通过：目标门和POI复用，首次场景生成，重进不重复生成。小尸王场景改用目标版本离线升级产物后，固定首入专项复验通过。见[固定首入实机记录](logs/2026-11.md#2026-11-18-人间道固定首入与保存重进)。

人间道随机结构01、03、04已通过一次真实新区块放置及同档读回复验；仅闭合限定样点，01跨区块裁切和自然分布仍待核。见[世界月志](logs/2026-11.md#2026-11-18-人间道随机结构自然样点与重进)。

修罗道已接入维度、维度类型和地形资源；八份资源定向转换逐字节一致，必要客户端通过首次与真实重进后两次往返、原出口复用和耐久保存。修罗沿用原版下界荒地，不增加来源自定义群系数量；自然地形、出生与多人仍待。见[修罗正文](architecture.md#修罗道维度接线2026-11-18)。

2026-11-17 在独立 R06 worktree 接续四道资源后，静态实现 Overworld 首次登录时生成传送阵及全局中心坐标查询；随机取样已按 NeoForge 21.1.265 `Mth.nextInt(RandomSource,...)` API 修正。此前手写NBT版本同步至集成树后，`gradlew --no-daemon --max-workers=1 compileJava`以768 MB堆通过；本批改用不可变`TeleportArray`和Codec读写Overworld SavedData，并在独立功能树以768 MB、单worker通过`compileJava`。首次登录结构位置/画面、保存重进、多人登录及 xiulian 消费交互仍未验证。消费者候选还未集成，见[本系统月志](logs/2026-11.md#2026-11-17-首次登录传送阵坐标与公开查询)。

标识 `world`，类别：系统。职责：维度、群系、地物、结构投放（[系统设计](../../development/system-design.md)）。

2026-11-18天阙七群系已合入，当前无归十群系独立单元已完成正式转换、注册加载、双编译/六架构和必要限定客户端，功能`29f44f24`已由`6e86380d`非快进合入。无归两草/魔眼模板真实保存重进、门户首次及重进后再次完整往返通过；当前资源为六自定义维度/60目标群系，修罗/地灵及余八来源群系待接收。一般门户组合在既有人间道加载随机结构时发生768 MB OOM，限定单项通过不关闭该问题。规则与未验边界见[无归正文](architecture.md#无归十群系与门户接收2026-11-18)，原件见[无归月志](logs/2026-11.md#2026-11-18-无归十群系接收与限定门户客户端)；天阙的既有有限证据见[天阙正文](architecture.md#天阙七群系与门户接收2026-11-18)。

2026-11-18公共卸载Future关联诊断已集成并取得实际样本：灵兽袋首轮为正常回调，修订轮在普通运行早期出现已完成同一Future、holder回到updating且生成引用未释放的密集重排，随后自行收敛并保存完成。精确源码说明旧回调可跨pending→updating迁移继续排队，当前task为空也不能排除邻区任务持有引用；具体任务/票据调用者仍未知。生产卸载、保存和取消条件未改，不能由本次样本推导旧停服挂死已修复，见[正文](architecture.md#公共停服回调future身份关联2026-11-18)及[修订轮月志](logs/2026-11.md#2026-11-18-future关联修订轮与短时密集重排)。

固定结构解封已接通28名称／42模板、延迟任务/NPC补充和镇妖塔44点消费者。管理命令必要单项已过；自然寻灵盘/光环/裂隙与共享进度已接入，最近目标、16格触发、2/4/80/82刻、回程与真实保存重进必要单项已通过；Q113全维度掉落物清理和Q115跨结构商品补充保持来源。规则与本批验证边界见[结构解封正文](architecture.md#固定结构解封命令2026-11-14)及[月志](logs/2026-11.md#2026-11-14-固定结构解封与镇妖塔后处理)。

历史原主目录工作区曾接入六道维度资源及20个模板feature、两个DeltaFeature；该组合未进入当前集成分支。注册阻断修复后编译通过，客户端进入世界后在768 MB堆下区块生成/加载OOM，未形成新的门户行为结论。旧两道往返/四道拒绝证据仅覆盖四道资源接入前；本次原始日志、崩溃报告及未覆盖边界见[月志](logs/2026-11.md#2026-11-16-六维度注册修复与区块生成oom)。R06签筒/装饰梯子供给包仍由独立树实现，其他既有样点与自然供给证据保持下述边界。

2026-11-11 扩大批次接入32种矿物、33种普通配置型地物、24种模板，以及41个来源群系中的28棵内联树、8种内联圆盘、171个内联投放和16个树装饰器；新增温泉完整流体族和随机结构主世界开关。89种独立Feature登记“已注册”。2026-11-12已正式生成41个来源群系与人间道、妖魂道资源；专服到达`Done`并保存两道。随后各生成一处两道样点新区块，已加载位置的群系身份分别确认为`biomeautumn_01`和`biomefeng_01`，人间道Y=16样点为石头；人工configured feature调用与高度图告警边界详见[月志](logs/2026-11.md)。这不等于41群系全量地形、自然地物或出生验收；新树/晶洞自然投放也仍待验收。

当前阶段仍为D/E自然供给整批装配与运行验证。41群系、2973条生成操作/483条出生及55原版根的既有严格来源证据继续复用；天阙七群系与无归十群系另按上方正文接收。2026-11-12的42群系、转换清单2765项，以及画卷接入后的43群系、2822行是各自历史批次口径；本单元六自定义维度/60群系以实际资源和本批注册导出为准。过去完整`build compileSmokeJava`证据保持原范围，各单元只运行必要双编译、六项架构检查及对应限定场景。

维度转换门禁已补查全部计划数据中的BlockState `Name`，以内容表、手工行为台账和精确目标blockstates核对身份；模拟缺失quicksand以及默认方块、默认流体、嵌套地表规则未知ID的针对性验证均准确拒绝，现有16份计划通过。2026-11-12正式资源加载到`Done`，注册导出与台账检查0问题；该次加载未强制加载自定义维度区块。后续样点区块运行证据见上方摘要和月志。

首次1.21.1版本专服启动在解析妖魂道地表规则时失败：来源quicksand方块尚未注册（`tmp/world-natural-server.log`）。已按来源职责将流沙方块及源/流态改归World并实现完整流体族。2026-11-12重跑完成：专服到达`Done`，42个群系进入注册表导出，两道维度均参与原生保存，台账4939行0问题，日志证据为`tmp/world-natural-registry-server.log`。随后在512 MB堆编译并登记`minecraft:bee`的无约束出生位置，使用单worker、1 GB堆专服复核到`Done`，蜂类缺失放置注册错误消失；执行`save-all flush`与`stop`后正常保存退出，证据`tmp/world-natural-bee-placement-server.log`。静态核对确认41群系的483条出生记录与目标数据完全一致；32条、涉及11种实体的类别差异按来源保留，因为刷怪额度由群系类别与实体自身类别共同计算，详见[Creature月志](../creature/logs/2026-11.md)。两道样点新区块由一次1 GB堆专服生成，`biomeautumn_01`、`biomefeng_01`的已加载位置检查均命中；人间道Y=16抽查为石头。证据分别为`tmp/world-biome-postcheck-server.log`与`tmp/world-minimal-biome-check-server.log`。后者的`/place feature yvanchuxiuzhen:cailiao_191ore`调用成功，但记录`Unprimed heightmap: OCEAN_FLOOR_WG`，只证明configured feature命令可执行，不证明自然placed feature正确；自然地物与实体出生、41群系全量实机覆盖、客户端视觉仍未验收。本轮未启动客户端。新树/晶洞实际放置与自然生成、19个未实现方块材料、完整视觉、1.18.2版本实机和两版本对照仍未验收。缺失方块以显式可选标签保留，未补占位；滴水石历史受控检查的高度图ERROR与Redirect未独立观测边界仍保留。World与全量active goal均保持未完成，不推送、不合main。

同日沿用现有客户端检查，正式维度下裂隙传送/返回12项通过；直接触发与真实碰撞各自的覆盖及未验收范围见[技能正文](../skill/architecture.md#虚空裂隙2026-11-07)，不据此升级整个世界系统的状态。

修者0101保留已有条目并补缺；实机名单共124条，主世界受控原生抽签及同一实体的磁盘重进通过。人间道常规自然调度与新区块实际出生仍未验，见[修者名单正文](architecture.md#修者0101群系自然名单2026-11-18)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 来源归属与阅读范围见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)，已读条数不作为功能验收数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [变更记录](changes.md)：历史批次与月志索引。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

晶洞与树补充：`SourceGeodeFeature`及17类来源树实现、Codec和注册已接入；晶洞4资源、树29资源完成统一转换和源码指纹核验，保留来源近似逆平方根、根系、竹林、丛林及草甸树算法。目标树实现指纹已统一使用仓库LF规范，换行基线修复与必要轻量检查见[月志](logs/2026-11.md#2026-11-13-目标树实现指纹换行基线修复)；本次没有重跑转换或游戏。上述历史构建和客户端完成Codec加载回归，实际放置与自然生成仍未验收。

## 公开操作

`RenjianEnteredEvent(ServerPlayer)`：玩家实际跨维到人间道后发布；首次到达先完成World的固定场景与重生点接线，修炼系统独占个人赠礼与体质字段，见[首入正文](architecture.md#人间道固定首入2026-11-18)。

`StructureSearch.search(ServerLevel, Vec3, String)`供Quest调用遗迹搜索；World独占28目录/主世界共享已寻进度，盘、光环、裂隙及来源两命令复用同一入口。规则与Q116–Q119见[自然遗迹链](architecture.md#寻灵盘与自然遗迹链2026-11-14)，本批结果见[月志](logs/2026-11.md#2026-11-14-寻灵盘光环与自然裂隙)。

`StructureReleases.release(ServerLevel, ServerPlayer, String, boolean)`是World内部固定结构释放写入口；公开`StructureReleasedEvent(level, structureName)`在来源40/60/100刻后同步通知内容所有者。Quest、NPC、Dungeon只处理自身数据，World不反向依赖消费者。实际管理命令语法见[结构解封正文](architecture.md#固定结构解封命令2026-11-14)。

其他系统通过原生服务端查找`yvanchuxiuzhen:xvkong`、`dimensionrenjian`、`dimensionyaohun`、`fabao_009weidu`及本批`dimensiontianqve`。六组天道门户通过`DimensionPortalBlock`和`DimensionPortalItem`公开接入，门户方块、POI、出口建门和`DimensionTransition`由World独占。人间道/妖魂道既有往返与保存证据复用；天阙首次入场回程及真实重进后门户/POI保存见[天阙正文](architecture.md#天阙七群系与门户接收2026-11-18)。修罗/无归/地灵仍只沿用资源缺失时拒绝的有限证据，历史原主树四道候选不计当前集成。画里乾坤、虚空及两道历史样点的边界保持各自正文；全分布、自然地物/出生和多人仍待验收。

2026-11-11 自然地物批次新增 World 自有操作，已完成启动装配，运行覆盖由本批月志分别登记：

- `WorldGeneration.randomStructuresAllowed(MinecraftServer)`：查询主世界共享随机结构开关，对应来源`!MapVariables.jiegoushengcheng`，初始允许。
- `WorldGeneration.setRandomStructuresAllowed(MinecraftServer, boolean)`：唯一写入入口，更新不可变Settings并标脏；其他系统不得另存或直接修改同值。
- `WorldGeneration.register()`：启动时安装主世界`LevelEvent.Load`、服务端玩家登录和服务端结束监听。主世界加载且初始化出生区块前读取SavedData，通过线程安全服务端索引发布；地物生成线程只读取volatile Settings，首次Overworld登录在服务端主线程生成传送阵并更新坐标记录，结束后移除索引。
- `WorldCommands.execute`：由 flow 的唯一 `/yvanchu` 入口按当前维度显示名定位玩家后分派结构开关与解封，结构开关语法仍为`/yvanchu <当前维度玩家显示名> unlock|lock jiegoushengcheng [true]`，权限等级1；解封仍用`unlock sealrelease <结构名> [true]`。来源没有`go`字面子命令。2026-11-15 共享入口拆分只完成静态核对，重构后的命令回归未执行，见[正文](architecture.md#固定结构解封命令2026-11-14)。
- `WorldStructures.place(ServerLevel, ResourceLocation, BlockPos)`：副本/画卷固定点结构放置，NONE旋转、NONE镜像、包含实体、flags3；由World访问原生模板服务，Dungeon拥有调用条件和实例坐标。候场大厅与战斗结构已随自然入场实际放置，私人画卷结构与出口也已随真实二击传送生成，见[副本记录](../dungeon/logs/2026-11.md)。
- `WorldStructures.teleportArrayCenter(ServerLevel): Optional<Vec3>`：读取 Overworld 所有的传送阵中心，结构完成后返回实际中心，初始化前返回空；调用方只读，不保存维度副本。xiulian的初始奖励候选路径（R08跨模块）依赖此公开查询，为无良商贾的坐标纸张和流浪侠客的 `ling_11` 提供同一实际中心；消费者仍在独立候选且未集成。调用仅限服务端主线程；此前手写NBT版本的集成树`compileJava`通过，当前Codec版本在独立功能树以768 MB、单worker通过compileJava，首登、奖励、Ling11消费和保存重进仍待运行验收。来源与目标边界见[首次登录传送阵](architecture.md#首次登录传送阵坐标与公开查询2026-11-17)。

已对照 `typed_field_accesses.csv`：`XvkongDimension`、`XvkongqvnxiBiome`没有玩家或世界持久字段读写；技能裂隙的返回编号`tizhiitem.weidu`仍由xiulian公开操作管理。随机结构字段包含`Suijijiegou01FuJiaDeShengChengTiaoJianProcedure`一次条件读取，以及`YvanchuGoProcedure`的unlock/lock两次写入，分别由六个地物查询与管理命令调用World拥有者承担。其余世界状态、结构操作与外部字段访问随对应批次列出；world不写玩家档案。

本轮变化记录在[本系统月志](logs/2026-11.md)与[项目月志](../project/logs/2026-11.md)。

数据重转由世界转换器恢复五份来源结构群系标签。
