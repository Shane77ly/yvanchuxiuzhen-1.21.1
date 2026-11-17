---
topic: documentation
role: guide
status: current
updated: 2026-11-16
---

# 文档维护

一项事实只维护一个当前正文。通用方法以[移植技能](../../.claude/skills/minecraft-mod-porting/SKILL.md)为准，项目只补充具体事实与约束。文档服务于闭合功能，不要求每次改动遍历全部状态页。

## 正文职责

| 文件 | 唯一职责 | 更新触发 |
| --- | --- | --- |
| `AGENTS.md` | 项目身份、长期协作与授权边界 | 长期规则改变；不存滚动分支与测试计数 |
| [remaining-work.md](remaining-work.md) | 实现覆盖、开放功能包、类型、承接系统、依赖与闭合依据 | 功能闭合、新缺口或调度变化；已有条目原位更新 |
| [handoff.md](handoff.md) | 当前分支、可定位基线、正在做什么及下一动作 | 会话交接；链接任务详情，不复制整段系统日志 |
| [roadmap.md](roadmap.md) | 全量工作包、依赖顺序与出口定义 | 批准范围、顺序或出口规则变化 |
| [项目状态](../systems/project/README.md) | 负责人决定与里程碑索引 | 新决定/里程碑；历史决定标日期和当时范围 |
| [来源问题索引](../reference/legacy/source-survey.md#具体问题与裁定位置) | 来源差异、疑似错误、断链及不确定项的稳定编号和处理状态 | 新发现、证据变化或裁定/实现/验证状态改变；详情链接所属系统正文 |
| `docs/systems/<系统>/README.md` | 系统当前实现摘要、公开操作、正文/台账入口 | 本系统实现或对外边界变化；历史长记录放月志 |
| 系统 `architecture/design/balance.md` | 唯一行为、架构和数值正文 | 对应规则或实现边界变化 |
| 系统 `ledger/*.csv` | 来源身份、映射与对应迁移状态 | 对应来源/实现事实改变，不为纯文档提交制造变化 |
| 系统或项目 `logs/YYYY-MM.md` | 批次变化、验证结果/限制、失败原因、证据路径 | 本批结果记一次；跨系统方法变更记项目月志 |
| `changes.md`、`docs/README.md` | 导航与决定索引 | 新增/月度记录、路径移动，不复制运行结果 |

并行模块的唯一调度入口仍为 `remaining-work.md`。每个活动模块只维护一个当前记录，字段包括负责人、分支/worktree、基线、范围/排除项、依赖、模块状态、验收出口、证据和下一动作；系统日志保存实现与验证过程，模块摘要链接日志，不复制报告。负责人或边界变化时先更新调度摘要，再由主协调者收口公共状态。模块状态不得合并表达“已集成”和“已验收”。台账内容状态以“批准调整”记录范围内行为调整，以“批准排除”记录负责人批准的范围排除；排除项保留来源身份但不作为开放待办，详情见[台账参考](../../.claude/skills/minecraft-mod-porting/references/parity-ledgers.md)。

项目开放清单是调度索引，不另建一套全量注册台账。系统正文中的具体剩余分支是该清单的明细；变化时同时校准对应清单行即可，不重写所有系统摘要。旧的“当前分支/下一步/goal active”移除或显式标为历史，不能在当前段落叠加互相矛盾的说法。

## 更新与证据

系统当前摘要原位改写，不无限追加日期段落。历史失败与已覆盖结论留在月志或Git历史，当前状态仅展示最新有效结果并链接证据。移动内容前核对引用和原始证据是否仍可追溯，不复制整份旧文档建立第二套正文。

记录区分未实现、确认缺陷、待核实/待验证、外部依赖、待裁定、负责人明确排除；没有证据不能把待验证升级成Bug或通过。静态核对、自动化、1.18.2版本实机、1.21.1版本实机、两版本对照分别说明。客户端交互与视觉检查按[验证参考](../../.claude/skills/minecraft-mod-porting/references/verification.md#客户端交互与视觉验收)记录入口、结果、保存边界、截图（适用时）与未执行项。具体格式按[台账参考](../../.claude/skills/minecraft-mod-porting/references/parity-ledgers.md)，无需每个功能创建额外表格。

负责人2026-11-14要求差异和拿不准的问题统一保留、后续集中处理。沿用来源问题索引，不另建第二套全项目问题表；所属正文登记入口/源码位置、实际证据、受影响路径、当前保持或偏离来源的做法，以及未核实/未运行的边界。已批准但未实现、已实现但未验证分别标明，来源疑点不能自动当作目标缺陷。独立工作继续推进，未批准的行为调整不自行实施。

每份正文含UTF-8 front matter：`topic`、`role`、`status`、`updated`。角色采用 `entry/design/architecture/balance/guide/data/changes/log/proposal/record/experience`；状态采用 `draft/partial/current/historical/generated`。`current`表示所声明文档范围有效，不代表系统全部验收；`partial`也不否定已经完成的批次。

已采用但未实现的规则在正文明确标注。项目范围在 `docs/design`，公共架构在 `docs/development`，系统正文在 `docs/systems`；原始来源证据、许可证与可复用实验保留各自用途。临时产物放 `tmp` 或 `build`。Excel生成字段与刷新见[命名与台账](naming-and-catalog.md)。

## 按变更选择检查

| 变更 | 必要检查 |
| --- | --- |
| 只改文档、Skill或状态导航 | `python -B -X utf8 tools/docs/check_docs.py`、`git diff --check`；Skill结构校验。无需Gradle或游戏 |
| 改来源映射/内容台账 | 上述检查及 `python -B -X utf8 tools/porting/check_ledger.py`；按实际转换范围检查产物 |
| 改提取器、锁定来源或来源清单 | 追加 `python -B -X utf8 tools/porting/inventory_legacy.py --check`，不每个普通提交扫描来源工作区 |
| 改实现/构建/资源 | 按[验证参考](../../.claude/skills/minecraft-mod-porting/references/verification.md)和[工程约束](engineering.md#验证基础设施)选择编译、规则检查与单项运行 |

[文档检查器](../../tools/docs/check_docs.py)验证入口可达、链接、元数据与重复正文，不证明文字内容、阶段状态或游戏行为正确。完成修改后还需对照实际Git和有效证据做语义审阅。
