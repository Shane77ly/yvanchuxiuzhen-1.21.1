# NeoForge 1.21.1

已核对范围：NeoForge 21.1.228 源码（sources jar 静态阅读），以及一个只含加载入口的开发环境实测（21.1.228 与 21.1.265 的构建和启动；除注明 21.1.265 的条目外，源码阅读未在 21.1.265 上重做）。其他 21.1.x 构建可能不同，使用前核对所用构建。源码事实只支撑架构判断，不替代编译和游戏验证。路径相对 NeoForge 源码根。

| 机制 | 源码 | 已确认事实 |
| --- | --- | --- |
| 延迟注册/命名空间 | `net/neoforged/neoforge/registries/DeferredRegister.java` | create/createItems/createBlocks 的 namespace 参数用于构造注册 ID；21.1.265 只检查非空，不要求与 Mod ID 一致 |
| 注册表别名 | `net/neoforged/neoforge/registries/IRegistryExtension.java`、`net/minecraft/core/MappedRegistry.java` | 21.1.265：`addAlias(from, to)` 使查找 `from`（且其不存在）时解析为 `to`，支持链式；`MappedRegistry` 按名与按键查找经过 `resolve`。区块、物品与玩家数据读取中的实际效果未实测 |
| 物品创建者 Mod | `net/neoforged/neoforge/common/CommonHooks.java` 的 `getDefaultCreatorModId` | 21.1.265：默认以物品注册名的命名空间作为创建者 Mod ID（原版附魔书按附魔命名空间）；物品可覆盖 `IItemExtension.getCreatorModId` |
| 玩家数据与死亡 | `net/neoforged/neoforge/attachment/AttachmentType.java` | 附件类型需注册；可序列化实体附件默认不在死亡时复制，可通过 copyOnDeath 选择复制；方块/区块变更需相应标脏 |
| 附件同步 | 同文件 `Builder.sync` 及 `attachment/IAttachmentHolder.java` 的 syncData | 支持自定义同步处理器、StreamCodec 与接收玩家过滤；完整 codec 方案每次写完整数据。21.1.228 与 21.1.265 的 sources jar 都有 `AttachmentSync`。21.1.265：实体的 `setData` 与首次 `getData` 创建默认值时自动同步；玩家本人的初次同步由 NeoForge 在登录（`PlayerList`）、重生（`PlayerList.respawn`，在 Clone 事件之后）与换维度（`ServerPlayer`）时发送；接收过滤写 `(holder, player) -> holder == player` 即只发本人。实测（21.1.265 开发客户端）：不可变值经 `setData` 写回后，客户端在提交、死亡重生、退出重进后都收到本人数据 |
| 原版属性基础值 | `net/minecraft/server/level/ServerPlayer.java` 的 `restoreFrom`，`net/minecraft/world/entity/LivingEntity.java` 的 `onAttributeUpdated` | 21.1.265：`restoreFrom` 在死亡与非死亡复制时都 `assignBaseValues`，`setBaseValue` 写入的基础值跨死亡保留，随后把生命设为新上限；玩家存档也保存基础值。最大生命降低时，当前生命在下一次属性刷新时压到新上限。实测同上 |
| 界面控件与实体预览 | `net/minecraft/client/gui/components/ImageButton.java`、`Button.java`，`client/gui/screens/inventory/InventoryScreen.java` | 21.1.265：`ImageButton` 只接受精灵图集 `WidgetSprites`，1.18.2 的“整张贴图 + 悬停纵向偏移”写法须自写 `Button` 子类覆写 `renderWidget`（`Button` 有 protected 构造器与 `DEFAULT_NARRATION`）；`GuiGraphics.blit(贴图, x, y, u, v, 宽, 高, 贴图宽, 贴图高)` 绘制整张贴图。1.18.2 的 `renderEntityInInventory(x, y, 缩放, 鼠标x, 鼠标y, 实体)` 以脚下为基准，1.21.1 的 `renderEntityInInventoryFollowsAngle` 以裁剪框中心为基准、纵向平移半个身高加 `yOffset`；框中心放在脚下并令 `yOffset = -身高 / 2 / 实体缩放` 即与来源一致（开发客户端截图核对） |
| 菜单与 payload | `world/inventory/MenuType.java`，`neoforge/network/registration/PayloadRegistrar.java`、`network/PacketDistributor.java` | 21.1.265：无额外数据的菜单可直接 `new MenuType<>(构造, FeatureFlags.VANILLA_SET)`，`ServerPlayer.openMenu(SimpleMenuProvider)` 打开，客户端在 `RegisterMenuScreensEvent` 登记界面；payload 在 Mod 总线 `RegisterPayloadHandlersEvent` 用 `registrar(版本).playToServer(Type, StreamCodec, 处理器)` 登记，客户端 `PacketDistributor.sendToServer` 发送，同一 tick 连发的多个 payload 按顺序处理（开发客户端实测） |
| 物品组件注册 | `registries/DeferredRegister.java` 的 registerComponentType | 有 DataComponentType 专用 builder 注册入口 |
| 实体复制事件 | `net/neoforged/neoforge/event/entity/player/PlayerEvent.java` 的 Clone | 提供原对象和是否死亡信息；复制不限于普通死亡 |
| 网络注册 | `net/neoforged/neoforge/network/event/RegisterPayloadHandlersEvent.java` | payload 注册发生于 Mod 总线；使用前需登记；单人内存连接也序列化 |
| 网络执行线程 | `net/neoforged/neoforge/network/registration/PayloadRegistrar.java` | 初始 handler 线程为 MAIN；支持 playToServer/playToClient 和显式线程选择 |
| 服务端重载 | `net/neoforged/neoforge/event/AddReloadListenerEvent.java` | 游戏事件总线提供重载监听器；此时内建和动态注册表已冻结；监听器得到 registry lookup |
| 伤害入口 | `event/entity/living/LivingIncomingDamageEvent.java` | 发生于无敌检查之后、减伤之前，可取消并读取 DamageContainer |
| 伤害应用与结果 | `event/entity/living/LivingDamageEvent.java` 的 Pre/Post | Pre 在护甲/药水减伤后、吸收前，可修改伤害；Post 在生命变化后提供结果 |

