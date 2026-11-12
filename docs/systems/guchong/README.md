---
topic: guchong
role: entry
status: partial
updated: 2026-11-12
---

# 蛊虫

标识 `guchong`，类别：系统。职责：蛊虫实体、蛊罐、认主与释放（[系统设计](../../development/system-design.md)）。

当前阶段：B1 身份、资源与基础属性已登记（方块 3、物品 13、声音事件 2）；D/E 批次已接入 `guchong_03`、`guchong_04` 实体和三档蛊罐方块初始实现。正在按来源闭合地上罐与手持罐生命周期；当前注册、编译边界与完整玩法状态分别登记，尚未完成蛊虫系统验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 73 个元素，其中 56 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

蛊虫真值由本系统维护：个体数据随实体保存，手持罐槽位数据随物品保存，地上罐库存与释放计数随方块实体保存。当前对外仅提供启动注册及实体类型查询；释放、回罐、死亡清槽和菜单操作为系统内部操作，不允许外部直接修改罐或虫的持久数据。`equipment` 的 `fabao_01701` 收纳入口仍待本系统提供已核对的具体操作，未以占位接口宣布闭合。

外部字段已逐项核对 `docs/reference/legacy/inventory/typed_field_accesses.csv`：

| 来源读取或写入 | 目标公开操作 | 核对范围 |
| --- | --- | --- |
| A `Taoguan01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:39,70` 的玩家 `friendlist/team` | `Relations.shareAllegianceToBlock/team` | 放置时使用玩家关系能力；与玩家本体 `team` 区分 |
| A `Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:62,64,183` 和 `GuchongAnniuzhixingProcedure:57,59,120,204` | `Relations.shareEntityAllegiance/team` | 手持释放与回收已接入；释放按钮链待核，不能读取 relation 数据包 |
| A `Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:39,85,131` | `Relations.team` | 同族物品操作仍待本轮继续闭合 |
| A `Guchong02itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:15,27` 的 `tizhiitem` | xiulian 具体体质操作 | 未在本轮实体与地上罐批次中实现 |
| B `Taoguan01ZaiFangKuaiShangYouJianProcedure:182,183` 的 `lctrl/lalt` | `ModifierKeys.ctrl/alt` | 回收地上罐 |
| B 地上罐认主与目标筛选直接读取实体本体 `team` | `Relations.entityTeam` | 保留本体字段与玩家能力字段的来源差异 |

罐 `friendlist` 的共享引用由 `Relations.shareAllegianceToBlock/shareBlockAllegiance` 承担；不得绕过接口写其他系统附件。

本轮变化见[蛊虫月志](logs/2026-11.md)，此前变化在[项目月度日志](../project/logs/2026-11.md)。
