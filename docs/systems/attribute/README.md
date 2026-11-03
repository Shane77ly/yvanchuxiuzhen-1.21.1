---
topic: attribute
role: entry
status: partial
updated: 2026-11-03
---

# 属性

标识 `attribute`，类别：系统。职责：基础属性、贡献汇总、派生值，含灵根与功法的五行加成和抗性（[系统设计](../../development/system-design.md)）。

当前阶段：公开操作、贡献汇总、玩家附件与原版属性写入已实现（`system/attribute`），JUnit 算例与开发客户端实机检查通过（2026-11-03）。提交贡献的系统（xiulian、gongfa、equipment、fulu、economy）尚未实现，真实来源的数值路径随它们验证；未实现项见下文。

- [架构与调查正文](architecture.md)：来源事实、归属阅读、目标实现、验证与裁定事项。
- 候选归属 9 个元素已全部读完：3 个归属本系统（其中 `Playershuxingjisuan` 为空过程），其余归 equipment、xiulian、creature。本系统没有来源注册，因此没有内容台账。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- 裁定：Q17 实际属性计入炼器数值、与显示一致（2026-11-03）；Q18 暂按来源行为、之后再议（2026-11-03）。见[架构与调查正文](architecture.md)“来源问题”。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

已对照 `typed_field_accesses.csv` 覆盖属性字段的全部外部读写点，五行部分按 `DamageIntegration` 读完（来源事实见[架构与调查正文](architecture.md)“来源事实核对”）。代码为 `system/attribute/api/PlayerAttributes`。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 查询最终值 | `value(玩家, Stat)`：生命、灵力、神识的上限与回复，物理、法术、神识的攻击与防御，冷却缩减，移速，灵根与功法五行 | combat 伤害与防御结算、xiulian 回复与金丹元婴、skill 冷却与消耗、equipment 速射弓与法宝、relation 好友信息、gongfa 学习台、economy 魂灵晶、全局重生、panel 显示 |
| 查询基础值 | `baseValue(玩家, Stat)`：初始值加基础层贡献（来源中不带 `_display` 的字段） | combat（`DamageIntegration` 读 `mana_Att`、`EntityBeforeHurt` 读 `attack_Def`）、skill（技能箭读 `attack_Num`）、equipment（`Fabao010` 读 `mana_Att`）、xiulian（资质检测） |
| 提交贡献 | `setContribution(玩家, 来源键, Contribution(层, 各属性加值))`；同一来源键覆盖上次的值。基础层：xiulian（境界与修为成长、单项提升、灵根）、fulu（浮生丹重置）、economy（血灵晶）；加成层：gongfa（主修功法加成与五行、层数）、equipment（防具与武器的炼器数值、饰品、附魔） | 同左 |
| 清除贡献 | `clearContribution(玩家, 来源键)` | 同上；卸下装备、取消主修、重置时 |
| 五行系数 | `elementCoefficient(攻击方, DamageElement)` = 灵根值 × 0.1（阴阳用 `yinyang`，非玩家为 0）；`receivedElementBonus(受击方, DamageElement)` = 功法五行值 × 功法层数 / 4（阴、阳与非玩家为 0） | combat 伤害结算 |

约定：

- 贡献只在服务端提交；提交后立即重算、写入原版最大生命、攻击伤害、移速的基础值，并同步给本人。客户端只能查询本人的值。
- 最大生命与攻击伤害按最终值写入，含防具与武器的炼器数值（Q17）。
- 贡献随附件跨越死亡与重进保留；装备类提交者在登录、重生与装备变化时按当前状态重新提交。
- 本系统不读取其他系统的状态。

## 未实现

- 重算时的粒子 `tiangandizhi`（来源在防具、饰品、功法重算的开始与结束各发一个）：粒子类型由 particle 登记，尚未迁移。
- 各提交系统的真实路径：xiulian、gongfa、equipment、fulu、economy 实现时提交贡献并在开发客户端验证；equipment 实现时去掉妖化防具的生命修饰符（Q17 后会重复计入）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
