---
topic: panel
role: architecture
status: draft
updated: 2026-11-08
---

# 玩家面板：架构调查与提案

来源事实与目标方案分别陈述。未标明已实现的目标方案仍为草案；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 范围

汇总：六个分页的导航框架、HUD 与覆盖层。分页内容归所属系统：装备页 `PlayerGUI03` 归 equipment；`PlayerGUI04` 是功法与技能的共用装配页，本批按数据边界落在 skill，功法槽真值仍归 gongfa；副职业页归 profession。panel 只导航与汇总。界面会话字段 `dangqianGUI`、`linshi`、`renwulistlinshi` 只服务显示。

## 目标所有权与接口提案

只显示各系统通过 `api` 提供的数据，不拥有玩法状态，不被系统依赖。

## 已实现：技能 HUD（2026-11-07）

`PanelClientSetup` 由独立客户端入口装配，在 NeoForge 21.1.265 的 `RegisterGuiLayersEvent` 中 `registerBelowAll`。对应来源 `Colddownxianshi` 与 `ArrowHUD`，通过 `Skills.cooldown`、`Skills.guardRemaining`、`Cultivation.tizhiDouble` 读取本人同步数据，不在绘制时推进计数或反写玩法档案。

冷却只显示大于零的分组，金、水、木、火、土、阴阳六行位于屏幕中心加 `(189, 86+9×序号)`；颜色、名称与剩余刻数除以 20 的数值形式保留来源。格挡大于零时显示十帧 `gedang01–10`，中心加 `(-52,-12)`，103×26；每两刻一档，18 刻及以上为第十档。弓蓄力读 `lagongtick`，至少一刻时显示十帧，中心加 `(-1,-13)`，18×29；每十刻一档，大于 90 刻为第十档。实际拉弓和蓄力值的写入随 equipment，本批只迁移显示。

验证：`skillui` 的 C 键按下/松开已通过本人数据同步的运行检查，HUD 截图已查看，可见格挡和蓄力图；来源固定坐标在默认小窗口下使右侧冷却文字裁切，未改布局。未执行全屏/不同 GUI 缩放、多人、1.18.2 版本实机对照与视觉认可。六分页和其他覆盖层仍待迁移。

后续 HUD 核对（2026-11-07）：沿用现有 `skillui` 九项检查全部通过，缩放 1 的854×480截图可见六行冷却、格挡和蓄力；开发设置随后还原。源码坐标、颜色及分档与来源一致，未改布局或新增检查。证据为 `tmp/skill-ui-native-hud.png` 及同前缀报告/日志；完整视觉、多人和1.18.2版本实机对照未执行。

## 已实现：K 键与两页导航（2026-11-07）

