---
topic: tiandao
role: architecture
status: partial
updated: 2026-11-15
---

# 天道：架构调查与提案

来源事实与当前实现边界分别陈述；源码定位采用[系统导航](../README.md)的A/B规则。

## 1.18.2 范围

内容族：作者“原初天道（生存模式不可得物品）”分类下的管理工具，包括 NPC 设置（`TiandaoNPCset*`）、物品 NBT 编辑（`Tiandaoitemnbt*`）、多结构工具（`Tiandaoduojiegou`）、建筑笔（`Tiandaojianzhu`）、`Ling08`、`Ling16` 与天道维度方块。天道副本方块是玩家入口，已归 dungeon。

## 目标所有权与接口提案

专属于某个系统的工具在该系统开工时按归属判定拆回；通用工具留在本内容族。没有 `api`。

## 穿梭令与物品锁（2026-11-15）

`A:Ling08Item`在两端右键空气时按来源写入速度：X为`-sin(yaw)`、Y为`-sin(pitch)`、Z为`cos(yaw)`；水平分量不乘俯仰余弦，不补冲刺持续或冷却。主手攻击修正-1、攻速修正-3、附魔能力2，挖掘与攻击分别调用1/2点耐久消耗并返回成功，来源耐久0按普通非耐久物品保持。挖掘速度来源空列表判断始终为1，沿用原生Item默认值。`MovementToolItem`保留来源`super.use`的返回结果，不因写入速度改成成功结果。

`A:Ling16Item`右键方块时只在服务端写入`lockblock=true`并发方块更新；目标由公共`BlockDisplayLock`的Boolean附件承载，依附任意目标方块实体保存。该共享状态有已确认的跨内容族需求：A工具写入，B的展示柜03和展示架1501–1505读取，详情由[Decor正文](../decor/architecture.md)维护；两个内容族之间不建立API依赖。来源空目标仍只发更新、不创建方块实体。没有解锁、创造模式权限或潜行分支。

右键空气按玩家坐标为中心、半径2的AABB和距离排序处理全部盔甲架；普通使用以完整实体保存/读取写`Marker=true`，潜行写`Invisible=true`。`A:RightClickEntityProcedure:588–623`的全局实体右键分支按`hand == getUsedItemHand()`过滤，要求主手是物品锁：盔甲架使用相同字段，普通/发光物品框分别写`Fixed=true`或`Invisible=true`。不取消事件、不补恢复操作；仍逐个目标在服务端发送来源中文提示“锁定/隐藏盔甲架”“锁定/隐藏物品展示框”。该源码的字段写入与两端执行顺序已静态复核，完整NBT保存/读取不涉及玩家档案。

