---
topic: profession
role: architecture
status: partial
updated: 2026-11-08
---

# 副职业：规则与实现状态

来源定位沿用[系统导航](../README.md)的 A/B 规则。十职业核心与矿石采集已接入主Mod，下文分别登记已执行客户端证据和剩余入口，不以核心通过标记整个系统完成。

## 1.18.2 数据与入口

十种职业按 `Alchemy`、`Refiner`、`Rune`、`collection`、`cooking`、`puppet`、`breed`、`ghost`、`insect`、`blood` 顺序检查。每种职业各有 `_LEVEL`、`_experience_Max`、`_experience` 三个独立 double，合计30个数值；血脉等级不计入副职业。默认值为等级0、经验上限100、经验0（`A:network/YvanchuxiuzhenModVariables.java:487–516`），所有克隆事件复制（同文件114–143），序列化与同步由能力承载。具体字段见[进度台账](ledger/progress.csv)。

`A:PlayerTickProcedure:31–35` 订阅玩家每刻 END，394行无间隔调用 `FuzhiyeLevelUp`。`A:FuzhiyeLevelUpProcedure:13–292` 逐职业执行独立的 if：等级小于9且经验达到旧上限时，等级加一、经验减去旧上限、上限改成新等级乘100，并向服务端玩家发送金色聊天提示。每职业每刻最多升一级，各职业可以同刻分别升级；不用 while。升到九级后停止升级且保留全部剩余经验和当前上限。命令设置的小数、负数、超过九级的数值不在这里校正。

`A:ZhiyeTXT01`–`20` 为十职业成对的等级和经验文本：等级 `DecimalFormat("0")`，经验采用 double 的 `当前/上限` 字符串；符箓等级文案为“画符等级”，升级反馈为“绘符术提升一级！”。

## 已实现核心（2026-11-08）

`ProfessionSetup.register(modBus)` 注册 `yvanchuxiuzhen:profession` 玩家附件、玩家每刻 Post 升级、登录/重生/跨维度后的本人同步与矿石采集监听。主Mod调用已接入；本系统无需客户端专用setup。

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
| `A:YvanchuGoProcedure:79–511` | 权限1的 `/yvanchu` 目标玩家循环，`levelup` 修改十职业等级；未改经验上限 | 已提供 `level/setLevel`；共享管理命令自然入口待其所属批次接回 |
| `A:YvanchuGoProcedure:512–513`、`A:YvanchuGo2Procedure:62–480` | `experienceup` 修改十职业经验，set赋值，其余分支加减 | 已提供 `experience/setExperience/addExperience`；命令待接 |
| `A:Fulutexiao37Procedure:327–536` | 十职业重置为0/100/0 | 已提供 `reset`；轮回/浮生丹所有者接入，血脉字段不归本系统 |
| `A:Alchemy01ZaiFangKuaiShangYouJianProcedure:37` | 开炼丹炉时复制炼丹等级到装置 | 已提供等级查询，随 liandan 装置生命周期接入 |
| `B:JingyanhuoqvProcedure:18–29`、`B:network/LiandanGUISlotMessage:73–80` | 炼丹槽9的changeType1/2操作；非炼制中且装置经验至少1时领取炼丹经验，炸炉除4并清标记，DecimalFormat("0")取整后通过 `/yvanchu` 加经验，随后装置经验清零 | 随 liandan 实际领产物与槽位消息接入；不要把炸炉除4或取整移到通用API |
| `B:Tripod01ZaiFangKuaiShangYouJianProcedure:43` | 开炼器鼎时读取炼器等级、写入鼎炉 `RefinerLEVEL` | 已提供等级查询，随 lianqi 接入 |
| `B:QvchuProcedure:33–37`、`B:network/TripodGUIButtonMessage:101–103` | 炼器GUI按钮7取出产物前，把装置int `jingyanhuoqv` 通过 `/yvanchu` 加炼器经验，随后装置经验清零 | 随 lianqi 按钮与取物生命周期接入 |
| `A:RightClickBlockProcedure:78` | 符箓入口复制绘符等级到 `RuneLEVEL` | 已提供等级查询，随 fulu 接入 |
| `B:HuizhifuluProcedure:220–223`、`B:network/HuafuGUIButtonMessage:99` | 绘符输出成功时通过 `/yvanchu` 添加 DecimalFormat("0")后的绘符经验并显示经验反馈；随后消耗输入 | 随 fulu 绘制成功、输出和输入消费完整路径接入 |
| `A:FooddaguoZaiFangKuaiShangYouJianProcedure:24,238–247` | 复制厨艺等级，领取产物时把 `cookexp` 加到玩家厨艺经验后清零装置值 | 随 cooking 接入，禁止在 profession 绕开装置领产物条件 |
| `A:FooddunguoZaiFangKuaiShangYouJianProcedure:24,107–116` | 同上，炖锅 | 随 cooking 接入 |
| `A:FoodzhengguoZaiFangKuaiShangYouJianProcedure:24,100–109` | 同上，蒸锅 | 随 cooking 接入 |
| `A:EntityDeathProcedure:151–182,206–254` | 蛊虫自身经验升级后，向同世界 UUID 查得的主人增加蛊虫新 `LEVELnumber ×10` 养蛊经验；存在两段来源分支 | 随 creature/guchong 的完整升级与主人查找入口接入，当前不增设泛化击杀经验 |
| `A:ZhiyeTXT01`–`20` | 十职业成对文本 | 已提供文本 API，副职业实际页面待接 |

