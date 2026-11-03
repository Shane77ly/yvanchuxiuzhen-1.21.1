---
topic: attribute
role: architecture
status: partial
updated: 2026-11-03
---

# 属性来源、派生与实现

来源事实与目标实现分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与调用

`A:ShuxingjisuanZongProcedure:27–122` 比较四件防具及主手；防具使用 `bubianzhi` 标记，变化调用 `ShuxingjisuanProcedure`，主手种类变化调用 `Shuxingjisuan2Procedure`。`A:PlayerTickProcedure:83,351–361` 还从定期更新与功法切换进入计算。

来源中同时存在 Variables 基础字段、`*_display`、实体实际属性、装备 `lianqizong`、功法属性和 `tizhiitem` 修正。`A:ShujvtongbuProcedure` 将部分显示/资源值写到实体 `synchronous*` NBT；其名为“同步”并不能独立证明网络同步已发生。

具体矛盾：`ShuxingjisuanZongProcedure:84–86` 处理腿部装备时更新了 `kuijia4` 的比较标记，而前面该分支保存的是 `kuijia3`。只影响是否触发重算，不影响算出的值；目标不复制该快照机制（见“目标实现”）。

## 归属阅读（2026-11-03）

9 个候选元素已读，结果写入[归属表](../../reference/legacy/inventory/ownership_assignment.csv)：

| 元素 | 归属 | 读到的规则 |
| --- | --- | --- |
| `Shuxingjisuan`、`Shuxingjisuan2` | attribute | 求派生值并写原版属性；其中读取防具、饰品、武器、功法与附魔的部分改由提交者提交贡献 |
| `Playershuxingjisuan` | attribute | 空过程（只声明局部变量），两个来源中都没有调用者，无行为可迁移 |
| `ShuxingjisuanZong` | equipment | 每 tick 比较四件防具与主手和快照 `kuijia1`–`4`、`wuqilinshi`，变化时触发重算，是装备变化检测 |
| `Weaponshuxing` | equipment | 妖化防具按部位加 `lianqizong` 的生命、击退抗性、攻速、移速修饰符，是物品自身属性 |
| `Attributexianshi`（原初系统） | equipment | 主手长枪（`forge:yispear`，`weaponspear_01`–`09`）加 `forge:attack_range` +2 |
| `Shujvtongbu` | xiulian | 把当前灵力、神识、修为与灵力、神识上限写入实体 NBT `synchronous*`；两个来源中没有读取者，写入无效果 |
| `Entitydaxiao`、`Yaoshoupifuxianshi` | creature | 妖兽与修者的实体属性（体型 0.7–10、皮肤变体 1–5），由生成与灵兽袋收放读写 |

本系统因此没有来源注册；两个实体属性的台账行随归属移到 creature。

## 目标实现（2026-11-03）

代码在 `system/attribute`：

- `api`：`Stat`（26 个属性项及初始值）、`Layer`（基础层、加成层）、`Contribution`、`DamageElement`（12 种伤害属性与所借用的灵根、功法项）、`PlayerAttributes`（[公开操作](README.md)）。
- `rule/AttributeTotals`：基础值 = 初始值 + 基础层贡献；最终值 = 基础值 + 加成层贡献；五行系数项与功法五行项。
- `data`：玩家附件 `yvanchuxiuzhen:attribute`，按来源键保存贡献（Codec 存档、死亡复制、只同步给本人）；基础值与最终值由贡献推导，不单独存档。
- `VanillaAttributes`：提交或撤销贡献后、以及登录时，把最终生命上限、最终攻击、最终移速写入原版 `MAX_HEALTH`、`ATTACK_DAMAGE`、`MOVEMENT_SPEED` 的基础值（来源同样用 `setBaseValue`，物品与效果修饰符在其上生效）。

两层的依据：来源的基础字段（`health_Max` 等）由修炼成长、单项提升、浮生丹与血灵晶写入，`_display` 另加防具与武器的炼器数值、饰品、主修功法与附魔；战斗、技能与法宝有几处只读基础字段（法术伤害读攻击方 `mana_Att`，`EntityBeforeHurt` 读 `attack_Def`，技能箭读 `attack_Num`，`Fabao010` 读 `mana_Att`），所以公开操作同时提供基础值。功法刷新 `Gongfashuaxin`、`Gongfashuaxin2` 也写基础字段，但生成代码中没有调用者（只在编辑器图中出现），按 gongfa 实现时核对。

与来源的差异：

- 不复制按 `bubianzhi` 随机标记比较装备快照的机制；装备变化由 equipment 监听并提交贡献（腿部分支写错快照的问题随之消失）。
- 装备类贡献随附件跨越死亡保留；来源靠快照与实际装备不同而在重生后重算，目标由提交者在登录、重生与装备变化时按当前状态重新提交。
- 来源在 `Shuxingjisuan` 开始与结束时在玩家位置各发一个粒子 `tiangandizhi`（`Shuxingjisuan2` 不发）。粒子类型由 particle 统一登记，尚未迁移；接入前本系统不发粒子（未实现项）。

