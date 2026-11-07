---
topic: material
role: entry
status: partial
updated: 2026-11-08
---

# 材料

标识 `material`，类别：系统。职责：编号材料身份、品阶、等级、辨识（[系统设计](../../development/system-design.md)）。

当前阶段：材料目录基础已接回。目标资源读取 225 条材料定义；材料实例的等级/品阶使用物品组件，玩家辨识进度使用玩家附件；异火登录时的 19 条材料初始化已接入。材料袋、辨识界面与扣料、采集产出、炼器/炼丹/阵法消费仍未实现，不能将本系统标为完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 348 个元素，其中 270 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

- `Materials.definition(ItemStack)`：按注册 ID 查询材料定义；只读。
- `Materials.instance(ItemStack)`：读取材料实例的等级/品阶组件；只读。
- `Materials.initializeYihuo(ServerPlayer)`：按来源登录入口初始化异火材料辨识进度；只由 yihuo 调用。

当前公开入口只覆盖目录、实例组件和异火初始化。材料辨识扣料、玩家材料仓读写和生产用途查询要等 storage、lianqi、liandan、zhenfa 的实际消费者完成后再扩展，不能让调用者直接修改材料系统附件。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
