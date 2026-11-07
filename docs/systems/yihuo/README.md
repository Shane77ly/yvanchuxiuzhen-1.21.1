---
topic: yihuo
role: entry
status: partial
updated: 2026-11-07
---

# 异火

标识 `yihuo`，类别：系统。职责：异火的材料、投掷、火块与命中效果（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现。B1 已登记方块18、物品36、声音事件1的身份与资源；2026-11-07 为 skill 依赖接回 `effect_023`/`effect_024` 持续伤害、粒子与爆燃、18种火块的燃料/接触/保存同步，以及丹火放置和补充燃料。源码核对、构建和专用服务器正常启停已完成；唯一一次专项7项中2项通过、5项失败，新增专项脚本已按负责人再次强调的精简要求撤下，未重跑，不计为验收通过。炉鼎消费事件仍待D阶段接入，异火材料、投掷与武器触发等主体玩法未迁移，不将系统标为完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围101个元素，均已完成归属阅读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`effect_023`、`effect_024` 的跨系统施加、查询和移除统一使用 combat 的 `StatusEffects`，持续行为归 yihuo。skill 的火箭只施加阳火，不提供 `fireAttackUUID`；不能把火箭射手自动补为持续伤害来源。已对照 `typed_field_accesses.csv` 核对两个持续过程：玩家只读 `tizhiitem` 中的 `fireAttackUUID`/`fireteam`，通过 xiulian 的 `Cultivation.tizhiString` 查询；非玩家从本体持久数据查询。`yanghuotick`/`yinhuotick` 由本系统保管在实体持久数据中，不在效果移除时清零。

丹火物品仍归 skill，火块和燃料归 yihuo。`Yihuo.useDanFire(UseOnContext)` 负责丹火的上表面放置、施放者校验和向 `blockfire` 标签方块补充燃料；调用者为 skill 的丹火物品。`FireHeatEvent` 把每二十刻的热量交给上方装置所有者；`FireExhaustedEvent` 请求拥有 `danludinglu` 装置的系统按剩余 `lianpower` 与 `gengxin` 决定是否保留火块。事件处理者写入自己的装置，本系统只写火块燃料和来源身份；两条装置消费链仍待 D 阶段真实炉鼎接入，不计为已完成。

丹火放置另有一处 `friendlist` 外部读取（`typed_field_accesses.csv` 第 931 行，来源过程第 78 行），经 relation 的 `Relations.friendlyTeams` 取得只读视图，保留来源重载前共用引用、重载后独立的时序；火块敌友判定经 `Relations.isFriendly(集合, 实体)`。来源放置读取实体 NBT 的 `team`，`PlayerJoinGame` 登录时把玩家能力团队写入该字段，使用 `Relations.entityTeam` 和本系统所属的登录复制保留载体区别。异火主体的其余外部读写点随下一批核对，本批不将系统标为完成。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
