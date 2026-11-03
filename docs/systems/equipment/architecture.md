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

## 来源登记位置

GeckoLib 盔甲（`Armor01Item`–`Armor13Item`、`Fuqi02Item`、`Clothes05Item`、`Clothes19Item`、`Yaohua0101Item`，共 65 件，`Armor10Item` 只有胸甲）不在 `ModItems`，由 `YvanchuxiuzhenModGeckoLibArmors` 用独立的注册器登记，渲染器与动画由 `ModGeckoLibArmors`、`ArmorAnimationFactory` 装配。2026-11-02 之前的登记索引漏扫了这个类，现已补入台账（装备 61 件、`festival` 4 件）；它们随 GeckoLib 的引入迁移。

## 向 attribute 提交属性贡献（2026-11-03 attribute 归属阅读）

attribute 的归属阅读把下列元素归入本系统，实现时按 [attribute 公开操作](../attribute/README.md)提交加成层贡献：

- `ShuxingjisuanZong`：来源每 tick 比较四件防具与主手和快照 `kuijia1`–`4`、`wuqilinshi`（`bubianzhi` 随机标记与物品种类），变化时重算。目标监听装备变化，按当前装备提交防具 `lianqizong.<属性>`、饰品物品 NBT `<属性>`、四件防具的附魔（`dawn` → 生命回复，`powerup` → 灵力回复，`attdef` → 物理防御，`manadef` → 法术防御）、`colddown` 附魔等级 × 0.01（四件防具与四个饰品槽）与手持武器（`forge:wuqi` 或 `forge:fabao`）`lianqizong.attack_num`、`mana_Att`、`soul_Att`；同种物品组件变化也要重新提交。登录与重生时按当前装备重新提交（贡献随附件跨越死亡保留）。
- `Weaponshuxing`：妖化防具（`forge:yaohuaarmor`，按部位带 `toukui`、`xiongjia`、`kuzi`、`zhanxve`）的物品修饰符。Q17 裁定后最大生命已含防具炼器数值，`lianqizong.health_Max` 的生命修饰符不再添加，否则重复计入；击退抗性、攻速、移速三项（按基础值乘算）保留。
- `Attributexianshi`（原初系统）：主手长枪 `weaponspear_01`–`09`（`forge:yispear`）的 `forge:attack_range` +2；1.21.1 中对应的原版属性实现时核对。
