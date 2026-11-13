---
topic: guchong
role: architecture
status: draft
updated: 2026-11-13
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

2026-11-13收纳批次已通过统一编译、架构检查及一级手持罐菜单/序列化保存的最小客户端单项，见下节。地上罐与手持罐全生命周期、其余自然操作、完整存档/多人、专服专项、1.18.2版本实机与两版本对照未执行。

## 待调查

蛊虫释放界面 `GuchongshifangGUI` 与按钮过程、虫卵 `Chongluan` 的来源与用途尚未逐条读取。

## 2026-11-13 摄虫网收纳与蛊罐消费者

R05/R10的外部收纳缺口由`GuchongCapture.captureWild`承担：`equipment`仅传入当前服务端世界、目标实体和法宝主人UUID，不读取或写入蛊虫持久字段。操作先检查`guchongxvnfu`，已认主时返回对应结果，法宝按来源在网的位置广播怒粒子；未认主时，将目标实体完整注册ID加`item`后以英文小写查物品，无对应物品则跳过。来源没有虫标签、存活、节肢类别或所有者过滤，目标保持，不将其收窄为仅本系统两种实体。

找到对应物品后写入实体ID、等级名/数值、经验上限/当前经验、队伍、十三项资质和对应属性；`health_Num`读取当前生物生命，非生物为-1。名称删除方括号后保存为物品自定义名称，认主标记为false；不复制整个实体NBT或附加来源未写入的归属字段。由本系统通过物品组件写回；法宝主人在当前世界且为玩家时交给玩家，库存满时沿原生`ItemHandlerHelper.giveItemToPlayer`掉落。来源在主人缺席时仍移除可捕获目标，目标保持该顺序。

所得虫物品已携带`guchongid`，可直接进入三档蛊罐的既有标签槽，关闭时由`TaoguanMenu`保存编号槽，罐物品序列化仍由原生组件承担。重新开罐继续保持来源不读回`health_Num`的差异，不能以捕获保存当前生命为理由改写这条菜单规则。地上罐精血认主、释放、回罐、死亡清槽仍沿用现有实现，本次不宣称这些完整自然路径已验收。

来源依据：A `procedures/Fabao01701DangShiTiGengXinKeShiProcedure.java`的摄虫网分支；具体法宝入口、时序、模型见[装备正文](../equipment/architecture.md#摄虫网与困虫兜2026-11-13)。本次最小客户端单项已通过普通物品入口、活动实体和捕获物品序列化重读、认主虫排除、真实捕获物品入罐/关罐/重开，以及同物品困虫兜门槛和时序；完整结果只登记[装备月志](../equipment/logs/2026-11.md#2026-11-13摄虫网收纳与蛊罐保存)。完整地上罐链、多人、重登、1.18.2版本实机与两版本对照仍未执行。
