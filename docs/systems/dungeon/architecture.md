---
topic: dungeon
role: architecture
status: partial
updated: 2026-11-14
---

# 副本：入口、实例、波次与私人画卷

本系统拥有天道副本、画里乾坤、控制器、刷怪占位符及四个菜单。源码定位采用[系统导航](../README.md)的A/B规则。下文是已核对的来源规则和本批实现边界，转换、构建及实机状态见末节。

## 来源入口与数据所有者

`A:TiandaofubenZaiFangKuaiShangYouJianProcedure`按创造模式打开管理页或普通入口，`TiandaofubenDangShiTiZaiFangKuaiZhongPengZhuangShiProcedure`用本体`openfubengui`限制重复打开。四个菜单保留`fubenopen`、`tiandaofuben_gui`、`fubenblock_gui`、`fubenentity_gui`身份。普通页门票槽与控制器45槽只展示；管理页门票槽可放取；占位符菜单没有库存。输入初始为空，管理按钮只应用非空字段，简介按16个UTF-16字符拆分；攻击复选框初始未选，勾选才写真。

玩家原`tizhiitem`中的副本倒计时、定义、控制点、集合点、回程点与原模式改由Dungeon玩家附件保存，死亡复制；原`chuansongitem`的私人画卷快照由独立ItemStack附件保存，死亡复制。世界SavedData保存全服实例序号、大厅生成标志及各来源维度的画卷序号。运行阶段、波次、计时、占位符与奖励随控制器保存，服务器是唯一写入者。

`typed_field_accesses.csv`的`fubenweizhi`、`datingshengcheng`、`tizhiitem`副本字段、`chuansongitem`私人画卷分支、`LEVELnumber`及两种`teamwork`是本批外部边界：境界从`Cultivation.get`只读；玩家能力和本体实体临时队伍经`Relations`分开操作；结构交`WorldStructures.place`；怪物等级和临时攻击者交`DungeonCreatures.prepare`。本系统只写自己的标志/控制点，不修改其他系统玩家档案。

## 天道入口与候场

`A:TiandaofubenFangZhiFangKuaiShiProcedure`写“花妖连战”、`fuben002`、境界0–40、人数上限10、预览5、冷却240秒及默认定义。入口每20刻推进冷却。`FubenjinruProcedure`按16格内友方玩家核对条件、扣门票后写`fubenbegin`，支持原版三维和天阙、修罗、人间、妖魂、地灵、无归六维。普通门票按Item统计，带`fubenitem`者另比该身份，费用只从发起者扣除。准入边界保留Q103/Q104。

首入时入口槽1写燧石标志。实例X为`-100-100×全服序号`；`fuben002`控制点为`(实例X,98,-100)`，战斗集合点为`(实例X+16,100,-96)`。附近友方转冒险并传送候场`(-32,100,-32)`，随后按传送后的玩家坐标在原世界检查并补白块。队长保存6秒倒计时/定义；跟随者只保存模式并传送。

`A:Fabao009weiduDangWanJiaJinRuWeiDuShiProcedure`首次进入画里乾坤，在`(-48,98,-48)`放`fuben001`，清附近来源椅子/蒲团并置大厅标志。需要新实例时放`fuben002`、增加全服序号。`PlayerfubentickProcedure`由原体质秒钟调用，目标订阅既有`CultivationSecondEvent`。6秒结束保留控制方块状态/NBT并替换控制器，传送候场范围友方、写`fubenopen`。普通维度实体加入按来源附件恢复模式；本体返回标志另由玩家秒钟同步并复位入口冷却标志。

## 控制器与奖励

`B:FubenblocksetProcedure`扫描控制点正方向边长立方体，将占位符持久标签按ID加入控制器。`Fubenqingchu`只清五个波次数和运行字段；`Fubenqingchufubenentity`读连续`zhanwei{n}`，遇X偏移0停止。`Fubenshujvjiexi`按百分号解析总波数、总时间、奖励及各波`实体ID/等级/占位ID`，保留`fybenboci`拼写。

`B:FubenblockGengXinYouXiKeProcedure`每20刻推进：准备7秒→释放敌人1秒→监测→下一波/成功/失败。同次释放怪物共享运行期友方集合，各自团队强制为`isfubenentity`；写等级和最近12格玩家临时攻击者后原生初始化、入世界。每刻统计区域全部玩家和带`fubenentity`的非玩家，不核对实例控制点。总时间每活动刻减1；成功后倒数10秒回程，失败两游戏刻后回程。

