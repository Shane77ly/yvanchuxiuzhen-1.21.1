---
topic: world
role: entry
status: partial
updated: 2026-11-11
---

# 世界

标识 `world`，类别：系统。职责：维度、群系、地物、结构投放（[系统设计](../../development/system-design.md)）。

2026-11-11 扩大批次接入32种矿物、33种普通配置型地物、24种模板，以及41个来源群系中的28棵内联树、8种内联圆盘、171个内联投放和16种树装饰器；新增温泉完整流体族和随机结构主世界开关。89种独立Feature登记“已注册”，完整群系与人间道、妖魂道维度仍未装配，不能据此认定自然供给完成。具体规则见[正文](architecture.md#自然供给地物与随机结构2026-11-11)，本批运行结果与未验收范围见[月志](logs/2026-11.md)。

当前阶段：D自然供给的World生成依赖已部分实现，继续闭合人间道、妖魂道及41个来源群系；89种独立地物和207份群系内联生成资源已接，完整维度与自然供给尚未完成。2026-11-07接入的正式虚空维度、维度类型及 `xvkongqvnxi` 群系是已完成的技能依赖批次，转换与规则见[正文](architecture.md#正式虚空维度2026-11-07)；其build/ArchUnit、专服正式维度加载、区块生成及stop保存退出结果不替代本批验证。本批客户端343项INFO、0 FAIL、DONE，专服七个受控标记成功并原生stop保存四维度；最终完整构建通过，详细证据见[月度记录](logs/2026-11.md)，历史索引见[变更记录](changes.md)。

同日沿用现有客户端检查，正式维度下裂隙传送/返回12项通过；直接触发与真实碰撞各自的覆盖及未验收范围见[技能正文](../skill/architecture.md#虚空裂隙2026-11-07)，不据此升级整个世界系统的状态。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 来源归属与阅读范围见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)，已读条数不作为功能验收数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

其他系统通过原生服务端查找 `yvanchuxiuzhen:xvkong`，其类型为同名维度类型，唯一群系为 `yvanchuxiuzhen:xvkongqvnxi`。

2026-11-11 自然地物批次新增 World 自有操作，已完成启动装配，运行覆盖由本批月志分别登记：

- `WorldGeneration.randomStructuresAllowed(MinecraftServer)`：查询主世界共享随机结构开关，对应来源`!MapVariables.jiegoushengcheng`，初始允许。
- `WorldGeneration.setRandomStructuresAllowed(MinecraftServer, boolean)`：唯一写入入口，更新不可变Settings并标脏；其他系统不得另存或直接修改同值。
- `WorldGeneration.register()`：启动时安装主世界`LevelEvent.Load`和服务端结束监听。主世界加载且初始化出生区块前读取SavedData，通过线程安全服务端索引发布；地物生成线程只读取volatile Settings，结束后移除索引。
- `WorldGenerationCommands.register()`：安装实际原初管理命令中的两个结构分支，语法`/yvanchu <当前维度玩家显示名> unlock|lock jiegoushengcheng [true]`，权限等级1。来源没有`go`字面子命令。

已对照 `typed_field_accesses.csv`：`XvkongDimension`、`XvkongqvnxiBiome`没有玩家或世界持久字段读写；技能裂隙的返回编号`tizhiitem.weidu`仍由xiulian公开操作管理。随机结构字段包含`Suijijiegou01FuJiaDeShengChengTiaoJianProcedure`一次条件读取，以及`YvanchuGoProcedure`的unlock/lock两次写入，分别由六个地物查询与管理命令调用World拥有者承担。其余世界状态、结构操作与外部字段访问随对应批次列出；world不写玩家档案。

本轮变化记录在[本系统月志](logs/2026-11.md)与[项目月志](../project/logs/2026-11.md)。
