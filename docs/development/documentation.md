---
topic: documentation
role: guide
status: current
updated: 2026-11-02
---

# 文档维护

文档总入口为 [docs/README.md](../README.md)，根目录 README 仅作导航。项目范围在 `docs/design`，公共架构在 `docs/development`。本轮已建立 19 个系统的 `README.md` 和 `architecture.md`；已确认玩法与完整数值达到独立正文规模时，再拆分 `design.md`、`balance.md`，不创建空模板。

一项规则只保留一个正文。通用移植方法（含数值记录与完成判定）以[移植技能](../../.claude/skills/minecraft-mod-porting/SKILL.md)为正文，本项目文档链接引用、不复制。

每份正文含 UTF-8 front matter：`topic`、`role`、`status`、`updated`。角色采用 `entry/design/architecture/balance/guide/data/changes/log/proposal/record/experience`；状态使用 `draft/partial/current/historical/generated`。`draft` 为建议稿，`current` 仅表示所声明核对范围有效，均不自动表示游戏验收通过。

采用但未实现的规则在所属正文明确标注，不留在日志中让读者猜测。废弃方案不转存另一套设计目录；必要原始证据、许可证和实验记录保持各自用途。

月度记录放所属系统的 `logs/YYYY-MM.md`，包括变化、原因与取舍、验证范围和正文链接。`changes.md` 只做月份与决定导航；本轮跨系统设计记录归项目维护。

检查命令在新项目根目录执行：

```powershell
python -B -X utf8 tools/docs/check_docs.py
python -B -X utf8 tools/porting/inventory_legacy.py --check
```

检查器从邻近项目复用，负责入口可达、链接、元数据与重复正文检查，不验证代码行为。提取工具核对1.18.2 来源与已生成清单，不改1.18.2 工程；跨机器用 `--source-workspace` 指定包含两个参考目录的工程根路径。

所有人工源码核对、自动化、1.18.2 版本实机、1.21.1 版本实机验证分别登记。工具顺序和完整证据表见 [调查索引](../reference/legacy/inventory/README.md)；文档检查使用 [check_docs.py](../../tools/docs/check_docs.py)。

## 标准结构与正文职责

```text
README.md                         总入口
AGENTS.md                         开发协作约束与项目档案
.claude/skills/minecraft-mod-porting/  通用移植方法正文（规则、工作单、参考）
docs/README.md                    导航和当前阶段
docs/design/scope.md              项目范围与批准边界
docs/development/                 公共架构、数据流、命名、开发及验证规则
docs/systems/<系统>/
  README.md                      系统入口、实际实现状态、证据与待办
  design.md                      玩家目标、操作、规则与交互
  architecture.md                存储→加载→修改→同步→消费→保存，及扩展方式
  balance.md                     配置值、派生值、覆盖值、单位、公式与取整
  changes.md                     月份和决定索引
  logs/YYYY-MM.md                 变化、原因、取舍、验证、正文/提交链接
docs/reference/legacy/           1.18.2 原始证据与提取结果
docs/licenses/                   来源与许可资料（有实际内容时建立）
docs/structure_exports/          结构导出（有实际内容时建立）
docs/vfx/<效果>/                  特效参数、制作、移植和实机验证
docs/knowledge/                  可复用且已核实的经验
outputs/<任务>/                  对外审阅工作簿
tmp/ 或 build/                   临时调查与检查产物
```

系统开始有实际规则正文时再建立所属文件，避免提前生成大量空模板。小系统在 README 中按相同职责分节；复杂系统按上述文件拆分。

## 单项规则与状态标注

规则、数值与架构正文的记录项见[台账参考](../../.claude/skills/minecraft-mod-porting/references/parity-ledgers.md)的“规则正文记录项”。

每次变化同步更新受影响正文、README 实现状态与月度日志。已批准但尚未实现写“已采用，未实现”；本项目当前架构写“草案，待批准”。调查发现的问题留在所属正文，不自动转为额外修复任务。

Excel 的生成字段、人工映射列、刷新方式见 [命名与台账](naming-and-catalog.md)。跨系统决定在项目状态页留索引，具体规则只在唯一正文维护。后续新增主题或移动路径必须同步修改导航与导出工具。
