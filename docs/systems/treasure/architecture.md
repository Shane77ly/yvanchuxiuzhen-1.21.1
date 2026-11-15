---
topic: treasure
role: architecture
status: partial
updated: 2026-11-14
---

# 宝箱：奖励与开箱

本系统按批准的系统所有权实现奖励与开箱；源码定位采用[系统导航](../README.md)的A/B规则。未实现范围在末节单列，不把静态来源核对作为游戏验收。

## 1.18.2 数据与入口（已读范围）

`Baoxiangzong` 由 `/baoxiangopen` 命令进入，按容器类型（如书架、药架）与类别（书籍堆、符箓堆、丹方、功法书、技能书等）分派到 `Baoxiang0101`、`BaoxiangFulu`、`BaoxiangLiandantuzhi`、`BaoxiangGongfa`、`BaoxiangJineng` 等过程。`TreasureChest11ZaiFangKuaiShangYouJian` 右键时调用 `TreasureChest11FangZhiFangKuaiShi`（按 1–100 随机值分 8 级）并写方块实体 `dakai`。`~/tag/baoxiang` 有 26 个标签。

## 所有权与奖励表

宝箱定义（容器类型、类别、等级与奖励表）归本系统；奖励物品由所属系统的生成入口创建，本系统组合结果。

`tools/porting/extract_treasure_rewards.py`从B的锁定来源递归提取30个纯奖励过程，生成`TreasureRewardTables`及[输入/输出指纹](../../reference/conversion/treasure_reward_tables.json)。保留原有条件顺序、随机上下界、重复项、空分支、不可达分支及`doube`字段拼写，只合并命名空间并将临时等级、随机源改为调用参数。书架6类、药架5类、武器架5类、宝箱8类共24个分派；完整类别见生成脚本的`CATEGORIES`，不另复制一份概率表。

`B:BaoxiangzongProcedure:41–68`先随机次数，再逐次随机奖励；`127–180`按百分号分段、斜线分字段，首段确定承载物品，第二段命名或“无”，余段按char/int/double/doube/logic解释，数值解析失败为0。普通物品在命令位置加`(0.5,1,0.5)`掉落、拾取延迟10刻；指令奖励以加`(0.5,0.5,0.5)`、权限4、隐藏输出的来源执行。装备分支执行`/lianqi`，保留其容器中转限制。

宝箱只解析临时参数，不直接写功法、技能、丹药、丹方、符箓或材料的业务实例。六类由各所有者API构造，公共书既有阅读/研习链消费功法、技能与丹方实例。炼丹API保留有序药效和材料，来源重复键由后写覆盖；技能书使用现有类型化组件，不复制另一份根NBT。

### R11 奖励分派静态核验（2026-11-15）

`tools/porting/check_treasure_reward_dispatch.py`从`extract_treasure_rewards.py`读取唯一的`CATEGORIES`定义，逐项比较目标`TreasureRewardTables.select`的书架6项、药架5项、武器架5项和宝箱8项，共24项分派，并确认24个目标表方法均存在。检查同时按提取器的文本换行语义核对`docs/reference/conversion/treasure_reward_tables.json`登记的30个来源过程SHA-256和目标输出SHA-256；结果为24/24、30/30及输出一致。

目标`TreasureRewards`静态保留六个业务所有者入口：`Techniques.createBook`、`Skills.createBook`、`Fulu.createReward`、`Liandan.createMedicineReward`、`Liandan.createRecipeReward`和`Materials.setInstance`。奖励串空首段或斜线字段不足三段时终止当次发放；指令分支使用权限4、无实体来源和隐藏输出；`double`/来源拼写`doube`、数值失败归零、未知字段忽略及普通`ItemStack`回退均存在。结果报告写入`build/diagnostics/r11-treasure-reward-dispatch/static-parity.json`。

## 01至11宝箱载体

01/02读取自身`TreasureTypeBig/Small`、`TreasureLEVEL`、`minimumvalue/Maximumvalue`，大类非空时立即发奖并扣一件，取整对应来源`DecimalFormat("0")`的半偶规则。来源调用位置先加`(0.5,0.5,0.5)`，奖励层再加其偏移。01使用动画NONE/时长0，02为EAT/32，但发奖均在右键开始时执行。

