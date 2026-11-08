---
topic: skill
role: architecture
status: draft
updated: 2026-11-08
---

# 技能：架构调查与提案

来源事实与目标方案分别陈述。未标明已实现的目标方案仍为草案；源码定位采用 [系统导航](../README.md)的 A/B 规则。
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

最初阶段划分为401个归属元素（过程297、动画实体35、弹体物品26、药水效果14、按键10、配方10、命令4、附魔3、界面1、物品1），过程源码约1.2 MB；2026-11-08本批将15个04装配元素按实际职责从panel候选改判归skill，当前416个均已读。按依赖关系分阶段迁移，每阶段独立提交并经开发客户端检查：

| 阶段 | 内容 | 状态 |
| --- | --- | --- |
| 1 框架 | 技能书、技能栏与选择、施放与冷却、命令、藏宝与附赠、霸体与禁施 | 框架规则完成；不包含当时未接的公共书自然use与04背包装配，见本批自然入口章节 |
| 2 弹体技能 | 弹体实体基础（`SkillProjectile`、`ProjectileSkill`）与 14 个元素弹体技能（金芒术、水球术、水灵弹、冰锥术、破碎冰矛、飞羽冰花、拈花飞叶、风弹术、火球术、光弹术、定魄神光、幻光术、飞石术及生命感知；水系小水弹 `Water0101` 由激流波实际放出）；`SkillGold03`（依赖动画实体）归阶段 3；10 种技能箭的实体行为与服务端箭种选择已实现，弓条件入口已随 equipment 接回（Q57 保留自然右键断链）、轮盘已接入阶段 5；`Lingwentexiao` 弹体已随灵纹迁移 | 弹体侧实现且自定义粒子已接回；弓条件入口已接回；炙烤已由yihuo接入且主路径复验通过，完整视觉待验收 |
| 3 实体技能 | 35 个动画实体（`SkillEntity` 通用模板 + 按实体登记的行为）与 12 系的施放过程 | 进行中：基础、火系（大火球、引火、控火术、火焰护盾、火焰漩涡、火龙卷）、土系（滚石术、天外陨石、化石手、巨石锤）、金系（金刚轮四式、捆妖轮、金弧轮、天堑金轮）、冰系（冰天雪地、裂冰斩）、光系（闪光术、万丈光芒）、风系（聚风术、聚风化气、风飘术、造云术）、雷系（掌心雷、地滚雷、惊雷阵、天雷术、五雷轰顶）与水系（水蛇鞭、水连鞭、激流波、水龙吟、空浮球、陨河术、水灵阵、玲珑阵）与木系（乱枝、灰木剑、乱木之种、灰木树梯、木篱术、毒蔓鞭、回身鞭、地蔓鞭）完成，剑法（中平、柔云、拜年、弱鸡）完成；灵纹（命中生成的灵纹特效与灵纹弹）完成；火鸟术（可驯服飞行实体）与起尸诀（阴，可驯服亡灵实体）完成；虚空裂隙的实体与传送已实现（正式维度随 world、按钮随阶段 5）；伤害范围实体与公开生成 API 已实现（creature 生成者待接入，控雷阵落雷已接回）；元婴出窍实体与公开入口已实现（按钮与来源粒子已接入，视觉认可待核）；天雷术、五雷轰顶已接入 combat 的共享范围落雷 |
| 4 身法、轻功、武器技 | 按键输入（W、S、A、D、空格、F）、身法与轻功的刻推进、剑法、`Effect035` 禁空 | 主体已实现，7 项针对性检查一次全部通过；饰品附魔已由 equipment 接回并通过合法单件运行检查，来源轻功粒子已接回 |
| 5 界面与表现 | 技能界面（四技能按钮、翻页、箭种轮盘）、冷却与格挡 HUD、技能界面六个功能按钮（扫描、丹火、妖化、虚空、自爆、出窍）、格挡实体、粒子 | 部分实现：原生界面、轮盘、连招、六个功能入口、C 键与格挡实体、技能 HUD；妖化已由 equipment 接回，丹火放置与炙烤已由 yihuo 接回且主路径复验通过，完整供热链仍随D，来源有效粒子调用已接回，视觉验收仍待完成 |
| 自然入口补批 | 公共书潜行研习、04共用功法与技能装配、01/03/04导航与记页 | 完整构建与现有skillui29项通过，真实研习、背包装配、两页保存和三页导航有运行证据；其余book分支、02/05/06未展开 |

第 1 阶段的来源事实：

- 技能书是真实物品，来源 NBT 键见 `SkillBook` 类注释；承载物品由命令第一个参数决定（`book_003` 等），名字写入自定义名称；品阶与境界文字由数值推出（`SkillText`）。生成途径只有四个命令、书架藏宝（13 本，`Jinengget01/02`）与研习两本指定功法后的附赠；`/jineng` 把书掉在命令位置，其余三个放进执行者背包。
- 技能栏 16 槽分四页，每页四槽；技能界面的四个按钮（按钮 0–3，来源 `AnjianZ/X/C/V`）选择当前页对应槽，同页同键再按取消；Tab 翻页，B 取消；沉默与禁施时除第 1 键外不能选择，提示文字对调（Q39）。
- 右键施放入口 `PlayerRightClick` 由两个分支组成：开灵附魔的工具射出（归 equipment）与手持武器施放（归 skill）。施放条件次序：武器类别 → 本人研习 → 沉默 → 灵力 → 冷却 → 禁施；然后禁施、分派、累加冷却、扣灵力、连招、战斗状态、挥手。灵力消耗 = (法术攻击基础值 + 武器 `mana_Att`) × 消耗系数，再加体质固定值、乘体质百分比。
- 冷却计算有两处疑似错误（Q36），武器重量检查读错对象（Q38），`/jineng` 缺权限（Q37），均保持来源行为。连招判定（`Jinenglengqvejiance`）与施放判定的语义不同：前者逐分组取最后一个有冷却分组的结果，后者任一分组满足即可。
- 格挡（`Canjian`）在来源中只设置计时 `canjianjishi` 并驱动 HUD 十帧画面，没有任何伤害过程读取它，实体 `Gedang` 也没有生成者；随阶段 5 的 HUD 迁移，不加伤害减免。
- 技能界面按钮 6–16 的服务端箭种选择与 `arrowlinshi` 附件已实现，轮盘界面已在阶段 5 接入；按钮 18–23 的六个功能入口均已接入，妖化经 equipment 的请求监听执行，丹火放置与补燃料已由 yihuo 的公开入口接入，丹火与炙烤的主路径已于2026-11-08复验通过，完整链及视觉边界见异火正文。

目标设计（第 1 阶段已实现）：

- 数据：`SkillBook` 物品组件（公共字段加数值表与文本表，键沿用来源，四类共用）；`SkillState` 附件（技能栏 16 槽、身法、轻功、武器技、页、选择、连招开关，死亡复制，同步给本人）；`SkillCooldowns` 附件（六分组，来源仅非死亡克隆复制，同步给本人）。
- 规则：`SkillSelection`（页与选择）、`SkillCasting`（施放、冷却、连招）、`SkillStudy`（研习，灵根要求复用 xiulian 的 `RootRequirement`，gongfa 一并改用）、`SkillLibrary`（命令参数解析、藏宝与附赠表）、`SkillReleases`（技能名到施放过程的分派表）。
- 跨系统：战斗状态记分板抽成 combat 的 `FightingState`；状态效果的结束钩子 `EffectBehavior.ended` 补进内容表框架（到期、移除与刷新重应用；2026-11-07 改由原生属性钩子执行，见本文状态效果复核）；武器数值经 `WeaponStatEvent`。
- 数据表：`tools/porting/extract_skill_books.py` 提取 `content/skill/treasure_books.json`（13 本）与 `studied_bonuses.json`（2 条）。

阶段 2 的实现要点：

- `SkillProjectile` 继承 `AbstractArrow`，基础伤害 0.1（发射时可改）、无拾取；1.21.1 的箭没有击退与穿透的设置方法，覆盖 `doKnockback`、`getPierceLevel`；计时、攻击量、倍率、技能名、转角、方向在实体字段上（来源放在 `getPersistentData`）；停在方块里时消失；行为只在服务端调用，飞行和实体命中要求发射者，方块命中的发射者条件按具体过程保留（见下文弹体落地复核）。内容表用 `ContentTables.entity` 登记，工厂带 `ProjectileSkill`。
- 命中取值有两种：多数在第一刻记下发射者所选技能的攻击量与倍率；金芒术在命中时现读发射者当前所选技能（连招换技能后会变）。发射者不是玩家时攻击量取其 `LEVELnumber`。
- 敌友的判定方总是发射者（`Friendpanding*` 六个变体只是参数次序）；伤害经 combat 的 `SpellDamage`。
- Q40 记录拈花飞叶的取值键错误等；飞羽冰花沿侧向圆弧飞行（每刻转 20 度，计时每刻加两次），正前方的目标打不到。
- 自定义粒子（`SMALLEXPLOSION*`、`FLASH_ICE`、`SMOKELIGHTGRAY`、`SKILL_GOLD_0101`、`PEACHFLOWERLIZI`）已通过 particle 公开类型查询接回，保留来源的服务端发送参数与循环位置。

阶段 3 的实现要点（2026-11-04 起）：

