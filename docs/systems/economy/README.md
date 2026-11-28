---
topic: economy
role: entry
status: partial
updated: 2026-11-28
---

# 经济

阶段 4 续批（2026-11-28）修正 Q72 生存商铺限购、Q73 图纸重量折价、Q93 商店以物易物预检与精确扣料、Q94 材料商店高档随机可达与 Q96 拍卖托管字段对齐，并经 `CommerceCheck` 烟测验证（见[经济月志](logs/2026-11.md#阶段-4-续批q72q73q93q94q96-商铺估价商店概率与拍卖托管修复)）。

已完成商贸会话与方块状态去套壳：`CommerceData.SESSION` 改为强类型 `CommerceSession`，`EconomyBlockEntity` 改持 `EconomyBlockState`（含商店与拍卖子状态，`Codec` 字段上限内拆分子记录），见[去套壳记录](logs/2026-11.md#阶段-5c经济会话与方块状态去套壳)。

阶段 4 功能分支已修正 Q05 商店一级灵石找零；修前复现与本批验证边界见[经济月志](logs/2026-11.md#阶段-4-首批商店找零)。

标识 `economy`，类别：系统。职责：灵石货币、银行、商店、现场拍卖（[系统设计](../../development/system-design.md)）。

九种灵石矿脉具有人间道在内的四维度门和全群系地物入口。原矿方块已接来源空战利品回退，衔接既有右键开封；九档预置矿均已由生存玩家实采及开封；自然矿点整链未验。见[原矿规则](architecture.md#九级灵石原矿开封2026-11-15)与[接线记录](logs/2026-11.md#2026-11-21-九档灵石矿自然生产接线)。

B1 已登记本单位不依赖新代码类型的内容身份与资源（方块12、物品40），只表示身份、资源与基础属性就位。

2026-11-14 R13银行账户与高级通宝玉单步入口已通过必要客户端单项：127条报告检查无失败，覆盖认主、登录、九级存取、关闭、死亡和真实退出重进；编译、6项架构测试、文档和台账检查通过。验证范围与截图见[经济月志](logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。专服、1.18.2版本实机、两版本对照和多人验收未执行。该单项只验证银行账户包；本日后续交易包的实现和未验证范围见下段。具体来源规则和银行包边界见[银行正文](architecture.md#银行与高级通宝玉单步入口2026-11-14)。

2026-11-14 R13交易包已接入天地银行方块四选项请求、十二格商品分页与购买、两种收购、玩家交易方块、拍卖令牌/台/椅及七页原生菜单，并补齐九级灵石食用和天地银行Geo身份。拍卖缺商品或合格NPC时按已批准Q69在启动前拒绝并提示；其余来源异常保持来源。定向转换、编译和6项架构测试已通过；真实银行四选项、商店整额账户购买、普通玉购买及基础出售与保存重进通过必要单项。Q69缺定义拒启动通过现场乘坐检查；有效拍卖、玩家商铺、其他收购/付款分支和完整表现仍待验，不代表完整经济系统验收。规则、数值、局部问题与验证边界见[交易正文](architecture.md#商店收购与现场拍卖2026-11-14)。

2026-11-22 财神商店红包受控路径已通过目标版本客户端单项：真实右键进入中文对话与商店，支付20枚一文钱币购买1个`fuqi_06`，保存重进后客户端使用该红包并自然拾取一次随机奖励，再次重进后库存保持。财神自然出生/遇见、全部奖励档、多人与专服以及其余经济交易仍开放。证据见[经济月志](logs/2026-11.md#2026-11-22-财神红包购买开启与两次重进)。

2026-11-15 九级灵石原矿右键整组开封已实现：同数量转换为对应`cailiao_191…199`，材料等级为5的级别倍数、品阶4；背包满时沿用原生给予掉落。来源提示称“破开石头”，实际共享过程不检查或破坏方块；本批保留来源提示，差异待统一复核。`spiritstonebundle`的50项必要检查已通过，覆盖九档映射、整组数量、实例属性和满背包掉落；证据见[本月原矿记录](logs/2026-11.md#2026-11-15-九档原矿开封必要单项)；详细来源和边界见[原矿开封正文](architecture.md#九级灵石原矿开封2026-11-15)。

2026-11-15补核发现`EconomyCurrency.command`已有处理器，但共享命令此前未分派到该入口。主线补接分派；新增独立`EconomyCommandCheck`经真实命令覆盖银行价值入账、本人同步和八类维度价格倍率，结束恢复原账户与价格。本子批未运行Gradle或客户端，待主线必要单项验证，见[管理命令正文](architecture.md#经济管理命令分派2026-11-15)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围263个元素；已读数量以归属表为准，阅读范围不代表已验证功能数。R13交易包补核七页菜单和shopsell规则，变更由统一整合者回写归属表；跨系统全局过程仍只记录本包已核分支。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

下列接口由经济系统持有账户、购买会话及交易策略；任务、NPC与命令调用公开入口。

| 公开操作 | 来源字段／调用者与边界 |
| --- | --- |
| `BankAccounts.snapshot / balance` | `bank.lingshi01…09` 只读九级int余额；覆盖银行刷新及商店、钱包、出售和悬赏外部读点 |
| `BankAccounts.setBalance / add / depositCount` | 银行存取、`ShopblockGuigoumai`、`Shopblockqvchu`、`ShopBuy`、`Shougousell`、`Xvanshanghuoqv` 的账户写入由经济系统统一收口；存入保持double数量加到int余额后的截断；外部消费者的运行结果由所属包登记 |
| `BankAccounts.creditValue` | `DepositinbankGo` 的正价值拆分入账；保留高到低floor、一级截断，不重新兑换既有余额 |
| `BankAccounts.withdrawOne` | `Naqv` 的单枚取款及材料实例等级/品质；库存不足沿用原生给予后掉落 |
| `BankAccounts.display / refresh` | `renwuitem.banktxtlingshi01…09 / banktmaster / banktgoumaili` 改为本人只读显示投影，不持有第二份账户真值 |
| `BankCards.read / ownedBy / bind` | 通宝玉实例 `belongToMasterUUID` 与物品自身double余额；与玩家账户分离 |
| `BankMenus.openLogin / openBank` | 对话动作“打开界面/银行”“打开界面/免登银行”；保留来源打开坐标，不能从占位方块过程补造直接入口 |
| `BankDialogueRequestEvent` | 高级通宝玉本人非潜行右键请求，任务系统接收并在来源END刻消费一次；经济系统不反向依赖任务 |
| `EconomyMenus.setProductList / setShopInformation / snapshot` | 隐藏载体的`productlist/shopinformation`迁为本人持久化交易会话；写入只经经济入口，读取返回副本 |
| `EconomyMenus.open` | 对话动作“商店/出售/基础出售/银行/免登银行”；七页注册及交易方块、拍卖台/椅入口保留来源菜单与坐标 |
| `EconomyCurrency.giveCoins / giveStones / command` | 金银1/10/100/1000、灵石4倍级差拆分；通用命令分派`numerical depositinbank`及八种收购倍率，经济接口负责实际写入 |
| `EconomyCurrency.payTrackingReissue` | 任务的寻踪虫补领先尝试20文、再尝试2贯，不合并两币种凑付 |
| `EconomyCatalogs.materialShop` | NPC请求来源三档60件随机材料商品；保留等级、品质、价格与来源分支顺序 |
| `EconomyDialogueRequestEvent` | 天地银行自然右键请求四选项原脚本；任务系统消费脚本与普通通宝玉购买动作 |
| `AuctionNpcRequestEvent` | 拍卖向NPC所有者索取来源定义副本；经济读取候选并保存本场实例，不写NPC所有者的数据 |
| `AuctionSeatRequestEvent` | 拍卖请求生成、乘坐或移除实体椅子；Decor独占实体写入，经济不依赖Decor实现 |

已对照`typed_field_accesses.csv`的`bank`外部读写类和`bankceshi1/2`显示读写点，并补读隐藏载体的商品、分页、钱包与拍卖坐标分支。账户由`BankAccounts`写入；`bankceshi1/2`和商品显示属于经济交易会话，装置库存与经营/现场状态属于其方块实体。通用命令、NPC和任务的接入及验证由对应包登记；其他系统菜单的来源槽映射仍开放。

本轮实现与验证记录在[经济月志](logs/2026-11.md)，此前跨系统供给记录见[项目月度日志](../project/logs/2026-11.md)。

银行第四项与当铺的“基础出售”走两槽灵石收购；“凡物出售”走单槽钱币收购。
