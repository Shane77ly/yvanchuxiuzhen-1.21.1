---
topic: gongfa
role: architecture
status: draft
updated: 2026-11-01
---

# 功法、研习与学习小游戏：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:BookRightClickProcedure:135–260` 检查绑定、境界和灵根；`GongfashuxingjisuanProcedure:13–28` 将绑定者和初始等级写入手持书籍。`KaishigongfaProcedure:82–129` 在装备书上更新 `ExercisesExp/ExercisesLevel`，逐项增长属性后重算玩家属性。

`PlayerGUI04DangZheGeJieMianBeiGuanBiProcedure:19–199` 将菜单 0/1/2/7 槽写回功法/轻功/飞行/武器技，3–6 槽对应四页共 16 技能槽。打开过程延迟填槽；`PlayerTickProcedure:351–361` 用 `gongfalinshi` 做比较。该暂存不是第二本永久成长档案。

**同名字段已经分开核对**：B 的 `gongfa/gongfalinshi/xvexigongfa` 在显式类型访问索引中没有类外访问；A 的功法栏有真实入口。B 的功法相关内容还包含学习小游戏：`B:GongfaxvexiGuiDangZheGeJieMianDaKaiShiProcedure:28–145` 建立穴位/星宿目标，使用长度 280 的固定随机序列游标 `suijishu`，起点为 (8,8)，把 `gongfaxvexi` 同时写到装置和玩家持久 NBT。不能用两个字段同名推断存在两套实际功法装备栏。

## 目标所有权与接口提案

功法定义说明属性档位、需求和升级公式；书籍组件保存绑定、等级、经验与实例值。玩家功法槽只保存真实书籍，成长不会转为永久解锁列表。参悟台保存自身输入与加工状态。

学习小游戏单独保留 `LearningSession`：装置位置/维度、参与者、目标集、当前位置、剩余灵力和序列游标；其结果调用书籍学习入口。不要把 415 个显示过程机械变成 415 个服务，也不能删掉这部分交互。显示网格由会话状态计算。

接口分别为绑定书籍、装备/取出、推进研习、参悟、创建/操作学习会话、读取属性贡献。装备界面跨功法和技能的操作由具体协调入口完成；菜单关闭与取出必须以真实物品去向验证。

## 实现进度（2026-11-03）

### 阶段 1：功法定义、书、研习、功法栏与成长

- 定义：`tools/porting/extract_techniques.py` 从 `Gongfalist01`–`08` 提取 67 本功法（物品、名字、品阶、所需境界、灵根要求、简介、五行值、十三项档位）与各表的随机上限，`TechniqueLibrary` 读取。书架藏宝按藏宝等级取第 1–8 表（Q26）。
- 书：物品组件 `yvanchuxiuzhen:technique`（`TechniqueBook`），显示名为功法名；来源 NBT 键与字段的对应见类注释。
- 研习（`TechniqueRules.study`，来源 `BookRightClick` 的研习分支与 `Gongfashuxingjisuan`）：已研习、他人已研习、境界不足时只提示；灵根要求含“无”或为空直接可学，含“五”须五行灵根都至少 1，否则按金水木火土阴阳功业罡煞逐字判定，任一满足即可，提示取最后一个不满足的。成功后层数 1、按档位算出十三项加成，关闭当前界面并发出 `TechniqueStudiedEvent`（行尸御火、真言吒雷诀附赠的技能由 skill 处理）。研习时重置答题计数 `daduishuliang` 属 book 的右键流程。
- 成长（`TechniqueStat`）：十三项加成 = 分段基础值(层数) × 档位系数（0.6–1.4），除移速外向上取整；研习初值等于层数 1 的值（JUnit `TechniqueStatTest`）。
- 功法栏：玩家附件 `technique_slot`（死亡复制，只同步本人）。变化时提交 xiulian 主修功法信息与 attribute 加成层贡献 `gongfa:main`。
- 修炼：监听 xiulian 的 `CultivationSecondEvent`（“功法”开关的功法修炼，见 `TechniqueRules.train`）与 `CultivationExperienceEvent`（闭关吸纳元气 × 1.5 给功法经验，不检查研习者）。升级条件：层数低于境界层数 + `LevelNumberCorrect` 且低于 40；所需经验 = 层数² × 品阶 × 10。
- 提示文本（`TechniqueTooltip`，来源 `ItemLore` 功法分支与 `GongfaLore`）。
- 来源中 `Gongfashuaxin`、`Gongfashuaxin2` 没有调用者，不迁移。
- 验证：冒烟 `GongfaCheck`（研习条件、提交、闭关经验与升级、提示文本、书架藏宝）与 `ComprehensionCheck`（放置说明、右键界面、参悟、切换、两次继承与 Q27），共 36 项符合，日志 ERROR 0 条，截图 `gongfa_comprehension.png`。

### 阶段 2：参悟台

- 方块（`ComprehensionTableBlock`，内容表方块工厂 `gongfa:canwutai_block`）：放置时写入参悟模式与初始说明，右键打开参悟界面，拆除时掉出槽内物品，比较器按槽内物品输出。方块实体（`ComprehensionTableBlockEntity`）保存一格物品、模式、三行说明与可继承经验并同步给客户端。来源的槽位经漏斗可放入任何物品，目标只接受功法书（界面的槽位来源已限定为功法书；其他物品会让参悟规则读到空字段）。
- 规则（`Comprehension`）：模式切换、刷新说明、参悟（寿元 参悟次数² × 品阶 + 20，所需境界降一层并改写境界文字，只能由第一个参悟者继续）、继承（经验 × (0.5 + 品阶差 × 0.02)，Q27）。参悟后槽内物品写回为数量 1。境界文字经 xiulian 公开操作 `Cultivation.realmName`。
- 界面（`ComprehensionScreen`）：客户端菜单使用一格临时容器，内容随菜单同步，文字读取菜单所指位置的方块实体。

### 阶段 3：学习小游戏的可达性；完成

- 读后结论（Q28）：研习台 `yvanchuxiuzhen:gongfaxvexi`（GeckoLib 动画方块）与原初系统的界面 `gongfaxvexi_gui`（415 个显示过程与 14 个规则过程）在来源中不可达：研习台物品 `tab(null)`、没有配方，409 个结构与全部代码都不放置它；研习台槽位的书与 `booklevellinshi` 没有写入者；界面只在菜单登记中构造，右键研习台的分支（原初系统 `PlayerRightClickBlock`）打开的是 `TripodGUI`。按 Q20 的先例记录待裁定，不迁移；研习成功过程 `Gongfashuxingjiyv` 与研习（`Techniques.study`）结果相同。
- 完成核对（2026-11-03）：归属 478 个元素全部已读；`gradlew check`（含 JUnit）、`check_ledger.py`、`check_docs.py` 通过；开发服务端启动 ERROR 0 条；冒烟 relation、attribute、xiulian、gongfa 共 209 项全部符合（gongfa 34 项），日志 ERROR 0 条。

## 裁定与验证

`Gongfashuaxin/2` 未从当前结构入口图到达；不能仅因它们做增减属性就选作目标主链，当前有效链包括 PlayerTick 的 `Shuxingjisuan/2`。学习会话同时写玩家/装置后的多人覆盖行为需明确目标操作规则。数值、实例转移、重登、普通死亡、参悟和小游戏成功/失败分别对照。
