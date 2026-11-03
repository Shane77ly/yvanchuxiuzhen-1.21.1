---
topic: material
role: entry
status: partial
updated: 2026-11-03
---

# 材料

标识 `material`，类别：系统。职责：编号材料身份、品阶、等级、辨识（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 21、物品 265），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 351 个元素，其中 270 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 待裁定归属

以下物品读完来源仍不能按规则判定归属（[系统设计](../../development/system-design.md)“归属判定”第 5 条），暂留在本单位只因为来源目录 `~/item/zawu` 的批量候选；它们不是编号材料，来源创造栏是“原初物品”。物品已按内容表登记，台账保持“待处理”、归属为“候选”，裁定后移到所属单位。

| 来源元素 | 目标 ID | 来源事实 |
| --- | --- | --- |
| `Ling07` 令牌 | `yvanchuxiuzhen:ling_07` | 堆叠 64，无提示文本，无过程；代码、数据、结构、语言载荷中都没有使用者 |
| `Ling17` 梦之羽毛 | `yvanchuxiuzhen:ling_17` | 史诗稀有度，堆叠 1，提示“愿你不忘初心，砥砺前行！”，无过程，没有使用者 |
| `Ling19` 云清令 | `yvanchuxiuzhen:ling_19` | 堆叠 1，提示“云清派身份牌”，无过程；只出现在结构 `yvnqingpai`（云清派）的容器中 |

## 公开操作

未编写。实现本系统前在此列出供其他系统调用的查询与写操作，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点（开工条件）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
