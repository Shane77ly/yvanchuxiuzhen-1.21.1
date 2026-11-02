---
topic: engineering
role: architecture
status: current
updated: 2026-11-02
---

# 工程结构与长期维护

状态：负责人 2026-11-02 批准（main 提交 `3e0585c` 版本）；标注“待验证”的做法实测后写回并移除标注（B0 于步骤 5 后结束，其余项随 B1 与首个使用该机制的系统批次实测），批准不含存档导入、界面复刻程度、license 与 Q01–Q14。系统划分、归属判定与依赖规则以[系统设计](system-design.md)为正文（2026-11-02 批准，本文目录名与 ArchUnit 规则随之改写），玩法所有权以[整体架构](architecture.md)为正文，本文规定代码与工程如何承载它们。文中“已核实”指本机源码、制品或运行结果；其余为方案或“待验证”。

## 设计约束

| 约束 | 来源 | 对工程的要求 |
| --- | --- | --- |
| 规模 | 4,867 条注册（2,243 物品、1,180 方块、271 实体类型、315 粒子、150 声音），16,282 个来源资源文件，28 个系统与 3 个内容族 | 不能一条内容一个手写注册段；资源必须可批量、可复现地转换 |
| 长期多会话推进 | 负责人以新会话分批移植 | 冲突热点要拆开；每次会话可从仓库状态恢复；规则要能自动检查 |
| 目标平台变化 | 已核实：1.21.1 的附魔为数据驱动 `record`，群系与地物在 worldgen 数据中，燃料与堆肥为 NeoForge 数据映射，数据目录改为单数（`recipe`、`loot_table`、`tags/item`），配方产物使用 `id` | 来源中以 Java 注册的 85 个附魔、68 个群系和 220 个地物需要“代码注册 → 数据定义”转换 |
| 依赖生态 | GeckoLib 是唯一已确认的必需库；NeoForge 21.1.x 持续发布补丁（2026-11-02 maven 最新 21.1.265） | 依赖少而固定；版本更新有固定时机和检查 |
| 邻近工程经验 | 其 `init` 下物品/方块/实体集中注册类分别为 2,852 / 1,550 / 1,237 行 | 不建立全项目集中注册类 |

## 工程形态

一个 Gradle 工程、一个 Mod、一个发布 JAR。多 Gradle 子工程能在编译期强制边界，但 ModDevGradle 需要逐工程配置并合并产物，运行配置与 IDE 同步也更复杂；对单一发布物收益不足。边界改由自动检查保证（见下节）。

```text
src/main/java/net/yvanchuxiuzhen/
  YvanchuxiuzhenMod.java        公共入口：按依赖顺序调用各系统的 <名称>Setup.register
  YvanchuxiuzhenClient.java     @Mod(value = ModId.VALUE, dist = Dist.CLIENT) 客户端入口：调用各系统的 <名称>ClientSetup
  platform/                     至少两个系统实际共用的平台适配：内容表加载、注册辅助、payload 登记、codec 工具
  flow/                         跨系统具体流程（闭关、击杀结算、装备重算等）
  panel/                        玩家面板：分页导航框架、HUD 与覆盖层；各页菜单归所属系统
  collection/<内容族>/          内容族：内容表与元素自身行为，没有 api
  system/<系统>/
    <名称>Setup.java            启动装配：把本系统的注册器与监听接到总线；公共入口只调用这一类
    api/                        对其他系统公开的查询、操作与值类型
    rule/                       纯规则与公式，不依赖 net.minecraft / net.neoforged
    data/                       附件、组件、SavedData、codec
    content/                    物品、方块、实体、方块实体、菜单
    network/                    payload 与服务端处理
    client/                     屏幕、HUD、按键、渲染、模型、粒子；<名称>ClientSetup 为本系统客户端装配
  compat/<modid>/               可选联动；当前无计划
src/main/resources/
  assets/yvanchuxiuzhen/        唯一命名空间，与 Mod ID 相同
  data/yvanchuxiuzhen/
  content/<系统或内容族>/        静态内容表（见“注册与内容表”）
src/main/lang/<语言>/<系统或内容族>.json   语言片段，构建时合并
src/main/sounds/<系统或内容族>.json        声音事件片段，构建时合并
src/gametest/java/              GameTest，只进入开发运行，不进入发布 JAR（待验证）
tools/porting/                  来源索引、台账检查、资源转换
```

