---
topic: yihuo
role: architecture
status: partial
updated: 2026-11-07
---

# 异火：来源规则与实现边界

本系统进行中，本批仅接回 skill 所需的炙烤持续效果与丹火火块，不代表异火主体或炉鼎生产已经完成。源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

归属范围为101个元素，2026-11-07全部完成归属阅读，逐分支实现与运行另记。B 的 `YihuoshujvfuyvProcedure:22–58` 从材料目录连续读取19条，以来源固定值 `jindumax = 6` 初始化材料辨识；该过程与 A 的 `FireskillProcedure` 武器触发尚未迁移。不从邻近重做工程继承“18种异火”的范围划分。

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

`FireHeatEvent` 和 `FireExhaustedEvent` 让生产系统消费自己的热量与刷新字段，异火不直接写其他系统的装置状态。**目前没有炉鼎监听者，实际炉鼎热量累计、保留火与生产流程仍待D阶段接入，不记为已迁移的完整供热链。** 火块台账保持“已注册”并写明已实现部分与缺口；两个效果和丹火物品按各自实际验证状态登记。

目标API核对版本为NeoForge 21.1.265，读取构建生成的sources jar：`BlockBehaviour`、`BlockEntity`、`RandomizableContainerBlockEntity`、`BaseContainerBlockEntity`、`RegisterCapabilitiesEvent`、`SidedInvWrapper`、`UseOnContext`、`MobEffectInstance`。更新包经完整更新标签包含持久数据，方块字段变更标脏并通知客户端。

## 保真约束

异火吞噬/联动等规则必须由本次 1.18.2 消费者证明，不从其他工程导入。

## 丹火调用链复核（2026-11-07）

本轮复核覆盖丹火召唤与自然 `useOn`、18种火块的放置/计划刻/接触、18个方块实体以及两个炙烤持续过程。18种方块逐类比较后，除身份外只有224/227的回退掉落、226/239的碰撞与发光属性不同，均已由内容表保留；18个方块实体除身份与名称外同模板。计划刻的18个分支已逐项核对：两族耗料单位均为1、2、4、8、16、32、64、128、256，扣料后才向上方方块实体加热，供热本身没有炉鼎标签门槛；仅耗尽后的保留判断要求上方属于 `danludinglu` 且热量至少为1。目标仍待D阶段装置所有者消费事件。

NeoForge 21.1.265 的 `BlockEntity.saveAdditional` 保存持久数据副本，目标先把当前共享友方视图写回再保存，因此保存标签与完整更新包包含保存时的友方表。`LivingEntity.tickEffects`、`removeEffect` 与 `MobEffectInstance.tick` 已核对，目标保持爆燃过程内立即移除两个效果，没有改成下一刻任务。自然放置的快照回放与燃料最终值沿用上文已经核实的两加载器依据。

本轮未发现上述已实现玩法需要新增修改。修正台账中18个火块方块实体仍为“待处理”的遗漏，改为“已注册”，依据为 `YihuoBlockEntities` 的独立类型注册及 `YihuoSetup` 装配；现有注册表导出不含 `block_entity_type`，不能把台账检查通过写成18个类型逐项实机验证。静态证据为 `tmp/yihuo-chain-source-audit.log` 和 `tmp/yihuo-family-audit.log`。本轮仅更新核对记录与台账，未新增或修改Test、未运行客户端或专用服务器；原专项失败和上文未验收范围继续保留。

## 未完成

材料辨识初始化已在 2026-11-08 通过 material 的 `Materials.initializeYihuo` 接入登录事件，实际目录与玩家附件仍由 material 所有；投掷物及命中、武器触发和完整获取消费链随异火主体继续。炉鼎所有者随 D 阶段接供热事件。1.18.2 版本实机、两版本对照、完整视觉与多人未执行。

## 本批验证（2026-11-07）

仅一次 `-PsmokeOnly=yihuo`：7项中2项通过、5项失败，报告虽有 `DONE`，不能记为专项通过。通过项为方块实体保存重读/方向零槽能力/共享友方与接触燃料，以及无炉鼎消费方时的耗尽移除。其余丹火燃料、客户端同步、计划刻守恒、普通炙烤伤害与阴阳爆燃尚未获得通过证据。

失败中的检查错误已定位：丹火放置后的燃料应为10，补7后的守恒基数应为17；检查牛的2000生命超过原生最大生命属性1024的上限。更正检查后 `build compileSmokeJava`（含 ArchUnit）通过，未重跑本专项或旧神通，不能补记为运行通过。负责人随后再次强调不写无必要的 Test、优先按1.18.2版本代码核对移植；本批新增的 `YihuoCheck` 及选择器入口已撤下，脚本备份于 `tmp/yihuo-check-withdrawn-20261107.java`，原失败报告保留。后续以源码核对和必要的实际验收推进，不以修检查脚本作为移植工作。

专用服务器一次启动至 `Done`，原生 `stop` 正常保存全部世界并退出，任务成功、ERROR为0。它证明当前注册与公共端加载能够完成，不证明炙烤伤害、丹火操作、完整供热链或视觉已验收。证据：`tmp/yihuo-report.txt`、`tmp/yihuo-smoke.log`、`tmp/yihuo-final-build.log`、`tmp/yihuo-server.log`。
