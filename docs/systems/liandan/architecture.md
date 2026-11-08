---
topic: liandan
role: architecture
status: partial
updated: 2026-11-08
---

# 炼丹、药理、药物与毒药

本页记录已核对的 1.18.2 版本来源规则与 1.21.1 版本实现范围。源码定位采用[系统导航](../README.md)的 A/B 规则，公开操作见[系统入口](README.md#公开操作)。2026-11-08 本批实现八档丹炉自然炼制链，构建、内置客户端13项和专服正常停服已通过，证据与边界见[月志](logs/2026-11.md)；丹药服用、药理、辨识和分合丹尚未实现，不能据此标记系统完成。

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

## 未实现范围与验证边界

本批覆盖丹炉、炉次、丹方装料/保存原行为、药性目录与职业/丹火接口。八种药瓶及葫芦的装填、服用和对应方块，分丹台与分合丹，药理台编辑，百草鉴辨识，毒药结束回调，`duedan` 丹药、`Grind` 命令与 `Jiesuocaoyao` 待后续核对实现；基础注册不等于玩法完成。丹方购买、宝箱和副本获取由对应系统接回。

此前“保存操作者、固定输入快照”等属于未批准提案，本批没有据此新增炉主记录或快照机制。后续数据设计继续依据实际来源与已批准所有权，不能从旧提案推定决定。

上述来源静态核对与完整 build 已完成；内置客户端最终13项通过，黄铜炉的自然放置、装料开炉、供热、完成覆盖、组件同步、保存重读与取丹经验已验证，专服及文档台账结果见本月记录。1.18.2 版本实机、两版本对照、多人、完整视觉、全部药性组合、自动化隐藏槽、药性快照和未完成炉次卸载重载均未验收。证据收尾由主会话写入[本月记录](logs/2026-11.md)，月份导航见[记录索引](changes.md)，不把未执行检查计为通过。
