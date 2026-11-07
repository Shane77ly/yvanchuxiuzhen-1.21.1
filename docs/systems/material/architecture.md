---
topic: material
role: architecture
status: partial
updated: 2026-11-08
---

# 材料、采集与辨识：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 定义与状态

材料共有身份来自注册 ID；等级/品阶在实例 `cailiaolevel/cailiaopinjie`，不是不同材料 ID。B 的 `WorldbeginProcedure:43–46` 在世界加载时写 `lianqicailiaolist`：每行包含材料 ID、重量及后续三元组，既有属性贡献、附魔，也有特殊标记。

`B:QiancailujiandingProcedure:49–149` 读取玩家 `playercailiaojiexi`，可从普通库存与玩家材料仓扣除辨识材料。其遍历等级为 1–40、品阶为 0–4；这是该消费者的范围，不推断所有材料永远只能有这些值。`YihuoshujvfuyvProcedure` 单独初始化异火辨识。

## 目标所有权与接口提案

材料定义保存编号/ID、重量、类别和各生产用途描述；材料实例保存等级、品阶及真实实例覆盖。辨识记录以材料 ID 为键保存进度、已揭示条目；`txtitemid/txtname` 等当前显示缓存不混入永久进度。

采集公开产物与职业进度操作；lianqi/liandan/zhenfa 各解释本系统用途，不让材料服务执行所有生产规则。辨识需要从 storage 扣料时调用其明确的数量操作，不能直接修改 `renwuitem`。

动态键 `{materialId}jindu/jindumax/yaoxiao{index}` 变为 ID 索引的辨识记录。顺序、重复材料条目和同一材料多种用途必须保留；不能按 ID 简单覆盖后丢失第二组效果。

## 验证与范围

来源注册与目录都要覆盖，包含矿物、妖兽材料、灵木、宝石、异火等。采集掉落、材料生成等级与品阶、辨识扣料和生产消费者必须形成完整路径。当前没有批准重新计算材料权重或贡献预算。

## 2026-11-08 基础目录与实例载体

从锁定的 1.18.2 材料目录提取 225 条 `cailiao_*` 定义，写入 `content/material/definitions.json`。目标目录保留材料 ID、固定重量、经验、属性条目、特殊效果条目和特殊标记；资源由 `ContentFiles` 在 Mod 启动时用 Codec 解码，重复 ID 或未知字段直接中止启动。材料物品的 `cailiaolevel/cailiaopinjie` 改为 `material_instance` 组件，玩家 `playercailiaojiexi` 改为 `material_analysis` 附件，附件按来源玩家变量在死亡时复制并只同步本人。

`YihuoshujvfuyvProcedure:22–58` 的登录入口由 yihuo 通过 `Materials.initializeYihuo` 调用，覆盖 `cailiao_221`–`cailiao_239` 的 19 个目录 ID，并按来源固定写入 `jindumax = 6`，使用一次性标记避免重复初始化。当前材料定义把固定重量/经验与属性、特殊效果分开保存，`entryCount()` 只统计结构化属性和特殊效果；原始目录三元组的完整顺序、逐条显示与扣料仍待材料辨识与 storage 批次接回，不能把本批初始化当作完整辨识验收。

验证证据：`gradlew build` 通过，包含 ArchUnit；沿用现有 `gradlew runSmokeClient -PsmokeOnly=skillparticle` 进入真实 1.21.1 客户端世界，资源加载、登录、保存退出完成，报告含 `DONE`，客户端日志 `ERROR=0`、`FATAL=0`。该已有选择器只证明公共资源和登录生命周期没有启动回归，不替代材料界面、扣料或两版本对照；本批没有新增 Test 类或冒烟选择器。
