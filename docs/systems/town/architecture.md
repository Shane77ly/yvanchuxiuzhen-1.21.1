---
topic: town
role: architecture
status: partial
updated: 2026-11-14
---

# 城镇规划、建筑与中心案台

源码定位采用[系统导航](../README.md)的A/B规则。本批实现来源已有的卷轴、建设和中心展示；空白监听与没有自然调用者的注册页面分别登记，不补造居民、战争或官职玩法。

## 规划入口与容器

`/obtainatowncenterscroll`按B命令原有无权限门槛，在命令坐标XYZ各加0.5处掉落“市镇中心案台”卷轴，拾取延迟10，返回0。卷轴`build_data`保存市镇中心类型、48×17×48尺寸、四向偏移和60木/60石/3羊毛费用。四向偏移依01–04为(18,2,3)、(29,2,44)、(44,2,18)、(3,2,29)。

卷轴只在目标为空气时放规划方块，水平点击取被点方块邻格、朝上取上一格、朝下取下两格，竖向点击以玩家水平朝向定向；来源不消耗卷轴，目标保持。NORTH选择02“坐南朝北”，SOUTH选择01“坐北朝南”，WEST选择03“坐东朝西”，EAST选择04“坐西朝东”。只读玩家实体团队经`Relations.entityTeam`交接，不把玩家关系能力中的其他字段合并到本系统。

规划核心十八槽、每栈64，中心一槽、每栈1；两者允许来源的侧面插入与提取，容器、`build_data`、团队和所有权随方块保存并以更新包同步。普通右键规划核心打开材料页；潜行右键未完工核心回收带原`build_data`的卷轴并移除方块，材料按方块移除掉落。完工核心需主手建筑锤才可拆除，未持锤显示来源中文提示。

## 材料与三类建筑

`ChengzhenBuildJiance:22–78`以`if/else if/else if`将每栈只归入木、石、羊毛的一类，依次使用`minecraft:logs`、来源stone与wool标签；目标后两者转换为`yvanchuxiuzhen:stone/wool`，不替换成猜测的原版标签。打开后计划1刻、槽变化和点击建造前重算十八槽数量，界面展示需求/当前材料。

`Jiegoufangzhi`按“市镇中心/工筑堂/灰瓦民居”与“坐北朝南/坐南朝北/坐东朝西/坐西朝东”选择`build01/02/03`加`nouth/south/east/west`。`nouth`为来源资源真实拼写。原点是核心位置减保存偏移，不再旋转或镜像，包含模板实体、放置标志3。建设后恢复偏移、尺寸、团队和`build_data`，写`construction_completed=true`。

### Q70 模板替换与材料扣费

已确认来源缺陷，2026-11-14经负责人批准调整并已实现，中心与受控02模板的跨栈余料必要单项已通过，完整分支仍待验。B `BuildingProcedure:29–34`先检查旧十八槽库存，然后调用结构放置；`:35–43`恢复数据，`:44–46`取费用，`:48–220`却从放置后的当前位置方块实体逐槽读取/扣费。B `ChengzhenBuildblockBlock.onRemove`在方块种类替换时掉落旧库存。模板会清空或替换库存，中心新容器更只有一槽，因此旧材料可能掉落而未支付，并在新中心访问槽1–17。

来源模板证据直接来自B `src/main/resources/data/yvanchuxitong/structures/build01{east,nouth,south,west}.nbt`与02/03同族共12文件，SHA1逐项匹配[结构清单](../../reference/conversion/structure_manifest.csv)。01四向分别在(44,2,18)/(18,2,3)/(29,2,44)/(3,2,29)放`chengzhen_antai`；02/03放`chengzhen_buildblock`；全部核心NBT的`Items`为空。01尺寸48×17×48，02为26×10×26，03为22×11×22。目标NeoForge21.1.265 `InvWrapper.getStackInSlot:42–43`直接调用容器`getItem(slot)`，不会将中心越界读变为空槽。

采用批准的“正常扣料并保留余料”：复制旧十八槽，按原槽序、分类与费用扣除，在结构放置前清空旧库存以阻断`onRemove`重复掉落；放置后十八槽核心恢复相同槽的余料，一槽中心在原核心位置旁掉落全部余料。若方块实体被替换，关闭旧菜单，防止继续向旧十八槽容器写入。没有更改材料分类、费用、模板或建造时机。需用混合多栈材料、无关物品、刚好足量、超额余料及中心/工筑堂/民居分别核验，并统计总物品守恒。

