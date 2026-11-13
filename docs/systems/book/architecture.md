---
topic: book
role: architecture
status: partial
updated: 2026-11-13
---

# 公共书籍、阅读与答题：架构调查与提案

来源事实与目标方案分别陈述。已实现自然研习、书架藏宝与普通阅读；装订台已实现并通过有限1.21.1版本实机单项。未标明已实现的目标方案仍为草案，答题尚未闭合。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 入口

公共书籍包括不同载体、阅读文本、答题以及将内容交给功法/技能/图纸等系统的入口。`A:BookRightClickProcedure:135–175` 检查绑定和学习条件，并调用 `BookGiveGongfa/Gongfashuxingjisuan/Jinenglistzong`。同一竹简载体不代表同一知识内容。

`datitimu/datinum1…4/datinum` 保存答题内容和当前答案状态。文档和编号台账必须分别记录书籍物品 ID、内容定义 ID、实例绑定与学习结果。

## 目标所有权与接口提案

book 负责书籍展示载体、内容查找、阅读/答题会话与学习入口分派。功法、技能、丹方、图纸和符箓知识由所属系统拥有；公共书籍不建立一份统管所有职业的永久解锁数据库。

可共享书籍组件中的内容引用与作者/绑定等必要信息；业务实例信息仍由内容所属系统解释。学习调用实际系统操作，并根据结果展示反馈。

## 完整性

文本、分页、答题、阅读限制、消耗和发放结果一起迁移；不会仅保留翻页界面。已有语言文本与机器配置分离后保留原始内容，界面重新排版需另行评审。

