---
topic: festival
role: entry
status: partial
updated: 2026-11-11
---

# 节日

标识 `festival`，类别：内容族。职责：烟花爆竹等节日物品（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（台账“已注册”：方块 4、物品 18、声音事件 7），只表示身份、资源与基础属性就位；玩法尚未实现，未进行游戏验收。

2026-11-11 D/E 自然供给依赖批次已实现财神 `caishenye` 的实体、属性、刷怪蛋工厂、闲逛AI、交互转身、死亡粒子与死亡第1刻移除，以及GeckoLib客户端渲染。自然生成条件保留来源 `Calendar.MONTH == 2`（服务器本机公历三月）。正式内容、资源、语言及客户端入口已接入，完整构建与客户端/专服基础加载完成；自然出生、实际交互、死亡表现、存档重载仍未专项验收，World群系引用尚未安装。本族其余节日内容仍未实现，不能据此登记整个Festival完成。规则与边界见[财神正文](architecture.md#财神2026-11-11)、[本月日志](logs/2026-11.md)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 54 个元素，其中 43 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本族没有供其他系统读取或修改的持久数据。World按注册ID和来源追加顺序声明财神生成项，Festival独立注册出生条件；其他系统不引用本族实现类。`Caishenye`及三条过程在`typed_field_accesses.csv`没有记录，源码亦没有玩家附件、其他系统档案或方块实体的强类型读写。死亡只通过`particle.api.Particles.type`查询粒子后使用服务端原生广播；交互仅修改本实体朝向。

`FestivalSetup.register`为公共装配入口，`FestivalClientSetup.register`为客户端装配入口；它们属于Mod入口调用，不构成跨系统玩法接口。

本轮变化记录在[本月日志](logs/2026-11.md)；此前记录见[项目月度日志](../project/logs/2026-11.md)。
