# NeoForge 1.21.1

已核对范围：NeoForge 21.1.228 源码（sources jar 静态阅读），以及一个只含加载入口的开发环境实测。其他 21.1.x 构建可能不同，使用前核对所用构建。源码事实只支撑架构判断，不替代编译和游戏验证。路径相对 NeoForge 源码根。

| 机制 | 源码 | 已确认事实 |
| --- | --- | --- |
| 延迟注册/命名空间 | `net/neoforged/neoforge/registries/DeferredRegister.java` | create/createItems/createBlocks 的 namespace 参数用于构造注册 ID |
| 玩家数据与死亡 | `net/neoforged/neoforge/attachment/AttachmentType.java` | 附件类型需注册；可序列化实体附件默认不在死亡时复制，可通过 copyOnDeath 选择复制；方块/区块变更需相应标脏 |
| 附件同步 | 同文件 `Builder.sync` 及 `attachment/IAttachmentHolder.java` 的 syncData | 支持自定义同步处理器、StreamCodec 与接收玩家过滤；完整 codec 方案每次写完整数据 |
| 物品组件注册 | `registries/DeferredRegister.java` 的 registerComponentType | 有 DataComponentType 专用 builder 注册入口 |
| 实体复制事件 | `net/neoforged/neoforge/event/entity/player/PlayerEvent.java` 的 Clone | 提供原对象和是否死亡信息；复制不限于普通死亡 |
| 网络注册 | `net/neoforged/neoforge/network/event/RegisterPayloadHandlersEvent.java` | payload 注册发生于 Mod 总线；使用前需登记；单人内存连接也序列化 |
| 网络执行线程 | `net/neoforged/neoforge/network/registration/PayloadRegistrar.java` | 初始 handler 线程为 MAIN；支持 playToServer/playToClient 和显式线程选择 |
| 服务端重载 | `net/neoforged/neoforge/event/AddReloadListenerEvent.java` | 游戏事件总线提供重载监听器；此时内建和动态注册表已冻结；监听器得到 registry lookup |
| 伤害入口 | `event/entity/living/LivingIncomingDamageEvent.java` | 发生于无敌检查之后、减伤之前，可取消并读取 DamageContainer |
| 伤害应用与结果 | `event/entity/living/LivingDamageEvent.java` 的 Pre/Post | Pre 在护甲/药水减伤后、吸收前，可修改伤害；Post 在生命变化后提供结果 |

## 开发环境（已实测）

2026-11-02 在 Windows + Temurin JDK 21.0.9 上实测：Gradle 8.8 wrapper + ModDevGradle 2.0.141 + NeoForge 21.1.228 完成 `build`，`runServer` 启动至 Done，`runClient` 加载至声音引擎启动。

- Parchment 的 1.21.1 映射使用 `parchment-1.21.1` 制品（同日 maven 元数据最新正式版 2024.11.17）；不要配用其他 Minecraft 版本的 Parchment。
- 开发运行的专用服务器未要求 eula.txt 即启动完成，不需要替负责人写入 EULA 同意。
- `runServer` 启动后等待控制台输入，不会自行结束；终止 Gradle 任务后，wrapper 与游戏 Java 进程可能残留，需按命令行查找并结束。
- Windows 控制台按系统代码页显示非 ASCII 的 Mod 名称时会乱码；`run/logs/latest.log` 为 UTF-8，以日志文件核对显示名。

## 使用注意

- 附件已提供同步时，不另建同内容的 payload；可变对象修改后的通知和保存仍须验证。
- payload 处理默认在主线程，不再套用其他版本经验加一层异步队列。
- 来源的伤害前置规则按目标事件阶段逐入口对照，不凭事件名称机械替换。
- `@EventBusSubscriber` 等 FML 注解不在 NeoForge sources jar 内，需另查 FML 源码。
- Data Component 的 Codec/StreamCodec、SavedData 工厂、菜单容量、实体生成和渲染注册，在目标开发环境解析出的 Minecraft 源码中确认，不凭记忆指定构造签名。
