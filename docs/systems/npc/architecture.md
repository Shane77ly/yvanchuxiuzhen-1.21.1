---
topic: npc
role: architecture
status: partial
updated: 2026-11-15
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

`spawn_data.json`保留各实体十三项资质、来源实例字段与本地化键。`LEVELname`为空才初始化，已有`LEVELnumber≥1`时不覆盖等级；先生成等级名，再写生命上限并注册20次服务端END tick回调，继续依来源顺序计算恢复、资源、原生攻击、其余攻防和移速；回调执行时读取最新生命上限并补满。同型友方团队仍由初始化入口写入。01和03注册移速的浮点常量保留为`0.30000000000000004`；其他初始值和延迟重算分别处理。09构造时装备原矿与20号衣甲，10构造时装备31材料与18号衣甲，11初始化时装备19号衣甲。

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

乾坤道人/门徒方块、管理路径及来源库存已接入，当前边界见下文；门徒断链任务和管理命令仍按Q153/Q154开放。固定结构、动态生成与自然生成分别核对，实体内容表和标签存在不等于已在生存世界投放。

本批完成静态源码和内容JSON核对；主线已接资源共享清单与`npcentity`标签，定向转换、编译和6项架构检查通过。普通商人原生右键/随机商店/存档/AI/模型仍待必要实机验证；居民和08飞梭有限1.21.1版本证据见下文及月志，不扩展为普通商人通过。1.18.2版本实机、多人和两版本对照均未执行；已有银行单步验收不能覆盖这些新增消费者。

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

## 阴司五NPC（2026-11-14）

`NpcUnderworldCreatures`独立装配`yanwang`、`wuchangbai`、`wuchanghei`、`niutou`、`mamian`及五枚来源刷怪蛋，不经过商人档案初始化。阎王使用静止路径，来源属性100生命、3攻击、0.3移动速度，初生时朝向清零并向X偏移0.5格，死亡20刻移除；黑白无常使用四守卫共用近战/受击反击目标，属性同为100生命、3攻击、0.3移动速度，死亡20刻移除，白无常产生云粒子、黑无常产生烟粒子；牛头、马面各10生命、3攻击、0.3移动速度，保留水下呼吸和120刻死亡时序。实体不因距离消失，刷怪蛋颜色、资源与注册分类按来源记录。只有牛头、马面进入`undead`标签，未把其他三者按外观归为亡灵。

