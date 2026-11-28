---
topic: economy
role: architecture
status: partial
updated: 2026-11-26
---

# 货币、银行、商店与现场拍卖

来源事实与实现边界分别陈述。银行和交易包按批准的全量移植范围接入，注册、静态实现、运行及两版本对照分别登记。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 所有权

`B:BankcunchuProcedure:30–90` 从银行菜单索引10存入玩家 `bank` 中的 `lingshi01…09`，菜单实际有11个自定义槽。银行显示缓存和购买会话还写在隐藏 `renwuitem`；账户金额与界面内容不能混成一份快照。

商店解析语言 `txt.shoplist.*`，同时处理 1/10/100/1000 钱币及有级差的灵石。`ShopBuyProcedure:264–266` 存在 money01 已计算却使用 money02 给 lingshi01 找零的变量不一致，1.21.1 版本的处理见下文 R13-E01。

`PaimaiblockGengXinYouXiKeProcedure:30–120` 按装置自身 tick 与 `paimaijindu` 推进准备、导入 NPC/商品、轮次、竞价、挑衅与清理。`paiitem{i}/paiblockX/Y/Z{i}` 属于现场装置；参与玩家的目标坐标放在其隐藏数据载体中。

## 目标所有权与接口

玩家账户保存各币种数量；商品定义、限购/刷新与价格调整分开；商店装置保存真实货物和经营状态；拍卖装置保存来源已有的现场轮次、商品与竞价者；来源未闭合的玩家竞价和结算不补造。目标不改为全服订单簿或离线账户平台。

入口为存款、取款、兑换、购买/出售、店铺维护、进入/退出拍卖和竞价。所有输入从当前菜单、账户和真实库存取得，单位转换由同一套明确公式处理。奖励系统通过这些入口发币。

同一主线程内检查与扣付不等于跨存档磁盘原子性；具体资产交接若有中断恢复需求，只为该业务设计恢复记录。物品交付使用来源原生给予与余物掉落，菜单关闭和装置移除按各来源路径迁移；未执行的退出、重载与两版本行为对照保留为验收边界。

## 数值及裁定

货币单位、取整、找零顺序、限购、刷新和 NPC 出价逻辑完整保留，已确认错误单独决定。`renwuitem.productlist` 属于各玩家当前购买会话；目标继续保持会话按玩家区分，商品/金额归原所有者，不将其改为共享的全局临时列表。

## 银行与高级通宝玉单步入口（2026-11-14）

