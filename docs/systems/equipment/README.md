---
topic: equipment
role: entry
status: partial
updated: 2026-11-08
---

# 装备

标识 `equipment`，类别：系统。职责：武器、防具、工具、饰品、飞行法器、法宝与本命培养、词条（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（物品 198、声音事件 8）。C 的 skill 依赖批次已接回 18 把弓的来源条件流程、九把拂尘的挥动与选中冷却、隐藏拂尘物品及弹体；构建与 ArchUnit 通过，弓与拂尘客户端专项一次五项全部符合。弓自然右键保留 Q57 断链，人工注入标记只验证条件分支。妖化四件护甲、菜单请求、装备实例数值与贡献、头盔计时及动画渲染已实现，验证见[妖化](architecture.md#妖化2026-11-07)。开灵附魔的工具右键入口、弹体和四种工具机已按来源过程接回，验证见[开灵工具机](architecture.md#开灵工具机与弹体2026-11-08)。8 个飞行法器的物品字段、实体、骑乘飞行、充灵力、取回、死亡掉落和 GeckoLib 资源已按来源过程接回，验证见[飞行法器](architecture.md#飞行法器2026-11-08)。覆火、覆风、覆情、文王鼓、明心镜、明光镜、血刀与赶仙鞭法宝入口及弹体/物品行为已按 1.18.2 版本源码接回，验证见[法宝](architecture.md#明光镜法宝2026-11-08)、[文王鼓法宝](architecture.md#文王鼓法宝2026-11-08)、[明心镜法宝](architecture.md#明心镜法宝2026-11-08)、[赶仙鞭法宝](architecture.md#赶仙鞭法宝2026-11-08)与[血刀法宝](architecture.md#血刀法宝2026-11-08)。本命培养的事件入口、经验组件、升级属性与进度提交已按来源接回，验证见[本命培养](architecture.md#本命培养2026-11-08)。其他装备玩法、完整表现及两版本对照尚未完成，不能把本批状态作为整个系统验收；弓与拂尘规则见[正文](architecture.md#弓与拂尘2026-11-07)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 339 个元素，其中 282 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

饰品依赖批次已实现，验证见[四饰品槽与飞行减耗](architecture.md#四饰品槽与飞行减耗2026-11-07)：`Equipment.openAccessories` 打开本系统四槽菜单，`Equipment.accessories` 返回已保存四槽的副本；关闭回写只由 equipment 执行。初始化与菜单共用的物品引用只保留在本系统内部。导航经 `AccessoryPageEvent` 请求汇总面板，equipment 不依赖 panel。对照 `typed_field_accesses.csv` 的 `PlayerGUI03` 开关过程与 `Shipintick`：四槽归 equipment；`tizhiitem.shipintick` 经 `Cultivation` 逐键读写；飞行附魔经 `FlightEnchantmentEvent` 提供给 skill；九项根属性与冷却缩减提交 attribute。`Equipment.properties` 查询 `/shipin` 的显式根属性，与成品的嵌套炼器数据分开保存；根 `mana_Att` 经 `WeaponStatEvent` 提供给 skill。

后续合法单件验收（2026-11-07）：未修改检查代码，沿用现有 `accessories` 的14项全部通过，补齐自然菜单搬移、保存同步、死亡复制、重登后操作和技能飞行减耗；正常保存退出、ERROR0。证据与剩余范围见上述正文，equipment 及GUI仍为部分实现。

成品生成契约已接入：`Equipment.createProduct(注册表, 参数表)` 构造单件成品，`Equipment.product(物品)` 查询类型化品阶、说明、原料数值及附魔记录；装备实例由本系统唯一写入。treasure 负责 `/lianqi` 的权限、坐标容器与掉落，依赖方向为 treasure → equipment。主手三攻击按 `wuqi/fabao` 标签提交 attribute；只为 `WeaponStatEvent` 的品阶查询提供值，根字段法攻不以炼器法攻替代。末次成品专项六项全部符合并 DONE，规则和证据见[宝箱成品与品阶](architecture.md#宝箱成品与品阶2026-11-07)。

弓与拂尘的选中推进、挥动和隐藏弹体物品的使用由本系统接收。妖化批次使用类型化 `EquipmentStats` 组件保存四件防具的来源数值，向 attribute 的加成层提交六项属性；监听 combat 的 `ArmorStatEvent` 查询防御，监听 skill 的 `DemonTransformationEvent` 执行变身。饰品穿戴页与本命培养入口已接回；饰品制作台、其他装备实例、完整表现和两版本对照仍未完成，不能据本批标记整个系统完成。

妖化对照 `typed_field_accesses.csv` 的 `Jinengyaohua`、四个 `Yaohuabuwei` 过程与 `Yaohua`：`LEVELnumber` 和五个体质标记通过 `Cultivation.get/tizhiBoolean` 查询，冷却只通过 `Cultivation.tizhiDouble/setTizhiDouble` 读写；xiulian 已有每秒递减入口。物品数值、头盔计时与装备贡献由本系统唯一写入；服务端搬移背包和护甲，客户端只显示。来源没有自动穿回旧防具，旧防具移入背包；结束依次清空当前护甲槽。

妖化本批验证：修正内容表动画字段契约后，build/ArchUnit通过；一次进入世界的专项四项全部符合并DONE，专用服务器一次启动、原生stop正常退出，两个日志均ERROR 0。四件护甲标为“行为已迁移”。截图中模型可见，完整视觉、多人、死亡/重登与两版本实机对照尚未执行；详见正文证据范围。

本批对照 `typed_field_accesses.csv` 中 `Weaponspeedbow01DangGongJuChuXianZaiShouZhongShi`、`Weaponhossu01DangShiTiHuiDongWuPinShi` 与 `Weaponhossu01DangGongJuChuXianZaiShouZhongShi` 的全部玩家字段读写，按数据所有者调用：

| 来源字段或操作 | 目标公开操作与所有者 |
| --- | --- |
| `arrowlinshi` 查询与清空 | `Skills.arrow/setArrow`；清空时保留轮盘选择序号 |
| `power`、`LEVELnumber` | `Cultivation.get/add` |
| `tizhiitem` 的 `lagongtick/manxvli/arrowskill/fuchenshiyongtick` | `Cultivation.tizhiDouble/tizhiBoolean/setTizhiDouble/setTizhiBoolean`；只读写对应键 |
| `attack_Num_display` | `PlayerAttributes.value(ATTACK)` |
| `lctrl/lalt` | `ModifierKeys.ctrl/alt`；沿用已有平台输入状态 |
| 创建技能箭、敌友判定与法术伤害 | `Skills.createArrow`、`Relations.isFriendly`、`SpellDamage.deal` |

`synchronizedata.rightclicking` 是实体持久化标签读取，来源未找到写入者，按 Q57 登记；不新增鼠标状态写入。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。

## 明光镜法宝（`fabao_014`，2026-11-08）

按 1.18.2 版本 `Fabao014Item`、`Fabao014Entity` 及四个绑定过程逐行接回：右键开始使用，松开时检查 `effect_002` 与当前灵力是否达到 `mana_Att_display`，成功后扣除同值灵力、消耗一点耐久并发射速度 2、散布 0 的不可拾取光线弹体。弹体首刻从主人主手物品的 `sneak` 标签复制潜行状态，每刻广播 `cailiao_229particle`，无重力飞行，20 刻后移除；命中敌对生物造成光属性法术伤害 `10 × 1.1`，潜行命中额外广播 `fulutexiao_33` 并施加缓慢 100 刻、等级 20。普通与潜行冷却分别为 80 与 300 刻，敌友判定复用 relation，伤害复用 combat。

本批未新增 Test 类、冒烟选择器、断言或 Computer Use。`gradlew build`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过；项目内置 `runSmokeClient -PsmokeOnly=equipmentweapon` 报告 `DONE`，客户端正常进入世界并保存退出，专用服务端启动至 `Done` 后原生 `stop` 正常保存四个维度，日志无 `ERROR/FATAL`。这些检查确认注册、资源加载和启动链路，不替代明光镜的自然发射、命中、冷却、灵力与视觉专项验收；1.18.2 实机、两版本对照、多人和完整视觉验收未执行。

## 明心镜法宝（`fabao_015`，2026-11-08）

1.18.2 版本 `Fabao015Item` 只有原生 `SwordItem` 构造，没有自定义右键、命中、刻更新或其他过程调用。目标内容表的 `tool` 表项已按来源保留剑阶 200 耐久、速度 4、攻击加成 -1、等级 1、附魔值 2、攻击力 3、攻击速度 -3 和无修理材料；通用 `ContentSword` 与来源原生剑行为等价，因此不新增专用物品类。

本批仅完成静态来源与目标表项对照，未新增 Test 或运行客户端；完整物品显示、1.18.2 实机和两版本对照仍未执行。

## 血刀法宝（`fabao_016`，2026-11-08）

按 1.18.2 版本 `Fabao016Item`、`Fabao016DangShiTiBeiGongJuJiZhongShiProcedure` 与 `Fabao016DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 逐行接回。命中时创造模式目标施加或延长 `effect_004`；非创造模式跳过玩家目标，并按来源读取目标 `LEVELnumber`，仅在不高于 1 时施加 600 刻流血。来源过程读取未赋值的临时 `ItemStack.EMPTY` 的 `lianqizong.item_level`，实际默认值为 `0`，目标实现保留该行为，不替换为炼器成品等级。

潜行右键按 `10 + LEVELnumber × 3` 扣除自身生命；血量足够时生成 `jingxve`，写入主人 UUID、名称、境界数值和境界名称，并发送 `xvedi` 粒子；血量不足分支保留来源提示。`fabao_016` 使用专用剑工厂，保留 80 耐久、剑属性和 `cailiao_201` 修理材料。

本批未新增 Test 类、冒烟选择器、断言或 Computer Use。验证使用项目内置 `runSmokeClient -PsmokeOnly=equipmentweapon`，报告为 `DONE`；该客户端检查只覆盖注册、资源加载和启动回归，不替代血刀命中、流血、潜行采血、物品 NBT 与视觉专项验收。1.18.2 实机、两版本对照、多人和完整视觉验收未执行。

## 赶仙鞭法宝（`fabao_022`，2026-11-08）

按 1.18.2 版本 `Fabao022Item` 接回主手属性：攻击力修正 `-2`、攻击速度修正 `-2.4`，耐久 `80` 保留。来源没有自定义右键、命中或刻更新过程；目标改用专用物品工厂写入属性组件。来源 GeckoLib 物品模型、动画和第一人称渲染尚未接入，不能据此宣称完整视觉完成。

本批未新增 Test 或冒烟选择器；内置 `runSmokeClient -PsmokeOnly=equipmentweapon` 报告为 `DONE`，客户端正常保存退出，日志无 `ERROR/FATAL`。该 smoke 仅作注册、资源加载和启动回归，不替代赶仙鞭属性与视觉专项验收。

## 文王鼓法宝（`fabao_021`，2026-11-08）

1.18.2 版本 `Fabao021Item` 只有 GeckoLib 物品渲染与动画控制器，没有右键、命中、属性或实体过程。目标普通物品表项保留单件堆叠与创造栏归属，服务端没有额外玩法差异；来源 `fabao021` 几何、动画、纹理和第一人称渲染尚未接入，完整视觉仍待后续资源批次。

本批仅完成静态来源与目标表项对照，未新增代码、Test 或冒烟选择器，未运行客户端。

## 开灵工具机与弹体（2026-11-08）

`PlayerRightClickProcedure` 的开灵分支由 equipment 接收 `RightClickItem`：主手开灵等级非零、手持斧/镐/锄/铲且非冒险模式时，服务端生成无拾取弹体，伤害 `0.1`、速度 `1`、位置为眼高减 `0.1`，并写入100刻冷却。弹体首刻冻结主人 UUID、工具类别和开灵等级；命中方块或飞行达到16刻时播放闪光并生成对应工具机，避免之后换手改变已发出实体。

四种工具机分别复现来源的持久状态机：斧按 `8 × 开灵等级` 扫描日志、寻路、11刻采集和3×3×3树叶清理；镐按附着方向螺旋搜索石质、冰或黏土方块，每轮最多处理25个目标；锄和铲扫描地面并在5刻后清除植物、改为湿润度7的耕地。工具机寿命使用来源的 `fumodengji × 2400`，斧与镐保留来源过程中的 `×400` 缺省初始化。所有状态字段随实体持久化，采集掉落由服务端按来源主人条件处理。

本批未新增 Test 类、冒烟选择器或 Computer Use 流程。`gradlew build`、`check_docs.py` 与 `check_ledger.py` 均通过；沿用项目现有的 `runSmokeClient -PsmokeOnly=equipmentweapon`，五项既有弓与拂尘检查全部符合并以 `DONE` 结束，客户端正常保存退出，未见 `ERROR/FATAL`。该既有检查用于确认新增实体注册、客户端装配和公共启动不阻塞，不替代工具机四类自然操作的专项验收。专用服务器启动至 `Done` 后以原生 `stop` 正常保存退出，实体导出 91 个；证据为 `build/smoke/report.txt`、`run/logs/latest.log` 与 `build/registry-export/entity_type.txt`。

## 覆火葫芦（2026-11-08）

按 1.18.2 版本 `Fabao010DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 逐行接回 `fabao_010`：普通右键从十级到一级选择最高可用火焰，十级至二级灵力消耗为 `256/128/64/32/16/8/4/2/1`，一级不消耗；高等级灵力不足时继续尝试低等级。潜行右键以视线五格方块为中心扫描 `8×8×8`，普通火、灵魂火和 `cailiao_221`–`229`、`231`–`239` 分别吸收为一级至十级火焰，并按法宝等级门槛限制。火焰数量使用 `FabaoFireData` 组件保存，普通火焰由 `Fabao010huoyan` 弹体释放；弹体保持无重力、十刻寿命、火焰粒子、命中方块上方放置普通火及来源命中字段写入。隐藏物品松开发射速度为 `2`、散布为 `0`，禁止拾取并保留来源耐久消耗。

本批未新增 Test 类、冒烟选择器或 Computer Use。`gradlew build`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过；沿用 `runSmokeClient -PsmokeOnly=equipmentweapon`，既有五项弓与拂尘检查全部 `DONE`，客户端正常保存退出，日志未见 `ERROR/FATAL`。该客户端检查仅证明覆火葫芦的注册、资源加载和公共客户端启动不阻塞，不替代火焰吸收、发射、命中与存档的完整自然操作验收。1.18.2 实机、两版本对照、多人及完整视觉验收未执行。

## 覆风法宝（`fabao_011`，2026-11-08）

按 1.18.2 版本 `Fabao011DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 接回普通与潜行右键。普通右键以视线五格命中的方块坐标为中心，在半径四格范围内按距离处理实体；潜行右键以玩家坐标为中心，在半径五格范围内处理实体。敌对目标造成风属性法术伤害 `5 × 1.2`，并按来源以 `(目标位置 - 玩家位置) × 0.5` 的水平速度和 `0.5` 的垂直速度击退；带 `skilleffect_08` 的生物仍受伤害但不被击退。普通与潜行冷却分别为100和200刻，`effect_002` 沉默时不执行。

本批仅新增法宝专用物品行为，复用 relation 的敌友判定和 combat 的 `SpellDamage` 公开入口；未新增 Test 类、冒烟选择器或 Computer Use。`gradlew build`、`runSmokeClient -PsmokeOnly=equipmentweapon`、专用服务端启动至 `Done` 后原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 均通过；客户端和服务端日志未见 `ERROR/FATAL`。客户端 smoke 只作为注册、资源加载和启动回归，不替代风场范围、伤害、击退、冷却和沉默分支的完整自然操作验收；1.18.2 实机、两版本对照、多人及完整视觉验收未执行。

## 覆情法宝（`fabao_012`，2026-11-08）

按 1.18.2 版本 `Fabao012DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure` 接回普通与潜行右键。两条分支都以视线四格命中的方块坐标为中心，扫描半径四格；普通右键发送爱心与焚烟粒子，使范围内动物进入200刻发情状态，冷却200刻。潜行右键发送焚烟粒子，对敌对生物添加 `effect_032` 1200刻，冷却600刻；`effect_002` 沉默时不执行。来源潜行分支只按敌友关系筛选，普通动物分支不做敌友筛选。

本批未新增 Test 类、冒烟选择器或 Computer Use。`gradlew build`、`runSmokeClient -PsmokeOnly=equipmentweapon`、专用服务端启动至 `Done` 后原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 均通过；客户端和服务端日志未见 `ERROR/FATAL`。smoke 只作注册、资源加载和启动回归，不替代普通发情、潜行幻术、冷却和沉默分支的完整自然操作验收；1.18.2 实机、两版本对照、多人及完整视觉验收未执行。
