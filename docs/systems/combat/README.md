---
topic: combat
role: entry
status: partial
updated: 2026-11-03
---

# 战斗

标识 `combat`，类别：系统。职责：伤害结算（读 attribute 最终值）、通用状态效果、附魔、木人桩（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 4、物品 5、声音事件 8）；C 阶段进行中：附魔 85 个已转为数据定义，状态效果第 1 批 22 个已迁移（见[架构](architecture.md)“状态效果”），伤害结算、其余状态效果、木人桩尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 258 个元素，其中 26 个已读（伤害主链 14 个，见[架构](architecture.md)“伤害主链的归属阅读”）；85 个附魔读后 75 个留在本系统，10 个改归 skill（`flying`、`flyingskill`、`jet`）、attribute（`dawn`、`powerup`、`colddown`）、equipment（`qiling`、`qingyv`、`kailing`）、fulu（`charm`）；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

已对照 `typed_field_accesses.csv` 与 `nbt_key_calls.csv` 中战斗相关字段的外部读写点（`ManaAttackDamage` 等未闭合的字段链见[架构](architecture.md)Q13、Q31，不迁移）。代码在 `system/combat/api` 与 `system/combat/damage`。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 造成灵力或神识伤害 | `Shanghai.deal(ServerLevel, 受击者, Request(类别, 攻击者UUID或空, 是否固定, 基础量, 倍率))`；命令 `/shanghai <类别> <攻击者UUID或无> <true或false> <基础量> <倍率>`（无权限要求，对命令执行者生效） | 技能、符箓、妖兽、法宝、蛊虫、异火、装备等的 209 处命令调用；本系统的护甲与武器附魔、毒药 |
| 附魔等级 | `Enchants.level / mainHand / slot / armorSum / armorSquares`，附魔按路径名取键 `Enchants.key("dodge")` | 本系统的伤害过程；attribute 的显示值按等级线性折入（自行读取物品） |
| 状态效果查询与施加 | `StatusEffects.amplifier / has / remove / add / holder`，效果按路径名（如 `effect_036`）指定 | 全部读取或施加状态效果的系统（creature 约 280 处、skill、fulu、equipment、liandan 等） |
| 效果的行为登记 | `ContentTables.effectBehavior(效果ID, 行为)`（`EffectBehavior`：开始、每刻、客户端每刻） | 效果归其他系统的行为（`effect_035` 归 skill，`yaoshou_*` 归 creature，`fulutexiao_*` 归 fulu） |
| 向其他系统借用规则的事件 | `DamageImmunityEvent`（creature 的防御姿态免伤）、`LethalDamageEvent`（fulu 的 `itemfulu_16` 复活）、`ArmorStatEvent`（lianqi、equipment 提供四件护甲的炼器数值）、`HitEvent`（gongfa 的命中特效、creature 对妖兽的打断）、`EffectExperienceEvent`（creature 给妖兽加修为） | 监听者在对应系统迁移时订阅 |

约定：

- 本系统只读 attribute 的最终值（攻击、防御、灵根与功法五行系数）和 xiulian 的体质键、灵力、精力、境界；非玩家实体的攻击防御读实体持久化数据里来源的键（`mana_Att`、`soul_Att`、`mana_Def`、`soul_Def`、`attack_Def`、`LEVELnumber`），写入方是各自的生成过程（随 creature、skill 定为实体属性）。
- 伤害在 `LivingIncomingDamageEvent` 中结算（命中处理在前、受伤处理在后），没有攻击者的伤害不处理。

实现状态：附魔 85 个已转数据定义；状态效果已迁移 `effect_001`–`022`（除标记外）、`004`、`034`、`036` 标记、毒药 `duyao_01`–`21`；灵力、神识伤害结算与受伤、命中处理（除 `EntityHurt2`、`EntityHurt3` 的附魔分支）已实现并经 `CombatCheck` 15 项实机检查；命中附魔（`EntityHurt2`/`EntityHurt3`）、连锁采集、穿石、破碎的基岩、曼陀罗已实现（`CombatCheck` 现为 46 项实机检查）；幻音 `effect_032`、木人桩（首次接入 GeckoLib 4.9.3 的动画实体）已实现；淬毒改归 lianqi；`Shanghaifanwei`、雷劫（xiulian 推迟）尚未实现。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
