---
topic: cooking
role: architecture
status: partial
updated: 2026-11-10
---

# 烹饪：架构调查与提案

来源行为、已实现边界和未完成范围分别陈述；源码定位采用 [系统导航](../README.md)的 A/B 规则。

## 1.18.2 数据与入口

`B:CookbeginProcedure:35–194` 根据食谱类型、厨具、职业和容器检查 9 个材料位置；蒸锅还检查上方最多 4 层笼屉，各有 cooking 状态。`A:FoodmuzengpeifangProcedure` 属于另一器具路径。

食谱材料键为 `cailiao00…08`，与炼器图纸的另一种编号字符串分别记录。锅具、食谱与食物结果必须一起迁移。

## 目标所有权与接口提案

厨具状态、菜谱与食物实例效果归本系统；职业进度归 profession，材料由 material 提供。生产入口按具体器具实现预览、开始、推进和完成，复杂蒸笼多块联动保留专用流程。成品保存食用、饮酒等实例效果，调用所属系统的操作生效。

## 验收

各设备槽数、燃料/火源、容器、消耗与产物不混用；拆除、取出和卸载恢复有自己的规则。

## 辟谷效果依赖（2026-11-08 已实现）

炼丹药草目录引用的 `yvanchuxiuzhen:effect_031` 归 cooking。已读取 `A:Effect031MobEffect:11–28` 和 `Effect031ZaiXiaoGuoChiXuShiMeiKeFaSheng:6–18`：效果为 BENEFICIAL，颜色 -13210，每刻处理，与等级无关。玩家饥饿值低于20时，实体持久 `baoshidu` 加一；累计至少15即将饥饿值加一、计时归零，不改饱和度。饱食时暂停计时，开始/移除效果不清计时；非玩家按饥饿值0继续计时和清零，不执行玩家食物写入。

1.21.1版本通过既有 `ContentTables` 和 `FastingEffect` 在服务端推进。炼丹沿用原版效果命令施加状态，烹饪独占计时和食物写入，不形成炼丹直接写烹饪数据的接口。已读精确 NeoForge 21.1.265 `FoodData#setFoodLevel`，该操作仅写饥饿值。原生玩家同步承担客户端显示。本批构建与运行证据见[本月记录](logs/2026-11.md)，不据此标记其他烹饪玩法已实现。

## 两锅与墨料坯（2026-11-09 部分实现）

来源 A 的铁锅与砂锅有序合成配方已由 `convert_data.py` 转成 `data/yvanchuxiuzhen/recipe`，输入指纹与目标文件见 `docs/reference/conversion/data_manifest.csv`。来源锅具菜单为14槽：九个食材槽、四个容器槽、一个菜谱槽；目标方块实体负责库存持久化、更新同步、自动化能力、拆除掉落和比较器。该项只闭合锅具本身的合成获取，不闭合食材、食谱与成品链。

无菜谱时，铁锅和砂锅均可尝试生产墨料坯。过程扫描九个食材槽，以 `forge:cailiao` 槽位的材料实例等级算术平均值分档：11/21/31 分别升至玄/地/天，其余为黄；须有壳或骨、皮或鳞、牙或爪三类材料，以及至少三份烟灰和朱砂。开始时每类材料各扣一个，粉末各扣三；十个服务端刻后按装置坐标查找锅具能力并覆盖当时槽0。提示文案写“同级”，与实际均级算法不完全一致，目标保留来源过程的算法。五次右键完成烹饪，经验归最后一次操作玩家；自然计时、结构拆除和存档行为尚未专项游戏验收。

两锅菜谱执行读取菜谱物品的 `CUSTOM_DATA` 字段，核对器具、职业等级、容器、材料与热源，开始后扣料并按五次右键完成。蒸锅/笼屉与普通菜肴消费于2026-11-10静态接入，见下文；来源全部菜谱与食材自然获取、完整玩家界面验收仍未闭合。静态对应见[烹饪源码证据](ledger/source-evidence.csv)，运行边界见[本月记录](logs/2026-11.md)。

## 木甑、酒与陈酿（2026-11-10 静态接入）

正式接入14个Java实现与突破后每秒事件，覆盖木甑、八酒、八陈酿坛和天然猴儿酒块共38条注册身份。来源与精确目标源码、39个外部字段访问已静态核对；转换、构建、木甑/陈酿/饮用/酒精客户端关键场景及专服启动保存退出通过，具体证据与受控计划刻边界见月志。下文说明已实现规则及未闭合自然入口。

