---
topic: cooking
role: entry
status: partial
updated: 2026-11-08
---

# 烹饪

标识 `cooking`，类别：系统。职责：食材、厨具、菜谱、食用效果（[系统设计](../../development/system-design.md)）。

当前阶段：D 部分实现。B1 已登记的方块52、物品198仍为基础身份与资源；炼丹依赖的辟谷效果 `effect_031` 已接回注册与每刻行为。其余烹饪玩法尚未实现，验证边界见[本月记录](logs/2026-11.md)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 294 个元素，其中 232 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [记录索引](changes.md)：按月查看已实现范围与验证证据。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本批只接回炼丹药草可产生的辟谷效果 `effect_031`。调用者通过原生效果注册表施加效果；`baoshidu` 计时由 cooking 独占写入，不暴露直接写入口。已查 `typed_field_accesses.csv`，该实体持久键不在强类型字段表；`key_expressions.csv` 与来源代码确认读写仅在 `Effect031ZaiXiaoGuoChiXuShiMeiKeFaSheng`，无跨系统写入。厨具和食物实例接口随相应批次核对。

本轮变化记录在[系统月度日志](logs/2026-11.md)，项目进度见[项目月度日志](../project/logs/2026-11.md)。
