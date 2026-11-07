---
topic: source-architecture-survey
role: data
status: partial
updated: 2026-11-07
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
| 实体类型注册（含形态/弹体） | 267 | 4 | 271 |
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

总计 4,938 条注册声明（2026-11-02 补入此前漏扫的 `ModGeckoLibArmors` 65 件盔甲、`ModAttributes` 2 个属性、`ModGameRules` 4 个游戏规则）、211 个状态字段及 4 个独立存档常量。基础索引另记录 7,683 处 Procedure 调用、5,752 处按名字段访问候选和 30,945 处字面量键调用候选。后续按 Mod/作用域解析获得 5,752 处可识别访问，另保留 315 处未解析成员；两个 5,752 的统计口径不同。2,841 个字面量键分组包含 NBT 与普通字典，不是已经确认的状态总量。

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

## 具体问题与裁定位置

问题在所属系统维护唯一正文，此处只保留索引。全部均未自动修复。

| 编号 | 核对结论 / 评审主题 | 唯一正文 |
| --- | --- | --- |
| Q01 | 同名功法字段已按类型分开；实际栏位和学习小游戏分别建模 | [功法](../../systems/gongfa/architecture.md) |
| Q02 | 制造职业快照与当前玩家/装置绑定，决定多人协作规则 | [炼器](../../systems/lianqi/architecture.md) |
| Q03 | 炼丹等级检查在图中为游离块，Java 与已连接图一致；恢复门槛属于行为调整 | [炼丹](../../systems/liandan/architecture.md) |
| Q04 | 阵法五行费用倍率缺失、分派名称错配、火2类别错配及部分伤害链未闭合 | [阵法](../../systems/zhenfa/architecture.md) |
| Q05 | 商店找零变量不一致；明确修正输入与结果 | [经济](../../systems/economy/architecture.md) |
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
| Q27 | 参悟台继承：装备的功法因继承而升级时槽中的书才被消耗；只加经验不升级时书留在槽中，可以反复继承同一本书的经验。评估为轻微，保持来源行为，待裁定 | [功法](../../systems/gongfa/architecture.md) |
| Q28 | 星宿穴位学习小游戏不可达：研习台 `gongfaxvexi` 的物品不在创造栏、没有配方，结构与代码都不放置它，槽位没有放入书的途径；界面 `gongfaxvexi_gui` 只登记、从未打开（右键研习台打开的是 `TripodGUI`）。评估为轻微：不迁移这部分（研习台方块、界面与 429 个过程），待裁定 | [功法](../../systems/gongfa/architecture.md) |
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
| Q40 | 拈花飞叶（`SkillTree01`）命中时用 `AttackDamage` 作攻击量，而它只在弹体消失那一刻从所选技能的 `ManaAttackDamage` 取值（技能书没有这个键，是 `jinengmanaatt`），命中时始终为 0，灵力伤害不会生效，只剩箭本身的伤害；`Ice02`（破碎冰矛）、`Light02`（定魄神光）复用 `Ice01`、`Light01` 的刻过程；飞羽冰花沿侧向圆弧飞行、计时每刻加两次，正前方的目标打不到。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q41 | 控火术（`SkillFire04`）用施放者自己 `getPersistentData` 里的 `AttackUUID` 拼 `/shanghai` 命令，玩家没有这个键，命令行里这一项为空串，空白合并后参数整体前移，实际按“无攻击者、基础量 = 伤害倍率、倍率 0”结算，伤害极小。评估为轻微，保持来源行为（经 `SpellDamage.command` 复现同样的命令解析） | [技能](../../systems/skill/architecture.md) |
| Q42 | 巨石锤（`SkillRock05`）类型一与类型二落地攻击里 20% 的眩晕（`effect_001`）加给了实体自己（`entity`）而不是受击者（`entityiterator`）；化石手（`SkillRock04`）按实体自己的朝向取 3.5 格或 1 格处作为攻击中心，而生成时朝向是随机的，攻击方向随机。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q43 | 雷系两处疑似疏漏（2026-11-05 更正：此前认为地滚雷比较的队伍名对玩家恒为空串、施放者本人走近也会触发，这是错的——登录过程 `PlayerJoinGame` 与每秒一次的 `Playerhuifu` 都把玩家变量 `team` 写入玩家的 `getPersistentData`，所以玩家的值就是其团队名，施放者本人与同队实体不会触发）：地滚雷（`SkillThunder08`）生成时没有转向，前进方向是随机朝向；`Skillwind02` 的聚风术（非聚风化气）不分敌友拉扯 4 格内全部实体。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q44 | 木系四处疑似疏漏：木篱术（`SkillTree05shifang`）放出九道木篱时只 `addFreshEntity`、没有走 `finalizeSpawn`，木篱的状态停在空串，实体刻过程不会让它展开、收起或消失，施放出的木篱永久留在世界里直到被打死（只有 `/summon` 之类走初始生成的才会进入阶段）；回身鞭（`SkillTree06`）只把带霸体（`skilleffect_08`）的目标抛起，与其他技能“无霸体才位移”相反；乱枝（`SkillTree04shifang`）潜行只给友方枝条写一个无处读取的 `baolie` 标记，没有效果；灰木树梯在实体刻里计数加了两次，存在时间是 26 刻而不是 52 刻。评估为轻微（木篱每次放 9 个永久实体，可被击杀或命令清除），保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q45 | 灵纹附魔（`EntityHurt2` 中读主手 `lingwen` 的一段）两处疑似疏漏：数量与灵力消耗用的是起尸诀附魔 `qishi` 的等级（`fumolevel`，没有起尸诀时为 0），灵纹附魔自己的等级只在灵纹弹的攻击量（等级 × 4）里用；扣的灵力是受击者 `entity` 的，不是攻击者 `sourceentity` 的（非玩家受击者视为 0 灵力，所以只有 `qishi` 等级为 0 时才会生成，同时带起尸诀附魔的武器打到怪物时灵纹不会触发）；另外生成的灵纹特效把计数 `lingwennum`、`friendnumber`、`belongToMasterUUID` 写进攻击者好友列表物品的 NBT（`getCompound` 取到的是同一个标签对象，又原样 `put` 进实体），好友列表里会残留这几项，下一只从残留值接着数。评估为轻微，保持来源行为（残留的计数不复现，每只从 1 起） | [技能](../../systems/skill/architecture.md) |
| Q46 | 火鸟术（`SkillFire06`）三处疑似疏漏：每 40 刻设一次飞行动画 `animation.skillFire06.fly`，同名的同步值不会再次触发，所以飞行动画只在 40 刻时播一次；有目标时的前冲速度竖直分量取 −0.2·sin(俯仰)，没有乘 cos(俯仰)，水平分量也没有乘 cos(俯仰)，俯视目标时前冲方向偏离视线；`OwnerHurtByTargetGoal`（目标类）加在行为选择器而不是目标选择器。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q47 | 起尸诀（`Skillyin01`）四处疑似疏漏：起尸诀附魔 `qishi` 的命中段（`EntityHurt2`）里的 `for (…4…)` 只是在找位置，找完后只放出<b>一只</b>尸体，而技能“起尸诀”放出四只；死亡拥抱的判定区在前冲结束处的前方 3 格（水平）、半边长 0.5，前冲 14 刻共约 5.6 格，目标只有在约 8.6 格外才落在判定内，目标更近时尸体先被目标挡住、判定区落在目标身后而打不到；近身且没有沉默时“普攻”被登记了两次（权重 20 加 20），抽到的概率是其余技能的两倍多；杀死实体让书升级时，修为加的是被杀实体的 `LEVELnumber`（而不是尸体的，多数原版生物没有这个键，所以只有带该键的妖兽等才让书增长），升级只改书的 `LEVELnumber` 与品阶文字 `belongTopinjietext`，品阶数值 `belongTopinjie` 不变，书上显示的品阶可能与品阶数值不一致。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q48 | 灵纹弹实体的 `tick` 先调用箭基类的命中处理，再推进灵纹弹过程；第一刻撞到目标时攻击量尚未写入，自定义灵力伤害不会发出第二次命中事件，灵纹数量为 1；第二刻以后撞到时攻击量已写入，自定义伤害再发出一次命中事件，数量为 2。1.18.2 版本的 `LingwentexiaoEntity.tick` 与目标实现保持同一顺序，故两种结果均为来源行为。评估为轻微，检查接受 1 或 2，不改变玩法顺序 | [技能](../../systems/skill/architecture.md) |
| Q49 | 虚空裂隙的碰撞过程只用实体上的 `shifouyichuanyve` 标记是否已使用，第一次有效碰撞后整道裂隙无法再被任何玩家使用；未找到目标世界或进入维度未匹配也会消费裂隙；从虚空返回时，`weidu` 在目标查找之前清零，目标缺失后不能靠同一道裂隙重试。坐标取裂隙位置，进入三轴均除以 50 后四舍五入（含高度），返回三轴均乘以 50。依据 `Jinengxvkong` 与 `XvkongliexiDangWanJiaYuGaiShiTiFaShengPengZhuangShi`；评估为轻微，保持来源行为，维度未迁移期间明确保留依赖限制 | [技能](../../systems/skill/architecture.md) |
| Q50 | 技能箭共用命中过程的箭雨分支，在命中实体时把受击实体设为二十支新箭的射手，命中方块时才使用原射手；新箭不继承技能名。暗箭把 1.2 或 0.8 写进目标的 `Damage_multiplier`，实际暗伤仍使用局部的 1.5 或 1。暗箭落地云指定 `Potion:minecraft:bad_luck`，该药水 ID 未登记，且命令中的命名空间值未加引号，在 SNBT 的冒号处解析失败，实际不生成云；保持原命令，不补造幸运或霉运效果。十个箭物品的释放条件恒为假，仅保留蓄力动画；孤立施放过程不接入新入口。评估为轻微，保持来源行为 | [技能](../../systems/skill/architecture.md) |
| Q51 | 身法双击窗口触发后不清空，十刻内后续按下仍可触发，冷却只检查追加前不超过上限；W 的移速效果不受位移冷却限制。轻功的 superjet 先清零再读扣费，因此喷射实际不额外扣灵力；flying 附魔减耗系数仅在大于零时使用，等级总和达到 20 后恢复全额耗费。起飞的拔空术速度 2 会立即被外层开关速度 0.5 覆盖；圆环和两行云雾把角度数值直接作弧度。依据 ShenfaW/S/A/D/space、ShenfaTick、FlyskillTick、Flyskillkaishitexiao 与 FanjianAnXiaAnJianShi，评估轻微，保持来源顺序和结果 | [技能](../../systems/skill/architecture.md) |

这份调查提供全量架构评审依据，未完成所有内容的逐分支数值、获取链和两个版本实机对照；具体证据范围写在各正文。

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

全量文件及调用定位见 [索引入口](inventory/README.md)。人工源码核对、自动索引、1.18.2 实机、1.21.1 实机的覆盖分别记录；目前后两项均未执行。