## 加载器与数据格式（已核实）

2026-11-02 依据 FML 4.0.42 字节码、ModDevGradle 生成的 `neoforge-21.1.228-sources.jar` 与原版资源制品核对：

| 项 | 事实 | 移植影响 |
| --- | --- | --- |
| 目标源码 | ModDevGradle 构建后在 `build/moddev/artifacts/` 生成 `neoforge-<版本>-sources.jar`，含 Minecraft（约 5,000 个 `net/minecraft` 源文件）与 NeoForge 源码 | 版本匹配的目标源码随构建获得；FML 不在其中，需从 Gradle 缓存的 `net.neoforged.fancymodloader:loader` 读取 |
| `@Mod` | 有 `dist` 属性；FML 4.0.45 的 `FMLJavaModLanguageProvider` 只选 `dist` 包含当前物理端的类，按覆盖端数降序构造（两端入口先于客户端入口）；`FMLModContainer` 要求每个类恰好一个 public 构造器，可注入 `IEventBus`、`ModContainer`、`FMLModContainer`、`Dist`。NeoForge 21.1.265 实测：专用服务器只构造公共入口，客户端先构造公共入口再构造客户端入口 | 可用独立的 `@Mod(value, dist = Dist.CLIENT)` 客户端入口隔离客户端装配，客户端装配可依赖公共入口已完成的登记 |
| `@EventBusSubscriber` | `bus` 属性已标记弃用 | 不再按来源 Forge 习惯显式指定总线 |
| 附魔 | `Enchantment` 为 `record`（描述、定义、互斥集合、效果组件），原版附魔位于 `data/minecraft/enchantment` | 来源中继承 `Enchantment` 的 Java 类改为数据定义；行为改由效果组件或读取附魔等级的玩法代码承担 |
| 群系与地物 | 位于 `data/<ns>/worldgen/` 数据 | 来源以 Java 构造的群系、已配置/已放置地物改为数据定义；地物类型本身仍代码注册 |
| 燃料与堆肥 | NeoForge `NeoForgeDataMaps.FURNACE_FUELS`、`COMPOSTABLES` 数据映射 | 来源的燃料、堆肥代码表改为 `data_maps` 数据 |
| 数据目录 | 单数：`advancement`、`recipe`、`structure`、`loot_table`、`tags/block`、`tags/item`、`tags/entity_type` 等 | 来源复数目录逐类改名 |
| 配方产物 | `"result": {"count": n, "id": "..."}` | 来源 `item` 字段改为 `id` |
| 熔炼类与切石配方 | 熔炼、高炉、烟熏、篝火的 `"result": {"id": "..."}`（`experience`、`cookingtime` 保留）；切石的 `"result": {"count": n, "id": "..."}`，不再有顶层 `count`；材料仍是 `{"item"}` 或 `{"tag"}` | 来源的字符串 `result` 与顶层 `count` 逐项改写 |
| 配方产物数量 | 产物数量大于该物品最大堆叠时解析报 `Item stack with stack size of N was larger than maximum: M`，配方不加载（2026-11-02 开发服务端实测） | 来源允许这类配方（1.18.2 产出超额堆叠），转换时须检出并裁定，不能静默改数量或堆叠 |
| 战利品表 | 条目引用不存在的物品使整张表报 `Unknown registry key` 并不加载；`match_tool` 的附魔谓词写成 `{"predicates": {"minecraft:enchantments": [{"enchantments": "<附魔>", "levels": {...}}]}}`；`{min, max}` 数量写明 `"type": "minecraft:uniform"`；实体表里的 `apply_bonus` 缺少 `minecraft:tool` 上下文，只给验证警告，表照常加载 | 转换工具改写后 109 张表在开发服务端 ERROR 0 条；掉落效果未在游戏内核对 |
| 进度 | `display.icon` 为 `{"id": "<物品>"}`（来源是 `{"item": ...}`）；`display.background`、`title`、`criteria` 写法不变 | 8 个进度在开发服务端加载无错误 |
| 旧结构 NBT | `StructureTemplateManager.readStructure` 按文件里的 DataVersion 用数据修复器升级；DataVersion 2975（1.18.2）的结构可直接加载，调色板里的原版 `grass` 变为 `short_grass`（2026-11-02 实测，160 个结构）。调色板里不存在的方块名静默变成空气，没有日志 | 不必预先升级，只需把来源命名空间改写成目标命名空间；登记新方块后重新核对调色板 |
| 结构里的额框与画 | NBT 中保存旧世界的绝对坐标 `TileX/Y/Z`，`BlockAttachedEntity` 读取时离实体超过 16 格即记 ERROR，实体位置仍由摆放代码重设 | 摆放结构会产生这类 ERROR 日志，功能不受影响 |
| 超大结构 | 展开后 304 MB、844 万方块的模板在默认堆下解析 NBT 即 `OutOfMemoryError`（`CompoundTag` 每个方块一个哈希表） | 内容里大部分是空气的模板需要压缩、分段或提高内存，不能依赖原样加载 |
| 流体与水 | NeoForge 21.1.265 把实体的 `FluidTags.WATER` 判断（`isInWater`、`isEyeInFluid` 等）改为判断水的流体类型；自定义流体列入 `minecraft:water` 标签不再让实体按水处理。游泳、溺水、灭火、摔落、载船、湿润耕地由 `FluidType.Properties` 决定 | 来源靠水标签获得水行为的流体，按需设置流体类型属性；标签仍影响其他按标签判断的逻辑。流体贴图在 `RegisterClientExtensionsEvent` 登记，渲染层用 `ItemBlockRenderTypes.setRenderLayer(Fluid, ..)`（未弃用） |
| 实体水下呼吸 | 21.1.265 的 `LivingEntity.canBreatheUnderwater()` 是 `final`，读取 `EntityTypeTags.CAN_BREATHE_UNDER_WATER`（`minecraft:can_breathe_under_water`）；其弃用说明指向 `canDrownInFluidType` | 来源覆写水下呼吸的方法不能原样移植；原版水中的呼吸能力可用该实体类型标签表达，涉及自定义流体时另核对 `FluidType` 的溺水规则 |
| 桶模型 | 1.18.2 Forge 的 `"loader": "forge:bucket"`、`"parent": "forge:item/bucket_drip"` 改为 `"loader": "neoforge:fluid_container"`、`"parent": "neoforge:item/bucket_drip"`，`fluid` 字段不变 | 资源转换遇到桶模型时改写加载器，不复制 forge 命名空间的父模型 |
| 原版 ID 改名 | `scute` 在 1.20.5 改名为 `turtle_scute`；原版语言文件仍保留 `item.minecraft.scute` 旧键 | 判断原版物品是否存在不能用语言键，用 `assets/minecraft/models/item` 与 `blockstates` 的文件；来源数据中出现的改名项登记为显式对应表 |

