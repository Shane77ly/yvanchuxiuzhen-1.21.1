---
topic: profession
role: architecture
status: partial
updated: 2026-11-15
---

# 副职业：规则与实现状态

来源定位沿用[系统导航](../README.md)的 A/B 规则。十职业核心与矿石采集已接入主Mod，下文分别登记已执行客户端证据和剩余入口，不以核心通过标记整个系统完成。

## 1.18.2 数据与入口

十种职业按 `Alchemy`、`Refiner`、`Rune`、`collection`、`cooking`、`puppet`、`breed`、`ghost`、`insect`、`blood` 顺序检查。每种职业各有 `_LEVEL`、`_experience_Max`、`_experience` 三个独立 double，合计30个数值；血脉等级不计入副职业。默认值为等级0、经验上限100、经验0（`A:network/YvanchuxiuzhenModVariables.java:487–516`），所有克隆事件复制（同文件114–143），序列化与同步由能力承载。具体字段见[进度台账](ledger/progress.csv)。

`A:PlayerTickProcedure:31–35` 订阅玩家每刻 END，394行无间隔调用 `FuzhiyeLevelUp`。`A:FuzhiyeLevelUpProcedure:13–292` 逐职业执行独立的 if：等级小于9且经验达到旧上限时，等级加一、经验减去旧上限、上限改成新等级乘100，并向服务端玩家发送金色聊天提示。每职业每刻最多升一级，各职业可以同刻分别升级；不用 while。升到九级后停止升级且保留全部剩余经验和当前上限。命令设置的小数、负数、超过九级的数值不在这里校正。

`A:ZhiyeTXT01`–`20` 为十职业成对的等级和经验文本：等级 `DecimalFormat("0")`，经验采用 double 的 `当前/上限` 字符串；符箓等级文案为“画符等级”，升级反馈为“绘符术提升一级！”。

## 已实现核心（2026-11-08）

`ProfessionSetup.register(modBus)` 注册 `yvanchuxiuzhen:profession` 玩家附件、玩家每刻 Post 升级、登录/重生/跨维度后的本人同步与矿石采集监听。主Mod调用已接入；新增职业页由 `ProfessionClientSetup` 注册原生screen。

`ProfessionData` 只保存完整的十职业映射，`ProfessionProgress` 的等级、经验、上限分别序列化。来源的默认状态在新建附件时产生，读档缺失职业会报Codec错误，不静默补造存档。附件使用 `copyOnDeath`，通过 `holder == player` 限制同步目标。可变档案只由profession写入，每次业务写入同步完成后的快照；客户端查询API不提供反写整份数据的网络入口。目标机制已静态核对 `neoforge-21.1.265-sources.jar` 中 `AttachmentType`、`PlayerTickEvent` 和 `BlockEvent`，死亡复制与重登已有下述客户端证据，跨维度尚未专项实机。

