---
topic: economy
role: entry
status: partial
updated: 2026-11-14
---

# 经济

标识 `economy`，类别：系统。职责：灵石货币、银行、商店、现场拍卖（[系统设计](../../development/system-design.md)）。

2026-11-11 World供给批次接入本系统九种灵石矿物Feature的来源生成机制与同名configured/placed资源，台账登记“已注册”。维度和群系尚未完整装配，自然获取与经济消费者未验收；规则及本轮验证边界见[世界正文](../world/architecture.md#自然供给地物与随机结构2026-11-11)和[世界月志](../world/logs/2026-11.md)。

B1 已登记本单位不依赖新代码类型的内容身份与资源（方块12、物品40），只表示身份、资源与基础属性就位。

2026-11-14 R13银行账户与高级通宝玉单步入口已通过必要客户端单项：127条报告检查无失败，覆盖认主、登录、九级存取、关闭、死亡和真实退出重进；编译、6项架构测试、文档和台账检查通过。验证范围与截图见[经济月志](logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。专服、1.18.2版本实机、两版本对照和多人验收未执行。银行方块四选项、普通通宝玉购买、商店/收购/拍卖、悬赏消费者以及货币自身使用仍未实现；具体来源规则和本包边界见[银行正文](architecture.md#银行与高级通宝玉单步入口2026-11-14)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 263 个元素，其中 87 个已读；这是检索范围，不是该单位已验证功能数。R13将两页银行菜单及8个完整读完的银行过程同步登记；跨系统全局过程仅记录已核银行分支，不计为整元素已读。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

银行包先按以下公开操作实现；其他经济子系统的接口仍在所属批次确定。

| 公开操作 | 来源字段／调用者与边界 |
| --- | --- |
| `BankAccounts.snapshot / balance` | `bank.lingshi01…09` 只读九级int余额；覆盖银行刷新及商店、钱包、出售和悬赏外部读点 |
| `BankAccounts.setBalance / add / depositCount` | 银行存取、`ShopblockGuigoumai`、`Shopblockqvchu`、`ShopBuy`、`Shougousell`、`Xvanshanghuoqv` 的账户写入由经济系统统一收口；存入保持double数量加到int余额后的截断；外部交易策略仍待所属包接入 |
| `BankAccounts.creditValue` | `DepositinbankGo` 的正价值拆分入账；保留高到低floor、一级截断，不重新兑换既有余额 |
| `BankAccounts.withdrawOne` | `Naqv` 的单枚取款及材料实例等级/品质；库存不足沿用原生给予后掉落 |
| `BankAccounts.display / refresh` | `renwuitem.banktxtlingshi01…09 / banktmaster / banktgoumaili` 改为本人只读显示投影，不持有第二份账户真值 |
| `BankCards.read / ownedBy / bind` | 通宝玉实例 `belongToMasterUUID` 与物品自身double余额；与玩家账户分离 |
| `BankMenus.openLogin / openBank` | 对话动作“打开界面/银行”“打开界面/免登银行”；保留来源打开坐标，不能从占位方块过程补造直接入口 |
| `BankDialogueRequestEvent` | 高级通宝玉本人非潜行右键请求，任务系统接收并在来源END刻消费一次；经济系统不反向依赖任务 |

已对照 `typed_field_accesses.csv` 的全部 `bank` 外部读写类。`bankceshi1/2`、商品表、价格、收购和拍卖数据仍归后续经济子包；本次不把这些读写点标作已接入。

本轮实现与验证记录在[经济月志](logs/2026-11.md)，此前跨系统供给记录见[项目月度日志](../project/logs/2026-11.md)。