- 35 个动画实体除 `SkillFire06`（火鸟）与 `Skillyin01`（起尸，两者继承 `TamableAnimal` 带 AI 目标，见下文“可驯服实体”）、`Xvkongliexi`（虚空裂隙，见下文）、`Shanghaifanwei`（伤害范围提示，见下文）、`Yvanyingchuqiao`（元婴出窍，见下文）外共用 `SkillEntity`（`PathfinderMob` + GeckoLib）。表现与属性由 `tools/porting/extract_entity_models.py` 从来源的 Model、Layer、Renderer 与实体类提取为 `content/skill/entity_models.json`（几何、动画、头部骨骼、发光层、阴影、默认与离地循环动画、五项属性、是否无 AI 及生命周期配置），并维护 `system_assets.csv` 的 skill 行；实体行为按实体 ID 登记在 `SkillEntities`（`EntitySkill`：刻、初始生成、被杀），内容表只登记台账已迁移的实体。
- 来源放在 `getPersistentData` 的施放信息（`AttackUUID`、`AttackDamage`、`Damage_multiplier`、`damagetype`、`jinengName`）改为字段，随存档；各技能私有的计数（`tick`、`tick2`、`kaishi` 等）仍用 `getPersistentData`，与来源一致。实体继承施放者的团队与友方集合，敌友的判定方是实体自己（`EtoE(entity, 其他)`）或施放者（施放过程内）。
- 过程动画 `setAnimation(名)` 经同步字段、逐刻消费与本地控制器播放一次后恢复默认动画，规则见[过程动画回核](#过程动画调用链与控制器复核2026-11-07)；纹理名同步给客户端（默认值是实体类里 `define(TEXTURE, …)` 的纹理，由提取工具写入 `entity_models.json`；金刚轮等少数默认透明的 `kong`，其余默认纹理均为可见贴图），发光层以 `RenderType.eyes` 重画同一几何。
- 伤害经 `SpellDamage.dealRounded`（来源把攻击量与倍率经 `DecimalFormat("0")` 取整）；来源拼出命令字符串的调用，经 `SpellDamage.command` 复现同样的解析（Q41）。
- 火系：`FireEntities`（大火球、火焰漩涡、火龙卷的实体刻过程，引火、控火术、火焰护盾的施放过程）。
- 土系：`RockEntities`（滚石术、天外陨石、化石手、巨石锤）。化石手与巨石锤按 `dangqianleixing`（类型一、类型二）和 `jindu`/`zhuangtai` 分段推进，潜行施放为类型二并播放对应动画；Q42 记录巨石锤的眩晕加给自己与化石手攻击方向随机。
- 金系：`GoldEntities`（金轮实体八种模式、金刚轮施放）与 `GoldWheel`（金轮弹体：捆妖轮、金弧轮、天堑金轮，第一刻生成跟随自己的金轮）。金轮的跟随对象记在实体的 `master`（施放者、弹体或被捆目标）。金刚轮的 Ctrl、Alt 式读玩家变量 `lctrl`、`lalt`，来源由 flow 的 Ctrl、Alt 键过程写入、多个系统读取：落在 `platform/input/ModifierKeys`（玩家附件，客户端每刻查看左 Ctrl、左 Alt 变化时通知服务端，不入存档），避免系统依赖 flow。无 AI 的生物（如冒烟检查里的目标）不会按速度移动，推开类效果只能核对速度。
- 冰系：`IceEntities`（冰天雪地、裂冰斩）。两种实体把攻击量与倍率截成整数记下（来源 `friendlist.putInt`）；裂冰斩的攻击量同时是后续冰刺的剩余根数，每根 3 刻后在推进方向生成下一根（`SkillEntities.chain`），初始生成过程（`finalizeSpawn`）设置“开始”状态与展开动画。
- 光系：`LightEntities`（闪光术、万丈光芒）；万丈光芒对生命低于最大生命 10% 的直接清零，立即发一组光爆，之后在 2、4、6、8 刻各补一组（合计五组），回调保留原实体并现读位置。
- 风系：`WindEntities`（聚风术、聚风化气、风飘术、造云术，以及由妖兽技能放出的 `skillwind_05` 到时消失和 `skillwind_06` 追踪风刃）。已按 `Yaoshou011baoqi`、`Yaoshou014heifeng` 实际生成代码提供 `Skills.showWindBurst/launchWindBlade`：风波在脚下随机朝向、继承敌友、播放 `animation.skillwind05.bodong04` 并设八刻；风刃在脚上方一格、随机朝向后面向目标脚下，记录施放者UUID、传入攻击量、倍率一、目标UUID与二百刻上限，两者均在加入世界前初始化。黑风的1.5倍法攻和第8/32/44刻三次施放由 creature 负责，技能接口不读取其他系统私有状态。构建/ArchUnit通过，实际妖兽生成者和自然路径运行仍随 creature；未新增专项。
- 雷系：`ThunderEntities`（掌心雷、地滚雷、惊雷阵、天雷术、五雷轰顶）。后两者调用 combat 的 `Lightning.command` 落白色雷。Q43 记录地滚雷无转向，并已更正玩家团队镜像的旧结论；目标以 `Relations.team` 读取真实团队。
- 水系：`WaterEntities`（水蛇鞭、水连鞭、激流波的鞭影实体与水龙吟）。鞭影实体按模式（潜行、Ctrl、Alt、正常）分阶段出手并每刻传送到施放者处；激流波施放时抛出目标、把视线 5 格处的非物品目标记为体质里的追踪目标（`mubiaouid`，小水弹读取），再延时放出 9 枚小水弹（`SkillDelays`，对应来源 `new Object(){…}.start(world, 刻数)`；任务只存内存，服务端停止即丢弃）。
- 水系后半：`WaterFormations`（空浮球、陨河术、水灵阵、玲珑阵）。`SkillEntity` 增加碰撞箱比例（`setHitboxScale`，来源重写 `getDimensions` 乘以实体里记下的比例，随存档保存）：空浮球放大 2 倍，陨河术正常式 3 倍、潜行式 1 倍。空浮球的潜行、Ctrl、Alt 作用于 16 格内自己放出的空浮球（引爆、转向施放者朝向、面向施放者）；空浮球与潜行式陨河术每隔一段时间伤害并把身边的非友方玩家或生物（无霸体）收为乘客，已有乘客时把其余目标推开；陨河术正常式落地或入水后爆发。水灵阵展开后每 7 刻向阵中放出一枚由施放者发射的水灵弹；玲珑阵每 20 刻为 3 格内的友方实体回复相当于攻击量的生命。
- 木系：`TreeEntities`（乱枝、灰木剑四式、乱木之种与爆裂、灰木树梯共用 `skill_tree_04`，以技能名区分模式；木篱术 `skill_tree_05`；毒蔓鞭、回身鞭、地蔓鞭 `skill_tree_06`）。乱木之种按施放者 16 格内枝条数 n 给木系冷却直接加 2 的 n−1 次方刻；毒蔓鞭的范围是水平 4 格、垂直 1 格，经 `ProjectileSupport.around` 的三向重载。来源的四处疑似疏漏（木篱永久不消失、回身鞭只抛起霸体目标、乱枝潜行无效果、灰木树梯计数加两次）保持来源行为，记为 Q44。
- 剑法：`SwordEntities`（四种剑影实体与出手）与 `WeaponArts`（触发与计数）。挥动剑类武器时，平台的 `ItemSwingEvent`（内容表工具行 `tool.swing`，见[工程结构](../../development/engineering.md)）交给 `WeaponArts`：武器技槽里要是类别为“剑”或“三尺青锋”的武器技，剑、木剑、阔剑要求书已由本人研习（来源比较 `belongToMasterUUID`），重剑与枪不要求（来源如此，四个触发过程只在这一条上不同）；出手冷却 10 刻，连击窗口 30 刻内式数加一、超过 `smanshipnumMax` 回到第 1 式，窗口结束归零，三个计数放在玩家的 `getPersistentData`（来源 `swordsmanshiptick`、`smanshipcolddown`、`swordsmanshipnum`，在 `WeaponArts.tick` 里随玩家刻推进）。出手在挥剑者处生成对应剑影（中平剑法高 1 格，其余 1.5 格），按式数播放动画 `animation.swordsmanshipNN.skillN`，再按剑法结算：中平剑法第 3 式挑起、第 5 式 3 格内伤害（攻击力 100%）、第 6 式跃起并伤害（150%）；柔云剑法第 1 式震开、第 2 与 4 式身边伤害（70%）、第 6 式挑起；拜年剑法第 1 式伤害（50%）；弱鸡剑法每式挑起。范围效果比较的是 `getPersistentData` 里的团队名（玩家的值与其团队一致），名字不同的实体都受影响，包括非生物；伤害是挥剑者攻击力属性乘以倍率的玩家攻击伤害，经 combat 的伤害管线。来源的“跃起”写在挥剑者所在一侧（客户端），这里改为服务端设置速度并通知客户端。非玩家没有武器技槽，来源里它们的挥动只改自己的计数而没有效果，不处理。剑影存在 13 刻后消失。
- 灵纹：`LingwenHit`（订阅 combat 的 `HitEvent`，主手武器带灵纹附魔时命中非友方目标）、`LingwenEntities`（灵纹特效实体 `lingwentexiao_2`）与 `LingwenBolt`（灵纹弹 `projectile_lingwentexiao`）。命中时：攻击者 2 格内已有灵纹特效，则属于自己且不足 6 枚的各加一枚，否则在攻击者处生成一个（数量 1）；灵纹特效跟随主人、按数量播放 `two`–`six` 动画，每 20 刻检查 10 格内最近的非友方实体，找到时沿主人的视线（不是朝目标）射出一枚散布 20 的灵纹弹并减一枚，每 100 刻自然减一枚，归零消失；灵纹弹第一刻取主人主手灵纹附魔等级 × 4 作攻击量，命中非友方生物造成无属性的固定伤害（取整，来源为主人），20 刻后消失。来源以起尸诀附魔 `qishi` 的等级作 `fumolevel`、从受击者身上扣灵力、计数写进好友列表物品的 NBT（Q45，保持来源行为，残留计数不复现）。灵纹弹的箭基类命中先于自身刻过程，首刻命中时攻击量可能尚未写入，因此来源会出现一次或两次 `HitEvent`，数量分别为 1 或 2（Q48）；`qishi` 的命中段（召唤起尸诀）随 `Skillyin01`。
- 默认纹理：来源每个实体类 `define(TEXTURE, …)` 的纹理各不相同（只有金刚轮、灵纹特效 2、伤害范围用透明的 `kong`）；提取工具现在把它写入 `entity_models.json` 的 `texture`，实体构造时设置，`SkillEntity` 不再统一用 `kong`。此前所有实体的默认纹理都是透明的，实机画面核对（阶段 5）要特别确认各实体可见。
- 可驯服实体：`TamedSkillEntity`（继承 `TamableAnimal` 的抽象基类，`SkillEntity` 是 `PathfinderMob`，二者不能共用类）。与 `SkillEntity` 共用的部分各有一个类：`SkillCast`（攻击者、攻击量、倍率、类别标签、技能名及其存档与结算，`SkillEntity` 的同名访问器也转给它）、`SkillAnimations`（`movement` 默认动画与 `procedure` 过程动画两个控制器及逐刻请求消费）、`AnimatedLook`（渲染所需纹理名，`AnimatedEntityRenderer` 按它泛化）。可驯服实体的行为（寻路、目标、刻过程）直接写在具体实体类里，不经 `EntitySkill` 表；来源 `isFood` 恒为假，不通过喂食繁殖或驯服，但刷怪蛋仍能调用 `getBreedOffspring`。后代创建后按来源以 `BREEDING` 执行 `finalizeSpawn`。右键刷怪蛋交给父类；其余交互在客户端对已驯服实体的主人返回成功、其他情况返回 `PASS`；服务端已驯服实体对主人调用父类、对非主人返回 `CONSUME`，未驯服实体的父类交互返回 `SUCCESS` 或 `CONSUME` 时设持久。具体实体类的构造里才能定下移动控制与导航：`createNavigation` 在父类构造期间调用，不能读子类字段。
- 火鸟术：`FireBird`（`skill_fire_06`，施放过程 `FireBird.release`）。施放时在施放者上方 1.5 格生成，朝向随机后转向施放者朝向，以施放者为主人（`tame`）、继承其团队，在加入世界前以 `MOB_SUMMONED` 执行 `finalizeSpawn`。无重力（首个实体刻的 `aiStep` 起设置）、飞行移动控制（转向上限 10、悬停）与飞行导航、免疫药水云、药水弹、仙人掌，无跌落伤害，受击与死亡声音是通用的；属性在模型表（移速 0.4、生命 10、攻击 3、跟踪 16）之外加飞行速度 0.4。目标选择：主人攻击的目标；行为选择：主人被攻击的目标（来源把 `OwnerHurtByTargetGoal` 加在行为选择器而不是目标选择器，Q46）、跟随主人（速度 3，离主人 10 格以上出发，停在 2 格内）、自定义追击（有目标且没有在移动时飞向目标眼睛，包围盒相交时冲撞，4 格内持续修正落点）。每刻过程（服务端）：寿命计数到 1200 刻消失；每 40 刻请求一次飞行动画（同步值逐刻消费后复位，允许后续同名请求；此前Q46的“只播一次”误判已撤销）；有目标时每刻沿自己的朝向以 0.2 的速度前冲（各分量都没有乘 cos(俯仰)，Q46）；1.5 格内出现非友方实体后记下“开始”，之后累计 3 刻爆发——对 2.5 格内的非友方实体造成火系伤害（攻击量与倍率取整）并点燃 5 秒，20 个火焰粒子，然后消失。寿命到期的同一刻，其后的探测与爆发仍按来源次序执行；被杀死时发粒子并立即消失。爆发时对每个敌方目标在半高处发一枚 `smallexplosionred`。
- 起尸诀：`Corpse`（`skillyin_01`，亡灵，实体类型标签 `minecraft:undead`）、`CorpseAi`（技能状态机）、`Corpses`（放出、成长与附魔命中）。
  - 状态与战斗数值沿用来源放在尸体 `getPersistentData` 的键（combat 的伤害结算读 `mana_Att`、`LEVELnumber` 等，`Corpse.empower` 按等级写入：攻击 `4 + 2 × 等级`、生命 `40 + 20 × 等级`，`attack_Def`、`mana_Def` 取攻击的 60%，`soul_Att` 取 20%，`soul_Def` 取攻击）。技能状态机不用 `SkillCast`（普攻与阴系技能的攻击者是尸体自己，攻击量取 `attack_Num` 与 `LEVELnumber`）。
  - 技能状态机每刻（服务端，存活时）：计数 `entitytick` 加一；空闲时目标 `gongjizhe` 取最近伤害自己的非友方 `linshigongjizhe`；目标存在且存活时，空闲则选下一个技能，否则推进当前技能；目标死亡清空目标并复位；目标不在维度里时每 40 刻随机走几步（“清除自身”期间不走）；存续计数 `cunxvtick` 到 600 且空闲时播放死亡动画并进入“清除自身”，再过 40 刻冒烟消失。
  - 选择技能：转向目标；骑乘其他实体时只有“挣脱”（等待 10 刻、权重 100）；未骑乘且没有沉默（`effect_002`）、僵直（`effect_033`）时，距离（中心距减去双方半宽）大于 3 格为“靠近”（5、30），否则为普攻（10、20）、喷射粘液（5、30）、死亡拥抱（10、30）、恐吓（10、30）；未骑乘、没有僵直且距离小于 3 格再加一个普攻（Q47）。按权重抽取，记下技能与等待刻数，到时开始；没有可选技能（僵直）时每刻重新判断。
  - 技能：靠近（1.5 倍速走向目标，40 刻结束）；挣脱（`Yaoshou001skillzhengtuo`，妖兽共用：准备 20 刻后脱离骑乘并朝视线向前跳起，30 刻结束，`CorpseAi.breakFree`）；普攻（1 刻后对视线 `0.5 + 宽度/2` 格处周围 1 格内非友方造成以尸体为来源的玩家式攻击伤害 `attack_Num`，4 刻结束）；死亡拥抱（前冲 14 刻、每刻 0.4 格，20 刻时对前方 3 格处周围半格内非友方缓慢 20 并造成阴系伤害，40 刻结束，Q47）；恐吓（动画，20 刻结束）；喷射粘液（自身霸体 56 刻，40 刻后对前方 3 格处周围 1.5 格内非友方施 `100 × LEVELnumber` 刻毒药 20 并造成阴系伤害，60 刻结束）。阴系伤害攻击量取 `LEVELnumber`、倍率 1，均取整。
  - 放出：技能“起尸诀”在施放者四角各放一只（等级 `ceil(品阶 × 0.5)`，自脚下一格起向上最多找 10 格的空位），记下施放时所选的技能栏序号；附魔 `qishi` 命中时随机数小于附魔等级则放出一只（等级为附魔等级 × 2，位置在攻击者周围随机试 4 次，没有技能栏序号）。两条路径均在属性写入后、加入世界前以 `MOB_SUMMONED` 执行 `finalizeSpawn`。来源的 `qishijveweizhi` 先写进玩家体质物品再读回，体质物品里的同名键没有别的读者，这里直接取技能栏序号。1.18.2 的 `PLANT`、`REPLACEABLE_PLANT` 材质在 1.21.1 不存在，植物位置改为可替换方块标签与灌木类方块。
  - 成长：尸体杀死实体时（`awardKillScore`），在尸体所在维度找到主人，取技能栏里记下序号的书；是“起尸诀”且品阶低于 40 时修为 `jinengExp` 加被杀实体的 `LEVELnumber`；修为达到 `品阶 × 30` 时扣除该值、书的 `LEVELnumber` 加一，品阶文字 `belongTopinjietext` 按新等级改写（存在书的 `texts` 里，显示该书的页面应优先于由品阶数值推出的文字），品阶数值不变（Q47）。
  - 动画：默认控制器只在无过程动画时运行；非激进状态的移动优先走路，再依次判断濒死、水中、疾跑、激进且移动，最后待机。死亡明确PLAY_ONCE。独立attacking控制器保留原版挥臂触发与十五刻标记，在停止时重载并播放一次攻击动画，不把它合并进技能过程动画。受击时空闲则播放受击动画，被打断标记 `beidaduan`（creature 的重击打断设置）清除过程动画。
  - 声音：环境、脚步取僵尸村民，受伤、死亡取僵尸马。持久，不会远离玩家后消失；受伤过程先执行，再拒绝 `WITHER` 类型或消息 ID 为 `witherSkull` 的伤害。

### 火鸟与尸体独立生命周期复核（2026-11-07）

完整重读 `SkillFire06Entity`、`Skillyin01Entity`，并沿 `Jinenglist01` 施放、每刻过程、受伤、被杀、击杀成长、三个独立技能、挣脱与 `EntityHurt2` 的 `qishi` 段核对。目标原生流程读取 NeoForge 21.1.265 的 `Animal`、`AgeableMob`、`TamableAnimal`、`SpawnEggItem`、`FollowOwnerGoal` 与 `DamageSource`。

火鸟与尸体补回施放时的初始生成，两个具体类按 `super.baseTick` → 自身过程 → `refreshDimensions` 的顺序刷新尺寸，客户端同样在基础刻后刷新。公用交互与后代生成按上文规则接回；撤销此前“没有食物因此交互等价、后代方法仅满足抽象方法”和“火鸟不走初始生成”的错误结论。

尸体选招中额外普攻仍只属于未骑乘分支；沉默时近身普攻与未沉默时双份普攻权重不变。死亡拥抱、恐吓和喷射粘液结束只复位技能名、阶段及 `entitytick`，不额外清 `entitytick2`；靠近、挣脱、普攻与通用结束过程仍清两项计时。击杀成长只在新 `LEVELnumber` 恰为 1–40 的整数时改写品阶文字，其余值保留原文字；经验来源、阈值与品阶数值不变（Q47）。

本批没有新增或修改 Test、选择器或断言。`build compileSmokeJava`（含 ArchUnit）通过，证据为 `tmp/tamed-skill-final-build.log`；静态源码证据为 `tmp/tamed-skill-source-audit.log`。本批未运行客户端：现有 `FireBirdCheck`、`CorpseCheck` 仅挂在整套 `skill` 下，没有独立选择器；按负责人要求不重跑整套旧神通，也不为此新建选择器。此前检查结果不作为本批修改的实机证据。刷怪蛋、非主人交互、骑乘选招、完整视觉、多人、1.18.2 版本实机与两版本对照均未执行；skill 仍未完成。

## 虚空裂隙（2026-11-07）

来源入口是 `A:JinengGuiButtonMessage` 的按钮 18 → `JinengxvkongProcedure`，不经过技能书或普通施放。境界 `LEVELnumber + tizhiitem.LevelNumberCorrect >= 31` 时关闭菜单，沿视线五格取方块整数坐标，清除该点三轴各八格内的旧裂隙，再以玩家水平朝向加 180 度生成；不足时只提示。来源实体 `XvkongliexiEntity` 无 AI、无重力，保留其飞行导航、伤害免疫列表和原有推挤行为。

目标使用独立实体 `VoidRift`，复用 `EntityModelRow`、`AnimatedLook`、`SkillAnimations` 与客户端渲染器；由 `Skills.openVoidRift` 提供按钮共用的服务端入口。生命周期见 `A:XvkongliexiDangShiTiGengXinKeShiProcedure`：每刻十个白灰粒子，32 刻可进入，332 刻移除并发出四十个云粒子。`xiaoshitick`、`keyijinru`、`shifouyichuanyve` 保留在实体持久数据中，随存档保存。

传送见 `A:XvkongliexiDangWanJiaYuGaiShiTiFaShengPengZhuangShiProcedure`：返回编号 1–9 依次为主世界、下界、末地、天阙、修罗、人间、妖魂、无归、地灵；读写 `weidu` 通过 xiulian 的 `Cultivation` 公开操作。进入使用裂隙的三轴坐标分别除以 50 后 `Math.round`，返回三轴乘以 50，保留朝向；例如裂隙 `(125, 101, -75)` 进入 `(3, 2, -1)`，再在该点返回为 `(150, 100, -50)`。目标 21.1.265 的 `ServerPlayer.teleportTo` 经 `changeDimension` 同步能力、状态效果与附件，传送后的事件 1032 按来源保留。

Q49：裂隙是整实体单次使用；未找到目标世界时也会消费，返回编号在查找目标之前清零；未识别的进入维度不传送但也消费。以上保持来源行为。按钮已在阶段5接入；正式 `xvkong` 数据已由 world 于2026-11-07接回，其余六个自定义返回维度仍随E阶段迁移，不能将本步登记为完整玩家路径已验收。

此前12项检查使用过平坦 `xvkong` 测试替身。2026-11-07删除覆盖后，沿用现有 `gradlew runSmokeClient -PsmokeOnly=voidrift`，正式空气噪声维度下12项全部通过并 `DONE`，四个世界正常保存退出，日志ERROR为0。进入由检查推进 `baseTick` 后直接调用 `playerTouch`，返回由玩家与裂隙重叠后的真实玩家碰撞触发；它证明正式维度下既有传送、返回、存档与计时检查通过，不代表完整自然菜单操作往返或裂隙视觉已验收。本次没有修改检查逻辑。证据在 `tmp/voidrift-formal-client.log` 与 `tmp/voidrift-formal-report.txt`；正式维度规则见[世界正文](../world/architecture.md#正式虚空维度2026-11-07)。

## 动画实体运动与死亡时序复核（2026-11-07）

重新读取35个来源动画实体的构造、导航、`aiStep`、属性与`tickDeath`，发现共用`SkillEntity`曾把30个实体统一为地面导航，遗漏固定无重力、飞行/游泳速度、水下呼吸与水流推动差异，并统一二十刻死亡移除。来源的九段水中移动控制在替换类名后完全相同，不以水系名称判断导航类型。

`extract_entity_models.py`现从实体类提取`lifecycle`，与原有模型表一起生成；skill的35行和共享同一Codec的xiulian四行均重新生成。`SkillEntity`消费自身配置：十九个飞行导航实体保留`FlyingMoveControl(10, true)`与来源的强制无重力；九个水中导航实体保留水路径代价零和`SkillWaterMoveControl`，水中每刻速度加`(0, 0.005, 0)`，朝目标转向，离水速度乘0.05；其余两个保留原生地面导航。飞行/游泳速度只在来源声明时加入属性。

水下呼吸按NeoForge21.1.265的`canDrownInFluidType`实现，仅对水类型关闭溺水，其余类型仍由原生规则判断；水流推动保留来源`isPushedByFluid`返回值。固定无重力在`aiStep`原生移动之后设置，并保留来源对外部`setNoGravity(false)`的覆盖；可变重力的空浮球、金轮等继续由自身技能过程设置。

死亡时序按来源先递增再比较：格挡5刻、水灵阵40刻，其余通常20刻；裂冰斩、万丈光芒与风波/风刃的零刻条件原样保留，不解释为立即移除。可驯服实体也读取来源时长，尸体由错误的20刻改回40刻，火鸟仍为20刻。这些是实体死亡阶段，与技能自身到期或命中后主动移除分别处理。

该批仅接回运动与死亡配置；复核发现的逐类`hurt`偏差已在下节修正，仍不能据此将35个实体全部表现标为对齐。来源摘要在`tmp/skill-entity-lifecycle-source-audit.log`；精确目标依据为21.1.265的`Mob`、`LivingEntity`、`MoveControl`、`FlyingMoveControl`、两种导航与流体扩展。没有新增Test或专项选择器，现有裂隙检查仅修正过时的说明注释。

最终`build compileSmokeJava`（含ArchUnit）通过。沿用未修改的`-PsmokeOnly=skillvisual`，延迟光爆与血雾的两项检查通过并DONE，四个世界正常保存退出、ERROR0；本次两张截图均捕获可见粒子样本。它验证当前共用实体能够加载并执行该光爆路径，不覆盖九种水中移动或各实体死亡边界，逐类动态验收、完整视觉、多人和1.18.2版本实机对照未执行。证据在`tmp/skill-entity-lifecycle-final-build.log`、`tmp/skill-entity-lifecycle-client.log`、`tmp/skill-entity-lifecycle-report.txt`与`tmp/skill-entity-lifecycle-{light,flight}.png`。

## 动画实体逐类伤害免疫（2026-11-07）

重新读取35个skill来源实体与四个共享模型表的xiulian实体的完整`hurt`、`causeFallDamage`、`checkFallDamage`。共用`SkillEntity`原先统一拒绝药水弹、药水云、跌落、仙人掌与溺水，既漏掉来源免疫，也给部分实体增加了免疫。现由既有提取器在`lifecycle.damage_immunities`保存实际条件，平台只声明数据，`SkillImmunity`在skill内判定；裂隙与伤害范围也读取自身来源配置，原有十四项条件不变。没有受伤过程的30个共用实体移除空`EntitySkill.hurt`回调，尸体和元婴的独立受伤流程保留。

| 来源实体ID | hurt拒绝条件 |
| --- | --- |
| `gedang`、`skill_rock_04/05`、`skill_tree_05` | 跌落、仙人掌、溺水 |
| `skill_fire_02` | 药水弹/云、跌落、仙人掌 |
| `skill_gold_0201`、`skill_ice_05` | 药水弹/云、跌落、仙人掌、溺水 |
| `skill_ice_04` | 上一行条件加闪电 |
| `skill_rock_02` | 跌落、仙人掌、溺水、铁砧 |
| `skill_rock_03` | 跌落、仙人掌、铁砧 |
| `skill_tree_04` | 跌落、溺水、三叉戟；没有仙人掌免疫 |
| `skill_water_04` | 药水弹/云、跌落、仙人掌、铁砧 |
| `skill_water_06` | 药水弹/云、跌落、仙人掌、溺水、铁砧 |
| `skill_fire_06`（独立火鸟） | 药水弹/云、仙人掌；自然摔落由独立钩子关闭，本批未改 |
| `skill_fire_07/08` | 完整十四项集合减溺水 |
| `skillwind_05/06` | 继承原生hurt，不追加免疫；自然摔落由独立钩子关闭 |
| `lingwentexiao_2`、`skill_light_03`、`skill_thunder_08/10`、`skill_tree_06`、`skill_water_01/02/07`、`skillwind_02`、`swordsmanship_01–04`、`xvkongliexi`、`shanghaifanwei` | 箭、玩家、药水弹、药水云、跌落、仙人掌、溺水、闪电、爆炸、三叉戟、铁砧、龙息、凋零、凋零头颅 |

箭、玩家、药水弹和云仍检查`getDirectEntity()`；不改用间接施放者。目标21.1.265的类型对应在`DamageSource`、`DamageTypes`、`DamageTypeTags`核对：铁砧为`FALLING_ANVIL`，爆炸为`IS_EXPLOSION`，三叉戟与凋零头颅保留原`getMsgId()`字面值。

来源的十九个共用实体同时覆写`causeFallDamage`返回false与空`checkFallDamage`，以必填`ignores_fall_damage`保存这对实际模板；其余十一个继续原生摔落流程，之后才由各自hurt条件判断伤害。不把自然摔落免疫等同于拒绝直接传入的FALL伤害，也不额外抑制原生落地事件。火鸟、尸体、元婴继续自身实现，xiulian四行仅同步必填数据格式，本批未改雷劫行为。

静态证据为`tmp/skill-entity-immunity-source-audit.log`。本批没有新增或修改Test、选择器、断言。最终`build compileSmokeJava`（含ArchUnit）通过；仅沿用未修改的`runSmokeClient -PsmokeOnly=damagearea`，六项全部通过并DONE，客户端保存四个世界后正常退出，日志ERROR0。该检查覆盖共享免疫入口、位置/阵营、计时与保存重读，加载时解析全部39行模型配置；没有据此验收各实体的动态受伤边界或完整视觉。证据为`tmp/skill-entity-immunity-build.log`、`tmp/skill-entity-immunity-client.log`及`tmp/skill-entity-immunity-report.txt`。逐类动态受伤边界、完整视觉、多人、1.18.2版本实机及两版本对照尚未执行。

该免疫批次随后定位到过程动画的完整调用链；当时尚未修改动画代码。消费、默认/过程切换、完成恢复与逐类过渡差异已在下一节接回。

## 过程动画调用链与控制器复核（2026-11-07）

完整读取35个来源实体的`movementPredicate`、`procedurePredicate`、控制器登记与全部对应`EntityAnimationFactory.onEntityTick`分支；尸体另有`attackingPredicate`。库行为以来源声明的GeckoLib3.0.57源码制品、目标依赖GeckoLib4.9.3源码制品核对，未使用邻近工程的其他GeckoLib版本。

来源同步字段的空请求值为`undefined`，本地过程空值为`empty`。每刻先取同步请求，复位同步字段，再写入本地过程名；目标由实体`tick`开头调用各实例的`SkillAnimations.tick`，随后推进原生实体刻与自身技能。请求不进入存档，也不由客户端反写玩法。相隔刻的同名请求能够再次到达；播放中的过程不因同名请求立即重启，新名字也等待当前控制器停止后处理。火鸟每40刻的请求保留，Q46曾遗漏工厂而得出的“只播一次”结论已撤销，Q46其余两项来源行为不变。

默认movement控制器只在本地过程为empty时运行；procedure控制器停止且存在请求名时显式以`Animation.LoopType.PLAY_ONCE`设置动画，完成后清本地名并`forceAnimationReset`，允许之后再播同名。GeckoLib4.9.3的`RawAnimation.thenPlay`采用JSON默认循环，不等同于来源的显式PLAY_ONCE，现不再用于过程动画。没有增加中断当前过程、请求队列、重试或动画兜底；来源恒false的loop及不可达lastloop分支没有变成新玩法。

过渡刻数从来源控制器登记提取到`lifecycle.animation_transition_ticks`：skill三十个实体为0刻，`skill_fire_06`、`skill_fire_07`、`skill_water_02`、`skillyin_01`和`yvanyingchuqiao`为4刻。不是按全部实体统一为0或4刻。水灵阵与惊雷阵的默认控制器在濒死时明确播放close一次，其余时循环idle；尸体的条件动画和独立原版挥臂控制器见上文可驯服实体，二者共享的aiStep按来源推进挥臂时间。金轮和掌心雷原有离地循环分支保留。

skill共35行与共享Codec的xiulian四行已重新生成；xiulian本批只同步模型数据格式，雷劫自身控制器未修改。虚空裂隙的来源过程没有动画请求调用者，保留其默认循环，无新增同步入口。静态证据`tmp/skill-animation-source-audit.log`包含35个实体的谓词与工厂分支；库源码位于`tmp/skill-animation-source/`、`tmp/skill-animation-target/`。

Q62：逐项读取35个来源动画资源中的实际名字，仅伤害范围默认请求的`animation.ycxzboss_01.idle`不在其绑定的动画文件中。该引用按来源保留，不猜测替换；生成者传入的具体范围动画独立处理。默认可见回退未做两版本实机核对。

本批没有新增或修改Test、选择器或断言。最终`build compileSmokeJava`（含ArchUnit）通过；沿用现有`runSmokeClient -PsmokeOnly=damagearea`，六项全部通过并DONE，四个世界正常保存退出，日志ERROR/FATAL均为0。该场景确认共享模型配置加载及伤害范围既有路径；完整过程切换、同名重播、尸体三个控制器的动态表现、多人、1.18.2版本实机和两版本对照仍未验收。证据为`tmp/skill-animation-build.log`、`tmp/skill-animation-client.log`和`tmp/skill-animation-report.txt`。

## 动画实体渲染逐类复核（2026-11-07）

已逐类读取35个1.18.2版本技能实体的Model、Layer、Renderer与尺寸覆写，并补核共享渲染器的四个雷劫实体。来源GeckoLib3.0.57、目标4.9.3的`GeoEntityRenderer`死亡旋转默认均为90度。技能中24个渲染器明确覆写0度，目标现逐类接回；其余11个（四种剑影、水龙、树梯、毒蔓鞭、天外陨石、化石手、金刚轮与灵纹特效）继续继承默认，不统一关闭侧翻。

模型表以`rendering`组合阴影半径、等比渲染缩放与可选死亡旋转覆写；根记录仍为16字段，来源没有覆写的旋转字段保持缺省，运行时委托库。35个技能模型渲染缩放均为1；共享的劫云为3、化灵雷龙为4，地滚雷和九霄雷柱为1，四个雷劫渲染器均关闭死亡侧翻。`AnimatedEntityRenderer.withScale`只在主渲染前应用，发光层沿用外层变换，不重复放大。渲染缩放不参与碰撞尺寸。

八个技能模型包含视线骨骼：中平/柔云/弱鸡剑影、毒蔓鞭、格挡与金刚轮使用`zong`，火鸟与尸体使用`head`。俯仰、头身偏航的符号与两个精确库版本一致，仍叠加到当前骨骼旋转；暂停且实体未允许暂停播放时不追加视线旋转。目标库冻结动画时间后仍可能调用自定义骨骼方法，故显式保留来源暂停条件。其余模型不增加头部动作。七个技能发光层的固定纹理和几何已逐项核对，沿用`eyes`及无受伤覆盖；主体均为来源的半透明渲染。来源库在`isInvisibleTo(当前玩家)`为真时跳过主体、仍独立处理发光层，默认颜色为白色；目标现保留同一可见性门槛与颜色，不引入4.9.3默认的旁观者透明度差异。

35个来源实体在`baseTick`过程之后刷新尺寸；其中34个固定比例1，空浮球从`friendlist.pengzhuangxiang`读取。已核对唯一实际写入者`SkillWater04shifang`：空浮球2、潜行陨河1、普通陨河3，生成后无其他比例写入者。目标共用实体现按来源在基础刻技能过程之后`refreshDimensions`，设置比例只记值，保留首刻过程执行前仍为初始尺寸的顺序。比例随实体保存，未补造来源没有的客户端尺寸同步。四个雷劫实体的碰撞尺寸另核为劫云3、雷龙2、其余1，原实现保留。

静态证据`tmp/skill-render-source-audit.log`收录全部类体、分组差异与尺寸方法，精确库源码在`tmp/skill-render-source/`及`tmp/skill-render-target/`。最终`build compileSmokeJava`（含ArchUnit）通过。沿用现有`runSmokeClient -PsmokeOnly=tribulation`，唯一进入世界的一次检查五项全部通过并DONE，四世界正常保存退出、ERROR/FATAL0；劫云截图已查看，模型可见。首次启动在FML4.0.45早期窗口字体初始化处阻塞，尚未进入游戏；核对该版本源码后仅临时关闭开发目录的earlyWindowControl，退出后已按原字节还原并核对哈希。没有新增或修改测试、选择器或断言。证据为`tmp/skill-render-final-build.log`、`tmp/skill-render-client.log`、`tmp/skill-render-report.txt`、`tmp/skill-render-cloud.png`和`tmp/skill-render-startup-failure.log`。该场景不覆盖死亡侧翻、隐身/旁观者、暂停/恢复、空浮球尺寸边界或全部模型，两版本视觉对照仍未验收。

## 伤害范围（2026-11-07）

来源实体 `Shanghaifanwei` 是由 creature 的九处妖兽技能生成的动画范围提示，不是独立的伤害结算入口。`ShanghaifanweiDangShiTiGengXinKeShiProcedure` 每刻读取持久数据中的小写键 `jinengname`：普通提示把 `daojishi` 减一并在不大于零时移除；`惊蛰闻雷` 计满 40 刻移除；`控雷阵` 计满 20 刻以 `AttackDamage`、`AttackUUID`、`team` 拼出 `lightninglanding` 命令后移除。范围内的伤害、击退、效果和粒子由生成者过程负责。

目标 `DamageArea` 复用 `EntityModelRow`、`AnimatedLook`、`SkillAnimations` 与统一渲染器，保留无 AI、无重力、飞行导航、流体不推动、保存倒计时与来源伤害免疫。`Skills.showDamageArea` 是 creature 可调用的公开入口：普通提示复制施放者阵营，控雷阵与惊蛰闻雷只设置团队和自身计时；控雷阵的命令调用保留在实体刻过程中，实际落雷已由 combat 的共享命令接回，生成者仍随 creature。

当前只完成实体与公开入口，尚未把九处 creature 生成者改接到该入口，因此台账的 `Shanghaifanwei` 保持“已注册”。`DamageAreaCheck` 的 6 项针对性检查核对了实体位置、阵营、免疫、普通倒计时、惊蛰闻雷边界和保存重读；生成者伤害/效果及控雷阵落雷未在本步重复伪造验证。

## 虚空裂隙与伤害范围独立生命周期复核（2026-11-07）

完整核对`XvkongliexiEntity`、`ShanghaifanweiEntity`、裂隙起手/更新/碰撞、范围更新，以及九份creature过程中的十二个范围生成块。两个来源构造器均未设置无重力，`aiStep`依次执行原生过程、挥臂推进、关闭重力；目标删除构造时提前设置，恢复该顺序。基础刻依次执行原生过程、自身过程和尺寸刷新，两端均刷新。无AI且没有控制者时，两版本均跳过原生travel；这里的时序修正不表示生成后会下落。

伤害范围补回水中不溺水与仅检查实体阻挡的生成规则，保留流体不推动；公共生成入口在数据、动画设置之后、加入世界之前调用一次`MOB_SUMMONED`初始化。裂隙已有的生成初始化保持一次。两个来源的受伤/死亡声音查找空ID，Forge40.1.0声音注册表无默认项，结果为null；目标原生默认却为通用声音，现显式恢复无声。粒子、倒计时、单次传送、返回编号与动画入口沿用已核实规则，未新增纹理存档或动画请求。

静态证据为`tmp/void-area-parity-source-audit.log`，目标依据为NeoForge21.1.265的原生实体、流体扩展及传送源码；无AI移动另核对1.18.2原生Mob/Entity及Forge40.1.0补丁。没有新增Test类、选择器或检查项目。`build compileSmokeJava`（含ArchUnit）通过，仅一次内置`runSmokeClient -PsmokeOnly=voidrift,damagearea`：裂隙12项通过，范围5项通过、1项失败，报告DONE，四世界正常保存退出，ERROR/FATAL0。失败是本轮把范围位置断言改成“首刻下落”时漏看NoAI门槛；已恢复位置不变并检查首个aiStep后的无重力状态，该次修正后仅编译，不能把该次报告改写为六项全过。后续逐类免疫与过程动画批次已沿用现有damagearea六项全部通过，见[免疫正文](#动画实体逐类伤害免疫2026-11-07)与[动画正文](#过程动画调用链与控制器复核2026-11-07)；旧5/6不再列作当前失败。

裂隙首项旧断言原要求生成即无重力，已按来源改为初始仍有重力，该项实机通过。其余进入仍由检查直接调用碰撞方法，返回由真实玩家碰撞触发；完整自然操作、范围的creature实际生成者、水中/声音边界、完整视觉、多人和两版本实机对照尚未验收。日志为`tmp/void-area-parity-{build.log,client.log,report.txt,check-compile.log}`；FML早期窗口仅临时关闭，退出后已按原字节还原且SHA256一致。skill继续进行中，不合main。

## 元婴出窍（2026-11-07）

来源入口是 `A:JinengchuqiaoProcedure`，由技能界面的出窍按钮调用，不经过技能书施放。雷劫开启、境界（`LEVELnumber + LevelNumberCorrect >= 31`）、法宝 009 维度和旁观模式按来源顺序限制；首次出窍扣除 `LEVELnumber × 2` 神识，创造模式只豁免余额门槛、同样扣费。保存本体坐标与原游戏模式，设置 `tizhiitem` 的 `yvanyingchuqiao` 标记并把玩家切换为旁观模式。公开入口为 `Skills.toggleYuanying`，菜单第21号按钮已接入；原模式内部使用英文值，恢复只处理来源实际保存的创造、冒险与生存三种模式，不为其他值补默认模式。

目标 `Yuanying` 复用技能实体的模型表、GeckoLib 动画和统一渲染器，记录本体 UUID，复制本体阵营与友方集合，继承生命上限及三项防御值。生成时同步当前及旧头部/身体朝向，先以玩家精确位置点AABB扩16格并按距离排序，把生物对本体的当前目标，以及无当前目标时持久数据中的 `gongjizhe`/`linshigongjizhe`，迁移到元婴，随后 `finalizeSpawn` 并加入世界。元婴受伤时只在当前世界玩家中找主人，先把主人生命同步为受击前的元婴生命；死亡同样只处理当前世界主人，先清除出窍标记、恢复原游戏模式，再提示并杀死主人。

完整重读 `A:PlayerTickProcedure:64–86` 后修正此前“每刻耗神”的误判：耗神位于世界时间达到 `jishiqi + 20` 的分支，现监听既有 `CultivationSecondEvent`，在吸收和恢复前计算 `境界 + 本体坐标距离 + 非空方块时的境界 × 3`。先经combat的 `FightingState.mark` 写战斗状态；非创造模式余额足够时设置40刻传送冷却并扣费，扣后不足同一次算出的需求时，或扣前已经不足时，经 `FightingState.clear` 同时清两个战斗分数并归窍。手动归窍不清战斗状态。归窍先传送到保存坐标，再以精确位置点AABB扩2格、按距离排序移除第一个属于该UUID的元婴，恢复模式。`chuqiaoX/Y/Z`、`chuqiaoqianzhuangtai` 和出窍标记均由xiulian附件保存。

独立实体按完整 `YvanyingchuqiaoEntity` 恢复原生地面导航与移动控制，去掉目标误加的飞行速度属性和两项摔落免疫钩子；保留原生重力和20刻死亡移除。基础刻之后刷新尺寸，`aiStep` 后推进挥臂时间，受伤/死亡声音分别为 `entity.generic.hurt/death`。来源 `PlayerTick:59–63` 的持续紫烟也已接回：出窍期间每刻在玩家脚下发送两枚 `smokedeeppurple`，扩散各0.2、速度0，与每二十刻耗神分别挂载。精确目标源码核对为NeoForge21.1.265的 `Mob`、`LivingEntity`、`Entity.moveTo` 和原生菜单按钮处理器；静态证据见 `tmp/yuanying-parity-source-audit.log`。

针对性检查入口为 `gradlew runSmokeClient -PsmokeOnly=yuanying`，2026-11-07 的 `YuanyingCheck` 五项检查覆盖境界门槛、初次扣神识与实体生成、持续消耗、受伤同步、归窍和神识不足。来源的 `starblue` 20 枚、`starskyblue` 10 枚出窍粒子已在生成元婴前于玩家头顶发出；元婴死亡时在脚下发 `brokendarkblue` 20 枚，三者速度均为 0.5。技能菜单按钮已接入；视觉认可、多人交互和 1.18.2 版本实机对照未执行。

2026-11-07 完整调用链修正后，最终 `build compileSmokeJava`（含ArchUnit）通过，沿用现有 `runSmokeClient -PsmokeOnly=yuanying,skillui`，出窍5项、技能界面9项全部通过并DONE；四世界正常保存退出、ERROR/FATAL0。该批没有新增Test类、选择器或检查项目；既有出窍检查把等待对齐到20刻周期并固定、还原计时基准，补足进入场景的神识，原归窍断言改走旁观状态真实屏幕按钮及网络。技能界面检查未修改，覆盖新按钮消息的翻页、选择、轮盘、丹火、扫描及自爆。临时关闭FML早期窗口，退出后配置按原字节还原且哈希一致。日志在 `tmp/yuanying-parity-{final-build.log,client.log,report.txt}`；自爆死亡至重生期间仍有32条未知实体附件同步警告，此前同一界面场景有31条，该批未处理此已有问题。创造起手、跨维度主人隔离、元婴死亡、自然摔落与完整粒子/声音没有该批实机证据，未执行多人或两版本对照。

### 完整生命周期复核（2026-11-08）

玩家出窍期间死亡的来源入口是 `A:EntityDeathProcedure` 的真实 `LivingDeathEvent` 订阅。它在 `49–51` 先要求死亡者和 `event.getSource().getEntity()` 均非空，才在 `300–303` 对出窍玩家调用 `ChuqiaosiwangProcedure`；该过程先归窍，再额外减365寿元并发送深红普通聊天提示。目标已由 `SkillSetup` 按同样条件分派 `Yuanying.playerDied`，寿元经xiulian公开操作修改。无来源实体的死亡不进入这一专属分支；`player.kill()` 在1.18.2版本使用无来源实体的 `OUT_OF_WORLD`，不能作为额外扣寿元的正例。元婴本体的死亡过程则在杀主人前已经清除出窍标记，保持不额外扣一年。普通 `EntityberebornProcedure` 的重生寿元修正与 `gamemoshi` 恢复是另一条规则，不能代替此出窍死亡入口。

来源出窍只保存 `chuqiaoX/Y/Z`，不保存起点维度；归窍使用当前世界的连接传送，在该世界保存坐标附近移除首个匹配UUID的元婴。玩家跨维度后仍在新维度的同一组坐标归窍，原世界元婴可能保留。受伤和死亡过程只遍历元婴所在 `ServerLevel.players()`；来源和目标原生实现都会在玩家离开世界时把其移出该集合，因此另一维度或离线的主人不会被该元婴直接同步生命或杀死。目标继续保留这些范围，没有添加全服寻主、跨维度寻回或离线元婴清理。

退出与重登按来源保存状态。`A:PlayerLeaveGameProcedure` 的真实退出事件只记录移动速度、清雷劫与劫云，不归窍、不清出窍标记；`PlayerJoinGameProcedure` 仅在体质载体为空时初始化，不重置已有标签。Forge40.1.0 的 `PlayerList.remove` 先发退出事件、再保存玩家、后移除，NeoForge21.1.265顺序相同。来源Capability序列化整个 `tizhiitem`，玩家原生数据保存当前游戏模式；目标xiulian附件保存体质标签并按 `copyOnDeath` 复制，玩家原生模式保存链继续承载旁观状态。重登读回后沿用既有每二十刻处理，不额外校验或重建本体关联。

元婴自身沿用可保存的 `PathfinderMob` 与持久化标记，禁止距离消失。来源没有自定义保存覆盖，但Forge40.1.0的 `Entity.saveWithoutId/load` 自动保存、恢复 `ForgeData`，包括 `friendlist.playeruuid`；目标 `Yuanying.addAdditionalSaveData/readAdditionalSaveData` 显式保存、恢复 `owner` UUID，阵营与友方另由relation附件序列化。两个版本的 `Entity.restoreFrom` 都以NBT保存、加载迁移实体数据。来源 `Mob.shouldDespawnInPeaceful` 默认false，实体未增加对应覆盖；没有因业务类缺少专用保存或登出钩子而补造额外生命周期。

上述静态核对同时读取锁定1.18.2工程内的精确Forge40.1.0源码包和目标NeoForge21.1.265源码包；Forge包位于来源 `build`，不在既有 `src/elements/Gradle` 静态清单指纹范围内。完整定位与证据范围见 `tmp/skill-yuanying-lifecycle-audit-20261108.md`。本批未新增生命周期检查；既有 `runSmokeClient -PsmokeOnly=yuanying` 五项全部通过。该入口只覆盖境界门槛、出窍、持续消耗、受伤同步、按钮归窍和神识不足，不覆盖有来源/无来源玩家死亡、元婴死亡、跨维度隔离、退出重登和实体重载；这些路径仍只有静态来源与原生保存链证据。元婴台账仍为“已注册”，完整视觉、多人和两版本实机对照继续待验收。

## 技能箭与箭种选择（2026-11-07）

来源为 `SkillArrow01–10Entity`、对应飞行过程、共用的实体命中过程（551 行）与方块命中过程，以及技能界面按钮 6–16。真实发射者是 equipment 的 `Weaponspeedbow01DangGongJuChuXianZaiShouZhongShi`；十个箭物品保留右键使用和 72000 刻使用时长；第 01–09 箭使用矛式动画，第 10 箭为 `UseAnim.NONE`，但其 `releaseUsing` 条件过程恒为假，不发射也不损耗。`SkillArrow00–10skill` 与 `ArrowSkillchufa` 的孤立入口及 `skill_arrowxinruzhishui` 已完成静态核对，见“剩余元素与入口核对”，不新接正常施放。

`Skills.createArrow` 返回尚未加入世界的箭；equipment 负责基础箭伤、位置、速度、蓄力、资源消耗和修饰键输入。创建时写入来源的 `arrowskill`、`manxvli` 标记，第一次飞行刻在箭基类命中之后消费并清空，模式只选一次。潜行优先于 Ctrl，Ctrl 优先于 Alt；满蓄力限制仅用于相应增伤分支。箭雨通过普通实体工厂生成新箭，保留基础箭伤、不继承模式。箭的模式、计时、无限与掉落标记使用实体持久数据；穿透使用原生PierceLevel保存并读回共用弹体字段，击退按1.18.2版本原生行为不保存。

| 箭 | 飞行模式与主要命中行为 |
| --- | --- |
| 01 | Ctrl 箭雨；Alt 满蓄力令基础箭伤乘 1.5；仅有普通箭伤 |
| 02 | 不初始化或消费发射者的箭技能标记；无重力、20 刻消失；命中追加无属性灵力伤害，不掉落 |
| 03 | 响箭吸引友方、铃箭吸引敌方、音爆箭在半边长 8 的盒内伤害；默认打草惊蛇使敌方生物以射手为目标 |
| 04 | 旋风箭每刻拉扯；风压箭命中范围推开并造成 0.3 倍风伤；飓风箭每刻范围风伤且基础箭伤乘 1.5 |
| 05 | 爆燃范围叠加阳火炙烤，爆炎范围 0.5 倍火伤；普通命中火伤并叠加 400 刻炙烤，等级封顶 2 |
| 06 | 基础箭伤大于目标实际物防时追加两者差额的虚空伤害；冲击波击退、内爆叠加效果、震爆范围推开并令射手后退 |
| 07 | 命中着地目标缓慢；坎坷路范围缓慢、飞沙走石范围 0.4 倍土伤；震灵在已有穿透上加 6 并无重力 |
| 08 | 箭雨或满蓄力增伤；命中施毒及暗伤，写入目标的来源倍率键；命中方块尝试来源的药水云命令，但其 SNBT 无效（Q50） |
| 09 | 暗流或漩涡；下雨或入水无重力，累计湿润飞行 100 刻消失；仅入水命中才有 1.5 倍水伤与控制 |
| 10 | 继承 01 模式；敌方命中发光 12000 刻；落地回收原版光灵箭 |

附加灵力伤害的攻击量固定为 1，增伤模式倍率 1.5，其他默认 1，不读取技能书攻击量。敌友与霸体限制逐分支保留；第 06 箭的冲击波没有霸体检查。方块命中后，仅首刻取得掉落标记且无无限标记的箭回收；第 02 箭不回收。

内爆 `skill_arrow_0601` 使用原生 `MobEffect.removeAttributeModifiers` 钩子：刷新时现读更新后的等级，到期和单独移除时效果已从集合移除，按零级计算；清除全部效果的原生顺序不同，仍现读当前等级。伤害为 `(现读等级 + 1) × 2`，无攻击者。`InnerExplosion` 用属性表到实体的弱引用补足 1.21.1 原生钩子缺少的实体参数，`ContentTables.effectFactory` 只给这一效果换用原生类，其余效果仍使用原有行为表。

`SkillState.arrow` 与 `arrowChoice` 保存箭种并同步给本人，其他技能写操作保留这两项；新增字段允许读取已有的目标版本附件。序号 1 是第 01 箭、2 是普通箭、3 是第 02 箭、4–10 是第 03–09 箭、11 是原版光灵箭。第 3 项要求灵力不少于境界编号乘 5，其他项要求库存持有对应物品；选择不扣资源。按钮 payload 只传动作，服务端重新检查条件后写入并关闭菜单。

十种模型保留来源的几何、UV、贴图、旋转与 1/16 透明度，不用物品图标替代。自定义粒子 `jibo`、`lingqi`、`xvlilizi`、`yinbo` 和各色小爆炸已按来源接回；已接入来源使用的原版粒子与爆炸声音。`effect_023` 的炙烤持续代码及阴阳火联动已由 yihuo 接回，2026-11-08自然效果刻主路径复验通过；火箭未写攻击者来源，仍不能作为完整炙烤链验收；炉鼎供热/耗尽消费者随D阶段，见[异火正文](../yihuo/architecture.md#本批验证2026-11-07)。轮盘已在阶段 5 接入并完成一次真实按钮选择检查；弓的条件发射与消耗已由 equipment 接回，其专项一次验证了人工标记下的分支，Q57 自然右键断链仍保留；规则及证据见[装备](../equipment/architecture.md)。模型视觉认可、多人和 1.18.2 版本实机对照未执行。

`SkillArrowCheck` 的 12 项检查覆盖选择资源条件、不扣资源、附件编解码与本人同步、灵气箭到期、实际火箭命中、箭雨 owner 与非递归、光灵箭掉落、内爆命中与刷新/移除、穿透追加及实体存档重读。实测发现并修复原生箭内部物品为空时无法保存的问题：内部默认物品使用原版箭，实际拾取仍为空。第 08 箭无效云命令在最后一次冒烟后仅作静态核对与编译。

## 剩余元素与入口核对（2026-11-07）

该批当时的401个归属元素已全部读完，归属表与类归属目录已同步；阅读覆盖不代表行为或实机验收完成。本次核清最后26项，2026-11-08新增04归属后的当前416项状态见本批装配节：

- `SkillTree02` 的有效调用者是 `A:Jinenglist01Procedure:221`，木系技能名“满天花雨”。`ElementProjectiles` 补登记施放，复用既有飞叶弹体：以 `yaw + 90 − 30` 为首角，共五枚、每次加 15 度；起点为施放者加 `(cos角, 1.5, sin角)`，方向为 `(3cos角, 5sin(−pitch), 3sin角)`，角均转弧度后计算。每枚速度 1、无散布、基础箭伤 2、击退 1、静音；伤害与飞行仍走已迁移的飞叶行为。
- `SkillArrow00–10skill` 按同模板及差异逐项阅读，包含箭雨扇形、飞炮箭、百步穿杨与默认单发。两个来源的 Java、事件、编辑器触发、命令和资源索引没有有效调用入口；不新接到正常施放。`ArrowSkillchufa` 设置加速射击/箭无虚发计数或施加心如止水，但仅有 `AnjianZAnXiaAnJianShi.mod.json` 中 `connected_to_trigger=False` 的游离 Blockly 引用，生成 Java 不调用。`skill_arrowxinruzhishui` 为有益标记效果，颜色 −13210，无自身过程和行为消费者；保留注册与图标，台账只标“已注册”（Q53）。
- 十个技能箭配方与 B1 转换结果逐项核对：第 02 配方实际产出四支原版普通箭；第 07/08 配方的 stone/duyao 标签已统一为项目命名空间；第 10 配方按已裁定 Q15 产出一个。没有重新转换或改写配方。
- `Skillarrow08fudu` 的真实合成事件只在第 08 箭产出时读取合成格 `getItem(1)`，截去物品文本前两字符并加项目命名空间，与 `cailiao_141` 比较，成立时显示 `111`，不写任何毒性数据。`SkillSetup` 接入同一服务端事件，并显式构造 1.18.2 的数量加物品路径文本；1.21.1 `Item.toString` 返回完整命名空间，直接使用会改变来源结果。两位数量时截后残留空格而不匹配的行为同样保留（Q53）。旧格式以缓存 Forge 40.3.0 静态对照，未做锁定 Forge 40.1.0 实机验证。
- `Jinengyaohua` 已通过第19号按钮和 `DemonTransformationEvent` 接回：体质、头盔标签、冷却、声音、背包搬移、四部位数值与消散均由 equipment 执行。来源不自动穿回旧甲，具体行为和 Q58 见[装备正文](../equipment/architecture.md#妖化2026-11-07)。

资源审计发现既有转换器没有搬入状态效果图标。`convert_assets.py` 现按所有已登记效果的来源注册 ID 复制 `textures/mob_effect/<ID>.png`，目标文件名使用台账目标 ID，并写入 manifest。目标 NeoForge 21.1.265 的 `MobEffectTextureManager.get` 按效果注册键取 sprite，原版 `atlases/mob_effects.json` 读取 `mob_effect` 目录；不使用编辑器中未被注册 ID 引用的同名近似图片。图标技术齐全不等于视觉认可。

本次针对性检查为 `-PsmokeOnly=skillremainder`，只验证新补的五枚飞叶入口和扇形；原有神通不重复运行。具体执行结果见项目月度记录。

## 身法与轻功（2026-11-07）

来源入口为 `AnjianW/S/A/D/Space`、`Fanjian` 消息与 `ShenfaW/S/A/D/space`、`FanjianAnXiaAnJianShi`；`PlayerTick` 的末刻先推进 `ShenfaTick`，再执行 `FlyskillTick`。目标 `SkillMovement`、`SkillFlight` 同样只在服务端的玩家末刻推进；`SkillClientSetup.ActionKey` 覆写 `setDown`，每次按下沿只发送动作，保留独立的可改键绑定和原版移动/跳跃，不把长按的重复事件算作双击。服务端写入速度并通过原生速度同步通知本人。

身法要求书的研习者为本人。W/S/A/D 的十刻窗口与跳跃窗口沿用实体持久键，触发后不清空；共享 `shenfatick` 不超过书的上限时可追加消耗，所以本次触发后能超出上限。方向位移向上 0.4 格，水平距离取对应书参数，W 的角度系数保留 `3.14 / 180`，其余为 `Math.PI / 180`；方向位移追加 0.1 饥饿消耗。W 的速度效果独立于位移与冷却，持续 160 刻。二段跳要求独立上限，速度为 `(0, 0.6, 0)`，冷却同样加到共享值；每刻冷却减少 `1 + 四件护甲 flyingskill 等级之和 × 0.03`，不低于零。骑乘时 W 切换坐骑的 `flying` 持久键，实际飞行由坐骑承担。

F 允许创造模式、本人研习的轻功书或境界含修正达到 31 的玩家切换飞行，非创造模式在法宝 009 维度禁用。开始时播放 1.2 音量的 `flybegin`、设置竖直速度 0.5 并开启原生 `mayfly/flying`；结束时非创造模式关闭能力，开关两端均清空掠空、档位与喷射。`flybegin` 独立保存在 `skill_flying` 附件，按来源全部克隆事件复制并只同步本人。`Skills.flying/setFlying/toggleFlight` 供外部操作；附件持久化不额外恢复来源未恢复的原生能力。

飞行中双击 W 在未开雷劫时开启掠空，播放 1.3 音量的 `flyspeeedbegin`；四甲 `jet` 等级之和不为零时获得一次 `1 + 等级之和 × 0.2` 的喷射。再次 W 加一档，最多 4；S 减一档，归零后再双击 S 结束掠空并重开飞行能力。基础掠空速度取本人轻功书，非本人书创造模式为 2、其余为 1；档位追加四分之一，喷射只加一次并清零。来源先清零再读取喷射扣费，因此没有额外喷射扣费（Q51）。

非创造飞行每刻要求灵力严格大于基础耗费：本人轻功书取 `flyskillxiaohao`，其余为 12；`skillwind_04` 存在时减其等级加一并至少为 1。满足时清摔落距离，每二十刻扣费：乘以正数范围内的 `1 - flying 总等级 × 0.05`，掠空翻倍，再加 `档位² × 2`。等级达到 20 时系数不再为正，来源改回全额消耗，保持此分支。护甲直接查询，四个饰品由 equipment 监听 `FlightEnchantmentEvent` 提交已保存四槽的等级，菜单关闭前替换的新物品不提前计入。灵力不足时关闭轻功与掠空，但保留来源未清空的档位。

`effect_035` 的每刻过程按来源互斥顺序处理：非创造玩家鞘翅飞行时设 `(0,-1,0)`；否则轻功飞行时关能力并清空掠空，再否则取消无重力；其他生物只取消无重力。`FlyskillTick` 的 `jinkong` 标记则另在末尾关闭轻功、清空 `takong` 并复位标记，不混同两种结束路径。云雾、两行云雾、黑雾、大黑雾、火花的原版粒子与本人可见的起飞/二段跳圆环已接入，保留原式直接将角度数值作为弧度；黄色/蓝色/红色祥云、血光、血雾与疾风自定义粒子已按来源接回。拔空术的中间速度 2 立即被开关过程的 0.5 覆盖，最终保留 0.5。

G 键技能界面、轮盘、HUD 与 C 键已在阶段 5 接入，见下节；来源没有任何伤害过程消费格挡计时，因此不将其接入 combat 结算。未执行多人、死亡/重登飞行能力与 1.18.2 版本实机对照。

## 技能界面、格挡与功能入口（2026-11-07，阶段 5 部分实现）

`SkillMenus` 注册来源 `jineng_gui`，`SkillMenu` 没有物品槽；G 键只请求服务端打开本人菜单。`SkillScreen` 使用原生菜单屏幕、来源500×500布局和贴图，按钮由 `SkillButtonPayload(容器ID, 按钮)` 发送。目标21.1.265的原生 `handleContainerButtonClick` 会拒绝旁观者，出窍后无法经该路径归窍；来源 `JinengGuiButtonMessage` 允许旁观者，故按来源使用自定义消息，并核对发送者当前确为SkillMenu、容器ID匹配、`stillValid`。`SkillMenu.clickMenuButton` 验证0–23范围并分派：0–3四技能，4/5翻页，6–16十一箭种，17连招，18虚空，19妖化，20自爆，21出窍，22扫描，23丹火。妖化请求由equipment监听并执行装备生命周期。名称与五行底图只显示本人研习的书；底图取最先达到一秒冷却的五行分组，阴阳不着色。来源数值6的背景没有写入者（Q52）。

`SkillGuard` 保留来源 `Canjian` 的按下/松开：冷却未大于零时，主手品阶除二加 20；副手在 `dunpai` 标签中时用两手品阶之和除二加 21，冷却设为 20 刻。松开把显示计时清零，每刻分别递减计时和冷却。真值仍在玩家实体的 `canjianjishi`、`canjianjishicolddown` 持久数据，不改变来源克隆策略；`skill_guard` 附件只把显示值同步给本人。品阶经已有 `WeaponStatEvent` 查询 `lianqizong.item_level_num`，equipment 已接入[成品生产与品阶监听](../equipment/architecture.md#宝箱成品与品阶2026-11-07)。`Gedang` 实体保留模型、无重力与八刻移除，但来源没有生成者，C 键也不生成它；格挡计时没有任何伤害消费者，不增设减伤。

`SkillMenuActions` 的所有条件、扣费和物品产出在服务端执行。丹火要求含修正境界至少 11，消耗未修正境界编号的两倍灵力；给出的 `danhuo` 物品在 `DataComponents.CUSTOM_DATA` 中保存 `power` 和 `ManaAttackUUID`，供后续 yihuo 的放置、火力合并及火焰方块流程读取。放置与补燃料已通过 `DanFireItem` 调用 yihuo 的公开入口接回，保留物品使用后的原生放置回放顺序；运行证据与缺口见[异火正文](../yihuo/architecture.md#本批验证2026-11-07)；2026-11-08真实客户端放置、补料和库存同步通过，物品台账改为“行为已迁移”，完整视觉仍待验收。扫描同样要求含修正境界至少 11，消耗未修正境界编号两倍的神识并关闭菜单；来源的定时扫描在服务端调用空的 `Level.addParticle`，无可见粒子或其他扫描效果（Q52），不补造新功能。

自爆要求含修正境界至少 21，灵力不少于最终上限 10%，先扣灵力、声音、缓慢 80 刻 20 级，再安排 30/35/40/45 刻的效果；随后当下关闭菜单、累加 `zibaonumber`、首次数额 3650 天且以后按前次三倍写 `zibaokouchu`，扣寿元并杀死玩家。延迟回调保留原玩家对象与原世界，执行时现读坐标与等级；30 刻先闪光和强度 16 的 TNT 类爆炸，再执行火伤，后三次重复火伤。基础量 `(LEVELnumber−20)×10+10` 经来源取整，倍率 2，敌友经 relation、伤害经 combat。原版粒子和直接把角度作弧度的圆环保留，起始半高发一枚灰色旋风，四次火伤前各发黄、金、橙烟各 20 枚；六色爆发另从 30 到 48 刻每两刻一组，共十组，大爆三色各为现读境界编号除二取整，小爆三色各为境界编号乘二取整，三轴偏移 16、速度 0。死亡后仍执行延迟回调是来源顺序（Q52）；不保证重生/离线后 combat 还能按 UUID 找到原攻击者。

HUD 归 panel，通过 `Skills`、`Cultivation` 的公开操作只读本人数据；冷却六行、格挡十帧与弓蓄力十帧见[面板正文](../panel/architecture.md)。弓蓄力真值仍随 equipment 的实际弓入口推进，本批不在渲染中补造。

当次历史验证：一次 `-PsmokeOnly=skillui`，9 项中 8 项通过，报告 `DONE`；翻页/连招、技能选择、箭种选择、丹火扣费与实例数据、扫描扣费、C 按下/松开同步、格挡实体到期、自爆立即死亡和死亡后爆炸均已走真实网络/世界路径。首项断言发生在首步立即结束后、客户端菜单尚未打开，后续按钮操作正常；检查将按键请求拆到下一步并后移截图，当次修正后只编译、未重复运行；后续九项全过与真实菜单截图见下段，该旧等待失败已被覆盖。HUD 截图已查看，固定来源坐标在默认小窗口中会裁切右侧冷却文字；菜单截图早于界面，尚不能据此认可菜单布局。自爆后出现针对已移除本人实体的附件同步警告，未出现 ERROR 或保存异常；多人、完整视觉及 1.18.2 版本实机对照未执行。

后续界面核对（2026-11-07）：逐项核对 `JinengGuiScreen`、按钮消息、界面打开过程、箭种显示/选择条件及两种 HUD 的来源坐标、资源、颜色与分档，未发现需改动的差异。沿用现有 `runSmokeClient -PsmokeOnly=skillui`，未修改检查代码或新增检查，9 项全部通过并 `DONE`；客户端保存四个世界后正常退出，ERROR 0。临时将开发客户端 GUI 缩放从 3 改为 1，结束后还原；854×480 截图中四技能区、轮盘、六功能图标及六行冷却可见，菜单截图已捕获真实界面。证据为 `tmp/skill-ui-native-{client.log,report.txt,menu.png,hud.png}`。这是当前界面和 HUD 的有限运行证据，不覆盖正式虚空往返、丹火放置、完整动画、多人或两版本实机对照。负责人明确客户端验证使用项目内置机制；本次误用 Computer Use 已停止，普通客户端在标题页正常退出，未进入世界。


## 弹体粒子发出处

2026-11-07 对照实体实际钩子和各飞行/命中过程，接回元素弹体、灵纹弹与技能箭中的19种自定义粒子。调用均为服务端sendParticles，保持数量、偏移、速度及循环位置：火球按每个敌方对象在同一弹体位置发一次，范围箭在各目标半高处发出，水弹的爆发不以敌友条件过滤，其余按各过程判定。没有自定义粒子的飞行或命中分支保持原样。

来源SkillArrow02TouZhiWuFeiXingTick只有无重力、每刻两枚灵气、二十刻时额外二十枚灵气和移除；不消费arrowskill/manxvli、不写jinengName。修正此前目标将第02箭合入普通箭模式初始化的偏差。来源本身没有新增矛盾；弓条件入口已随 equipment 接回（Q57）；炙烤代码已由yihuo接回，运行失败与炉鼎消费者仍未闭合，见[异火正文](../yihuo/architecture.md#本批验证2026-11-07)。

本批仅一次skillparticle专项两项通过并DONE，验证粒子实际广播到客户端提供器，以及第02箭保留基础伤害和玩家施放标记；截图已查看。build与ArchUnit通过，19个粒子引用均存在。未重跑旧神通；全分支视觉、多人、1.18.2版本实机对照未执行。

## 动画实体与功能入口粒子

2026-11-07 接回其余有效 skill 粒子调用，使用 particle 公开类型查询和既有 `ProjectileSupport`、`SkillDelays`，行为仍由服务端推进。命中粒子在原目标半高发出，保留敌友过滤、伤害/效果前后顺序与逐目标随机：冰天雪地每个目标各随机选择天蓝/白爆发；火龙卷的原版火焰在目标脚下，漩涡在半高；天外陨石没有化石手的深橙爆发。金轮各有效模式到期发 `brokenyellow` 30 枚，偏移 1/0/1、速度 0.2。

木系乱枝、灰木剑、爆裂、树梯的破碎粒子按各自到期分支发送，爆裂速度 0、树梯速度 1；地蔓鞭每次出手中心另发材料粒子。水蛇鞭各式保留命中和收尾碎片，Alt 式只在计数不小于 3 时持续发送；普通水连鞭没有同类命中粒子，只在 32–35 刻向视线两格位置发送收尾粒子。水龙每刻发十枚蓝色碎片，空浮球与陨河按引爆、命中和到期分别发送；陨河普通式补回来源原版水花。尸体死亡拥抱和粘液、生命感知施放、轻功起飞/持续、研习、元婴出窍/死亡及自爆均在原过程位置接回。

Q54：`SkillGold0201DangSiWangShiJianDaoShiProcedure` 中虽有黄色破碎粒子，但生成实体 `SkillGold0201Entity.tickDeath`（207–212 行）只计满二十刻后移除和掉经验，没有调用该过程；`SkillGold0201.mod.json` 未绑定它，全来源 Java、JSON、XML 及工作区文件仅有过程自身和元素清单登记。评估轻微，保持来源实际入口，不给目标新增死亡钩子。各模式刻过程中的有效到期粒子已接回。

静态审计：归属为 skill 的全部来源类引用 55 种自定义粒子，目标 skill 引用 53 种；差集的四种星形粒子仅属于 Q52 的服务端空扫描调用，目标另有共享研习入口的两种天干地支粒子。所有有效引用均有已登记提供器。审计核对身份覆盖，时机、参数和可达性分别以实际过程核对。

该批历史验证仅一次 `-PsmokeOnly=skillvisual`，两项通过并 `DONE`：本体移除后仍收到后续光爆，以及血雾起飞和持续粒子送达真实客户端提供器。客户端正常保存退出。血雾截图可见，当次光爆截图未捕获可见粒子；后续运动批次的 `tmp/skill-entity-lifecycle-{light,flight}.png` 已分别有光爆与血雾可见样本，见[运动正文](#动画实体运动与死亡时序复核2026-11-07)。这些有限样本仍不覆盖各分支完整视觉、多人及1.18.2版本实机对照。build（含 ArchUnit）通过。证据在 `tmp/skill-other-particles-{build,smoke}.log`、`tmp/skill-other-particles-report.txt` 和 `build/smoke/screenshots/skill-{light,flight}-particles.png`。

## 天雷术与五雷轰顶（2026-11-07）

`A:SkillThunder09shifangProcedure` 实际由“火”分组分派；在视线六格的方块位置上下找地面，最多六次。天雷术在中心落一道，五雷轰顶依次在中心、X+3、X−3、Z+3、Z−3 落五道；白色、直径 4、攻击量与倍率取技能书，施放者 UUID 与团队取公开操作。`ThunderEntities` 调用 combat 的 `Lightning.command`，范围伤害、同步颜色和原生闪电生命周期归 combat；队伍占位的来源解析问题见 Q55 和[战斗正文](../combat/architecture.md)。控雷阵原有计时命令也已接回。

当次历史验证（2026-11-07）：一次 `lightning` 检查3过1失败，报告DONE。范围雷伤、红色同步/渲染器及控雷阵命令通过；两个技能的合并断言因辅助方法连续选同槽、第二次取消而失败，随后在选择前清空并编译。该次build（含ArchUnit）及专服正常保存退出通过、ERROR0、实体导出63个；早期截图不能认可完整视觉，完整xiulian雷劫未在该次运行。原始失败保留于 `tmp/lightning-report.txt`，已由下述后续通过覆盖，不再列作未闭合专项。

后续运行（2026-11-08）：主会话未修改检查、未新增Test类或选择器，仅一次沿用现有 `runSmokeClient -PsmokeOnly=lightning`，4项全部INFO、0FAIL、DONE；额外鼠标不捕获INFO不计玩法项。58秒正常结束，四个世界全部保存，客户端日志ERROR/FATAL0，证据 `tmp/skill-exit-lightning-{client.log,latest.log,report.txt}`。通过范围为共享命令居中、范围内独立雷伤、红色数据到真实客户端渲染器且不追加原生雷击伤害、五雷十字五点与天雷单点生成、控雷阵到期命令及移除。

该检查的 `LightningCheck.cast` 人工创建并绑定技能书、写槽和选择后，直接在服务端调用生产 `SkillCasting.onRightClick`；未从客户端 `useItem` 发包，也未通过本批04装配。报告沿用的“真实右键入口”标签只证明生产施放方法和真实世界生成链，不升级为完整自然右键/网络验收。本次未新增构建、专服或逐项视觉检查；完整粒子声音、自然操作、1.18.2版本实机、两版本对照和多人仍未验收。

2026-11-07 雷劫批次：动画模型记录 `EntityModelRow`、纹理接口 `AnimatedLook` 和 `AnimatedEntityRenderer` 已移到 platform，供 skill 与 xiulian 共用；技能动画控制和玩法仍在 skill。本批不重跑旧神通套件，依赖关系由构建中的 ArchUnit 检查。

## 状态效果身份与原生生命周期复核（2026-11-07）

完整读取1.18.2版本 `Skilleffect01–09MobEffect`、`Skillwind04MobEffect`、霸体/禁施的开始与结束过程、风飘施放和飞行减耗，并检索两个来源的全部生成Java引用。`skilleffect_01`–`07` 的类只声明类别、颜色、描述ID和恒真刻判断，没有自身过程；生成Java除注册外未找到自然施加者或消费者，可读资源仅见语言键。保留登记身份与图标，不根据名称添加冷却/战斗行为。`skillwind_04` 由风飘术施加1200刻、等级为截整攻击量；飞行减耗读取等级加一、减后最低为一，再继续附魔折扣与掠空规则。目标 `WindEntities.drift`、`SkillFlight.tick` 与此相符。

原先共享效果在 `MobEffectEvent.Added` 中执行开始/结束，但NeoForge21.1.265该事件发生在原生效果写入/更新之前。首次霸体现读自身为空，附加发光为零刻；刷新读到旧时长。来源开始过程在原生属性钩子内执行，写入后能读自身，逃跑 `effect_034` 还会读取等级并移除自己。目标现使用 `ContentEffect.addAttributeModifiers/removeAttributeModifiers` 执行 `EffectBehavior.started/ended`；`Applicable` 覆盖普通和强制施加，到期/移除事件及服务端效果刻只绑定实体上下文。刷新与隐藏弱效果恢复由原生先移除再添加属性，不在事件中模拟更新。属性表弱键及弱实体引用不保留卸载实体；载入不重放开始过程。`InnerExplosion` 自有原生桥接保持独立。通用机制的精确源码依据见[移植参考](../../../.claude/skills/minecraft-mod-porting/references/targets/neoforge-1.21.1.md)。1.18.2原生映射源码载体为Forge40.3.11，另核对来源精确Forge40.1.0的 `LivingEntity` 补丁，未将前者称为40.1.0完整映射源码。

`build compileSmokeJava` 与ArchUnit通过；仅一次项目内置 `runSmokeClient -PsmokeOnly=combat`，53项战斗检查全部通过，报告DONE，客户端保存四世界并正常退出、ERROR/FATAL为零。在已有 `CombatCheck` 中仅补8项真实Bug回归：首次80刻发光、刷新240刻、抗性不重复、无效弱施加不重放、批量清除还原、强制施加、隐藏弱效果恢复及时长、恢复后清除还原；没有新增Test类或选择器、没有重跑整套skill。开发FML配置finally按原字节恢复，SHA256一致。证据为 `tmp/effect-hooks-{source-audit.log,build.log,client.log,report.txt,config-restore.log}`。本轮未运行专用服务器、未对每个效果逐一实机验证；多人、完整视觉、1.18.2版本实机与两版本对照未执行，skill仍未完成。

## 技能箭物品使用动画逐类复核（2026-11-07）

完整核对1.18.2版本 `SkillArrow01–10Item` 和共同释放条件过程：第01–09箭为 `UseAnim.SPEAR`，第10箭实际为 `UseAnim.NONE`；第02和10箭堆叠上限一且不在创造栏，其余箭的属性保持来源。十种物品均开始使用当前手物品，使用时长72000刻，释放条件恒为假。原先 `SkillArrowItem` 错把第10箭也统一为矛式，现由所属系统的 `skill:arrow` 和 `skill:light_arrow` 工厂传入各自原生动画，仍使用同一个物品类；转换覆盖表和生成内容表同步。目标NeoForge21.1.265的 `Item.getUseAnimation/getUseDuration/releaseUsing` 与 `UseAnim` 已核对。源码路径、行号和SHA256见 `tmp/arrow-items-source-audit.log`。

转换只改变第10箭的一行工厂配置，`build` 含ArchUnit通过；沿用已有 `runSmokeClient -PsmokeOnly=skillarrow`，12项全部通过并DONE，四世界正常保存退出、ERROR/FATAL0，没有新增或修改任何检查代码、Test类或选择器。该场景证明新工厂可随真实客户端加载及原有箭选择、命中、掉落和内爆/保存主路径可运行，未逐项查看手持动画；动画差异本轮为静态源码核对与实际装配证据，完整视觉及两版本对照未验收。FML配置finally按原字节还原、SHA256一致。证据 `tmp/arrow-items-{convert.log,build.log,client.log,report.txt,config-restore.log}`；本轮未运行专用服务器，skill仍未完成。

## 共用弹体保存与命中箭数复核（2026-11-08）

1.18.2版本的火球飞行过程将攻击量、倍率、技能名和计时写入实体持久数据；飞羽冰花与金轮的方向/角度、飞石的初始化标记同样如此。Forge40.1.0的Entity补丁把持久数据复制到ForgeData并在载入时恢复；原生NoGravity也保存，不能依赖重读后再次触发初始化。目标此前把这些值改为普通字段却未保存，载入已初始化的弹体会丢失数值和进度。现在由共用SkillProjectile写入skillProjectile复合标签：ticks对应tick、mana对应jinengmanaatt、multiplier对应jinengshanghai、skillName对应jinengName、angle对应jiaodu、reversed对应fangxiang、saved对应baocun。仍留在原生持久数据中的其他键继续由NeoForge保存。

穿透仍保存为原生PierceLevel；NeoForge21.1.265的原生读取写私有字段，共用类覆盖getter使用自己的字段，现同步读回。1.18.2原生AbstractArrow不保存击退等级，撤掉技能箭此前额外保存的skillArrowKnockback，不向共用标签增加击退；依据是Forge40.3.11映射载体的完整保存/读取方法，并核对精确来源Forge40.1.0补丁无此项改动，未把映射载体冒称40.1.0完整源码。

逐个核对26个对应来源箭类的doPostHurtEffects，全部在super之后无条件减一箭数。目标此前仅十种技能箭做了减回，现统一放到共用类，删除技能箭重复钩子；不增加穿透条件或数值钳制。静态证据为tmp/projectile-state-source-audit.log，含来源方法、四个飞行过程相关保存字段、来源精确补丁、原生保存/命中链与SHA256；这不表示26类完整生命周期已逐类复核。

验证：build compileSmokeJava及ArchUnit通过。已有skillparticle场景只补三项真实保存/命中Bug回归，共五项全部通过，覆盖原生实体保存重读、飞羽冰花下一刻从3推进至5且角度45减至25，以及共用火球真实命中不留插箭。首轮同时运行已有skillarrow，十一项通过、箭雨一项失败；报告DONE、四世界保存退出、ERROR/FATAL0。只给原箭雨断言增加诊断后复跑，实际剩17支，主箭已命中、模式为箭雨、服务端经过12刻；退出阶段另有两条ClosedChannelException错误，四世界仍保存完成。读取该失败存档确认场地上方Y=76–86处存在155个橡树叶和4个原木：通用场地只清空脚上7格，而箭雨生成在命中点上方16格，存活数检查受未清理树冠干扰。

仅将SkillArrowCheck本场景的清空高度延长到脚上18格，保留20支、主人和非递归断言；最终只运行skillarrow，十二项全部通过、实测箭雨20支、DONE、四世界保存并正常退出、ERROR/FATAL0。没有新增Test类或选择器，没有重跑整套skill。三次FML配置都finally按原字节恢复，SHA256相同。首轮、诊断和最终结果分别在tmp/projectile-state-{first,arrow,final}-{client.log,report.txt,config-restore.log}，场地证据tmp/projectile-state-rain-world-audit.log，构建tmp/projectile-state-build.log。未运行专用服务器，未逐类实机验证全部弹体；完整视觉、多人、1.18.2版本实机和两版本对照未执行，skill仍未完成。

## 弹体落地条件与原生模型复核（2026-11-08）

按16个非技能箭弹体的实际实体类、显示物品与渲染注册逐类核对，完整读取本批九个落地方块过程。火球、水球、水灵弹、冰锥、冰花、定魄神光、飞石与风弹的方块命中过程只接收弹体，不要求主人；目标此前被共用入口的主人条件提前拦截。现由服务端直接分派方块命中，冰矛与十种技能箭在各自过程内保留来源的主人条件，飞行和实体命中的主人条件继续保留。冰矛无主人时不破冰、不散射六枚冰锥。

来源将Ice01、Ice02、Ice03与Lingwentexiao注册为独立Java模型渲染器，其余十二类使用ThrownItemRenderer。现接回四个原生模型与对应textures/entities贴图，四个几何方法的全部表达式、部件层级、姿态及UV逐项机械比对一致，立方体数分别为3、9、91、1，纹理与来源逐字节一致。SkillProjectileRenderer由原技能箭渲染器复用，保持偏航减90度、俯仰加90度的旋转顺序、entityCutout、NO_OVERLAY和来源1/16透明度；技能箭原有几何继续由SkillArrowModels提供。冰花setupAnim虽设置zong.xRot，但来源渲染器只调用renderToBuffer，故未补入动画调用。

SkillProjectile的显示物品由各注册工厂按来源提供：三种冰弹、灵纹弹、金轮与水灵阵水弹返回空栈；飞叶返回cailiao_087；其余九类使用本体物品。十种技能箭显式传空栈，去掉仅覆写空栈的内部Arrow子类。原生箭内部保存所需的默认物品与实际拾取规则沿用此前实现。system_assets.csv登记四张贴图，convert_assets.py通过并仅在manifest增加四行；转换仍报来源已登记的空纹理和fabao021/022缺失，没有新增缺失。

静态证据为tmp/projectile-lifecycle-source-audit.log、tmp/projectile-lifecycle-differences.log及tmp/projectile-lifecycle-model-audit.log。前两者收集16个完整实体类及39个实际绑定过程；随后按光、土、木、水、风、灵纹及金、冰分组逐行复核全部过程，确认目标的命中、方块命中、计时、追踪、粒子、伤害与生成分支均与1.18.2实际代码相符，没有新增代码差异。实际自然施放与视觉仍按下述范围验收，不能以静态复核替代实机行为。目标API依据为NeoForge21.1.265的EntityRenderer、EntityRendererProvider、ThrownItemRenderer、ModelPart、模型构建器及EntityRenderersEvent。

验证：build compileSmokeJava和ArchUnit通过。沿用内置runSmokeClient的skillparticle选择器，只在已有场景加一项真实共享入口Bug回归及模型截图，没有新增Test类或选择器。首轮原五项通过、新撞墙检查失败；仅增加诊断后的同场景六项通过，水球入地并移除、自定义粒子数1，服务端经过5刻。首轮失败原因未被日志锁定，不能归咎于玩法或声称已确定为收包延迟。检查随后改为最多40个客户端刻等待粒子到达，并将原生入地标记加入必要条件；最终六项全部通过并DONE，四世界保存并正常退出，三次ERROR/FATAL均为0、开发FML配置均finally原字节还原且SHA256相同。最终截图已查看，四个模型可见；完整自然施放、多人、1.18.2版本实机及两版本对照未执行，本轮未运行专用服务器，未重跑整套skill或旧神通。

首轮、诊断和最终证据分别为tmp/projectile-lifecycle-{first,diagnostic}-{client.log,report.txt,config-restore.log}与tmp/projectile-lifecycle-{client.log,report.txt,config-restore.log}；构建、转换及失败存档检查为tmp/projectile-lifecycle-{build,assets,world-audit}.log，截图build/smoke/screenshots/skill-projectile-models.png。skill仍进行中，不合main。

## 弹体实际过程与发射入口复核（2026-11-08）

按1.18.2版本实际实体类、物品类、`Jinenglist01Procedure`、灵纹/冰矛/满天花雨/激流波/水灵阵及来源静态 `shoot` 调用逐项核对。16种非技能箭物品的 `releaseUsing` 均为有效入口，目标用统一 `skill:projectile` 工厂按物品 ID 接回使用、松手、耐久、显示物品、击退、暴击、声音及发射速度；十种技能箭的恒假条件没有套用到这16种物品。玩家技能施放的速度与基础伤害按实际过程保留：风弹术0.7、定魄神光3、幻光术0.2、飞石术1.2，其余该入口速度1；物品松手入口中 `skill_light_02` 与 `skill_water_05` 的速度为4，其余为2，`skill_water_0101` 的基础伤害为1，其余为0.1。

火球命中循环按来源保留：每个范围内非友方对象触发一次粒子，但伤害请求引用直接命中目标；Q63记录该来源行为。水灵阵即使找不到主人仍生成无主人水弹；`WaterSeeker` 使用目标版本 `UndashedUuid.fromStringLenient` 对齐来源 authlib 的宽松 UUID 解析，无效值只保留当前速度。拈花飞叶到期时补写来源的 `AttackDamage` 与 `Damage_multiplier`，命中读取这两个持久字段并请求木系伤害；常规书缺少前一个键时按来源结果为零。

两处金轮生成沿来源调用 `finalizeSpawn`，非玩家金轮模式读取主人 `friendlist.jinengName` 的左旋、右旋和天堑分支，目标保留这些模式、角度与动画。来源将该好友列表子标签作为共享引用并原位写入金轮字段；已逐项核对 skill 归属的实际消费者，金轮、灵纹、冰系、木系和水系使用的状态均由目标实体或技能本地字段承载。灵纹命中过程写入好友列表物品的计数属于来源副作用，按 Q45 保持当前项目取舍，不复现残留计数。`GuanghuanDangShiTiGengXinKeShiProcedure` 属于 world 系统，其共享字段消费者留给 world 批次，skill 不重复实现。

验证：`build compileSmokeJava`（含ArchUnit）通过；沿用项目内置 `runSmokeClient -PsmokeOnly=skillparticle`，同一检查场景的8项全部为INFO并以`DONE`结束，覆盖原生物品使用/松手生成、火球直接目标与邻近傀儡、无主人水球落地粒子、弹体保存重读与速度/伤害入口。四个世界正常保存退出，ERROR/FATAL为0，FML配置原字节恢复（`tmp/projectile-behavior-config-restore.log`），最终报告与日志见 `tmp/projectile-behavior-report.txt`、`tmp/projectile-behavior-client.log`。本次未逐类自然操作16种物品、未运行专用服务器、未做1.18.2版本实机或两版本对照；skill仍进行中，不合main。


## 共用功法与技能装配页（2026-11-08）

### 归属与自然入口

本批复读 `A:PlayerGUI04Menu/Screen`、按钮消息、`OpenGUI04`、延迟载入、四类插槽过滤、`GUI04pageup/down` 与关闭过程。04是共用功法/技能装配页：菜单与屏幕由 skill 持有，沿用已经存在的 skill→gongfa 依赖；功法槽真值仍归 gongfa，技能槽真值仍归 skill。panel 只导航、记页与返回，不建立合并的玩家玩法档案。15个装配元素据此改判归 skill；导航 `OpenGUI04` 留在 panel。

`SkillMenus.LOADOUT` 保留注册ID `player_gui_04`，由原生 `SkillLoadoutMenu` 与 `SkillLoadoutScreen` 实现；G键的 `SkillMenu` 没有物品槽，只负责选择、轮盘与功能操作，不能替代本页。公共书潜行研习由 book 的 `StudyBookItem` 分派到既有 `Skills.study`，完整36载体、条件、成功效果与答题计数只维护于[book正文](../book/architecture.md#自然研习入口2026-11-08)。

### 八槽与过滤

来源 `PlayerGUI04Menu:47–139` 的8个自定义槽加36格原生背包，共44槽：

| 槽 | 坐标 | 来源字段与条件 | 目标数据所有者 |
| --- | --- | --- | --- |
| 0 | 173,168 | `gongfa`；`ExercisesName`非空 | `Techniques.slot/setSlot` |
| 1 | 167,206 | `qinggong`；`shenfaName`非空 | `Skills.movement/setMovement` |
| 2 | 167,226 | `flyjineng`；`flyskillName`非空 | `Skills.flight/setFlight` |
| 3 | 296,206 | 当前页第1技能；`jinengName`非空 | `Skills.spell/setSpell` |
| 4 | 316,206 | 当前页第2技能；同上 | 同上 |
| 5 | 296,226 | 当前页第3技能；同上 | 同上 |
| 6 | 316,226 | 当前页第4技能；同上 | 同上 |
| 7 | 234,206 | `wuqiskill`；`smanshipName`非空 | `Skills.weaponArt/setWeaponArt` |

目标以功法组件或对应 `SkillKind` 的非空业务名实现上述过滤，不加已研习、本人归属或境界限制。来源说明“研习后才可装备”比真实插槽条件严格，不能据说明改变过滤；是否能选择、施放或飞行仍由实际消费者判断。

菜单不把装配槽硬限为一件。来源公共书可叠，载入时改数量1与关闭时保存完整栈是两个不同动作。原生背包槽8–34的坐标为 `170+18×列,251+18×行`，热键槽35–43的y为309。普通点击与Shift移动沿原生容器交互；快捷移动保留8/35/44区间、既有栈合并时检查 `mayPlace(existing)`、空槽阶段只接受首个可接收槽等来源次序，物品相等比较使用目标组件语义。

### 开窗、延迟与保存

`Skills.openLoadout(ServerPlayer)` 以玩家实时位置上方打开；重载 `openLoadout(ServerPlayer, BlockPos)` 接收最终开窗坐标。01/03/04跨页按钮携带旧菜单坐标，由panel增加一格后调用公开入口，K恢复从玩家实时坐标增加一格，保持来源 `OpenGUI04(x,y+1,z)` 的实际调用方式。菜单在最终位置查询无方向 `Capabilities.ItemHandler.BLOCK`；存在时绑定真实方块库存，否则使用私有8槽。自然打开因此可能绑定上方容器，不能简化成永远私有库存。

来源名称含“每刻”的打开过程实际只安排一次两服务端刻延迟。目标 `SkillSetup` 在 `PlayerContainerEvent.Open` 通过既有 `SkillDelays.after(2,...)` 安排一次任务，执行时查询玩家当时仍打开的04菜单，载入0/1/2/7及当前页3–6，并广播；不是每刻覆盖槽位。来源回调可处理任意带 `Supplier<Map<Integer,Slot>>` 的当时菜单，目标仅处理 `SkillLoadoutMenu`，两刻内换页、关闭与重开边界须单列运行验证。

载入沿用来源持久栈与菜单共享引用的行为：skill将原身法/飞行/武器技及当前四技能栈数量改1，经自身公开写操作同步，再把同一栈交给菜单；功法由 `Techniques.setSlot` 先归一数量，再读取gongfa当前持有栈装槽。共享引用让开窗期间的书籍成长进入同一实例，不能用临时副本在关闭时覆盖新进度；功法成长的所有者写入契约见[功法正文](../gongfa/architecture.md#自然入口依赖接回2026-11-08)。`Corpses.grow` 同样改在当前技能槽原栈上更新起尸诀组件，再经 `Skills.setSpell` 同步，不替换成菜单未持有的成长副本。延迟没有新增每菜单token或初始化标记，旧04关闭并在两刻内重开时，旧任务仍可载入当时的新04；目标只处理04的类型范围仍与来源通用Supplier模板有边界。

button0/1先把当前四技能保存到旧页，再通过 `Skills.turnPage` 减/加页码，最后载入新页四栈并设数量1。来源减一后≤0回4，加一后>4回1；四页分别对应01–04、05–08、09–12、13–16，页真值与G和Tab共用。翻页只保存四技能，功法、身法、飞行和武器技在关闭时保存。

关闭先执行 `super.removed` 的鼠标携带栈原生处理，再将0/1/2/7和当前页3–6交回各数据所有者：空栈清对应字段，非空保存完整栈。正常关闭、死亡或断开均不对已保存的0–7再次返还或掉落。功法必须经 `Techniques.setSlot`，立即重提xiulian主修信息与attribute贡献；来源 `gongfalinshi` 只是比较快照，不建立第二份功法真值。翻页与关闭不清已选技能、选中键/槽、连招、箭种或六组冷却。

### 显示与消息

`SkillLoadoutScreen` 保留500×500原生菜单屏幕，背景使用 `gui`、`gui04`、`gui0601`、`wupilanbiankuang`、`guipage03`、`shipintaigui05`。上一/下一页按钮分别在 `(295,193)`、`(325,193)`，8×10；页码显示整数。身法与飞行名称读取已保存的本人字段，刚插入但尚未关闭的新栈不会提前改名；空显示“无轻功”“无飞行技能”，功法同样读取gongfa公开槽信息。

屏幕按钮只走原生容器按钮消息，由服务端当前菜单分派：0/1翻页、2请求属性、4请求饰品、7发送九行来源说明。说明保留来源“武器法门未实装”的陈旧说法，未据此删除已经存在的武器技。02/05/06的来源按钮在本批不展开；三页导航、K记4和丹田返回契约只维护于[panel正文](../panel/architecture.md#三页导航与装配页归属2026-11-08)。

### 目标依据与本批验证边界

本批读取精确目标 `neoforge-21.1.265-sources.jar` 的 `SlotItemHandler`、`ItemStackHandler`、`IMenuTypeExtension`、`ServerPlayer.openMenu/doCloseContainer`、`AbstractContainerMenu.removed/moveItemStackTo`、`ItemStack.copyWithCount` 与 `PlayerContainerEvent`，按其真实能力、extra data、菜单关闭、堆叠与事件契约接线。`extract_book_study.py` 已提取36载体，`extract_skill_loadout.py` 已复制14项本页资源；转换成功只是静态接线证据。

本批唯一一次完整 `gradlew build compileSmokeJava` 在5分54秒通过，包含既有test/check与ArchUnit，证据 `tmp/study-loadout-build.log`。唯一一次 `runSmokeClient -PsmokeOnly=skillui` 在1分15秒完成，29项玩法检查全部INFO、0FAIL、DONE；另有一条鼠标不捕获INFO不计玩法项。客户端正常保存主世界、下界、末地和正式虚空后退出，ERROR/FATAL0。证据为 `tmp/study-loadout-{client.log,client-latest.log,report.txt}`。没有新增Test类、检查选择器或使用Computer Use。专用服务器一次启动到 `Done (1.360s)`，由原生stdin执行 `stop`，四维度全部保存并正常退出，ERROR/FATAL0，证据 `tmp/study-loadout-server-latest.log`。注册导出菜单12、方块1129、物品2126、实体109，含新增04；台账最终4939行0问题，来源指纹确认31623份文件未变。此前台账因专服旧导出缺新菜单出现一项问题，导出刷新后已闭合，不作为当前失败。

已查看 `build/smoke/screenshots/skill-loadout`（无扩展名PNG）的装配前真实04截图：原生槽位、页码、动态空名和导航可见；当前默认缩放使页面下方略超出屏幕，截图未展示装入后的书，也不代表完整视觉认可。

本批复用 `SkillUiCheck`，保留原9项并扩展20项自然链：四类技能及功法经真实客户端 `useItem` 潜行研习，境界失败/本人重复保留答题计数，成功清零且叠2数量与组件双端一致；普通左右点击拆一本、Shift装入功法/三类槽、两页保存、Shift取出与余本合并，再装入和关闭重开均通过。01→04→03→04→01真实按钮和K记忆通过；开窗时发布实际 `CultivationExperienceEvent(1)` 后功法经验增加1.5，同一菜单栈/本人组件同步，关闭与重开没有回滚。随后G选择验证消费刚由本页保存的同一技能组件。该次仅覆盖前两页，完整四页循环、未研习插入及之后拒选、灵根失败、他人归属、副手同载体、头顶真实容器、延迟抢先关闭/重开、起尸诀开窗成长、04死亡/断线/重登存档与完整视觉未验收；1.18.2版本实机、两版本对照和多人均未执行。

## 出口剩余范围（2026-11-08）

本批接回自然研习与装配不代表skill出口完成。当前余项分别登记，不能合称只有视觉待核：

| 范围 | 当前状态与去向 |
| --- | --- |
| 天雷术/五雷轰顶 | 后续现有lightning四项全过，旧辅助选择失败已覆盖；直接调用生产服务端施放方法，客户端自然useItem网络仍未验收，详见[落雷正文](#天雷术与五雷轰顶2026-11-07) |
| 丹火与炙烤 | 现有skillui的丹火真实使用/双端库存、计划刻与受控持续效果五组通过，旧失败转为历史，见[复验](../yihuo/architecture.md#丹火与炙烤主路径复验2026-11-08)；18材料逐类、接触全链与视觉未全验，炉鼎消费者随D |
| 伤害范围与妖兽风技能 | 公开实体/API已实现，九处creature范围生产者及两个妖兽施放时序随E阶段creature |
| 裂隙返回维度 | 正式虚空已有12项有限检查；`dimensiontianqve/xiuluo/renjian/yaohun/wugui/diling`六维度随world，完整自然往返未验收 |
| 其他自然消费者 | 书架藏宝自然use已由book接回并通过客户端消费/奖励同步；获取载体仍随出生/宝箱，公共书其他分支随book，完整重置随fulu |
| 独立功能生命周期 | 元婴跨维度、死亡与重登，飞行死亡恢复等未完整验收 |
| 表现与对照 | 35实体完整过程、同名重播、逐类受伤/死亡、粒子声音、GUI/HUD缩放，1.18.2版本实机、逐项两版本和多人仍待验收 |

旧damagearea 5/6、skillui 8/9、箭雨场地与早期弹体失败已有后续通过，不列为当前未修复问题；已有可见光爆、血雾、劫云、模型和GUI样本也不再写成完全没有截图。Q62缺绑定动画名、Q53孤立箭/标记、Q54未绑定金轮死亡过程、Q52空服务端扫描继续按来源无自然效果保留，不补造调用。