## 第三方库（已核实）

| 库 | 事实 |
| --- | --- |
| GeckoLib（`software.bernie.geckolib:geckolib-neoforge-1.21.1`） | Cloudsmith maven 元数据含 4.6–4.9.3（2026-11-02）；4.8.3 的 `neoforge.mods.toml` 声明 MIT、NeoForge `[21.1.150,)`；加载器识别 `.geo.json`、`.animation.json` 与 `animations` 目录。GeckoLib 3 的 `IAnimatable` 由 4.x 的 `GeoEntity`/`GeoAnimatable` 等接口取代，控制器代码需重写 |

## 开发环境（已实测）

2026-11-02 在 Windows + Temurin JDK 21.0.9 上实测：Gradle 8.8 wrapper + ModDevGradle 2.0.141 + NeoForge 21.1.228 完成 `build`，`runServer` 启动至 Done，`runClient` 加载至声音引擎启动。

同日升级后复测：Gradle 8.8 + ModDevGradle 2.0.148 + NeoForge 21.1.265（Gradle 缓存中的 FML 为 4.0.45）完成 `build`，`runServer` 启动至 Done，`runClient` 加载至声音引擎启动，两端日志无 ERROR。升级只改版本号，无需改动入口代码；21.1.265 的构建新增 `net.neoforged.jst:jst-cli-bundle` 约 58 MB 依赖，慢速网络下首次构建的耗时主要在依赖下载。