04/05在原生方块使用入口各抽1–2级、发一本功法/技能书后置空气；06给予玩家1–20枚`money_01`后置空气。原生入口保留物品优先使用与潜行绕过方块交互的时序，不在交互事件前置消费。

03/11均在放置时安排每20刻一次的方块计划刻，`dakai`为真后每次把自身`tick`加1；达到2且状态空时发奖，达到5且状态“爆物品”时移除。03在1–10抽样，1–7为一级妖兽材料、8–10为一级金属材料，次数2–5；先执行奖励再写状态。11无钥匙时动画1；首次有钥匙使用时先抽配置、动画2、写开箱标志并扣一把`treasure_chest_1101`，发奖前先写状态以保持来源顺序。等级、类别、状态和单槽库存随方块实体保存，计划刻沿用原生区块保存链。

11配置来源`A:TreasureChest11FangZhiFangKuaiShiProcedure`：先抽23类别，再抽1–100，阈值98/95/90/82/65/48/25对应8/7/6/5/4/3/2级，其余1级；最低次数1。类别与上限完整保留在`TreasureChestSettings`，宝箱材料类才额外抽1至对应上限；总表支持布匹，但23类随机池没有布匹。11独有含水状态及水源计划刻。03/11各自保留来源几何、动画、纹理，物品显示父链仍均引用11的来源显示设置。

