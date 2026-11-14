---
topic: npc
role: entry
status: partial
updated: 2026-11-14
---

# 修士

标识 `npc`，类别：系统。职责：修士身份、AI、交互入口、门徒（[系统设计](../../development/system-design.md)）。

已接入`businessman_01`–`07`、`09`–`11`与`daguanjia`共11个实体的来源属性、初始化、免伤、AI、模型/衣甲和全局右键；对话交任务系统，商品与商店交经济系统。拍卖修士定义由本系统提供副本。上述新增包已完成源码静态核对、定向转换、编译和架构检查；NPC自身自然右键/AI/模型与保存重进尚未实机验证，不表示NPC系统完整迁移或验收。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 来源候选与已读状态见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)；候选数量不是已实现或已验证的功能数。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 公开操作

`NpcStructureReleases`以LOW优先级订阅World结构后处理事件，按来源刷新03号计分板并补充花繁城09/10/11；21号任务只按真实注册身份匹配，目标21号尚未实现，保持R13开放，不注册占位实体。结构顺序及有限验证见[World月志](../world/logs/2026-11.md#2026-11-14-固定结构解封与镇妖塔后处理)。

`NpcDefinitions.snapshot()` 返回来源拍卖修士目录副本；`AuctionNpcRequestEvent`由经济系统请求，本系统提供目录。来源`Npcshujvjiaru`只生成维度`worlddata`中的NPC定义，不涉及玩家能力字段；已对照`typed_field_accesses.csv`，不得由经济系统反写修士目录。

`NpcDialogueRequestEvent`提交玩家、按来源顺序的对话/任务脚本、商品表及商店说明，任务系统选择第一个未完成脚本。对话会话与任务目录由任务系统独占写入，商品与商店说明由经济系统的公开操作写入。对应`PlayerRightClickEntity`的`renwuitem/renwuliebiao/productlist/shopinformation`外部读写，修士系统不持有这些玩家附件。

`businessman_08`、`21`–`30`、阴司NPC族、乾坤道人/门徒方块与管理路径、完整自然投放及验证仍开放；具体入口和来源差异见[正文](architecture.md)。本轮变化见[修士月志](logs/2026-11.md)。
