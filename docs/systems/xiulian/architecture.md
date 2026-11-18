---
topic: xiulian
role: architecture
status: partial
updated: 2026-11-18
---

# 修炼、身份与生命周期：规则与实现状态

来源事实与目标实现边界分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:PlayerTickProcedure:64–86` 按世界时间间隔区分修炼和功法研习，之后计算属性、副本、本命法宝及元婴出窍。`experience`、`experience_Stand`、`experience_Max` 分别参与已有修为、待吸收修为和突破门槛；不可合并成一个经验字段。日收益上限来自主世界 `dailyexperience`，已用额度放在玩家 `tizhiitem` 中（同文件 97–155）。

2026-11-07完整出窍调用链复核后，skill已监听既有 `CultivationSecondEvent`，在吸收与恢复前按来源每二十刻耗神；xiulian不依赖skill，也不新增计时器。该事件同时继续由gongfa推进功法修炼。最终build/ArchUnit与既有出窍5项、界面9项通过，范围见[元婴正文](../skill/architecture.md#元婴出窍2026-11-07)。

`A:LEVELupProcedure` 实际调用金丹、元婴、境界修正和升级表现。职业升级有独立 `FuzhiyeLevelUpProcedure`；只有职业字段存在并不证明对应职业拥有完整生产流程。

玩家能力类的 166 个字段中，157 个在所有 Clone 事件复制；六种 `colddown*`、`gongjian`、`gongjianjineng`、`chuansongitem` 共 9 个只在非死亡复制。另一个 Mod 是 15 + 2，后两项为 `bankceshi1/2`。这是普通 Clone 的源码规则，不覆盖轮回符的主动重置。见全量 [生命周期台账](../../reference/legacy/inventory/state_lifecycle.csv)。

## 目标所有权与接口提案

玩家成长档案保存境界、出生、种族/血脉、灵根、寿元及资源池；体质/金丹/元婴采用各自结构。`tizhiitem` 是混合数据载体，其副本、装备培养和属性贡献交给对应系统，不能把其整个 CompoundTag 再包装成“成长对象”。

公开操作包含修为获得/吸收、资源支付/恢复、开始与结束闭关、突破、出生选择、普通死亡处理和轮回处理。闭关协调功法服务，功法更新书籍实例；成长服务不修改功法组件内部字段。职业进度由所属生产或生物系统维护，共用升级公式只在规则确实相同时提取。

2026-11-18好友境界查询由`XiulianSetup`监听relation公开事件，仅读本系统保存的`CultivationText.LEVEL_NAME`回填，不修改关系行或附件。来源取值、同步生命周期和验证边界统一见[关系正文](../relation/architecture.md#好友行实时境界2026-11-18)，不建立relation→xiulian依赖。

加载玩家档案 → 恢复原始状态 → 属性重算 → 发本人快照。服务端 tick 推进闭关、寿元和资源；客户端提交操作。普通死亡、非死亡复制、寿元结束、轮回各用明确规则表，不用统一清空开关。

## 数值和中断约束

保留修为吸收次序、日收益截断、寿元与闭关天数单位、概率取样和取整位置。不能把 `world.dayTime()` 与每 tick 计数混用；时间倒退分支在 `A:PlayerTickProcedure:374–381` 明确重设计时基准。

尚无两个版本实机对照；实现前需要把每个体质、金丹、元婴及轮回分支的数值填入本系统 balance 正文。全体分支均在移植清单，当前架构不批准任何重平衡。

## attribute 归属阅读的结果（2026-11-03）

- `Shujvtongbu` 归本系统：`PlayerTick` 调用，把当前灵力、神识、修为（不超过上限）与灵力、神识上限写入实体 NBT `synchronousPower`、`synchronousEnergy`、`synchronousExperience` 等；两个来源中都没有读取者，写入无效果，实现时确认后不迁移。
- 境界与修为成长、单项提升（`LevelupCorrect`、`RealmupCorrect`、`HealthMaxUp` 等）与灵根 `body_*`、`yinyang` 改为向 attribute 提交基础层贡献（见 [attribute 公开操作](../attribute/README.md)）。

## 实现进度（2026-11-03）

- 数据：玩家附件 `yvanchuxiuzhen:cultivation`（`CultivationData`）保存 42 个来源字段（初值同来源，存档只写改变的项）、本命槽位与 `tizhiitem` 的键值（`CompoundTag`），死亡复制，只同步本人。
- 游戏规则：`linggenlevel`（整数，默认 9）、`jinruyouxichushenxvanze`（布尔，默认真），注册名不变。
- 首次进入（来源 `PlayerJoinGame` 前段）：五行灵根之和小于 1 且规则开启时，五行各取 0–`linggenlevel` 的随机整数，阴阳 = 金 + 火 − 木 − 水，体质取 1–10 的随机数，打开出身界面。
- 出身界面（`PlayerJoinGUI`，归属由 panel 改为本系统）：五个灵根锁定按钮（消耗与返还 `liftlock`），随机按钮（消耗 `randomlift`，重随未锁定的灵根与体质；体质经记分项 `suijishu` 写入再读出），出身切换（八种出身循环），显示寿命、灵根、体质、出身说明与星数。关闭后等待两个服务端结束刻，先发初始奖励事件，再使体质生效：打开 `tizhiopen_<序号>`，按体质加修正值（阴阳双生体另加修为效率 0.3），`tizhinumber` 加 1。
- 关闭后两个服务端结束刻的`PlayerJoin02`初始奖励归flow，体质应用归xiulian。落魄家族flow已注册；2026-11-17八出身扩展已集成并运行；世家空背包数量按Q228更正、士兵状态改为发奖同刻采样后，受控单项通过并保存退出，见[奖励正文](#出身初始奖励扩展2026-11-17)。World首次全局传送阵生产及查询已在当前集成基线存在；石盘消费已接入并通过首次客户端use，家族百宝袋外部生产者仍待核。

### 阶段 3：修炼推进、突破与恢复

- 每秒推进（来源 `PlayerTick` 的修炼部分，`CultivationTick`）：世界时间与 `tizhiitem.jishiqi` 相差 20 时执行一次，时间倒退时重设基准。依次为闭关修炼（“修炼”开关）、发出 `CultivationSecondEvent`（gongfa 在此推进“功法”开关的功法修炼）、吸收待吸收修为（× 修为效率，另加 `EwaiExperience*`；雷劫中清空；主世界数据 `XiulianWorldData` 的每日上限非 0 时截断并提示，登录时按现实日期清零当日额度）、妖化冷却、阳寿已尽提示、突破、发出 `CultivationAfterBreakthroughEvent`（cooking在此代谢自有酒精）、闭关冷却（每 15 秒减 1，受 `XiulianColdCorrect` 加速）、恢复。
- 突破：修为达到所需值时，在 `fabao_009weidu` 与 `xvkong` 维度停在所需值减 1；非创造模式且层数为 20、30、40 或 41 以上时开启雷劫并把修为置 1；否则 `LevelUp`：层数加 1（不超过 42）、属性点 5 + ⌊层数 × 0.1⌋、按 `LevelupCorrect*` 修正成长（成长量累计后作为 attribute 的基础层贡献 `xiulian:growth` 提交）、1/11/21/31/41 层与 41 层以上加寿元（7300、36500、73000、146000、292000、18250），11、21、31 与 41 层以上另按 `RealmupCorrect*` 修正；大境界播放 `levelup`、其余 `littlelevelup`；闭关中另加闭关冷却；写升级文字 10 行，20 tick 后（记分项 `zdzttick` 为 0 时）打开升级界面。
- 闭关修炼（`Cultivating`）：见类注释；给主修功法加经验的部分改为发出 `CultivationExperienceEvent`，由 gongfa 处理（依赖方向）。主修功法的名字、品阶、层数由 gongfa 提交 `MainTechnique`。
- 恢复（`Recovery`）：存活玩家每秒先经relation公开操作刷新本体团队，再把当前灵力/神识复制到 `power_display/soul_display` 后恢复；团队规则与验证边界见[关系正文](../relation/architecture.md#每秒本体团队刷新2026-11-18)。显示快照随修炼档案保存并同步本人，2026-11-07 为 K 键属性页接回。恢复量读 attribute 的最终值；战斗（记分项 `zdztbegin` 为 1）或雷劫中为四分之一；效果 `effect_007` 期间不恢复生命；主修“繁星呓语”夜晚露天无雨灵力恢复 × 1.1。
- 尚未迁移：境界提升粒子 `Jingjietishengtexiao1/2`（particle）、阳寿已尽 40 tick 后的轮回 `Fulutexiao37`、酒劲消化（`jiujing`，cooking）。

### 普通重生（2026-11-08）

`A:procedures/EntityberebornProcedure:37–134` 是普通 `PlayerRespawnEvent` 入口。目标 `system/xiulian/Rebirth` 保留其资源恢复顺序：生命、灵力、神识均取对应属性最终值的 20%，先加 `RebirthCorrectHealth`、`RebirthCorrectPower`、`RebirthCorrectJingli`，再乘对应百分比；`experience_Stand` 先加 `RebirthCorrectExperience`，再乘 `RebirthCorrectExperiencePercentage`。寿元从当前值开始，先加入固定 −30 天，再按 `RebirthCorrectLifetimePercentage` 加入乘法差额；消息使用来源的增长/扣除文本与整数格式。

来源在 `RebirthCorrectLifetime` 非零时把 `RebirthCorrectJingli` 加入寿元变化量，而不是读取 `RebirthCorrectLifetime`；目标按来源保留。`PlayerRespawnFlow` 是唯一监听：以原版最大生命基准执行资源结算，再调用 Dungeon 的游戏模式恢复公开操作，处理复活维度并在第20个服务端结束刻调用属性重算。

1.18.2 版本 `EntityDeathProcedure:291–303` 仅在玩家人间道死亡且伤害有来源实体时写入 `REVIVE=人间界`。目标由 `PlayerDeathRules` 经修炼接口写入，死亡附件复制；同维重生不移动、不清标记。若在其他维度重生且仍有标记，按来源回人间道 `(0,32,0)` 再清标记。目标事件早于原版替换连接对象，流程在传送前把连接指向实际重生的新玩家。实机边界见[修炼月志](logs/2026-11.md#2026-11-18-人间道死亡标记与普通重生)。

### 阶段 4（一）：金丹、元婴与丹田界面

- 金丹（`GoldenCore.formCore`，来源 `Jindanhuoqv01`–`05`，进入 21 层时在境界修正之后）：体质数加 1；第 1–4 档依次判定，每档内按五行均衡（功法五行为“无”“五”或“金水木火土”，第 1 档灵根之和 ≥ 44，其余每行 ≥ 8/7/5，功法品阶 ≥ 31/21/11/1、层数 ≥ 15，灵力上限 ≥ 2500/2200/1900/1500）→ 妖丹（本命槽为妖丹 `cailiao_187`/`186`/`185`/`183`–`189`，生命上限 ≥ 720/700/650/500，清空本命槽）→ 单一灵根（该行 ≥ 9/8/7/5，功法五行恰为该行，开启对应灵体 `tizhiopen_003`–`007`，属性熟练度 ≥ 400/300/200/100）；都不满足时第 5 档单一灵根（≥ 3，功法五行含该行），最后为废丹 101。结果编号与修正值见类注释。主修功法只在本人习得时计入，否则按无功法。
- 元婴（`GoldenCore.formNascentSoul`，来源 `Yvanyinghuoqv01`，进入 31 层时）：功法品阶 ≥ 31、层数 ≥ 25 时依次判定五行均衡（202，需五行均衡金丹 102–105、灵力上限 ≥ 6000）、妖丹（203，需妖丹金丹 106–109、生命上限 ≥ 1200）、单一灵根（204–208，需同行金丹与属性熟练度 ≥ 400），否则普通元婴 201。
- 妖丹的实例等级条件（来源另接受 `cailiaolevel` 达标的妖丹）随 material 接入，此前只按物品 ID 判定。
- 丹田界面（`DantianActions`、`DantianMenu`、`DantianScreen`，来源 `DantianGui` 及其过程）：打开时整理页码、金丹与元婴名、体质列表（键沿用 `tizhipagenum`、`tizhipaixv<n>id`、`tizhixianshi<n>id` 等），本命槽放入本命法宝；关闭时槽内物品写回本命槽，结丹期起设本命显示标记。本命槽只接受妖丹与带“启灵”附魔（`yvanchuxiuzhen:qiling`，读物品标签上的附魔）的法宝与武器。按钮：翻页、9 行体质说明（聊天发送 `tizhi.shuoming.<编号>`，客户端翻译）、出身说明、返回、本命法宝说明、属性熟练度说明。
- 依赖方向：返回按钮来源按 `dangqianGUI` 回到面板分页，面板不被系统依赖，改为发出 `DantianReturnEvent`，面板取消事件并打开自己的分页，没有取消时关闭界面；面板通过 `Dantian.open` 打开本界面。本命法宝的认主、等级与经验在物品上归 equipment，由 equipment 提交 `BenmingProgress` 供界面显示，未提交时显示 0。来源中本命槽放入物品时检查结丹期的 `Dantianwupingaibian`/`2` 没有调用者，不迁移。
- 语言：体质名与说明 `tizhi.*`（来源 284 键）经 `docs/reference/conversion/lang_prefixes.csv` 写入本系统语言片段；界面标签 `gui.yvanchuxiuzhen.dantian_gui.*` 随菜单迁移。

### 阶段 4（二）：血道

- 吸血石（`BloodStoneBlock`、`BloodStoneBlockEntity`，内容表方块工厂 `xiulian:xixveshi`）：放置后每 20 tick 检查充血量 `xveyetianchong`，达到 100 时在上方 1–4 格第一个空气处放血流并扣 100，4 格都不空时置为 100。充血由 combat 的流血效果经公开操作 `BloodStone.addBlood` 写入（效果等级 + 1）。血池渲染（`BloodStoneRenderer`）按充血量缩放粉红色陶瓦。来源扣除充血量时不同步客户端，目标在每次变化时同步，血池高度随之更新。
- 血流（`XiulianFluids`）：源与流动流体、液体方块、桶 `xveliu_bucket`（原版“工具与实用物品”栏），属性见类注释。来源把血流列入 `minecraft:water` 流体标签，实体在其中按水处理；1.21.1 的实体按流体类型判断（`FluidTags.WATER` 的判断被换成水的流体类型），因此流体类型取水的行为（游泳、溺水、灭火、清除摔落距离、载船、湿润耕地），标签仍保留。流体的滴落粒子 `xvedi` 随 particle 接入。
- 血精（`BloodEssenceItem`，物品工厂 `xiulian:xvejing`）：右键回复 2 点生命并消耗 1 个。闭关修炼在血流旁产出血精（`Cultivating`，阶段 3）。
- 雷劫（会话决定 2026-11-03，评估为轻微，记录后继续）：开启标记之后的劫云、七种天雷与天劫实体经 `/lightninglanding` 与 `/shanghai` 进入 combat 的伤害结算，并用到彩色闪电实体、particle 的粒子与 GeckoLib 动画实体。combat 依赖本系统，本系统不能调用 combat，因此雷劫过程在 combat 实现伤害入口之后迁移；此前生存模式修为圆满只开启雷劫标记。2026-11-07 已按下文“完整雷劫”接回真实过程，依赖方向保持单向。

### 阶段 4（三）：加点、灵根重置与出身物品；完成

- 加点（`SkillPoints`，来源 `HealthMaxUp` 等六个过程，面板第 2 页按钮）：见类注释；成长作为 attribute 基础层贡献，原版属性随之重算。
- 灵根重置（来源 `Rebirthinxiuxianfalse`、`Rebirthinxiuxiantrue`，管理命令 `/yvanchu` 的子命令，命令归 flow）：随机 0–9 或全为 9。
- 升仙令（`AscensionTokenItem`）与家族令牌（`ClanTokenItem`）：见类注释。传承灵物与家族百宝袋已接分次奖励，见[下文](#出身分次补给2026-11-15)；远古石盘坐标API已存在，自然消费已接入并通过首次客户端use。
- 读后改变归属：元婴出窍（`Yvanyingchuqiao` 等 6 个元素）是技能界面施展的技能，归 skill；被闪电击中时按主修功法给效果的 `EntityThunder` 归 gongfa；境界提升粒子留在本系统，随 particle 接入。
- 完成核对（2026-11-03）：`gradlew check`、`check_ledger.py`、`check_docs.py` 通过；开发服务端启动 ERROR 0 条；冒烟 relation、attribute、xiulian 共 175 项全部符合（其中 xiulian 131 项），日志 ERROR 0 条。

### 属性加点页（2026-11-15）

来源完整核对 `A:PlayerGUI02Menu`、`PlayerGUI02Screen`、`PlayerGUI02ButtonMessage`、`OpenGUI02`、`PlayerGui02jieshao`、`HealthUpxianshi/PowerUpxianshi/SoulUpxianshi`、`Att01xianshi/Att02xianshi/Att03xianshi` 与 `Pointsxianshi`。目标 `SkillPointMenu`、`SkillPointScreen` 保留菜单 ID `player_gui_02`、500×500 布局、36 格玩家背包、六个 8×8 加点按钮、五个跨页按钮及说明按钮。Shift 搬移保持主背包 0–26 与快捷栏 27–35 互转，来源无自定义槽分支搬移后直接返回空栈；菜单不绑定方块，`stillValid` 恒真，服务端按钮仍要求菜单坐标所在区块存在。

按钮 0–5 依次调用既有 `Cultivation.spendSkillPoint` 的生命、灵力、精力上限与物理、法术、精神攻击枚举；算法和同步仍以既有 `SkillPoints` 为唯一实现。特别保留“已分配点数大于层数×1.5 才拒绝”的来源比较，等于上限时仍可分配；不增加重置按钮。显示使用 `DecimalFormat("0")`，每行依次拼接来源语言前缀、基础增量加体质修正、来源后缀、当前最终属性与括号内已分配点数。生命取原版最大生命属性，其余五项只读 attribute 公开操作，剩余点数和修正只读本人修炼快照。

按钮 6–10 发出 `SkillPointPageEvent`，目标页为 1/3/4/5/6，携带当前菜单坐标；panel 监听后按来源上移一格打开目标页，并独占第 2 页记忆与 K 键导航。`Cultivation.openSkillPoints` 接受最终打开坐标，菜单打开包只传该坐标。xiulian 不导入 panel，服务端拥有加点真值，客户端只发原生菜单按钮编号。双态按钮复用 `TextureButton`，保留原图和坐标，中文消息用于键盘焦点朗读；19 个来源语言键进入本系统片段，动态 `gui02*` 已登记转换前缀，17 项贴图依赖已登记来源资源输入。

Q187（待裁定，保持来源）：`A:PlayerGui02jieshaoProcedure` 第二条说明为“境界提升不会直接增加属性值，只会给您若干属性点，需要您自行分配。”，而同版本升级、境界与体质修正链实际会直接增加基础属性。该矛盾影响第 2 页说明对成长方式的描述，不改变现有升级与加点执行。四条说明全部保留原文，未把文案改成新规则；1.18.2 版本实机与 1.21.1 版本说明按钮实机均未执行。

来源核对、定向转换、生产/Smoke编译及本页按钮网络、六项预览同步、跨页/K记忆必要单项通过；本页截图已复看。证据见[本批集成记录](../project/logs/2026-11.md#2026-11-15-页面命令阵法与展示家具集成验证)；完整背包搬移、死亡/重登、1.18.2版本实机与两版本对照未执行，历史核心检查不代替本页新证据。

### 来源问题

- Q22（轻微，记录后继续）：境界名来源以服务端语言翻译语言键 `levelnameNN` 后存入，单人游戏随客户端语言、专用服务器为英文；目标统一存中文名（与初值“凡人”一致）。升级文字第 9 行法术防御增加时写的是“旧值 − 新值”，第 10 行神识攻防沿用“物理攻击”“物理防御”标签，按来源保留。
- Q23（轻微，保持来源）：孕育元婴时来源把体质数加在空物品上，体质数不变。
- Q24（轻微）：丹田体质排序在打开的体质少于体质数时来源无限循环，目标扫描到 `tizhiopen_999` 为止；最后一页恰好 9 个体质时第 9 行不显示，保持来源。
- Q25（轻微，保持来源，待裁定）：本命槽上限 64，放入多个妖丹后关闭再打开只剩 1 个。
- Q21（轻微，保持来源）：随机体质只取 1–10，来源列出的第 11 种“风动玉髓体”、第 12 种“纯阳合剑体”随机取不到；关闭界面时不是前 11 种的体质（含空体质）都按第 12 种生效。体质锁定键 `tizhilock` 只被读取，没有写入者，体质总会随灵根一起重随。

### 验证

- 开发客户端 `runSmokeClient -PsmokeOnly=xiulian`：开启出身规则后按登录流程随机灵根（0–9）与体质（前 10 种），阴阳公式、火灵根提交到 attribute 的五行系数；经网络按钮锁定金灵根、随机（金灵根不变、随机次数减 1、体质取自记分项）、解锁返还次数、出身前后切换；关闭界面后体质标记、修正值与计数；公开操作的加减同步到客户端；死亡重生与退出重进后保持。2026-11-03 实测 34 项全部符合，截图 `xiulian_birth.png`。阶段 3 的 `XiulianProgressCheck`：修为转入、每日上限截断、突破（层数、境界名、扣除与重算所需修为、寿元、升级修正的生命成长、升级文字）、升级界面打开与关闭（截图 `xiulian_levelup.png`）、生存模式 20 层开启雷劫、灵力恢复、骑乘闭关的天数、寿元与冷却；连续 3 次运行全部符合。
- 阶段 4（一）的 `DantianCheck`（2026-11-03）：五行各 9、主修“无”五行 31 品 15 层、灵力上限 3000 时进入 21 层得金丹 105（冷却修正 −0.2、体质数 +1）；功法层数 15 进入 31 层得普通元婴 201（本命培养速度 +1、体质数不变）；本命槽接受妖丹、拒绝石头；丹田界面打开后本命槽为本命法宝、体质列表与页码、元婴名、本命显示标记；取出物品后按返回按钮关闭界面，本命槽清空。`-PsmokeOnly=xiulian` 77 项全部符合，日志 ERROR 0 条，截图 `xiulian_dantian.png`。
- 阶段 4（二）的 `BloodPathCheck`（2026-11-03）：吸血石充血 150 后放出一格血流、剩 50，血流属于水标签，腥桶已注册，血精回复 2 点生命并消耗 1 个；截图 `xiulian_bloodstone.png` 中血流按半透明红色贴图流动。`-PsmokeOnly=xiulian` 85 项全部符合。
- 冒烟检查在新建世界后约 10 秒内死亡并退出存档时，偶发集成服务端停在 `Saving worlds`（停止流程等待某个区块的保存条件，区块工作线程空闲），客户端随之卡住；xiulian 与 attribute 检查都出现过（2026-11-03 共 9 次运行中 3 次）。冒烟检查改为等区块待办任务连续 40 tick 为 0 后再退出，此后连续 8 次未复现，见[月度记录](../project/logs/2026-11.md)。
- `XiulianOperationsCheck`：加点（生命、灵力，属性点不足与超上限不分配）、灵根提升至最大与重置、升仙令，13 项符合。
- 未执行：1.18.2 版本实机对照；多人场景。

## 完整雷劫（2026-11-07）

已实现的来源链为 `PlayerTick` → `ThunderBegin` → `Jieyvn.baseTick` → `Tianjie01` / `Leijie0101–0107`；云的 `die` 与雷龙的 `die` 均实际绑定死亡过程。目标位于 `xiulian/tribulation`。`CultivationTick` 在置开关后、修为置 1 前调用开始过程；云成功后写 `jinji`，由原每秒突破入口继续升级。

境界 20/30/40/≥41 的基础组数为 13/25/37/37，基础攻击为 30/60/90/120。组数先加 `ThunderSufferingNumber`，攻击先加 `ThunderSufferingAttack`、再乘 `1 + ThunderSufferingAttackPercentage`；云最终攻击另加玩家 `thunder_Att`，有 `effect_022` 时减去效果等级加一。每组从雷 1–7 等概率选择并写入云的持久数据；玩家旧雷数随后清零。

| 组 | 来源前摇条件 | 释放与结束 |
| --- | --- | --- |
| 瞬雷连闪 | `qianyao ≤ 60` 时累加 | 每 15 刻白雷一次，共 9 次，倍率 2、直径 4 |
| 青雷 | `qianyao ≤ 200` 时累加 | 每 4 刻随机 ±8 格落雷一次，共 24 次，攻击 ×0.8；另每 30 刻中心攻击 ×1.2；均倍率 1、直径 4，中心粒子延迟 5/10/15/20 刻 |
| 地滚雷 | `qianyao ≤ 100` 时累加 | 每 10 刻在 ±10 格、高 12 格处生成一只，共 16 只 |
| 五雷轰顶 | `qianyao ≤ 200` 时累加 | 每 40 刻一组，共 5 组；中心倍率 3/直径 4，四角 ±2 格倍率 2/直径 2；中心粒子延迟 1/6/11/16 刻，角落延迟 5 刻 |
| 九霄雷柱 | `qianyao ≤ 200` 时累加 | 生成一只雷柱，计时 320 刻后推进下一组 |
| 紫金神雷 | `qianyao ≤ 160` 时累加 | 每 20 刻一次，共 9 次，倍率 1、直径 4，粒子延迟 1/6/11/16 刻 |
| 化灵雷龙 | 累计至 200 刻 | 在云下 36 格生成雷龙并重置前摇，再计 100 刻推进下一组 |

范围落雷以 `TribulationLightningEvent` 把原始落点及完整命令交给 combat；攻击格式保留 `DecimalFormat("")`，攻击者“无”、队伍占位“天劫”。实体范围先按来源 AABB、距离排序与 `Relations.isFriendly` 筛选，再发 `TribulationDamageEvent`；攻击量用 `DecimalFormat("0")`，倍率保留小数。xiulian 不导入 combat。粒子使用原 ID、数量、坐标与延迟；延迟回调现读实体位置，移除实体不取消已安排的回调。

实体模型由 `extract_entity_models.py --owner xiulian` 提取为 `content/xiulian/entity_models.json`。`EntityModelRow`、`AnimatedLook` 与 `AnimatedEntityRenderer` 因 skill 和 xiulian 的实际共享需求移到 platform；技能实体只改 import。云保留飞行控制、无重力、35×5 注册尺寸再放大 3 倍、生命 800、动画过渡 4 刻与死亡 20 刻移除；雷柱保留水生导航、生命 500、动画过渡 4 刻与死亡 2 刻移除。云与雷柱不被推挤、流体不推并可水下呼吸。

地滚雷生命 20，以原三角公式飞向保存坐标；接近两格、遇雨水或触地后两刻放魂火、直径 5/倍率 2 雷伤，允许生物破坏时强度 4 爆炸，移除后在 1/11/21 刻继续粒子。雷柱追随存活目标，每 10 刻直径 3/倍率 0.7 雷伤，320 刻消散。雷龙生命 40、尺寸放大 2 倍：徘徊 40 刻、下坠，160 刻到期或距玩家高度不超过 2 后进入结束，下一分支执行直径 12/倍率 5 雷伤与 1/6/11/16/21/26 刻粒子；仅其死亡时发独立碎片、1 刻移除。免疫与火焰属性分别按四个来源实体保留。

共享渲染复核（2026-11-07）：四个来源Renderer均明确关闭死亡侧翻，劫云渲染缩放3倍、雷龙4倍、地滚雷和雷柱1倍，现由模型表`rendering`与共享渲染器接回。以上是渲染尺寸，独立于劫云3倍和雷龙2倍的碰撞尺寸；阴影与固定eyes发光层保持逐类配置，发光层不重复放大。静态证据为`tmp/skill-render-source-audit.log`，精确GeckoLib3.0.57和4.9.3源码分别在`tmp/skill-render-source/`和`tmp/skill-render-target/`。本批没有修改雷劫时序、伤害或新增测试；最终build/ArchUnit及检查源集编译通过，现有tribulation五项通过并DONE，四世界正常保存退出、ERROR/FATAL0，劫云截图可见。首次字体初始化阻塞、运行配置还原及详细证据见[技能渲染复核](../skill/architecture.md#动画实体渲染逐类复核2026-11-07)。完整视觉及两版本对照仍未验收。

Q56（轻微，保持来源行为）：`ThunderBegin` 的负功德分支只消耗每满 50 的负功德，局部 `gongdeyeli` 没有加到雷数，`meritslight` 实际仍为 0；正功德抵消旧雷数的循环上界每次读取正在减少的旧雷数，例如旧雷数 2、功德 100 时只抵消一次。云每五刻发现玩家死亡只清玩家开关，清最大组数与增加失败雷数误写了云的玩家能力对象，玩家这两项不变；云被杀的独立过程则正确清最大组数并给玩家失败雷数加 1。`tiaoguo` 清标记并宣布成功、设置进阶和清开关/最大组数，但没有清云序列或移除云，后续仍会放雷；玩家离开本维度时云移除，玩家状态不补写。以上分支均不新增修正或终止。

验证：`build compileSmokeJava`（含 ArchUnit）通过；仅一次 `runSmokeClient -PsmokeOnly=tribulation`，5 项主路径断言全部符合并 `DONE`，覆盖满修为自然开始、功德动态抵消、真实云 tick 的范围雷伤、序列成功、三类实体的生成与结束、劫云被杀失败。客户端正常保存退出，日志 ERROR 0；劫云截图已查看，模型可见。专用服务器一次启动到 Done，ERROR 0，导出实体 67 个，使用原生 `stop` 正常保存退出。证据在 `tmp/tribulation-build.log`、`tmp/tribulation-check-compile.log`、`tmp/tribulation-smoke.log`、`tmp/tribulation-report.txt`、`tmp/tribulation-server.log` 与 `build/smoke/screenshots/tribulation-cloud.png`。未重跑旧神通；未执行 1.18.2 版本实机、两版本对照、多人、全分支时序与完整视觉认可。

## 修炼、观想与功法参悟页（2026-11-15）

来源核对 `A:PlayerGUI06/07/08Screen`、同名Menu与ButtonMessage、`PlayerGUI06shouming/tianshu`、六个 `PlayerGUI06anniu01–06`、`PlayerGUI06xiulian`、`PlayerGui08xiulian`、`Guanxiang`、三页说明与 `Qiehuan01–03`。目标 `RetreatMenu/RetreatScreen` 保留三个注册ID、500×500布局、36格原生背包、寿命/日数的 `DecimalFormat("0")`、按钮ID与原图坐标。服务端按钮保留菜单坐标所在区块存在条件；客户端只发送原生菜单按钮编号。

日数保存在xiulian体质载体的 `xiuliandaynum`：加1仅在寿元减日数大于1时增加；加10/100仅在差额大于对应量时增加，否则直接设寿元减1。减1仅在日数大于1时减少；减10/100在日数不足对应量时归零。开始修炼按来源先检查骑乘、闭关天数不大于0、设定至少1天、冷却小于100；内部再按功法名和研习者判断，成功后设置剩余天数、清设定、写开关“修炼”并关闭菜单。参悟沿用同顺序但不检查冷却，开关写“功法”；开始与失败中文提示保留来源。实际后续逐秒推进复用已有 `Cultivating` 和gongfa功法修炼入口。

功法归属由 `RetreatTechniqueEvent` 向gongfa查询；gongfa按自身当前实例回填未装备、未研习、他人所有和本人所有四种结果，xiulian不读取功法数据。观想在玩家位置周围8格AABB中按距离扫描，跳过玩家和非Mob；非本人驯养妖兽阻止，随后检查两类坐具并提前结束扫描，其余非AmbientCreature且非Animal的Mob阻止。受阻只提示且保留菜单。安全时寿元必须大于3，罡煞至多三次向0移动1，施加60刻20级减速，再发 `MeditationRefineEvent` 交cooking按原条件炼化酒精；随后提示、扣3天寿元、灵力与精力补到最终上限、生命加30，并关闭菜单。寿元不足只提示大限将至，仍关闭菜单；观想不要求骑乘。

06→07、07→06/08、08→07沿用同一菜单坐标，不修改主分页6；K记页6只恢复06。所有其他主分页入口由panel统一打开并沿旧坐标Y+1，xiulian不导入panel。三个来源说明分别保留四行、一行、三行。新增语言片段各18个导航键与锁定来源相同；05和06/07/08的屏幕依赖及动态语言前缀已登记转换输入，缺失原图直接补入。

### 来源边界与集中复核

Q189（待裁定，保持来源）：`PlayerGUI06anniu04`减一天在1天时不能归零；`anniu02/03`加十/百天在寿元小于1时会将设定写为负数。

Q190（待裁定，保持来源）：`Guanxiang`扫描先遇 `shitiputuan/shitiyizi` 便对整个循环break，可能使更远的威胁不再被检查；仅对Mob扫描的前提也保持来源。

Q191（待裁定，保持来源）：`PlayerGui08jieshao`称“没有时限”，实际 `PlayerGui08xiulian` 启动仍要求至少1天并设置闭关天数，`Kaishigongfa` 每秒减一天并按剩余天数结束。文字与执行矛盾留集中复核，不修正这些规则，也不逐项中断独立移植。

静态来源、按钮、资源与语言核对、定向转换及生产/Smoke编译通过；06/07/08受控按钮、日数、观想结算、K记页和跨页必要单项通过，三页截图已复看。证据见[本批集成记录](../project/logs/2026-11.md#2026-11-15-页面命令阵法与展示家具集成验证)。完整骑乘自然修炼、专服、页面专用真实保存重进、多人、1.18.2版本实机和两版本对照未执行。

## 原初管理命令（2026-11-15）

来源 `A:command/YvanchuCommand:26–70` 的唯一注册和玩家选择由 `flow.PlayerCommandFlow` 承担，本系统通过 `Cultivation.command` → `CultivationCommand` 只写自己的档案、主世界日收益上限与游戏规则。权限仍为1，目标仍是命令源当前维度首个显示名精确匹配玩家；本系统不另注册同名命令。源码范围与锁定哈希见[来源证据台账](ledger/source-evidence.csv)，有限真实命令已随本批场景运行，边界见本月志。

数值语法为 `/yvanchu <玩家显示名> <action> <subject> <operation> <value> [true]`。`set`是唯一赋值操作，其他任何操作都累加数值；`Double.parseDouble(trim)`读负数、小数和科学记数法，格式错误/空参数取0，不额外截断。以下分支均已接：

| action / subject | 写入与顺序 | 来源与反馈 |
| --- | --- | --- |
| `levelup level` | 设置或累加`LEVEL_NUMBER`，可选反馈后把`EXPERIENCE_MAX`改成`100 + 层数³ × 50`；不触发升级成长、境界名/金丹/元婴或属性点补发 | `Go:29–78`；set显示原参数“等级设置为”，加减显示写入后的“等级变更为” |
| `experienceup experience/experiencestand` | 分别写已有/待吸收修为；不立即吸收或突破 | `Go2:20–61/483–524`；两个分支都沿用“经验设置为/经验增减量为” |
| `experienceup rate/hinder` | 分别写修为吸收率/道途阻力 | `Go2:525–608`；显示“修为吸收率”或“道途”的原参数 |
| `experienceup skillpoints/vigorous/merits` | 分别写剩余属性点/罡煞/功业 | `Go2:609–710`；来源没有成功回执，传true也不补消息 |
| `experienceup bodygold/bodywater/bodytree/bodyfire/bodysoil` | 改对应灵根；金/火set按`旧阴阳 + 新根 − 旧根`，加减按`旧阴阳 + 参数`；水/木set按`旧阴阳 − (新根 − 旧根)`，加减按`旧阴阳 − 参数`；土不改阴阳 | `Go2:711–908`、`Go3:24–131`；原“某灵根设置为/增减量为”加原参数；灵根/阴阳继续提交attribute |
| `experienceup lifetime/liftlock` | 分别写寿元天数/出身锁定次数，不重置出身或消耗标记 | `Go3:132–215`；显示“寿元”或“命轮锁”的原参数 |
| `experienceup dailyexperience` | 写同一主世界`XiulianWorldData.dailyExperience`，保留double和0不限制的消费规则；不写玩家今日已用额度 | `Go3:527–558`；set回显原参数，加减回显新上限 |
| `experienceup linggenlevel` | 写命令源世界的同名游戏规则；set后转int，加减先与旧int相加再转int | `Go3:559–591`；set回显原参数，加减回显新int |

上述可选反馈仅当最后参数恰为`true`时发给目标，保留原金色与中文前缀。例：`levelup level set 2.5`只改层数和门槛881.25，不额外发放属性点；旧金根3、阴阳5时`experienceup bodygold set 8.5`得到根8.5、阴阳10.5；旧灵根随机上限9时`experienceup linggenlevel minus -1.5`先算7.5再转成7。原来的职业等级命令仍不联动经验上限，与本分支不同。

`unlock chongzhilinggen`和`unlock chongzhilinggenmax`（`Go:532–535`）接回既有`resetRoots/maximizeRoots`；随机仍固定0–9，不读取`linggenlevel`，满灵根仍五行9/阴阳0，两个操作沿用自身无条件中文回执。这里补的是命令到既有操作的真实接线，历史公开操作Smoke不等于本次命令已经验收。

`unlock tizhi <add或其他操作> <键> [true]`（`Go:516–531`）按来源对任意体质载体键写布尔：仅add写true并使`tizhinumber +1`，其他操作写false并使计数−1；重复添加/移除不先检查标记，也不加减体质属性修正。可选true发送“获得名为/移除名为…的体质”。该计数可能与实际打开标记数不一致，保留来源，并沿用Q24的丹田排序边界，不自行去重或重算计数。

`unlock|lock rebirthgui [true]`（`Go:542–548/565–571`）只切`jinruyouxichushenxvanze`，可选true使用原无色中文回执，真正首次进入仍走既有`BirthSelection.onLogin`。`open rebirthgui`调用轮回37，仍随37跨所有者清理契约处理，本批没有接入；执行目标疑点见下文Q193。

### Q193 轮回命令的执行者与目标不一致

来源`A:YvanchuGoProcedure:25–27/580–583`先按参数匹配当前维度目标玩家，但`open rebirthgui`把`Fulutexiao37`施于命令执行实体`entity`，而非匹配到的`entityiterator`；坐标仍来自命令源位置且Y加1。以管理员A执行`/yvanchu B open rebirthgui`为例，匹配B后进入分支，真实传入轮回过程的是A。无执行实体来源使用FakePlayer，仍不是B。该行为来自锁定源码静态核对，两个版本实机均未执行。状态为待裁定/未实现；本批不在37跨所有者清理契约之外单独接入此入口，也不擅自改成目标B，后续按统一问题索引集中处理。

锁定来源核对、命令接线及生产/Smoke编译通过；真实修炼/五行命令与本人同步共同101项必要检查通过。随机/满灵根、任意体质标记、轮回37、命令专用保存重进、1.18.2版本实机及两版本对照未验；本批不改变历史核心批次的已验证结论。`killentity* / killentitypoints*`是combat伤害规则维护的五行击杀数量与属性技能熟练度，目标现由`ElementStatistics.command`接回同一载体键，见[战斗正文](../combat/architecture.md#五行击杀统计管理命令2026-11-15)。本系统的丹田与金丹/元婴继续通过自有载体查询读取；不新增xiulian→combat依赖或第二份属性统计。

## 出身分次补给（2026-11-15）

来源为`A:Ling18Item:33–41`、`A:JiazubaibaodaiItem:32–40`及其同名右键过程（完整范围与SHA256见[来源证据](ledger/source-evidence.csv)）。两件物品沿用父类use结果，不消耗、不替换载体。目标`BirthSupplyItem`只在服务端核条件并把double计数写入物品`CUSTOM_DATA`，随后同步发出`BirthSupplyEvent`；`flow.BirthSupplyFlow`按新次数精确命中来源整数分支。非整数次数继续自增但不截断成有效奖励。修炼系统不反向依赖技能、装备或符箓；业务实例分别通过Materials、Fulu、Liandan、Skills、Caskets公开操作构造，普通材料不附加实例等级。发奖使用原生`ItemHandlerHelper.giveItemToPlayer`，保持入包与满包掉落路径。

传承灵物`ling_18`使用`yikaiqi`：旧次数小于30且当前境界层数严格大于旧次数时先自增，再按1–29次奖励。无冷却；境界不足提示“§4当前境界没有能取出的宝物了，请努力修炼！”，耗尽提示“§4传承灵物已空。”。完整固定表见`BirthSupplyFlow.inheritance`：来源显式写过的材料等级/品阶由Materials保留；第4次46号符、第19次材料保持默认实例；第10次09号符只写品阶2、不生成品阶文字；第14/20次玄阶、第24次地阶文字保留。第2次十张杜康酒配方纸保持自定义名，第23次以领取玩家为权限4执行者进入真实`/shenfa`命令，保留原文参数，不另造书定义。

家族百宝袋`jiazubaibaodai`使用`geiyvcishu`：仅出身“世家纨绔”通过，先提示“认证成功，是家族子弟。”；旧次数小于20时先自增，按1–20次发奖，随后原生冷却20刻。不设境界或日期门槛；耗尽仍先认证成功再提示“§4已全部领完。”，不追加冷却；非家族出身提示“§4认证失败，非家族子弟。”。20个来源分支共有101次普通栈发放，加上2瓶丹药、5个配置宝匣与2本技能书共110次发放。普通材料均为默认实例。第2次回春养元丹10份、2效果（再生2级120来源单位，016效果1级30）；第6次固本培元丹8份、015效果1级60；第4次飓风扇与第8次四山甲装备保持宝匣及完整lianqi命令，不预先变成成品。第10次踏风步、第12次激流波保留各字段和第14次飞行法器额外奖励，完整普通材料/食物顺序见`BirthSupplyFlow.FAMILY`。

### Q197 传承灵物第三十次无奖励

`A:Ling18DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:22–24,237–255`允许旧次数29且境界大于29时自增到30，但奖励链只到29。当前为待裁定，目标保持自增与空奖励，不补造第30份；实现已接，实机未执行。

### Q198 家族袋激流波的承载与身份不一致

`A:JiazubaibaodaiDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:436–453`实际创建`book_020`，却写`belongToItemID=book_005`及技能“激流波”；水冷却20、木冷却−4、水冷却忽略30均按来源保留。目标实际载体仍为020，技能类型为SPELL，来源身份005保留在技能书texts中。当前为待裁定，不改为005物品，不改为身法；实现已接，实际阅读/研习消费仍待验证。

### 远古石盘的外部依赖

`A:Ling11DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:34–36`读取MapVariables的全局传送阵三轴。当前集成基线已有World首次登录生产、Overworld持久化及`WorldStructures.teleportArrayCenter(ServerLevel): Optional<Vec3>`，查询与玩家当前维度无关；石盘自然use已接入并以非零坐标完成首次客户端检查。来源MapVariables三轴默认0，八出身奖励在查询为空时仍发“0 0 0”纸并写石盘double零值。World监听先于出身菜单监听的静态装配关系不代替首次登录完整时序或坐标持久化实测。

传承物的落魄家族完整初始生产者已由[初始奖励批次](#落魄家族初始奖励2026-11-15)接回并注册flow；扩展验证仍待执行。家族百宝袋未在来源procedures中找到直接give引用，结构/商铺生产者待核，不据此补造发放入口。

验证边界：本批静态核对两件物品和全部奖励分支，新增`BirthSupplyCheck`覆盖真实use门槛、严格比较、小数次数、特殊实例、Q197/Q198、耗尽与载体序列化重读；未改SmokeRun选择器。转换、Gradle、客户端、满包掉落、真实重登、完整获取/消费与1.18.2版本实机/两版本对照均未执行。夹具恢复库存、原境界和出身；仅序列化重读不能替代保存退出重进。

## 落魄家族初始奖励（2026-11-15）

静态核对`A:PlayerJoinGUIMenu:31–64`、关闭过程`PlayerJoinGUIDangZheGeJieMianBeiGuanBi:16–56`、`PlayerJoin02:115–139/409–501`及`PlayerJoinGame:69–74/218–235`，源码指纹见[来源证据](ledger/source-evidence.csv)。目标菜单保存打开时的维度，每次服务端关闭均交给`BirthSelection.close`，等待第二个服务端结束刻，先发`BirthInitialRewardsEvent`，再应用体质并使`tizhinumber`加1。出身和体质均在延迟执行时读取；空出身不发该包，空体质仍按来源落入纯阳分支。普通登录仍由五根和小于1且出身规则开启的既有门槛决定是否开页，没有额外登录发奖入口或一次性领取标记。

`BirthInitialRewardsFlow`既有“落魄家族”分支按实际发放顺序给五级四品`cailiao_191`二十件、`ling_18`、功法藏宝书、技能藏宝书。两书保持`book_021`，由ShelfBooks构造中文名和来源字段，材料由Materials构造；之后按菜单打开维度和执行时玩家坐标，以权限4、无实体命令源执行原`playerchuansuo`，最后应用体质。函数只作目标单数目录适配，原维度/选择器/坐标保留。其余七种扩展见下文。

### Q200 出身传送使用无实体最近玩家选择器

来源`PlayerJoin02:437–444`对五种出身以无实体命令源执行函数，函数原文为`execute in yvanchuxiuzhen:dimensionrenjian run teleport @p 0 32 0`。它在切换至人间道的执行上下文中用`@p`选最近玩家，没有明确绑定关闭菜单的玩家；在多人或跨维度场景可能命中人间道内其他最近玩家。这是静态推断，两个版本实机均未执行。状态为待裁定、目标保持来源；本批不把选择器改成本人，未声称传送已验证。

当前集成基线已注册`BirthInitialRewardsFlow.register()`和`birthinitialrewards`选择器。既有夹具检查落魄家族服务端菜单关闭、同调用未提前发奖、四栈顺序/实例/中文、两书服务端use及空出身/体质边界；八出身扩展已通过`compileJava`与`compileSmokeJava`。2026-11-17单项客户端运行到八分支断言后，在公共停服阶段卡于`ChunkMap.processUnloads`，报告停在`WAITING_FOR_WORLD_EXIT`，线程/堆证据见`build/diagnostics/birthinitialrewards-save-hang-20261117/run-01/`，因此该次不产生出生奖励行为通过结论；后续修订夹具的有效单项结果见下文和系统月志。未精确采样第一/第二个服务端结束刻；真实保存重进、满包、完整自然消费、多人、专服与两个版本实机仍未验。

## 出身初始奖励扩展（2026-11-17）

来源`A:PlayerJoin02Procedure:31–436,437–444`逐分支核对八种出身初始奖励、临时物品栈最后给出顺序及五种出身传送条件，SHA256见[来源证据](ledger/source-evidence.csv)。目标沿用既有`BirthSelection.close`第二个服务端结束刻事件和已注册`BirthInitialRewardsFlow.register()`；已集成的扩展flow，出身仍于延迟执行时读取，普通登录没有额外领取入口。来源归属主表将`PlayerJoin02`跨系统奖励归flow，体质部分由xiulian继续持有；`Ling11`物品与右键过程归xiulian，独立消费已集成，首次use结果见石盘正文。

- `世家纨绔`：立即给`ling_10`、“一小包灵石”宝箱2件和`book_021`“灵步身法”；公共尾段再给同一宝箱1件，然后给藏宝功法书、藏宝技能书与`book_030`“精铁剑图纸”。来源`PlayerJoin02:54–65,76–95,409–435`保持两次发放和同一临时栈；空背包路径实际合计2件，不能把调用数量直接相加，别名与库存分支见[Q228](#q228-世家礼包复用已入包物品栈)。目标保留调用顺序与实例共享。宝箱为宝箱/灵石、等级1、随机2–7。身法为MOVEMENT、品阶/境界1、灵根要求“无”、冷却20/20、四向跳0.7，原简介保持。图纸hover名称“精铁剑图纸”、配方名“精铁剑”、载体身份`yvanchuxiuzhen:book_030`，通过Lianqi三参构造操作写入；产物为`weaponsword_01`，三个配方格均为`cailiao_201`×3。
- `凡人奇遇`：顺序给“无名修士储物袋”（宝箱/综合、等级1、固定8–8）和“一个装有修仙武器的宝匣”（`baoxiaitem`），其中完整`lianqi`风语弓配置命令保留来源文本。
- `失魂士兵`：生命5、饥饿2、饱和度0；穿戴损耗51/41/14/51的铁头盔/胸甲/护腿/靴，背包给损耗114的铁剑和烤马铃薯1。
- `他乡故知`：依次给`ling_09`、铁斧、`qiankundai_01`、烤马铃薯8。
- `远古秘辛`：五级四品`cailiao_191`×5、`book_030`文本信件、锋利I铁剑。信件由BookTexts写原中文名称与根`textdisplay`，继续交给书籍自然阅读入口消费。
- `落魄家族`：沿用既有四栈顺序，五级四品`cailiao_191`×20、`ling_18`、功法藏宝书、技能藏宝书。
- `无良商贾`：先给绿宝石28、面包12并穿戴无损皮甲；尾段再给纸，名称按`DecimalFormat("0")`格式化World全局三轴、以空格分隔。首次坐标生产前按来源三轴默认0，仍发“0 0 0”纸。
- `流浪侠客`：不祥之兆120000刻、放大器5；先给面包32，穿戴每件独立随机损耗1–30的铁甲，再给同时带锋利I/亡灵杀手I的钻石剑及`ling_11`。石盘保存发奖时全局坐标的三个double副本`mubiaoX/Y/Z`；坐标未初始化时也明确写入零值。右键指引由xiulian独立消费实现，首次use和引导画面边界见[石盘正文](#远古石盘引导2026-11-17)。

五种来源传送分支为`失魂士兵`、`世家纨绔`、`落魄家族`、`远古秘辛`、`凡人奇遇`。奖励后仍以权限4、无实体命令源执行原`playerchuansuo`函数，Q200最近玩家选择器保持来源待裁定。

跨系统实例分别由Materials、ShelfBooks、BookTexts、Skills、Lianqi、Caskets和TreasureContainers的现有公开操作构造。石盘三轴副本由xiulian新增`BirthItems.ancientStoneDisc(Vec3)`写入，flow只传坐标与设置本次原版装备/状态，不直接写业务私有字段。`WorldStructures.teleportArrayCenter(ServerLevel)`已在当前集成基线存在，查询独立于玩家当前维度；其他系统已有API无需随本包重复实现或覆盖。

扩展`BirthInitialRewardsCheck`经真实服务端菜单关闭覆盖八种奖励及空出身，检查延迟同调用内未提前发奖、完整物品实例/中文/顺序、世家空背包两箱与身法、图纸载体身份、士兵状态、侠客双附魔和随机护甲，商贾纸与侠客石盘副本对照World实际坐标。复用已存在`birthinitialrewards`选择器；代码已编译；首轮退出卡点保留，随后正常退出轮发现世家3件预期错误，已按Q228更正为空背包2件；士兵状态改为发奖同刻采样后，同一受控单项通过并保存退出，证据见[系统月志](logs/2026-11.md#2026-11-17-世家初始礼包同栈回写与夹具更正)。士兵生命/饥饿/饱和通过LOWEST奖励事件观察者在生产flow完成的同刻读取，避免和平难度10/20刻自然恢复污染取值；观察后注销，恢复阶段再次清理。该夹具未精确采样第一/第二个结束刻；服务端use书籍样本也不等于全部自然消费验收。首次登录完整时序、满包掉落、真实保存重进、多人Q200、专服、1.18.2版本实机及两版本对照均未执行。石盘使用和完整表现不属于本包闭合结论。

### Q228 世家礼包复用已入包物品栈

来源`PlayerJoin02Procedure:54–65`构造临时礼包栈并设数量2，`409–414`再次对同一个对象设数量1并发放。精确核对Forge 40.1.0源码包中`ItemHandlerHelper:149–167`、`PlayerMainInvWrapper:22–32`、`InvWrapper:80–90,111–140`：Helper传入原对象；空槽且数量未超过槽位上限时，包装器把原对象直接保存到槽位。尾段的`setCount(1)`会同时把已入包的两件改为一件，再与这同一槽合并一件，因此空背包最终为2件。既有同类栈的合并路径会复制对象，最终数量取决于实际库存分支，不能把所有入口统一写成2或3件。

NeoForge 21.1.265同名Helper和包装器保留上述路径；目标`BirthInitialRewardsFlow.give`沿来源共享同一栈。此前“Helper未消耗临时栈所以合计3”的记录遗漏了别名回写，已更正。Q228状态为静态已核实、保持来源；若要消除库存相关差异须另行裁定，本次未改生产代码。1.18.2版本实机、已有同类栈与满包掉落未验证；当前夹具明确只断言空背包路径。

## 人间道个人入场（2026-11-18）

1.18.2 版本 `YijichushishengchengProcedure` 的个人分支在每次实际入场执行，不依赖世界首次初始化。1.21.1 版本 `RenjianArrival` 消费 World 的 `RenjianEnteredEvent`：有 `shengxianling` 时立即清标记，80 个服务端结束刻后移除缓慢与漂浮；没有 `chuanjiegujing` 时依次给一件 `fabao_013` 和一件 `qiankundai_06`，然后标记已领取。两个个人字段沿用修炼档案并按既有接口同步；世界场景只由 [World](../world/architecture.md#人间道固定首入2026-11-18) 管理。

## 远古石盘引导（2026-11-17）

归属主表`ownership_assignment.csv`的`Ling11`物品与同名右键过程均明确归xiulian；`PlayerJoin02`发奖归flow。本包只接石盘自然use和自身`zhuangtai`，不写World坐标、不增加Equipment或Treasure实现。来源物品use返回父类结果，目标`Ling11GuideItem`同样保留返回值，由服务端`Ling11Guidance`读取World公开查询。精确来源范围与哈希见[来源证据](ledger/source-evidence.csv)。

只有主世界执行引导。坐标和非0时保留来源斜率，沿X方向取5个点，各点在玩家Y+1发送3个`lingqi`，散布0.1、速度0；空`zhuangtai`发送玩家名及“好像有一道气流在指引我。”并写“追踪”。已有“追踪”状态按来源条件顺序显示大阵、上方、接近、下方或五种独立概率闲语，其他状态仍发粒子但不追加提示。非主世界提示“好像失灵了？”，全局值未初始化或三轴和为0提示“一个空的石盘？”。右键总是重读全局坐标，不消费奖励时物品保存的`mubiaoX/Y/Z`副本。

### 远古石盘坐标哨兵与距离表达式

来源`A:Ling11DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:37–64,65–102`用三轴坐标和是否为0判空，已生成的非零坐标也可能落入空盘；粒子斜率分母为`targetX-playerX`，玩家X与目标X重合时可能产生NaN/Infinity；提示使用`abs(playerX-targetX+playerZ-targetZ)`，两个方向差可能抵消，不能等同平面距离。候选保留原式、条件顺序和五种独立随机调用。上述影响是静态推断，零和坐标、X重合和抵消距离的客户端表现及两版本实机均未执行；尚未分配公共问题编号，由主agent集中登记。本节不用旧候选的临时编号。

工厂`xiulian:ling_11`、单件内容表和使用实现已由`28391ef9`集成；实际语言片段英文名为`Ling 1 1`，最终合并`zh_cn.json`及运行截图中中文名为“远古石盘”。`Ling11GuideCheck`已接入`ling11`选择器，只读取World已有自然中心，保持出生区域并转向目标，不写全局坐标。2026-11-18在视距4、模拟距离5、全粒子及`zh_cn`下取得6 INFO、0 FAIL、DONE：首次真实客户端use、服务端/本人追踪状态一致、局部气流粒子和中文物品名画面通过，所有维度正常保存、Gradle退出0。截图、报告与日志见`build/diagnostics/ling11-guidance-20261118/run-05-visual/`。五点完整数量/排列、全部提示分支、自然奖励获取、真实保存重进、多人、专服、1.18.2版本实机与两版本对照仍未执行，不记为完整视觉或整系统验收。
