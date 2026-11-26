---
topic: source-architecture-survey
role: data
status: partial
updated: 2026-11-26
---

# 1.18.2 版本架构调查

## 范围与证据等级

两个来源工程分别为 `yvanchuxiuzhen` 和 `yvanchuxitong`，元数据均声明版本 0.5.2、Minecraft 1.18.2，且互为必需依赖。构建文件使用 Java 17、Forge 40.1.0、official 1.18.2 mappings、ForgeGradle 5.1.+；修真工程 mcreator.gradle 另依赖 GeckoLib Forge 1.18 的 3.0.57。当前读取的是参考目录内的生成 Java、MCreator 文件及资源，未运行 1.18.2 游戏。

本轮完成全部 Java、MCreator 元素、过程连接调用的静态索引与关键架构调用链核对；没有逐一证明每个过程分支的行为，也没有把已有移植工程的研究结论自动当作本轮验证。文中“已核对”仅指明确列出的源码位置。零调用处、文件存在和命名匹配分别保留其证据边界。

## 规模基线

| 分类 | yvanchuxiuzhen | yvanchuxitong | 合计 |
| --- | ---: | ---: | ---: |
| Java 文件 | 6,052 | 1,822 | 7,874 |
| src/main/resources 文件 | 14,894 | 1,388 | 16,282 |
| Procedure | 2,108 | 1,320 | 3,428 |
| 物品注册（含方块物品、GeckoLib 盔甲） | 2,120 | 188 | 2,308 |
| 方块注册 | 1,026 | 154 | 1,180 |
| 实体类型注册（含形态/弹体） | 267 | 5 | 272 |
| 方块实体类型注册 | 76 | 79 | 155 |
| 菜单注册/菜单类 | 44 | 48 | 92 |
| 附魔注册 | 84 | 1 | 85 |
| 状态效果注册 | 77 | 0 | 77 |
| 群系注册 | 68 | 0 | 68 |
| Feature 注册 | 220 | 0 | 220 |
| 流体注册 | 10 | 0 | 10 |
| 粒子类型注册 | 312 | 3 | 315 |
| 声音注册 | 150 | 0 | 150 |
| 画作注册 | 1 | 0 | 1 |
| 属性注册 | 2 | 0 | 2 |
| 游戏规则注册 | 2 | 2 | 4 |
| 玩家字段 | 166 | 17 | 183 |
| 维度字段 | 4 | 7 | 11 |
| 主世界数据字段 | 11 | 6 | 17 |

总计 4,939 条注册声明（2026-11-02 补入此前漏扫的 `ModGeckoLibArmors` 65 件盔甲、`ModAttributes` 2 个属性、`ModGameRules` 4 个游戏规则；2026-11-07 补入实体内部嵌套注册的彩色闪电）、211 个状态字段及 4 个独立存档常量。基础索引另记录 7,683 处 Procedure 调用、5,752 处按名字段访问候选和 30,945 处字面量键调用候选。后续按 Mod/作用域解析获得 5,752 处可识别访问，另保留 315 处未解析成员；两个 5,752 的统计口径不同。2,841 个字面量键分组包含 NBT 与普通字典，不是已经确认的状态总量。

资源还包括 8 个维度及 8 个维度类型、161 份结构文件、561 份配方、109 份掉落表、230 份 geo、233 份 animation。结构文件存在不证明每个结构有自然生成入口。

**计数修正**：初次提取遗漏了 84 个方块物品 helper 注册，修真工程物品数已从 1,971 修正为 2,055；初次变量表把 4 个 `DATA_NAME` 常量计入状态，已拆成 211 + 4。以当前生成文件为准。

`yvanchuxitong` 有 133 个 block/entity Java 文件但仅 79 个方块实体注册，还有 5 个 entity Java 文件但 4 个实体注册。不能把所有类直接等同于可运行内容。菜单 `plaque_gui` 和粒子 `lingqi` 在两个命名空间都有同名路径；完整 ID 不冲突，压平命名空间才需要改名处理。

## 全量结构核对结果

7,465 个编辑器元素全部建立来源目录与文件关联，38 个元素标记锁定代码。3,428 个过程的 Blockly 已连接子树与生成 Java 的同 Mod 直接过程调用集合全部一致；192 个过程存在游离调用节点。该对照只比较调用集合，不证明条件、参数和执行次序完全相同。

新增索引包含 531 个注解事件方法、86 个消息类、92 个菜单、22 个命令类、307 个实体相关类和 2,515 处间接入口。类级结构图到达 3,342 个过程，86 个未到达；不将结构可达等同于游戏生效。

动态状态访问按载体及还原表达式形成 630 组，183 个玩家字段有逐项 Clone 策略，211 个字段有生命周期与所有者提案。全部来源元素已有候选归属，19 个系统均有具体入口、载体、接口和待裁定事项。详见 [公共入口与依赖](../../development/entry-and-dependency.md)、[系统导航](../../systems/README.md)、[机器证据索引](inventory/README.md)。

## 系统与实际入口

下表是架构调查，不是全系统验收结论。引用标号对应后面的源码位置表。

| 系统 | 已核对的入口与数据流 | 对架构的约束 / 尚待核对 |
| --- | --- | --- |
| 成长与闭关 | [S01] GUI 检查坐下、天数、冷却及本人绑定功法；[S02] tick 根据 `xiuliankaiguan` 分别调用修炼与研习 | 修为、待吸收池、功法经验分开；全体体质/金丹/元婴与雷劫分支仍需逐项核对 |
| 功法与书籍 | [S03] 书籍写 `belongToMasterUUID`、`ExercisesLevel`；[S04] 装备书写 `ExercisesExp` 并升级 | 绑定和成长属于书籍实例；A 菜单槽与比较缓存已核；B 同名字段未见显式外部消费，另有穴位/星宿学习会话 |
| 属性和战斗 | [S02] 调属性总计算；[S05] `LivingHurtEvent` 接入敌友与结算；另一 Hurt 监听只声明局部变量 | 分清真正效果与空监听；其他伤害入口/词条需记录目标事件阶段与运算顺序 |
| 技能、移动、关系 | [S02] 调轻功、飞行、冷却与饰品；Friend 系列调用已索引 | 技能编号/物品、施法状态、好友和队伍不是一个万能 CompoundTag；逐技能成本/命中/表现待核 |
| 炼器 | [S06] 交互把玩家 `Refiner_LEVEL` 写进共享方块 `RefinerLEVEL`；[S07] 按钮直接调用 LianqiBegin，后者分升级和计算 | 明确职业快照与操作者；全部图纸、材料贡献、成品和词条消费者待逐条对照 |
| 炼丹 | [S08] `fanhuizhi=0`，生成 Java 未调用等级检查；开炉写进度、经验、炸炉概率数据 | 门槛冲突不能凭名称补回；药效运算、成品与服用单位需单独核对 |
| 符箓 | [S09] 菜单到绘制；读取方块 `RuneLEVEL+1`，操作纸/墨/成品槽 | 知识、组合、装置状态、成品及释放是不同数据；51 个 itemfulu 编号项逐项保留 |
| 阵法 | [S10] 阵基要求 `powerneed>0` 且 `power>powerneed`，按触发模式调用效果 | 布阵、供能、触发和效果消费者分开；写伤害字段不等于实际扣血 |
| 妖兽与生物 | [S11] 寄魂蟹类实现 IAnimatable，受伤、初始化、baseTick 接 Procedure，模型按大小缩放 | 实体族同时包含行为/形态/动画/体型/交互；271 是实体类型数 |
| 蛊虫、灵兽与培养 | [S12] 蛊虫 tick 用 UUID 查关联实体并更新释放装置状态；灵兽袋过程存在公共清理入口 | 主人/关联实体、装置与实体存档需要明确关系；不因未细读而删减此范围 |
| 材料与世界配置 | [S13] Worldbegin 初始化维度状态，写材料/药草/结构长字符串，随后载入 NPC 和价格 | 静态定义与世界实例必须分离；材料的炼器、药效、阵法用途分别记录 |
| 世界/结构/副本 | 8 个维度类、12 个 teleporter 类；[S13] 固定结构清单；结构 NBT 和玩家副本 tick 可定位 | 全部自然投放、固定场景、回程与管理工具需要核对；注册不证明可获得 |
| 银行与交易 | [S14] 个人 bank 按灵石层级写入；[S15] 商店从语言键解析 productlist，分别处理 1/10/100/1000 钱币和 4 倍级差灵石 | 多货币保留单位；账户归玩家；语言 JSON 中机器配置须迁出 |
| 现场拍卖 | [S16] 拍卖方块按 paimaijindu 推进，导入 NPC/商品、竞价并清理 | 拍卖是现场装置流程，不能未经确认改成全服市场；成交/离场/卸载待核 |
| NPC 与任务 | [S17] renwuitem 保存任务游标；按 `%`、`/` 解码，执行选择、NBT 修改、条件、传送、药效、指令和跳转 | 现有任务动作需要等价实现；这是已有内容格式，不是新建通用脚本平台的理由 |
| 生活生产 | [S18] 装订入口从真实菜单槽 0/1 扣 3/1；锅具、木工、缝纫、作物过程与菜单已索引 | 职业、配方、装置、成品分开；全部生活分支列入批次 D |
| 特殊容器 | [S19] 乾坤袋菜单建 27 格并尝试绑定物品/实体/方块库存；关闭时未绑定库存归还或掉落 | 乾坤袋容量已核为 27/54/108/220；材料袋另为玩家计数仓，不能统一按物品库存保存 |
| UI、命令和表现 | 92 菜单类、95 client/gui 文件、22 个命令类，以及独立渲染/模型/粒子文件 | 菜单数不覆盖 HUD/覆盖层；UI 接线、管理权限、显示与实际效果分别追踪 |

## 本轮双源差异核查（2026-11-23）

