---
topic: equipment
role: architecture
status: partial
updated: 2026-11-07
---

# 装备、法宝、本命与饰品：架构调查与提案

来源事实与目标方案分别陈述。前半的完整系统方案尚未整体采用；本页末尾单独记录已按批准系统设计实施的弓与拂尘批次。源码定位采用 [系统导航](../README.md)的 A/B 规则。
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

## 弓与拂尘（2026-11-07）

### 来源与状态

18 把弓为 `Weaponspeedbow01–09Item` 和 `Weaponlongbow01–09Item`，实际均继承 `SwordItem`，选中更新共用 `A:Weaponspeedbow01DangGongJuChuXianZaiShouZhongShiProcedure`。九把拂尘为 `Weaponhossu01–09Item`，共用选中与挥动过程 `Weaponhossu01DangGongJuChuXianZaiShouZhongShi`、`Weaponhossu01DangShiTiHuiDongWuPinShi`；第一把为剑，其余八把为多用途 `TieredItem`。隐藏物品、实体与飞行过程分别是 `FuchenItem`、`FuchenEntity`、`FuchenTouZhiWuFeiXingTick`。本批接回这些入口及其消费者，其他装备尚未实现。验证结果见本节末尾及项目月度记录。

Q57：弓读取实体 `synchronizedata.rightclicking`，绑定的右键过程为空。两个来源的 `src` 与 `elements` 全检只找到弓过程读取与 B 的 `Zxc` 游离实验块（另含 `rifhtclicking` 拼写），未找到有效写入。目标保留来源条件流程与剑类型，不新增鼠标同步；自然右键不能开始蓄力。该断链的修复取舍待负责人裁定，人工注入标记的检查只证明条件分支运行。

### 弓的条件流程

`Skills.arrow` 非空且满足资源条件才推进：灵气箭 `skill_arrow_02` 的灵力至少为层数 × 5，或者库存含所选箭；其他箭必须在库存中。创造与无限附魔均不绕过这一步。库存比较按完整物品实例，消耗则按物品类型移除一件，保留来源区别。

按住标记时每刻基础拉弓速度为 3，再加快速装填等级；层数至少 5、潜行且所选为第 01/02/08/09 箭、普通箭或光灵箭时再加 1。速弓每刻增加全量，长弓增加一半；不足 100 时允许本刻越过 100，下一刻发现至少 100 才写 `manxvli`，广播一枚 `xvliman` 并播放 `bowxvliman`。`xvlilizi` 仍用客户端本地 `addParticle`，不把来源服务端无效的调用改成广播。

松开且蓄力大于 0 时，把用于发射的蓄力截至 100，写 `arrowskill`，再让主手损耗一点耐久（创造同样消耗，破损后伤害值清零）。轻羽等级先于损耗读取；弓类别与无限附魔在损耗后读取，保留破损时的先后次序。发射位置为眼高减 0.1、无散布，速弓速度为 `3 × 蓄力 × 0.01 + 轻羽等级 / 2`，长弓的首项系数为 6。

| 所选箭 | 基础伤害 | 固定击退 | 发射声音与音量 |
| --- | --- | --- | --- |
| 第 01 箭 | 1 | 0 | 原版箭发射，1 |
| 普通箭 | 最终物攻 / 3；Alt 再乘 1.5 | 1；Ctrl 穿透 5 | 原版箭发射，1 |
| 第 02 箭 | 0.1 | 0 | `bowshoot3`，1 |
| 第 03 箭 | 0.1 | 1 | `bowshoot4`，1.2 |
| 第 04 箭 | 0.1 | 1 | `bowshoot5`，1；再播 `bowshoot1`，0.3 |
| 第 05 箭 | 0.1 | 0 | `bowshoot6`，1 |
| 第 06 箭 | 最终物攻 / 3 | 1 | `bowshoot1`，0.3 |
| 第 07/08/09 箭 | 0.1 | 0（来源 `(int)0.5`） | `bowshoot1`，0.3 |
| 原版光灵箭（使用第 10 箭实体） | 0.1 | 0（同上） | 原版箭发射，1 |

技能箭为静音弹体，拾取许可与来源一致，具体命中和物品掉落由 skill 消费。普通箭保持原版注册类型；21.1.265 的 `Arrow` 构造器从发射武器快照计算穿透，命中从该快照读取击退。因此只用原版弓的快照携带冲击 1 和按 Ctrl 决定的穿透 5，不把手持弓的其他附魔带入箭。两项随原生 `weapon/PierceLevel` 存档，避免临时匿名子类重读后丢失行为。

