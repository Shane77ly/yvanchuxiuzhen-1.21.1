---
topic: combat
role: architecture
status: draft
updated: 2026-11-01
---

# 战斗：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与入口

`A:EntityHurtProcedure:49–100` 监听 `LivingAttackEvent`，处理状态、战斗记分板及后续附魔入口；`EntityBeforeHurtProcedure:45–170` 监听 `LivingHurtEvent`，判断 NPC 免伤、玩家/生物队伍、闪避和物理结算。两者是不同阶段。`EntityBeforeHurt2Procedure` 只有局部声明，不承担有效伤害计算。

战斗状态还使用记分板 `zdztbegin/zdzttick`（`EntityHurtProcedure:79–98`）。这些状态不能因未列入 Variables 而丢失。

77 个状态效果、85 个附魔注册及伤害命令均在范围。402 处命令执行调用另有索引，包含效果、粒子及自定义命令路径，不能只沿 `.execute` 静态调用追伤害。

## 实际命令链与未闭合的字段链

`A:command/ShanghaiCommand.java:24–62` 注册 `/shanghai` 并调用 `ShanghaiGoProcedure`。后者 68–90 行读取类别、攻击者 UUID、固定伤害标记、基础量、倍率；111–150 行处理关系，154–182 行处理闪避，183–214 行调用 `Zhilingdamage/Zhilingdefence` 并结算：音波/灵魂为攻击量减防御，其余为 `攻击量² / (攻击量 + 防御)`。这只是该命令路径的阶段公式，不代表全部属性贡献公式。

216–257 行处理保命状态和 `itemfulu_16` 的受伤替代分支；实际 `hurt` 在 257、321 行。超过当前生命的分支之后还更新体质击杀计数（258–317 行），不等于已确认 `hurt` 后目标确实死亡。目标击杀结果应如何定义需裁定，不能默默搬到死亡事件后宣称等价。

| 字段读取位置 | 读取的对象 | 当前证据 |
| --- | --- | --- |
| DamageIntegration:26、ManaDamage:50/57/61 | 受击实体的 ManaAttackDamage | 两类计算过程未从结构入口图到达 |
| SoulAttjisuan:50/57/61 | 受击实体的 SoulAttackDamage | 计算过程未从结构入口图到达 |
| B Zhenyang01DangTouZhiWuJiZhongShiTiShi:22 | 弹体的 ManaAttackDamage | 复制到受击实体；该读取本身不调用伤害结算 |
| A SkillTree01TouZhiWuFeiXingTick:28 | 玩家所选技能物品的同名键 | 不属于阵法写入的实体载体，不能因同名认作消费者 |

以上来自全量键接收者索引和实际源码复核，仍不排除运行时脚本的动态键操作。`/shanghai` 直接以命令参数输入，不能自动视为所有实体伤害键的统一消费者。保真验收需要分别覆盖命令路径、原生攻击和这些未闭合路径。

## 目标所有权与接口提案

敌友判定调用 relation 的查询。伤害入口明确攻击者、受击者、来源、类别、基础量及来源效果。把免伤/目标过滤、伤害变换、实际承伤后的效果分到目标事件对应阶段；禁止同时在入口和事件中重复计算同一份伤害。

保留原生 `hurt`、直接 `setHealth`、效果施加等不同路径的证据，逐项决定目标方式。依赖规则（[系统设计](../../development/system-design.md)）：combat 只读 attribute 的最终值，灵根与功法的五行加成、抗性由 attribute 计算；其他系统以伤害类型造成伤害，combat 在伤害事件中结算，不被直接调用；状态效果与受伤时扣减灵力、神识使用 xiulian 的扣减操作。附魔数据定义与执行规则分离；特例可以有专用方法。

## 待裁定与验收

`EntityBeforeHurtProcedure:102–110` 与 `ShanghaiGoProcedure:132–134` 的非玩家攻击玩家分支混用了双方数据来源，需独立裁定。前者、命令路径、阵法字段链不能强行合成一套推测公式。伤害阶段映射见公共目标 API 证据；目标版本运行时还要验证护甲、吸收、无敌帧、友军和击杀归属。

## relation 归属阅读转来的元素（2026-11-03）

- `FindTheEnemy`：`LivingSetAttackTargetEvent` 中，目标的 NBT 没有 `findtheenemy` 标记时，在攻击方头顶 0.5 格处发一个 `exclamation_mark` 粒子并给目标加标记；`EntityTick` 每 400 tick 检查一次，目标此时没有锁定对象则清除标记。适用于所有生物，与敌友数据无关。粒子由 particle 登记。

## 伤害主链的归属阅读（2026-11-04）

