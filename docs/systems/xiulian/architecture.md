---
topic: xiulian
role: architecture
status: draft
updated: 2026-11-01
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
- 尚未迁移：雷劫过程 `ThunderBegin`（开启标记之后的劫云与天雷）、金丹与元婴获取（21、31 层）、境界提升粒子 `Jingjietishengtexiao1/2`（particle）、阳寿已尽 40 tick 后的轮回 `Fulutexiao37`、酒劲消化（`jiujing`，cooking）、来源每秒复制到 `power_display`、`soul_display` 的显示字段（只供界面）。

### 来源问题

- Q22（轻微，记录后继续）：境界名来源以服务端语言翻译语言键 `levelnameNN` 后存入，单人游戏随客户端语言、专用服务器为英文；目标统一存中文名（与初值“凡人”一致）。升级文字第 9 行法术防御增加时写的是“旧值 − 新值”，第 10 行神识攻防沿用“物理攻击”“物理防御”标签，按来源保留。
- Q21（轻微，保持来源）：随机体质只取 1–10，来源列出的第 11 种“风动玉髓体”、第 12 种“纯阳合剑体”随机取不到；关闭界面时不是前 11 种的体质（含空体质）都按第 12 种生效。体质锁定键 `tizhilock` 只被读取，没有写入者，体质总会随灵根一起重随。

### 验证

- 开发客户端 `runSmokeClient -PsmokeOnly=xiulian`：开启出身规则后按登录流程随机灵根（0–9）与体质（前 10 种），阴阳公式、火灵根提交到 attribute 的五行系数；经网络按钮锁定金灵根、随机（金灵根不变、随机次数减 1、体质取自记分项）、解锁返还次数、出身前后切换；关闭界面后体质标记、修正值与计数；公开操作的加减同步到客户端；死亡重生与退出重进后保持。2026-11-03 实测 34 项全部符合，截图 `xiulian_birth.png`。阶段 3 的 `XiulianProgressCheck`：修为转入、每日上限截断、突破（层数、境界名、扣除与重算所需修为、寿元、升级修正的生命成长、升级文字）、升级界面打开与关闭（截图 `xiulian_levelup.png`）、生存模式 20 层开启雷劫、灵力恢复、骑乘闭关的天数、寿元与冷却；连续 3 次运行全部符合。
- 冒烟检查在新建世界后约 10 秒内死亡并退出存档时，偶发集成服务端停在 `Saving worlds`（停止流程等待某个区块的保存条件，区块工作线程空闲），客户端随之卡住；xiulian 与 attribute 检查都出现过（2026-11-03 共 9 次运行中 3 次）。原因调查中，见[月度记录](../project/logs/2026-11.md)。
- 未执行：1.18.2 版本实机对照；多人场景。