公共接口见[README](README.md#公开操作)。经验写入与升级分开；`setLevel` 按来源管理命令只改等级，不重新计算上限；`reset` 提供浮生丹的十职业重置片段。来源 `A:LeftClickBlockProcedure:362` 把采集等级抄到实体持久键 `collection_LEVEL`，供原初系统连锁采集读取；当前在等级写入、升级、重置与登录/重生/跨维度时维护该派生镜像，附件仍为唯一真值。

## 矿石采集自然入口

`Gathering` 接回 `A:BlockDestroyProcedure:71–111` 的采集片段。破坏 `forge:ores` 对应的公共 `c:ores` 或本命名空间 `ores` 时，主手或副手来源工具等级达到当前方块硬度便增加2点采集经验。该条件比较数字等级与硬度，不能改成“工具能正确采掘”。1.21.1 的本 Mod 工具由 `LegacyTier.level` 保留来源等级，原版工具按对应不能正确采掘标签恢复木/金0、石1、铁2、钻石3、下界合金4，与既有内容表适配一致。

随后用独立随机源在 `[1,100)` 掷一次 double：小于等于采集等级乘0.5时还原并挖掉该方块3次、发送10个电火花；否则小于等于采集等级时还原并挖掉2次、发送5个电火花。没有改成从0开始或两次独立百分比判定。两分支粒子均为 `ELECTRIC_SPARK`，中心为方块坐标加0.5，三轴偏移0.1，速度1，与来源一致。额外掉落不传工具或玩家，不继承时运和精准采集，沿用来源的还原后掉落、无掉落移除顺序。来源没有创造模式排除。六面相邻 `tiandaoguding` 时整个采集分支跳过；禁止破坏的取消行为归天道/共享破坏入口，本批未接回那段保护。

真实原版矿石破坏已实机触发并增加2点采集经验。与现有combat连锁采集同次破坏的监听顺序、低概率额外掉落和粒子、主副手各边界数值条件及相邻天道固定块仍待专项验收。

## 外部字段访问与后续接入

本表对照 `typed_field_accesses.csv` 中本系统30个字段的全部外部类；名称或目录只是检索，行为以锁定源码为准。

| 来源入口 | 读取或写入 | 目标接入状态 |
| --- | --- | --- |
| `A:BlockDestroyProcedure:78–81` | 矿石合条件加2采集经验，读取采集等级决定额外掉落 | `Gathering` 已接入；真实矿石加2已通过，工具边界与额外掉落待专项 |
| `A:LeftClickBlockProcedure:362` | 采集等级写入旧持久键 | 核心生命周期与等级写操作维护镜像 |
| `A:YvanchuGoProcedure:79–511` | 权限1的 `/yvanchu` 目标玩家循环，`levelup` 修改十职业等级；未改经验上限 | `PlayerCommandFlow` → `Professions.command` → `ProfessionCommand` 已接；不联动经验和上限，有限真实命令已验，边界见本月志 |
| `A:YvanchuGoProcedure:512–513`、`A:YvanchuGo2Procedure:62–480` | `experienceup` 修改十职业经验，set赋值，其余分支加减 | 同一共享命令已接，`set` 赋值、其他操作加参数值；不立即升级，有限真实命令已验，边界见本月志 |
| `A:Fulutexiao37Procedure:327–536` | 十职业重置为0/100/0 | 已提供 `reset`；轮回/浮生丹所有者接入，血脉字段不归本系统 |
| `A:Alchemy01ZaiFangKuaiShangYouJianProcedure:37` | 开炼丹炉时复制炼丹等级到装置 | `AlchemyRuntime` 开窗已复制等级到炉内；自然全链与边界见炼丹正文 |
| `B:JingyanhuoqvProcedure:18–29`、`B:network/LiandanGUISlotMessage:73–80` | 炼丹槽9的changeType1/2操作；非炼制中且装置经验至少1时领取炼丹经验，炸炉除4并清标记，DecimalFormat("0")取整后通过 `/yvanchu` 加经验，随后装置经验清零 | `LiandanBrewing.claimExperience` 已经公开 API 写入，领取条件、炸炉除4、取整与清零保留在炼丹生命周期 |
| `B:Tripod01ZaiFangKuaiShangYouJianProcedure:43` | 开炼器鼎时读取炼器等级、写入鼎炉 `RefinerLEVEL` | `TripodRuntime` 开窗已复制等级到鼎炉 |
| `B:QvchuProcedure:33–37`、`B:network/TripodGUIButtonMessage:101–103` | 炼器GUI按钮7取出产物前，把装置int `jingyanhuoqv` 通过 `/yvanchu` 加炼器经验，随后装置经验清零 | `TripodRecipes.claim` 已经公开 API 写经验并清装置值；实际条件见炼器正文 |
| `A:RightClickBlockProcedure:78` | 符箓入口复制绘符等级到 `RuneLEVEL` | `DrawingTableRuntime` 打开画符台已复制等级 |
| `B:HuizhifuluProcedure:220–223`、`B:network/HuafuGUIButtonMessage:99` | 绘符输出成功时通过 `/yvanchu` 添加 DecimalFormat("0")后的绘符经验并显示经验反馈；随后消耗输入 | `DrawingTableRuntime` 已在绘制成功链经公开 API 写经验；费用、输出和反馈见符箓正文 |
| `A:FooddaguoZaiFangKuaiShangYouJianProcedure:24,238–247` | 复制厨艺等级，领取产物时把 `cookexp` 加到玩家厨艺经验后清零装置值 | `CookingRuntime` 已在大锅产物领取条件内写经验并清装置值 |
| `A:FooddunguoZaiFangKuaiShangYouJianProcedure:24,107–116` | 同上，炖锅 | 共享 `CookingRuntime` 已接；不额外提供脱离装置的经验入口 |
| `A:FoodzhengguoZaiFangKuaiShangYouJianProcedure:24,100–109` | 同上，蒸锅 | `SteamerRuntime` 已在产物领取条件内写经验并清装置值 |
| `A:EntityDeathProcedure:151–182,206–254` | 蛊虫自身经验升级后，向同世界 UUID 查得的主人增加蛊虫新 `LEVELnumber ×10` 养蛊经验；存在两段来源分支 | `GuchongGrowth` 已在升级和同世界主人查找后经公开 API 写入；不增设泛化击杀经验 |
| `A:ZhiyeTXT01`–`20` | 十职业成对文本 | 已提供文本 API，05实际显示页与六主分页导航已接，必要单项已验，完整视觉待验 |

炼丹、炼器、绘符以跨 Mod 管理命令间接增加经验，不能只查 Java 字段赋值而遗漏。其命令以权限4、无执行实体发出，但 `A:command/YvanchuCommand:26–48` 已注册 `/yvanchu`、把空执行实体替为 FakePlayer，并由参数中的玩家显示名选择目标，故三条已注册命令具有消费者；后续改用公开 API 时保持各生产入口的取整、炸炉减额、装置清零和金色经验反馈。傀儡、御鬼、御灵、血道在本轮两个锁定工程的字段与命令字符串搜索中未发现自然经验生产者，不因职业名称增加合成经验或击杀经验。血道的吸血石、血流、血精不使用职业进度，归 xiulian。

## 十职业管理命令（2026-11-15）

来源 `A:command/YvanchuCommand:26–70` 注册 `/yvanchu`，权限 1，参数采用贪婪字符串并按空白拆分。目标唯一注册归 `flow.PlayerCommandFlow`，按命令源当前维度玩家的 `getDisplayName().getString()` 精确匹配首个目标；找不到时沿用执行实体或无实体来源的 FakePlayer，发送原“未找到名为…”反馈，返回值仍为 0。有效目标上的职业分支先由 `Professions.command` 识别，其余已迁移分支交所属系统处理，世界结构分支仍经 `WorldCommands`，不重复注册同名贪婪参数节点。

职业语法为 `/yvanchu <玩家显示名> levelup|experienceup <职业> set|其他操作 <数值> [true]`；职业关键字按来源恰为 `alchemy/refiner/rune/collection/cooking/puppet/breed/ghost/insect/blood`，不把目标存档枚举中的 `refining/gathering/breeding` 当作命令别名。`set` 是唯一赋值操作，其余任何字符串都在旧值上加参数值；负数、小数和超过九级的值不校正。数值采用 `Double.parseDouble(trim)`，格式错误或缺参数时按来源取 0，不增加 Brigadier 数值范围；经验写入不立即升级，等级写入不联动经验或上限，采集等级镜像继续由既有公开操作维护。例：旧炼器等级 2.5，`levelup refiner add -0.5` 得 2；旧炼丹经验 10.5，`experienceup alchemy minus -2.25` 得 8.25，旧上限均不变。

可选参数恰为 `true` 时才向目标玩家发金色反馈；`set` 和经验加减显示原始参数字符串，厨艺分支仍用来源“仙厨”。本批复用已有 `setLevel/setExperience/addExperience`，没有重写附件、同步、升级或自然生产入口。炼丹、炼器、绘符、三厨具和蛊虫的现有职业消费者已由实际代码回核并更新上表，不重复实现；轮回37/浮生丹涉及多个数据所有者且仍有待裁定清理边界，本批不以单独职业重置替代该链。

### Q188 等级加减反馈错读修炼层数

来源 `A:YvanchuGoProcedure:118/161/204/247/290/333/376/419/462/506` 的十个非 `set` 分支均正确写入本职业 `_LEVEL`，但紧随其后的“等级变更为”统一读取 `LEVELnumber`。例如修炼层数 7、炼丹等级 2，增加炼丹等级 1 后实际职业等级 3，开启反馈却显示“炼丹等级变更为7.0”。目标 `ProfessionCommand` 通过修炼公开查询保留该原文案结果；`set` 分支仍显示原参数。状态为待裁定、保持来源，未执行 1.18.2 版本或 1.21.1 版本反馈实机；不把来源显示疑点当作字段写入错误。

本批静态核对来源十职业映射、20组set/加减分支、反馈条件、权限/定位/数字语义与世界结构原有分派；生产/Smoke编译及53项真实命令/本人同步必要检查通过。死亡/保存重进、结构开关和解封回归未执行。历史 storage 核心验收不替代新共享命令入口的验证。

## 连锁采集命令消费断链

`B:ChainProcedureProcedure:63` 从 `collection_LEVEL` 读取 int，额外矿石处理累计经验；151–155行尝试以无执行实体的 `CommandSourceStack` 执行 `collection experience add <数量>`。两个锁定工程全部命令注册类均未注册 `collection`，有效管理职业的路径是权限1的 `/yvanchu`，参数还需目标玩家与 `experienceup` 等前缀。因此来源该字符串没有有效经验消费者；不能与上表确有注册的 `/yvanchu` 生产命令混同。

目标 combat 已发出 `GatherExperienceEvent`。本批保持来源可达性，不订阅该事件增加职业经验，也不新增 `collection` 命令把断链变成新玩法。连锁采集的矿石额外掉落仍能从派生镜像读取等级；经验事件的最终去向应由来源矛盾裁定后处理。此结论来自静态源码，两个版本实机均未执行。

## 验证与剩余范围

静态来源与目标精确版本机制核对后，主会话已接入setup，并沿用既有 `runSmokeClient -PsmokeOnly=storage` 进入真实1.21.1版本客户端。全场景45项玩法检查PASS、0FAIL、DONE，另外2条INFO为过程说明；职业检查通过实际原版矿石破坏增加2点经验、自然玩家每刻Post升级减去旧经验上限并同步本人，以及真实死亡重生、退出重进后采集等级保留且双端相同。该结果不代表十职业所有升级边界或全部自然生产者已验收。

证据 `tmp/material-storage-client.log`、`tmp/material-storage-final-report.txt`、`tmp/material-storage-final-latest.log`。最终客户端WARN37（含已有厨具缺失纹理）、ERROR/FATAL0。首次共用场景因材料袋06工厂漏接失败，修复后重跑通过，`tmp/material-storage-first-*` 保留。最终 `build compileSmokeJava`（含既有test/check/ArchUnit）及专服启动至Done后原生stop均通过，四维度正常保存退出、ERROR/FATAL0、菜单18个；证据 `tmp/material-storage-build-final.log`、`tmp/material-storage-server-latest.log`。未新增Test类、检查类或冒烟选择器。

未完成：浮生丹自然重置；管理命令其余边界、三厨具/炼丹/炼器/绘符/蛊虫生产的完整自然链与边界验证；职业05页和六主分页导航已接，必要单项通过，完整视觉仍待；跨维度、真实1.18.2版本实机、两版本对照及多人。主手达标/副手达标/双手不足、普通工具硬度差异、相邻天道固定块跳过、同次连锁事件顺序和低概率额外掉落/粒子尚未专项实机；十职业批量经验逐刻升级、九级剩余保留等边界仍需对应路径验证。进度核心通过不等于上述入口完成。

## 副职业05页（2026-11-15）

锁定来源 `A:client/gui/PlayerGUI05Screen`、`A:world/inventory/PlayerGUI05Menu`、`A:network/PlayerGUI05ButtonMessage` 和 `A:PlayerGui05jieshao`。目标 `ProfessionMenu` 保留36格原生背包，`ProfessionScreen` 保留500×500坐标、gui05底图、双列十职业等级/经验和六行导航标签，显示只调用本人同步的 `Professions.levelText/experienceText`，不写进度。原三行说明照来源显示；经验字符串仍为double形式。

05菜单与客户端注册归profession，`PanelNavigation`调用 `Professions.openPage`并保存页5；01/03/04进入05、05返回01/03/04和K恢复05均已接线，跨主分页仍沿用旧菜单坐标Y+1。02/06按钮也已由各自所有者接齐。丹田返回既有记页契约扩展到5，未增设05丹田按钮。

静态来源、语言与资源核对后，生产/Smoke编译及05页必要单项通过，截图已复看；原件见[本批集成记录](../project/logs/2026-11.md#2026-11-15-页面命令阵法与展示家具集成验证)。专服、1.18.2版本实机、全缩放、完整视觉和两版本对照未执行。