本包接通两种通宝玉、九级账户、银行登录/存取菜单、本人投影及高级玉真实单步对话。生产与smoke代码编译、6项架构测试及127条客户端检查通过，检查包含死亡、关闭和真实退出重进；截图与报告见[经济月志](logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。专服、1.18.2版本实机、两版本对照与多人未执行；本节结果不代表银行其余功能或任务整系统已验收。

### 来源路径与去向

| 1.18.2 版本实际行为 | 1.21.1 版本实现／边界 |
| --- | --- |
| `A:Ling12DangYouJianDianJiKongQiShiShiTiDeWeiZhi`：潜行仅在owner字符串为空时认主；普通右键本人时逐级打印物品自身double余额，至少1才显示 | `BankCardItem`＋`BankCard`组件。普通卡余额与`BankAccount`玩家账户分离，不把账户查询替换此处物品查询 |
| `A:Ling13DangYouJianDianJiKongQiShiShiTiDeWeiZhi`只负责潜行认主；`B:Playerrightclickwithitem:41–62`识别本人主手高级卡非潜行右键，写固定`bankbankbank`脚本、游标0、启动标记 | `BankCardInteraction`保留使用手过滤，经济发`BankDialogueRequestEvent`；任务系统消费真实单步脚本后打开登录。已启动后切换主手或潜行不取消，详见[任务正文](../quest/architecture.md#高级通宝玉银行单步对话2026-11-14) |
| `A:TiandiyinhangZaiFangKuaiShangYouJian`为空；`B:PlayerRightClickBlock:79–95`全局右键识别天地银行并写四选项脚本 | 自然入口实际为银行方块→任务对话→经济菜单。本日交易包接入经济对话请求，任务包消费商店、银行、购买普通通宝玉、出售四选项；天地银行Geo身份同步补齐，仍沿真实对话入口，不补造直接GUI |
| `B:BankGUIdengluMenu`的槽0收`forge:bank`；`BankGUIdenglujinru`验证owner，先复制并清旧槽，再打开银行并将复制卡数量固定1放新槽0 | `BankLoginMenu`保持顺序与标签过滤，提示保留。菜单ID为`bank_gu_idenglu`；正常关闭归还，死亡/断线关闭掉落 |
| `B:BankGUIMenu`实际11槽：0卡、1–9展示、10输入；0–9禁取放，10收`forge:lingshi`；两个页面都500×500并有原玩家库存位置 | `BankMenu`与原生客户端页面复用来源纹理、按钮、位置；ID为`bank_gui`。关闭只处理0/10，不把展示币返给玩家。Shift合并已有栈也核对`mayPlace` |

两页实际引用的16张纹理已按`B:BankGUIScreen`与`BankGUIdengluScreen`登记到[系统资产输入](../../reference/conversion/system_assets.csv)，每项使用来源`yvanchuxitong`命名空间，由转换器统一输出到`yvanchuxiuzhen`。来源PNG头核对尺寸：背景`bankgui01`为276×151、`bankgui04`为52×30、`wupinlan`为176×94；九级取款图集均79×48，存款20×40，登录51×40，关闭与说明各12×24，与源码绘制尺寸一致。共享的库存背景与说明图和既有目标哈希相同；资源转换与清单已输出，银行账户包单项已有有限页面截图；全页视觉与全部缩放条件未验收。

### 账户与数值

- `BankAccount`保存九个int，初值为0；`BankData.ACCOUNT`持久化、`copyOnDeath`并只同步本人，对应`clone_policy.csv`中`bank=all_clone_events`。账户唯一写入口在`BankAccounts`，跨系统调用者不写附件。
- `B:Bankcunchu:30–90`按输入整栈记数量。来源先把count赋给double再加到int余额，目标`depositCount`按`(int)(旧余额+(double)count)`处理，保留越界时的Java截断语义。标签中的其他ID仍清空输入但不入九级账户，按来源保留，不擅自修复。
- `B:Naqv:23–43`每次只取1枚；余额至少1才扣1并发对应`cailiao_191…199`，材料品质4、等级为灵石级别×5。目标用`Materials.setInstance`和原生`ItemHandlerHelper.giveItemToPlayer`，满库存余物掉落。
- `B:Bankshuaxin`从九级到一级计算正余额，权重为65536、16384、4096、1024、256、64、16、4、1；来源int乘法后加double，目标保留乘法溢出，不改为long。显示名称取玩家displayName；数量至少1显示“n枚”，否则“无”。
- `B:DepositinbankGo:29–99`仅处理正价值，九级到二级逐级floor拆分，余值至少1时加入一级，最后逐级`(int)(旧余额+double)`；不重新兑换旧余额，0到1之间的正小数不入账。公开`creditValue`由交易包的`EconomyCurrency.command`承接`YvanchuGo`的`numerical/depositinbank`分派；具体奖励与命令消费者由其工作包接入，不虚构独立`/depositinbank`命令。

### 同步、延后与中断边界

`BankDisplay`是独立本人只读投影，替代隐藏载体中的银行显示缓存。取款按钮立即刷新；打开与任何存入按钮处理后，按`B:BankGUIDangZheGeJieMianDaKaiShi`、`Bankcunchu`等待两个服务端END刻后刷新。打开回调保留来源现读当前`Supplier<Map<Integer, Slot>>`，再逐槽1–9放展示币并广播的顺序。银行两页提供来源自定义槽映射，未增加“仍是原菜单”条件；若切到只有槽0的登录页，来源缺槽异常边界仍保留。

同一R13未闭合边界：其他已移植系统菜单尚未提供该来源自定义槽映射，切到它们时对应来源现读/覆槽行为未迁移，不把这个差异登记为已批准修复。当前真实银行打开调用只传坐标，方块库存能力绑定已保留；构造器额外手/实体能力绑定分支未接入实际调用，仍待核实其消费者。本包暂不改变来源断线先保存玩家、后关闭临时库存的时序；银行卡与未存入币的完整断线去向仍待必要验证。

目标机制依据：NeoForge21.1.265归档中的`AttachmentType.Builder.serialize/copyOnDeath/sync`、`IMenuTypeExtension.create`、`AbstractContainerMenu.clicked/moveItemStackTo`、`SlotItemHandler.set`、`Inventory.placeItemBackInInventory`和`ItemHandlerHelper.giveItemToPlayer`；这是精确版本静态核对，非游戏证据。

银行账户包必要客户端单项已通过127项检查；本日后续交易包接入银行方块四选对话请求、普通通宝玉购买调用所需接口、九级灵石食用和商店/收购/拍卖。通宝玉完整获取生产者、跨其他系统菜单的来源槽映射及交易包未覆盖路径仍开放；悬赏/奖励和命令消费账户由对应工作包登记。专服、多人及两版本对照未验收，不随注册或文档更新提升完成状态。


## 商店、收购与现场拍卖（2026-11-14）

本批在`port/r13-economy-rest`的`09fbb55c`基线上接入交易实现。源代码已完成静态核对；提取脚本产生七页几何、37张独立页面纹理引用、来源价表及语言输入。主线已应用共享输入并完成定向转换、编译和6项架构检查；自然交易有限单项与Q69通过，具体边界见本节末尾及月志。专服、多人、1.18.2版本实机和两版本对照未执行。此前银行账户127项客户端结果只适用于银行账户包。

### 来源链与数据归属

| 来源链 | 目标实现与边界 |
| --- | --- |
| `B:PlayerRightClickBlock:79–95`识别天地银行，设置本人商品及商店说明，再写四选项脚本 | `CommerceEntrances`先经`EconomyMenus`设置会话，再发布`EconomyDialogueRequestEvent`。任务拥有脚本游标、选项与普通玉支付动作；经济不反向读写任务持久化对象 |
| `B:ShopGUIopen/ShopBuy/ShopPageUp/ShopPageDown`解析五元组商品、12件分页、两种付款 | `CommerceTrading`与`CommerceGoods`保留物品ID、名称、币种、价格、属性串及来源分页公式；商品与说明按来源语言键解析，不补写缺失定义 |
| `B:BankGUIshougoushuaxin/Shougousell/Shougouselljisuan`与`Putongshougoushuaxin/Putongchushouanniu` | 灵石收购用卡槽0、货槽1；基础收购用槽0。本人卡优先入账户，否则给实物灵石；基础物品拆成金银。`shopsell`默认true，只限制灵石出售 |
| `B:Shopblock01/ShopblockGuiset/ShopblockGui/ShopblockGuigoumai/Shopblockqvchu` | `EconomyBlockEntity`的七槽是唯一真实库存：0商品，1–3需求，4–6收入；经营属性留在装置持久化数据。主人设置、生存实物/创意天道商店、刷新和领取保留来源路径 |
| `B:Paimaiblock/Ling20/Paimaichair`及其过程 | 拍卖台21槽，0–19候选商品、20展示；令牌保存12字符身份和台/椅坐标，最多1台10椅。NPC定义经事件取副本，座椅生成/乘坐/移除经事件由Decor执行 |
| `A:TiandiyinhangBlock/TileEntity/DisplayItem` | `BankBlock/BankBlockEntity/BankBlockItem`保持同名身份、9槽、64上限、六面SidedInvWrapper；无方向沿Forge40.1.0父类返回完整InvWrapper。方块与物品使用来源Geo、半透明层、0循环及1单次动画；对话打开坐标若指向该方块，会沿来源构造器绑定9槽 |
| `A:Cailiao191…199WanJiaWanChengShiYongWuPinShi` | 九级灵石食用后经修炼公开接口增加当前灵力`4^(级别−1)`；二级起增加待吸收修为`4^(级别−2)`。灵力不在经济接口按上限截断，消息保留来源数值表达 |

`CommerceData.SESSION`持久化并复制死亡数据，仅向本人同步，替代隐藏载体中的商品、页数、商店文本、钱包/估价显示和当前拍卖坐标；客户端只读显示副本。`EconomyPrices`按来源维度保存八个double倍率，初值1，载入时仅将0归为1。`EconomyCurrency.command`处理八种`set`与`numerical depositinbank`分派，仍保留来源逐在线玩家匹配、未命中逐次反馈的顺序。共享入口此前遗漏经济分派，2026-11-15由主线补接，当前运行边界见[管理命令节](#经济管理命令分派2026-11-15)。

九页经济菜单均提供来源自定义槽映射。新增七页共用`CommerceMenu`，槽位过滤、可取性、玩家背包坐标和按钮操作编号由锁定来源提取。临时菜单关闭按来源返还或掉落可返还槽；绑定装置库存保留在装置。交易方块钱包在打开后的两个服务端END刻刷新；来源拍卖台每20刻执行一次现场过程。其余系统菜单的槽映射及快速切页时的延后覆槽仍未闭合。

### 经济管理命令分派（2026-11-15）

来源`B:YvanchuGoProcedure:48–169`在`/yvanchuxitong <玩家显示名>`之后处理`numerical depositinbank <价值>`与八类`set <价格键> <倍率> [true]`。已有经济处理器未被`SystemCommandFlow`调用，导致这九类入口无法自然到达；主线补接`EconomyCurrency.command`分派，经济生产规则不变。逐非目标玩家的未找到提示沿用来源，复用[Q195](../material/architecture.md#q195-逐玩家未找到提示)，不在本批调整多人反馈。

新增独立`EconomyCommandCheck(List<String>,String)`，通过共享命令注册执行：以已知九级旧余额入账87383.75，检查逐档拆分、保留旧余额及小数截断，再检查0、负数与不足1的正值不改账户；八类价格使用不同的受控倍率逐项检查执行维度实际值，并核对本人账户自动同步。结束恢复原账户与该维度八类价格。主线已接入选择器，生产/Smoke编译及25项真实命令/本人同步必要检查通过；证据见本月志。专服、多人、真实1.18.2版本实机与两版本对照未执行，既有银行或交易结果不替代本次命令证据。

### 币值与估价

金银四种币值为1、10、100、1000；实物找零由高到低floor拆分，最低级转int。银行灵石权重为1、4、16、64、256、1024、4096、16384、65536；商品购买仍先从低级账户余额消耗，不能用背包实物灵石替代账户付款。来源`ShopBuy`一级找零变量不一致已修正，见R13-E01。

收购优先级为物品自带`money`、书、武器/法宝/四防具、丹药罐、符箓、饰品、药草、材料。来源40档功法表、50档装备表、50档材料表、41档材料商店表、40档饰品表与80个丹药效果正负判定由`extract_economy_commerce.py`提取，避免用单一增长公式替换。功法书按技能书价×2；图纸价按`9×4^min(8, floor((等级−1)/5))×图纸倍率`，来源重量读取异常使有效折价因子为1。装备仅无主人且等级有效时收购，并保留重量折价分支；符箓保留基础等级档、逐ID系数及ceil；药草逐ID读取，材料按等级、品质+1和倍率估价，灵石与火焰走独立固定级差。

三档材料商店各生成60件，等级、品质概率、材料标签抽选和价格均沿来源分支。前两档`>=20`在`>=50`之前，后分支不可达，未自行恢复中间档。饰品21–30级重复11–20级低价段：21–25为16/32/48/64/80，26–30为64/128/192/256/320；31–35为4096至20480，36–40为16384至81920，大于40为98304，随后乘饰品倍率与2。

### 现场拍卖的有效边界

来源只有34级“呼隆老鬼”定义：筑基8–22级和结丹18–32级没有候选，元婴28–42级只有该候选，少量/大量模式实际重复导入8/44次。四份A/B中英文语言资源均无`auction.itemdata.goods*`定义；来源缺键回显长度超过17，商品扫描无法结束。这两项统一为[Q69](../../reference/legacy/source-survey.md)：已批准在缺商品或合格NPC时保持进度0，向现场玩家中文反馈一次；离座或更改设置后重新允许检查。不创建商品、NPC、玩家出价或结算规则。拒绝分支已通过真实右键入座、进度0、中文原因与连续现场刻仅提示一次的必要单项；有效配置分支未实机验证，证据见本月记录。

有效配置的来源分支仍接入NPC导入、商品导入、心理价/意愿、放弃、挑衅和对话分行。来源现场页面的11个按钮均为空回调，没有出价包和结算推进；目标保留其显示及空操作，不把静态状态机接入描述为完整拍卖成交。六条放弃语与63条挑衅语按源码赋值提取；来源初始空声明不计作一条台词。

### 来源差异与当前处理

本地编号保留详细经济证据，集中问题索引使用已有Q号；未获批准项保持来源，待集中裁定。以下均为静态源码核对，未执行1.18.2版本实机或两版本对照。

| 编号／集中索引 | 入口与来源实际效果 | 当前处理及未完成边界 |
| --- | --- | --- |
| R13-E01／Q05 | `B:ShopBuy:264–266` 已计算 money01，却将 money02 计入一级账户 | `CommerceTrading.buyStones` 按实际一级余款入账；来源中错误的结果不保留。有限场景见[经济月志](logs/2026-11.md#阶段-4-首批商店找零)。 |
| R13-E02／Q71 | `B:Shopblockqvchu:37–90`不清cunchulingshi，可重复入账；反馈在计算前读取局部值，显示0 | 已修复并验证：`ShopTrading.claim` 入账前将 `cunchulingshi` 清零并输出实际金额（`gongfa,commerce,questjournal` 烟测验证首次入账 25、暂存归 0、二次打开不重复入账，见[经济月志](logs/2026-11.md#阶段-4-续批q71-玩家商铺领取清零)） |
| R13-E03／Q72 | `B:ShopblockGuigoumai:798–801`写getCompound返回值；生存放置无playerxiangou时写入临时tag，创意刷新会初始化 | 已修复并验证：`EconomyBlockState.ShopState` 随方块实体创建与序列化始终初始化 `playerPurchases` 映射，生存商铺限购可正常拦截二次购买，已通过 `CommerceCheck` 烟测验证 |
| R13-E04 | `B:ShopblockGuigoumai`仅Inventory.contains为真才核需求数；整类需求缺失时仍能购买 | 已修复并验证：`ShopTrading.buy` 移除 `contains` 跳过分支，对 1..3 槽全部需求作模拟扣减预检，缺失或不足均拒绝购买；已通过 `CommerceCheck` 烟测验证 |
| R13-E05 | 同过程需求不足栈先shrink为0，再减getCount，后续所需数不减少 | 已修复并验证：`ShopTrading.buy` 预检全部满足后再按 `Math.min(candidate.getCount(), need)` 精确扣减，不吞材料；已通过 `CommerceCheck` 烟测验证 |
| R13-E06／Q73 | `B:Tuzhizhongliang`读取材料串索引1的attribute作为数字，得到0；自然图纸折价因子恒1 | 已修复并验证：`CommerceValuation.blueprintWeightDiscount` 改读材料定义 `fixedWeight` 累计图纸重量并应用来源折价曲线（105..150 分段折价），已通过 `CommerceCheck` 烟测验证 |
| R13-E07 | `B:Cailiaoshop01/02`先判断>=20，再判断>=50，后档不可达 | 已修复并验证：`EconomyCatalogs.materialShop` 修正为先判 `roll >= 50`（6 档）再判 `roll >= 20`（11 档），低/中/高三段均可达；已通过 `CommerceCheck` 烟测验证 |
| R13-E08 | `B:Shougouselljisuan`的饰品21–30级价段重复低档 | 提取逐级表保留原价，不使用递增公式替代 |
| R13-E09／Q69 | `B:Goodsdaoru`缺语言定义时扫描不能终止 | 已批准启动前拒绝；不执行来源挂起分支 |
| R13-E10／Q69 | `B:PaimaiDaorunpc`仅34级定义，筑基/结丹筛选无候选时不能终止；元婴重复同NPC | 已批准仅无候选时拒绝；有效候选重复规则保留 |
| R13-E11 | `B:PaimaiTuoguananniu`创意操作写paimaimoshi的全自动/自营，显示却读tuoguan | 已修复并验证：`Auctions.button` 操作 `0` 改为切换 `state.managed`，不覆盖 `state.auctionMode`，与显示和自动托管读取字段一致；已通过 `CommerceCheck` 烟测验证 |
| R13-E12 | `B:PaimaihangScreen`11按钮空回调，源状态机无玩家竞价与成交结算入口 | 保留空操作，完整成交未实现且不补造 |
| R13-E13 | `B:PaimaichairZaiFangKuaiShangYouJian`不校验令牌身份即可移椅并减计数，未同步台内坐标和计数 | 保留来源；待裁定，不以令牌归属名义加限制 |
| R13-E14 | 银行/交易菜单构造器有手和实体能力绑定分支，但已知打开调用只传坐标 | 保留已知方块/独立绑定；未发现实际消费者的额外分支继续待核，不宣称全路径对齐 |
| R13-E15 | 延后回调现读任意当前菜单的Supplier槽映射；其他系统映射尚未全部接入 | 本批只补经济九页；跨系统边界仍开放 |
| R13-E16 | `B:Ling20`身份从固定字母串按闭区间0–25取12字符 | 保留来源字母串与端点，不替换成标准UUID |
| R13-E17 | `B:Goodsdaoru`商品复合根累计，循环临时自定义名又被后写覆盖 | 保留累计根和覆盖顺序；缺定义时不合成样品验证 |
| R13-E18 | `B:Duihuafenhang`仅外循环判断20行，内循环可继续写xianshiwenben21及以后 | 保留写入边界；页面仍只显示来源20行 |
| R13-E19 | `A:TiandiyinhangTileEntity`9槽但createMenu返回独立三行箱；默认名与显示名为内部英文 | 保留未用提供者；自然右键走中文四选项。Forge40.1.0父类在null方向仍返回InvWrapper，绑定后的菜单容量不匹配风险保留待核；不补造银行GUI |

银行能力的精确来源依据为Forge40.1.0源码归档`patches/net/minecraft/world/level/block/entity/BaseContainerBlockEntity.java.patch`：`createUnSidedHandler`返回`InvWrapper(this)`，`getCapability`不限制方向。子类六面分支以外仍通过super获得null方向能力；目标因此显式提供无方向包装器，不能仅凭子类判断为空。

### 资源与验证登记

七页原生页面按来源提取布局；其中三页的普通按钮与底图未迁全（Q245）；Minecraft客户端采用原生组件，适用的布局与可访问性规范参考shadcn技能，不引入React。`language_zh_cn.json`保存本包动态键和界面键，注册脚本只把运行期键写语言片段，来源GUI键由菜单转换器生成，避免重复合并。天地银行Geo/动画/纹理及实体椅子纹理单独加入资产输入。

主线已完成定向内容/资产转换、生产及smoke编译、6项架构测试和`commerce`必要客户端单项；Q69由`construction`现场入座检查覆盖。真实银行四选项、商店整额账户购买、普通玉不足/成功购买、基础出售和正常保存重进已有有限证据，具体原件只在本月记录登记。七页手/实体能力绑定、跨其他系统延后槽映射、玩家商铺、灵石收购、非整额找零、全部估价与食用、有效拍卖、专服、多人、1.18.2版本实机和两版本对照仍开放；来源静态数值表完整不等于运行验收。

2026-11-23 双源对照发现对话“基础出售”应进入两槽灵石收购页，而“凡物出售”进入单槽钱币页；当前 `EconomyMenus.open` 已分别映射到 `STONE_SALE` 与 `BASIC_SALE`。既有 commerce Smoke 扩展为从银行第四项进入灵石页，再显式调用“凡物出售”入口，验证本人通宝玉售出后灵石入账，以及凡物卖价、钱币找零与灵石余额不变。目标版本 `commerce` 客户端已验证上述两页的结算与重进。

`CommerceScreen`的财神商店商品标签不再显示服务端生成的内部物品名：客户端从当前商品槽的本地化名称读取商品名，并按`shoppage * 60 + slot * 5 + 3`从同步商品列表取价；其余经济页面继续使用各自会话标签。该实现及受控购买/重进证据见[财神商店月志](logs/2026-11.md#2026-11-22-财神商店红包购买与保存重进)。

### Q102 小窗口下的固定画布裁切

目标版本已观察，来源版本未实机对照，暂保持来源布局。`CommerceScreen`直接采用来源提取的页面宽高居中；在854×480窗口、GUI缩放2下，商店及拍卖现场截图可见内容超出视口，不能检查全部标签、库存和按钮。普通对话同样保留500×500固定画布，本批文字、四选项、来源按钮与分隔线截图可见且能操作，不据此宣称全页视觉通过。完整窗口/缩放矩阵和来源同条件截图未执行；是否调整布局留待集中处理。具体截图见本月交易记录。

副本续批在同一窗口与缩放下也观察到管理页/普通入口页的固定画布边缘裁切，且通知遮挡部分内容；四页中文操作截图与证据路径见[副本月志](../dungeon/logs/2026-11.md)。该样本沿用Q102登记，来源同条件对照和完整缩放矩阵仍待。

## 九级灵石原矿开封（2026-11-15）

开封前的自然供给来自九种灵石矿脉。世界系统负责地物身份、维度门和群系投放；方块归经济系统，对应 `Cailiao191oreBlock` 至 `Cailiao199oreBlock`。玩家采矿沿用当前手持镐与来源等级0至8的判定。矿石取战利品时先保留非空原生结果；仅在结果列表为空时，按内容行返同档未开封原矿一件，不是直接给成品灵石，也不在采矿时设置材料实例等级。生成规则见[世界正文](../world/architecture.md)。

1.18.2 版本的`Cailiao19101Item`至`Cailiao19901Item`均绑定右键空气触发，调用共享过程`Cailiao19101DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`。过程按原矿档位映射`cailiao_191`至`cailiao_199`，给出原矿整组数量，材料实例等级依次为5、10、15、20、25、30、35、40、45，品阶固定为4；先给予物品，再消耗整组原矿。目标由`SpiritStoneBundleItem`保留该顺序，通过材料公开接口设置实例，使用`ItemHandlerHelper.giveItemToPlayer`处理库存不足掉落。

来源提示写“右键可以破开石头取出灵石”，但九个物品都是右键空气触发；共享过程不接收目标方块、未检查或破坏方块，只执行原矿到灵石的转换。本批保持来源提示和实际过程，文本与来源行为的差异登记待统一复核，不据提示补造挖矿机制。

`spiritstone` Smoke 扩展为同时检查九级灵石服用与九档原矿右键开封；开封检查覆盖同数量、等级、品阶、整组消耗及满背包掉落。静态源码与目标调用边界已核对；主线编译及原矿50项必要检查通过，证据见本月原矿记录，完整自然获取与两版本对照未验。

### Q245 三个经济页面缺少普通按钮与底图

这是操作入口缺失，不是 Q102 的小窗口裁切。1.18.2 版本 B 的 `ShopblockGuiScreen.init` 有 1 个购买按钮，`ShopblockGuisetScreen.init` 有 18 个普通按钮与 1 个图片按钮，`PaimaifangkuaiguiScreen.init` 有 6 个普通按钮。入口包括购买、设置、回收商店、托管与拍卖模式切换。三页 `renderBg` 分别绘制 `shopblock_gui.png`、`shopblock_guiset.png`（均 176×166）、`paimaifangkuaigui.png`（230×230），源 PNG 均有非透明像素。

1.21.1 版本 `extract_economy_commerce.py:50–68` 只提取 `new ImageButton` 和内联资源位置；`economy/screens.json` 三页按钮数量成了 0、1、0，没有上述主底图。`CommerceScreen.init/renderBg` 依表创建图片按钮和背景层，未另建普通按钮；`CommerceMenu.button` 保留服务端交易分派，不能代替客户端点击。确认 25 个来源普通按钮未呈现，不表示有 25 个不同服务端缺陷。未修、未作本轮实机。复核：用买家打开玩家商铺、店主潜行打开设置、打开拍卖方块设置，在相同 GUI 缩放下点击购买/设置/模式，核对画面、库存与方块数据；不要用直接发包或 NPC 财神商店代替。

### Q256 玩家商铺材料不足反馈次数不同

B `ShopblockGuigoumaiProcedure:121–146,232` 在首项不足时置拒绝标志，后项受门控；目标 `ShopTrading:126–141` 遍历各需求槽，对每个不足槽发一次提示。有货、灵石价 0、需 2 石头和 2 泥土，各带 1：来源仅报第一项，目标服务端分派可报两项，两者均不发货、不扣料。完全未带材料是既有 Q93；直接服务端分派和 Q245 导致玩家无购买按钮也是两件事。未实机。

### Q266 替换拍卖椅方块时扩大了座椅实体清理范围

B `PaimaichairBlock:112–122` 只在玩家破坏或爆炸时清除小范围内的全部实体椅；令牌收椅或收台走 `PaimaichairZaiFangKuaiShangYouJian:56–67` / `PaimaiblockZaiFangKuaiShangYouJian:62–77`，只选最近的一把。目标 `AuctionChairBlock.onRemove:24–27` 在任何不同方块替换时都发 `REMOVE_ALL`，`AuctionSeats:29–35` 清掉范围内所有实体椅。令牌显式请求 `REMOVE` 不会限制额外的全清。复核：用令牌建一椅，以普通 `/setblock … air` 替换方块（不用 destroy），立即数原坐标的实体椅；再对照玩家直接拆椅。与 Q98 的令牌身份、台账计数无关。
