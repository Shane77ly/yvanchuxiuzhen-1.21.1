---
topic: smoke-evidence
role: record
status: current
updated: 2026-11-02
---

# 客户端冒烟检查证据（2026-11-02）

`gradlew runSmokeClient`（NeoForge 21.1.265、开发客户端）自动生成的结果，规则见[工程结构](../../../../development/engineering.md)“验证基础设施”。本次覆盖加入植物、工具与普通盔甲之后的全部内容，取代此前只含方块与物品的一次结果（832 个方块、1,626 个物品，见 git 历史）。

- [report.txt](report.txt)：放置 11,337 个方块状态，核对方块 1,127、物品 2,063，无缺失模型、名称与提示文本翻译；25 个创造栏逐栏切换无异常。
- `world_0.png`–`world_6.png`：高空平台上从俯视角度拍摄的方块行。
- `plant_cassia_02.png`、`plant_tailgreenbristlegrass.png`：第一个花方块与第一个双格植物（补放上半格）的近景，十字模型带透明，贴图正常。
- `armor_sets.png`：11 套盔甲穿戴后的三人称正面截图，由 11 张 `armor_<套>_<部件>.png` 裁出玩家部分拼成（原图在 `build/smoke/screenshots`，不入库）。每套的两层贴图都显示，没有紫黑缺失方格；`clothes_16`、`clothes_17` 的来源只有胸甲与护腿两个部件。
- `tab_*.png`：本 Mod 的 10 个创造栏（`tab_tabyvanchu_entity` 为空，刷怪蛋随生物系统迁移）。`tab_tabyvanchu_weapon.png` 的“熔融巨剑”提示显示 6 攻击伤害、1 攻击速度，与来源一致（来源 `SwordItem(tier, 3, -3f)`，阶层加成 2.0，基础 1，速度基础 4）。

本次未覆盖：门、按钮、压力板的交互与音效，透明方块的专项核对（植物只看了两个），工具的挖掘与损耗，盔甲的防御与耐久数值，使用自定义模型的 7 套盔甲（未登记），食用与右键等玩法。
