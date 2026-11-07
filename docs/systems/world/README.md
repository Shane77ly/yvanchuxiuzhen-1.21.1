---
topic: world
role: entry
status: partial
updated: 2026-11-07
---

# 世界

标识 `world`，类别：系统。职责：维度、群系、地物、结构投放（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（方块 1、物品 9）。2026-11-07 接入 skill 所需的正式虚空维度、维度类型和 `xvkongqvnxi` 群系数据，并撤下同 ID 的平坦基岩测试替身；其余世界主体仍随 E 阶段迁移。转换与规则见[正文](architecture.md#正式虚空维度2026-11-07)，build/ArchUnit及一次专服正式维度加载、区块生成和stop保存退出通过；自然裂隙往返与视觉未验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 106 个元素，其中 19 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

当前虚空批次只提供数据包注册契约：其他系统通过原生服务端查找 `yvanchuxiuzhen:xvkong`，其类型为同名维度类型，唯一群系为 `yvanchuxiuzhen:xvkongqvnxi`。没有新增 Java 查询或写接口。

已对照 `typed_field_accesses.csv`：`XvkongDimension`、`XvkongqvnxiBiome` 均没有玩家或世界持久字段的读写记录；技能裂隙的返回编号 `tizhiitem.weidu` 仍由 xiulian 的公开操作管理，world 不写玩家档案。其余世界状态、结构操作与外部字段访问在对应 E 批次逐项列出，本批不引入尚未使用的接口。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
