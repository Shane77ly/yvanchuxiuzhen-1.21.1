---
topic: guchong
role: entry
status: partial
updated: 2026-11-20
---

# 蛊虫

2026-11-20 修正手持罐、蛊笛和地上罐释放时的个体字段与属性恢复顺序；编译通过，实际释放、战斗和保存重进未验证，见[本月记录](logs/2026-11.md#2026-11-20-三种入口的放虫状态恢复)。

2026-11-20接回妖兽与蛊虫击杀成长的来源分支顺序、逐级经验与属性重算；限定静态接线，实际战斗成长和存档仍待验证，见[成长正文](architecture.md#妖兽与蛊虫击杀成长2026-11-20)。

标识 `guchong`，类别：系统。职责：蛊虫实体、蛊罐、认主与释放（[系统设计](../../development/system-design.md)）。

当前阶段：B1 身份、资源与基础属性已登记（方块 3、物品 13、声音事件 2）；D/E 批次已接入 `guchong_03`、`guchong_04` 实体和三档蛊罐方块初始实现。R05/R10已实现摄虫网公开收纳操作，捕获结果以个体组件接入既有蛊罐菜单和编号槽保存，最小单项已通过；运行边界见[正文](architecture.md#2026-11-13-摄虫网收纳与蛊罐消费者)。03/04单虫物品的野生/认主释放入口、关系团队读取与三档地上罐专属工厂已接回。九槽罐第九槽的认主、自然计划刻放虫/回罐、死亡清槽、Ctrl收罐、手持回收及活动虫/背包罐真实退出重进专项已通过；见[本批正文](architecture.md#2026-11-13-0304单虫释放与蛊罐生命周期)。R10编号01实体、单虫释放和共用击杀成长已接回，28条玩法检查与真实退出重进单项通过，结果见[本月记录](logs/2026-11.md#2026-11-13-编号01完整来源包)；其余编号、其他容量全部自然分支、完整AI战斗/成长/表现、多人和蛊虫系统总体验收仍未完成。

蛊笛`fabao_018`的普通右键六罐分页界面、八按钮服务端操作与潜行全局收回已装配，来源空闲动画和界面资源已转换。必要单项通过真实物品入口、客户端按钮、库存UUID定位、只读展示、沉默边界和保存重进，三张中文截图已核；首次页0、右箭头条件、空UUID匹配和部分收回不写罐保持来源行为。结果见[本月记录](logs/2026-11.md#2026-11-14-蛊笛六罐释放与潜行收回)，规则见[蛊笛正文](architecture.md#2026-11-14-蛊笛六罐释放与潜行收回)。受控样本为七罐与03双虫，全容量、全部编号、完整自然战斗/生存获取、表现、多人和两版本对照继续开放。

三档地上罐的1/4/9满槽与独立装饰物经必要单项真实原位重进、收罐再放置和双端同步通过；完整自然放虫战斗及各档其余分支仍待。结果与边界见[原位重进正文](architecture.md#三档地上蛊罐原位重进2026-11-18)。

人间道单一样本已通过自然飞蝗、原生摄网、小蛊罐装放、精血认主、同档重进及计划刻触发放出。放出的飞蝗原生近战击杀了受控目标羊；羊移除后 40 刻内未自然回罐（[Q234](architecture.md#q234-普通动物死亡后的守罐目标)）。无控制目标的战斗与其余回罐分支仍待实测。见[蛊虫月志](logs/2026-11.md#2026-11-19-人间道实捕飞蝗至原罐放出)。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 归属范围 76 个元素，其中 64 个已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

R13已接入05荧光蛊物品、飞行实体、悬赏坐标生产和释放/回收生命周期；定向`guchong05`必要单项已通过普通/追踪物品入口、中途真实保存重进、百刻返还与计时字段边界，完整自然悬赏与跨维失联仍待验。普通虫不自动获得目标，追踪虫保持同维主人查找与100刻回收顺序；具体边界及Q87/Q91见[05正文](architecture.md#2026-11-14-05荧光蛊与悬赏追踪)。

## 公开操作

`GuchongGrowths.awardOwnerExperience` 在蛊虫标签实体每次升级时处理来源主人查找与养蛊经验；职业数据由 `Professions` 写入。

`GuchongTracking.createBound(ServerPlayer, BlockPos)`由悬赏提交主人和目标坐标，生产来源字段的荧光蛊物品；释放、追踪、回收、计时与字段复制均由蛊虫系统维护。该路径只取实体UUID，不访问玩家能力字段。

蛊虫真值由本系统维护：个体数据随实体保存，手持罐槽位数据随物品保存，地上罐库存与释放计数随方块实体保存。释放、回罐、死亡清槽和菜单操作为系统内部操作，不允许外部直接修改罐或虫的持久数据。`GuchongCapture.captureWild(服务端世界, 目标实体, 主人UUID)` 是 `equipment` 的摄虫网收纳操作：由本系统判定认主状态、按来源实体ID寻找对应物品、保存个体字段、交给主人并移除目标；返回已捕获、已认主或无对应物品，法宝只负责时序与粒子反馈。未找到主人时仍移除可捕获目标，保持来源顺序；不增加虫标签过滤。该操作未读取或写入其他系统玩家字段，对照 `typed_field_accesses.csv` 的017两个过程无外部玩家能力读写。

`GuchongVictimDataEvent` 与 `GuchongUpgradeEvent` 是击杀成长流程的公开事件。生物系统响应前者并返回死者等级与额外经验；升级后响应后者，按来源死者妖兽标签选择死者属性重算分支。未命中该标签时由本系统重算升级蛊虫属性。事件订阅方不得直接写蛊虫个体持久数据；养蛊经验通过 `Professions.addExperience(owner, Profession.INSECT, amount)` 交由副职业系统写入。主人UUID非空但在当前维度查找不到主人时，保留来源在扣经验并增加等级/上限后异常中断的行为；具体顺序与未验边界见[编号01正文](architecture.md#2026-11-13-编号01完整来源包)。运行与存档验证边界见[正文](architecture.md)。

外部字段已逐项核对 `docs/reference/legacy/inventory/typed_field_accesses.csv`：

| 来源读取或写入 | 目标公开操作 | 核对范围 |
| --- | --- | --- |
| A `Taoguan01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:39,70` 的玩家 `friendlist/team` | `Relations.shareAllegianceToBlock/team` | 放置时使用玩家关系能力；与玩家本体 `team` 区分 |
| A `Taoguan01itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:62,64,183` 和 `GuchongAnniuzhixingProcedure:57,59,120,204` | `Relations.shareEntityAllegiance/team` | 手持罐与蛊笛按钮链已接入并通过必要单项，不能读取 relation 数据包 |
| A `Guchong01itemDangYouJianDianJiFangKuaiShiFangKuaiDeWeiZhiProcedure:39,85,131` | `Relations.team/setTeam` | 01/03/04同族物品释放已接入，编号01与03/04入口必要单项已通过；完整战斗未验 |
| A `EntityDeathProcedure:241,254` 的玩家 `insect_experience` | `Professions.addExperience(owner, Profession.INSECT, 新等级 × 10)` | 每次升级逐级增加；字段由 profession 独占写入 |
| A `Guchong02itemDangYouJianDianJiKongQiShiShiTiDeWeiZhiProcedure:15,27` 的 `tizhiitem` | `Cultivation.tizhiBoolean/editTizhi` | 02右键首次写四字段并一次同步；重复拒绝，体质真值由xiulian拥有 |
| B `Taoguan01ZaiFangKuaiShangYouJianProcedure:182,183` 的 `lctrl/lalt` | `ModifierKeys.ctrl/alt` | 回收地上罐 |
| B 地上罐认主与目标筛选直接读取实体本体 `team` | `Relations.entityTeam` | 保留本体字段与玩家能力字段的来源差异 |

实体等级、额外击杀经验和实体类型标签不属于玩家字段访问索引；其来源读写由击杀成长事件约束：Creature只提供等级/额外经验查询，并在死者标签选择重算分支时更新其拥有的妖兽属性，不操作蛊虫实体持久状态。罐 `friendlist` 的共享引用由 `Relations.shareAllegianceToBlock/shareBlockAllegiance` 承担；不得绕过接口写其他系统附件。

摄虫网收纳的来源实体本体`team`在目标由relation附件拥有，`GuchongCapture`通过`Relations.entityTeam`读取后保存到虫物品；不从蛊虫实体的旧式持久标签取第二份团队值。

本轮变化见[蛊虫月志](logs/2026-11.md)，此前变化在[项目月度日志](../project/logs/2026-11.md)。

酒蛊02已接回一次性体质入口，2026-11-15补齐创造模式不扣件适配；修复后的必要客户端单项待执行，历史“创造保留通过”结论已在[月志](logs/2026-11.md#2026-11-15-酒蛊创造模式数量修复)纠正。虫卵为静态物品，采药锄生产者已由Equipment接入。吞噬效果与03/04目标、共享击杀成长已有实现，来源04仍吃03。来源培养入口断链Q163及完整自然战斗/表现仍开放，规则见正文。
