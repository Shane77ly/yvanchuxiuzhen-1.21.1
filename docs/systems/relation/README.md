---
topic: relation
role: entry
status: partial
updated: 2026-11-18
---

# 关系

2026-11-20接回好友子表在灵纹、出窍与玩家克隆中的共享及保存语义；针对缺失子表、共享引用和编码重读的受控数据测试通过，战斗、多人和真实重进仍待验证。

标识 `relation`，类别：系统。职责：组队、好友、敌友判定（[系统设计](../../development/system-design.md)）。

2026-11-13补入神傀符驯服的好友表替换与团队交接公开操作，真实001妖兽成功及保存重读必要单项通过，结果见[符箓月志](../fulu/logs/2026-11.md#2026-11-13-神傀符18完整路径)。

当前阶段：敌友判定、团队与阵营、团队招募令、好友界面与好友申请界面已实现（`system/relation`）。2026-11-18补齐好友实时境界查询及登录、重生、每秒恢复的本体团队刷新，必要编译、架构及受控客户端检查通过，结果与未验边界见[关系月志](logs/2026-11.md#2026-11-18-关系境界与团队镜像必要客户端)。台账：`ling_15`、`friendgui`、`zuduiteam_gui` 为“行为已迁移”；`gui_0301` 入口不可达（Q20），为“待处理”。

星标与普通好友行均读取已匹配玩家保存的`LEVEL_NAME`，真实每秒入口可恢复受控清空的本体团队；真实死亡及同档重进后关系字段保持。死亡后等待40客户端刻的检查只证明字段最终恢复，不能独立证明重生事件的即时写回。取值、所有权与时序见[境界查询](architecture.md#好友行实时境界2026-11-18)、[重生回写](architecture.md#重生本体团队回写2026-11-18)及[每秒刷新](architecture.md#每秒本体团队刷新2026-11-18)。

- [架构与调查正文](architecture.md)：来源数据与行为、归属阅读、目标实现、来源问题 Q19（保持来源行为、待再议）与 Q20、验证。
- 候选归属 86 个元素全部已读；`FindTheEnemy` 改归 combat。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

已对照 `typed_field_accesses.csv` 的 `team`、`friendlist` 字段与 `nbt_key_calls.csv` 的实体 NBT `team`、`friendlist` 覆盖全部外部读写点。关系读写由 `system/relation/api/Relations` 提供；好友境界文本通过同目录的公开查询事件取得。

| 操作 | 形式 | 调用者（来源中的读写点） |
| --- | --- | --- |
| 查询团队 | `team(实体)`：玩家读关系附件，其他实体读阵营附件 | 召唤与投射物生成时复制施放者团队的技能、符箓、妖兽、法宝、蛊虫、灵兽袋等（约 120 处） |
| 查询实体字段团队 | `entityTeam(实体)`：单独读实体阵营附件 | yihuo 丹火放置和异火命中，保留玩家能力字段与本体字段的区别 |
| 刷新玩家实体团队 | `refreshEntityTeam(服务端玩家)`：只把能力团队写入本体实体团队，不改友方集合 | 本系统登录、每次重生事件及xiulian每秒恢复入口；来源`PlayerJoinGame`、`Entitybereborn`、`Playerhuifu` |
| 在线好友境界查询 | `FriendRealmNameEvent(已匹配服务端玩家)`：同步请求其保存的境界文本，必须由所有者回填 | xiulian监听并仅读自己的`LEVEL_NAME`；不写关系附件或好友行 |
| 敌友判定 | `isFriendly(判定方, 对方)`：判定方的友方集合含对方团队 | combat（`EntityHurt`、`EntityBeforeHurt`、`ShanghaiGo`）、skill、fulu、creature、guchong、equipment 法宝等经 `Friendpanding*` 的全部调用 |
| 方块友方查询 | `friendlyTeams(主人)` 返回只读视图；`isFriendly(集合, 对方)` 按对方当前团队判断 | yihuo 火块保留来源共享友方引用，序列化与重载时保存为独立集合；不允许调用者修改关系 |
| 继承阵营 | `copyAllegiance(主人, 非玩家实体)`：团队与友方集合整体复制 | 同“查询团队”的召唤与投射物生成 |
| 灵纹与出窍友表 | `initializeRune` / `runeCount` / `setRuneCount` / `runeOwner` / `incrementRuneForOwner` / `restoreLegacyRune`；`initializeProjection` / `projectionOwner` | skill 按来源共享和消费好友子表；旧字段迁入及实体持久化边界见正文 |
| 共享实体阵营 | `shareEntityAllegiance(来源非玩家实体, 目标非玩家实体)`：团队值复制、友方集合运行期共享，独立存档重载后分离 | creature 008召唤007，保留来源直接赋入同一CompoundTag的后续写入效果 |
| 蛊罐共享友方 | `shareAllegianceToBlock(主人, 方块持久数据)`与`shareBlockAllegiance(方块持久数据, 虫, 虫团队)`：共享来源friendlist子标签，团队由虫物品单独赋值 | guchong放置与地上放虫，独立存档重载后解除共享 |
| 驯服替换好友与团队 | `replaceTamingFriends(目标)`先复制目标能力好友并移除旧实体团队；`setTamingTeam(施放者, 目标)`另写实体团队 | Creature神傀符命中；来源`Itemfulu18DangTouZhiWuJiZhongShiTiShiProcedure:225–234`，保留原生认主和粒子在两次写入之间的顺序 |
| 设置团队 | `setTeam(非玩家实体, 团队名)` | 只写 `team` 的生成过程、creature 的 `EntityTeamShezhi`（团队名为实体类型 ID，`:` 改为 `_`） |
| 加入友方 | `addFriendlyTeam(非玩家实体, 团队名)` | `EntityTeamShezhi` 把自身团队加入友方集合 |
| 玩家能力临时队伍 | `temporaryTeam(玩家)`、`setTemporaryTeam(服务端玩家, 值)`；独立存盘、死亡复制、只同步本人 | Dungeon准入读取来源能力`teamwork`；03页来源显示/退出已接，建立/加入非空生产者未找到，见Q123 |
| 退出能力临时队伍 | `leaveTemporaryTeam(服务端玩家)`先按旧队名显示中文提示，再清空能力队名并仅同步本人 | Equipment 03页原生button 5；来源`Teamleave` |
| 本体临时队伍 | `entityTemporaryTeam(实体)`、`setEntityTemporaryTeam(实体, 值)`；独立保存本体`teamwork` | Dungeon维度进入和成功/失败清理，不反写能力同名字段 |

约定：普通团队公开写操作只用于非玩家实体，对玩家调用时抛出异常；灵纹遍历和出窍入口按其接口说明使用玩家或实体；驯服两操作与玩家本体团队刷新明确只改本体实体附件，即使目标带玩家能力也不修改能力团队/好友。玩家能力关系由本系统登录和界面流程维护。方块实体敌友查询已随 yihuo 首个调用者接入，读取者持有只读视图，不反写玩家关系。

临时队伍03页来源显示/退出已接，原生K导航、真实按钮消息、本人同步与NBT编码必要单项通过；非空建立/加入断链Q123与超宽来源布局Q124见[临时队伍正文](architecture.md#临时队伍03页与退出2026-11-14)，自然组队、多人及默认窗口鼠标可达仍未验。

## 未实现

- `gui_0301` 改团队名界面：入口不可达（Q20），待裁定。
- 方块阵营保存与消费已接回 yihuo 火块和 guchong 蛊罐；旗母与副本方块随各所有者接入。

本轮副本操作见[本系统月志](logs/2026-11.md)，其他历史批次见[项目月度日志](../project/logs/2026-11.md)。
