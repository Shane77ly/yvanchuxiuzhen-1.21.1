---
topic: storage
role: architecture
status: partial
updated: 2026-11-08
---

# 储物：架构调查与提案

来源事实、已实现部分和未实现方案分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 已核实的两种不同所有权

乾坤袋菜单的 ItemStackHandler 容量为 27/54/108/220（`A:Qiankundai01…04GUIMenu`）。蛊罐归 guchong，见[蛊虫](../guchong/architecture.md)。菜单生成模板还能绑定手持物品、实体或方块，实际入口决定所有者。来源 `stillValid` 直接返回 true 的情况要结合真实绑定重写。

**材料袋不是普通随物品保存的 46 格容器。** `B:CailiaodaiGuicunruProcedure:30–123` 把数量写到玩家 `renwuitem`；该字段由 `PlayerJoinGameProcedure:40–44` 创建为隐藏铁锭。其菜单 46 格承担 1 个输入与 45 个分页显示，目录数值按材料 ID、等级、品阶保存。

动态键含 `cailiaozu{materialId}` → 目录序号、`cailiaozu{ordinal}` → 材料组、`level{level}pinjie{quality}` → 数量。`CailiaoqvchuzongProcedure:63–100` 从选定组恢复物品并扣数量。炼器/辨识的自动取料也访问同一玩家仓。

## 目标所有权与接口

普通物品库存随物品组件，方块库存随方块，实体库存随实体；玩家材料仓单独随玩家持久化。不能为了统一所有容器而改变材料仓归属。材料仓保存数量、来源计数和页码、排序、筛选、自动存入、允许扣仓偏好，菜单会话保存当前选择及显示缓存；来源筛选计时另随玩家持久化。

材料仓用 `(materialId, level, quality)` 对应数量的结构表达，顺序索引作为显示需要处理。公开存入、取出、查询与按规则扣料，生产系统不直接改仓内部 Map。乾坤袋保持真实 ItemStack，不抹去附魔、名称或实例组件。

## 验收与取舍

明确区分玩家材料仓和能交易/丢弃的袋物品；跨实例转移不得改变源行为。验证翻页、筛选、自动收集、批量扣料、关闭界面、移动持有物品、死亡、重登及多人。只断言槽数正确不足以验证资产去向。

## 材料袋与玩家材料仓（2026-11-08）

### 来源覆盖与自然入口

storage归属116项已逐项读完，包含本轮剩余100项：A的4份配方，B的1个GUI、94个过程和1份配方。45个 `Cailiaoxvanze01`–`45` 的Java实现去除名称和槽序号后相同，编辑器槽序号1–45与网络按钮8–52对应；5个 `Pinjiexvanze0`–`4` 仅品阶不同，按钮3–7对应品阶4–0。不能以一份代表过程代替其余条目的来源阅读。

`qiankundai_06` 的实际物品工厂接入 `MaterialBagItem`，自然右键按来源在 `(x,y+1,z)` 开启 `MaterialBagMenu` 和原生界面。菜单保留1个真实输入和45个不可放入、不可拿取的显示槽，`stillValid=true`。来源自然开窗未写物品手或实体标识，菜单模板会尝试绑定开窗位置的方块物品能力；目标保留该路径，绑定时使用该方块库存，不绑定时用临时输入，关闭返还或掉落输入。该来源特殊绑定路径尚未专项实机。

材料输入使用目标命名空间 `yvanchuxiuzhen:cailiao`，排除 `lingshi`、`huoyan`，实例等级不得大于41；来源forge标签已转换为目标命名空间，不能因未生成 `forge:cailiao` 而改掉入口条件。整栈存入仅将等级0改为1；负等级和异常品阶不额外校正。与普通乾坤袋不同，玩家仓只保存材料ID、等级、品阶和数量，原栈名称及其他组件不保留，取出仅重建材料实例。

5份配方已按来源实际运行JSON转换。B的 `Qiankundaihecheng.mod.json` 编辑器描述仍为兔皮、皮革和染料，但实际运行配方使用 `cailiao_171×4 + cailiao_001×4 + cailiao_181×1`；目标采用运行JSON。其余4份乾坤袋配方也按运行JSON核对。

### 持久化、显示与消费

`MaterialWarehouse` 是不可变玩家附件，由 `MaterialStorage` 唯一写入，持久化、死亡复制并仅向本人同步；仓随玩家，不随袋06转移。组保留插入顺序，同ID按1–41级显示，每页45个样本；样本数量表示等级，实例品阶固定5。排序有来源顺序、等级降序、等级升序三个状态，等级排序稳定保留同等级原有次序。最大页码由未筛选的材料样式数计算、最低1，样式减少不强制夹紧当前页码；空存入、空取出以及未选择或空样本的品阶按钮也重新计算最大页码。