礼物按`&`分条、`/`分字段：首四项是物品、显示名或“无”、1–100权重、数量；后续三元组支持`char/str/int/double/doube/logic`。非法数值为0，布尔仅“真”为真，同键后写覆盖，不完整尾项不消费。18槽对区域每位玩家各自独立抽样。空礼物列表不清旧权重/库存。

业务实例经所有者API构造：材料`Materials.setInstance`、技能`Skills.createBook`、九材料图纸`Lianqi.createBlueprintReward`、丹方`Liandan.createRecipeReward`、宝箱`TreasureContainers.create`。随后把所有已解析且所有者尚未写入的根字段存入`CUSTOM_DATA`；所有者既有根字段优先，包括转换后的物品ID。技能和材料消费者读取类型化组件，根字段副本不能覆盖组件真值；图纸/丹方/宝箱读取所有者构造的根字段。`belongToItemID`、`danfangid`及任意额外配置会随栈保存，本批未新增其消费者；保留字段不表示字段已消费。

入口2槽、控制器45槽、占位符0槽、画卷1槽分别保存，画卷上限1、其余64。强制赋槽及菜单镜像只按来源容器上限截断。六侧`SidedInvWrapper`与空侧`InvWrapper`分开：入口六侧允许放入、禁止取出，控制器/画卷六侧禁止放取，无方向包装沿来源父类原生规则。能力父类证据复用[目标参考](../../../.claude/skills/minecraft-mod-porting/references/targets/neoforge-1.21.1.md#库存身份匹配与无方向能力2026-11-10-核对)。

## 私人画卷

`A:Fabao00901DangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure`只取主手画卷；向上面点击在上方2格放置，其余按点击面邻格；直接替换方块、朝玩家反方向。登记主人UUID和本端坐标、把数量1写单槽、清空整个主手栈。放置物品与普通图标的画卷方块物品保持两个来源身份。

`Fabao009ZaiFangKuaiShangYouJianProcedure`中主人潜行取回，非主人失败提示；首次普通点击播放动画1并开8秒窗口，第二次传送。外维度首次激活先增加本世界画卷序号，出口为`(48×序号,100,48)`，给缓降200刻并保存两端快照。进入画卷维度后清`hualiqiankun`，首次在出口偏移`(-24,0,-24)`放`fabao008jiegou`，出口上2格放画卷、出口Y加2。内维度返回写`fanhui`；普通世界实体加入时清标志并刷新入口槽0。未为入口丢失补造重建逻辑。

## 镇妖塔与外部依赖

`tools/porting/extract_dungeon_definitions.py`从锁定A源码提取默认定义1份、镇妖塔44份及楼板，生成`DungeonSourceData`和[来源/输出指纹](../../reference/conversion/dungeon_definitions.json)。楼板逐一为固定点Y-1；43份有完整百分号头，第44份保持Q81。`Dungeons.configureTower`提供固定点补充操作。

来源调用者`A:SealreleaseGoProcedure:444`属于World结构释放命令：`YvanchuGo:536–537`在`unlock sealrelease`分支调用，镇妖塔固定放置后40服务端刻补充44点定义；与Fulu的50→5001妖兽尸体封存没有调用关系。2026-11-14已接入World发布的`StructureReleasedEvent`和Dungeon消费者，保持既有Dungeon→World依赖方向；管理命令路径与运行边界见[World正文](../world/architecture.md#固定结构解封命令2026-11-14)。寻灵盘/裂隙自然生产者仍由R06承接。

## 来源异常与当前处理

### Q81 镇妖塔第44点缺少波次头

`A:ZhenyaotashengchengbuchongProcedure`最后点`(1278,83,-587)`只写奖励串，无`%`头；`B:FubenshujvjiexiProcedure`取第1字段时越界。目标逐字保留，不补波次或奖励入口；1.18.2版本实机未执行。

### Q82 私人画卷地灵与无归返回互换

来源外维度登记`dimensiondiling`为“地灵界”、`dimensionwugui`为“无归界”；内部返回时“无归界”指向`dimensiondiling`、“地灵界”指向`dimensionwugui`。目标保留互换；未知回程名仍形成空ID，不加兜底。

### Q103 准入与扣票顺序

`A:FubenjinruProcedure`的能力临时队伍为空时，不查发起者境界且人数为0；境界/人数拒绝后仍扣票；不支持维度在成功准入后已写冷却。目标保持顺序。临时队伍界面/生产者由Relation/Panel后续接回。

### Q104 妖魂回程分支

同过程妖魂分支误重复判断主世界，妖魂准入保留旧`chuansongid`，首次可能为空；目标不按当前维度补正。

### Q105 跟随者与模式

同过程只给队长写定义、实例点和回程，跟随者可能保留自身旧附件；两种临时队伍不同。来源`非创造 || 非旁观`恒真，所有原模式都转冒险；目标保持并按来源实体加入分支恢复。

### Q106 占位符扫描与攻击位

`B:FubenqingchufubenentityProcedure`遇X偏移0停止清理；`FubenblocksetProcedure`扫描不清累计计数；未勾攻击不清旧真。解析复制攻击位，释放过程不消费。目标保持。

### Q107 区域统计与标题

`B:FubenblockGengXinYouXiKeProcedure`统计区域所有玩家与副本怪物标志，不核实例坐标；副本名不经JSON转义直接拼标题命令。目标不补过滤或转义。

### Q108 同维度失败回调不注销

来源两刻延后回程若已在目标维度直接退出回调，未注销监听器；目标保留任务后续继续尝试，服务器结束清空运行期任务。

### Q109 火焰护盾奖励错位

镇妖塔两份“火焰护盾”简介含`30//jinengneeditem…`，斜线拆分使后续三元组错位，后续数值/冷却未写；“火鸟术”空`jinengjieyin/int/`按来源转0。逐字保留，不修字符串。

### Q77 未使用的三行菜单

来源入口/画卷方块实体的`createMenu`返回独立三行`ChestMenu`，不绑定2/1槽库存；自然交互使用入口页或画卷操作。目标保留独立死入口，与Treasure的Q77共用同族问题编号，详见[统一问题索引](../../reference/legacy/source-survey.md#具体问题与裁定位置)。

### Q111 二次入场的检查时序疑点

画卷维度补齐后的首轮，首次入场、三波、成功发奖与回程通过，但第二次入场的复合等待条件超时；当轮没有记录维度、候场和控制器各阶段，不能定位失败环节。后续夹具在双方菜单就绪后等待8客户端刻再操作，增加分阶段日志，完整第二次入场/原控制器重新准备/受控超时回程及私人画卷链通过。该轮同时补齐纯白群系全局事件，未改副本生产流程。保留两轮证据，原因未定，不能认定来源异常、目标缺陷或已修复玩法Bug；不为此无改动重复运行。具体原件见[本月志](logs/2026-11.md)。

### Q112 控制器与占位符标签的目标显示差异

来源`B:client/gui/FubenentityGuiScreen.java:84–87`调用`Zhanweifuidxianshi`，只在`(6,16)`用黑色`-16777216`绘制原占位ID。目标`DungeonScreen.renderLabels`在`(6,11)`用灰色`0x404040`绘制“占位符ID：”加ID，文字、位置和颜色均未完全对齐。控制器“边长：N”的内容与来源一致，但标签颜色同样由黑变灰。该差异已静态确认；按负责人集中处理要求记录，当前尚未修正，不表述为保持来源布局或完整视觉验收。完整页面逐像素对照尚未执行。

## 开放范围与验证边界

入口/管理、候场实例、控制器/占位符、波次与回程、奖励、私人画卷及四页/渲染已主线集成；工厂、来源资源、公共客户端与画里乾坤维度已接入。最终合并`zh_cn.json`的24个运行时键、动态键与四页中文标题已核。四页菜单真实操作、首次自然入场、三波受控清敌及成功发奖回程已有1.21.1版本运行证据；第二次自然入场、受控超时回程与私人画卷放取/双击往返也通过；前轮二次入场超时原因保留Q111待核。

静态来源、精确NeoForge21.1.265签名与同版能力父类规则已核；提取器`--check`、三份结构持久字段对照、编译和6项正式ArchUnit通过。沿用项目内置客户端框架新增必要副本场景；结果、失败分类与截图覆盖只记[本月志](logs/2026-11.md)。本批未运行专服、保存重进、多人、1.18.2版本实机或两版本对照。R06世界结构释放入口、临时队伍页面/生产者和完整自然奖励消费者继续开放，R11未验收。
