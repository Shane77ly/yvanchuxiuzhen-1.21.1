---
topic: plant
role: entry
status: partial
updated: 2026-11-18
---

# 植物

2026-11-18修复八种野生植物ground中九处Mod土壤引用缺少命名空间的问题，保留来源的原版土壤、列表顺序与双格区别。生产转换器和八行内容已更正，标准转换、编译与原生存活/放置/地物必要单项通过，81 INFO、0 FAIL、DONE、全维度保存且Gradle退出0。阴魂草与尸草仍依赖未接收的无归道自然消费者。规则与未验边界见[土壤正文](architecture.md#野生植物土壤命名空间2026-11-18)和[本月记录](logs/2026-11.md#2026-11-18-八种野生植物土壤引用)。

2026-11-18接回柳树垂枝的原位替换入口，保留手持自身物品时向相邻格放置的来源区别；自然柳树装饰器与柳苗成树模板已在活动集成存在。标准转换、生产/Smoke编译及一次原生放置单项通过：11 INFO、0 FAIL、DONE、所有维度保存且Gradle退出0。真实保存重进、自然新区块/成树获取、多人、专服及来源版本实机仍未验；规则与验证边界见[垂枝正文](architecture.md#柳树垂枝原位替换2026-11-18)和[本月记录](logs/2026-11.md#2026-11-18-柳树垂枝原位替换)。

2026-11-18补齐植物物品扩展的52条燃料与43条堆肥数据，94个来源元素逐项核对，共93个实际物品；另一个来源元素未启用行为。标准转换、编译及原生机器必要单项通过，报告30 INFO、0 FAIL、DONE，所有维度正常保存且Gradle退出0。71条Plant物品台账仅补规则及有限验证说明，未升级整项状态；Cooking、Material、Decor物品继续保留各自注册所有者。规则、加载器数据映射路径及未验边界见[物品扩展正文](architecture.md#植物物品扩展堆肥与燃料2026-11-18)和[本月记录](logs/2026-11.md#2026-11-18-植物物品扩展堆肥与燃料)。

2026-11-18按活动集成复核12个菌类/豆科/芝麻成熟Feature：已有显式注册、两道维度门、配置/投放资源、来源manifest与27处原群系挂载，台账从待处理回填为已注册；没有新增生产实现或运行验收。规则与候选边界见[成熟地物正文](architecture.md#成熟地物转换与证据边界)和[本月记录](logs/2026-11.md#2026-11-18-成熟作物地物活动基线回填)。

2026-11-16补齐高草`biggrass`→`biggrassyincang`完整碰撞/30刻恢复族、九槽实体/六向能力和Geo物品渲染，内容定义现为436方块/476物品。妖魂道`biometiger_02`的既有自然入口直接复用；本批生产实现和转换输入已接；标准转换隔离输出、集中编译和六架构检查已通过，高草定向运行仍未执行。规则及验证边界见[高草正文](architecture.md#高草碰撞与动画恢复2026-11-16)与[本月记录](logs/2026-11.md#2026-11-16-高草完整生命周期)。

标识 `plant`，类别：系统。职责：作物、树木、野生植物的生长与收获（[系统设计](../../development/system-design.md)）。

2026-11-11 World依赖批次接入另22种植物地物（16种普通配置型植物、六种树木模板）及Rushes/Ricciafluitans的放置含水回调；同批World接入89种独立地物及207份群系内联生成资源。非植物环境地物与温泉依据实际规则归World，植物内容归属保持Plant；生成执行按注册ID读取方块，不产生系统循环依赖。本批客户端343项INFO、0 FAIL、DONE；专服七个受控标记全部通过并原生stop保存四维度；完整构建通过，后续地形Mixin接线另行验证。具体规则与验证边界见[正文](architecture.md#普通植物地物与水草回调2026-11-11)、[本月日志](logs/2026-11.md)及[世界月志](../world/logs/2026-11.md)。

当前阶段：D 食材与工艺供给部分实现。已接入水稻001、糯稻002、桑树及004–008、011–033共28种作物；009/010果树及034–039藤本库存批次已提交`789c9c23`。本批继续接入040–045菌类、046–056豆科、05701芝麻双株、棉麻、霓裳草、桃花、17种普通树苗、47种野生植物Feature、20种树木Feature、四种树叶粒子行为及枫脂方块；逐项身份与实现映射见架构正文。新增67个Feature保持“已注册”，尚未装配来源群系/维度的自然投放；霓裳草06召引已于2026-11-13接回同步事件，静态核对及荫翠符必要单项通过，运行边界见[本批正文](architecture.md#荫翠符催熟与霓裳草开花2026-11-13)。Plant台账共1,066项，确切状态见[内容台账](ledger/content.csv)。`content/plant`当前有436个方块与476个物品定义；注册数不代表全系统玩法验收完成。

上一批“野生植物与自然树木Feature扩展”的完整`build compileSmokeJava`通过，最终`runSmokeClient -PsmokeOnly=fulucooking`为343项INFO、0 FAIL、DONE，客户端四维度正常保存退出且ERROR/FATAL0；该批专服启动至Done并导出注册表，未取得原生stop/保存退出证据。上一批证据见[本月日志](logs/2026-11.md)、`tmp/plant-world-features-final-{client.log,report.txt}`；本批World依赖验证另记，不能混用两批专服结论。受控检查不覆盖完整树木长成、自然生成、新区域自然成长、完整存档重登、来源1.18.2实机、多人、完整视觉及两版本对照。目标群系与`dimensionrenjian`、`dimensionyaohun`缺失，Feature注册不代表自然供给闭合；霓裳草06召引已于2026-11-13接回同步事件，静态核对及荫翠符必要单项通过，运行边界见[本批正文](architecture.md#荫翠符催熟与霓裳草开花2026-11-13)。

- [架构与来源规则](architecture.md)：逐编号身份、实现族、工厂对应、生长与掉落差异、公开API和自然投放边界。
- 来源范围与逐元素阅读状态见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)；来源已读不代表功能验收，本批94条扩展的阅读状态已按独立增量接收。
- [内容台账](ledger/content.csv)：来源注册身份、目标ID、行为迁移和地物装配状态。
- [来源证据](ledger/source-evidence.csv)与[来源指纹](ledger/source-fingerprints.csv)：来源路径、已读分支范围、目标实现和SHA256。
- [变更索引](changes.md)、[月度日志](logs/2026-11.md)与[系统导航](../README.md)：按批次查证验证范围与源码引用规则。

## 公开操作

Plant拥有本族作物方块和状态，Cooking保留可食物品身份、FoodProperties与Tooltip。跨系统播种统一调用 `plant.api.Planting`，不直接引用Plant内部实现；自然区块投放由World单向引用Plant地物ID。Craft按注册ID消费种子/桑叶或生成蚕，不读取或改写Plant内部状态。

| 操作 | 调用与数据边界 |
| --- | --- |
| `PlantRipening.ripen` | fulu逐格催熟委托；Plant按来源顺序推进本系统方块或调用原生骨粉，不接受客户端快照回写 |
| `WildPlantCreatureEvent` | 魔眼玩家破坏或血桃苗计划刻同步提交服务端坐标与生成种类，魔眼附玩家目标；Creature独占构造、初始化与加入世界 |
| `NichangGrassBloomEvent` | Plant在来源开花顺序的对应位置同步发布服务端坐标；Creature订阅并拥有妖兽召引，不引入Plant→Creature依赖 |
| `Planting.lotus` | Cooking莲子00801委托播种；Plant修改世界方块并扣本次手持栈，保留来源耕地只扣不种分支 |
| `Planting.garlic` | Cooking大蒜01502委托耕地播种；物品食用身份仍由Cooking拥有 |
| `Planting.shicai028 / shicai029 / shicai030 / shicai031 / shicai032 / shicai033` | 六种食材本体委托Plant播种；029/030/031的独立种子另走本系统物品入口 |
| `Planting.shicai040..shicai056` | Cooking食材本体按来源编号委托Plant播种；目标入口分别保留木生菌、地生菌、花型豆科及支架豆科差异 |
| `CerealSeedItem / GrainSeedItem / VegetableSeedItem / ProduceItem.useOn` | Plant拥有的非食播种物入口；原生创造模式数量恢复沿用外层 |
| `CerealGrowth / GrainGrowingBlock / VegetableGrowth / ProduceGrowth` | 系统内部随机刻、计划刻与骨粉；各族只修改自身世界状态 |
| `LotusRootBlock.attack / onDestroyedByPlayer` | 只由Plant写玩家自由NBT键`caiou`；记录本次挖掘数量，破坏后独立掉藕并清零，不提供跨系统写接口 |
| `CropFeature.place` | 28种已接成熟地物共有的两维度限制；逐编号configured/placed保留来源参数，群系modifier待World |
| `MulberryGrowth.grow / afterServerTick / stopped` | 桑苗生长、模板后补叶/木、首个服务端Post写根与停服清临时队列 |
| `MulberryLeavesBlock.onDestroyedByPlayer` | 原生破坏回调后按注册ID构造Craft蚕实体，初始化仍走原生finalizeSpawn |

逐族已对照 `typed_field_accesses.csv`，已接作物阶段与播种入口没有强类型外部持久字段读写。莲藕`caiou`属于实体自由NBT，不在该强类型表内，已另按开始挖掘、玩家破坏与共享过程调用链核对。服务端负责采藕玩法真值；这不表示其他尚未实现植物的持久状态已核完。

## 剩余范围与验证

本批静态已核生长、湿度、光照、骨粉、重复采摘及来源异常，运行证据以本批月志为准。成熟作物地物仅允许 `dimensionrenjian` / `dimensionyaohun`，群系集合各编号独立记录；不安装指向缺失World内容的modifier。当前继续核对非编号树苗、棉麻、霓裳草和桃花，具体实现及运行状态见架构正文与月志。桑树自然地物及所需World维度/群系仍未闭合。`003`为白面物品，不存在对应003Plant，不能按连续编号补造作物。

原生NBT保存读回、give和/place分别只证明对应入口，不代替完整退出重登或生存自然获取验收。烹饪、Craft及World的完整自然供给仍未闭合，整个Plant系统不登记完成。

2026-11-16补齐27种反应野外方块的碰触、计划刻、攻击与玩家破坏回调，按五种来源基类装配；魔眼/血桃苗通过公开同步事件交Creature生成。2026-11-17精确源码核对确认1.18.2基类会将随机刻委派给tick，Q218原先的自然入口结论已更正；五种反应花型方块补回该委派，菌类既有桥接保持。新修复已通过集中编译和reactiveplants必要单项；仅覆盖正式交互及受控随机/计划刻，完整自然供给与真实重进仍未验。规则和分批验证边界见[反应植物正文](architecture.md#反应野外方块2026-11-16)与[本月更正记录](logs/2026-11.md#2026-11-17-反应植物随机刻委派更正)。
