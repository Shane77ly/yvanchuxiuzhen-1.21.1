---
topic: town
role: architecture
status: draft
updated: 2026-11-02
---

# 城镇：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口（已读范围）

案台方块存入木、石、羊毛（`ChengzhenBuildJiance` 按 `minecraft:logs` 等标签计数），足量后 `Building` 调用 `Jiegoufangzhi` 放置建筑；方块实体保存 `build_data` 与 `team`。城镇中心案台物品由 `obtainatowncenterscroll` 命令（`ObtainatowncenterscrollGo`）生成。`Npcshujvjiaru` 未读。

## 待调查

建设类型清单、居民行为、`worlddata` 中的城镇数据与生存获取途径尚未逐条读取。
