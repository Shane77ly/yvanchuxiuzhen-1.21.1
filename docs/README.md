---
topic: documentation-index
role: entry
status: current
updated: 2026-11-02
---

# 调查与架构评审入口

已确认：全新项目，完整移植两个 1.18.2 Mod；目标 Minecraft 1.21.1 + NeoForge + Java 21；一个可安装 Mod，内部按系统组织。整体架构、工程结构与实施路线已于 2026-11-02 获批，当前执行 B0 架构验证切片；存档导入、界面复刻程度、license 与来源矛盾 Q01–Q14 仍待裁定，未启动 goal。

| 阅读顺序 | 文档 | 用途 |
| --- | --- | --- |
| 0 | [移植技能](../.claude/skills/minecraft-mod-porting/SKILL.md) | 通用移植规则、系统工作单与参考的正文（本项目文档只记录适用与决定） |
| ★ | [交接说明](development/handoff.md) | 新会话入口：当前状态、阅读顺序、环境事实与下一步 |
| 1 | [1.18.2 版本架构调查](reference/legacy/source-survey.md) | 全量静态规模、关键真实调用链、已核实事实与问题 |
| 2 | [范围与保真原则](design/scope.md) | 完整范围、行为保留原则和待裁定事项 |
| 3 | [整体架构](development/architecture.md)（已批准） | 系统职责、数据所有者、代码组织与实际依赖 |
| 3a | [工程结构与长期维护](development/engineering.md)（已批准） | 代码目录、边界检查、内容表、资源转换、台账、依赖治理与并行协作 |
| 3b | [系统设计](development/system-design.md)（已批准） | 系统定义、归属判定、命名规则、依赖规则、28 个系统与内容族清单、写入收口与公开操作 |
| 4 | [数据与运行时](development/data-and-runtime.md) | 状态的存储、加载、修改、同步、消费、保存 |
| 4a | [19 个系统调查正文](systems/README.md) | 每个系统的来源入口、数据归属、操作和待裁定项 |
| 4b | [公共入口与依赖](development/entry-and-dependency.md) | 全量事件/菜单/消息/命令、跨系统调用方向与动态数据族 |
| 5 | [编号与变量 Excel](../outputs/01a19713-78e6-72b2-844d-c8e8d0ce7ded/1.18.2-content-register.xlsx) | 内容编号、字段、字典键、过程与菜单的审阅台账 |
| 6 | [命名与台账](development/naming-and-catalog.md) | 来源身份、新命名、人工列和刷新规则 |
| 7 | [内容与对齐](development/content-and-parity.md) | 本项目的 ID 决定、台账现状与迁移适用范围 |
| 8 | [全量实施顺序](development/roadmap.md)（已批准） | 全部系统工作包、依赖、验收与 goal 启动条件 |
| 9 | [开发规则](development/coding-policy.md) | 对过度防御代码的检索结论与具体编码/测试约束 |
| 10 | [文档结构与维护](development/documentation.md) | 正文职责、状态、日志和检查方式 |
| 11 | [目标 API 依据](reference/target-api.md) | 本地 NeoForge 21.1.228 核对后本项目仍待验证的范围 |
| 12 | [项目状态与决定](systems/project/README.md) | 已确认事项、本轮完成情况、剩余调查与日志 |

原始来源位置见 [来源入口](reference/legacy/README.md)，机器清单见 [索引说明](reference/legacy/inventory/README.md)。本项目不继承已有移植工程的完成状态。
