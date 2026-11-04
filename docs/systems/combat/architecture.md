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