读过 `EntityHurt`、`EntityBeforeHurt`、`EntityBeforeHurt2`、`ShanghaiGo`、`Zhilingdamage`、`Zhilingdefence`、`DamageXiaoguo`、`DamageIntegration`、`DamageDefense`、`ManaDamage`、`ManaDefense`、`SoulAttjisuan`、`SoulDefjisuan`；`EntityHurt2`、`EntityHurt3` 的武器与防具附魔分支读到结构层，逐项数值在实现附魔时按源码核对。下列行号为 1.18.2 版本 `procedures/` 下生成文件的行号。

### 入口与阶段

| 入口 | 事件或调用方 | 作用 |
| --- | --- | --- |
| `EntityHurt` | `LivingAttackEvent`（全局订阅，需要攻击者） | 命中即触发的附魔效果、战斗状态记分板、功法特效、驯服宠物的攻击目标；调用 `EntityHurt2`、`EntityHurt3` |
| `EntityBeforeHurt` | `LivingHurtEvent`（全局订阅，需要攻击者） | 免伤判定、闪避、物理伤害结算、吸血、符箓复活 |
| `ShanghaiGo` | `/shanghai <类别> <攻击者UUID> <固定> <基础量> <倍率>`（权限 4，由 `ShanghaiCommand` 调用） | 灵力、神识伤害结算，调用 `Zhilingdamage`、`Zhilingdefence` |
| `EntityBeforeHurt2`、`DamageXiaoguo` | 订阅了 `LivingHurtEvent` 但只有局部声明；空方法 | 无作用 |
| `DamageIntegration`、`DamageDefense`、`ManaDamage`、`ManaDefense`、`SoulAttjisuan`、`SoulDefjisuan` | 无过程调用者、无事件订阅、无元素触发 | 另一版伤害计算，经受击者持久化键 `ManaAttackDamage`、`ManaAttackUUID`、`damagetype`、`Damage_multiplier` 传值，不可达（Q31） |

没有攻击者的伤害（摔落、窒息、岩浆等环境伤害）两个事件都直接返回，不经本系统的任何结算。

### `LivingHurtEvent`：免伤、闪避与物理伤害（`EntityBeforeHurt`）

1. 受击者带标签 `yvanchuxiuzhen:npcentity` 或 `yvanchuxiuzhen:jiunpc`：免伤。
2. 敌友：攻击者团队在受击者友方集合中则免伤，再按攻击方与受击方是否玩家四种组合分别取团队与友方集合（玩家来自关系附件，其他实体来自持久化 `team`、`friendlist`）；玩家攻击生物、生物攻击玩家、生物攻击生物三种组合中任一方团队为空时，把免伤改回否；非玩家攻击玩家与非玩家攻击非玩家两种组合再叠加 `Yaoshou006fangyvzitai` 的免伤。免伤时取消事件。
3. 不是 `IN_WALL`、`DROWN`、`STARVE`、`DRY_OUT` 时判闪避：随机 1–100 不大于四件护甲 `dodge` 附魔等级之和则取消事件，并在受击者上方 1 格生成 `miss` 粒子。
4. 不是 `DRAGON_BREATH`、`FREEZE`、`IN_FIRE`、`LIGHTNING_BOLT`、`ON_FIRE`、`LAVA`、`HOT_FLOOR`、`OUT_OF_WORLD`、`MAGIC`、`WITHER` 之外且伤害量大于 0 时，改写伤害量：
   - 受击者防御 `wulideflinshi`：玩家取 attribute 的 `attack_Def` 基础值，其他实体取持久化 `attack_Def`；抗性倍率 `kangxingbaifenbi`：玩家取体质 `AttackDefCorrectPercentage + 1`，其他为 1。
   - 破甲系数 `pojia`：手持武器有 `pojia` 附魔时为 `1 − 等级 × 0.1`（上限 10 的判断永远不成立），否则为 0。
   - 攻击量 `gongjilinshi = 伤害量 × (受击者体质 AttackAttCorrectPercentage + 1)`；
   - 结算量 = `gongjilinshi² / (gongjilinshi + ((防御 + 四件护甲 lianqizong.attack_Def 之和) × 破甲系数 + Σ attdef 等级² ) × 抗性倍率)`。
   - 玄武岩 `basalt`：四件护甲 `basalt` 等级之和 × 2 = `basalt`；随机 1–100 小于该值时结算量再减去该值。
   - 结算量大于 0：受击者有 `effect_036`（免死）时结算量不超过当前生命 − 1；写回伤害量。结算量不大于 0 时伤害量不变。
   - 手持武器 `vampire` 等级 n：攻击者回复 结算量 × n × 5%（结算量为 0 时也执行，不受“结算量大于 0”限制）。
   - 暴击：手持 `critical` 等级 n 时以 n × 2% 概率生成暴击粒子并把结算量记为 伤害量 × (1.5 + criticalatt × 0.2)，但这个值随即被上一条的结算量覆盖，不影响伤害（Q30）。