- 本文的“系统”指[系统设计](system-design.md)清单中的 28 个系统，即拥有自身数据与规则的所有权单位；不是 ECS 式运行时系统，也没有“系统注册”框架。内容族与 `panel` 同样有自己的 `<名称>Setup`、内容表、语言片段、文档目录与台账，下文以 `<系统>` 表示的路径同样适用于它们。
- `<名称>Setup` 只在 Mod 构造时调用一次：登记本系统的 `DeferredRegister`、payload 与事件监听，不持有状态，不实现接口，运行期不再被调用。例如 `AlchemySetup.register(IEventBus modBus)`。装配集中在入口显式调用，使启动顺序与注册范围在一处可见；`DeferredRegister` 本身也需要显式 `register(modBus)`。
- 系统与内容族的目录名取[系统设计](system-design.md)的标识。简单系统可以平铺，不预建空子包。
- 客户端代码放在所属系统的 `client` 子包，由客户端入口装配；已实测（NeoForge 21.1.265、FML 4.0.45）专用服务器只构造公共入口，客户端先构造公共入口再构造客户端入口。`@EventBusSubscriber` 的 `bus` 属性在该版本已标记弃用，按事件类型自动选择总线。
- `platform` 替代原方案的 `shared`：只放平台适配，不放玩法类型；玩法共享值类型放在所有者的 `api`。

## 边界与自动检查

规则写进构建才有约束力。采用 ArchUnit（测试依赖，只检查编译后的类）在 `gradlew check` 中执行：

1. `system.X` 只能依赖其他系统的 `api` 包；`flow`、`panel`、`collection` 同样只用 `api`。
2. `client` 包与客户端入口之外的类不得依赖任何 `client` 包或 `net.minecraft.client`。
3. `rule` 包不得依赖 `net.minecraft`、`net.neoforged`。
4. `platform` 不得依赖 `system`、`flow`、`panel` 或 `collection`。
5. `system` 下各系统包之间没有循环依赖。
6. 除 Mod 入口外，任何类不得依赖 `collection` 或 `panel`。

这些是结构守卫，不属于“为每个类写测试”；违反即构建失败。已接入（B0 步骤 3）：`src/test/java/net/yvanchuxiuzhen/ArchitectureTest.java` 用 ArchUnit 核心库在 JUnit 5 中检查，第 2 条的“client 包”包括本项目的 `client` 子包、`net.minecraft.client` 与 `net.neoforged.neoforge.client`；第 3 条在还没有 `rule` 包时允许为空。2026-11-02 为每条规则临时制造一次违规，六条各被检出一次并使构建失败。

## 注册与内容表

同质内容（普通材料、装饰方块、家具、普通物品等）由所属系统的静态内容表声明，在 Mod 构造时用 Codec 解码并登记到该系统的 `DeferredRegister`：

- 每行包含路径（命名空间固定为 Mod ID）、工厂类型和必要属性（堆叠、稀有度、方块属性、创造栏）。工厂类型由所属系统的代码提供，未知类型在启动时报出文件与条目，不回退为普通物品。
- 有专属行为的物品、方块、实体在所属系统用 Java 显式注册；它们仍在台账中登记。
- 每个系统按注册表类型持有自己的 `DeferredRegister`，命名空间均为 `ModId.VALUE`；不存在全项目集中注册类。
- 代码需要引用的条目通过所属系统 `api` 暴露的持有者访问。

全部注册使用命名空间 `yvanchuxiuzhen`（负责人 2026-11-02 决定）：原初系统的注册保留来源路径，只换命名空间。两个来源在同一注册表下同名的只有菜单 `plaque_gui` 与粒子类型 `lingqi`，读来源后确认两边行为不同，原初系统一侧暂定为 `plaque_gui_xitong`、`lingqi_xitong`，所属单位实现时可按语义改名。目标 ID 的命名空间与唯一性由台账检查保证。

### 内容表格式（B1 已实现并实测）

