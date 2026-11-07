---
topic: xiulian
role: architecture
status: draft
updated: 2026-11-07
---

# 修炼、身份与生命周期：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:PlayerTickProcedure:64–86` 按世界时间间隔区分修炼和功法研习，之后计算属性、副本、本命法宝及元婴出窍。`experience`、`experience_Stand`、`experience_Max` 分别参与已有修为、待吸收修为和突破门槛；不可合并成一个经验字段。日收益上限来自主世界 `dailyexperience`，已用额度放在玩家 `tizhiitem` 中（同文件 97–155）。

`A:LEVELupProcedure` 实际调用金丹、元婴、境界修正和升级表现。职业升级有独立 `FuzhiyeLevelUpProcedure`；只有职业字段存在并不证明对应职业拥有完整生产流程。

玩家能力类的 166 个字段中，157 个在所有 Clone 事件复制；六种 `colddown*`、`gongjian`、`gongjianjineng`、`chuansongitem` 共 9 个只在非死亡复制。另一个 Mod 是 15 + 2，后两项为 `bankceshi1/2`。这是普通 Clone 的源码规则，不覆盖轮回符的主动重置。见全量 [生命周期台账](../../reference/legacy/inventory/state_lifecycle.csv)。

## 目标所有权与接口提案

玩家成长档案保存境界、出生、种族/血脉、灵根、寿元及资源池；体质/金丹/元婴采用各自结构。`tizhiitem` 是混合数据载体，其副本、装备培养和属性贡献交给对应系统，不能把其整个 CompoundTag 再包装成“成长对象”。

公开操作包含修为获得/吸收、资源支付/恢复、开始与结束闭关、突破、出生选择、普通死亡处理和轮回处理。闭关协调功法服务，功法更新书籍实例；成长服务不修改功法组件内部字段。职业进度由所属生产或生物系统维护，共用升级公式只在规则确实相同时提取。

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
- 出身界面（`PlayerJoinGUI`，归属由 panel 改为本系统）：五个灵根锁定按钮（消耗与返还 `liftlock`），随机按钮（消耗 `randomlift`，重随未锁定的灵根与体质；体质经记分项 `suijishu` 写入再读出），出身切换（八种出身循环），显示寿命、灵根、体质、出身说明与星数。关闭时体质生效：打开 `tizhiopen_<序号>`，按体质加修正值（阴阳双生体另加修为效率 0.3），`tizhinumber` 加 1。
- 关闭后 2 tick 的 `PlayerJoin02` 其余部分（按出身发放各系统的物品、部分出身执行函数 `playerchuansuo`）归 flow，待物品所属系统实现后接入。`PlayerJoinGame` 中每次登录把原版 `keepInventory` 设为真、传送阵生成等部分归 flow 与 world，尚未实现。

### 阶段 3：修炼推进、突破与恢复

