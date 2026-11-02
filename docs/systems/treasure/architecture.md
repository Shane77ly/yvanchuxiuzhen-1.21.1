---
topic: treasure
role: architecture
status: draft
updated: 2026-11-02
---

# 宝箱：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口（已读范围）

`Baoxiangzong` 由 `/baoxiangopen` 命令进入，按容器类型（如书架、药架）与类别（书籍堆、符箓堆、丹方、功法书、技能书等）分派到 `Baoxiang0101`、`BaoxiangFulu`、`BaoxiangLiandantuzhi`、`BaoxiangGongfa`、`BaoxiangJineng` 等过程。`TreasureChest11ZaiFangKuaiShangYouJian` 右键时调用 `TreasureChest11FangZhiFangKuaiShi`（按 1–100 随机值分 8 级）并写方块实体 `dakai`。`~/tag/baoxiang` 有 26 个标签。

## 目标所有权与接口提案

宝箱定义（容器类型、类别、等级与奖励表）归本系统；奖励物品由所属系统的生成入口创建，本系统组合结果。

## 待调查

各类别奖励过程、`Tiandaobaoxiang` 与 `/baoxiangset` 尚未逐条读取。