- 位置：`src/main/resources/content/<单位>/blocks.json`、`items.json`，每个文件是行的数组，一行一个对象；创造栏在 `content/platform/tabs.json`。由 `tools/porting/convert_content.py` 生成，系统验收前可重跑。
- 行：方块行有 `id`、`factory`、`material`、`sound`、`states` 等字段，物品行有 `id`、`factory`、`order`（来源注册顺序）与 `tab`、`stack`、`rarity`、`food`、`tooltip` 等，完整字段见 `platform/content/BlockRow`、`ItemRow` 的 Codec。字段取自来源**生成的 Java**（构造器 Properties 链、状态属性与放置规则、`getShape`、`getLightBlock`、可燃、`canHarvestBlock`、回退掉落），不取编辑器字段名；方块类中玩法钩子（右键、tick、方块实体、菜单等）只记入 `pending_hooks`，B1 不实现。
- 加载：每个单位的 `<名称>Setup` 持有一个 `ContentTables(单位)`，Mod 构造时读取该单位的内容表，用 Codec 解码并登记到本单位的 `DeferredRegister`。解码失败或含有 Codec 不认识的字段（编码回写后比对键集合）时报出文件与行号并中止启动；未知工厂类型同样中止。
- 工厂：`platform/content` 提供 13 种方块工厂（普通、含水普通、台阶、楼梯、墙、树叶、栅栏、栅栏门、按钮、压力板、玻璃板、门、活板门、下落）与物品、方块物品、双格方块物品。系统专属类型（工具、盔甲、动画方块等）由所属系统提供，内容需要专属行为时所属系统把该行的工厂换成自己的类型。
- 不进内容表：渲染层写在模型 JSON 的 `render_type`（见资源转换）；挖掘等级沿用来源的“工具类 + 等级”判定，在方块的 `canHarvestBlock` 中实现，等级由 Tier 的“不能正确采掘的方块”标签还原（1.21.1 的 Tier 没有等级；自定义等级随工具工厂实现时再登记）。
- 实测（2026-11-02，NeoForge 21.1.265）：832 个方块与 1,626 个物品（方块物品含在内）登记后，开发服务端启动至 `Done (4.2s)`，日志 ERROR 0 条；开发客户端加载至声音引擎启动，ERROR 0 条。开发服务端按 `build.gradle` 的运行配置导出注册表到 `build/registry-export`，`check_ledger.py` 与台账比对一致。
- 与 1.21.1 构造器的差异：`DoorBlock`、`TrapDoorBlock`、`ButtonBlock` 的构造器会用 `BlockSetType` 的声音覆盖 Properties 的声音，`FenceGateBlock(WoodType, ..)` 同理，因此按 (用途, 基础类型, 音效) 派生 `BlockSetType`、栅栏门用显式声音的构造器，使来源音效保留。

### B1 范围（2026-11-02 静态盘点，来源为 MCreator 2023.1 的 forge-1.18.2 生成器，全部元素未锁定代码）

| 来源形态 | 数量 | B1 处理 |
| --- | ---: | --- |
| 普通方块（`Block`） | 662 | 方块工厂：形状（按朝向旋转的碰撞箱）、朝向方式、含水、透明与透光、亮度、可燃、推动反应、挖掘判定 |
| 原版模板方块：台阶、楼梯 30/30，树叶 27，门 16，玻璃板 16，压力板 15，按钮、栅栏、栅栏门、活板门各 8，墙 5 | 171 | 对应原版类的工厂 |
| 植物：`FlowerBlock`、`DoublePlantBlock` | 267 / 28 | 植物工厂 |
| 下落方块（`FallingBlock`） | 1 | 方块工厂 |
| 带物品栏的方块 | 115 | 按方块登记身份；方块实体、菜单随所属系统 |
| GeckoLib 动画方块（`BaseEntityBlock`） | 40 | 随所属系统（渲染依赖方块实体与 GeckoLib） |
| 维度传送门（`NetherPortalBlock`）、流体方块（`LiquidBlock`） | 6 / 5 | 随 `world` 与所属系统（需要维度与流体类型） |
| 普通物品（`Item`） | 794 个类 | 物品工厂：堆叠、稀有度、防火、食物、提示文本 |
| 方块物品 | 1,169 | 随方块登记 |
| 工具（剑 72、斧 11、镐 9、`TieredItem` 9、锹 1）、普通盔甲（18 套） | — | 所属系统提供的工厂，B1 登记身份与属性 |
| GeckoLib 盔甲与动画物品、桶 | 17 / 4 / 4 | 随所属系统 |
| 创造栏 | 原初修真 8 个、原初系统 2 个 | 1.21.1 中为注册内容，由 `platform` 登记，物品按来源归入 |

