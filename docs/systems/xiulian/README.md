---
topic: xiulian
role: entry
status: partial
updated: 2026-11-03
---

# 修炼

标识 `xiulian`，类别：系统。职责：境界、修为、灵力、寿元、出身、灵根体质、丹田、金丹元婴、本命槽位、雷劫、轮回（[系统设计](../../development/system-design.md)）。

当前阶段：完成（2026-11-03 合入 `main`），留待其他系统的部分见交接说明“已完成”。已完成：修炼档案附件与公开操作、灵根提交 attribute、两个游戏规则、首次进入的灵根与体质随机、出身界面、修炼推进与恢复、升级与突破、闭关修炼、金丹与元婴、丹田界面、血道（吸血石、血流、血精），开发客户端检查通过。另有加点、管理命令的灵根重置、升仙令与家族令牌。未迁移：雷劫过程（待 combat 的伤害入口，见架构“阶段 4（二）”）；远古石盘、传承灵物、家族百宝袋（待 world、material、fulu 的数据）；元婴出窍与被闪电击中的功法效果读后归 skill、gongfa。B1 已登记的内容身份见台账。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 196 个元素，其中 160 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 依赖方向

2026-11-03 会话决定（gongfa 开工时）：来源中 gongfa 读 xiulian 的境界、灵根、功德、罡煞、寿元与闭关天数（研习条件与功法修炼），xiulian 读主修功法的名字、品阶与归属（特殊功法效果、修为、金丹与元婴）。按“系统之间不得相互依赖”定为 gongfa → xiulian 单向：xiulian 需要的主修功法信息由 gongfa 向 xiulian 提交（与 attribute 的贡献同一形式），xiulian 不读 gongfa。因此 C 中 xiulian 先于 gongfa 实现。 本系统据此提供“提交主修功法信息”的写操作与境界、灵根等查询。

## 公开操作

已对照 `typed_field_accesses.csv` 汇总 42 个修炼字段的外部读写点（不含本系统）：写入集中在灵力（经济、异火、技能、战斗、装备、功法共 60 余处）、待吸收修为（经济、战斗、烹饪）、寿元、罡煞、功德，形式只有“在当前值上加减”“乘系数后写回”“直接赋值”三种，均不在写入处截断上限；读取遍及战斗、技能、装备、面板、流程等。代码为 `system/xiulian/api/Cultivation`。

| 操作 | 形式 | 调用者 |
| --- | --- | --- |
| 查询 | `get(玩家, CultivationValue)`：境界层数、修为三项与效率、灵力、神识、灵根与阴阳、罡煞、功德、雷劫、血脉、属性点、寿元、闭关天数等 35 项；`get(玩家, CultivationText)`：境界名、种族、血脉、体质、出身、`fuhuo` | 全部读取方 |
| 加减、赋值 | `add(玩家, 项, 增量)`、`set(玩家, 项, 值)`、`set(玩家, 文本项, 文本)`；乘法由调用方读后赋值 | 经济、异火、技能、战斗、装备、功法、符箓、流程等 |
| 雷劫开关 | `thunderOpen`、`setThunderOpen` | 流程、世界、符箓、技能 |
| 本命槽位 | `benming`、`setBenming` | equipment |
| 丹田界面 | `Dantian.open`；返回按钮发出 `DantianReturnEvent`（取消时不关闭界面） | panel |
| 本命培养进度 | `benmingProgress`、`setBenmingProgress`（`BenmingProgress`：认主、等级、经验、所需经验，供丹田界面显示） | equipment |
| 主修功法 | `mainTechnique`、`setMainTechnique`（`MainTechnique`：有无、本人所有、名字、五行、品阶、层数） | gongfa |
| 加点 | `spendSkillPoint(玩家, SkillPoint)`（六项：生命、灵力、神识上限与物理、法术、神识攻击） | panel |
| 灵根重置 | `resetRoots`、`maximizeRoots`（管理命令 `/yvanchu` 的两个子命令） | flow |
| 吸血石 | `BloodStone.addBlood`、`blood`（流血效果给脚下的吸血石加充血量） | combat |
| 体质与修正值 | `tizhiDouble/Boolean/String` 与对应的 `set`：来源隐藏载体 `tizhiitem` 的键值（335 个键及读写系统见 [tizhiitem_keys.csv](ledger/tizhiitem_keys.csv)） | 战斗（属性修正）、技能（冷却与消耗修正）、流程（重生修正）、装备（武器培养速度）等 |

约定：写操作只在服务端调用，写入后同步给本人；灵根与阴阳变化时自动向 attribute 提交基础层贡献 `xiulian:roots`。`tizhiitem` 由本系统整体保管（会话决定，2026-11-03，评估为轻微）：其中蛊虫、战斗、副本等系统自己写入的键，在这些系统实现时可移到各自的数据。尚未提供、待后续阶段补充：雷劫与轮回的流程操作。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