五实体来源资源指纹见`ledger/source-evidence.csv`，定向转换输入快照及API核对见`tmp/underworld-evidence/`。Q147所列两条缺失骨骼引用按来源保留；不补模型骨骼或改写动画。npc_underworld必要单项已通过，覆盖五类实体注册、实例初生/死亡时序、刷怪蛋指向和渲染器加载；截图只证明技术加载，不构成模型或动画视觉认可。显式刷怪蛋和受控生成不等于自然生成验收。该族没有发现内置任务脚本生产者，不为实体新增Quest写入接口。运行原件见[本月记录](logs/2026-11.md#2026-11-14-阴司五实体与刷怪蛋)。

### Q147 阴司动画引用不存在的模型骨骼

A `animations/yanwang.animation.json`的`animation.yanwang.idle`引用`bai`骨骼，A `geo/yanwang.geo.json`没有该骨骼；A `animations/mamian.animation.json`的`animation.mamian.death`引用`gangcha`，A `geo/mamian.geo.json`没有该骨骼。目标资源按来源保持，待集中决定是否接受缺失通道，不人工补骨骼或删动画轨道；来源表现与目标实际渲染尚未验收。

### Q149 管理工具档案消费者覆盖范围

A `RightClickEntityProcedure:188–473`允许工具复制到任意非玩家`Mob`，写入等级、13项资质、任务脚本、商品、装备与名称，并调用`NpcshuxingjisuanProcedure`。目标`NpcProfiles.apply`对任意`Mob`保留名称、装备和粒子；`applyOwnedProfile`已覆盖NPC所有者的五个基类`NpcMob`、`NpcResident`、`NpcFlyingShuttle`、`NpcUnderworldGuard`和`NpcUnderworldJudge`，补齐阴司五实体；本批再为带原生`ATTACK_DAMAGE`的其他自定义`Mob`接入同一档案写入与资质计算。缺少该属性的原版`Mob`仍只接收姓名和装备，保留Q159的来源异常边界。跨所有者补齐需由Flow编排和各所有者公开操作承接，不能直接订阅形成npc→economy→equipment→guchong→npc或npc→skill/world→npc循环。其他数据所有者和无攻击属性生物的完整覆盖仍属Q149开放范围。

NPC所有者按来源顺序写脚本/商品、六槽装备、等级/资质、固定标记、等级名称和属性；事件持有与工具库存隔离的装备快照，未被所有者或通用自定义Mob分支接收的生物仍由入口交付原生装备。`NpcStats`在写生命上限后立即注册20次END回调，先更新原生攻击再计算其余攻防与移速；回调读取执行时最新生命上限，不增加存活或移除门控。四个代表实体的小数等级、十三项混合资质、属性及真实保存重进已通过有限单项，两个旧客户端粒子计数检查仍失败，详细边界见[本月记录](logs/2026-11.md#2026-11-15-npc管理赋档与保存重进)。

### Q159 管理工具对缺少攻击属性的Mob发生异常

A `RightClickEntityProcedure:188–473`允许任意非玩家Mob进入管理赋档，`:472`先更新等级名称、`:473`再调用`NpcshuxingjisuanProcedure`。后者`:27`先写`health_Max`，`:28–54`立即注册20次服务器END tick的回调，随后同步计算恢复、资源和攻击；`:126`写`attack_Num`，`:127`直接访问`ATTACK_DAMAGE`，没有属性存在检查。此处异常会截断后续防御、法术、神识及速度计算；之前的字段写入和回调注册已发生。回调在执行时重新读取最新`health_Max`，设置最大生命并补满，没有存活、移除或世界归属门控，多次调用也不合并。

两来源工程全域检索未发现给普通Mob注入攻击属性；A `YvanchuxiuzhenModAttributes:34–93`只向指定自定义实体加入皮肤与大小属性。精确缓存`forge-1.18.2-40.1.0-sources.jar`和其userdev指定的`joined-1.18.2-20220404.173914-srg.jar`只读核对显示，Cow→Mob→LivingEntity默认属性不含`ATTACK_DAMAGE`，`AttributeSupplier.createInstance`遇到缺失属性返回null，Forge40.1.0补丁未补此属性。因此两来源配合该版本默认配置时，牛会在上述`:127`空指针，可由静态证据确认。

当前状态为来源缺陷待集中裁定；不擅自给普通生物补攻击属性、增加判空或改变失败后的部分写入语义。目标其他所有者的档案交付缺口仍属Q149，不将其现有未消费状态当作已保真复现本异常。来源文件指纹见`build/diagnostics/r13-npc-management-20261115/source-audit.json`；1.18.2版本实机及异常后的服务器tick生命周期未执行，静态回调已注册不等于实机已完成回调。

### Q150 管理工具潜行提示与实际入口不一致

A `TiandaoNPCsetitemItem.appendHoverText`提示潜行右键打开面板，但同类`use`入口在右键时直接调用`NetworkHooks.openGui`，未检查潜行；目标`NpcManagementItem.use`同样未检查潜行。目标语言提示已改为普通右键，与实际入口一致，没有新增潜行门控。来源提示是否表达了未实现的原意仍待统一裁定；管理Smoke已执行，首轮与复跑边界见本月记录，未将该来源门控疑点判为已裁定。

### Q151 管理工具02界面没有可达入口

A `TIANDAO_NP_CSET_02`在`YvanchuxiuzhenModMenus`和`YvanchuxiuzhenModScreens`注册，但除定义、注册与屏幕绑定外，来源静态索引未找到打开调用。`TiandaoNPCset02Menu`的内部容器容量为0，菜单只添加玩家背包；屏幕只有`chouqvnum`文本框，没有按钮、按键分派或网络消息。目标保留已确认可达的`tiandao_np_cset`管理面板，没有接入该02菜单，也不猜测抽取次数字段的消费者；将它作为来源注册但不可达的项目保留，待集中判断其身份应如何处理。


## 乾坤道人与门徒方块（2026-11-15）

两个方块、两个物品、两个方块实体及门徒菜单实际已在`b938271d`集成，本批复核后补齐菜单归属阅读和七条台账状态，不再应用旧worktree冻结补丁。来源依据为A `block/QiankundaorenBlock.java`、`QiankunmentuBlock.java`、对应`block/entity/*TileEntity.java`、`world/inventory/QiankunmentuGuiMenu.java`、`client/gui/QiankunmentuGuiScreen.java`及下列过程。自然放置、获取与完整模型动画对照仍需另验。

道人任意普通右键在`<玩家UUID>zhuangtai`为空时置动画1、`yanwubaofa=true`及“准备”；同一玩家已有状态时不重启。方块每两游戏刻执行一次烟雾过程，每次发送10个SMOKE与10个CLOUD，`yanwutick`以double递增；达到54后先关闭再清零。门徒仅创意潜行右键打开176×166设置页，菜单自身无槽且持续有效，0号按钮将同一方块实体的double字段`renwuset`加1，达到10回绕0；按来源保留先同步增量、再同步回绕的顺序。编号动态文本与加号使用最终合并语言键。

道人暴露零槽物品能力；门徒暴露六面及无方向共用的一槽库存，单栈上限1，菜单不展示该槽。方块实体持有编号、按玩家状态、烟雾状态及库存，服务端写入、客户端经方块实体更新读取；持久数据与物品实例组件随方块实体保存。

Q119：道人首次右键只保存「准备」，不打开任务；已「准备」的后续右键发布`QiankunDialogueRequestEvent`。这保留1.18.2版本全局右键事件先读取已有状态、随后方块交互初始化的顺序。事件不传脚本、不写玩家任务附件；[任务系统](../quest/architecture.md#q119-初见乾坤道人)处理对话、发盘及完成状态，烟雾和门徒路径不变。

### Q155 道人烟雾逐次状态同步遗漏

A `QiankundaorenGengXinYouXiKeProcedure`在每次计数递增后调用`sendBlockUpdated`，结束时分别同步关闭和清零。目标先前只调用`setChanged`，中间计数没有交付客户端；本批按来源补齐三处更新顺序。这是确认的目标遗漏，必要客户端单项已确认自然周期、中间计数与结束状态同步；真实保存重进保留UUID状态与已关闭计数，结果见本月记录。

### Q156 乾坤方块贴图目录没有进入目标图集

先前直接复制到`textures/blocks`并保留模型粒子`blocks/qiankun*`引用，目标1.21.1方块图集缺失这两个sprite，运行日志已出现警告。本批把七条内容台账接入既有`convert_assets.py --owner npc`，由转换器统一产出`textures/block`并重写粒子模型，Geo方块及物品渲染器同步使用该路径。几何、动画和菜单背景进入唯一转换清单；旧目录副本移除。转换84项、缺失0，必要客户端单项中这两类图集缺失警告消失，截图可见道人侧面与门徒台座/金块；该视角不代表完整模型或动画对照通过。

### Q157 门徒任务编号翻译参数中断颜色

1.18.2版本`MentuRenwuweizhiProcedure`先拼出完整的“§6第<编号>个任务”字符串，再交给字体绘制。本批首次将编号迁入翻译组件后，截图显示只有“第”字为金色，其余参数及后缀回到默认色；这属于本批引入的目标表现差异。目标改为先展开翻译组件的完整字符串，再按来源字符串绘制，继续保留语言键及来源颜色范围；修复后再次串行运行必要单项，截图已复看，编号整段金色与来源一致。

### 乾坤道人与门徒来源断链

QiankunmentuZaiFangKuaiShangYouJian生成Java的事件根只有创造模式潜行右键打开管理界面；Blockly XML中另有8条任务脚本及0/9分支，但它们是未连接到event_trigger的根级controls_if，生成代码不会执行任务提交。目标按生成Java的实际可执行行为接入方块、方块实体、管理界面与renwuset 0–9回绕，不凭未连接脚本补造普通右键任务入口。来源另有/renwuset管理命令把同名字段写成字符串，并从命令源位置计算未消费的射线坐标；该命令不在当前管理包范围内，保留为Q154待裁定。证据为来源elements/QiankunmentuZaiFangKuaiShangYouJian.mod.json与生成Java文件，生成Java的可达管理入口已纳入本批必要单项；脱接脚本与命令仍待集中裁定。

## 剧情跟随消费者（2026-11-15）

`NpcFollowers`拥有gensuiplayer/gensuiplayeruid，允许来源脚本向任意Entity登记；`NpcFollowerRuntime`只在服务端LivingEntity的Pre tick消费，gensuitick每20刻清零，Mob只查询当前维度玩家UUID并向玩家XZ各±2的随机坐标寻路，Y相同、速度1。不增加主人在线跨维查找或瞬移兜底；取消只清标志和UUID。精确Forge40.1.0 LivingEntity补丁确认LivingUpdate位于tick开头，未因目标Post便利改变来源时序。本批静态接线，完整自然跟随与多人保存仍待。