带过程的方块（321 个元素）、物品的使用与右键过程、方块矿物生成（45 个元素）不在 B1，随所属系统迁移；B1 的“已注册”只表示身份、资源与基础属性就位。

## 资源转换管线

来源资源以可复现的转换工具迁入，而不是手工复制或用 datagen 重画：

| 资源 | 处理 |
| --- | --- |
| 模型、方块状态、纹理、粒子定义、声音文件 | 复制并校验引用；路径按目标原版目录；`yvanchuxitong` 下的文件移入 `yvanchuxiuzhen`，文件中的 ID 引用一并改写 |
| 配方、掉落表、标签、进度、结构 | 改单数目录；配方产物改用 `id`/`count`；物品 NBT 改为组件；`yvanchuxitong:` ID（含结构 NBT 的方块调色板）改写为目标 ID |
| 代码注册的附魔、群系、地物 | 依来源 Java 的构造参数生成目标数据定义；附魔效果按目标效果组件或战斗系统读取等级重写 |
| 燃料、堆肥等来源代码表 | 生成 NeoForge 数据映射 |
| 语言文件 | 拆出机器配置交给所属系统定义；显示文本按系统拆成语言片段；键中的命名空间与改名同步改写 |
| `sounds.json` | 按系统片段合并 |
| GeckoLib 几何与动画 | 文件后缀与 `geo`/`animations` 目录约定沿用（已核实 GeckoLib 4.8.3 的加载器识别 `.geo.json`、`.animation.json` 与 `animations`）；控制器代码按 GeckoLib 4 重写 |
| 结构 NBT | 旧 DataVersion 由游戏加载时升级还是预先升级，B1 实测后决定 |

两个来源在 `assets`、`data` 下相对路径相同的文件有 60 个：57 个字节相同，只迁入一份；其余 3 个是两份语言文件与 `particles/lingqi.json`，前者按键合并（同名键只有 `plaque_gui` 的界面文本），后者随粒子改名分开。转换工具遇到相对路径相同而内容不同、又没有改名登记的文件时报错。

转换工具每次输出一份清单（来源文件、哈希、目标文件），台账检查据此确认没有遗漏。系统验收前可以重跑转换；验收后转换结果成为手工维护的正文，清单保留来源。只有转换无法表达的资源才使用 datagen。

已实现并实测（2026-11-02）：`tools/porting/convert_content.py` 生成内容表与语言片段（方块、物品名称与提示文本、创造栏名称），`tools/porting/convert_assets.py` 迁移方块状态、模型与纹理并输出 `docs/reference/conversion/manifest.csv`（来源、来源 SHA-1、目标）；`docs/reference/conversion/skipped.csv` 列出未生成行的注册及原因（由所属系统迁移的基类、未识别的写法）。资源转换规则：目录 `blocks/`、`items/` 改为 `block/`、`item/`；两个来源命名空间统一，JSON 中的 `parent`、`model`、`textures` 引用同步改写；方块 Java 的 `registerRenderLayer` 写为模型 `render_type`，由生成的子模型 `block/<名>__cutout` 等承载（NeoForge 的 render_type 沿 parent 链继承，方块物品的模型 parent 同步指向子模型）；来源 Blockbench 模型的 `parent` 指向不存在的原版模型（无命名空间，如 `food002`）时丢弃该 parent，1.18.2 与 1.21.1 都回退为缺失模型的父级，不影响模型自身的元素。同一目标路径来自内容不同的来源文件时报错。迁移出 6,309 个文件（约 40 MB），开发客户端加载后模型与纹理警告只剩来源本身的缺陷（3 个方块的 `missing` 纹理指向空路径，已写入台账备注）。尚未转换：配方、方块掉落表（回退掉落与来源掉落表）、标签（含 `mineable`）、进度、结构、声音文件、植物方块（`FlowerBlock`、`DoublePlantBlock`）、方块着色。

