---
topic: handoff
role: guide
status: current
updated: 2026-11-09
---

# 交接说明

本页写给接手移植的新会话与负责人。它只给出当前状态、阅读顺序、环境事实和下一步；规则正文在各自文档中，这里只链接。

## 当前状态（2026-11-09）

| 项 | 状态 |
| --- | --- |
| 分支与提交 | `main`（combat 已合入）；当前系统 skill 在分支 `port/c-skill`（已推送到 `origin/port/c-skill`，尚未合入 `main`；当前最新提交以 `git log` 为准） |
| 已完成 | 两个 1.18.2 工程的全量静态调查与索引；系统调查正文（2026-11-02 按[系统设计](system-design.md)重组为 28 个系统、3 个内容族与玩家面板）；通用移植技能；开发环境（构建、开发服务端与客户端加载已实测） |
| 代码 | 公共入口与客户端入口；attribute 系统（贡献汇总、玩家附件、原版属性写入，2026-11-03）；relation 系统（敌友判定、团队、好友与申请界面、按钮 payload，2026-11-03）；xiulian 系统（修炼档案、出身、修炼推进与突破、金丹元婴、丹田界面、血道，2026-11-03）；gongfa 系统（功法定义、功法书组件、研习、功法栏、参悟台，2026-11-03）；combat 系统（伤害结算与 `/shanghai`、85 个附魔数据定义与命中、护甲、挖掘行为、状态效果登记基础与 47 个效果行为、连锁采集、穿石、木人桩，2026-11-04；首次接入 GeckoLib 4.9.3、实体内容表与菜单界面）；`platform/content` 的内容表加载器与工厂（15 种方块、物品、工具、盔甲）按单位登记 1,127 个方块、2,125 个物品与 150 个声音事件，29 个有内容的单位各有一个 `<名称>Setup`；ArchUnit、台账工具、归属证据工具、片段合并与客户端冒烟检查已接入；skill 系统进行中（框架、14 个元素弹体、动画实体基础与火土金冰光风雷水木各系实体技能、剑法、灵纹、火鸟术、起尸诀、虚空裂隙实体与传送、伤害范围、元婴出窍、十种技能箭与箭种选择、身法轻功、技能界面与 HUD 的部分实现，见下节）|
| 批次进度 | B0 完成步骤 1–5 后按负责人指示结束（2026-11-02），B1 于 2026-11-03 出口核对并经负责人认可后结束，当前执行 C：attribute、relation、xiulian、gongfa 2026-11-03、combat 2026-11-04 完成并合入 `main`（xiulian 按依赖方向调到 gongfa 之前，见[功法](../systems/gongfa/README.md)“依赖方向”），combat 2026-11-04 完成并合入 `main`，当前是 skill（进行中，见下节）；[系统设计](system-design.md) 2026-11-02 全部批准并完成迁移；命名空间统一为 `yvanchuxiuzhen`。逐步进度见[月度记录](../systems/project/logs/2026-11.md) |
| 已批准 | 2026-11-02 负责人批准整体架构、[工程结构与长期维护](engineering.md)、[实施路线](roadmap.md)（`3e0585c` 版本），记录见[项目状态](../systems/project/README.md)；不含存档导入、界面复刻程度、license、Q01–Q14 |
| 未完成 | skill及后续系统仍按各自批次推进；D材料仓、千材录、职业核心、炼丹、八鼎及四炼器装置已有实现与批次验证，不能计为全系统完成；四装置末次汉化构建与48项客户端通过，1.18.2实机、完整存档生命周期及视觉终核待验收。完整移植goal已由负责人恢复active |

## 进行中

2026-11-09 D阶段符箓与烹饪批次（分支 `port/c-skill`，本批实现与记录同一提交，最终提交以 `git log -1` 为准）：已接回 fulu 知识、画符、烧灰与传承入口，以及 cooking 两锅、墨料坯生产和部分菜谱执行；两锅自然合成配方由 `convert_data.py` 按来源转换。转换后的完整 `build`（含 Gradle `check`）通过；台账4939行与98篇文档检查均0问题，符箓与烹饪源码证据及月志已建立。符箓本批只有静态来源复核，未做专项运行验收；烹饪全量冒烟无对应断言，不构成验收。全量冒烟报告另有伤害范围一项失败。目标汉化合并语言文件已包含本批关键中文键。铁锅墨料坯、砂锅菜谱烹饪与符箓知识/画符台已由内置 `-PsmokeOnly=fulucooking` 14项验收通过（见两系统月志），烧灰架、传承之光、蒸锅与专服仍待执行；符箓施用、辰砂及墨料自然闭环、蒸锅和自然菜谱/食用等仍未完成。下一步核验两锅配方加载并运行专项场景；不把系统标记完成、不合main。

2026-11-09 D阶段四炼器装置批次（前一提交`56010985`，工作分支`port/c-skill`；本批最新提交以 `git log -1` 为准）：修理台、针线笸箩、天道图纸台、毒药炼锅及六材料副手淬毒已接回，四装置覆盖15条、炼器累计42条注册的行为实现。归属147/147已读、来源证据170条；29项剩余候选的已有资源、12个孤立过程、7个宝箱后续消费者与Armor历史差异已逐项登记。两次`build compileSmokeJava`通过；既有equipmentproduct第二轮47INFO/0FAIL/DONE，保存四维度、退出码0且ERROR/FATAL0。首轮43INFO/4FAIL中缝纫2项翻译namespace、图纸1项目录截栈已修；另一项旧成品属性失败原因未锁定，第二轮未放宽条件即通过。三张界面截图已看中文正常，修理顶部与图纸默认窗口裁切仍待视觉验收。专服四BE受控样本已实际保存、退出、重启读回，两次Done和stop正常；这不是自然在线玩家专服验收。证据及前置样本输入修正见[炼器月志](../systems/lianqi/logs/2026-11.md)。

本批当前状态：持久化文本Component已避免专服英文名称固化，静态中文审计367键/252名称0缺漏；最终构建与第三轮内置equipmentproduct客户端48项全INFO/0FAIL/DONE通过，正常保存退出、ERROR/FATAL0。证据`tmp/lianqi-devices-build-localized-final.log`、`tmp/lianqi-devices-localized-report.txt`和`tmp/lianqi-devices-localized-client-latest.log`。最终复审修正模板自定义名称括号规则，第四轮构建和48项检查通过，证据为`tmp/lianqi-devices-*-reviewed*`。提交前97篇文档、4939行台账及31623份来源指纹均通过。下一步按`tmp/fulu-next-batch-audit.md`推进制符知识、画符、烧灰与传承，并依据`tmp/cooking-ink-blank-audit.md`接两锅墨料坯生产；朱砂石磨与自然矿物依赖继续跟踪，收口详情见炼器月志。转换使用`convert_content.py --owner lianqi`；14份旧衣甲manifest证据保留。全量goal已恢复active，继续按路线推进D后续；完整生命周期、多人、完整视觉及两版本未验收，skill遗留未闭合，当前不合main或push。

2026-11-09 D阶段八鼎炼器批次（前一提交 `b85ae3d7`，本批最新提交以 `git log -1` 为准）：`port/c-skill` 已接回八档炼器鼎、十二槽库存、制作/升级/熔矿/精炼、供热、图纸保存与装料、领取及原生界面；材料仓扣仓和装备完整累计组件由所属系统公开接口承担。炼器27条注册行为已迁移，归属82/146已读。`build compileSmokeJava`通过；既有equipmentproduct客户端最终36项INFO/0FAIL/DONE，本批8项通过，前两轮丹火准备与期望算例错误已修正留档。客户端退出时有1条Netty连接关闭ERROR，四维度仍正常保存且退出码0；专服受控起炉的熔矿/精炼计划刻样本通过，原生stop正常保存四维度、ERROR/FATAL0。具体证据及未验收范围见[炼器月志](../systems/lianqi/logs/2026-11.md)。下一步按 `tmp/lianqi-repair-source-audit.md` 接修理台、针线笸箩；天道图纸台与毒药炼锅便览在 `tmp/lianqi-blueprint-pot-source-audit.md`。全八档自然路径、完整生命周期、多人、完整视觉和两版本仍未验收，skill遗留未闭合，当前不合main或push。

2026-11-09 D阶段毒厄丹与毒药结束批次（前一提交 `2c046433`，本批提交以 `git log -1` 为准）：`port/c-skill` 已接回自服/喂用与combat三项结束回调，炼丹69条行为已迁移、归属129/129已读。构建通过；既有combat客户端最终54INFO/0FAIL/DONE，首次三项失败及只修随机地形空间准备的证据见[炼丹月志](../systems/liandan/logs/2026-11.md)；专服05/07非玩家移除/刷新清零、19三种结束路径猪转僵尸及属性装备样本通过，双端ERROR/FATAL0、正常保存四维度退出。毒厄丹自然自服/喂用、玩家结束分支、其他六类转换、完整视觉与两版本未验收。下一步按 `tmp/lianqi-source-audit.md` 接炼器：先补材料仓装料和装备结果接口，再接八鼎12槽库存/菜单、制作/升级/熔矿/精炼四分支、供热和按钮领取；9号成品槽禁止手取、11号隐藏临时槽、来源异常顺序均已核。Grind已归craft，随石磨实现。沿用现有完整移植目标，skill遗留未闭合，当前不合main或push。

2026-11-09 D阶段百草鉴批次（前一提交 `971f3ae7`，本批提交以 `git log -1` 为准）：`port/c-skill` 已接回书本自然开窗、逐项辨识、药草Tooltip、全解锁命令与本人同步的持久附件，炼丹累计68条行为已迁移、归属121/133已读。构建、现有storage客户端54条INFO/0FAIL/DONE、专服启动至Done及stop保存四维度通过，双端ERROR/FATAL0；54条含原有检查，百草鉴六条及复用死亡/重登路径的具体覆盖见[炼丹月志](../systems/liandan/logs/2026-11.md)。下一步核对毒药05/07/19结束回调、duedan与Grind，并复核剩余12项候选归属。完整差异提供暂存审阅；完整两版本、多人及视觉未验收，skill遗留未闭合，当前不合main或push。

2026-11-09 D阶段药理台批次（前一提交 `b2cbdaa4`，本批最新提交以 `git log -1` 为准）：`port/c-skill` 已接回十二格药理台、八草丹方预览与媒介写入，炼丹累计66条行为已迁移、归属102/133已读。构建、既有内置equipmentproduct客户端28项全INFO/0FAIL/DONE和专服正常stop保存四维度通过，双端ERROR/FATAL0，截图已查看。首次分丹台检查地基缺失已修正并保留失败证据，来源专服输入文本断链与破坏丢库存照实保留。百草鉴、药草Tooltip/解锁命令已接回，内置storage场景54项及专服加载保存退出通过；下一步毒药剩余，skill遗留未闭合，当前不合main。

