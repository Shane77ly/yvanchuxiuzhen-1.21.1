---
topic: zhenfa
role: entry
status: partial
updated: 2026-11-15
---

# 阵法

标识 `zhenfa`，类别：系统。职责：阵位、阵元、阵旗、阵法效果（[系统设计](../../development/system-design.md)）。

当前状态：阵元、阵基、母旗/子旗与罗盘已接入方块实体、内容工厂和来源资源；阵基布局扫描、供能、阵旗绑定/控制、阴1–4、空间1–5、阳1–4及五行来源复制分支已实现接线。阳系三实体、箭物品、原生模型与Geo渲染已接，模型表与资产正式定向转换已完成，目标检查按本月实际记录接续；五行继续保持Q04的费用/名称错配，伤害标记继续保持Q31消费断链，不代表已获得推测的五行效果。此前内容/资源转换、`compileSmokeJava`和`formation` Smoke已通过；报告归档于`build/diagnostics/r15-zhenfa-formation-20261114/`，含54条INFO、0 FAIL、DONE，保存重进和正常退出完成。本批空间8项及阳/五行19项受控必要检查通过，定向转换工厂回归已修复；阳四19×19破坏、自然阵基全链、后期消费者、跨维度、多人、1.18.2版本实机或完整表现仍未验。Q148经来源父类复核，确认六面及空方向能力均属来源行为，目标保持共库。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 59 个元素，其中 40 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

阵元BlockEntity公开 `material()`、`disguise()`、`formationOrigin()`、`displayAngle()` 查询，`assignFormation(origin, angle)`用于阵基检测写入实例坐标与展示角；`handler(direction)`返回对应方向的物品能力。`FormationInspection.report(level, inspected, rank, center)`由空间三和 flow 手动命令入口共用并向检测坐标附近玩家发送来源可达的名称、境界、属性、功法与修炼参数反馈；灵根分支保持来源不可达。阵元仍为数据所有者，其他系统不得直接写入其库存或持久标签。后续接入继续对照 `typed_field_accesses.csv` 更新完整跨系统访问。

`FormationProtection.amount(blockEntity)`查询阳二附着的罡气计数，`add(blockEntity, int)`按来源整数累加；该附件属于阵法并随目标方块实体持久化，没有另外的减伤消费者。阳系目标的临时伤害字段交combat公开接口登记，实体团队经relation公开操作复制，均不直接修改其他系统的数据对象。

本轮变化见[本月日志](logs/2026-11.md)，Smoke 使用 -PsmokeOnly=formation；跨系统决定见[项目月度日志](../project/logs/2026-11.md)。
