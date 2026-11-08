---
topic: book
role: architecture
status: draft
updated: 2026-11-08
---

# 公共书籍、阅读与答题：架构调查与提案

来源事实与目标方案分别陈述。未标明已实现的目标方案仍为草案；本批仅接回下述自然研习依赖，不能据此视为完整 book 架构或玩法已批准、已完成。源码定位采用 [系统导航](../README.md)的 A/B 规则。
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

本批只接最后分支中的技能/功法研习，前述优先分支与普通阅读仍未实现。当前标准功法与技能生产者生成单一业务实例；完整 book 迁移时必须先恢复上述优先分支，不能因存在技能组件而绕过文本、藏宝或图纸等实际消费者。本批不建立混合多种知识组件的额外分派规则。

`StudyBookItem.use` 先取 `super.use` 的返回栈，只在服务端玩家潜行且满足物品类型比较时分派；非空功法名调用 `Techniques.study`，否则非空四类技能名调用 `Skills.study`。原样返回父类结果。命令允许生成空名组件，自然入口仍要求业务名非空，空名实例不因组件存在而绑定。

### 研习契约与数据所有权

条件由功法和技能各自的研习操作承担：已归本人、已归他人分别只提示；未绑定书要求实际境界 `LEVELnumber >= belongToLevel`，不加境界修正。灵根复用 `RootRequirement.unmet`：空或含“无”放行，含“五”要求五行各至少 1，其余依金、水、木、火、土、阴、阳、功、业、罡、煞顺序取任一满足，失败提示取最后一个未满足条件。book 不复制条件公式或直接改写业务组件。

成功共同效果按来源先发 `tiangandizhi` 20 枚（y+1，三轴偏移0、速度1）、`tiangandizhi_2` 20 枚（脚下，偏移2/0/2、速度0）、原生 FLASH 1 枚（脚下，偏移0、速度0），再绑定本人 UUID、提示成功并写本人名字、关闭当前容器。功法操作本批补齐这三次粒子；技能原有粒子不重复发送。功法还初始化层数1与十三项加成，并发出既有 `TechniqueStudiedEvent`；skill 监听该事件附赠两门指定技能，book 不再次发放。功法自身规则见[功法正文](../gongfa/architecture.md#阶段-1功法定义书研习功法栏与成长)。

`daduishuliang` 是玩家实体持久 double，属于 book 答题进度。来源 `Xvanxiang1/2/3/4Procedure` 与 `DatiGUIwenzi6Procedure` 还会累加、读取或在答错/答满时清零；本批不接完整答题。两个 `study` 保持原有 `void` 签名：book 调用前记录未绑定，调用后重新查询实例是否已归本人，只有这次成功状态转移才执行 `putDouble("daduishuliang", 0)`。失败、本人重复与他人已研习均保留计数。

研习不扣书、不拆整栈、不扣资源、不自动装配，也不清原有技能选择或冷却；绑定作用于被使用的整栈。装配菜单和八槽生命周期独立见[skill 装配正文](../skill/architecture.md#共用功法与技能装配页2026-11-08)。

### 目标依据与验证边界

精确目标制品为 `build/moddev/artifacts/neoforge-21.1.265-sources.jar`：`Item:165–176` 的默认非食物 `use` 返回当前手中栈的 pass，`ItemStack:399–400` 直接委托物品 `use`；`Entity:getPersistentData` 及其保存/读取使用 `NeoForgeData`，因此答题计数继续保存为玩家实体的同名 double。本批 `build compileSmokeJava`（含既有test/check与ArchUnit）通过，现有 `skillui` 客户端29项全部INFO、0FAIL、DONE，四世界正常保存退出、ERROR/FATAL0。该批实际通过四类技能及功法的自然潜行 `useItem`、境界不足不绑定且保留答题计数、本人重复保留计数、成功清零、叠2数量保持与组件双端同步；完整报告为 `tmp/study-loadout-report.txt`，构建与客户端证据见[skill验证段](../skill/architecture.md#目标依据与本批验证边界)。本批专用服务器一次正常启动并原生stop保存四维度退出，ERROR/FATAL0，见同一验证段。灵根失败、他人归属、副手相同载体及完整成功粒子视觉未验收，1.18.2版本实机、两版本对照和多人未执行。书籍其他分支的B1登记不作为玩法验收。