### 木甑自然入口、库存、菜单

A:block/FoodmuzengBlock.java全文：木声、硬度2/抗性10、水平朝向、非水浸、轮廓(1,0,1)→(15,14,15)、lightBlock0、透天光、不导红石、视觉形状空、燃烧与蔓延5。自然任意右键服务端打开FoodmuzengGuiMenu，无潜行分支；提供者同样打开该真实菜单。正常loot为空时掉自身；拆成另一方块dropContents五槽并更新比较器；同方块状态变化不spill。菜单与BE完整更新包/Items/loot table保存。

A:block/entity/FoodmuzengBlockEntity.java：5槽、max64；六面全部5槽可见，插入仅0..3、取出仅4；null能力由精确Forge40.1.0 BaseContainer补丁提供InvWrapper，可直接强制写4。source setItem只按容器64上限，不按item max截量。自然生产不需新增职业/精力/容器/水条件。

A:world/inventory/FoodmuzengGuiMenu.java：槽0=(41,17)、1=(59,17)、2=(41,35)、3=(59,35)、结果4=(114,26)，另36玩家库存；0..3任意手放取，4只取。stillValid恒true，无距离门槛。shift流程与来源其余MCreator菜单相同，已有栈合并额外检查mayPlace；机器向玩家逆序，玩家先机器、失败回退主包/快捷栏。removed只清未绑定临时容器，真实BE不清。

A:network/FoodmuzengGuiButtonMessage.java：按钮0开始蒸馏Muzengzhengliu，按钮1聊天打印普通配方Foodmuzengpeifang。来源坐标packet只检查hasChunkAt；目标采用当前菜单/BE身份验证，保留无距离行为。目标沿既有自定义payload路径处理按钮，保留来源旁观者按钮行为；原生1.21.1按钮packet会拒绝旁观者。

### 木甑八配方与开始顺序

A:MuzengzhengliuProcedure.java全文。先要求正下方方块有BooleanProperty名lit且值true，不要求指定方块/tag。因此任意具有lit=true的方块都通过；来源提示“熔炉或篝火”等只是例子，不可实现成白名单。无热源深红聊天“木甑下方要有正在燃烧的熔炉或篝火等方块。”；有热源但begin=true则普通聊天“正在酿酒中，请稍后”。无匹配配方静默。

空闲时读取输入0..3各槽的物品身份，按**占用槽数**计数，不按栈count。一个64栈只记一，四槽各1和各64匹配相同；空槽也以air登记但不匹配下面配方。位置顺序无关。结果4必须为空或同种酒，不检查剩余容量。按下面顺序第一个匹配写jiuniangid、begin=true，再依次每个输入槽shrink1；保留其余栈与组件。开始不清jindu、不写产物，busy不重排/不扣料。

| 顺序/输出 | 0..3四槽身份分布 | 酒名 |
| --- | --- | --- |
| jiuniang_001 | shicai_002三槽 + ginseng一槽 | 状元红 |
| jiuniang_002 | shicai_002四槽 | 女儿红 |
| jiuniang_003 | minecraft:wheat两槽 + shicai_005两槽 | 杜康酒 |
| jiuniang_004 | jiuniang_001一槽 + fragranceflower三槽 | 鸿茅药酒 |
| jiuniang_005 | shicai_032四槽 | 地瓜烧 |
| jiuniang_006 | peachpeach四槽 | 猴儿酒 |
| jiuniang_007 | jiuniang_001一槽 + peachflower三槽 | 桃花酿 |
| jiuniang_008 | jiuniang_001一槽 + xvejing三槽 | 碧血琼浆 |

这里ID均yvanchuxiuzhen，minecraft:wheat例外。起始用jiuniang_001作原料时不继承其agenum。帮助按钮只打印标题“普通酒水配方”（金色）和001、002、003、005的四行文本；004/006/007/008实际有配方但不在帮助文本，不补造完整帮助。

### 木甑周期、完成与屏幕