5. 受击者当前生命小于原伤害量且是持有 `itemfulu_16` 的玩家：播放图腾动画与音效、发光 40 tick、生命 + (符箓等级 + 1) × 10、消耗一张符箓、取消事件。三个分支（闪避未中后的特殊伤害来源、伤害量不大于 0、普通结算之后）各有一份相同的复活判定。

### `LivingAttackEvent`：命中附魔与战斗状态（`EntityHurt`）

1. 受击者有 `duyao_19`：生命减 (效果等级 + 1)，暴击粒子。
2. 以下 2–4 项都在“攻击者与受击者不友好（`FriendpandingStoE` 为否）”之内：记分板 `zdztbegin` 设为 1，`zdzttick` 设为受击者 NBT `HurtByTimestamp`；之后按攻击者**主手武器**附魔逐项触发：
   - `haoyu`：受击者灵力（持久化 `power` 与玩家变量 `power`）乘 `1 − 等级 × 1%`；
   - `phoenix`：概率 等级 × 1%，受击者生命减 等级²；
   - `longdignified` → `effect_007`（100 tick，等级 = 附魔等级）；`tigerdignified` → 虚弱（100 tick）；概率均为 等级 × 1%；
   - `sawtooth`（受击者非亡灵，概率 等级 × 10%）→ `effect_004`；`fluorescence`（概率 等级 × 10%）→ 发光；
   - 毒药附魔（概率 等级 × 1%）：`beepoison` → `duyao_10`、`firepoison` → `duyao_16`、`redrefining` → `duyao_13`、`spore` → `duyao_17`、`sinomeninevine` → `duyao_11`、`coldtoxin` → `duyao_12`、`cautery` → `duyao_14`、`snakevenom` → `duyao_15`；效果时长 = 受击者已有同效果剩余 tick × 20 + 600，等级 = 附魔等级 − 1，并在受击者位置生成 `zhongdulizi` 粒子（Q32）；
   - `vampire`：攻击者回复 `amount × 等级 × 5%`（与 `LivingHurtEvent` 的吸血各自回复一次）；
   - 调用 `EntityHurt2`、`EntityHurt3`；
   - 攻击者是玩家且装备功法名为 `流星春水`（20% 在受击者上方 16 格生成陨石 `skill_rock_03`，倍率 1.2）、`星君诀`（同上，倍率 2.4）、`金廷落`（受击者生命减 功法层数 × 0.5）、`血魔祭魂秘法`（同样扣血并给攻击者 2 个 `xvejing`）；
   - 以攻击者为中心、边长 64 的方体内主人为攻击者的已驯服动物，持久化 `linshigongjizhe` 设为受击者 UUID（受击者是玩家时，以受击者为中心、主人为受击者的已驯服动物设为攻击者 UUID）。
3. 伤害量不小于受击者最大生命 / 16（在不友好分支内）：受击者获得 `effect_033` 10 tick；带标签 `forge:yaoshou` 且持久化 `jineng` 非空的妖兽被打断：`beidaduan` 设为真、`daduan` 粒子、调用 `Entityjinengjieshu`。
4. 受击者带标签 `forge:yaoshou` 或 `forge:xiuzhe`：`effect_034` 每次命中叠加 1 级、时长 20 tick。

### `/shanghai`：灵力与神识伤害（`ShanghaiGo`、`Zhilingdamage`、`Zhilingdefence`）

参数 `0` 类别（金水木火土光暗雷冰风阴阳音波灵魂）、`1` 攻击者 UUID（`无`、`wu`、`no`、`false` 表示无来源）、`2` 是否固定伤害、`3` 基础量、`4` 倍率。受击者须存活且是 `Mob` 或玩家，基础量大于 0。

