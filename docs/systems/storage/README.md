---
topic: storage
role: entry
status: partial
updated: 2026-11-08
---

# 储物

标识 `storage`，类别：系统。职责：乾坤袋、材料袋与玩家材料仓（[系统设计](../../development/system-design.md)）。

当前阶段：乾坤袋01–04的物品库存、能力、四个菜单和原生界面已实现，验证结果见[正文](architecture.md#乾坤袋物品库存2026-11-08)。材料袋 `qiankundai_06`、玩家材料仓、自动存入、排序与按材料扣料尚未实现，不能将储物系统标为完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 116 个元素，其中 16 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

乾坤袋对外沿用物品库存能力 `Capabilities.ItemHandler.ITEM`：`getSlots/getStackInSlot` 查询容量和栈快照，`insertItem/extractItem` 存取且支持模拟。库存由 storage 的 `BagInventory` 唯一写回该袋的容器组件；调用者不得把整份组件当作玩家材料仓写入。

本批已对照 `typed_field_accesses.csv`：四档乾坤袋没有玩家变量写入；材料仓使用的混合载体 `renwuitem` 有510处访问、246个来源类，包含任务、商店、拍卖与材料显示缓存，不能整体迁入storage。其材料存入、取出、数量查询和辨识/炼器扣料接口随材料仓下一批建立，其他所有者字段继续留在各自系统。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
