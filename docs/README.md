---
topic: documentation-index
role: entry
status: current
updated: 2026-11-01
---

# 架构设计入口

已确认：完整移植两个旧 Mod；全新独立项目；Minecraft 1.21.1 + NeoForge + Java 21；合并成一个 Mod。当前仅设计与静态基线，游戏实现为零。

| 阅读顺序 | 文档 | 解决的问题 |
| --- | --- | --- |
| 1 | [范围与保真原则](design/scope.md) | 什么算完整移植，哪些旧工程行为需要裁定 |
| 2 | [整体架构](development/architecture.md) | 系统边界、依赖方向、包结构、集成方式 |
| 3 | [数据与运行时](development/data-and-runtime.md) | 存储、加载、修改、同步、消费、保存与生命周期 |
| 4 | [内容与对齐流程](development/content-and-parity.md) | 注册 ID、资源、数值、菜单、完整性台账 |
| 5 | [实施与验收顺序](development/roadmap.md) | 如何逐批完成全部范围，避免长期半成品 |
| 6 | [旧工程证据入口](reference/legacy/README.md) | 注册项、玩家字段和来源文件指纹 |
| 7 | [目标 API 核对](reference/target-api.md) | 方案使用的本地 NeoForge 依据与待验证点 |
| 8 | [项目状态与决定](systems/project/README.md) | 已确认事项、待裁定事项、月度记录 |

文档按[维护规范](development/documentation.md)维护。新项目没有继承已有工程的移植完成度；静态提取数量也不代表玩家可用内容数量。
