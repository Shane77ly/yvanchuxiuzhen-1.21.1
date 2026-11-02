---
topic: smoke-evidence
role: evidence
status: current
updated: 2026-11-02
---

# 客户端冒烟检查证据（2026-11-02）

`gradlew runSmokeClient`（NeoForge 21.1.265、开发客户端）自动生成的结果，规则见[工程结构](../../../../development/engineering.md)“验证基础设施”。

- [report.txt](report.txt)：放置 11,014 个方块状态，核对方块 832、物品 1,626，无缺失模型、名称与提示文本翻译；25 个创造栏逐栏切换无异常。
- `world_0.png`–`world_6.png`：高空平台上从俯视角度拍摄的方块行。
- `tab_*.png`：本 Mod 的 10 个创造栏（`tab_tabyvanchu_entity` 为空，刷怪蛋随生物系统迁移）。

本次未覆盖：门、按钮、压力板的交互与音效，透明方块的专项核对，食用与右键等玩法。
