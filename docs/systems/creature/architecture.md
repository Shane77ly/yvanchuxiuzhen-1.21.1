---
topic: creature
role: architecture
status: draft
updated: 2026-11-11
---

# 生物：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 环境生物01–12直接迁移（2026-11-11）

本节记录已批准全量范围内的直接来源迁移，不采纳后文未批准的通用妖兽方案。编号和注册ID为核对主键：`organism_01`–`organism_08`对应`ButterflyEntity`，`organism_09`–`organism_12`分别对应`Organism09Entity`–`Organism12Entity`。每个编号独立保留实体类型、刷怪蛋、几何、动画、纹理、属性、尺寸、追踪参数及群系出生列表；共用实现没有合并注册身份。

01–08来源类在替换编号后完整方法同构；`convert_butterflies.py`逐次重建同构证据，08额外每刻向服务端广播2个`flash_ice`粒子。蝴蝶保留无重力飞行、蜂蜜瓶诱惑及三分之一驯服、仅主人治疗、非主人已驯服交互返回、原生后代初始化、接触攻击、两个目标选择器与来源goalSelector中的主人受击目标。刷怪蛋后代入口和普通繁殖分别沿原生生命周期。帧刷新、死亡20刻移除、伤害和过程动画控制器按来源处理；默认动画对每个模型表取自己的名称。

09没有来源自然出生条目，不能按编号补造；10为AMBIENT、权重4、数量1–4、无限制放置；11为CREATURE、权重10、数量1–4、草地方块且原生亮度大于8；12为WATER_CREATURE、权重5、数量1–1、来源水中位置规则。实际出生群系由World从来源实体事件与群系直接列表合并，保留顺序与重复项。09–12的特殊移动、物品交互和自由NBT核对及运行状态登记在本批月志。

