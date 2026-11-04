---
topic: skill
role: entry
status: partial
updated: 2026-11-04
---

# 技能

标识 `skill`，类别：系统。职责：技能栏、施法、冷却、12 系技能、身法、轻功、飞行、武器技（[系统设计](../../development/system-design.md)）。

当前阶段：进行中（2026-11-04，分支 `port/c-skill`）。已完成第 1 阶段“框架”：技能书实例（四类，`content/skill/` 藏宝表与研习附赠表）、玩家技能装配与选择附件、冷却附件、研习、技能栏的页与选择、右键施放（武器类别、沉默、灵力、冷却、禁施、连招）、四个生成命令、藏宝技能书、研习功法附赠技能、霸体与禁施两个状态效果，开发客户端检查通过（85 项）。第 2 阶段进行中：元素弹体技能 14 个已迁移并通过检查（63 项），10 种技能箭与箭种选择待做；第 3 阶段进行中：技能动画实体基础、火系 6 个、土系 4 个（含潜行式）与金系（金刚轮四式、捆妖轮、金弧轮、天堑金轮）技能已迁移并通过检查（70 项），其余各系实体技能、身法、轻功、武器技、技能界面与按键 HUD 在后续阶段迁移；见[架构](architecture.md)“迁移阶段”。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 400 个元素，其中 133 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

已对照 `typed_field_accesses.csv` 汇总技能字段的外部读写（本系统以外）：panel 读写技能栏 16 槽（各 48 处）、页 `jinengweizhi`（读 29 写 4）、身法 `qinggong`、轻功 `flyjineng`、武器技 `wuqiskill`；fulu 重置全部技能字段（各 1 处写）；equipment 读箭种选择 `arrowlinshi`（24 读 2 写）与武器技 `wuqiskill`（14 读）。代码为 `system/skill/api/Skills`；箭种选择与重置随对应阶段补充。

| 操作 | 形式 | 调用者 |
| --- | --- | --- |
| 查询技能书 | `book(物品)` → `SkillBook`（类别、名字、品阶、所需境界、灵根要求、研习者、参数表） | book、panel、equipment 等 |
| 查询、放入、取出技能栏 | `state(玩家)`、`spell(玩家, 槽)`、`setSpell(玩家, 槽, 物品)`；`movement`、`flight`、`weaponArt` 与对应 `set…` | panel（技能页放入、取出）、equipment（武器技） |
| 当前选择 | `selected(玩家)`；页、按键序号、技能栏序号与连招开关在 `state(玩家)` | panel、HUD |
| 冷却 | `cooldown(玩家, 分组)` | panel、HUD |
| 研习 | `study(玩家, 书)`：已被研习提示、境界与灵根条件（`RootRequirement`）、记下研习者 | book（右键） |
| 生成技能书 | `createBook(类别, 参数)`（与命令同一解析） | flow 等给书的玩法 |
| 书架藏宝 | `giveTreasureBook(玩家)`：13 本随机一本 | book（书架藏宝右键，需藏宝等级不低于 1） |
| 武器数值 | 事件 `WeaponStatEvent(武器, 键)`，监听者 `add` | lianqi、equipment 提供武器的 `mana_Att` |
| 研习附赠 | 监听 gongfa 的 `TechniqueStudiedEvent`，“行尸御火”“真言吒雷诀”各附赠一本技能书 | gongfa（事件） |

约定：技能书的技能名与灵根要求首字决定施放过程（`SkillReleases`，对应来源 `Jinenglist01`）；施放扣灵力、累加冷却、进入战斗状态（`FightingState`）；伤害经 combat 的 `Shanghai.deal`；敌友经 relation。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