2026-11-09 D阶段分丹台批次（前一提交 `5b530c70`，本批最新提交以 `git log -1` 为准）：`port/c-skill` 已接回三槽分丹台、分合丹双页菜单与服务端按钮，保留来源数量、容器转换和异常分支；炼丹累计62条行为已迁移、归属83/133已读。最终构建与既有内置equipmentproduct客户端25项全INFO、0FAIL、DONE，专服正常stop保存四维度，双端ERROR/FATAL0，两页截图已查看。首次旧丹炉放置harness失败与未复现原因如实留在[月志](../systems/liandan/logs/2026-11.md)。下一步按已完成的来源便览 `tmp/pharmacology-source-audit.md` 接药理台八草预览/丹方写入和百草鉴玩家辨识；来源药理台文本跨端断链与无onRemove库存掉落需按来源保留并登记。skill遗留与两版本验收未闭合，当前不合main，沿用完整移植目标。

2026-11-08 D阶段药瓶消费批次（前一提交 `d014a639`，本批最新提交以 `git log -1` 为准）：`port/c-skill` 已接回八种药瓶自服/堆叠合并/耗尽换瓶、实体喂药、Tooltip、放置拾回及八种单槽方块实体；cooking补齐辟谷效果。完整构建与既有equipmentproduct客户端19项全部通过、0FAIL、DONE，客户端四维度正常保存退出，ERROR/FATAL0；专服和检查结果见[炼丹月志](../systems/liandan/logs/2026-11.md)。下一个具体操作是读B的 `Fendantai`、`FendanButton`、`HedanButton` 与菜单/按钮/槽位完整调用链，接回分合丹；药理台、百草鉴、毒药回调与两版本完整验收仍待续。复用当前子agent，内置客户端，运行工具串行。完整goal active，skill遗留未闭合，当前不合main。

2026-11-08 D 阶段丹炉自然炼制批次（前一提交 `eadc22fe`，本批最新提交以 `git log -1` 为准）：`port/c-skill` 已接回八档丹炉、12 槽持久库存、原生菜单、37 条药草/100 条有序药性、20 游戏刻供热推进、出丹/炸炉、丹方装料和取丹经验。最终 `build compileSmokeJava` 通过；既有 `runSmokeClient -PsmokeOnly=equipmentproduct` 最终 13 项通过、0 FAIL、DONE，含本批 7 项库存/同步/炉次检查，四维度正常保存退出，ERROR/FATAL 0。首次因检查误把自然丹火燃料作30而失败，按来源实际10点加真实补料后通过；用户指出的 `lianqibiao01` 漏翻译已补成来源“灵力”，新截图已查看。专服与文档台账检查见[炼丹月志](../systems/liandan/logs/2026-11.md)。后续药瓶消费已接回，见本节顶部；分合丹、药理台、百草鉴、毒药和两版本完整验收仍未完成。完整goal active，skill跨系统遗留未全闭合，当前不合main。

2026-11-08 D阶段材料仓、千材录和职业依赖批次（前一提交 `ba5782b9`，本批提交以 `git log -1` 为准）：`port/c-skill` 已接回袋06自然入口、玩家材料仓及显示/过滤/排序/自动入库、千材录逐条辨识与扣仓、十职业进度核心和真实矿石采集。内置storage场景45项玩法检查通过、0FAIL、DONE，覆盖仓数量、辨识成本、采集升级、死亡和重登；最终构建与专服正常stop通过，菜单18个、ERROR/FATAL0。客户端37条WARN按来源分类保留；首次袋06工厂漏接失败修复后重跑通过。细节与未验收边界见[storage](../systems/storage/architecture.md)、[material](../systems/material/architecture.md)、[profession](../systems/profession/architecture.md)。此批之后的丹炉自然入口、炉次与供热已接回，见本节顶部；炼器自动装料继续随lianqi接回。完整移植goal保持active，skill跨系统遗留尚未全部闭合，当前不合main。

元婴生命周期与普通重生收口（2026-11-08）：按1.18.2版本来源补回有来源实体致死时先归窍再扣365寿元、普通 `PlayerRespawnEvent` 的资源/寿元公式，并修正书架藏宝创造模式仍应消耗一件载体的偏离。`compileSmokeJava`、既有 `runSmokeClient -PsmokeOnly=skillui`（29项）和 `-PsmokeOnly=yuanying`（5项）均通过；本次另运行既有 `runSmokeClient -PsmokeOnly=xiulian`，重生资源断言按来源公式通过（灵力4、寿元9970），但退出后重开步骤仍在客户端服务器已销毁后触发既有 harness 空指针，不能作为生产逻辑失败。没有新增Test或专项入口。普通重生完整实机、元婴死亡/跨维度/重登路径仍未专项验收。证据以 `build/smoke/report.txt`、`run/logs/latest.log` 与本页项目日志为准。

自然入口与异火主路径收口（2026-11-08，前一提交 `4b9ff36d`；本批代码与本页同一提交，最新提交以 `git log` 为准）：book已接书架藏宝真实右键，保留分派优先级、等级、随机表及先发书后减一；完整build通过，现有skillui唯一一次35项全INFO、0FAIL、DONE，1分34秒正常保存四维度退出、ERROR/FATAL0。新增五组丹火实际使用/双端同步/计划刻/炙烤/爆燃和一组藏宝库存检查均通过；未恢复YihuoCheck或选择器，未用Computer Use。日志仍有32条未知实体附件同步WARN，与错误分开登记。本批未另跑专服。证据 `tmp/skill-natural-exit-{build.log,client.log,latest.log,report.txt}`。书架载体由检查构造后真实use，获得链未验收；持续效果由受控前置后自然实体刻结算，不代表18材料自然命中全链。来源31623文件指纹通过，台账4939行和文档88篇均0问题，差异空白检查通过。

