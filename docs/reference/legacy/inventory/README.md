---
topic: legacy-source/inventory
role: data
status: generated
updated: 2026-11-01
---

# 1.18.2 版本静态索引

来源、范围和人工核对见 [调查正文](../source-survey.md)。当前索引覆盖全部 7,874 个 Java 文件，注册提取器未解析的声明数为 0；这不代表全部行为已经核实。

| 文件 | 内容 |
| --- | --- |
| [summary.json](summary.json) | 注册计数、来源清单哈希、同路径项和提取限制 |
| [registrations.csv](registrations.csv) | 4,938 条注册声明、来源 ID、Java 符号、文件与行号 |
| [content_catalog.csv](content_catalog.csv) | 加入中文语言名、原有数字标记、编号族；模块匹配仅为候选 |
| [player_fields.csv](player_fields.csv) | 原始玩家字段提取结果 |
| [variables_detailed.csv](variables_detailed.csv) | 211 个玩家/维度/主世界数据字段，初值、存档键和读写候选 |
| [storage_constants.csv](storage_constants.csv) | 4 个 DATA_NAME 存档名称常量，未计入状态字段 |
| [variables_summary.json](variables_summary.json) | 状态、Java、资源、调用及限制汇总 |
| [java_files.csv](java_files.csv) | 全量 Java 文件、目录类别、过程调用与事件入口数量 |
| [procedures.csv](procedures.csv) | 3,428 个过程类、事件订阅、调用目标及静态入边 |
| [procedure_calls.csv](procedure_calls.csv) | 7,683 处调用位置；未进行完整 Java 类型/动态可达性分析 |
| [menu_classes.csv](menu_classes.csv) | 92 个菜单类与 addSlot 语句数；循环槽位数另核 |
| [field_accesses.csv](field_accesses.csv) | 5,752 处同名字段读写候选，接收对象须人工确认 |
| [nbt_key_calls.csv](nbt_key_calls.csv) | 30,945 处字面量 get/put 候选调用，包含普通字典 |
| [literal_key_catalog.csv](literal_key_catalog.csv) | 2,841 个来源 Mod + 字面量键分组；不同容器尚未分离 |
| [workbook_data.json](workbook_data.json) | 工作簿的数据输入，不作为人工评审结论 |
| [source_manifest.csv](source_manifest.csv) | 31,623 个纳入文件的大小和 SHA-256；范围见来源入口 |

## 架构调查新增索引

| 文件 | 内容与口径 |
| --- | --- |
| [architecture_summary.json](architecture_summary.json) | 结构扫描总量及局限 |
| [source_design_summary.json](source_design_summary.json) | 7,465 元素类型、锁定标记与连接图调用对照 |
| [source_elements.csv](source_elements.csv)、[element_files.csv](element_files.csv) | 源元素、目录、文件关联 |
| [element_triggers.csv](element_triggers.csv)、[element_architecture.csv](element_architecture.csv) | 2,689 触发属性、1,347 相关结构定义 |
| [blockly_procedure_calls.csv](blockly_procedure_calls.csv)、[procedure_design_comparison.csv](procedure_design_comparison.csv) | 连接/游离块及 3,428 个过程调用集合对照 |
| [method_calls.csv](method_calls.csv)、[procedure_reachability.csv](procedure_reachability.csv) | 调用方法/实参与类级入口图，非运行证明 |
| [class_surfaces.csv](class_surfaces.csv)、[entity_surfaces.csv](entity_surfaces.csv) | 全部类的客户端引用/保存/监听表面，307 实体相关类的继承/AI/同步 |
| [event_entries.csv](event_entries.csv)、[network_surfaces.csv](network_surfaces.csv)、[command_surfaces.csv](command_surfaces.csv)、[menu_surfaces.csv](menu_surfaces.csv) | 531 注解方法、86 消息、22 命令、92 菜单 |
| [indirect_surfaces.csv](indirect_surfaces.csv)、[language_payloads.csv](language_payloads.csv) | 2,515 间接入口；101 长文本/分隔文本候选，不全是机器配置 |
| [key_expressions.csv](key_expressions.csv)、[dynamic_keys.csv](dynamic_keys.csv)、[dynamic_state_keys.csv](dynamic_state_keys.csv) | 40,173 键调用，动态语法及可识别载体子集；保留匿名 helper 实参还原 |
| [typed_field_accesses.csv](typed_field_accesses.csv)、[clone_policy.csv](clone_policy.csv)、[state_lifecycle.csv](state_lifecycle.csv) | 显式类型/作用域匹配、183 复制策略与 211 字段生命周期 |
| [ownership_assignment.csv](ownership_assignment.csv) | 人工维护的元素归属：`已读` 行按[系统设计](../../../development/system-design.md)“归属判定”读过来源规则，`候选` 行是批量归组结果；不是生成结果 |
| [ownership_proposal.csv](ownership_proposal.csv)、[class_ownership.csv](class_ownership.csv)、[system_coverage.csv](system_coverage.csv) | 由归属表推导的元素、Java 类与系统汇总；候选行不作为实现依据 |
| [module_dependencies.csv](module_dependencies.csv)、[dynamic_key_catalog.csv](dynamic_key_catalog.csv) | 66 组来源依赖、630 个载体/表达式分组 |
| [architecture_workbook.json](architecture_workbook.json) | 新增架构工作表的生成输入 |

## 生成顺序

```powershell
python -B -X utf8 tools/porting/inventory_legacy.py
python -B -X utf8 tools/porting/inventory_legacy.py --check
python -B -X utf8 tools/porting/investigate_legacy.py
python -B -X utf8 tools/porting/analyze_architecture.py
python -B -X utf8 tools/porting/catalog_source_design.py
python -B -X utf8 tools/porting/build_architecture_catalog.py
```

读取源码的工具默认定位同级 `yuanchuxiuzhen-1.21.1`，支持 `--source-workspace <path>`；归属整理器读取前序 CSV 与人工维护的 `ownership_assignment.csv`；归属表缺少某个元素时直接报错。读过来源规则后在归属表中修改归属并把状态改为 `已读`，再重新运行归属整理器。按上述顺序生成，源数据没有变化时复用已有结果。生成结果不手动写入目标实现结论；Excel 黄色人工列按 [命名与台账](../../../development/naming-and-catalog.md)维护。

同命名空间中的完整 ID 才是身份；不同命名空间的同名路径不直接构成冲突。自动分组与字段访问数量仅用于检索。动态键、命令、资源及未解析类型的跨调用需要继续人工核对，不能把“没搜到”写成“功能不存在”。
