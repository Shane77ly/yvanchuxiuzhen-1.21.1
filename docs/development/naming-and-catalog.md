---
topic: naming-and-catalog
role: guide
status: draft
updated: 2026-11-02
---

# 命名与编号台账

## 编号的含义

来源身份的通用规则（编号、符号、注册 ID、显示名与存档键分别记录，及各类对象的稳定身份）见[台账参考](../../.claude/skills/minecraft-mod-porting/references/parity-ledgers.md)的“来源身份”。本项目示例：`cailiao_002` 的 `002` 是来源编号，工作表第二行不是编号 2；`yaoshou_017` 与其形态/技能也不能只靠数字 17 认定是同一个注册条目。

当前已核对语言资源中的示例：`yvanchuxiuzhen:cailiao_002` 为上等皮毛，`yvanchuxiuzhen:yaoshou_017` 为寄魂蟹，`yvanchuxiuzhen:itemfulu_37` 为轮回符，`yvanchuxiuzhen:book_022` 为竹简。其中竹简是公共载体，中文名称不等于其承载的全部书籍内容。

| 对象 | 本项目命名建议 |
| --- | --- |
| 注册内容 | 优先保留来源 ID；Java 符号在语义确认后命名 |
| Variables 字段 | 采用含义明确的 camelCase；持久化键用 snake_case |
| NBT / 字典键 | 同名键按所有者区分 |
| 动态编号键 | 例如 `jineng01…16` 可表达为有固定槽位意义的列表，但保留编号映射 |
| 公式中的局部变量 | 使用有意义名称 |

拼音可用于无合适英文的修仙概念，并在全项目保持一致。

## Excel 内容

[工作簿](../../outputs/01a19713-78e6-72b2-844d-c8e8d0ce7ded/1.18.2-content-register.xlsx)包含阅读说明、内容编号、状态变量、存档常量、过程入口、菜单入口、字典键候选、状态生命周期、动态表达式、系统归组、跨系统调用，共 11 页。

内容编号表保留全部 4,938 条注册声明，含中文名称、原有数字、完整 ID、注册符号及出处。状态变量表包含 211 个实际字段；4 个 `DATA_NAME` 常量单列。字典键候选记录 2,841 个“来源 Mod + 字面量键”分组，供人工拆分所属对象，不将其宣称为 2,841 个已验证 NBT 状态。

新增架构页记录 211 个字段的类型匹配访问与复制策略、630 个动态表达式分组、35 个候选归组（28 个系统、3 个内容族及平台、粒子库、流程、玩家面板）和 66 组来源调用方向；后两者不代表已批准架构或实现进度。架构页是生成证据，人工目标名称仍在原有黄色列填写。

[工程结构](engineering.md)方案把人工映射移到各系统台账 CSV，工作簿改为由 CSV 生成的审阅视图；批准并在 B0 切换前，下述人工列规则仍有效。

原有数字采用文本单元格，保留前导零。来源位置、原有名称和提取结果属于生成列；黄色列为人工评审的目标名称、目标所有者及备注。目标列空白表示尚未决定，不是自动采用来源名或自动判定无需移植。

## 更新方式

静态事实由 [提取器](../../tools/porting/inventory_legacy.py)、[基础调查](../../tools/porting/investigate_legacy.py)、[架构扫描](../../tools/porting/analyze_architecture.py)、[编辑器图对照](../../tools/porting/catalog_source_design.py)和[候选归属整理](../../tools/porting/build_architecture_catalog.py)生成 CSV/JSON；Excel 由 [工作簿构建器](../../tools/porting/build_variable_register.mjs)从 JSON 生成。两类来源分开：CSV/JSON 负责机械提取，工作簿人工列负责映射评审；正式规则及批准结论写入所属 Markdown 正文。

初次生成运行构建器；后续刷新必须传入 `--review-workbook <当前工作簿路径>`，按“来源身份”保留人工列。刷新前保留已填写文件的副本；若来源条目删除或身份改变，先人工处理对应映射，不把未匹配行悄悄当作已完成。不要直接修改 CSV 的自动生成列来代替源码调查。

完整数值公式、单位、取整与实际消费者写在系统 `balance.md`；Excel 用于编号映射、数值记录、同类对比和调整讨论，不复制成另一套生效规则。

## 状态证据

证据等级与检索口径见[验证参考](../../.claude/skills/minecraft-mod-porting/references/verification.md)和台账参考。当前目标名称尚未逐条确认，未开始内容迁移。