- 每秒推进（来源 `PlayerTick` 的修炼部分，`CultivationTick`）：世界时间与 `tizhiitem.jishiqi` 相差 20 时执行一次，时间倒退时重设基准。依次为闭关修炼（“修炼”开关）、发出 `CultivationSecondEvent`（gongfa 在此推进“功法”开关的功法修炼）、吸收待吸收修为（× 修为效率，另加 `EwaiExperience*`；雷劫中清空；主世界数据 `XiulianWorldData` 的每日上限非 0 时截断并提示，登录时按现实日期清零当日额度）、妖化冷却、阳寿已尽提示、突破、闭关冷却（每 15 秒减 1，受 `XiulianColdCorrect` 加速）、恢复。
- 突破：修为达到所需值时，在 `fabao_009weidu` 与 `xvkong` 维度停在所需值减 1；非创造模式且层数为 20、30、40 或 41 以上时开启雷劫并把修为置 1；否则 `LevelUp`：层数加 1（不超过 42）、属性点 5 + ⌊层数 × 0.1⌋、按 `LevelupCorrect*` 修正成长（成长量累计后作为 attribute 的基础层贡献 `xiulian:growth` 提交）、1/11/21/31/41 层与 41 层以上加寿元（7300、36500、73000、146000、292000、18250），11、21、31 与 41 层以上另按 `RealmupCorrect*` 修正；大境界播放 `levelup`、其余 `littlelevelup`；闭关中另加闭关冷却；写升级文字 10 行，20 tick 后（记分项 `zdzttick` 为 0 时）打开升级界面。
- 闭关修炼（`Cultivating`）：见类注释；给主修功法加经验的部分改为发出 `CultivationExperienceEvent`，由 gongfa 处理（依赖方向）。主修功法的名字、品阶、层数由 gongfa 提交 `MainTechnique`。
- 恢复（`Recovery`）：恢复量读 attribute 的最终值；战斗（记分项 `zdztbegin` 为 1）或雷劫中为四分之一；效果 `effect_007` 期间不恢复生命；主修“繁星呓语”夜晚露天无雨灵力恢复 × 1.1。
- 尚未迁移：境界提升粒子 `Jingjietishengtexiao1/2`（particle）、阳寿已尽 40 tick 后的轮回 `Fulutexiao37`、酒劲消化（`jiujing`，cooking）、来源每秒复制到 `power_display`、`soul_display` 的显示字段（只供界面）。

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
- 升仙令（`AscensionTokenItem`）与家族令牌（`ClanTokenItem`）：见类注释。远古石盘读 world 的传送阵位置并发 particle 的粒子，传承灵物与家族百宝袋发放带 material、fulu 实例数据的物品，随这些系统的数据接入。
- 读后改变归属：元婴出窍（`Yvanyingchuqiao` 等 6 个元素）是技能界面施展的技能，归 skill；被闪电击中时按主修功法给效果的 `EntityThunder` 归 gongfa；境界提升粒子留在本系统，随 particle 接入。
- 完成核对（2026-11-03）：`gradlew check`、`check_ledger.py`、`check_docs.py` 通过；开发服务端启动 ERROR 0 条；冒烟 relation、attribute、xiulian 共 175 项全部符合（其中 xiulian 131 项），日志 ERROR 0 条。

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

Q56（轻微，保持来源行为）：`ThunderBegin` 的负功德分支只消耗每满 50 的负功德，局部 `gongdeyeli` 没有加到雷数，`meritslight` 实际仍为 0；正功德抵消旧雷数的循环上界每次读取正在减少的旧雷数，例如旧雷数 2、功德 100 时只抵消一次。云每五刻发现玩家死亡只清玩家开关，清最大组数与增加失败雷数误写了云的玩家能力对象，玩家这两项不变；云被杀的独立过程则正确清最大组数并给玩家失败雷数加 1。`tiaoguo` 清标记并宣布成功、设置进阶和清开关/最大组数，但没有清云序列或移除云，后续仍会放雷；玩家离开本维度时云移除，玩家状态不补写。以上分支均不新增修正或终止。

验证：`build compileSmokeJava`（含 ArchUnit）通过；仅一次 `runSmokeClient -PsmokeOnly=tribulation`，5 项主路径断言全部符合并 `DONE`，覆盖满修为自然开始、功德动态抵消、真实云 tick 的范围雷伤、序列成功、三类实体的生成与结束、劫云被杀失败。客户端正常保存退出，日志 ERROR 0；劫云截图已查看，模型可见。专用服务器一次启动到 Done，ERROR 0，导出实体 67 个，使用原生 `stop` 正常保存退出。证据在 `tmp/tribulation-build.log`、`tmp/tribulation-check-compile.log`、`tmp/tribulation-smoke.log`、`tmp/tribulation-report.txt`、`tmp/tribulation-server.log` 与 `build/smoke/screenshots/tribulation-cloud.png`。未重跑旧神通；未执行 1.18.2 版本实机、两版本对照、多人、全分支时序与完整视觉认可。
