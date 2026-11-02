# NeoForge 1.21.1

已核对范围：NeoForge 21.1.228 源码（sources jar 静态阅读），以及一个只含加载入口的开发环境实测（21.1.228 与 21.1.265 的构建和启动；除注明 21.1.265 的条目外，源码阅读未在 21.1.265 上重做）。其他 21.1.x 构建可能不同，使用前核对所用构建。源码事实只支撑架构判断，不替代编译和游戏验证。路径相对 NeoForge 源码根。

| 机制 | 源码 | 已确认事实 |
| --- | --- | --- |
| 延迟注册/命名空间 | `net/neoforged/neoforge/registries/DeferredRegister.java` | create/createItems/createBlocks 的 namespace 参数用于构造注册 ID；21.1.265 只检查非空，不要求与 Mod ID 一致 |
| 注册表别名 | `net/neoforged/neoforge/registries/IRegistryExtension.java`、`net/minecraft/core/MappedRegistry.java` | 21.1.265：`addAlias(from, to)` 使查找 `from`（且其不存在）时解析为 `to`，支持链式；`MappedRegistry` 按名与按键查找经过 `resolve`。区块、物品与玩家数据读取中的实际效果未实测 |
| 物品创建者 Mod | `net/neoforged/neoforge/common/CommonHooks.java` 的 `getDefaultCreatorModId` | 21.1.265：默认以物品注册名的命名空间作为创建者 Mod ID（原版附魔书按附魔命名空间）；物品可覆盖 `IItemExtension.getCreatorModId` |
| 玩家数据与死亡 | `net/neoforged/neoforge/attachment/AttachmentType.java` | 附件类型需注册；可序列化实体附件默认不在死亡时复制，可通过 copyOnDeath 选择复制；方块/区块变更需相应标脏 |
| 附件同步 | 同文件 `Builder.sync` 及 `attachment/IAttachmentHolder.java` 的 syncData | 支持自定义同步处理器、StreamCodec 与接收玩家过滤；完整 codec 方案每次写完整数据 |
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

## 使用注意

- 附件已提供同步时，不另建同内容的 payload；可变对象修改后的通知和保存仍须验证。
- payload 处理默认在主线程，不再套用其他版本经验加一层异步队列。
- 来源的伤害前置规则按目标事件阶段逐入口对照，不凭事件名称机械替换。
- Data Component 的 Codec/StreamCodec、SavedData 工厂、菜单容量、实体生成和渲染注册，在上述 sources jar 的 Minecraft 源码中确认，不凭记忆指定构造签名。
| 方块模板构造器 | `net/minecraft/world/level/block/DoorBlock.java`、`TrapDoorBlock.java`、`ButtonBlock.java`、`FenceGateBlock.java`、`state/properties/BlockSetType.java` | 21.1.265：门、活板门、按钮的构造器用 `BlockSetType` 的声音覆盖 Properties 的声音，`FenceGateBlock(WoodType, ..)` 同理（另有显式声音的构造器）；需要保留来源音效时派生 `BlockSetType`（`BlockSetType.register`）。`PressurePlateBlock` 的触发范围来自 `BlockSetType.pressurePlateSensitivity` |
| 方块 Properties | `net/minecraft/world/level/block/state/BlockBehaviour.java` | 21.1.265：1.18.2 的 `Material` 已不存在，颜色用 `mapColor`、可燃用 `ignitedByLava`、可替换用 `replaceable`、推动反应用 `pushReaction`；`noDrops` 改名 `noLootTable`；`getLightBlock`、`getShape`、`getVisualShape`、`propagatesSkylightDown` 为 protected 方法，覆写 `getPistonPushReaction` 须为 public（与 `IBlockExtension` 冲突）；每个方块类必须实现 `codec()` |
| 模型渲染层 | `net/neoforged/neoforge/client/model/ExtendedBlockModelDeserializer.java`、`geometry/BlockGeometryBakingContext.java` | 21.1.265：模型 JSON 的 `render_type`（如 `minecraft:cutout`）由 NeoForge 读取，沿 parent 链继承，子模型可覆盖；取代 1.18.2 在代码里 `setRenderLayer` |
| 食物 | `net/minecraft/world/food/FoodProperties.java` | 21.1.265：食物是物品组件，`FoodProperties(nutrition, saturation, canAlwaysEat, eatSeconds, ..)`；1.18.2 的 `saturationMod` 经 `FoodConstants.saturationByModifier` 换算为 saturation，使用时长为 `eatSeconds`（1.18.2 默认 32 tick = 1.6 秒） |
