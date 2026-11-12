---
topic: yihuo
role: architecture
status: partial
updated: 2026-11-08
---

# 异火：来源规则与实现边界

本系统进行中。本批接回 skill 所需的炙烤持续效果、丹火火块，以及18种异火材料物品和投掷实体；外部获取与装置消费链随所属系统接回，完整交互与视觉仍待验收。源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

归属范围为101个元素，2026-11-07全部完成归属阅读，逐分支实现与运行另记。B 的 `YihuoshujvfuyvProcedure:22–58` 从材料目录连续读取19条，以来源固定值 `jindumax = 6` 初始化材料辨识；该过程已由 material 的登录公开入口接回。A 的 `FireskillProcedure` 另行核对后确认是孤立过程，不存在运行时入口，不向目标添加未被来源调用的武器触发。不从邻近重做工程继承“18种异火”的范围划分。

## 武器触发来源核对（2026-11-08）

`FireskillProcedure` 的事件块为 `no_ext_trigger`，源码仅包含自身 `execute(LevelAccessor, Entity)` 方法；来源索引的 `procedure_reachability.csv` 将其标为 `False`，`procedure_calls.csv` 只记录它对 `FriendpandingStoEeProcedure` 的内部调用，未发现事件注解、物品回调或其他非过程类调用者。它的逻辑是 10% 概率检查主手 `cailiao_221` 或 `lianqizong.fenglei`，在半径 4.5 的范围内对不友好实体执行固定的 `/shanghai 火 <UUID> false 5 1`，并发出两组粒子；由于没有调用者，这条分支在 1.18.2 版本也不会自然执行。

本次保持来源可达性：不把该孤立过程接入 combat 命中事件、物品右键或玩家刻更新，不新增伪造的武器入口，也不为不可达代码新增测试。后续若在来源工程中发现新的真实调用者，再按调用者所属系统重新划分数据所有权与事件阶段。

本批实际调用链：

- A 的 `JinengdanhuoProcedure` 经技能菜单给出单件丹火；`DanhuoItem.useOn` 调用 `DanhuoDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure`，放置 `cailiao_221fireblock` 或补充 `blockfire` 标签方块的燃料。
- `cailiao_221fireblock`–`229fireblock`、`231fireblock`–`239fireblock` 共18种火块，各保留自己的同名方块实体类型。全部共用 `Cailiao221fireblockFangZhiFangKuaiShiProcedure`、`Cailiao221fireblockGengXinYouXiKeProcedure` 与 `Cailiao221fireblockDangShiTiZaiFangKuaiZhongPengZhuangShiProcedure`。
- 接触附加 `effect_023` 或 `effect_024`，各自的 `Effect023/024ZaiXiaoGuoChiXuShiMeiKeFaShengProcedure` 推进持续伤害；耗尽燃料时走 `HuoyanxiaoshipandingProcedure`。

## 丹火与火块

丹火右键返回成功。先判断点击方块是否在 `blockfire` 标签：发20枚火焰粒子，有方块实体则把物品的 `power` 加入其同名燃料字段；不校验施放者，无论是否有方块实体都消耗一件。该标签含原版普通火、灵魂火、营火与灵魂营火，不能把标签成员都当作自定义火块。

其他方块仅在点上表面且上方为空气时尝试放置，物品 `ManaAttackUUID` 必须等于操作者UUID。过程放一级阳火，写入物品 `power`、施放者UUID、实体团队和友方表，并消耗一件；异主操作只发少量火焰与烟，不消耗。`PlayerJoinGameProcedure` 在登录时把能力团队写入实体字段，丹火读取的是该实体字段；死亡克隆与后续改队时不能自动把两个载体视为一个字段。

过程内的燃料写入不是自然使用的最终结果。Forge 40.1.0 的 `ForgeHooks.onPlaceItemIntoWorld` 与 `LevelChunk` 补丁、NeoForge 21.1.265 的 `CommonHooks.onPlaceItemIntoWorld` 与 `LevelChunk.setBlockState` 均在物品使用期间捕获方块快照，延后执行 `onPlace`。丹火过程返回后再执行火块放置钩子，一级燃料最终为10，随后补入7则为17；不把两次中间值22与29作为自然路径期望。此结论来自两个加载器的精确版本源码，1.18.2版本实机未执行。静态摘录见 `tmp/yihuo-placement-audit.log`，完整钩子位于 `tmp/yihuo-target/`。

