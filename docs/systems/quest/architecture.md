---
topic: quest
role: architecture
status: partial
updated: 2026-11-14
---

# 任务、对话动作、悬赏与成就

来源事实与实现边界分别陈述。银行固定单步保留已有效验收；普通对话与悬赏新增包已编译并通过架构检查；银行四选项自然路径已有有限单项，悬赏与其余分支未实机验证。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 状态与入口

任务持久记录在 `renwuliebiao`，当前脚本、游标、选项和交易等会话混在 `renwuitem`。`B:PlayerJoinGameProcedure:40–54` 首次创建隐藏铁锭并在登录时清空当前任务/商店入口与部分触发标记，不清空整个材料仓。

`TaskduihuashijianProcedure:48–129` 以 `%` 拆步骤、`/` 拆参数，支持文字、选项、任意键的 NBT 变更等；后续分支还处理条件、传送、药效、命令和跳转。聊天选项通过 `/taskduihua` 回到任务入口。原有数据解释能力是实际范围，不可只迁移对话文字。

Q11为已确认分支矛盾：`PlayerTickxitongProcedure:47` 首分支条件为 `renwubegin || taskbegin`，后面的 `else if(taskbegin)`（85 行）被覆盖。此结论来自生成 Java；全量 Blockly 连接调用对比没有发现遗漏的 Procedure 调用，但不代表条件无错误。

## 所有权与公开操作

定义层为任务及有类型的动作列表；玩家持久态为任务 ID/状态/进度/奖励记录；当前对话会话为来源 NPC/方块、步骤游标与选项。悬赏全局目录和玩家接取/追踪分别归属。

明确实现来源已有动作类型，复用实际 Minecraft 命令入口处理来源命令动作，不新造通用脚本语言。任意剧情 NBT 键是原有扩展点，保留在该内容的命名数据域，不能借此让任务直接覆盖所有核心类型化存档。

入口包含接取/继续/放弃、提交选择、接收行为结果、验证条件、领取奖励和悬赏完成。跨系统动作调用所属服务；同一步的扣费、发奖、游标更新按既有次序确定，不能把空回调当成功。

## 裁定与验收

被覆盖任务分支、奖励与进度次序、重新登录如何恢复均需明确规则。每条脚本要保留分支/跳转/命令目标，8 个成就元素一起迁移。任务显示正常而结果未消费不算完成。

### Q119 初见乾坤道人

1.18.2版本 A `QiankundaorenZaiFangKuaiShangYouJianProcedure:22,44`写方块的`<玩家UUID>zhuangtai=准备`；B `PlayerRightClickBlockProcedure:46–49,290–303`在方块使用前按当前使用手筛选，并在已准备时写入`chujianqiankun`普通对话。B `PlayerTickxitongProcedure:47–80`按任务目录启动或恢复，`DuihuashijianProcedure:912–914`分派，`DuihuashijiantwoProcedure:98–150`解释物品动作。目标NPC保留右键手别和准备状态，只发布`QiankunDialogueRequestEvent`；Quest的`QiankunDialogue`保存生成Java里的完整54段（含第0段任务头），复用原有`DialogueActions`、文字/选择菜单和服务端执行器；无新任务附件或语言合并键，原中文对白和选项保持。

