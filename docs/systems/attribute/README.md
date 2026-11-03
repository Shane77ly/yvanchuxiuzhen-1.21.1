---
topic: attribute
role: entry
status: partial
updated: 2026-11-03
---

# 属性

标识 `attribute`，类别：系统。职责：基础属性、贡献汇总、派生值，含灵根与功法的五行加成和抗性（[系统设计](../../development/system-design.md)）。

当前阶段：1.18.2 架构调查与目标提案。尚未实现或进行游戏验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 9 个元素，其中 0 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

2026-11-03：已对照 `typed_field_accesses.csv` 覆盖属性字段的全部外部读写点，五行部分按 `DamageIntegration` 读完（来源事实见[架构与调查正文](architecture.md)“来源事实核对”）。Q17、Q18 裁定前按来源行为实现。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 查询最终值 | `value(玩家, 属性)`：生命、灵力、神识的上限与回复，物理、法术、神识的攻击与防御，冷却缩减，移速 | combat 伤害与防御结算、xiulian 回复与金丹元婴、skill 冷却与消耗、equipment 速射弓与法宝、relation 好友信息、gongfa 学习台、economy 魂灵晶、全局重生、panel 显示 |
| 提交贡献 | `setContribution(玩家, 来源键, 各属性加值)`；同一来源键覆盖上次的值 | xiulian（境界与修为成长、单项提升）、gongfa（主修功法）、equipment（防具与武器的炼器数值、饰品、附魔）、fulu（浮生丹重置）、economy（血灵晶） |
| 清除贡献 | `clearContribution(玩家, 来源键)` | 同上；卸下装备、取消主修、重置时 |
| 五行系数 | `elementCoefficient(攻击方, 伤害属性)` = 灵根值 × 0.1（阴阳用 `yinyang`）；`receivedElementBonus(受击方, 伤害属性)` = 主修功法五行值 × 功法层数 / 4 | combat 伤害结算；灵根值由 xiulian、功法五行值由 gongfa 作为贡献提交 |

本系统在贡献变化后重算，结果写入实体属性并同步给本人；不读取其他系统的状态。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
