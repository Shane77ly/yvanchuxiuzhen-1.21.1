---
topic: dungeon
role: entry
status: partial
updated: 2026-11-14
---

# 副本

标识 `dungeon`，类别：系统。职责：副本入口、实例、波次、进出与回程（[系统设计](../../development/system-design.md)）。

当前阶段：天道入口/管理、候场实例、控制器/占位符、波次与回程、奖励、私人画卷及四个原生菜单/渲染已写入本批代码。默认1份、镇妖塔44份定义确定性提取并校验。主线工厂、转换和客户端接线已完成，编译与6项架构检查通过；1.21.1版本已验证自然放置、四页操作、首次候场/三波受控清敌/奖励/成功回程。第二次自然入场、受控超时回程和私人画卷放取/双击往返也通过，前轮二次入场超时原因保留Q111待核；具体有效结果与未验范围见[本月志](logs/2026-11.md)。来源异常、奖励字段优先级与依赖见[正文](architecture.md)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 49 个元素，其中 49 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [本月记录](logs/2026-11.md)与[记录索引](changes.md)：本批证据和未执行范围。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本批对照 `typed_field_accesses.csv` 的 `fubenweizhi`、`datingshengcheng`、`tizhiitem` 副本字段、玩家 `LEVELnumber` 与 `teamwork` 读写点。副本玩家附件保存入场倒计时、定义、控制点、入场点、回程点与原游戏模式；全服副本序号/候场大厅生成标记及各原世界画卷序号由本系统存盘。入口、控制器与占位符保留来源独立方块实体字段和库存。修炼境界通过 `Cultivation.get` 读取，玩家能力临时队伍与本体临时队伍通过 `Relations` 分别操作，结构由 `WorldStructures.place` 放置，刷怪等级与攻击者由 `DungeonCreatures.prepare` 写入。

| 操作 | 形式 | 调用者 |
| --- | --- | --- |
| 打开入口 | `Dungeons.open(玩家, 坐标, 管理模式)`；服务端绑定当前入口与菜单 | 天道副本右键/碰撞 |
| 进入副本 | `Dungeons.enter(玩家, 坐标)`；保留队伍、门票、模式、实例序号与维度顺序 | 来源进入按钮 |
| 镇妖塔定义补充 | `Dungeons.configureTower(世界)`；按来源44个固定点写完整定义与楼板 | 已订阅World结构释放40刻后事件；管理命令已接入，寻灵盘/裂隙自然入口留R06；与50→5001尸体封存无关 |

上述为本批实际实现契约；验证完成前不提升系统整体状态。

本系统变化记录在[本月志](logs/2026-11.md)。
