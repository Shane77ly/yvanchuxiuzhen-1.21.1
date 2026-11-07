---
topic: yihuo
role: entry
status: partial
updated: 2026-11-07
---

# 异火

标识 `yihuo`，类别：系统。职责：异火的材料、投掷、火块与命中效果（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 18、物品 36、声音事件 1），只表示身份、资源与基础属性就位；异火主体玩法尚未实现，未进行游戏验收。2026-11-07 为 skill 的火箭施加行为登记 `effect_023`（阳火炙烤）身份，台账仅记“已注册”；持续伤害、自定义粒子和与 `effect_024` 的联动仍待本系统迁移。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 101 个元素，其中 97 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`effect_023` 已可经 combat 的 `StatusEffects` 查询与施加，供 skill 的火箭使用；这不代表持续行为完成。其余公开操作在实现异火主体前列出，并对照 `typed_field_accesses.csv` 覆盖全部外部读写点。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
