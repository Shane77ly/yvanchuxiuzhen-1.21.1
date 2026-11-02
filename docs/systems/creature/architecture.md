---
topic: creature
role: architecture
status: draft
updated: 2026-11-01
---

# 生物：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 结构

271 个注册实体类型包含生物、形态、弹体和效果实体；不能当作 271 种妖兽。源元素有 169 个 animatedentity 和 17 个 livingentity。实体相关 Java 的继承、AI goal、属性、同步字段、生命周期方法另有 [索引](../../reference/legacy/inventory/entity_surfaces.csv)。

`A:Yaoshou017DangShiTiGengXinKeShiProcedure:79–170` 依条件组建有序技能候选，使用 `linshijineng{i}/jinengjishi{i}/jinengqvanzhong{i}`，累计权重并保存预定技能。索引范围是当次候选数量，不是统一固定技能槽。寄魂蟹还存在缩壳、脱壳、弹体等特例；通用 AI 不能覆盖掉这些行为。

`LingshoudaiqingkongProcedure:36–67` 在主人当前物品栏内按袋 UUID 查找并重建袋物品，说明袋、主人、实体有明确关联。

## 目标所有权与接口提案

每个实体族定义基础属性、自然生成、技能和资源；每个实例保存等级、形态、主人、培养、采集及执行阶段。候选技能可用列表表示，保持条件、顺序、权重和持续时间；特殊 AI 继续使用专用逻辑。

creature 提供生成初始化、驯服/主人查询、采集、收纳/释放和培养入口；伤害以伤害类型造成，敌友与主人查询调用 relation（见[系统设计](../../development/system-design.md)“依赖规则”）。

实体保存和跟踪同步分开。动画消费动作/形态状态，命中由服务端负责。保存后重载需恢复所属实体族必要状态，临时目标引用重新按身份查找。

## 完整验收

逐实体族验证自然投放、刷怪蛋/命令/副本生成、属性、技能、形态切换、碰撞箱/视觉大小、掉落采集、驯养和移除。离线主人/袋被移动的行为需依据来源明确，不预设离线召回系统。静态 AI 索引不等于已验证寻路和战斗观感。