现有lightning已另一次4项全过；本次把天雷术/五雷轰顶改为客户端自然useItem，经服务端右键事件验证十字五点与单道天雷均通过。旧辅助选择失败不再作为当前缺陷。异火剩余获取/消费已按宝箱、任务、传承灵物、红包、材料、经济与装置所有者登记，本次静态未发现新的yihuo自有可实施缺口，见[异火正文](../systems/yihuo/architecture.md#外部获得与消费去向2026-11-08)。

公共书自然研习与功法技能装配批次（2026-11-08，前一提交 `ff623ac`；本批代码与本页同一提交，最新提交以 `git log` 为准）：36种书载体已接回潜行右键，04八槽装配、四页保存/载入、01/03导航与K记忆已接回；跨页沿用旧菜单坐标再上移一格，开窗栈共享保留功法与起尸诀成长。book只负责分派与成功清答题计数，skill/gongfa继续各自写真值。规则见[书籍](../systems/book/architecture.md#自然研习入口2026-11-08)、[装配页](../systems/skill/architecture.md#共用功法与技能装配页2026-11-08)与[导航](../systems/panel/architecture.md#三页导航与装配页归属2026-11-08)。

本批 `build compileSmokeJava`（含既有test/check/ArchUnit）通过；仅一次项目内置 `runSmokeClient '-PsmokeOnly=skillui'`，29项玩法检查全部INFO、0FAIL、DONE，四维度正常保存退出、ERROR/FATAL0。已验证自然研习、整栈绑定、原生点击/Shift装配、前两页保存与关闭重开、K记忆和功法开窗成长不回滚。专服一次到 `Done (1.360s)` 后原生 `stop`，四维度正常保存退出、ERROR/FATAL0，菜单导出12个。证据在 `tmp/study-loadout-{build.log,client.log,client-latest.log,report.txt,server-latest.log}`；04截图已查看，布局样本可见，完整缩放和两版本视觉未认可。来源31623份文件未变，最终台账4939行零问题。

本批共享落雷客户端自然右键复核已提交，最新提交以 `git log` 为准。下一步按[skill出口剩余范围](../systems/skill/architecture.md#出口剩余范围2026-11-08)继续核对未闭合的生命周期与跨系统依赖；沿用现有客户端入口，不重跑旧神通整套。普通重生和元婴完整死亡/跨维度/重登仍需专项验收。lightning、丹火/炙烤五组已有有效通过，不继续反复修旧专项。炉鼎消费者随D，creature生产者和六个正式返回维度随E。完整四页循环、头顶容器、延迟竞争、起尸诀开窗成长、04死亡/断线/重登、灵根/他人/副手分支、完整视觉、1.18.2版本实机、两版本和多人未验收。当前仍为 `port/c-skill`，skill未完成，不合 `main`；`.eclipse/`保留且不提交。

equipment 饰品制作台批次（2026-11-08，前一提交 `031c88d`；本批代码与本页同一提交，最新提交以 `git log` 为准）：四槽持久库存、类别翻页、36条即时配方、成品根字段与饰品Tooltip、来源原生界面和Geo动画已接回。保留结果覆盖、动画期间再次合成、第七档品质文字差异、二维物品图标、全部方向四槽自动化与自身掉落回落。转换来源结构时补齐加载器持久键适配，161份结构共1117处直接 `nbt.ForgeData` 改为 `NeoForgeData`；DataVersion和业务字段保持来源。规则及证据边界见[装备正文](../systems/equipment/architecture.md#饰品制作台2026-11-08)。

本批最终 `build compileSmokeJava`（含既有test/check与ArchUnit）通过；仅一次内置 `runSmokeClient '-PsmokeOnly=accessories'`，22项全部符合并DONE（既有饰品14项、制作台8项），包括真实右键开台和槽位点击、两档头簪的消费/根字段/附魔双端同步、80刻内覆盖、翻页与关闭重开；客户端正常保存四维度退出，仅一条Yggdrasil公钥请求Connection reset联网ERROR。专服一次启动至Done后原生 `stop`，四维度正常保存退出、无ERROR/FATAL，导出方块1129、物品2126、菜单11、实体109。证据为 `tmp/jewelry-{build-final.log,client.log,report.txt,client-latest.log,server-latest.log}`。本批未新增Test类、选择器或Computer Use，仅扩展既有 `AccessoryCheck` 的库存/网络路径；其余34配方逐条、自动化、制作台存档重登、破坏掉落、完整视觉、1.18.2实机、两版本对照与多人未执行。

当前继续在 `port/c-skill` 推进，skill尚未完成、不合main。负责人已授权子agent加速，采用同工作树分文件协作，运行工具仍逐个执行。此前待接的技能书自然研习与 `PlayerGUI04` 八槽装配、翻页、关闭回写和01/03导航已于 `4b9ff36d` 完成，证据见上文；当前操作以本页顶部下一步为准，不展开02加点及其他系统副页。`fabao_013` 的正式人间维度、`fabao_017`/`01701` 的动画实体及蛊虫收纳继续随各自系统接续。`.eclipse/` 为用户配置，保留且不提交。

负责人2026-11-07补充goal续接要求：①按roadmap直接推进；②当前从C阶段继续实现并测试验收；③目标覆盖两个Mod的全部范围及后续阶段；④若反复压缩已造成上下文丢失，及时开新会话，把这四点完整设为goal继续循环。新会话先核对本页、当前分支与最后提交，接续未完成操作，保留无必要不新增Test、内置客户端验证及逐个运行的约定。

2026-11-08 当前批次：`port/c-skill` 已接回普通防具 `clothes_08`–`clothes_12`、`clothes_18`、`clothes_20` 共28件的普通 ArmorItem 内容表与资源，并接回 `gongjv_001` 镰刀右键范围清理；同时修正元婴归窍先更新服务端坐标再同步客户端。`compileJava`、完整 `build`、文档检查、台账检查、差异空白检查，以及既有 `runSmokeClient -PsmokeOnly=yuanying` 和 `-PsmokeOnly=equipmentweapon` 均已通过。本批验证完成。随后已核对 equipment 剩余 `fabao_01701` 与 `weaponlevelup`：前者等待 guchong 的蛊虫收纳接口，后者归 lianqi 的 D 阶段炼器升级规则；不在 equipment 中硬补。防具自定义穿戴视觉、镰刀自然清理专项、1.18.2 实机、两版本对照与多人仍未执行。

2026-11-08 D 阶段起步：按1.18.2版本 `YvanchuxitongModGameRules.java:13` 与 `LianqiBeginProcedure` 接回 `weaponlevelup` 的目标 `LianqiRules.WEAPON_LEVEL_UP` 注册，保留 `MISC`、默认真和原注册名；归属已从 equipment 改判 lianqi，炼器炉消费者、命令切换与升级行为仍待本阶段继续实现。`build compileSmokeJava`、文档/台账检查与既有 `runSmokeClient -PsmokeOnly=skillparticle` 通过，8项 `INFO`、`DONE`，客户端正常保存退出。下一步进入 material、storage、profession 的依赖核对与实现；skill 的 creature/world 遗留继续随所属系统推进。元婴跨维度、死亡与重登静态审计已完成，现有五项检查仍是唯一运行入口。


C 玩家与战斗核心，combat 已完成并合入 `main`，当前系统 skill（分支 `port/c-skill`，从 `main` 建立；最新提交以 `git log` 为准）：2026-11-04 第 1 阶段“框架”完成（技能书、技能栏与选择、施放与冷却、命令、藏宝与附赠、霸体与禁施；Q36–Q39；冒烟 85 项），按五个阶段分批提交，阶段表见[技能架构](../systems/skill/architecture.md)“迁移阶段”；skill 全部阶段完成后才合入 `main`。负责人 2026-11-03 指示（/loop）：每完成一个路线步骤即提交、合入 `main` 并进入下一步，直到移植完成或出现重大问题；判断不定的问题先评估影响，轻微的记入文档后继续，重大的停下商议；客户端测试由会话自己完成。运行约定（负责人 2026-11-03）：变换工具、Gradle、开发服务端与冒烟检查逐个运行，不同时进行（负责人的 VS Code 两次因内存不足崩溃，均在大批文件写入之后）。B1 范围内需要新代码类型的行（`skipped.csv`）与其余语言键随所属系统迁移。盔甲挥臂闪烁与药草疑似缺失两项反馈由负责人指示暂缓（2026-11-03）。

已完成（2026-11-03，均已合入 `main`）：
- attribute：Q17 裁定实际属性计入炼器数值，Q18 暂按来源行为待再议；见[属性](../systems/attribute/README.md)。留待其他系统：重算粒子随 particle；提交贡献的真实路径随 xiulian、gongfa、equipment、fulu、economy；equipment 已在本批去掉妖化防具的重复生命修饰符，六项数值进入统一属性加成层。
- relation：敌友判定、团队与阵营、好友与申请界面；Q19、Q20；见[关系](../systems/relation/README.md)。留待其他系统：方块实体的敌友判定随首个调用者；好友行境界名随 xiulian。
- gongfa：功法定义 67 本、功法书组件、研习、功法栏与提交、功法修炼与闭关经验、升级成长、提示文本、书架藏宝、参悟台（参悟与继承）及 `/technique` 管理命令；Q26–Q28；见[功法](../systems/gongfa/README.md)。留待其他系统：书的右键（研习、书架藏宝、答题计数重置）随 book；研习附赠技能（监听 `TechniqueStudiedEvent`）随 skill；功法页放入取出随 panel；被闪电击中的功法效果 `EntityThunder` 需要 combat 的效果。
- xiulian：修炼档案与公开操作、出身、修炼推进与突破、闭关、金丹元婴、丹田界面、血道、加点、灵根重置、升仙令与家族令牌；Q21–Q25；见[修炼](../systems/xiulian/README.md)。留待其他系统：雷劫过程原随 combat 推迟，已于 2026-11-07 在当前分支接回（见下文）；远古石盘随 world 的传送阵位置与 particle；传承灵物、家族百宝袋随 material 与 fulu 的实例数据；妖丹实例等级条件随 material；境界提升与血流滴落粒子随 particle；本命培养与进度提交随 equipment；面板加点与丹田按钮、返回分页随 panel；`/yvanchu` 灵根子命令、出身发放物品与 `keepInventory` 随 flow；被闪电击中的功法效果随 gongfa；元婴出窍随 skill。

combat（2026-11-04 完成，合入 `main`）：伤害主链归属阅读与 Q29–Q35；85 个附魔转数据定义并按标签表达类别；状态效果登记基础（`effects.json`、`EffectBehavior`）与 47 个效果的行为；伤害结算（`Shanghai.deal`、`/shanghai`、`HurtHandler`、四个伤害类型）；命中附魔、连锁采集、穿石（破碎的基岩）；木人桩（GeckoLib 首次接入、实体内容表 `entities.json`、设置界面）；公开操作写在[战斗 README](../systems/combat/README.md)，设计、借用事件（`DamageImmunityEvent`、`LethalDamageEvent`、`ArmorStatEvent`、`HitEvent`、`EffectExperienceEvent`、`GatherExperienceEvent`）与各节写在[战斗架构](../systems/combat/architecture.md)；开发客户端 `CombatCheck` 46 项，全系统冒烟 254 项全部符合。留待其他系统：自定义粒子随 particle 登记后接入发出处；`qishi`、`lingwen`（skill）、`starsky`（fulu）订阅 `HitEvent`；`breakfree` 随 flow；淬毒归 lianqi（Q35）；伤害范围指示实体 `Shanghaifanwei` 归 skill；`effect_023`、`024` 归 yihuo，`effect_035` 等归 skill、xiulian、cooking、creature、guchong（登记为标记）。

xiulian 推迟的完整雷劫已于 2026-11-07 在当前分支接回：`ThunderBegin`、劫云 `Jieyvn`、天雷实体 `Tianjie03/05/07` 与七种天雷 `Leijie0101–0107`。通过具体伤害请求事件接入 combat，保持 combat → xiulian；共享模型与渲染移至 platform。仅一次雷劫专项 5 项全部通过，build、ArchUnit 及专用服务器启动/正常停服通过，实体导出 67 个。Q56、时序与未验收范围见[修炼架构](../systems/xiulian/architecture.md)“完整雷劫”。弓与拂尘已在下述 equipment 批次接回；下一步为其余 equipment、yihuo、world 依赖及视觉收口。

equipment 弓与拂尘依赖批次（2026-11-07，提交 `26c461e`）：18 把弓保留剑类型并接回来源条件流程、九把拂尘的挥动与选中冷却、隐藏物品及弹体已接回。Q57 记录两个来源均未找到 `synchronizedata.rightclicking` 写入者，自然右键断链保持；不补造鼠标同步。build/ArchUnit、检查源集编译通过，仅一次 `-PsmokeOnly=equipmentweapon` 五项全部符合并 DONE，含人工标记的弓蓄力/扣费/普通箭存档及拂尘入口/命中/寿命；客户端正常保存退出。专用服务器一次启动到 Done，原生 stop 正常保存退出，ERROR 0，实体导出 68 个。证据在 `tmp/equipment-{build,smoke,server}.log`、`tmp/equipment-report.txt`。妖化见下述批次；品阶、饰品飞行减耗、yihuo/world 依赖及完整视觉继续推进，skill 未完成，不合 main。

equipment 妖化依赖批次（2026-11-07，前一提交 `26c461e`；本批提交以 `git log -1` 为准）：第19号按钮通过 `DemonTransformationEvent` 请求 equipment，四件动画护甲、实例六项数值及三项倍率、旧甲入背包、头盔保存与佩戴计时、分刻清槽均已接回，沿用 xiulian 的120秒冷却递减。Q58保持来源41格空位统计、没有额外境界门槛、不自动穿回及延迟清空当前槽；Q17生命只计入统一贡献一次。首次客户端在动画字段严格校验处中断，字段契约修正后 build/ArchUnit通过；仅一次进入世界的 `-PsmokeOnly=demon` 四项全部符合并DONE，客户端正常保存退出、ERROR 0。专用服务器一次到Done并原生stop正常保存退出、ERROR 0，物品导出2068个。截图已查看，模型可见；完整视觉、多人、死亡/重登和两版本实机对照未执行。证据在 `tmp/demon-{build,smoke,server}.log`、`tmp/demon-smoke-startup-failure.log`、`tmp/demon-report.txt`、`tmp/demon-check-compile.log` 与 `build/smoke/screenshots/equipment-demon-armor.png`。

成品生产与品阶已接回（2026-11-07，前一提交`0e2da02`，本批以`git log -1`为准），规则见[装备](../systems/equipment/architecture.md#宝箱成品与品阶2026-11-07)。`/lianqi` 实为 treasure 的容器奖励入口，equipment 构造类型化实例并把品阶提交格挡；Q59 保持嵌套炼器法攻与根法攻的区别，根值生产者主要为灵兽袋、蛊罐与部分法宝，不能补恒零监听或用炼器法攻替代。末次build/ArchUnit与成品专项六项全部符合并DONE；专用服务器一次正常启动/原生stop保存退出，客户端和专服ERROR0。初次专项六项已通过，复核补齐独立护甲整数后只再次验证本批组件，未重跑旧神通；服务器是在该补齐前运行。证据在`tmp/equipment-product-{build,smoke,server}.log`、`tmp/equipment-product-final-{build,smoke}.log`和`tmp/equipment-product-report.txt`。

四饰品与 K 键依赖批次（2026-11-07，前一提交 `f5dd77d`；本批代码与本页同一提交，最新提交号以 `git log -1` 为准）：equipment 已实现 `/shipin` 根字段、四槽生命周期、属性及飞行减耗；panel 已实现 K 键、属性汇总、饰品导航与丹田往返，但 GUI 未完整。Q60 保留腰手错位、首刻装入引用与头顶容器绑定。负责人指出数量3后，逐件核对36件来源/目标饰品均上限1；发现是检查代码手动 `setCount(3)` 伪造非法堆叠，已删掉并改合法单件，原报告12项不能作为自然穿戴验收，相关结论撤回。重登等待另有超时，后两项未执行；检查器已补实验性存档确认页处理。检查修正只编译，未重跑；build/ArchUnit与一次专用服务器启动/原生stop保存退出通过，ERROR0，菜单导出10个。旧截图仅作布局证据。规则、证据和验收限制见[装备正文](../systems/equipment/architecture.md#四饰品槽与飞行减耗2026-11-07)及[面板正文](../systems/panel/architecture.md#已实现k-键与两页导航2026-11-07)。

2026-11-07 负责人澄清：饰品堆叠与 GUI 完整性消息是询问，并未要求插入完整 GUI 开发批次。会话曾误读并编写加点、装配与修炼分页草稿，现已备份到 `tmp/gui-draft-withdrawn-20261107-200122/` 并撤回全部相关未提交代码，源码恢复至已推送的 `c6f6f5a`。合法单件饰品运行验收仍未执行，GUI 保持部分实现。继续遵守原路线：当前 skill 收口及所需 yihuo 炙烤/丹火、world 正式维度依赖与视觉核对；profession、cooking、dungeon 依各自批次推进，不能为补页面提前展开。界面随所属系统同步接入，H 是全量终核；skill 未完成，不合 main。

**本批进展与下次开工的第一步（2026-11-08）**：

- 当前分支 `port/c-skill`，本批前一提交 `a75e3f3`；本批实现与记录在同一提交，最后提交以 `git log -1` 为准。完整移植 goal 已由负责人恢复 active，继续原路线。`.eclipse/` 是用户的 IDE 配置，保留且不提交。
- 正式 `xvkong` 空气噪声维度、同名类型与 `xvkongqvnxi` 群系已按1.18.2版本实际代码接入，保留实际overworld效果绑定；smoke同ID平坦基岩替身及生成副本已删除。转换、群系语言键与服务端动态注册表导出接入现有管线；world106/19已读。规则见[世界正文](../systems/world/architecture.md#正式虚空维度2026-11-07)。
- 本批没有新增Test、专项选择器或修改旧专项。负责人再次强调来源核对优先、没有必要不写Test；曾暂停时留下当前改动未提交，现按“继续做”恢复。本批build/ArchUnit通过；补入群系语言后的一次专服加载至Done，原生命令确认正式虚空群系和空气区块，移除强加载后stop保存四世界并正常退出，ERROR0，群系导出1个。证据为 `tmp/world-void-{build,content,server}.log`；自然裂隙往返与视觉未验收。
- 上一丹火/炙烤批次已提交：来源放置回放已核实，唯一旧专项7项中2项通过、5项失败，新增脚本与选择器已撤下，不恢复或刷通过；炉鼎消费者仍待D阶段。记录见[异火正文](../systems/yihuo/architecture.md)。
- 本批已逐项核对 `SkillScreen`、HUD与来源界面/按钮/显示过程，未发现需修改的差异。使用现有 `runSmokeClient -PsmokeOnly=skillui` 九项全部通过，缩放1的真实菜单与HUD截图已查看，开发设置已还原；现有 `accessories` 合法单件14项全部通过，补齐保存/死亡复制/重登后操作和飞行减耗。两次客户端均正常保存四世界、ERROR0，没有新增或修改检查代码。证据为 `tmp/skill-ui-native-{client.log,report.txt,menu.png,hud.png}` 与 `tmp/accessory-native-{client.log,report.txt}`。此前误用Computer Use已停止，普通客户端在标题页正常退出、未入世界；负责人明确沿用项目内置客户端验证。
- 开灵工具机与弹体已按1.18.2版本实际源码接回，未新增Test、冒烟选择器或Computer Use。`gradlew build`、`check_docs.py`、`check_ledger.py`通过；沿用现有`runSmokeClient -PsmokeOnly=equipmentweapon`的五项既有弓与拂尘检查全部符合并`DONE`，客户端正常保存退出、未见`ERROR/FATAL`。专用服务器一次启动至`Done`并经原生`stop`正常保存退出，实体导出91个；该客户端检查仅作为注册/装配/启动回归，不替代四类工具机自然操作专项。1.18.2实机、两版本对照、多人及完整视觉仍未执行，规则见[装备开灵工具机](../systems/equipment/architecture.md#开灵工具机与弹体2026-11-08)。
- 覆火葫芦与普通火焰弹体已按1.18.2版本实际过程接回：十级火焰组件、灵力回退、潜行扫描吸收、普通火焰弹体命中放火与隐藏物品松开发射均已实现。未新增Test、冒烟选择器或Computer Use；`gradlew build`、文档/台账检查与现有`runSmokeClient -PsmokeOnly=equipmentweapon`通过，五项既有检查`DONE`，客户端正常保存退出且无`ERROR/FATAL`。现有客户端检查仅作注册/装配/启动回归，完整自然操作、1.18.2实机、两版本对照、多人及视觉仍未验收，规则见[装备覆火葫芦](../systems/equipment/architecture.md#覆火葫芦2026-11-08)。
- `Skills.showWindBurst/launchWindBlade` 已按妖兽011爆气和014黑风生成过程接回；skill拥有风波八刻、风刃攻击者/攻击量/倍率一/目标/二百刻上限及初始化。creature 仍需在自身时序调用，风刃1.5倍法攻由调用者计算。build/ArchUnit通过（`tmp/skill-wind-api-build.log`），没有新增专项，未将接口编译视为自然妖兽入口验收。
- 正式虚空下现有`voidrift`检查12项通过并DONE，四世界正常保存退出、ERROR0。进入人工推进后直接调用碰撞方法，返回由真实玩家碰撞触发；完整自然操作和裂隙视觉未验收。证据在`tmp/voidrift-formal-{client.log,report.txt}`，本轮没有修改检查逻辑。
- 新核对35个动画实体的运动与死亡源码，按来源接回30个共用实体的十九个飞行导航、九个水中导航、两个地面导航及属性、流体、死亡时序，尸体恢复40刻死亡移除；共享模型表的xiulian四行同步生成。最终build/ArchUnit及检查源集编译通过，现有`skillvisual`两项通过并DONE，四世界正常保存退出、ERROR0，光爆与血雾截图均有可见样本；不覆盖逐类水中运动、死亡边界或完整视觉。规则及`tmp/skill-entity-lifecycle-*`证据见[实体复核](../systems/skill/architecture.md#动画实体运动与死亡时序复核2026-11-07)，当前客户端已退出。
- 本批以前一提交`d40d303`继续，按完整hurt方法恢复30个共用实体的逐类免疫，并恢复十九个共用实体的独立摔落钩子；风波/风刃没有hurt覆写，树梯有三叉戟而无仙人掌免疫。裂隙和伤害范围改读同一来源数据，原十四项免疫保持；移除无实际调用需求的共用受伤空回调，三个独立实体不改受伤流程。共享模型表共39行同步生成。静态证据在`tmp/skill-entity-immunity-source-audit.log`，规则及验证边界见[免疫正文](../systems/skill/architecture.md#动画实体逐类伤害免疫2026-11-07)。最终build/ArchUnit及检查源集编译通过；现有`damagearea`六项通过并DONE，四世界正常保存退出、ERROR0。日志与报告为`tmp/skill-entity-immunity-{build.log,client.log,report.txt}`；不覆盖全部实体动态边界，客户端已退出。
- 弹体剩余过程静态复核（2026-11-08）：在上一发射入口批次后，逐行读取光、土、木、水、风、灵纹及金、冰的39个实际绑定过程；命中、方块命中、计时、追踪、粒子、伤害和生成分支与目标实现一致，没有新增代码修改。逐类自然施放、完整视觉、多人、1.18.2版本实机和两版本对照仍待后续验收；本轮没有新增Test或选择器。
- 继续沿用项目内置 `runSmokeClient -PsmokeOnly=skillparticle` 做客户端验证，现有8项全部通过并以 `DONE` 结束，四世界正常保存退出，日志无 `ERROR/FATAL`；没有新增Test、选择器或修改检查逻辑。
- 动画批次以前一提交`f63643a`继续。35个来源谓词及工厂分支已完整回核，目标接回undefined请求消费、实例本地过程、显式PLAY_ONCE、完成恢复及逐类0/4刻过渡；水灵阵/惊雷阵濒死close一次、尸体原版挥臂控制器及移动条件也已恢复。Q46“火鸟只播一次”是此前遗漏工厂的误判，现撤销该结论，保留其余两项来源行为。规则与验证边界见[动画正文](../systems/skill/architecture.md#过程动画调用链与控制器复核2026-11-07)，静态证据`tmp/skill-animation-source-audit.log`；精确库源码为`tmp/skill-animation-source/`（3.0.57）和`tmp/skill-animation-target/`（4.9.3）。最终build/ArchUnit及检查源集编译通过，现有damagearea六项通过并DONE，四世界正常保存退出、ERROR/FATAL0；证据`tmp/skill-animation-{build.log,client.log,report.txt}`。没有新增或修改检查代码，完整动态表现仍待验收，客户端已退出。
- 渲染批次以前一提交`49578a1`继续。35个技能及四个共享雷劫的Model/Layer/Renderer和碰撞尺寸已逐类核对，接回24个技能及四个雷劫的0度死亡旋转，11个技能继承库默认90度；八个视线骨骼补暂停条件，共用实体恢复基础刻过程后的尺寸刷新。劫云3倍与雷龙4倍渲染缩放已接回，碰撞尺寸仍分别3倍与2倍。最终build/ArchUnit及检查源集编译通过，现有tribulation五项通过并DONE、四世界正常保存退出、ERROR/FATAL0，劫云截图可见；首次FML字体阻塞未进入世界，临时关闭早期启动画面后完成检查，配置已按原字节还原。静态证据`tmp/skill-render-source-audit.log`，运行证据`tmp/skill-render-{final-build.log,client.log,report.txt,cloud.png,startup-failure.log}`；没有新增或修改测试，规则与验证边界见[渲染正文](../systems/skill/architecture.md#动画实体渲染逐类复核2026-11-07)。
- 元婴批次以前一提交`e9584d8`继续。完整实体、起手/持续/归窍、受伤/死亡、PlayerTick与菜单消息链已回核，恢复原生地面导航与摔落、挥臂、声音、持续紫烟，耗神改接CultivationSecondEvent；创造起手扣费、战斗状态、精确范围与朝向、只查当前世界主人按来源修正。发现原生按钮消息拒绝旁观者导致归窍断链，改用绑定当前菜单ID的SkillButtonPayload。最终build/ArchUnit及检查源集编译通过，现有出窍5项与技能界面9项全部通过并DONE，含旁观者真实按钮归窍；四世界保存退出、ERROR/FATAL0，早期窗口配置按原字节还原。既有出窍检查只对齐周期、场景基准和真实按钮路径，未新增检查项目。证据`tmp/yuanying-parity-{source-audit.log,final-build.log,client.log,report.txt}`；自爆死亡至重生间32条未知实体附件警告此前同场景已有，未处理。完整视觉、创造起手/跨维度主人/死亡/摔落的本轮实机证据仍缺，见[元婴正文](../systems/skill/architecture.md#元婴出窍2026-11-07)。
- 火鸟与尸体独立生命周期批次（分支`port/c-skill`，前一提交`14e2d0d`；本批与记录同一提交，最后提交以`git log -1`为准）：完整读取两来源实体、施放、每刻、受伤/死亡、独立技能、击杀成长与qishi命中链。补两种施放的finalizeSpawn、各自过程后的refreshDimensions、公用后代BREEDING初始化与实际mobInteract；尸体恢复witherSkull消息ID条件、骑乘时仅挣脱、三个技能不清副计时及成长文字只匹配整数等级。build compileSmokeJava与ArchUnit通过，没有新增或修改Test、选择器和断言。现有FireBirdCheck/CorpseCheck仅属于整套skill，按要求不重跑整套，本批未运行客户端；旧结果不作新修改的实机证据。规则、未验收范围与证据见[独立生命周期正文](../systems/skill/architecture.md#火鸟与尸体独立生命周期复核2026-11-07)，日志在`tmp/tamed-skill-{source-audit,final-build}.log`。
- 裂隙与伤害范围独立生命周期批次（分支`port/c-skill`，前一提交`e75460d`；本批实现与记录同一提交，最后提交以`git log -1`为准）：完整核对两个来源类、裂隙起手/更新/碰撞与范围更新、九份creature过程十二个生成块，恢复aiStep挥臂及无重力时序、baseTick后尺寸刷新、无声；范围补生成初始化、水中呼吸和仅实体阻挡。build compileSmokeJava与ArchUnit通过。仅一次内置`voidrift,damagearea`客户端，裂隙12项通过，范围5项通过1项失败，DONE、四世界正常保存退出、ERROR/FATAL0；失败是本轮误断言首刻下落，补核NoAI门槛后恢复位置断言并仅编译、未重跑。没有新增Test类、选择器或检查项目；FML配置按原字节还原。规则与边界见[生命周期正文](../systems/skill/architecture.md#虚空裂隙与伤害范围独立生命周期复核2026-11-07)，证据`tmp/void-area-parity-{source-audit.log,build.log,client.log,report.txt,check-compile.log}`，客户端已退出。
- 丹火调用链复核批次（分支`port/c-skill`，前一提交`0577ac0`；本批记录与台账同一提交，最后提交以`git log -1`为准）：核对召唤/自然使用、18种火块的放置/计划刻/接触、18个方块实体及两种炙烤过程，已实现部分未发现新差异。补正18个方块实体台账为“已注册”；独立类型装配已核，现有导出不含该注册表，未取得逐类型实机证据。静态证据`tmp/yihuo-chain-source-audit.log`、`tmp/yihuo-family-audit.log`，范围见[异火正文](../systems/yihuo/architecture.md#丹火调用链复核2026-11-07)。本批只改文档与台账，没有新增或修改Test、没有运行游戏，原失败证据继续保留。
- 材料目录与异火辨识基础（2026-11-08）：按1.18.2版本 `WorldbeginProcedure`、`YihuoshujvfuyvProcedure` 和玩家变量载体核对，接回225条材料目录、物品等级/品阶组件、玩家辨识附件及异火登录初始化；修正异火19条材料必须统一写入来源固定 `jindumax=6`，不再使用结构化条目数。`gradlew build`与既有`runSmokeClient -PsmokeOnly=skillparticle`均通过，报告`DONE`，客户端正常保存退出、ERROR/FATAL为0；没有新增Test类或冒烟选择器。材料袋、辨识界面、扣料和生产消费者仍未实现，skill不合main。规则见[材料正文](../systems/material/architecture.md)与[异火正文](../systems/yihuo/architecture.md)。
- 状态效果生命周期批次（分支 `port/c-skill`，前一提交 `6bd4658`；本批代码与记录同一提交，最后提交以 `git log -1` 为准）：核清七个标记效果和风飘减耗，修复共享效果在原生写入前执行开始/结束的错误，现交给原生属性钩子，覆盖普通/强制施加及隐藏弱效果恢复。build compileSmokeJava/ArchUnit通过，仅一次已有内置combat场景53项全部通过并DONE，四世界正常保存退出、ERROR/FATAL0；仅补8项真实Bug回归，没有新增Test类或选择器。FML配置按原字节还原，客户端已退出。依据和限制见[状态效果正文](../systems/skill/architecture.md#状态效果身份与原生生命周期复核2026-11-07)，证据 `tmp/effect-hooks-{source-audit.log,build.log,client.log,report.txt,config-restore.log}`。本轮未运行专服，未逐一实机验证全部效果。
- 技能箭物品逐类复核批次（分支 `port/c-skill`，前一提交 `f61dc0c`；本批代码与记录同一提交，最后提交以 `git log -1` 为准）：完整读取十种来源物品和共同恒假释放条件，恢复第10箭UseAnim.NONE，第01–09箭SPEAR、72000刻使用时长保持；同类按明确工厂参数装配，覆盖表与生成内容表同步。build/ArchUnit通过，已有内置skillarrow场景十二项通过并DONE，四世界正常保存退出、ERROR/FATAL0；没有新增或修改任何检查代码。FML配置已原字节还原。手持动画本身未逐项实机查看，具体范围见[物品正文](../systems/skill/architecture.md#技能箭物品使用动画逐类复核2026-11-07)，证据 `tmp/arrow-items-*`。
- 共用弹体保存与命中箭数批次（分支port/c-skill，前一提交a84bd91；本批代码与记录同一提交，最新以git log -1为准）：按来源持久数据接回七个字段及原生穿透读回，26类共用命中减箭数，撤掉来源未保存的击退存档。build compileSmokeJava/ArchUnit通过，现有skillparticle五项通过；同轮skillarrow十一过一失败，补诊断后仍剩17支。失败存档上方159个树叶/原木超出原7格清空范围；仅把该场景清空到18格，最终skillarrow十二项全部通过，箭雨20支、DONE、正常保存四世界退出、ERROR/FATAL0。诊断运行退出时两条ClosedChannelException保留；三次配置均原字节恢复，没有新增Test类/选择器或重跑整套skill。规则与全部证据边界见[弹体正文](../systems/skill/architecture.md#共用弹体保存与命中箭数复核2026-11-08)，tmp/projectile-state-*保留首轮、诊断和最终结果，客户端已退出。
- 弹体落地与模型批次（分支port/c-skill，前一提交a75e3f3；本批实现与记录同一提交，最后提交以git log -1为准）：修复八类落地过程的额外主人门槛，冰矛与技能箭保留来源门槛；三冰弹/灵纹弹四个完整原生模型和纹理已接回，飞叶显示cailiao_087，六类显式空栈。build compileSmokeJava/ArchUnit通过；仍用已有内置skillparticle，仅加一项真实共享Bug回归及截图。首轮五过一失败、诊断六项通过，检查改为有时限地等客户端粒子并明确核对入地，最终六项通过；三次均DONE、四世界保存退出、ERROR/FATAL0，FML配置原字节恢复。首轮原因未锁定，不归咎于实现；最终截图四个模型可见，完整视觉仍未验收。证据tmp/projectile-lifecycle-*，具体边界见[弹体落地正文](../systems/skill/architecture.md#弹体落地条件与原生模型复核2026-11-08)，客户端已退出。
- 发射入口复核批次（本批前一提交`a75e3f3`，本批提交号以`git log -1`为准）：继续按1.18.2版本实际过程核对16种非技能箭的39个绑定过程及实际发射消费者，接回统一物品松手入口、四种玩家施放速度、基础伤害、无主人水灵阵生成、宽松UUID解析、拈花飞叶到期字段、火球直接命中目标与金轮生成初始化；修正了两处来源调用参数。随后完成 skill 归属的 `friendlist` 共享附加字段消费者核对：金轮、灵纹、冰系、木系和水系的实际状态均由本地字段承载，灵纹计数残留按 Q45 保持来源取舍，`Guanghuan` 留给 world 系统。`build compileSmokeJava`（含ArchUnit）通过；沿用`runSmokeClient -PsmokeOnly=skillparticle`，8项全部通过并以`DONE`结束，四世界正常保存退出、ERROR/FATAL0，FML配置原字节恢复。未新增Test类或选择器，未逐类自然操作16种物品、未运行专用服务器、未做1.18.2实机或两版本对照。证据为`tmp/projectile-behavior-{client.log,report.txt,config-restore.log,final-build.log}`，规则见[技能正文](../systems/skill/architecture.md#弹体实际过程与发射入口复核2026-11-08)。skill未完成、不合main。
- 下一步：共享附加字段核对已完成，继续按路线处理 skill 余项；不新增无必要Test，不重跑整套旧神通，客户端沿用项目内置`runSmokeClient`。丹火炉鼎消费者随D阶段，范围的creature实际调用者与六个返回维度随E阶段；`Guanghuan` 随 world 批次处理；skill未完成、不合main。

**此前已完成的检查与历史定位**：
- 已运行一次 skill 冒烟（`gradlew runSmokeClient -PsmokeOnly=skill`）：报告以 `DONE` 结束，精简后的 `CorpseCheck` 28 项全部符合；其余检查也完成。`LingwenCheck` 的数量 1/2 差异已由 Q48 解释为来源同序的首刻命中时序，不再重复整套冒烟。
- 虚空裂隙的实体、传送与公开入口已实现，`VoidRiftCheck` 12 项通过（`-PsmokeOnly=voidrift`，往返使用 smoke 测试维度，正式维度仍随 world）；本次不再重复。伤害范围实体与 `Skills.showDamageArea` 已实现，`DamageAreaCheck` 6 项通过；九处 creature 生成者仍待接入，控雷阵落雷已接回。元婴出窍的实体与 `Skills.toggleYuanying` 已实现，`YuanyingCheck` 5 项通过；菜单按钮与出窍粒子已接入，视觉认可待核。十种技能箭与服务端选择已实现；弓入口随 equipment、轮盘已在阶段 5 接入，阳火炙烤持续行为随 yihuo。身法与轻功 7 项针对性检查已通过，不重复运行。阶段 5 的界面、五个功能入口、C 格挡和技能 HUD 已部分实现；一次 skillui 检查 9 项中 8 项通过，首项检查时序修正后仅编译，未重跑。剩余 26 项已核清，满天花雨已补齐且两项检查一次通过；315种粒子的提供器与完整资源已登记并通过粒子专项检查，元素弹体、灵纹弹和技能箭的粒子发出处已接回，动画实体、生命感知、轻功、研习、出窍和自爆的有效粒子调用也已接回；共享落雷命令与彩色闪电已接回天雷术、五雷轰顶和控雷阵；完整雷劫主路径已通过；下一步处理 equipment/yihuo/world 依赖与视觉收口。
- 本机有一个小陷阱：在 bash 的 here-document 里写含引号的长 Python 脚本会因引号错位而整段不执行，改用 Write 写到暂存目录再运行。
- `port/c-skill` 已推送到 `origin/port/c-skill`；`.eclipse/` 仍是唯一未跟踪的 IDE 配置文件。

早期分阶段操作记录（下列状态保留当时证据；当前操作与后续有效运行以上文及系统正文为准）：
0. 已提交（分支 `port/c-skill`，最新提交以 `git log` 为准）：第 1 阶段框架；第 2 阶段元素弹体 14 个；第 3 阶段动画实体基础与火、土、金、冰、光、风、雷、水（水蛇鞭、水连鞭、激流波、水龙吟、空浮球、陨河术、水灵阵、玲珑阵）、木（乱枝、灰木剑、乱木之种、灰木树梯、木篱术、毒蔓鞭、回身鞭、地蔓鞭）、剑法（`SwordEntities`、`WeaponArts`；平台 `ItemSwingEvent` 触发，2026-11-05）、灵纹（`LingwenHit`、`LingwenEntities`、`LingwenBolt`；Q45）、火鸟术（`FireBird`，基类 `TamedSkillEntity`；2026-11-07；Q46）、起尸诀（`Corpse`、`CorpseAi`、`Corpses`；2026-11-07；Q47）；虚空裂隙实体、生命周期与传送（`VoidRift`、`Skills.openVoidRift`；2026-11-07；Q49；按钮随阶段 5，正式维度随 world）；冒烟 `-PsmokeOnly=skill`（`SkillCheck`、`SkillProjectileCheck`、`SkillEntityCheck`、`SwordArtCheck`、`LingwenCheck`、`FireBirdCheck`、`CorpseCheck`；`Arena` 平地由后四者共用，此前弹体检查在不平地形上整批失败；动作列表类检查继承 `ActionCheck`）。读过程用 `TERSE=1`（去样板）、`DIFF=1`（同模板族）；新增元素族的做法见 `entity/FireEntities` 等：`SkillEntities.register` 里登记行为表，`SkillReleases.register` 登记施放，`mark_ownership.py`、`build_ledger.py`、`mark_ledger.py 已注册`、`convert_content.py`，按必要性选择验证并记录证据，再标 `行为已迁移`。
1. 第 3 阶段余项（按元素）：伤害范围实体与 `Skills.showDamageArea`、元婴出窍与 `Skills.toggleYuanying` 已实现；九处 creature 生成者与 `控雷阵` 经 `/lightninglanding` 的落雷已接回，生成者随 creature，元婴出窍和死亡粒子已接回。天雷术、五雷轰顶已接入 combat 共享落雷，两个入口的合并检查因选择取消失败，检查修正后仅编译。拂尘 `Weaponhossu01DangShiTiHuiDongWuPinShi` 的发射已由 equipment 接回，本批五项专项通过。可驯服实体（火鸟、尸体）已完成，基类与共用件见[技能架构](../systems/skill/architecture.md)“可驯服实体”；尸体的 `CorpseAi.breakFree` 与 `finish` 对应妖兽共用的 `Yaoshou001skillzhengtuo`、`Entityjinengjieshu`，creature 迁移时再决定是否公开。
2. 阶段 2：十种技能箭的实体、模式、命中、掉落、原生模型、内爆效果与服务端箭种选择已实现（Q50；共用实体命中过程实际 551 行）；`Lingwentexiao` 已随灵纹实现。待接入：equipment 的弓蓄力/发射/扣资源与 Ctrl/Alt 输入、yihuo 的炙烤持续行为；自定义粒子已接回。`SkillArrow00–10skill` 已核对为孤立过程，`ArrowSkillchufa` 仅有游离 Blockly 引用；`skill_arrowxinruzhishui` 已登记有益标记身份，未形成自然入口（Q53）。本批 `skillarrow` 冒烟 12 项全部通过并以 `DONE` 结束；最后 `build`、检查源集编译、开发服务端启动（ERROR 0 条）、台账与来源指纹检查通过。第 08 箭云命令后续静态修正已编译，该分支未运行；弓与完整炙烤链未验收。下一步为下条阶段 4–5。
3. 阶段 4 身法与轻功：`SkillMovement`、`SkillFlight`、W/S/A/D/空格/F 按下沿、每刻冷却、掠空与二十刻耗灵力、禁空已实现，7 项针对性检查一次全部通过（`-PsmokeOnly=skillmovement`），`build`、台账与文档检查通过；专用服务端启动到 Done、ERROR 0 条，未重跑旧神通。饰品飞行减耗由 equipment 监听 `FlightEnchantmentEvent`，有效轻功粒子已接回。阶段 5 已部分实现：G 键原生无槽技能菜单、四技能/翻页/十一箭种/连招、虚空/出窍/扫描/丹火/自爆、C 格挡显示与 Gedang 八刻移除，panel 的六组冷却/格挡/弓蓄力 HUD。Q52 记录扫描无粒子、自爆立即死亡和背景值 6 无写入者；保留来源行为。格挡不减伤，品阶经 `WeaponStatEvent` 的 `lianqizong.item_level_num`，lianqi/equipment 监听尚未接入。妖化已由 equipment 接回（规则和证据见下述批次），丹火放置随 yihuo，出窍和自爆的有效粒子已接回。
4. 阶段 5 验证：仅一次 `-PsmokeOnly=skillui`，8/9 项通过并 `DONE`；首项检查早于客户端菜单打开（后续菜单按钮主路径正常），已拆开发按键与初始化、后移截图，仅重新编译未重跑。HUD 已查看，默认小窗口右侧冷却裁切；菜单截图早于界面，布局视觉认可尚未完成。最后 `build compileSmokeJava`、ArchUnit、专用服务端启动、台账、文档与来源指纹检查通过；服务端终端仍不转发 stop，任务经中断结束，未验证优雅停服。上一实现提交为 `df8f929`；本批代码与本页同一提交，最新提交号以 `git log -1` 为准。skill 归属 401/401 已读；满天花雨补齐五枚扇形飞叶，`skillremainder` 两项一次通过并 DONE。第 08 箭合成过程只显示111、不附毒（Q53）；十配方已逐项核对。70 个已登记效果的来源图标已进入资源转换；build（含 ArchUnit）与专用服务端启动/正常停服通过，效果导出增至70。粒子库前置已完成类型与提供器登记，弹体侧粒子已接回，其余有效 skill 发出处已接回，完整雷劫已在后续批次接回，下一步处理 equipment/yihuo/world 依赖及视觉认可；不得据此宣称 skill 完成。
5. creature 迁移时：妖兽技能通过已提供的 `Skills.showWindBurst/launchWindBlade` 放出 `skillwind_05/06`，不直接写技能实体字段；调用者承担爆气的击退/粒子/声音和黑风1.5倍法攻及第8/32/44刻施放，skill承担生成和生命周期。
6. skill 全部阶段完成后更新 README、正文、月度日志与本页，合入 `main` 并推送，再建 yihuo 的分支。剩余跨系统小事为 `PlayerRightClick` 的开灵附魔分支归 equipment。

粒子库前置（2026-11-07，本批前一实现提交 `3406d99`；本批提交号以 `git log -1` 为准）：315个来源粒子全部已读并登记原生提供器、参数、JSON及3625张独立贴图。particle归属315/315已读，creature705/586已读，fulu230/154已读；七个纯粒子归属已转入particle。仅一次 `-PsmokeOnly=particle` 两项通过并DONE，截图样本可见；build与ArchUnit、专用服务端启动/正常停服通过，ERROR0、导出315个粒子，台账核对无问题。证据为 `tmp/particle-{build,smoke,server}.log`、`tmp/particle-report.txt` 和 `build/smoke/screenshots/particle-library.png`。粒子台账保持“已注册”，弹体侧发出处随后已接回，动画实体、生命感知、轻功、研习、出窍与自爆的有效调用也已接回，全量视觉与两版本对照未执行。后续按来源逐处区分服务端 `sendParticles` 和客户端 `addParticle`；服务端原本为空的 `Level.addParticle` 不改成广播。

注意（2026-11-04 发现并修正）：此前 `SkillEntity` 默认纹理统一为透明的 `kong`，现在改为各实体来源 `define(TEXTURE, …)` 的纹理（`entity_models.json` 的 `texture`）；冒烟检查看不到画面，阶段 5 实机核对动画实体时要确认各实体可见、动画与碰撞箱（空浮球、陨河术有碰撞箱放大）正确。

测试按必要性选择（负责人2026-11-07最新强调）：优先核对1.18.2版本实际源码并直接移植，没有必要不写Test，不为每批新增专项；必要时才做编译、加载或关键路径验收。已存在检查的纯脚本修正不重跑整套，编译并如实写明未运行。`LingwenCheck` 的 1/2 差异已记录为 Q48，检查改为接受来源顺序产生的两种结果；本次不再重复整套冒烟。

验证习惯（会话内）：按改动选择必要检查；内容表变化时运行 `convert_content.py`，新玩法先完成来源核对，再按风险选择必要验证，不默认新增专项或重复已通过的旧神通。纯检查代码修正只编译并记录未重跑。构建含结构检查；注册变化时启动一次服务端刷新导出，再运行 `check_ledger.py`。转换工具、Gradle 与游戏进程逐个运行。终端需向服务端发送 stop 时，`exec_command` 使用 `tty=true` 保留可写输入，并使用 `--no-configuration-cache --init-script tools/porting/server_stdin.init.gradle` 接通标准输入；2026-11-07 已实测正常保存、停服与任务成功退出。

其余 skill 粒子批次（2026-11-07，前一实现提交 `5f374c4`，本批提交号以 `git log -1` 为准）：有效来源粒子调用已接回，Q54 保留未绑定的金轮死亡过程。仅一次 `-PsmokeOnly=skillvisual` 两项通过并 `DONE`，客户端正常保存退出；血雾截图可见，光爆仅确认客户端后续粒子计数，截图未捕获可见效果。build、ArchUnit、台账与文档检查通过。证据在 `tmp/skill-other-particles-{build,smoke}.log`、`tmp/skill-other-particles-report.txt`。共享落雷与完整 xiulian 雷劫已在后续批次接回，下一步处理 equipment/yihuo/world 依赖与视觉认可。skill 仍未完成，不重复旧神通套件。

共享落雷批次（2026-11-07，前一实现提交 `8a78be4`；本批提交号以 `git log -1` 为准）：已实现 combat 共享 `/lightninglanding`、彩色闪电及技能落点；补出来源内部嵌套注册，台账总数 4939，来源指纹未变。Q55 保留空团队词导致参数前移。共享落雷验证（2026-11-07）：仅一次 `-PsmokeOnly=lightning`，四项中三项通过并 `DONE`，范围雷伤、红色同步/渲染器及控雷阵命令均走真实世界路径。两个技能入口的合并断言失败，检查辅助方法连续选同槽导致第二次取消；已在选择前清空并编译，未重跑，不能记为两个入口实机通过。截图早于有效画面，完整视觉待核。build（含 ArchUnit）通过，专用服务器启动、全部世界保存及正常退出通过，ERROR 0，实体导出为 63 个。该共享落雷批次未执行完整 xiulian 雷劫，后续实现与验证见完整雷劫批次。 证据在 `tmp/lightning-{build,smoke,server}.log`、`tmp/lightning-report.txt`、`tmp/lightning-check-compile.log` 及 `build/smoke/screenshots/lightning-red.png`。完整雷劫已在后续批次接回，其余跨系统依赖和视觉仍待闭合；不重跑旧神通套件。

## 先读顺序

1. 仓库根 `AGENTS.md`：项目档案、当前关口、工程与文档规则。
2. 本页。
3. `.claude/skills/minecraft-mod-porting/SKILL.md`：通用移植规则与系统工作单（规则正文）。
4. [工程结构与长期维护](engineering.md)：代码目录、边界检查、内容表、资源转换、台账、依赖治理、并行协作。
5. [系统设计](system-design.md)：系统划分、归属判定、依赖规则与公开操作。
6. [整体架构](architecture.md)与[数据与运行时](data-and-runtime.md)：源码边界、调用组织、状态归属。
7. [实施路线](roadmap.md)：批次、出口和系统完成条件。
8. 所接手系统的 `docs/systems/<系统>/README.md` 与 `architecture.md`。

## 开工前必须确认

AGENTS.md 的“当前关口”写明架构未批准时，只能做调查、文档与工具工作，不写玩法代码。2026-11-02 负责人已批准架构、工程结构与路线并记入 AGENTS.md 当前关口与[项目状态](../systems/project/README.md)，B0 已开始。批准只覆盖这三份文档；下列事项仍待负责人决定，遇到时在所属系统实现前提请裁定：

| 事项 | 当前方案 | 位置 |
| --- | --- | --- |
| 1.18.2 世界与玩家存档是否导入 | 未承诺 | [范围](../design/scope.md) |
| 界面复刻程度 | 保留全部操作与反馈，布局另评审 | [范围](../design/scope.md) |
| 发布 license | 暂填 All Rights Reserved | [架构](architecture.md) |
| 来源矛盾 Q01–Q14 | 逐项裁定，实现所属系统前解决 | [调查问题表](../reference/legacy/source-survey.md) |

## 本机环境事实

| 项 | 值 |
| --- | --- |
| JDK | Temurin 21.0.9（`C:\Program Files\Eclipse Adoptium\jdk-21.0.9.10-hotspot`）；另有 JDK 17 可用于运行 1.18.2 工程（未验证） |
| Gradle | wrapper 8.8；GRADLE_USER_HOME 为 `D:\Gradle`。本机网络约 25–40 KB/s，依赖变化后的首次构建可能需二十分钟以上（2026-11-02 升级到 NeoForge 21.1.265 用时 24 分钟），日志在 `--console=plain` 下不显示下载进度，可看 `D:\Gradle\caches\modules-2\files-2.1` 与 `D:\Gradle\.tmp` 的文件变化判断是否仍在下载 |
| 来源工作区 | `D:/yuanchuxiuzhen-1.21.1-port/yuanchuxiuzhen-1.21.1`，两个 1.18.2 工程位置见[来源说明](../reference/legacy/README.md) |
| 目标源码 | 构建后的 `build/moddev/artifacts/neoforge-<版本>-sources.jar`，含 Minecraft 与 NeoForge 源码；FML 在 Gradle 缓存 `net.neoforged.fancymodloader` |
| 构建 | `./gradlew build` |
| 运行 | `./gradlew runServer`、`./gradlew runClient`；运行目录 `run/`（已忽略） |
| Git | `origin` = https://github.com/Shane77ly/yvanchuxiuzhen-1.21.1 （公开）；凭据由 Git Credential Manager 保存的 `Shane77ly` 账户提供，走 HTTPS（本机未配置 GitHub SSH）；提交作者 `Lix <207489130@qq.com>`；`.gitattributes` 规定换行 |
| 文档与来源检查 | `python -B -X utf8 tools/docs/check_docs.py`；`python -B -X utf8 tools/porting/inventory_legacy.py --check` |

运行检查注意：

- `runServer` 启动后等待控制台输入，不会自行退出。结束 Gradle 任务后按命令行查找残留进程并结束：

  ```powershell
  Get-CimInstance Win32_Process -Filter "Name='java.exe'" |
    Where-Object { $_.CommandLine -match 'yuanchuxiuzhen-rebuild' } |
    ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
  ```

- 开发运行的专用服务器不要求 eula.txt；不要替负责人写入 EULA 同意。
- Windows 控制台以系统代码页显示中文 Mod 名时乱码；以 `run/logs/latest.log`（UTF-8）为准。
- 运行日志、临时脚本放 `tmp/`。
- 客户端行为由会话自己验证：`./gradlew runSmokeClient`（见[工程结构](engineering.md)“验证基础设施”），结果在 `build/smoke/report.txt`，截图在 `run/screenshots`；不要让负责人手工开客户端复现。

## 下一步：C 玩家与战斗核心

范围与出口在[路线](roadmap.md)“批次和出口”的 C 行：依次完成 attribute → relation → xiulian → gongfa → combat → skill → yihuo → equipment，每个系统按移植技能的系统工作单推进，完成后合入 `main` 再开下一个系统的分支。当前进展与具体操作见上文“进行中”。B1 已于 2026-11-03 结束，结果见[项目状态](../systems/project/README.md)“B1 出口核对”。

### B0 结束时的状态与未完成项去向

B0 完成步骤 1–5 后由负责人 2026-11-02 指示结束（逐步记录见[月度记录](../systems/project/logs/2026-11.md)）：

- 依赖：NeoForge 21.1.265、ModDevGradle 2.0.148，`neoforge.mods.toml` 下限为负责人决定的 21.1.228；此后版本不必追最新，只需不低于 21.1.228。
- 骨架：公共入口调用各单位的 `<名称>Setup.register`，客户端入口 `@Mod(value = ModId.VALUE, dist = Dist.CLIENT)`；Mod ID 常量 `platform/ModId` 同时是唯一命名空间。实测专用服务器只构造公共入口，客户端先公共后客户端。
- ArchUnit：`ArchitectureTest` 检查[工程结构](engineering.md)六条规则，随 `gradlew check` 运行。
- 台账：`build_ledger.py` 同步 4,938 条来源注册（B0 结束时为 4,867 条，2026-11-02 补入漏扫的 71 条，见月度记录）到 31 个单位与 `project` 台账；`check_ledger.py` 检查列、覆盖、目录与归属、目标 ID 命名空间与唯一性、状态值、非“待处理”须“已读”。
- 片段合并：`mergeAssetFragments` 把 `src/main/lang/<语言>/<单位>.json` 与 `src/main/sounds/<单位>.json` 合并为生成资源。

步骤 6 的内容表、资源转换与注册表导出并入[路线](roadmap.md)“B1 实施步骤”的步骤 2–4。步骤 7 的运行机制在首个需要它的系统批次按正式结构实现，在开发服务端与客户端实测后写回[工程结构](engineering.md)。B0 曾选定的来源实物与读过的归属仍可作为首个实例：

| 机制 | 曾选实物 | 归属 | 预计批次 |
| --- | --- | --- | --- |
| 玩家附件（Codec、按[复制策略表](../reference/legacy/inventory/clone_policy.csv)设置死亡复制、同步给本人） | 2026-11-03 由 attribute 的贡献附件先实现并实测（做法见[工程结构](engineering.md)“数据与存档契约”）；原选实物 `colddownGold`（`non_death_only`，非死亡复制）随 skill 迁移 | attribute | C 已完成 |
| payload | 2026-11-03 由 relation 的界面按钮 `RelationButtonPayload` 先实现并实测（做法见[工程结构](engineering.md)“数据与存档契约”）；原选实物 `AnjianWMessage`（按键 W）随 skill 迁移 | relation | C 已完成 |
| 代码注册转数据定义 | 原初系统唯一的附魔 `yvanchuxitong:chain`（目标 `yvanchuxiuzhen:chain`） | combat | C |
| GameTest 独立源集（确认不进入发布 JAR）、JUnit 规则测试 | JUnit 已由 attribute 接入（2026-11-03）；GameTest 按首个需要世界内自动化的系统选取 | — | C |
| 方块实体与菜单 | 家具 `Beechfurniture14`：`RandomizableContainerBlockEntity`，27 格，自有菜单 `Beechfurniture14GuiMenu`；归属已读（27 格无槽位过滤，不属于 `storage`） | decor | D |
| GeckoLib 实体（选定 GeckoLib 版本） | `Silkwormmoth`：`PathfinderMob`，实现 `IAnimatable`；归属已读（只由蚕盒生成） | craft | D |

完整雷劫批次（2026-11-07，前一实现提交 `268d024`；本批与本页同一提交，最新提交号以 `git log -1` 为准）：自然突破、劫云与七种天雷、地滚雷/九霄雷柱/化灵雷龙、成功进阶和失败路径已接回。Q56 保持来源的功德动态循环、死亡状态误写、跳过后继续放雷与跨维度不补状态。`read_procedure.py` 已修正表达式延迟折叠，不存在此前工具产生的“延迟未启动”疑点。共享动画模型与渲染移到 platform，skill 只改引用；`extract_entity_models.py --owner xiulian` 与独立彩色闪电的内容转换路径已接通。build、ArchUnit、检查源集编译通过；仅一次 `-PsmokeOnly=tribulation`，5 项全部符合并 DONE；客户端正常保存退出，劫云截图可见。专用服务器一次启动、原生 stop 正常保存退出，ERROR 0，导出实体 67 个。证据在 `tmp/tribulation-{build,smoke,server}.log`、`tmp/tribulation-report.txt` 与 `build/smoke/screenshots/tribulation-cloud.png`。四个修炼实体台账改为“行为已迁移”，完整视觉、全分支时序、多人和两版本对照未执行。后续弓与拂尘已接回（见“进行中”）；下一步按 CodeGraph 与来源核对 equipment 的品阶、饰品飞行减耗与妖化，再接 yihuo/world 依赖及视觉收口；旧神通不重跑，skill 未完成，不合 main。

## 每次会话的流程

按 AGENTS.md 的“进度记录与交接”执行；分支、合并、worktree 与提交格式见[工程结构](engineering.md)的“Git 与会话协作”。工作中按移植技能的系统工作单推进，遇到来源矛盾先记录并提请裁定。

## 已知陷阱

- 邻近工程 `yuanchuxiuzhen-1.21.1` 的玩法、数值、删减和完成状态不是依据；它的 Parchment 配置（1.21.11）也不适用于本项目。
- 来源中按 Java 注册的附魔、群系、地物在 1.21.1 必须改为数据定义；燃料与堆肥改为 NeoForge 数据映射。
- 来源语言文件中混有机器配置（如商品列表），迁移时先拆出，不能当作普通翻译。
- 不要用“注册成功”“构建通过”“文件存在”标记玩法完成；状态按台账约定填写。

## 文档地图

| 内容 | 位置 |
| --- | --- |
| 总入口 | [docs/README.md](../README.md) |
| 项目状态与决定 | [项目状态](../systems/project/README.md)，日志 `docs/systems/project/logs/` |
| 来源证据与索引 | [来源说明](../reference/legacy/README.md)、[索引](../reference/legacy/inventory/README.md) |
| 目标 API 待验证范围 | [目标 API](../reference/target-api.md) |
| 编码与测试规则 | [开发规则](coding-policy.md) |
| 文档维护 | [文档维护](documentation.md) |

## 2026-11-08 异火材料投掷批次

按1.18.2版本源码接回 `cailiao_221`–`229`、`231`–`239` 共18种材料物品、对应投掷实体、灵力/沉默/连续施放冷却、无重力8刻寿命、粒子、实体命中灼烧、潜行放置火块及非潜行补燃料；实体使用原生 `ThrownItemRenderer`。`build` 通过；沿用项目内置 `runSmokeClient -PsmokeOnly=skillparticle`，客户端进入世界、报告 `DONE`、四维度正常保存退出、未新增 Test 或选择器。该验证只覆盖公共注册、资源加载和客户端启动，不宣称18种材料完整自然施放、命中、获取消费、武器触发、多人或视觉已验收。未完成项见[异火正文](../systems/yihuo/architecture.md#异火材料投掷物2026-11-08)。

## 2026-11-08 异火武器触发来源核对

按 1.18.2 版本 `FireskillProcedure` 源码和来源可达性索引核对：该过程使用 `no_ext_trigger`，没有事件注解、物品回调或其他非过程类调用者；`procedure_reachability.csv` 标记为 `False`。它只在自身被调用时以 10% 概率检查主手 `cailiao_221` 或 `lianqizong.fenglei`，再执行固定范围火伤。目标保持来源可达性，不把孤立过程接入 combat、物品使用或玩家刻更新，也不新增对应 Test。结论已写入[异火正文](../systems/yihuo/architecture.md#武器触发来源核对2026-11-08)。下一步继续核对异火获取/消费的真实所属系统；炉鼎热量消费者仍留到 D 阶段。

## 2026-11-08 异火材料灵力不足反馈

按1.18.2版本 `Cailiao221KeYiShiYongYuanChengWuPinDeTiaoJianProcedure` 逐行复核18种材料的共用条件分支，发现目标灵力不足时遗漏来源红色玩家提示；已在 `YihuoItem` 扣费前补回，`cailiao_234` 的检查8点/扣80点来源差异保持。`gradlew build` 和项目内置 `runSmokeClient -PsmokeOnly=skillparticle` 通过，报告 `DONE`，客户端真实进入世界并正常保存退出，日志无 `ERROR/FATAL`。该场景只作加载回归，不作为提示自然交互验收；未新增 Test、选择器或断言。下一步继续按路线核对异火获取/消费的真实所属系统，炉鼎热量消费者仍留到 D 阶段。

## 2026-11-08 功法 `/technique` 命令批次

按 1.18.2 版本 `TechniqueCommand` 与 `TechniqueGOProcedure` 接回权限 1 的 `/technique`：贪婪字符串参数按 0–22 号顺序生成未研习功法书，承载物品、名称、品阶、所需境界、灵根要求、简介、五行值和十二项档位均保留来源顺序，第十三项移速档位保持空字符串；数值解析失败为 0，缺失文本为空，无效承载物不生成书。书在命令坐标掉落，拾取延迟 10 刻。`gradlew compileJava` 已通过；本批完成后运行完整 build、既有 `runSmokeClient -PsmokeOnly=skillui`、文档检查和台账检查，未新增 Test 或冒烟选择器。

## 2026-11-08 开灵工具机批次

当前分支仍为 `port/c-skill`，`.eclipse/` 为用户 IDE 配置，保留且不提交。本批按1.18.2版本实际源码接回开灵附魔的右键工具入口、`projectile_gongjvshechu` 弹体、`gongjvfu`、`gongjvgao`、`gongjvchan`、`gongjvchu` 四种工具机及其 GeckoLib 模型、动画和贴图。首刻冻结主人/工具/等级，命中方块或16刻到期生成工具机；斧、镐、锄、铲状态机按来源扫描、移动、采集和寿命过程实现。`gradlew build`、文档检查、台账检查和既有 `runSmokeClient -PsmokeOnly=equipmentweapon` 已通过；客户端正常保存退出，专用服务器正常 `stop`，实体导出 91 个。现有客户端检查不替代四类工具机自然操作专项，1.18.2 实机、两版本对照、多人及完整视觉仍待后续。

本批完成后继续按路线推进 skill 收口及其后的 yihuo、equipment 依赖批次；不新增 Test、专项选择器或 Computer Use 流程。

当前继续处理 equipment 法宝批次。本批 `fabao_011`、`fabao_012`、`fabao_014`、`fabao_015`、`fabao_016`、`fabao_021` 与 `fabao_022` 已按 1.18.2 版本源码接回；`fabao_016` 的完整构建、内置 `runSmokeClient -PsmokeOnly=equipmentweapon`、文档与台账检查均通过，客户端报告 `DONE`、正常保存退出，代码提交为 `0a47d01`。`fabao_015` 已完成静态对照；`fabao_021` 已完成无服务端玩法差异的静态对照；`fabao_022` 的属性实现、完整 build、内置客户端 smoke、文档与台账检查均通过，已提交 `e6e6fe9`。下一步继续处理不依赖正式 `dimensionrenjian` 映射的法宝；`fabao_017` 依赖动画实体和蛊虫收纳链，暂留待其依赖系统闭合，`fabao_013` 仍暂缓。所有客户端验证沿用项目自带入口，不使用 Computer Use；不新增无必要测试。

## 2026-11-08 飞行法器批次

`port/c-skill` 在 `cdb8758` 之后接回 8 个飞行法器的专用物品工厂、共享实体、字段存档、骑乘飞行、充灵力、取回、死亡掉落、乘客偏移及 GeckoLib 资源。首次专用服务端启动发现实体模型表有记录但 `entities.json` 缺少 8 个实体，已补齐来源尺寸和 `MONSTER` 类别；修复后 `gradlew build`、专用服务端启动至 `Done`、原生 `stop`、`check_docs.py`、`check_ledger.py` 与 `git diff --check` 通过，服务端导出 101 个实体且台账 0 项问题。项目内置 `runSmokeClient -PsmokeOnly=equipmentweapon` 五项既有弓与拂尘检查全部 `DONE`，客户端正常进入世界并保存退出，日志无 `ERROR/FATAL`。未新增 Test、冒烟选择器、断言或 Computer Use；内置客户端检查只覆盖注册、资源加载和启动回归，不替代飞行法器自然操作、完整视觉、多人、1.18.2 实机及两版本对照验收。下一步为提交本批并继续 equipment 及后续依赖批次。

## 2026-11-08 本命培养批次

当前分支仍为 `port/c-skill`，继续按 1.18.2 版本来源过程推进 equipment。本批接回 `DantianbenmingfabaoProcedure`、`BenmingfabaotishengProcedure`、`DantianbenmingexpProcedure` 与 `DantianbenmingxianshiProcedure` 的实际调用链：修炼每秒事件触发、结丹期门槛、启灵与体质培养速度、单次一级上限、耐久消耗、法术/精神攻击交替升级、物品组件保存及丹田进度提交均已实现；补回经验上限为 0 时的来源初始化顺序。

本批已提交 `5fc3133`，未新增 Test、冒烟选择器、断言或 Computer Use。验证沿用项目自带 `runSmokeClient -PsmokeOnly=equipmentweapon`，仅用于注册、资源加载和客户端启动回归；完整本命培养自然行为、存档重载、1.18.2 实机、两版本对照、多人和完整界面视觉仍待专项验收。后续投掷法宝进展见下节。

## 2026-11-08 投掷法宝批次与并行协作

负责人授权启用子 agent 加速，当前采用共享工作树的三路分工：来源与物品入口核对、模型资源迁移、台账与正文；主会话整合玩法并串行执行构建、客户端与专服验证。沿用当前完整移植 goal，不建立重复 goal。当前分支 `port/c-skill`，前一提交 `5fc3133`；本批与本页同一提交，最新提交号以 `git log -1` 为准。`.eclipse/` 保留且不提交。

已按 1.18.2 版本八个物品类、八个实体类及完整绑定过程接回 `fabao_001`–`008`：互斥的松开/使用刻发射入口、原引用临时栈与最后一件耗尽语义、首刻初始化、九毒、逐类伤害与回收分支、`003` 的十次延迟毒域、八种弹体及四套专用模型。来源疑似错误保持，具体字段边界与证据在[投掷法宝正文](../systems/equipment/architecture.md#投掷法宝fabao_0010082026-11-08)。八件物品与八种弹体登记“行为已迁移”，不据此标记整个 equipment 或 skill 完成。

最终 `gradlew build`（含既有 test/check 与 ArchUnit）通过；内置 `runSmokeClient -PsmokeOnly=equipmentweapon` 五项既有检查全部 `INFO`、`0 FAIL`、`DONE`，客户端正常保存四维度并退出。专用服务器一次启动至 `Done`，实体导出109个，通过原生 `MinecraftServer.halt(false)` 正常保存四维度退出；客户端与专服日志 `ERROR/FATAL=0`。证据为 `tmp/fabao-final-build.log`、`tmp/fabao-smoke.log`、`tmp/fabao-report.txt`、`tmp/fabao-client-latest.log`、`tmp/fabao-server-latest.log`。本批未新增或修改 Test、检查代码、选择器，也未使用 Computer Use；这些客户端检查只覆盖注册、资源加载和启动回归，八法宝逐类自然发射/命中/冷却/回收、存档重载、完整视觉、1.18.2 实机、两版本对照与多人验收未执行。

下一步：继续 D 炼丹分合丹调用链，随后药理台、百草鉴和毒药；药瓶消费批次见“进行中”顶部。skill 的 creature/world 遗留随所属系统闭合；`fabao_013` 依赖正式人间维度，`fabao_017`/`01701` 依赖动画实体和蛊虫收纳。沿用项目内置客户端入口并按风险验证，当前不合 `main`。
