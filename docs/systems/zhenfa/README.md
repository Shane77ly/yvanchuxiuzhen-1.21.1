---
topic: zhenfa
role: entry
status: partial
updated: 2026-11-14
---

# 阵法

标识 `zhenfa`，类别：系统。职责：阵位、阵元、阵旗、阵法效果（[系统设计](../../development/system-design.md)）。

当前状态：阵元、阵基、母旗/子旗与罗盘已接入方块实体、内容工厂和来源资源；阵基布局扫描、供能、阵旗绑定/控制及阴1–4已实现。内容与资源定向转换、`compileSmokeJava` 和 `formation` Smoke 已通过；报告归档于`build/diagnostics/r15-zhenfa-formation-20261114/`，含54条INFO、0 FAIL、DONE，保存重进和正常退出完成。仅登记本批必要覆盖，不代表来源全分支、多人、跨维度、1.18.2版本实机或完整表现验收。Q148经来源父类复核，确认六面及空方向能力均属来源行为，目标保持共库。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 59 个元素，其中 36 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

阵元BlockEntity公开 `material()`、`disguise()`、`formationOrigin()`、`displayAngle()` 查询，`assignFormation(origin, angle)`用于阵基检测写入实例坐标与展示角；`handler(direction)`返回对应方向的物品能力。阵元仍为数据所有者，其他系统不得直接写入其库存或持久标签。后续阵基检测接入前需继续对照 `typed_field_accesses.csv` 更新完整跨系统访问。

本轮变化见[本月日志](logs/2026-11.md)，Smoke 使用 -PsmokeOnly=formation；跨系统决定见[项目月度日志](../project/logs/2026-11.md)。