1. 敌友：攻击者存在时按四种玩家组合取友方集合，友方即免伤（生物受击者另叠加 `Yaoshou006fangyvzitai2`）；与 `LivingHurtEvent` 不同，这里没有“任一方团队为空则改回不免伤”（Q30）。
2. 闪避：记分板 `suijishu` 写入随机 0–100；该值大于四件护甲 `dodge` 等级之和才继续，否则本次伤害无效且不播放未命中提示（没有闪避附魔时随机数为 0 也使伤害无效，约 1/101，Q30）。
3. 攻击量（非固定且有来源时）`Zhilingdamage`：
   - 攻击者是玩家：音波、灵魂加 `soul_Att_display`；其他类别 `(基础量 + mana_Att_display) × (体质 ManaattAttCorrectPercentage + 1)`；装备功法的归属者是攻击者本人时，功法层数取 `ExercisesLevel`（否则为 0）；
   - 五行类别按下表取 `属性倍数 = body_五行 × 0.1`、`功法伤害 = 功法 Exercises五行 × 层数 / 4`、`基础量 = (基础量 + 体质 五行AttCorrect) × (体质 五行AttCorrectPercentage + 1)`；光、雷用火，暗用土，冰用水，风用木，阴阳用 `yinyang × 0.1` 且没有功法伤害；音波、灵魂把体质 `Soulattof{yin,jing}AttCorrectPercentage` 加进倍率；
   - 攻击者是生物：加持久化 `soul_Att` 或 `mana_Att`；
   - 手持武器 `shuxingatt_01…12` 等级 n 对应类别加 n²（阴阳为 n⁴）：金 01；水 02；木 03；火 04；土 05；光 04 与 06；暗 05 与 07；雷 04 与 08；冰 02 与 09；风 03 与 10；阴 11；阳 12；
   - 受击者带 `forge:{金水木火土}entity` 标签时按攻击五行调整倍率：受击者被攻击五行所克 +0.3，克攻击五行 −0.3，生攻击五行 +0.2，为攻击五行所生 −0.2（五行相克为金克木、木克土、土克水、水克火、火克金，相生为金生水、水生木、木生火、火生土、土生金）。光、雷按火，暗按土，冰按水，风按木取标签；阴阳没有标签调整。火类别（不含光、雷）受击者有 `duyao_16` 时基础量加 (等级 + 1) × 10；
   - 音波、灵魂：手持 `soulatt` 等级 n 加 n²，攻击者有 `effect_020` 加 (等级 + 1)²；其他：`manaatt` 等级 n 加 2n，攻击者有 `effect_013` 加 (等级 + 1)²；
   - 倍率不大于 0 时取 0.1；攻击量 = 基础量 × 倍率 + 功法伤害；
   - 手持 `critical` 等级 n：随机 0–100 小于 2n 时生成附魔暴击粒子，攻击量 × (1.5 + criticalatt × 0.2)。
4. 防御 `Zhilingdefence`：
   - 四件护甲附魔：金水木火土各取同名 `*def` 等级之和 × 2；光取火 `firedef` 之和 + `lightdef` 等级²之和；暗取土 `soildef` 之和；雷取火之和 + `thunderdef` 等级²之和，受击者有 `effect_022` 加 (等级 + 1)²；冰取水之和 + `icedef` 等级²之和；风取木之和 + `winddef` 等级²之和；音波、灵魂取 `souldef` × 2 之和，音波再加 `sounddef` × 2 之和；
   - 玩家：非音波灵魂乘体质修正的步骤因体质物品未赋值而不生效（Q29）；音波灵魂 `(防御 + soul_Def_display) × 倍率`，其他 `(防御 + mana_Def_display) × 倍率`，倍率同样来自未赋值的体质而恒为 1；
   - 生物：加持久化 `soul_Def` 或 `mana_Def`；
   - 四件护甲炼器数据 `lianqizong` 的 `soul_Def` 或 `mana_Att`（来源写作 `mana_Att`，与物理防御不同键，Q30）逐件相加；非音波灵魂再加 `manadef` 等级²之和；
   - `basalt`：四件护甲等级之和为概率（%），命中时播放 `yvanchuxiuzhen:basalt` 音效与 `basaltlizi` 粒子，防御再加 等级之和 × 2。
5. 结算：非固定有来源时用上面的攻击量；音波、灵魂 `总量 = 攻击量 − 防御`，其他 `总量 = 攻击量² / (攻击量 + 防御)`；总量大于 0 时伤害来源为 `linglidamage`（灵魂音波为 `jinglidamage`），有来源时带攻击者（`EntityDamageSource`，来源名后缀 `.player`），都绕过护甲；`effect_036` 限制不超过当前生命 − 1；符箓 `itemfulu_16` 与 `LivingHurtEvent` 同样复活，没有符箓时 `hurt`。
6. 击杀统计：受击者当前生命小于总量（`hurt` 之前判断）时，若攻击者是玩家，其体质物品按类别（金水木火土）累加 `killentity{五行}`（击杀数）与 `killentitypoints{五行}`（受击者境界层数或持久化 `LEVELnumber`），写回玩家变量并同步。体质物品每次按引用取出再写回。
7. 命令路径由 `EntityHurt2`、`EntityHurt3`、技能、符箓等发起，来源中经 `/shanghai` 的调用共 209 处。

### 附魔与效果的实现范围