装订台的来源规则与实现边界见[装订台正文](#装订台2026-11-13)。

## 自然研习入口（2026-11-08）

### 载体与分派顺序

1.18.2 版本的 `A:Book001Item`–`Book036Item` 都在 `use` 中调用 `BookRightClickProcedure`，返回父类结果。1.21.1 版本以同一 `StudyBookItem` 工厂 `book:study_book` 接到 `book_001`–`book_036`，保留注册 ID、属性与载体范围；不是仅接藏宝表当前使用的九种载体，也不为所有带技能组件的任意物品新增右键行为。`makebook` 对应 B 的装订台方块物品，保持原状。

`A:BookRightClickProcedure:46–291` 的完整分派次序是：先比较调用栈与玩家主手的物品类型，再依次处理非空 `textdisplay`（含潜行符箓命令）、`TreasureTypeBig="书架"`（发书并消耗一件）、非空 `fabaotype`、非空 `danyaoname`；这些分支都不满足时，潜行研习，否则普通阅读。物品类型比较不是严格主手事件或整栈相等，副手持相同书载体也能满足。

2026-11-08 批次接回书架藏宝和最后分支中的技能/功法研习，保留非空文本、图纸或丹方的优先级，并接回文本符箓的潜行命令执行。文本展示、图纸/丹方说明与普通阅读于[2026-11-13 批次](#普通阅读2026-11-13)补齐。当前标准功法与技能生产者生成单一业务实例；显示的混合字段优先级仍逐行遵循来源，不建立新的知识选择规则。

`StudyBookItem.use` 先取 `super.use` 的返回栈，在服务端玩家满足物品类型比较时按上述次序分派。藏宝不要求潜行；进入研习分支时仍要求潜行，非空功法名调用 `Techniques.study`，否则非空四类技能名调用 `Skills.study`。原样返回父类结果。命令允许生成空名组件，自然入口仍要求业务名非空，空名实例不因组件存在而绑定。

### 书架藏宝自然入口

来源 `A:BookRightClickProcedure:91–98` 在 `textdisplay` 为空且 `TreasureTypeBig="书架"` 时，按 `TreasureTypeSmall` 分派：`功法书` 调用一次 `BookGiveGongfa`，`技能书` 调用一次 `Jinenglistzong`，调用后均对原栈 `shrink(1)`。等级不足或抽中空结果仍减一；其他小类没有发放、消耗或后续研习。该分支自身不发送消息、粒子或声音，不关闭菜单，不清答题计数，也不自动研习或装配产物。1.21.1 版本由 `StudyBookItem` 在服务端执行同一分派；原始 `textdisplay`、`TreasureTypeBig`、`TreasureTypeSmall` 和数值 `TreasureLEVEL` 存入 `minecraft:custom_data`，保持精确大小写。读取副本不改写这些实例字段，后续 treasure/flow 生产者须按此契约生成载体。

功法等级规则来自 `A:BookGiveGongfaProcedure:11–28`：`TreasureLEVEL >=8` 取第8表，依次降到 `>=1` 的第1表，低于1不给书。目标调用 `Techniques.giveTreasureBook`，沿用已迁移的八张均匀随机表及 Q26 来源行为：第6表随机9、10为空结果，第7表随机上限11使分支12不可达；具体定义与证据见[功法正文](../gongfa/architecture.md)。book 不复制随机表或改变空结果。

技能等级规则来自 `A:JinenglistzongProcedure:13–21`：`>=8` 与 `>=1` 分支一致，均匀抽取1–13，依次调用 `Jinengget01`、`Jinengget02`，合计仅发放一本未研习技能书。目标仅在 `TreasureLEVEL >=1` 调用一次 `Skills.giveTreasureBook`，由 skill 保有13本内容与随机数。来源把抽号暂存到调用栈根 double `suijinum`；全A源码中此物品键仅被这三个发书过程读写，其他同名符号均为局部变量，没有后续消费者。现有公开操作不返回抽号，目标不持久化这个过程内中间值；剩余叠栈不会因此增加来源的随机残留标签。此差异不改变发书内容、概率、次数与数量。

来源可获得载体的实例证据为 `A:PlayerJoin02Procedure:66–75、126–135、409–427`：出生流程构造 `BOOK_021`，分别写书架/功法书或技能书及等级1，随后给玩家；书架方块及藏宝命令另有生产流程。本次只接已存在载体的自然使用，不实现出生发放、宝箱或结构生成等获取链，也不据此宣称完整获得链闭合。

精确目标制品 `neoforge-21.1.265-sources.jar` 的 `CustomData:134–135` 返回 NBT 副本，`ItemStack:1085–1086` 的 `shrink` 直接减计数；实现保留来源显式 `shrink(1)`，不使用会按无限材料能力跳过扣除的 `consume`。本地未找到锁定1.18.2版本的 `ServerPlayerGameMode` 源码，未对其原生创造模式计数恢复作结论。

本次统一 `gradlew build` 通过（1分36秒，含既有test/check与ArchUnit）；唯一一次 `runSmokeClient -PsmokeOnly=skillui` 完成35项玩法INFO、0FAIL、DONE（1分34秒），四维度正常保存退出，ERROR/FATAL0。新增藏宝主路径由检查按 `CUSTOM_DATA` 构造前置书，再调用真实客户端自然 `useItem` 消费；`[总物品差, 未绑定技能差, 未研习功法差]` 在等级0技能、等级1技能、等级1功法三例分别为 `[-1,0,0]`、`[0,1,0]`、`[0,0,1]`。三例输入载体均从叠2变为余1，双端库存同步通过。这些结果覆盖有限的自然消费主路径，不证明出生或宝箱等获取链已接回，也不代表完整36载体已逐一验收。

证据为 `tmp/skill-natural-exit-build.log`、`tmp/skill-natural-exit-client.log`、`tmp/skill-natural-exit-latest.log` 和 `tmp/skill-natural-exit-report.txt`。日志有 unknown entity 附件同步WARN；本次未运行专用服务器。藏宝其他等级、13种技能与八张功法表的全部随机分支、未知小类、文本优先阻断、副手相同载体及完整反馈视觉未作本批真实客户端验收；创造模式的两版本原生计数差异未核实；1.18.2版本实机、两版本对照和多人未执行。后文29项结果及其专用服务器记录保留为先前研习批次的历史证据。

### 研习契约与数据所有权

条件由功法和技能各自的研习操作承担：已归本人、已归他人分别只提示；未绑定书要求实际境界 `LEVELnumber >= belongToLevel`，不加境界修正。灵根复用 `RootRequirement.unmet`：空或含“无”放行，含“五”要求五行各至少 1，其余依金、水、木、火、土、阴、阳、功、业、罡、煞顺序取任一满足，失败提示取最后一个未满足条件。book 不复制条件公式或直接改写业务组件。

成功共同效果按来源先发 `tiangandizhi` 20 枚（y+1，三轴偏移0、速度1）、`tiangandizhi_2` 20 枚（脚下，偏移2/0/2、速度0）、原生 FLASH 1 枚（脚下，偏移0、速度0），再绑定本人 UUID、提示成功并写本人名字、关闭当前容器。功法操作本批补齐这三次粒子；技能原有粒子不重复发送。功法还初始化层数1与十三项加成，并发出既有 `TechniqueStudiedEvent`；skill 监听该事件附赠两门指定技能，book 不再次发放。功法自身规则见[功法正文](../gongfa/architecture.md#阶段-1功法定义书研习功法栏与成长)。

`daduishuliang` 是玩家实体持久 double，属于 book 答题进度。来源 `Xvanxiang1/2/3/4Procedure` 与 `DatiGUIwenzi6Procedure` 还会累加、读取或在答错/答满时清零；本批不接完整答题。两个 `study` 保持原有 `void` 签名：book 调用前记录未绑定，调用后重新查询实例是否已归本人，只有这次成功状态转移才执行 `putDouble("daduishuliang", 0)`。失败、本人重复与他人已研习均保留计数。

研习不扣书、不拆整栈、不扣资源、不自动装配，也不清原有技能选择或冷却；绑定作用于被使用的整栈。装配菜单和八槽生命周期独立见[skill 装配正文](../skill/architecture.md#共用功法与技能装配页2026-11-08)。

### 目标依据与验证边界

精确目标制品为 `build/moddev/artifacts/neoforge-21.1.265-sources.jar`：`Item:165–176` 的默认非食物 `use` 返回当前手中栈的 pass，`ItemStack:399–400` 直接委托物品 `use`；`Entity:getPersistentData` 及其保存/读取使用 `NeoForgeData`，因此答题计数继续保存为玩家实体的同名 double。先前研习批次 `build compileSmokeJava`（含既有test/check与ArchUnit）通过，现有 `skillui` 客户端29项全部INFO、0FAIL、DONE，四世界正常保存退出、ERROR/FATAL0。该批实际通过四类技能及功法的自然潜行 `useItem`、境界不足不绑定且保留答题计数、本人重复保留计数、成功清零、叠2数量保持与组件双端同步；完整报告为 `tmp/study-loadout-report.txt`，构建与客户端证据见[skill验证段](../skill/architecture.md#目标依据与本批验证边界)。该研习批次专用服务器一次正常启动并原生stop保存四维度退出，ERROR/FATAL0，见同一验证段。灵根失败、他人归属、副手相同载体及完整成功粒子视觉未验收，1.18.2版本实机、两版本对照和多人未执行。书籍其他分支的B1登记不作为玩法验收。

## 普通阅读（2026-11-13）

### 自然入口、实例与关闭

`StudyBookItem` 延续原分派顺序：非空 `textdisplay` 中 `fuluget && 潜行` 执行来源命令，其余文本使用清行/切行后阅读；其后是书架藏宝，再依次图纸 `fabaotype`、丹方 `danyaoname`，最后为潜行研习或普通阅读。图纸和丹方无论是否潜行都打开阅读页，不绑定、不扣材料、不研习。末分支普通右键即使没有知识实例，也沿来源打开空阅读页。仍按来源只比较物品类型，副手同类载体可满足调用条件；书页内容永远读取玩家当前主手，而非锁定刚使用的那一栈。

文本缓存规则来自 `A:BookRightClickProcedure:55–75`：先清 `text01`–`text11`，再按 Java 字符串每 22 个 UTF-16 单元切行，写入 `text01` 起的全部行；少于 22 写一行，恰好 22 也写一行。超过 242 单元会继续写 `text12` 等，但界面只读取十一行；缩短文本也不会清理第十二行及其后的旧缓存。这是来源实际边界，本批没有新增分页或清理额外缓存。缓存由服务端写 `minecraft:custom_data`，调用原生库存同步后打开阅读菜单；原 `textdisplay` 和其他键保持原值。

`book_gui` 保留来源菜单注册身份。`A:BookGUIMenu` 没有物品槽和按钮，`stillValid` 恒真；目标同样不转移、暂存或关闭返还任何物品。关闭使用原生 Esc/背包键生命周期，无答题计数、技能/功法组件或修炼档案写入。书页不拥有业务真值。

### 十一行的逐行分派

`A:Bookwenzi1/2` 前两行依次选择功法、技能、武器技、身法、轻功、图纸、丹方，均无匹配时才读对应 `text01/02`。功法和技能显示金色名字/品阶与青色归属，技能/武器技追加所需武器。图纸显示名字、品阶/类别与说明（`tuzhishuoming` 为空才用 `fabaoshuoming`）；丹方显示名字和 `danfangshuoming`。

`Bookwenzi3`–`Bookwenzi10` 各行先检查图纸，再检查丹方：图纸显示 `cailiao00name/num`–`cailiao07name/num`；第十一行 `Bookwenzi11` 另显示图纸第九材料 `cailiao08name/num`，其余类型读 `text11`。丹方在第三至十行只显示八个药草名，不凭名称补出材料数量。目标直接读现有天道图纸台、炼器随机图纸和药理台生产者写入的根字段，未改写这些生产者或配方真值。

无图纸/丹方时，第三行对本人功法显示层数与 `层数² × 品阶 × 10` 升级经验，对本人技能四类显示简介；未归本人的书显示灵根要求。第四行未归本人时显示境界要求；本人功法至第十行依次显示血量、灵力、精力、物理攻防、法术攻防、精神攻防与移动速度，保持两项之和大于零才显示一组的条件。每项颜色按书与当前主修档位比较：最下/下/中/上为 1/2/3/4，其余（含空档位）为 5；因此空主修也参与比较，不采用 Tooltip 的空槽白色规则。

本人法术技能第四行按 `jinengmanaatt >0` 显示基础法术或精神伤害；第五行消耗倍率、第六行金水木火土阴阳的正负冷却、第七行冷却豁免均按来源不额外限制归属。多个正豁免依次覆盖，最终只显示最后一项。武器技/身法/轻功没有新增法术详情行。非功法/法术分支的后续行仍读取原始文本缓存，保留来源混合字段的逐行优先级。

`BookReadingText` 使用 `Techniques.book/main`、`Skills.book` 和只读 `SkillBook.gradeText/levelText`；对照 `typed_field_accesses.csv` 中 `Bookwenzi4`–`Bookwenzi10` 的全部 `gongfa` 访问，主修档位统一经公开接口查询。业务组件不被 book 写入，技能文字计算仍由原所属系统承担。

### 界面与目标依据

来源 `A:client/gui/BookGUIScreen` 使用 176×166 背景 `book_gui`，叠加相对坐标 (-42,-24) 的 260×213 `bookgui`；十一行坐标为 x=-11、y=-11+18×行号，颜色 -12829636，无自动折行。目标沿用同一贴图和坐标，不新增滚动或重排。原网络标题内部名 `BookGUI` 改用已登记中文“书籍阅读”，不在书页追加来源不存在的可见标题。

精确目标制品 `neoforge-21.1.265-sources.jar` 已核：`ServerPlayer:518` 只广播当前菜单，故文本缓存修改在开无槽菜单前显式调用原生 `inventoryMenu.broadcastChanges`；`CustomData:59–65、76–79` 基于副本执行更新再替换组件；`AbstractContainerScreen:132–136` 自身绘制背景，目标无须重复调用背景方法；`ServerPlayer:1142–1170` 的原生 `openMenu` 负责关闭旧容器并登记新菜单。既有普通 `ItemStack.use` 与菜单生命周期证据直接复用，不重新引入网络按钮机制。

本批独立检查 `bookreading` 选择自然客户端 `useItem`，覆盖文本优先于书架、分行与缓存同步、十一可见行、未研习/本人功法、四类本人技能、图纸与丹方说明、无槽及不消费库存。检查中的书实例与配方前置由夹具构造，验证消费/展示路径，不作为出生/宝箱/文本学习书获取生产链闭合证据。定向转换、`compileJava compileSmokeJava` 和六条架构守卫通过；单次 `bookreading` 客户端70条玩法检查与鼠标检查均通过，正常保存退出，五张截图已核。技能消耗长句在截图中沿来源越过书页并于窗口边缘截断，未新增折行或重排。真实注册表已导出并核对 `book_gui`，完整结果统一记[书籍月志](logs/2026-11.md#2026-11-13-普通阅读同族路径)。来源版本实机、两版本对照、多人和完整 36 逐载体验收未执行。

## 装订台（2026-11-13）

### 来源配方、目录与成本

`B:recipes/makebookhecheng.json`以六根原版线围住中心木板，合成一件`makebook`；目标现有同名配方已完成1.21.1格式转换。`B:MakebookBlock`的自然右键绑定真实方块实体并打开`makebook_gui`。它是制作空白载体的工作台，来源没有稿件输入、随机产物、知识解锁或复制内容过程。

`B:MakebookBlockEntity`保存38槽：逻辑0为纸、1为线、2–37为36个结果位。`MakebookGuiMenu`先添加全部结果，再添加纸线，因此原生菜单序号0–35对应逻辑2–37，36/37对应逻辑0/1，玩家背包从38开始。纸槽只接受`PAPER`，线槽只接受`STRING`，结果槽禁止放入。菜单`stillValid`恒真。

`B:MakebookshuaxinProcedure`在玩家菜单END tick检查0槽至少3张纸、1槽至少1根线，满足时每刻以全新单件栈覆盖逻辑2–25，顺序固定为`book_001`–`book_012`、`book_020`、`book_021`、`book_023`–`book_029`、`book_033`–`book_035`，共24种。充足分支不处理剩下逻辑26–37；不足分支清空全部36结果。目标`BookBindingRuntime`保持同一条件和目录，服务端末刻统一广播本刻结果。

来源36个结果槽的`onTake`都调用`MakebookGuiSlotMessage`，服务端`MakebookxiaohaoProcedure`每次从逻辑0移除至多3张、逻辑1移除至多1根，不另验取出时的门槛，不发送额外消息、声音或粒子。目标用原生服务端`Slot.onTake`扣同一材料，普通领取与快捷领取都走这一入口。目录只是预览；领取后欠料的目录在下次末刻清空，材料仍充足则再次生成。没有新增持续制作或永久解锁。

### 关闭、保存、自动化与掉落

`B:MakebookqingchuProcedure`在菜单关闭时先清逻辑2–37。绑定方块实体保留0/1真实输入；未绑定的内部容器再按存活/在线状态返还或掉落剩余内容。目标沿用原生`removed`顺序，38槽序列化使用目标组件上下文。保存本身包含38槽，清预览依赖正常菜单关闭，未另加保存时过滤。

来源`WorldlyContainer`把38槽全部暴露给各面，但只有0/1允许侧面插入和提取，且侧面输入并不限制纸线类型；纸线过滤属于玩家菜单。Forge40.1.0的`BaseContainerBlockEntity`补丁提供无方向`InvWrapper`，来源菜单通过无方向能力绑定真实库存。无方向提取预览不经过侧面规则，目标保留该区别，没有收紧自动化权限。来源`MakebookBlock.getDrops`在原生战利品为空时返回一件装订台，目标显式保留该覆写；破坏方块时来源掉落整个方块实体内容，比较器也统计整个容器；若菜单仍开着，预览同样属于当时库存，本批没有添加额外抑制。关闭后破坏的普通玩家路径由单项验证。

方块沿来源内容表保留羊毛音效、硬度2/爆炸抗性10、水平朝向、含水状态、无完整遮挡及`(2,0,2)–(14,11,14)`外形；自定义类继承`WaterloggedContentBlock`以保留水源流体与水tick。`BookBlockEntities`和`BookMenus`分别保留`makebook`、`makebook_gui`注册身份，内容表由`book:binding_table`工厂承接已实现钩子。

### 原生界面、目标依据与验证边界

`B:client/gui/MakebookGuiScreen`的176×204背景、133/99纸图示、151/99线图示及原生Tooltip直接迁移，无可见标题、文字标签或按钮。默认shadcn技能按Minecraft原生适用范围使用来源主题，不引入React。原网络内部标题`Makebook`改用已有最终中文映射`block.yvanchuxiuzhen.makebook=装订针线`；三张屏幕贴图由定向资产转换登记。

精确目标`neoforge-21.1.265-sources.jar`已核：`InvWrapper`按`canPlaceItem`过滤插入，提取不询问侧面；`SidedInvWrapper`调用对应侧面放取规则；`AbstractContainerMenu`普通领取和快捷移动均进入各自槽的`onTake`；`PlayerTickEvent.Post`仍需显式限制服务端；`BlockEntity.loadWithComponents/saveWithFullMetadata`携带注册表上下文。既有同版本注册、菜单打开、原生库存同步机制沿用有效证据。

定向内容与资产转换、`compileJava compileSmokeJava`和六条架构守卫通过。`bookbinding`单项64条装订玩法INFO（含原生重进提示）、鼠标INFO1、0FAIL，正常保存退出；覆盖真实来源配方对象装配、自然放置/打开、欠料与固定目录、普通和快捷领取各扣一次、38槽双端同步、侧面预览禁取放、关闭余料、方块实体序列化、打开时退出存档再重进、关闭后破坏掉落。三张截图及最终中文映射已核，真实注册表在同次集成服导出。首轮自掉落覆写漏项已修复，失败样本与复验统一记录[书籍月志](logs/2026-11.md#2026-11-13-装订台自然制作与生命周期)。配方检查不是实际合成菜单扣六线一木板的验收；来源版本实机、两版本对照、多人同时打开、开着菜单破坏及无方向外部自动化提取未执行。
