---
topic: equipment
role: entry
status: partial
updated: 2026-11-07
---

# 装备

标识 `equipment`，类别：系统。职责：武器、防具、工具、饰品、飞行法器、法宝与本命培养、词条（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（物品 198、声音事件 8）。C 的 skill 依赖批次已接回 18 把弓的来源条件流程、九把拂尘的挥动与选中冷却、隐藏拂尘物品及弹体；构建与 ArchUnit 通过，本批客户端专项一次五项全部符合。弓自然右键保留 Q57 断链，人工注入标记只验证条件分支。其他装备玩法、完整表现及两版本对照尚未完成，不能把本批状态作为整个系统验收；规则与证据见[弓与拂尘](architecture.md#弓与拂尘2026-11-07)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 335 个元素，其中 254 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

当前弓与拂尘批次没有其他系统需要调用的装备数据读写入口；选中推进、挥动和隐藏弹体物品的使用由本系统接收。后续装备实例、槽位、本命培养与属性贡献操作仍未实现，不能据本批标记整个系统完成。

本批对照 `typed_field_accesses.csv` 中 `Weaponspeedbow01DangGongJuChuXianZaiShouZhongShi`、`Weaponhossu01DangShiTiHuiDongWuPinShi` 与 `Weaponhossu01DangGongJuChuXianZaiShouZhongShi` 的全部玩家字段读写，按数据所有者调用：

| 来源字段或操作 | 目标公开操作与所有者 |
| --- | --- |
| `arrowlinshi` 查询与清空 | `Skills.arrow/setArrow`；清空时保留轮盘选择序号 |
| `power`、`LEVELnumber` | `Cultivation.get/add` |
| `tizhiitem` 的 `lagongtick/manxvli/arrowskill/fuchenshiyongtick` | `Cultivation.tizhiDouble/tizhiBoolean/setTizhiDouble/setTizhiBoolean`；只读写对应键 |
| `attack_Num_display` | `PlayerAttributes.value(ATTACK)` |
| `lctrl/lalt` | `ModifierKeys.ctrl/alt`；沿用已有平台输入状态 |
| 创建技能箭、敌友判定与法术伤害 | `Skills.createArrow`、`Relations.isFriendly`、`SpellDamage.deal` |

`synchronizedata.rightclicking` 是实体持久化标签读取，来源未找到写入者，按 Q57 登记；不新增鼠标状态写入。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