- 原初修真的 84 个附魔类与原初系统的 `chain` 都是只有稀有度、类别、槽位和 `isTradeable`/`isTreasureOnly` 覆盖的空壳类，没有 `getMaxLevel` 等覆盖（1.18.2 默认最高 1 级）；效果全部由上述订阅过程读取手持物或四件护甲的等级。原初修真 84 个中武器类（`EnchantmentCategory.WEAPON`）51 个、护甲类 28 个、可损坏类 3 个、挖掘类 2 个；83 个普通稀有度、1 个 `sawtooth` 少见；`isTradeable` 返回否 45 个、`isTreasureOnly` 返回是 2 个（`kailing`、`sawtooth`）。
- `EntityHurt2` 包含：防具附魔 `firearmor`、`icearmor`、`discharge`、`pool`（概率为四件护甲等级之和 %，命中后对以受击者为中心、边长 5 的方体内非友好目标发 `/shanghai`，固定伤害，基础量为等级之和 × 2）；武器附魔 `qishi`（召唤 `skillyin_01`）、`lingwen`、`liuyan`、`pojia`、`yvnxvan`、`chenmo`、`lingtikiller`、`jvtong`、`zhongshang`、`confine` 等；`EntityHurt3` 包含 `iceexplosion`、`liuqing`、`threaten`、`nofly`、`starsky` 等。其中 `lingtikiller` 取了 `zhongshang` 的等级、`lingwen` 的消耗取了 `qishi` 的等级、`liuqing` 的时长使用未赋值的 `fumolevel`（Q32）。
- 这些过程调用的实体与粒子（`skill_rock_03`、`skillyin_01`、`lingwentexiao_2`、`zhongdulizi`、`miss` 等）属于 skill、particle，登记后随所属系统接入。

## 目标方案：附魔数据定义与等级查询（2026-11-04 已实现，会话决定）

来源 85 个附魔（原初修真 84、原初系统 `chain`）转为数据包定义，规则见[工程结构](../../development/engineering.md)“资源转换管线”的附魔条目，工具 `tools/porting/convert_enchantments.py`。

| 1.18.2 版本 | 1.21.1 版本 |
| --- | --- |
| `Enchantment` 子类、稀有度 | `data/yvanchuxiuzhen/enchantment/<名>.json`，权重 10（普通）或 5（少见）、铁砧代价 1 或 2 |
| 类别 WEAPON、ARMOR、DIGGER、BREAKABLE | 物品标签 `yvanchuxiuzhen:enchantable/weapon`、`armor`、`digger`、`breakable` |
| 最高等级（默认 1，`chain` 为 3） | `max_level`；游戏内其他过程可直接写入更高等级，查询读到的就是写入的等级 |
| `isTreasureOnly`、`isTradeable`、默认可发现 | `minecraft` 命名空间附魔标签（`treasure`/`non_treasure`/`in_enchanting_table`、`tradeable`、`on_random_loot`） |
| `getItemEnchantmentLevel(附魔, 物品)` | `Enchants.level/mainHand/slot/armorSum`（`system/combat/api/Enchants`），附魔按注册表键 `Enchants.key("dodge")` 取得 |

留待其他系统：原版附魔（锋利、保护等）适用于本 Mod 武器、盔甲、挖掘工具，需要把这些物品加入 `minecraft:enchantable/*` 等原版标签，随 equipment 迁移；来源的 1.18.2 `Enchantment.slots` 为空，附魔不参与按槽位生效的数据效果，行为由读取等级的玩法代码承担。

### 附魔的归属阅读（2026-11-04）

85 个附魔都是空壳类，归属按读取等级并赋予含义的规则判定（读取者清单由脚本对全部过程的 `YvanchuxiuzhenModEnchantments.*` 引用统计）：

- 留在本系统 75 个：伤害主链与命中附魔读取的全部（`*def`、`shuxingatt_01–12`、`manaatt`、`soulatt`、`critical*`、`dodge`、`basalt`、`pojia`、`vampire`、毒药类、`EntityHurt2`/`EntityHurt3` 的武器与护甲附魔），`attdef`、`manadef`（防御公式在本系统，attribute 的显示值另按等级线性相加）、`breakfree`（flow 的 `Effectslownessget` 缩短原版缓慢效果）、`chuanshi`（破碎的基岩）、`lingtikiller`、`lingwen`、`chain`，以及来源没有读取过程的 `ghost`、`guard`、`shuangsheng`、`summoningspeed`、`zimu`（只保留说明文本）。
- 改归 skill：`flying`（`FlyskillTick`）、`flyingskill`（`ShenfaTick`）、`jet`（`ShenfaW`）；attribute：`dawn`、`powerup`、`colddown`（`Shuxingjisuan` 折入回血、回灵与冷却缩减）；equipment：`qiling`（本命法宝培炼条件）、`qingyv`（弓）、`kailing`（工具自动开采）；fulu：`charm`（`Itemfulu18` 驯服几率）。

