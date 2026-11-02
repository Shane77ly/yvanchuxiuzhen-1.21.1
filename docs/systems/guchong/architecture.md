---
topic: guchong
role: architecture
status: draft
updated: 2026-11-02
---

# 蛊虫：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

蛊罐由 A 的 `Taoguan01item`–`Taoguan03item` 与菜单、B 的 `Taoguan01`–`Taoguan03` 方块组成。菜单槽位为 1/4/9，只收 `forge:guchong`（`A:Taoguan01GUIMenu:89`）。物品右键方块时按字符串 ID 放置 `yvanchuxitong:taoguan_0x` 并写入玩家好友列表；方块右键手持精血时，若精血记录的 `playerlevelnum` 不低于蛊虫 `LEVELnumber`，写入 `guchongxvnfu`、`team` 与 `belongToMasterUUID`（`B:Taoguan01ZaiFangKuaiShangYouJianProcedure`）；方块 tick 按队伍与周围实体条件释放蛊虫（`B:Taoguan01GengXinYouXiKeProcedure`）。方块实体的 `createMenu` 返回原版 `ChestMenu` 占位。

`GuchongtickProcedure:27–106` 用 UUID/坐标关联目标与释放装置，回收时修改装置计数并移除实体。

## 目标所有权与接口提案

蛊虫装置拥有放出数量与槽，实体拥有个体状态，双方通过具体操作更新，不持有永久 Java 实体引用。

## 待调查

蛊虫释放界面 `GuchongshifangGUI` 与按钮过程、虫卵 `Chongluan` 的来源与用途尚未逐条读取。
