---
topic: combat
role: entry
status: current
updated: 2026-11-27
---

# 战斗

阶段 4 已按裁定修复 Q18/Q29/Q30（受击同属性功法减伤与负伤保护、`Shanghai.defence` 玩家体质防御及法防/神识防御倍率、`ArmorStatEvent` 法防改读 `mana_Def`、物理 `breakArmor=1`/攻击者 `AttackAttCorrectPercentage`/暴击倍率、`Shanghai.deal` 闪避 `1–100`、`AttackStage` 移除重复吸血、`Shanghai.friendly` 空团队不免伤）；见[本月记录](logs/2026-11.md#2026-11-27-阶段-4q18q29q30-战斗与防御修复)。

落雷：已知施法实体使用 `Lightning.cast`。玩家命令与直接入口保留 Q55 队伍占位语义，伤害不再逐目标拼命令。[本批验证](../project/logs/2026-11.md#2026-11-25-阶段-3-实体施法者落雷)。

盾挡后的受伤、两段吸血和替死符已修正时序，已备原生盾牌回归夹具，尚未实跑。见[规则与源码位置](architecture.md#盾挡后的受伤阶段与复活符2026-11-23)。

人间界死亡的复活标记只由有致因实体的伤害设置，详见[规则正文](architecture.md#人间界死亡标记)。

主世界普通牛和僵尸的真实近战击杀已验证待吸收修为、罡煞、功德和正常吸收，见[玩家击杀收益](architecture.md#玩家击杀收益2026-11-18)。更高层数、功法加成和自然个体样点未验。

- 人间道普通战斗状态的真实脱战、战斗/正常灵力恢复档和磁盘重开已通过受控僵尸近战场景；规则与边界见[普通战斗状态与脱战](architecture.md#普通战斗状态与脱战2026-11-18)。多人、专服、其他维度和来源版本对照未验。

标识 `combat`，类别：系统。职责：伤害结算（读 attribute 最终值）、通用状态效果、附魔、木人桩（[系统设计](../../development/system-design.md)）。

当前阶段：C 阶段完成（2026-11-04）。候选归属 258 个元素已全部读完：留在本系统 212 个，改归其他系统 46 个（淬毒归 lianqi，状态效果与附魔按读取者改归 skill、attribute、equipment、fulu、xiulian、cooking、creature、guchong、yihuo，伤害范围指示实体归 skill）。实现与验证：伤害结算（灵力、神识伤害、命令 `/shanghai`、受伤与命中处理）、85 个附魔（数据定义，命中、护甲、挖掘类行为）、状态效果 47 个（控制、修炼、毒药、战斗类与标记；全部登记 64 个含改归其他系统的标记）、连锁采集、穿石与破碎的基岩、木人桩（首次接入 GeckoLib 的动画实体、设置界面）；开发客户端 `CombatCheck` 46 项实机检查全部符合，全系统冒烟（relation、attribute、xiulian、gongfa、combat）通过。自定义粒子（`miss`、`zhongdulizi`、`basaltlizi`、`xvedi`、`smokered`、`effect_033lizi`、`smallexplosion*`、`flash_ice`、`thunderdeeppurple`、`starlightblue`、`smokedeeporange`）属于 particle，随其登记后在本系统的发出处接入；召唤 skill、fulu 实体的 `qishi`、`lingwen`、`starsky` 经 `HitEvent` 随各自系统；xiulian 推迟的完整雷劫于 2026-11-07 接回，本系统监听其具体落雷与范围伤害请求，保持 combat → xiulian 单向依赖。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 2026-11-19接回玩家的[挣脱附魔缓慢处理](architecture.md#挣脱附魔的玩家缓慢处理2026-11-19)：护甲与饰品共同缩短首次缓慢，源码接线候选尚未编译或实机。
- 候选归属 258 个元素全部已读，去向见上；这是检索范围，不是已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

2026-11-15补齐共享`/yvanchu`下的五行击杀数量与属性技能熟练度十项管理分支，复用既有伤害统计的体质载体键；静态核对完成，新增十键真实命令与本人同步已随修炼场景通过必要检查；命令专用保存重进待验，见[正文](architecture.md#五行击杀统计管理命令2026-11-15)。

## 公开操作

已对照 `typed_field_accesses.csv` 与 `nbt_key_calls.csv` 中战斗相关字段的外部读写点（`ManaAttackDamage` 等未闭合的字段链见[架构](architecture.md)Q13、Q31，不迁移）。代码在 `system/combat/api` 与 `system/combat/damage`。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 统一击杀归因 | `CombatAttribution.player(DamageSource)`：按致因实体、直接实体及投射物主人、技能实体施放者、驯服主人解析服务端玩家 | `LivingDeathEvent` 的玩家收益结算；`Shanghai` 五行击杀统计复用同一归因 |
| 五行统计管理命令 | `ElementStatistics.command(目标玩家, action, subject, operation, value, feedback)`：只接受`experienceup`和十个来源统计键；set赋值，其他操作累加 | flow的唯一`/yvanchu`入口；载体读写经xiulian公开操作 |
| 战斗状态 | `FightingState.mark(生物)` 写开关1及受击时间；`clear(生物)` 将两项同时清0 | 受伤、技能施放、元婴出窍每二十刻推进与耗神不足归窍 |
| 共享范围落雷 | `Lightning.cast`（实体）、`castSaved`（存档主人）、`tribulation`（无来源劫云）；命令 `/lightninglanding <攻击量> <攻击者UUID> <倍率> <队伍占位> <范围直径> <颜色> [独立]`；同步颜色的原生闪电负责显示，范围内的 Mob/Player 经 `Shanghai.deal` 结算雷伤 | skill 的天雷术、五雷轰顶、控雷阵与 xiulian 的雷劫；队伍词只占位，缺失时按来源空白分词前移 |
| 造成灵力或神识伤害 | 其他系统经 `SpellDamage.deal(level, 受击者, 类别, 攻击者, 是否固定, 基础量, 倍率)`；存档攻击者文本走 `SpellDamage.dealSaved`，区分空字段、无来源标志和无效 UUID（`dealRounded` 先把基础量与倍率取整，对应来源用 `DecimalFormat("0")` 格式化的调用）；范围命中可用不带 `level` 的重载，按目标所在维度只结算服务端生物；本系统内 `Shanghai.deal(ServerLevel, 受击者, Request(类别, 攻击者UUID或空, 是否固定, 基础量, 倍率))`；命令 `/shanghai <类别> <攻击者UUID或无> <true或false> <基础量> <倍率>`（无权限要求，对命令执行者生效） | 技能、符箓、妖兽、法宝、蛊虫、异火、装备等的 209 处命令调用；本系统的护甲与武器附魔、毒药 |
| 雷劫伤害请求监听 | 监听 xiulian 的 `TribulationLightningEvent` 与 `TribulationDamageEvent`，分别交给 `Lightning.tribulation` 与 `SpellDamage.deal` | 劫云七种天雷及地滚雷、雷柱、雷龙；请求保留原坐标、已格式化的攻击量与小数倍率，xiulian 不导入 combat |
| 附魔等级 | `Enchants.level / mainHand / slot / armorSum / armorSquares`，附魔按路径名取键 `Enchants.key("dodge")` | 本系统的伤害过程；attribute 的显示值按等级线性折入（自行读取物品） |
| 状态效果查询与施加 | `StatusEffects.amplifier / has / remove / add / holder`，效果按路径名（如 `effect_036`）指定 | 全部读取或施加状态效果的系统（creature 约 280 处、skill、fulu、equipment、liandan 等） |
| 实体禁空标记 | `StatusEffects.markEntityFlightBlocked(entity)`写入来源`jinkong`标记；`consumeEntityFlightBlock(entity)`读取并清除一次 | zhenfa写入；skill与equipment的飞行逻辑分别消费 |
| 阵法伤害标记 | `SpellDamage.markFormationSoulAttack`登记阴二；`markFormationManaAttack`登记阳一/阳四整数攻击量；`markFormationFallingAttack`登记阳三浮点攻击量并保持目标攻击UUID原值 | zhenfa范围效果与弹体命中；均只登记来源字段，不接回Q31缺失的消费链 |
| 效果的行为登记 | `ContentTables.effectBehavior(效果ID, 行为)`（`EffectBehavior`：开始、结束、每刻、客户端每刻） | 效果归其他系统的行为（`effect_035` 归 skill，`yaoshou_*` 归 creature，`fulutexiao_*` 归 fulu） |
| 向其他系统借用规则的事件 | `DamageImmunityEvent`（creature 的防御姿态免伤）、`LethalDamageEvent`（fulu 的 `itemfulu_16` 复活）、`ArmorStatEvent`（lianqi、equipment 提供四件护甲的炼器数值）、`HitEvent`（gongfa 的命中特效）、`HeavyHitEvent`（达到目标最大生命十六分之一后的妖兽重击打断）、`EffectExperienceEvent`（creature 给妖兽加修为） | 监听者在对应系统迁移时订阅 |

约定：

- 本系统只读 attribute 的最终值（攻击、防御、灵根与功法五行系数）和 xiulian 的体质键、灵力、精力、境界；非玩家实体的攻击防御读实体持久化数据里来源的键（`mana_Att`、`soul_Att`、`mana_Def`、`soul_Def`、`attack_Def`、`LEVELnumber`），写入方是各自的生成过程（随 creature、skill 定为实体属性）。
- 伤害在 `LivingIncomingDamageEvent` 中结算（命中处理在前、受伤处理在后），没有攻击者的伤害不处理。


本系统本轮变化见[战斗月志](logs/2026-11.md)，跨系统流程变化见[项目月度日志](../project/logs/2026-11.md)。

共享落雷验证（2026-11-07）：仅一次 `-PsmokeOnly=lightning`，四项中三项通过并 `DONE`，范围雷伤、红色同步/渲染器及控雷阵命令均走真实世界路径。两个技能入口的合并断言失败，检查辅助方法连续选同槽导致第二次取消；已在选择前清空并编译，未重跑，不能记为两个入口实机通过。截图早于有效画面，完整视觉待核。build（含 ArchUnit）通过，专用服务器启动、全部世界保存及正常退出通过，ERROR 0，实体导出为 63 个。该落雷批次未执行完整 xiulian 雷劫。后续雷劫专项一次 5 项全部通过，并在专用服务器确认 67 个实体注册及正常停服；完整验证范围见[修炼架构](../xiulian/architecture.md)“完整雷劫”。

2026-11-07 共享状态效果生命周期修正：开始/结束改由原生属性钩子执行，事件只绑定实体上下文，恢复强制施加和隐藏弱效果接替的原生语义。build/ArchUnit通过，已有内置combat场景53项全部通过并DONE、正常保存退出，含8项真实时序Bug回归；未新增Test类或选择器。规则与验证范围见[状态效果方案](architecture.md#目标方案)。本轮没有逐一验收47个效果。

2026-11-09 毒药结束回调补齐：`PoisonEffects` 已接回 `duyao_05` 的非玩家瘴气层数清零、`duyao_07` 的玩家/非玩家层数清零，以及 `duyao_19` 对来源七类生物的原地僵尸转换。三项均随原生属性移除钩子执行，包含刷新重应用时的移除；毒厄丹转换属于 combat。来源与当前实现已静态核对，具体规则见[结束回调](architecture.md#毒药结束回调补齐2026-11-09)。本批构建通过，专服原生命令验证05/07非玩家主动移除与刷新清零、19三种结束路径的猪转僵尸及属性装备样本；内置客户端回归和首次失败记录见[炼丹月志](../liandan/logs/2026-11.md)。玩家分支与其余六类转换未验收，不扩展此前 combat 批次的验收范围。

`SpellDamage.markQuestAttack`由Quest剧情动作调用，只由Combat写入来源法术/精神临时伤害、UUID、类型和倍率；当前不直接结算，与Q31不可达的另一版消费链分别记录，不将字段登记当作伤害路径完成。

`SpellDamage.markHotOilContact`由Decor热油接触回调调用，只由Combat登记无来源的火系灵力攻击字段，不发起扣血。见[热油正文](../decor/architecture.md#热油与滚油桶2026-11-18)。
