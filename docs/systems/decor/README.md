---
topic: decor
role: entry
status: partial
updated: 2026-11-12
---

# 装饰

标识 `decor`，类别：内容族。职责：建筑方块、家具、牌匾、牌位（[系统设计](../../development/system-design.md)）。

2026-11-11 World供给批次接入青石矿物Feature的来源生成机制与同名configured/placed资源，台账登记“已注册”；自然投放随来源维度和群系闭合。规则与验证见[世界正文](../world/architecture.md#自然供给地物与随机结构2026-11-11)及[世界月志](../world/logs/2026-11.md)。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 514、物品 537、声音事件 1），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。2026-11-12完整来源复核将流沙方块及源/流态3项归入World，因其为地表流体族并被妖魂道地表规则直接引用；本内容族不再拥有该注册。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 1047 个元素，其中 700 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

本轮变化记录在 [本系统月志](logs/2026-11.md)与[项目月度日志](../project/logs/2026-11.md)。

2026-11-10 工艺归属复核将五份cailiao_211加两木棍制作建筑锤的 `Hammerhecheng` 移回本内容族，随已读Hammer及瓦片形态切换规则推进；配方数据已转换，建筑锤行为仍待本内容族实现。