炼丹、炼器、绘符以跨 Mod 管理命令间接增加经验，不能只查 Java 字段赋值而遗漏。其命令以权限4、无执行实体发出，但 `A:command/YvanchuCommand:26–48` 已注册 `/yvanchu`、把空执行实体替为 FakePlayer，并由参数中的玩家显示名选择目标，故三条已注册命令具有消费者；后续改用公开 API 时保持各生产入口的取整、炸炉减额、装置清零和金色经验反馈。傀儡、御鬼、御灵、血道在本轮两个锁定工程的字段与命令字符串搜索中未发现自然经验生产者，不因职业名称增加合成经验或击杀经验。血道的吸血石、血流、血精不使用职业进度，归 xiulian。

## 连锁采集命令消费断链

`B:ChainProcedureProcedure:63` 从 `collection_LEVEL` 读取 int，额外矿石处理累计经验；151–155行尝试以无执行实体的 `CommandSourceStack` 执行 `collection experience add <数量>`。两个锁定工程全部命令注册类均未注册 `collection`，有效管理职业的路径是权限1的 `/yvanchu`，参数还需目标玩家与 `experienceup` 等前缀。因此来源该字符串没有有效经验消费者；不能与上表确有注册的 `/yvanchu` 生产命令混同。

目标 combat 已发出 `GatherExperienceEvent`。本批保持来源可达性，不订阅该事件增加职业经验，也不新增 `collection` 命令把断链变成新玩法。连锁采集的矿石额外掉落仍能从派生镜像读取等级；经验事件的最终去向应由来源矛盾裁定后处理。此结论来自静态源码，两个版本实机均未执行。

## 验证与剩余范围

静态来源与目标精确版本机制核对后，主会话已接入setup，并沿用既有 `runSmokeClient -PsmokeOnly=storage` 进入真实1.21.1版本客户端。全场景45项玩法检查PASS、0FAIL、DONE，另外2条INFO为过程说明；职业检查通过实际原版矿石破坏增加2点经验、自然玩家每刻Post升级减去旧经验上限并同步本人，以及真实死亡重生、退出重进后采集等级保留且双端相同。该结果不代表十职业所有升级边界或全部自然生产者已验收。

证据 `tmp/material-storage-client.log`、`tmp/material-storage-final-report.txt`、`tmp/material-storage-final-latest.log`。最终客户端WARN37（含已有厨具缺失纹理）、ERROR/FATAL0。首次共用场景因材料袋06工厂漏接失败，修复后重跑通过，`tmp/material-storage-first-*` 保留。最终 `build compileSmokeJava`（含既有test/check/ArchUnit）及专服启动至Done后原生stop均通过，四维度正常保存退出、ERROR/FATAL0、菜单18个；证据 `tmp/material-storage-build-final.log`、`tmp/material-storage-server-latest.log`。未新增Test类、检查类或冒烟选择器。

未完成：管理命令、浮生丹自然重置、三厨具、炼丹/炼器/符箓装置读等级及经验生产、蛊虫经验生产、职业页面与完整视觉、跨维度、真实1.18.2版本实机、两版本对照及多人。主手达标/副手达标/双手不足、普通工具硬度差异、相邻天道固定块跳过、同次连锁事件顺序和低概率额外掉落/粒子尚未专项实机；十职业批量经验逐刻升级、九级剩余保留等边界仍需对应路径验证。进度核心通过不等于上述入口完成。
