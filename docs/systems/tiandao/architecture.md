---
topic: tiandao
role: architecture
status: draft
updated: 2026-11-02
---

# 天道：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

内容族：作者“原初天道（生存模式不可得物品）”分类下的管理工具，包括 NPC 设置（`TiandaoNPCset*`）、物品 NBT 编辑（`Tiandaoitemnbt*`）、多结构工具（`Tiandaoduojiegou`）、建筑笔（`Tiandaojianzhu`）、`Ling08`、`Ling16` 与天道维度方块。天道副本方块是玩家入口，已归 dungeon。

## 目标所有权与接口提案

专属于某个系统的工具在该系统开工时按归属判定拆回；通用工具留在本内容族。没有 `api`。
