---
topic: material
role: entry
status: partial
updated: 2026-11-11
---

# 材料

标识 `material`，类别：系统。职责：编号材料身份、品阶、等级、辨识（[系统设计](../../development/system-design.md)）。

2026-11-11 World供给批次接入本系统21种材料矿物Feature的来源生成机制与同名configured/placed资源，台账登记“已注册”；维度、群系和自然获取尚未闭合。规则及验证边界见[世界正文](../world/architecture.md#自然供给地物与随机结构2026-11-11)和[世界月志](../world/logs/2026-11.md)。

当前阶段：材料目录与千材录辨识入口已接回。225条材料定义保留1451条来源有序三元组；实例等级/品阶使用物品组件，玩家附件保存辨识进度、上限、已揭示名称、命名空间、`open` 与特殊标记。现有storage客户端场景共45项玩法检查通过，其中材料覆盖自然开窗、先刷新后辨识、首次免费揭示、成本2/4优先扣仓、异火初始化、死亡复制与重登同步。普通库存与输入槽独立扣料分支、完整视觉及来源对照尚待专项验证；采集产出与炼器/炼丹/阵法消费仍待所属批次，不能将本系统标为完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属371个元素，其中294个已读；见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。归属统计是检索范围，不是已验证功能数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

2026-11-09静态核对确认 `Qancailuitxtjiesuoneed`、`Qancailuitxtjiesuoneed2`、`Qancailuitxtjiesuoneed3` 只读取千材录成本显示缓存，三项从 liandan 移归 material；`Duedanhecheng` 按毒厄丹用途移归 liandan。现有 `MaterialAnalysisScreen` 已显示对应三行成本，本批未重新验收显示缓存生命周期或运行画面，来源定位见[正文](architecture.md#千材录成本显示过程归属2026-11-09)。

- `Materials.definition(ItemStack)`：按注册 ID 查询材料定义；只读。
- `Materials.instance(ItemStack)`：读取材料实例的等级/品阶组件；只读。
- `Materials.setInstance(ItemStack,int,int)`：由材料所有者写入实例等级与品阶，供 storage 取出真实材料及构造显示样本。
- `Materials.revealedAttributes(Player,String)`：按辨识顺序查询已揭示且 `open=false` 的名称，供 storage 九属性筛选。
- `Materials.revealedEntries(Player,String)`：返回只读 `MaterialRevealedEntry(namespace,name,open)`，供材料仓详情按来源显示已揭示效果。
- `Materials.initializeYihuo(ServerPlayer)`：按来源登录入口初始化异火材料辨识上限并逐条追加揭示效果；只由 yihuo 调用。
- `MaterialInventoryRequestEvent`：材料辨识发出 `COUNT/CONSUME` 请求，由 storage 订阅；前者查询允许自动消费时的原始 ID 总数，后者按等级 1–40、品阶 0–4 消费并返回实际扣量。

storage 依赖材料的公开查询与实例写入，材料通过自己定义的具体库存请求事件消费仓材料，依赖方向不形成循环。材料系统不读取或修改 storage 附件；生产用途查询随实际消费者接入。

本轮变化记录在[材料月度日志](logs/2026-11.md)，项目级进度见[项目月度日志](../project/logs/2026-11.md)。
