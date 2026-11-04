---
topic: skill
role: architecture
status: draft
updated: 2026-11-01
---

# 技能：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与入口

技能具有真实 ItemStack 实例：16 槽、飞行、轻功、武器技和弓技各自有载体。`A:JinengGOProcedure:21–139` 构造书/技能物品并写入需求、伤害、消耗、结印和六类冷却；声明值需追到实际施放消费者。

`A:ShenfaTickProcedure:16–51` 分别推进 W/S/A/D 输入窗口、身法冷却和跳跃时间。工程共有 15 个按键元素，完整清单包含修饰键、取消、切换与任务操作。技能效果还使用弹体、效果实体、声音和原生命令。

## 目标所有权与接口提案

技能定义、技能物品组件、玩家槽位、当前施法、弹体状态分别建模。公开入口：选择/装备、尝试施放、取消、更新输入、推进施法与冷却。资源通过成长服务扣除，命中通过战斗服务，属性以只读结果取得。

有持续时间的施法保存阶段、剩余 tick、目标身份和所属实体；声画参数独立同步。来源中的动态事件监听有实际延迟用途，改为所属施法或装置的定时推进，并明确玩家退出、死亡、换维度、实体移除时的结束方式；不建立全项目重试调度平台。

## 保真约束

按来源各技能保留结印、持续、冷却分组、弹道、移动和伤害次序；同族可共享机制，特殊技能单独保留。按键输入和专用服务器类加载需分别验证。

## 裁定（Q15）

配方 `skillarrow_10hecheng`（无序：箭 5 支加光羽 `cailiao_044` 1 个）在来源中产出 5 个光灵箭 `skill_arrow_10`，而该物品 `stacksTo(1)`、`tab(null)`（不在创造栏）。堆叠上限是物品自己的属性，配方产出数量是配方的属性，来源里两者矛盾：1.18.2 不检查，产物是数量为 5 的超额堆叠；1.21.1 解析配方时检查“产物数量不得超过该物品的最大堆叠”（`Item stack with stack size of 5 was larger than maximum: 1`，2026-11-02 开发服务端实测），配方因此不加载。

**负责人 2026-11-02 裁定（方案 A）：光灵箭的堆叠上限保持 1，配方产出改为 1 个。** 依据来源的事实：同系列 `skill_arrow_01`、`03`–`09` 堆叠 64、配方产出 4、6、10 个，只有 `02` 与 `10` 堆叠为 1；`SkillArrow10Item` 蓄力后释放，发射 `SkillArrow10Entity`，并调用 `hurtAndBreak(1)`，物品没有设置耐久，按 `ItemStack.hurtAndBreak` 的逻辑不会损耗，多得 4 个没有实际收益。转换工具对“产物数量超过物品堆叠上限”的配方一律降到上限并记录，目前只有这一条；实测 `runServer` 加载该配方，ERROR 0 条。

## 迁移阶段与第 1 阶段（框架）阅读结论（2026-11-04）

规模：400 个归属元素（过程 294、动画实体 35、弹体物品 26、药水效果 14、按键 10、配方 10、命令 5、附魔 3 等），过程源码约 1.2 MB。按依赖关系分阶段迁移，每阶段独立提交并经开发客户端检查：

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| 1 框架 | 技能书、技能栏与选择、施放与冷却、命令、藏宝与附赠、霸体与禁施 | 完成 |
| 2 弹体技能 | 弹体实体基础（`SkillProjectile`、`ProjectileSkill`）与 14 个元素弹体技能（金芒术、水球术、水灵弹、冰锥术、破碎冰矛、飞羽冰花、拈花飞叶、风弹术、火球术、光弹术、定魄神光、幻光术、飞石术及生命感知；水系小水弹 `Water0101` 待调用者）；`SkillGold03`（依赖动画实体）归阶段 3；10 种技能箭、箭种选择、`Lingwentexiao` 待做 | 进行中：元素弹体完成 |
| 3 实体技能 | 35 个动画实体（`SkillEntity` 通用模板 + 按实体登记的行为）与 12 系的施放过程 | 进行中：基础、火系（大火球、引火、控火术、火焰护盾、火焰漩涡、火龙卷）、土系（滚石术、天外陨石、化石手、巨石锤）、金系（金刚轮四式、捆妖轮、金弧轮、天堑金轮）、冰系（冰天雪地、裂冰斩）与光系（闪光术、万丈光芒）完成；火鸟术（可驯服 AI 实体）、水、木、风、雷、阴、剑法待做 |
| 4 身法、轻功、武器技 | 按键输入（W、S、A、D、空格、F）、身法与轻功的刻推进、剑法、`Effect035` 禁空 | 未开始 |
| 5 界面与表现 | 技能界面（四技能按钮、翻页、箭种轮盘）、冷却与格挡 HUD、技能界面六个功能按钮（扫描、丹火、妖化、虚空、自爆、出窍）、格挡实体、粒子 | 未开始 |