九属性筛选对 `Materials.revealedAttributes` 返回的已揭示且 `open=false` 名称逐项作AND判断，未辨识属性不满足条件。筛选和自动存入选项每累计20次开窗刻比对并保存；来源 `guijishiqi` 改为独立 `material_filter_ticks` 玩家附件，保存并死亡复制，跨关闭和重新开窗累计。允许扣仓在关闭时保存。复选框消息明确编码选择值，客户端从服务端菜单状态更新控件时禁止回发回调。自动存入仅处理输入槽，输入非空且启用时每刻排入两刻后存入，没有去重；回调只在当前菜单仍打开时执行（来源开窗每刻过程24–125行）。

品阶按钮每次取一件，由 `ItemHandlerHelper` 交付背包或掉落。选择、翻页和取出后的一刻刷新触发详情缓存更新；排序、存入和筛选只刷新样本，不强制重算已选择详情，保留来源显示缓存生命周期。45个样本在每次刷新时全部覆盖，等价承担 `CailiaodaiguiitemqingchuProcedure:17–22` 的清空步骤；该过程确由 `CailiaodaiGuixianshicailiaoProcedure:55` 调用。开窗详情清理由 `CailiaodaiGuiopenProcedure:29` 调用 `CailiaodaiqingchujieshaoProcedure:17–27`，目标用空菜单缓存承担。

材料辨识通过材料定义的 `MaterialInventoryRequestEvent` 发出COUNT与CONSUME，storage订阅并在允许扣仓时提供原始ID总数，随后按等级1–40、品阶0–4消费。来源在清空等级时会关闭等级标记、减少样式数，却不减少组 `levelCount`；目标分别保存数量与计数，不在辨识消费后重建组计数或重算最大页码。炼器的高等级、高品阶优先装料尚未接入实际消费者，不能使用辨识顺序替代。

### 保持的来源异常与剩余范围

- `B:CailiaodaiqingchuProcedure` 已检查Java调用、编辑器触发及GUI按钮，均无运行入口；编辑器断开的文字也说明未被调用。代码仅在选中组总数不足1时清除ID索引，不重新计算总数；目标不增加新入口调用它。
- `B:CailiaodaichongxinpaixvProcedure:34–44` 在累计遇到10个空组后停止，不是连续10个。正常仓存取的稳定压紧由列表表示承担；异常稀疏来源存档的导入尚未裁定，不能据此宣称兼容。
- 部分编辑器XML的断开块引用不存在的 `Cailiaodaixianshizhuanhuan`，未连接事件，生成Java也无此调用，不作为缺失运行流程补造。
- 来源可存41级、辨识只消费1–40级，原始ID总数预检可能通过而实际仓扣料不足；普通库存和输入槽的独立扣减分支仍需专项核对。
- 超过45个样式的分页、五档品阶全部取出、特殊方块能力绑定、多人、完整GUI缩放与视觉、1.18.2版本实机和两版本对照未执行。炼器实际装料入口待所属系统批次接回。

### 本批运行证据

沿用现有 `runSmokeClient -PsmokeOnly=storage` 场景扩展检查，未新增Test类或选择器。最终45项玩法检查PASS、0FAIL、DONE；47条INFO中另外2条为实验性世界确认页操作和鼠标捕获说明。覆盖四档袋的真实开窗、37件命名材料及等级17/品阶3组件、末槽分堆与Shift、拒插、换手关闭、关闭重开和重登；新增材料袋真实右键46槽、非材料/异火/42级拒入、整栈入玩家仓、17级五品样本双端一致、展示拒拿、取一件且丢弃原名、输入槽自动存入、同ID不同等级、已辨识属性AND筛选、升降序、关闭保存偏好，以及千材录免费首条和成本2/4按仓消费。实际死亡重生与退出重进后，材料仓完整快照在死亡和重登后双端一致；死亡后检查服务端辨识条目数，重登后检查双端条目数；两阶段均核对采集等级。

证据为 `tmp/material-storage-client.log`、`tmp/material-storage-final-report.txt`、`tmp/material-storage-final-latest.log`。首次运行因袋06物品工厂漏接而失败，修复工厂后重跑通过，历史日志 `tmp/material-storage-first-*` 保留，不计为通过。最终客户端WARN37（含已有厨具缺失纹理）、ERROR/FATAL0。最终 `build compileSmokeJava`（含既有test/check/ArchUnit）通过，证据 `tmp/material-storage-build-final.log`；专服经既有stdin脚本启动至Done后原生stop，四维度正常保存退出、ERROR/FATAL0，导出菜单18个，证据 `tmp/material-storage-server-latest.log`。收尾的来源语言迁移、195号提取值、跨开窗计时及未选样本时的详情刷新修正仅重新构建，未重复客户端整套；这些收尾点未另作专项实机。

