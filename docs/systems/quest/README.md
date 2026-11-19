---
topic: quest
role: entry
status: partial
updated: 2026-11-15
---

# 任务

标识 `quest`，类别：系统。职责：对话动作、任务、悬赏、成就（[系统设计](../../development/system-design.md)）。

B1 已登记本单位不依赖新代码类型的内容身份与资源（方块8、物品8），只表示身份、资源与基础属性就位。

高级通宝玉固定银行单步入口已通过127条共同客户端检查，证据见[经济月志](../economy/logs/2026-11.md#2026-11-14-银行账户与高级通宝玉登录)。本批另接通普通文字/四选项、物品/条件/跳转/任务目录存储等动作，承接NPC与经济脚本；六条悬赏的生成、追踪、05荧光蛊、击杀结算与刷新已经接线。新增包已完成定向转换、编译与架构检查；银行四选项、普通玉扣料/发奖、商店/银行/基础出售打开和登录清会话已通过必要客户端单项。其余NPC脚本、任务目录分支、悬赏与05自然追踪仍未游戏验证。本批接回U键任务列表、八行选择、分类/翻页/继续及高级NBT/名称物品判断、跟随/取消/删除/物理伤害/法术精神标记/移动动作；来源分页和连续启动遮蔽保持。连续聊天解释器与/taskduihua命令已接回；任务列表必要验证、连续任务自然启动/选择消费、触发配置页与成就自然授予仍开放，详细边界见正文与本月记录，不表示整个任务系统完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围124个元素，本批新增16个完整任务目录元素已读；本轮另核读`PlayerTickxitong`与`Duihuashijian`的银行路径分支，保留整元素“候选”，不计入完整已读数量。这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`DisguisedQuestRuntime`订阅World结构后处理，独占任务伪装方块的单槽展示、朝向、连续脚本及本体商店配置；对话调用`DialogueActions`、玩家商品会话调用`EconomyMenus`。14项精确字符串由提取表维护，NPC21真实身份仍开放；寻灵盘/裂隙消费者已接，结果见World月志。动态材料/陈氏商品补丁由`QuestBlockRefresh`先于原生方块交互执行，Q120必要验证见[月志](logs/2026-11.md#2026-11-14-任务商品动态刷新)。规则见[任务正文](architecture.md#结构任务伪装方块2026-11-14)。

| 公开操作 | 来源字段与当前边界 |
| --- | --- |
| `BankDialogueActions.request(ServerPlayer)` | 固定的`bankbankbank`单步“打开界面/银行”请求；写本任务会话的脚本、游标0和启动标记 |
| `BankDialogueActions.snapshot(Player)` | 只读该银行会话的脚本、游标和启动标记，不查询或改写其他任务档案 |
| `BankDialogueRequestEvent`订阅 | 高级通宝玉入口由经济系统发事件；任务单向调用经济的`BankMenus.openLogin`，不让经济直接写任务附件 |
| `QuestJournalRuntime`内部菜单操作 | U键只请求打开本人目录；八行选择、分类、页码与继续由服务端更新SESSION，本人附件同步，客户端不提交整份档案 |
| `DialogueActions.request/snapshot/journal/finished` | 普通脚本会话、任务目录与完成状态；返回副本，启动与选择由服务端执行 |
| `EconomyDialogueRequestEvent`、`NpcDialogueRequestEvent`订阅 | 入口只提交脚本，任务独占游标、选项与任务存储；商品和界面通过经济公开操作交接 |
| `QiankunDialogueRequestEvent`订阅 | NPC只发送已准备的点击者；Quest持有`chujianqiankun`原脚本、选择、持久检查点和寻灵盘发放，见[Q119](architecture.md#q119-初见乾坤道人) |
| `BountyRuntime`内部生命周期 | 公共告示牌拥有悬赏与猎人标记；生成前等级通过`CreatureBounties.prepare`提交，荧光蛊由`GuchongTracking.createBound`生产，费用和奖励交`EconomyCurrency/BankAccounts`写入 |

已对照`typed_field_accesses.csv`中`Playerrightclickwithitem`、`PlayerTickxitong`、`Duihuashijian`、`PlayerJoinGame`、`PlayerRightClickEntity`和悬赏过程的外部读写：会话与任务目录由本系统写入；商品表、商店说明、菜单、补领费用与银行奖励由经济API处理，追踪物品与妖兽等级由各实体所有者处理。银行固定脚本没有“任务储存”步骤，不另造任务jindu；其他来源动作的未接范围见[普通对话正文](architecture.md#普通对话与任务目录2026-11-14)。

八个空谷残卷结构模板保留任务方块内嵌的成就授予命令，目标脚本入口至命令执行器静态可达；八模板与八成就已由静态检查通过，真实运行仍待核实，逐项证据见[Q162](architecture.md#q162-八个结构的成就动态授予链)。本轮变化见[任务月志](logs/2026-11.md)，共同银行运行结果由[经济月志](../economy/logs/2026-11.md)维护。