A:FoodmuzengBlock.onPlace/tick：放置排20刻，tick调用FoodmuzengGengXinYouXiKe后继续排20刻，空闲仍排。方块状态更新会调用onPlace，应保留来源排程，不能仅不同block时排。生产过程仅begin=true且下方lit=true时发CAMPFIRE_COSY_SMOKE4（设备x+.5,y+1,z+.5、偏移.5/.5/.5、speed0），jindu+=1并同步；jindu>=100先归零，再用已保存jiuniangid创建默认酒栈，以**当时槽4数量+1覆盖槽4**，最后begin=false并同步。不再次查原料、结果身份或容量，不继承旧结果agenum；移走或换结果可改变最终累加基数。64满时仍扣原料，完成setItem截64。断热暂停begin/jindu、不取消不退款；热源恢复继续。正常从零启动完成需第100次既有20刻周期，约1980..2000刻，不是固定从按钮后2000刻。加载保存begin/jindu/jiuniangid，不能另起不持久异步任务。

A:client/gui/FoodmuzengGuiScreen.java：176×166背景foodmuzeng_gui.png，muzeng.png以(-9,-29)画194×112。进度纹理muzengjindu1..10.png均在(-7,-27)、190×8，分别当jindu>=5/15/25/35/45/55/65/75/85/95叠画，并非互斥；满时tick归零不画。无标题或文字。按钮0在(87,20)、18×27、atlas/imagebutton_pengrenbegin01.png18×54；按钮1在(22,27)、12×12、imagebutton_bankgui10.png12×24。资源必须按原screen全量提取，不能只两张背景。

来源自然合成foodmuzenghecheng为shapeless foodzhengguo+竹子→木甑1，本批已转换；蒸锅/笼屉自然配方同时接回并由客户端确认加载。其模型block/foodmuzeng.json父custom/jiuniangtianguo，纹理blocks/jiuniangtianguo；物品父block/foodmuzeng，均普通模型cutout，不用同名纹理猜测。

### 酒物品放置与陈酿

A:item/Jiuniang001Item至008Item完整三回调均共用001三个过程。8种酒stacks64、COMMON、nutrition0、alwaysEat，saturationMod通常2，006为1（nutrition0时无额外饱和量）。自然use调用super.use后把返回手中物品ID/agenum复制进玩家体质快照jiuniangid/agenum；finish先super完成食用再按该**共享开始快照**结算，不直接读finish物品。不要悄悄改成完成时读当前栈来消除来源共享快照行为。普通原版食物消耗/创造规则由super承担，无返容器。

A:Jiuniang001DangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhi：useOn始终SUCCESS；仅点击UP且正上方isEmptyBlock才放置对应XXX01.defaultBlockState，随后改facing为玩家朝向反向；复制手中完整栈组件但count1写新BE槽0，最后手栈shrink1；自然创造模式的ServerPlayerGameMode在useOn返回后恢复原count，因此最终不净扣。不是普通BlockItem的可替换位置/生存规则；不要求潜行，不放水浸快照。来源8个XXX01注册block item仍独立存在，直接放block item初始BE为空，不能删身份或把它自动初始化成酒。

A:Jiuniang00101GengXinYouXiKe：8酒坛onPlace排1200，tick先处理再继续排1200。每次copy槽0，物品NBT agenum+=1，强制count1写回，随后服务端BE TileData.agenum=0并同步。年份真值在**槽0物品agenum**，BE同名字段每次归零，不得读错。无环境要求、上限、年限状态或配方；1200刻=1分钟游戏刻时间，不是真实游戏一日，不按离线墙钟补年。

A:Jiuniang00101ZaiFangKuaiShangYouJian：普通右键读槽0年份，<1普通聊天“新酿的酒。”，否则DecimalFormat("0")+“年陈酿。”。潜行时把槽0复制count1给背包，再清槽0为空，再把方块置AIR；该顺序避免onRemove再spill同一酒。背包满走ItemHandlerHelper原规则掉物。没有GUI自然入口。

8个酒坛BE均RandomizableContainer/WorldlyContainer，只有1槽、max1，Items/loot table和完整更新保存。六面都暴露0但禁止插/取；无方向InvWrapper可读、extract槽0、modifiable强写仍有效（插入仍因canPlaceItem=false拒绝），不可一刀切封死无方向。提供者ChestMenu.threeRows是独立空箱，不绑定酒槽；自然右键不打开。拆换另一block时spill槽0及更新比较器，fallback方块loot为AIR；自然挖掉应掉**带年份酒**而非酒坛block item。creative pick返回对应未陈酿酒物品，年份不复制。酒坛正常比较器由一槽库存计算。

