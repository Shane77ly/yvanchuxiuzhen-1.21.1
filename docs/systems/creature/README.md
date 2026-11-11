---
topic: creature
role: entry
status: partial
updated: 2026-11-11
---

# 生物

标识 `creature`，类别：系统。职责：妖兽、Boss、鬼怪、天魔、绿林、环境生物，驯服与灵兽袋，通用怪物技能（[系统设计](../../development/system-design.md)）。

当前阶段：D/E自然供给依赖。环境生物`organism_01`–`organism_12`整族、刷怪蛋及模型资源已接入；01–08为同构蝴蝶，09–12保留各自移动与交互分支。完整构建及专服基础加载通过，客户端资源加载冒烟完成；关闭时序警告、`organism_11`掉落表上下文警告及行为专项未验，详见[本月日志](logs/2026-11.md)。自然投放仍依赖World的41群系及人间道/妖魂道闭合。52个编号妖兽中12个已接入完整来源闭包，其余按独立批次并行实现；其余生物、灵兽袋、技能与完整生命周期未实现，不能将内容身份登记计为玩法完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 705 个元素，其中 586 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。[编号妖兽来源证据](ledger/yaoshou-source-evidence.csv)固定本批12个编号的实体及递归过程闭包身份，不代表完整行为验收。
- [变更记录](changes.md)：按日期索引本系统批次记录。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

| 操作 | 调用与数据边界 |
| --- | --- |
| `CreatureSetup.entityType(String)` | 本系统客户端装配读取已登记类型，不暴露内容表或可变实例数据；跨系统自然生成按注册ID引用 |
| `Butterflies.register / Organisms.register / YaoshouCreatures.register` | 本系统注册实体、属性、刷怪蛋工厂和原生刷怪位置；妖兽当前装配001/002/019/020/030/038/041/042/043/045/049/052；群系出生列表由World保持来源顺序及参数 |
| `ButterflyEntity.mobInteract`及09–12各自原生回调 | 服务端拥有驯服、治疗、状态和生成后代，客户端仅返回来源交互反馈和消费同步表现；实例存档沿用各自原生父类及来源自由NBT |
| `SpiritBeastBags.onCreatureDeath(Entity)` | 生物系统负责按来源袋UUID重建同维度主人首个匹配袋，并清理`zhaohuanwu == 1`召唤物；原生死亡回调在`super.die`之后调用，不反写其他系统玩家档案 |

001/002/019/020/030/038/041/042/043/045/049/052共12个编号已装入`CreatureSetup`，每个编号保留独立`YaoshouNNNEntity`类、注册ID、刷怪蛋与来源资源；[编号对应及生命周期](architecture.md#编号妖兽直接迁移2026-11-11)集中核对。同步体型属性和001/002皮肤属性、来源模型/动画、动态尺寸及纹理渲染均已接线。来源`Material.GRASS`出生条件按锁定材料快照的8种方块保留。灵兽袋死亡清理由公开入口接到这12个编号的独立死亡回调，静态来源与精确版本API已核对；背包清袋和完整战斗仍待实机验收；12编号逐刻存活与召唤死亡移除24个专服标记已通过，收纳/释放等袋本体操作尚未实现。skill 的延迟回调与毒蔓鞭生成通过[skill公开API](../skill/README.md)调用，不直接访问其实现包。

本批按`Organism01`–`Organism12`核对`typed_field_accesses.csv`未检出强类型外部玩家档案读写；原生主人、生命与本体NBT只由本系统实体修改。死亡清袋入口仅读实体来源自由NBT、袋物品自有`CUSTOM_DATA`、同维度玩家原生物品能力和原生计分板，不涉及强类型玩家档案。其他未实现灵兽袋操作和通用技能仍需逐项核对。

本批来源、验证与未完成项见[本月日志](logs/2026-11.md)；项目里程碑仍记入[项目月度日志](../project/logs/2026-11.md)。

2026-11-07 读取 `Yaoshou033skill01`、`Yaoshou040skill0201`、`Yaoshou040skill03` 后，确认三者只有原生贴图粒子表现和物理，归属与台账转入[粒子库](../particle/README.md)；妖兽的实际发出时机仍随本系统迁移。