- Parchment 的 1.21.1 映射使用 `parchment-1.21.1` 制品（同日 maven 元数据最新正式版 2024.11.17）；不要配用其他 Minecraft 版本的 Parchment。
- 开发运行的专用服务器未要求 eula.txt 即启动完成，不需要替负责人写入 EULA 同意。
- `runServer` 启动后等待控制台输入，不会自行结束；终止 Gradle 任务后，wrapper 与游戏 Java 进程可能残留，需按命令行查找并结束。
- Windows 控制台按系统代码页显示非 ASCII 的 Mod 名称时会乱码；`run/logs/latest.log` 为 UTF-8，以日志文件核对显示名。
- 客户端在窗口活动且没有打开界面时捕获鼠标（`MouseHandler.grabMouse` 只检查 `Minecraft.isWindowActive()`）。自动化的客户端运行在每 tick 开始时 `setWindowActive(false)` 并关闭失去焦点暂停，就不会占用开发者的鼠标；窗口启动时取得键盘焦点是操作系统行为，无法由模组避免。
- 新建世界后很快退出时，集成服务端的停止流程可能在“等待区块工作完成”的循环中空转（某个区块尚有加载或光照待办，`isReadyForSaving` 一直为假）。自动化退出前等 `ServerChunkCache.getPendingTasksCount()` 连续一段时间为 0。
- 本机内存不足时 Gradle wrapper 的 JVM 会在工作目录写 `hs_err_pid*.log`、`replay_pid*.log`；这类文件应被版本控制忽略。资源转换一次重写数千个文件后紧接构建，会给编辑器的文件监视带来很大压力，重型任务逐个运行。
- 客户端自动化退出存档时，`Minecraft.disconnect` 在等待集成服务端关闭期间循环调用 `runTick`，`ClientTickEvent` 会嵌套触发；脚本在调用前置忙标志，嵌套的 tick 不推进。重新进入用 `WorldOpenFlows.openWorld(存档目录名, 失败回调)`（21.1.265 实测）。