| 酒坛 | 水浸属性 | 轮廓(单位1/16) | 额外 |
| --- | --- | --- | --- |
| 00101 | 无 | (1,0,1)→(15,14,15)+(5,14,5)→(11,16,11) | GLASS |
| 00201 | 有 | 同001 | GLASS |
| 00301 | 有 | (4,0,4)→(12,16,12) | GLASS |
| 00401 | 有 | (2,0,2)→(14,10,14)+(5,10,5)→(11,13,11) | GLASS |
| 00501 | 有 | (4,0,4)→(12,8,12) | GLASS |
| 00601 | 有 | (4,0,4)→(12,14,12) | WOOD、dynamicShape/XZ偏移 |
| 00701 | 有 | (4,0,4)→(12,8,12)+(6,8,6)→(10,17,10) | GLASS |
| 00801 | 有 | (4,0,4)→(12,9,12) | GLASS红色、dynamicShape/XZ偏移 |

全部硬度1/抗性10、noOcclusion、light0、视觉空、不导红石，火焰可燃/蔓延20。002..008的水浸与neighbor流体tick要保留；普通酒item useOn放default state不自动含水。

### 酒坛爆炸与猴儿酒自然块

8个XXX01方块**全部**wasExploded调用Jiuniang00201DangFangKuaiBeiBaoZhaPoPiShi，不止002。全部onProjectileHit同样调用另一个002过程，只有弹体entity.isOnFire时触发。共同结果：服务端中心x+.5,y+.5,z+.5发LAVA20、speed1；中心x+.5,y,z+.5半径4、null来源、旧BlockInteraction.BREAK爆炸，之后按x/y/z嵌套循环对以原pos为中心每轴-3..3共343位置直接setBlock(FIRE,3)，**会覆盖实心块/容器，不只是空气随机点火，不检查生存或火焰游戏规则**。来源行为应明确记录而非重平衡。目标精确NeoForge21.1.265仍有Block.wasExploded，BlockBehaviour.onExplosionHit走state.onBlockExploded后调用，已核精确NeoForge IBlockExtension.onBlockExploded:739–741先setBlock(AIR,3)后wasExploded；来源1.18.2 Explosion.finalizeExplosion:251只在旧DESTROY加EXPLOSION_RADIUS、BREAK不加，与目标BlockBehaviour.onExplosionHit仅DESTROY_WITH_DECAY加该参数对照，因此旧BREAK对应目标DESTROY（保留正常掉落），不是DESTROY_WITH_DECAY，实测连锁/掉物/火域不能以被取消的爆炸事件代替验收。

jiuniang_00602为另一无BE猴儿酒自然块，任何右键给默认jiuniang_006一件后置AIR，无潜行/年限，普通挖掘loot空时也默认酒1。STONE、硬度5/抗性20、要求正确镐/最低木、水平水浸、轮廓(1,0,1)→(15,6,15)、燃烧/蔓延5。不能与00601一槽陈酿设备合并。来源Java除自身外还由结构模板提供：压缩NBT全量扫描A149/B12仅A:chuancheng02命中，精确解码确认17×14×20模板palette18在(5,12,15)放1个西向、不含水天然猴儿酒块。A:SealreleaseGoProcedure:352–358在“天狼王传承地”分支于(-566,78,-771)放模板；A:YvanchuGoProcedure:536–537由unlock sealrelease子命令调用，A:Setjiegou01Procedure:24–32通过结构方块数据playername/jiegouname触发该命令。此为明确结构来源，不应写无调用；结构解锁、正式世界映射与实际地图获取仍待world/structure批次，当前不能宣称天然获取闭合。

### 饮用数值、酒精与外部接口

A:Jiuniang001WanJiaWanChengShiYongWuPinShi全文。age=共享体质快照agenum，基础duration=400+20*age。按最高满足阈值2560/1280/640/320/160/80/40/20/10→amplifier9/8/7/6/5/4/3/2/1，并分别duration减12800/6400/3200/1600/800/400/200/100/50；age<10 amp0不减。不是duration每档固定，也不把amp当显示等级减一。传入MobEffectInstance是Java(int)截断。

| 酒 | jiujing增加 | 待吸收修为增加 | 效果（duration/amp采用上式） |
| --- | ---: | ---: | --- |
| 001 | 18 | 5 | effect_010 |
| 002 | 50 | 5 | LUCK + WATER_BREATHING |
| 003 | 70 | 5 | effect_017 |
| 004 | 35 | 20 | REGENERATION + effect_010 + effect_017 |
| 005 | 50 | 5 | effect_031 |
| 006 | 10 | 15 | 无 |
| 007 | 20 | 5 | effect_025 |
| 008 | 0 | 条件40/否则0 | 主修功法名为御血术/汲血功/地血大法/血魔祭魂秘法时effect_013(duration,amp0)并移除effect_004；否则duyao_08(duration×2,本档amp) |