发射后清蓄力。第 02 箭扣层数 × 5 灵力，即使由库存分支放行也照扣，可能出现负值；扣后不足下次费用则只清箭物品。其他箭没有无限时移除一件，库存不再有箭时只清箭物品。轮盘序号不重置；普通箭不消费满蓄力标记，技能箭按其原有飞行过程消费。

独立算例：层数 5、潜行、快速装填 2 时，速弓每刻 +6、长弓 +3；物攻 9、Alt、长弓满蓄力且轻羽 2 时，普通箭基础伤害 4.5、速度 7。层数 5、灵力 20 但库存有灵气箭时，发射扣 25 后为 −5，箭物品选择清空而序号 3 保留。

### 拂尘与生命周期

挥动时 `fuchenshiyongtick < 1` 且灵力至少为层数 × 2 + 2，才生成速度 0.5、基础伤害 `(float)0.1`、击退 0、穿透 10 的静音 `projectile_fuchen`；扣灵力、设冷却 20、实际挥动物品损耗一点耐久。冷却仅在选中九把拂尘之一时每刻减一，切走暂停。目标 `yihossu` 对应的来源标签没有这九个成员，不用空标签替代实际入口绑定。

第二至第九把的工具等级恒为 1、任意方块挖掘速度为 2，能采掘斧/锄/镐/锹标签中的方块但不能采掘要求铁或钻石等级者；保留五类默认工具能力。主手伤害修饰为 0、攻速修饰 −3.5；挖掘损耗 1，近战损耗 2（来源的耐久调用传入受击者）。耐久值从内容表取得，不另存第二份定义。

弹体在 `super.tick` 后推进：有发射者才关闭重力，每刻广播 `lingqi` 四枚、三轴散布各 1；在以自身为中心、半边长 2 的 AABB 中按距离排序，敌友由发射者判定。每个对象只在没有 `uid<UUID>` 标记时执行一次 `shanghai 无 <发射者UUID> false 2 1`，然后标记；无伤害能力的对象仍记标记。第十刻移除并清计数，插地也移除。命中生物后无条件减一插箭数，保留来源调用顺序。弹体自身不可见、没有实际可拾取物品；内部保留非空箭物品以满足目标存档格式。

隐藏 `fuchen` 物品右键开始使用、无动作、时长 72000 刻，松开立即发射速度 2、基础伤害 0.1、无穿透的同类弹体，禁止拾取；物品无耐久，不扣库存或灵力。来源发射辅助查询空声音 ID，没有可迁移的有效声音，不给它补造音效。

玩家持久字段沿用 xiulian 的逐键公开操作，箭选择归 skill；服务端持有玩法真值。选中推进在玩家 Post tick 处理，挥动复用 `ItemSwingEvent`，实体通过 equipment 内容表登记，渲染只在本系统客户端入口装配。弹体计数和逐对象命中标记随实体持久数据保存，穿透沿用原生存档键。

### 验证范围

静态来源与目标 21.1.265 源码已核对；第二至第九把拂尘归一化后，除了名称与耐久值外源码一致。`build compileSmokeJava`（含 ArchUnit）通过；仅一次 `runSmokeClient -PsmokeOnly=equipmentweapon`，五项全部符合并 `DONE`，客户端正常保存退出，ERROR 0。验证覆盖挥动与重复挥动、扣费耐久、切走暂停及选中恢复、隐藏物品松开、范围敌友与一次命中、十刻寿命、自然右键空过程，以及人工标记下的两种蓄力、普通箭发射/存档与灵气箭库存放行后扣到负值。

专用服务器一次启动到 Done，原生 `stop` 后所有维度正常保存退出，ERROR 0；实体导出 68 个，包含 `projectile_fuchen`。

证据：`tmp/equipment-server.log`、`tmp/equipment-build.log`、`tmp/equipment-smoke.log`、`tmp/equipment-report.txt`、`tmp/equipment-family-source.txt`。1.18.2 实机、两版本对照、多人、完整声音及视觉验收未执行；人工标记不代表来源自然右键入口可用。品阶、饰品飞行减耗、妖化与其他装备功能留待后续批次。
