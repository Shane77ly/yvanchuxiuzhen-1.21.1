---
topic: engineering
role: architecture
status: current
updated: 2026-11-02
---

# 工程结构与长期维护

状态：负责人 2026-11-02 批准（main 提交 `3e0585c` 版本）；标注“待验证”的做法由 B0 实测后写回并移除标注，批准不含存档导入、界面复刻程度、license 与 Q01–Q14。玩法所有权与调用方向仍以[整体架构](architecture.md)为正文，本文规定代码与工程如何承载它们。文中“已核实”指本机源码、制品或运行结果；其余为方案或“待验证”。

## 设计约束

| 约束 | 来源 | 对工程的要求 |
| --- | --- | --- |
| 规模 | 4,867 条注册（2,243 物品、1,180 方块、271 实体类型、315 粒子、150 声音），16,282 个来源资源文件，19 个系统 | 不能一条内容一个手写注册段；资源必须可批量、可复现地转换 |
| 长期多会话推进 | 负责人以新会话分批移植 | 冲突热点要拆开；每次会话可从仓库状态恢复；规则要能自动检查 |
| 目标平台变化 | 已核实：1.21.1 的附魔为数据驱动 `record`，群系与地物在 worldgen 数据中，燃料与堆肥为 NeoForge 数据映射，数据目录改为单数（`recipe`、`loot_table`、`tags/item`），配方产物使用 `id` | 来源中以 Java 注册的 85 个附魔、68 个群系和 220 个地物需要“代码注册 → 数据定义”转换 |
| 依赖生态 | GeckoLib 是唯一已确认的必需库；NeoForge 21.1.x 持续发布补丁（2026-11-02 maven 最新 21.1.265） | 依赖少而固定；版本更新有固定时机和检查 |
| 邻近工程经验 | 其 `init` 下物品/方块/实体集中注册类分别为 2,852 / 1,550 / 1,237 行 | 不建立全项目集中注册类 |

## 工程形态

一个 Gradle 工程、一个 Mod、一个发布 JAR。多 Gradle 子工程能在编译期强制边界，但 ModDevGradle 需要逐工程配置并合并产物，运行配置与 IDE 同步也更复杂；对单一发布物收益不足。边界改由自动检查保证（见下节）。

```text
src/main/java/net/yvanchuxiuzhen/
  YvanchuxiuzhenMod.java        公共入口：按依赖顺序调用各系统的 <名称>Setup.register
  YvanchuxiuzhenClient.java     @Mod(value = MOD_ID, dist = Dist.CLIENT) 客户端入口：调用各系统的 <名称>ClientSetup
  platform/                     至少两个系统实际共用的平台适配：内容表加载、注册辅助、payload 登记、codec 工具
  flow/                         跨系统具体流程（闭关、击杀结算、装备重算等）
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
  assets/yvanchuxiuzhen/  assets/yvanchuxitong/
  data/yvanchuxiuzhen/    data/yvanchuxitong/
  content/<系统>/               静态内容表（见“注册与内容表”）
src/main/lang/<命名空间>/<语言>/<系统>.json   语言片段，构建时合并
src/gametest/java/              GameTest，只进入开发运行，不进入发布 JAR（待验证）
tools/porting/                  来源索引、台账检查、资源转换
```

- 本文的“系统”指[整体架构](architecture.md)“系统所有权”中的 19 个玩法领域，即拥有自身数据与规则的所有权单位；不是 ECS 式运行时系统，也没有“系统注册”框架。
- `<名称>Setup` 只在 Mod 构造时调用一次：登记本系统的 `DeferredRegister`、payload 与事件监听，不持有状态，不实现接口，运行期不再被调用。例如 `AlchemySetup.register(IEventBus modBus)`。装配集中在入口显式调用，使启动顺序与注册范围在一处可见；`DeferredRegister` 本身也需要显式 `register(modBus)`。
- 系统目录名：cultivation、gongfa、attributes、combat、abilities、equipment、materials、alchemy、refining、talismans、formations、creatures、npc、economy、quests、world、life、storage、knowledge。简单系统可以平铺，不预建空子包。
- 客户端代码放在所属系统的 `client` 子包，由客户端入口装配；已核实 FML 4.0.42 的 `@Mod` 提供 `dist` 属性。`@EventBusSubscriber` 的 `bus` 属性在该版本已标记弃用，按事件类型自动选择总线。
- `platform` 替代原方案的 `shared`：只放平台适配，不放玩法类型；玩法共享值类型放在所有者的 `api`。

## 边界与自动检查

规则写进构建才有约束力。采用 ArchUnit（测试依赖，只检查编译后的类）在 `gradlew check` 中执行：

1. `system.X` 只能依赖其他系统的 `api` 包；`flow` 同样只用 `api`。
2. `client` 包与客户端入口之外的类不得依赖任何 `client` 包或 `net.minecraft.client`。
3. `rule` 包不得依赖 `net.minecraft`、`net.neoforged`。
4. `platform` 不得依赖 `system` 或 `flow`。

这些是结构守卫，不属于“为每个类写测试”；违反即构建失败。初次接入与规则清单在 B0 完成。

## 注册与内容表

同质内容（普通材料、装饰方块、家具、普通物品等）由所属系统的静态内容表声明，在 Mod 构造时用 Codec 解码并登记到该系统的 `DeferredRegister`：

