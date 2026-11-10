---
topic: plant
role: entry
status: partial
updated: 2026-11-10
---

# 植物

标识 `plant`，类别：系统。职责：作物、树木、野生植物的生长与收获（[系统设计](../../development/system-design.md)）。

当前阶段：D 食材与工艺供给批次部分实现。B1 已登记的方块433、物品470仅表示内容身份、资源和基础属性；已接入水稻001与糯稻002各17条、桑苗/桑叶4条，共38条行为注册，两个成熟地物完成类型及configured/placed装配。完整构建及检查代码编译通过；最新内置 `runSmokeClient -PsmokeOnly=fulucooking` 166项INFO、0 FAIL、DONE，覆盖本批受控播种、骨粉、随机刻、loot、配方加载、Tooltip和已有D类场景。客户端四维度正常保存退出、ERROR/FATAL0。目标版本自动场景不代表自然供给链、自然生成、完整重载或来源实机验收。水稻自然投放仍待World两个维度与三个群系，桑树自然地物 `mulberry_02` 尚未实现，完整植物系统与自然供给链均未完成。

- [架构与来源规则](architecture.md)：本批入口、状态归属、保留异常和自然投放边界。
- 归属范围839个元素，其中574个已读；这是来源阅读范围，不是玩法验收数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：两批40条及全系统注册身份和迁移状态。
- [来源证据](ledger/source-evidence.csv)与[来源指纹](ledger/source-fingerprints.csv)：按来源路径登记已读规则、分支范围、目标实现和SHA256。
- [系统导航](../README.md)记录源码引用规则与各级证据范围。

## 公开操作

本批没有供其他系统调用的玩家档案查询或写操作，也没有跨系统可变状态接口。Plant唯一写入本族世界方块及其方块状态；自然区块的投放装配由World单向引用Plant地物ID。食材 `shicai_001`、`shicai_00102` 和桑葚仍归Cooking，Craft按注册ID消费种子/桑叶或初始化蚕，不读取或改写Plant内部状态。

| 操作 | 调用与数据边界 |
| --- | --- |
| `CerealSeedItem.useOn` | 原生种子入口，只写服务端八阶段作物方块并扣本次种子；创造模式消耗恢复沿用原生外层 |
| `CerealGrowth.randomTick / bonemeal` | 本族内部随机刻、计划刻与骨粉入口；只读相邻方块状态、改写本族上下两格，不保存玩家字段 |
| `CerealFeature.place` | Plant拥有的001/002成熟地物规则，configured/placed数据引用稳定ID；World尚未安装群系modifier |
| `MulberryGrowth.grow / afterServerTick / stopped` | 桑苗随机刻、计划刻和骨粉共用生长过程；模板后补叶/木、首个服务端Post写根、停服清临时队列 |
| `MulberryLeavesBlock.onDestroyedByPlayer` | 原生破坏回调后按ID构造Craft拥有的蚕实体，初始化仍走实体原生finalizeSpawn；不引用Craft私有实现、不反写其持久状态 |

对照 `typed_field_accesses.csv`，`Shicai001`、`ShicaiPlantCuishujilv`、`Mulberrysapling`、`Mulberryleaves` 对应来源类没有玩家/世界能力字段访问行；两个实现包完整方法阅读也未发现外部持久字段读写。本结论仅覆盖本批，不替代其余植物的字段核对。共享种植与骨粉过程已迁移Shicai001及Shicai002分支，其他编号不因该过程存在而计作实现。

## 剩余范围与验证

本批完整构建及内置客户端自动场景已由主会话执行。160项INFO、0 FAIL、DONE；包括计划刻可重复等待的受控随机刻、一次水稻骨粉概率失败、桑苗遮挡放置后等待12刻再调用原生骨粉且保持遮挡不生长、四维度保存退出。Tooltip截图通过指定坐标绘制原生Tooltip并已查看中文/金色，不是鼠标悬停检查。本批专服启动至Done后原生stop并正常保存四维度；未执行自然区块生成、完整重登/区块保存周期、来源1.18.2实机和两版本对照。水稻上半格骨粉不归一下半格、无空间预检、桑苗首个Post根覆盖等来源行为保持，自动场景未覆盖最终自然布局。

水稻地物只允许 `dimensionrenjian`、`dimensionyaohun` 与 `biomeautumn_01/02/03`；World未注册前不安装缺引用的modifier、不替换为主世界。桑树 `mulberry_02` 地物仍待实现及World装配。其余作物、树木、野生植物、完整食材与Craft自然供给继续待移植；创造物品、give或/place不能替代生存自然获得验收。

批次变化与验证边界见[月度日志](logs/2026-11.md)，[变更索引](changes.md)只作导航。

## 糯稻002续批（2026-11-10）

001水稻与002糯稻用 `CerealCrop.SHICAI_001` / `CerealCrop.SHICAI_002` 明确绑定来源编号，`PlantSetup` 每条编号工厂可直接核对注册ID。相同的已核生长/阶段机制共用 `CerealGrowth` 和三个阶段类；两者属性、炖菜效果、骨粉属性复制差异仍独立保留。新增糯稻17条行为注册及1条成熟地物类型装配；只接其来源两群系的待装配规则，不因共享类覆盖其他编号。完整`build compileSmokeJava`通过（4m31s，含既有test/check与ArchUnit）；标准`convert_data.py`输出1707项，002两份configured/placed JSON各追踪锁定来源SHA-1。内置`runSmokeClient -PsmokeOnly=fulucooking`共166项INFO、0 FAIL、DONE，客户端正常保存四维度退出、ERROR/FATAL0；本批新增6项均通过。专服启动至Done后通过原生halt(false)正常保存四维度，Gradle BUILD SUCCESSFUL、游戏日志ERROR/FATAL0；控制台stdin已关闭，未收到stop命令；动态加载既有停服agent产生JDK警告，PowerShell包装返回1，Gradle原生返回0。证据为`tmp/shicai002-{build.log,smoke.log,report.txt,client.log,server.log}`。新增检查覆盖002原生生存播种扣种及泥土拒绝、受控原生randomTick干旱枯萎与对应种子掉落、原生骨粉有效目标扣一及本次结果、成熟上下半原生loot，以及真实加载RecipeManager的四种子配方匹配/assemble产一糯米；配方检查没有模拟玩家工作台取出。完整自然成长、上下半骨粉异常布局、自然新区域、完整保存重登、来源1.18.2版本实机、多人及两版本对照未执行。

002种子四份转一份糯米的普通无序配方按食材加工职责归Cooking，目标既有JSON无改动；001/002种植与生长只写Plant方块状态，`typed_field_accesses.csv`未见本族外部持久字段读写。其余作物和完整自然供给仍未闭合。