截图 `build/smoke/screenshots/storage-material-warehouse`、`build/smoke/screenshots/storage-material-analysis` 已保存，文件没有扩展名。材料仓默认GUI缩放截图存在裁切，截图保存不代表完整视觉认可。完整缩放、1.18.2版本实机与两版本对照仍未执行。

## 乾坤袋物品库存（2026-11-08）

本节同时覆盖01–04四档。已读取 `A:Qiankundai01Item`–`Qiankundai04Item`、四份 `item/inventory/Qiankundai*InventoryCapability`、四份 `Qiankundai*GUIMenu`、四份 `Qiankundai*GUIScreen`、四个开窗期间过程与 `JinzhifangruProcedure`。实际入口只有物品右键；目标不保留生成模板中的方块/实体备用绑定和未绑定临时库存回收分支。

| 档 | 容量 | 界面尺寸 | 来源槽位 |
| --- | --- | --- | --- |
| `qiankundai_01` | 27 | 176×166 | 9列3行；玩家背包从(8,84)开始 |
| `qiankundai_02` | 54 | 176×208 | 9列6行；玩家背包从(8,125)开始 |
| `qiankundai_03` | 108 | 304×202 | 16列6行，另加左右各3列2行；玩家背包从(72,122)开始 |
| `qiankundai_04` | 220 | 435×244 | 23列8行，另加左右各6列3行；玩家背包从(137,163)开始 |

四档共409个储物槽坐标已与来源逐项匹配，贴图保持原文件。菜单槽位禁止 `chuwu` 标签内的四档乾坤袋和两种藏宝箱；物品能力与来源一样只拒绝同档袋、每槽上限64，不能将菜单过滤扩大到自动化入口。真实物品栈及其名称、附魔和各系统实例组件随袋物品的原版 `CONTAINER` 组件持久化与同步；不是玩家附件，丢弃和交易随物品转移。

打开时服务端绑定实际右键的栈，客户端使用临时槽位接收菜单同步。目标依据 NeoForge 21.1.265 源码的 `ItemContainerContents`、`ComponentItemHandler`、`ItemHandlerCopySlot` 与 `StackCopySlot`：组件为不可变值，菜单读取到的栈副本修改后必须经复制槽写回，Shift合并也显式写回，不能把来源可变 `ItemStackHandler` 的引用语义直接套到组件。每次存取即时写入袋，关闭时不复制整仓，也不将袋内物品退给玩家。

开窗期间仍按来源每刻检查主手**物品类型**；不是该档就关闭，即使由副手打开也按同一规则关闭。换为另一个同档袋不改变原来绑定，来源 `stillValid=true` 保持；客户端抛出同档袋时也关闭该档界面。只有01保留 `EAT` 和32刻使用参数，右键不额外开始食用；02–04沿用普通物品参数。

`compileJava`、完整 `build compileSmokeJava`（含既有check/ArchUnit）通过。项目内置 `runSmokeClient -PsmokeOnly=storage` 最终24项玩法检查全部INFO、0FAIL、DONE：四档真实右键与网络槽位存取、37件命名材料及等级17/品阶3组件、末槽分堆和Shift合并、储物物品拒插、切走主手关闭、关闭重开、组件序列化与退出重进全部通过；客户端两次正常保存四维度、日志ERROR/FATAL0。首次20项存取通过后重进超时，已定位为实验性维度的原生确认页未处理；在现有 `SystemCheck` 补上此页实际按钮操作与开世界期间的tick重入保护后通过，不修改生产玩法。首次证据 `tmp/storage-first-{client.log,latest.log,report.txt}` 保留，最终证据 `tmp/storage-client-final.log`、`tmp/storage-final-{latest.log,report.txt}`。已查看04截图，槽位和来源纹理可见。专服通过现有标准输入脚本启动至 `Done (1.246s)`，原生 `stop` 正常保存四维度退出，ERROR/FATAL0，菜单导出16个；证据 `tmp/storage-server-latest.log`。首次专服漏接输入，stop无效后结束进程，不计作正常停服验收，其日志另存 `tmp/storage-first-server-latest.log`。最终构建证据 `tmp/storage-final-build.log`。来源版本实机、两版本对照、多人、死亡掉落及全缩放视觉未执行。材料袋、玩家仓与辨识消费者的后续实现及本批45项客户端证据见上节；炼器实际装料仍待所属批次。
