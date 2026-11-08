---
topic: storage
role: entry
status: partial
updated: 2026-11-08
---

# 储物

标识 `storage`，类别：系统。职责：乾坤袋、材料袋与玩家材料仓（[系统设计](../../development/system-design.md)）。

当前阶段：乾坤袋01–04的物品库存、能力、四个菜单和原生界面，以及材料袋 `qiankundai_06`、玩家材料仓、筛选、排序、自动存入和辨识扣仓已实现。现有 storage 客户端场景45项玩法检查通过，覆盖真实开窗、存取、辨识消费、死亡复制和重登；完整视觉、来源实机、两版本对照及炼器实际扣仓入口尚未验收，不能将储物系统标为完成。分项证据见[正文](architecture.md)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属116个元素已逐项读完；这是来源检索范围，不是已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

乾坤袋对外沿用物品库存能力 `Capabilities.ItemHandler.ITEM`：`getSlots/getStackInSlot` 查询容量和栈快照，`insertItem/extractItem` 存取且支持模拟。库存由 storage 的 `BagInventory` 唯一写回该袋的容器组件；调用者不得把整份组件当作玩家材料仓写入。

玩家材料仓的公开入口为 `api.MaterialStorage`，只由 storage 写附件：

- `snapshot(Player)`：返回不可变 `MaterialWarehouse`，按材料ID查询组和总数；客户端仅查询本人同步快照。
- `accepts(ItemStack)`：查询材料袋输入条件。
- `deposit(ServerPlayer,ItemStack)`：存入已通过输入条件的整栈，只保留材料ID、等级和品阶。
- `withdraw(ServerPlayer,String,int,int)`：按ID、等级、品阶取一件；菜单负责交付或掉落。
- `consumeForAnalysis(ServerPlayer,String,int)`：按辨识的1–40级、0–4品阶顺序扣仓，返回未满足数量。
- `preferences(ServerPlayer,Preferences)`：保存页码、排序、筛选、自动存入和允许扣仓偏好。

本批已对照 `typed_field_accesses.csv`：四档乾坤袋没有玩家变量写入；材料仓使用的混合载体 `renwuitem` 有510处访问、246个来源类，包含任务、商店、拍卖与材料显示缓存，不能整体迁入storage。材料定义 `MaterialInventoryRequestEvent` 的 `COUNT/CONSUME` 请求由 storage 订阅，材料不直接改仓；炼器的相反扣料顺序随真实装料消费者接回，其他所有者字段仍留在各自系统。

本轮变化记录在[储物月度日志](logs/2026-11.md)，项目级进度见[项目月度日志](../project/logs/2026-11.md)。
