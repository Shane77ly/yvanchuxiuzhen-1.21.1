---
topic: treasure
role: entry
status: partial
updated: 2026-11-07
---

# 宝箱

标识 `treasure`，类别：系统。职责：宝箱分级、奖励生成（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 4、物品 9）。C 的 skill 依赖批次已接入 `/lianqi` 成品命令，装备构造与品阶消费由 equipment 承担，末次专项六项全部符合；奖励总表、开箱与其余宝箱玩法尚未实现，不能据此标记整个系统完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 114 个元素，其中 28 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本批先接 `/lianqi` 成品入口：权限 1，命令位置的物品能力槽 0 必须为空，且方块需支持原生容器写入；调用 equipment 的 `Equipment.createProduct` 构造实例，经槽 0 读回后在原坐标掉落并清槽。命令返回 0，不写玩家持久字段；`typed_field_accesses.csv` 中没有该命令或 `BaoxiangChengpin` 的玩家字段读写。奖励总表与开箱流程仍待迁移，本批未提供全量奖励生成接口。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