## 状态效果（2026-11-04 起分批迁移）

77 个状态效果的类都只有类别、颜色和三个钩子（开始 `addAttributeModifiers`、每刻 `applyEffectTick`、结束 `removeAttributeModifiers`），`isDurationEffectTick` 恒为真，行为全在同名过程中。归属按赋予含义的规则判定（读取者清单由脚本统计 `YvanchuxiuzhenModMobEffects.*` 的引用）：`effect_001`–`022`、`036`、`duyao_*`、`effect_023`–`024`、`032`–`035` 留在本系统；其余待读取时按下列已判定的方向调整：`effect_025`–`027` 归 xiulian（修炼加持）、`effect_028`–`031` 归 cooking、`effect_037` 归 creature、`huxiangtunshi` 归 guchong、`skilleffect_01`–`09` 与 `skillwind_04` 归 skill。

### 目标方案

- 登记走内容表 `content/<单位>/effects.json`（`id`、`category`、`color`），行为由所属系统用 `ContentTables.effectBehavior(id, 行为)` 登记，没有登记的效果只是标记，供其他过程读取等级。名称与说明语言键由 `convert_content.py` 按台账所属单位写入（`effect.<命名空间>.<名称>[.description]`）。
- 行为接口 `EffectBehavior`：`started`（效果加上，以及已有效果被刷新且数值有变化而重新应用时，判断与 `MobEffectInstance.update` 一致，在 `MobEffectEvent.Added` 中分发，只在服务端）、`tick`（服务端每刻）、`clientTick`（客户端实体每刻，仅用于转动视角这类来源在两端都执行的表现）。结束钩子在第一个需要它的效果（`skilleffect_08`、`skilleffect_09`）迁移时加入。
- 来源对实体持久化数据写的计数（`effect003tick`、`effect010`、`duyao01tick` 等）保持原键存在实体的持久化数据中；玩家数据的读写走 xiulian 的公开操作与 attribute 的最终值。

### 已迁移（第 1 批，22 个）

| 效果 | 行为 |
| --- | --- |
| `effect_001` 眩晕 | 开始播放 `effect_001yinxiao`；每刻水平转 5 × (等级 + 1) 度、俯仰随机 ±10 度，生物停止寻路（两端） |
| `effect_002` 沉默 | 开始播放 `effect_002yinxiao`；等级由技能、法宝、火焰释放的过程读取 |
| `effect_003` 剧痛 | 开始播放音效；计数到 80–89 刻时每刻抖动视角（两端），到 90 清零 |
| `effect_005` 无力 | 开始播放音效；每刻丢出主手物品（拾取延迟 10 × (等级 + 1) 刻），桃木精与第四个 Boss 除外 |
| `effect_006` 发抖 | 开始播放音效；每刻水平随机抖动 ±3 × (等级 + 1) 度（两端） |
| `effect_007` 重伤 | 开始播放音效；每刻把当前生命压到 最大生命 − 4 × (等级 + 1)（至少 1） |
| `effect_008/009/012/014/019/021` | 瞬时：把对应的已加点次数减 (等级 + 1)（可再加点的次数增加），提示后移除；对应血量上限、物理攻击、灵力上限、法术攻击、精力上限、精神攻击 |
| `effect_010/017` | 每 20 刻在当前灵力（精力）低于最终上限时恢复 10 × (等级 + 1)（说明文本写“每两秒 5 × 等级”，实际与之不符，保持来源） |
| `effect_011/018` | 瞬时：当前值低于最终上限时恢复 (等级 + 1)² × 100，移除 |
| `effect_015/016` | 持续型每 40 刻、瞬时型一次增加待吸收修为 ((等级 + 1) × 5)² 与 ((等级 + 1) × 20)²，玩家直接加，妖兽发出 `EffectExperienceEvent`（creature 订阅） |
| `effect_013/020/022/036` | 标记，等级由伤害结算读取 |

留待其他系统：`EffectExperienceEvent` 随 creature 订阅；实体 `ycxzboss_04`、`taomujing`（无力的豁免）按路径名判断，随 creature 登记生效；说明文本与实现不符的 `effect_010/017` 按评估为轻微记录，不修改。

### 已迁移（第 2 批，毒药 20 个）

共同结构（`PoisonEffects`）：只作用于存活的生物和不在创造、旁观模式的玩家；周期型到期后玩家按体质 `PoisonLoseEfficacyPercentage` 的概率免疫本周期（计数照常清零），伤害按体质 `PoisonEffectPercentage` 放大后直接扣减当前生命（`setHealth`，不经伤害结算）。累积层数玩家存在 xiulian 的体质键、其他实体存在持久化数据。