## 验证

- JUnit `AttributeTotalsTest`：Q17 与 Q18 的算例（两层汇总、五行项），随 `gradlew check` 运行。
- `runSmokeClient` 的属性检查（`-PsmokeOnly=attribute` 可单独运行）：开发客户端的集成服务端上提交四个贡献，核对服务端最终值、基础值与三个原版属性，客户端收到的本人数据与原版最大生命；撤销一项后当前生命压到新上限；死亡重生、退出重进（存档前把原版基础值改成过时值）后贡献与原版属性保持。2026-11-03 实测 34 项全部符合。
- 未执行：1.18.2 版本实机对照；真实装备、功法与修炼的提交路径（随各提交系统实现时验证）。

## 来源事实核对（2026-11-03，C 开工）

静态核对原初修真 `ShuxingjisuanZong`、`Shuxingjisuan`、`Shuxingjisuan2`、`Weaponshuxing`、`Shujvtongbu` 与 `typed_field_accesses.csv`；未运行 1.18.2 版本。

### 字段

| 类别 | 玩家字段（PlayerVariables） |
| --- | --- |
| 基础值 | `health_Max`、`health_Rec`、`power_Max`、`power_Rec`、`soul_Max`、`soul_Rec`、`attack_Def`、`mana_Def`、`soul_Def`、`attack_Num`、`mana_Att`、`soul_Att` |
| 派生值 | 上述每项的 `<名>_display`；`Cooling_reduction`；`walkspeed` |
| 计算用的装备快照 | `kuijia1`–`kuijia4`、`wuqilinshi`（与实际装备比较 `bubianzhi` 标记以决定是否重算） |

### 公式（全部为加法，无乘算、上限与取整）

- 防具类九项 `<名>_display` = 基础值 + 四件防具 `lianqizong.<名>`（整数）+ 四个饰品槽 `headornaments`、`necklaceornaments`、`beltornaments`、`handornaments` 物品 NBT `<名>`（小数）+ 主修功法加成（功法物品 `belongToMasterUUID` 为本人时才计入：`ExercisesHeath`、`ExercisesHeathRec`、`Exercisespower`、`ExercisespowerRec`、`ExercisesSoul`、`ExercisesSoulRec`、`ExercisesAttackDef`、`ExercisesManaDef`、`ExercisesSouDef`）+ 四件防具的附魔等级（`dawn` → `health_Rec`，`powerup` → `power_Rec`，`attdef` → `attack_Def`，`manadef` → `mana_Def`）。
- 武器类三项 `<名>_display` = 基础值 + 手持武器（`wuqilinshi` 带 `forge:wuqi` 或 `forge:fabao` 标签）`lianqizong.attack_num` / `mana_Att` / `soul_Att` + 主修功法 `ExercisesAttack` / `ExercisesMana` / `ExercisesSouatt`。
- `Cooling_reduction` = 四个饰品槽与四件防具的 `colddown` 附魔等级之和 × 0.01。
- `walkspeed` = 0.1 + 主修功法 `ExercisesSpeed`。

### 写入实体属性

| 原版属性 | 写法 | 计入的来源 |
| --- | --- | --- |
| `MAX_HEALTH` 基础值 | `setBaseValue` | `health_Max` + 四个饰品槽 + 主修功法 `ExercisesHeath`；**不含防具 `lianqizong.health_Max`** |
| `MOVEMENT_SPEED` 基础值 | `setBaseValue` | `walkspeed` |
| `ATTACK_DAMAGE` 基础值 | `setBaseValue` | `attack_Num` + 主修功法 `ExercisesAttack`；**不含武器 `lianqizong.attack_num`** |
| 妖化防具（`forge:yaohuaarmor` 且按部位带 `toukui`、`xiongjia`、`kuzi`、`zhanxve`） | `ItemAttributeModifierEvent` 按部位加修饰符 | `lianqizong.health_Max`（加算）、`knockback_resistance`、`attack_speed`、`speed_num`（三者按基础值乘算） |

### 入口与消费者

- 入口：`PlayerTick` 定期与功法切换时调用；`ShuxingjisuanZong` 比较四件防具与主手的 `bubianzhi` 标记与物品种类，变化时调用 `Shuxingjisuan`（防具、饰品、功法）或 `Shuxingjisuan2`（武器）。`Shujvtongbu` 把 `power_Max_display`、`soul_Max_display` 与当前灵力、神识写入实体 NBT `synchronous*`。
- 基础值的外部写入：xiulian 的 `LevelupCorrect`、`RealmupCorrect`（全部 12 项）与 `HealthMaxUp`、`PowerMaxUp`、`SoulMaxUp`、`AttackUp`、`ManaUp`、`SoulUp`（单项）；gongfa 的 `Gongfashuaxin`、`Gongfashuaxin2`（全部 12 项）；fulu 的 `Fulutexiao37`（浮生丹，全部 12 项与 `Cooling_reduction`）；economy 的 `Cailiao200WanJiaWanChengShiYongWuPinShi`（血灵晶，`power_Max`）。`walkspeed` 另由 `PlayerLeaveGame` 写入。
- 派生值的读取：combat（`ManaDamage`、`DamageDefense`、`ManaDefense`、`SoulAttjisuan`、`SoulDefjisuan`、`Zhilingdamage`、`Zhilingdefence`，效果 010、011、017、018），xiulian（`Playerhuifu` 的回复、金丹与元婴流程读上限），skill（`Colddownjisuan`、`Jinengxiaohao`、`Jinengchuqiao`、`Jinengzibao`、技能箭），equipment（速射弓、法宝 014），relation（`Friendshuaxin`），gongfa（`GongfaxvexiZaiFangKuaiShangYouJian`），economy（魂灵晶），flow（`Entitybereborn`），panel 显示。