来源直接把玩家友方 `CompoundTag` 放入火块，未复制；`ZuduiteamYesProcedure` 和 `PlayeruidelProcedure` 在原表上改值。因此重载前已放置的火会随原玩家对象的关系变化，玩家或方块分别重载后引用分开。目标由 relation 提供只读集合视图，火块持有视图但不能写关系；序列化保存当时的值，重读后使用独立集合。

火块等级为0–8，燃料单位为 `2^等级`，放置默认燃料为该单位的10倍。每20刻发对应异火粒子4枚；燃料足够时扣一个单位，向上方装置提交同量热量。不足时，上方在 `danludinglu` 标签且剩余 `lianpower >= 1`，则仅翻转装置 `gengxin` 并保留火；否则变为空气。供热发生在本轮扣燃料后，刚好归零的火留到下次计划刻判断。

接触先按保存的友方表查询接触者当前团队。非友方先写接触者的 `fireteam` 与 `fireAttackUUID`：玩家经 xiulian 的体质操作，其他实体写本体数据。燃料足够则命令火伤，基础量 `3×(等级+1)`、非固定、倍率1.5，再把同种炙烤时长增加100刻、等级设为火块等级、环境与可见标志均为真；接触不扣燃料。不足时发熄灭声并移除火块。

零槽方块实体仍继承原生 `RandomizableContainerBlockEntity` 并实现 `WorldlyContainer`，保留战利品表、容器存取、完整更新数据和六个方向的零槽物品能力；无方向查询不提供该能力。菜单提供器仍返回独立的三行箱子临时容器，来源没有自然右键打开入口，不增加入口。226、239的实心碰撞与发光后处理，以及224、227的掉落规则保持内容表差异，不统一物理属性。

## 阴阳持续效果

仅存活Mob及非创造、非旁观玩家推进。每刻发对应阳火或阴火粒子2枚，位置为实体脚下加0.7，散布0.8，速度0.1；等级0–7使用对应粒子，其他等级用第九种。`yanghuotick`、`yinhuotick` 是实体独立持久计时，效果移除与刷新不重置；每累计20刻结算并归零。

普通持续伤害固定基础量 `8×(等级+1)`、火属性、倍率1。攻击者从玩家体质或其他实体本体的 `fireAttackUUID` 读取，按原命令字符串解析，空字符串保留参数移位造成的无伤害。阳火命令保留末尾“异火”，并按来源一刻延迟队列在服务端刻结束时清受击无敌；阴火没有该清除操作。来源读取但未消费的 `fireteam` 不引入额外逻辑。

阳火到结算时若另有阴火，在半径2的AABB中按距离遍历Mob，包括本体，不预先过滤敌友，不包括玩家；每个目标命令非固定火伤，基础量 `10×(阳等级+阴等级+2)`、倍率1，命令内部的 combat 敌友与免伤仍生效。之后播放爆炸声、爆炸发射器及40个方向的火焰，两个炙烤效果立即移除。环的角度为序号乘9，原数值直接传入三角函数，不擅自换算弧度。

`SkillArrow05*` 施加阳火时没有写入 `fireAttackUUID`，不能把箭射手自动补成持续伤害来源。无来源且没有先前接触残留字段时，来源命令无法产生持续伤害；有残留字段时会读取残留来源。这是本次静态调用链结论，未用重跑旧箭测试代替核对。

## 目标实现与数据所有者

