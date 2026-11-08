---
topic: book
role: entry
status: partial
updated: 2026-11-08
---

# 书籍

标识 `book`，类别：系统。职责：公共书籍、阅读、答题、装订（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现（2026-11-08）。B1 已登记方块 1、物品 37 的身份、资源与基础属性；本批为 `book_001`–`book_036` 接回潜行右键的技能/功法研习分派及成功后的答题计数清零，沿用两个业务所有者的公开操作。本批完整构建及现有skillui客户端29项通过，已覆盖四类技能和功法的自然研习、境界失败、本人重复与整栈/计数同步；完整证据与未覆盖项见正文。文本、普通阅读、书架藏宝自然使用、图纸/丹方说明、符箓分派、完整答题和装订仍未实现，`makebook` 是装订台方块物品，不是公共研习书。规则与证据边界见[自然研习正文](architecture.md#自然研习入口2026-11-08)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 70 个元素，其中 38 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

本批不新增供其他系统调用的 book API。公共书物品 `StudyBookItem` 只经 `Techniques.book/study` 和 `Skills.book/study` 查询、分派研习；境界与灵根判断留在原业务操作。答题进度 `daduishuliang` 由 book 唯一写入，本批仅接成功清零，不建立 skill/gongfa 的答题数据写入口。

已对照 `typed_field_accesses.csv` 中 `BookRightClick` 的修炼字段读取，并补核该索引未收录的实体持久字段：`daduishuliang` 的其他真实消费者是答题四选项与题数显示，完整调用链留待 book 批次。公共书不直接改写功法/技能组件、装配附件或修炼档案。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
