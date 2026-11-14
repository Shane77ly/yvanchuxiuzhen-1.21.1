---
topic: npc
role: architecture
status: partial
updated: 2026-11-14
---

# 修士身份、实例与交互

源码定位采用[系统导航](../README.md)的A/B规则。本页登记当前实现和明确剩余范围，静态核对不等于实体自然投放或实机对齐。

## 本批实体与实例状态

`NpcCreatures`装配11个来源实体：`businessman_01`–`07`与`daguanjia`使用来源Gecko模型，`businessman_09`–`11`使用玩家骨架和衣甲层。注册分类、尺寸、跟踪范围64、更新间隔3、火焰免疫逐行取A `init/YvanchuxiuzhenModEntities.java`。09–11为`MONSTER`、0.6×2，其余为`CREATURE`、0.6×1.8，不统一改成同一分类。

| 实体 | 无实例等级时的来源等级 | 独有实例边界 |
| --- | --- | --- |
| businessman_01/02/03/05/09/10/11 | 11 | 03/10/11带每日材料店刷新记分 |
| businessman_04 | 33 | 初始原生生命10；受击可重置技能字段，空闲20刻/有目标100刻重置entitytick |
| businessman_06 | 3 | 其余按该实体初始化资质 |
| businessman_07 | 7 | 初始原生生命10，其余按该实体初始化资质 |
| daguanjia | 31 | 近战、受击仇恨、受击/近战动画和200刻死亡移除 |

`spawn_data.json`保留各实体十三项资质、来源实例字段与本地化键。`LEVELname`为空才初始化，已有`LEVELnumber≥1`时不覆盖等级；立即算攻击/移动属性，20刻后写最大生命并补满，随后按来源生成等级名与同型友方团队。01和03注册移速的浮点常量保留为`0.30000000000000004`；其他初始值和延迟重算分别处理。09构造时装备原矿与20号衣甲，10构造时装备31材料与18号衣甲，11初始化时装备19号衣甲。

商人只注册看玩家、随机看向和漂浮；大管家另有1.2速近战与受击目标，不给其他商人补攻击或漫步目标。来源对箭、玩家直接伤害、药水/效果云、摔落、仙人掌、溺水、闪电、爆炸、三叉戟、铁砧、龙息、凋零和凋零头颅的免疫均保留；受击过程在免疫判定之前执行。来源无距离消失，目标保持。客户端按来源空闲/行走、04受击与管家攻击/死亡控制器运行；服务端只维护实例和同步请求，不加载模型类。

来源依据：A `entity/Businessman01Entity.java`–`Businessman07Entity.java`、`Businessman09Entity.java`–`Businessman11Entity.java`、`DaguanjiaEntity.java`及其model/renderer；相应初始化/受击/更新过程，A `NpcshuxingjisuanProcedure.java`。`NpcStats`由NPC自己写实例属性，不借用Creature持久数据。

## 全局右键与消费者

B `PlayerRightClickEntityProcedure:40–43`限制事件手等于玩家当前使用手；`:66–180`按`npcentity`标签与实体ID设置脚本、商品表和说明。目标保留来源顺序：01–07、管家、既有财神、09–11；03/10/11只有`keyishuaxin=1`才调用经济`materialShop(1/2/3)`生成60项表并清记分。09保留固定12项矿石表。每日刷新在实例tick满足`旧时间+24000≤当前时间`或时间回拨时置1，再更新旧时间。

