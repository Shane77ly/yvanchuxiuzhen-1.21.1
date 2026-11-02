---
topic: world
role: architecture
status: draft
updated: 2026-11-01
---

# 世界：架构调查与提案

来源事实与目标方案分别陈述。目标方案未批准；源码定位采用 [系统导航](../README.md)的 A/B 规则。
## 1.18.2 状态与生成

范围包含 8 维度、68 群系、220 个 Feature 注册、161 结构文件；编辑器有 66 structure 元素和 35 feature 元素，二者与注册/文件计数不是同一口径。自然投放、固定场景、指令放置和副本刷出分别记录。

`B:WorldbeginProcedure:29–54` 的 worlddata 初始化有开关，但后面的材料/药草/服饰/结构/悬赏字符串赋值在开关之外；每次入口执行都会重写这些定义。`WorldcloseProcedure:26` 只复位 worldchufa。不能把所有这些字符串误判为允许永久在线编辑的世界真值。

## 目标所有权与接口提案

定义：维度/群系/生成配置与结构。实例：维度状态与已生成标志。城镇归 [town](../town/architecture.md)，副本归 [dungeon](../dungeon/architecture.md)。MapVariables 的主世界范围和 WorldVariables 的维度范围分别保存。

world 提供投放、维度与结构放置、结构管理操作；creature 负责生成实体的玩法初始化。`Weidubianhuan` 中按 `gamemoshi` 恢复游戏模式的分支归 dungeon。

按来源实际触发方式迁移生成。固定坐标目录、管理工具、传送器和场景清理不能被自然生成配置替代。目标具体 Codec/放置 API 在目标构建源码就绪后锁定，不能凭其他版本签名编写。

## 验证与裁定

保存/加载、多人进入、死亡/离场、跨维度返回和场景卸载分别检查。主世界与维度数据不能用英文类名推断。是否承诺 1.18.2 世界存档转换仍待负责人决定；保留资源 ID 本身不提供完整存档升级能力。
