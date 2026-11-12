---
topic: gongfa
role: entry
status: partial
updated: 2026-11-08
---

# 功法

标识 `gongfa`，类别：系统。职责：功法书、研习、参悟、星宿穴位学习（[系统设计](../../development/system-design.md)）。

当前阶段：主体规则已实现（2026-11-08 补齐 `/technique` 命令）。已实现功法定义（67 本，`content/gongfa/techniques.json`）、功法书实例组件、研习、功法栏与提交、功法修炼与闭关经验、升级成长、提示文本、书架藏宝给书 API、参悟台（参悟与继承）和管理命令；既有功法/参悟客户端36项通过。本批接回 book 自然研习与 skill 共用装配页的调用链，并补功法成功粒子，完整构建与现有skillui客户端29项通过，覆盖自然研习、真实装配和开窗时获得功法经验后关闭重开的保持；具体范围见正文，不能推广到未测分支。星宿穴位学习小游戏在来源中不可达，未迁移（Q28，待裁定）；book 书架藏宝自然使用、完整两版本对照及多人仍未完成。

- [架构与调查正文](architecture.md)：来源入口、数据所有者、调用契约、生命周期和裁定事项。
- 候选归属 478 个元素，全部已读；这是检索范围，不是该单位已验证功能数。见[归属表](../../reference/legacy/inventory/ownership_assignment.csv)。
- [内容台账](ledger/content.csv)：来源注册的目标 ID、归属状态与迁移状态（`tools/porting/check_ledger.py` 检查）。
- [系统导航](../README.md)记录源码引用规则与各项证据范围。

## 依赖方向

2026-11-03 会话决定（gongfa 开工时）：来源中 gongfa 读 xiulian 的境界、灵根、功德、罡煞、寿元与闭关天数（研习条件与功法修炼），xiulian 读主修功法的名字、品阶与归属（特殊功法效果、修为、金丹与元婴）。按“系统之间不得相互依赖”定为 gongfa → xiulian 单向：xiulian 需要的主修功法信息由 gongfa 向 xiulian 提交（与 attribute 的贡献同一形式），xiulian 不读 gongfa。因此 C 中 xiulian 先于 gongfa 实现。

## 公开操作

已对照 `typed_field_accesses.csv` 汇总功法栏 `gongfa` 的来源外部读写：读 388 处中本系统以外约 195 处——xiulian 读名字、品阶、层数与研习者（目标改为本系统提交 `MainTechnique`），combat 读五行值、层数与名字，book 读档位，来源 panel 页读名字与研习者，flow 与 cooking 读名字；写 6 处来自来源 panel 页（放入、取出）、fulu 与 flow（重置）。目标04共用装配菜单归 skill，panel 只导航；代码为 `system/gongfa/api/Techniques`。

| 操作 | 形式 | 调用者 |
| --- | --- | --- |
| 查询功法书 | `book(物品)` → `TechniqueBook`（定义、研习者、层数、经验、十三项加成） | book、skill装配页、combat 等 |
| 查询功法栏 | `slot(玩家)`、`main(玩家)` | combat（五行值、层数、名字）、skill装配页、flow、cooking |
| 放入、取出功法栏 | `setSlot(玩家, 物品)`，随后提交 xiulian 主修功法信息与 attribute 加成层贡献 `gongfa:main` | skill装配菜单、fulu、flow |
| 研习 | `study(玩家, 书)`：境界与灵根条件、记下研习者、层数 1；成功后发出 `TechniqueStudiedEvent` | book（右键）；skill 监听事件附赠技能 |
| 生成功法书 | `createBook(TechniqueDefinition)` | flow、material 等给书的玩法；`/technique` 命令由本系统注册 |
| 书架藏宝 | `giveTreasureBook(玩家, 藏宝等级)` | book（书架藏宝右键） |

约定：功法栏的书升级、放入或取出后自动重新提交；十三项加成只在本人研习时计入，五行与层数不检查研习者。

自然研习分派和成功答题计数归 book，见[书籍正文](../book/architecture.md#自然研习入口2026-11-08)；04八槽生命周期只维护于[skill 装配正文](../skill/architecture.md#共用功法与技能装配页2026-11-08)。本系统只接收公开调用，不让菜单直接写 `GongfaData.SLOT`。

本轮变化统一记录在 [项目月度日志](../project/logs/2026-11.md)。