单槽最大堆叠1，六侧与无方向能力共享同一库存；无方向通过来源父类`InvWrapper`保留，目标此前遗漏已按[Q131](#q131-0311无方向库存能力遗漏)补回。来源方块实体的`createMenu`均返回独立三行`ChestMenu`，与单槽不一致，但自然右键只开箱、不打开菜单。目标保留该未使用入口，不新增单槽菜单。

## 来源异常的当前处理

- `B:BaoxiangzongProcedure:127–138`不校验首段长度，空串、材料空分支追加的`%cailiaolevel…`、符箓空分支追加的`0%fululeveltxt…`会在取第三字段时终止当次命令，已掉落结果保留。目标明确识别这些首段不足的来源结果并停止同次发放，不补抽或补造奖励。来源`BaoxiangOpenCommand`自身没有catch；不把这一事实写为过程吞异常。框架错误提示未实机对照。
- `B:BaoxiangLiandantuzhiProcedure`仅一级四个分支为丹方，二级起直接返回技能书；按实际表保留，未重平衡。
- 符箓随机池的重复、空分支及不可达分支原样保留。丹药表中重复`danyaotick0`后写覆盖、缺少`danyaotick1`读取为0；不按名字补改药效时间。
- 部分数值包含“每发”等文字，来源转换失败为0；目标保持，不自动提取文本中的数字。

## 开放范围与验证边界

本批覆盖奖励总表及01/02、03、04/05/06、11自然消费代码；宝匣`baoxia/baoxiaitem`及03/11无方向能力修复已接回。`Tiandaobaoxiang`工具/管理菜单/弹体已接，挥动开页和设置不扣栈的必要单项已通过；完整自然配置与奖励消费仍待。2026-11-15主线串行完成01状态类丹药专项客户端消费：一级辟谷丹、一级避水丹、一级铁肤丹三候选的来源药效/剂数、原生服用、保存重进和服尽返空容器均通过，报告`build/diagnostics/r11-treasuremedicine-20261115-final/report.txt`为DONE、0 FAIL且客户端`ERROR/FATAL=0`。`treasure_chest_gui`孤立入口仍未接回；副本与世界结构生成入口、完整生存获得和全部奖励消费者仍需对应包接回。既有开箱族来源指纹、定向转换、编译、结构检查及1.21.1版本有限必要单项已通过，2026-11-15又完成24分派及奖励解释器静态核验，实际范围与失败夹具只记[本月志](logs/2026-11.md#2026-11-14-奖励总表与01至11开箱族)，静态报告见`build/diagnostics/r11-treasure-reward-dispatch/static-parity.json`。开箱中只检查方块实体序列化，真实保存重进覆盖未开11、已发奖励与已结束箱，不据此认定开箱中断恢复已验。完整24分派消费、空表/畸形表、双手/多次使用、含水和手持渲染、1.18.2版本实机、两版本对照、专用服务器及多人仍未执行；既有成品专项不能代替新增调用链验证。

## 成品命令（2026-11-07）

已读 `B:LianqiCommand` 与 `BaoxiangChengpin`，归属确认为奖励生成而非正式炼器；未写玩家能力，`typed_field_accesses.csv`没有这两个入口的玩家字段访问。`/lianqi`要求权限1，参数按空白拆分，返回0；来源奖励字符串（如`BaoxiangFabao`）会调用该命令，该批当时尚未迁移完整奖励总表；现状见上文奖励与开箱章节。

命令位置向下取整为方块坐标，先读物品能力槽0，非空时整段不执行；原生临时物品命令只支持`Container`，没有可用容器时不改成随地给予。目标经equipment的公开成品构造生成物品，经容器槽0读回副本，在命令原坐标掉落、拾取延迟10刻；物品能力可修改时清槽0。保留来源通过容器中转的限制与顺序。奖励总表现已接回该命令，尚未进行该新增自然调用链的运行验证。

品阶、参数、数值与Q59的唯一正文在[装备](../equipment/architecture.md#宝箱成品与品阶2026-11-07)。构建/ArchUnit及末次六项成品专项通过，专用服务器一次启动和原生stop保存退出通过，ERROR0；证据及运行次序同装备正文。本命、开箱分流、奖励概率与正式炼器均不能据此标记完成。

## 宝匣指令载体（2026-11-14）

`baoxiaitem`点击顶面且上方为空气才放`baoxia`，水平朝向与玩家相反，初始不含水；只复制`zhiling`并扣一件。BE单槽上限64，无方向`InvWrapper`、六面`SidedInvWrapper`与直接Container共用库存，物品、战利品表、指令和开启标记持久化并同步；未使用的三行`ChestMenu`入口保持，不与10槽`treasure_chest_gui`混用。

空指令或已开启无动作；首次非空指令在宝匣整数坐标以权限4、无实体、隐藏输出执行，安排五个全服`ServerTickEvent.Post`后置空气，再给执行后的当前BE写`yikaiqi=true`。队列只在当前服务器会话存在，停止时清除；移除触发库存掉落。`/lianqi`自然命令继续由Treasure校验中转容器，经Equipment公开构造实际成品，未新增另一份装备状态。物品Tooltip接B全局`Baoxialore`的参数预览，保留颜色、顺序、零值判断与来源错字段；附魔名使用原生翻译。

18份来源文件及精确Forge40.1.0/NeoForge21.1.265归档和关键成员SHA见`tmp/next-frozen-evidence/casket-source-evidence.json`，来源文件登记在`ledger/source-evidence.csv`。静态导入、15动态文本键及来源Java硬度已核；运行结果和未验边界见[本月记录](logs/2026-11.md#2026-11-14-宝匣与无方向库存能力)。

### Q131 03/11无方向库存能力遗漏

A `TreasureChest03TileEntity/TreasureChest11TileEntity:175–178`仅在方向非空返回自己的`SidedInvWrapper`，null继续父类。Forge40.1.0精确`BaseContainerBlockEntity.java.patch:8–16`由父类创建`InvWrapper(this)`并返回同库能力，来源并非null无能力。目标旧BE返回null使无方向自动化和`FinishedEquipmentCommand.run`前置查询失效；已新增无方向wrapper，有方向规则保持。此前开箱单项不涉及该能力，不能证明一致；针对性实际插入、六面读回、提取和序列化检查单独登记。

### Q132 宝匣元素硬度与生成Java冲突

A `BaoxiaBlock.java:56`实际`strength(-1,3600000)`，编辑器元素为50/500；目标按生成Java保留负硬度和抗性，内容转换器已支持该负值，不按编辑器覆盖。集中留档，来源实机未执行。

### Q133 护甲预览生命显示错字段

B `BaoxialoreProcedure:68–72`按字段7判断生命增量却显示字段8，与灵力行同值；短指令和附魔参数还按原索引直接访问。目标保持字段和条件，不修数值、不补猜值或吞异常；是否调整由后续集中裁定。

### Q134 宝匣延迟盲移除与保存边界

A `BaoxiaZaiFangKuaiShangYouJianProcedure:69–73`五个END刻后直接置空气，不核对原方块，替换后的方块也会被移除；非空无效命令同样开启。`yikaiqi`持久而队列不持久，开箱中停服不提供自动恢复。目标保持这些规则，真实开箱中保存重进未验，不自行加身份检查或补执行。

### Q135 独立宝箱菜单没有自然入口

A Java有界检索仅发现`treasure_chest_gui`菜单、屏幕及注册引用，没有构造或打开调用；宝匣自然右键直接执行指令，BE另返回三行`ChestMenu`。目标不把10槽菜单强接到宝匣；该来源身份仍待后续完整迁移或裁定，不将孤立入口当已验自然玩法。

## 天道宝箱工具（2026-11-14）

来源A `Tiandaobaoxiangitem`归Treasure，天道只是创造分类。空大类或潜行时左键挥动开176×166零槽设置页，右键只开始BOW/72000刻蓄放；十按钮修改操作时主手的五字段，不绑定打开栈。六大类及其小类、112条转换边、等级1–8循环、最小值减至0而再减回64、最大值1再减回64、上下调后的最大不低于最小，均保来源。背景与箭头坐标不重排，16动态中文键保留；显示默认仅客户端渲染改当前主手，不向服务端回写整栈。

松手服务端检查非潜行且使用栈大类非空后发Arrow，速度2、散布0、基础伤害1、静音、禁拾取，显示材料100。先原生tick再无重力、持久tick加1，服务端20刻或入地时移除；命中生物沿原生箭伤害并抵消插箭计数。无自然调用者的目标生物shoot重载保原速度2/散布12。目标Arrow内部默认pickupItemStack为原生箭以符合非空保存codec，对外getPickupItem仍空且禁拾取。

方块命中按来源逐次从射手当前主手读两字符串三double，写任意BE并五次更新，再发十粒子及四行中文或失败提示；不限物品类型、不保发射配置快照。现有03固定随机表与11保存配置消费者保持；不反写其他系统业务数据。所有五字段为Treasure配置，本族typed_field_accesses不需新增外部玩家能力写接口；菜单请求仅传菜单ID及按钮，由服务端校验当前菜单后操作本系统数据。

52份来源及归档成员SHA见`tmp/hammer-tool-evidence/r14-treasure-tool-source-evidence.json`，source-evidence台账登记。静态转换/资源与必要运行结果分别见本月志，来源实机、全类别自然获取消费、完整视觉、多人和专服未执行。

### Q140 客户端默认与服务端空类别断链

A `TiandaobaoxiangScreen.renderLabels:94–109`是五默认显示过程唯一调用者，只给客户端当前主手写书架/书桶/1/1/3；按钮消息`72–120`只分派，不回传配置，大类上下调只匹配现有非空六类。物品`releaseUsing:50–60`要求服务端大类非空，故空工具不能凭界面默认成为可发射配置。目标保客户端/服务端边界，不搬默认到服务端；静态断链待集中裁定，来源实机未执行。

### Q141 菜单分类与奖励分派不对齐

A小类包含书架的炼器、药架的采药篓/种子包、武器架的综合宝箱，宝箱只有五种外观箱名，还有储物袋和纳戒大类；B `Baoxiangzong:69–125`四大类/24分派并不接受上述多项，宝箱八分派使用材料类别而非外观箱名。目标保持两侧实际来源，不补映射或奖励；夹具种入宝石只验证弹体到现有11消费者，不冒充来源菜单自然配置。

### Q142 设置操作读取当前主手

A十按钮及命中过程`25/35/45/55/65`均读操作当时主手，无物品类型/最初打开栈校验和发射快照。切换普通物品可修改/写出该栈五参数，切空则空/0；目标按来源，不擅自绑定工具。后续集中决定是否调整。

### Q143 弹体空声音标识

A `TiandaobaoxiangitemEntity`两种shoot查询空ResourceLocation声音，没有有效音效ID，弹体自身silent。目标不补造原版箭声；空ID在1.18.2版本的具体异常/日志未实机，当前差异与声音表现仍待核。