语言片段与声音片段由 `build.gradle` 的 `mergeAssetFragments` 任务合并为生成资源 `assets/yvanchuxiuzhen/lang/<语言>.json` 与 `assets/yvanchuxiuzhen/sounds.json`（命名空间取 `mod_id`），同一片段内或不同片段之间的重复键、无效 JSON、不符合上述路径约定的文件都使构建失败；`src/main/resources` 中不再放这两类文件。这样各系统并行修改时不在同一个大文件上冲突。已实测（B0 步骤 5）：两种语言的片段合并进 JAR，转义值往返不变；开发客户端读取了合并后的 `sounds.json`。2026-11-02 改为单一命名空间后重新核对了 JAR 中的输出路径，旧的带命名空间目录的片段路径使构建失败。

## 台账与检查工具

三份台账（内容映射、行为对齐、验收证据）以 CSV 存放在所属系统目录 `docs/systems/<系统>/ledger/` 下的 `content.csv`、`behavior.csv`、`evidence.csv`；Excel 工作簿改为由 CSV 生成的只读审阅视图，不再承载人工列（当前工作簿人工列均为空，切换无数据损失）。内容映射由 `tools/porting/build_ledger.py` 从 `registrations.csv`、`element_files.csv` 与[归属表](../reference/legacy/inventory/ownership_assignment.csv)同步：每条来源注册一行，列为 `registry,legacy_id,source_mod,element,source_file,line,owner,ownership,target_id,status,note`；来源列每次重写，人工列（`target_id`、`status`、`note`）按注册保留，新行的 `target_id` 取 `yvanchuxiuzhen:<来源路径>`；同名冲突在 `target_id` 中改名并在 `note` 说明。行放在 `owner` 所写单位的目录，归属改动时先改归属表再运行同步，行随之移动；没有编辑器元素的注册（声音事件、画）放在 `docs/systems/project/ledger/`，归属为“待读”，读出所属后在归属表登记。`behavior.csv`、`evidence.csv` 在所属单位出现第一条行为或证据时建立。

`tools/porting/check_ledger.py`（B0 步骤 4 建立）检查：列结构；每条来源注册恰好一行；行所在目录与归属一致；来源列与同步计算一致；`target_id` 的命名空间为 `yvanchuxiuzhen` 且同一注册表内不重复；状态只取约定值；状态高于“待处理”的行归属必须为“已读”。状态为“已注册”及以上的目标 ID 与开发服务端导出注册表的比对、转换清单覆盖对应资源，在步骤 6 建立注册表导出与资源转换工具时加入。状态值：

| 状态 | 含义 |
| --- | --- |
| 待处理 | 尚未迁移 |
| 已注册 | 身份、资源与基础属性就位，没有专属行为 |
| 行为已迁移 | 入口到消费者已实现，静态核对完成 |
| 已验收 | 按验证参考完成所需运行验证 |
| 批准调整 | 负责人批准的行为调整或合并，附决定链接 |

## 数据与存档契约

- 附件、组件、SavedData 与 payload 一律使用 Codec / StreamCodec，同一定义承担存盘与网络。
- 首个对外版本发布之前，存档格式可以直接修改，不写兼容分支；发布之后，注册 ID 与持久化格式是契约，变更必须附迁移和旧存档副本验证。
- 1.18.2 世界与玩家存档是否导入仍待负责人决定，不在本结构中预设转换器。

## 依赖治理