所有标签NPC置`renwuzuopen`；主手为天道NPC设置工具时跳过对话。按`renwulist1/tasklist1`起编号连续读取，优先对话字段，两者都空时停止；任务系统以脚本头ID跳过已完成记录，启动第一个未完成脚本。商品与说明交由经济公开操作写入；说明为空时清商品。不存在已实现的脚本不伪造完成。当前所用12个`txt.renwulist.*`语言脚本的动作类型已逐项核对；任务解释器的其余来源动作仍见[任务正文](../quest/architecture.md#普通对话与任务目录2026-11-14)。

### Q78 大管家出生判定读取错误实例

确认的来源缺陷，当前保留。B `PlayerRightClickEntityProcedure:113`向被点击的`entity`读取玩家能力`birth`，而非向`sourceentity`玩家读取。A `YvanchuxiuzhenModVariables:424–426`只给真实Player且非FakePlayer附加该能力，`:564`默认birth为“流浪侠客”；自然生成的大管家因此使用`npcdaguanjia04`，不会进入“世家纨绔”的01–03脚本。目标自然交互固定04并清后两脚本，未改成根据玩家出身选择。01–03语言脚本仍保留，尚无该自然生产者；是否修正由负责人统一裁定。

### Q88 强筋资质文本

确认的来源文本差异，当前保留。普通商人初始化含`mana_Att_LEVEL="强筋"`，A `NpcshuxingjisuanProcedure:140–150`只识别“强劲”，因此进入最低档公式。目标保留原文本和结果；不按字面推测纠正资质。静态已确认，来源与目标实机均未执行。

## 拍卖修士定义

B `NpcshujvjiaruProcedure:27–85`从`npc.data.list{n}`语言键逐项解析`/`分隔的类型化三元组，写入维度`worlddata`的嵌套标签。当前`definitions.json`保留已导出的唯一34级定义；`NpcDefinitions.snapshot()`返回副本，经经济的`AuctionNpcRequestEvent`交付。NPC是定义唯一写入者；经济拥有现场竞价状态。低级拍卖循环没有候选的来源事实与竞价生命周期由[经济正文](../economy/architecture.md)维护，不补造低级修士定义。

## 结构解封后处理（2026-11-14）

`NpcStructureReleases`以LOW优先级订阅World的`StructureReleasedEvent`，由Quest先完成花繁城任务方块补充，再按来源给附近真实21号写第二任务并在固定坐标生成09/10/11；四处商店/营地按距离顺序筛03号，将`keyishuaxin`计分板置1。实体与计分板在Npc所属入口写入，World不访问NPC实例。

目标现已实现`businessman_21`注册、模型和来源刷怪蛋，结构模板实例及追加第二任务由本页居民族承接。静态核对、主线编译及有限结构必要单项证据见[World月志](../world/logs/2026-11.md#2026-11-14-固定结构解封与镇妖塔后处理)，花繁城全部自然NPC/对话/刷新和保存重进仍未验收。

## 开放范围与验证边界

本批未接入`businessman_08`、`mamian/niutou/wuchangbai/wuchanghei/yanwang`、`qiankundaoren/qiankunmentu`方块/管理路径与其剩余行为。固定结构、动态生成与自然生成分别核对，实体内容表和标签存在不等于已在生存世界投放。

本批完成静态源码和内容JSON核对；主线已接资源共享清单与`npcentity`标签，定向转换、编译和6项架构检查通过。NPC原生右键/随机商店/存档/AI/模型仍待必要实机验证。1.18.2版本实机、1.21.1版本实机、多人和两版本对照均未执行；已有银行单步验收不能覆盖这些新增消费者。

## 居民21至30与结构任务（2026-11-14）

`NpcResidents`单独装配十类居民和十枚来源刷怪蛋。21/22为Gecko实体，默认生命20、攻击1，来源0.5×1.5碰撞尺寸再缩放0.8，渲染同样0.8；控制器过渡分别2和4刻。两侧`baseTick`累计100刻，当前生命达到上限九成才写微笑请求，无论是否微笑均清计数；`tick`先消费上一刻同步请求再调用父类，保留Forge40.1.0的`LivingUpdateEvent`顺序。死亡第79刻移除。23–30默认生命100、攻击1、0.6×1.8，使用原版PLAYER骨架和20刻死亡。十类保留看玩家、随机看向、漂浮、免伤、无距离消失及右键转身，不补攻击或商人档案初始化。

A/B实体、model、renderer、注册、过程与17个结构居民逐字段核对，来源SHA及关键行见`ledger/source-evidence.csv`，原始快照为`tmp/npc-residents-source-manifest.json`及`tmp/npc-residents-template-audit.json`。来源结构中的最大生命、攻击、移速、姓名和持久字段覆盖默认值，花繁城03的小孩哥为100血、2攻、0.2移速；首次目标单项发现旧属性格式未升级，实际回退20血/1攻，修复和03/04/08/09/11/12模板再生成统一见[Q130](../world/architecture.md#q130-结构mod实体旧属性没有升级)。全局标签右键继续由任务系统处理，NPC不写任务玩家附件。十类来源均关闭自然出生；实际结构放置、命令生成、刷怪蛋分别登记，不补自然出生规则。

`NpcStructureReleases`在Quest后处理之后，给花繁城附近真实21号追加`yiqizhuatuzier`，不覆盖模板首任务`yiqizhuatuzi`。首任务生产空笼并消费带兔笼，由Decor持有笼子生命周期；本批仅验证受控完成首任务后选择第二任务，不冒充自然捕兔交付全链。

### Q121 小孩哥奖励说明与动作不一致

A `structures/fanhuacheng03.nbt`的`entities[13].nbt.ForgeData.renwulist1`头部第6项说明“兔肉煲*4”，实际动作是`物品/加/minecraft:cooked_rabbit/1/无`。目标结构与解释器保留说明及熟兔肉一份的实际动作，不按说明增发或更换物品；来源差异留待集中裁定。静态核对已完成，实际首任务交付和奖励尚未验。

### Q122 士兵八选项只显示四项

A `structures/fanhuacheng12.nbt`的`entities[0].nbt.ForgeData.renwulist1`配置八种酒；B `DuihuaGUI02Screen.java:39–42`只有四个按钮，`:141/149/157/165`分派0–3，`DuihuashijianProcedure.java:134–184`解析脚本后打开该界面。目标保留全部八项脚本和来源四项界面，后四项自然不可达，不擅自增加按钮。静态核对已完成，自然士兵对话尚未验。

## 08号飞梭（2026-11-14）

`businessman_08`是独立`NpcFlyingShuttle`，不进入需要商人档案的模型注册循环。来源实际Java为MONSTER、3×1.5、火焰免疫、跟踪64/更新3；生命100、攻击3、移动与飞行速度0.3、护甲0、跟随16。飞行导航与`FlyingMoveControl(10,true)`只配10格看玩家目标，无攻击、漫步或漂浮目标；来源伤害免疫、空受伤/死亡声、零经验、常驻和20刻死亡保持。

初生加1200刻0级、无环境标记/无粒子的缓降，右键原生`startRiding`。载人每次`baseTick`设世界坐标速度`(0,.1,-.1)`，不按乘客或朝向旋转；首次载人移除缓降并持久保存`jieduan=true`，之后不再次移除新加的缓降。动画为来源`animation.feisuo.idle`，两控制器过渡4刻，同步请求在父类tick前消费；保留半透明、缩放1、阴影.5和死亡旋转0。无自定义纹理存档字段，无蛋、回收物品、玩家操控或额外消耗。

来源20文件及7份精确NeoForge21.1.265源码条目SHA见`tmp/next-frozen-evidence/npc08-source-manifest.json`和`npc08-target-api-evidence.json`，文件登记于`ledger/source-evidence.csv`。来源`npcentity`成员接入目标canonical标签，复用全局右键、药瓶与蛊罐排除，不写其他系统持久对象。验证及显式生成边界见[本月记录](logs/2026-11.md#2026-11-14-08号飞梭)。

### Q136 飞梭没有已发现的自然生产者

A `Businessman08.mod.json`关闭自然出生和刷怪蛋，Java实际注册MONSTER，与编辑器Creature描述不同，目标按Java。A/B有界文本及409个结构NBT解压扫描未命中08、Businessman08或feisuo生产者，证据为`tmp/next-frozen-evidence/npc08-producer-scan.json`。目标保留注册及显式生成后的完整行为，不补造自然投放、蛋或航线；完整获取入口仍待集中复核，受控生成不能作为生存获取通过。