第 1 阶段的来源事实：

- 技能书是真实物品，来源 NBT 键见 `SkillBook` 类注释；承载物品由命令第一个参数决定（`book_003` 等），名字写入自定义名称；品阶与境界文字由数值推出（`SkillText`）。生成途径只有四个命令、书架藏宝（13 本，`Jinengget01/02`）与研习两本指定功法后的附赠；`/jineng` 把书掉在命令位置，其余三个放进执行者背包。
- 技能栏 16 槽分四页，每页四槽；技能界面的四个按钮（按钮 0–3，来源 `AnjianZ/X/C/V`）选择当前页对应槽，同页同键再按取消；Tab 翻页，B 取消；沉默与禁施时除第 1 键外不能选择，提示文字对调（Q39）。
- 右键施放入口 `PlayerRightClick` 由两个分支组成：开灵附魔的工具射出（归 equipment）与手持武器施放（归 skill）。施放条件次序：武器类别 → 本人研习 → 沉默 → 灵力 → 冷却 → 禁施；然后禁施、分派、累加冷却、扣灵力、连招、战斗状态、挥手。灵力消耗 = (法术攻击基础值 + 武器 `mana_Att`) × 消耗系数，再加体质固定值、乘体质百分比。
- 冷却计算有两处疑似错误（Q36），武器重量检查读错对象（Q38），`/jineng` 缺权限（Q37），均保持来源行为。连招判定（`Jinenglengqvejiance`）与施放判定的语义不同：前者逐分组取最后一个有冷却分组的结果，后者任一分组满足即可。
- 格挡（`Canjian`）在来源中只设置计时 `canjianjishi` 并驱动 HUD 十帧画面，没有任何伤害过程读取它，实体 `Gedang` 也没有生成者；随阶段 5 的 HUD 迁移，不加伤害减免。
- 技能界面按钮 6–16 选择箭种（`arrowlinshi`，equipment 读取）；按钮 17 以后是六个功能按钮，随阶段 5。

目标设计（第 1 阶段已实现）：

- 数据：`SkillBook` 物品组件（公共字段加数值表与文本表，键沿用来源，四类共用）；`SkillState` 附件（技能栏 16 槽、身法、轻功、武器技、页、选择、连招开关，死亡复制，同步给本人）；`SkillCooldowns` 附件（六分组，来源仅非死亡克隆复制，同步给本人）。
- 规则：`SkillSelection`（页与选择）、`SkillCasting`（施放、冷却、连招）、`SkillStudy`（研习，灵根要求复用 xiulian 的 `RootRequirement`，gongfa 一并改用）、`SkillLibrary`（命令参数解析、藏宝与附赠表）、`SkillReleases`（技能名到施放过程的分派表）。
- 跨系统：战斗状态记分板抽成 combat 的 `FightingState`；状态效果的结束钩子 `EffectBehavior.ended` 补进内容表框架（到期、移除、刷新前）；武器数值经 `WeaponStatEvent`。
- 数据表：`tools/porting/extract_skill_books.py` 提取 `content/skill/treasure_books.json`（13 本）与 `studied_bonuses.json`（2 条）。

阶段 2 的实现要点：

- `SkillProjectile` 继承 `AbstractArrow`，基础伤害 0.1（发射时可改）、无拾取；1.21.1 的箭没有击退与穿透的设置方法，覆盖 `doKnockback`、`getPierceLevel`；计时、攻击量、倍率、技能名、转角、方向在实体字段上（来源放在 `getPersistentData`）；停在方块里时消失；行为只在服务端、有发射者时调用。内容表用 `ContentTables.entity` 登记，工厂带 `ProjectileSkill`。
- 命中取值有两种：多数在第一刻记下发射者所选技能的攻击量与倍率；金芒术在命中时现读发射者当前所选技能（连招换技能后会变）。发射者不是玩家时攻击量取其 `LEVELnumber`。
- 敌友的判定方总是发射者（`Friendpanding*` 六个变体只是参数次序）；伤害经 combat 的 `SpellDamage`。
- Q40 记录拈花飞叶的取值键错误等；飞羽冰花沿侧向圆弧飞行（每刻转 20 度，计时每刻加两次），正前方的目标打不到。
- 自定义粒子（`SMALLEXPLOSION*`、`FLASH_ICE`、`SMOKELIGHTGRAY`、`SKILL_GOLD_0101`、`PEACHFLOWERLIZI`）随 particle 迁移，弹体只保留原版粒子。

