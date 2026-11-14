---
topic: economy
role: architecture
status: partial
updated: 2026-11-14
---

# 货币、银行、商店与现场拍卖：架构调查与提案

来源事实与实现边界分别陈述。银行本包按批准的全量移植范围实现；后续商店、收购与拍卖提案仍需在对应来源工作包具体化。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 所有权

`B:BankcunchuProcedure:30–90` 从银行菜单索引10存入玩家 `bank` 中的 `lingshi01…09`，菜单实际有11个自定义槽。银行显示缓存和购买会话还写在隐藏 `renwuitem`；账户金额与界面内容不能混成一份快照。

商店解析语言 `txt.shoplist.*`，同时处理 1/10/100/1000 钱币及有级差的灵石。`ShopBuyProcedure:264–266` 存在 money01 已计算却使用 money02 给 lingshi01 找零的变量不一致，需作为明确行为差异裁定。

`PaimaiblockGengXinYouXiKeProcedure:30–120` 按装置自身 tick 与 `paimaijindu` 推进准备、导入 NPC/商品、轮次、竞价、挑衅与清理。`paiitem{i}/paiblockX/Y/Z{i}` 属于现场装置；参与玩家的目标坐标放在其隐藏数据载体中。

## 目标所有权与接口提案

玩家账户保存各币种数量；商品定义、限购/刷新与价格调整分开；商店装置保存真实货物和经营状态；拍卖装置保存现场轮次、商品、竞价者和结果。目标不改为全服订单簿或离线账户平台。

入口为存款、取款、兑换、购买/出售、店铺维护、进入/退出拍卖和竞价。所有输入从当前菜单、账户和真实库存取得，单位转换由同一套明确公式处理。奖励系统通过这些入口发币。

同一主线程内检查与扣付不等于跨存档磁盘原子性；具体资产交接若有中断恢复需求，只为该业务设计恢复记录。物品无法交付、装置被破坏、退出和重载的处理需要与来源行为逐条对照。

## 数值及裁定

货币单位、取整、找零顺序、限购、刷新和 NPC 出价逻辑完整保留，已确认错误单独决定。`renwuitem.productlist` 属于各玩家当前购买会话；目标继续保持会话按玩家区分，商品/金额归原所有者，不将其改为共享的全局临时列表。

## 银行与高级通宝玉单步入口（2026-11-14）

