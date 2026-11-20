---
topic: book
role: entry
status: partial
updated: 2026-11-17
---

# 书籍

2026-11-15 答题完成已补来源通用根归属写入（`belongToMasterUUID`、`belongToName`）；来源答题自然打开断链登记为 Q208，文本/符箓学习载体生产断链见 Q209。2026-11-17新增`BookTexts.create`文本书构造操作，出生奖励接入与自然阅读验收由修炼模块继续完成。

标识 `book`，类别：系统。职责：公共书籍、阅读、答题、装订（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现（2026-11-15）。B1 已登记方块 1、物品 37 的身份、资源与基础属性；36 公共载体已接回技能/功法潜行研习、成功清答题计数与书架藏宝自然分派，历史有效验证见[自然使用正文](architecture.md#自然研习入口2026-11-08)。本批补齐同一载体族的普通阅读：文本书、功法、四类技能、图纸九项材料与丹方八项药草共用来源无槽、无按钮的十一行书页；保留文本书潜行符箓命令分派及来源显示细节。定向转换、编译和六条架构守卫已通过；`bookreading` 单项70条玩法检查、0失败，正常保存退出，五张中文截图已核，结果见[书籍月志](logs/2026-11.md)。来源版本实机、两版本对照、完整 36 逐载体验收未执行。装订台已接回六线一木板配方、自然放置与打开、固定24空白载体目录、每次3纸1线、关闭清预览、38槽保存和掉落；定向转换、编译和六条架构守卫通过，`bookbinding`单项64条玩法INFO、0失败、正常保存退出，三张中文截图已核。首轮自掉落漏项已按来源最小修复，失败与复验边界见[书籍月志](logs/2026-11.md#2026-11-13-装订台自然制作与生命周期)，规则见[装订正文](architecture.md#装订台2026-11-13)。答题自身40题、菜单、服务端按钮及功法/四类技能答满写入已实现；定向转换、编译和六条架构守卫通过，`bookquiz`首轮96条答题检查0失败，真实保存重进及正常退出、四张中文截图已核。答满任意主手栈通用归属已按来源写入；来源没有自然打开调用，Q208与37号重置契约仍开放，见[答题正文](architecture.md#答题2026-11-13)。2026-11-15新增`ShelfBooks`所有者构造入口并供落魄家族完整初始包生产两本藏宝书，主线flow注册与编译/实机待执行，见[修炼初始奖励正文](../xiulian/architecture.md#落魄家族初始奖励2026-11-15)。文本/符箓学习载体、其余出身及宝箱完整获取链仍开放；`makebook` 是装订台方块物品。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 70 个元素，其中 67 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

轮回可调用 `BookQuizzes.clearQuestion` 清除题号并同步，保留答对计数。

`ShelfBooks.technique(double等级)`与`skill(double等级)`供flow构造`book_021`书架藏宝载体，写原中文名和三个来源藏宝字段；不抽取具体功法/技能内容。`BookTexts.create(Item载体, String中文名, String原文)`由书籍所有者构造文本书，保留载体、名称和根`textdisplay`来源字段。公共书物品 `StudyBookItem` 经 `Techniques.book/study/giveTreasureBook` 和 `Skills.book/study/giveTreasureBook` 查询、分派研习与藏宝发书；随机表、境界与灵根判断留在原业务操作。书架藏宝的来源实例键由 `minecraft:custom_data` 承载，具体契约见正文。答题进度 `daduishuliang` 由 book 唯一写入，由book维护累计、答错/答满清零及仅本人同步；skill/gongfa只接答满后的所属书组件写入，不拥有答题数据。

阅读通过 `Techniques.book/main` 和 `Skills.book` 查询书实例与主修档位；技能品阶/境界中文经 `SkillBook.gradeText/levelText` 委托既有计算，避免复制公式。对照 `typed_field_accesses.csv` 中 `Bookwenzi4`–`Bookwenzi10` 对 `gongfa` 的全部读取，目标只经 `Techniques.main` 取主修。图纸和丹方显示读取其生产者已经写入 `minecraft:custom_data` 的根字段，不改变配方或装备真值；文本行 `text01` 起由 book 在阅读前写入被使用的真实物品实例并同步。

已对照 `typed_field_accesses.csv` 中 `BookRightClick` 的修炼字段读取，并补核该索引未收录的实体持久字段：`daduishuliang` 的其他真实消费者是答题四选项与题数显示，现由`BookQuiz`承接自身调用链；题面六字段及选项也已对照该索引归book所有。公共书不直接改写功法/技能组件、装配附件或修炼档案。装订台菜单、目录与库存由book自身管理，不新增跨系统操作；`typed_field_accesses.csv`中没有`Makebook`系列的外部字段读写，实际过程只操作当前菜单的38个槽。

- [变化索引](changes.md)与[书籍月志](logs/2026-11.md)记录本批结果；先前研习与藏宝批次证据仍在[项目月志](../project/logs/2026-11.md)。