本体玩法真值在服务端，客户端通过原生实体同步和GeckoLib渲染。对外查询与写入边界见[README公开操作](README.md#公开操作)。正式群系与维度尚未输出，当前不能将刷怪蛋/命令生成或注册检查当作完整自然投放验收。

## 灵兽袋关联实体死亡直接迁移（2026-11-11）

本节属于已批准全量范围内的直接来源迁移。`A:LingshoudaiqingkongProcedure`由原生实体`die`在`super.die`之后直接或经编号死亡过程调用，目标公开入口为`SpiritBeastBags.onCreatureDeath`，实现及袋实例写入均属于本系统。当前001、002、019、020、030、038、041、042、043、045、049、052已逐实体接线。来源022通过`Yaoshou022DangShiTiBeiShaSiShiProcedure`间接清袋，随后请求恢复重力；来源setNoGravity覆写始终写true，实际仍无重力；022已按此顺序接入。死亡入口按各编号来源迁移，不在共享实体基类无条件添加。来源证据及文件哈希见[死亡清袋来源表](ledger/death-bag-source-evidence.csv)。

清袋仅在实体`lingshoudaiuuid`非空时执行。按实体所在维度`world.players()`找`belongToMasterUUID`匹配的首个主人；不跨维度、不查离线玩家。来源玩家仅在存活时提供空朝向物品能力，扫描顺序为主背包、护甲、副手，目标NeoForge21.1.265的`CapabilityHooks`及`PlayerInvWrapper`同序。按来源`forge:lingshoudai`标签身份映射到目标`yvanchuxiuzhen:lingshoudai`，匹配首个袋物品`lingshoudaiuuid`；无匹配、无能力或能力不可写时沿来源结束而不补偿。

匹配后在原槽位放入同注册物品新建的1个物品；名称取来源`getDisplayName().getString()`并删除全部`[`、`]`，以字面名称组件写回。来源没有复制其余NBT，目标同样不复制原实例组件；名称原有颜色、格式及JSON结构也不保留。袋UUID仍使用来源根字段，对应1.21.1版本`DataComponents.CUSTOM_DATA`，本批不引入新的袋存档格式。

无论是否有关联袋，随后读取原生`zhaohuanwu`计分板；目标存在时使用与来源相同的get-or-create读法。分数恰为1时，007广播`brokenblue`25和`brokenpurple`10，020广播`brokendeeporange`25，013广播`brokenblack`15和`smokeblack`20；位置为实体中心高度，偏移分别为实体宽、高、宽，速度0.1。此后全部该分数实体在服务端`discard`，其他编号也保留立即移除效果。粒子只通过[particle公开API](../particle/README.md)查询，生物系统不写其他系统档案。

验证：来源过程、当前26个实体调用顺序、022间接入口、来源Forge40.1.0玩家能力、目标NeoForge21.1.265物品能力/显示名/计分板源码静态核对已执行。目标构建与12编号专服召唤死亡移除已通过；玩家背包清袋、来源实机、两版本对照未执行。待主会话专项覆盖主背包与副手匹配、重复UUID仅首个、名称括号剥离、数量归一与其他组件清空、无袋/无主人/跨维度/死亡主人不处理、普通死亡与召唤死亡，以及存档重登；袋本体收纳、释放和培养仍待实现。

## 编号妖兽直接迁移（2026-11-11）

本节记录已批准全量范围内当前实际接线的42个编号；其余编号继续保留在全量范围中。每个编号使用独立实体类，注册身份不由英文别名代替。类位于`system/creature/entity/yaoshou`，行为和动画按来源闭包拆分，复用已确认相同的初始化、属性和原生生命周期。

| 注册ID | 中文名 | 独立实体类 |
| --- | --- | --- |
| `yaoshou_001` | 花妖 | `Yaoshou001Entity` |
| `yaoshou_002` | 蔷薇花妖 | `Yaoshou002Entity` |
| `yaoshou_003` | 长风原野羊 | `Yaoshou003Entity` |
| `yaoshou_004` | 平原牦牛 | `Yaoshou004Entity` |
| `yaoshou_005` | 瞽猪 | `Yaoshou005Entity` |
| `yaoshou_006` | 赤霞天鹅 | `Yaoshou006Entity` |
| `yaoshou_007` | 月狼 | `Yaoshou007Entity` |
| `yaoshou_008` | 银月狼 | `Yaoshou008Entity` |
| `yaoshou_009` | 巨鲇 | `Yaoshou009Entity` |
| `yaoshou_010` | 长尾灵猫 | `Yaoshou010Entity` |
| `yaoshou_018` | 幻灵刺猬 | `Yaoshou018Entity` |
| `yaoshou_019` | 芝仙 | `Yaoshou019Entity` |
| `yaoshou_020` | 跑参 | `Yaoshou020Entity` |
| `yaoshou_021` | 跳羚 | `Yaoshou021Entity` |
| `yaoshou_022` | 蒲鸟 | `Yaoshou022Entity` |
| `yaoshou_025` | 跳蜥 | `Yaoshou025Entity` |
| `yaoshou_027` | 追风马 | `Yaoshou027Entity` |
| `yaoshou_029` | 龙鲤 | `Yaoshou029Entity` |
| `yaoshou_030` | 金翅雀 | `Yaoshou030Entity` |
| `yaoshou_038` | 丛林巨蚺 | `Yaoshou038Entity` |
| `yaoshou_041` | 斑纹甲虫 | `Yaoshou041Entity` |
| `yaoshou_042` | 雷斑鹿 | `Yaoshou042Entity` |
| `yaoshou_043` | 雷原巨鳄 | `Yaoshou043Entity` |
| `yaoshou_045` | 军颚蚁 | `Yaoshou045Entity` |
| `yaoshou_049` | 雪妖兔 | `Yaoshou049Entity` |
| `yaoshou_052` | 雪狐 | `Yaoshou052Entity` |

### 第三批十六编号

011、012、013、014、023、024、026、028、031、032、033、035、036、039、040、054各自保留独立实体类和来源行为闭包。013投桃、014飞羽、023爆蛋、026火焰、028水枪、031水弹和040游离光/落阳的弹体或技能过程使用专属目标实体；036黑泽按来源效果逐刻处理粒子与敌方减速；054裂冰斩通过skill公开冰锥入口并保留来源friendlist、方向和递归数量。014的妖兽发射与物品发射分别保持来源暴击参数，040落阳使用独立Geo模型与发光层。该批基础构建、ArchUnit、专服存活/移除和注册导出已验证；完整战斗、视觉、自然出生、背包清袋及两版本对照未验收。

| 注册ID | 中文名 | 独立实体类 |
| --- | --- | --- |
| `yaoshou_011` | 原野黑熊 | `Yaoshou011Entity` |
| `yaoshou_012` | 天虎 | `Yaoshou012Entity` |
| `yaoshou_013` | 大耳玄臂猴 | `Yaoshou013Entity` |
| `yaoshou_014` | 银羽黑天雕 | `Yaoshou014Entity` |
| `yaoshou_023` | 彩凤绶带 | `Yaoshou023Entity` |
| `yaoshou_024` | 化灵螳螂 | `Yaoshou024Entity` |
| `yaoshou_026` | 蓝焰虎 | `Yaoshou026Entity` |
| `yaoshou_028` | 巨鳖 | `Yaoshou028Entity` |
| `yaoshou_031` | 河流龟 | `Yaoshou031Entity` |
| `yaoshou_032` | 撞角犀 | `Yaoshou032Entity` |
| `yaoshou_033` | 毒孢菇 | `Yaoshou033Entity` |
| `yaoshou_035` | 山魈 | `Yaoshou035Entity` |
| `yaoshou_036` | 河流鳄 | `Yaoshou036Entity` |
| `yaoshou_039` | 光狐 | `Yaoshou039Entity` |
| `yaoshou_040` | 光环空母 | `Yaoshou040Entity` |
| `yaoshou_054` | 冰鼹 | `Yaoshou054Entity` |

[来源闭包证据](ledger/yaoshou-source-evidence.csv)有517行，逐编号记录实体及递归调用过程的真实来源路径、原始文件SHA256和直接调用；同一共享过程按编号保留各自可达关系。静态逐项比较本批来源初始化的13项资质字符串与默认等级，目标`YaoshouShared.initialize`实参全部相同。该证据不等于517行对应过程语义均已验收。

共同生命周期保留`finalizeSpawn`初始化、`baseTick`来源行为后刷新尺寸、受击先记录来源目标及动画再调用原生伤害、逐实体`super.die`后清袋、死亡600刻移除。001经验奖励为5，本批其余编号为0。各类保留来源`aiStep`中原生更新后的`updateSwingTime`；001/002此前遗漏的覆盖已在本批补齐。原12个编号保持`ON_GROUND`、`MOTION_BLOCKING_NO_LEAVES`、亮度大于8及来源`Material.GRASS`的8成员标签；自然出生群系由World提供，尚未验收自然供给。

001/002使用同步体型和皮肤属性，按来源模型表选择变体。019/020眼高分别为0.4和1.0，020保留来源`spring`动画字符串。030保留无`FloatGoal`的飞行导航、强制无重力、空跌落检查、来源速度乘0.7及2刻动画更新。038/043保留溺水免疫；041/045保留跌落免疫，041受击打断移除缓降。042保留乘客偏移−0.2、雷击免疫及5/10/15/20刻雷粒子；043保留无`FloatGoal`及来源落雷命令。045/052眼高分别为0.3和1.7。052继续调用来源053吹摆过程的编号条件：仅053播放该动画，伤害与冻结仍执行；未经裁定不修正来源矛盾。

目标精确版本父类与已列生命周期静态核对已执行；完整技能链、战斗观感、袋背包、保存重读、完整视觉、自然出生、多人、1.18.2版本实机及两版本对照未验收。034/044来源没有注册，不新增占位身份；其他26个来源编号仍需实际实现，不以资源复制或注册壳计为完成。

### 第二批十四编号

003–010、018、021、022、025、027、029各自保留独立Entity与来源过程闭包。003与021的羊类八动作按来源条件运行，021复用已核对的003过程实现；006保留摔落免疫；007默认等级随机8或9、召唤倒计时和变体贴图保留。008的狼嚎、飞扑、撕咬和召唤两只007保持各动作阶段、延迟与参数；母体与子体的friendlist运行期共享由relation公开操作承担，子体初始化加入007团队后母体也观察到该成员。

009/029各有独立匿名水生MoveControl、WaterBoundPathNavigation、WATER寻路代价0、无目标Goal、水流不推动和原生碰撞出生检查；出生位置及上格都必须是WATER，水下呼吸由原生实体类型标签声明。009保留溺水免疫，初始化泳速为原速度乘6.5；029保留同一倍率及来源未列候选但仍有分派入口的027飞扑分支。

018保留眼高0.3与来源没有调用Entityjingyanjisuan的差异，初始化不写experience_Max。022保留FlyingMoveControl、无FloatGoal、速度乘0.7、强制无重力、空落地检查和错误018普攻动画判断。025的水球在蓄力16刻通过SkillSummons发出，30刻结束，候选、游走和逃跑完整来源分支均保留。027保留乘客偏移−0.5、受击移速累积和落地震击，FALL免疫不跳过其causeFallDamage来源回调。目标构建及游戏覆盖由本月日志分别登记，不从身份或资源登记推导完整战斗验收。

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

## 实体属性（2026-11-03 attribute 归属阅读）

来源的两个属性注册 `entitydaxiao`（体型，0.7–10，默认 1）与 `yaoshoupifuxianshi`（皮肤变体，1–5，默认 1）归本系统，台账行已移入本系统。`EntityAttributeModificationEvent` 把前者加给妖兽 `yaoshou_001`–`054`（缺 034、044）、`yaoshou_01701`、修者 `xiuzhe_0101` 与桃木精，后者加给 `yaoshou_001`、`002` 与 `xiuzhe_0101`；由生成、灵兽袋收放（`Lingshoushouna`、`Lingshoudaiyoujian`）与修者身高过程读写。
