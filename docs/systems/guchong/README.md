---
topic: guchong
role: entry
status: partial
updated: 2026-11-13
---

# 蛊虫

标识 `guchong`，类别：系统。职责：蛊虫实体、蛊罐、认主与释放（[系统设计](../../development/system-design.md)）。

当前阶段：B1 身份、资源与基础属性已登记（方块 3、物品 13、声音事件 2）；D/E 批次已接入 `guchong_03`、`guchong_04` 实体和三档蛊罐方块初始实现。R05/R10已实现摄虫网公开收纳操作，捕获结果以个体组件接入既有蛊罐菜单和编号槽保存，最小单项已通过；运行边界见[正文](architecture.md#2026-11-13-摄虫网收纳与蛊罐消费者)。地上罐与手持罐全生命周期、单虫物品全部入口及蛊虫系统验收仍未完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 73 个元素，其中 56 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

蛊虫真值由本系统维护：个体数据随实体保存，手持罐槽位数据随物品保存，地上罐库存与释放计数随方块实体保存。释放、回罐、死亡清槽和菜单操作为系统内部操作，不允许外部直接修改罐或虫的持久数据。`GuchongCapture.captureWild(服务端世界, 目标实体, 主人UUID)` 是 `equipment` 的摄虫网收纳操作：由本系统判定认主状态、按来源实体ID寻找对应物品、保存个体字段、交给主人并移除目标；返回已捕获、已认主或无对应物品，法宝只负责时序与粒子反馈。未找到主人时仍移除可捕获目标，保持来源顺序；不增加虫标签过滤。该操作未读取或写入其他系统玩家字段，对照 `typed_field_accesses.csv` 的017两个过程无外部玩家能力读写。运行与存档验证边界见[正文](architecture.md)。

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