八项来源指纹见[本包来源指纹](ledger/source-fingerprints.csv)。本包两物品及工厂输入已接，正式转换、最终合并中文、编译与同展示家具共同110项必要客户端检查通过，包含六家具库存及共享锁真实保存重进；证据见[本批集成记录](../project/logs/2026-11.md#2026-11-15-页面命令阵法与展示家具集成验证)。专服、完整自然获取、多人与1.18.2版本实机/两版本对照未执行；其他通用管理工具仍未实现，不将两工具代码接线视为整个内容族完成。

## 固定块保护（2026-11-15）

`A:block/TiandaogudingBlock.java:12–20`定义完整石质方块，来源硬度-1、爆炸抗性3600000、无掉落，光照遮挡值15；其方块物品使用原生放置入口，生存模式没有自然获取链。`A:procedures/BlockDestroyProcedure.java:34–71`订阅全局方块破坏事件，检查被破坏坐标的东、西、北、南、上、下六邻块，只要任一是`tiandaoguding`且执行玩家不是创造模式，就取消该次事件；来源没有提示文本或其他权限判断。相邻分支中无论是否创造模式都不进入后续采集奖励，由[Profession采集入口](../profession/architecture.md#矿石采集自然入口)保留该跳过规则。

目标`FixedBlockProtection`由`TiandaoSetup`在NeoForge游戏总线按默认NORMAL优先级注册；使用服务端实际`GameType.CREATIVE`判断，保留六面邻块条件和取消结果。已核对NeoForge21.1.265精确源码：`CommonHooks.fireBlockBreak`发布事件并在取消时向客户端发送原方块状态，`ServerPlayerGameMode.destroyBlock`在收到取消结果后直接返回false；本包不新增文本或方块状态。与Combat连锁采集同次事件的监听顺序及直接移除邻块互作尚未专项验证，不能据事件接线宣称已覆盖该组合路径。

固定块原内容表漏掉负硬度，原因是`legacy_blocks.py`的二参数`strength`解析只接受非负首参数。本包将首参数改为允许负号，直接解析锁定来源的固定块、NBT编辑器和纯白块均得到`[-1,3600000]`；不另建专用方块工厂。两个来源所有负值调用的专项清单见[转换属性核对](../../reference/conversion/negative_strength_audit.csv)，该CSV固定记录`04c66d9a`基线：24条二参数调用中9条内容表已有值、14条缺值、1条未生成；另外6条单参数传送门不在本次解析器改动范围。未执行全量或定向转换，不将其他系统的内容表标为已修。

本包保护代码和负值解析规则已接，正式Tiandao内容再生、编译、1.21.1版本自然破坏/创造模式/六方向/对角邻块、采集经验与连锁互作、1.18.2版本实机及两版本对照均未执行。来源指纹包含固定块定义与全局破坏过程；固定块没有新增持久化数据。

## 多结构生成器（2026-11-15）

`A:block/TiandaoduojiegouBlock`定义金属声音、硬度-1、爆炸抗性3600000、不遮挡、非红石导体、光照遮挡15及空视觉形状；空掉落表回退自身一件。方块物品沿原生放置入口，放置后安排20刻，服务端每次计划刻调用生成过程后再安排20刻。目标工厂`tiandao:multi_structure`和独立方块实体身份已接入，属性由内容转换表承载；本包不改动既有cutout模型包装与同名中文键。

`A:block/entity/TiandaoduojiegouBlockEntity`是零槽容器，最大堆叠数1，六面有向能力和Forge父类提供的无方向`InvWrapper`均保留，移除后能力为空。`jiegouzu`归本方块实体持久化数据所有，无外部业务写入接口；数据随父类保存和完整方块实体更新包保存/同步。保留来源菜单提供者及默认三行原版箱子菜单，但来源方块没有右键打开过程，不新增配置界面。目标能力按NeoForge21.1.265显式登记，父类保存和能力迁移依据复用已有同版本核对结论。

`A:procedures/TiandaoduojiegouGengXinYouXiKeProcedure`仅在`jiegouzu`严格等于“寻木”时调用`Ancienttimeswood03Procedure`。每轮先分别随机取整数厚度10–15和长度5–12；层号从0开始，逐层执行“厚度减层号”次，非正次数自然为空。每片X、Z偏移各独立取整数-4至4，Y偏移为层号乘5再加独立整数-4至4；每次随机独立创建随机源，对应来源逐次`new Random()`。放置`ancienttimes_wood_01`模板，不旋转、不镜像、包含实体，使用世界随机源及flags3。目标复用World公开操作`WorldStructures.place`，不直接写World持久化对象。一次调用40–114片：厚10长5为10+9+8+7+6=40；厚15长12为15至4共114；厚10长12只有10至1共55片，末两层无调用。生成不清空分组、不记录一次性完成；模板可能覆盖生成器自身，该来源结果保持。

### Q199：多结构分组没有自然写入者

状态：**待核实，当前保持来源**。已沿两个来源全部Java、元素和函数检索`jiegouzu`，没有赋值过程；来源`ancienttimes_wood_03.nbt`是1×1×1单生成器，`ForgeData`为空，转换后的同模板`NeoForgeData`也为空。`ancienttimes_wood_01.nbt`是6×10×6的360块模板，仅含空气和寻木方块，已在结构清单转换。普通放置及03模板均保持每20刻空转；只有外部显式配置“寻木”才生成。本包不补默认分组、配置GUI或一次性标记，后续统一复核自然配置是否有来源遗漏；显式配置检查不作为自然获取链证据。

六项来源Java/模板指纹登记在本系统CSV。本包提供独立`TiandaoMultiStructureCheck`供主线接入必要单项：原生放置扣件、负硬度、七方向零槽能力、45服务端刻空配置、分组真实保存重进和显式寻木模板生成，再核对最终合并中文并截图。场景先保存模板完整可能覆盖区域的方块/方块实体及原计划刻，清理时恢复方块数据、方块/流体计划刻和玩家库存/状态。未修改共享Smoke选择器。

已完成来源闭包和目标API静态核对、资源/工厂/台账轻量检查；正式内容转换、Gradle编译、该必要单项、自然运行、1.18.2版本实机及两版本对照均未执行。主线需先定向再生Tiandao内容，恢复本方块的`[-1,3600000]`和新工厂，再执行必要单项；不能由源文件接线宣称运行或行为对齐已通过。

## 通用物品NBT编辑器（2026-11-15）

本包为 `tiandaoitemnbt` 方块、单槽方块实体、`tiandaoitemnbt_gui` 菜单与屏幕及八类型写入过程。专属代码、工厂、消息、资源转换输入与必要检查已实现，主线生产/客户端装配和必要单项尚待执行；不据此提升其他天道工具的状态。来源定位为 A `block/TiandaoitemnbtBlock.java`、`block/entity/TiandaoitemnbtBlockEntity.java`、`world/inventory/TiandaoitemnbtGuiMenu.java`、`client/gui/TiandaoitemnbtGuiScreen.java`、`network/TiandaoitemnbtGuiButtonMessage.java`、`procedures/TiandaoitemnbtsetProcedure.java` 及同名界面纹理。

### 入口与库存生命周期

来源方块右键无权限或创造模式门槛，打开176×166原生菜单。输入框位于 `(3,4)`、`(3,24)`，均为120×20、最长32767字符；物品槽 `(140,27)`，按钮 `(128,46)`、40×20；八个独立复选框位于 `x=-64`、`y=1+21×序号`，初始均未选中。目标保留来源布局和原生控件，为确认按钮提供中文“设置NBT”。来源提示“上面三个输入框”与实际两框的差异随Q201记录，未增加第三框。

库存一槽、上限64、允许任意物品，随方块实体保存；关闭方块菜单不返还物品，破坏时掉出槽内物品，工具方块自身沿来源不掉落，并提供比较器输出。六面能力允许插入、禁止抽取；无方向能力供菜单直接操作，复用同机制Q148的来源父类证据。菜单不按距离关闭，客户端提交当前容器编号，服务端验证当前菜单、已加载区块与绑定方块实体后修改实际槽位。来源模板的未绑定库存关闭时归还玩家，死亡或断线时掉落；本包保留该生命周期，未找到生产用的手持/实体绑定入口，不额外构造入口。

### 输入、类型与写入顺序

屏幕按来源拼接 `nbtname:<键>,nbtcontent:<值>,aaa-0,bbb-0,...hhh-0,`。服务端先 `split(",")`，用 `contains(前缀)` 查找并用全局 `replace` 去掉前缀；未命中时取第一段。逗号截断和输入包含前缀时的碰撞语义均保留，不引入嵌套路径或SNBT。

键名或值为空时提示“需要在上面三个输入框内写入对应内容。”；没有选中项时提示“请勾选一个复选框，只能勾选一个”。非空输入且有选中类型时按下列顺序只处理首项，空槽直接返回。

| 优先级 | 类型 | 来源实际写入 |
| --- | --- | --- |
| 1 | Boolean | 仅精确 `true`/`false` 写ByteTag布尔值，其他文本不写 |
| 2 | String | 原字符串 |
| 3 | Byte | 先解析double，再Java强制转换byte |
| 4 | Double | 解析后的double |
| 5 | Float | 先解析double，再强制转换float |
| 6 | Int | 先解析double，再强制转换int |
| 7 | Long | 先解析double，再强制转换long |
| 8 | Short | 先解析double，再强制转换short |

数值入口先trim再 `Double.parseDouble`，非法数字实际使用0；来源接受的NaN/Infinity保留解析语义，其在目标持久化Codec中的完整结果尚待核实。处理选中分支后来源强制物品数量为1，包含Boolean文本非法而未写入的情况。实际Tag保存在 `DataComponents.CUSTOM_DATA`；原生及专有消费者映射见Q203。

### Q201 多选提示与实际优先级

状态：**静态已核实、目标保持来源、待统一裁定**。来源界面使用八个独立复选框，提示称只能勾选一个，实际过程按上述 `if/else if` 首项优先。目标保留多选与首项优先，不改成单选、不拒绝多选。提示称有三个输入框但界面实际仅两框，也保留原文集中复核。证据为来源Screen与set过程；本批未运行多选实机分支。

### Q202 编辑后强制数量为1

状态：**静态已核实、目标保持来源、待统一裁定**。来源每个选中类型分支均把输入物品数量写为1，且Boolean非法文本仍执行数量修改；数量大于1时其余物品没有归还或掉落。目标保留该行为，不新增拆分或余量退还。必要检查覆盖合法八类型后的数量1，非法Boolean与其他堆叠数量仍待实机验证。

### Q203 根NBT与目标组件消费者映射

状态：**部分映射已实现、其余待核实或待处理**。1.18.2版本可直接编辑任意物品根NBT；1.21.1版本的原生字段和部分模组实例已转为专有组件，仅保存同名CUSTOM_DATA并不保证驱动消费者。天道不得直接修改其他系统专有组件，装备投影通过 `Equipment.updateRootField` 由所有者提交。

| 来源键或族 | 目标载体/消费者 | 当前边界 |
| --- | --- | --- |
| 任意八种原始根值 | `CUSTOM_DATA` | 已保存实际Tag类型；不解释Compound、List、嵌套路径 |
| 飞行法器 `health/health_Max/power_Max`、摄虫网 `item_level_num` | equipment既有CUSTOM_DATA消费者 | 根值已接；消费者仍按自己的getInt/getDouble规则读取 |
| 蛊虫 `guchongid/LEVELnumber/experience/belongToMasterUUID` 等 | `GuchongJarData.read` 与既有个体搬运 | 仍读CUSTOM_DATA，根写可供现有消费者读取；未误记为全部专有组件 |
| `Damage` | 原生 `DAMAGE` | 已映射getInt；原生组件要求非负，组件写入按0截断，raw保留输入；负值差异待集中核实 |
| `CustomModelData` | 原生 `CUSTOM_MODEL_DATA` | 已映射getInt；显示仍依赖物品模型定义 |
| `Unbreakable` | 原生 `UNBREAKABLE` | 已按getBoolean添加/移除；写此键时按raw `HideFlags & 4` 设置Tooltip显示 |
| `RepairCost`、单独修改 `HideFlags` | 原生修理费用及多个组件Tooltip属性 | 仅保存raw，未完整映射；单改HideFlags不重建全部原生显示属性 |
| 饰品九项根属性、根 `mana_Att`、根类型/品阶/说明 | `EquipmentProperties` 与饰品属性/武器法攻/Tooltip | 已经所有者API同步单键投影，其他投影保持；不改嵌套炼器成品 |
| `benmingexp/benmingexpmax` | `BenmingData` 培养经验 | 已经Equipment API同步，保持其余字段 |
| `benminglevel` | 来源double；目标 `BenmingData.level` 为int | 仅保存raw与根投影；培养和Tooltip仍读原组件，待处理 |
| 技能书 `jinengmanaatt/belongTopinjie/belongToMasterUUID` 等 | `SkillBook` 的numbers、grade、owner等 | 仅保存raw，不驱动专有组件；尚未接入所有者单字段编辑契约 |
| 功法书 `ExercisesLevel/ExercisesExp/Exercises*`、`belongToMasterUUID` | `TechniqueBook` 的level、exp、values、owner | 仅保存raw，不驱动专有组件；尚未接入所有者单字段编辑契约 |
| 炼器 `lianqizong` | Equipment炼器组件/成品投影 | 来源工具仅支持八种标量，没有Compound编辑；同名字符串不解释为完整炼器对象 |

此表是本批已定位消费者，不是全部模组根键普查。后续统一核对归属、来源可表达类型及真实消费者后补所有者接口，不将工具代码已写完称为全部字段行为闭合。必要检查覆盖一个装备根属性双端投影与八种raw类型；其他映射、原生负值及特殊浮点完整存档仍开放。

### 验证范围

新增 `NbtEditorCheck(List<String>, String)` 供主线装配。夹具真实右键开原生菜单、操作两输入框/八复选框/按钮，核对八种Tag值与数量1、本人同步、装备根属性投影、物品完整组件与方块库存保存读回、六面/无方向能力、关闭留槽及破坏回收。受控序列化不等于真实重登。生产/Smoke编译、目标运行、完整自然存档、多人、1.18.2版本实机、两版本对照与完整视觉未在独立树执行；实际轻量检查只记[本月日志](logs/2026-11.md)。
