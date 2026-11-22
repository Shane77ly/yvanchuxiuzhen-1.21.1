---
topic: combat
role: architecture
status: current
updated: 2026-11-18
---

# 战斗：规则与实现状态

来源事实与目标实现边界分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与入口

`A:EntityHurtProcedure:49–100` 监听 `LivingAttackEvent`，处理状态、战斗记分板及后续附魔入口；`EntityBeforeHurtProcedure:45–170` 监听 `LivingHurtEvent`，判断 NPC 免伤、玩家/生物队伍、闪避和物理结算。两者是不同阶段。`EntityBeforeHurt2Procedure` 只有局部声明，不承担有效伤害计算。

战斗状态还使用记分板 `zdztbegin/zdzttick`（`EntityHurtProcedure:79–98`）。这些状态不能因未列入 Variables 而丢失。

元婴出窍调用链复核（2026-11-07）：`FightingState`新增已有状态的清除操作，两个分数同时写0；skill每二十刻耗神前调用mark，耗神不足自动归窍前调用clear，手动归窍不清除，依据为 `YvanyingchuqiaotickProcedure`。最终build/ArchUnit通过；沿用现有出窍5项及技能界面9项全部通过，战斗分数清除本身仅有静态核对证据，详见[元婴正文](../skill/architecture.md#元婴出窍2026-11-07)。

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

### 共享落雷（2026-11-07）

`B:LightningLandingCommand` 无权限限制、允许无参数并返回 0；`LightningLandingGo` 先生成彩色闪电，再以命令原坐标为中心、范围直径除二扩展 AABB，按距离排序，对 Mob/Player 执行 `shanghai 雷`。颜色支持灰、黄、金、红、血、绿、天蓝、海蓝、紫、黑，其余为白，均为不透明 ARGB。独立伤害只在第七个参数为“独立”时开启。队伍词只占位，空词前移结果见 Q55；不改写来源参数语义。

`ColoredLightningBolt`、`LightningLanding` 与 `Lightning.command` 承担共享落雷，命令与彩色闪电归 combat。实体坐标取原坐标向下取整后的底部中心，设置 `visualOnly=true` 与原生 damage=0.1；雷伤另经 `/shanghai`。保留原生闪电生命周期、声音及避雷针/铜交互，注册为不保存、零尺寸、追踪范围 16、最大更新间隔。客户端复用精确目标 NeoForge 21.1.265 的 `LightningBoltRenderer` 几何，通过 `VertexConsumerWrapper` 替换同步颜色与 alpha；未另写客户端玩法。技能只提供落点与次数；xiulian 雷劫完整流程已在后续批次接回，规则与验证范围见[修炼架构](../xiulian/architecture.md)“完整雷劫”。

来源实体在自身 `Register` 嵌套类注册，`B:YvanchuxitongMod.java:61` 实际挂接事件总线；此前提取器仅扫 init 而漏记。已补齐注册、人工归属与类覆盖表，来源注册总数为 4939，来源文件指纹未变。`convert_content.py` 对该 ID 明确保留独立注册路径，不再套用 init 中的普通实体构建器，也不重复生成内容表条目。

完整雷劫由 xiulian 保存与推进状态，combat 在 `CombatSetup` 监听其 `TribulationLightningEvent` 和 `TribulationDamageEvent`，分别调用共享落雷与伤害命令。这样保持 combat → xiulian，避免修炼反向依赖战斗。五项雷劫主路径专项均通过；这不覆盖先前落雷检查未通过的两个技能入口。

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
- 行为接口 `EffectBehavior`：`started` 与 `ended` 由 `ContentEffect` 的原生属性添加/移除钩子执行，普通施加、刷新、强制施加和隐藏弱效果恢复均遵循原生顺序；`ContentEffects` 的事件及服务端效果刻只绑定实体上下文。`tick` 为服务端每刻，`clientTick` 为客户端每刻。2026-11-07 修正此前在写入前事件中执行开始/结束的时序错误；共享机制和本轮回归证据见[技能状态效果正文](../skill/architecture.md#状态效果身份与原生生命周期复核2026-11-07)。
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

每刻行为的共同结构（`PoisonEffects`）：只作用于存活的生物和不在创造、旁观模式的玩家；周期型到期后玩家按体质 `PoisonLoseEfficacyPercentage` 的概率免疫本周期（计数照常清零），伤害按体质 `PoisonEffectPercentage` 放大后直接扣减当前生命（`setHealth`，不经伤害结算）。累积层数玩家存在 xiulian 的体质键、其他实体存在持久化数据。结束回调按下文各自条件执行，不继承每刻行为的存活、模式或免疫检查。

| 效果 | 周期与行为 |
| --- | --- |
| `duyao_01` 断肠 | 每 100 刻 4 × (等级 + 1) |
| `duyao_02` 罗刹 | 每 200 刻精力减 100 × (等级 + 1)（放大后），不低于 0（玩家） |
| `duyao_03` 雷公藤 | 瞬时，3 × (1 + 等级)²，伤害不为 0 时结算并移除；被体质免疫时保留效果，下一刻重判 |
| `duyao_04` 鹤顶红 | 每 200 − 20 × (1 + 等级) 刻 4 点 |
| `duyao_05` 瘴气 | 每 40 刻累积层数（玩家首次 1、此后 +0.1），伤害 层数 × (1 + 等级)，并清除速度、力量、治疗、跳跃、生命恢复、抗火、水下呼吸、生命提升、伤害吸收、幸运；结束时仅清非玩家 `Duyao05level` |
| `duyao_06` 含笑半步颠 | 每 5 刻检查取整坐标：站定记录，下次坐标变化即受 100 点凋零伤害并爆炸（音效 `entity.generic.explode`、爆炸粒子）；游泳、跳跃、骑乘、岩浆中清除记录 |
| `duyao_07` 一日丧命散 | 每 20 刻累积 1 层，满 1200 层毙命；玩家灵力压到 1，其他生物每次被沉默 40 刻；结束时玩家体质或非玩家持久化数据的 `duyao07level` 清零 |
| `duyao_08` 砒霜 | 每 20 刻 2 × (等级 + 1) |
| `duyao_09` 番木鳖 | 每刻把当前生命压到 最大生命 − 10 × (1 + 等级)（体质放大目标值，至少 1） |
| `duyao_10` 蜂毒 | 瞬时，4 × (1 + 等级) 并附加缓慢（60 刻，20 级）与发抖（20 刻，1 级），移除 |
| `duyao_11` 青藤毒 | 每 20 刻 (1 + 等级) 点并扣灵力 10 × (1 + 等级)（玩家，不低于 0） |
| `duyao_12` 寒毒 | 每刻冷冻值 +1，每 80 刻发抖 20 刻 |
| `duyao_13` 赤炼毒 | 每 40 刻 4 × (1 + `duyao_01` 的等级)（Q33） |
| `duyao_14` 烙毒 | 每 1200 刻 20 × (等级 + 1) |
| `duyao_15` 蛇纹毒 | 每 100 刻 最大生命 × 1% × (1 + 等级) |
| `duyao_17` 孢子 | 每 100 刻随机获得缓慢、中毒、霉运、发光、虚弱、失明、隐身之一，100 刻，等级同孢子 |
| `duyao_19` 毒厄丹 | 开始时附加眩晕、剧痛、无力、发抖各 1200 刻 3 级；结束时由 combat 将来源指定七类生物原地转换为僵尸，详见下文 |
| `duyao_20` 尸毒 | 每次感染累积 1 层；每 80 刻受 (等级 + 1 + 层数) 点，身上有瞬间治疗或生命恢复时层数清零；体质放大被覆盖不生效（Q33） |
| `duyao_16`、`duyao_18` | 标记，等级由伤害结算读取 |

`duyao_21`（曼陀罗）经 `/shanghai` 结算，随伤害结算迁移。

### 毒药结束回调补齐（2026-11-09）

已逐份读取 A 来源三项结束过程及当前 `PoisonEffects`，在 `Miasma.ended`、`DayDeath.ended` 和 `duyao_19` 的 `ended` 接回来源行为。`ContentEffect.removeAttributeModifiers` 调用这些回调，因此自然到期、主动移除以及效果刷新重应用中的属性移除均可触发；不能把过程名中的“结束”理解为仅剩余时间归零。

`A:Duyao05DangXiaoGuoJieShuShiProcedure:10–12` 只把非玩家持久化数据 `Duyao05level` 设为 0，玩家体质中的同名层数保留。`A:Duyao07DangXiaoGuoJieShuShiProcedure:12–16` 则把玩家体质或非玩家持久化数据中的 `duyao07level` 设为 0；目标玩家写入经 xiulian 的 `Cultivation.setTizhiDouble`，保留准确键名。两个过程都不清周期计数。

`A:Duyao19DangXiaoGuoJieShuShiProcedure:27–92` 仅匹配 `Animal`、`Villager`、`Witch`、`Pillager`、`Vindicator`、`Evoker`、`Illusioner`。服务端在原坐标直接 `new Zombie(EntityType.ZOMBIE, level)`，随机水平朝向、俯仰 0；依次转交原实体主手、副手、头、胸、腿、脚六个 `ItemStack` 引用。随后将基础 `MAX_HEALTH` 设为 130、`ATTACK_DAMAGE` 设为 6，并写持久化 `attack_Def=6`、`mana_Def=4`、`soul_Def=10`、`LEVELnumber=1`，再执行 `finalizeSpawn(MOB_SUMMONED)`、`addFreshEntity`，最后移除原实体。目标保留该顺序，不主动回满当前生命；生成初始化仍按原生过程执行。转换由 combat 的效果结束回调直接承担，原“随 flow 迁移”的说明已纠正。

本批来源与实现的静态核对、构建及专服加载保存退出通过。专服用原生命令确认05/07非玩家主动移除和刷新时层数归零，19主动移除、刷新及自然到期时猪转僵尸；原猪均移除，僵尸持久字段为6/4/10/1，样本主手铁剑保留、基础最大生命130、攻击6、当前生命20。内置客户端回归、首次失败与证据见[炼丹月志](../liandan/logs/2026-11.md)；自然服用/喂用、玩家分支、其他六类转换、多人及两版本对照未验收。

### Q235 灵力与神识伤害的盾牌标签

1.18.2 版本 `ShanghaiGoProcedure` 四个分支均调用 `bypassArmor()`。原版 `LivingEntity.isDamageSourceBlocked` 在方向判定前排除此标志；Forge 40.1.0 的补丁在已满足该谓词的分支内触发盾事件，不改此前过滤。1.21.1 版本 NeoForge 21.1.265 的同名谓词改查 `DamageTypeTags.BYPASSES_SHIELD`，护甲结算另查 `BYPASSES_ARMOR`。精确版本的原生资源包中，`bypasses_shield` 引用 `#minecraft:bypasses_armor`；项目以普通追加方式将 `linglidamage`、`linglidamage_player`、`jinglidamage`、`jinglidamage_player` 加入后者，因此默认数据包配置下四种类型无需另列盾牌标签。

[风三野羊实测](../creature/logs/2026-11.md#2026-11-19-人间道风系自然野羊互伤)只证实两次震地在玩家举盾时仍造成伤害，未记录伤害源向量满足正向条件。独立的[原生盾牌正面对照](logs/2026-11.md#2026-11-19-q235-原生盾牌正面对照)已确认默认生效标签及带来源两类伤害不挡、不损盾；不能用 `LivingShieldBlockEvent` 出现推定盾挡。未改标签，未执行来源游戏或两版本实机对照。

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

## 淬毒的归属（2026-11-04 改判）

淬毒（炼药锅 `cauldronduyao` 记录毒药材料，右键武器附毒，或空手右键 `cailiao_141`–`146` 给副手武器附毒）读写物品 `lianqizong` 里的附魔记录 `fumo{序号}`、`fumozongshu`、`fumo{序号}num`（lianqi 的工艺数据），玩法含义在 lianqi，方块 `cauldronduyao`、过程 `CauldronduyaoZaiFangKuaiShangYouJian`、`Jinduliebiao`、`Cailiao141DangYouJianDianJiKongQiShiShiTiDeWeiZhi` 改归 lianqi；毒药附魔的效果仍在本系统（`AttackStage`）。来源条件前后不一致，记 Q35。

## 木人桩（2026-11-04 已实现，会话决定）

来源 `Murenzhuang`（GeckoLib 动画实体，MCreator 生成）、`Murenzhuangitem`、界面 `MurenzhuangGui`、11 个过程与合成配方。目标：

| 来源 | 目标 |
| --- | --- |
| `MurenzhuangEntity`（`PathfinderMob` + `IAnimatable`，MONSTER，0.5 × 2，跟踪范围 64、更新间隔 3） | `WoodenDummyEntity`（`GeoEntity`），实体类型经 `entities.json` 登记；1024 点生命、无 AI、持久、不被推挤；受击先 `react`：按战利品表 `entities/suijizhanlipin` 随机取一件材料，`cailiao_191`–`199` 对应 attacked01–05 动画并 `triggerAnim("main", …)`，下一刻（`tick`）向攻击的玩家报告 `伤害：<最大生命 − 当前生命>` 并回满生命；不接受药水云、跌落、仙人掌、溺水、凋零；死亡 20 刻后掉出木人桩物品 |
| 动画控制器 `movement`、`procedure` 与同步数据 `ANIMATION`、`animationprocedure` | 单个控制器 `main`：待机循环，受击动画用 `triggerableAnim` 触发，播完回到待机 |
| `MurenzhuangitemItem` 右键方块 | `WoodenDummyItem.useOn`：点方块上表面且上方是空气时在其上放置一个木人桩，朝向与玩家相反，消耗一个（创造模式不消耗，与原版物品使用一致） |
| 界面 `MurenzhuangGuiMenu`/`Screen`、`MurenzhuangGuiButtonMessage` | `DummyMenu`（四格装备槽 7/25/43/61, 10 与玩家物品栏）、`DummyScreen`（三个输入框、三个设置按钮、拿走木人桩按钮，坐标与贴图沿用来源）、`DummyButtonPayload`；打开时把木人桩的三项防御基值抄进玩家体质的 `attack_Def`、`mana_Def`、`soul_Def`，标签据此显示（来源如此）；关闭时把四格装备穿回木人桩；来源靠体质键 `chufauuid` 在 2 格内找木人桩，目标由菜单记实体编号 |
| `Murenzhuangdel` | 拿走木人桩：退回装备槽物品、移除木人桩、给玩家一个木人桩物品并关闭界面 |

实机检查（`CombatCheck`）：物品放置、受击 6²/(6+0)、下一刻回满生命、设置物理与法术防御并在数字无法解析时置 0、装备槽的头盔关闭后穿在木人桩上、拿走木人桩；截图 `combat_dummy.png` 目视确认 GeckoLib 模型、待机姿态与“伤害：6.0”报告。

GeckoLib 首次接入（版本 4.9.3）的做法写入[工程结构](../../development/engineering.md)“依赖治理”，目标版本 API 事实写入移植技能的 NeoForge 1.21.1 参考。

## Q31 剧情伤害字段登记边界（2026-11-15）

Quest普通脚本“伤害实体/法术或精神”来源仅写ManaAttackDamage或SoulAttackDamage、对应UUID、damagetype和Damage_multiplier，本批经Combat公开操作markQuestAttack恢复该写入。A ManaDamage:50–61、SoulAttjisuan:50–61存在字段读取但无调用者；DamageIntegration另有读取但同属Q31无可达入口。目标不直接调用deal补造结算，不因恢复写入宣布Q31闭合；字段消费仍待集中核实/裁定。


## 玩家击杀收益（2026-11-18）

1.18.2 版本 `EntityDeathProcedure:39–144` 只在真实死亡且伤害来源的 `getEntity()` 为玩家时发放收益，不追溯最近攻击者。`PlayerDeathRules` 读取死者实体镜像 `LEVELnumber=L` 和 `additional_experience`。`L≥1` 时待吸收基础为 `L^2.4+4L+5+additional_experience`；否则取普通 `Mob` 生命上限的五分之一，其他实体为零。按主修功法名应用五行加成；击杀者层数不高于死者时扣 `(L−击杀者层数+1)^2` 罡煞。功德依序判定：非妖兽动物减1、亡灵加 `L+2`、其余 `Mob` 加 `L+1`。收益由修炼所有者写入，原修炼周期负责吸收；不改非玩家成长与五行击杀统计。人间道重生标记见[修炼正文](../xiulian/architecture.md#普通重生2026-11-08)。

## 五行击杀统计管理命令（2026-11-15）

来源`A:YvanchuGo3Procedure:216–526`接在已注册`/yvanchu`的`experienceup`链中，五行各有击杀数量`killentitygold/water/tree/fire/soil`和属性技能熟练度`killentitypointsgold/water/tree/fire/soil`，合计十个精确关键字。每项只读取参数3是否恰为`set`：是则把参数4解析为double后赋值，否则在旧值上加参数；不是按`minus`字面值做减法。负数、小数、科学记数法和超过正常成长范围的值保持，格式错误或缺参数取0。可选参数5恰为`true`时，向目标发金色“某属性技能杀死的实体数量设置为/增减量为”或“某属性技能熟练度设置为/增减量为”，均回显原参数字符串。

目标共享`flow.PlayerCommandFlow`按原权限1、命令源当前维度首个玩家显示名精确匹配后调用`combat.api.ElementStatistics.command`。十个分支写入`Shanghai.recordKill`已经使用的同一组体质载体键，保存和本人同步继续通过`Cultivation.setTizhiDouble`；没有另建附件、贡献项或重置其他进度。来源`typed_field_accesses`只记录变量字段，十键还须对照`nbt_key_calls`与[体质键台账](../xiulian/ledger/tizhiitem_keys.csv)；后者的写者为combat与flow，不能因命令中出现“属性”就归入attribute的Stat贡献。

自然生产仍由`Shanghai.recordKill`在既有伤害结算位置判断“目标当前生命小于伤害总量”后累加，Q13的实际死亡边界保持；本批没有重写或额外调用伤害生产链。xiulian的丹田页读取五行熟练度显示，金丹/元婴读取同键判断门槛，继续通过其自有载体查询，保持combat→xiulian既有单向依赖。管理命令只改指定计数，不补造击杀事件、经验或成长奖励。

独立算例：旧金击杀数量2.5，`/yvanchu <显示名> experienceup killentitygold minus -0.5 true`后为2.0，反馈末尾仍是`-0.5`；水熟练度10，`set 4.25e1 true`后为42.5，反馈仍是`4.25e1`；把火熟练度设为格式错误值会写0。十个映射、20条原回执和写入路径均已静态核对；新入口Java编译通过，十键真实set/非set与本人同步随修炼共同101项必要检查通过；命令专用保存重进、1.18.2版本实机与两版本对照未执行。源码定位与锁定哈希见[来源证据](ledger/source-evidence.csv)。

## 普通战斗状态与脱战（2026-11-18）

1.18.2 版本 `EntityHurtProcedure:79–98` 对非友方受击将 `zdztbegin` 置 1、`zdzttick` 设为受击者当时的 `HurtByTimestamp`。`EntityTickProcedure:21–27,42–98` 在生物刻先递增保存的 `zdzttick`，再以其与实体当前 `HurtByTimestamp` 的差值达到 100 为清除边界，并同时清零两个记分项。边界基于两计时值之差，而非从入战时启动固定倒计时。

1.21.1 版本由 `FightingState` 在服务端玩家刻及非玩家生物刻推进原有记分板；玩家只走玩家刻分支，避免与实体刻重复推进。仍比较当前原生 `HurtByTimestamp`，不引入登录清空、附加存档字段或维度特判。xiulian 已有灵力恢复过程在战斗状态下按四分之一倍率恢复，脱战后恢复正常倍率；本实现不改变恢复公式，也不将一般受伤作为强制结束闭关的规则。

`renjiancombatlifecycle` 人间道自动化客户端 run-01 以原生 `Zombie.doHurtTarget` 真正命中玩家，确认入战与战斗恢复；同档正常保存重开后，在 `PlayerLoggedInEvent` 读回 `active=1`、`count=239`、`HurtByTimestamp=528`，确认登录没有清除或倒退计数。随后实体刻运行至来源差值恰为 100，两个战斗分数均清零，实测灵力恢复从战斗中的 `0.05` 回到正常档 `0.2`。报告为 0 FAIL 并以 `DONE` 收尾；两次完整保存，Gradle 退出码 0。证据：`worktrees/rebuild-renjian-starter-20261118/build/diagnostics/renjian-combat-lifecycle/run-01/report.txt` 与同目录 `gradle.log`。本场景只覆盖人间道受控普通僵尸对玩家的单机路径，不等同自然目标全覆盖、多人、专服、其他维度、1.18.2 版本实机或两版本对照。

## 挣脱附魔的玩家缓慢处理（2026-11-19）

来源`A:PlayerTickProcedure:28–35,384–399`在每个玩家结束刻调用`Effectslownessget`，不受每秒修炼计时门槛限制；`EffectslownessgetProcedure:21–67`在玩家有原版缓慢且体质键`slownessget`为假时，保存当前持续刻数和等级、移除缓慢，再读取头胸腿脚及头饰、项坠、腰佩、手饰的`breakfree`等级之和。持续刻数严格大于等级和×20时，以差值及原等级重新添加默认参数的缓慢；否则不重加。无附魔也会移除并重建效果，因此原效果的粒子、图标和隐藏效果状态不保证保留。最后将标志置真；后续连续有缓慢不重复处理，无缓慢且标志为真时置假。实际短于或等于减免量的效果也要置真，下一次无缓慢刻才复位。

目标`SlownessResistanceFlow`订阅服务端`PlayerTickEvent.Post`，经combat的`Enchants`读护甲与装备系统的饰品快照，经xiulian体质操作读写标志；根入口注册，不引入系统之间的反向依赖。来源能力提供器不挂载FakePlayer，故假玩家每次读取默认假标志和空饰品，只计算护甲且不保存标志。来源同时在客户端玩家刻执行，目标仅服务端处理；两端瞬时显示、假玩家及效果同步顺序尚待对照。源码及哈希见[来源台账](ledger/source-evidence.csv)；已有中文附魔名称和说明、英文名称，英文说明为空，本批不改文本。编译、玩家真实受击、多件叠加、恰等于减免量、连续重新施加、保存重进、客户端和来源版本对照均未执行。


## 普通玩家击杀收益与 DamageSource 归属（2026-11-20）

来源 `EntityDeathProcedure.onEntityDeath/execute:37–144,291–299`：过程先要求 `event.getSource().getEntity()` 非空，再按受害者持久化 `LEVELnumber` 或生命上限计算待吸收修为，按主修功法命中候选加成，写入 `experience_Stand`，并按等级差扣 `vigorous`、按妖兽/动物/亡灵/Mob互斥顺序写 `merits`；人间界玩家死亡写入 `fuhuo = 人间界`。目标 `PlayerDeathRules` 从 `LivingDeathEvent` 的 `DamageSource.getEntity()` 读取归属，技能伤害由 `CombatDamageTypes.source(..., attacker)` 沿用同一来源实体。

本批接入中央注册并通过 `Cultivation` 公开写操作写入修为、罡煞、功德和复活标记；既有 `CultivationTick` 继续消费 `EXPERIENCE_STAND`，`LevelUp` 继续处理吸收后的境界成长。来源空攻击者仍不发放玩家击杀收益。未运行编译、客户端/专服或真实击杀，当前仅登记静态调用链与实现边界。
### 统一攻击者归因与修炼战斗共享链（2026-11-21）

本批把来源 `EntityDeathProcedure` 的玩家击杀收益从单一 `DamageSource.getEntity()` 判断扩展为可追溯的共享归因链：`CombatAttribution.player` 依次检查伤害来源的致因实体与直接实体，并沿 `CombatAttributable` 技能实体、原版 `Projectile` 主人和驯服生物主人追溯到服务端玩家。直接攻击、`SkillEntity`/`TamedSkillEntity`、`SkillProjectile`（沿用 `AbstractArrow` 的 owner）以及 `ShanghaiGoProcedure` → `Shanghai.deal` 产生的伤害，均进入同一个 `PlayerDeathRules` 死亡收益入口；`Shanghai` 原有五行击杀统计仍在伤害致死阈值处独立保留。

共享消费顺序对照 1.18.2 版本 `PlayerJoinGameProcedure`、`PlayerTickProcedure`、`LEVELupProcedure`、`EntityberebornProcedure` 和 `EntityDeathProcedure`：玩家境界以修炼附件为真值，在死亡收益读取死者境界、五行击杀积分及普通重生恢复前先刷新 `LEVELnumber` 镜像；收益写入待吸收修为、罡煞和功德，由既有修炼 Tick 吸收并进入突破消费者；普通重生先执行修炼重生修正，再刷新镜像并处理人间界 `REVIVE` 标记。该批只做生产接线和静态调用链核对，未执行 Gradle、转换器、客户端、专服或真实击杀/重生/存档重进验收。

## 人间界死亡标记

`EntityDeathProcedure` 的入口读取致因实体；若为空，直接返回。对应地，玩家在人间界因无致因伤害死亡时保留原有 `REVIVE`；若有致因实体则写入“人间界”，不要求致因者是玩家。这一条件与谁领取击杀收益分别判断。
