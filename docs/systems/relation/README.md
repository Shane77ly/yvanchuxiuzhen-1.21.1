---
topic: relation
role: entry
status: partial
updated: 2026-11-07
---

# 关系

标识 `relation`，类别：系统。职责：组队、好友、敌友判定（[系统设计](../../development/system-design.md)）。

当前阶段：敌友判定、团队与阵营、团队招募令、好友界面与好友申请界面已实现（`system/relation`），开发客户端检查 43 项通过（2026-11-03）。台账：`ling_15`、`friendgui`、`zuduiteam_gui` 为“行为已迁移”；`gui_0301` 入口不可达（Q20），为“待处理”。

- [架构与调查正文](architecture.md)：来源数据与行为、归属阅读、目标实现、来源问题 Q19（保持来源行为、待再议）与 Q20、验证。
- 候选归属 86 个元素全部已读；`FindTheEnemy` 改归 combat。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

已对照 `typed_field_accesses.csv` 的 `team`、`friendlist` 字段与 `nbt_key_calls.csv` 的实体 NBT `team`、`friendlist` 覆盖全部外部读写点。代码为 `system/relation/api/Relations`。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 查询团队 | `team(实体)`：玩家读关系附件，其他实体读阵营附件 | 召唤与投射物生成时复制施放者团队的技能、符箓、妖兽、法宝、蛊虫、灵兽袋等（约 120 处） |
| 查询实体字段团队 | `entityTeam(实体)`：单独读实体阵营附件；登录时按来源写入玩家的实体团队 | yihuo 丹火放置，保留玩家能力字段与本体字段的区别 |
| 敌友判定 | `isFriendly(判定方, 对方)`：判定方的友方集合含对方团队 | combat（`EntityHurt`、`EntityBeforeHurt`、`ShanghaiGo`）、skill、fulu、creature、guchong、equipment 法宝等经 `Friendpanding*` 的全部调用 |
| 方块友方查询 | `friendlyTeams(主人)` 返回只读视图；`isFriendly(集合, 对方)` 按对方当前团队判断 | yihuo 火块保留来源共享友方引用，序列化与重载时保存为独立集合；不允许调用者修改关系 |
| 继承阵营 | `copyAllegiance(主人, 非玩家实体)`：团队与友方集合整体复制 | 同“查询团队”的召唤与投射物生成 |
| 设置团队 | `setTeam(非玩家实体, 团队名)` | 只写 `team` 的生成过程、creature 的 `EntityTeamShezhi`（团队名为实体类型 ID，`:` 改为 `_`） |
| 加入友方 | `addFriendlyTeam(非玩家实体, 团队名)` | `EntityTeamShezhi` 把自身团队加入友方集合 |

约定：公开写操作只用于非玩家实体，对玩家调用时抛出异常；玩家的团队与好友只由本系统的登录和界面流程维护。方块实体敌友查询已随 yihuo 首个调用者接入，读取者持有只读视图，不反写玩家关系。

## 未实现

- `gui_0301` 改团队名界面：入口不可达（Q20），待裁定。
- 陶罐、旗母、副本方块的阵营保存与消费随各所有者接入，当前只接回 yihuo 火块。
- 好友行的境界名：xiulian 提供境界查询后接入（当前为初始境界“凡人”）。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
