---
topic: relation
role: architecture
status: draft
updated: 2026-11-02
---

# 关系：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

关系并不只有队伍字符串：玩家 `friendlist` 物品的嵌套 CompoundTag、实体 `friendlist`、`team` 以及主人 UUID 都参与判定。组队入口有 `Ling15`“团队招募令”，好友判定为 `Friendpanding*` 过程；完整流程尚未逐条读取。

## 目标所有权与接口提案

关系服务负责队伍、好友、主人归属查询。战斗、技能、生物、蛊虫等通过查询判定敌友，不直接读写队伍与好友数据。
