---
topic: plant
role: entry
status: partial
updated: 2026-11-11
---

# 植物

标识 `plant`，类别：系统。职责：作物、树木、野生植物的生长与收获（[系统设计](../../development/system-design.md)）。

2026-11-11 World依赖批次接入另22种植物地物（16种普通配置型植物、六种树木模板）及Rushes/Ricciafluitans的放置含水回调；同批World接入89种独立地物及207份群系内联生成资源。非植物环境地物与温泉依据实际规则归World，植物内容归属保持Plant；生成执行按注册ID读取方块，不产生系统循环依赖。本批客户端343项INFO、0 FAIL、DONE；专服七个受控标记全部通过并原生stop保存四维度；完整构建通过，后续地形Mixin接线另行验证。具体规则与验证边界见[正文](architecture.md#普通植物地物与水草回调2026-11-11)、[本月日志](logs/2026-11.md)及[世界月志](../world/logs/2026-11.md)。

当前阶段：D 食材与工艺供给部分实现。已接入水稻001、糯稻002、桑树及004–008、011–033共28种作物；009/010果树及034–039藤本库存批次已提交`789c9c23`。本批继续接入040–045菌类、046–056豆科、05701芝麻双株、棉麻、霓裳草、桃花、17种普通树苗、47种野生植物Feature、20种树木Feature、四种树叶粒子行为及枫脂方块；逐项身份与实现映射见架构正文。新增67个Feature保持“已注册”，尚未装配来源群系/维度的自然投放；霓裳草06召引妖兽仍待Creature。Plant台账共1,066项，确切状态见[内容台账](ledger/content.csv)。`content/plant`当前有435个方块与475个物品定义；注册数不代表全系统玩法验收完成。

上一批“野生植物与自然树木Feature扩展”的完整`build compileSmokeJava`通过，最终`runSmokeClient -PsmokeOnly=fulucooking`为343项INFO、0 FAIL、DONE，客户端四维度正常保存退出且ERROR/FATAL0；该批专服启动至Done并导出注册表，未取得原生stop/保存退出证据。上一批证据见[本月日志](logs/2026-11.md)、`tmp/plant-world-features-final-{client.log,report.txt}`；本批World依赖验证另记，不能混用两批专服结论。受控检查不覆盖完整树木长成、自然生成、新区域自然成长、完整存档重登、来源1.18.2实机、多人、完整视觉及两版本对照。目标群系与`dimensionrenjian`、`dimensionyaohun`缺失，Feature注册不代表自然供给闭合；霓裳草06召引妖兽仍待Creature。

- [架构与来源规则](architecture.md)：逐编号身份、实现族、工厂对应、生长与掉落差异、公开API和自然投放边界。
- 归属范围812个元素，其中649个已读；这是来源阅读范围，不是功能验收数，见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册身份、目标ID、行为迁移和地物装配状态。
- [来源证据](ledger/source-evidence.csv)与[来源指纹](ledger/source-fingerprints.csv)：来源路径、已读分支范围、目标实现和SHA256。
- [变更索引](changes.md)、[月度日志](logs/2026-11.md)与[系统导航](../README.md)：按批次查证验证范围与源码引用规则。

## 公开操作

Plant拥有本族作物方块和状态，Cooking保留可食物品身份、FoodProperties与Tooltip。跨系统播种统一调用 `plant.api.Planting`，不直接引用Plant内部实现；自然区块投放由World单向引用Plant地物ID。Craft按注册ID消费种子/桑叶或生成蚕，不读取或改写Plant内部状态。

| 操作 | 调用与数据边界 |
| --- | --- |
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
