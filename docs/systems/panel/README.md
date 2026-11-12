---
topic: panel
role: entry
status: partial
updated: 2026-11-08
---

# 玩家面板

标识 `panel`，类别：汇总。职责：六个分页的导航框架、HUD 与覆盖层；各页的菜单归所属系统（[系统设计](../../development/system-design.md)）。

当前阶段：部分实现（2026-11-08）。已接入 K 键记页、属性汇总页、丹田入口及返回；本批新增属性01、饰品03、共用装配04三页导航与第4页记忆，完整构建与现有skillui客户端29项已通过，覆盖01/03/04真实按钮导航与K记页；04死亡、断线和重登记页未在本批验收。02加点、05副职业、06修炼分页及临时队伍仍待迁移。原生 HUD 的六组技能冷却、十帧格挡与十帧弓蓄力显示只读 skill/xiulian 的本人同步数据，弓蓄力已由 equipment 的来源条件流程提供。新增导航与菜单归属见[三页导航正文](architecture.md#三页导航与装配页归属2026-11-08)；先前两页与 HUD 证据保持原记录，不代替04验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 77 个元素，其中 25 个已读；15个04装配元素本批改判归 skill，导航过程 `OpenGUI04` 仍归 panel 并已读。这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。

HUD 后续核对（2026-11-07）：现有 `skillui` 九项通过；缩放 1 的真实截图可见六行冷却、格挡和蓄力，开发设置已还原。未修改检查代码，具体证据与限制见[HUD正文](architecture.md#已实现技能-hud2026-11-07)。

现有 `accessories` 合法单件14项已通过，补齐当次K键、两页和丹田往返、死亡记页、重登后再打开的运行证据；该次尚无04装配页，不能作为新三页链路的验收，详见[当次导航正文](architecture.md#已实现k-键与两页导航2026-11-07)。

## 公开操作

panel 拥有 `dangqianGUI`，只向本人同步并在全部克隆事件保留。按键只传打开请求；服务端按记住的分页打开属性汇总、调用 `Equipment.openAccessories` 或 `Skills.openLoadout`，监听 `AccessoryPageEvent`、`SkillPageEvent` 及 `DantianReturnEvent`。属性汇总同时显示 attribute 与 xiulian 的公开快照；饰品菜单归 equipment，共用功法与技能装配菜单归 skill。汇总面板不持有玩法档案，不直接读写装配栈。02/05/06分页及临时队伍显示/退出随所属系统继续迁移。