所有8种在该分支之后都计算耐酒：threshold=100+AlcoholPatience，再加threshold*AlcoholPatiencePercentage（键为0等价不加）。jiujing>=threshold时添加CONFUSION，duration=floor(12*jiujing)、amp0，读**加酒精之后**值。不设上限、不主动清旧反胃。008不增加酒精仍执行阈值判断。

A:PlayerTickProcedure:318–334酒精自然代谢在dayTime>=tizhi.jishiqi+20的每秒段内；jiujing>=1才计算delta=-1+AlcoholDigestion+100*AlcoholDigestionPercentage并相加，不clamp0，也不是每游戏tick扣1或按当前酒精百分比。time倒退行为由既有CultivationTick承担，来源酒精代谢在修为吸收和突破之后、冷却之前。目标已接入公开CultivationAfterBreakthroughEvent，保留该顺序；较早的CultivationSecondEvent发生在吸收前，不承载酒精代谢。

A:GuanxiangProcedure:78–90在观想时jiujing>=1先提示“体内酒精已被全部炼化。”，再清0并移除CONFUSION。当前目标观想自然入口未接回；Alcohol.refine已实现该操作，但尚无观想自然调用。酒精自然每秒代谢已接入。jiujing按批准系统设计由cooking持久Double附件拥有，Alcohol提供查询、增减和清除操作；后续xiulian观想经事件或公开接口交互，不直接改附件。原tizhi的AlcoholPatience/Percentage、AlcoholDigestion/Percentage及use时快照经Cultivation公开tizhi接口，experience_Stand经Cultivation.add(EXPERIENCE_STAND)，主修功法经Techniques.main；禁止直接访问其他系统附件。现有combat效果010/017及duyao08已有行为，effect031烹饪已实现，013/025现有消费者已有。

typed_field_accesses.csv中Jiuniang及Muzeng相关共39行：tizhi读2/写1，jiujing读9/写7，experience_Stand读8/写8，gongfa读4，全部已在cooking README公开操作与边界登记，并完成逐行独立静态复审。已核A:network/YvanchuxiuzhenModVariables.java：jiujing默认0（598）、NBT写772/读943、客户端同步1133；PlayerCloneEvent的216行无条件clone.jiujing=original.jiujing，233行之后才进入!isWasDeath条件。故酒精应保存、同步并在普通死亡和非死亡clone均复制；不得默认清零。共享tizhi快照也在218行无条件复制。来源finish没有清共享jiuniangid/agenum。

本批来源证据见[源码证据](ledger/source-evidence.csv)，目标客户端关键交互、专服保存退出结果与未完成项见[月度日志](logs/2026-11.md)。1.18.2版本实机、完整目标自然生命周期、多人、完整视觉与两版本对照未执行。

## 蒸锅与笼屉（2026-11-10 静态接入）

正式接入12个Java及共享工厂、注册、资源和网络装配，覆盖两种方块、物品、方块实体和菜单共8条注册。来源为A两设备及其菜单、点击过程、模型和配方，B全局右键的蒸锅分支、Cookbegin蒸锅分支、蒸锅菜单与指南；已完成独立静态复审。纠正zhengguo背景来源命名空间后资源转换13411项、既有missing19、dropped91，比基线13382项净增29；data1703项。构建、客户端关键交互及专服启动保存退出通过，具体覆盖与未验范围见本节结尾和月志。

### 自然入口、库存和获取

`B:PlayerRightClickBlockProcedure:130`在同usedItemHand的自然右键事件中为未烹饪蒸锅打开真实14槽菜单，不取消随后原生方块或物品使用。持物潜行可跳过原生block.use而仍触发该全局开窗，因此两个入口分别保留。随后`A:FoodzhengguoZaiFangKuaiShangYouJianProcedure:24`写当前玩家厨艺等级；空闲时按正上方是否空播放1或2动画。全局过程所有者仍为flow，本系统只核读并实现其中蒸锅分支。

