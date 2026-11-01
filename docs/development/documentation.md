---
topic: documentation
role: guide
status: current
updated: 2026-11-01
---

# 文档维护

文档总入口为 [docs/README.md](../README.md)，根目录 README 仅作导航。项目范围在 `docs/design`，公共架构在 `docs/development`。系统进入实施时建立 `docs/systems/<系统>/README.md`，并按需要拆分 `design.md`、`architecture.md`、`balance.md`。

一项规则只保留一个正文。数值记录必须区分定义值、派生值、实例覆盖与兼容字段；文档存在或注册成功不代表实际生效。

每份正文含 UTF-8 front matter：`topic`、`role`、`status`、`updated`。角色采用 `entry/design/architecture/balance/guide/data/changes/log/proposal/record/experience`；状态使用 `draft/partial/current/historical/generated`。`draft` 为建议稿，`current` 仅表示所声明核对范围有效，均不自动表示游戏验收通过。

采用但未实现的规则在所属正文明确标注，不留在日志中让读者猜测。废弃方案不转存另一套设计目录；必要原始证据、许可证和实验记录保持各自用途。

月度记录放所属系统的 `logs/YYYY-MM.md`，包括变化、原因与取舍、验证范围和正文链接。`changes.md` 只做月份与决定导航；本轮跨系统设计记录归项目维护。

检查命令在新项目根目录执行：

```powershell
python -B -X utf8 tools/docs/check_docs.py
python -B -X utf8 tools/porting/inventory_legacy.py --check
```

检查器从邻近项目复用，负责入口可达、链接、元数据与重复正文检查，不验证代码行为。提取工具核对旧来源与已生成清单，不改旧工程；跨机器用 `--source-workspace` 指定包含两个参考目录的工程根路径。

所有人工源码核对、自动化、旧版实机、新版实机验证分别登记。本轮工具：[文档检查器](../../tools/docs/check_docs.py)、[来源提取器](../../tools/porting/inventory_legacy.py)。
