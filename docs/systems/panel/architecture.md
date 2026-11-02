---
topic: panel
role: architecture
status: draft
updated: 2026-11-02
---

# 玩家面板：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

汇总：六个分页的导航框架、HUD 与覆盖层。分页内容归所属系统，例如装备页 `PlayerGUI03` 归 equipment、功法页 `PlayerGUI04` 归 gongfa、副职业页归 profession。界面会话字段 `dangqianGUI`、`linshi`、`renwulistlinshi` 只服务显示。

## 目标所有权与接口提案

只显示各系统通过 `api` 提供的数据，不拥有玩法状态，不被系统依赖。
