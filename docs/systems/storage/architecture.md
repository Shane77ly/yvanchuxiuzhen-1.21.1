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

## 目标所有权与接口提案

普通物品库存随物品组件，方块库存随方块，实体库存随实体；玩家材料仓单独随玩家持久化。不能为了统一所有容器而改变材料仓归属。菜单会话保存筛选、页码、当前选择；持久仓只保存真实数量及已确认的个人偏好。

材料仓用 `(materialId, level, quality)` 对应数量的结构表达，顺序索引作为显示需要处理。公开存入、取出、查询与按规则扣料，生产系统不直接改仓内部 Map。乾坤袋保持真实 ItemStack，不抹去附魔、名称或实例组件。

## 验收与取舍

明确区分玩家材料仓和能交易/丢弃的袋物品；跨实例转移不得改变源行为。验证翻页、筛选、自动收集、批量扣料、关闭界面、移动持有物品、死亡、重登及多人。只断言槽数正确不足以验证资产去向。

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

`compileJava`、完整 `build compileSmokeJava`（含既有check/ArchUnit）通过。项目内置 `runSmokeClient -PsmokeOnly=storage` 最终24项玩法检查全部INFO、0FAIL、DONE：四档真实右键与网络槽位存取、37件命名材料及等级17/品阶3组件、末槽分堆和Shift合并、储物物品拒插、切走主手关闭、关闭重开、组件序列化与退出重进全部通过；客户端两次正常保存四维度、日志ERROR/FATAL0。首次20项存取通过后重进超时，已定位为实验性维度的原生确认页未处理；在现有 `SystemCheck` 补上此页实际按钮操作与开世界期间的tick重入保护后通过，不修改生产玩法。首次证据 `tmp/storage-first-{client.log,latest.log,report.txt}` 保留，最终证据 `tmp/storage-client-final.log`、`tmp/storage-final-{latest.log,report.txt}`。已查看04截图，槽位和来源纹理可见。专服通过现有标准输入脚本启动至 `Done (1.246s)`，原生 `stop` 正常保存四维度退出，ERROR/FATAL0，菜单导出16个；证据 `tmp/storage-server-latest.log`。首次专服漏接输入，stop无效后结束进程，不计作正常停服验收，其日志另存 `tmp/storage-first-server-latest.log`。最终构建证据 `tmp/storage-final-build.log`。来源版本实机、两版本对照、多人、死亡掉落及全缩放视觉未执行。材料仓、材料袋及其辨识/炼器消费者仍在本系统下一批，不由乾坤袋实现替代。