阶段 3 的实现要点（2026-11-04 起）：

- 35 个动画实体除 `SkillFire06`（火鸟）与 `Skillyin01`（起尸，两者继承 `TamableAnimal` 带 AI 目标）外共用 `SkillEntity`（`PathfinderMob` + GeckoLib）。表现与属性由 `tools/porting/extract_entity_models.py` 从来源的 Model、Layer、Renderer 与实体类提取为 `content/skill/entity_models.json`（几何、动画、头部骨骼、发光层、阴影、默认与离地循环动画、动画清单、五项属性、是否无 AI），并维护 `system_assets.csv` 的 skill 行；实体行为按实体 ID 登记在 `SkillEntities`（`EntitySkill`：刻、初始生成、被杀、受伤），内容表只登记台账已迁移的实体。
- 来源放在 `getPersistentData` 的施放信息（`AttackUUID`、`AttackDamage`、`Damage_multiplier`、`damagetype`、`jinengName`）改为字段，随存档；各技能私有的计数（`tick`、`tick2`、`kaishi` 等）仍用 `getPersistentData`，与来源一致。实体继承施放者的团队与友方集合，敌友的判定方是实体自己（`EtoE(entity, 其他)`）或施放者（施放过程内）。
- 过程动画 `setAnimation(名)` 用 GeckoLib 的触发动画（`procedure` 控制器，登记动画文件中的全部名字），播放一次后回到默认循环；纹理名同步给客户端（默认透明的 `kong`），发光层以 `RenderType.eyes` 重画同一几何。
- 伤害经 `SpellDamage.dealRounded`（来源把攻击量与倍率经 `DecimalFormat("0")` 取整）；来源拼出命令字符串的调用，经 `SpellDamage.command` 复现同样的解析（Q41）。
- 火系：`FireEntities`（大火球、火焰漩涡、火龙卷的实体刻过程，引火、控火术、火焰护盾的施放过程）。
- 土系：`RockEntities`（滚石术、天外陨石、化石手、巨石锤）。化石手与巨石锤按 `dangqianleixing`（类型一、类型二）和 `jindu`/`zhuangtai` 分段推进，潜行施放为类型二并播放对应动画；Q42 记录巨石锤的眩晕加给自己与化石手攻击方向随机。
- 过程动画改为同步的 `ANIMATION` 字符串（来源 `setAnimation` 在放入世界之前调用，触发动画会丢失），客户端的 `procedure` 控制器按名字播放一次。
- 金系：`GoldEntities`（金轮实体八种模式、金刚轮施放）与 `GoldWheel`（金轮弹体：捆妖轮、金弧轮、天堑金轮，第一刻生成跟随自己的金轮）。金轮的跟随对象记在实体的 `master`（施放者、弹体或被捆目标）。金刚轮的 Ctrl、Alt 式读玩家变量 `lctrl`、`lalt`，来源由 flow 的 Ctrl、Alt 键过程写入、多个系统读取：落在 `platform/input/ModifierKeys`（玩家附件，客户端每刻查看左 Ctrl、左 Alt 变化时通知服务端，不入存档），避免系统依赖 flow。无 AI 的生物（如冒烟检查里的目标）不会按速度移动，推开类效果只能核对速度。
- 冰系：`IceEntities`（冰天雪地、裂冰斩）。两种实体把攻击量与倍率截成整数记下（来源 `friendlist.putInt`）；裂冰斩的攻击量同时是后续冰刺的剩余根数，每根 3 刻后在推进方向生成下一根（`SkillEntities.chain`），初始生成过程（`finalizeSpawn`）设置“开始”状态与展开动画。
- 光系：`LightEntities`（闪光术、万丈光芒）；万丈光芒对生命低于最大生命 10% 的直接清零，来源的三次延时光爆粒子随 particle。

