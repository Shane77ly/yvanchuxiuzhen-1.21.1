---
topic: equipment
role: architecture
status: draft
updated: 2026-11-01
---

# 装备、法宝、本命与饰品：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 实例与调用

装备包括基础 Item 类型、炼器生成的 `lianqizong`、原生附魔、耐久和实例词条，不能只迁移注册类。饰品有四个独立玩家槽；本命法宝保存到 `benmingfabao`，属性比较用的 `kuijia1…4/wuqilinshi` 是另一种职责。

`A:DantianbenmingfabaoProcedure:23–79` 读取境界、体质培养贡献和器灵附魔，向法宝自身写入等级/经验，阈值为 `2000 + benminglevel * 2000`。`BenmingfabaotishengProcedure:15–30` 交替增加法术/精神字段，同时修改 `lianqizong` 中相应来源值。交替标志现放在玩家 `tizhiitem`。

## 目标所有权与接口提案

equipment 拥有器物实例组件与专属槽位；lianqi 计算制作结果后调用器物构造入口。基础定义、实例覆盖、附魔和本命成长分开，避免炼器端和战斗端各保存一份器物属性。

公开操作：装备/取出、实例生成/升级、修理/耐久消耗、绑定本命、推进培养、查询属性贡献和可施放能力。法宝主动效果由 skill 执行，装备系统不拥有世界扫描和伤害事件总线。

载体随物品或玩家槽保存；界面编辑只在服务端提交。取出、死亡、物品移动和本命关系解绑需要明确物品归属；普通 Clone 与轮回规则分别对照。

## 规则裁定

交替培养标志是随玩家还是随某件法宝，现有载体明确为玩家；目标如果移入物品组件会改变换法宝行为，必须作为规则变更评审。不得凭“更合理”直接迁移该字段。服饰、动画护甲和渲染效果也在完整范围。

本命槽位（玩家 `benmingfabao`）归 xiulian 丹田，本命培养数据在物品上归本系统（[系统设计](../../development/system-design.md)“依赖规则”）。

## 待裁定（Q16）

`item` 目录有 `Armor01Item` 等 GeckoLib 盔甲类、渲染器与模型，`YvanchuxiuzhenModGeckoLibArmors` 为它们注册渲染器，但 `YvanchuxiuzhenModItems` 没有登记任何 `armor_NN_<部位>`；来源标签 `armor`、`toukui`、`xiongjia`、`kuzi`、`zhanxve` 等引用这些 ID，另有 `yaohua_0101_*`、部分 `clothes_*` 同样悬空，共 50 个 ID，不在来源注册表也不在台账中。数据转换工具把这些标签成员记为“来源没有登记该 ID”并去掉（见 `docs/reference/conversion/data_skipped.csv`）。需要负责人裁定：这些盔甲是否属于范围（登记并迁移 GeckoLib 类），还是按来源实际行为视为未启用；裁定前不登记。
