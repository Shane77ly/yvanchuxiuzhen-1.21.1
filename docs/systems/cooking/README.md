---
topic: cooking
role: entry
status: partial
updated: 2026-11-09
---

# 烹饪

标识 `cooking`，类别：系统。职责：食材、厨具、菜谱、食用效果（[系统设计](../../development/system-design.md)）。

当前阶段：D 部分实现。除 B1 内容身份与资源及辟谷效果 `effect_031` 外，已接回铁锅、砂锅的14槽库存/菜单、两锅自然合成配方、菜谱执行路径和墨料坯自然生产；完整食材与成品获取链未闭合。2026-11-09 已用内置客户端 `fulucooking` 场景验收铁锅自然右键开窗、墨料坯开始/10刻交付/五次翻炒完成与保存重读，并验收砂锅有菜谱烹饪（食材扣一件、产出覆盖槽4、菜谱不消耗、完成后厨艺经验加5），该场景含符箓共用共14项全部通过；蒸锅、自然菜谱获取、专服与来源版本对照尚未验收。蒸锅/笼屉、自然菜谱获取与食用等范围仍未实现。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 294 个元素，其中 236 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [源码证据](ledger/source-evidence.csv)：本批两锅配方、菜谱开始过程与墨料坯生产的来源对应；未覆盖项目范围内其余烹饪内容。
- [记录索引](changes.md)：按月查看已实现范围与验证证据。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

炼丹通过原生状态效果入口施加 `effect_031`；`baoshidu` 计时由 cooking 独占写入，不暴露直接写入口。已查 `typed_field_accesses.csv`，该实体持久键不在强类型字段表；`key_expressions.csv` 与来源代码确认读写仅在 `Effect031ZaiXiaoGuoChiXuShiMeiKeFaSheng`。厨具库存与生产状态由 cooking 独占写入；其他系统不得直接改写方块实体或菜谱状态。材料实例只通过 `Materials.instance` 查询，职业只通过 `Professions.level` 查询并以 `Professions.addExperience` 写入经验；覆盖 `typed_field_accesses.csv:1304–1309` 的外部读写点。

两锅批次实现中：`fooddaguo`、`fooddunguo` 的14槽库存、烹饪状态与坐标延迟由 cooking 独占写入；菜单按钮只提交按钮编号，服务端验证当前菜单绑定的方块实体。材料等级只读 `Materials.instance`，厨艺等级读取 `Professions.level`，完成经验通过 `Professions.addExperience` 写入并同步；覆盖 `typed_field_accesses.csv:1304–1309` 两锅对 `cooking_LEVEL` 和 `cooking_experience` 的全部外部读写。不存在 cooking 对职业附件或材料实例的直接改写。菜谱当前读取物品 `CUSTOM_DATA` 中已核来源字段；完整食材与成品获取链及菜谱自然获取尚未闭合。已读来源算法按 `forge:cailiao` 槽位平均材料实例等级；来源帮助文案称“同级”材料，与实际算法不完全一致，目标保留算法并记录该来源差异。墨料坯生产静态代码复核未发现公式、扣料和10刻交付时序偏差；铁锅墨料坯与砂锅菜谱路径已有内置客户端验收，专服未验收。

本轮变化记录在[系统月度日志](logs/2026-11.md)，项目进度见[项目月度日志](../project/logs/2026-11.md)。