| 项 | 规则 |
| --- | --- |
| 准入 | 来源玩法确有需要；支持 1.21.1 NeoForge 且仍在维护；许可证允许随 Mod 分发；明确所属系统与端；记录替换代价 |
| 当前计划 | 仅 GeckoLib（MIT，已核实 4.8.3 元数据；1.21.1 线 2026-11-02 最新 4.9.3）。具体版本在首次接入时按 API 与运行检查选定 |
| 版本声明 | `gradle.properties` 固定精确版本，不用动态版本；`neoforge.mods.toml` 的范围以最低支持版本为下限、下一个大版本为上限。NeoForge 下限为 21.1.228（负责人 2026-11-02 决定，不要求最新），构建与运行验证使用 21.1.265；低于验证版本的 21.1.x 未逐一运行验证 |
| 更新时机 | NeoForge、ModDevGradle、GeckoLib 只在批次边界更新；每次更新执行构建、服务端与客户端启动检查、GameTest，并记入月度日志 |
| 首次更新 | B0 开始时把 NeoForge 与 ModDevGradle 升到当时 21.1.x / 2.0.x 最新版；此时没有内容，代价最低。已于 2026-11-02 完成：NeoForge 21.1.265、ModDevGradle 2.0.148，构建、开发服务端与开发客户端检查通过。负责人说明此后版本不必追最新，只需不低于下限 21.1.228 |
| 目标源码 | 以 ModDevGradle 生成的 `build/moddev/artifacts/neoforge-<版本>-sources.jar` 为准，已核实其中含 Minecraft 与 NeoForge 源码；FML 等库从 Gradle 缓存读取（本机 GRADLE_USER_HOME 为 `D:\Gradle`） |
| Mixin / AT | 默认不用；确需时登记目标类、原因与替代方案 |
| 可选联动 | JEI/EMI 等配方查看等联动只在负责人要求时加入 `compat`，缺失时不影响加载 |

## 验证基础设施

| 层 | 工具 | 接入时机 |
| --- | --- | --- |
| 结构守卫 | ArchUnit 1.5.1（JUnit 5.10.2 运行） | B0 步骤 3 已接入 |
| 纯规则 | JUnit 5，只测易回归公式与已复现问题 | 第一个需要的系统 |
| 世界内行为 | NeoForge GameTest（独立源集，`gameTestServer` 运行） | 第一个需要的系统建立源集，之后按风险添加 |
| 加载检查 | 开发服务端启动至 Done、客户端加载至声音引擎启动、日志无 ERROR | 每次依赖更新与批次出口 |
| 台账与文档 | `check_ledger.py`、`check_docs.py`、`inventory_legacy.py --check` | 每次提交前 |

远端已建立，CI 尚未配置；配置后在 CI 执行构建、`check` 与文档/台账检查。来源工作区不在仓库中，`inventory_legacy.py --check` 只能在有来源的机器上执行。

## Git 与会话协作

| 项 | 规则 |
| --- | --- |
| 主分支 | `main` 始终可构建，只接收通过检查的合并。直接提交到 `main` 只限仓库与文档维护；玩法、工具和构建改动走工作分支 |
| 工作分支 | `port/<批次>-<主题>`（如 `port/b0-slice`、`port/c-xiulian`），工具与文档用 `chore/<主题>`、`docs/<主题>`；分支名不带所用工具的前缀 |
| 合并 | 工作单位完成且构建、`check`、台账与文档检查通过后，用 `git merge --no-ff` 合入 `main` 保留单位边界，合并后删除工作分支 |
| worktree | 并行会话在仓库同级的 `../worktrees/rebuild-<主题>` 建立独立 worktree；一个分支同时只在一个 worktree 检出；合并后 `git worktree remove` |
| 提交 | 一个提交只含一个可说明的单位；消息为 `<类型>: <摘要>`，类型取 build、feat、fix、refactor、test、docs、chore；中断时可在工作分支提交 `wip: <摘要>` |
| 历史 | 推送到远端后不改写 `main` 历史，不 force push |
| 换行 | 由 `.gitattributes` 规定：文本 LF，Windows 批处理 CRLF，二进制不转换；不依赖各机器的 `core.autocrlf` |
| 远端 | `origin` 为 GitHub `Shane77ly/yvanchuxiuzhen-1.21.1`，公开仓库（负责人 2026-11-02 确认以公开方式推送）；`main` 与已合并结果推送到远端 |

冲突热点已按系统拆分：注册、内容表、语言片段、声音片段、台账、系统文档与日志；入口类每个系统只占一行装配。会话开始与结束、进度记录和交接更新的规则见 AGENTS.md 的“进度记录与交接”。

## 版本与发布

`mod_version` 在全量对齐前保持 0.x。发布前须完成许可证逐资产核对并确定 license 字段；发布说明从月度日志汇总。