| 效果 | 周期与行为 |
| --- | --- |
| `duyao_01` 断肠 | 每 100 刻 4 × (等级 + 1) |
| `duyao_02` 罗刹 | 每 200 刻精力减 100 × (等级 + 1)（放大后），不低于 0（玩家） |
| `duyao_03` 雷公藤 | 瞬时，3 × (1 + 等级)²，伤害不为 0 时结算并移除；被体质免疫时保留效果，下一刻重判 |
| `duyao_04` 鹤顶红 | 每 200 − 20 × (1 + 等级) 刻 4 点 |
| `duyao_05` 瘴气 | 每 40 刻累积层数（玩家首次 1、此后 +0.1），伤害 层数 × (1 + 等级)，并清除速度、力量、治疗、跳跃、生命恢复、抗火、水下呼吸、生命提升、伤害吸收、幸运 |
| `duyao_06` 含笑半步颠 | 每 5 刻检查取整坐标：站定记录，下次坐标变化即受 100 点凋零伤害并爆炸（音效 `entity.generic.explode`、爆炸粒子）；游泳、跳跃、骑乘、岩浆中清除记录 |
| `duyao_07` 一日丧命散 | 每 20 刻累积 1 层，满 1200 层毙命；玩家灵力压到 1，其他生物每次被沉默 40 刻 |
| `duyao_08` 砒霜 | 每 20 刻 2 × (等级 + 1) |
| `duyao_09` 番木鳖 | 每刻把当前生命压到 最大生命 − 10 × (1 + 等级)（体质放大目标值，至少 1） |
| `duyao_10` 蜂毒 | 瞬时，4 × (1 + 等级) 并附加缓慢（60 刻，20 级）与发抖（20 刻，1 级），移除 |
| `duyao_11` 青藤毒 | 每 20 刻 (1 + 等级) 点并扣灵力 10 × (1 + 等级)（玩家，不低于 0） |
| `duyao_12` 寒毒 | 每刻冷冻值 +1，每 80 刻发抖 20 刻 |
| `duyao_13` 赤炼毒 | 每 40 刻 4 × (1 + `duyao_01` 的等级)（Q33） |
| `duyao_14` 烙毒 | 每 1200 刻 20 × (等级 + 1) |
| `duyao_15` 蛇纹毒 | 每 100 刻 最大生命 × 1% × (1 + 等级) |
| `duyao_17` 孢子 | 每 100 刻随机获得缓慢、中毒、霉运、发光、虚弱、失明、隐身之一，100 刻，等级同孢子 |
| `duyao_19` 毒厄丹 | 开始时附加眩晕、剧痛、无力、发抖各 1200 刻 3 级；动物变僵尸的部分在 flow 的过程中，随 flow 迁移 |
| `duyao_20` 尸毒 | 每次感染累积 1 层；每 80 刻受 (等级 + 1 + 层数) 点，身上有瞬间治疗或生命恢复时层数清零；体质放大被覆盖不生效（Q33） |
| `duyao_16`、`duyao_18` | 标记，等级由伤害结算读取 |

`duyao_21`（曼陀罗）经 `/shanghai` 结算，随伤害结算迁移。

## 目标方案：伤害结算（2026-11-04 已实现，会话决定）