## 中心归属与页面

中心普通右键在无主人时补“无名村落”、8个随机小写字母town_uuid、最低等级1“村落”，再保存当前玩家UUID；主人打开02页，其他玩家打开01页。01显示占领说明，02显示人口/住房/旅馆/死亡数，03按`build_list`重算`town_build`并以8行每行18字符显示建筑列表，04显示军队/修者，05显示官职。

六个菜单候选已完成B `world/inventory/ChengzhenbuildGuiMenu.java`、`ChengzhenUi01Menu.java`–`ChengzhenUi05Menu.java`及相应屏幕的完整阅读。建筑菜单由`ChengzhenBuildblockZaiFangKuaiShangYouJianProcedure:63`自然打开，十八槽消费`BuildingProcedure`，归城镇；01/02由`ChengzhenAntaiZaiFangKuaiShangYouJianProcedure:47,79,96`按归属自然打开，03–05仍无自然打开者。目标`TownBuildMenu/TownMenu`保留原注册身份，03–05只登记展示装配与静态注册，不将无调用链的页面标为自然玩法已迁移。目标动态建筑语言键统一为`txt.yvanchuxiuzhen.build01`–`05`，同时转换`build_list`原字符串，避免来源命名空间泄露为原始键名。

### Q79 城镇空监听与页面调用断链

确认的来源空实现与待核调用，当前保持。B `ChengzhenUi01Screen:88–89`占领按钮监听为空；`ChengzhenUi05Screen:98–103`两个官职按钮同样为空。02屏幕只有原生背景和数值文本，无翻页按钮。`ChengzhenUi03/04/05`在来源注册菜单和屏幕，但当前静态事件/过程引用未找到自然打开者；目标仍注册和实现展示，没有为玩家新增翻页入口或让空按钮产生效果。该情况不等于居民、战争、官职系统已实现；进一步动态调用或来源实机核对仍开放。

## 来源与验证边界

本批来源还包括B `block/ChengzhenAntaiBlock.java`、`ChengzhenBuildblockBlock.java`及两BE、卷轴物品、六个菜单/屏幕、`ObtainatowncenterscrollGo`、卷轴放置、核心右键、`Building`、`Jiegoufangzhi`、`ChengzhenBuildJiance`、中心右键、03页打开和各显示过程。菜单构造中没有自然调用的手持物品/实体绑定模板不扩展成新玩法。

目标方块新放置时访问BE已核对同一sources.jar：`LevelChunk.setBlockState`在显式创建BE前调用`onPlace`，但`Level.getBlockEntity`使用`EntityCreationType.IMMEDIATE`，可即时创建；两自定义方块保留`triggerEvent`转发。此项为静态生命周期依据，尚无运行结论。

主线已完成定向转换、生产/smoke编译与6项架构检查，`construction`必要客户端单项覆盖真实中心卷轴放置、材料页及建造按钮。中心按来源60木/60石/3羊毛扣料，12木/12石/2羊毛/7钻石余料原位掉落；受控02模板按原槽保留同组余料且不重复掉落，正常保存重进仍一致。02夹具复用中心费用只用于检查模板替换和库存守恒，不能当作02来源费用或自然获取事实。中文材料页截图已查看；建造后的截图为现场墙面，不能充当余料菜单视觉证据。完整生存卷轴获取、三类建筑全向实机、03模板、刚好足量与更多材料边界、居民/战争/官职、多人、专服、1.18.2版本实机和两版本对照仍开放；原始报告及夹具失败边界只在本月记录登记。

## Q138 潜行持锤拆除核心的入口冲突

B `ChengzhenBuildblockBlock:105–116`只从Block.use调用，过程`34–49`要求潜行且主手hammer。精确Forge40.1.0的ServerPlayerGameMode补丁在secondaryUse且手持物、doesSneakBypassUse默认false时跳过Block.use；来源锤未覆写，两份来源也无事件强制放行。静态推断正常潜行持锤将走锤useOn，核心硬度−1满足移动规则，上方为空时搬状态而不搬BE。目标保持来源，不补sneak bypass；Town已实现的扣料/余料批准Q70不代表此自然拆除可达。1.18.2版本与目标自然操作未实机，待集中核实，证据见`tmp/hammer-tool-evidence/hammer-source-review.json`。