## 使用注意

- 附件已提供同步时，不另建同内容的 payload；可变对象修改后的通知和保存仍须验证。
- payload 处理默认在主线程，不再套用其他版本经验加一层异步队列。
- 来源的伤害前置规则按目标事件阶段逐入口对照，不凭事件名称机械替换。
- 物品组件用 `DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, modid)` 与 `registerComponentType(名, b -> b.persistent(Codec).networkSynchronized(StreamCodec))` 登记；无参版本 `createDataComponents(modid)` 已弃用。物品栈作为玩家附件时用 `ItemStack.OPTIONAL_CODEC` 存档、`ItemStack.OPTIONAL_STREAM_CODEC` 同步。
- 方块容器的菜单在客户端用临时容器（如 `SimpleContainer`），内容随菜单同步，只把方块位置写进打开数据；方块刚放置的同一 tick 打开界面时客户端还没有方块实体。
- Data Component 的 Codec/StreamCodec、SavedData 工厂、菜单容量、实体生成和渲染注册，在上述 sources jar 的 Minecraft 源码中确认，不凭记忆指定构造签名。
| 方块模板构造器 | `net/minecraft/world/level/block/DoorBlock.java`、`TrapDoorBlock.java`、`ButtonBlock.java`、`FenceGateBlock.java`、`state/properties/BlockSetType.java` | 21.1.265：门、活板门、按钮的构造器用 `BlockSetType` 的声音覆盖 Properties 的声音，`FenceGateBlock(WoodType, ..)` 同理（另有显式声音的构造器）；需要保留来源音效时派生 `BlockSetType`（`BlockSetType.register`）。`PressurePlateBlock` 的触发范围来自 `BlockSetType.pressurePlateSensitivity` |
| 方块 Properties | `net/minecraft/world/level/block/state/BlockBehaviour.java` | 21.1.265：1.18.2 的 `Material` 已不存在，颜色用 `mapColor`、可燃用 `ignitedByLava`、可替换用 `replaceable`、推动反应用 `pushReaction`；`noDrops` 改名 `noLootTable`；`getLightBlock`、`getShape`、`getVisualShape`、`propagatesSkylightDown` 为 protected 方法，覆写 `getPistonPushReaction` 须为 public（与 `IBlockExtension` 冲突）；每个方块类必须实现 `codec()` |
| 模型渲染层 | `net/neoforged/neoforge/client/model/ExtendedBlockModelDeserializer.java`、`geometry/BlockGeometryBakingContext.java` | 21.1.265：模型 JSON 的 `render_type`（如 `minecraft:cutout`）由 NeoForge 读取，沿 parent 链继承，子模型可覆盖；取代 1.18.2 在代码里 `setRenderLayer` |
| 食物 | `net/minecraft/world/food/FoodProperties.java` | 21.1.265：食物是物品组件，`FoodProperties(nutrition, saturation, canAlwaysEat, eatSeconds, ..)`；1.18.2 的 `saturationMod` 经 `FoodConstants.saturationByModifier` 换算为 saturation，使用时长为 `eatSeconds`（1.18.2 默认 32 tick = 1.6 秒） |
| 伤害事件 | `net/minecraft/world/entity/LivingEntity.java` 的 `hurt`、`actuallyHurt`，`net/neoforged/neoforge/event/entity/living/LivingIncomingDamageEvent.java` | 21.1.265：`LivingAttackEvent` 与 `LivingHurtEvent` 合并为 `LivingIncomingDamageEvent`，在 `hurt` 通过无敌、客户端、死亡与抗火检查后、护甲与魔法减免之前触发，可取消、可改伤害量；减免之后是 `LivingDamageEvent.Pre`。没有攻击者的伤害来源 `getEntity()` 为空 | 来源在两个事件里先后处理的规则合并到一个监听器内按原顺序执行；先检查来源的伤害类型再决定是否套用公式
| 状态效果钩子 | `net/minecraft/world/effect/MobEffect.java`、`MobEffectInstance.java`，`LivingEntity.java` 的 `addEffect`、`tickEffects`，`MobEffectEvent.java` | 21.1.265：`MobEffect` 没有携带实体的 `addAttributeModifiers`；每刻处理用 `shouldApplyEffectTickThisTick` 与返回布尔的 `applyEffectTick`（客户端实体同样执行）；`onEffectStarted` 每次 `addEffect` 调用，`onEffectAdded` 只在新增时调用；效果刷新是否应用由 `MobEffectInstance.update` 的返回值决定，可用 `new MobEffectInstance(旧).update(新)` 在 `MobEffectEvent.Added` 中复现；对应 `removeAttributeModifiers` 的结束要分三处：到期 `MobEffectEvent.Expired`、被移除 `MobEffectEvent.Remove`（均可被取消，取最低优先级且不接收已取消的事件）、刷新前（`Added` 中旧实例存在且 `update` 为真）；`MobType` 已删除，亡灵用实体类型标签 `minecraft:undead` | 按钩子语义逐个对照来源的开始、每刻与结束过程
| 附魔数据 | `net/minecraft/world/item/enchantment/Enchantment.java`，`EnchantmentHelper.java` | 21.1.265：附魔是数据包定义（`description`、`supported_items`、`weight`、`max_level` 1–255、`min_cost`/`max_cost`、`anvil_cost`、`slots`），读等级用 `stack.getEnchantmentLevel(Holder)`；写入的等级可以超过 `max_level`；`in_enchanting_table`、`non_treasure`、`treasure`、`on_random_loot`、`tradeable` 等是附魔标签，`bypasses_armor` 是伤害类型标签 | 来源的稀有度、类别、槽位与 `isTradeable`、`isTreasureOnly` 逐项换成对应字段与标签
| 物品耐久 | `net/minecraft/world/item/ItemStack.java` | 21.1.265：`hurtAndBreak(int, ServerLevel, LivingEntity, Consumer<Item>)` 在实体为空时也扣耐久，实体带无限材料（创造模式）时不扣；`hurtAndBreak(int, LivingEntity, EquipmentSlot)` 经前者，创造模式不扣 | 来源以空玩家调用 `hurt` 的损耗不分游戏模式，目标用实体为空的重载
| 重复键与按下沿 | `net/minecraft/client/KeyMapping.java` 的 `set`、`click`、`setDown`，`KeyboardHandler.java` | 21.1.265：同一个物理键可分派到多个绑定；键盘重复事件也会调用 `setDown(true)` 并增加点击计数。开发客户端验证自定义绑定与原版移动共用键、网络传动作后本人产生实际位移 | 来源只在松开到按下时触发的动作可覆写 `setDown` 检查状态变化，不能把每次 `consumeClick` 当作新的物理按下
| 原生菜单按钮 | `net/minecraft/server/network/ServerGamePacketListenerImpl.java` 的 `handleContainerButtonClick`，`AbstractContainerMenu.java` 的 `clickMenuButton` | 21.1.265：消息处理检查非旁观者、当前菜单容器 ID 与 `stillValid`，再调用菜单的按钮处理；`SimpleMenuProvider` 可打开没有物品槽的菜单 | 只有按钮的界面也可沿用原生菜单身份和消息，不必另建不绑定当前菜单的 payload；操作条件仍由服务端按钮入口判断 |
| HUD 层注册 | `net/neoforged/neoforge/client/event/RegisterGuiLayersEvent.java`，`GuiLayerManager.java` | 21.1.265：客户端 Mod 总线事件提供 `registerBelowAll` 等顺序注册，绘制回调接收 `GuiGraphics`、`DeltaTracker` | 客户端入口装配显示层，只读取已同步数据；固定来源坐标须在实际窗口与 GUI 缩放中核对裁切 |
| 服务端粒子空调用 | `net/minecraft/world/level/Level.java` 的 `addParticle`，`ServerLevel.java` 的 `sendParticles` | 21.1.265：基础 `Level.addParticle` 的相关重载为空，`ServerLevel` 未覆写为广播；服务端发送粒子使用 `sendParticles` | 来源在服务端调用空方法时，迁移为广播会新增原本不存在的表现；先核对实际执行端和来源版本，不能仅凭方法名补造效果 |
| 箭的内部物品与保存 | `net/minecraft/world/entity/projectile/AbstractArrow.java` 的构造、`getDefaultPickupItem`、`getPickupItem`、`addAdditionalSaveData` | 21.1.265：内部箭物品通过非空 `ItemStack` 编码保存；默认物品为空会在实体保存时报 `Cannot encode empty ItemStack`。开发客户端实测复现并验证修复 | 不可拾取的自定义箭仍须给内部物品非空值；实际拾取可由 `getPickupItem` 返回空控制
| 结束钩子的效果查询时机 | `net/minecraft/world/entity/LivingEntity.java` 的 `onEffectUpdated`、`tickEffects`、`removeEffect`、`removeAllEffects`，`MobEffect.removeAttributeModifiers` | 21.1.265：刷新时实体中的实例已更新；到期或单独移除时已从集合删除；清除全部效果时先调用移除属性钩子再删集合。开发客户端验证刷新现读新等级、单独移除查询为空 | 来源结束过程若重新查询实体的效果，不能用事件中保存的旧等级代替；需要准确时序时使用原生钩子，另补其缺失的实体参数
| GeckoLib 4.9.3 | `software.bernie.geckolib:geckolib-neoforge-1.21.1:4.9.3`（maven `https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/`，只放行组 `software.bernie.geckolib`；javap 核对 jar） | 2026-11-04 在开发客户端实测（实体渲染、待机与触发动画、受击动画）：Mod ID `geckolib`，元数据依赖写 `required`；实体实现 `GeoEntity`，`GeckoLibUtil.createInstanceCache(this)`，`registerControllers(AnimatableManager.ControllerRegistrar)` 里加 `AnimationController`，用 `triggerableAnim(名, RawAnimation)` 注册触发动画、服务端 `triggerAnim(控制器名, 动画名)` 触发（不再需要同步数据字段）；渲染器继承 `GeoEntityRenderer<T>`，几何、动画、贴图由 `GeoModel<T>` 的 `getModelResource`、`getAnimationResource`、`getTextureResource` 给出，`geo/*.geo.json` 与 `animations/*.animation.json` 沿用 1.18.2（GeckoLib 3）的文件；改渲染类型重写 `getRenderType(T, ResourceLocation, MultiBufferSource, float)` | 冒烟运行的 `smokeImplementation` 也要加该依赖；IDE 未刷新依赖时会误报找不到类，以 Gradle 编译为准