第31步按注册ID给一枚`ling_14item`且不附加自定义数据。第41–42步标为未完成并存游标43，第43步为两项选择。「一会儿再说」去44–45并结束本次会话，不移动玩家；再次右键或真实重进后右键从43恢复。「现在就出发」去46–53，第48–49步先标完成并存53，第50步才在当前维度传送到`(20,65,13)`。已完成后右键沿普通执行器显示已完成提示，不重发、不再传送。登录清临时会话，保留任务目录及NPC自己的准备状态；在第一次储存前退出会从头开始，若已执行31但尚未存43，继续到31会再次按来源发放，不额外增加领取标记或提前存档。首入与门户均不发盘；[居所步行边界](../world/architecture.md#q119-寻灵盘来源任务与居所可达性)另计。

`q119`专项的首次与再次右键、发盘、稍后分支及原生保存重进已有受控客户端证据。扩展夹具在人间道专用存档副本中布置道人，新增重进后右键直接回到第43步、中文选项截图，以及「现在就出发」后的完成检查点53、恰好一枚寻灵盘与同维原生传送位置包的断言；扩展尚未运行。两轮的环境和证据见[任务月志](logs/2026-11.md#2026-11-18-q119-初见乾坤道人任务)；均不验证居所自然可达或完成后再次右键。

## 高级通宝玉银行单步对话（2026-11-14）

此包只接通必要的真实自然路径：本人主手高级通宝玉非潜行右键→固定bankbankbank会话→服务端END刻→银行登录菜单。构建、6项架构测试与127条客户端检查通过，包含死亡复制、登录清空和真实退出重进；报告与截图见[经济月志](../economy/logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。专服、1.18.2版本实机、两版本对照和多人未执行。四选项银行方块对话和通用脚本分派的新增接线见下节，已独立完成四选项必要单项，未把本次银行单步结果扩充为通用验收。

| 来源顺序与定位 | 本包实现 |
| --- | --- |
| `B:Playerrightclickwithitem:41–62`检查非潜行、主手高级玉与owner，写“打开银行/bankbankbank/一个打开银行系统的任务/0/未完成/无/无%打开界面/银行”、游标0、启动true并同步 | 经济发`BankDialogueRequestEvent`；任务`BankDialogueActions.request`保存同一固定脚本、游标与标记。经济系统不写任务附件 |
| `B:PlayerTickxitong:27–30,47–80`只在END刻消费；分派前清begin，首次无任务持久记录时设游标1 | `BankDialogue.tick(PlayerTickEvent.Post)`先写游标1/begin=false；本链无“任务储存”步骤，也未发现`bankbankbank`持久记录生产者，不制造新的任务记录 |
| `B:Duihuashijian:792–794,812–826`先清脚本和游标，才在玩家当时X/Y+1/Z处打开`BankGUIdenglu` | 随后写`BankDialogueSession.EMPTY`，调用`BankMenus.openLogin`；不在END刻重新检查卡、owner或潜行，不因切换手持取消已启动会话，不增加重试 |
| `clone_policy.csv`中`renwuitem=all_clone_events`，来源隐藏载体保存读回；`B:PlayerJoinGame:49–54`每次登录清脚本、游标与begin | `BankDialogueData.SESSION`序列化并`copyOnDeath`，只同步本人；登录写EMPTY，死亡与登录清理是两个不同生命周期事实 |

本链不输出对话文字或选项，不调用`/taskduihua`、`CunchurenwuProcedure`，也不收取费用。银行方块的四选项脚本确实存在，其中普通通宝玉购买需判断背包一级灵石至少10、扣10、给`ling_12`并提示绑定；这些分支由本批通用解释器承接，并已另行通过银行方块四选项、普通玉不足/成功购买检查；不能把该有限路径扩展为完整任务系统验收。

当前固定会话的读取/启动接口在`system/quest/api`，消费与登录清理在任务实现；银行打开、账户、通宝玉实例归经济。两系统保持单向依赖。本银行历史包没有接入其他来源脚本、选项缓存、交易状态、任务目录、悬赏与成就；本批增量和剩余范围见下节。


## 普通对话与任务目录（2026-11-14）

`DialogueRuntime`承接NPC和经济事件。请求只写脚本、游标0和begin；服务端END刻先清begin，按脚本头ID查询已存目录，初次从1开始，未完成记录从jindu继续，完成记录显示来源提示。登录清当前会话，任务目录保留；会话和目录按B `YvanchuxitongModVariables.clonePlayer:78,90`跨死亡复制。剧情扩展字段按原玩家顶层PersistentData语义保存但不跨死亡复制：精确Forge40.1.0 `patches/net/minecraft/server/level/ServerPlayer.java.patch:123–128`只复制`PlayerPersisted`子标签，来源“改变nbt”并不写此子标签。客户端只同步本人副本，不能写整份玩家真值。

| 已接动作族 | 当前实现边界 |
| --- | --- |
| 文字、自发文字、选项 | 原生500×500无背景页；八行正文每行18字符，最多四个来源选项与原位置；按钮补中文朗读 |
| 判断物品、物品加减 | 数量/ID检查，附加类型化NBT与自定义名称；物品ID统一命名空间，保持来源扣除顺序 |
| 改变nbt、判断nbt/数值/任务 | 本系统剧情域与任务目录，不借脚本覆盖其他系统类型化附件 |
| 跳转、任务进度、任务储存、任务设定修改 | 目录保存ID/名称/说明/进度/状态/需求/奖励/全文；`gaojirenwu=!tasksustained`按来源 |
| 裂隙解封 | 通过World公开搜索操作接回来源最近目标/立即登记流程 |
| 删除方块、改变坐标、判断坐标/维度 | 当前维度坐标、以给定范围一半膨胀的AABB；来源维度缺陷保持 |
| 药剂效果、指令、任务转接、打开界面、结束 | 命令以原权限/坐标执行；转接下一END刻；先清脚本再交经济菜单 |

来源是B `Duihuashijian`、`Duihuashijiantwo`、`Duihuashijianthree`、`PlayerTickxitong`、`PlayerRightClickEntity`及`CunchurenwuProcedure`对应分支；`Taskduihuashijian`聊天路径不是当前执行器的替代。高级物品判断、NPC跟随/取消/删除/受伤/移动、任务列表GUI、`/taskduihua`、触发方块、持续任务和八个成就仍未接入。遇未接动作保留游标并停止，不把空回调当成功。当前12个NPC语言脚本动作均在上述已接集合内；完整动态脚本覆盖、畸形参数与无限跳转不在本批已验范围。

对话菜单的完整来源是B `world/inventory/DuihuaGUI01Menu.java:29–51`、`DuihuaGUI02Menu.java:29–51`、相应屏幕与按钮消息。两菜单无库存槽；01屏幕`:78–122`显示八行正文及继续按钮，02屏幕`:89–169`显示同样正文及四个选项；`DuihuashijianProcedure:101,128,179`提供自然打开者，按钮回到步骤执行。上述两个候选菜单已经完成所有权与调用链阅读，目标由任务自己的`DialogueMenu/DialogueScreen`承担，不将通用无槽菜单归为公共核心。

### Q83 字符串累加读取会话载体

确认来源缺陷，当前保留。B `Duihuashijian:185–190`的char/str“加”把结果写玩家PersistentData，却从`renwuitem`会话同名键读旧值，连续累加通常不会接续剧情域旧串。目标`changeFlag`从SESSION读旧串、写FLAGS，保持该实例选择；不改成FLAGS累加。静态已核，游戏未验。

### Q84 当前维度字符串拼接

确认来源缺陷，当前保留。B `DuihuashijiantwoProcedure:452–470`“判断当前维度”分支将`ResourceKey[minecraft:dimension / ]`与维度ID及`]`连接，多出括号；正常维度文本比较失败。目标保留相同字符串和失败分支，不因目标ResourceKey API可直接比较而修正。实际脚本消费者与两版本实机仍待。

### Q85 带NBT扣料与跨栈索引

确认来源缺陷，当前保留。B `DuihuashijiantwoProcedure:153–241`的带NBT删除分支在足量栈先把待扣数清为0，再按0删除；参数索引在各栈间持续增加，没有每栈重置。不足量分支按物品ID删除，不再过滤NBT。目标`changeItems`保留这些顺序和可能中断的索引行为；普通无NBT银行扣料不受此分支影响。完整多栈实机仍未执行。

### Q92 对话按钮与分隔线

本次静态复核发现目标两页最初使用通用箭头按钮且遗漏选项页分隔线，现已恢复来源表现，真实继续/四选项按钮已用于银行自然单项，五张截图均已查看；完整页面与焦点状态未验收。B `DuihuaGUI01Screen:115–122`继续按钮为(240,300)的16×16，来源图`atlas/imagebutton_jixv01`为16×32；`DuihuaGUI02Screen:138–169`四按钮为(160,265/283/301/319)，同样16×16，使用`imagebutton_xvanxiang01/011/012/013`四张16×32图。常态取上半，悬停/键盘焦点取下半；源`:68–69`的`fengexian`在(121,256)以256×2原尺寸绘制。

目标复用现有`TextureButton`适配整张双态贴图；NeoForge21.1.265 `ImageButton:28–30`已改用GUI精灵图集，不能直接传来源PNG。继续和四个选择的服务端动作号保持，中文朗读随当前选项更新，500×500画布和文字坐标不变。已按默认shadcn技能的适用边界保留原生按钮焦点与朗读；未引入网页运行时。六张PNG已转换，无缺失；银行四选项截图确认来源按钮与分隔线可见，真实按钮可推进动作。固定画布的小窗口裁切及完整表现边界见经济Q102。

## 结构任务伪装方块（2026-11-14）

来源B的`Renwuweizhuangfangkuai`方块、方块实体、放置/右键/四刻调度与渲染过程已接入Quest。保留一个上限1的展示槽、来源朝向文字、连续任务选择、粒子与每日刷新标记；展示槽按方块/普通物品分别使用来源矩阵。14项结构补充字符串按精确来源提取并逐点核对NBT：`renwulist1`、`shopinformation1`、`productlist1`及`fangxiang`属于本体对话配置，未直接写其他系统持久数据。

World结构后处理事件由`DisguisedQuestRuntime`订阅，花繁城先在`(-114,64,-277)`放置任务方块并放`book_022`、设置粒子，再应用精确字段；NPC以较低事件优先级执行后续补充，保留来源顺序。对话会话经`DialogueActions`，玩家商品与说明经`EconomyMenus`。佣兵分支写云清派坐标的来源行为见[Q115](../world/architecture.md#q115-佣兵营地的跨结构条件补充)。

普通生存/创造右键执行任务，不主动打开容器；旁观者原生`getMenuProvider`路径可以打开`ChestMenu.threeRows(id, inventory)`创建的临时27格菜单，它与本体展示槽无关。来源与目标均如此，不能写成完全不可达。方块保存、更新包和各面物品能力均装配，独立审查与运行结果及未执行边界见[本批月志](logs/2026-11.md#2026-11-14-结构任务伪装方块)。本批不闭合任务列表、持续任务或寻灵盘/裂隙自然链。

## 六行悬赏与结算（2026-11-14）

`xvanshang`公共告示牌拥有六槽每栈1库存与六份目标目录；保存和更新包同步由BE承担。侧面不允许插入/提取槽0–4，槽5保持可操作；无侧面`InvWrapper`仍按来源容器公开范围工作，不概称全部自动化禁用。渲染用来源六张赏纸按3×2放置、四向旋转、比例1.8。原生界面保持500画布、六个追踪按钮、说明按钮及来源贴图，没有手工刷新按钮。

目录21个编号/最低等级/数量为008/15/1、011/15/1、012/15/1、014/10/1、015/5/1、024/10/1、026/15/1、032/15/1、035/20/1、036/20/1、037/25/1、038/25/1、040/25/1、043/20/1、045/10/8、047/25/1、048/25/1、050/20/1、051/25/1、053/25/1、054/18/1。随机等级在最低至40，水平偏移各±500，初始将Y上移到上下2/四向3共六采样为空；刷新改用六处能见天空，距离向下取整，报酬等级×80，赏纸1–6随机。刷新时上方红羊毛5刻后清为空气。

距离大于1才可追踪；首次记录猎人，生成一只带告示牌坐标/行号/猎人UUID与“(悬赏)”名的妖兽，等级经`CreatureBounties.prepare`在出生初始化前写入，给予绑定目标的05荧光蛊并显示坐标。本人再次领取经经济收费：先检查20文，再检查2贯，不混合凑钱；其他玩家拒绝。妖兽击杀任何玩家都使该条失败并移除妖兽，不局限指定猎人；带yaoshou标签的悬赏妖兽由任意实体击杀，都只给当前维度在线指定猎人入账，然后刷新，未在线同样刷新。没有责任实体的死亡不处理。

来源依据：B `Xvanshangblockplace`、`Xvanshanghuoqv`、`Xvanshangzhuizongheji`、1–6追踪与刷新、`Xvanshangshuaxinjihe`、`XvanshangFangKuaiShiTiXuanRanShi`、`EntityDieProcedure`悬赏分支，以及`XvanshangBlock`/BE、`XvanshanglingGuiMenu/Screen/ButtonMessage`。奖励只调用经济`BankAccounts.creditValue`；本系统不直接改银行附件或妖兽属性集合。

悬赏菜单候选的完整来源是B `world/inventory/XvanshanglingGuiMenu.java`、`client/gui/XvanshanglingGuiScreen.java`与`network/XvanshanglingGuiButtonMessage.java`。六槽绑定告示牌BE，显示目录、六行追踪和说明按钮，方块交互自然开窗；库存、目录、按钮请求与结算均为任务系统职责。该候选已完成完整类与消费者阅读，目标由`BountyBoardMenu/BountyBoardScreen`承担。

### Q80 悬赏刷新无玩家入口

确认来源调用边界，当前保持。来源界面六按钮只调用追踪，说明为第七按钮；B `XvanshangshuaxinProcedure`全刷新过程未找到事件/命令/页面调用，其`:43,105`第六条还使用槽6而非5。自然结算通过逐条刷新，不消费此全刷新过程。目标不增加刷新按钮或悄悄修正无调用者槽位；自然无效坐标提示“请刷新悬赏”仍可能没有玩家自助入口，交后续统一处理。

### Q86 045目录数量与生成数

确认来源不一致，当前保留。B `WorldbeginProcedure:51`目录为045定义数量8，`XvanshangblockplaceProcedure:73–99`及`XvanshangshuaxinjiheProcedure:40–64`解析为`entitynum=8`，`XvanshangzhuizonghejiProcedure:40–57`却只创建一次，没有按数量循环。目标目录保留8、追踪仍只生成1；不按显示数补七只。来源与目标实机未执行。

### Q89 悬赏说明调试文案

已实施显示差异，依据项目`AGENTS.md`“Tooltip只表达背景、操作、效果和必要数值；实现约束、自检结果……留在开发文档”的玩家文案规则，B `XvanshangshuomingProcedure:17–20`第三条源码调试/错误说明未放到玩家聊天。前两条玩法介绍仍显示。该文案调整不改变追踪、费用或奖励；已在差异索引登记，未作游戏验收。

### Q90 告示牌消失后的结算中断

确认来源现场风险，当前恢复并保留。悬赏目标保存告示牌坐标，B `EntityDieProcedure:56,79`结算按该位置直接取BE；告示牌已移除时会空引用中断，后续刷新/发奖不完整。目标同样直接读取BE，不以空检查跳过结算；该异常现场尚未实机复现，状态为来源静态风险、待统一裁定。

本批已完成定向转换、编译及6项架构检查；银行四选项、物品条件/扣料/发奖、经济菜单分派、本人同步及正常重进清会话通过必要客户端单项，原件见经济月志。悬赏、05自然消费者、其余脚本和目录存档分支、专服与1.18.2版本实机未执行。必验自然路径为NPC右键→中文对话/选项→经济菜单，银行四选项购买与扣料，以及告示牌首次追踪/补领/他人拒绝/失败/指定猎人入账和05释放回收；不能沿用银行固定单步报告宣称这些新增链已通过。

### Q120 任务伪装方块动态右键补丁缺失

目标遗漏已由`QuestBlockRefresh`补齐，原生玩家九条必要单项已通过；证据与未验边界见[月志](logs/2026-11.md#2026-11-14-任务商品动态刷新)。B `PlayerRightClickBlockProcedure:46–49`先筛当前使用手，`:61`全局调用`Renwubuding`，在原生方块右键前执行，持物/潜行/旁观不排除。`RenwubudingProcedure:8–12`在(-103,64,-263)且shuaxin时清标记并生成材料店01商品；`:14–20`在(-51,64,-320)且说明精确匹配旧陈氏文案时替换说明及整份11件商品串。坐标确在真实模板中，不能归为无调用者。目标已接回全局右键消费者，先手筛再修补本体、之后交原生方块消费；不改玩家会话，不宣称完整自然商店闭合；来源价格与属性原串保留，不自主统一商品数值。

## 任务列表与剩余普通动作（2026-11-15）

来源任务键为U（B `YvanchuxitongModKeyMappings:24`），K仍归已有主面板。`QuestJournalRuntime`读取本人JOURNAL，SESSION保存分类、原始目录页码、八行快照和选择；`renwu_gui`保持500×500无槽画布，八行、双分类、双翻页和继续共13按钮，16份原纹理由唯一转换输入登记。只向服务端发打开请求/原生菜单按钮号，服务端写本人SESSION并同步。重开清选择；未完成行加“（进行中）”，已完成分类同时纳入失败等非“未完成”状态。说明按UTF-16每18字符分段，显示前八段，超出段仍按来源保存在会话。

继续读取已选任务完整脚本和进度：高级置renwubegin，普通置taskbegin并关菜单；共享玩家tick按来源首分支消费任一标记、同时清两者，普通解释器恢复已存游标。Q11的首分支遮蔽保持，不据此宣称连续解释器自然启动已闭合。高级物品判断按物品ID、来源显示名及char/str/logic/int/double字段逐栈累计；实体动作保持来源范围、距离排序与逐实体处理，跟随状态交NPC公开操作，法术/精神字段交Combat公开操作，不直接写其他系统数据。

### Q160 任务页数与原始目录偏移

B `RenwuanjianAnXiaAnJianShi`先做整数除法list.size()/8再ceil，总页码范围0至该结果，整八项会出现额外空页。先按原始目录页码偏移、再过滤分类并继续收集最多八行，因此分类间会重复或空白；分类切换不清页码。目标保持来源，待集中裁定；不擅自改为先分类再分页。

### Q161 来源页码显示恒为0/0

B `Fanyexianshi.execute()`没有玩家参数，从新建ItemStack.EMPTY读取renwuyenum/renwuzongye，实际均为0。目标保留0/0显示，实际导航仍读服务端会话；待集中裁定，不将显示异常修成来源未有的新行为。

### Q162 八个结构的成就动态授予链

当前状态为**静态链已发现、自动核验通过、待运行核实**。A锁定来源的八个空谷残卷固定结构模板各嵌一条`advancement grant @p only yvanchuxiuzhen:chengjiu_00N`。命令存于任务伪装方块的`ForgeData`，目标对应结构保留同一块索引、相对坐标与完整脚本，仅将包装迁为`NeoForgeData`、方块和方块实体ID由`yvanchuxitong:renwuweizhuangfangkuai`转为合并后的命名空间。不能因来源Java没有直接调用成就API，就将这八项判为没有动态授予入口。

来源模板位于A `src/main/resources/data/yvanchuxiuzhen/structures/`，目标位于`src/main/resources/data/yvanchuxiuzhen/structure/`。下表块索引为NBT列表的零基索引；完整来源字段为`blocks[索引].nbt.ForgeData.脚本字段`，目标对应`NeoForgeData`。

| 模板 | 块索引 | 模板内相对坐标 | 脚本字段 | 授予目标 |
| --- | ---: | --- | --- | --- |
| `konggucanjvan01.nbt` | 1007 | (2,2,10) | `tasklist1` | `chengjiu_001` |
| `konggucanjvan02.nbt` | 2024 | (8,2,12) | `tasklist1` | `chengjiu_002` |
| `konggucanjvan03.nbt` | 3165 | (18,8,5) | `tasklist1` | `chengjiu_003` |
| `konggucanjvan04.nbt` | 1271 | (1,2,12) | `tasklist1` | `chengjiu_004` |
| `konggucanjvan05.nbt` | 4045 | (18,3,8) | `tasklist1` | `chengjiu_005` |
| `konggucanjvan06.nbt` | 4616 | (14,1,18) | `renwulist1` | `chengjiu_006` |
| `konggucanjvan07.nbt` | 17405 | (17,2,17) | `tasklist1` | `chengjiu_007` |
| `konggucanjvan08.nbt` | 26782 | (26,4,10) | `tasklist1` | `chengjiu_008` |

八串均按“文字→指令授予→任务进度更改/完成→任务储存/5→结束”排列，目标脚本文本与来源逐字一致。A `SealreleaseGoProcedure.java:361–411`依结构名放置这八模板，调用者为`YvanchuGoProcedure.java:536–537`的`unlock sealrelease`分支。目标固定表`StructureReleaseData.java:41–48`保留八项放置，裂隙在`RiftBlockEntity.java:77–78`经`StructureExploration.java:121–124`执行解封命令，再由共享 `PlayerCommandFlow` 分派到 `WorldCommands.execute` 进入释放；`WorldStructures.java:18–20`使用原生模板加载方块实体数据。2026-11-15 仅调整共享命令入口归属，结构与八串内容保持。

目标任务方块`DisguisedQuestBlock.java:64–78`将两种右键入口交给`DisguisedQuestRuntime.interact:38–53`，按序读取`renwulistN`或`tasklistN`并跳过已完成任务，然后经`DialogueActions.request:14–20`写会话；`DialogueRuntime.tick:64–102`启动执行，文字页继续后，`:207–214`以玩家位置和权限4执行“指令”。因此这批脚本从已实现任务方块入口至授予命令静态可达，仍保留来源`@p`选择器，不添加额外授予触发、替换为直接API或修改任务字段。

`chengjiu_001`至`008`定义均已迁入目标singular `advancement`目录；八份来源与目标的criterion均为`minecraft:impossible`、奖励20经验、隐藏显示，001无父项、002–008依次以前一项为父项。`tools/porting/check_quest_advancement_chain.py`逐个解析来源/目标NBT与JSON，已核对八个块索引、相对坐标、`ForgeData`→`NeoForgeData`、任务方块ID、脚本SHA-256和001–008命令对应关系，输出见`build/diagnostics/q162-advancement-chain/static-parity.json`。来源Java全文仍未发现`award`/`getAdvancements`等直接API授予调用。本结论来自源码与NBT/JSON静态核对，未执行1.18.2版本实机、1.21.1版本成就自然授予、多人与两版本对照，完整授予及任务完成/重进表现待必要运行核实。

剧情法术/精神动作只交付ManaAttackDamage/SoulAttackDamage、对应UUID、damagetype和Damage_multiplier；来源另一版消费过程无调用者，Q31保持开放，不能把字段登记写成已完成实际伤害结算。运行与未验范围只在本月记录维护。

## 升仙令与传承灵物自然任务入口（2026-11-23）

原初系统 `PlayerrightclickwithitemProcedure:24,64–93` 先过滤使用手，再读主手物品。升仙令分支在潜行判断之外；传承灵物必须潜行，且交互前主手栈的 `getInt("yikaiqi") == 30`。修真物品的 `use` 排在全局右键事件之后：从 29 领到 30 的那次不启动任务，之后再潜行使用才启动。`DialogueActions.request` 只设置会话，下一个服务端 tick 打开首个文字页。

- `sxling`：9 页文字后原样执行 `execute in yvanchuxiuzhen:dimensionrenjian run teleport @p 0 32 0`，再扣一枚主手所持类型的升仙令并结束。保留 `@p`，不是强制选定使用者。
- `chuanchenglingwurenwu`：12 页文字后执行裸命令 `futouget 3`。`DuihuashijianProcedure:718–732` 用玩家位置、权限 4、无实体的命令源原样分派第二段，不加前缀，不依赖命令成功才推进。两份来源的 Java 注册均未找到该命令根；`YvanchuGoProcedure` 的 `unlock futou` 是另一入口。保留这个断链：不代发符头，接着标为“完成”、储存进度 16 并结束。
- 普通任务的“删除方块”按 `DuihuashijianthreeProcedure:84–115` 的每组三坐标写入 `Blocks.AIR`，更新标志 3。与 `removeBlock` 不同，含水方块被删除的当刻不会留下水源。

`birthitemdialogue` 是单人新档的定向 Smoke：服务端实际使用物品，经 tick 打开首页、逐页点击菜单；区分 29→30、未潜行、潜行和 double 30.5 的整型读取，检查目录与符头不变。升仙令检查副手负例、站立和潜行启动、无需过门到达人间并扣一枚。删除动作用单独的普通对话一次处理含水半砖和石块，立即检查两处空气及会话结束。此为已编写的验证，不是实机结果。

## 连续聊天任务与选择命令（2026-11-15）

`DialogueRuntime`在没有启动标志、tasksustained为真且xvanxiang为假时累计tasktick，每60刻调用`QuestChatRuntime`并清零计时。来源首分支仍先消费renwubegin/taskbegin，Q11保持。文字推进后仅允许第二、第三扩展过程动作在本次继续，下一条文字等待下一节拍；选项暂停。动作覆盖文字、自发文字、原始tellraw选项、条件/物品/任务/坐标/维度判断、库存、NPC控制、传送、效果/命令、跳转、目录/头部修改、裂隙解封、删除方块与结束清理。法术/精神动作仅登记字段，Q31没有因此闭合。

`/taskduihua`保留无额外权限限制，并兼容来源点击产生的`<显示名> renwuweizhi <序号>`格式；目标用贪婪字符串取最后数字、剥离可选标记，因此显示名含空格时仍能定位当前维度玩家。命令继续写来源`renwuweizhi`记分板，同时在玩家有活动选项时按`daan<序号>`取得目标步骤，写入会话`playerxvanze`并直接继续`QuestChatRuntime`；没有活动选项时只保留记分板副作用，不把无效点击当作任务推进。选项依来源创建目标并关闭sendCommandFeedback，脚本文字经聊天输出；连续聊天选择已接回会话，完整自然脚本仍待验。

### Q169 连续选项命令参数错位

B `Taskduihuashijian:91–92`生成`/taskduihua <显示名> renwuweizhi <序号>`，而`TaskduihuaCommand:26–65`注册名字后直接接数字；名字使用MessageArgument读取剩余输入，下级数字不可稳定到达。目标改为单个greedy参数解析末尾数字和可选`renwuweizhi`标记，兼容来源点击字符串、带空格显示名和带引号显示名；保留原命令无权限限制及当前维度显示名匹配。静态修复完成，必要客户端命令点击仍待运行补核。

### Q170 内嵌选项命令缺少参数

B `Taskduihuashijian2:185–186`内嵌选项仅生成`/scoreboard players set -2`，缺少完整玩家、目标与数值参数。目标保留该字符串，未擅自补参；自然点击与后续流程未验，待集中裁定。

### Q171 连续选择后处理未发现调用者

B `TaskXvanzehouProcedure`可将记分板选择转为`playerxvanze`并继续解释器，但生成Java检索仅见定义，未发现调用者；`PlayerTickxitong.mod.json`中的`call_procedure_at`块位于XML顶层孤立块，不属于玩家tick主链。目标保留任务会话对`playerxvanze`的消费，不另造tick或命令调用。`QuestChatCommand`只写记分板`renwuweizhi`，`DialogueRuntime.choose`直接写任务游标；经济系统的同名字段属于独立Commerce会话，不能作为任务选择生产者。动态资源与实际消费者待核实，不能把连续解释器接线宣称为完整自然任务闭合。

### Q172 任务按键语言命名空间遗漏

首次必要客户端检查发现任务按键请求`key.yvanchuxiuzhen.renwuanjian`，转换片段仍为`key.yvanchuxitong.renwuanjian`，显示原始键名。目标遗漏已修：两语言片段与键前缀转换统一映射至合并后的命名空间；下一次必要客户端场景补核这一键，不重跑已通过目录/重进全链。修复后运行尚未执行。

### Q173 任务列表固定画布裁切

来源任务列表保持500×500画布。1.21.1版本本轮854×480截图可见八行和中文，但顶部、左侧及右侧说明超出可视范围；与来源固定尺寸有关，不能据按钮/存档通过宣称完整视觉认可。当前保持来源布局，后续集中处理；完整来源实机和两版本视觉对照未执行。
