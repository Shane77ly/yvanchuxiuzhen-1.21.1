---
topic: panel
role: architecture
status: draft
updated: 2026-11-07
---

# 玩家面板：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

汇总：六个分页的导航框架、HUD 与覆盖层。分页内容归所属系统，例如装备页 `PlayerGUI03` 归 equipment、功法页 `PlayerGUI04` 归 gongfa、副职业页归 profession。界面会话字段 `dangqianGUI`、`linshi`、`renwulistlinshi` 只服务显示。

## 目标所有权与接口提案

只显示各系统通过 `api` 提供的数据，不拥有玩法状态，不被系统依赖。

## 已实现：技能 HUD（2026-11-07）

`PanelClientSetup` 由独立客户端入口装配，在 NeoForge 21.1.265 的 `RegisterGuiLayersEvent` 中 `registerBelowAll`。对应来源 `Colddownxianshi` 与 `ArrowHUD`，通过 `Skills.cooldown`、`Skills.guardRemaining`、`Cultivation.tizhiDouble` 读取本人同步数据，不在绘制时推进计数或反写玩法档案。

冷却只显示大于零的分组，金、水、木、火、土、阴阳六行位于屏幕中心加 `(189, 86+9×序号)`；颜色、名称与剩余刻数除以 20 的数值形式保留来源。格挡大于零时显示十帧 `gedang01–10`，中心加 `(-52,-12)`，103×26；每两刻一档，18 刻及以上为第十档。弓蓄力读 `lagongtick`，至少一刻时显示十帧，中心加 `(-1,-13)`，18×29；每十刻一档，大于 90 刻为第十档。实际拉弓和蓄力值的写入随 equipment，本批只迁移显示。

验证：`skillui` 的 C 键按下/松开已通过本人数据同步的运行检查，HUD 截图已查看，可见格挡和蓄力图；来源固定坐标在默认小窗口下使右侧冷却文字裁切，未改布局。未执行全屏/不同 GUI 缩放、多人、1.18.2 版本实机对照与视觉认可。六分页和其他覆盖层仍待迁移。
