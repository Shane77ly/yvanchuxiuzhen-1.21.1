---
topic: profession
role: architecture
status: draft
updated: 2026-11-02
---

# 副职业：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

10 种副职业由同一个 `FuzhiyeLevelUp` 升级，有独立面板页。玩家字段按前缀分为 `Alchemy_`、`Refiner_`、`Rune_`、`collection_`、`cooking_`、`puppet_`、`breed_`、`insect_`、`ghost_`、`blood_` 的等级与经验（31 个字段）。各生产系统直接增加经验，外部写入 80 处；炼器开炉时从修真玩家能力读取 `Refiner_LEVEL` 写入鼎炉（`B:Tripod01ZaiFangKuaiShangYouJianProcedure:43`）。

## 目标所有权与接口提案

全部副职业进度归本系统，提供 `addExperience(职业, 数量)` 一个写入口与等级查询。傀儡、御鬼、御虫目前只确认了等级字段，发现完整玩法再评估。血道的等级字段 `blood_LEVEL`、`blood_experience` 在来源中只由 `/yvanchu` 命令、副职业升级与浮生丹重置写入，玩法中没有经验来源；不经过这两个字段的血道玩法（吸血石、血流、血精）归 `xiulian`，见[系统设计](../../development/system-design.md)“什么是系统”。
