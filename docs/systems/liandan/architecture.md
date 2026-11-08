---
topic: liandan
role: architecture
status: partial
updated: 2026-11-08
---

# 炼丹、药理、药物与毒药

本页记录已核对的 1.18.2 版本来源规则与 1.21.1 版本实现范围。源码定位采用[系统导航](../README.md)的 A/B 规则，公开操作见[系统入口](README.md#公开操作)。2026-11-08 八档丹炉自然炼制链的构建、内置客户端13项和专服正常停服已通过；本批新增八种药瓶的服用、喂药、显示、放置拾回及单槽库存，来源静态核对、构建与内置客户端19项检查已通过。证据与边界见[月志](logs/2026-11.md)；药理、辨识和分合丹尚未实现，不能据此标记系统完成。

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

`B:LiandanYaojixiaoguoProcedure:26–44,45–92` 仅在实体放置丹炉或药理台时生成 `zong`。每条先写 `<物品ID>number`，再按顺序写效果名称、等级操作/数值、时间操作/数值。装置保存独立快照；指令放置和结构恢复不补造实体放置事件。药理台快照编辑与放置消费者仍待本系统后续批次。

目标 `LiandanHerbs` 从 `content/liandan/herbs.json` 生成相同键结构，`tools/porting/convert_content.py` 的 `convert_herb_definitions` 抽取 Worldbegin。目录中的 `yvanchuxiuzhen:nichanggrass` 未注册，实际注册为 `nichanggrass_01`–`07`；保留目录错名，不重定向到 `nichanggrass_07`，该行没有自然匹配物品。保留定义不能登记为该药材药性已自然生效。

药草目录及玩家 `playercaoyaojiexi` 辨识属于炼丹，与 [material](../material/README.md) 的千材录辨识不同。玩家辨识、`Lore` 提示与 `Jiesuocaoyao` 写入口仍待接回。

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

时间除法的整数分母是来源 `307–328` 实际写入表达式，其 double 临时值未使用，不能套用等级除法。目前 100 条目录药性没有除法项，药理台后续编辑仍须保留其语义。未知操作不写对应字段。

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

## 未实现范围与验证边界

当前覆盖丹炉、炉次、丹方装料/保存原行为、药性目录、职业/丹火接口，以及八种药瓶的自服、堆叠、空容器返回、喂药、Tooltip 和方块生命周期。分丹台与分合丹装填、药理台编辑、百草鉴辨识、毒药结束回调、`duedan` 丹药、`Grind` 命令与 `Jiesuocaoyao` 待后续核对实现；基础注册不等于玩法完成。丹方购买、宝箱和副本获取由对应系统接回。

此前“保存操作者、固定输入快照”等属于未批准提案，本批没有据此新增炉主记录或快照机制。后续数据设计继续依据实际来源与已批准所有权，不能从旧提案推定决定。

丹炉批次的来源静态核对与完整 build 已完成；内置客户端最终13项通过，黄铜炉的自然放置、装料开炉、供热、完成覆盖、组件同步、保存重读与取丹经验已验证，专服及文档台账结果见本月记录。药瓶批次来源静态核对与完整构建通过；既有内置客户端19项全INFO、0FAIL、DONE，含本批六项实际服用、数量合并、效果/实例同步、生存潜行放置与单槽保存重读、空手拾回及对实体喂药。主手葫芦与自然满盒样本已验证；双手同ID、创造、完整Tooltip、自动化与破坏掉落、八容器逐一操作、宝箱丹药与所有无效命令分支仍未实测。

1.18.2 版本实机、两版本对照、多人、完整视觉、全部药性组合、丹炉自动化隐藏槽、药性快照和未完成炉次卸载重载均未验收。各批运行证据写入[本月记录](logs/2026-11.md)，月份导航见[记录索引](changes.md)，不把未执行检查计为通过。
