---
topic: world
role: architecture
status: draft
updated: 2026-11-11
---

# 世界：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 正式虚空维度（2026-11-07）

本节为 skill 当前依赖的直接来源迁移，不表示后文世界主体方案已实施。来源为 `A:world/dimension/XvkongDimension.java`、`A:world/biome/XvkongqvnxiBiome.java` 与 `data/yvanchuxiuzhen/{dimension,dimension_type}/xvkong.json`；编辑器两个元素与实际注册、技能裂隙调用已交叉阅读。归属 world，当前无可变状态或新 Java API，公开契约见 [README](README.md#公开操作)。

### 1.18.2 版本实际规则

- `minecraft:noise` 生成器，单项 multi_noise 群系 `xvkongqvnxi`；噪声范围 Y=0–127、水平格宽8、垂直格高4。默认方块、流体和地表均为空气；关闭原生生成生物、含水层和矿脉。群系没有地物、雕刻器或生物条目，原版结构的适用群系不包含它。
- 群系有降水，温度与降水量均0.5；雾、水、水雾、天空、草、叶颜色均为来源整数−16777216；没有环境音或背景音乐定义。
- 维度范围 Y=0–255，逻辑高度256，坐标倍率1；无天光、无顶、环境亮度0、非自然、非灼热、猪灵安全；床可用、重生锚不可用、无袭击、不固定时间。
- 类型 JSON 实际绑定 `minecraft:overworld`。Java 登记的同 Mod ID 无天空特效没有被该类型引用，本批保持实际绑定，不主动切换效果。
- `Fixers` 在公共初始化中向全局 `WorldCarver.CAVE`、`CANYON` 的可替换集合加入AIR；它不只作用于虚空。虚空群系本身没有雕刻器。

### 1.21.1 版本表达

`tools/porting/convert_world.py` 由 `convert_data.py` 调用，直接读取来源维度、类型和实际群系 Java 定义，四个目标文件均写入 `data_manifest.csv`，登记来源路径与SHA-1。正式虚空资源位于主源集；移除 smoke 同 ID 的平坦基岩替身。

精确目标依据为 NeoForge21.1.265 的 `NoiseBasedChunkGenerator`、`NoiseGeneratorSettings`、`DensityFunctions`、`NoiseRouterData`、`DimensionType`、群系相关Codec、`Carvers` 与 `WorldCarver`。1.18.2版本密度函数与滑移公式另从同版本Minecraft源码核对，Forge40.1.0源码包没有修改这些类的补丁。转换差异：

| 来源字段或机制 | 目标表达与影响 |
| --- | --- |
| `minecraft:slide` 与 noise 的上下滑移 | 按垂直格高4展开：顶部从Y56至312、由1至0，目标−23.4375；底部从Y4至32、由0至1，目标−0.234375；保留先顶部后底部，以及原插值、混合、压缩顺序 |
| `sampling`、`island_noise_override`、`terrain_shaper` 与旧开关 | 目标Codec已移除，保留来源路由引用 `minecraft:end/sloped_cheese` 与其他密度字段；旧sampling的水平2/格宽8、垂直1/格高4对应目标End混合噪声0.25/0.25、因子80/160、smear倍率4；地形输出仍为空气 |
| 生成器独立 `seed` | 目标生成器Codec已移除，改用目标原生世界种子；只有一个群系、方块与流体均为空气，因此不产生地形或群系分布差异 |
| 旧 `structures` 分布表 | 目标按结构集与结构适用群系决定；该空群系不加入任何原版结构群系标签，保留无自然结构 |
| 新必填 `spawn_target` | 空列表；不新增玩家出生点偏好 |
| 新必填怪物光照字段 | 方块光上限0、总亮度阈值0–7均匀取值，对应1.18.2版本 `Monster.isDarkEnoughToSpawn`；群系没有自然生物条目 |
| CAVE/CANYON 的全局AIR替换 | 给 `minecraft:overworld_carver_replaceables` 增补AIR；目标原版 cave、cave_extra_underground、canyon 共用此标签，nether_cave使用另一标签。其他Mod自定义雕刻器配置不属于本批对照范围 |

技能入虚空时按裂隙坐标各轴除50并四舍五入，返回乘50，编号保存在 xiulian 体质数据；本批不改变传送代码。其他六个自定义返回维度尚未接入，随 E 阶段各自实现。正式维度的数据加载与保存证据见下方记录；完整视觉、自然裂隙往返、多人、1.18.2版本实机和两版本对照尚未执行。

### 本批验证

`build`（含ArchUnit）通过；补入来源群系名称后，专用服务器仅启动一次至Done，正式维度与群系加载成功。原生命令强制加载虚空区块(0,0)，在(0,64,0)同时确认 `xvkongqvnxi` 与空气；随后移除强加载，原生stop保存四个世界并正常退出，日志ERROR0，导出群系1个。没有新增测试类、专项选择器或重跑旧裂隙检查。证据为 `tmp/world-void-build.log`、`tmp/world-void-content.log`、`tmp/world-void-server.log` 与 `build/registry-export/biome.txt`。

本次确认数据加载、一个实际生成区块及保存；不作为自然裂隙往返、客户端视觉、多人或两版本实机对照的证据。台账仅标“已注册”。

## 自然供给地物与随机结构（2026-11-11）

本节记录直接来源迁移的32种矿物、24种模板、33种普通配置型地物、41个来源群系内联生成片段与16个树装饰器。来源完整群系、正式人间与妖魂维度及自然可达性仍按各自台账记录；生成机制接入不等于这些系统已经闭合。逐项证据与SHA256见 [来源证据](ledger/source-evidence.csv)和[来源指纹](ledger/source-fingerprints.csv)。证据中的owner严格沿用内容台账，矿物产物、植物树木与World自然投放职责分别记录；41个群系只登记本节读取的内联片段，其整体ownership保持候选。

### 矿物与内联圆盘

来源 `A:world/features/ores` 完整32种OreFeature均用纯Block身份比较匹配 `Blocks.STONE`，没有深板岩、下界岩或矿物替换标签。目标使用 `minecraft:block_match`，零空气暴露丢弃率；30种来源triangle高度映射目标trapezoid且plateau0，Bluestone与辰砂两种uniform保持。矿脉大小、次数、高度、31个全群系选择器与Bluestone13个群系，以及各地物的维度门槛独立保留。`DimensionOreFeature`在原生算法取随机数前过滤维度，`WorldOreFeatures`保留原始注册ID；转换由 `convert_ore_worldgen.py` 完成。

8个来源内联沙/砾石圆盘使用 `legacy_biome_disk` 类型与目标DiskConfiguration格式。`LegacyBiomeDiskFeature`先保留来源`Feature.DISK → DiskReplaceFeature`在落点检查`FluidTags.WATER`的守卫，再保留1.18.2版本BaseDiskFeature的X外/Z内/Y降序、最低额外一层、固定状态、半径单次采样、setBlock标志2和后处理。若FallingBlock刚放置的上一层下方原状态为空气，上一层变砂岩；红沙才变红砂岩，来源砾石同样变普通砂岩。目标原生DiskFeature已删除该支撑逻辑，因此此处保留来源算法。

### 普通配置型地物

`convert_native_worldgen.py`完整读取33种配置型地物：9个RandomPatch、5个SimpleBlock、3个BlockBlob、1个BlockPile、2个Lake、7个BlockColumn、3个Coral、3个ReplaceBlobs。来源配置、provider、默认方块状态、整数分布与投放修饰器逐表达式展开；新增或未知表达式、遗漏地物及额外place行为均直接报错。33个同名Feature保留独立注册身份，`DimensionFeature`在原算法取随机数前检查原维度门并转发同一Context。华光柱、石树01和石树05来源没有维度门，保持无条件入口。

22项含BiomeFilter、11项没有BiomeFilter，按来源顺序保留。腐竹依次执行高度范围、次数、方形散布、向下环境扫描、群系过滤；扫描目标为空气且下方腐土，允许条件为空气，最大10步。紫水晶柱的次数位于高度图之后。冰湖的fluid与barrier均为空气。BlockColumn.simple展开为单层、向上、只允许精确 `minecraft:air`、prioritize_tip=false；空气或水谓词只匹配两个注册方块，不扩大到任意isAir或流体状态。

3种石团使用 `SourceBlockBlobFeature`保留三次团块、随机半径、位置遍历、结束偏移与写方块标志4；目标原生BlockBlob已将该标志改为3。来源Forge40.1.0的 `Feature.isStone`读取 `forge:stone`，与目标 `base_stone_overworld`不同。转换器从锁定来源recomp.jar读取并核验12个成员，生成固定 `legacy_worldgen_stone` 标签：安山岩、闪长岩、花岗岩、虫蚀石头、石头、磨制安山岩、磨制闪长岩、磨制花岗岩、深板岩、磨制深板岩、虫蚀深板岩、凝灰岩。来源 `Feature.isDirt`读取 `minecraft:dirt`；转换器从锁定1.18.2版本client.jar读取7个成员，生成固定 `legacy_worldgen_dirt` 标签：泥土、草方块、灰化土、砂土、菌丝体、缠根泥土、苔藓块。地面判定使用上述7土或12石，保留磨制与虫蚀石料上的落点；不引入目标版本泥土标签新增的泥巴与沾泥红树根。两份资源均经转换器输出并登记来源归档指纹。

珊瑚三种原版算法、RandomPatch、SimpleBlock、BlockColumn、BlockPile、ReplaceBlobs与精确目标源码已逐读；循环、概率、方向顺序、shuffle随机消费与方块更新标志对应。Lake的Material.isLiquid/isSolid迁为目标BlockState.liquid/isSolid，仍依赖内容方块的来源材质属性。灯心草、浮苔的来源onPlace还会检查上方source流体并写waterlogged=true；其内容消费者按Plant台账闭合，默认态配置相符本身不证明回调完成。

### 群系内联树与16个装饰器

范围沿来源人间、妖魂维度biome_source与群系实际注册解析，共41个群系文件。`convert_biome_inline_worldgen.py`转换其中实际调用的内联TreeConfiguration、DiskConfiguration及PlacedFeature，保留树干、树冠、最小尺寸、ignoreVines、forceDirt、原默认泥土、装饰器与投放顺序。每个注册名独立输出，不以新模板替换旧配置；未知表达式或重名目标直接报错。

16个来源树装饰器只有七个群系内联树配置调用，归World；四种依赖植物方块按注册ID读取默认态，不写Plant持久状态。六种Leave逐叶抽取nextInt(4)==0，检查正下方空气后写第一格，再向下最多四格；延伸循环保留先检查空气、再判断剩余计数的顺序。六种Trunk逐树干抽取nextInt(3)>0，检查下方空气后写当前树干位置，检查位置与写入位置的差异保留。

四种Fruit整树抽取nextFloat()<0.2F，取首个树干Y，仅处理相对高度不超过2的树干；按水平面方向顺序逐向抽取nextFloat()<=0.25F，取反方向邻格，空气时写默认态。没有树干为空时的兜底，来源同样直接取首项。`biomewind_02_tree_fruit_decorator`写空气时仍调用生成setter。藤蔓不附加面属性，可可不附加朝向或年龄；目标 `TreeDecorator.Context`沿用原生成setter，16个类型各自使用无字段MapCodec与原注册ID。

### 24种模板与生成顺序

`WorldTemplateFeatures`分别登记华光、恶灵树01–04、黄土石、丁香01、箭毒木01、柱子岛屿01–04、浮空礁石、随机结构01–06、石树02/03/04/06/07。`WorldTemplateFeature`保留维度、概率、次数、基底、Heightmap、偏移和处理器；算法按概率、次数、X/Z、高度、基底、随机结构开关、镜像/旋转、模板放置执行。镜像只取原枚举前2项、旋转前3项，保留来源nextInt(1)随机消费；黄土石为nextInt(6)+3次。普通模板忽略AIR，随机01–04忽略STRUCTURE_AND_AIR，05/06只忽略STRUCTURE_BLOCK；保留实体，首次放置标志2。

华光首次成功放置后保留来源后置过程的 `world instanceof ServerLevel`条件。自然生成使用WorldGenRegion，不追加后四层；直接ServerLevel执行时，在Y+48/96/144/192各追加一次同模板，标志3、无忽略处理器、未setRandom、使用该ServerLevel.random。保持来源守卫及全部高度调用。

### 随机结构开关与管理入口

来源 `MapVariables.jiegoushengcheng`为主世界范围、初值false，实际生成条件为取反。World是唯一写入者，`WorldGeneration.Settings`以Codec/StreamCodec定义字段，主世界SavedData保存该键；公开操作为 `randomStructuresAllowed(server)`与 `setRandomStructuresAllowed(server,allowed)`，修改后标脏，六个Feature不保存第二份开关。

存档读取只在主世界LevelEvent.Load的服务端线程执行。精确目标MinecraftServer先发布该事件，随后才初始化出生区块；通过ConcurrentHashMap安全发布SavedData，地物线程只读取volatile Settings，不访问非线程安全的DimensionDataStorage HashMap。服务端停止后清理索引；保存与修改仍由World承担。

已对照typed_field_accesses.csv及全部来源Java：附加条件过程是条件消费者，六个Feature调用；`YvanchuGoProcedure`是唯一实际写入过程，unlock写false，lock写true。`WorldGenerationCommands`接回精确语法 `/yvanchu <当前维度玩家显示名> unlock|lock jiegoushengcheng [true]`，permission1；Go是Java过程名，没有go字面子命令。

保留空白分词、缺参空串、当前维度显示名精确比较、首个匹配后结束及返回0。第四参数恰为true时向目标玩家发送原开启或关闭提示；找不到匹配时向执行者发送原红色错误，控制台由FakePlayer承接。此开关影响随后地物尝试，不触发重生成。来源完整MapVariables广播没有此字段的玩家侧消费者，目标不额外广播整份世界档案；Settings保留StreamCodec。本节只接结构unlock/lock两个管理分支。

### 温泉流体

`SpringFluids`独立登记温泉静止/流动流体、FluidType、液体块与水桶，保持原ID、UNCOMMON、密度1000、黏度1000、温度300、不可自增水源、爆炸抗性100及4/1/5流动参数。来源两种温泉流体均属于water标签，目标保留相应身份。桶堆叠1、归还空桶，加入来源材料页。

`SpringLiquidBlock`在受伤且无恢复时给予40tick恢复0级，并在该次施加分支内以10%概率给予600tick的017效果0级；已有恢复时不抽取该概率，碰撞过程不添加服务端守卫。来源客户端随机展示入口内部仍检查ServerLevel，再发送三个BUBBLE_POP；目标保持该守卫。客户端保留无世界颜色−13083194、世界群系平均水色、两种贴图与半透明渲染层。温泉湖的自然可达性由对应群系与维度装配闭合。

1.18.2版本与精确目标的恢复效果均以剩余duration模50判断0级治疗，并仅在duration大于0时应用。来源40tick时长依次经过40至1，不产生一次治疗；效果结束后再次接触只会重新施加同样时长。该批保持来源行为，受控断言检查效果存在及持续时间，不将血量增加作为预期结果，也不登记回血已验收。

温泉碰撞样本须启用实体正常移动流程。精确目标中 `Mob.isEffectiveAi`在NoAI时返回false，`LivingEntity.travel`据此跳过移动；方块 `entityInside`检查位于 `Entity.move`内。因此直接在池内召唤静止NoAI猪不能触发碰撞回调，其未获得效果不证明温泉实现失效。受控样本使用围墙约束启用AI的受伤猪，并分别读取Health、Pos及 `active_effects`；目标效果NBT使用小写 `active_effects`和字符串id。实测结果单独登记于本批月志。

### 本批验证范围

以上规则登记静态来源、精确目标源码与实现职责。运行结果由本批月志及交接登记；此前虚空或植物启动结果不作为新增地物、命令和温泉流体的运行验收证据。1.18.2版本实机与两版本对照尚未执行，完整World系统仍未完成。

## 原版群系依赖对齐（2026-11-11）

41个来源群系的55个原版生成根由 `verify_vanilla_biome_parity.py` 分别核对配置引用图、展开标签和锁定源码指纹。55个原版生成根静态核验全部闭合：38项可复用、14项来源自定义实现、3项洞穴配置，0项待核。完整数据转换2600条记录；41群系计划保持2973条生成操作、483条出生记录，当前唯一阻断为46个缺失实体类型。计划与静态核对不代表完整群系已装配或自然生成已验收。

化石使用来源0.9/0.1腐化、保护方块和煤矿替换深层钻石矿处理器；16份原版骨架与覆盖模板除DataVersion外字节一致。熔岩湖通过 `SourceLakeFeature` 保留来源循环、随机调用和写入次序，solid/liquid谓词来自锁定的2078个来源方块材料快照；湖泊与化石保护标签使用来源成员。Cave/Canyon复用已核对的目标算法，替换标签为来源43项默认成员与维度Fixer追加成员的60项并集。

紫水晶晶洞使用 `SourceGeodeFeature`：1.18.2版本在分布点和裂缝密度累加时调用 `Mth.fastInvSqrt(double)`，1.21.1版本改为 `Mth.invSqrt(double)`，会跨越层阈值。独立实现保留来源位常量6910469410427058090及一次Newton迭代、随机调用、分布点/裂缝遍历、逐格写入和晶簇方向顺序。固定分布点、噪声置零的33³公式样本有10处层判定差异，说明不能直接替换；这不是实际种子生成对照。

两版本均按世界种子创建 `WorldgenRandom(new LegacyRandomSource(seed))`，以 `NormalNoise.create(random, -4, 1.0)` 创建两个Perlin噪声。已逐段核对 `forkPositional`、`octave_-4`字符串散列、LCG及位数、ImprovedNoise置换/插值、输入缩放0.0625与第二噪声缩放1.0181268882175227，相关调用链的有效运算与顺序一致；未运行两版本噪声自动对照。14个来源/目标类的原始源码指纹保存在 `tools/porting/data/legacy_geode_source_fingerprints.json`，验收器要求全部匹配，同时核对方向/坐标遍历、写入及晶簇谓词所依赖的类。

晶洞转换计划输出配置、放置和两个独立标签共4份资源。保护集合精确为bedrock、spawner、chest、end_portal_frame；无效集合精确为bedrock、water、lava、ice、packed_ice、blue_ice，均使用 `replace:true`。来源生成算法固定读全局无效块标签，独立实现改读同成员的专用标签，配置字段同步指向该标签。保留来源1/24稀有度及Y=bottom+6至30的均匀高度和放置顺序，仅转换三个整数提供器的Codec包装并重定向地物ID。配置经类型/标签替换后与目标原生Codec数据一致；验收器逐项核对4份落盘资源、群系映射及Feature→WorldSetup→Mod事件总线生命周期。

来源身份仍保留在材料/洞穴标签中。尚未移植的19个方块仅以 `required:false` 成员登记（洞穴集合中的quicksand包含在这19项内）；已登记成员保持必需引用。目标1.21.1的TagEntry Codec与build方法支持该格式，缺失可选成员不会使整条标签失效；对应缺口由报告 `unported_block_dependencies` 逐项列出，不据此标记来源方块或自然供给完成。重跑转换时根据内容表和台账重新判定必需性。

### 来源树算法闭包

`legacy_trees.py`输出12份configured、10份placed及7份独立标签，共29资源；31组来源/目标源码指纹及17份独立实现指纹由验收器核对。竹林、杜鹃根系、丛林树和草甸树保持来源放置、树干/树冠/装饰器调用、随机消费与来源标签；`SourceRootSystemFeature`与树Codec在World注册生命周期中接线。完整转换、构建及资源加载回归已经执行，目标游戏实际放置、自然生成和两版本实机对照未执行。

## 1.18.2 状态与生成

范围包含 8 维度、68 群系、220 个 Feature 注册、161 结构文件；编辑器有 66 structure 元素和 35 feature 元素，二者与注册/文件计数不是同一口径。自然投放、固定场景、指令放置和副本刷出分别记录。

`B:WorldbeginProcedure:29–54` 的 worlddata 初始化有开关，但后面的材料/药草/服饰/结构/悬赏字符串赋值在开关之外；每次入口执行都会重写这些定义。`WorldcloseProcedure:26` 只复位 worldchufa。不能把所有这些字符串误判为允许永久在线编辑的世界真值。

## 目标所有权与接口提案

定义：维度/群系/生成配置与结构。实例：维度状态与已生成标志。城镇归 [town](../town/architecture.md)，副本归 [dungeon](../dungeon/architecture.md)。MapVariables 的主世界范围和 WorldVariables 的维度范围分别保存。

world 提供投放、维度与结构放置、结构管理操作；creature 负责生成实体的玩法初始化。`Weidubianhuan` 中按 `gamemoshi` 恢复游戏模式的分支归 dungeon。

按来源实际触发方式迁移生成。固定坐标目录、管理工具、传送器和场景清理不能被自然生成配置替代。目标具体 Codec/放置 API 在目标构建源码就绪后锁定，不能凭其他版本签名编写。

## 验证与裁定

保存/加载、多人进入、死亡/离场、跨维度返回和场景卸载分别检查。主世界与维度数据不能用英文类名推断。是否承诺 1.18.2 世界存档转换仍待负责人决定；保留资源 ID 本身不提供完整存档升级能力。

## 实现前须定（B1 实测）

结构 `difujiegou` 展开后 304 MB、8,442,357 个方块，其中约 92% 是同一个状态（调色板索引 41，应为空气），来源在 `DimensiondilingDangWanJiaJinRuWeiDuShiProcedure` 里进入地府维度时 `getOrCreate` 加载并摆放。1.21.1 开发客户端默认堆下加载它在解析 NBT 时 `OutOfMemoryError`（2026-11-02 实测，15.9 秒后失败，与数据版本升级无关）。实现地府维度时从三个办法中选：压缩掉空气条目并在摆放前清空区域（改变模板内容，需对照来源摆放结果）、拆成多个较小模板分段摆放、把它保持原样并提高运行内存要求。裁定前文件原样放在 `data/yvanchuxiuzhen/structure/difujiegou.nbt`。