`A:FoodlongtiZaiFangKuaiShangYouJianProcedure:26`先按上方空位播放动画，再向下扫描1–4格；任意忙碌蒸锅会阻止自然开窗，空隙和其他方块不阻断扫描，没有连续支撑要求。笼屉默认BE提供者直接绑定真实九槽菜单，旁观路径可绕过该自然扫描；蒸锅默认BE提供者则为独立空ChestMenu.threeRows，不等同于全局自然开窗。

蒸锅14槽、容器上限64；笼屉9槽、上限1。两者六面全部槽可插取，无方向InvWrapper保留全部库存。强制setItem只按容器上限截量；客户端菜单镜像同样覆盖按物品查询的容量，避免目标SimpleContainer再按物品自身上限截小。菜单恒有效；槽4可插取，蒸锅9–12分别按碗/瓷盘/大碗/铁盘身份过滤，13不筛选菜谱。关闭真实绑定菜单不清库存，快捷移动按来源mayPlace合并规则。换成另一方块时掉库存并更新比较器；同块动画变化不撒库存。Items/loot table、持久字段与完整更新标签沿来源保存，loadAllItems不因读到LootTable而跳过。

两设备均横向反放、无含水、全立方碰撞、noOcclusion、透天光、光阻0、不导红石。蒸锅金属材质木声、强度2/20；笼屉木材质木声、1/20。自然配方为四角竹子、四边木棍、中心干草块出笼屉1；笼屉加炼药锅无序出蒸锅1。木甑以蒸锅加竹子为后续配方，设备获取关系不代替食材或菜谱获取。

### 菜谱开始、双扫描与五次完成

`B:CookbeginProcedure`先验busy、菜谱非空、MenuType为蒸锅、品阶不高于记录厨艺等级以及盛装容器；未知非空容器静默失败，无菜谱不走墨料坯。九原料字段只检查0–8是否有同身份物品，空字段也通过，重复身份可由同一槽满足，不按数量预留。容器只检查不消耗，运行身份匹配保持来源大小写敏感。

主锅九项分数先计算，但尚未据此失败或检查热源时，第一轮已向上扫描1–4层连续笼屉，遇非笼屉break。层匹配分跨层累计不重置，空字段也加分，累计至少9只将当前层cooking置true，不清旧标记。例如菜谱两必需与七空，第一层仅一必需得8，第二层全空再得7，第二层仍被标记。随后主锅缺材或缺热失败也不回滚标记，保留来源异常。

热源为已点燃营火/灵魂营火/熔炉/烟熏炉，或熔岩、火、灵魂火、岩浆块、熔岩炼药锅，或目标blockfire标签；与木甑任意BooleanProperty lit=true规则分别实现。成功后写主锅cooking、关窗、记录cookanimation，逐字段从当前首个匹配槽扣1，重复扣到无匹配时跳过；Menuid转小写后构建默认成品1，立即覆盖槽4，不检查原输出身份/容量，不继承组件。

第二轮固定检查上1–4格，非笼屉或未标记层仅continue，可以跨空层处理此前失败留下的旧标记。每标记层按当前主锅菜谱扣料、覆盖同一默认成品、累计主锅belongToLevel整数平方加5经验，随后清该层标记；主锅cookexp为本锅同式加各层累计。这里不读取笼屉自身等级，不增加等待时长。

主锅忙碌自然点击只加cooknumber，满5发CLOUD15和CAMPFIRE_SIGNAL_SMOKE5，清cooking/cooknumber/cookanimation，仅当cookexp至少1时给最后点击者加经验并清cookexp。不复验热源，忙点击不额外播放动画。typed表物理行1310–1312的三个外部访问分别通过Professions.level和Professions.addExperience完成；设备内部不直接改职业附件。

### 表现与未验范围

两设备各自geo和animation共用来源foodzhengguo纹理，方块/物品采用半透明Geo渲染；0循环，非0单次播放复位，物品不主动选动画。状态0–3而资源只有0/1/2的来源情况保留。蒸锅500×500原画布、14设备槽和两按钮；笼屉176×166，使用longtiui及四条shipintaigui05，未误用未调用的longti_gui图。笼屉背景longtiui、通用shipintaigui05来自A；蒸锅背景zhengguo及chujvwupinlan、chengfangui、caidangui、lianqi_gui_slot纹理来自B，资源清单必须登记真实yvanchuxitong来源ID。两个atlas按钮在A/B相同。目标合并命名空间后均引用yvanchuxiuzhen资源路径。MenuName直接来自菜谱，容器与九材料显示查名才转小写；按钮0开始、1五行来源指南。来源错误的铁锅称呼和铁盘第二格提示原样登记，未重写为新的玩法规则。

