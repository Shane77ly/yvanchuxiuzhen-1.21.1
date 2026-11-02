---
topic: materials
role: architecture
status: draft
updated: 2026-11-01
---

# 材料、采集与辨识：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 定义与状态

材料共有身份来自注册 ID；等级/品阶在实例 `cailiaolevel/cailiaopinjie`，不是不同材料 ID。B 的 `WorldbeginProcedure:43–46` 在世界加载时写 `lianqicailiaolist`：每行包含材料 ID、重量及后续三元组，既有属性贡献、附魔，也有特殊标记。

`B:QiancailujiandingProcedure:49–149` 读取玩家 `playercailiaojiexi`，可从普通库存与玩家材料仓扣除辨识材料。其遍历等级为 1–40、品阶为 0–4；这是该消费者的范围，不推断所有材料永远只能有这些值。`YihuoshujvfuyvProcedure` 单独初始化异火辨识。

## 目标所有权与接口提案

材料定义保存编号/ID、重量、类别和各生产用途描述；材料实例保存等级、品阶及真实实例覆盖。辨识记录以材料 ID 为键保存进度、已揭示条目；`txtitemid/txtname` 等当前显示缓存不混入永久进度。

采集公开产物与职业进度操作；refining/alchemy/formations 各解释本系统用途，不让材料服务执行所有生产规则。辨识需要从 storage 扣料时调用其明确的数量操作，不能直接修改 `renwuitem`。

动态键 `{materialId}jindu/jindumax/yaoxiao{index}` 变为 ID 索引的辨识记录。顺序、重复材料条目和同一材料多种用途必须保留；不能按 ID 简单覆盖后丢失第二组效果。

## 验证与范围

来源注册与目录都要覆盖，包含矿物、妖兽材料、灵木、宝石、异火等。采集掉落、材料生成等级与品阶、辨识扣料和生产消费者必须形成完整路径。当前没有批准重新计算材料权重或贡献预算。
