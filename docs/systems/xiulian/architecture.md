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

### 来源问题

- Q21（轻微，保持来源）：随机体质只取 1–10，来源列出的第 11 种“风动玉髓体”、第 12 种“纯阳合剑体”随机取不到；关闭界面时不是前 11 种的体质（含空体质）都按第 12 种生效。体质锁定键 `tizhilock` 只被读取，没有写入者，体质总会随灵根一起重随。

### 验证

- 开发客户端 `runSmokeClient -PsmokeOnly=xiulian`：开启出身规则后按登录流程随机灵根（0–9）与体质（前 10 种），阴阳公式、火灵根提交到 attribute 的五行系数；经网络按钮锁定金灵根、随机（金灵根不变、随机次数减 1、体质取自记分项）、解锁返还次数、出身前后切换；关闭界面后体质标记、修正值与计数；公开操作的加减同步到客户端；死亡重生与退出重进后保持。2026-11-03 实测 34 项全部符合，截图 `xiulian_birth.png`。
- 一次冒烟运行中客户端在退出存档时无日志终止（`Saving worlds` 之后），之后三次运行未复现；记录待观察。
- 未执行：1.18.2 版本实机对照；多人场景。
