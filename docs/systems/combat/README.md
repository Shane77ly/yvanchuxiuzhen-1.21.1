---
topic: combat
role: entry
status: partial
updated: 2026-11-03
---

# 战斗

标识 `combat`，类别：系统。职责：伤害结算（读 attribute 最终值）、通用状态效果、附魔、木人桩（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 4、物品 5、声音事件 8），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 258 个元素，其中 26 个已读（伤害主链 14 个，见[架构](architecture.md)“伤害主链的归属阅读”）；85 个附魔读后 75 个留在本系统，10 个改归 skill（`flying`、`flyingskill`、`jet`）、attribute（`dawn`、`powerup`、`colddown`）、equipment（`qiling`、`qingyv`、`kailing`）、fulu（`charm`）；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

未编写。实现本系统前在此列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点（开工条件）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
