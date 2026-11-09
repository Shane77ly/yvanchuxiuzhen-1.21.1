---
topic: craft
role: entry
status: partial
updated: 2026-11-09
---

# 工艺

标识 `craft`，类别：系统。职责：木匠台、陶轮、石磨、织布机与蚕丝链（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 4、物品 15），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 128 个元素，其中 48 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

2026-11-09已完整读取 `Grind`、`GrindGo` 及石磨自然调用链，两项从 liandan 移归 craft。七种产物与来源输出复制、输入数量缓存异常见[正文](architecture.md#石磨与-grind-来源核对2026-11-09)；该结论只确认来源职责，目标石磨玩法和运行验收仍未完成。

未编写。实现本系统前在此列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点（开工条件）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
