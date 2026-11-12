---
topic: economy
role: entry
status: partial
updated: 2026-11-11
---

# 经济

标识 `economy`，类别：系统。职责：灵石货币、银行、商店、现场拍卖（[系统设计](../../development/system-design.md)）。

2026-11-11 World供给批次接入本系统九种灵石矿物Feature的来源生成机制与同名configured/placed资源，台账登记“已注册”。维度和群系尚未完整装配，自然获取与经济消费者未验收；规则及本轮验证边界见[世界正文](../world/architecture.md#自然供给地物与随机结构2026-11-11)和[世界月志](../world/logs/2026-11.md)。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 12、物品 40），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 263 个元素，其中 77 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

未编写。实现本系统前在此列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点（开工条件）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
