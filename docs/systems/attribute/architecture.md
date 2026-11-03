---
topic: attribute
role: architecture
status: draft
updated: 2026-11-03
---

# 属性来源、派生与显示：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:ShuxingjisuanZongProcedure:27–122` 比较四件防具及主手；防具使用 `bubianzhi` 标记，变化调用 `ShuxingjisuanProcedure`，主手种类变化调用 `Shuxingjisuan2Procedure`。`A:PlayerTickProcedure:83,351–361` 还从定期更新与功法切换进入计算。

来源中同时存在 Variables 基础字段、`*_display`、实体实际属性、装备 `lianqizong`、功法属性和 `tizhiitem` 修正。`A:ShujvtongbuProcedure` 将部分显示/资源值写到实体 `synchronous*` NBT；其名为“同步”并不能独立证明网络同步已发生。

具体矛盾：`ShuxingjisuanZongProcedure:84–86` 处理腿部装备时更新了 `kuijia4` 的比较标记，而前面该分支保存的是 `kuijia3`。这要作为源行为问题裁定，不能复制后再靠反复重算掩盖。

## 目标所有权与接口提案

建立明确输入集合：成长基础、功法实例、装备实例、体质/状态修正。属性计算返回派生结果；服务端适配层将对应结果写到 Minecraft 属性，UI 消费只读快照。每个属性标明基础、加算、乘算、上限、取整与实际消费者。

调用形态采用 `recalculate(player, contributions)` 和只读查询即可，不为每个属性建立 interface/repository。输入拥有者负责产生贡献；属性公式不得反向修改装备、功法或成长存档。

加载/来源变化 → 计算 → 实体属性生效 → 必要显示同步。缓存由来源变化维护；来源检测策略替换前要核对同物品种类但组件改变的情况，不能照抄仅比较 Item 的主手逻辑。

## 验证范围

全量字段的明确接收类型已索引。尚不能把所有带 `_display` 的数值认定为最终战斗真值：实际战斗读取位置必须逐属性记录。数值验收使用独立算例和游戏中的真实扣血/恢复，不能以 Tooltip 正确代替生效。

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

- Q17（待裁定）：防具 `lianqizong.health_Max` 计入 `health_Max_display`，但实际最大生命只在妖化防具上通过修饰符生效；武器 `lianqizong.attack_num` 计入 `attack_Num_display`，但不进入 `ATTACK_DAMAGE`。界面显示与实际属性不一致；战斗系统读的是 `*_display`（伤害与防御按显示值结算），原版生命上限与近战伤害按实际属性。
- `ShuxingjisuanZong` 腿部分支把新标记写到已存的靴子快照 `kuijia4`，而不是 `kuijia3`（第 84–86 行）。只影响是否触发重算，不影响算出的值；目标按装备变化事件重算，不复制该标记机制。
- 未读：灵根与功法的五行加成和抗性目前在 combat 的 `DamageIntegration` 中计算，按系统设计归本系统，定稿公开操作前读。
