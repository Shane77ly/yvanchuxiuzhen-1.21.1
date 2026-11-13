---
topic: book
role: entry
status: partial
updated: 2026-11-13
---

# 书籍

标识 `book`，类别：系统。职责：公共书籍、阅读、答题、装订（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现（2026-11-13）。B1 已登记方块 1、物品 37 的身份、资源与基础属性；36 公共载体已接回技能/功法潜行研习、成功清答题计数与书架藏宝自然分派，历史有效验证见[自然使用正文](architecture.md#自然研习入口2026-11-08)。本批补齐同一载体族的普通阅读：文本书、功法、四类技能、图纸九项材料与丹方八项药草共用来源无槽、无按钮的十一行书页；保留文本书潜行符箓命令分派及来源显示细节。定向转换、编译和六条架构守卫已通过；`bookreading` 单项70条玩法检查、0失败，正常保存退出，五张中文截图已核，结果见[书籍月志](logs/2026-11.md)。来源版本实机、两版本对照、完整 36 逐载体验收未执行。装订台已接回六线一木板配方、自然放置与打开、固定24空白载体目录、每次3纸1线、关闭清预览、38槽保存和掉落；定向转换、编译和六条架构守卫通过，`bookbinding`单项64条玩法INFO、0失败、正常保存退出，三张中文截图已核。首轮自掉落漏项已按来源最小修复，失败与复验边界见[书籍月志](logs/2026-11.md#2026-11-13-装订台自然制作与生命周期)，规则见[装订正文](architecture.md#装订台2026-11-13)。完整答题、文本/符箓学习载体的获取生产及出生/宝箱获取链仍开放；`makebook` 是装订台方块物品。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 70 个元素，其中 55 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本批不新增供其他系统调用的 book API。公共书物品 `StudyBookItem` 经 `Techniques.book/study/giveTreasureBook` 和 `Skills.book/study/giveTreasureBook` 查询、分派研习与藏宝发书；随机表、境界与灵根判断留在原业务操作。书架藏宝的来源实例键由 `minecraft:custom_data` 承载，具体契约见正文。答题进度 `daduishuliang` 由 book 唯一写入，本批仅接成功清零，不建立 skill/gongfa 的答题数据写入口。

阅读通过 `Techniques.book/main` 和 `Skills.book` 查询书实例与主修档位；技能品阶/境界中文经 `SkillBook.gradeText/levelText` 委托既有计算，避免复制公式。对照 `typed_field_accesses.csv` 中 `Bookwenzi4`–`Bookwenzi10` 对 `gongfa` 的全部读取，目标只经 `Techniques.main` 取主修。图纸和丹方显示读取其生产者已经写入 `minecraft:custom_data` 的根字段，不改变配方或装备真值；文本行 `text01` 起由 book 在阅读前写入被使用的真实物品实例并同步。

已对照 `typed_field_accesses.csv` 中 `BookRightClick` 的修炼字段读取，并补核该索引未收录的实体持久字段：`daduishuliang` 的其他真实消费者是答题四选项与题数显示，完整调用链留待 book 批次。公共书不直接改写功法/技能组件、装配附件或修炼档案。装订台菜单、目录与库存由book自身管理，不新增跨系统操作；`typed_field_accesses.csv`中没有`Makebook`系列的外部字段读写，实际过程只操作当前菜单的38个槽。

- [变化索引](changes.md)与[书籍月志](logs/2026-11.md)记录本批结果；先前研习与藏宝批次证据仍在[项目月志](../project/logs/2026-11.md)。
