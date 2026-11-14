---
topic: guchong
role: architecture
status: draft
updated: 2026-11-14
---

# 蛊虫：架构调查与提案

按项目已批准的系统边界实施；来源事实、已实现范围和待验证范围分别登记。源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

蛊罐由 A 的 `Taoguan01item`–`Taoguan03item` 与菜单、B 的 `Taoguan01`–`Taoguan03` 方块组成。菜单槽位为 1/4/9，只收 `forge:guchong`（`A:Taoguan01GUIMenu:89`）。物品右键方块时按字符串 ID 放置 `yvanchuxitong:taoguan_0x` 并写入玩家好友列表；方块右键手持精血时，若精血记录的 `playerlevelnum` 不低于蛊虫 `LEVELnumber`，写入 `guchongxvnfu`、`team` 与 `belongToMasterUUID`（`B:Taoguan01ZaiFangKuaiShangYouJianProcedure`）；方块 tick 按队伍与周围实体条件释放蛊虫（`B:Taoguan01GengXinYouXiKeProcedure`）。方块实体的 `createMenu` 返回原版 `ChestMenu` 占位。

`GuchongtickProcedure:27–106` 用 UUID/坐标关联目标与释放装置，回收时修改装置计数并移除实体。

## 目标所有权与接口提案

蛊虫装置拥有放出数量与槽，实体拥有个体状态，双方通过具体操作更新，不持有永久 Java 实体引用。

## 2026-11-12 地上罐与手持罐生命周期

`TaoguanBlockEntity` 保存容量为1/4/9的蛊虫槽及末槽装饰物。地上交互保持来源顺序：精血认主、潜行取放装饰物、Ctrl/Alt收罐；方块下方 Material 使用 `legacy_material_grass/dirt/stone/wood` 标签。`GuchongJarRuntime.release` 每20刻按距罐中心排序检查实体，保留来源局部释放标志跨目标持续的行为；一旦触发，后续每个目标迭代均会遍历全部蛊槽，实体创建返回空时仍累加释放计数。罐关系友方标签由 `Relations.shareAllegianceToBlock` 从玩家共享到罐，再由 `Relations.shareBlockAllegiance` 共享给虫；虫自身 `team` 取蛊虫物品实例字段。来源地上释放不写 `guguanuuid`，目标保持。

手持罐右键放出全部归属本人且有编号的虫；再次右键按罐 UUID 收回半径8内匹配蛊虫，保存当前生命、属性、等级和经验并移除实体。潜行右键打开三档原生菜单；菜单开启从主手罐编号字段构建临时槽，关闭写回当前主手物品。放置地上罐只接受朝上且上方为空的方块使用，并按来源先主手后副手清除罐物品。UUID只在潜行开罐时为空才生成，8个字母每次取a–y，保留来源不取z的范围。

三档菜单均176×166，使用来源 `textures/screens/taoguan_01_gui.png`、`02`、`03` 原图。01槽为(79,35)；02槽为(70,26)、(88,26)、(70,44)、(88,44)；03槽为三行三列，从(61,17)每格18像素。玩家背包从(8,84)起、快捷栏从(8,142)起，蛊槽仅收蛊虫标签。来源屏幕没有按钮或文字标签，目标保持；网络菜单标题改用来源中文名称“小蛊罐 / 蛊罐 / 大蛊罐”，避免内部英文标题。

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

精确依据：A `entity/Guchong05Entity.java:45–163`、`client/renderer/Guchong05Renderer.java:11–18`、`item/Guchong05itemItem.java`、`procedures/Guchong05itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure.java:18–38`、`Guchong05DangYouJianDianJiShiTiProcedure.java:16–33`与`Guchong05DangShiTiGengXinKeShiProcedure.java:26–70`。05实体候选归属已完成该完整类、表现、物品和过程调用链阅读，已同步公共归属清单并完成定向转换/编译/架构检查；代码已接入不等于运行验收。自然悬赏领取/补领、释放/任意玩家回收、100刻交还、失联与真实退出重进均未执行。

## 待调查

虫卵 `Chongluan` 的完整来源与用途尚未逐条读取。蛊笛释放界面与按钮已核对并接线，范围与未验边界见下方蛊笛正文。

## 2026-11-13 摄虫网收纳与蛊罐消费者

R05/R10的外部收纳缺口由`GuchongCapture.captureWild`承担：`equipment`仅传入当前服务端世界、目标实体和法宝主人UUID，不读取或写入蛊虫持久字段。操作先检查`guchongxvnfu`，已认主时返回对应结果，法宝按来源在网的位置广播怒粒子；未认主时，将目标实体完整注册ID加`item`后以英文小写查物品，无对应物品则跳过。来源没有虫标签、存活、节肢类别或所有者过滤，目标保持，不将其收窄为仅本系统两种实体。

找到对应物品后写入实体ID、等级名/数值、经验上限/当前经验、队伍、十三项资质和对应属性；`health_Num`读取当前生物生命，非生物为-1。名称删除方括号后保存为物品自定义名称，认主标记为false；不复制整个实体NBT或附加来源未写入的归属字段。由本系统通过物品组件写回；法宝主人在当前世界且为玩家时交给玩家，库存满时沿原生`ItemHandlerHelper.giveItemToPlayer`掉落。来源在主人缺席时仍移除可捕获目标，目标保持该顺序。

所得虫物品已携带`guchongid`，可直接进入三档蛊罐的既有标签槽，关闭时由`TaoguanMenu`保存编号槽，罐物品序列化仍由原生组件承担。重新开罐继续保持来源不读回`health_Num`的差异，不能以捕获保存当前生命为理由改写这条菜单规则。地上罐精血认主、释放、回罐、死亡清槽仍沿用现有实现，本次不宣称这些完整自然路径已验收。

来源依据：A `procedures/Fabao01701DangShiTiGengXinKeShiProcedure.java`的摄虫网分支；具体法宝入口、时序、模型见[装备正文](../equipment/architecture.md#摄虫网与困虫兜2026-11-13)。本次最小客户端单项已通过普通物品入口、活动实体和捕获物品序列化重读、认主虫排除、真实捕获物品入罐/关罐/重开，以及同物品困虫兜门槛和时序；完整结果只登记[装备月志](../equipment/logs/2026-11.md#2026-11-13摄虫网收纳与蛊罐保存)。完整地上罐链、多人、重登、1.18.2版本实机与两版本对照仍未执行。

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