- 每行包含命名空间、路径、工厂类型和必要属性（堆叠、稀有度、方块属性、创造栏）。工厂类型由所属系统的代码提供，未知类型在启动时报出文件与条目，不回退为普通物品。
- 有专属行为的物品、方块、实体在所属系统用 Java 显式注册；它们仍在台账中登记。
- 每个系统按注册表类型持有自己的 `DeferredRegister`，`yvanchuxitong` 内容使用该命名空间的注册器；不存在全项目集中注册类。
- 代码需要引用的条目通过所属系统 `api` 暴露的持有者访问。

内容表格式、启动耗时及两个命名空间并存的实际行为在 B0 验证后定稿。

## 资源转换管线

来源资源以可复现的转换工具迁入，而不是手工复制或用 datagen 重画：

| 资源 | 处理 |
| --- | --- |
| 模型、方块状态、纹理、粒子定义、声音文件 | 复制并校验引用；路径按目标原版目录 |
| 配方、掉落表、标签、进度、结构 | 改单数目录；配方产物改用 `id`/`count`；物品 NBT 改为组件 |
| 代码注册的附魔、群系、地物 | 依来源 Java 的构造参数生成目标数据定义；附魔效果按目标效果组件或战斗系统读取等级重写 |
| 燃料、堆肥等来源代码表 | 生成 NeoForge 数据映射 |
| 语言文件 | 拆出机器配置交给所属系统定义；显示文本按系统拆成语言片段 |
| `sounds.json` | 按系统片段合并 |
| GeckoLib 几何与动画 | 文件后缀与 `geo`/`animations` 目录约定沿用（已核实 GeckoLib 4.8.3 的加载器识别 `.geo.json`、`.animation.json` 与 `animations`）；控制器代码按 GeckoLib 4 重写 |
| 结构 NBT | 旧 DataVersion 由游戏加载时升级还是预先升级，B1 实测后决定 |

转换工具每次输出一份清单（来源文件、哈希、目标文件），台账检查据此确认没有遗漏。系统验收前可以重跑转换；验收后转换结果成为手工维护的正文，清单保留来源。只有转换无法表达的资源才使用 datagen。

语言片段与 `sounds.json` 片段由 Gradle 任务合并到生成资源目录，重复键使构建失败。这样各系统并行修改时不在同一个大文件上冲突。

## 台账与检查工具

三份台账（内容映射、行为对齐、验收证据）以 CSV 存放在所属系统目录 `docs/systems/<系统>/ledger/` 下的 `content.csv`、`behavior.csv`、`evidence.csv`；Excel 工作簿改为由 CSV 生成的只读审阅视图，不再承载人工列（当前工作簿人工列均为空，切换无数据损失）。内容映射的初始行由 `registrations.csv` 与候选归属生成，每条来源注册一行；归属尚未确认的行先放在 `docs/systems/project/ledger/`，确认后移到所属系统。

`tools/porting/check_ledger.py`（B0 建立）检查：每条来源注册都有行；状态只取约定值；状态为“已注册”及以上的目标 ID 出现在开发服务端导出的注册表中；转换清单覆盖对应资源。状态值：

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
| 结构守卫 | ArchUnit | B0 |
| 纯规则 | JUnit 5，只测易回归公式与已复现问题 | 第一个需要的系统 |
| 世界内行为 | NeoForge GameTest（独立源集，`gameTestServer` 运行） | B0 建立一个，之后按风险添加 |
| 加载检查 | 开发服务端启动至 Done、客户端加载至声音引擎启动、日志无 ERROR | 每次依赖更新与批次出口 |
| 台账与文档 | `check_ledger.py`、`check_docs.py`、`inventory_legacy.py --check` | 每次提交前 |

远端已建立，CI 尚未配置；配置后在 CI 执行构建、`check` 与文档/台账检查。来源工作区不在仓库中，`inventory_legacy.py --check` 只能在有来源的机器上执行。

## Git 与会话协作

| 项 | 规则 |
| --- | --- |
| 主分支 | `main` 始终可构建，只接收通过检查的合并。直接提交到 `main` 只限仓库与文档维护；玩法、工具和构建改动走工作分支 |
| 工作分支 | `port/<批次>-<主题>`（如 `port/b0-slice`、`port/c-cultivation`），工具与文档用 `chore/<主题>`、`docs/<主题>`；分支名不带所用工具的前缀 |
| 合并 | 工作单位完成且构建、`check`、台账与文档检查通过后，用 `git merge --no-ff` 合入 `main` 保留单位边界，合并后删除工作分支 |
| worktree | 并行会话在仓库同级的 `../worktrees/rebuild-<主题>` 建立独立 worktree；一个分支同时只在一个 worktree 检出；合并后 `git worktree remove` |
| 提交 | 一个提交只含一个可说明的单位；消息为 `<类型>: <摘要>`，类型取 build、feat、fix、refactor、test、docs、chore；中断时可在工作分支提交 `wip: <摘要>` |
| 历史 | 推送到远端后不改写 `main` 历史，不 force push |
| 换行 | 由 `.gitattributes` 规定：文本 LF，Windows 批处理 CRLF，二进制不转换；不依赖各机器的 `core.autocrlf` |
| 远端 | `origin` 为 GitHub `Shane77ly/yvanchuxiuzhen-1.21.1`，公开仓库（负责人 2026-11-02 确认以公开方式推送）；`main` 与已合并结果推送到远端 |

冲突热点已按系统拆分：注册、内容表、语言片段、声音片段、台账、系统文档与日志；入口类每个系统只占一行装配。会话开始与结束、进度记录和交接更新的规则见 AGENTS.md 的“进度记录与交接”。

## 版本与发布

`mod_version` 在全量对齐前保持 0.x。发布前须完成许可证逐资产核对并确定 license 字段；发布说明从月度日志汇总。
