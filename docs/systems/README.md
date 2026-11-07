---
topic: systems-index
role: entry
status: current
updated: 2026-11-08
---

# 全量系统架构调查

各单位的划分、类别与职责以[系统设计](../development/system-design.md)为正文；当前目标接口和数据模型均为待批准提案。数量为[归属表](../reference/legacy/inventory/ownership_assignment.csv)的候选口径，不能当作已移植数或互相独立的功能数。

| 基座 | 中文 | 候选元素 | 已读 | 状态 |
| --- | --- | ---: | ---: | --- |
| [`particle`](particle/README.md) | 粒子库 | 307 | 0 | 只有内容台账；没有玩法规则 |

没有编辑器元素的注册（声音事件）按[注册归属表](../reference/legacy/inventory/registration_ownership.csv)归属；尚未确定归属的注册放在 `project/ledger/content.csv`，当前没有。

| 系统 | 中文 | 候选元素 | 已读 | 状态 |
| --- | --- | ---: | ---: | --- |
| [`attribute`](attribute/README.md) | 属性 | 3 | 3 | 公开操作、贡献汇总与附件已实现，开发客户端检查通过；提交系统的真实路径未验证 |
| [`relation`](relation/README.md) | 关系 | 86 | 86 | 敌友判定、团队、好友与申请界面已实现，开发客户端检查通过；两人流程与改名入口（Q20）未完成 |
| [`material`](material/README.md) | 材料 | 372 | 0 | 225 条目录定义、实例组件、辨识附件与异火登录初始化已接入；辨识界面、扣料和生产消费未完成 |
| [`storage`](storage/README.md) | 储物 | 116 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`profession`](profession/README.md) | 副职业 | 21 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`gongfa`](gongfa/README.md) | 功法 | 475 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`xiulian`](xiulian/README.md) | 修炼 | 170 | 17 | 调查正文已建立；实现及运行验收未开始 |
| [`combat`](combat/README.md) | 战斗 | 258 | 12 | 调查正文已建立；实现及运行验收未开始 |
| [`skill`](skill/README.md) | 技能 | 375 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`yihuo`](yihuo/README.md) | 异火 | 97 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`equipment`](equipment/README.md) | 装备 | 334 | 253 | 调查正文已建立；实现及运行验收未开始 |
| [`plant`](plant/README.md) | 植物 | 812 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`cooking`](cooking/README.md) | 烹饪 | 322 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`liandan`](liandan/README.md) | 炼丹 | 150 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`lianqi`](lianqi/README.md) | 炼器 | 143 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`fulu`](fulu/README.md) | 符箓 | 233 | 5 | 调查正文已建立；实现及运行验收未开始 |
| [`craft`](craft/README.md) | 工艺 | 128 | 23 | 调查正文已建立；实现及运行验收未开始 |
| [`world`](world/README.md) | 世界 | 104 | 1 | 调查正文已建立；实现及运行验收未开始 |
| [`treasure`](treasure/README.md) | 宝箱 | 114 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`town`](town/README.md) | 城镇 | 36 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`dungeon`](dungeon/README.md) | 副本 | 43 | 8 | 调查正文已建立；实现及运行验收未开始 |
| [`creature`](creature/README.md) | 生物 | 707 | 585 | 调查正文已建立；实现及运行验收未开始 |
| [`guchong`](guchong/README.md) | 蛊虫 | 71 | 35 | 调查正文已建立；实现及运行验收未开始 |
| [`npc`](npc/README.md) | 修士 | 72 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`zhenfa`](zhenfa/README.md) | 阵法 | 59 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`economy`](economy/README.md) | 经济 | 263 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`quest`](quest/README.md) | 任务 | 124 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`book`](book/README.md) | 书籍 | 70 | 0 | 调查正文已建立；实现及运行验收未开始 |

| 内容族 | 中文 | 候选元素 | 已读 | 状态 |
| --- | --- | ---: | ---: | --- |
| [`decor`](decor/README.md) | 装饰 | 1,045 | 3 | 调查正文已建立；实现及运行验收未开始 |
| [`festival`](festival/README.md) | 节日 | 54 | 0 | 调查正文已建立；实现及运行验收未开始 |
| [`tiandao`](tiandao/README.md) | 天道 | 36 | 0 | 调查正文已建立；实现及运行验收未开始 |

| 汇总 | 中文 | 候选元素 | 已读 | 状态 |
| --- | --- | ---: | ---: | --- |
| [`panel`](panel/README.md) | 玩家面板 | 121 | 0 | 调查正文已建立；实现及运行验收未开始 |

跨系统流程、客户端呈现和平台注册不是额外玩法模块，其入口、依赖、延迟行为及代码隔离见 [公共入口调查](../development/entry-and-dependency.md)。

## 源码定位规则

A = 来源工作区的 `reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen`；B = `reference/1.18.2yvanchuxitong`。来源工作区位置见 [来源说明](../reference/legacy/README.md)。

正文的 `A:FooProcedure:10–20` 表示该工程 `src/main/java/net/mcreator/yvanchuxiuzhen/procedures/FooProcedure.java` 的对应行；B 使用 `net/mcreator/yvanchuxitong`。`*Menu` 位于 `world/inventory`，`*Entity` 位于 `entity`；其余完整路径见 [Java 文件索引](../reference/legacy/inventory/java_files.csv)。区间表示本轮人工检查的源片段，未声称整个类逐分支验收。

静态范围包括全部 7,874 个 Java、7,465 个 MCreator 元素和 3,428 个过程的连接调用对照；动态键、状态归属和生命周期另有完整索引。人工语义核对依据各正文列出的具体位置。未运行 1.18.2 或 1.21.1 游戏。
