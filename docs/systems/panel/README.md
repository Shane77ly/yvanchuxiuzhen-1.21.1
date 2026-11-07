---
topic: panel
role: entry
status: partial
updated: 2026-11-07
---

# 玩家面板

标识 `panel`，类别：汇总。职责：六个分页的导航框架、HUD 与覆盖层；各页的菜单归所属系统（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现（2026-11-07）。随 skill 阶段 5 接入原生 HUD：六组技能冷却、十帧格挡显示与十帧弓蓄力显示，只读 skill/xiulian 的本人同步数据。HUD 截图已查看；技能界面同批主路径检查的 9 项中 8 项通过，首项检查等待时序已修正但未重跑。六个分页导航和其余 HUD 尚未实现，弓蓄力真值由后续 equipment 提供。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 93 个元素，其中 2 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
