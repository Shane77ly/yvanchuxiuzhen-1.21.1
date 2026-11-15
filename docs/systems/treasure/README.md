---
topic: treasure
role: entry
status: partial
updated: 2026-11-15
---

# 宝箱

标识 `treasure`，类别：系统。职责：宝箱分级、奖励生成（[系统设计](../../development/system-design.md)）。

当前阶段：奖励总表30个纯过程、24类分派，01/02物品箱、04/05/06地面藏宝及03/11定时箱已实现；功法、技能、丹药、丹方、符箓和材料实例通过所属系统公开操作构造。来源指纹、定向转换、编译及六项架构检查通过；2026-11-15新增静态核验逐项确认24类分派、30个来源过程指纹、奖励所有者API及空/畸形奖励规则，报告见`build/diagnostics/r11-treasure-reward-dispatch/static-parity.json`。自然右键、钥匙、计划刻发奖与有限保存重进必要单项通过；同日完成01状态类丹药三候选的随机奖励、来源药效、原生服用、保存重进和服尽返空容器专项客户端验证，证据见[本月志](logs/2026-11.md#2026-11-15-01状态丹药消费专项客户端验证)。完整随机奖励消费、含水、手持表现及两版本对照仍待；`/lianqi`既有六项专项证据继续有效。天道工具、管理页与设置弹体已接，挥动开页/设置扣栈必要单项通过；完整自然配置与奖励消费、来源实机和视觉仍待，独立宝箱菜单继续开放，详见[正文](architecture.md#开放范围与验证边界)。

宝匣放置、指令执行、单槽库存/能力、五刻移除及来源预览已接，03/11无方向能力遗漏Q131已补；新增包的验证边界见[本月记录](logs/2026-11.md#2026-11-14-宝匣与无方向库存能力)。来源硬度冲突、预览错字段、盲移除及孤立菜单Q132–Q135集中登记，尚未裁定的行为保持来源。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 114 个元素，其中 28 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [本月记录](logs/2026-11.md)和[记录索引](changes.md)：批次结果与验证边界。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`Caskets.create(command)`由宝匣所有者创建配置物品并写入zhiling，供家族百宝袋奖励使用，放置/执行/五刻移除继续复用现有生命周期；本生产者尚未实机。

`TreasureContainers.create(Item, category, subtype, level, minimum, maximum)`由本系统构造五项宝箱参数，供Dungeon奖励使用；根字段沿用来源拼写`minimumvalue`与`Maximumvalue`。调用者可保留其他已解析字段，但不得覆盖所有者构造的参数。副本自然奖励消费的运行边界见[副本月志](../dungeon/logs/2026-11.md)。

`TreasureRewards.open`按本次调用的类别、等级与次数生成掉落，等级不写玩家档案或共享临时全局字段。实例构造分别调用`Techniques.createBook`、`Skills.createBook`、`Liandan.createMedicineReward / createRecipeReward`、`Fulu.createReward`及`Materials.setInstance`；装备奖励继续执行`/lianqi`，由equipment构造。`typed_field_accesses.csv`的`baoxianglevellinshi`仅为来源奖励表临时参数读写，目标由方法参数等价承担；本包不接入玩家持久字段。

`/baoxiangopen`与`/baoxiangset`要求权限1。前者解析来源五参数后发奖，后者对七格视线命中的方块实体写宝箱配置并报告中文结果。`/lianqi`成品入口要求命令位置的物品能力槽0为空且支持原生容器写入；经槽0读回后在原坐标掉落并清槽，返回0。来源限制与既有验证见[成品命令](architecture.md#成品命令2026-11-07)。

本轮变化记录在[系统本月志](logs/2026-11.md)。
