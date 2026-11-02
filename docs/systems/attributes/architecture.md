---
topic: attributes
role: architecture
status: draft
updated: 2026-11-01
---

# 属性来源、派生与显示：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 数据与调用

`A:ShuxingjisuanZongProcedure:27–122` 比较四件防具及主手；防具使用 `bubianzhi` 标记，变化调用 `ShuxingjisuanProcedure`，主手种类变化调用 `Shuxingjisuan2Procedure`。`A:PlayerTickProcedure:83,351–361` 还从定期更新与功法切换进入计算。

来源中同时存在 Variables 基础字段、`*_display`、实体实际属性、装备 `lianqizong`、功法属性和 `tizhiitem` 修正。`A:ShujvtongbuProcedure` 将部分显示/资源值写到实体 `synchronous*` NBT；其名为“同步”并不能独立证明网络同步已发生。

具体矛盾：`ShuxingjisuanZongProcedure:84–86` 处理腿部装备时更新了 `kuijia4` 的比较标记，而前面该分支保存的是 `kuijia3`。这要作为源行为问题裁定，不能复制后再靠反复重算掩盖。

## 目标所有权与接口提案

建立明确输入集合：成长基础、功法实例、装备实例、体质/状态修正。属性计算返回派生结果；服务端适配层将对应结果写到 Minecraft 属性，UI 消费只读快照。每个属性标明基础、加算、乘算、上限、取整与实际消费者。

调用形态采用 `recalculate(player, contributions)` 和只读查询即可，不为每个属性建立 interface/repository。输入拥有者负责产生贡献；属性公式不得反向修改装备、功法或成长存档。

加载/来源变化 → 计算 → 实体属性生效 → 必要显示同步。缓存由来源变化维护；来源检测策略替换前要核对同物品种类但组件改变的情况，不能照抄仅比较 Item 的主手逻辑。

## 验证范围

全量字段的明确接收类型已索引。尚不能把所有带 `_display` 的数值认定为最终战斗真值：实际战斗读取位置必须逐属性记录。数值验收使用独立算例和游戏中的真实扣血/恢复，不能以 Tooltip 正确代替生效。
