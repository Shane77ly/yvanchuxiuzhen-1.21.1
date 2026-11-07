---
topic: equipment
role: entry
status: partial
updated: 2026-11-07
---

# 装备

标识 `equipment`，类别：系统。职责：武器、防具、工具、饰品、飞行法器、法宝与本命培养、词条（[系统设计](../../development/system-design.md)）。

当前阶段：B1 已登记本单位不依赖新代码类型的内容身份与资源（物品 198、声音事件 8）。C 的 skill 依赖批次已接回 18 把弓的来源条件流程、九把拂尘的挥动与选中冷却、隐藏拂尘物品及弹体；构建与 ArchUnit 通过，弓与拂尘客户端专项一次五项全部符合。弓自然右键保留 Q57 断链，人工注入标记只验证条件分支。妖化四件护甲、菜单请求、装备实例数值与贡献、头盔计时及动画渲染已实现，验证见[妖化](architecture.md#妖化2026-11-07)。其他装备玩法、完整表现及两版本对照尚未完成，不能把本批状态作为整个系统验收；弓与拂尘规则见[正文](architecture.md#弓与拂尘2026-11-07)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 339 个元素，其中 266 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

饰品依赖批次已实现，验证见[四饰品槽与飞行减耗](architecture.md#四饰品槽与飞行减耗2026-11-07)：`Equipment.openAccessories` 打开本系统四槽菜单，`Equipment.accessories` 返回已保存四槽的副本；关闭回写只由 equipment 执行。初始化与菜单共用的物品引用只保留在本系统内部。导航经 `AccessoryPageEvent` 请求汇总面板，equipment 不依赖 panel。对照 `typed_field_accesses.csv` 的 `PlayerGUI03` 开关过程与 `Shipintick`：四槽归 equipment；`tizhiitem.shipintick` 经 `Cultivation` 逐键读写；飞行附魔经 `FlightEnchantmentEvent` 提供给 skill；九项根属性与冷却缩减提交 attribute。`Equipment.properties` 查询 `/shipin` 的显式根属性，与成品的嵌套炼器数据分开保存；根 `mana_Att` 经 `WeaponStatEvent` 提供给 skill。

后续合法单件验收（2026-11-07）：未修改检查代码，沿用现有 `accessories` 的14项全部通过，补齐自然菜单搬移、保存同步、死亡复制、重登后操作和技能飞行减耗；正常保存退出、ERROR0。证据与剩余范围见上述正文，equipment 及GUI仍为部分实现。

成品生成契约已接入：`Equipment.createProduct(注册表, 参数表)` 构造单件成品，`Equipment.product(物品)` 查询类型化品阶、说明、原料数值及附魔记录；装备实例由本系统唯一写入。treasure 负责 `/lianqi` 的权限、坐标容器与掉落，依赖方向为 treasure → equipment。主手三攻击按 `wuqi/fabao` 标签提交 attribute；只为 `WeaponStatEvent` 的品阶查询提供值，根字段法攻不以炼器法攻替代。末次成品专项六项全部符合并 DONE，规则和证据见[宝箱成品与品阶](architecture.md#宝箱成品与品阶2026-11-07)。

弓与拂尘的选中推进、挥动和隐藏弹体物品的使用由本系统接收。妖化批次使用类型化 `EquipmentStats` 组件保存四件防具的来源数值，向 attribute 的加成层提交六项属性；监听 combat 的 `ArmorStatEvent` 查询防御，监听 skill 的 `DemonTransformationEvent` 执行变身。饰品穿戴页已接回；饰品制作台、其他装备实例与本命培养仍未完成，不能据本批标记整个系统完成。

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