本批完整构建与内置客户端124项通过，覆盖自然菜单、五次完成、双扫描异常、库存保存读回、强制容量和能力入口；专服原生stop正常保存退出。蒸锅、笼屉和木甑实际截图已查看，可见中文与背景纹理正常。资源最终13411项、既有missing19；来源实机、完整目标自然生命周期/自动化设备、多人、完整视觉及两版本对照未执行。

## 普通食物消费（2026-11-10 静态接入）

正式接入MealEffects和4个内容类共5个Java、89条专用工厂覆盖，关联37种盘菜及52种直接食用物品，共126条注册。已重新扫描全部cooking来源Item：A有60个finishUsingItem，其中8酒另族实现，本批52个全覆盖；B无直接Item食用覆写。37盘菜use、46普通餐食finish及5返物finish各自为同型方法。93个来源元素、8个标签与精确目标原版机制均已独立静态核对；完整构建及本批真实食用客户端场景通过，范围见月志。

### 原生食用、标签与茶点

`A:FoodNNNItem.finishUsingItem`先super完成原生食用，再以同一原栈调用FooditemshiyongProcedure，最终返回super结果。1.18.2与精确目标原生食用均对非创造实体缩1；原栈吃空后getItem为AIR，因此最后一件不再匹配餐食标签，余栈与创造不缩栈时仍可匹配。非玩家LivingEntity余栈同样可结算效果，不人为限定玩家。目标ContentItem无额外完成逻辑，基础FoodProperties不配置usingConvertsTo。

餐食按item标签zhushi→caipin→tangshui→chadian互斥判定；前3类分别加effect_028/029/030，1200刻、0级、ambient=true、visible=false，五参构造使图标也不可见。茶点统计三标记的种类数，1/2/3类分别给LUCK2400/0、6000/1、12000/2，然后移除全部三标记；0类不加也不清。三个标记由cooking普通BENEFICIAL效果承载，没有typed表外部附件访问；其他系统通过原生效果API读取。

### 盘菜饱食与容器

`A:FoodNNNBlock.use`调用FoodYouJian，先执行Foodblockyoujian，再结算餐食标记和茶点，最后替换容器。当前世界方块构建默认物品栈用于餐食标签；主食加饱食12/饱和2、菜品2/12、汤水8/4、茶点2/2，两数各只上截断20。采用FoodData直接setter，不限制饱和≤饱食，不增加饥饿、潜行、手持或创造豁免。

效果处理后重新读当前方块，按block标签panzhuang→wanzhuang→dapenzhuang→tiepanzhuang互斥换为统一命名空间foodcipan/foodwan/fooddawan/foodtiepan默认state，flag3，不继承方向。无容器标签不删除原盘菜，保持可重复右键行为；37来源盘菜实际各唯一命中一种餐食和一种容器标签。目标useItemOn/useWithoutItem均沿原生方块门槛触发，返回SUCCESS。四item标签与四block标签由既有转换器从forge统一为yvanchuxiuzhen，已逐JSON对照无差异。

### 059毒材与五类返物

`A:Shicai059Item`营养2、saturationMod0.3、64堆叠；super完成食用后独立Math.random()<0.3施加POISON1200/0，不依赖余栈标签、玩家身份或创造模式。目标仅在服务端决定随机玩法效果。

food_084和shicai_060返默认骨头，shicai_034/036/037分别返各自01果核。来源先构建默认返物，再super食用：原栈空则直接返回返物一件；原栈非空时只给非创造玩家背包，add失败drop；创造和非玩家余栈不额外给返物，最终返回原栈。目标保留这些分支，未换成usingConvertsTo而改变非玩家语义或重复发物。客户端只保留原生消费返回栈预测，服务端独占额外库存和效果写入。

### 未闭合范围

本批覆盖消费回调，不代表自然获取闭合。食材种植与生物来源、菜谱自然获取、B容器叠放/库存收取、砧板和菜刀属于后续范围。名称与Tooltip继续既有转换路径；实际食用和盘菜交互已由内置客户端验证，完整视觉仍待终核。最终资源13411项、missing19、dropped91，较前一批13382项净增29且旧输出及source SHA均保留，data1703项。构建、客户端124项与专服启动保存退出通过；来源版本实机、目标完整自然生命周期、多人、完整视觉及两版本对照未执行。
