---
topic: guchong
role: architecture
status: draft
updated: 2026-11-22
---

# 蛊虫：架构调查与提案

## 璞玉奇遇105蛊虫接口（2026-11-22）

1.18.2 版本来源 A `Cailiao16102DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java:59–105` 在普通雕琢且奇遇命中时调用 `CailiaoyvshiqiyvProcedure`；潜行批量分支不调用。后者第301–309行在服务端创建04蛊虫，移动到玩家坐标，用世界随机源取朝向，再以独立 `Random` 抽取闭区间1–35的整数并以 `putDouble` 写入 `LEVELnumber`，然后按 `MOB_SUMMONED` 初始化并加入世界。

1.21.1 版本公开 `GuchongEncounters.spawnJadeEncounter(ServerPlayer)`：由蛊虫系统负责创建、等级写入和加入世界，外部不修改蛊虫持久字段。目标 NeoForge 21.1.265 的 `EventHooks.finalizeMobSpawn` 在出生事件未取消时调用实体 `finalizeSpawn`；`Guchong04Entity` 随后经 `GuchongStats.initialize` 读取预设等级计算经验上限与属性。事件取消时不能保证该初始化发生，是否加入世界由目标出生事件行为决定。材料侧调用已集成；受控璞玉奇遇105场景检查生成一只04和1–35级，见[材料月志](../material/logs/2026-11.md#2026-11-22-璞玉雕琢与奇遇)。

## 手持罐放虫与死亡清槽

1.18.2 版本来源 A：`item/Taoguan01itemItem.java:49`、`Taoguan02itemItem.java:49`、`Taoguan03itemItem.java:60` 的空气右键均调用 `procedures/Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java`。第 65–84 行逐槽先查非空实体 ID，再比当前玩家 UUID；只有非空他主槽显示拒绝。第 85–114 行沿用当前玩家队友关系，写罐 UUID 和原槽号，实体认主位置真、驯服给当前玩家并写槽内主人 UUID。第 146–162 行在生成分支外按获准槽计数。不同于蛊笛的 `procedures/GuchongAnniuzhixingProcedure.java:115`，后者读取槽内认主位。

死亡消费者：`entity/Guchong04Entity.java:213` 调用 `procedures/Guchong01DangShiTiBeiShaSiShiProcedure.java`。第 32 行以实体认主位为门槛；非看家虫在第 91–158 行查同维主人背包，按罐 UUID 找第一只匹配罐，清原编号槽并扣计数，最后一只死亡才解锁。主人离线、跨维或其背包中没有该罐时，来源没有补偿查找，本改动不额外回收。

1.21.1 版本集成 `3c2a8a63` 已有 `TaoguanItem.use` → `GuchongLifecycle.releaseOwnedJar` 和 `GuchongEntity.die` → `GuchongBehavior.died`。缺口在放出端：普通空槽也走主人拒绝；有主人残留的空槽可被误计数；有效槽的认主位若为假，原生主人虽已设置，死亡过程仍提前返回。修正手持入口，不放宽死亡门槛，不改变蛊笛读槽规则。

按项目已批准的系统边界实施；来源事实、已实现范围和待验证范围分别登记。源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

蛊罐由 A 的 `Taoguan01item`–`Taoguan03item` 与菜单、B 的 `Taoguan01`–`Taoguan03` 方块组成。菜单槽位为 1/4/9，只收 `forge:guchong`（`A:Taoguan01GUIMenu:89`）。物品右键方块时按字符串 ID 放置 `yvanchuxitong:taoguan_0x` 并写入玩家好友列表；方块右键手持精血时，若精血记录的 `playerlevelnum` 不低于蛊虫 `LEVELnumber`，写入 `guchongxvnfu`、`team` 与 `belongToMasterUUID`（`B:Taoguan01ZaiFangKuaiShangYouJianProcedure`）；方块 tick 按队伍与周围实体条件释放蛊虫（`B:Taoguan01GengXinYouXiKeProcedure`）。方块实体的 `createMenu` 返回原版 `ChestMenu` 占位。

`GuchongtickProcedure:27–106` 用 UUID/坐标关联目标与释放装置，回收时修改装置计数并移除实体。

## 目标所有权与接口提案

蛊虫装置拥有放出数量与槽，实体拥有个体状态，双方通过具体操作更新，不持有永久 Java 实体引用。

## 2026-11-12 地上罐与手持罐生命周期

2026-11-20 按来源修正三种放虫入口的状态恢复：手持罐和蛊笛读取双精度等级、经验及属性；地上罐对等级、经验和速度以外的属性按整数读取，速度仍为双精度。三者依次写入资质与数值，并在最大生命、攻击和速度写入后立即设置对应原生属性；当前生命只设置在实体生命值上，不写成个体持久字段。物品身份和名称不作为实体持久字段复制，队伍与友方关系仍经关系系统接口写入，地上坐标使用整数。来源为 A `Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:79–147`、`GuchongAnniuzhixingProcedure:83–153`，B `Taoguan01GengXinYouXiKeProcedure:98–167`。此修正已编译，实际释放、战斗和保存重进未验证；旧版实机及两版本对照未执行。

`TaoguanBlockEntity` 保存容量为1/4/9的蛊虫槽及末槽装饰物。地上交互保持来源顺序：精血认主、潜行取放装饰物、Ctrl/Alt收罐；方块下方 Material 使用 `legacy_material_grass/dirt/stone/wood` 标签。`GuchongJarRuntime.release` 每20刻按距罐中心排序检查实体，保留来源局部释放标志跨目标持续的行为；一旦触发，后续每个目标迭代均会遍历全部蛊槽，实体创建返回空时仍累加释放计数。罐关系友方标签由 `Relations.shareAllegianceToBlock` 从玩家共享到罐，再由 `Relations.shareBlockAllegiance` 共享给虫；虫自身 `team` 取蛊虫物品实例字段。来源地上释放不写 `guguanuuid`，目标保持。

手持罐右键放出全部归属本人且有编号的虫；再次右键按罐 UUID 收回半径8内匹配蛊虫，保存当前生命、属性、等级和经验并移除实体。潜行右键打开三档原生菜单；菜单开启从主手罐编号字段构建临时槽，关闭写回当前主手物品。放置地上罐只接受朝上且上方为空的方块使用，并按来源先主手后副手清除罐物品。UUID只在潜行开罐时为空才生成，8个字母每次取a–y，保留来源不取z的范围。

三档菜单均176×166，使用来源 `textures/screens/taoguan_01_gui.png`、`02`、`03` 原图。01槽为(79,35)；02槽为(70,26)、(88,26)、(70,44)、(88,44)；03槽为三行三列，从(61,17)每格18像素。玩家背包从(8,84)起、快捷栏从(8,142)起，蛊槽仅收蛊虫标签。来源屏幕没有按钮或文字标签，目标保持；网络菜单标题改用来源中文名称“小蛊罐 / 蛊罐 / 大蛊罐”，避免内部英文标题。

2026-11-20依据三档菜单共用的 `Taoguan01GUIDangZheGeJieMianBeiGuanBiProcedure:32–46` 修正关闭时身份：有身份的虫写入该身份；无身份的01/03/04写来源默认身份；其他无身份虫保留该编号槽既有身份，空槽则清空。上述菜单写入与地上罐整数路径独立，尚未完成实际交互和重进验证。

保持的来源差异：菜单开启不读回 `health_Num`；关闭存入当前主手罐，不把原罐Java引用固定为所有者。来源系统 `GuchongshuxingProcedure` 以完整registry字符串与裸路径比较三种虫物品，导致资质分支无法命中；初始虫物品可能没有 `guchongid`，地上罐放虫因此断链，源码未擅自修正。其随机team文本在同次槽循环持续累加也保持。编号03/04的自然初始化、十三项资质、跟随开关、吞噬条件、目标、死亡回调与Gecko模型已逐条读取，实体注册与完整玩法验收分别登记。

精确来源：A `item/Taoguan01itemItem.java`–`03itemItem.java`、`world/inventory/Taoguan01GUIMenu.java`–`03GUIMenu.java`、`client/gui/Taoguan01GUIScreen.java`–`03GUIScreen.java`、`procedures/Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java`、`Taoguan01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure.java`、`TaoguanGuiopenProcedure.java`、`Taoguan01GUIDangZheGeJieMianBeiGuanBiProcedure.java`、`GuchongtickProcedure.java`、`Guchong01DangShiTiBeiShaSiShiProcedure.java`；B `procedures/Taoguan01GengXinYouXiKeProcedure.java`、`Taoguan01ZaiFangKuaiShangYouJianProcedure.java`、`GuchongshuxingProcedure.java`。路径前缀见[系统导航](../README.md)。

2026-11-13收纳批次已通过统一编译、架构检查及一级手持罐菜单/序列化保存的最小客户端单项，见下节。后续03/04与九槽罐第九槽专项补充自然生命周期及活动虫/背包罐真实退出重进证据，范围见本页末节；其他容量全部自然操作、完整存档/多人、专服专项、1.18.2版本实机与两版本对照仍未执行。

## 2026-11-14 05荧光蛊与悬赏追踪

05由本系统拥有实体和物品生命周期，悬赏仅经`GuchongTracking.createBound`交入猎人UUID及目标坐标。普通`guchong_05item`右键在三格视线射线的方块坐标释放，忽略流体、使用方块OUTLINE，随机水平朝向，随后消耗一只；物品没有追踪字段时不补目标或主人。释放/回收仅复制`zhuizhong`、`belongToMasterUUID`、三项`zuobiao`、`guchongxvnfu`、`kanjia`和三项`homeblock`，两个计时器不存入回收物品。实体完整实例状态随原生实体NBT保存。

来源实体为无重力持久飞行`PathfinderMob`，随机漫步目标每20刻取自身XYZ各±16，另有随机看向；没有攻击或仇恨目标。基础生命20、护甲0、攻击3、跟随范围16，移动/飞行速度均0.3。箭、摔落、仙人掌和铁砧伤害免疫，无受伤/死亡音效。A `Guchong05Entity.init`为空，未添加自然生成规则；实体右键没有主人过滤，任意玩家可获得保存字段的物品，随后移除实体。

每刻发一颗`guchong_05lizi`；追踪状态下看向目标并按视线×0.2移动，每5刻向同维度在线主人显示本人至目标的三维整数距离。第100刻先清计时器、发8颗`yaoshou_040skill_03`，在当前维度玩家中按UUID交还虫物品，随后移除实体。来源渲染为`Modelguchong05`加透明`kong.png`、阴影0，目标使用不可见实体渲染与来源粒子表达，没有补造可见虫体。

### Q87 同维主人缺席仍移除

确认来源顺序，当前保留。A `Guchong05DangShiTiGengXinKeShiProcedure:32–57`只遍历当前世界玩家，交还循环后无条件移除；主人离线或跨维度时该次物品不会交还。目标不增加跨维查找、等待或补发。该生命周期静态已核，离线/跨维与存档实机未执行。

### Q91 节肢类别的核对边界

A `Guchong05Entity:79–82`明确返回`MobType.ARTHROPOD`，目标05需加入`minecraft:arthropod`实体类型标签，另保留原`guchongentity`身份。早先将01/03一并视作遗漏的假设经来源核对撤销：两者来源为`UNDEFINED`，不能以“蛊虫”名称补成节肢类别。该结论只约束本次05增量，不扩展到未逐条核对的其他编号；标签转换已完成；节肢伤害效果仍未实机验证。

精确依据：A `entity/Guchong05Entity.java:45–163`、`client/renderer/Guchong05Renderer.java:11–18`、`item/Guchong05itemItem.java`、`procedures/Guchong05itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java:18–38`、`Guchong05DangYouJianDianJiShiTiProcedure.java:16–33`与`Guchong05DangShiTiGengXinKeShiProcedure.java:26–70`。05实体候选归属已完成该完整类、表现、物品和过程调用链阅读，已同步公共归属清单并完成定向转换/编译/架构检查。新增`guchong05` Smoke源码覆盖普通与悬赏绑定物品右键、中途实体真实退出重进、追踪计时续行、同维百刻返还及回收物计时器边界；回收路径使用与当前玩家不同的主人UUID检查无身份过滤，并检查05粒子类型可由公开接口读取。2026-11-15修正退出等待计时竞争后，该Smoke11项通过，真实中途重进与百刻返还均已到达；证据及首次失败边界见本月记录。自然悬赏领取/补领、第二客户端真实回收交互、主人失联/跨维时行为、粒子视觉和节肢伤害仍未实机验证。

### Q180 荧光蛊释放扣件的端侧边界

A `Guchong05itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java:17–38`的`itemstack.shrink(1)`在服务端生成分支之后，语法上客户端也执行；目标`GuchongTrackingItem`仅在`ServerLevel`中生成实体并扣件。当前保留服务端真值与数据组件同步边界，不把扣件移到客户端。两版本的预测、回包和创造模式实际结果尚未对照，本项为待统一复核的端侧差异，不据此宣称目标缺陷。

## 待调查

虫卵Chongluan已完成物品、Java/配方和药锄生产者核对，静态物品由现有内容表承担；来源培养入口断链见Q163。蛊笛释放界面与按钮已核对并接线，范围与未验边界见下方蛊笛正文。

## 2026-11-13 摄虫网收纳与蛊罐消费者

R05/R10的外部收纳缺口由`GuchongCapture.captureWild`承担：`equipment`仅传入当前服务端世界、目标实体和法宝主人UUID，不读取或写入蛊虫持久字段。操作先检查`guchongxvnfu`，已认主时返回对应结果，法宝按来源在网的位置广播怒粒子；未认主时，将目标实体完整注册ID加`item`后以英文小写查物品，无对应物品则跳过。来源没有虫标签、存活、节肢类别或所有者过滤，目标保持，不将其收窄为仅本系统两种实体。

找到对应物品后写入实体ID、等级名/数值、经验上限/当前经验、队伍、十三项资质和对应属性；`health_Num`读取当前生物生命，非生物为-1。名称删除方括号后保存为物品自定义名称，认主标记为false；不复制整个实体NBT或附加来源未写入的归属字段。由本系统通过物品组件写回；法宝主人在当前世界且为玩家时交给玩家，库存满时沿原生`ItemHandlerHelper.giveItemToPlayer`掉落。来源在主人缺席时仍移除可捕获目标，目标保持该顺序。

所得虫物品已携带`guchongid`，可直接进入三档蛊罐的既有标签槽，关闭时由`TaoguanMenu`保存编号槽，罐物品序列化仍由原生组件承担。重新开罐继续保持来源不读回`health_Num`的差异，不能以捕获保存当前生命为理由改写这条菜单规则。地上罐精血认主、释放、回罐、死亡清槽仍沿用现有实现，本次不宣称这些完整自然路径已验收。

来源依据：A `procedures/Fabao01701DangShiTiGengXinKeShiProcedure.java`的摄虫网分支；具体法宝入口、时序、模型见[装备正文](../equipment/architecture.md#摄虫网与困虫兜2026-11-13)。本次最小客户端单项已通过普通物品入口、活动实体和捕获物品序列化重读、认主虫排除、真实捕获物品入罐/关罐/重开，以及同物品困虫兜门槛和时序；完整结果只登记[装备月志](../equipment/logs/2026-11.md#2026-11-13摄虫网收纳与蛊罐保存)。完整地上罐链、多人、重登、1.18.2版本实机与两版本对照仍未执行。

### Q233 摄虫网保存捕获时的显示名

1.18.2 版本 A `Fabao01701DangShiTiGengXinKeShiProcedure.java:78–80` 将实体显示名解析为字符串，去方括号后写为物品自定义名。B `Taoguan01ZaiFangKuaiShangYouJianProcedure.java:77` 直接把槽内物品的显示名拼接进认主消息。1.21.1 版本由 `GuchongCapture.captureWild` 与 `GuchongJarRuntime` 承接这两处行为。用默认英文客户端采集的物品留有字面名，之后切换到简体中文仍显示 `[Guchong 0 3]认主成功！`。最终简中资源的飞蝗实体、物品及精血键均存在。保持来源的自定义名持久化。全程简中从新捕获开始的动态显示及 1.18.2 版本实机待验。

### Q234 普通动物死亡后的守罐目标

1.18.2 版本 B `Taoguan01GengXinYouXiKeProcedure` 放虫时写入目标 UUID；A `GuchongtickProcedure.java:27–58` 按 UUID 移交原生攻击目标并清空指令，只在原生目标引用为空时回罐。1.21.1 版本由 `GuchongJarRuntime` 与 `GuchongBehavior` 承接。两版原生 `MeleeAttackGoal.stop()` 均不会仅因普通羊死亡而清掉羊的目标引用。1.21.1 版本实测在羊移除后 40 刻内引用仍在，活动数和原槽未变；不新增主动清除死目标的逻辑。实际结果见[蛊虫月志](logs/2026-11.md#2026-11-19-实捕飞蝗原生近战与战后罐状态)。

## 2026-11-13 03/04单虫释放与蛊罐生命周期

03/04物品此前仍是普通工厂，来源的`useOn`没有执行者；现由`GuchongReleaseItem`承担。只有点击顶面且上方为空才在该格中心释放一只对应蛊虫并消耗物品。未认主物品只生成并执行该编号的自然初始化，不把已捕获资质带回野生实体；已认主物品先写等级、十三项资质及其数值、显示名，设置当前生命和原生生命/攻击/移动属性，再认当前使用者为原生主人，团队通过`Relations.team/setTeam`承接。最后执行来源初始化与加入世界；等级名非空时保持已有个体值。

来源已认主单虫分支不读取物品的原主人，不写`belongToMasterUUID`、经验、`guchongid`或蛊罐关联字段，名称也不去方括号；目标保持这些遗漏及顺序，不套用罐释放的完整搬运方法。01物品复用同一来源入口；编号01行为由本页R10专项接入，03/04入口保持既有边界。

同批修正收纳团队数据来源：1.18.2版本直接读取实体NBT的`team`，目标实体初始化已通过`Relations.setTeam`写relation附件，旧收纳代码读取持久标签会保存空值。`GuchongCapture`现用`Relations.entityTeam`查询，由蛊虫系统继续唯一写入捕获物品组件。没有改变来源团队语义或新增跨系统写入。

静态依据：A `item/Guchong03itemItem.java`、`Guchong04itemItem.java`和`procedures/Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure.java`的03/04两分支；`typed_field_accesses.csv`列出的三处玩家团队读取中，本批实现后两处。罐放置、精血认主、自然计划刻、回罐及死亡过程复用上一批已核来源，仅补未覆盖的运行风险；现有三档罐实现不重写。

首次原生放罐暴露三档内容表仍选普通`block`工厂，已有`TaoguanBlock`和方块实体没有实际装配；现补齐三档工厂覆盖及转换输入，清除已经实现的方块/罐物品待迁移钩子。`TaoguanBlock`同时恢复B `block/Taoguan01Block.java`同族的菜单提供者与方块事件转发，保留原版占位容器；没有绕过原生物品放置钩子或修改来源放置规则。

必要单项`-PsmokeOnly=guchonglifecycle`已通过03/04物品入口与团队承接、九槽罐第九槽的放置/精血认主、地上罐序列化重读、自然计划刻放虫/回罐、地上死亡清槽、Ctrl收罐、手持释放/收回，以及活动虫和非手持背包罐真实退出重进后的死亡清槽与另一罐隔离。地上回罐不回写当前生命，手持收回保存生命与经验，分别保持来源行为。地上罐保存检查是完整方块实体序列化重读，真实退出重进发生在活动虫与背包罐阶段，不宣称地上罐原位重登。夹具固定虫和目标位置，服务端使用原生物品/方块入口；完整客户端按键、AI追击战斗、其他两档罐全部自然场景、多人和两版本对照未执行。失败、修复、通过证据与验证边界只登记[本月记录](logs/2026-11.md#2026-11-13-0304单虫入口与生命周期续批)。

## 2026-11-13 编号01完整来源包

A `Guchong01Entity` 注册为`MONSTER`，尺寸0.5×0.8，跟踪范围64、更新间隔3，基础移速0.1、生命10、护甲0、攻击3、跟随范围16。实体只有主人跟随与随机漫步目标；不注册近战、仇恨、玩家或反击目标。来源没有01自然生成位置规则，目标不添加出生规则。

来源每19刻将以自身为中心、膨胀4格的实体按距离排序，遍历存活`Mob`（自身除外）：前一只Mob非空、不同于当前Mob且其`attackmubiao`为空时，写入前一只Mob的UUID并设为原生目标；每次遍历都推进鼓上蚤自身的`linshiuuid`。不加阵营筛选，不覆盖已有标记；来源共享虫tick每20刻维护`guchongtick2`，在被地上罐释放时累计至5后回罐，即约100刻，即使原生目标仍存活。每77刻播放`guchong01`中性音效。来源死亡移除时间2刻，动画控制器过渡4刻；渲染资源、阴影0.4、比例1和死亡旋转0按`entity_models.json`登记。

来源初始化为7级、`二阶低级`；十三项资质依次为生命上限平庸、生命恢复/灵力上限与恢复/神识上限与恢复贫弱、攻击数值贫弱、攻击防御勉强、其余资质贫弱。现有蛊虫公式得到生命148、经验上限1570、攻击4、移速0.135。来源`init()`为空，目标不增加自然生成位置；本批接回九个`elm_01`合成的`guchong_01item`及既有摄虫网捕获入口，顶面放置执行野生初始化。已认主单虫入口恢复物品等级、资质和生命，认当前使用者并写团队；来源不复制物品原主人、经验、蛊罐关联字段，本实现保留该遗漏。

蛊虫击杀成长是由Creature数据所有者消费的公开事件。`GuchongVictimDataEvent`同步读取死者`LEVELnumber`与`additional_experience`供成长公式使用；同类型击杀增加死者经验上限四分之一，否则等级至少1时按`level^2.4 + level*4 + 5 + additional_experience`，无等级Mob按死者生命上限五分之一。经验可连续升级，每级扣除旧上限、等级加一、上限增加`newLevel^2*10+10`；每次升级通过`Professions.addExperience`给同维度在线主人增加`newLevel*10`养蛊经验。升级反馈播放`littlelevelup`、发20个`WAX_ON`并更新等级名。

来源保留一个有明确实例选择的分支：升级后按死者`yaoshou`标签决定属性重算；命中时重算死者妖兽属性，否则重算升级蛊虫属性。`Creature`在`GuchongUpgradeEvent`订阅处理，标签命中后只写Creature拥有的死者属性；蛊虫资质和个体数据仍由Guchong写入。玩家经验由profession公开操作写入。A `EntityDeathProcedure:234–254`在扣除旧经验并调用`EntitylevelupProcedure:12–16`增加等级/上限后，按当前维度和主人UUID查询实体；非空UUID查询不到主人时，`getCapability`空实体解引用使流程在`jisuan=true`前中断，后续连续升级、等级名和音效/粒子及属性重算均不执行。目标保留相同顺序，向profession公开操作传递查找结果，未增加跳过发放并继续结算的兜底；该异常分支只完成静态核对，未运行游戏或视为已验收。

实体、单虫物品、资源、`guchongentity`标签与九榆木配方已接回；`Guchong01Check`已通过自然挑衅、认主释放、七级精血门槛、罐释放与百刻回罐、跟随、击杀成长及真实退出重进，并以03/04各一例确认共同死亡消费者。默认768 MB、单worker下产品与夹具编译、六条结构检查及必要单项通过；转换失败与定向恢复、原始报告和截图范围只登记[本月记录](logs/2026-11.md#2026-11-13-编号01完整来源包)。编号01完整表现、多人、专服和两版本对照尚未验收；其余编号的完整战斗、吞噬与成长仍开放。

03/04既有表现元数据待核：A `Guchong03Renderer`与`Guchong04Renderer`均未覆写死亡旋转，而当前两行`rendering.death_rotation`显式为0；A `Guchong04Entity`的movement动画过渡为4刻，当前表为2刻。03来源通过`hurt`排除摔落伤害，但未覆写`causeFallDamage`；当前表同时声明`ignores_fall_damage=true`与`damage_immunities=[fall]`，提取器按方法分别推导的前者为false。这些是本次只读逐字段核对发现的历史差异，需在后续R10表现包核实原生渲染/伤害调用与视觉效果；现有03/04实现和历史验收状态保持其原边界。

## 2026-11-14 蛊笛六罐释放与潜行收回

`GuchongFluteItem`承接A `Fabao018Item`的普通右键与潜行右键；沉默`effect_002`阻断两种操作并提示“§4沉默状态下，无法释放或收回蛊虫。”。物品不消耗、不设冷却，并保持来源`super.use`的返回结果。既有`fabao_018hecheng`配方已按来源转换：两根竹子与`cailiao_181`沿右上至左下的三格对角线合成一支蛊笛，没有新增获取方式。

普通右键打开原注册身份`guchongshifang_gui`，`GuchongReleaseMenu`六格只读槽按玩家物品能力顺序复制`taoguan`标签罐，每份数量1。目标能力保持主库存、护甲、副手的组合视图；没有把显示副本当成实际库存。来源开窗数据只传玩家坐标，目标保留该坐标方块物品能力存在时的绑定；没有调用入口的手持物品/实体绑定模板不引入。展示槽不能放入、取出或快捷转移，玩家背包与快捷栏仍能相互转移；关闭、死亡与断开连接不交还或掉落六份副本。

页码`pagenum`、选中序号`anniunum`和六个提示`xianshiweizhi0`–`5`均保存在当前主手组件，副手触发也仍使用当前主手。`populate`先清空六格及六个提示，实际页码低于1时仅取展示范围按第一页处理，不回写1；界面按实际`pagenum`以`DecimalFormat("0")`显示，因此首次可显示0而展示第一页。按钮0在实际页小于`ceil(罐数/6)`时加1，否则设1；按钮1在实际页大于总页数时减1，否则设总页数，保持来源`Guchonguiup`的条件。没有罐时两箭头不修改实际页码，但仍重建空展示。后端只接受当前容器号、有效主手蛊笛、已加载开窗坐标与0–7按钮号；客户端只发请求，不反写保存真值。

按钮2–7写入选中序号0–5，以展示罐`guguanuuid`匹配玩家能力中的真实罐，空UUID的相等比较同样参与。释放容量按小蛊罐1、中蛊罐4、其余标签罐9；逐编号只检查保存的主人UUID等于玩家UUID，不增加`guchongid`非空判断。实体出生于玩家Y+1，朝玩家朝向，初速度为玩家视线向量加Y轴1；恢复等级、经验、资质、数值、名称、原主人和保存的认主标记，原生可驯服实体认当前玩家，团队与友方表由`Relations.shareEntityAllegiance`承接。生命、攻击、移速恢复后按`MOB_SUMMONED`初始化并加入世界。创建返回空仍计数；成功计数达到1后真实罐保存释放状态和数量，并同步选中展示罐及“§6收回蛊虫”提示。

按钮收回只遍历以玩家为中心膨胀8格的AABB、按距离排序的01/03/04三类实体，只比较罐UUID，不增加主人条件。每只把等级、经验、十三项资质/数值、主人、当前生命及玩家团队写到本次罐副本并减计数，随即移除实体。只有本次计数低于1才将释放状态改为false、计数设0、写回真实库存和展示副本，并改为“§a释放蛊虫”；附近只有部分虫时，已移除虫的副本修改不写回真实罐，保持来源的局部收回结果。完成收回只结束当前实体遍历，随后仍沿来源顺序检查其他同UUID罐；正常UUID不重复时只影响选中罐。

潜行右键沿相同AABB/距离顺序遍历`guchongentity`标签实体，要求保存主人UUID等于玩家UUID；逐虫扫描真实玩家能力，取第一个相同罐UUID的标签罐。每只保存相同个体字段和当前生命、使用玩家团队、减计数并立即写回罐；数量低于1只关闭释放标记，不强制把负数归零。标签实体非生物时当前生命按来源记-1。蛊笛入口的沉默门槛不延伸到已打开界面的按钮，来源按钮没有这一门槛。当前主手改为其他物品时按来源每刻关闭菜单。

界面保持176×168，背景`guchongshifang_gui`及(0,-40)的176×124原图`yanggugui01`；两列罐槽X为25/97、Y为-22/5/32，六按钮位于X22/94、Y-25/2/29，尺寸62×22，使用原双态贴图。提示坐标X46/118、Y-17/10/37，页码(85,72)，两箭头(6,62)/(132,62)尺寸36×15；背包Y85起、快捷栏Y143。按默认shadcn技能的适用边界提供按钮中文朗读与键盘焦点，画面沿用Minecraft原生菜单和来源资产。蛊笛使用既有`builtin/entity`显示变换、`fabao018`模型与空闲动画、转换后的`textures/item/fabao018.png`和来源半透明渲染；首次完成渲染后启用空闲控制器，无调用者的过程动画模板不形成新玩法。

静态依据：A `item/Fabao018Item.java`、`item/model/Fabao018ItemModel.java`、`item/renderer/Fabao018ItemRenderer.java`、`world/inventory/GuchongshifangGUIMenu.java`、`client/gui/GuchongshifangGUIScreen.java`、`network/GuchongshifangGUIButtonMessage.java`；过程`Fabao018DangYouJianDianJiKongQiShiShiTiDeWeiZhi`、`GuchongshifangOpen`、`GuchongshifangGUIDangZheGeJieMianDaKaiQiJianMeiKeFaSheng`、`GuchongAnniuzhixing`、`Guchonguidown/up`、`Guchonganniu01`–`06`、`Guchonganniuxianshi01`–`06`、`Guchongyeshu`，以及`data/yvanchuxiuzhen/recipes/fabao_018hecheng.json`。目标能力与槽写入核对NeoForge21.1.265精确sources.jar的`Capabilities`、`PlayerInvWrapper`和`SlotItemHandler`；菜单网络与Gecko生命周期复用工程既有同版本入口。

本包已完成来源静态核对、定向资源转换、统一编译与结构检查；最小`guchongflute`客户端单项通过原生物品入口和真实按钮、七罐分页、受控03双虫放收、物品移动后的UUID定位、只读展示、沉默边界、空UUID和局部收回，以及真实保存重进。三张截图确认中文提示、页0/2与六罐/一罐显示；854×480、界面缩放2时来源顶部偏移造成上边框略裁剪，保持原布局。原件与运行边界只登记[本月记录](logs/2026-11.md#2026-11-14-蛊笛六罐释放与潜行收回)。全容量、全部编号、完整自然战斗、生存获取、1.18.2版本实机、两版本对照、专服、多人和完整视觉/音效未执行；本包不升级整个R10或蛊虫系统验收状态。

## 酒蛊02、虫卵与吞噬边界（2026-11-15）

`guchong_02item`自然右键读取体质tizhiopen_013。首次经`Cultivation.editTizhi`一次写入开启、AlcoholPatience增加100、AlcoholDigestionPercentage减1、tizhinumber加1，并提示获得千杯不倒；已开启只显示重复提示。来源use返回super结果，过程直接shrink；MC1.18.2原生`ServerPlayerGameMode`的`m_6261_`在调用物品后以`m_9295_()`判断创造并恢复数量（MCP `1.18.2-20220404.173914/joined/decompile/output.jar`内该类261–295行），Forge40.1.0的同类补丁只在入口加入右键事件、没有删改恢复分支。NeoForge21.1.265来源包内1.21.1的`useItem:306–338`已移除该恢复。目标酒蛊显式跳过创造扣件，生存首次扣1；这是保留1.18.2版本自然入口结果的适配。两个版本均可能在数量、耐久和使用时长未变化时提前返回，必要复验经`player.gameMode.useItem`进入，不以直接调用物品`use`替代。修复已实现、静态核对通过，修复后的客户端单项未执行，见[本月记录](logs/2026-11.md#2026-11-15-酒蛊创造模式数量修复)。

`huxiangtunshi`没有额外自定义tick，Guchongtunshi只检查效果；03/04目标与继续条件已经消费，04依来源仍吃03，共享同类击杀经验为死者经验上限四分之一。虫噬草效果生产配置已在Liandan herbs表，完整自然制药/吞噬战斗仍未验，不能把未验写成缺少一套吞噬系统。

### Q163 虫卵培养说明没有已发现的可执行入口

A ChongluanItem只有64堆叠与说明，无use/useOn/tick；A/B Java与resources/data未找到虫卵孵化、虫卵到成虫或02生产配方。已知虫卵生产者为Equipment采药锄，本批接回；培养说明与实际消费者之间仍断链。保持普通物品身份，不补造孵化系统；来源动态入口继续集中核实。

## 三档地上蛊罐原位重进（2026-11-18）

现有九槽生命周期证据中，地上罐只经`saveWithFullMetadata`/`BlockEntity.loadStatic`序列化重读，真实退出重进发生在活动虫与背包罐阶段；不能据此登记三档地上罐原位重登已通过。本单项只补这一持久库存风险，不重跑完整AI战斗。

B `Taoguan01/02/03BlockEntity`分别持有2/5/10槽：1/4/9个蛊虫位与独立末槽装饰物，`load/saveAdditional`通过`ContainerHelper`保存全部库存，`getUpdateTag`使用完整元数据。A `Taoguan01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure`经物品顶面使用移交编号槽和罐UUID；B `Taoguan01ZaiFangKuaiShangYouJianProcedure:36–44`按1/4/9槽认主与收纳，210–243行收罐把等级、经验、生命和战斗数值按`getInt/putInt`搬运，速度仍使用Double。收回再放置后这些来源整数读取字段应截掉小数，不能误设为完整小数保存。目标已有`TaoguanItem`、`TaoguanBlockEntity`、`GuchongJarRuntime`实现，本单项未改生产规则。

`GuchongGroundSaveCheck`使用三档满槽、各槽不同的01/03/04编号、名称、经验、资质与数值，经原生物品放罐、一次精血认满全部槽、放入末槽装饰物后真正退出并重进同一存档；检查重新创建的方块实体、全槽组件、UUID/阵营与友方数据、末槽装饰物和客户端同步。随后以原生潜行取装饰物、Ctrl收罐及再次放置检查数量、槽次序和整数截断；速度保留小数。场地位于高处并使用草方块罐底以隔开自然目标；没有受控或自然放虫、追击、攻击或掉落样本。

`guchonggroundsave`必要客户端取得66 INFO、0 FAIL、DONE和Gradle 0；实际日志确认同档两次登录、两次全维度保存。上述满槽、原位重进、双端同步及收罐再放置断言通过，生产代码未改。854×480截图已查看，三档外形可见；画面没有菜单或名称，不作为中文界面或完整模型对照验收。两份最终合并中文的58个Guchong键逐值一致。来源、原始报告和限制见[本月记录](logs/2026-11.md#2026-11-18-三档地上蛊罐原位重进)。完整自然放虫、战斗/掉落、多人、专服、1.18.2版本实机及两版本对照未执行。

## 地上罐名称与主人经验来源边界（2026-11-20）

1.18.2版本 B `Taoguan01GengXinYouXiKeProcedure:111–113` 在地上罐计划刻放虫时，先对空字符串调用 `replace(虫名, "[")`，再对空字符串调用 `replace(前次结果, "]")` 并作为自定义名称；非空虫名与空虫名的结果不同。1.21.1版本 `GuchongLifecycle` 已恢复同样的表达式；受控地上放虫的空名和非空名分别实测为空串及 `]`，详见[放虫字段记录](logs/2026-11.md#2026-11-22-跨入口放虫字段与地上罐名称)。手持罐和蛊笛入口不得套用此规则。来源是否意欲去除括号待集中复核，未执行两版本实机。

1.18.2版本 A `EntityDeathProcedure:224–266` 在蛊虫逐级升级、主人UUID非空时，从当前 `ServerLevel` 根据UUID查实体后直接访问玩家能力；找不到或无法解析UUID可能得到空值并中断。1.21.1版本 `GuchongGrowth` 当前也按当前世界查找并向 `Professions.addExperience` 传递结果，主人缺席时可能抛出异常。这里仅登记静态来源与当前路径，不扩大来源语义或推断实际触发频率；自然战斗、离线及跨维度情形均未实测。

## 摄虫网与追踪虫交叉入口（2026-11-20）

1.18.2版本 A `Fabao01701DangShiTiGengXinKeShiProcedure:64–130` 对范围内 `guchongxvnfu` 为假的实体，按实体完整注册名加 `item` 查询对应物品，不限定01/03/04。来源实体 `guchong_05` 和物品 `guchong_05item` 符合该命名，但复制清单只有普通蛊虫属性，不复制 `zhuizhong`、`zuobiaoX/Y/Z` 或追踪主人；认主标记为真的实体先被拒绝。1.21.1版本 `GuchongCapture.captureWild` 按相同命名检索并复制同类字段，具体可达场景仍取决于实体状态。自然悬赏链及摄虫网交叉捕获均未实机验证，不以命名关系断言某次悬赏虫一定可捕获。


## 妖兽与蛊虫击杀成长（2026-11-20）

锁定1.18.2版本 `EntityDeathProcedure` 对玩家、妖兽标签、蛊虫标签依次选择互斥击杀分支。妖兽分支按死者等级/附加经验或原版生物生命计算收益，处理来源效果倍率并逐级升级；同时带蛊虫标签的击杀者每升一级也向主人增加养蛊职业经验。蛊虫分支同种击杀按死者经验上限四分之一计算，其他击杀使用相同死者经验基础，升级后按来源死者妖兽标签选择属性重算对象。妖兽升级移速资质基值与出生计算不同；最大生命、回血与攻击等属性按来源顺序写入。

主人查找使用当前服务端维度，非空UUID无法查到实体时来源中断的边界保持；非玩家或假玩家没有来源玩家能力，不写职业经验。跨维失联、外部标签实体、真实战斗升级、死亡/保存重进及两版本实机对照尚未运行验证。

## 蛊虫实体与单虫释放共享生命周期（2026-11-20）

对照1.18.2版本 `Guchong01/03/04Entity`、三个出生过程与 `Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure`：01、03、04在原生 `aiStep` 之后更新挥臂；03和04吞噬目标先执行原生目标判断再判定效果；04游荡、观察和浮水优先级为8、9、10。出生先计算经验上限、再更新等级显示、计算属性并设置团队。已认主单虫物品恢复生命资质时立即设置原生最大生命和当前生命，恢复攻击资质时立即设置攻击；未认主分支保持来源字段边界。

`Guchong05Entity` 来源的落地回调为空；手持罐首次生成八字母标识时每次取字母使用独立随机源，范围仍为a至y。上述为源码核对与目标实现，不代表自然战斗、飞行、认主释放、罐交互或保存重进已实机验收；Gecko过程动画控制器差异未包含本批。

## 地上罐与蛊笛放虫关系次序（2026-11-20）

对照1.18.2版本原初系统 `Taoguan01GengXinYouXiKeProcedure`，地上罐放虫先共享罐好友表，再写等级名、槽号、等级与经验并设置名称和原生主人；之后设置守罐、认主、主人UUID、虫物品团队、坐标及属性，最后执行出生。蛊笛按原初修真 `GuchongAnniuzhixingProcedure` 在驯服及主人UUID赋值后再次读取并写入玩家团队。实机放虫、好友变化、保存重进及多人均未验证。


## 三档罐虫物品重建与方块掉落（2026-11-20）

对照1.18.2版本 A `TaoguanGuiopenProcedure:39–86` 和 `Taoguan01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:74–124`：菜单及放置地上罐均以编号槽的 `guchongname` 设置重建物品的显示名，不写入物品的 `guchongname` 自定义字段；菜单独有 `guguanuuid` 且不复制 `health_Num`，放置地上罐则复制 `health_Num` 而不写槽位 UUID。1.21.1版本 `GuchongJarData.unpack` 按这两条路径重建字段。地上罐计划刻 B `Taoguan01GengXinYouXiKeProcedure:110–112` 的空字符串替换表达式仍按来源执行，重建物品缺省名称字段与直接插入自带名称字段物品的结果可能不同。

来源 B `Taoguan01Block`、`Taoguan02Block`、`Taoguan03Block` 的 `getDrops` 先取父类战利品，空时返回空气物品；1.21.1版本三档共用方块调用父类战利品，空结果不产生掉落。目标资源当前无三档罐方块战利品表，额外数据包战利品及实际破坏尚未验证；方块实体库存掉落接线保持不变。自然获取、混合罐操作、战斗成长及同实例保存恢复未作本批验收。

静音客户端 `guchonglifecycle` 中，01/03/04各两项受控重建字段检查通过；已有场景包含受控放虫、回罐及一次实际退出重进。此检查没有运行自定义数据包战利品或自然获取，不能据此推断完整混养验收。日志和范围见[月志](logs/2026-11.md#2026-11-20-三档蛊罐重建与战利品)。


## 蛊虫跨入口共享生命周期族（2026-11-20）

`GuchongLifecycle` 统一承载来源单虫物品释放、手持蛊罐/蛊笛释放和地上蛊罐释放的字段恢复、主人/团队、位置/朝向、属性应用与出生收尾；各入口仅保留来源入口字段差异。`GuchongJarData` 的既有名称、槽位UUID、生命值和菜单/地上罐差异继续保留。新增荧光蛊模型/渲染注册。

本批保留既有真实来源保护与字段边界；生产/Smoke编译通过，未执行实际放虫、自然战斗、完整死亡掉落或保存重进。

## 飞蝗与黑蚁的自然战斗目标

1.18.2 版本 `Guchong03Entity.registerGoals`、`Guchong04Entity.registerGoals` 均先登记 4 号玩家目标、5 号吞噬目标，再登记 7 号受伤反击；目标选择器与动作选择器保持独立。5 号对两种蛊虫都找飞蝗，并在原生开始/继续判定之后检查行动者的 `huxiangtunshi`。仅飞蝗在空闲动作前登记 8 号跃击；黑蚁的 8 号直接是游荡。目标版本 `GoalSelector` 按插入顺序尝试启动，数字优先级不能代替插入顺序。

人间道出生名单和权重见[来源台账](../creature/ledger/renjian-ecology-source.csv)。不改变出生资质、杀敌经验、罐内存档或死亡清槽；猎物是飞蝗不等于黑蚁击杀飞蝗有同种经验。真实自然吞噬与战斗后收回仍待验。

## 地上罐生命恢复与实体死亡经验（2026-11-22）

1.18.2版本原初系统 `Taoguan01GengXinYouXiKeProcedure.java:99–167` 从地上罐虫物品读取 `health_Num` 时使用 `getInt`，其他生命、资质数值也按整数读取；速度仍使用双精度。手持罐及蛊笛的 `Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`、`GuchongAnniuzhixingProcedure` 按双精度读取当前生命。地上罐放虫现恢复这一区别，回罐仍只减少数量并移除实体，不回写槽内生命或成长。

1.18.2版本 `Guchong01Entity`、`Guchong03Entity`、`Guchong04Entity` 的死亡刻调用无参 `dropExperience()`；1.21.1版本 NeoForge 21.1.265 的 `LivingEntity.die` 已在 `dropAllDeathLoot` 调用 `dropExperience(damageSource.getEntity())`。目标共享死亡刻只按原定时间移除，不再重复调用经验掉落；三虫当前默认经验值为零，额外调用仍会重复经过经验事件。死亡清槽、放虫、生命恢复及掉落的游戏行为未因静态核对而视为验收。

## 跨入口放虫字段与地上罐名称（2026-11-22）

1.18.2 版本单虫 `Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:40–69`、手持罐 `Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure`、蛊笛 `GuchongAnniuzhixingProcedure` 和地上罐 `Taoguan01GengXinYouXiKeProcedure:132–167` 按资质字段顺序写入，写入生命上限后立即设置原生上限和当前生命，写入攻击后立即设置原生攻击，最后写速度并设置原生速度；当前生命直接设置，不写入实体自由字段。地上罐数值按整数读取、速度按双精度，单虫和手持罐/蛊笛按双精度读取。1.21.1 版本三条入口复用已有顺序恢复操作。

地上罐来源 `Taoguan01GengXinYouXiKeProcedure:111–113` 两次以空字符串为替换对象：虫物品自定义 `guchongname` 为空时结果为空，为非空时结果为 `]`。此前目标固定为空，现按来源表达式恢复；从手持罐重建物品不写这个自定义字段，直接放入带该字段的物品则可触及另一分支。保留来源异常结果，不把显示名清理规则套用到地上罐。

扩展现有 `guchonglifecycle`：单虫、地上罐和手持罐检查原生属性及自由字段；地上罐第九槽分别检查空名称字段和受控非空字段，并继续经历目标、回罐、死亡及收罐。该夹具在集成 `8179b634` 已通过，证据见[蛊虫月志](logs/2026-11.md#2026-11-22-跨入口放虫字段与地上罐名称)；只覆盖受控样本，实际获取、自然战斗、蛊笛、多人、专服与两版本对照仍未验收。

## 蛊笛放虫战斗成长与再次放出（2026-11-22）

既有蛊笛放收、飞蝗原生战斗和蛊虫击杀成长证据分别覆盖不同样本；尚未验证同一只虫在蛊笛放出后击杀、升级、收罐再放出是否连续保留成长。扩展现有 `guchongflute` 场景，在已有分页和局部收回检查后创建受控野生03飞蝗，调用摄虫网的公开收纳操作，受控设置主人、认主标记和升级前经验，再装入小罐，通过原生蛊笛物品及界面按钮放出。给飞蝗指定受控猪为目标，由原生近战与真实死亡事件触发成长；观察伤害直接来源、虫和主人经验，之后用同一按钮收回并再放出，核对罐及新实体的等级、剩余经验、上限、生命和主人。

预期独立取自1.18.2版本 `EntityDeathProcedure:64–68,215–286`、`EntitylevelupProcedure`、`GuchongshuxingUpProcedure`：猪无等级、原生最大生命10，击杀收益2；1级虫在119/120时击杀后升2级、余1、下一级上限170，贫弱生命资质使上限及当前生命为122，主人获得20养蛊经验。目标NeoForge 21.1.265 `Pig.createAttributes` 的原生生命上限为10。来源 `GuchongAnniuzhixingProcedure` 按罐UUID和槽位收放并保存成长字段。

这是受控跨入口场景：野生虫为夹具创建，收纳直接调用公开操作，主人/认主及初始经验由夹具设置，猪被指定为目标且生命被调低；不代表自然出生、原生摄虫网飞行捕捉、精血认主或自然选敌。新链路发生在已有真实重进之后，因此本次再次放出不是战斗后存档重进。集成 `a0339df0` 首次运行在受控战斗步骤未击杀目标，成长与收回再放出尚未到达，见[蛊虫月志](logs/2026-11.md#2026-11-22-蛊笛放虫战斗成长与再次放出)。多人、专服、1.18.2版本实机与两版本对照均未执行。