### 来源问题

- Q17（2026-11-03 负责人裁定选项 b）：防具 `lianqizong.health_Max` 计入 `health_Max_display`，但实际最大生命只在妖化防具上通过修饰符生效；武器 `lianqizong.attack_num` 计入 `attack_Num_display`，但不进入 `ATTACK_DAMAGE`。界面显示与实际属性不一致；战斗系统读的是 `*_display`（伤害与防御按显示值结算），原版生命上限与近战伤害按实际属性。算例：基础生命 20、胸甲炼器 +10、饰品 +5 时显示 35，实际最大生命 25（妖化防具为 35）；基础攻击 5、武器炼器 +8 时显示 13，`ATTACK_DAMAGE` 基础值为 5。供裁定的选项：(a) 保持来源行为；(b) 实际属性也计入防具、武器的炼器数值，与显示一致；(c) 显示改为与实际属性一致。 裁定：实际最大生命与攻击伤害按最终值写入，含防具与武器的炼器数值，与显示一致。由此妖化防具不再另加 `lianqizong.health_Max` 的生命修饰符，否则重复计入（equipment 实现时去掉该项，击退抗性、攻速、移速修饰符保留）。
- `ShuxingjisuanZong` 腿部分支把新标记写到已存的靴子快照 `kuijia4`，而不是 `kuijia3`（第 84–86 行）。只影响是否触发重算，不影响算出的值；目标按装备变化事件重算，不复制该标记机制。
- Q18（2026-11-03 负责人指示暂按选项 a）：见下节，受击方主修功法与来袭伤害同属性时，功法五行值使伤害增加，而系统设计称其为“抗性”。“抗性”是本项目系统设计的叫法，来源代码没有为该项命名。算例：火属性伤害 10、攻击方 `body_Fire` 3 时为 10 ×（3 × 0.1 + 1）= 13；受击方主修 `ExercisesFire` 5、层数 8 时再加 5 × 8 / 4 = 10，结果 23，按抗性理解应为 3。供裁定的选项：(a) 保持来源行为（修同属性功法更易受该属性伤害）；(b) 改为减去该项；(c) 改读攻击方功法作为攻击加成（同一公式的灵根项读攻击方 `sourceentity`，功法项读受击方 `entity`，可能是误读）。代码无法判定作者意图，未给建议。 负责人指示：保持来源行为（修同属性功法更易受该属性伤害），作为待处理问题保留，之后再议；公式中该项是单独的一项（`AttributeTotals.receivedElementBonus`），改变时只改这一处。

### 五行加成（combat 的 `DamageIntegration`，按系统设计由本系统计算）

伤害值 = （攻击方基础 `mana_Att`（不是 `mana_Att_display`）+ 主手物品 NBT `mana_Att` + 受击方待结算的 `ManaAttackDamage` + 主手 `manaatt` 附魔等级 × 2 + 主手属性附魔 `shuxingatt_<n>` 等级²）×（攻击方灵根 `body_<五行>` × 0.1 + 受击方 `Damage_multiplier`）+ 受击方主修功法 `Exercises<五行>` × 受击方功法 `ExercisesLevel` / 4。

- 伤害属性取受击方 NBT `damagetype`：金、水、木、火、土分别对应 `body_Gold`/`ExercisesGold`、`body_Water`/`ExercisesWater`、`body_Tree`/`ExercisesTree`、`body_Fire`/`ExercisesFire`、`body_Soil`/`ExercisesSoil`；光、雷借用火，暗借用土，冰借用水，风借用木；阴、阳用攻击方 `yinyang`，附魔等级取 4 次方，没有功法项；其他类型不变。
- 之后：攻击方带效果 `effect_013` 时加（等级 + 1）²；主手 `critical` 附魔以等级 × 2% 的概率使伤害 ×（1 + 等级 × 0.1）；最后把 `Damage_multiplier` 复位为 1（≤ 0 时先置 1）。
- 来源中功法的五行值全部为正（`Gongfalist*` 中 56 处赋值），所以受击方主修同属性功法时受到的伤害增加（Q18）。
- 功法项读受击方 `gongfa` 物品，不检查 `belongToMasterUUID`；派生值中的功法加成则只在功法属于本人时计入。gongfa 提交五行贡献时按此区分。