| 来源 | 目标 |
| --- | --- |
| `EntityHurt`（`LivingAttackEvent`）、`EntityBeforeHurt`（`LivingHurtEvent`） | 1.21.1 版本两个事件合并为 `LivingIncomingDamageEvent`：`HurtHandler` 先执行 `AttackStage`（命中处理）再执行 `HurtStage`（受伤处理），都要求有攻击者；`AttackStage` 的伤害量取原值，与来源一致 |
| `/shanghai` 的 `ShanghaiGo` 及两个计算过程 | `Shanghai.deal`（`damage/Shanghai`），命令 `ShanghaiCommand` 只解析参数；类别 `DamageCategory`（14 种，含“无”）承载各类别的灵根、体质键、武器附魔、护甲附魔、相克族 |
| `DamageSource("linglidamage")` 等四种来源 | 数据包伤害类型 `linglidamage`、`linglidamage_player`、`jinglidamage`、`jinglidamage_player`（消息 ID 与来源一致，`exhaustion` 为 0），加入标签 `minecraft:bypasses_armor`；`CombatDamageTypes.source` 构造（死亡消息键与译文对不上，Q34） |
| 记分板 `suijishu` | 来源每次先写随机数再读回，只是借用；改为局部随机数 |
| 玩家变量、体质物品 | attribute 的最终值与基础值、xiulian 的体质键（`Cultivation.tizhiDouble/setTizhiDouble`）、灵力精力与境界（`Cultivation.get/set`） |
| 敌友判定 | relation 的 `Relations.team/isFriendly`；`/shanghai` 与受伤处理按攻击方、受击方是否玩家分四种组合，规则同上文“伤害主链”（来源把受击玩家的数据来源与攻击者的混用，生物攻击玩家不会判为友方） |
| 妖兽防御姿态 `Yaoshou006fangyvzitai*` | `DamageImmunityEvent`，creature 订阅 |
| 符箓 `itemfulu_16` 复活（三处） | `LethalDamageEvent`，fulu 订阅；监听者让受击者存活后伤害作废 |
| 护甲 `lianqizong` 的 `attack_Def`、`soul_Def`、`mana_Att` | `ArmorStatEvent`，equipment 或 lianqi 订阅 |
| 命中后的功法特效、妖兽被打断 | `HitEvent`（`heavy` 表示伤害量不小于最大生命的 1/16），gongfa、creature 订阅；本系统自己执行僵直 `effect_033`、逃跑层数 `effect_034`、驯服宠物的攻击目标 |
| 实体标签 `forge:*entity`、`forge:yaoshou`、`forge:xiuzhe`、`npcentity`、`jiunpc` | `yvanchuxiuzhen:` 命名空间的实体类型标签（随登记该实体的系统生成；没有成员时标签不存在，判断为否） |
| `MobType.UNDEAD` | 实体类型标签 `minecraft:undead` |

已实现命中附魔：`AttackStage` 中的 `haoyu`、`phoenix`、`longdignified`、`tigerdignified`、`sawtooth`、`fluorescence`、八种毒药附魔、`vampire`；`HitEnchantments` 中来源 `EntityHurt2`、`EntityHurt3` 的防具附魔 `firearmor`、`icearmor`、`discharge`、`pool`（概率为四件护甲等级之和 %，命中后对以受击者为中心边长 5 的方体内非友好生物发固定伤害，基础量为等级之和 × 2）和武器附魔 `liuyan`、`iceexplosion`（概率 等级 %，范围内非友好生物受 等级 × 4、倍率 0.2 的完整结算，围一圈粒子）、`pojia` 的 `caijinumber` 标记、`yvnxvan`、`chenmo`、`jvtong`、`zhongshang`、`nofly`、`threaten`、`confine`（概率 等级 × 2%）、`lingtikiller`、`liuqing`、`chaos`、`thegreatvoid`。召唤其他系统实体的 `qishi`、`lingwen`（skill）与 `starsky`（fulu）随各自系统订阅 `HitEvent`。粒子（`miss`、`zhongdulizi`、`basaltlizi`、`xvedi`、`smokered`、`effect_033lizi`、`smallexplosion*`、`flash_ice`、`thunderdeeppurple`、`starlightblue`、`smokedeeporange`）随 particle 迁移。

## 连锁采集与穿石（2026-11-04 已实现，会话决定）

- **连锁采集**（`chain`，原初系统的 `ChainProcedure`，`enchant/ChainMining`）：`BlockEvent.BreakEvent` 中，主手工具对被挖方块有效且有 `chain` 时，从该方块按 3×3×3 邻域展开挖掉相同方块，展开条目不超过 附魔等级 × 5 + 1（条目可重复，来源如此）；每挖一块工具损耗 1 点；掉落不带工具，不受时运与精准采集影响，掉在被挖的方块位置。矿石（`c:ores` 与本 Mod 命名空间的 `ores`，后者收有来源 `forge:ores` 中本 Mod 的矿石）按玩家持久化数据的 `collection_LEVEL`（采集等级，随 profession 写入）以 等级 × 0.5% 的概率额外掉 3 份、等级 % 概率额外掉 2 份，每个矿石 2 点采集经验，经 `GatherExperienceEvent` 交给 profession（来源执行命令 `collection experience add`）。
- **穿石**（`chuanshi`，`enchant/BedrockCracking`）：主手镐带穿石时左键方块给自己急迫 II（100 刻）；点到基岩且其下不是虚空空气时把基岩换成 `bedrock_01`，镐损耗 10 点。`bedrock_01`、`bedrock_02` 由 `CrackedBedrockBlock` 在被玩家破坏后原位变成下一阶段，`bedrock_03` 破坏后掉出基岩（内容表的兜底掉落）。
- 来源 `LeftClickBlock` 的这段在 flow 的过程里，每次左键末尾还把玩家变量 `collection_LEVEL` 抄进持久化数据，由 profession 写入时直接维护持久化数据即可。
