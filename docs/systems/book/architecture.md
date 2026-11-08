---
topic: book
role: architecture
status: draft
updated: 2026-11-08
---

# 公共书籍、阅读与答题：架构调查与提案

来源事实与目标方案分别陈述。未标明已实现的目标方案仍为草案；本批仅接回下述自然研习与书架藏宝分支，不能据此视为完整 book 架构或玩法已批准、已完成。源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 入口

公共书籍包括不同载体、阅读文本、答题以及将内容交给功法/技能/图纸等系统的入口。`A:BookRightClickProcedure:135–175` 检查绑定和学习条件，并调用 `BookGiveGongfa/Gongfashuxingjisuan/Jinenglistzong`。同一竹简载体不代表同一知识内容。

`datitimu/datinum1…4/datinum` 保存答题内容和当前答案状态。文档和编号台账必须分别记录书籍物品 ID、内容定义 ID、实例绑定与学习结果。

## 目标所有权与接口提案

book 负责书籍展示载体、内容查找、阅读/答题会话与学习入口分派。功法、技能、丹方、图纸和符箓知识由所属系统拥有；公共书籍不建立一份统管所有职业的永久解锁数据库。

可共享书籍组件中的内容引用与作者/绑定等必要信息；业务实例信息仍由内容所属系统解释。学习调用实际系统操作，并根据结果展示反馈。

## 完整性

文本、分页、答题、阅读限制、消耗和发放结果一起迁移；不会仅保留翻页界面。已有语言文本与机器配置分离后保留原始内容，界面重新排版需另行评审。

装订：`B:MakebookxiaohaoProcedure` 从真实菜单扣料，不是纯知识解锁。

## 自然研习入口（2026-11-08）

### 载体与分派顺序

1.18.2 版本的 `A:Book001Item`–`Book036Item` 都在 `use` 中调用 `BookRightClickProcedure`，返回父类结果。1.21.1 版本以同一 `StudyBookItem` 工厂 `book:study_book` 接到 `book_001`–`book_036`，保留注册 ID、属性与载体范围；不是仅接藏宝表当前使用的九种载体，也不为所有带技能组件的任意物品新增右键行为。`makebook` 对应 B 的装订台方块物品，保持原状。

`A:BookRightClickProcedure:46–291` 的完整分派次序是：先比较调用栈与玩家主手的物品类型，再依次处理非空 `textdisplay`（含潜行符箓命令）、`TreasureTypeBig="书架"`（发书并消耗一件）、非空 `fabaotype`、非空 `danyaoname`；这些分支都不满足时，潜行研习，否则普通阅读。物品类型比较不是严格主手事件或整栈相等，副手持相同书载体也能满足。

当前已接书架藏宝和最后分支中的技能/功法研习。非空文本、图纸或丹方实例保留来源分派次序，阻断低优先级的藏宝或研习，但其文本展示、符箓执行、图纸/丹方说明与普通阅读仍未实现。当前标准功法与技能生产者生成单一业务实例；本批不建立混合多种知识组件的额外分派规则。

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
