---
topic: tiandao
role: entry
status: partial
updated: 2026-11-03
---

# 天道

标识 `tiandao`，类别：内容族。职责：生存模式不可得的通用管理工具；专属工具归所属系统（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 3、物品 5），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 29 个元素，其中 10 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