`YihuoEffects` 绑定两个效果的行为；`FireBlock` 保留内容表物理属性并负责计划刻、接触和移除；`FireBlockEntity` 保存燃料与来源身份；`YihuoBlockEntities` 注册18个独立类型与方向能力。skill 的 `DanFireItem` 调用 [公开操作](README.md#公开操作) `Yihuo.useDanFire`。

`FireHeatEvent` 和 `FireExhaustedEvent` 让生产系统消费自己的热量与刷新字段，异火不直接写其他系统的装置状态。`LiandanSetup` 与 `LianqiSetup` 已注册各自的热量和耗尽处理者，由所属系统写入丹炉或炼器鼎；处理及测试范围分别见[炼丹](../liandan/README.md)和[炼器](../lianqi/README.md)。火块台账保持“已注册”并写明已实现部分与缺口；两个效果和丹火物品按各自实际验证状态登记。

目标API核对版本为NeoForge 21.1.265，读取构建生成的sources jar：`BlockBehaviour`、`BlockEntity`、`RandomizableContainerBlockEntity`、`BaseContainerBlockEntity`、`RegisterCapabilitiesEvent`、`SidedInvWrapper`、`UseOnContext`、`MobEffectInstance`。更新包经完整更新标签包含持久数据，方块字段变更标脏并通知客户端。

## 保真约束

异火吞噬/联动等规则必须由本次 1.18.2 消费者证明，不从其他工程导入。

## 丹火调用链复核（2026-11-07）

本轮复核覆盖丹火召唤与自然 `useOn`、18种火块的放置/计划刻/接触、18个方块实体以及两个炙烤持续过程。18种方块逐类比较后，除身份外只有224/227的回退掉落、226/239的碰撞与发光属性不同，均已由内容表保留；18个方块实体除身份与名称外同模板。计划刻的18个分支已逐项核对：两族耗料单位均为1、2、4、8、16、32、64、128、256，扣料后才向上方方块实体加热，供热本身没有炉鼎标签门槛；仅耗尽后的保留判断要求上方属于 `danludinglu` 且热量至少为1。目标仍待D阶段装置所有者消费事件。

NeoForge 21.1.265 的 `BlockEntity.saveAdditional` 保存持久数据副本，目标先把当前共享友方视图写回再保存，因此保存标签与完整更新包包含保存时的友方表。`LivingEntity.tickEffects`、`removeEffect` 与 `MobEffectInstance.tick` 已核对，目标保持爆燃过程内立即移除两个效果，没有改成下一刻任务。自然放置的快照回放与燃料最终值沿用上文已经核实的两加载器依据。

本轮未发现上述已实现玩法需要新增修改。修正台账中18个火块方块实体仍为“待处理”的遗漏，改为“已注册”，依据为 `YihuoBlockEntities` 的独立类型注册及 `YihuoSetup` 装配；现有注册表导出不含 `block_entity_type`，不能把台账检查通过写成18个类型逐项实机验证。静态证据为 `tmp/yihuo-chain-source-audit.log` 和 `tmp/yihuo-family-audit.log`。本轮仅更新核对记录与台账，未新增或修改Test、未运行客户端或专用服务器；原专项失败和上文未验收范围继续保留。

## 外部获得与消费去向（2026-11-08）

本轮依据锁定1.18.2版本生成Java与数据标签，核对既有丹火、火块、炙烤和18材料实现之外的剩余入口。未发现新的 yihuo 自有可立即实施缺口；以下获取与消费流程尚待各所有者接回，不据此升级系统完成状态。

| 获取流程与所有者 | 具体来源与产物 |
| --- | --- |
| [宝箱](../treasure/README.md) | B `BaoxiangYihuocailiaoProcedure:17–96` 经 `BaoxiangCailiao:23` 或 `Baoxiangzong:110–111` 返回奖励描述：级别≤1为 `cailiao_221/231`，2至7依次为 `222/232` 至 `227/237`，≥8为 `228/238`；材料等级5至40，品阶另行随机。该表没有229/239分支。 |
| [任务](../quest/README.md) | B `PlayerRightClickBlockProcedure:271` 构造乾坤道人任务七的动作文本，扣10个 `cailiao_193`、给1个 `cailiao_221`；实际发放由任务步骤与动作解释流程执行。 |
| [修炼](../xiulian/README.md) | A `Ling18DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:184–191` 在传承灵物 `yikaiqi=22` 时给1个 `cailiao_222`，材料等级10、品阶4；完整开启顺序随传承灵物接回。 |
| [节日内容族](../festival/README.md) | A `Fuqi06DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:49,186` 的大红包随机奖励分别给1个 `cailiao_221` 或 `cailiao_231`；奖励掉落与红包消耗属于其完整奖励流程。 |

[material](../material/README.md) 管理目录、辨识进度、辨识界面及其扣料；19条异火辨识登录初始化已通过 `Materials.initializeYihuo` 接回。[storage](../storage/README.md) 的材料袋须保留 B `Cailiaodaishaixvan:12–14` 对 `huoyan` 的排除，B `Xiulitaixiuli:66,188` 同样排除异火作为修理材料，后者随 [lianqi](../lianqi/README.md)。这些排除分支不构成待补的异火消耗入口。

[economy](../economy/README.md) 的 B `Shougouselljisuan:349–350` 按 `huoyan` 标签调用 `Yihuochushou` 返回两族同级估价，未改变异火状态。[fulu](../fulu/README.md) 的 `HuafushaohuiZaiFangKuaiShangYouJian:33` 按 `huoyvan` 标签接受火源；[craft](../craft/README.md) 的 `TaopiZaiFangKuaiShangYouJian:62–67` 按 `dahuozhuangzhi` 标签启动烧炼；[zhenfa](../zhenfa/README.md) 的 `Zhenfajiance4:120–121` 按 `buzhenfire` 累计火行阵元。标签已登记，装置与流程状态由对应系统写入；烧符台、陶坯的火块渲染引用不视为物品产出。

[liandan](../liandan/README.md)、[lianqi](../lianqi/README.md) 已按上文 `FireHeatEvent/FireExhaustedEvent` 契约消费热量并决定是否保留火块。符箓、NPC等来源的炙烤施加与移除随其施放流程接回，跨系统效果操作沿用 `StatusEffects`。`Fireskill` 按上文来源不可达结论保留，不列为待补的自然武器入口。

## 未完成

材料右键的灵力和耐久消耗已实现；尚未接回的是上述辨识、外部获取、交易及装置等所属链。旧专项失败保留为历史记录，本次自然链检查及仍未覆盖的范围见下文。1.18.2 版本实机、两版本对照、完整视觉与多人未执行。

## 异火材料投掷物（2026-11-08）

1.18.2 版本的 `cailiao_221`–`229`、`231`–`239` 共18种材料物品共用一个使用类，但分别登记对应物品和实体类型。物品右键开始使用，服务端每个使用刻检查沉默效果与灵力；非创造模式按 `1 << (编号末位 - 1)` 扣除灵力，`cailiao_234` 保留来源的 `>= 8` 检查但实际扣除80。创造模式不扣灵力，并清除来源的连续施放计数。每100刻重置连续施放计数，连续第五次施放开始添加100刻物品冷却；来源使用 `dayTime` 计时，目标保持该载体。

每次成功施放生成对应 `projectile_cailiao_<编号>`，扣除物品1点耐久，无重力飞行，服务端每刻广播4枚对应粒子，累计8刻移除。命中实体先按 relation 敌友判定；非友方按火块同一来源字段写入 `fireteam`/`fireAttackUUID`，火伤基础量为 `3 × (等级 + 1)`、倍率1.5，再施加阳火 `effect_023` 或阴火 `effect_024`，在已有效果时增加200刻。编号221–226及236按来源延后一刻清受击无敌时间，其余命中立即清除。目标使用原生 `ThrownItemRenderer` 显示材料物品模型。

潜行命中方块时仅在上方为空气的位置放置对应火块，并以10倍燃料初始化施放者身份；非潜行命中 `blockfire` 标签方块时增加相应燃料。实体注册、客户端渲染装配和内容表已接回；外部获取与炉鼎消费随上文所属系统接回，完整操作、多人和两版本实机仍未验收。

本轮按1.18.2版本源码完成静态核对，`gradlew build` 通过；沿用项目自带 `runSmokeClient -PsmokeOnly=skillparticle`，客户端真实进入世界，报告以 `DONE` 结束并正常保存四个维度退出，未新增 Test、选择器或断言。该 smoke 结果只证明新增注册、客户端装配、资源加载和公共端启动未阻塞，不作为18种材料投掷/命中全路径的实机通过证据。

## 材料投掷物反馈补齐（2026-11-08）

逐行对照1.18.2版本 `Cailiao221KeYiShiYongYuanChengWuPinDeTiaoJianProcedure` 及其18种材料共用的条件分支，确认非创造模式灵力不足时来源会向玩家发送红色“灵力不足 N 点！”提示；其中 `cailiao_234` 保留来源的检查8点、扣除80点差异，提示仍显示8点。目标 `YihuoItem` 已在扣费前补回该提示，沉默、创造模式、连续施放计数和冷却条件不变。

本次使用项目自带 `runSmokeClient -PsmokeOnly=skillparticle` 做客户端加载回归，报告 `DONE`，客户端真实进入世界并正常保存四个维度退出，日志无 `ERROR/FATAL`。该既有场景不覆盖灵力不足提示的自然交互，提示差异以1.18.2源码静态核对为依据；未新增 Test、选择器或断言。

## 本批验证（2026-11-07）

当次唯一一次 `-PsmokeOnly=yihuo`：7项中2项通过、5项聚合失败，报告虽有 `DONE`，不能记为当次专项通过。通过项为方块实体保存重读/方向零槽能力/共享友方与接触燃料，以及无炉鼎消费方时的耗尽移除。其余丹火燃料、客户端同步、计划刻守恒、普通炙烤伤害与阴阳爆燃在当次未获得通过证据；后续有限样本的通过范围见下节。

当次检查把丹火自然放置及补7后的燃料误作22/29，实际应为10/17；检查牛的2000生命超过原生最大生命属性1024的上限。这些预期与场景错误、以及缺少子条件诊断的5项聚合FAIL，不能认定为对应玩法Bug。更正检查后当时仅 `build compileSmokeJava`（含 ArchUnit）通过，未重跑该专项或旧神通，不能倒改原运行结果。负责人随后再次强调不写无必要的 Test、优先按1.18.2版本代码核对移植；新增的 `YihuoCheck` 及选择器入口已撤下，脚本备份于 `tmp/yihuo-check-withdrawn-20261107.java`，原失败报告保留。

专用服务器一次启动至 `Done`，原生 `stop` 正常保存全部世界并退出，任务成功、ERROR为0。它证明当前注册与公共端加载能够完成，不证明炙烤伤害、丹火操作、完整供热链或视觉已验收。证据：`tmp/yihuo-report.txt`、`tmp/yihuo-smoke.log`、`tmp/yihuo-final-build.log`、`tmp/yihuo-server.log`。

## 丹火与炙烤主路径复验（2026-11-08）

本次 `gradlew build` 在1分36秒通过，包含既有check与ArchUnit；唯一一次现有 `runSmokeClient -PsmokeOnly=skillui` 在1分34秒完成，35项玩法检查全部INFO、0FAIL、DONE。客户端正常保存主世界、下界、末地与正式虚空后退出，ERROR/FATAL为0；日志有unknown entity附件同步WARN。证据为 `tmp/skill-natural-exit-build.log`、`tmp/skill-natural-exit-client.log`、`tmp/skill-natural-exit-latest.log`、`tmp/skill-natural-exit-report.txt`。

其中五组火链检查得到以下实际结果：

| 路径 | 本次有限样本 |
| --- | --- |
| 本人丹火自然放置 | 由真实客户端 `useItemOn` 放置221火块；双方燃料10，施放者UUID、团队一致，物品已消费。 |
| 异主与补燃料 | 异主拒绝新放但可向既有火补7，双方燃料17；向普通火使用会消费物品，营火燃料为22。 |
| 自然计划刻 | 两次实际游戏刻为 `[416,436]`，每20刻扣1；`FireHeatEvent` 累计热量2，燃料降至15。该观测不代替真实配方生产和完整炉次验收。 |
| 普通炙烤 | 受控500生命牛预置效果与计时后由自然效果刻推进，阳火/阴火/空来源生命差分别为16/8/0；阳火和阴火处理后的无敌时间分别为0/17。 |
| 阴阳爆燃 | 预置阳火等级1、阴火等级2及计时后由自然效果刻推进，两个Mob各损失50生命，双效果移除。 |

本次未改 yihuo 生产公式，未恢复 `YihuoCheck`、专项选择器或使用Computer Use。两个持续效果按源码实现与上述行为证据登记“行为已迁移”，不表示完整验收。火块继续登记“已注册”：221自然放置、同步和计划刻只是有限样本，未逐项操作18类；普通效果与爆燃的预置场景也不能证明火块接触或18材料自然命中全链。liandan/lianqi消费者已注册并随所属系统测试；异火本批未运行专用服务器，未执行1.18.2版本实机、两版本对照、完整视觉与多人。