本包接通两种通宝玉、九级账户、银行登录/存取菜单、本人投影及高级玉真实单步对话。生产与smoke代码编译、6项架构测试及127条客户端检查通过，检查包含死亡、关闭和真实退出重进；截图与报告见[经济月志](logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。专服、1.18.2版本实机、两版本对照与多人未执行；本节结果不代表银行其余功能或任务整系统已验收。

### 来源路径与去向

| 1.18.2 版本实际行为 | 1.21.1 版本实现／边界 |
| --- | --- |
| `A:Ling12DangYouJianDianJiKongQiShiShiTiDeWeiZhi`：潜行仅在owner字符串为空时认主；普通右键本人时逐级打印物品自身double余额，至少1才显示 | `BankCardItem`＋`BankCard`组件。普通卡余额与`BankAccount`玩家账户分离，不把账户查询替换此处物品查询 |
| `A:Ling13DangYouJianDianJiKongQiShiShiTiDeWeiZhi`只负责潜行认主；`B:Playerrightclickwithitem:41–62`识别本人主手高级卡非潜行右键，写固定`bankbankbank`脚本、游标0、启动标记 | `BankCardInteraction`保留使用手过滤，经济发`BankDialogueRequestEvent`；任务系统消费真实单步脚本后打开登录。已启动后切换主手或潜行不取消，详见[任务正文](../quest/architecture.md#高级通宝玉银行单步对话2026-11-14) |
| `A:TiandiyinhangZaiFangKuaiShangYouJian`为空；`B:PlayerRightClickBlock:79–95`全局右键识别天地银行并写四选项脚本 | 自然入口实际为银行方块→任务对话→经济菜单。商店、银行、购买普通通宝玉、出售四选项仍未实现，不能从空方块过程推断自然入口不存在，也不补造直接GUI |
| `B:BankGUIdengluMenu`的槽0收`forge:bank`；`BankGUIdenglujinru`验证owner，先复制并清旧槽，再打开银行并将复制卡数量固定1放新槽0 | `BankLoginMenu`保持顺序与标签过滤，提示保留。菜单ID为`bank_gu_idenglu`；正常关闭归还，死亡/断线关闭掉落 |
| `B:BankGUIMenu`实际11槽：0卡、1–9展示、10输入；0–9禁取放，10收`forge:lingshi`；两个页面都500×500并有原玩家库存位置 | `BankMenu`与原生客户端页面复用来源纹理、按钮、位置；ID为`bank_gui`。关闭只处理0/10，不把展示币返给玩家。Shift合并已有栈也核对`mayPlace` |

两页实际引用的16张纹理已按`B:BankGUIScreen`与`BankGUIdengluScreen`登记到[系统资产输入](../../reference/conversion/system_assets.csv)，每项使用来源`yvanchuxitong`命名空间，由转换器统一输出到`yvanchuxiuzhen`。来源PNG头核对尺寸：背景`bankgui01`为276×151、`bankgui04`为52×30、`wupinlan`为176×94；九级取款图集均79×48，存款20×40，登录51×40，关闭与说明各12×24，与源码绘制尺寸一致。共享的库存背景与说明图和既有目标哈希相同；当前只完成输入登记及静态核对，资源转换、清单输出和实机显示未执行。

### 账户与数值

- `BankAccount`保存九个int，初值为0；`BankData.ACCOUNT`持久化、`copyOnDeath`并只同步本人，对应`clone_policy.csv`中`bank=all_clone_events`。账户唯一写入口在`BankAccounts`，跨系统调用者不写附件。
- `B:Bankcunchu:30–90`按输入整栈记数量。来源先把count赋给double再加到int余额，目标`depositCount`按`(int)(旧余额+(double)count)`处理，保留越界时的Java截断语义。标签中的其他ID仍清空输入但不入九级账户，按来源保留，不擅自修复。
- `B:Naqv:23–43`每次只取1枚；余额至少1才扣1并发对应`cailiao_191…199`，材料品质4、等级为灵石级别×5。目标用`Materials.setInstance`和原生`ItemHandlerHelper.giveItemToPlayer`，满库存余物掉落。
- `B:Bankshuaxin`从九级到一级计算正余额，权重为65536、16384、4096、1024、256、64、16、4、1；来源int乘法后加double，目标保留乘法溢出，不改为long。显示名称取玩家displayName；数量至少1显示“n枚”，否则“无”。
- `B:DepositinbankGo:29–99`仅处理正价值，九级到二级逐级floor拆分，余值至少1时加入一级，最后逐级`(int)(旧余额+double)`；不重新兑换旧余额，0到1之间的正小数不入账。当前公开`creditValue`接口已实现，来源`YvanchuGo`的`numerical/depositinbank`命令分派及奖励消费者仍未接入，不虚构独立`/depositinbank`命令。

### 同步、延后与中断边界

`BankDisplay`是独立本人只读投影，替代隐藏载体中的银行显示缓存。取款按钮立即刷新；打开与任何存入按钮处理后，按`B:BankGUIDangZheGeJieMianDaKaiShi`、`Bankcunchu`等待两个服务端END刻后刷新。打开回调保留来源现读当前`Supplier<Map<Integer, Slot>>`，再逐槽1–9放展示币并广播的顺序。银行两页提供来源自定义槽映射，未增加“仍是原菜单”条件；若切到只有槽0的登录页，来源缺槽异常边界仍保留。

同一R13未闭合边界：其他已移植系统菜单尚未提供该来源自定义槽映射，切到它们时对应来源现读/覆槽行为未迁移，不把这个差异登记为已批准修复。当前真实银行打开调用只传坐标，方块库存能力绑定已保留；构造器额外手/实体能力绑定分支未接入实际调用，仍待核实其消费者。本包暂不改变来源断线先保存玩家、后关闭临时库存的时序；银行卡与未存入币的完整断线去向仍待必要验证。

目标机制依据：NeoForge21.1.265归档中的`AttachmentType.Builder.serialize/copyOnDeath/sync`、`IMenuTypeExtension.create`、`AbstractContainerMenu.clicked/moveItemStackTo`、`SlotItemHandler.set`、`Inventory.placeItemBackInInventory`和`ItemHandlerHelper.giveItemToPlayer`；这是精确版本静态核对，非游戏证据。

当前未实现范围继续使用R13：银行方块四选对话和普通通宝玉10枚一级灵石购买、通宝玉完整获取生产者、货币自身使用、商店/收购/拍卖、悬赏/奖励消费账户及上述跨菜单映射差异。本包必要客户端单项已通过127项检查；专服、多人、两版本对照和未覆盖路径仍未验收，不随注册或文档更新提升完成状态。
