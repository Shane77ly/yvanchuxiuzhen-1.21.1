---
topic: dungeon
role: architecture
status: draft
updated: 2026-11-02
---

# 副本：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 状态与入口

`A:PlayerfubentickProcedure:45–142` 使用玩家 tizhiitem 的副本状态、倒计时和坐标，写入副本方块并组织参与玩家。`B:FubenblockGengXinYouXiKeProcedure:51–140` 持有准备、释放敌人等阶段，按 `fybenboci{n}num/mobtagdata{i}` 刷怪。场景实例不等于一张静态结构。

天道副本方块 `Tiandaofuben` 是玩家入口：非创造模式打开 `FubenopenMenu`，创造模式打开副本名称、数据列表与最高支持等级的设置界面。进入画里乾坤维度时 `A:Fabao009weiduDangWanJiaJinRuWeiDuShiProcedure` 放置 `fuben001`/`fuben002` 结构并写 `fubenweizhi` 与副本坐标；`Zhenyaotashengchengbuchong` 向镇妖塔控制方块写 `fubenshujv`；`Weidubianhuan` 中按 `gamemoshi` 恢复游戏模式的分支属于回程。

## 目标所有权与接口提案

副本定义、实例序号、参与者、回程点、控制方块阶段与当前波次归本系统。玩家副本身份与回程位置随玩家保存，场景运行阶段由实例与控制方块持有。world 提供维度与结构放置；本系统负责进入、退出、回程与波次。

## 验证

保存/加载、多人进入、死亡/离场、跨维度返回和场景卸载分别检查。