`panel` 位于基础包根部，沿用批准架构的汇总职责；原 `system/panel/client` 的 HUD 装配一并移入，不建立玩法系统对 panel 的反向依赖。已读来源 `Guicaidan`、`GuicaidanAnXiaAnJianShi`、`PlayerGUI01`、`OpenGUI01/03`、属性显示与说明过程，并对照 `typed_field_accesses.csv` 核对 `dangqianGUI` 与只读显示字段。饰品菜单的槽位、保存和制作规则归 equipment，详见[装备正文](../equipment/architecture.md#四饰品槽与飞行减耗2026-11-07)。

客户端 K 键只在按下沿且没有界面时发送空的打开请求，不传档案或页码。服务端使用本人分页附件：默认0进入属性页并记1，记3时进入饰品页。附件随玩家存盘，全部克隆事件复制并只同步本人。属性页 `player_gui_01` 使用来源36格背包、500×500界面及来源贴图坐标；按钮1打开饰品并记3、5发送来源说明、6打开丹田。饰品返回属性经公开事件交给 panel；丹田返回监听既有 `DantianReturnEvent`，记1或3时恢复对应页并取消默认关闭。原生菜单按钮消息核对当前容器身份，客户端不直接执行玩法写操作。

属性页只读 attribute 的最终上限、恢复和攻防，以及 xiulian 的境界、修为、灵根阴阳、罡煞、功德与两个显示字段。来源每秒先复制灵力/神识到 `power_display/soul_display`，再恢复资源，已由 xiulian 的 `Recovery` 保留顺序；不能改为渲染时直接读取实时资源池。属性汇总同时消费两个系统，放在 panel 避免 attribute 与 xiulian 循环依赖。显示格式、来源境界翻译键和固定位置保持来源。

该批只接通属性与饰品两页，其他四分页及临时队伍显示/退出未迁移；静态分页文字保留，尚未实现的操作没有占位按钮。`PlayerGUI01/03` 台账以“已注册”配合备注标明已实现路径，不能据此宣称六分页完成；本批新增04的状态见下节。

当次构建与 ArchUnit 通过；仅一次 `accessories` 专项的原报告记录12项符合，但使用了检查代码人为构造的数量3饰品，超过来源和目标共同的上限1，不能作为合法穿戴流程的验收。退出重登另有等待超时，后两项未执行。已修正为合法单件，并按 NeoForge21.1.265 的 `WorldOpenFlows/BackupConfirmScreen` 补实验性存档确认页处理；当次只编译未重跑，后续合法14项证据见下段。两张旧截图仅证明布局和贴图可见，不代表合法饰品使用或 GUI 完整；零层检查玩家显示 `levelname00`，按来源键保留。专用服务器一次正常启动并原生 stop 保存退出，ERROR0，导出含两个菜单。证据与撤回范围见装备正文；当时其余四页、跨页按钮和临时队伍仍未完成，不能视为已验收后最后统一补。1.18.2版本实机、两版本对照、多人、全屏与不同 GUI 缩放尚未执行。

后续运行（2026-11-07）：沿用现有 `accessories` 检查，合法单件14项全部通过，K键、两页导航、丹田往返、死亡记页、退出重登及重登后再次打开均通过；正常保存全部世界、ERROR0。证据为 `tmp/accessory-native-{client.log,report.txt}`。未改动检查代码或补充其余分页，当前GUI仍部分实现。

## 三页导航与装配页归属（2026-11-08）

来源 `PlayerGUI01/03ButtonMessage` 的 button2 进入04；04的 button2 返回01、button4进入03。目标 `PlayerPanelMenu` 与 `AccessoryMenu` 保留原按钮身份，equipment 通过含目标页号的 `AccessoryPageEvent` 请求导航；`SkillLoadoutMenu` 通过 `SkillPageEvent.player()/page()` 请求01或03。`PanelNavigation` 统一调用各所有者的公开打开操作并写 `dangqianGUI`，没有 skill/equipment 指向 panel 的反向依赖。

两个导航事件新增保存旧菜单 `BlockPos` 的重载与 `pos()`；01/03/04跨页时由 `PanelNavigation` 对旧坐标增加一格，传给各菜单公开打开重载的参数已是最终坐标。01原生菜单也保留坐标，按钮沿用该坐标继续增加一格；K恢复从玩家实时位置增加一格。来源沿用菜单坐标的行为不替换成每次重取玩家当前位置，头顶容器绑定位置由实际打开路径决定。

K 键恢复页号3时调用 `Equipment.openAccessories`，恢复4时调用 `Skills.openLoadout`，其余仍进入属性页并记1。分页附件的保存、克隆与本人同步机制沿用既有实现。`DantianReturnEvent` 在记1、3或4时恢复记忆页并取消默认关闭；这只是既有返回契约的页号扩展，未为04新增来源没有的丹田按钮。

04菜单、屏幕与装配生命周期由 skill 持有，功法和七个技能槽各由原数据所有者写入。完整槽位、四页、延迟及保存规则只维护于[skill 装配正文](../skill/architecture.md#共用功法与技能装配页2026-11-08)，panel 不复制装配状态或跨系统提交逻辑。01/03/04保留来源500×500原生界面和实际按钮坐标，02/05/06仍只保留来源调查及静态文字，没有占位实现。

本批完整 `build compileSmokeJava`（含ArchUnit）通过；现有 `skillui` 客户端29项全部INFO、0FAIL、DONE，四世界正常保存退出、ERROR/FATAL0。真实按钮已走01→04→03→04→01，并通过K分别恢复01、03、04，跨页关闭保存与双端库存检查也通过；报告 `tmp/study-loadout-report.txt`，完整本批证据见[skill验证段](../skill/architecture.md#目标依据与本批验证边界)。既有两页14项不替代04死亡、断线、重登记页，本批也未覆盖头顶真实容器。专服一次正常启动并原生stop保存四维度退出，ERROR/FATAL0。已查看装配前的真实04截图，页码、槽位、动态空名与导航可见，当前默认缩放下页下方略超出屏幕；它不证明已装书的完整视觉。完整缩放、1.18.2版本实机、两版本对照和多人未验收。
