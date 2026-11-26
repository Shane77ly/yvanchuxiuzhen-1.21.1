---
topic: world
role: architecture
status: draft
updated: 2026-11-26
---

# 世界：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 全群系矿物与霓裳草入口（2026-11-20）

1.18.2版本 `YvanchuxiuzhenModFeatures.addFeaturesToBiomes` 对群系列表为 `null` 的地物向每个群系按注册顺序添加入口，各 Feature 的 `place` 再按各自维度筛选。来源38项包括地下矿物阶段31项、植物阶段霓裳草1项及其他阶段随机结构6项；随机结构保持独立候选，本批不接收。目标通过 `AddMissingBiomeFeatures` 在对应阶段追加缺失的31矿和霓裳草，已含该 placed feature 的群系保留既有位置，不重复追加；原有维度门槛、地物投放参数和基底不改。允许维度内不一定都有可替换基底。

静音客户端检查所有124个已加载群系对应阶段的32项各出现一次，当前10维度群系组合调用原生 `FeatureSorter` 均无环；结果只覆盖该运行环境，额外数据包群系、实际新区块产物、生成分布、存档重进及两版本实机未执行。来源与原始报告见[世界月志](logs/2026-11.md#2026-11-20-全群系矿物与霓裳草入口)。

## 修罗道维度接线（2026-11-18）

修罗道保持1.18.2 版本的唯一群系 `minecraft:nether_wastes`，不新增自定义群系或出生列表；种子、地形路由、表面规则和维度类型随 `dimensionxiuluo` 独立保存。转换器将来源无命名空间的原版群系补为 `minecraft:`。已有修罗点火工具、门户和POI继续走通用World入口，不改放置、冷却或出口策略。[本批月志](logs/2026-11.md#2026-11-18-修罗道独立接线)记录首次及真实重进后再次碰撞往返、出口复用和保存；自然地形取样与出生未执行。

## 无归十群系与门户接收（2026-11-18）

无归最终密度中的高度滑移现在由 `legacy_noise_slide` 读取单份输入密度图。Y 除以单元高度时向零截断；先算顶部再算底部，端点沿用目标值或原密度，中间保留 `target + weight * (value - target)`。仅无归改变内部表示，其余维度仍按各自转换规则生成。对照与内存证据见[世界月志](logs/2026-11.md#2026-11-26-无归滑移去重)。

本单元在`port/r06-wugui-20261118`、`worktrees/rebuild-wugui-20261118`自集成`fbe4c666`接收，功能`29f44f24`已由`6e86380d`非快进合入。`dimensionwugui`实际biome_source包含幽梦01–03、尸绝01–03、百川01–03和炼渊01共十身份，六专属与四共享全部保留。来源独立seed3381820403747779834，noise范围Y0–127、格点4×8，dimension_type范围Y0–255；默认魂石、water level0、sea_level0，没有默认热油。Overworld sloped_cheese路由复用五密度图，sampling xz1/y3、因子80/60、常量terrain_shaper0及bottom2.5/4/−1、top0.9375/3/0保持来源；1536个整数Y静态slide算例相等。24份正式转换JSON与冻结候选逐字节一致，转换清单只接纳本批26条来源追溯，既有输出语义保持。

十群系44个外部地物中，十矿、阴魂草、尸草、铁链、腐竹、金色板块和魔眼结构共16准入；21其他矿、nichanggrass_06与六随机结构共28拒绝。十个准入矿191–199ore及fuluzhushablock仍只替换`minecraft:stone`，不能由维度准入推导魂石层有自然矿物供给。helper的CarversAndLakes及false默认矿分支16项保持，没有ore_diamond_medium。十群系修者0101事件均为CREATURE/10/1/1，出生仍要求脚下Material.GRASS且亮度>8；魂石、幽薄土、腐土不在该来源材料集合内。上述自然出生与全域供给尚未验收。

`NativeWorldFeatures`仅增加同名金色板块Delta注册，保持BLACKSTONE/RED_SAND、size16/rim2及Heightmap MOTION_BLOCKING→count25→InSquare→Y0..top→BiomeFilter。两版Delta place/isClear算法经局部变量及Random接口归一后相等。`WorldTemplateFeatures`新增魔眼结构来源参数：chance70000/1000000、count nextInt1+1、OCEAN_FLOOR_WG地下Y8..max(height,9)、腐土基底、Y+1、Mirror前2/Rotation前3、包含实体、flags2及STRUCTURE_BLOCK处理。现有193字节模板压缩指纹不同，解压原字节相等，1×6×1为五黑曜石与顶端向西魔眼；既有Plant破坏事件/Creature生成消费者复用，未另造行为。两内容台账仍归Plant，生成由World既有机制承载。

铁链placed为Y0–120 uniform→count2–6→InSquare→BiomeFilter，列高60；腐竹为Y0–120 uniform→count6–16→InSquare→向下最多10格仅穿空气扫描→BiomeFilter，脚下必须腐土，列高2–5。百川01内联双蘑菇保持count1→rarity32→InSquare→MOTION_BLOCKING→BiomeFilter，原版配置为patch96/7/3及AIR谓词；既有五土壤蘑菇RETURN注入继续保留亮度<13或MUSHROOM_GROW_BLOCK条件。本批未另改光照、土壤或旧plantsoils场景。两草规则只见[Plant正文](../plant/architecture.md#无归两草生成消费者2026-11-18)。

`WuguiWorldgenCheck`验证实际十群系、原点一块FULL区块的魂石、28地物真实维度拒绝、金色板块同名类型/主世界拒绝、铁链/腐竹列、受控腐土魔眼模板及两草正式configured。魔眼使用来源seed70和受控OCEAN_FLOOR_WG上界251，验证拒绝/允许、五黑曜石及180度变换后的朝东魔眼；高度图保存后恢复。两草、土壤及模板结果在真实退出重进后逐格核对，再恢复临时环境与玩家状态。金色板块在无归允许落地、完整自然地物/植物分布与出生仍未验。

首次`wugui,dimensionportal`组合在进入既有人间道加载随机结构suijijiegou01/03/04的DataFixer过程中发生768 MB OOM；线程栈显示门户建门等待区块，客户端进入emergencySave，IO线程随后堆溢出。该轮留有一次九维度保存与截图，但无完整报告、最终保存或正常退出，不能算整项通过。仅新增Smoke的`wuguiportal`限定入口，复用原夹具、六工具及六主世界门户检查，只进入无归；首次及真实重进后的两次完整往返、目标门户复用/POI保存、工具耐久均通过。生产规则和内存上限未变，人间道多维组合OOM及旧停服重排问题仍开放。

本单元通过后的实际资源为六个自定义维度、60个目标群系（58来源及虚空/画卷），游戏含原版三维度共九维度；修罗/地灵及余八来源群系尚未接收。十群系与两Feature仅回填已读/已注册，未提升为全项验收。两草及无归门户截图已查看；自然样点截图仍为加载地形画面，不作自然地形视觉证据。完整自然分布/供给、全部表现、多人/专服、1.18.2版本实机和两版本对照未执行。两轮原始日志、编译、转换、保存和图像证据只在[本批月志](logs/2026-11.md#2026-11-18-无归十群系接收与限定门户客户端)登记。

## 天阙七群系与门户接收（2026-11-18）

本单元在`port/r06-tianqve-20261118`、`worktrees/rebuild-tianqve-20261118`自集成基线`53a8074d`接收，功能`9e9c4caf`已由`292d3a13`非快进合入。实际`dimensiontianqve` biome_source只包含`biometianqve_01`–`07`，来源独立seed为196079949153306047，noise范围Y0–127、格点8×4，dimension_type范围Y0–255；默认方块和全部地表保持`cloudblock`、sea_level0。转换器仅对实际引用`minecraft:end/sloped_cheese`的来源路由增加本维度第六份密度图`end_sloped_cheese`，保留end_islands、来源BlendedNoise、slide和blend_density/interpolated/0.64/squeeze顺序；人间道、妖魂道和已先行重写的画卷输出不受此分支影响。18份正式生成JSON与冻结候选逐字节相同，无关输出已恢复，证据见下方月志。

七群系各38个外部地物为31矿、六随机结构和`nichanggrass_06`。来源及目标维度集合逐项相等，全部拒绝天阙。04的唯一内联橡树复用`SourceTreeFeature`的来源直干/Blob树冠、4/2/0、2/0/3、次数10及投放顺序。来源云块为Material.STONE，不满足来源树苗落点；五种来源土壤仍可承接受控树。该来源条件不提供自然云层橡树供给，也不通过补造矿、结构或植物改变来源。

来源01出生表保留财神爷3/1/1、`moshou_002`的MONSTER 20/1/1及修者0101的10/1/1；02–05和07含财神爷及修者，06仅修者。财神爷条件Calendar.MONTH==2实际为公历三月；修者脚下Material.GRASS且raw brightness>8，云块不满足。`moshou_002`的非和平/原生黑暗/Mob基础出生规则沿Creature实现，完整族已由`19c63649`集成，01出生引用无需删除。以上是静态规则核对，自然出生率尚未运行验收。

`TianqveWorldgenCheck`核验七群系实际biome_source、原点一块FULL区块的自然云块、38地物直接调用拒绝、云层和五土壤的树苗落点，以及受控水体中来源树干替水、草转泥土和叶片非含水。受控972格区域结束后恢复。自然截图可见云块地形轮廓但较暗，不能据此验收全部表面纹理或地形分布。门户夹具按目标维度实际存在性选择往返或缺失拒绝：天阙首次碰撞入场、出口建门和回程通过；真实保存重进后，六组主世界门户、已到达的人间/妖魂/天阙目标门户与POI及六工具耐久均保留。重进后的再次往返只覆盖人间道和妖魂道；天阙只核目标门户与POI保存。

天阙单元接收时有五个自定义维度、50个目标群系资源，含48个来源群系与虚空/画卷两群系；游戏运行包括原版三维度，共八维度。七台账仅补记已读/已注册，两个既有天阙门户内容行仅更新有限运行说明。完整自然分布、出生与供给、天阙重进后的再次完整往返、全部表现、多人/专服、1.18.2版本实机及两版本对照仍未验；公共高视距OOM和停服重排未据本次成功关闭。转换、运行与截图原件只在[本批月志](logs/2026-11.md#2026-11-18-天阙七群系接收与必要客户端)登记。

## 群系目录与传送门名称（2026-11-18，历史核对基线）

以下为天阙接收前的历史核对，当前接收状态见上方天阙正文。目录核对使用集成树`worktrees/rebuild-batch0-integration-20261117`，只读证据基线`cf44958cf3595c0d71c70e4ef08f55d6628b38b5`。实际维度为`dimensionrenjian`、`dimensionyaohun`、`fabao_009weidu`与`xvkong`；43份目标群系包含人间道/妖魂道来源计划的41份、技能虚空使用的`xvkongqvnxi`及画里乾坤使用的`tiandaochunbaikongjian`。41是本批已生成的来源群系口径，43是该历史基线目标资源口径；不能写成四道候选已经进入集成。

本次只读复核357份来源指纹，41份群系在模块树及活动集成树中均与既有严格计划逐字段相同，覆盖2973条生成操作与483条出生记录。55个原版根的既有38项复用、14项自定义、3项雕刻及0项待核结论未失效；41行归属改为已读、内容状态补记已注册，来源证据由旧内联片段范围更新为完整气候/效果、直接与helper生成、直接出生和外部事件调用。状态校正不建立新的运行结论，跨独立事件的原Forge注册先后仍未证明。

模块树候选25份群系从已读来源独立展开后与候选JSON逐字段一致，共1280条生成操作、32条出生记录；该历史基线没有这25份。候选四道资源及关联资源的精确115文件清单保存在`worktrees/rebuild-birth-reward-expectation-20261117/build/diagnostics/r06-biome-catalog-20261118/audit.json`：4份dimension、4份dimension_type、4份noise_settings、21份密度函数、25份群系、26份configured feature、31份placed feature，均尚未存在于该历史核对基线。该清单记录候选存在与来源引用，不代表接收、全量转换、注册加载或自然生成。候选内容保持候选/待处理；来源阅读事实不使用内容状态“已读”。

41个群系名从`world_runtime.json`移入由已注册台账生成的`world.json`，避免`convert_content.py`的`convert_registry_lang`重复输出同键。候选25名仅保存在候选资源接收包，未写入当前runtime片段或实际合并语言。主协调者按768 MB、单worker执行`mergeAssetFragments`成功，实际中英文合并输出均含41个来源群系名及六个门户名，47键逐值一致且无重复；本批没有游戏画面验收。

六个门户方块的目标描述键由`DimensionPortalSpec.portalId()`得到`block.yvanchuxiuzhen.dimension<名称>_portal`；来源仅提供同维度点火物品的显示名，没有门户方块名键。中文直接沿用人间道、妖魂道、天阙道、修罗道、无归道、地灵道；英文从来源`Dimension… Portal Igniter`去掉物品动作词` Igniter`作为门户方块名。键保留在runtime片段，不改变传送条件或四道资源接收状态。357份来源指纹与41份目标资源在接收树复核一致；语言实际重建与逐值检查原件归档于上述诊断目录的`verification.json`及`merge-assets.log`。本批未执行转换、Java编译、客户端、专服、存档重进、1.18.2 版本实机或两版本对照。

## 公共停服回调Future身份关联（2026-11-18）

NeoForge21.1.265精确`ChunkMap.java:532–562`在`chunkHolder.getSaveSyncFuture()`上调用双参数`thenRunAsync(Runnable, Executor)`，Executor为`this.unloadQueue::add`；未就绪的原生Runnable再次调用`this.scheduleUnload(chunkPos, chunkHolder)`。因此，旧诊断在Runnable入口重新调用`holder.getSaveSyncFuture()`取得的是回调时的当前Future，不能据其identity、完成状态或`pendingSame=false`反推注册该Runnable的parent Future。已有`generationRefCount`非零、当前Future完成、当前task为空等样本仍只表示被观察字段的状态，不能单独证明真实引用新增路径或生产修复条件。

新增的Smoke专用`ChunkMapUnloadFutureDiagnosticsMixin`只拦截`ChunkMap.scheduleUnload`中这一处双参数调用，`require/expect/allow`均为1，接收实际receiver Future、原Runnable、原Executor及原方法的坐标/holder参数。`WorldStopFutureDiagnostics`未arm时直接返回原Runnable；arm后，每个闭包捕获本次receiver Future及唯一registration序号，在原Runnable执行前比较捕获的parent与holder当前Future的引用`==`，不按holder映射推测关联。原Executor直接传回原`thenRunAsync`，原Runnable只调用一次且其异常仍进入原来的完成链，没有捕获或替换该动作的异常，没有增加queue drain、清引用、改票据或跳过保存。

日志保留注册时parent的完成/异常样本、回调时parent/current状态与是否同一、当前pending/updating成员、生成引用和当前task。前32次及随后二次幂采样限制日志量，计数在解除挂载时汇总；同一回调的字段读取仍非原子全局快照，identityHash只便于日志定位，Future是否同一以`==`字段为准。新Mixin配置和实现仅放`src/smoke`，不进入发布JAR；公共`WorldStopDiagnostics`的arm/disarm接线与Smoke元数据以集成`28a4cfb2`为冻结基线，协调者在`e7a06ff1`定向接收并由`cf44958c`合入。既有生产只读观察入口保留；本树旧`cancelStaleUnload`候选不进入本包。

实际Mixin应用已随灵兽袋两轮必要运行确认：首轮为正常回调，修订轮在普通运行早期取得短时密集重排样本，随后自行结束且两次全维度保存成功。`SmokeRun.stopDiagnosticsArmed`重进后未复位，两轮诊断各仅覆盖首个世界生命周期，不将重进后的保存冒充第二份诊断样本。原始统计和采样边界见[修订轮月志](logs/2026-11.md#2026-11-18-future关联修订轮与短时密集重排)。

精确NeoForge21.1.265 `ChunkMap.processUnloads:492–520`只在生成引用归零后将holder从updating移至pending并注册卸载；`updateChunkScheduling:414–421`可以把同一个holder从pending取回updating，旧回调仍然存活。`scheduleUnload:532–562`先判断`isReadyForSaving`，未就绪时先重注册，仅在就绪分支才检查pending身份。因此同一已完成的saveSync、返回updating的holder及新生成引用，足以解释本次短时重排；已完成Future上的`thenRunAsync`由原unloadQueue Executor排队，不是同步Java递归。

`ChunkGenerationTask.create:33–36`对半径缓存的全部holder调用`acquireGeneration`，`releaseClaim`再释放整份缓存；holder.task只记录以该holder为中心的当前任务。邻区中心任务或已被替换但尚待收尾的任务仍可能持有引用，故`currentTask=none`与`ref>0`不矛盾。逐字段日志不是原子快照，限流样本也不能重建全部回调、引用持有者或具体票据调用者。本次自行收敛不闭合此前停服不收敛与高视距OOM，亦不支持生产取消或跳过保存。只读源码摘录及四份精确源码指纹已归档在集成树`build/diagnostics/unload-future-20261118/run-02-analysis/`。

## 首次登录传送阵坐标与公开查询（2026-11-17）

1.18.2 版本 `PlayerJoinGameProcedure.onPlayerLoggedIn` 在玩家位于 Overworld 且 `WorldVariables.chuansongzhenshengcheng` 为 false 时执行初始化。X、Z 分别由新建 `java.util.Random` 经 `Mth.nextInt` 在闭区间 `[-300,300]` 取整数，Y 固定为20；过程依次清除以中心为基准偏移`(-10,-2,-10)`的21×8×21方块区域，以无旋转、无镜像、包含实体、flags3放置 `yvanchuxiuzhen:chuansongdazhen`，再将完成标记写为true。坐标由 Overworld `MapVariables`保存，完成标记由 `WorldVariables`保存；调用链和字段名由来源指纹条目限定。

1.21.1 版本由 `WorldGeneration` 在服务端玩家登录事件中保留相同维度条件、坐标范围、清理顺序、模板原点和完成时序。坐标与完成标记集中保存在 Overworld `SavedData`，避免维度副本；`WorldStructures.teleportArrayCenter(ServerLevel)`仅在完成标记成立时返回保存的中心`Vec3`，此前返回`Optional.empty()`。该查询是只读公开操作，不将未初始化坐标解释为原点。按 NeoForge 21.1.265 本地 sources jar，`Mth.nextInt` 的随机源参数为 `RandomSource`；目标代码以`RandomSource.create()`分别生成X和Z，保留来源每轴独立新建随机源及闭区间`[-300,300]`的取样方式。

当前传送阵状态由私有不可变`TeleportArray(generated,x,y,z)`承担，使用`RecordCodecBuilder`定义完成标记及三轴来源键，缺失字段保留来源false/0初值。来源坐标字段虽然声明double，但全部实际写入仅为初值0、两轴整数取样和固定Y20；目标以int保存，经`Vec3`提供相同数值。现有`Settings.CODEC`只占用`jiegoushengcheng`，与四个`chuansongzhen*`键互不重叠，分别从同一CompoundTag解析并编码合并。已核NeoForge21.1.265：`SavedData.save(File,Provider)`每次传入新CompoundTag，`CompoundTag.merge`按键复制，因此省略默认字段不会残留旧坐标或覆盖结构开关。初始化、查询及存盘均沿用服务端生命周期；公开查询只在服务端主线程使用，本路径没有客户端状态或网络同步消费者。

此前手写NBT版本同步至集成树后由`gradlew --no-daemon --max-workers=1 compileJava`编译通过，使用768 MB Gradle堆；当前Codec版本另在独立功能树以768 MB、单worker通过`compileJava`；两次编译分别登记，均不覆盖保存重进或首登行为。精确目标源码另确认Overworld的`LevelEvent.Load`在初始化出生点和`prepareLevels`之前触发，索引先于玩家登录发布；`PlayerList.placeNewPlayer`经`EventHooks.firePlayerLoggedIn`同步发布登录事件。标记仍在模板放置调用返回后写true，不读取原生放置布尔返回值，保留来源时序。

来源跨系统消费者归xiulian：`PlayerJoin02Procedure`为无良商贾出身玩家生成坐标命名纸张，为流浪侠客生成`ling_11`并复制目标坐标NBT；`Ling11DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`实际重新读取全局中心坐标，未消费物品内复制值。当前集成实现通过同一公开查询接入纸张坐标和Ling11三轴NBT；空查询使用`Vec3.ZERO`以保留来源全零全局字段，Ling11仍在坐标和为零时按来源生成空盘。奖励消费者通过出身界关闭后的延迟事件执行，不与通常同步Overworld首次登录生成共用回调时序。2026-11-18 `ling11` Smoke以非零中心完成首次右键、双端状态与粒子画面检查并正常保存退出；完整首次登录时序、实际奖励获取、存档重进和多人登录仍未验。

## 天道维度方块与门户来源闭包（2026-11-15）

来源`TiandaodimensionblockBlock`是六个自定义维度门户共用的框架方块：材质`WOOD`、地图颜色`GOLD`、声音`WOOD`，硬度与爆炸抗性为`strength(-1,3600000)`，光照等级15，`getLightBlock`固定返回15，掉落列表为空时显式返回`minecraft:air`。目标内容表已登记`yvanchuxiuzhen:tiandaodimensionblock`及对应方块物品，静态属性、灯光和空气掉落表达已接入；来源负硬度已纳入转换审计，目标内容行当前没有重复记录门户运行时。

来源闭包包含六组同构门户实现：`Dimensiondiling`、`Dimensionrenjian`、`Dimensiontianqve`、`Dimensionwugui`、`Dimensionyaohun`、`Dimensionxiuluo`，每组各有`PortalShape`和`Teleporter`。形状类用天道维度方块判定框架，沿水平轴寻找底角，宽度2–21、高度3–21，扫描并生成`NetherPortalBlock.AXIS`内框，创建门户块后写入对应传送器的POI。传送器为每组独立的`ITeleporter`，登记独立POI和300刻传送票，入场查找半径16、出口查找半径128，按最近距离和Y排序；找不到出口时以16格螺旋范围寻找可承载位置，必要时按来源高度夹取并用天道维度方块重建框架，再写2×3门户内框，最后按入口矩形、维度缩放、实体尺寸和速度生成`PortalInfo`。

目标已用`DimensionPortalBlock`/`DimensionPortalItem`承载六个门户方块与耐久工具，`DimensionPortalShape`保留来源框架判定、2×3至21×21形状、`AXIS`内框和完整性拆除规则；`DimensionPortalRuntime`按门户身份分别登记POI与300刻票，保留16/128格查找、最近距离/Y排序、16格螺旋建门、天道维度方块框架、坐标倍率、实体尺寸和速度偏移。NeoForge 1.21.1 的`Portal`/`DimensionTransition`承担来源`ITeleporter`的跨维度生命周期，六组注册均共用同一实现而保留独立方块、POI、票据和目标维度身份。目标找不到对应维度时返回空，暂不补造默认维度。

Q207 的运行接线已实现，门户模型路径已统一为目标`block/`目录，且已补齐来源`ling0201`–`ling0601`方块贴图。以下为2026-11-15四道资源缺失基线的历史证据；当前天阙及无归已接收，分别见[天阙正文](#天阙七群系与门户接收2026-11-18)和[无归正文](#无归十群系与门户接收2026-11-18)，修罗/地灵资源仍待接收。`runSmokeClient -PsmokeOnly=dimensionportal` 已在768 MB堆、单worker下完成：六组工具均能在生存主手点门并消耗一点耐久，六组门户均登记六个独立POI；人间道、妖魂道完成真实碰撞入场、回程、目标门户复用与保存重进后的再次往返；天阙道、修罗道、无归道、地灵道在目标维度缺失时返回空转换，真实碰撞保持主世界且不创建默认维度；保存重进后六组主世界门户、两组目标门户和POI均可再次查找。客户端退出码0、报告`DONE`、`ERROR/FATAL=0`，未覆盖多人、完整自然等待及四道目标维度资源。十三份来源类指纹、六组目标内容和实现位置见`ledger/source-evidence.csv`、`ledger/source-fingerprints.csv`；运行证据见`build/diagnostics/r06-dimensionportal-20261115-final/`。

## Q130 结构Mod实体旧属性没有升级

2026-11-14居民必要单项确认目标缺陷：花繁城03模板仍保存1.18.2版本`Attributes/Name/Base/Modifiers`，精确NeoForge21.1.265的`LivingEntity.readAdditionalSaveData`只读取`attributes`，`AttributeMap.load`读取`id`，`AttributeInstance.load`读取`base/modifiers`。模板经过原生结构DataFixer后，自定义Mod实体的这些字段仍未升级，导致小孩哥最大生命与攻击从模板100/2回退到注册默认20/1，保存重进后仍为20。

静态扫描161个来源结构，其中26个含实体、22个含本次436个Mod实体；共1261属性条和283条随机跟随范围修饰，17名居民含134条属性。该数量是转换影响范围，不是已验证数量。修复由结构转换器承担，按精确来源及目标属性语义改写Mod实体和乘客的属性字段、修饰与实际出现的属性ID；不在NPC运行类添加旧存档兜底。Forge附加步高与目标原生步高的基础语义需要分别核对，不只按字段名称直接替换。转换器已只在结构entities及递归乘客的两个来源Mod实体内升级属性字段、修饰与已核ID，22个含本族实体模板已定向再生成，其余139模板未重写。六居民模板中100血/2攻及保存重进已生效；其余16模板只完成转换，未实机放置。150条forge:step_height_addition均为base0且无修饰，来源精确版本注册语义未核，保持旧ID由目标跳过并保实体注册步高，不映射为原生绝对步高0；该项仍待集中核实。验证原件见所属月志，其他结构属性及旧NBT字段的完整审计仍开放。

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

技能入虚空时按裂隙坐标各轴除50并四舍五入，返回乘50，编号保存在 xiulian 体质数据；本批不改变传送代码。人间道与妖魂道已于2026-11-12正式生成，并各加载一个样点新区块，分别核对`biomeautumn_01`与`biomefeng_01`；人间道另确认石层并人工调用配置矿石地物。该小样不覆盖其余39群系、自然placed-feature、实体出生或完整地形分布，详见[世界月志](logs/2026-11.md)。其余四个自定义返回维度随 E 阶段接入。正式虚空的数据加载与保存证据见下方记录；完整视觉、自然裂隙往返、多人、1.18.2版本实机和两版本对照尚未执行。

### 本批验证

`build`（含ArchUnit）通过；补入来源群系名称后，专用服务器仅启动一次至Done，正式维度与群系加载成功。原生命令强制加载虚空区块(0,0)，在(0,64,0)同时确认 `xvkongqvnxi` 与空气；随后移除强加载，原生stop保存四个世界并正常退出，日志ERROR0，导出群系1个。没有新增测试类、专项选择器或重跑旧裂隙检查。证据为 `tmp/world-void-build.log`、`tmp/world-void-content.log`、`tmp/world-void-server.log` 与 `build/registry-export/biome.txt`。

本次确认数据加载、一个实际生成区块及保存；不作为自然裂隙往返、客户端视觉、多人或两版本实机对照的证据。台账仅标“已注册”。

## 画里乾坤维度（2026-11-14）

来源A的`world/dimension/Fabao009weiduDimension.java`、`world/biome/TiandaochunbaikongjianBiome.java`及`data/yvanchuxiuzhen/{dimension,dimension_type}/fabao_009weidu.json`共同定义本维度。纯白群系仅由该维度引用，归属由Tiandao候选校正为World；玩家进入、候场、固定结构与私人画卷状态由Dungeon拥有。

来源维度使用独立种子`-7568801392089884081`、单项multi_noise纯白群系，噪声范围Y=0–127，水平格宽8、垂直格高4，默认方块、流体和地表为`tiandaowhiteblock`。维度类型Y=0–255、逻辑高度256、环境亮度0.5，无天光、无顶、猪灵安全，床可用、重生锚不可用，无袭击、无固定时间；实际`effects`为`minecraft:overworld`，来源Java注册的同名无天空特效未被引用，目标保持实际绑定。

`convert_painting_dimension.py`读取来源并生成维度、类型、群系、噪声设置及offset/depth/sloped_cheese三份密度函数，接入`convert_data.py`并将来源SHA-1写入同一转换清单。复用`legacy_dimension`保留独立种子；`LegacyBlendedNoise`保留8×4采样、来源缩放2/1、因子80/160，内层目标噪声参数为0.25/0.25、smear4。来源和目标`end_islands`实现已逐行核对；专属末地密度为该岛屿函数加来源混合噪声，随后保留来源先上下slide、blend_density、interpolated、乘0.64、squeeze的顺序。offset原始样条为0，仍经来源`-0.50375F`偏移生成专属depth，不继承目标主世界地形样条。

纯白群系保持有降水、温度/降水量0.5，以及来源六项颜色。群系Builder没有地物、雕刻器和出生条目，但来源全局事件仍加入38个地物及CREATURE类别的`xiuzhe_0101`（权重10、最少/最多1）；转换保留外部注入及顺序。38个地物各自的维度门均排除画里乾坤，目标复用既有门禁。修者出生仍要求脚下草质基底、亮度大于8，不能凭出生列表认定纯白地面会刷修者。来源Codec不消费的旧name/structure表、island_noise_override及洞穴/深板岩开关不作为生效规则移植；岛屿来自路由显式函数。新增怪物光照字段与两道转换相同。全局CAVE/CANYON可替换白块由既有来源雕刻器标签转换承担。

本批新增7份数据，轻量对照确认既有人间道/妖魂道16份计划与当前输出一致。自然入场及运行结果统一见[副本月志](../dungeon/logs/2026-11.md)，完整地形数值采样、保存重进、多人、1.18.2版本实机与两版本对照未执行。

## 自然供给地物与随机结构（2026-11-11）

本节记录直接来源迁移的32种矿物、24种模板、33种普通配置型地物、41个来源群系内联生成片段与16个树装饰器。41个来源群系与正式人间道、妖魂道已于2026-11-12生成，运行与自然可达性仍按各自台账记录；生成机制接入不等于这些系统已经闭合。逐项证据与SHA256见 [来源证据](ledger/source-evidence.csv)和[来源指纹](ledger/source-fingerprints.csv)。证据中的owner严格沿用内容台账，矿物产物、植物树木与World自然投放职责分别记录；41个群系的整体转换与地形表达见下节，来源归属与台账状态分别登记。

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

已对照typed_field_accesses.csv及全部来源Java：附加条件过程是条件消费者，六个Feature调用；`YvanchuGoProcedure`是唯一实际写入过程，unlock写false，lock写true。`WorldCommands`由共享命令入口分派，保留精确语法 `/yvanchu <当前维度玩家显示名> unlock|lock jiegoushengcheng [true]`，permission1；Go是Java过程名，没有go字面子命令。

保留空白分词、缺参空串、当前维度显示名精确比较、首个匹配后结束及返回0。第四参数恰为true时向目标玩家发送原开启或关闭提示；找不到匹配时向执行者发送原红色错误，控制台由FakePlayer承接。此开关影响随后地物尝试，不触发重生成。来源完整MapVariables广播没有此字段的玩家侧消费者，目标不额外广播整份世界档案；Settings保留StreamCodec。本节只接结构unlock/lock两个管理分支。

### 温泉流体

`SpringFluids`独立登记温泉静止/流动流体、FluidType、液体块与水桶，保持原ID、UNCOMMON、密度1000、黏度1000、温度300、不可自增水源、爆炸抗性100及4/1/5流动参数。来源两种温泉流体均属于water标签，目标保留相应身份。桶堆叠1、归还空桶，加入来源材料页。

`SpringLiquidBlock`在受伤且无恢复时给予40tick恢复0级，并在该次施加分支内以10%概率给予600tick的017效果0级；已有恢复时不抽取该概率，碰撞过程不添加服务端守卫。来源客户端随机展示入口内部仍检查ServerLevel，再发送三个BUBBLE_POP；目标保持该守卫。客户端保留无世界颜色−13083194、世界群系平均水色、两种贴图与半透明渲染层。温泉湖的自然可达性由对应群系与维度装配闭合。

1.18.2版本与精确目标的恢复效果均以剩余duration模50判断0级治疗，并仅在duration大于0时应用。来源40tick时长依次经过40至1，不产生一次治疗；效果结束后再次接触只会重新施加同样时长。该批保持来源行为，受控断言检查效果存在及持续时间，不将血量增加作为预期结果，也不登记回血已验收。

温泉碰撞样本须启用实体正常移动流程。精确目标中 `Mob.isEffectiveAi`在NoAI时返回false，`LivingEntity.travel`据此跳过移动；方块 `entityInside`检查位于 `Entity.move`内。因此直接在池内召唤静止NoAI猪不能触发碰撞回调，其未获得效果不证明温泉实现失效。受控样本使用围墙约束启用AI的受伤猪，并分别读取Health、Pos及 `active_effects`；目标效果NBT使用小写 `active_effects`和字符串id。实测结果单独登记于本批月志。

### 本批验证范围

以上规则登记静态来源、精确目标源码与实现职责。运行结果由本批月志及交接登记；此前虚空或植物启动结果不作为新增地物、命令和温泉流体的运行验收证据。1.18.2版本实机与两版本对照尚未执行，完整World系统仍未完成。

## 修者0101群系自然名单（2026-11-18）

1.18.2 版本的 `Xiuzhe0101Entity.addLivingEntityToBiomes` 不判群系或维度，对每个群系的 `CREATURE` 名单追加 `xiuzhe_0101`，权重10、最小/最大组大小均为1。修罗使用与下界共享的 `minecraft:nether_wastes`，不建立独立群系或按维度叠加。1.21.1 版本的 World 在 NeoForge `ADD` 阶段只对缺项群系追加这一条：基线60个自定义群系的59条已内联，虚空 `xvkongqvnxi` 和原版群系由修饰器补齐；叠加独立地灵包后八个地灵群系均保留内联条目，68个自定义群系中67个不发生重复追加。

名单不替代[Creature的原生放置规则](../creature/architecture.md)：地面位置、草质标签和严格大于8的光照条件仍归Creature；不改群系资源或另设维度级名单。开发服务端启动完成后，原有注册导出额外写 `biome_spawns.tsv`，记录修饰后的全部命名空间、出生类别、实体、权重和组大小；不改变原有 `biome.txt`。`xiuzhespawn` 检查有效名单，再从主世界原版群系的受控平台调用原生自然抽签，核到站实体；这不证明正常调度频率、生成时出生、各维度实际生态或地灵完整运行。

## 人间道随机模板延后读取（2026-11-18）

六个人间道随机模板 Feature 在通过原生抽签、基底和共享开关检查后，才向当前服务端的结构管理器取模板。避免未中签的区块装饰都触发旧模板升级与读取；概率、随机数调用顺序、变换、处理器、写入标志和实体设置不变。非随机地物读取时机不变。[实机记录](logs/2026-11.md#2026-11-18-人间道随机结构自然样点与重进)覆盖01、03、04实际新区块、相邻区块探针、关档重开以及低内存下的有限生成窗口。

### Q232 人间道随机模板跨区块写入裁切

1.21.1 版本首轮 `renjianrandom` 的01样点在两种实际落点和朝向下触及距中心两块的区块，原生装饰记录远区块写入拒绝；`placeInWorld` 返回成功也不表示全部方块落地。原件存档对照发现两样点分别有165、293个该距离的非空气模板位置未保留预期方块。

锁定 1.18.2 版本的 `ChunkStatus.FEATURES` 以 `WorldGenRegion(..., 1)` 调用 `applyBiomeDecoration`；注册距离 `8` 不是写入半径。`Suijijiegou01Feature` 将原 `context.level()` 传给模板。`WorldGenRegion.ensureCanWrite` 按生成中心判断，X 或 Z 的区块距离大于 1 即拒绝写入；升级区块另有有效高度检查。1.21.1 版本的 `ChunkPyramid` 为 `FEATURES` 声明 `blockStateWriteRadius(1)`，目标横向检查相同。两版 `StructureTemplate` 逐格调用 `setBlock`，布置返回值不保证成功写入的总数。

两版 `suijijiegou01` 模板均为 `(26,16,24)`，排除空气和结构方块后 2042 个位置完全一致。将首轮日志的实际镜像与旋转代入锁定来源模板：`(2677,62,2987)`（`NONE/NONE`，中心 `[167,186]`）有 165 格超界；`(3887,62,2769)`（`NONE/CLOCKWISE_180`，中心 `[242,173]`）有 293 格。两集合与目标 `run-01/native-latest.log` 中的拒写坐标逐项一致。已观察的这两组属来源已有的裁切，不是目标新增裁切；保留模板、落点与生成调度。静态重算未验证 1.18.2 版本实机在同种子下的落点或半径内方块状态；局部探针读回不作完整结构验收。

## 原版群系依赖对齐（2026-11-11）

41个来源群系的55个原版生成根由 `verify_vanilla_biome_parity.py` 分别核对配置引用图、展开标签和锁定源码指纹。55个原版生成根静态核验全部闭合：38项可复用、14项来源自定义实现、3项洞穴配置，0项待核。2026-11-12严格计划为41个群系、2973条生成操作、483条出生记录，全部依赖blocker=0，原16个实体缺口已闭合。审计原文保存在`tmp/source-biomes-plan-next.json`；统一转换已正式写出群系与两道资源，运行自然生成尚未验收。

化石使用来源0.9/0.1腐化、保护方块和煤矿替换深层钻石矿处理器；16份原版骨架与覆盖模板除DataVersion外字节一致。熔岩湖通过 `SourceLakeFeature` 保留来源循环、随机调用和写入次序，solid/liquid谓词来自锁定的2078个来源方块材料快照；湖泊与化石保护标签使用来源成员。Cave/Canyon复用已核对的目标算法，替换标签为来源43项默认成员与维度Fixer追加成员的60项并集。

紫水晶晶洞使用 `SourceGeodeFeature`：1.18.2版本在分布点和裂缝密度累加时调用 `Mth.fastInvSqrt(double)`，1.21.1版本改为 `Mth.invSqrt(double)`，会跨越层阈值。独立实现保留来源位常量6910469410427058090及一次Newton迭代、随机调用、分布点/裂缝遍历、逐格写入和晶簇方向顺序。固定分布点、噪声置零的33³公式样本有10处层判定差异，说明不能直接替换；这不是实际种子生成对照。

两版本均按世界种子创建 `WorldgenRandom(new LegacyRandomSource(seed))`，以 `NormalNoise.create(random, -4, 1.0)` 创建两个Perlin噪声。已逐段核对 `forkPositional`、`octave_-4`字符串散列、LCG及位数、ImprovedNoise置换/插值、输入缩放0.0625与第二噪声缩放1.0181268882175227，相关调用链的有效运算与顺序一致；未运行两版本噪声自动对照。14个来源/目标类的原始源码指纹保存在 `tools/porting/data/legacy_geode_source_fingerprints.json`，验收器要求全部匹配，同时核对方向/坐标遍历、写入及晶簇谓词所依赖的类。

晶洞转换计划输出配置、放置和两个独立标签共4份资源。保护集合精确为bedrock、spawner、chest、end_portal_frame；无效集合精确为bedrock、water、lava、ice、packed_ice、blue_ice，均使用 `replace:true`。来源生成算法固定读全局无效块标签，独立实现改读同成员的专用标签，配置字段同步指向该标签。保留来源1/24稀有度及Y=bottom+6至30的均匀高度和放置顺序，仅转换三个整数提供器的Codec包装并重定向地物ID。配置经类型/标签替换后与目标原生Codec数据一致；验收器逐项核对4份落盘资源、群系映射及Feature→WorldSetup→Mod事件总线生命周期。

来源身份仍保留在材料/洞穴标签中。尚未移植的19个方块仅以 `required:false` 成员登记（洞穴集合中的quicksand包含在这19项内）；已登记成员保持必需引用。目标1.21.1的TagEntry Codec与build方法支持该格式，缺失可选成员不会使整条标签失效；对应缺口由报告 `unported_block_dependencies` 逐项列出，不据此标记来源方块或自然供给完成。重跑转换时根据内容表和台账重新判定必需性。

### 来源树算法闭包

`legacy_trees.py`输出12份configured、10份placed及7份独立标签，共29资源；31组来源/目标源码指纹及17份独立实现指纹由验收器核对。竹林、杜鹃根系、丛林树和草甸树保持来源放置、树干/树冠/装饰器调用、随机消费与来源标签；`SourceRootSystemFeature`与树Codec在World注册生命周期中接线。完整转换、构建及资源加载回归已经执行，目标游戏实际放置、自然生成和两版本实机对照未执行。

17份目标Java实现按仓库LF规范计算SHA256，仅把CRLF转换为LF，其余源码字节全部参与核验；31组来源/目标原版源码归档条目继续直接核对原始字节。目标实现的预期值统一锁定LF版本，换行以外的内容变化仍使门禁失败。换行基线修复及轻量验证边界见[World月志](logs/2026-11.md#2026-11-13-目标树实现指纹换行基线修复)。

## 人间道、妖魂道与41群系正式装配（2026-11-12）

`convert_data.py`按先`convert_source_biomes.generate`、后`convert_terrain_dimensions.generate`的顺序生成资源。群系转换展开来源直接调用、原版helper与外加事件，保留生成阶段、事件内顺序与重复条目；41个群系共2973条生成操作、483条出生记录（215直接、268事件追加）。严格计划确认全部依赖blocker=0后，已写出41份群系及两道共16份地形资源：每道dimension、dimension_type、noise_settings各1份，密度函数5份。连同既有虚空，主资源集现有42个群系；整体World仍为部分实现。

两道采用`legacy_dimension`生成器，保存来源独立地形种子：人间道−3958683526324992456、妖魂道6901069156277239836。`ChunkMapMixin`在结构状态创建前以该种子构建`RandomState`；随机散布结构继续使用世界种子，同心环结构种子由生成器设置为来源地形种子。`legacy_terrain_spline`保持来源float坐标、山脊折叠及OFFSET常量，`legacy_blended_noise`保持来源格采样和乘除次序；`minecraft:slide`展开为保留Java截断语义的Y区间表达。生成范围Y=−64至319，海平面63；地表规则及来源默认方块、流体和开关直接复制。维度类型保留来源`minecraft:overworld`特效绑定，不切换到来源Java中未被类型引用的同Mod特效；新怪物光照字段按来源0–7规则补齐。

来源JSON中的旧`structures`字段不是运行分布依据：锁定1.18.2版本`NoiseGeneratorSettings.DIRECT_CODEC`已不读取该字段，`NoiseBasedChunkGenerator`使用全局StructureSet且没有结构覆盖。目标继续由结构集与适用群系决定，不将该死字段重新解释成新规则。人间道进入事件触发的固定场景不属于地形与自然群系装配，见[固定首入](#人间道固定首入2026-11-18)。

只读静态复核按精确NeoForge21.1.265源码确认：Mixin配置登记及`ChunkMap`构造器目标调用相符；自定义Codec字段与落盘JSON匹配；两道21/20群系的FeatureSorter有向依赖图分别252/225节点，均无环；12份密度/噪声配置直接引用34个注册资源全部存在；两道surface_rule与来源JSON深比较相同。来源不同实体BiomeLoadingEvent的注册/加载先后仍无确定证据，转换计划仅按来源路径稳定排序并保留各事件内部顺序，不据此声称跨事件随机选择顺序完全一致。

统一数据转换清单2765项，完整`build compileSmokeJava`含test/check、ArchUnit通过（`tmp/world-natural-resume-build.log`）。1.21.1版本运行证据包括两维度正式资源加载与保存，以及上述两个样点新区块的biome身份查询；人间道Y=16为石头，人工configured feature调用成功但伴随`Unprimed heightmap: OCEAN_FLOOR_WG`，不作为自然投放通过。41群系完整实际区块分布、自然placed-feature、出生、保存重载后续行为、完整客户端视觉、多人、1.18.2版本实机和两版本对照仍未验收。19个未实现材料方块继续以显式可选标签保留。

首次目标专服启动发现妖魂道地表规则的`minecraft:block`条件引用`yvanchuxiuzhen:quicksand`，但旧内容台账仍将该ID留在Decor待处理，因此注册表解码以Unknown registry key终止。回读来源发现它不是普通方块：`QuicksandBlock`继承`LiquidBlock`并连接独立source/flowing `QuicksandFluid`；流体使用来源quicksand01/02贴图、密度1650、爆炸抗性100、每格液位下降2、寻坡距离2、默认tick间隔5，无桶、无碰撞或其他附加事件。来源 `minecraft:water` 流体标签含source与flowing两项；目标保留该标签语义、白色流体基色、砂色地图色、LiquidBlock液体属性和半透明渲染，不引入来源未定义的桶或地表替代规则。注册、资源与所有权已归World；待统一转换、构建及专服重验。

该故障同时暴露正式转换门禁只核对群系、实体和地物的遗漏。`convert_terrain_dimensions.require_biome_closure`现递归检查全部16份计划数据中的每个`Name`字段，覆盖noise_settings的default_block、default_fluid与任意深度surface_rule。Mod方块依据现有内容表及台账“行为已迁移”的手工注册行，原版方块依据精确NeoForge21.1.265资源归档中的blockstates，均不使用语言名称作注册依据；未知ID直接拒绝正式写出，并报告资源路径与JSON字段路径。针对性内存验证确认当前计划通过；移除可用集合中的quicksand后准确拒绝两处地表引用；分别向默认方块、默认流体及嵌套地表规则注入不存在的原版ID也均被拒绝。该检查验证转换前的身份闭包，不替代运行注册与Codec加载。

## 人间道固定首入（2026-11-18）

1.18.2 版本由 `DimensionrenjianDimension.onPlayerChangedDimensionEvent` 调用 `YijichushishengchengProcedure`；`qiankundaochang` 属于人间道自己的 `WorldVariables`，不是主世界共享 `MapVariables`。1.21.1 版本在玩家真正跨维后按来源顺序执行：于 `(-12,30,-29)` 放置乾坤道人居所，于 `(0,32,-2)` 以 `MOB_SUMMONED` 加入大管家，标记场景已生成，将触发玩家的强制重生点绑定到实际入门落点，再于 `(390,0,355)` 放置小尸王场景。两个模板均无旋转、无镜像、包含实体，使用完整方块更新。内置小尸王结构在构建前使用 1.21.1 版本的原生 STRUCTURE 数据升级器转换为 DataVersion 3955，运行时仍由原生结构管理器选择与加载。60 个服务端结束刻后放置榉木摆件和南向参悟台；延时任务不写入存档，保持来源的会话语义。

`RenjianGeneration` 唯一写入人间道 `yvanchuxiuzhen_renjian_generation` 存档。每次实际进入都发布 `RenjianEnteredEvent`；修炼系统独立消费其[个人入场规则](../xiulian/architecture.md#人间道个人入场2026-11-18)。该入口不把玩家传至固定居所，也不替代随机结构生成、自然出生或人间道真实死亡结算。

## 1.18.2 状态与生成

范围包含 8 维度、68 群系、220 个 Feature 注册、161 结构文件；编辑器有 66 structure 元素和 35 feature 元素，二者与注册/文件计数不是同一口径。自然投放、固定场景、指令放置和副本刷出分别记录。

`B:WorldbeginProcedure:29–54` 的 worlddata 初始化有开关，但后面的材料/药草/服饰/结构/悬赏字符串赋值在开关之外；每次入口执行都会重写这些定义。`WorldcloseProcedure:26` 只复位 worldchufa。不能把所有这些字符串误判为允许永久在线编辑的世界真值。

## 目标所有权与接口提案

定义：维度/群系/生成配置与结构。实例：维度状态与已生成标志。城镇归 [town](../town/architecture.md)，副本归 [dungeon](../dungeon/architecture.md)。MapVariables 的主世界范围和 WorldVariables 的维度范围分别保存。

world 提供投放、维度与结构放置、结构管理操作；creature 负责生成实体的玩法初始化。`Weidubianhuan` 中按 `gamemoshi` 恢复游戏模式的分支归 dungeon。

按来源实际触发方式迁移生成。固定坐标目录、管理工具、传送器和场景清理不能被自然生成配置替代。目标具体 Codec/放置 API 在目标构建源码就绪后锁定，不能凭其他版本签名编写。

## 验证与裁定

保存/加载、多人进入、死亡/离场、跨维度返回和场景卸载分别检查。主世界与维度数据不能用英文类名推断。是否承诺 1.18.2 世界存档转换仍待负责人决定；保留资源 ID 本身不提供完整存档升级能力。

## 固定结构解封命令（2026-11-14）

来源`A:YvanchuGoProcedure:536–537`调用`SealreleaseGoProcedure`，目标实际入口为`/yvanchu <当前维度玩家显示名> unlock sealrelease <结构名> [true]`，权限1；显示名匹配、命令源世界和固定坐标均保留。可选true只控制“解锁结构”中文反馈，不添加解锁持久记录、维度限制或传送。`extract_structure_releases.py`提取28个名称、42次直接放置和42个唯一模板，花繁城15份、其余各1份，保存来源与输出SHA256。无旋转、无镜像、包含实体、flags3复用`WorldStructures.place`。

2026-11-15 接入十职业管理分支时，把唯一 `/yvanchu` 注册和当前维度显示名目标循环移至 `flow.PlayerCommandFlow`，World 只公开 `api.WorldCommands.execute` 承接自己的结构分支。来源权限、贪婪字符串分词、首个匹配、空执行实体的未找到反馈以及结构参数位置保持；随机开关仍写同一存档，当时解封传命令源世界和位置调用 `StructureReleases.release`，没有提前写入职业字段。此次拆分静态核对通过，重构后的结构开关与解封运行回归未执行，历史运行证据保留其原基线范围；跨系统变化见[项目月志](../project/logs/2026-11.md#2026-11-15-共享原初管理命令归属收口)。

`StructureReleases`在服务端结束刻推进运行期延迟队列；花繁城及四处商店/营地60刻、冲关河村/云清派/佣兵营地100刻、镇妖塔40刻，随后发布`StructureReleasedEvent`。Quest持有伪装方块配置，NPC持有修士补充，Dungeon订阅镇妖塔写44份定义及楼板；World不访问这些系统的实现或持久对象。服务器结束清空本服队列，不添加来源不存在的中断恢复。命令→结构→后处理已接回；寻灵盘、光环和裂隙自然生产者已由本篇自然遗迹链接回；管理命令单项与自然入口的验证分别登记。

静态核对42模板均已有目标资源和转换记录；没有旧命名空间或精确旧`ForgeData`键，629处`NeoForgeData`、镇妖塔49处。14项Java字符串补充及NPC21任务另由提取器维护，不由结构转换自动生成。提取器曾跨布尔字段块误配花繁城商品坐标，集成前已限制代码块边界并逐字段独立复核；这属于目标工具缺陷，不归为来源疑点。自动化与必要客户端结果、未执行范围见[本批月志](logs/2026-11.md#2026-11-14-固定结构解封与镇妖塔后处理)。

## Q113 结构解封的全维度掉落物清理

`A:SealreleaseGoProcedure`末尾无条件注册十个服务端END刻后的权限4静默命令`kill @e[type=item]`，未知结构名称同样安排。此选择器没有坐标或距离条件，不能按命令源世界推断其范围。精确NeoForge21.1.265的`EntitySelectorParser:78,228–232,631–632`默认`worldLimited=false`，`type`只限定实体类型；`EntitySelector:153–158`据此遍历`server.getAllLevels()`。本机1.18.2映射源码对应路径相同，锁定Forge40.1.0相关补丁不改变原版选择器语义。

因此来源会删除所有维度内已加载、可查询的掉落物，不限结构附近；未加载实体不在选择结果中。1.21.1 版本在同一延迟刻遍历各维度，收集原版物品实体后逐一调用 `kill()`，不加载额外区块。前轮只读核对误记“当前维度”，本批在运行前已更正夹具和正文；现纳入[统一问题索引](../../reference/legacy/source-survey.md#具体问题与裁定位置)，后续集中裁定。

## Q115 佣兵营地的跨结构条件补充

来源`A:SealreleaseGoProcedure`佣兵营地分支在100刻后写`(-848,165,-217)`的商品表。该点不在佣兵模板内，实际对应云清派原点`(-882,141,-267)`加相对坐标`(34,24,50)`的任务伪装方块；其余佣兵本地点及冲关、云清、花繁城字段也已逐点对照结构NBT。

目标保留该远点、时序和“存在方块实体才写”的条件，故结果依赖云清派结构是否已生成；不将远点移动到佣兵营地，也不据此认定完全没有消费者。商品字符串作为Quest方块对话配置保存，玩家经济会话通过Economy公开操作写入。来源意图及不同解封顺序的实机结果未确认，纳入[统一问题索引](../../reference/legacy/source-survey.md#具体问题与裁定位置)后集中复核。

## 地府模板加载

内置模板为 227 × 69 × 539，完整覆盖每个坐标。离线导出时每格保留一个调色板编号，显式空气仍在原坐标；调色板、带 NBT 的方块和实体由目标原版结构数据修复器升级。运行时从随包压缩状态表和已升级的元数据恢复，不逐条解码原始方块列表。仅对字节摘要匹配的内置地府采用紧凑路径；存在管理器缓存、存档模板或 IDE 测试结构时，或数据包资源摘要不同时，沿用原版入口。地灵八个群系的地表规则保持基岩优先，先按共同的地表与深度条件过滤，再按互斥群系选择原材料。内置地府沿模板底层扫描 X 列时提前请求下一列原生区块，不改变逐格放置顺序和标记时机。内置地府生成期间，结构起点通知仍在原版服务端执行器处理；排队时保留区块坐标与起点表视图，不保留整个区块。来源专属生成器新建的区块，以及内置地府放置期间新建的区块，延后分配各自独立的空分段：访问群系或分段数组前完整展开，原生存档读盘不走此入口。生成标记属于地灵界，仅在放置调用返回后写入。存档覆盖与内置模板的验证范围分别见[本次记录](logs/2026-11.md#2026-11-26-地府首次放置与存档覆盖)。以下为原生整表的历史内存基线。

结构 `difujiegou` 展开后 304 MB、8,442,357 个方块，其中约 92% 是同一个状态（调色板索引 41，已核实为 minecraft:air），来源在 `DimensiondilingDangWanJiaJinRuWeiDuShiProcedure` 里进入地府维度时 `getOrCreate` 加载并摆放。1.21.1 开发客户端默认堆下加载它在解析 NBT 时 `OutOfMemoryError`（2026-11-02 实测，15.9 秒后失败，与数据版本升级无关）。实现地府维度时从三个办法中选：压缩掉空气条目并在摆放前清空区域（改变模板内容，需对照来源摆放结果）、拆成多个较小模板分段摆放、把它保持原样并提高运行内存要求。裁定前文件原样放在 `data/yvanchuxiuzhen/structure/difujiegou.nbt`。

## 寻灵盘与自然遗迹链（2026-11-14）

有效目录采用B `WorldbeginProcedure:38`每次维度Load覆盖的28项，保留顺序、水上孤舟X=28和各怪物组；声明初值23项不作为运行目录。`StructureExplorationData`在主世界保存`jiegoubaocunlist`，初值`wu%kong`，全服所有玩家/维度共用。World唯一写入搜索/已寻状态；公开`StructureSearch.search(level,position,playerUuid)`由Quest对话“裂隙解封”调用，不反写其他系统存档。

`/searchingforstructure <UUID> [team]`无权限或维度门槛，team无消费者；三维距离遍历未登记项，严格更近替换、平距先项，零哨兵见Q116。执行点±2范围内全部光环更新目标；匹配玩家UUID显示整距。距离≤16且该点尚非裂隙时放块、随机朝向，写结构/怪物组/玩家显示名，然后立即登记编号。`/renjiansealrelease`权限1，遍历未登记项，逐项尝试解封后无条件登记；控制台使用来源等价FakePlayer，命令门控失败仍会登记。

寻灵盘`ling_14item`只允许人间道：Alt优先Ctrl，Alt把当前三坐标写在实际物品实例并同维度回(0,32,0)；Ctrl按三坐标和非零返回记录，否则到(-70,63,-275)。普通使用生成Y+1光环，好友列表交Relation共享、团队清空，原生认主/出生，再从Y+1执行搜索并冷却100刻，不消耗盘。Alt/Ctrl不加该冷却。光环Monster、1×0.2、免火、tracking64、update3；每刻跟随当前维度主人Y+1，看目标，每20刻重搜，100刻移除，主人不在当前维度立即移除。最后移除当刻仍继续后续逻辑。

裂隙保存本体字段和零槽库存，六向及空方向能力保留；旁观原生临时27槽菜单不连接本体。每2游戏刻计划更新一次，40次只清kaishi，第41次即通常82游戏刻发保存显示名的`/yvanchu ... unlock sealrelease ...`，随后无条件删裂隙。`moblist`保存但实际Java不消费，不补造刷怪。World复用现有结构释放实现，Dungeon40刻后处理关系不变；几何/动画/纹理和Geo渲染来自来源，资源格式路径作目标适配。静态来源42文件SHA256与定位在`tmp/r06-world-exploration/source-manifest.json`；必要运行结果见[月志](logs/2026-11.md#2026-11-14-寻灵盘光环与自然裂隙)。

### Q116 零距离哨兵与全部解封后的空裂隙

B `SearchingforstructureGoProcedure:48,89–105,139`用distance=0表示尚未选择；真实零距离会在下一项重置候选。所有编号都已登记时仍保留默认坐标(0,0,0)/空名称及distance0，显示已全解封、删除附近光环后仍可能在原点放空裂隙并登记空编号。目标保持实际流程，未增加“无候选退出”；具体异常现场未实机验，后续集中裁定。

### Q117 搜索UUID解析失败后的裂隙半成品

同过程`:139–213`先放裂隙再查当前维度UUID，直接读取结果显示名；无效/离线/不同维度UUID可空引用中断，留下已放块而未写完/未登记状态。目标保持来源，未补默认玩家或吞异常；异常现场未实机复现，后续集中裁定。

### Q118 立即登记与延迟命令门控不回滚

同过程`:213`在搜索时登记，A `Setjiegou01Procedure:23–32`约82刻后用保存显示名执行管理命令。玩家离线、换维度或改名可能使在线显示名门控失败，但裂隙照样删除、已登记编号不回滚。批量入口同样在尝试解封后直接登记。裂隙与批量入口直接匹配当前维度首个显示名，不接受空名或多词名；结构字段的首词为名称，第二词为 `true` 时发送反馈。存档字段不能执行其他管理操作。目标保持时序和门控，不改用UUID绕过或补重试；完整异常现场待验、集中裁定。

<a id="q119-寻灵盘赠送只有旧编辑器文本"></a>

### Q119 寻灵盘来源任务与居所可达性

修正历史「仅旧编辑器文本」结论：原初修真生成 Java 只写准备状态，但配套原初系统的实际右键与任务解释器会发盘；1.21.1 版本已接回[原任务入口与存档语义](../quest/architecture.md#q119-初见乾坤道人)。这不是入维度赠礼，盘需要先接触居所方块才能获得。人间道首入样本的保存区块中，居所空间被完整方块封闭、不连目标门户。两版固定场景均未清理走廊或把门户落点绑到居所。该档已证实可用生存铁镐开路至原正厅，不证明其他地形或无挖掘通路；见[世界月志](logs/2026-11.md#2026-11-19-人间道旧门至正厅生存开路)。不增加首入发盘、快捷传送或通道。

## 人间道冬季冰湖（2026-11-20）

锁定1.18.2版本 `IcelakeshengchengFeature` 继承来源湖泊算法，并限制人间道冬季03群系；目标沿用已核对的 `SourceLakeFeature` 与来源材料标签，保留既有配置与维度门。自然新区块的形状、分布与保存重进尚未验证。

## 双源差异续查：六组天道门户

适用于六套同构的维度工具、门户方块与传送器（以下用人间道定位），目标原生符号均取自项目的 NeoForge 21.1.265 源码包。Q207 的既有往返与保存样本仍有效；以下为源码差异，不是本次实机结果。

### Q267 生存玩家短暂穿门不再立即传送

A `DimensionrenjianPortalBlock.entityInside:82–97` 在无传送冷却时直接换维度；目标 `DimensionPortalBlock` 继承原生 `NetherPortalBlock:107–125`。`PortalProcessor:21–27` 累加接触时间，`GameRules:136–138` 的生存默认门槛是 80 刻（约 4 秒）。复核：在人间道已有出口时，无冷却的生存玩家先走穿门面不停留，再回到门中站定。分别记录第一次穿过和最终传送；不是比较出门后的冷却。

### Q268 非玩家首入也会尝试建出口

A `DimensionrenjianTeleporter:197–204,229–244` 仅为服务端玩家创建缺失的门；找不到出口时用原坐标和零速度构造落点，普通实体仍能到目标维度。目标 `DimensionPortalRuntime.destination:153–171` 不区分实体类型，先尝试建门，失败返回空转换。复核：目标维度已加载且查找范围内无同类门，玩家不要先过，送一头牛进入；记录落点、是否生成门框及门户兴趣点。缺少整份维度资源不属于本项。

### Q269 载有乘客的载具不再被门户入口排除

A `DimensionrenjianPortalBlock:82–84` 同时排除乘客与载客实体。目标继承 `NetherPortalBlock:107–110`，原生 `Entity.canUsePortal:2781–2783` 在这里排除乘客但不排除载具；`Entity.changeDimension:2723–2731,2748–2750` 包含逐个转移及恢复乘坐的路径。复核：两侧都有门时，将载有动物的矿车推进入口，分别记录矿车、动物所在维度及乘坐关系。

### Q270 门边云粒子变为下界门粒子并增加环境音

A `DimensionrenjianPortalBlock.animateTick:61–79` 每次产生四个 `CLOUD`，不播环境音。目标 `DimensionPortalBlock` 没有覆写这个方法，原生 `NetherPortalBlock:237–267` 使用 `PORTAL`，且随机播放 `PORTAL_AMBIENT`。复核：开启方块音量和全部粒子，站门旁不触碰，区分环境音与跨维度瞬间的声音。

### Q271 副手点门工具破损反馈选槽不同

A `DimensionrenjianItem.useOn:33–38` 按本次交互的 `context.getHand()` 广播破损；目标 `DimensionPortalItem.useOn:30–41` 扣正确的交互物品，但用表示持续使用的 `player.getUsedItemHand()` 选反馈槽（原生 `LivingEntity:3133–3135,3590–3592`）。复核：生存玩家主手空，副手用最后 1 点耐久点门，不持续使用其他物品，比较收到的破损事件手位，而不是只看哪件工具消失。

### Q272 极限坐标附近寻找出口的中心偏移

A `DimensionrenjianTeleporter:209–217` 对缩放后的 X/Z 同时施加世界边界内缩 16 格与 ±29,999,872 的硬限制。目标 `DimensionPortalRuntime:160–165` 只有前一层。数值反例：缩放后 X 为 29,999,900、边界内缩后仍容得下它，查找中心分别取 29,999,872 与 29,999,900；实际会否选中不同出口取决于地形和已有门的位置。

### Q277 妖魂道温泉对无碰撞木块的地基判定不同

A `WenqvandiwuFeature:29,35–38,46–56` 调用 1.18.2 版本 `LakeFeature`；湖体下半外边界按方块材质是否为实体判定（`LakeFeature:67–72`）。1.21.1 版本 `NativeWorldFeatures:40,64` 为冰湖选用 `SourceLakeFeature`，温泉则用原生 `Feature.LAKE`，其下半边界检查 `BlockState.isSolid()`（21.1.265 源码 `LakeFeature:83–87`）。`biomelong_04` 仍有温泉入口，差异在一次投放能否通过地基检查，不是温泉没有注册。

复核：固定同一个湖体轮廓，其他条件均满足，仅将下半外边界的一块石头换成 `elm_05`。A `Elm05Block:39` 使用实体木材质却无碰撞；1.21.1 版本 `content/plant/blocks.json` 与 `BlockTraits:92–93` 保留无碰撞，且该块在 `legacy_lake_solid` 标签内。它能通过来源的材质检查，却使目标原生湖泊因空碰撞形状判非实体并取消本次放置（`BlockBehaviour:535–549`）。静态反例；未测该组合自然出现的频率，未修、未实机。

### Q278 人间道采用外部群系时缺六种随机结构入口

A `YvanchuxiuzhenModFeatures:628–639,665–670` 将 `Suijijiegou01–04` 放在地表结构阶段、`05–06` 放在地下结构阶段。六个 Feature 的 `GENERATE_BIOMES=null` 不筛群系，实际放置时才筛人间道。1.21.1 版本 `WorldTemplateFeature:44–50` 保留维度门槛，但投放依赖群系文件内列出的地物，没有向全部群系补这六项的修饰器。68 份随包群系中，67 份列出六项，只有 `xvkongqvnxi` 未列；默认人间道实际选用的 21 个不同群系均已列在正确阶段。

复核：用数据包把人间道固定为原版平原，不额外向平原添加地物；在允许生成结构的世界检查平原两个阶段的投放列表，再区分模板自身的放置门槛。来源会安排这六项，目标不会；这不是 Q232 的跨区块裁切，也不表示默认人间道缺结构。本次仅核注册、数据和维度过滤，未修、未实机。
