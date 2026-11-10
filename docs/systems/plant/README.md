---
topic: plant
role: entry
status: partial
updated: 2026-11-10
---

# 植物

标识 `plant`，类别：系统。职责：作物、树木、野生植物的生长与收获（[系统设计](../../development/system-design.md)）。

当前阶段：D 食材与工艺供给部分实现。此前已接入水稻001、糯稻002、桑树及004–008、011–033共28种作物；当前批次新增009/010果树、034–039共23个藤本阶段和6种子，并为03904接入九槽葫芦库存。Plant内容台账现有384条“行为已迁移”；30个成熟地物完成类型/configured/placed注册，其中果树009/010的World群系投放尚未安装。`content/plant`当前有434个方块与474个物品定义；这些数目不代表全系统玩法验收完成。

当前批次`build compileSmokeJava`与既有`runSmokeClient -PsmokeOnly=fulucooking`已通过；冒烟报告301项INFO、0 FAIL、DONE，客户端四维度正常保存退出且ERROR/FATAL0。专服启动至Done后收到原生stop并正常保存退出，Gradle BUILD SUCCESSFUL、游戏日志ERROR/FATAL0。资源审计确认179份模型/贴图/Geo/动画引用闭包均有目标文件和manifest，154份真实来源文件SHA-1一致；完整自然生成、树苗完整生长、葫芦动画全程播放、完整退出重登、来源1.18.2版本实机、多人和两版本对照仍未验收，见[月志](logs/2026-11.md)。

- [架构与来源规则](architecture.md)：逐编号身份、实现族、工厂对应、生长与掉落差异、公开API和自然投放边界。
- 归属范围842个元素，其中608个已读；这是来源阅读范围，非功能验收数，见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册身份、目标ID、行为迁移和地物装配状态。
- [来源证据](ledger/source-evidence.csv)与[来源指纹](ledger/source-fingerprints.csv)：来源路径、已读分支范围、目标实现和SHA256。
- [变更索引](changes.md)、[月度日志](logs/2026-11.md)与[系统导航](../README.md)：按批次查证验证范围与源码引用规则。

## 公开操作

Plant拥有本族作物方块和状态，Cooking保留可食物品身份、FoodProperties与Tooltip。跨系统播种统一调用 `plant.api.Planting`，不直接引用Plant内部实现；自然区块投放由World单向引用Plant地物ID。Craft按注册ID消费种子/桑叶或生成蚕，不读取或改写Plant内部状态。

| 操作 | 调用与数据边界 |
| --- | --- |
| `Planting.lotus` | Cooking莲子00801委托播种；Plant修改世界方块并扣本次手持栈，保留来源耕地只扣不种分支 |
| `Planting.garlic` | Cooking大蒜01502委托耕地播种；物品食用身份仍由Cooking拥有 |
| `Planting.shicai028 / shicai029 / shicai030 / shicai031 / shicai032 / shicai033` | 六种可食本体委托Plant播种；029/030/031的独立种子另走本系统物品入口 |
| `CerealSeedItem / GrainSeedItem / VegetableSeedItem / ProduceItem.useOn` | Plant拥有的非食播种物入口；原生创造模式数量恢复沿用外层 |
| `CerealGrowth / GrainGrowingBlock / VegetableGrowth / ProduceGrowth` | 系统内部随机刻、计划刻与骨粉；各族只修改自身世界状态 |
| `LotusRootBlock.attack / onDestroyedByPlayer` | 只由Plant写玩家自由NBT键`caiou`；记录本次挖掘数量，破坏后独立掉藕并清零，不提供跨系统写接口 |
| `CropFeature.place` | 28种已接成熟地物共有的两维度限制；逐编号configured/placed保留来源参数，群系modifier待World |
| `MulberryGrowth.grow / afterServerTick / stopped` | 桑苗生长、模板后补叶/木、首个服务端Post写根与停服清临时队列 |
| `MulberryLeavesBlock.onDestroyedByPlayer` | 原生破坏回调后按注册ID构造Craft蚕实体，初始化仍走原生finalizeSpawn |

逐族已对照 `typed_field_accesses.csv`，已接作物阶段与播种入口没有强类型外部持久字段读写。莲藕`caiou`属于实体自由NBT，不在该强类型表内，已另按开始挖掘、玩家破坏与共享过程调用链核对。服务端负责采藕玩法真值；这不表示其他尚未实现植物的持久状态已核完。

## 剩余范围与验证

本批静态已核生长、湿度、光照、骨粉、重复采摘、lotus采藕及来源异常，运行证据以本批月志为准。成熟作物地物仅允许 `dimensionrenjian` / `dimensionyaohun`，群系集合各编号独立记录；不安装指向缺失World内容的modifier。桑树自然地物 `mulberry_02`、009/010果树、034–039藤蔓及其他树木、野生植物继续移植。`003`为白面物品，不存在对应003Plant，不能按连续编号补造作物。

原生NBT保存读回、give和/place分别只证明对应入口，不代替完整退出重登或生存自然获取验收。烹饪、Craft及World的完整自然供给仍未闭合，整个Plant系统不登记完成。