对照基线是 `port/20261123-parity-integration@cdcc561a`，来源是[锁定的两个工程](README.md)。**本轮新增问题从 Q244 起**，定位、影响、状态和最小复核步骤链接到下方索引对应的系统正文。没有把“未测”当作“未实现”。[本轮检查范围与没有覆盖的部分](../../systems/project/logs/2026-11.md#2026-11-23-双源差异只读审计)另记；此前七处玩法与一次转换修复见[上一批](../../development/remaining-work.md#2026-11-23-两份来源的行为修复)，不算在本轮新增项中。

续批 **Q265–Q274** 以首批合入点 `d70a570a` 为基线，均未修、未实机；[实际覆盖和反例](../../systems/project/logs/2026-11.md#2026-11-23-双源差异续查门户采集与物品流转)。

续批 **Q275–Q278** 以 `d0834be4` 为基线，均未修、未实机；[奖励物品和地物投放的核查边界](../../systems/project/logs/2026-11.md#2026-11-23-双源差异续查任务奖励与地物投放)。

续批 **Q279–Q281** 以 `2e85c89c` 为基线，均未修、未实机；[任务奖励使用样例及反例](../../systems/project/logs/2026-11.md#2026-11-23-双源差异续查任务奖励的实际使用)。

## 具体问题与裁定位置

问题在所属系统维护唯一正文，此处只保留统一索引。来源差异、疑似错误、消费断链和拿不准的问题都保留稳定编号，区分待核实、待裁定、已批准、已实现与已验证；负责人2026-11-14要求后续集中处理。未获批准的玩法调整保持来源，不把编译或单项通过当作两版本行为对照。历史问题继续保留原编号和当时证据，不因新增问题重新调查已核实范围。

| 编号 | 核对结论 / 评审主题 | 唯一正文 |
| --- | --- | --- |
| Q01 | 同名功法字段已按类型分开；实际栏位和学习小游戏分别建模 | [功法](../../systems/gongfa/architecture.md) |
| Q02 | 制造职业快照与当前玩家/装置绑定，决定多人协作规则 | [炼器](../../systems/lianqi/architecture.md) |
| Q03 | 炼丹等级检查在图中为游离块，Java 与已连接图一致；恢复门槛属于行为调整 | [炼丹](../../systems/liandan/architecture.md) |
| Q04 | 阵法五行费用倍率缺失、分派名称错配、火2类别错配及部分伤害链未闭合 | [阵法](../../systems/zhenfa/architecture.md) |
| Q05 | 商店灵石购买：已修正一级找零错用二级金额 | [经济正文](../../systems/economy/architecture.md)、[复现与验证范围](../../systems/economy/logs/2026-11.md#阶段-4-首批商店找零) |
| Q06 | 世界定义在初始化门外重写，静态定义/实例/管理覆盖需分开 | [世界](../../systems/world/architecture.md) |
| Q07 | 真实容器绑定、材料仓玩家归属、关闭与死亡后的资产去向 | [容器](../../systems/storage/architecture.md) |
| Q08 | GeckoLib 实际依赖与元数据 optional 标记不一致 | [整体架构](../../development/architecture.md) |
| Q09 | 注册、编辑器元素、Java 类和结构可达口径不同 | [公共入口](../../development/entry-and-dependency.md) |
| Q10 | 动态键已有全量表达式与主要数据族；全部取值域尚未逐项确认 | [公共入口](../../development/entry-and-dependency.md) |
| Q11 | taskbegin 后分支被前面的 OR 条件覆盖；剧情任意键需保留真实作用域 | [任务](../../systems/quest/architecture.md) |
| Q12 | 裤装比较标记写入其他槽；主手仅比较 Item 类型 | [属性](../../systems/attribute/architecture.md) |
| Q13 | 命令伤害与未闭合字段链分开；关系读取及击杀计数时机待裁定 | [战斗](../../systems/combat/architecture.md) |
| Q14 | 本命成长交替标记在玩家体质载体，迁到物品会改变换装行为 | [装备](../../systems/equipment/architecture.md) |
| Q15 | 配方 `skillarrow_10hecheng` 一次产出 5 个最大堆叠为 1 的 `skill_arrow_10`，1.21.1 拒绝此类配方；2026-11-02 已裁定：物品保持 1，配方产出改为 1 | [技能](../../systems/skill/architecture.md) |
| Q17 | 防具与武器的炼器数值（`lianqizong.health_Max`、`attack_num`）计入显示值与战斗结算，但实际最大生命只在妖化防具上生效、近战伤害基础值不含武器数值；2026-11-03 已裁定：实际属性计入炼器数值，与显示一致 | [属性](../../systems/attribute/architecture.md) |
| Q18 | 伤害结算把受击方主修功法与伤害同属性的五行值 × 层数 / 4 加到伤害上（功法五行值全为正），受击方修同属性功法反而受伤更多；系统设计称其为“抗性”；2026-11-03 负责人指示暂按来源行为，作为待处理问题保留，之后再议 | [属性](../../systems/attribute/architecture.md) |
| Q19 | 好友功能的来源缺陷：申请时向申请者打印调试文字；同意时重复判定比较对象写错，会重复添加；删除在线好友时读错对象，通常只删除本人一侧；删除时把该团队整体移出友方集合。2026-11-03 评估为轻微，保持来源行为，待再议 | [关系](../../systems/relation/architecture.md) |
| Q20 | 改团队名界面 `Gui0301` 的唯一入口 `Gui03anniu` 没有调用者；`ling_15` 提示文本描述的改名功能不存在；`Playeraddplayer` 没有调用者。保留台账、不实现，待裁定 | [关系](../../systems/relation/architecture.md) |
| Q21 | 随机体质只取 1–10，第 11、12 种体质随机取不到；关闭出身界面时其他体质（含空）按第 12 种生效；体质锁定键 `tizhilock` 没有写入者。评估为轻微，保持来源行为 | [修炼](../../systems/xiulian/architecture.md) |
| Q22 | 境界名按服务端语言翻译后存入（单人随客户端语言、专用服务器为英文）；升级文字中法术防御增加的差值方向与神识攻防的标签写错。评估为轻微：境界名统一存中文，显示错误保持来源 | [修炼](../../systems/xiulian/architecture.md) |
| Q23 | 孕育元婴时体质数 `tizhinumber` 加在空物品上，体质数不变（元婴不进入丹田体质列表的计数）。评估为轻微，保持来源行为 | [修炼](../../systems/xiulian/architecture.md) |
| Q24 | 丹田体质排序在打开的体质少于 `tizhinumber` 时无限循环（服务端卡死）；翻页时最后一页恰好 9 个体质则第 9 行不显示。评估为轻微：排序扫描到最后一个三位编号为止，第 9 行保持来源 | [修炼](../../systems/xiulian/architecture.md) |
| Q25 | 丹田本命槽上限 64，放入多个妖丹后关闭再打开只剩 1 个，其余消失。评估为轻微，保持来源行为，待裁定 | [修炼](../../systems/xiulian/architecture.md) |
| Q26 | 书架藏宝的功法表：第 6 表随机 1–10 但只有 8 个分支，取到 9、10 时不给书而书架物品照常消耗；第 7 表有 12 个分支但随机只到 11，第 12 本取不到。评估为轻微，保持来源行为 | [功法](../../systems/gongfa/architecture.md) |
| Q27 | 原先误读参悟台继承清槽条件：`CanwudianjiProcedure:213–260` 的清槽位于升级分支之外，成功继承无论是否升级均消耗典籍；原疑点撤销，目标按来源修正，验证边界见正文 | [功法](../../systems/gongfa/architecture.md) |
| Q28 | 星宿穴位学习小游戏不可达：研习台 `gongfaxvexi` 的物品不在创造栏、没有配方，结构与代码都不放置它，槽位没有放入书的途径；界面 `gongfaxvexi_gui` 只登记、从未打开（右键研习台打开的是 `TripodGUI`）。负责人已于2026-11-16明确批准排除这部分（研习台方块、界面与429个过程）；保留来源身份与不可达证据，不再作为待裁定或开放移植项 | [功法](../../systems/gongfa/architecture.md) |
| Q29 | `/shanghai` 的防御 `Zhilingdefence` 把玩家体质物品读入 `itemlinshi`，之后的体质防御修正与神识、法术防御倍率却读从未赋值的 `tizhi`（空物品）：体质对 `/shanghai` 防御的全部修正（`*DefCorrect`、`*DefCorrectPercentage`、`Souldefof*AttCorrectPercentage`、`ManaDefDefCorrectPercentage`）不生效。评估为轻微，保持来源行为，待裁定 | [战斗](../../systems/combat/architecture.md) |
| Q30 | 物理伤害与 `/shanghai` 结算的疑似错误：① 破甲系数 `pojia` 初值 0，没有 `pojia` 附魔时受击者基础物理防御与护甲 `lianqizong.attack_Def` 乘 0 后不计入，只有附魔 `attdef` 与 `basalt` 起作用；② 暴击结算值紧接着被物理公式覆盖，暴击只有粒子；③ 物理攻击量乘的 `AttackAttCorrectPercentage` 读的是受击者体质而不是攻击者；④ 法术防御的护甲项读 `lianqizong.mana_Att`（神识项读 `soul_Def`）；⑤ `/shanghai` 没有“任一方团队为空则不免伤”的判断，而 `LivingHurtEvent` 有；⑥ `/shanghai` 闪避随机数为 0 时即使没有闪避附魔也使伤害无效（约 1/101）；⑦ 吸血在 `LivingAttackEvent` 与 `LivingHurtEvent` 各回复一次。评估为轻微，保持来源行为，待裁定（与 Q13 的关系读取、击杀计数时机同属战斗） | [战斗](../../systems/combat/architecture.md) |
| Q31 | `DamageIntegration`、`DamageDefense`、`ManaDamage`、`ManaDefense`、`SoulAttjisuan`、`SoulDefjisuan`、`DamageXiaoguo`、`EntityBeforeHurt2` 没有过程调用者、事件订阅或元素触发（后两者是空壳），经受击者持久化键 `ManaAttackDamage`、`ManaAttackUUID`、`damagetype`、`Damage_multiplier` 传值的另一版伤害计算不可达。评估为轻微：不迁移，待裁定（与 Q13 的未闭合字段链同属一处） | [战斗](../../systems/combat/architecture.md) |
| Q32 | 命中附魔的疑似错误：毒药类与 `sawtooth` 的效果时长为“已有同效果剩余 tick × 20 + 600”；`lingtikiller` 取 `zhongshang` 的等级，`lingwen` 的灵力消耗取 `qishi` 的等级；`liuqing` 的时长使用此前分支才赋值的 `fumolevel`（单独触发时时长为 0）；`chaos` 的范围中心把受击者视线方向的 z 分量当作 z 坐标；无队伍的生物穿戴 `firearmor`、`icearmor`、`discharge` 时自己不是自己的友方（空队伍名取不到友方集合），会被自己的范围伤害击中。评估为轻微，保持来源行为，待裁定 | [战斗](../../systems/combat/architecture.md) |
| Q33 | 状态效果的疑似错误：`duyao_13` 的伤害读 `duyao_01` 的等级；`duyao_20`（尸毒）对玩家的伤害先乘体质放大再被赋值覆盖，放大不生效；`duyao_21`（曼陀罗）与 `duyao_20` 共用计数键 `duyao20tick`，伤害读 `duyao_07` 的等级；`effect_010`、`effect_017` 的说明文本写“每两秒恢复等级 × 5”，实际每秒恢复 (等级 + 1) × 10；`duyao_09`（番木鳖）把体质放大乘在目标生命上而不是伤害上。评估为轻微，保持来源行为，待裁定 | [战斗](../../systems/combat/architecture.md) |
| Q34 | 灵力、神识伤害的死亡消息：来源语言键写成 `death.attacklinglidamage.player`、`death.attackjinglidamage.player`（缺一个点，且 `lingli` 多一个 `l`），伤害消息 ID 是 `linglidamage`、`jinglidamage`（有来源者时加 `.player`），实际死亡消息键 `death.attack.linglidamage.player` 找不到译文，玩家看到原始键。评估为轻微，保持来源行为（语言键原样迁移），待裁定 | [战斗](../../systems/combat/architecture.md) |
| Q35 | 淬毒炼药锅 `CauldronduyaoZaiFangKuaiShangYouJian` 的条件前后不一致：蜂毒（`beepoison`）要求武器还没有该附魔，其余七种毒药要求武器已经有该附魔（再附为 1 级）；右键空气的 `Cailiao141…146` 则要求副手武器没有该附魔。评估为轻微，保持来源行为，随 lianqi 迁移时裁定 | [炼器](../../systems/lianqi/architecture.md) |
| Q36 | 技能冷却计算的疑似错误：`Colddownjisuan` 中增量变量 `linshicolddown` 在六个分组之间不重置，某组两个条件都不满足时沿用上一组的增量；阴阳组的“冷却不超过上限”比较读取的是土组冷却 `colddownSoil`（其余组读各自的）。评估为轻微，保持来源行为，随 skill 迁移 | [技能](../../systems/skill/architecture.md) |
| Q37 | `/jineng` 命令没有权限要求（`/shenfa`、`/smanship`、`/flyskill`、`/technique` 都要求权限 1），任何玩家都能给自己生成任意参数的技能书。评估为中等偏轻（只有作者的测试命令在用），保持来源行为，待负责人裁定是否补权限 | [技能](../../systems/skill/architecture.md) |
| Q38 | `Jiancewuqi` 的“武器太重”检查读取的是所选技能书的 `lianqizong.weightnum`，不是手持武器的，技能书没有该键，条件永远不成立，提示“武器太重了！无法正常释放技能”不会出现。评估为轻微，保持来源行为（不实现这条永不成立的检查），随 equipment 与 lianqi 的武器数据迁移时裁定 | [技能](../../systems/skill/architecture.md) |
| Q39 | 技能选择的提示文字对调：施法中禁施（`skilleffect_09`）且未沉默时显示“沉默中”，沉默（`effect_002`）时显示“冷却中”；按键 Z（第 1 键）的选择不检查禁施与沉默，X、C、V 检查。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q40 | 拈花飞叶（`SkillTree01`）命中时读取弹体的 `AttackDamage` 与 `Damage_multiplier`；来源只在弹体到期时从所选技能的 `ManaAttackDamage`、`jinengshanghai` 写入这两个键，常规技能书通常不产生前一个键，因此通常为 0，但不能删掉来源写入与命中请求。`Ice02`（破碎冰矛）、`Light02`（定魄神光）复用 `Ice01`、`Light01` 的刻过程；飞羽冰花沿侧向圆弧飞行、计时每刻加两次，正前方的目标打不到。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q41 | 控火术（`SkillFire04`）用施放者自己 `getPersistentData` 里的 `AttackUUID` 拼 `/shanghai` 命令，玩家没有这个键，命令行里这一项为空串，空白合并后参数整体前移，实际按“无攻击者、基础量 = 伤害倍率、倍率 0”结算，伤害极小。评估为轻微，保持来源行为（直接传入前移后的攻击量和倍率，不把玩家补为攻击者） | [技能](../../systems/skill/architecture.md) |
| Q42 | 巨石锤（`SkillRock05`）类型一与类型二落地攻击里 20% 的眩晕（`effect_001`）加给了实体自己（`entity`）而不是受击者（`entityiterator`）；化石手（`SkillRock04`）按实体自己的朝向取 3.5 格或 1 格处作为攻击中心，而生成时朝向是随机的，攻击方向随机。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q43 | 雷系两处疑似疏漏（2026-11-05 更正：此前认为地滚雷比较的队伍名对玩家恒为空串、施放者本人走近也会触发，这是错的——登录过程 `PlayerJoinGame` 与每秒一次的 `Playerhuifu` 都把玩家变量 `team` 写入玩家的 `getPersistentData`，所以玩家的值就是其团队名，施放者本人与同队实体不会触发）：地滚雷（`SkillThunder08`）生成时没有转向，前进方向是随机朝向；`Skillwind02` 的聚风术（非聚风化气）不分敌友拉扯 4 格内全部实体。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q44 | 木系四处疑似疏漏：木篱术（`SkillTree05shifang`）放出九道木篱时只 `addFreshEntity`、没有走 `finalizeSpawn`，木篱的状态停在空串，实体刻过程不会让它展开、收起或消失，施放出的木篱永久留在世界里直到被打死（只有 `/summon` 之类走初始生成的才会进入阶段）；回身鞭（`SkillTree06`）只把带霸体（`skilleffect_08`）的目标抛起，与其他技能“无霸体才位移”相反；乱枝（`SkillTree04shifang`）潜行只给友方枝条写一个无处读取的 `baolie` 标记，没有效果；灰木树梯在实体刻里计数加了两次，存在时间是 26 刻而不是 52 刻。评估为轻微（木篱每次放 9 个永久实体，可被击杀或命令清除），保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q45 | 灵纹附魔（`EntityHurt2` 中读主手 `lingwen` 的一段）两处疑似疏漏：数量与灵力消耗用的是起尸诀附魔 `qishi` 的等级（`fumolevel`，没有起尸诀时为 0），灵纹附魔自己的等级只在灵纹弹的攻击量（等级 × 4）里用；扣的灵力是受击者 `entity` 的，不是攻击者 `sourceentity` 的（非玩家受击者视为 0 灵力，所以只有 `qishi` 等级为 0 时才会生成，同时带起尸诀附魔的武器打到怪物时灵纹不会触发）；另外生成的灵纹特效把计数 `lingwennum`、`friendnumber`、`belongToMasterUUID` 写进攻击者好友列表物品的 NBT（`getCompound` 取到的是同一个标签对象，又原样 `put` 进实体），好友列表里会残留这几项，下一只从残留值接着数。保持来源行为；目标已接回好友子表中的残留计数和运行期共享，实际战斗、存档重进与多人仍待验证 | [技能](../../systems/skill/architecture.md) |
| Q46 | 火鸟术（`SkillFire06`）两处来源行为：2026-11-07完整读取`EntityAnimationFactory.onEntityTick`后撤销此前“飞行动画只播一次”的核对结论；工厂逐刻消费同步名字并复位undefined，后续同名请求能够再次到达。其余两项保持：有目标时的前冲速度竖直分量取 −0.2·sin(俯仰)，没有乘 cos(俯仰)，水平分量也没有乘 cos(俯仰)，俯视目标时前冲方向偏离视线；`OwnerHurtByTargetGoal`（目标类）加在行为选择器而不是目标选择器。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q47 | 起尸诀（`Skillyin01`）四处疑似疏漏：起尸诀附魔 `qishi` 的命中段（`EntityHurt2`）里的 `for (…4…)` 只是在找位置，找完后只放出<b>一只</b>尸体，而技能“起尸诀”放出四只；死亡拥抱的判定区在前冲结束处的前方 3 格（水平）、半边长 0.5，前冲 14 刻共约 5.6 格，目标只有在约 8.6 格外才落在判定内，目标更近时尸体先被目标挡住、判定区落在目标身后而打不到；近身且没有沉默时“普攻”被登记了两次（权重 20 加 20），抽到的概率是其余技能的两倍多；杀死实体让书升级时，修为加的是被杀实体的 `LEVELnumber`（而不是尸体的，多数原版生物没有这个键，所以只有带该键的妖兽等才让书增长），升级只改书的 `LEVELnumber` 与品阶文字 `belongTopinjietext`，品阶数值 `belongTopinjie` 不变，书上显示的品阶可能与品阶数值不一致。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q48 | 灵纹弹实体的 `tick` 先调用箭基类的命中处理，再推进灵纹弹过程；第一刻撞到目标时攻击量尚未写入，自定义灵力伤害不会发出第二次命中事件，灵纹数量为 1；第二刻以后撞到时攻击量已写入，自定义伤害再发出一次命中事件，数量为 2。1.18.2 版本的 `LingwentexiaoEntity.tick` 与目标实现保持同一顺序，故两种结果均为来源行为。评估为轻微，检查接受 1 或 2，不改变玩法顺序 | [技能](../../systems/skill/architecture.md) |
| Q49 | 虚空裂隙的碰撞过程只用实体上的 `shifouyichuanyve` 标记是否已使用，第一次有效碰撞后整道裂隙无法再被任何玩家使用；未找到目标世界或进入维度未匹配也会消费裂隙；从虚空返回时，`weidu` 在目标查找之前清零，目标缺失后不能靠同一道裂隙重试。坐标取裂隙位置，进入三轴均除以 50 后四舍五入（含高度），返回三轴均乘以 50。依据 `Jinengxvkong` 与 `XvkongliexiDangWanJiaYuGaiShiTiFaShengPengZhuangShi`；评估为轻微，保持来源行为，维度未迁移期间明确保留依赖限制 | [技能](../../systems/skill/architecture.md) |
| Q50 | 技能箭共用命中过程的箭雨分支，在命中实体时把受击实体设为二十支新箭的射手，命中方块时才使用原射手；新箭不继承技能名。暗箭把 1.2 或 0.8 写进目标的 `Damage_multiplier`，实际暗伤仍使用局部的 1.5 或 1。暗箭落地云指定 `Potion:minecraft:bad_luck`，该药水 ID 未登记，且命令中的命名空间值未加引号，在 SNBT 的冒号处解析失败，实际不生成云；1.21.1 版本保留不生成云的结果，不再执行无效召唤，不补造幸运或霉运效果。十个箭物品的释放条件恒为假，仅保留蓄力动画；孤立施放过程不接入新入口。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q51 | 身法双击窗口触发后不清空，十刻内后续按下仍可触发，冷却只检查追加前不超过上限；W 的移速效果不受位移冷却限制。轻功的 superjet 先清零再读扣费，因此喷射实际不额外扣灵力；flying 附魔减耗系数仅在大于零时使用，等级总和达到 20 后恢复全额耗费。起飞的拔空术速度 2 会立即被外层开关速度 0.5 覆盖；圆环和两行云雾把角度数值直接作弧度。依据 ShenfaW/S/A/D/space、ShenfaTick、FlyskillTick、Flyskillkaishitexiao 与 FanjianAnXiaAnJianShi，评估轻微，保持来源顺序和结果 | [技能](../../systems/skill/architecture.md) |
| Q52 | 技能界面三处来源疑点：神识扫描在服务端定时回调中调用 `Level.addParticle`，该空方法不发送粒子，实际只有扣神识和关闭菜单；自爆先安排延迟爆炸与火伤，随后当下扣寿元并杀死玩家，死亡后回调仍执行；四技能背景的数值 6 没有写入者。依据 `Jinengsaomiao`、`Jinengzibao`、`JinengGuiDangZheGeJieMianDaKaiShi` 及显示过程；扫描的空方法另以缓存 1.18.2 / Forge 40.3.0 源码与目标 21.1.265 源码核对，非锁定 40.1.0 实机证据。评估轻微，保持来源顺序，不补造扫描或背景效果 | [技能](../../systems/skill/architecture.md) |

| Q53 | `Skillarrow08fudu` 的合成事件没有附毒逻辑：仅在产物为第 08 箭时，将第二合成格的物品文本截去前两字符，再与 `cailiao_141` 比较，成立时显示 `111`。1.21.1 的 `Item.toString` 多了命名空间，目标显式构造来源的数量加路径文本，保留两位数量时无法匹配的结果；1.18.2 文本格式以缓存 Forge 40.3.0 静态对照，不称锁定 40.1.0 实机证据。`SkillArrow00–10skill` 没有有效事件、Java 或资源调用入口；`ArrowSkillchufa` 只被 `AnjianZ` 未连接触发器的游离 Blockly 块引用，生成 Java 不调用，故其施加的“心如止水”也没有自然入口或行为消费者。评估轻微，保留效果身份与图标，不增设附毒、箭技或效果入口 | [技能](../../systems/skill/architecture.md) |

| Q54 | 金轮死亡粒子过程 `SkillGold0201DangSiWangShiJianDaoShiProcedure` 未绑定实际入口：生成实体 `SkillGold0201Entity.tickDeath`（207–212 行）只在二十刻移除并掉经验，`SkillGold0201.mod.json` 没有对应触发；全来源 Java、JSON、XML 与工作区文件仅有过程自身和元素清单登记。过程名不能证明触发有效。评估轻微，保留来源实际行为，不给目标新增死亡粒子钩子；各模式刻过程的有效到期粒子已接回 | [技能](../../systems/skill/architecture.md) |

| Q55 | `/lightninglanding` 以空白拆分完整命令，队伍词虽未被结算过程消费，却占据第四个参数。调用方的团队为空时，范围直径和颜色前移，范围会把颜色文字解析为 0，颜色回到白色；这不是“玩家团队恒为空”，登录与 `Playerhuifu` 会写入玩家实际团队。依据 `B:LightningLandingCommand`、`LightningLandingGo`、`A:SkillThunder09shifang` 与 `Playerhuifu:26`。评估轻微，保持来源参数次序和空词消失行为，不补占位词或默认范围 | [战斗](../../systems/combat/architecture.md) |

| Q56 | 雷劫的负功德不增加雷数、正功德抵消的循环上界随扣减变化；玩家死亡分支误写云的玩家能力，跳过只标成功而未清云序列，离维度移除云未补玩家状态。依据 `ThunderBegin`、劫云刻过程与实际绑定的死亡过程，评估轻微，保留来源行为，细节见修炼正文 | [修炼](../../systems/xiulian/architecture.md) |

| Q57 | 18 把弓都是 `SwordItem`，选中时共用 `Weaponspeedbow01DangGongJuChuXianZaiShouZhongShi`；其蓄力读取 `synchronizedata.rightclicking`，右键过程为空。两个来源 `src` 与 `elements` 全检未找到有效写入者，B 的 `Zxc` 只有未绑定的编辑器实验块且含 `rifhtclicking` 拼写。按来源迁移条件流程，保留自然右键断链，不新增鼠标同步或改成原版弓；人工注入标记的验证不能宣称自然入口可用。影响仅弓入口，其他移植继续；修复取舍待裁定 | [装备](../../systems/equipment/architecture.md) |

| Q58 | 妖化提示与实际流程有差异：`Jinengyaohua` 只查五个体质标记，没有额外境界门槛；`Yaohua` 用包含护甲和副手的完整41格物品能力统计空位，旧甲移入背包、放不下沿原生给予逻辑掉落；`Yaohuajieshu` 不自动穿回旧甲，延迟回调直接清空当时的槽内物品。影响仅变身入口，评估轻微，保留来源流程、顺序及后果，不增设恢复或库存规则 | [装备](../../systems/equipment/architecture.md) |

| Q59 | 宝箱成品过程 `BaoxiangChengpin` 写 `lianqizong.mana_Att`，而 `PlayerRightClick` 的耗费与 `DamageIntegration` 读根 `mana_Att`；根值由灵兽袋、蛊罐及部分法宝实例产生，不能把炼器法攻作为根法攻使用。成品附魔数大于3仍只处理第二、第一组，未知防具类型仅在需原生修饰槽名时使物品命令失效。评估轻微，保持路径及分支；本批只提交成品品阶给格挡，根生产者随其所有者迁移 | [装备](../../systems/equipment/architecture.md) |

| Q60 | `PlayerGUI03` 槽2要求手饰标签却保存为腰佩，槽3要求腰佩标签却保存为手饰；说明文字仍称第三格腰佩。首刻把保存物品原对象数量设1并交给菜单，共用引用，关闭才回写被替换的槽对象；若头顶坐标有物品能力则绑定并覆盖前四槽。评估轻微，保留标签、字段、数量与绑定生命周期，不自行交换或限制为单件。`/shipin` 已提供 Q59 中根 `mana_Att` 的另一真实生产者，与炼器法攻分开消费 | [装备](../../systems/equipment/architecture.md) |

| Q61 | 火箭施加阳火没有写持续来源UUID，普通炙烤命令可能因空参数失效或沿用先前接触来源；阴阳爆燃只遍历Mob且不按受燃者友方预筛，实际命令仍执行combat敌友判定。丹火给已有火补燃料不验归属，普通火无方块实体也消耗物品；放置共用玩家友方表引用，重载后分开，并读取登录时复制的实体团队。影响限上述路径，评估轻微，保留来源载体、调用顺序与结果；不补箭射手、不加归属校验、不擅改为冻结快照 | [异火](../../systems/yihuo/architecture.md) |
| Q62 | `ShanghaifanweiEntity.movementPredicate` 默认请求 `animation.ycxzboss_01.idle`，但其绑定的 `shanghaifanwei.animation.json` 不含该动画；动态范围过程提供的有效动画另行播放。静态核对35个来源默认/离地/濒死名称，仅该默认名缺失。影响限范围提示默认待机，评估轻微，保留实际引用，不替换为猜测动画；1.18.2实机及目标可见回退未验收 | [技能](../../systems/skill/architecture.md#过程动画调用链与控制器复核2026-11-07) |
| Q63 | `SkillFire01` 的火球命中过程遍历弹体周围 2 格的非友方对象；循环变量只控制粒子次数，伤害请求仍把直接命中目标作为目标，因此邻近对象不会因该循环额外受伤。评估轻微，按来源保留；目标已补入相同的循环与目标引用，内置客户端回归覆盖直接目标与邻近傀儡 | [技能](../../systems/skill/architecture.md#弹体实际过程与发射入口复核2026-11-08) |
| Q64 | `Fulujinglineed` 在体质 `FuluConsumeJingliPercentage` 非 0 时，实际乘的是 `(FuluConsumeJingli + 1)` 而不是该百分比字段自身；`FuluConsumeJingli` 为 0 时百分比修正等于乘 1，非 0 时二者相互牵连。疑似字段误用，目标保持来源（`FuluUse.pay`），不自行改平衡，待负责人裁定。两个来源 Mod 中均未找到这两个键的写入者，当前实际不触发。 |
| Q65 | `Itemfulu21...` 登记 21 号符生效数时写 `fulunumber = round(物品的 fulunumber + 1)`，读的是物品组件而不是玩家体质（39 号符同类位置读的是体质）。物品上没有该键，所以 21 号符生效时把玩家的生效数直接置为 1，覆盖其他仍在生效的符。疑似字段误用，目标保持来源，待负责人裁定。 |
| Q66 | `Itemfulu04...` 的 46 号符分支先射出火球，再把品阶写入玩家的实体持久数据 `fululevel46`；火球 tick 过程对玩家发射者读的是体质里的 `fululevel46`（仅 13 号符写入）。所以玩家直接使用 46 号符时火球按品阶 1 结算（体质值为 0 时回退 1），符箓品阶对伤害无影响。疑似存储位置误用，目标保持来源，待负责人裁定。 |
| Q67 | `Fulutexiao32TouZhiWuFeiXingTick` 用使用者的 `kaiqi` 判断是否已初始化，却把 `kaiqi` 写在子母弹自己身上，使用者的 `kaiqi` 永不为真，所以每刻都重复读取品阶与分裂标记；使用者只要持有 `fulu32shoufa`，下一刻存在的任意子母弹都会抢走它成为主弹。疑似写错对象，目标保持来源，待负责人裁定。 |
| Q68 | 04 装配页只在菜单 `removed()` 时写回八槽；1.18.2 Forge40.1.0 与 NeoForge21.1.265 的断线移除顺序均先保存玩家、再移除实体并关闭菜单，退出时刚修改的装配可能未入档。2026-11-17目标客户端真实同档断线重进确认未关闭菜单的编辑丢失，K重开双端八空槽及中文画面通过；保持来源还是改为保存前写回仍待负责人裁定 | [技能](../../systems/skill/architecture.md#q68-装配页断线保存时序) |

### 2026-11-14 续批差异与不确定项

以下独立包发现已经登记；标为“实施中”的项还未集成或验证，不能据此提高玩法完成状态。来源位置采用下方A/B约定。详细规则和证据由所属正文收口，后续只更新本行状态及链接。

| 编号 | 类型与当前处理 | 发现与影响摘要 | 所属正文或决定 |
| --- | --- | --- | --- |
| Q69 | 来源缺定义；已批准实现，拒启动必要单项通过 | 拍卖商品语言定义缺失，且仅34级NPC不满足筑基/结丹候选区间，原无限扫描不能结束。缺商品或合格NPC时拒绝启动并中文提示，真实入座后进度0、中文原因及同次失败只提示一次已验证，保留有效场次规则；未运行来源挂起分支或有效拍卖 | [经济](../../systems/economy/architecture.md)、[批准记录](../../systems/project/logs/2026-11.md#2026-11-14-拍卖缺定义时拒绝启动) |
| Q70 | 来源库存替换与扣料异常；已批准实现，有限守恒/重进通过 | B:`BuildingProcedure:31–220`先放结构再读18槽扣料；B的12个`build01/02/03`四向来源模板均清空核心库存，01变为一槽案台，02/03仍为18槽，可能丢材料并越界。已实现保存原库存、按原费用扣料，18槽回写余料、一槽原位掉落；中心与受控02模板跨栈余料/无关物品守恒及十八槽保存重进已过；03/全向/刚好足量仍待 | [城镇](../../systems/town/architecture.md#q70-模板替换与材料扣费)、[批准记录](../../systems/project/logs/2026-11.md#2026-11-14-城镇扣料与差异集中登记) |
| Q71 | 已核来源异常；保持来源，待集中裁定 | B:`ShopblockqvchuProcedure:37–90`领取入账后不清`cunchulingshi`，主人可重复领取历史灵石；尚未实机对照 | [经济](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q72 | 已核来源载体疑点；保持来源，待集中裁定 | B:`ShopblockGuigoumaiProcedure:798–801`写`getCompound("playerxiangou")`；生存放置未初始化该compound，限购写入可能只落在临时空tag，创意刷新会初始化。具体两版本运行仍待核 | [经济](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q73 | 已核字段误读；保持来源，待集中裁定 | B:`Tuzhizhongliang`对材料列表误读attribute索引1，读数为0，自然图纸估价的折价因子恒1；不按名称修正材料重量 | [经济](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q74 | 来源空表异常；保留中断结果，错误提示待对照 | B:`Baoxiangzong:127–138`读取奖励首段第三字段，空串/畸形首段会终止同次发放。目标明确终止并保留此前掉落，不补抽；来源框架错误文案及空分支实机未对照 | [宝箱](../../systems/treasure/architecture.md#来源异常的当前处理) |
| Q75 | 来源奖励类型疑点；保持来源，待集中裁定 | B:`BaoxiangLiandantuzhi`只有一级四支为丹方，二级及以上实际返回技能书；不补造高档丹方 | [宝箱](../../systems/treasure/architecture.md#来源异常的当前处理) |
| Q76 | 来源重复字段；保持覆盖结果，待集中裁定 | 丹药表重复写`danyaotick0`，止血丹后写0且缺`danyaotick1`；保留后写覆盖与缺字段读0，不按药名改药效 | [宝箱](../../systems/treasure/architecture.md#来源异常的当前处理) |
| Q77 | 来源菜单与库存不一致；保持未用入口，待集中复核 | 03/11宝箱仅一槽、副本入口/画卷为2/1槽，`createMenu`均返回不绑定原库存的独立三行箱菜单；自然交互另走开箱/入口页/画卷操作。目标保留未用入口 | [宝箱](../../systems/treasure/architecture.md#01至11宝箱载体)、[副本Q77](../../systems/dungeon/architecture.md#q77-未使用的三行菜单) |
| Q78 | 来源能力对象疑点；保持来源，待集中裁定 | B:`PlayerRightClickEntity:113`读取大管家NPC的玩家`birth`能力，A的能力只附加Player，自然入口走普通04脚本；不改读交互玩家的出身 | [NPC](../../systems/npc/architecture.md#q78-大管家出生判定读取错误实例) |
| Q79 | 来源空入口；保留身份，待集中复核 | 城镇Ui02无按钮、Ui03/04/05仅注册，Ui01占领与Ui05官职按钮为空；不补造管理玩法。未连接分支继续作为开放内容保留 | [城镇](../../systems/town/architecture.md#q79-城镇空监听与页面调用断链) |
| Q80 | 来源刷新入口；保持自然链，待集中复核 | 悬赏页面只有六个领取整行按钮，没有六个刷新按钮；`Xvanshangshuaxin`无调用者，单行刷新由目标死亡触发。不能凭过程名增设刷新入口 | [任务](../../systems/quest/architecture.md#q80-悬赏刷新无玩家入口) |
| Q81 | 来源配置畸形；保持异常串，待核实运行影响 | 镇妖塔固定点共44个，其中`(1278,83,-587)`的`fubenshujv`只有奖励列表、无波次头；源解析读取`fields[1]`会越界。保留原配置，不补造波次头，未运行此分支 | [副本](../../systems/dungeon/architecture.md) |
| Q82 | 来源映射与动画疑点；保持来源，待集中复核 | 私人画卷地灵界与无归界的回程映射互换，首次动画实际选择1；按实际分支迁移，不依据名称交换，未作两版本实机对照 | [副本](../../systems/dungeon/architecture.md) |
| Q83 | 来源载体误读；保持来源，待集中裁定 | B:`Duihuashijian:185–190`字符串“加”从会话读取旧串，却写剧情持久域；连续累加不接续剧情域旧值 | [任务Q83](../../systems/quest/architecture.md#q83-字符串累加读取会话载体) |
| Q84 | 来源维度字符串异常；保持来源，待集中裁定 | B:`Duihuashijiantwo:452–470`拼接当前维度时多出括号，正常文本比较失败；脚本消费者与实际运行仍待核 | [任务Q84](../../systems/quest/architecture.md#q84-当前维度字符串拼接) |
| Q85 | 来源扣料与参数索引异常；保持来源，待集中裁定 | B:`Duihuashijiantwo:153–241`带NBT扣物品先将足量需求清0再删除，参数索引跨栈持续递增；完整多栈影响未实机复核 | [任务Q85](../../systems/quest/architecture.md#q85-带nbt扣料与跨栈索引) |
| Q86 | 来源目录与生成数量不符；保持来源，待集中裁定 | B:`Worldbegin:51`为045悬赏定义数量8，追踪`Xvanshangzhuizongheji:40–57`仅生成一次；不补造七只 | [任务Q86](../../systems/quest/architecture.md#q86-045目录数量与生成数) |
| Q87 | 来源失联移除风险；保持来源，待集中裁定 | A:`Guchong05DangShiTiGengXinKeShi:32–57`百刻返还只找当前维度在线主人，未找到仍移除，可能不返还物品 | [蛊虫Q87](../../systems/guchong/architecture.md#q87-同维主人缺席仍移除) |
| Q88 | 来源文本与公式不符；保持来源，待集中裁定 | 普通商人资质“强筋”不匹配A:`Npcshuxingjisuan:140–150`识别的“强劲”，实际走最低档公式 | [NPC Q88](../../systems/npc/architecture.md#q88-强筋资质文本) |
| Q89 | 按既有文案规则实施的显示差异；待验证 | B:`Xvanshangshuoming:17–20`第三条源码调试说明移入开发记录，前两条玩法介绍仍显示；依据项目玩家文案规则，不改变悬赏结算 | [任务Q89](../../systems/quest/architecture.md#q89-悬赏说明调试文案) |
| Q90 | 来源失效坐标异常；保持中断，待集中裁定 | B:`EntityDie:56,79`直接解引用悬赏告示牌BE，牌子移除后结算可能中断；已撤回目标额外静默跳过，现场未实机复现 | [任务Q90](../../systems/quest/architecture.md#q90-告示牌消失后的结算中断) |
| Q91 | 分类疑问已核排除；05标签已接，待验证 | A的01/03来源为`MobType.UNDEFINED`，不能凭蛊虫名称加节肢标签；05明确为`ARTHROPOD`，只为05补目标标签 | [蛊虫Q91](../../systems/guchong/architecture.md#q91-节肢类别的核对边界) |
| Q92 | 目标显示遗漏已补接；有限自然操作与截图通过 | 普通对话恢复五张来源双态按钮贴图与`fengexian`分隔线，沿用动作号、坐标和中文朗读；转换/编译及银行自然继续/四选项按钮通过，截图已查看；完整焦点/页面视觉仍待，裁切见Q102 | [任务Q92](../../systems/quest/architecture.md#q92-对话按钮与分隔线) |
| Q93 | 来源实物购买扣费异常；保持来源，待集中裁定 | `ShopblockGuigoumai`只在背包包含某类需求时检查数量，整类缺失可跳过；不足栈清0后再减其数量，剩余需求不减少 | [经济R13-E04/E05](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q94 | 来源概率分支不可达；保持来源，待集中裁定 | `Cailiaoshop01/02`先判断随机数≥20再判断≥50，后档不可达；未按推测调概率 | [经济R13-E07](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q95 | 来源估价重复低档；保持来源，待集中裁定 | `Shougouselljisuan`饰品21–30级价格重复11–20级段；逐级迁移，不替换成递增公式 | [经济R13-E08](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q96 | 来源托管字段错位；保持来源，待集中裁定 | `PaimaiTuoguananniu`写`paimaimoshi`的全自动/自营，显示却读`tuoguan`；不补造开关 | [经济R13-E11](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q97 | 来源竞价与成交断链；保留空操作，待集中复核 | `PaimaihangScreen`11按钮均为空回调，未找到玩家出价与成交结算推进入口；不将已接现场状态机称为完整成交 | [经济R13-E12](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q98 | 来源座椅令牌与台状态不一致；保持来源，待集中裁定 | `PaimaichairZaiFangKuaiShangYouJian`不验令牌身份即可移椅，且不更新台内坐标/计数 | [经济R13-E13](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q99 | 菜单绑定和延后覆槽边界；待核实/待实现 | 额外手持/实体能力绑定未见实际调用，其他系统Supplier槽映射尚未全部接入；银行九槽绑定十一槽页存在容量风险。已核Forge40.1.0父类提供无方向能力并补入目标，先前“无能力”判断撤销 | [经济R13-E14/E15/E19](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q100 | 来源商品根累计与命名覆盖；保持来源，待集中裁定 | `Goodsdaoru`复合根跨商品累计，循环临时自定义名又被后写覆盖；缺定义分支未合成假商品进行验证 | [经济R13-E17](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q101 | 来源文本分行超过显示范围；保持来源，待集中裁定 | `Duihuafenhang`仅外循环检查20行，内循环仍可写21行及以后，页面只显示20行 | [经济R13-E18](../../systems/economy/architecture.md#来源差异与当前处理) |
| Q102 | 目标小窗口裁切已观察；来源同条件待对照 | 854×480、GUI缩放2下已观察商店、对话、副本页及炼丹固定画布裁切；副本及炼丹另观察通知遮挡。暂保持来源布局，完整窗口/缩放矩阵和来源实机未执行 | [经济Q102](../../systems/economy/architecture.md#q102-小窗口下的固定画布裁切)、[副本证据](../../systems/dungeon/logs/2026-11.md)、[炼丹证据](../../systems/liandan/logs/2026-11.md#2026-11-19-人间道虫蚀草炼丹与炉次重进) |
| Q103 | 来源准入与扣票顺序疑点；保持来源，待集中裁定 | A:`FubenjinruProcedure`临时队伍为空时不核发起者境界且人数为0；境界/人数拒绝后仍扣票，不支持维度在准入后已写冷却。临时队伍自然生产者仍待 | [副本Q103](../../systems/dungeon/architecture.md#q103-准入与扣票顺序) |
| Q104 | 来源妖魂回程分支疑点；保持来源，待集中裁定 | 同过程妖魂分支重复判断主世界，保留旧`chuansongid`，首次可能为空；不按当前维度补正 | [副本Q104](../../systems/dungeon/architecture.md#q104-妖魂回程分支) |
| Q105 | 来源跟随者状态与模式疑点；保持来源，待集中裁定 | 只给队长写定义/实例/回程，跟随者可能保留旧附件；`非创造 || 非旁观`恒真，全部转冒险。未执行多人对照 | [副本Q105](../../systems/dungeon/architecture.md#q105-跟随者与模式) |
| Q106 | 来源占位清理与攻击位断链；保持来源，待集中复核 | B的清理遇X偏移0停止、扫描不清累计计数；未勾攻击不清旧真，解析复制攻击位而释放不消费 | [副本Q106](../../systems/dungeon/architecture.md#q106-占位符扫描与攻击位) |
| Q107 | 来源区域统计与标题疑点；保持来源，待集中裁定 | B:`FubenblockGengXinYouXiKeProcedure`统计区域所有玩家/副本怪物，不核实例控制点；名字直接拼JSON标题，未补过滤或转义 | [副本Q107](../../systems/dungeon/architecture.md#q107-区域统计与标题) |
| Q108 | 来源同维度延后回调疑点；保持来源，待集中裁定 | 两刻延后失败回程若已在目标维度直接退出、未注销监听；目标任务继续尝试，服务器结束清空运行期任务，未验该分支 | [副本Q108](../../systems/dungeon/architecture.md#q108-同维度失败回调不注销) |
| Q109 | 来源奖励字段错位；保持原串，待集中裁定 | 两份镇妖塔火焰护盾含`30//jinengneeditem…`，三元组错位，后续数值/冷却未写；火鸟术空吟唱按来源转0，不补字段 | [副本Q109](../../systems/dungeon/architecture.md#q109-火焰护盾奖励错位) |
| Q110 | 来源实体掉落表上下文疑点；保持来源，待集中复核 | `organism_11`三处`apply_bonus(fortune, ore_drops)`要求`minecraft:tool`，目标加载时告警；来源函数相同，实际掉落影响及两版本对照未验 | [生物Q110](../../systems/creature/architecture.md#q110-organism_11掉落上下文) |
| Q111 | 二次入场检查时序疑点；后续全链通过，前轮原因未定 | 补维度首轮第二次入场复合条件超时、缺分阶段状态；夹具菜单稳定等待后，日志确认扣票/候场/控制器准备及全部后续通过。未改副本生产流程，不定性来源或目标Bug，不以复验通过抹去前轮未知原因 | [副本Q111](../../systems/dungeon/architecture.md#q111-二次入场的检查时序疑点) |
| Q112 | 已确认目标显示差异；待统一对齐 | 占位符标签由来源原ID/黑色/(6,16)变为“占位符ID：”加ID/灰色/(6,11)；控制器标签颜色也不同。当前未修正，不宣称完整视觉保真 | [副本Q112](../../systems/dungeon/architecture.md#q112-控制器与占位符标签的目标显示差异) |
| Q113 | 来源结构解封跨维度清理；保持来源，待集中裁定 | 每次调用（包括未知名称）十刻后执行`kill @e[type=item]`；无空间限制，清理所有维度中已加载的物品实体 | [世界Q113](../../systems/world/architecture.md#q113-结构解封的全维度掉落物清理) |
| Q114 | 来源尸体封存的状态重置；保持来源，待集中裁定 | 团队读玩家专属能力得到空串；不保存驯服/装备/采丹标志，攻击和速度只恢复自由字段；自然再次采集影响未验 | [生物Q114](../../systems/creature/architecture.md#q114-尸体封存的选定字段与状态重置) |
| Q115 | 来源跨结构条件写入；保持来源，待集中复核 | 佣兵营地100刻后向云清派的任务伪装方块写商品表，依赖另一结构是否存在；实际意图与不同顺序结果未验 | [世界Q115](../../systems/world/architecture.md#q115-佣兵营地的跨结构条件补充) |
| Q116 | 来源零哨兵异常；保持来源，待集中裁定 | 零距离可跳选，全解封后仍尝试原点空名称裂隙 | [世界Q116](../../systems/world/architecture.md#q116-零距离哨兵与全部解封后的空裂隙) |
| Q117 | 来源UUID半成品风险；保持来源，待集中裁定 | 先放块后解析当前维度UUID，查无实体可能空引用，异常现场未验 | [世界Q117](../../systems/world/architecture.md#q117-搜索uuid解析失败后的裂隙半成品) |
| Q118 | 来源先登记后门控；保持来源，待集中裁定 | 延迟期间离线/换维度/改名可导致未放结构而编号已登记 | [世界Q118](../../systems/world/architecture.md#q118-立即登记与延迟命令门控不回滚) |
| Q119 | 来源获取断链；待核实完整入口 | 赠盘仅旧编辑器文本，实际Java只写准备状态，未补造获取 | [世界Q119](../../systems/world/architecture.md#q119-寻灵盘赠送只有旧编辑器文本) |
| Q120 | 目标遗漏已补，九条原生玩家必要单项通过 | 接回全局右键消费shuaxin材料刷新与陈氏条件商品替换，原串保留，完整商店自然交互未验 | [任务Q120](../../systems/quest/architecture.md#q120-任务伪装方块动态右键补丁缺失) |
| Q121 | 来源奖励说明与实际动作不一致；保持待裁定 | 小孩哥说明兔肉煲四份，动作熟兔肉一份；完整自然交付未验 | [修士Q121](../../systems/npc/architecture.md#q121-小孩哥奖励说明与动作不一致) |
| Q122 | 来源八选项只有四个自然按钮；保持待裁定 | 士兵八酒脚本原样保留，界面只显示来源前四项 | [修士Q122](../../systems/npc/architecture.md#q122-士兵八选项只显示四项) |
| Q123 | 来源临时队伍生产者断链；保留，待集中复核 | 能力teamwork没有非空建立/加入写入，好友申请不是临时队伍 | [关系Q123](../../systems/relation/architecture.md#q123-临时队伍没有建立或加入生产者) |
| Q124 | 来源临时队伍布局超宽；保持，待集中裁定 | 500宽画布文字x928、退出x915，不能以Button发包单项宣称默认窗口鼠标可达 | [关系Q124](../../systems/relation/architecture.md#q124-临时队伍显示与退出按钮超出画布) |
| Q125 | 来源向下放笼跨两格；保持待裁定 | DOWN用点击位置下两格，其余面相邻，六面自然操作未全验 | [装饰Q125](../../systems/decor/architecture.md#q125-向下放笼跨两格) |
| Q126 | 来源多动物只返回最后一笼；保持待裁定 | 每只删除、数量写1、不累加，单动物样本不覆盖 | [装饰Q126](../../systems/decor/architecture.md#q126-多只动物收取只返回最后一只) |
| Q127 | 来源兔种重载查询未限兔；保持待核 | 新兔附近全部实体被save/load并写RabbitType | [装饰Q127](../../systems/decor/architecture.md#q127-兔种重载覆盖周围全部实体) |
| Q128 | 来源放出重建个体；保持待裁定 | 只保留兔种/数量，不保存年龄血量名字UUID | [装饰Q128](../../systems/decor/architecture.md#q128-放出动物只保留种类与数量) |
| Q129 | 来源无饵也可捕获；保持待裁定 | 诱饵只影响引导，碰撞只查保护和动物类型 | [装饰Q129](../../systems/decor/architecture.md#q129-没有诱饵也可碰撞关笼) |
| Q130 | 目标结构属性转换遗漏已修；附加步高待核 | Mod实体旧Attributes未被原生结构DataFixer升级，小孩哥100血2攻回退20血1攻；22模板已再生，六居民模板及重进通过，其余模板未实机，150条附加步高保持待核 | [世界Q130](../../systems/world/architecture.md#q130-结构mod实体旧属性没有升级) |
| Q131 | 目标无方向库存能力遗漏已修；必要单项通过 | 来源父类InvWrapper与六面共库，目标03/11原null无能力，旧开箱单项不覆盖 | [宝箱Q131](../../systems/treasure/architecture.md#q131-0311无方向库存能力遗漏) |
| Q132 | 来源元素硬度与Java不同；按实际Java保持 | 宝匣生成Java负硬度/高抗性，元素50/500，不用编辑器覆盖 | [宝箱Q132](../../systems/treasure/architecture.md#q132-宝匣元素硬度与生成java冲突) |
| Q133 | 来源护甲预览错字段；保持待集中裁定 | 生命按字段7判断却显示8，与灵力同值，未补猜值兜底 | [宝箱Q133](../../systems/treasure/architecture.md#q133-护甲预览生命显示错字段) |
| Q134 | 来源延迟盲移除及队列不持久；保持待裁定 | 五刻后替换方块也删除；无效指令也开启，真实开箱中停服未验 | [宝箱Q134](../../systems/treasure/architecture.md#q134-宝匣延迟盲移除与保存边界) |
| Q135 | 来源独立宝箱菜单没有自然入口；待核 | 宝匣直接执行指令，不把10槽孤立菜单补成自然入口 | [宝箱Q135](../../systems/treasure/architecture.md#q135-独立宝箱菜单没有自然入口) |
| Q136 | 来源飞梭没有已发现自然生产者；保留待核 | 无蛋/自然出生；有界文本及409结构NBT未命中，保留显式生成后行为 | [修士Q136](../../systems/npc/architecture.md#q136-飞梭没有已发现的自然生产者) |
| Q137 | 来源普通移动覆盖/负硬度/BE不随行；保持待裁定 | 下移覆盖下方、只搬BlockState，容器原位掉落，来源实机未验 | [装饰Q137](../../systems/decor/architecture.md#q137-普通方块覆盖与方块实体不随行) |
| Q138 | 来源潜行持锤与核心Block.use冲突；待核 | 原生secondaryUse跳过方块入口，可能改走锤移动；未加绕过 | [城镇Q138](../../systems/town/architecture.md#q138-潜行持锤拆除核心的入口冲突) |
| Q139 | 来源左键各action重复入口；保持待核 | 不取消/不筛action；目标服务端写入，持续按键与多人未验 | [装饰Q139](../../systems/decor/architecture.md#q139-左键各动作重复入口) |
| Q140 | 工具客户端默认/服务端空类别生产断链；保持待裁定 | 渲染默认不回写，空服务端工具不能凭按钮补有效类别 | [宝箱Q140](../../systems/treasure/architecture.md#q140-客户端默认与服务端空类别断链) |
| Q141 | 工具分类与奖励分派不匹配；保持待裁定 | 外观箱名等多项不受B奖励分派消费，不补映射 | [宝箱Q141](../../systems/treasure/architecture.md#q141-菜单分类与奖励分派不对齐) |
| Q142 | 工具菜单/命中读取当前主手；保持待裁定 | 无栈绑定或发射快照，普通物品配置同样生效 | [宝箱Q142](../../systems/treasure/architecture.md#q142-设置操作读取当前主手) |
| Q143 | 弹体来源空声音标识；表现待核 | 宝箱工具及妖兽016毒液、028水枪不补声音；026蓝焰火来源明确指定原版箭声，原样迁移。来源空ID实际日志/异常未实机 | [宝箱Q143](../../systems/treasure/architecture.md#q143-弹体空声音标识)、[妖兽投射物品](../../systems/creature/architecture.md#三件妖兽投射物品松手入口2026-11-18) |
| Q144 | 目标飞行法器修理遗漏已修；必要单项通过 | 受损法器持锤恢复1点生命并发送十粒子，满血持锤不进入骑乘；真实物品召唤单类通过，不代表八类完整生命周期验收 | [装备Q144](../../systems/equipment/architecture.md#q144-飞行法器持锤修理分支遗漏) |
| Q147 | 来源动画引用缺失模型骨骼；按来源保留 | 阎王idle引用不存在的`bai`，马面death引用不存在的`gangcha`；目标资源原样接入，表现尚未实机核验 | [NPC Q147](../../systems/npc/architecture.md#q147-阴司动画引用不存在的模型骨骼) |
| Q148 | 阵元能力差异误判已更正；必要单项通过 | 子类空方向委托Forge40.1.0父类，实际继承InvWrapper；布局经空方向能力读材料，无普通库存回退。目标保持六面和无方向共库，撤回额外扩展的判断 | [阵法](../../systems/zhenfa/architecture.md#阵元方块2026-11-14) |
| Q149 | 目标管理档案消费者已补齐带攻击属性的自定义Mob；其他所有者与原版Mob仍开放 | 来源管理工具向任意非玩家Mob复制档案并执行通用属性计算；目标已覆盖NPC所有者五基类并补齐阴司族，本批为带原生`ATTACK_DAMAGE`的其他自定义Mob复用档案写入与资质公式；缺少该属性的原版Mob继续保持Q159边界，其他数据所有者仍需按公开操作接入 | [NPC Q149](../../systems/npc/architecture.md#q149-管理工具档案消费者覆盖范围) |
| Q150 | 来源操作提示与实际开面板条件不一致；目标提示已按实际入口改为“右键”，来源是否原意要求潜行门控仍待统一裁定 | 提示写“潜行右键”，但来源与目标物品`use`均未检查潜行；目标保持手持右键开面板并修正文案，不新增潜行条件 | [NPC Q150](../../systems/npc/architecture.md#q150-管理工具潜行提示与实际入口不一致) |
| Q151 | 来源02管理菜单已注册但未发现可达入口；保持待核 | `TIANDAO_NP_CSET_02`只有菜单/屏幕注册，零槽容器与单文本框无按钮或消息；源码范围内未找到打开调用，目标未接入 | [NPC Q151](../../systems/npc/architecture.md#q151-管理工具02界面没有可达入口) |

| Q152 | 阵旗来源写入任意方块实体，目标仅接受阵基实体；待裁定 | FlagmotherItem/FlagsonItem按目标坐标访问下方任意方块实体；目标保持阵法数据所有者边界，不扩写未知方块实体 | [阵法](../../systems/zhenfa/architecture.md#来源差异与当前边界) |
| Q153 | 乾坤门徒Blockly任务脚本与事件根脱接；保持来源可执行行为 | XML存在8条脚本及0/9分支，但生成Java事件根仅含创意潜行GUI，未连接任务提交；目标不补造普通右键任务入口 | [NPC](../../systems/npc/architecture.md#乾坤道人与门徒来源断链) |
| Q154 | 来源renwuset命令与门徒GUI字段写入方式不同；待集中裁定 | 命令写同名键字符串并接受任意命令源坐标，GUI写0–9数值（double）；命令射线坐标未消费，当前管理包不迁移命令 | [NPC](../../systems/npc/architecture.md#乾坤道人与门徒来源断链) |
| Q155 | 目标道人烟雾计数同步遗漏已修；必要单项通过 | 按来源逐次递增同步，并在54次后分别同步关闭和清零；不以setChanged替代网络更新 | [NPC Q155](../../systems/npc/architecture.md#q155-道人烟雾逐次状态同步遗漏) |
| Q156 | 目标乾坤方块贴图目录转换遗漏已修；图集警告消失 | 粒子模型与Geo渲染统一使用textures/block，由内容台账和资产清单重生成，移除旧blocks副本 | [NPC Q156](../../systems/npc/architecture.md#q156-乾坤方块贴图目录没有进入目标图集) |
| Q157 | 本批门徒动态文本颜色中断已修；截图复看通过 | 按1.18.2版本先展开完整带§6的编号字符串，再绘制，保留中文映射和整段颜色 | [NPC Q157](../../systems/npc/architecture.md#q157-门徒任务编号翻译参数中断颜色) |
| Q158 | 来源三类盘菜模型引用空纹理路径；保持来源，待集中裁定 | food_011/027/066来源模型的missing槽分别被4/4/5个面实际引用，绑定blocks/空路径；目标仅规范化为block/，缺失不是本轮转换遗漏 | [Cooking Q158](../../systems/cooking/architecture.md#q158-三类盘菜模型纹理警告) |
| Q159 | 来源管理工具对缺少攻击属性的Mob会空指针；静态确认，待集中裁定 | 来源允许任意Mob，但计算到attack_Num后无判空更新ATTACK_DAMAGE；Forge40.1.0默认牛不含此属性，前序写入及20刻生命回调已发生；不擅自加属性或跳过异常 | [NPC Q159](../../systems/npc/architecture.md#q159-管理工具对缺少攻击属性的mob发生异常) |
| Q160 | 来源分页先整数除法及原始偏移；保持来源，待裁定 | 任务分类后仍按原始目录偏移，整页可能出现空页，分类切换不清页码 | [quest Q160](../../systems/quest/architecture.md#q160-任务页数与原始目录偏移) |
| Q161 | 来源任务页码恒为0/0；保持来源，待裁定 | 显示过程从ItemStack.EMPTY取页码而非玩家会话，实际导航仍读本人目录 | [quest Q161](../../systems/quest/architecture.md#q161-来源页码显示恒为00) |
| Q162 | 八个空谷残卷结构的成就动态授予链已发现；静态自动核验通过，待运行核实 | `check_quest_advancement_chain.py`已逐个核对八模板块索引/坐标、`ForgeData`→`NeoForgeData`、任务方块ID、脚本逐字一致及001–008成就链；Java直接API授予者仍未发现，未作实机验证 | [quest Q162](../../systems/quest/architecture.md#q162-八个结构的成就动态授予链) |
| Q163 | 虫卵培养说明与可执行入口断链；保持来源，待核实 | 静态虫卵没有use/useOn/tick或成虫配方，药锄生产者已接，不补孵化系统 | [guchong Q163](../../systems/guchong/architecture.md#q163-虫卵培养说明没有已发现的可执行入口) |
| Q164 | 采药锄霉运读取幸运等级；保持来源，待裁定 | 霉运分支同样读幸运等级，无幸运时扣2%；概率不裁剪 | [equipment Q164](../../systems/equipment/architecture.md#q164-霉运分支读取幸运等级) |
| Q165 | 红包末两档不可达；保持来源，待裁定 | roll为[1,100)，0.5档及else不命中，完整58档仍保留 | [festival Q165](../../systems/festival/architecture.md#q165-红包末两档不可达) |
| Q166 | 财神服全套文案与胸甲独立行为；保持来源，待裁定 | 来源只检查胸甲部位，目标不补全套条件 | [festival Q166](../../systems/festival/architecture.md#q166-财神服全套说明与胸甲独立行为) |
| Q167 | 红包多个掉落实体共享奖励栈；待核实 | 逐件发放使用同一ItemStack，实际合并/拾取影响未核，保持来源调用 | [festival Q167](../../systems/festival/architecture.md#q167-红包多掉落共享奖励栈) |
| Q168 | 红包外部生产链已静态核实；受控购买已验、自然生成待验 | 来源与目标均有财神20金银商店链；目标受控右键、中文对话、20金银购买与保存重进通过，公历三月自然出生及自然遇见仍待，详细入口和指纹见所属正文 | [festival Q168](../../systems/festival/architecture.md#q168-红包外部自然生产者待核) |

这份调查提供全量架构评审依据，未完成所有内容的逐分支数值、获取链和两个版本实机对照；具体证据范围写在各正文。
| Q169 | 连续选项命令参数错位；代码已兼容来源点击格式，待运行补核 | click多出renwuweizhi参数，名字MessageArgument吞余参；目标改为greedy参数解析末尾序号并兼容可选标记与空格显示名 | [quest Q169](../../systems/quest/architecture.md#q169-连续选项命令参数错位) |
| Q170 | 内嵌选项命令缺参；待裁定/保持来源 | 来源只有/scoreboard players set -2，未擅自补参数 | [quest Q170](../../systems/quest/architecture.md#q170-内嵌选项命令缺少参数) |
| Q171 | 连续选择后处理无已发现调用者；保持来源，待核实 | `TaskXvanzehouProcedure`仅有定义；`PlayerTickxitong.mod.json`中的调用块为XML顶层孤立块，生成Java无实际调用。目标仅保留任务会话`playerxvanze`消费；记分板写入和经济同名字段均不构成任务生产者，不补造tick或命令调用 | [quest Q171](../../systems/quest/architecture.md#q171-连续选择后处理未发现调用者) |

| Q172 | 任务按键语言命名空间遗漏；目标已修/待运行补核 | 首轮显示原始键；已修两语言与转换输入，下轮只补此键 | [quest Q172](../../systems/quest/architecture.md#q172-任务按键语言命名空间遗漏) |
| Q173 | 任务列表固定画布裁切；待裁定/保持来源 | 854×480截图可见中文八行，500×500画布部分越界，完整视觉不判通过 | [quest Q173](../../systems/quest/architecture.md#q173-任务列表固定画布裁切) |
| Q174 | 任务方块编辑器空值与事件顺序差异；待核实/保持来源 | 空输入保留旧值、切换高级/普通保留另一类脚本、粒子勾选不能取消；右键事件不取消，潜行空手可能继续执行原对话，已接入但未做客户端复核 | [quest 月志](../../systems/quest/logs/2026-11.md) |
| Q175 | 固定商人刷怪蛋转换未完成；待验证 | 11枚蛋的生产注册、来源颜色、内容条目、双语名称和模型已接回，仍待客户端显示/自然生成验证，未提升为行为已迁移 | [npc 月志](../../systems/npc/logs/2026-11.md) |
| Q176 | Pao10 资源静态核对已完成；客户端显示待验证 | 资源审计确认Pao10–Pao13所需方块状态、模型、物品模型、贴图和Pao10粒子图集均存在；来源`pao1002.png`无JSON/Java调用，记录为来源孤儿资源，不复制；客户端显示与表现单项仍待 | [festival 月志](../../systems/festival/logs/2026-11.md) |
| Q177 | 8 档护腿及花繁城两档宝匣的命令已修正；材料未决 | 奖励池 `hujia` 及商店 `preset_n51_64_n320` 只修正 `leggins` 拼写。`BaoxiangYaoshoucailiaoProcedure.java:129` 三级第 22 签为未注册 `cailiao_153`，不推断替代物品。 | [宝箱记录](../../systems/treasure/logs/2026-11.md#阶段-4-首批奖励表) |
| Q208 | 来源答题自然打开入口断链；保持现状，待集中复核 | DatiGUIMenu只在客户端菜单工厂注册处构造，BookRightClick.mod.json生成题目/打开菜单积木脱离event_trigger，全A源码未发现普通书右键调用；目标仅保留显式BookMenus.openQuiz夹具入口，不补造自然入口 | [书籍](../../systems/book/architecture.md#答题2026-11-13) |
| Q209 | 符箓学习载体生产者未发现；保持消费链，待集中复核 | B FudangetGoProcedure只读取主手/副手fuluget并成功后扣除；A/B源码未发现向物品写入fuluget的生产者。目标保留FuluKnowledgeRuntime、DrawingTableRuntime及Fulu.createReward，不补造来源没有的学习载体生产入口 | [符箓](../../systems/fulu/architecture.md#q209-符箓学习载体生产者未发现) |
| Q210 | 原初系统旧牌匾菜单无自然打开者，按钮消息不提交文本；保持独立注册身份、不补造输入路径 | `PlaqueGUIMenu`、`PlaqueGUIScreen`与`PlaqueGUIButtonMessage`来源调用闭包未找到自然打开者；按钮payload仅含按钮号，服务端过程读取静态EditBox并带调试广播，不形成可信文本提交链 | [装饰](../../systems/decor/architecture.md#q210-旧系统牌匾菜单缺少自然文本提交链) |
| Q211 | 浅蓝染料触发黄色映射；保持来源 | 牌位染色过程把浅蓝染料映射到黄色；目标保留此颜色差异，不按物品名称改为浅蓝 | [装饰](../../systems/decor/architecture.md#q211-浅蓝染料颜色映射) |
| Q212 | 牌位收回物品不复制染色与发光状态；保持来源 | 来源回收过程只按方块身份写物品，没有把方块实体中的染色或发光字段复制到收回栈 | [装饰](../../systems/decor/architecture.md#q212-牌位收回未复制染色与发光) |
| Q218 | 五种反应植物遗漏来源随机刻委派；确认版本差异、已修复且受控必要单项通过 | 1.18.2/Forge40.1.0基类randomTick调用tick，1.21.1/NeoForge21.1.265为空；五花型randomTicks只覆写tick，目标补回委派，保留原概率/顺序/维度限制 | [植物](../../systems/plant/architecture.md#q218-五种反应植物的随机刻委派差异) |
| Q219 | 都承盘多件库存切换只返一件并清整槽；保持来源，原生网络按钮有限验证通过/待裁定 | 23单槽允许64件，但复用15菜单的切换过程把返还副本强制为1后清槽并默认变为1501；原decor临时Q218重号已独立编号，不改变来源行为 | [装饰](../../systems/decor/architecture.md#q219-都承盘多件库存切换只返一件) |
| Q224 | 余家具强制赋槽被目标父类按物品上限截断；确认版本差异、已修复且有限验证通过 | 23/29来源直接赋槽仅按容器64件限制，目标覆写setItem还原；三把带名剑的客户端同步、真实保存重进和破坏掉落通过，普通插入仍守物品上限 | [装饰](../../systems/decor/architecture.md#q224-强制赋槽的版本差异与修复) |


| Q178 | 空间五无映射仍拼接空维度与显示名命令；保持来源，待裁定/待验证 | 未匹配阵位或境界时维度字符串为空；目标保留来源命令，不补默认维度，也不改用UUID；完整跨维路径未验 | [阵法空间效果](../../systems/zhenfa/architecture.md#阵基效果阴系与空间系2026-11-15) |
| Q179 | 阵法检测的灵根分支不可达、等级2错读技能名及功德文案错配；保持来源，待裁定 | 仅灵根因来源互斥嵌套不可达；属性、功法、效率和难度可达。等级2从功法槽读jinengName，功德使用罡煞文案，目标均保持 | [阵法空间效果](../../systems/zhenfa/architecture.md#阵基效果阴系与空间系2026-11-15) |
| Q180 | 荧光蛊释放扣件的端侧位置不同；待统一复核 | 来源过程在服务端生成分支之后执行扣件，目标由服务端生成并扣件；实际双端预测影响未做两版本对照，保持当前服务端真值边界 | [蛊虫Q180](../../systems/guchong/architecture.md#q180-荧光蛊释放扣件的端侧边界) |
| Q181 | 03/04死亡旋转、04动画过渡及03摔落元数据与来源不同；待机制核实 | 来源渲染器未覆写死亡旋转、04动画过渡4刻、03仅hurt排除摔落；目标分别记录0旋转、2刻与额外忽略摔落标志。尚未核实原生调用及视觉结果，未据此改动或撤销历史必要单项 | [蛊虫表现边界](../../systems/guchong/architecture.md#2026-11-13-编号01完整来源包) |
| Q182 | 九档灵石原矿提示与触发方式不同；保持来源，待裁定 | 提示称破开石头，实际为右键空气将整组原矿转换为同数量、对应等级的四品灵石，未检查或破坏方块；目标保留提示与过程 | [经济原矿开封](../../systems/economy/architecture.md#九级灵石原矿开封2026-11-15) |
| Q183 | 阵法检测的可达属性及功法反馈此前漏实现；已实现，待运行验证 | 已纠正全部高阶输出不可达的误判，补齐属性、功法、效率和难度；命令坐标保留小数，跨系统读取经公开API，来源异常仍按Q179保留 | [阵法反馈规则](../../systems/zhenfa/architecture.md#阵基效果阴系与空间系2026-11-15) |
| Q184 | 阳二向任意方块实体写罡气且空目标会异常；保持来源，待裁定 | 同语义改由共享附件承载，仍不增加目标类型限制或空目标兜底 | [阳二边界](../../systems/zhenfa/architecture.md#q184-阳二方块罡气与空目标) |
| Q185 | 阳三在下方空气时立即移除，攻击者字段自赋；保持来源，待裁定 | 高空召唤可在首刻消失，未改成落地移除，未补攻击者消费链 | [阳三边界](../../systems/zhenfa/architecture.md#q185-阳三移除条件与攻击者字段) |
| Q186 | 阳一箭漏复制筛选/攻击者、基础攻击0.1及二十刻广播；保持来源，待裁定 | 保留原生箭伤与标记字段差异，不补字段或静默来源广播 | [阳一边界](../../systems/zhenfa/architecture.md#q186-阳一弹体字段与广播) |
| Q187 | 02加点页说明称境界不直接增加属性，与实际成长矛盾；保持来源，待裁定 | 来源四行说明原样保留，加点复用既有六项SkillPoints与服务端扣点，不增加重置入口 | [修炼加点页](../../systems/xiulian/architecture.md#属性加点页2026-11-15) |
| Q188 | 十职业等级加减反馈错读修炼层数；保持来源，待裁定 | 职业等级实际写入正确，可选反馈按来源读取LEVELnumber；set反馈仍显示原参数 | [职业命令反馈](../../systems/profession/architecture.md#q188-等级加减反馈错读修炼层数) |
| Q189 | 修炼日数减一不能从一归零，加十/百可写负数；保持来源，待裁定 | 保留来源按钮的严格比较与寿元减一分支，不新增截断 | [修炼三页边界](../../systems/xiulian/architecture.md#修炼观想与功法参悟页2026-11-15) |
| Q190 | 观想先遇坐具便结束整轮威胁扫描；保持来源，待裁定 | 远处威胁可能不再核对，继续保持仅扫描Mob及来源顺序 | [观想规则](../../systems/xiulian/architecture.md#修炼观想与功法参悟页2026-11-15) |
| Q191 | 参悟说明无时限与实际天数倒计时矛盾；保持来源，待裁定 | 说明原文保留，启动仍需一天且每秒减天，不改玩法或文案 | [参悟边界](../../systems/xiulian/architecture.md#修炼观想与功法参悟页2026-11-15) |
| Q192 | 展示架对工具裸ID的排除比较恒放行；保持来源，待裁定 | 注册ID含命名空间，裸lockblock比较不命中，未锁架仍可放非空工具 | [展示架排除](../../systems/decor/architecture.md#q192-博古架工具排除条件恒放行) |
| Q193 | 轮回管理命令匹配目标后却作用执行者；待裁定、未实现 | 管理员A指定B时来源给A执行轮回37；保持37跨所有者清理契约开放，不擅自改目标 | [轮回目标](../../systems/xiulian/architecture.md#q193-轮回命令的执行者与目标不一致) |
| Q194 | 展示架潜行换型先保存移除再读回；保持来源，待裁定 | 换型与原移除掉物顺序可能使旧栈掉落后恢复副本，未自行去重或改变顺序 | [换型库存](../../systems/decor/architecture.md#q194-潜行换型保存与移除掉落顺序) |
| Q195 | 材料命令在每个非目标玩家分支发送未找到提示；保持来源，待裁定 | 多人同维度即使命中目标也可能夹带报错；永久解锁不刷新当前千材录缓存，按原刷新入口更新 | [材料管理解锁](../../systems/material/architecture.md#q195-逐玩家未找到提示) |
| Q196 | 展示架所谓关闭回调每刻操作玩家脚下坐标；保持来源，待裁定 | 空菜单槽时按玩家位置清理方块槽0，而非菜单目标；保留原触发和坐标 | [展示架回调](../../systems/decor/architecture.md#q196-架菜单所谓关闭过程实为每刻操作玩家坐标) |

| Q197 | 传承灵物第30次为空奖励；保持来源，待裁定 | 第30次条件明确无奖励，目标分次奖励保留空分支，不补造额外奖励 | [传承灵物](../../systems/xiulian/architecture.md#q197-传承灵物第三十次无奖励) |
| Q198 | 家族袋第12次承载物与来源身份不一致；保持来源，待裁定 | 来源给出book_020但身份字段写book_005，目标保留来源序列和差异 | [家族袋](../../systems/xiulian/architecture.md#q198-家族袋激流波的承载与身份不一致) |
| Q199 | 多结构分组没有自然写入者；保持来源，待裁定 | 目标保留空配置空转与显式分组生成，不补默认配置或隐藏写入者 | [多结构](../../systems/tiandao/architecture.md#q199多结构分组没有自然写入者) |
| Q200 | 出身传送函数没有实体命令源；保持来源，待裁定 | @p可能选择错误玩家，目标保留函数语义并记录风险 | [初始奖励](../../systems/xiulian/architecture.md#q200-出身传送使用无实体最近玩家选择器) |
| Q201 | NBT多选提示称只能选一个但来源按首项优先；保持来源，待裁定 | 目标按来源选中顺序处理首项，不把提示改成多选说明 | [NBT编辑器](../../systems/tiandao/architecture.md#q201-多选提示与实际优先级) |
| Q202 | NBT编辑后来源强制数量为1且不返还余量；保持来源，待裁定 | 目标保留数量归一化与余量不返还边界 | [NBT编辑器](../../systems/tiandao/architecture.md#q202-编辑后强制数量为1) |
| Q204 | 建筑笔以零坐标作选点哨兵且第二点高度不消费；保持来源，待裁定 | 目标保留零哨兵和铺设时强制同高，不改成显式点状态或斜坡屋顶 | [建筑笔](../../systems/decor/architecture.md#q204选点零哨兵和第二点高度未消费) |
| Q205 | 建筑笔歇山/庑殿没有切换入口且过程复用；保持来源，待裁定 | 目标保留来源分支、名称和黑瓦共享过程，不补造形态切换 | [建筑笔](../../systems/decor/architecture.md#q205屋顶形态切换与过程复用断链) |
| Q206 | 建筑笔铺设无范围、替换、材料或回滚保护；保持来源，待裁定 | 目标保留直接写入、宽度0退化和无成本边界 | [建筑笔](../../systems/decor/architecture.md#q206铺设范围和失败保护为空) |
| Q207 | 六组门户运行消费者已实现；无归、修罗首次及重进后再次完整往返、门户/POI保存通过；地灵资源待接收 | 人间/妖魂和天阙既有有限证据复用；一般多维门户组合加载人间道随机结构发生768 MB OOM仍开放；完整自然等待/多人及天阙重进再次完整往返未验 | [天道门户](../../systems/world/architecture.md#天道维度方块与门户来源闭包2026-11-15)、[天阙](../../systems/world/architecture.md#天阙七群系与门户接收2026-11-18)、[无归](../../systems/world/architecture.md#无归十群系与门户接收2026-11-18)、[修罗](../../systems/world/architecture.md#修罗道维度接线2026-11-18) |
| Q214 | 签筒公式使第36首诗不可达，零档仍发送空消息；保持来源 | 非负世界时间下来源`ceil((时+分+秒+时间尾两位)/7)`取值0–35，静态第36档不可达；目标保留公式和空消息，不补造随机入口 | [装饰](../../systems/decor/architecture.md#签筒与装饰梯子2026-11-16) |
| Q215 | 签筒动画待办按来源由服务端刻推进；集成服可播放、专服客户端无入口 | 两逻辑端右键均向共享静态队列加入携带原Level的待办；`ServerTickEvent`递减40刻后，抽签过程在集成服可发布ClientLevel激活事件，专用服务端没有客户端待办。目标保留该端侧边界，不新增开奖payload | [装饰](../../systems/decor/architecture.md#签筒与装饰梯子2026-11-16) |
| Q225 | 同名签筒配方实际产陶轮；来源已核、保持来源、待统一复核 | 文件名不构成签筒自然获取入口；仅纠正文档，不改配方或补造供给 | [装饰](../../systems/decor/architecture.md#q225-同名签筒配方的来源结果) |
| Q226 | 灵兽袋外层妖兽标签/存活门槛漏读；旧普通狼异常来源结论撤回，目标入口已纠正且必要受控客户端通过 | 补核完整当前使用手、三种主手前置物品、妖兽标签、死亡/活体及驯服门槛；旧305项原件保留，只撤回普通狼异常的规格解释，其余有效证据复用；本批客户端结果见所属月志 | [生物](../../systems/creature/architecture.md#q226-灵兽袋收纳入口与体型属性) |
| Q227 | 灵兽袋染色过程接受未上色袋但菜单槽拒绝；保持来源，扩大输入待裁定 | A `lingshoudai`标签仅含01–16，`LingshoudaizhuanhuanMenu:80–95`首槽只接受该标签，而`LingshoudairanseProcedure:24–27`另接受00袋；目标保留该不可达分支，不补造未上色袋染色入口 | [生物](../../systems/creature/architecture.md#q227-未上色灵兽袋染色菜单断链) |
| Q228 | 世家初始礼包两次发放复用同一栈，数量依赖库存插入分支；静态已核实、保持来源 | 空槽保留对象引用，尾段改数量后再次合并，空背包合计2件；旧3件预期已更正，已有同类栈及满包未验；调整行为须另行裁定 | [修炼](../../systems/xiulian/architecture.md#q228-世家礼包复用已入包物品栈) |
| Q229 | 普通妖兽交互的食物标签和物品驯服度生产断链；来源静态已核、保持来源，供给扩充待裁定 | 已连接消费者保留食物优先恢复6/扣1、物品tame_num严格(0,100)令实体+5和带名反馈；A/B未找到food成员或物品tame_num生产者，游离Blockly调用不构成入口，目标不补造自然供给 | [生物](../../systems/creature/architecture.md#普通交互与自然供给断链2026-11-18) |
| Q230 | 游魂01死亡先递增计时再比较零；保持来源 | 目标受控致死后二十个自然服务端刻未移除；不改为二十刻移除，1.18.2版本实机未验 | [生物](../../systems/creature/architecture.md#棺材与游魂01来源功能族2026-11-18) |
| Q231 | 游魂01读取保存NBT的根`IsAngry`；常规写入者未确认，保持来源 | 目标仅验证自由字段保存至`NeoForgeData`子项，不能证明根字段粒子分支可达 | [生物](../../systems/creature/architecture.md#棺材与游魂01来源功能族2026-11-18) |
| Q232 | 人间道随机结构01跨区块写入裁切；锁定来源静态已核、保持来源 | 两版本 `FEATURES` 横向半径均为1；两处目标样点的458个拒写坐标与来源规则重算一致，不代表全模板完整或1.18.2版本实机已验 | [世界](../../systems/world/architecture.md#q232-人间道随机模板跨区块写入裁切) |
| Q233 | 摄虫网按捕获时显示名保存字面自定义名称。静态来源已核，保持来源 | 英文首次捕获、简中重放后，认主消息沿用已存英文虫名。简中资源已核，全程简中新捕获动态显示待验 | [蛊虫](../../systems/guchong/architecture.md#q233-摄虫网保存捕获时的显示名) |
| Q234 | 守罐飞蝗击杀普通动物后仍可能持有已移除目标，导致不回罐；保留来源判定 | 1.21.1 版本实测移除后 40 刻及两次虫维护均不回罐，活动数仍为 1；1.18.2 版本静态已核，未作实机对照 | [蛊虫](../../systems/guchong/architecture.md#q234-普通动物死亡后的守罐目标) |
| Q235 | 灵力／神识伤害旧版绕过护甲同时排除盾挡；目标默认标签嵌套保留此行为 | 四种标签生效，带来源的两种伤害已通过正面对照；无生产修改，不添加冗余标签 | [战斗](../../systems/combat/architecture.md#q235-灵力与神识伤害的盾牌标签) |
| Q236 | 地上罐计划刻放虫名称对空字符串执行替换；来源规则已实现，受控空名与非空名已验证，待集中复核 | 空名得到空串、非空名得到 `]`；不套用手持罐或蛊笛入口，未执行两版本实机 | [蛊虫](../../systems/guchong/architecture.md#地上罐名称与主人经验来源边界2026-11-20) |
| Q237 | 蛊虫击杀升级向当前维度查找主人后直接访问能力；静态已核、保持来源，待集中复核 | 主人不在当前维度或UUID无效时查找可能为空；目标当前路径同样可能异常，不补跨维或离线容错 | [蛊虫](../../systems/guchong/architecture.md#地上罐名称与主人经验来源边界2026-11-20) |
| Q238 | 摄虫网按注册名动态匹配物品，05追踪虫符合命名但追踪字段不被复制；静态已核、保持来源 | `guchongxvnfu` 为真时先被排除；实际交叉捕获和追踪丢失场景未实测 | [蛊虫](../../systems/guchong/architecture.md#摄虫网与追踪虫交叉入口2026-11-20) |
| Q239 | 环境生物 01–12 有死亡掉落与第 20 刻两个经验结算入口；静态已核、保持来源 | 实际经验球数量受原生条件和事件影响，两版本实机待核 | [生物](../../systems/creature/architecture.md#自然出生寻路与环境生物经验) |
| Q240 | 璞玉奇遇97被骨粉分支截获，101只有提示；静态已核、保持来源，待集中复核 | 厚土诀命令因前序范围不可达；来源与目标版本实机均未验证 | [材料](../../systems/material/architecture.md#q240-奇遇97与101来源分支) |

| Q220 | 来源模板的悬空火把、牌顶地毯与藤蔓原生更新；保持来源，来源版本实机待核 | 北向中心四支火把下方为空气，工筑堂四向地毯下方墙牌最后放入，藤蔓UP按原生邻块更新；目标仅修夹具预期，未补支撑或改模板 | [城镇](../../systems/town/architecture.md#q220-北向中心模板的四支悬空火把) |
| Q221 | 动态楼梯遗漏来源实体材质导致墙牌脱落；已确认迁移遗漏、已修复且有限复验通过 | 30种来源楼梯均为WOOD/STONE且dynamicShape；ContentStairs以forceSolidOn保留来源实体性，四向工筑堂墙牌/BE建造及真实重进后保持，其他材质实例/全部消费者未验 | [装饰](../../systems/decor/architecture.md#q221-动态楼梯的来源实体材质) |
| Q222 | 工筑堂东向物品框含来源旧绝对坐标；保持来源，目标加载ERROR待核 | build02east保留Tile坐标(88,-57,-167)，加载时1条ERROR；最终保存重进后的世界文件确认重定位并保留铁斧，未当作全部实体通过或零ERROR | [城镇](../../systems/town/architecture.md#q222-工筑堂东向物品框的旧绝对坐标诊断) |
| Q223 | 动态建筑文本键仍为旧命名空间；已确认资源遗漏、已修复，合并核对通过/显示实机待验 | 代码引用txt.yvanchuxiuzhen.build01–05而转换输出仍为旧键；修convert_prefix_lang及双语Town片段并重建合并资源，保留来源文案，未重拍卷轴名/中心列表 | [城镇](../../systems/town/architecture.md#q223-动态建筑文本的语言键命名空间遗漏) |
| Q241 | 1.18.2 炼器材料计算与天道图纸台预览未单独分支 `quality == 2`，中品落入 `0.85` 与次品同档；2026-11-23 负责人已裁定中品改用 `1.00`、已实现，待门禁与实机复验 | `B:Lianqijisuan`、`B:Lianqiuplevel` 与 `B:Shuaxin` 仅判断品质 4/3/1 分别乘 `1.15`/`1.05`/`0.95`，其余（含 `quality == 2` 中品与 `0` 次品）均乘 `0.85`；目标版本 `TripodMaterials` 与 `BlueprintTableRuntime` 已按负责人裁定将 `quality == 2` 调整为 `1.00`，`quality == 0` 仍保持 `0.85` | [炼器](../../systems/lianqi/architecture.md#材料累计与成品) |
| Q242 | 传承灵物末页调用未注册的裸命令；按来源保留 | 未补造符头奖励，不阻止任务继续完成 | [任务](../../systems/quest/architecture.md#升仙令与传承灵物自然任务入口2026-11-23) |
| Q243 | 喂狼最后一件肉在 1.18.2 版本可能先扣空再读取食物属性；静态风险，未实机复现 | 保留可确认的治疗数值，1.21.1 版本沿用先治疗后消费，不人为制造崩溃 | [烹饪记录](../../systems/cooking/logs/2026-11.md#2026-11-23-小肉与烤小肉喂狼) |
| Q244 | 长枪主手缺少普通攻击距离 +2；已修复（待实机验收） | 九柄枪已在 `EquipmentAttributes` 主手补充 `+2.0` `ENTITY_INTERACTION_RANGE`；副手不加 | [装备](../../systems/equipment/architecture.md#q244-主手长枪缺少两格攻击距离) |
| Q245 | 玩家商铺、商铺设置、拍卖方块设置缺普通按钮及底图；已修复（待实机验收） | 已补齐 3 张底图与 25 个 `PlainButton` 按钮及交互包，待客户端画面复核 | [经济](../../systems/economy/architecture.md#q245-三个经济页面缺少普通按钮与底图) |
| Q246 | 可达 NPC 管理页以宽版色块布局替换来源底图；保留现实现状（待裁定） | 既有 `NpcManagementCheck` 依赖四页结构与输入框契约，不破坏现有验收基线 | [NPC](../../systems/npc/architecture.md#q246-npc-管理页背景与尺寸不同) |
| Q247 | 好友页少了右下角无操作图标；已修复（待实机验收） | 已迁入 `imagebutton_friendpailie09.png` 并在 `(232, 198)` 绘制 20×20 空回调按钮 | [关系](../../systems/relation/architecture.md#q247-好友页右下角空回调按钮未绘制) |
| Q248 | 乾坤门徒方块名“任务点”变为“乾坤门徒”；保留现实现状（待裁定） | `QiankunCheck` 明确断言标题 `"乾坤门徒"`，不强行回退破坏既有 Smoke 校验 | [NPC](../../systems/npc/architecture.md#q248-乾坤门徒方块中文名不同) |
| Q249 | 创造模式对空气使用物品的扣件差异；已修复（待实机验收） | 五处空气使用扣件已补充 `!instabuild` 创造模式守卫 | [修炼](../../systems/xiulian/architecture.md#q249-创造模式对空气使用物品的扣件差异) |
| Q250 | 三类弹体的附加击退受抗性影响；已修复（待实机验收） | 三类弹体 `doKnockback` 已移除 `KNOCKBACK_RESISTANCE` 缩放 | [技能](../../systems/skill/architecture.md#q250-三类弹体的附加击退受抗性影响) |
| Q251 | 飓风扇两种施放缺云粒子；已修复（待实机验收） | `Fabao011Item` 潜行 20+40、普通 20 颗 `CLOUD` 粒子已接入 | [装备](../../systems/equipment/architecture.md#q251-飓风扇两种施放缺云粒子) |
| Q252 | 穿界古镜传送对象不同；保留精确目标玩家实现（待裁定） | 保持直传 `serverPlayer`，避免 `@p` 在玩家重叠时误传 | [装备](../../systems/equipment/architecture.md#q252-穿界古镜传送对象待多人对照) |
| Q253 | 饰品命令自定义字段不被收购与任务读取；已修复（待实机验收） | `CommerceValuation` 与 `DialogueRuntime` 已补齐装备 `money` 与通用字段匹配 | [装备](../../systems/equipment/architecture.md#q253-饰品命令自定义字段不被收购与任务读取) |
| Q254 | 宝箱设置命令权限由零升为一；保留权限 1 防生存越权（待裁定） | 避免降回 0 后生存玩家任意修改宝箱配置 | [宝箱](../../systems/treasure/architecture.md#q254-宝箱设置命令权限由零升为一) |
| Q255 | 连续聊天传送后指令使用旧坐标；已修复（待实机验收） | `QuestChatRuntime` 选项、命令与结构搜索改为实时读取 `player.position()` | [任务](../../systems/quest/architecture.md#q255-连续聊天传送后指令使用旧坐标) |
| Q256 | 玩家商铺材料不足反馈次数不同；已修复（待实机验收） | `ShopTrading` 首个不足槽位输出提示后即 `break` | [经济](../../systems/economy/architecture.md#q256-玩家商铺材料不足反馈次数不同) |
| Q257 | 四个技能命令不再接受裸命令；已修复（待实机验收） | `SkillCommands` 已为四条命令补回无参数根 `executes` | [技能](../../systems/skill/architecture.md#q257-四个技能命令不再接受裸命令) |
| Q258 | 四项游戏规则缺少来源简中名称及说明；已修复（待实机验收） | 双语 `xiulian.json` 已补全 8 组 `gamerule.*` 翻译键 | [修炼（跨系统）](../../systems/xiulian/architecture.md#q258-四项游戏规则的名称与说明未合入语言表) |
| Q259 | 独立 Shift 事件键未注册；保留现状（来源无读者） | 来源双模组均未消费该按键包，不额外注册无用键绑定 | [技能](../../systems/skill/architecture.md#q259-控制设置缺少来源的独立-shift-事件键) |
| Q260 | 65 件自定义护甲缺护甲与耐久类附魔资格；已修复（脚本重建） | 修复 `convert_enchantments.py` 并重生成 4 份 `enchantable/*.json` | [装备](../../systems/equipment/architecture.md#q260-自定义护甲缺少附魔适用标签) |
| Q261 | 重转附魔标签会移除两件旗与 29 件衣物；已修复（脚本同步） | 生成脚本已按 `armor` 字段与阵法旗成员对齐现行资源标签 | [装备](../../systems/equipment/architecture.md#q261-重转附魔会丢失现有标签成员) |
| Q262 | 妖兽未收到两种修为效果的经验；已修复（待实机验收） | `YaoshouCombat` 已订阅 `EffectExperienceEvent` 并触发升级属性/名称重算 | [战斗](../../systems/combat/architecture.md#q262-妖兽未收到两种修为效果的经验) |
| Q263 | 四个效果触发点少了本人客户端音效；已修复（待实机验收） | 四处效应已在 `clientTick` 经 `Level.playLocalSound` 补齐本地音效 | [战斗](../../systems/combat/architecture.md#q263-四个效果触发点少了本人客户端音效) |
| Q264 | 孢子效果的本机状态抽取；保留服务端权威实现（待裁定） | 避免客户端非权威随机抽样 `addEffect` 造成双端状态失步 | [战斗](../../systems/combat/architecture.md#q264-孢子效果的本机状态抽取不同) |
| Q265 | 连锁工具损坏后仍会扩挖；已修复（待实机验收） | `ChainMining` 循环每次读取主手实时 `chain` 附魔等级 | [战斗](../../systems/combat/architecture.md#q265-连锁工具损坏后仍会扩挖) |
| Q266 | 替换拍卖椅方块时扩大了座椅实体清理范围；已修复（待实机验收） | `AuctionChairBlock` 改回 `onDestroyedByPlayer` 与 `wasExploded` 清理 | [经济](../../systems/economy/architecture.md#q266-替换拍卖椅方块时扩大了座椅实体清理范围) |
| Q267 | 生存玩家短暂穿门不再立即传送；已修复（待实机验收） | `DimensionPortalBlock#getPortalTransitionTime` 返回 `0` 恢复瞬传 | [世界](../../systems/world/architecture.md#q267-生存玩家短暂穿门不再立即传送) |
| Q268 | 非玩家首入也会尝试建出口；已修复（待实机验收） | `DimensionPortalRuntime` 仅允许 `ServerPlayer` 建门，非玩家无门降级落点 | [世界](../../systems/world/architecture.md#q268-非玩家首入也会尝试建出口) |
| Q269 | 载有乘客的载具不再被门户入口排除；已修复（待实机验收） | `DimensionPortalBlock#entityInside` 补充 `!entity.isVehicle()` 拦截 | [世界](../../systems/world/architecture.md#q269-载有乘客的载具不再被门户入口排除) |
| Q270 | 门边云粒子变为下界门粒子并增加环境音；已修复（待实机验收） | `DimensionPortalBlock#animateTick` 改为发出 4 颗 `ParticleTypes.CLOUD` | [世界](../../systems/world/architecture.md#q270-门边云粒子变为下界门粒子并增加环境音) |
| Q271 | 副手点门工具破损反馈选槽不同；已修复（待实机验收） | `DimensionPortalItem` 的 `hurtAndBreak` 传入 `context.getHand()` | [世界](../../systems/world/architecture.md#q271-副手点门工具破损反馈选槽不同) |
| Q272 | 极限坐标附近寻找出口的中心偏移；已修复（待实机验收） | `DimensionPortalRuntime` 出口坐标夹紧到 `±29999872.0` | [世界](../../systems/world/architecture.md#q272-极限坐标附近寻找出口的中心偏移) |
| Q273 | 千材录与材料袋的已有栈合并跳过输入限制；已修复（待实机验收） | 抽取 `LegacyContainerMove` 检查已有栈合并时的 `slot.mayPlace(stack)` | [材料](../../systems/material/architecture.md#q273-千材录与材料袋的已有栈合并跳过输入限制) |
| Q274 | 百草鉴将无方块实体的物品能力当成样本库存；已修复（待实机验收） | `HerbAnalysisMenu` 校验 `getBlockEntity != null` 与 `IItemHandlerModifiable` 后绑槽 | [炼丹](../../systems/liandan/architecture.md#q274-百草鉴将无方块实体的物品能力当成样本库存) |
| Q275 | 乾坤道人奖励的五行基础和灵步不能研习；已修复（待实机验收） | `DialogueRuntime` 与功法/技能库已补齐从任务旧 NBT 挂载研习组件 | [任务](../../systems/quest/architecture.md#q275-乾坤道人奖励的五行基础和灵步不能研习) |
| Q276 | 按等级验收和扣除材料时读不到材料等级；已修复（待实机验收） | `DialogueRuntime#matchesFields` 已兼容 `Materials.instance` 写入的材料等级 | [任务](../../systems/quest/architecture.md#q276-任务按等级验收和扣除材料时读不到材料等级) |
| Q277 | 妖魂道温泉对无碰撞木块的地基判定不同；已修复（待实机验收） | `NativeWorldFeatures` 已将 `wenqvandiwu` 切回 `SourceLakeFeature` | [世界](../../systems/world/architecture.md#q277-妖魂道温泉对无碰撞木块的地基判定不同) |
| Q278 | 人间道采用外部群系时缺六种随机结构入口；已修复（待实机验收） | 已新增地表与地下群系修改器通过 `add_missing_biome_features` 挂载 `suijijiegou_01..06` | [世界](../../systems/world/architecture.md#q278-人间道采用外部群系时缺六种随机结构入口) |
| Q279 | 画卷两端被其他容器替换时的快照写入；保留安全边界（待裁定） | 禁止非副本画卷方块写入外部容器槽 0，防止破坏玩家正常炉具存档 | [副本](../../systems/dungeon/architecture.md#q279-画卷两端被其他容器替换时的快照写入) |
| Q280 | 大风雷火命中非生物时缺少碰撞粒子；已修复（待实机验收） | `YihuoProjectile` 已在非生物早退前发出 10 颗 `cailiao_<N>particle` | [异火](../../systems/yihuo/architecture.md#q280-大风雷火命中非生物时缺少碰撞粒子) |
| Q281 | 大风雷火不再向普通容器写入炉热；保留安全边界（待裁定） | 保持仅正规炉具写入热量，避免污染普通储物箱实体数据 | [异火](../../systems/yihuo/architecture.md#q281-大风雷火不再向普通容器写入炉热) |
| Q282 | 004 践踏、027 连打砸地的来源烟环名在目标版本未注册（待裁定） | `Yaoshou004jiantaProcedure:62`、`Yaoshou027liandazadiProcedure:141` 均写 `minecraft:smoke_large`；暂保留不显示 | [生物](../../systems/creature/README.md) |

## 源码位置

以下 A = `reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen`，B = `reference/1.18.2yvanchuxitong`，均相对 [来源工作区](README.md)。行号供编辑器定位。

| 标号 | 相对源码位置与本轮核对范围 |
| --- | --- |
| S01 | A `src/main/java/net/mcreator/yvanchuxiuzhen/procedures/PlayerGUI06xiulianProcedure.java` 13–31 |
| S02 | A 同 procedures 目录 `PlayerTickProcedure.java` 32–35、58–88、351–399 |
| S03 | A 同目录 `BookRightClickProcedure.java` 135–141、245–268 |
| S04 | A 同目录 `KaishigongfaProcedure.java` 75–118 |
| S05 | A 同目录 `EntityBeforeHurtProcedure.java` 45–120；`EntityBeforeHurt2Procedure.java` 1–28 |
| S06 | B `src/main/java/net/mcreator/yvanchuxitong/procedures/Tripod01ZaiFangKuaiShangYouJianProcedure.java` 22–45 |
| S07 | B `src/main/java/net/mcreator/yvanchuxitong/network/TripodGUIButtonMessage.java` 67–100；procedures `LianqiBeginProcedure.java` 118–122、436 |
| S08 | B procedures `LiandanBeginProcedure.java` 33–35、445、472–494；全文件检索等级检查调用 |
| S09 | B network `HuafuGUIButtonMessage.java` 67–99；procedures `HuizhifuluProcedure.java` 96–97、165–255 |
| S10 | B procedures `ZhenjiGengXinYouXiKeProcedure.java` 23–69 |
| S11 | A `src/main/java/net/mcreator/yvanchuxiuzhen/entity/Yaoshou017Entity.java` 73–77、127、158–235、271–320 |
| S12 | A procedures `GuchongtickProcedure.java` 27–106；`LingshoudaiqingkongProcedure` 静态调用索引 |
| S13 | B procedures `WorldbeginProcedure.java` 29–54 |
| S14 | B procedures `DepositinbankGoProcedure.java` 29–92；`BankcunchuProcedure.java` 29–115 |
| S15 | B procedures `ShopBuyProcedure.java` 56、70–76、158–182、257–266 |
| S16 | B procedures `PaimaiblockGengXinYouXiKeProcedure.java` 30–120 |
| S17 | B procedures `TaskduihuashijianProcedure.java` 48–59、60–238、555–690 |
| S18 | B procedures `MakebookxiaohaoProcedure.java` 10–20 |
| S19 | A `src/main/java/net/mcreator/yvanchuxiuzhen/world/inventory/Qiankundai01GUIMenu.java` 45–83、254–255、372–381 |

全量文件及调用定位见 [索引入口](inventory/README.md)。人工源码核对、自动索引、1.18.2 实机、1.21.1 实机的覆盖分别记录；目标版本有限实机结果见各系统月志；1.18.2版本实机与两版本对照仍未执行。
