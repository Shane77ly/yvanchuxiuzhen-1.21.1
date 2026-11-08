---
topic: lianqi
role: entry
status: partial
updated: 2026-11-08
---

# 炼器

标识 `lianqi`，类别：系统。职责：鼎炉、图纸、熔炼、精炼、修理（[系统设计](../../development/system-design.md)）。

当前阶段：D 阶段开工。B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 10、物品 11）；本批先接回来源 `weaponlevelup` 游戏规则注册，炼器炉升级消费者、装置生命周期与游戏验收仍未实现。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 146 个元素，其中 32 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

千材录两个属性/等级说明过程已随实际菜单移归[material](../material/README.md)。

## 公开操作

- `LianqiRules.WEAPON_LEVEL_UP`：查询炼器鼎是否允许升级武器、法宝和护甲；规则真值由世界游戏规则保存，炼器系统是唯一消费者。

炼器炉、图纸、材料扣除和职业进度接口仍待本系统实现；完成前不把游戏规则注册写成玩法完成。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
