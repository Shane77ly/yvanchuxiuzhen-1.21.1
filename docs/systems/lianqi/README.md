---
topic: lianqi
role: entry
status: partial
updated: 2026-11-03
---

# 炼器

标识 `lianqi`，类别：系统。职责：鼎炉、图纸、熔炼、精炼、修理（[系统设计](../../development/system-design.md)）。

当前阶段：1.18.2 架构调查与目标提案。尚未实现或进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 143 个元素，其中 27 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

未编写。实现本系统前在此列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点（开工条件）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
