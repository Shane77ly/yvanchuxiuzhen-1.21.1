---
topic: liandan
role: architecture
status: partial
updated: 2026-11-09
---

# 炼丹、药理、药物与毒药

本页记录已核对的 1.18.2 版本来源规则与 1.21.1 版本实现范围。源码定位采用[系统导航](../README.md)的 A/B 规则，公开操作见[系统入口](README.md#公开操作)。八档丹炉自然炼制链、八种药瓶消费/放置链、分丹台及药理台已有相应构建和客户端样本验证；2026-11-09 本批接回百草鉴辨识、药草提示、全解锁及玩家附件，来源静态核对已完成，构建、内置客户端54条INFO/0FAIL/DONE及专服加载保存退出通过。证据与边界见[月志](logs/2026-11.md)；毒药结束回调等仍未实现，不能据此标记系统完成。

## 八档丹炉与状态

来源 `A:Alchemy01Block:118–132` 每 20 游戏刻执行一次 `Alchemy01GengXinYouXiKe`，八档共用此过程。各自放置过程的 16、25、34、43 行写入名称、等级、火力和分丹能力。

| 注册 ID | 名称 | 等级与火力 | 分丹能力 |
| --- | --- | --- | --- |
| `alchemy_01` | 黄铜炉 | 1 | 1 |
| `alchemy_02` | 红蛇炉 | 2 | 1 |
| `alchemy_03` | 海渊炉 | 3 | 1 |
| `alchemy_04` | 地肺炉 | 4 | 2 |
| `alchemy_05` | 紫云炉 | 5 | 2 |
| `alchemy_06` | 天青炉 | 6 | 2 |
| `alchemy_07` | 锦纹炉 | 7 | 3 |
| `alchemy_08` | 八卦金炉 | 8 | 3 |

`B:PlayerRightClickBlockProcedure:46–76` 的全局右键事件按丹炉标签打开 `LiandanGUI`；`A:Alchemy01ZaiFangKuaiShangYouJianProcedure:24–40` 随后在空闲炉写开盖动画 3，并把右键玩家的 `Alchemy_LEVEL` 保存到装置。开炉使用该快照；来源没有保存炉主或操作者 UUID，也没有锁定首个开窗玩家。

方块实体有 12 槽：0 为容器，1–8 为材料，9 为结果，10 为丹方，11 为炉次临时产物。菜单显示 0–10；方块能力向全部方向开放 12 槽，隐藏的 11 号槽仍可由自动化修改。库存、药性表 `zong`、热量、进度、职业快照和经验均随装置保存，服务端写真值，菜单与方块实体包同步显示。

目标 `AlchemyBlock`、`AlchemyBlockEntity` 承担库存和计划刻，`AlchemyRuntime` 承担初始化、右键、供热与完成，`LiandanBrewing` 承担开炉和菜单动作。跨系统不直接改其持久化对象。

## 药草目录与装置药性

实际目录来自 `B:WorldbeginProcedure:47–49` 每次世界加载写入的 `caoyaolist`，共 **37 条材料、100 条有序药性**。`YvanchuxitongModVariables` 的旧默认值少两条，而且 `goldenboilinggrass` 为三项；运行时目录为四项并保留两条同名 `water_breathing` 药性的先加后乘顺序。

`B:LiandanYaojixiaoguoProcedure:26–44,45–92` 仅在实体放置丹炉或药理台时生成 `zong`。每条先写 `<物品ID>number`，再按顺序写效果名称、等级操作/数值、时间操作/数值。装置保存独立快照；指令放置和结构恢复不补造实体放置事件。两类装置的放置消费者均已接回；药理台依据快照预览丹方，不提供编辑药性表的来源入口。

目标 `LiandanHerbs` 从 `content/liandan/herbs.json` 生成相同键结构，`tools/porting/convert_content.py` 的 `convert_herb_definitions` 抽取 Worldbegin。目录中的 `yvanchuxiuzhen:nichanggrass` 未注册，实际注册为 `nichanggrass_01`–`07`；保留目录错名，不重定向到 `nichanggrass_07`，该行没有自然匹配物品。保留定义不能登记为该药材药性已自然生效。

药草目录及玩家 `playercaoyaojiexi` 辨识属于炼丹，与 [material](../material/README.md) 的千材录辨识不同。百草鉴复用上述 37 草、100 条顺序药性，玩家已揭示结果独立保存；`Lore` 只读这些结果，`Jiesuocaoyao` 由炼丹公开写入口承接，具体规则见下文。

## 开炉、药效与数量

正式入口 `B:network/LiandanGUIButtonMessage:78–80` 调 `LiandanBeginProcedure`，仅排除正在炼制状态，没有容器、材料、结果槽和玩家等级的额外前置检查。

**等级检查游离块保持不可达**：`elements/LiandanBegin.mod.json` 的 `LiandanLVjiance` 位于顶层，不在 `event_trigger` 子树，生成 Java 未调用它，`fanhuizhi` 初值为 0。另一个 `Linshiceshiliandan` 虽然调用该检查，但没有实际调用者。目标不恢复游离块功能。

`B:LiandanBeginProcedure:43–113` 用火药承载临时结果。槽 0 的 04、05 容器分别映射到 0401、0501，其余保持原物品 ID。槽 10 非空就取 `danyaoname/danyaoshuoming`，即使字段为空也不另取默认名；无丹方时按槽 1 显示名 `substring(1,3)` 加“丹”。临时显示名为“丹药残渣”。

八格材料依次处理，每个非空槽只消耗一件，不按栈数量重复加入。首个 `yaodan` 标签物品仍参与计算和消耗，但其后的全部材料槽跳过。未知药性材料仍消耗一件。证据为 `B:LiandanBeginProcedure:114–146,426–443`。

药效用 `danyaoxiaoguo{i}/danyaolevel{i}/danyaotick{i}` 与 `shuliang` 表示，不限制为固定四种。每条药性按顺序匹配同名效果并保留整数规则（`B:LiandanBeginProcedure:147–423`）：

| 情况 | 等级 | 时间 |
| --- | --- | --- |
| 已有同名效果，加/减 | 原值加/减数值 | 原值加/减数值 |
| 已有同名效果，乘 | `int(原值 × (数值 / 100.0))` | 同左 |
| 已有同名效果，除 | `int(原值 / (数值 / 100.0))` | `原值 / (整数数值 / 100)` |
| 新效果，加/减 | 写正数/负数 | 写正数/负数 |
| 新效果，乘/除 | 不写数值字段 | 不写数值字段 |

时间除法的整数分母是来源 `307–328` 实际写入表达式，其 double 临时值未使用，不能套用等级除法。目前 100 条目录药性没有除法项，药理台丹方预览仍保留其语义。未知操作不写对应字段。

每处理一条药性，基础时长加 4，合并或新增同名效果均如此。炉次规则（`B:LiandanBeginProcedure:445–491`）：

- `danyaonum = 1 + 随机整数[0, alchemyDannum]`，临时栈写槽 11 后消耗槽 0 一件。
- `lianqiTickMax = int(基础时长 × (1 − (alchemyFire + Alchemy_LEVEL) × 0.05))`，不限制最小时长。
- 最大药效初值为 0，只取正等级；令 `L = 最大药效等级 / 100.0`，待领取经验为 `int(L × 4)`。
- 若 `L > Alchemy_LEVEL + alchemyLevel`，`zhalu=true`，`zhalugailv=(Alchemy_LEVEL+alchemyLevel)/ceil(L)`；否则仅清 `zhalu`。
- 开炉不清旧 `lianqiTick`、结果槽、`zhenzhale`，保持来源边界。

## 供热、火力与完成

炼丹订阅既有 `FireHeatEvent` 与 `FireExhaustedEvent`，唯一写自身 `lianpower/gengxin`。丹火每 20 游戏刻交付热量；自然放置一级丹火的最终初始燃料为 10，物品过程中的燃料会被延后执行的火块 `onPlace` 覆盖，需继续补料才能支撑超过 10 炉刻的黄铜炉次（已核实加载器生命周期见[异火正文](../yihuo/architecture.md#丹火与火块)）；燃料耗尽时，上方炉热量至少 1 就翻转 `gengxin` 并保留火块，否则移除火块，不要求正在炼制。来源为 `A:Cailiao221fireblockGengXinYouXiKeProcedure` 与 `HuoyanxiaoshipandingProcedure:13–55`。

`A:Alchemy01GengXinYouXiKeProcedure:30–226` 每次计划刻先处理火力：`firepower >= 1` 时发熔岩粒子并使 `firepowertick += 1`，累计 12000 次后同时清火力与计时，与是否正在炼制无关。火力符 `A:Itemfulu20DangTouSheWuSheZhongCiFangKuaiProcedure:13–25` 只覆盖 `firepower`，不重置计时；目标 `Liandan.boostHeat` 已提供，符箓命中消费者随 fulu 接入。

炼制时仅在 `lianpower >= alchemyLevel` 时消费对应等级热量，播放正常/风险动画 1/2、发火焰粒子，并使 `lianqiTick += 1 + firepower`。缺热量时发烟雾、保持进度；空闲计划刻置动画 0。

达到时长后，风险炉以 `Math.random() > zhalugailv` 判为炸炉，所以该字段实际是成功阈值。成功播放动画 4，按槽 11 的 `danyaoid` 构造容器、复制临时实例数据并改显示名，以一件覆盖槽 9；失败以普通火药一件覆盖。两者均清槽 11、进度、炼制和风险标记；失败随后爆炸，在仍存在的炉上写 `zhenzhale=true`。爆炸强度 `2 + 2 × alchemyLevel`，`mobGriefing` 控制方块破坏，保留来源 BREAK 的完整掉落语义。来源为 `A:Alchemy01GengXinYouXiKeProcedure:227–405`。

进度显示在结算后读取当前状态计算 `lianqiTick / lianqiTickMax × 100`，不截断或额外处理零分母。输出覆盖的运行结果见本月记录；自动化修改临时槽、爆炸摧毁炉体和完整时序仍需运行验证。

## 丹方按钮与经验领取

`B:LiandanzidongfangruProcedure:37–137` 要求丹方名称非空。先返还八格材料整栈，再按 `cailiao01`–`08` 在玩家实体物品能力中查首个同物品 ID 实例，每格取一件并保留实例字段。不要求凑齐，不读材料仓，不限制炼制期间使用，也不装槽 0 容器。

`B:LiandantuzhibaocunProcedure:23–154` 有两处异常：槽 10 有物但 `danyaoname` 为空时反而提示“此物已承载了一份丹方，无法覆盖”；名称非空才检查材料总数。成功分支只对槽位副本写名称和 ID，没有回写库存，按钮实际不保存丹方。目标保留该结果与来源帮助文本，不按文案补造保存功能。

结果槽 changeType 1/2 均调用 `Jingyanhuoqv`，不额外要求实际取到成品。非炼制且待领取经验至少 1 时，炸炉标记先令经验除四并清标记；经 `DecimalFormat("0")` 整数文本舍入后加到操作者炼丹经验，随后炉内经验清零。目标通过 `Professions.addExperience` 写职业，保留“炼丹经验增减量为”反馈。来源为 `B:network/LiandanGUISlotMessage:73–80`、`B:JingyanhuoqvProcedure:18–29`、`A:YvanchuGo2Procedure:83–102`。

## 八种药瓶与自然入口

来源为 `B:item/Itemdanyaohulu*Item`、同名 `block/Itemdanyaohulu*blockBlock` 与 `block/entity/Itemdanyaohulu*blockBlockEntity`。下表后缀属于普通物品 `itemdanyaohulu_<后缀>`；每种另有独立的 `itemdanyaohulu_<后缀>block` 方块、方块实体和隐藏方块物品。普通物品使用二维模型，不能与隐藏方块物品合并身份。

| 后缀 | 容器 | 最大堆叠 | 空气右键 | 创造栏 | 方块朝向与含水 |
| --- | --- | --- | --- | --- | --- |
| `01` | 葫芦 | 64 | 有 | MISC | 无朝向、可含水 |
| `02` | 瓷瓶 | 64 | 有 | MISC | 无朝向、可含水 |
| `03` | 精致葫芦 | 64 | 有 | MISC | 水平朝向、可含水 |
| `04` / `0401` | 小盒子 | 64 / 1 | 无 / 有 | MISC / 隐藏 | 水平朝向、不含水 |
| `05` / `0501` | 六边盒 | 64 / 1 | 无 / 有 | MISC / 隐藏 | 水平朝向、不含水 |
| `06` | 玉瓶 | 64 | 有 | MISC | 无朝向、可含水 |

六种空气 `use` 执行服用后仍返回父类的旧调用栈与 PASS，不增加进食动作、声音或冷却。八种 `useOn` 统一返回 SUCCESS：潜行走放置，非潜行走服用，所以 04/05 仍有方块右键服用入口。来源为 `B:Itemdanyaohulu01DangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:29–149`；实际能否先到达物品由原生方块交互分派决定。

## 自服、堆叠与空容器

`B:Itemdanyaohulu01DangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:34–123` 仅要求非潜行、初始 `danyaonum >= 1`。不加饥饿、血量或职业前置条件；初始空瓶直接无操作，来源重复条件内的“空空如也”分支不可达。

- 栈数量至少 2 时只合并、不施效：以 Java int 乘法计算 `danyaonum × 栈数量`，保留整份调用实例数据并变为一件；另造同 Item、无原实例数据的空容器 `栈数量−1` 件交给玩家，满库存由 `ItemHandlerHelper` 掉落余量。此分支不把 0401/0501 改成 04/05。
- 单件时逐项施效，再把丹数减一；即使命令失败也消耗。丹数耗尽则创建一件全新空容器，0401→04、0501→05，其余同 ID，旧自定义名、丹药字段及其他实例数据全部舍弃。
- 来源合并与耗尽换瓶都按“副手同 Item 优先，否则主手”选手，不比较实例数据，也不按本次交互手选择。双手同类型时可能由主手调用而替换副手，保留此异常。创造模式仍交给原生 `use/useOn` 外层处理计数恢复，不另加豁免。

药效从索引 0 遍历至首个零或缺失的 `danyaolevelN`；不读取 `shuliang`，负等级继续遍历但不施效。普通丹药等级 `L = getInt(level) / 100`、秒数 `S = getInt(tick) / 20`，均先做 int 除法；`danyaobaoxiang=true` 的宝箱丹药直接使用原始整数。自服仅对 `resistance`、`minecraft:resistance` 设置 L 的上限 3。先算 S，再把小于 1 的原始 tick 回写为 1，本次 S 不重新计算。

L 至少 1 时以权限 4、无执行实体、屏蔽输出的来源执行 `effect give <服用者UUID> <原始效果名> <S> <L−1>`。目标 `LiandanMedicine` 沿用原版命令，保留效果名查找、秒数 1–1000000、等级参数 0–255 的校验；失败仍继续遍历并扣丹。原生瞬时效果使用 S 作为持续 tick，其余使用 S×20；`effect_011/016` 并非原生瞬时效果，仍经过其第一刻处理与移除，不直接代替为数值写入。来源依据为上述服用过程 `:68–94`，以及两版本 `EffectCommands`。

药效行为由原数据所有者承担：既有回灵力、修为、精力和毒药效果继续走 combat；`effect_031` 辟谷计数由 cooking 接回。`effect_037`、`huxiangtunshi` 的妖兽/蛊虫消费者分别随 creature、guchong 实现；取得效果标记不等于这些后续消费者已完成。

## 药瓶 Tooltip

`B:LoreProcedure:186–221` 在前置装备标签分支未命中、存在玩家且物品属于 `danyaoguan` 时追加药瓶信息。该标签只有 01、02、03、06、0401、0501，04/05 空盒不在其中。`danyaonum >= 1` 时依次显示青色说明、白色剩余数量和药效；空说明仍保留空行。

药效提示遍历至首个空的 `danyaoxiaoguoN`，与服用的零等级终止条件不同。普通等级至少 100、宝箱等级至少 1 才显示该项，否则跳过后继续；等级与秒数沿用上述整数换算，显示“L级<效果译名>：S秒”。无命名空间取 `effect.minecraft.<名字>`，其余把冒号改成点后加 `effect.`；未知译名保留原键。提示不做抗性三级限制，因此显示值可能高于实际自服等级。目标为 `MedicineTooltip` 的原生 Tooltip 事件。

## 对实体喂药

`B:PlayerRightClickEntityProcedure:39–43,66–310` 的 EntityInteract 先要求事件手等于玩家 `usedItemHand`。任务分支优先：来源给 `npcentity` 目标设置 `renwuzuopen`，已有此标记的目标也先进入任务分支。目标 `MedicineFeeding` 只读排除该标签和已有标记，任务字段仍由对应系统写入。

喂药使用主手，要求属于 `danyaoguan`、丹数至少 1、潜行、目标存活，且目标 Mob 当前攻击目标不是喂药者。多栈分支计算出的合并副本没有写回，实际不改变喂药者库存；另造的 `栈数量−1` 件同 ID 空容器只发给被喂的目标玩家，非玩家不发。这是来源 `:237–249` 的消费断链，不能复用自服合并来修正。

单栈沿用零等级终止、普通/宝箱单位和先算秒再回写 tick 的规则，但不限制抗性等级；执行者是被喂目标自身、选择器为 `@s`，命令屏蔽输出并提升权限。扣除与耗尽换空容器固定写喂药者主手，不走副手优先。死亡或拒绝提示发给目标玩家，非玩家无文字；来源不取消交互事件，目标保持其他自然实体交互。

## 放置、拾回与单槽自动化

`B:Itemdanyaohulu01DangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:29–147` 只允许潜行点 UP 且正上方为空气。按调用 Item 的准确 ID 放对应方块，不作满盒/空盒转换；有水平朝向的五种设为玩家反方向。调用物品完整实例数据复制为一件放入槽 0，再按副手同 Item 优先规则扣一。不增加一般可替换、内容非空或支撑条件，也不因 `setBlock` 返回失败补造退款。

`B:Itemdanyaohulu01blockZaiFangKuaiShangYouJianProcedure:18–44` 拾回没有潜行或空手条件：给玩家槽 0 的一件副本，清槽，再设 AIR；满库存仍由 helper 在玩家处掉落。空槽或缺少方块实体仍移除方块。目标同时实现 `useItemOn/useWithoutItem`，普通持物右键先拾回并结束，不额外执行手中物品，也不重复拾回；潜行绕过仍由原生分派决定。

八种方块实体均为一槽、最大堆叠 1，六面及无面能力开放，允许任意物品插入/抽取。自动化改入其他物品后拾回也原样给出；破坏、爆炸或替换通过 `onRemove` 掉落槽内实例，拾回先清槽避免再次掉落。来源没有八种瓶体自身的战利品表，空壳不额外生成普通药瓶。只有 01 无比较器输出，其余七种读取单槽容器。后备菜单保留来源空临时箱构造，普通右键不走此菜单。

目标 `MedicineContainerPlacement` 与 `MedicineContainerBlockEntity` 保存完整实例并提供完整更新标签；放置先登记方块变化，同一调用再写槽，原生后续广播读取当时的方块实体，因此首次更新包含槽内物品。已按 NeoForge 21.1.265 的 `Level`、`ServerLevel`、`ChunkHolder` 调用链核对。四种含水实现、朝向、形状、XZ 偏移、材质、cutout 及模型贴图保留来源；04/0401、05/0501 虽共用贴图，各自保持不同几何。隐藏方块物品走普通方块放置，不自动把自身填入库存。

## 分丹台与双页库存

来源为 `B:block/FendantaiBlock`、`block/entity/FendantaiBlockEntity`、`world/inventory/FendanGUIMenu`、`HedanGUIMenu`、两份 Screen 和 ButtonMessage。台面保留来源木材属性、硬度 2、抗爆 10、水平朝向、含水、形状及 cutout；已有原生 `fendantaihecheng` 配方用药瓶标签、三木板和两木棍合成，本批不改变配方。

三槽各最多一件，允许任意物品。六面能力均开放三个槽的插入、禁止全部抽取；无面能力继承 Forge 40.1.0 `BaseContainerBlockEntity` 的 `InvWrapper` 语义，可插入及抽取，两个菜单绑定该无面库存。目标用 `MedicineSeparationBlockEntity` 保存完整组件，发送完整更新标签和更新包；比较器读取库存，破坏或替换掉落三个槽内物品。

台面右键打开分丹页，切页只更换菜单身份及坐标，共用同一份库存；关闭绑定菜单不返还台面库存。分丹页槽 0/1/2 分别位于 `(61,53)`、`(97,53)`、`(79,17)`；合丹页分别位于 `(61,17)`、`(97,17)`、`(79,53)`。两页使用来源 176×166 画布、原图按钮与文字。来源 `stillValid` 恒为真，打开后远离不额外关窗；目标按钮核实当前菜单编号及原方块实体仍在，再由服务端执行。来源菜单还包含未绑定临时库存的关闭返还/死亡掉落机制，目标保留该后备行为；实际来源入口均以台面坐标绑定，没有查到物品或实体绑定调用者。Shift 移动保留来源的槽位过滤与显式写回能力库存。

`B:FendantaiFangKuaiShiTiXuanRanShiProcedure:27` 读取三个槽对应的 `<物品ID>block`，但仅绘制槽 2 的模型。全来源 Java 检索只有自身定义，没有方块实体渲染器或其他调用者；本批登记其炼丹归属和孤立状态，不把它接成新的运行入口，也不据此宣称台面瓶体视觉已迁移。

## 分丹与来源异常

`B:FendanButtonProcedure:23–96` 先要求槽 2 非空且 `danyaoname` 非空，不要求源物品属于药瓶标签；槽 0/1 至少一个属于 `pingguan`。来源把源与有效接收瓶都复制成临时钻石栈，清丹数后仅比较 Item，实际不比较丹名或药效。临时钻石不会进入库存，也不作为成品。

- 两侧均为有效瓶时恒走平分，覆盖接收瓶原有丹药，不累计其原丹数。
- 两个临时栈丹名均为空时也平分；缺少一侧容器仍可能进入此分支，该空槽分得的部分随源清空而丢失。非药瓶接收物在来源临时栈中相当于空名，但实际非空槽仍会被写入源数据。
- 其他情况左侧判定恒成立，走全部分给左侧；右侧独占分支不可达。左侧若为空，来源清源后丹药同样丢失。
- 平分要求源丹数至少 1，左侧量为 `Math.ceil(丹数 / 2)`，其中先做整数除法，所以 5 颗分为左 2、右 3；左侧独占分支不增加丹数正值条件。

接收时保留目标 Item 和栈数量，替换为源的完整实例数据及计算后的丹数；空 04/05 先生成一件 0401/0501。目标 `MedicineSeparation.fill` 用完整组件转移承接来源整份 NBT，保留自定义名、闪光及药效等实例信息。源槽整栈清空，仅 01、02、03、0401、0501、06 返回一件全新空容器，满盒返回 04/05；其他源物品直接消失。规则位置为 `B:FendanButtonProcedure:97–360`，目标不按提示文字自行修正来源分支。

## 合丹与来源异常

`B:HedanButtonProcedure:23–89` 要求槽 2 属于 `pingguan` 且丹名为空；槽 0/1 各自非空且有丹名，不要求属于药瓶标签、丹数为正或栈数量为一。

两源丹名相同时，逐项比较 `danyaoxiaoguoN`、`danyaolevelN`、`danyaotickN`，直到双方效果名同时为空；任一项不同则提示并拒绝消费。`danyaobaoxiang`、说明、自定义名等不在比较范围。丹名不同时，来源只提示“必须是相同的”，没有设置阻止合并的 `like` 标记，随后仍合并，保留左源全部实例数据和药效；目标照此执行。依据为 `B:HedanButtonProcedure:90–110`。

数量为两源 `danyaonum` 的 Java int 加法，不乘栈数量，保留溢出语义。目标容器 04/05 先变为满盒，其余保持目标 Item 与数量，再用左源完整组件和相加丹数覆盖。两输入整栈清空，各自只对上述六个来源 ID 返回一件全新空容器。依据为 `B:HedanButtonProcedure:111–240`；按钮、切页、库存与组件同步由 `MedicineSeparationMenu` 管理。

## 药理台库存与自然入口

来源 `B:block/YaolitaiblockBlock:64–66,85–123,135–156` 保留木材、木声、硬度 1、抗爆 5、水平朝向、含水和四向形状。任意右键打开原生药理菜单，没有职业、潜行或持物条件；左键开始破坏只清隐藏槽 11（`B:YaolitaiblockDangWanJiaKaiShiPoPiShiProcedure:14–19`），不清显示缓存或其他库存。既有 `yaolitaihecheng` 配方用药草标签、三木板、两木棍合成，目标仅做版本格式与命名空间转换。

`B:block/entity/YaolitaiblockBlockEntity:34–150` 保存十二格、每格最多一件。槽 0 为丹方媒介、1–8 为材料、9 为丹方说明载体、10 为丹药说明载体，11 为隐藏预览。槽 0–10 允许任意物品，槽 11 拒绝插入；六面暴露全部十二格但仅允许抽出 9/10，无面沿来源 `InvWrapper` 可抽出包括 11 在内的全部槽。隐藏槽的临时钻石因此可经无面能力取出，目标保留该能力边界。

来源方块完整类没有 `onRemove` 库存掉落调用，破坏、爆炸或替换台面时库存随方块实体移除而丢失；`getDrops` 仅在原战利品为空时回落一件台面自身，不补发库存。目标 `PharmacologyBlock` 同样不增加库存掉落。`PharmacologyBlockEntity` 保存完整实例组件、药性快照和显示缓存，提供完整更新标签/更新包及比较器输出。

`B:world/inventory/YaolitaiMenu:36–109,113–238` 的实际入口绑定台面无面库存，显示十一槽：0 `(79,62)`、1–8 `(16+18×(槽号−1),17)`、9 `(151,-19)`、10 `(169,-19)`。来源后备库存为十一格，关闭时只有未绑定菜单返还，死亡/断线掉落；`stillValid` 恒真。目标保留这些行为及来源 Shift 过滤/能力写回；物品和实体绑定模板未发现实际调用者。

## 药理台丹方预览与显示

`B:YaolitaishuaxinProcedure:33–367` 创建一件临时钻石承载预览，空输入或字面“丹方名称”均按槽 1 完整显示名字符串 `substring(1,3)` 加“丹”自动命名。没有槽 1 非空或材料标签前置条件。八格材料按槽号与 `zong` 中药性次序聚合，复用[药效公式](#开炉药效与数量)的匹配与整数运算；同名扫描含 `shuliang` 末尾额外一格，新效果乘/除不写初始数值。此处不消费材料，不在首个妖丹后停扫，不计算实际炉次、经验或炸炉。

结果覆盖隐藏槽 11，只有药效字段、`shuliang` 和丹名，没有丹数、成品容器 ID 或自定义显示名。临时钻石不是可服用丹药，不进入可见成品槽。`B:XianshijisuanProcedure:19–77` 随后只清 `effectxianshi0`–`9`，再遍历至首个空效果名，等级至少 100 的项按原下标显示“L级<效果译名>：S秒”，`L=int/100`、`S=int/20`；不压缩被跳过的索引。

来源 `Effectxianshi00`–`12` 逐项读取装置缓存，屏幕显示十三行，故 10–12 行可能残留旧预览；`Danyaonamexianshi` 读取隐藏槽 11 的丹名。来源刷新过程没有主动发送方块更新，按钮同时在本地和服务端执行，目标保留两端各自计算显示缓存及服务端隐藏库存的方式。实体自然放置时发送的快照用于客户端计算，不由客户端回写服务端库存。

来源 `B:client/gui/YaolitaiScreen:54–77,143–200` 使用 176×166 画布，右侧 104×166 药效面板位于 `(177,0)`，十三行位于 `(186,34+9×行号)`。输入框 `(27,-21)`、120×20、最大 32767 字符；写入图按钮 `(52,37)`、70×21，刷新图按钮 `(183,6)`、16×16。目标使用来源原图与原生输入框。

输入只保存在来源 `YaolitaiMenu.guistate` 的客户端静态映射，`YaolitaiButtonMessage:66–72` 不传文本。集成服务端与客户端共享静态输入，独立专服没有该输入，因此写媒介使用自动名，而客户端预览可显示输入名。目标 `PharmacologyMenu` 保留同一静态名称及不含文本的 payload，不自行补齐来源专服文本断链。多人名称串用与完整专服输入差异尚未实测。

## 药理台媒介写入

`B:YaolitaiXieruProcedure:32–142` 仅要求槽 0 非空、槽 1–8 的栈数量总和至少 1，不要求药草标签、特定媒介、空丹方或槽 1 非空；材料和媒介均不消耗。媒介缺失与无材料分别发送来源提示。

写入名称仅对空字符串使用上述自动名，字面“丹方名称”在此直接成为名称，与刷新不同。新数据包含丹名、自定义显示名、槽 9/10 完整显示名字符串作为 `danfangshuoming/danyaoshuoming`，以及 `cailiao01`–`08` 的准确物品 ID 和完整显示名。空材料槽仍写 `minecraft:air` 与空栈显示名，不省略，也不读取或复制槽 11 的药效。

来源最后对真实菜单槽 0 的栈执行 `setTag(新临时栈tag.copy)`，完整替换原 tag、保留 Item/count，实际写入有效；目标 `Pharmacology.write` 以同 Item/count 的全新栈承接物品默认组件，再写本次名称和丹方数据，舍弃原自定义名、附魔等实例组件。此处不能复用丹炉“保存按钮只改副本、无写回”的异常。写出的丹方随后由丹炉按其既有装料和命名规则消费，不把药理台预览字段当作直接成丹结果。

## 百草鉴自然入口与样本库存

`B:item/BaicaojianbookItem:16–29` 的工具栏书本最多一件、普通稀有度，空气右键调用开窗后仍返回父类原 PASS 和原调用栈，不消耗书、不要求潜行。两份无序合成分别以任意 `caoyao` 标签物品配 `book` 或 `writable_book` 获得一件百草鉴；第二份来源文件准确名称为 `baicaojianbookhecheng_2.json`。

`B:BaicaojianbookDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:26–38` 仅传玩家当前位置的 BlockPos，标题为 `Baicaojian`，没有绑定书本。`B:world/inventory/BaicaojianMenu:38–95` 的模板随后尝试获取该位置方块的无面物品能力，因此玩家脚部坐标恰有可访问库存时会意外绑定其槽 0；目标保留该路径，不改成永远使用临时库存。没有发现此菜单的物品或实体绑定调用者。

未绑定时使用一格临时库存、上限 64；NeoForge 21.1.265 的 `ItemStackHandler` 默认上限已改为 `Item.ABSOLUTE_MAX_STACK_SIZE`，目标显式覆写 64 以保留来源。绑定时使用真实能力的容量和已有物品。槽 0 位于 `(30,-13)`，仅允许 `yvanchuxiuzhen:caoyao`；`stillValid` 恒为真。Shift 合并保留来源 `mayPlace(existing)` 检查与显式 `slot.set` 写回，空槽插入一格后停止。未绑定菜单关闭时返还样本，死亡或断线时掉落；绑定库存关闭保留。

目标 `HerbAnalysisBookItem`、`HerbAnalysisMenu`、`HerbAnalysisPayload` 与 `HerbAnalysisScreen` 使用原生客户端菜单。画布 176×166，背景 `baicaojian`，覆盖图 `baicaogui01` 位于 `(-12,-39)`、202×120；刷新/辨识按钮分别 `(47,-13)` 和 `(-7,22)`、16×16。标题、进度和成本分别位于 `(-3,-27)`、`(-3,7)`、`(5,26)`；十条药性在 `(93,-27+10×索引)`，空缓存也保留来源冒号行；三条帮助位于 y167/177/187。按钮只发送当前容器编号与 0/1 操作，服务端核实真实菜单和打开坐标区块已加载后执行。

## 药草刷新与逐项辨识

`B:BaicaojianShuaxinProcedure:35–110` 在槽 0 为空或不是药草时直接结束，保留旧显示缓存。有效样本只在旧 `txtname` 非空时清标题、进度、成本、物品 ID 和十行文本，不清各药草已揭示的真值；随后按准确 Item ID 查运行目录更新该 ID 的解析上限。目录未收录时保留该 ID 原有上限，不生成默认药性。打开菜单本身不刷新或清缓存。

显示名称使用样本完整 `getDisplayName().getString()`，保留原生方括号。进度为 `p/max`；只有 `p>=1` 且 `p>=max` 才显示完全解析，否则显示下一项成本，包括 `p=0,max=0` 时仍显示 4 个。已揭示药效名称及说明取玩家保存值，不随目录即时重算。十行均按 `§b<效果译名>:§8<原始说明>` 绘制；刷新只写自身附件，客户端读取同步快照。

`B:BaicaojianjiandingProcedure:41–48` 要求样本为药草。缓存标题为空或缓存 ID 与当前样本不同，第一次辨识只刷新，不扣料或揭示。`p<max` 时才尝试下一项，成本为 Java int 表达式 `4 + 4×p`；不使用千材录的公式或材料仓。

来源 `:49–89` 通过玩家实体 `IItemHandler` 按 Item 身份累计同类库存，不比较实例组件；可用量是该数量与样本数之和。真正扣费使用玩家普通 `Inventory.clearOrCountMatchingItems`，连同 `inventoryMenu` 的 craft slots 作为参数，先扣普通库存，不足部分再从菜单槽 0 扣除。统计能力与扣费入口不同的来源边界保持，不改为能力逐格抽取。

成功后先令 `p+1`，按新序号从 1 开始揭示目录中的一项效果，保存原效果 ID 和 `药效<符号><数值>点；持续时间<符号><数值>刻`，重新写上限但不覆盖旧已揭示项；这些是原始药性，不是聚合后丹药等级/时间。费用不足发送来源红字，仍保存、同步并刷新；已经完全解析只提示，不再扣费。样本整栈恰好耗尽时，后续刷新因空槽直接返回，玩家真值已经递增而屏幕仍保留先前标题、进度和成本，目标保留该缓存残留。

## 药草提示、全解锁与玩家生命周期

`B:LoreProcedure:222–245` 的草药分支在前置装备和药瓶分支未命中后执行，追加金色“炼丹材料”。上限为 0 时显示红色“还未开始解析”，否则未完成时显示青色进度；`p>=1` 按保存顺序显示效果译名与原始符号说明，完全解析不另加完成行。无命名空间效果走 `effect.minecraft.<名称>`，有命名空间则把冒号换为点再加 `effect.`。其他系统只读炼丹快照，不能修改玩家解析附件或把同物品多种标签分支叠加显示。

`B:JiesuocaoyaoProcedure:24–74` 按完整运行目录逐项令 `p=max` 并保存所有原始效果和说明，包含未注册的 `nichanggrass` 原 ID，不清菜单显示缓存。目标由 `LiandanHerbCommand` 按当前维度 displayName 分派到 `Liandan.unlockHerbs`，再调用 `LiandanHerbAnalysis.unlockAll`；没有来源自定义重置入口，不新增 reset 操作。

来源命令名实际为 `/yvanchuxitong`（`B:command/YvanchuCommand:26`），权限 1；贪婪字符串按空白拆参数。`B:YvanchuGoProcedure:23–35` 只遍历执行者当前 ServerLevel 的玩家，以 `displayName.getString()` 精确匹配参数 0；参数 1/2 为 `unlock yaocai` 时解锁。参数 3 等于 `true` 只控制向执行者显示成功消息，不是解锁条件；命令返回 0。目标 `LiandanHerbCommand` 保留该分支并由 `LiandanData.register` 挂载，公共命令解析仍归 flow，炼丹只写自己的附件，不把整个来源命令归入炼丹。

`B:network/YvanchuxitongModVariables:88` 在死亡判断外无条件复制 `playercaoyaojiexi`，`:308,333,355,396` 完成定义、保存、读取与本人同步，`:54–68` 在登录、重生和换维度时同步。目标 `LiandanData.HERB_ANALYSIS` 使用可保存附件、`copyOnDeath` 与仅向持有者本人同步的配置；`HerbAnalysis` 对原始 CompoundTag 构造和读取均复制，界面不能反写整份玩家档案。内置客户端已验证真实死亡重生、退出重进后服务端与本人客户端数据保留且一致；非死亡重建和跨维度仅完成静态核对。

## 药草字段访问覆盖

`typed_field_accesses.csv` 中 `playercaoyaojiexi` 有 20 处（17 处读取/访问、3 处显式写回），`caoyaolist` 有 5 处（4 读、1 写），合计 25 处。变量定义、序列化、克隆和同步属于模板内部生命周期，不在该 CSV 内，已另按上述 Variables 来源核对。下表中的过程均为 B 来源，未省略跨系统访问。

| 字段 | 过程 | 来源行 | 访问 | 所有权与目标去向 |
| --- | --- | --- | --- | --- |
| playercaoyaojiexi | Baicaojianjianding | 43 | 读取/访问 | liandan，辨识内部读取 |
| caoyaolist | Baicaojianjianding | 94 | 读取/访问 | liandan，运行药性目录 |
| playercaoyaojiexi | Baicaojianjianding | 120 | 写回 | liandan，辨识附件写入 |
| playercaoyaojiexi | BaicaojianShuaxin | 37 | 读取/访问 | liandan，刷新缓存 |
| caoyaolist | BaicaojianShuaxin | 55 | 读取/访问 | liandan，运行药性目录 |
| playercaoyaojiexi | BaicaojianShuaxin | 108 | 写回 | liandan，刷新附件写入 |
| playercaoyaojiexi | Baicaojiantxtjiesuoneed | 14 | 读取/访问 | liandan，成本显示快照 |
| playercaoyaojiexi | Baicaojiantxtjindu | 14 | 读取/访问 | liandan，进度显示快照 |
| playercaoyaojiexi | Baicaojiantxtname | 14 | 读取/访问 | liandan，标题显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao10 | 14 | 读取/访问 | liandan，第十条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao1 | 14 | 读取/访问 | liandan，第一条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao2 | 14 | 读取/访问 | liandan，第二条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao3 | 14 | 读取/访问 | liandan，第三条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao4 | 14 | 读取/访问 | liandan，第四条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao5 | 14 | 读取/访问 | liandan，第五条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao6 | 14 | 读取/访问 | liandan，第六条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao7 | 14 | 读取/访问 | liandan，第七条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao8 | 14 | 读取/访问 | liandan，第八条药性显示快照 |
| playercaoyaojiexi | Baicaojiantxtyaoxiao9 | 14 | 读取/访问 | liandan，第九条药性显示快照 |
| playercaoyaojiexi | Jiesuocaoyao | 24 | 读取/访问 | liandan，解锁内部读取 |
| caoyaolist | Jiesuocaoyao | 27 | 读取/访问 | liandan，运行药性目录 |
| playercaoyaojiexi | Jiesuocaoyao | 71 | 写回 | liandan，解锁附件写入 |
| caoyaolist | LiandanYaojixiaoguo | 44 | 读取/访问 | liandan，实体放置药性快照 |
| playercaoyaojiexi | Lore | 224 | 读取/访问 | panel 公共提示分派，仅只读炼丹查询 |
| caoyaolist | Worldbegin | 48 | 写回 | flow 世界加载定义，由 liandan 运行目录承接 |

## 未实现范围与验证边界

当前覆盖丹炉、炉次、丹方装料/保存原行为、药性目录、职业/丹火接口，八种药瓶消费与方块生命周期、分丹台分合丹、药理台丹方预览/媒介写入，以及百草鉴辨识、药草提示、解锁和玩家附件。毒药结束回调、`duedan` 丹药与 `Grind` 命令待后续核对实现；基础注册不等于玩法完成。丹方购买、宝箱和副本获取由对应系统接回。

此前“保存操作者、固定输入快照”等属于未批准提案，本批没有据此新增炉主记录或快照机制。后续数据设计继续依据实际来源与已批准所有权，不能从旧提案推定决定。

丹炉批次的来源静态核对与完整 build 已完成；内置客户端最终13项通过，黄铜炉的自然放置、装料开炉、供热、完成覆盖、组件同步、保存重读与取丹经验已验证，专服及文档台账结果见本月记录。药瓶批次来源静态核对与完整构建通过；既有内置客户端19项全INFO、0FAIL、DONE，含本批六项实际服用、数量合并、效果/实例同步、生存潜行放置与单槽保存重读、空手拾回及对实体喂药。主手葫芦与自然满盒样本已验证；双手同ID、创造、完整Tooltip、自动化与破坏掉落、八容器逐一操作、宝箱丹药与所有无效命令分支仍未实测。

分丹台批次完整构建与既有检查源集编译通过；既有内置客户端25项全INFO、0FAIL、DONE，六项分合丹覆盖自然放置开窗、实际分丹按钮、双页共享库存、真实合丹、完整组件保存重读/同步、方向能力和来源同名药效拒绝/异名仍合并。两页截图已查看，专服加载后正常stop保存四维度，ERROR/FATAL0。分合丹全部异常分支、Shift、六面真实自动化、破坏掉落与重登尚未专项实测。

药理台来源静态核对、构建、既有内置客户端28项及专服正常保存退出通过。三项新增路径覆盖自然放置/开窗、预览与双端显示、实际写入丹方/全部旧实例组件替换、隐藏库存保存重读及方向能力；截图已查看。左键清除、破坏库存丢失、真实自动化、重登和专服名称差异未专项验收，结果见月度记录。

百草鉴本批来源静态核对完成，构建、内置客户端54条 INFO/0FAIL/DONE、专服正常加载保存退出及文档台账检查均通过，证据见[本月记录](logs/2026-11.md)。54条包含原有仓储、材料与职业检查；百草鉴新增六条覆盖自然开窗/槽过滤、首次仅刷新、背包优先扣料、费用不足、背包加样本联合扣料、样本耗尽缓存和Tooltip同步，并复用死亡重生、退出重进验证解析附件。全37草逐项辨识、无面库存绑定、断线掉落、跨维度、Tooltip标签优先级、自然合成、解锁命令与完整视觉未专项验收。

1.18.2 版本实机、两版本对照、多人、完整视觉、全部药性组合、丹炉自动化隐藏槽、完整药性快照与未完成炉次卸载重载均未验收。各批运行证据写入[本月记录](logs/2026-11.md)，月份导航见[记录索引](changes.md)，不把未执行检查计为通过。
