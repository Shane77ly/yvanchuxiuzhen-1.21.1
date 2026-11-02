---
topic: systems-index
role: entry
status: current
updated: 2026-11-01
---

# 全量系统架构调查

每个系统已有具体调查正文；当前目标接口和数据模型均为待批准提案。表中数量来自 MCreator 源目录与显式归组，不能当作已移植数或互相独立的功能数。

| 系统 | 来源元素归组数 | 状态 |
| --- | ---: | --- |
| [修炼、身份与生命周期](cultivation/README.md) | 152 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [功法、研习与学习小游戏](gongfa/README.md) | 479 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [属性来源、派生与显示](attributes/README.md) | 9 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [战斗、状态、附魔与关系](combat/README.md) | 338 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [技能、施法、移动与异火](abilities/README.md) | 458 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [装备、法宝、本命与饰品](equipment/README.md) | 281 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [材料、采集与辨识](materials/README.md) | 403 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [炼丹、药理、药物与毒药](alchemy/README.md) | 147 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [炼器、图纸、熔炼与修理](refining/README.md) | 135 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [符箓知识、制作与释放](talismans/README.md) | 218 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [阵位、阵元、阵旗与阵法效果](formations/README.md) | 59 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [妖兽、生物族、驯养、蛊虫与傀儡](creatures/README.md) | 736 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [NPC 身份、交互与世界定义](npc/README.md) | 64 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [货币、银行、商店与现场拍卖](economy/README.md) | 261 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [任务、对话动作、悬赏与成就](quests/README.md) | 112 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [维度、投放、场景、副本与城镇](world/README.md) | 426 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [生活生产、作物与家具](life/README.md) | 2326 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [普通容器、乾坤袋与玩家材料仓](storage/README.md) | 149 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |
| [公共书籍、阅读与答题](knowledge/README.md) | 64 | 已列入口/载体/接口与取舍；实现及运行验收未开始 |

跨系统流程、客户端呈现和平台注册不是额外玩法模块，其入口、依赖、延迟行为及代码隔离见 [公共入口调查](../development/entry-and-dependency.md)。

## 源码定位规则

A = 来源工作区的 `reference/old-1.18.2-mcreator/1.18.2yvanchuxiuzhen`；B = `reference/1.18.2yvanchuxitong`。来源工作区位置见 [来源说明](../reference/legacy/README.md)。

正文的 `A:FooProcedure:10–20` 表示该工程 `src/main/java/net/mcreator/yvanchuxiuzhen/procedures/FooProcedure.java` 的对应行；B 使用 `net/mcreator/yvanchuxitong`。`*Menu` 位于 `world/inventory`，`*Entity` 位于 `entity`；其余完整路径见 [Java 文件索引](../reference/legacy/inventory/java_files.csv)。区间表示本轮人工检查的源片段，未声称整个类逐分支验收。

静态范围包括全部 7,874 个 Java、7,465 个 MCreator 元素和 3,428 个过程的连接调用对照；动态键、状态归属和生命周期另有完整索引。人工语义核对依据各正文列出的具体位置。未运行 1.18.2 或 1.21.1 游戏。
